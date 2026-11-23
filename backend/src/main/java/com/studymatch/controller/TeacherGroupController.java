package com.studymatch.controller;

import com.studymatch.dto.CreateGroupContextRequest;
import com.studymatch.dto.GroupMembersRequest;
import com.studymatch.dto.TeacherGroupContextResponse;
import com.studymatch.dto.TeacherGroupsResponse;
import com.studymatch.grouping.GroupAccessDeniedException;
import com.studymatch.grouping.GroupMembershipConflictException;
import com.studymatch.grouping.ImpossibleGroupConfigurationException;
import com.studymatch.security.AuthenticatedUser;
import com.studymatch.service.TeacherGroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Grupos das UCs do docente (Issue #106). Só DOCENTE (ver SecurityConfig: /api/teacher/**).
 *
 * <p>Erros: UC de outro docente → 403 (R4); aluno não inscrito, já noutro grupo, repetido ou
 * tamanho fora de 4–5 → 409 (R4, R5); configuração impossível na geração → 400.
 */
@RestController
@RequestMapping("/api/teacher/course-units/{courseUnitId}/group-contexts")
public class TeacherGroupController {

    private final TeacherGroupService service;

    public TeacherGroupController(TeacherGroupService service) {
        this.service = service;
    }

    /** GET — contextos da UC. */
    @GetMapping
    public List<TeacherGroupContextResponse> contexts(@AuthenticationPrincipal AuthenticatedUser user,
                                                      @PathVariable Long courseUnitId) {
        return service.contexts(user.id(), courseUnitId);
    }

    /** POST — cria um contexto com os alunos elegíveis (US10). */
    @PostMapping
    public ResponseEntity<TeacherGroupContextResponse> createContext(@AuthenticationPrincipal AuthenticatedUser user,
                                                                     @PathVariable Long courseUnitId,
                                                                     @RequestBody CreateGroupContextRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createContext(user.id(), courseUnitId, request));
    }

    /** GET {contextId}/groups — grupos atuais com as estatísticas (R6). */
    @GetMapping("/{contextId}/groups")
    public TeacherGroupsResponse groups(@AuthenticationPrincipal AuthenticatedUser user,
                                        @PathVariable Long courseUnitId,
                                        @PathVariable Long contextId) {
        return service.groups(user.id(), courseUnitId, contextId);
    }

    /** POST {contextId}/generate — gera grupos equilibrados (US11). */
    @PostMapping("/{contextId}/generate")
    public TeacherGroupsResponse generate(@AuthenticationPrincipal AuthenticatedUser user,
                                          @PathVariable Long courseUnitId,
                                          @PathVariable Long contextId) {
        return service.generate(user.id(), courseUnitId, contextId);
    }

    /** POST {contextId}/groups — cria um grupo à mão (R4, R5). */
    @PostMapping("/{contextId}/groups")
    public ResponseEntity<TeacherGroupsResponse> createGroup(@AuthenticationPrincipal AuthenticatedUser user,
                                                             @PathVariable Long courseUnitId,
                                                             @PathVariable Long contextId,
                                                             @RequestBody GroupMembersRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createGroup(user.id(), courseUnitId, contextId, request));
    }

    /** PUT {contextId}/groups/{groupId} — muda os membros de um grupo (R4, R5). */
    @PutMapping("/{contextId}/groups/{groupId}")
    public TeacherGroupsResponse updateGroup(@AuthenticationPrincipal AuthenticatedUser user,
                                             @PathVariable Long courseUnitId,
                                             @PathVariable Long contextId,
                                             @PathVariable Long groupId,
                                             @RequestBody GroupMembersRequest request) {
        return service.updateGroup(user.id(), courseUnitId, contextId, groupId, request);
    }

    /** DELETE {contextId}/groups/{groupId} — apaga um grupo; os alunos ficam sem grupo. */
    @DeleteMapping("/{contextId}/groups/{groupId}")
    public TeacherGroupsResponse deleteGroup(@AuthenticationPrincipal AuthenticatedUser user,
                                             @PathVariable Long courseUnitId,
                                             @PathVariable Long contextId,
                                             @PathVariable Long groupId) {
        return service.deleteGroup(user.id(), courseUnitId, contextId, groupId);
    }

    @ExceptionHandler(GroupAccessDeniedException.class)
    ResponseEntity<Map<String, Object>> forbidden(GroupAccessDeniedException e) {
        return error(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(GroupMembershipConflictException.class)
    ResponseEntity<Map<String, Object>> conflict(GroupMembershipConflictException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ImpossibleGroupConfigurationException.class)
    ResponseEntity<Map<String, Object>> impossible(ImpossibleGroupConfigurationException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** Mesmo formato dos erros do SecurityConfig: {"status": ..., "message": ...}. */
    private static ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("status", status.value(), "message", message));
    }
}
