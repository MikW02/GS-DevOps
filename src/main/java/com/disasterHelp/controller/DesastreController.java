package com.disasterHelp.controller;

import com.disasterHelp.model.Desastre;
import com.disasterHelp.repository.DesastreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("disasterHelp/api/desastre")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Desastre", description = "Eventos climáticos cadastrados no sistema")
public class DesastreController {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    DesastreRepository desastreRepository;

    @PostMapping
    @Operation(
            summary = "Cadastro de um evento climático",
            description = "Cadastra um novo desastre (evento climático) no sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Desastre cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou faltando")
    })
    public ResponseEntity<Object> cadastro(@RequestBody @Valid Desastre desastre) {
        log.info("Salvando desastre: {}", desastre);
        desastreRepository.save(desastre);
        return ResponseEntity.status(HttpStatus.CREATED).body(desastre);
    }

    @CrossOrigin
    @GetMapping
    @Operation(
            summary = "Listar desastres",
            description = "Retorna todos os desastres cadastrados"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Desastres listados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Token inválido")
    })
    public Page<Desastre> listar(@PageableDefault(size = 5) Pageable pageable) {
        log.info("Recuperando todos os desastres");
        var list = desastreRepository.findAll();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    @GetMapping("{id}")
    @Operation(
            summary = "Detalhar desastre",
            description = "Busca um desastre por ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Desastre detalhado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Desastre não encontrado"),
            @ApiResponse(responseCode = "403", description = "Token inválido")
    })
    public ResponseEntity<Desastre> index(@PathVariable Long id) {
        log.info("Buscando desastre {}", id);
        var result = desastreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Desastre não encontrado"));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("{id}")
    @Operation(
            summary = "Deletar desastre",
            description = "Remove um desastre do sistema por ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Desastre deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Desastre não encontrado"),
            @ApiResponse(responseCode = "403", description = "Token inválido")
    })
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("Deletando desastre {}", id);
        var result = desastreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Desastre não encontrado"));
        desastreRepository.delete(result);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    @Operation(
            summary = "Atualizar desastre",
            description = "Atualiza os dados de um desastre por ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Desastre atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Desastre não encontrado"),
            @ApiResponse(responseCode = "403", description = "Token inválido")
    })
    public ResponseEntity<Desastre> update(@PathVariable Long id, @RequestBody @Valid Desastre desastre) {
        log.info("Atualizando desastre {}", id);
        var result = desastreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Desastre não encontrado"));
        desastre.setId(id);
        desastreRepository.save(desastre);
        return ResponseEntity.ok(desastre);
    }
}
