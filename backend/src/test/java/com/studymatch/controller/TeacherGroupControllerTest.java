package com.studymatch.controller;

import com.studymatch.config.SecurityConfig;
import com.studymatch.dto.GeneratedGroupResponse.GroupStudentResponse;
import com.studymatch.dto.TeacherGroupContextResponse;
import com.studymatch.dto.TeacherGroupsResponse;
import com.studymatch.grouping.AreaStatistics;
import com.studymatch.grouping.GroupAccessDeniedException;
import com.studymatch.grouping.GroupMembershipConflictException;
import com.studymatch.grouping.ImpossibleGroupConfigurationException;
import com.studymatch.model.Role;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.security.AuthenticatedUser;
import com.studymatch.security.StudentAccess;
import com.studymatch.security.TokenService;
import com.studymatch.service.TeacherGroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Camada web dos grupos de uma UC (Issue #106): rotas, permissões e códigos de erro.
 * O service é simulado.
 */
@WebMvcTest(TeacherGroupController.class)
@Import(SecurityConfig.class)
class TeacherGroupControllerTest {

    private static final String BASE = "/api/teacher/course-units/10/group-contexts";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeacherGroupService service;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AppUserRepository userRepository;

    @MockitoBean
    private StudentAccess studentAccess;

    @Test
    void withoutASessionGives401() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void studentsCannotManageGroups() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void theAdminUsesTheGroupsWithoutUnitAndNotThisRoute() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    void listsTheContextsOfTheUnit() throws Exception {
        when(service.contexts(1L, 10L)).thenReturn(List.of(
                new TeacherGroupContextResponse(20L, 10L, "Projeto", 9, null, null, 1)));

        mockMvc.perform(asTeacher(get(BASE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Projeto"))
                .andExpect(jsonPath("$[0].studentCount").value(9))
                .andExpect(jsonPath("$[0].generationNumber").value(1));
    }

    @Test
    void createsAContextWith201() throws Exception {
        when(service.createContext(eq(1L), eq(10L), any()))
                .thenReturn(new TeacherGroupContextResponse(21L, 10L, "Projeto", 4, null, null, null));

        mockMvc.perform(json(asTeacher(post(BASE)), "{\"name\":\"Projeto\",\"studentIds\":[1,2,3,4]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(21));
    }

    @Test
    void returnsTheGroupsWithTheirStatistics() throws Exception {
        when(service.groups(1L, 10L, 20L)).thenReturn(sample());

        mockMvc.perform(asTeacher(get(BASE + "/20/groups")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxAverageGap").value(0.5))
                .andExpect(jsonPath("$.groups[0].memberCount").value(4))
                .andExpect(jsonPath("$.groups[0].gaps[0]").value("PROG"))
                .andExpect(jsonPath("$.groups[0].areas[0].competencyCode").value("ORG"))
                .andExpect(jsonPath("$.groups[0].areas[0].distribution['4']").value(4))
                .andExpect(jsonPath("$.groups[0].strongestByArea[0].studentName").value("Ana"))
                .andExpect(jsonPath("$.unassigned[0].name").value("Rui"));
    }

    @Test
    void generatesCreatesEditsAndDeletesGroups() throws Exception {
        when(service.generate(1L, 10L, 20L)).thenReturn(sample());
        when(service.createGroup(eq(1L), eq(10L), eq(20L), any())).thenReturn(sample());
        when(service.updateGroup(eq(1L), eq(10L), eq(20L), eq(31L), any())).thenReturn(sample());
        when(service.deleteGroup(1L, 10L, 20L, 31L)).thenReturn(sample());
        String body = "{\"studentIds\":[1,2,3,4]}";

        mockMvc.perform(asTeacher(post(BASE + "/20/generate"))).andExpect(status().isOk());
        mockMvc.perform(json(asTeacher(post(BASE + "/20/groups")), body)).andExpect(status().isCreated());
        mockMvc.perform(json(asTeacher(put(BASE + "/20/groups/31")), body)).andExpect(status().isOk());
        mockMvc.perform(asTeacher(delete(BASE + "/20/groups/31"))).andExpect(status().isOk());

        verify(service).deleteGroup(1L, 10L, 20L, 31L);
    }

    @Test
    void aUnitOfAnotherTeacherGives403WithTheMessage() throws Exception {
        when(service.contexts(1L, 10L))
                .thenThrow(new GroupAccessDeniedException("Só pode gerir grupos das suas unidades curriculares."));

        mockMvc.perform(asTeacher(get(BASE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Só pode gerir grupos das suas unidades curriculares."));
    }

    @Test
    void aBrokenMembershipRuleGives409WithTheMessage() throws Exception {
        when(service.updateGroup(eq(1L), eq(10L), eq(20L), eq(31L), any()))
                .thenThrow(new GroupMembershipConflictException("O aluno 4 já está no grupo 1 deste contexto."));

        mockMvc.perform(json(asTeacher(put(BASE + "/20/groups/31")), "{\"studentIds\":[4,5,6,7]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("O aluno 4 já está no grupo 1 deste contexto."));
    }

    @Test
    void anImpossibleConfigurationGives400WithTheMessage() throws Exception {
        when(service.generate(1L, 10L, 20L)).thenThrow(new ImpossibleGroupConfigurationException(
                "Com 6 alunos não é possível formar grupos de 4 ou 5."));

        mockMvc.perform(asTeacher(post(BASE + "/20/generate")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Com 6 alunos não é possível formar grupos de 4 ou 5."));
    }

    // ---------------------------------------------------------------------

    private static TeacherGroupsResponse sample() {
        AreaStatistics org = new AreaStatistics("ORG", 4.0, 4.0, 4.0, 4, 4, Map.of(1, 0, 2, 0, 3, 0, 4, 4, 5, 0));
        var group = new TeacherGroupsResponse.GroupView(31L, 1, 4, 3.0, 0.0, true, List.of("PROG"), List.of(org),
                List.of(new GroupStudentResponse(1L, "a1", "Ana")),
                List.of(new TeacherGroupsResponse.Strongest("ORG", "Ana")));
        return new TeacherGroupsResponse(20L, "Projeto", 1, 0.5, 0.0, true, List.of(group),
                List.of(new GroupStudentResponse(9L, "a9", "Rui")));
    }

    /** Pedido como o docente com o ID 1 (mesmo padrão do TeacherControllerTest). */
    private static MockHttpServletRequestBuilder asTeacher(MockHttpServletRequestBuilder request) {
        AuthenticatedUser user = new AuthenticatedUser(1L, "docente", Role.DOCENTE, null, "Docente");
        return request.with(authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_DOCENTE")))));
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
