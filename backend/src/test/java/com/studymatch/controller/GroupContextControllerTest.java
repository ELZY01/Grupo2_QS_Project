
package com.studymatch.controller;

import com.studymatch.dto.GroupContextResponse;
import com.studymatch.dto.GeneratedGroupResponse;
import com.studymatch.service.GroupGenerationService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(GroupContextController.class)
class GroupContextControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupGenerationService groupGenerationService;

    @Test
    void shouldCreateContext() throws Exception {
        GroupContextResponse response =
                new GroupContextResponse(1L, "Turma A", null, null, List.of());

        when(groupGenerationService.createContext(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/group-contexts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Turma A\",\"studentIds\":[1,2]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Turma A"));

        verify(groupGenerationService).createContext(any());
    }

    @Test
    void shouldGetAllContexts() throws Exception {
        GroupContextResponse response =
                new GroupContextResponse(1L, "Turma A", null, null, List.of(1));

        when(groupGenerationService.getAllContexts())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/group-contexts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Turma A"));

        verify(groupGenerationService).getAllContexts();
    }

    @Test
    void shouldGenerateGroups() throws Exception {
        GeneratedGroupResponse response =
                new GeneratedGroupResponse(1L, 1, 1, 3.5, 0.2, List.of());

        when(groupGenerationService.generateGroups(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(post("/api/group-contexts/1/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].generationNumber").value(1));

        verify(groupGenerationService).generateGroups(1L);
    }

    @Test
    void shouldGetGroups() throws Exception {
        GeneratedGroupResponse response =
                new GeneratedGroupResponse(1L, 1, 1, 3.5, 0.2, List.of());

        when(groupGenerationService.getGroups(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/group-contexts/1/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].groupNumber").value(1));

        verify(groupGenerationService).getGroups(1L);
    }
}
