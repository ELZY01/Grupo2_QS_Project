package com.studymatch.security;

import com.jayway.jsonpath.JsonPath;
import com.studymatch.model.Competency;
import com.studymatch.repository.CompetencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Grupos de uma UC com o backend completo (H2), do login ao grupo editado (Issue #106).
 * O docente de teste ("docente") leciona a UC QS-TESTE criada pelo DataSeeder.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TeacherGroupFlowTest {

    /** Números de estudante próprios, para não colidir com os outros testes que usam a mesma H2. */
    private static final AtomicInteger SEQ = new AtomicInteger(800);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CompetencyRepository competencies;

    private String admin;
    private String teacher;
    private int unit;

    @BeforeEach
    void setUp() throws Exception {
        // As competências vêm do V2 no Supabase; na H2 de testes são criadas aqui.
        if (competencies.count() == 0) {
            competencies.saveAll(List.of(
                    new Competency("ORG", "Organização", "", true),
                    new Competency("TEMPO", "Gestão do tempo", "", true),
                    new Competency("PROG", "Programação", "", true),
                    new Competency("EQUIPA", "Trabalho em equipa", "", true)));
        }
        admin = login("admin", "studymatch");
        teacher = login("docente", "docente123");
        unit = testCourseUnitId();
    }

    @Test
    void aTeacherCreatesGeneratesEditsAndDeletesTheGroupsOfTheirUnit() throws Exception {
        List<Integer> ids = enrolledStudents(9);
        String base = "/api/teacher/course-units/" + unit + "/group-contexts";

        // US10: contexto na UC com os 9 alunos elegíveis.
        String created = mvc.perform(json(auth(post(base), teacher),
                        "{\"name\":\"Projeto final\",\"studentIds\":" + ids + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseUnitId").value(unit))
                .andExpect(jsonPath("$.studentCount").value(9))
                .andReturn().getResponse().getContentAsString();
        int context = JsonPath.read(created, "$.id");
        String groupsUrl = base + "/" + context + "/groups";

        mvc.perform(auth(get(base), teacher))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + context + ")].name").value("Projeto final"));

        // US11: 9 alunos → um grupo de 5 e um de 4, com as estatísticas de cada um (R6).
        String generated = mvc.perform(auth(post(base + "/" + context + "/generate"), teacher))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generationNumber").value(1))
                .andExpect(jsonPath("$.groups", hasSize(2)))
                .andExpect(jsonPath("$.groups[0].memberCount").value(5))
                .andExpect(jsonPath("$.groups[1].memberCount").value(4))
                .andExpect(jsonPath("$.groups[0].areas", hasSize(4)))
                .andExpect(jsonPath("$.unassigned", hasSize(0)))
                .andReturn().getResponse().getContentAsString();
        int bigGroup = JsonPath.read(generated, "$.groups[0].id");
        int smallGroup = JsonPath.read(generated, "$.groups[1].id");
        List<Integer> big = JsonPath.read(generated, "$.groups[0].members[*].studentId");
        List<Integer> small = JsonPath.read(generated, "$.groups[1].members[*].studentId");

        // R5: um aluno do grupo grande não pode entrar também no pequeno.
        mvc.perform(json(auth(put(groupsUrl + "/" + smallGroup), teacher), members(small, big.get(0))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("já está no grupo")));

        // Mudar um aluno de grupo: sai do grupo de 5 (fica sem grupo) e entra no de 4.
        Integer moved = big.get(0);
        mvc.perform(json(auth(put(groupsUrl + "/" + bigGroup), teacher), members(big.subList(1, 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups[0].memberCount").value(4))
                .andExpect(jsonPath("$.unassigned[0].studentId").value(moved));
        mvc.perform(json(auth(put(groupsUrl + "/" + smallGroup), teacher), members(small, moved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups[1].memberCount").value(5))
                .andExpect(jsonPath("$.unassigned", hasSize(0)));

        // Apagar o grupo de 5 deixa os seus alunos sem grupo; com eles cria-se um grupo novo.
        mvc.perform(auth(delete(groupsUrl + "/" + smallGroup), teacher))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups", hasSize(1)))
                .andExpect(jsonPath("$.unassigned", hasSize(5)));
        List<Integer> freed = new ArrayList<>(small);
        freed.add(moved);
        mvc.perform(json(auth(post(groupsUrl), teacher), members(freed)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groups", hasSize(2)))
                // O nº do grupo apagado (2) fica livre: o novo é o maior que sobra (1) + 1.
                .andExpect(jsonPath("$.groups[1].number").value(2))
                .andExpect(jsonPath("$.unassigned", hasSize(0)));

        // As médias guardadas acompanham a alteração (o endpoint do admin mostra o mesmo).
        String teacherView = mvc.perform(auth(get(groupsUrl), teacher))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String adminView = mvc.perform(auth(get("/api/group-contexts/" + context + "/groups"), admin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Double> teacherAverages = JsonPath.read(teacherView, "$.groups[*].average");
        List<Double> adminAverages = JsonPath.read(adminView, "$[?(@.generationNumber == 1)].average");
        org.junit.jupiter.api.Assertions.assertEquals(teacherAverages, adminAverages);
    }

    @Test
    void aStudentNotEnrolledInTheUnitCannotJoinAContext() throws Exception {
        List<Integer> ids = enrolledStudents(4);
        int outsider = register();

        List<Integer> withOutsider = new ArrayList<>(ids);
        withOutsider.add(outsider);
        mvc.perform(json(auth(post("/api/teacher/course-units/" + unit + "/group-contexts"), teacher),
                        "{\"name\":\"Projeto\",\"studentIds\":" + withOutsider + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("O aluno " + outsider + " não está inscrito nesta UC."));
    }

    @Test
    void anImpossibleNumberOfStudentsGives400() throws Exception {
        List<Integer> ids = enrolledStudents(6);
        String base = "/api/teacher/course-units/" + unit + "/group-contexts";
        String created = mvc.perform(json(auth(post(base), teacher),
                        "{\"name\":\"Seis alunos\",\"studentIds\":" + ids + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int context = JsonPath.read(created, "$.id");

        mvc.perform(auth(post(base + "/" + context + "/generate"), teacher))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("6")));
    }

    @Test
    void aTeacherCannotUseTheGroupsWithoutUnit() throws Exception {
        // R4: /api/group-contexts não confirma a UC, por isso é só do admin.
        mvc.perform(auth(get("/api/group-contexts"), teacher)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/group-contexts"), admin)).andExpect(status().isOk());
    }

    // --- Ajudas -----------------------------------------------------------

    /** Regista {@code n} alunos e inscreve-os na UC de teste (como o admin faz). */
    private List<Integer> enrolledStudents(int n) throws Exception {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int id = register();
            mvc.perform(json(auth(post("/api/admin/course-units/" + unit + "/students"), admin),
                            "{\"id\":" + id + "}"))
                    .andExpect(status().isOk());
            ids.add(id);
        }
        return ids;
    }

    private int register() throws Exception {
        int n = SEQ.incrementAndGet();
        String body = mvc.perform(json(post("/api/auth/register"), """
                        {"studentNumber":"g%d","name":"Aluno G%d","email":"grupo%d@ulusofona.pt",
                         "course":"LEI","academicYear":2,"password":"segredo123"}
                        """.formatted(n, n, n)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.studentId");
    }

    private static String members(List<Integer> ids, Integer... more) {
        List<Integer> all = new ArrayList<>(ids);
        all.addAll(List.of(more));
        return "{\"studentIds\":[" + all.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]}";
    }

    private int testCourseUnitId() throws Exception {
        String body = mvc.perform(auth(get("/api/course-units"), admin))
                .andReturn().getResponse().getContentAsString();
        List<Integer> ids = JsonPath.read(body, "$[?(@.code == 'QS-TESTE')].id");
        return ids.get(0);
    }

    private String login(String username, String password) throws Exception {
        String body = mvc.perform(json(post("/api/auth/login"),
                        "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
