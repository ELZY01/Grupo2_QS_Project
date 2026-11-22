
package com.studymatch.service;

import com.studymatch.dto.CreateGroupContextRequest;
import com.studymatch.dto.GeneratedGroupResponse;
import com.studymatch.dto.GroupContextResponse;
import com.studymatch.grouping.FormedGroup;
import com.studymatch.grouping.GroupCandidate;
import com.studymatch.grouping.GroupFormation;
import com.studymatch.grouping.GroupFormationResult;
import com.studymatch.model.GroupContext;
import com.studymatch.model.GroupContextStudent;
import com.studymatch.model.GroupMember;
import com.studymatch.model.Student;
import com.studymatch.model.StudentGroup;
import com.studymatch.profile.StudentProfile;
import com.studymatch.repository.CompetencyRepository;
import com.studymatch.repository.GroupContextRepository;
import com.studymatch.repository.GroupContextStudentRepository;
import com.studymatch.repository.GroupMemberRepository;
import com.studymatch.repository.StudentGroupRepository;
import com.studymatch.repository.StudentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GroupGenerationService {

    private final GroupContextRepository contextRepository;
    private final GroupContextStudentRepository contextStudentRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final StudentRepository studentRepository;
    private final CompetencyRepository competencyRepository;
    private final ProfileService profileService;
    private final GroupFormation groupFormation;

    public GroupGenerationService(
            GroupContextRepository contextRepository,
            GroupContextStudentRepository contextStudentRepository,
            StudentGroupRepository studentGroupRepository,
            GroupMemberRepository groupMemberRepository,
            StudentRepository studentRepository,
            CompetencyRepository competencyRepository,
            ProfileService profileService,
            GroupFormation groupFormation) {

        this.contextRepository = contextRepository;
        this.contextStudentRepository = contextStudentRepository;
        this.studentGroupRepository = studentGroupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.studentRepository = studentRepository;
        this.competencyRepository = competencyRepository;
        this.profileService = profileService;
        this.groupFormation = groupFormation;
    }

    /**
     * Cria um contexto e associa os estudantes selecionados.
     */
    @Transactional
    public GroupContextResponse createContext(CreateGroupContextRequest request) {

        if (request == null || request.name() == null
                || request.name().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome do contexto é obrigatório."
            );
        }

        if (request.studentIds() == null || request.studentIds().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "É necessário selecionar estudantes."
            );
        }

        Set<Long> uniqueIds = new HashSet<>(request.studentIds());

        if (uniqueIds.size() != request.studentIds().size()
                || uniqueIds.contains(null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A lista contém estudantes repetidos ou inválidos."
            );
        }

        GroupContext context = contextRepository.save(
                new GroupContext(request.name().trim())
        );

        List<GroupContextStudent> associations = new ArrayList<>();

        for (Long studentId : request.studentIds()) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Estudante não encontrado: " + studentId
                    ));

            associations.add(new GroupContextStudent(context, student));
        }

        contextStudentRepository.saveAll(associations);

        return toContextResponse(context);
    }

    /**
     * Lista todos os contextos e as respetivas gerações.
     */
    @Transactional(readOnly = true)
    public List<GroupContextResponse> getAllContexts() {

        return contextRepository.findAll()
                .stream()
                .map(this::toContextResponse)
                .toList();
    }

    /**
     * Gera grupos para um contexto e guarda uma nova geração.
     * As gerações anteriores são preservadas.
     */
    @Transactional
    public List<GeneratedGroupResponse> generateGroups(Long contextId) {

        GroupContext context = findContext(contextId);

        List<GroupContextStudent> associations =
                contextStudentRepository.findByContext_Id(contextId);

        if (associations.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O contexto não tem estudantes associados."
            );
        }

        List<String> competencyCodes = competencyRepository
                .findAllByOrderByIdAsc()
                .stream()
                .map(competency -> competency.getCode())
                .toList();

        if (competencyCodes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não existem competências configuradas."
            );
        }

        List<GroupCandidate> candidates = new ArrayList<>();
        Map<Long, Student> studentsById = new HashMap<>();

        for (GroupContextStudent association : associations) {

            Student student = association.getStudent();

            StudentProfile profile =
                    profileService.getStudentProfile(student.getId());

            candidates.add(GroupCandidate.fromProfile(
                    student.getId(),
                    student.getStudentNumber(),
                    student.getName(),
                    profile
            ));

            studentsById.put(student.getId(), student);
        }

        // O algoritmo valida se é possível formar grupos de 4 ou 5.
        GroupFormationResult result =
                groupFormation.form(competencyCodes, candidates);

        // Calcular o número da nova geração.
        List<StudentGroup> previousGroups =
                studentGroupRepository
                        .findByContext_IdOrderByGenerationNumberDescGroupNumberAsc(
                                contextId
                        );

        int generationNumber = previousGroups.isEmpty()
                ? 1
                : previousGroups.get(0).getGenerationNumber() + 1;

        List<StudentGroup> groupsToSave = new ArrayList<>();
        List<List<GroupCandidate>> candidatesByGroup = new ArrayList<>();

        for (FormedGroup formedGroup : result.groups()) {

            StudentGroup group = new StudentGroup(
                    context,
                    generationNumber,
                    formedGroup.number(),
                    formedGroup.average(),
                    formedGroup.gapToOthers()
            );

            groupsToSave.add(group);
            candidatesByGroup.add(formedGroup.members());
        }

        List<StudentGroup> savedGroups =
                studentGroupRepository.saveAll(groupsToSave);

        List<GroupMember> membersToSave = new ArrayList<>();

        for (int i = 0; i < savedGroups.size(); i++) {

            StudentGroup savedGroup = savedGroups.get(i);

            for (GroupCandidate candidate : candidatesByGroup.get(i)) {

                Student student = studentsById.get(candidate.studentId());

                membersToSave.add(new GroupMember(
                        savedGroup,
                        student,
                        contextId,
                        generationNumber
                ));
            }
        }

        groupMemberRepository.saveAll(membersToSave);

        context.setGeneratedAt(LocalDateTime.now());
        contextRepository.save(context);

        return getGenerationResponse(contextId, generationNumber);
    }

    /**
     * Consulta os grupos e membros de todas as gerações de um contexto.
     */
    @Transactional(readOnly = true)
    public List<GeneratedGroupResponse> getGroups(Long contextId) {

        findContext(contextId);

        List<StudentGroup> groups =
                studentGroupRepository
                        .findByContext_IdOrderByGenerationNumberDescGroupNumberAsc(
                                contextId
                        );

        return groups.stream()
                .map(this::toGroupResponse)
                .toList();
    }

    /**
     * Procura um contexto ou devolve 404.
     */
    private GroupContext findContext(Long contextId) {

        return contextRepository.findById(contextId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Contexto de grupos não encontrado."
                ));
    }

    /**
     * Constrói a resposta de um contexto.
     */
    private GroupContextResponse toContextResponse(GroupContext context) {

        List<StudentGroup> groups =
                studentGroupRepository
                        .findByContext_IdOrderByGenerationNumberDescGroupNumberAsc(
                                context.getId()
                        );

        List<Integer> generations = groups.stream()
                .map(StudentGroup::getGenerationNumber)
                .distinct()
                .toList();

        return new GroupContextResponse(
                context.getId(),
                context.getName(),
                context.getCreatedAt(),
                context.getGeneratedAt(),
                generations
        );
    }

    /**
     * Constrói a resposta de uma geração.
     */
    private List<GeneratedGroupResponse> getGenerationResponse(
            Long contextId,
            Integer generationNumber) {

        List<StudentGroup> groups =
                studentGroupRepository
                        .findByContext_IdAndGenerationNumberOrderByGroupNumberAsc(
                                contextId,
                                generationNumber
                        );

        return groups.stream()
                .map(this::toGroupResponse)
                .toList();
    }

    /**
     * Constrói a resposta de um grupo com os respetivos estudantes.
     */
    private GeneratedGroupResponse toGroupResponse(StudentGroup group) {

        List<GeneratedGroupResponse.GroupStudentResponse> members =
                groupMemberRepository
                        .findByGroup_IdOrderByStudent_StudentNumberAsc(
                                group.getId()
                        )
                        .stream()
                        .map(member ->
                                new GeneratedGroupResponse.GroupStudentResponse(
                                        member.getStudent().getId(),
                                        member.getStudent().getStudentNumber(),
                                        member.getStudent().getName()
                                ))
                        .toList();

        return new GeneratedGroupResponse(
                group.getId(),
                group.getGenerationNumber(),
                group.getGroupNumber(),
                group.getAverage(),
                group.getGapToOthers(),
                members
        );
    }
}
