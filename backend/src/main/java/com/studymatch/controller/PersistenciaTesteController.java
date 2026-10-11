package com.studymatch.controller;

import com.studymatch.model.PersistenciaTeste;
import com.studymatch.repository.PersistenciaTesteRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Endpoint temporário para verificar a leitura e escrita na base de dados.
// Deve ser removido quando os testes da Issue #10 terminarem.
@RestController
@RequestMapping("/api/teste-persistencia")
public class PersistenciaTesteController {

    private final PersistenciaTesteRepository repository;

    // O Spring injeta automaticamente o Repository.
    public PersistenciaTesteController(PersistenciaTesteRepository repository) {
        this.repository = repository;
    }

    // Guarda um registo de teste no Supabase.
    @PostMapping
    public PersistenciaTeste guardar() {
        PersistenciaTeste teste = new PersistenciaTeste("Ligação StudyMatch funcionando");
        return repository.save(teste);
    }

    // Consulta os registos existentes na tabela.
    @GetMapping
    public List<PersistenciaTeste> listar() {
        return repository.findAll();
    }
}