
package com.studymatch.service;

import com.studymatch.dto.CreateGroupContextRequest;
import com.studymatch.model.GroupContext;
import com.studymatch.repository.CompetencyRepository;
import com.studymatch.repository.GroupContextRepository;
import com.studymatch.repository.GroupContextStudentRepository;
import com.studymatch.repository.GroupMemberRepository;
import com.studymatch.repository.StudentGroupRepository;
import com.studymatch.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupGenerationServiceTest {

    @Mock
    private GroupContextRepository contextRepository;

    @Mock
    private GroupContextStudentRepository contextStudentRepository;

    @Mock
    private StudentGroupRepository studentGroupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CompetencyRepository competencyRepository;

    @Mock
    private ProfileService profileService;

    @Mock
    private com.studymatch.grouping.GroupFormation groupFormation;

    @InjectMocks
    private GroupGenerationService service;

    @Test
    void createContextShouldRejectNullRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createContext(null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void createContextShouldRejectBlankName() {
        CreateGroupContextRequest request =
                new CreateGroupContextRequest(" ", List.of(1L));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createContext(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void createContextShouldRejectEmptyStudentList() {
        CreateGroupContextRequest request =
                new CreateGroupContextRequest("Turma A", List.of());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createContext(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void createContextShouldRejectDuplicateStudents() {
        CreateGroupContextRequest request =
                new CreateGroupContextRequest("Turma A", List.of(1L, 1L));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createContext(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void createContextShouldReturnNotFoundWhenStudentDoesNotExist() {
        GroupContext context = new GroupContext("Turma A");

        when(contextRepository.save(any(GroupContext.class)))
                .thenReturn(context);
        when(studentRepository.findById(99L))
                .thenReturn(Optional.empty());

        CreateGroupContextRequest request =
                new CreateGroupContextRequest("Turma A", List.of(99L));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createContext(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void generateGroupsShouldReturnNotFoundWhenContextDoesNotExist() {
        when(contextRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.generateGroups(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getGroupsShouldReturnNotFoundWhenContextDoesNotExist() {
        when(contextRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getGroups(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
