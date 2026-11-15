package com.studymatch.security;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de acesso com o backend completo (H2): quem pode chamar o quê (R1, R7, RN05, RN16).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAccessTest {

    private static final AtomicInteger SEQ = new AtomicInteger(500);

    @Autowired
    private MockMvc mvc;

    // --- Sem sessão -------------------------------------------------------

    @Test
    void healthIsPublic() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk());
    }

    @Test
    void protectedEndpointsNeedASession() throws Exception {
        mvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Tens de iniciar sessão."));
        mvc.perform(get("/api/course-units")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/quiz")).andExpect(status().isUnauthorized());
    }

    @Test
    void anInvalidTokenIsTreatedAsNoSession() throws Exception {
        mvc.perform(get("/api/students").header("Authorization", "Bearer inventado"))
                .andExpect(status().isUnauthorized());
    }

    // --- Login ------------------------------------------------------------

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(json(post("/api/auth/login"), "{\"username\":\"admin\",\"password\":\"errada123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Utilizador ou palavra-passe errados."));
    }

    @Test
    void adminManagesStudents() throws Exception {
        String admin = login("admin", "studymatch");

        mvc.perform(auth(get("/api/students"), admin)).andExpect(status().isOk());
        mvc.perform(auth(get("/api/auth/me"), admin)).andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void logoutInvalidatesTheToken() throws Exception {
        String admin = login("admin", "studymatch");

        mvc.perform(auth(post("/api/auth/logout"), admin)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/auth/me"), admin)).andExpect(status().isUnauthorized());
    }

    // --- Aluno ------------------------------------------------------------

    @Test
    void aStudentOnlySeesTheirOwnData() throws Exception {
        Registered ana = register();
        Registered rui = register();

        mvc.perform(auth(get("/api/students/" + ana.id), ana.token)).andExpect(status().isOk());
        mvc.perform(auth(get("/api/students/" + ana.id + "/enrollments"), ana.token)).andExpect(status().isOk());

        mvc.perform(auth(get("/api/students/" + rui.id), ana.token)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/students/" + rui.id + "/enrollments"), ana.token)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/students/" + rui.id + "/profile"), ana.token)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/students"), ana.token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Não tens permissão para isto."));
    }

    @Test
    void aStudentCannotChangeEnrollmentsOrOtherStudents() throws Exception {
        Registered ana = register();
        Registered rui = register();

        mvc.perform(json(auth(post("/api/students/" + ana.id + "/enrollments"), ana.token), "{}"))
                .andExpect(status().isForbidden());
        mvc.perform(json(auth(put("/api/students/" + rui.id), ana.token), "{}"))
                .andExpect(status().isForbidden());
        mvc.perform(json(auth(post("/api/students/" + rui.id + "/quiz-submissions"), ana.token), "{}"))
                .andExpect(status().isForbidden());
        mvc.perform(json(auth(put("/api/admin/students/" + ana.id + "/password"), ana.token),
                        "{\"password\":\"hacked1234\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void aStudentCanLogInWithTheEmail() throws Exception {
        Registered ana = register();

        mvc.perform(json(post("/api/auth/login"),
                        "{\"username\":\"" + ana.email + "\",\"password\":\"segredo123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ALUNO"))
                .andExpect(jsonPath("$.studentId").value(ana.id));
    }

    @Test
    void registrationNeedsAPasswordWithAtLeast8Characters() throws Exception {
        int n = SEQ.incrementAndGet();
        mvc.perform(json(post("/api/auth/register"), registerBody(n, "curta")))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("A palavra-passe tem de ter pelo menos 8 caracteres."));
    }

    // --- Admin define palavra-passe -----------------------------------------

    @Test
    void adminResetsAStudentPassword() throws Exception {
        Registered ana = register();
        String admin = login("admin", "studymatch");

        mvc.perform(json(auth(put("/api/admin/students/" + ana.id + "/password"), admin),
                        "{\"password\":\"novaPass123\"}"))
                .andExpect(status().isNoContent());

        login(ana.number, "novaPass123");
        mvc.perform(json(post("/api/auth/login"),
                        "{\"username\":\"" + ana.number + "\",\"password\":\"segredo123\"}"))
                .andExpect(status().isUnauthorized());
    }

    // --- Docente ----------------------------------------------------------

    @Test
    void aTeacherCannotUseTheAdminAreaNorSeeStudentsOutsideTheirUnits() throws Exception {
        Registered ana = register();
        String teacher = login("docente", "docente123");

        mvc.perform(auth(get("/api/students"), teacher)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/students/" + ana.id + "/profile"), teacher)).andExpect(status().isForbidden());
        mvc.perform(json(auth(put("/api/admin/students/" + ana.id + "/password"), teacher),
                        "{\"password\":\"hacked1234\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentsCannotUseTheTeacherArea() throws Exception {
        Registered ana = register();
        mvc.perform(auth(get("/api/teacher/course-units"), ana.token)).andExpect(status().isForbidden());
    }

    // --- R1/R2: atribuições às UCs ------------------------------------------

    @Test
    void onlyTheAdminEnrollsStudentsInCourseUnits() throws Exception {
        Registered ana = register();
        String admin = login("admin", "studymatch");
        String teacher = login("docente", "docente123");
        int unit = testCourseUnitId(admin);
        String body = "{\"id\":" + ana.id + "}";

        mvc.perform(json(auth(post("/api/admin/course-units/" + unit + "/students"), teacher), body))
                .andExpect(status().isForbidden());
        mvc.perform(json(auth(post("/api/admin/course-units/" + unit + "/students"), ana.token), body))
                .andExpect(status().isForbidden());

        mvc.perform(json(auth(post("/api/admin/course-units/" + unit + "/students"), admin), body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.students[?(@.id == " + ana.id + ")]").exists());
        mvc.perform(json(auth(post("/api/admin/course-units/" + unit + "/students"), admin), body))
                .andExpect(status().isConflict());
    }

    @Test
    void onlyTheAdminAssignsTeachers() throws Exception {
        String admin = login("admin", "studymatch");
        String teacher = login("docente", "docente123");
        int unit = testCourseUnitId(admin);

        mvc.perform(auth(get("/api/admin/teachers"), teacher)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/admin/course-units/" + unit + "/assignments"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teachers[0].username").value("docente"));
    }

    // --- Ajudas -----------------------------------------------------------

    private int testCourseUnitId(String adminToken) throws Exception {
        String body = mvc.perform(auth(get("/api/course-units"), adminToken))
                .andReturn().getResponse().getContentAsString();
        java.util.List<Integer> ids = JsonPath.read(body, "$[?(@.code == 'QS-TESTE')].id");
        return ids.get(0);
    }

    private record Registered(int id, String number, String email, String token) {
    }

    private Registered register() throws Exception {
        int n = SEQ.incrementAndGet();
        String body = mvc.perform(json(post("/api/auth/register"), registerBody(n, "segredo123")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(body, "$.studentId");
        return new Registered(id, "a" + n, "aluno" + n + "@ulusofona.pt", JsonPath.read(body, "$.token"));
    }

    private static String registerBody(int n, String password) {
        return """
                {"studentNumber":"a%d","name":"Aluno %d","email":"aluno%d@ulusofona.pt",
                 "course":"LEI","academicYear":2,"password":"%s"}
                """.formatted(n, n, n, password);
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
