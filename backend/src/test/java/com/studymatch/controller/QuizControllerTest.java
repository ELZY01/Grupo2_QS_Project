package com.studymatch.controller;

import com.studymatch.config.SecurityConfig;
import com.studymatch.model.Question;
import com.studymatch.model.QuizSubmission;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.security.StudentAccess;
import com.studymatch.security.TokenService;
import com.studymatch.service.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizController.class)
@Import(SecurityConfig.class)
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizService quizService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AppUserRepository userRepository;

    @MockitoBean
    private StudentAccess studentAccess;

    // =========================================================================
    // GET /api/quiz
    // =========================================================================

    @Test
    void getQuiz_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/quiz"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(quizService);
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void getQuiz_whenQuestionsExist_returns200AndQuestions() throws Exception {
        // Mock da entidade Question usando Mockito para evitar problemas de setters
        Question q1 = mock(Question.class);
        when(q1.getId()).thenReturn(10L);
        when(q1.getText()).thenReturn("Gosto de liderar equipas");
        when(q1.getQuestionOrder()).thenReturn(1);

        when(quizService.getQuizQuestions(1L)).thenReturn(List.of(q1));

        mockMvc.perform(get("/api/quiz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].text").value("Gosto de liderar equipas"));

        verify(quizService).getQuizQuestions(1L);
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void getQuiz_whenNoQuestionsFound_returns404() throws Exception {
        when(quizService.getQuizQuestions(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/quiz"))
                .andExpect(status().isNotFound());

        verify(quizService).getQuizQuestions(1L);
    }

    // =========================================================================
    // POST /api/students/{studentId}/quiz-submissions
    // =========================================================================

    @Test
    void submitQuiz_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/students/5/quiz-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[]}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(quizService);
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void submitQuiz_whenNotSelf_returns403() throws Exception {
        when(studentAccess.isSelf(any(), eq(5L))).thenReturn(false);

        mockMvc.perform(post("/api/students/5/quiz-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[]}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(quizService);
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void submitQuiz_whenValidRequestAndDefaultQuizId_returns200() throws Exception {
        when(studentAccess.isSelf(any(), eq(5L))).thenReturn(true);

        // Mock da submissão devolvida pelo serviço
        QuizSubmission saved = mock(QuizSubmission.class);
        when(saved.getId()).thenReturn(99L);
        when(quizService.submitQuiz(eq(5L), eq(1L), any())).thenReturn(saved);

        String body = """
                {
                  "answers": [
                    {"questionId": 1, "value": 4}
                  ]
                }
                """;

        mockMvc.perform(post("/api/students/5/quiz-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));

        verify(quizService).submitQuiz(eq(5L), eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void submitQuiz_whenCustomQuizIdParam_passesQuizIdToService() throws Exception {
        when(studentAccess.isSelf(any(), eq(5L))).thenReturn(true);
        when(quizService.submitQuiz(eq(5L), eq(2L), any())).thenReturn(mock(QuizSubmission.class));

        mockMvc.perform(post("/api/students/5/quiz-submissions")
                        .param("quizId", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[]}"))
                .andExpect(status().isOk());

        verify(quizService).submitQuiz(eq(5L), eq(2L), any());
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void submitQuiz_whenInvalidPayloadOrServiceBadRequest_returns400() throws Exception {
        when(studentAccess.isSelf(any(), eq(5L))).thenReturn(true);
        when(quizService.submitQuiz(eq(5L), eq(1L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deve conter 20 respostas"));

        mockMvc.perform(post("/api/students/5/quiz-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void submitQuiz_whenMissingRequestBody_returns400() throws Exception {
        when(studentAccess.isSelf(any(), eq(5L))).thenReturn(true);

        mockMvc.perform(post("/api/students/5/quiz-submissions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // GET /api/students/{studentId}/quiz-results
    // =========================================================================

    @Test
    void getLatestQuizResults_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/students/5/quiz-results"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(quizService);
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void getLatestQuizResults_whenCannotView_returns403() throws Exception {
        when(studentAccess.canView(any(), eq(5L))).thenReturn(false);

        mockMvc.perform(get("/api/students/5/quiz-results"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(quizService);
    }

    @Test
    @WithMockUser(roles = "DOCENTE")
    void getLatestQuizResults_whenAuthorized_returns200AndCompetencyMap() throws Exception {
        when(studentAccess.canView(any(), eq(5L))).thenReturn(true);
        when(quizService.getLatestResultsForStudent(5L))
                .thenReturn(Map.of("Liderança", 4.2, "Comunicação", 3.8));

        mockMvc.perform(get("/api/students/5/quiz-results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Liderança").value(4.2))
                .andExpect(jsonPath("$.Comunicação").value(3.8));

        verify(quizService).getLatestResultsForStudent(5L);
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void getLatestQuizResults_whenStudentOrSubmissionNotFound_returns404() throws Exception {
        when(studentAccess.canView(any(), eq(999L))).thenReturn(true);
        when(quizService.getQuizQuestions(any())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        when(quizService.getLatestResultsForStudent(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Não encontrado"));

        mockMvc.perform(get("/api/students/999/quiz-results"))
                .andExpect(status().isNotFound());
    }
}