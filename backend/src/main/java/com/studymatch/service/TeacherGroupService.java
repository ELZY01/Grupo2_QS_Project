package com.studymatch.service;

import com.studymatch.dto.CreateGroupContextRequest;
import com.studymatch.dto.GeneratedGroupResponse.GroupStudentResponse;
import com.studymatch.dto.GroupMembersRequest;
import com.studymatch.dto.TeacherGroupContextResponse;
import com.studymatch.dto.TeacherGroupsResponse;
import com.studymatch.grouping.GroupCandidate;
import com.studymatch.grouping.GroupFormation;
import com.studymatch.grouping.GroupMembershipRules;
import com.studymatch.grouping.GroupSizeLimits;
import com.studymatch.grouping.GroupStatistics;
import com.studymatch.model.Competency;
import com.studymatch.model.CourseUnitStudent;
import com.studymatch.model.GroupContext;
import com.studymatch.model.GroupContextStudent;
import com.studymatch.model.GroupMember;
import com.studymatch.model.Student;
import com.studymatch.model.StudentGroup;
import com.studymatch.repository.CompetencyRepository;
import com.studymatch.repository.CourseUnitRepository;
import com.studymatch.repository.CourseUnitStudentRepository;
import com.studymatch.repository.CourseUnitTeacherRepository;
import com.studymatch.repository.GroupContextRepository;
import com.studymatch.repository.GroupContextStudentRepository;
import com.studymatch.repository.GroupMemberRepository;
import com.studymatch.repository.StudentGroupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Grupos de uma UC na área do docente (Issue #106).
 *
 * <ul>
 *   <li>R4: o docente só gere contextos e grupos das suas UCs, e só com alunos inscritos nelas.</li>
 *   <li>R5: um aluno não pode estar em dois grupos do mesmo contexto; grupos de 4 a 5 alunos.</li>
 *   <li>R6: cada resposta traz as estatísticas de cada grupo.</li>
 * </ul>
 *
 * <p>As regras estão em {@link GroupMembershipRules} e as estatísticas em {@link GroupStatistics}
 * (Java puro). Este service só vai buscar os dados aos repositórios e chama essas classes.
 * A geração automática continua no {@link GroupGenerationService} (#69).
 *
 * <p>O docente vê e edita a geração mais recente do contexto (a "atual").
 */
@Service
public class TeacherGroupService {

    private static final double EPSILON = 1e-9;

    private final CourseUnitRepository courseUnits;
    private final CourseUnitTeacherRepository teachers;
    private final CourseUnitStudentRepository enrolled;
    private final GroupContextRepository contexts;
    private final GroupContextStudentRepository contextStudents;
    private final StudentGroupRepository groups;
    private final GroupMemberRepository members;
    private final CompetencyRepository competencies;
    private final ProfileService profiles;
    private final GroupGenerationService generation;

    public TeacherGroupService(CourseUnitRepository courseUnits,
                               CourseUnitTeacherRepository teachers,
                               CourseUnitStudentRepository enrolled,
                               GroupContextRepository contexts,
                               GroupContextStudentRepository contextStudents,
                               StudentGroupRepository groups,
                               GroupMemberRepository members,
                               CompetencyRepository competencies,
                               ProfileService profiles,
                               GroupGenerationService generation) {
        this.courseUnits = courseUnits;
        this.teachers = teachers;
        this.enrolled = enrolled;
        this.contexts = contexts;
        this.contextStudents = contextStudents;
        this.groups = groups;
        this.members = members;
        this.competencies = competencies;
        this.profiles = profiles;
        this.generation = generation;
    }

    /**
     * Contextos de uma UC do docente, do mais recente para o mais antigo.
     */
    @Transactional(readOnly = true)
    public List<TeacherGroupContextResponse> contexts(Long teacherId, Long courseUnitId) {
        requireTeacher(teacherId, courseUnitId);
        return contexts.findByCourseUnitIdOrderByCreatedAtDescIdDesc(courseUnitId).stream()
                .map(context -> toContextResponse(context,
                        contextStudents.findByContext_Id(context.getId()).size()))
                .toList();
    }

    /**
     * US10: cria um contexto na UC com os alunos elegíveis (todos inscritos na UC).
     */
    @Transactional
    public TeacherGroupContextResponse createContext(Long teacherId, Long courseUnitId,
                                                     CreateGroupContextRequest request) {
        requireTeacher(teacherId, courseUnitId);
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome da atividade é obrigatório.");
        }
        if (request.studentIds() == null || request.studentIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escolhe os alunos elegíveis.");
        }

        Map<Long, Student> enrolledStudents = enrolledStudents(courseUnitId);
        GroupMembershipRules.requireNoRepeated(request.studentIds());
        GroupMembershipRules.requireEnrolled(request.studentIds(), enrolledStudents.keySet());

        GroupContext context = contexts.save(new GroupContext(request.name().trim(), courseUnitId));
        contextStudents.saveAll(request.studentIds().stream()
                .map(id -> new GroupContextStudent(context, enrolledStudents.get(id)))
                .toList());
        return toContextResponse(context, request.studentIds().size());
    }

    /**
     * US11: gera uma nova geração com o algoritmo (#69/#71) e devolve-a com as estatísticas.
     * Configuração impossível → {@code ImpossibleGroupConfigurationException} (400).
     */
    @Transactional
    public TeacherGroupsResponse generate(Long teacherId, Long courseUnitId, Long contextId) {
        GroupContext context = findContext(teacherId, courseUnitId, contextId);
        generation.generateGroups(contextId);
        return currentGroups(context, false);
    }

    /**
     * Grupos atuais do contexto com as estatísticas (R6).
     */
    @Transactional(readOnly = true)
    public TeacherGroupsResponse groups(Long teacherId, Long courseUnitId, Long contextId) {
        return currentGroups(findContext(teacherId, courseUnitId, contextId), false);
    }

    /**
     * Cria um grupo à mão na geração atual (R4, R5). Sem geração, começa a 1.ª.
     */
    @Transactional
    public TeacherGroupsResponse createGroup(Long teacherId, Long courseUnitId, Long contextId,
                                             GroupMembersRequest request) {
        GroupContext context = findContext(teacherId, courseUnitId, contextId);
        List<Long> studentIds = studentIds(request);
        Integer current = currentGeneration(contextId);
        int generationNumber = current == null ? 1 : current;

        Map<Long, Student> enrolledStudents = enrolledStudents(courseUnitId);
        GroupMembershipRules.validateMembers(studentIds, enrolledStudents.keySet(),
                groupOfStudent(contextId, generationNumber), null, GroupSizeLimits.DEFAULT);
        addToContextIfMissing(context, studentIds, enrolledStudents);

        int number = groups.findByContext_IdAndGenerationNumberOrderByGroupNumberAsc(contextId, generationNumber)
                .stream()
                .mapToInt(StudentGroup::getGroupNumber)
                .max()
                .orElse(0) + 1;
        StudentGroup group = groups.save(new StudentGroup(context, generationNumber, number, null, null));
        members.saveAll(studentIds.stream()
                .map(id -> new GroupMember(group, enrolledStudents.get(id), contextId, generationNumber))
                .toList());

        return currentGroups(context, true);
    }

    /**
     * Muda os membros de um grupo da geração atual (R4, R5).
     * Os alunos que saem ficam sem grupo; os que entram não podem estar noutro grupo.
     */
    @Transactional
    public TeacherGroupsResponse updateGroup(Long teacherId, Long courseUnitId, Long contextId, Long groupId,
                                             GroupMembersRequest request) {
        GroupContext context = findContext(teacherId, courseUnitId, contextId);
        StudentGroup group = findCurrentGroup(contextId, groupId);
        List<Long> studentIds = studentIds(request);
        int generationNumber = group.getGenerationNumber();

        Map<Long, Student> enrolledStudents = enrolledStudents(courseUnitId);
        GroupMembershipRules.validateMembers(studentIds, enrolledStudents.keySet(),
                groupOfStudent(contextId, generationNumber), group.getGroupNumber(), GroupSizeLimits.DEFAULT);
        addToContextIfMissing(context, studentIds, enrolledStudents);

        // Só se apaga quem sai e só se insere quem entra: os que ficam não mudam,
        // por isso as restrições únicas da tabela group_members nunca colidem.
        List<GroupMember> currentMembers = members.findByGroup_IdOrderByStudent_StudentNumberAsc(groupId);
        Set<Long> wanted = new HashSet<>(studentIds);
        Set<Long> present = currentMembers.stream()
                .map(member -> member.getStudent().getId())
                .collect(Collectors.toSet());

        members.deleteAll(currentMembers.stream()
                .filter(member -> !wanted.contains(member.getStudent().getId()))
                .toList());
        members.saveAll(studentIds.stream()
                .filter(id -> !present.contains(id))
                .map(id -> new GroupMember(group, enrolledStudents.get(id), contextId, generationNumber))
                .toList());

        return currentGroups(context, true);
    }

    /**
     * Apaga um grupo da geração atual; os seus alunos ficam sem grupo.
     */
    @Transactional
    public TeacherGroupsResponse deleteGroup(Long teacherId, Long courseUnitId, Long contextId, Long groupId) {
        GroupContext context = findContext(teacherId, courseUnitId, contextId);
        StudentGroup group = findCurrentGroup(contextId, groupId);

        members.deleteAll(members.findByGroup_IdOrderByStudent_StudentNumberAsc(groupId));
        groups.delete(group);

        return currentGroups(context, true);
    }

    // ---------------------------------------------------------------------
    // Acesso (R4)
    // ---------------------------------------------------------------------

    /** UC inexistente → 404; UC de outro docente → {@code GroupAccessDeniedException} (403). */
    private void requireTeacher(Long teacherId, Long courseUnitId) {
        if (!courseUnits.existsById(courseUnitId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade curricular não encontrada.");
        }
        Set<Long> unitIds = teachers.findByTeacherIdOrderByCourseUnitNameAsc(teacherId).stream()
                .map(assignment -> assignment.getCourseUnit().getId())
                .collect(Collectors.toSet());
        GroupMembershipRules.requireTeacherOfUnit(unitIds, courseUnitId);
    }

    /** Contexto desta UC; um contexto de outra UC (ou sem UC) dá 404, como se não existisse. */
    private GroupContext findContext(Long teacherId, Long courseUnitId, Long contextId) {
        requireTeacher(teacherId, courseUnitId);
        return contexts.findByIdAndCourseUnitId(contextId, courseUnitId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Contexto de grupos não encontrado nesta UC."));
    }

    /** Grupo deste contexto e da geração atual; as gerações antigas ficam só para consulta. */
    private StudentGroup findCurrentGroup(Long contextId, Long groupId) {
        StudentGroup group = groups.findById(groupId)
                .filter(g -> g.getContext().getId().equals(contextId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Grupo não encontrado neste contexto."));
        if (!group.getGenerationNumber().equals(currentGeneration(contextId))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Só se podem alterar os grupos da geração mais recente.");
        }
        return group;
    }

    // ---------------------------------------------------------------------
    // Dados
    // ---------------------------------------------------------------------

    private static List<Long> studentIds(GroupMembersRequest request) {
        if (request == null || request.studentIds() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica os alunos do grupo.");
        }
        return request.studentIds();
    }

    /** Alunos inscritos na UC, por ID. */
    private Map<Long, Student> enrolledStudents(Long courseUnitId) {
        Map<Long, Student> result = new LinkedHashMap<>();
        for (CourseUnitStudent enrollment : enrolled.findByCourseUnitIdOrderByStudentNameAsc(courseUnitId)) {
            result.put(enrollment.getStudent().getId(), enrollment.getStudent());
        }
        return result;
    }

    /** Geração mais recente que tem grupos, ou {@code null}. */
    private Integer currentGeneration(Long contextId) {
        List<StudentGroup> all = groups.findByContext_IdOrderByGenerationNumberDescGroupNumberAsc(contextId);
        return all.isEmpty() ? null : all.get(0).getGenerationNumber();
    }

    /** Grupo atual de cada aluno nesta geração (aluno → nº do grupo), para a R5. */
    private Map<Long, Integer> groupOfStudent(Long contextId, int generationNumber) {
        Map<Long, Integer> result = new HashMap<>();
        for (GroupMember member : members.findByContextIdAndGenerationNumber(contextId, generationNumber)) {
            result.put(member.getStudent().getId(), member.getGroup().getGroupNumber());
        }
        return result;
    }

    /**
     * Um aluno inscrito na UC que ainda não era elegível no contexto passa a ser.
     * A base de dados obriga a isto: um membro tem de estar nos alunos do contexto.
     */
    private void addToContextIfMissing(GroupContext context, List<Long> studentIds,
                                       Map<Long, Student> enrolledStudents) {
        Set<Long> eligible = contextStudents.findByContext_Id(context.getId()).stream()
                .map(cs -> cs.getStudent().getId())
                .collect(Collectors.toSet());
        List<GroupContextStudent> missing = studentIds.stream()
                .filter(id -> !eligible.contains(id))
                .map(id -> new GroupContextStudent(context, enrolledStudents.get(id)))
                .toList();
        if (!missing.isEmpty()) {
            contextStudents.saveAll(missing);
        }
    }

    private List<String> competencyCodes() {
        List<String> codes = competencies.findAllByOrderByIdAsc().stream()
                .map(Competency::getCode)
                .toList();
        if (codes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não existem competências configuradas.");
        }
        return codes;
    }

    private GroupCandidate candidate(Student student) {
        return GroupCandidate.fromProfile(student.getId(), student.getStudentNumber(), student.getName(),
                profiles.getStudentProfile(student.getId()));
    }

    private static GroupStudentResponse toMember(GroupCandidate candidate) {
        return new GroupStudentResponse(candidate.studentId(), candidate.studentNumber(), candidate.name());
    }

    // ---------------------------------------------------------------------
    // Respostas
    // ---------------------------------------------------------------------

    private TeacherGroupContextResponse toContextResponse(GroupContext context, int studentCount) {
        return new TeacherGroupContextResponse(context.getId(), context.getCourseUnitId(), context.getName(),
                studentCount, context.getCreatedAt(), context.getGeneratedAt(), currentGeneration(context.getId()));
    }

    /**
     * Grupos da geração atual com as estatísticas (R6).
     *
     * @param saveAverages {@code true} depois de uma alteração à mão: guarda a média e a diferença
     *                     novas de cada grupo, para o endpoint do admin (#69) mostrar o mesmo
     */
    private TeacherGroupsResponse currentGroups(GroupContext context, boolean saveAverages) {
        Long contextId = context.getId();
        Integer generationNumber = currentGeneration(contextId);
        List<StudentGroup> list = generationNumber == null
                ? List.of()
                : groups.findByContext_IdAndGenerationNumberOrderByGroupNumberAsc(contextId, generationNumber);

        List<List<GroupCandidate>> candidates = new ArrayList<>();
        Set<Long> grouped = new HashSet<>();
        for (StudentGroup group : list) {
            List<GroupCandidate> groupCandidates = members.findByGroup_IdOrderByStudent_StudentNumberAsc(group.getId())
                    .stream()
                    .map(member -> candidate(member.getStudent()))
                    .toList();
            groupCandidates.forEach(c -> grouped.add(c.studentId()));
            candidates.add(groupCandidates);
        }

        List<GroupStudentResponse> unassigned = contextStudents.findByContext_Id(contextId).stream()
                .map(GroupContextStudent::getStudent)
                .filter(student -> !grouped.contains(student.getId()))
                .sorted(Comparator.comparing(Student::getStudentNumber))
                .map(student -> new GroupStudentResponse(student.getId(), student.getStudentNumber(),
                        student.getName()))
                .toList();

        if (list.isEmpty()) {
            return new TeacherGroupsResponse(contextId, context.getName(), generationNumber,
                    GroupFormation.MAX_AVERAGE_GAP, 0, true, List.of(), unassigned);
        }

        List<String> codes = competencyCodes();
        List<GroupStatistics> statistics = GroupStatistics.ofAll(codes, candidates);
        List<TeacherGroupsResponse.GroupView> views = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            StudentGroup group = list.get(i);
            GroupStatistics stats = statistics.get(i);
            if (saveAverages) {
                group.updateAverages(stats.average(), stats.gapToOthers());
            }
            List<TeacherGroupsResponse.Strongest> strongest = GroupStatistics
                    .strongestByArea(codes, candidates.get(i))
                    .entrySet().stream()
                    .map(e -> new TeacherGroupsResponse.Strongest(e.getKey(), e.getValue().name()))
                    .toList();
            views.add(new TeacherGroupsResponse.GroupView(group.getId(), group.getGroupNumber(),
                    stats.memberCount(), stats.average(), stats.gapToOthers(), stats.balanced(), stats.gaps(),
                    stats.areas(), candidates.get(i).stream().map(TeacherGroupService::toMember).toList(),
                    strongest));
        }
        if (saveAverages) {
            groups.saveAll(list);
        }

        double max = statistics.stream().mapToDouble(GroupStatistics::average).max().orElse(0);
        double min = statistics.stream().mapToDouble(GroupStatistics::average).min().orElse(0);
        double spread = max - min;
        return new TeacherGroupsResponse(contextId, context.getName(), generationNumber,
                GroupFormation.MAX_AVERAGE_GAP, spread, spread <= GroupFormation.MAX_AVERAGE_GAP + EPSILON,
                views, unassigned);
    }
}
