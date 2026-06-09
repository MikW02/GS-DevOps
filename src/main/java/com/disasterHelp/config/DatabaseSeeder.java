package com.disasterHelp.config;

import com.disasterHelp.model.*;
import com.disasterHelp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private DesastreRepository desastreRepository;


    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {

        /* --- Usuário administrador (criado primeiro p/ vincular aos desastres) --- */
        Usuario admin = usuarioRepository.findByEmail("admin@disasterHelp.com")
                .map(u -> (Usuario) u)
                .orElseGet(() -> {
                    Usuario novo = Usuario.builder()
                            .nome("Administrador")
                            .cpf("00000000191")
                            .email("admin@disasterHelp.com")
                            .senha(encoder.encode("admin123"))
                            .build();
                    usuarioRepository.save(novo);
                    System.out.println("Usuário admin criado com sucesso.");
                    return novo;
                });

        /* --- desastres (só semeia uma vez; vinculados ao admin via FK) --------- */
        if (desastreRepository.count() == 0) {
            Desastre desastre1 = Desastre.builder()
                    .tipo("Enchente")
                    .descricao("Chuvas intensas causando alagamentos em bairros centrais.")
                    .regiao("São Paulo - Zona Leste")
                    .dataPrevista("2025-06-08")
                    .usuario(admin)
                    .build();

            Desastre desastre2 = Desastre.builder()
                    .tipo("Onda de Calor")
                    .descricao("Temperaturas previstas acima de 40°C por vários dias consecutivos.")
                    .regiao("Cuiabá - MT")
                    .dataPrevista("2025-06-10")
                    .usuario(admin)
                    .build();

            desastreRepository.saveAll(List.of(desastre1, desastre2));
            System.out.println("Desastres iniciais cadastrados e vinculados ao admin.");
        }
    }
}
