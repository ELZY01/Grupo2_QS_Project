package com.studymatch.dto;

/**
 * Pedido de login.
 *
 * @param username nome de utilizador; no aluno também serve o número de estudante ou o email
 * @param password palavra-passe
 */
public record LoginRequest(String username, String password) {
}
