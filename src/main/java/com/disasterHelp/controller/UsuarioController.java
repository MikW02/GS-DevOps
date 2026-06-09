package com.disasterHelp.controller;

import com.disasterHelp.model.Credencial;
import com.disasterHelp.model.Usuario;
import com.disasterHelp.repository.UsuarioRepository;
import com.disasterHelp.service.TokenService;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("DisasterHelp/api/usuario")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Usuário", description = "Usuário da disasterHelp")
public class UsuarioController {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    AuthenticationManager manager;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    TokenService tokenService;

    @Autowired
    UsuarioRepository usuarioRepository;

    @PostMapping
    @Operation(
            summary = "Cadastro de usuário",
            description = "Cadastra um usuário no banco"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou faltando")
    })
    public ResponseEntity<Object> cadastro(@RequestBody @Valid Usuario usuario) {
        log.info("cadastrando usuario");
        usuario.setSenha(encoder.encode(usuario.getSenha()));
        usuarioRepository.save(usuario);
        log.info("cadastrado usuario: "+usuario.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @CrossOrigin
    @PostMapping("login")
    @Operation(
            summary = "Login de usuário",
            description = "Realiza o login de um usuário"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login efetuado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Dados inválidos")
    })
    public ResponseEntity<Object> login(@RequestBody Credencial credencial) {
        manager.authenticate(credencial.toAuthentication());
        log.info("credenciais autenticadas");
        var token = tokenService.generateToken(credencial);
        log.info("gerado token");
        return ResponseEntity.ok(token);
    }

    @CrossOrigin
    @GetMapping
    @Operation(
            summary = "Listar Usuários",
            description = "Retorna todos os Usuários cadastrados"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuários listados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Token inválido")
    })
    public Page<Usuario> listar(@PageableDefault(size = 5) Pageable pageable) {
        var listUsuario = usuarioRepository.findAll();
        int start = (int) pageable.getOffset();
        int end = (int) (Math.min((start + pageable.getPageSize()), listUsuario.size()));
        return new PageImpl<Usuario>(listUsuario.subList(start, end), pageable, listUsuario.size());
    }

    @GetMapping("{id}")
    @Operation(
            summary = "Detalhar usuário",
            description = "Busca um usuário por id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário detalhado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Não foi encontrado Usuário com esse ID"),
            @ApiResponse(responseCode = "403", description = "Token inválido")
    })
    public ResponseEntity<Usuario> index(@PathVariable Long id) {
        log.info("buscando usuario " + id);
        var result = usuarioRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario não Encontrado"));
        return ResponseEntity.ok(result);
    }

    @GetMapping("user")
    @Operation(
            summary = "Próprio usuário",
            description = "Busca um usuário pelo token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário detalhado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Não foi encontrado Usuário com esse ID"),
            @ApiResponse(responseCode = "403", description = "Token inválido")
    })
    public ResponseEntity<Usuario> userToken(@RequestHeader("Authorization") String header) {
        log.info("procurando usuario do token");
        var usuarioResult = tokenService.validate(tokenService.getToken(header));
        log.info("econtrado usuario: "+usuarioResult.toString());
        return ResponseEntity.ok(usuarioResult);
    }

    @DeleteMapping("{id}")
    @Operation(
            summary = "Deletar usuario",
            description = "Deleta um usuario por id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "usuario deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Não foi encontrado usuario com esse ID"),
            @ApiResponse(responseCode = "403", description = "Token inválido")
    })
    public ResponseEntity<Usuario> destroy(@PathVariable Long id){
        log.info("deletando usuario " + id);
        var result = usuarioRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario não Encontrado"));
        usuarioRepository.delete(result);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    @Operation(
            summary = "Editar usuario",
            description = "Editar um usuario por id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou faltando"),
            @ApiResponse(responseCode = "404", description = "Não foi encontrado Usuario com esse ID"),
            @ApiResponse(responseCode = "403", description = "Token inválido")
    })
    public ResponseEntity<Usuario> update(@PathVariable Long id, @RequestBody @Valid Usuario usuario){
        log.info("atualizando usuario "+id);
        var result = usuarioRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario não Encontrado"));
        usuario.setId(id);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(usuario);
    }

}
