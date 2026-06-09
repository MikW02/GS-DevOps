# DisasterHelp - DevOps Tools & Cloud Computing

API REST em Java (Spring Boot) para cadastro e gestao de eventos climaticos / desastres,
conteinerizada com Docker e executada em nuvem (VM no Azure) com dois containers integrados:
a aplicacao Java e o banco PostgreSQL.

RM do representante: 551382
Containers: `app-disasterhelp-rm551382` (aplicacao) e `postgres-disasterhelp-rm551382` (banco)

## Descricao da solucao

O DisasterHelp permite registrar usuarios e desastres naturais (enchentes, ondas de calor,
tempestades, etc.), informando tipo, descricao, regiao afetada e data prevista. A ideia e
centralizar informacoes de eventos climaticos para apoio a prevencao e resposta.

A solucao roda em dois containers Docker orquestrados por Docker Compose, na mesma rede
(`disasterhelp-net`):

- `app-disasterhelp-rm551382`: imagem personalizada (build multi-stage), API Java/Spring Boot na porta 8080.
- `postgres-disasterhelp-rm551382`: imagem oficial postgres:16, banco de dados na porta 5432.

Os dados sao gravados em duas tabelas no PostgreSQL:

- `usuario`: usuarios do sistema (usados na autenticacao).
- `desastres`: eventos climaticos cadastrados (CRUD completo).

A tabela `desastres` tem a coluna `usuario_id` como chave estrangeira para `usuario(id)`
(relacionamento N:1, indicando o usuario responsavel pelo registro do desastre).

## Tecnologias

- Java 17 e Spring Boot 3.1.5 (Web, Data JPA, Security/JWT, Validation, Swagger)
- PostgreSQL 16
- Docker (build multi-stage) e Docker Compose
- Maven (build feito dentro do container)

## Arquitetura

O diagrama de arquitetura sera adicionado em `docs/arquitetura.png`.

Resumo do fluxo: o usuario acessa a API pela porta 8080 da VM no Azure. Dentro da VM, o Docker
executa o container da aplicacao e o container do banco, que se comunicam pela rede interna
`disasterhelp-net`. Os dados do banco ficam em um volume nomeado, mantendo a persistencia mesmo
que os containers sejam recriados.

## Como executar (do clone ate a nuvem)

Pre-requisito: uma VM Linux no Azure (Azure for Students) com Docker instalado. Os passos abaixo
mostram como preparar a VM. Se ja tiver a VM com Docker, pule para a parte "Subir os containers".

### Criar a VM no Azure

1. Acessar o Portal Azure (portal.azure.com) com a conta Azure for Students.
2. Create a resource > Virtual Machine:
   - Image: Ubuntu Server 22.04 LTS
   - Size: B1s ou B2s
   - Authentication: SSH public key (ou senha)
   - Username: azureuser
3. Em Networking, liberar as portas de entrada: 22 (SSH), 8080 (app) e 5432 (banco).
4. Review + create. Anotar o IP publico da VM.
5. Conectar por SSH:

   ```
   ssh azureuser@<IP_PUBLICO_DA_VM>
   ```

6. Instalar Docker e Compose na VM:

   ```
   sudo apt-get update
   sudo apt-get install -y docker.io docker-compose-plugin git
   sudo usermod -aG docker $USER && newgrp docker
   docker --version && docker compose version
   ```

### Subir os containers

```
git clone https://github.com/MikW02/GS-DevOps.git
cd GS-DevOps
docker compose up -d --build
docker ps
```

### Ver os logs dos dois containers

```
docker compose logs app
docker compose logs postgres-db
```

### Acessar o terminal dos containers

```
docker container exec -it app-disasterhelp-rm551382 bash
pwd        # /opt/disasterhelp
ls -l
whoami     # disasteruser (usuario nao root)
exit
```

```
docker container exec -it postgres-disasterhelp-rm551382 bash
pwd
ls -l
whoami
exit
```

## Testar o CRUD

A API usa autenticacao JWT. Ja existe um usuario admin criado automaticamente:

- email: admin@disasterHelp.com
- senha: admin123

Documentacao interativa (Swagger): http://<IP_DA_VM>:8080/swagger-ui.html
Ao testar de fora da nuvem, trocar `localhost` pelo IP publico da VM.

Login (obter token):

```
TOKEN=$(curl -s -X POST http://localhost:8080/DisasterHelp/api/usuario/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@disasterHelp.com","senha":"admin123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo $TOKEN
```

Create:

```
curl -X POST http://localhost:8080/disasterHelp/api/desastre \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"tipo":"Deslizamento","descricao":"Encosta instavel apos chuvas","regiao":"Petropolis - RJ","dataPrevista":"2026-06-15"}'
```

Read:

```
curl http://localhost:8080/disasterHelp/api/desastre -H "Authorization: Bearer $TOKEN"
curl http://localhost:8080/disasterHelp/api/desastre/1 -H "Authorization: Bearer $TOKEN"
```

Update:

```
curl -X PUT http://localhost:8080/disasterHelp/api/desastre/1 \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"tipo":"Enchente","descricao":"Nivel do rio subindo","regiao":"Sao Paulo - Zona Leste","dataPrevista":"2026-06-20"}'
```

Delete:

```
curl -X DELETE http://localhost:8080/disasterHelp/api/desastre/3 -H "Authorization: Bearer $TOKEN"
```

## Evidencia da persistencia no banco (SELECT)

Conectar direto no container do banco e rodar os SELECT apos cada operacao do CRUD:

```
docker container exec -it postgres-disasterhelp-rm551382 psql -U postgres -d disasterdb
```

No psql:

```
\dt
\d desastres
SELECT * FROM desastres;
SELECT * FROM usuario;

SELECT d.id, d.tipo, d.regiao, d.usuario_id, u.nome AS responsavel
FROM desastres d
LEFT JOIN usuario u ON u.id = d.usuario_id
ORDER BY d.id;
\q
```

## Encerrar o ambiente

```
docker compose down       # para e remove os containers (mantem o volume)
docker compose down -v    # remove tambem o volume (apaga os dados)
```

## Estrutura do repositorio

```
GS-DevOps/
  Dockerfile             # imagem da API (multi-stage, usuario nao root)
  docker-compose.yml     # app + postgres, rede e volume nomeado
  .dockerignore
  pom.xml                # dependencias Maven
  src/                   # codigo-fonte Java (Spring Boot)
  docs/                  # diagrama de arquitetura
```
