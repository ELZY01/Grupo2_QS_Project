package com.studymatch.security;

import com.jayway.jsonpath.JsonPath;
import com.studymatch.model.CourseUnit;
import com.studymatch.repository.CourseUnitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Área do docente com o backend completo (H2): R3, R6 (por aluno) e R7.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TeacherAccessTest {

    private static final AtomicInteger SEQ = new AtomicInteger(800);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CourseUnitRepository courseUnits;

    @Test
    void theTeacherSeesTheirUnitTheEnrolledStudentsAndTheirStats() throws Exception {
        String admin = login("admin", "studymatch");
        String teacher = login("docente", "docente123");
        int unit = testUnit(admin);
        int ana = register();
        enroll(admin, unit, ana);

        mvc.perform(auth(get("/api/teacher/course-units"), teacher))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.courseUnit.code == 'QS-TESTE')]").exists());
        mvc.perform(auth(get("/api/teacher/course-units/" + unit + "/students"), teacher))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + ana + ")]").exists());

        // R6 por aluno: perfil e histórico (notas) do aluno inscrito.
        mvc.perform(auth(get("/api/students/" + ana + "/profile"), teacher)).andExpect(status().isOk());
        mvc.perform(auth(get("/api/students/" + ana + "/enrollments"), teacher)).andExpect(status().isOk());
        mvc.perform(auth(get("/api/students/" + ana), teacher)).andExpect(status().isOk());
    }

    @Test
    void theTeacherCannotSeeStudentsOutsideTheirUnits() throws Exception {
        String teacher = login("docente", "docente123");
        int rui = register();

        mvc.perform(auth(get("/api/students/" + rui + "/profile"), teacher)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/students/" + rui + "/enrollments"), teacher)).andExpect(status().isForbidden());
    }

    @Test
    void theTeacherCannotOpenAnotherTeachersUnit() throws Exception {
        String teacher = login("docente", "docente123");
        int n = SEQ.incrementAndGet();
        CourseUnit other = courseUnits.save(new CourseUnit("OUTRA-" + n, "Outra UC", 6, 1, 1));

        mvc.perform(auth(get("/api/teacher/course-units/" + other.getId() + "/students"), teacher))
                .andExpect(status().isForbidden())
                .andExpect(status().reason("Esta UC não te está atribuída."));
        mvc.perform(auth(get("/api/teacher/course-units/999999/students"), teacher))
                .andExpect(status().isNotFound());
    }

    @Test
    void aTeacherWithoutUnitsSeesNothing() throws Exception {
        String admin = login("admin", "studymatch");
        int unit = testUnit(admin);
        int ana = register();
        enroll(admin, unit, ana);

        int n = SEQ.incrementAndGet();
        mvc.perform(json(auth(post("/api/admin/teachers"), admin),
                        "{\"username\":\"prof" + n + "\",\"displayName\":\"Prof " + n + "\",\"password\":\"segredo123\"}"))
                .andExpect(status().isCreated());
        String newTeacher = login("prof" + n, "segredo123");

        mvc.perform(auth(get("/api/teacher/course-units"), newTeacher))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(auth(get("/api/teacher/course-units/" + unit + "/students"), newTeacher))
                .andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/students/" + ana + "/profile"), newTeacher)).andExpect(status().isForbidden());
    }

    @Test
    void studentsAndAdminsCannotUseTheTeacherArea() throws Exception {
        String admin = login("admin", "studymatch");
        mvc.perform(auth(get("/api/teacher/course-units"), admin)).andExpect(status().isForbidden());
        mvc.perform(get("/api/teacher/course-units")).andExpect(status().isUnauthorized());
    }

    // --- Ajudas -----------------------------------------------------------

    private int testUnit(String admin) throws Exception {
        String body = mvc.perform(auth(get("/api/course-units"), admin)).andReturn().getResponse().getContentAsString();
        List<Integer> ids = JsonPath.read(body, "$[?(@.code == 'QS-TESTE')].id");
        return ids.get(0);
    }

    private void enroll(String admin, int unit, int student) throws Exception {
        mvc.perform(json(auth(post("/api/admin/course-units/" + unit + "/students"), admin), "{\"id\":" + student + "}"))
                .andExpect(status().isOk());
    }

    private int register() throws Exception {
        int n = SEQ.incrementAndGet();
        String body = mvc.perform(json(post("/api/auth/register"), """
                        {"studentNumber":"d%d","name":"Aluno %d","email":"doc%d@ulusofona.pt",
                         "course":"LEI","academicYear":2,"password":"segredo123"}
                        """.formatted(n, n, n)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.studentId");
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
