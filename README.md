# DisasterHelp - DevOps Tools & Cloud Computing
RM551382 Michael Leon
RM97677 Lucas Felix


Projeto da **Global Solution 2026/1 (FIAP)** - entrega da disciplina **DevOps Tools & Cloud Computing**.

API REST em Java (Spring Boot) para cadastro e gestao de eventos climaticos / desastres,
conteinerizada com Docker e executada em nuvem (VM no Azure) com dois containers integrados:
a aplicacao Java e o banco PostgreSQL.

RM do representante: 551382
Containers: `app-disasterhelp-rm551382` (aplicacao) e `postgres-disasterhelp-rm551382` (banco)

## Descricao da solucao

O tema da Global Solution 2026/1 e a **Economia Espacial** ("O espaco e a nova fronteira"): usar a
exploracao espacial e a infraestrutura orbital para resolver problemas reais aqui na Terra. Um dos
eixos propostos e justamente **Agro & Clima - monitoramento por satelite para previsao climatica e
prevencao de desastres**, e e nesse eixo que o DisasterHelp se posiciona.

O **DisasterHelp** e uma plataforma que centraliza informacoes de **eventos climaticos e desastres
naturais** (enchentes, ondas de calor, tempestades, deslizamentos, etc.). A ideia e que esses
eventos possam ser alimentados a partir de **dados de monitoramento por satelite** (sensoriamento
remoto, imagens orbitais, previsao climatica) e fiquem disponiveis numa API central, apoiando a
**prevencao, o alerta e a resposta rapida** a regioes afetadas - inclusive areas remotas, que sao
um dos focos da conectividade via satelite.

Cada evento registra tipo, descricao, regiao afetada, data prevista e o usuario responsavel pelo
cadastro. Nesta entrega de DevOps, o foco e **conteinerizar e colocar essa solucao em nuvem** com
boas praticas (imagem personalizada, usuario nao-root, rede e volume dedicados, persistencia em
banco).

A solucao roda em dois containers Docker orquestrados por Docker Compose, na mesma rede
(`disasterhelp-net`):

- `app-disasterhelp-rm551382`: imagem personalizada (build multi-stage), API Java/Spring Boot na porta 8080.
- `postgres-disasterhelp-rm551382`: imagem oficial postgres:16, banco de dados na porta 5432.

O **build multi-stage** do app acontece em duas etapas dentro do mesmo Dockerfile: na primeira, uma
imagem com Maven + JDK compila o codigo-fonte e gera o arquivo `.jar`; na segunda, esse `.jar` e
copiado para uma imagem enxuta so com o Java de execucao (JRE). Dessa forma a imagem final fica
menor e mais segura, sem carregar as ferramentas de compilacao usadas no build.

Os dados sao gravados em duas tabelas no PostgreSQL:

- `usuario`: usuarios do sistema (autenticacao) - CRUD completo.
- `desastres`: eventos climaticos cadastrados - CRUD completo.

A tabela `desastres` tem a coluna `usuario_id` como chave estrangeira para `usuario(id)`
(relacionamento N:1, indicando o usuario responsavel pelo registro do desastre). Ao cadastrar um
desastre, o sistema grava automaticamente o usuario autenticado (do token) como responsavel, e a
resposta da API traz os campos `usuarioId` e `responsavel`.

## Tecnologias

- Java 17 e Spring Boot 3.1.5 (Web, Data JPA, Security/JWT, Validation, Swagger)
- PostgreSQL 16
- Docker (build multi-stage) e Docker Compose
- Maven (build feito dentro do container)

## Arquitetura

![Arquitetura Macro da solucao DisasterHelp](docs/arquitetura.png)

Resumo do fluxo:

- O usuario acessa a API (porta 8080) na VM do Azure e tem acesso ao App.
- O container da aplicacao Java conversa com o container PostgreSQL pela rede interna disasterhelp-net (porta 5432).
- Os dados ficam no volume nomeado, garantindo persistencia.
- A equipe acessa a VM por SSH (porta 22).

## Como executar (do clone ate a nuvem)

Pre-requisitos ja instalados na VM Linux do Azure: Docker, Docker Compose e Git. Para conferir as versoes:

```
docker --version && docker compose version && git --version
```

### Subir os containers

Clonar o repositorio e entrar na pasta:

```
git clone https://github.com/MikW02/GS-DevOps.git
```

```
cd GS-DevOps
```

Subir App + Banco em modo background (-d) e construindo a imagem:

```
docker compose up -d --build
```

Conferir os containers em execucao:

```
docker ps
```

### Ver os logs dos dois containers

Logs do container da aplicacao:

```
docker compose logs app
```

Logs do container do banco:

```
docker compose logs postgres-db
```

### Acessar o terminal dos containers (usuario e diretorio)

Container da aplicacao - usuario conectado (disasteruser, nao root), diretorio de trabalho e estrutura:

```
docker container exec app-disasterhelp-rm551382 whoami
docker container exec app-disasterhelp-rm551382 pwd
docker container exec app-disasterhelp-rm551382 ls -l
```

Container do banco - usuario conectado, diretorio atual e estrutura:

```
docker container exec postgres-disasterhelp-rm551382 whoami
docker container exec postgres-disasterhelp-rm551382 pwd
docker container exec postgres-disasterhelp-rm551382 ls -l
```

## Testar o CRUD (pelo Swagger) e evidenciar no banco

A API usa autenticacao JWT. Ja existe um usuario admin criado automaticamente:

- email: admin@disasterHelp.com
- senha: admin123

Abrir a documentacao interativa (Swagger) no navegador (trocar pelo IP publico da VM):

```
http://<IP_DA_VM>:8080/swagger-ui/index.html
```

### Passo 1 - Login (no Swagger)

Em "Usuario" -> POST /DisasterHelp/api/usuario/login -> Try it out -> usar o corpo abaixo -> Execute.
Copiar o valor de "token" que aparece na resposta.

```
{ "email": "admin@disasterHelp.com", "senha": "admin123" }
```

### Passo 2 - Authorize (no Swagger)

Clicar no botao Authorize (cadeado, canto superior direito), colar apenas o token e confirmar.
A partir daqui os endpoints protegidos funcionam.

### Passo 3 - Mostrar as duas tabelas e o relacionamento (banco)

Listar as duas tabelas (usuario e desastres):

```
docker container exec postgres-disasterhelp-rm551382 psql -U postgres -d disasterdb -c "\dt"
```

Mostrar a estrutura da tabela desastres (coluna usuario_id e a chave estrangeira para usuario):

```
docker container exec postgres-disasterhelp-rm551382 psql -U postgres -d disasterdb -c "\d desastres"
```

Estado inicial dos dados:

```
docker container exec postgres-disasterhelp-rm551382 psql -U postgres -d disasterdb -c "SELECT * FROM desastres ORDER BY id;"
```

### Passo 4 - CREATE (no Swagger)

Em "Desastre" -> POST /disasterHelp/api/desastre -> Try it out -> corpo abaixo -> Execute (retorna 201, com usuarioId e responsavel):

```
{ "tipo": "Deslizamento", "descricao": "Encosta instavel apos chuvas", "regiao": "Petropolis - RJ", "dataPrevista": "2026-06-15" }
```

Evidencia no banco (a nova linha aparece, id 3):

```
docker container exec postgres-disasterhelp-rm551382 psql -U postgres -d disasterdb -c "SELECT * FROM desastres ORDER BY id;"
```

### Passo 5 - READ (no Swagger)

Listar todos: GET /disasterHelp/api/desastre -> Execute.
Buscar por id: GET /disasterHelp/api/desastre/1 -> Execute.

### Passo 6 - UPDATE (no Swagger)

PUT /disasterHelp/api/desastre/3 -> Try it out -> corpo abaixo -> Execute (retorna 200):

```
{ "tipo": "Enchente", "descricao": "Nivel do rio subindo", "regiao": "Sao Paulo - SP", "dataPrevista": "2026-06-20" }
```

Evidencia no banco (a linha id 3 aparece alterada):

```
docker container exec postgres-disasterhelp-rm551382 psql -U postgres -d disasterdb -c "SELECT * FROM desastres ORDER BY id;"
```

### Passo 7 - DELETE (no Swagger)

DELETE /disasterHelp/api/desastre/3 -> Execute (retorna 204).

Evidencia no banco (a linha id 3 sumiu):

```
docker container exec postgres-disasterhelp-rm551382 psql -U postgres -d disasterdb -c "SELECT * FROM desastres ORDER BY id;"
```

### Passo 8 - Tabela de usuarios

Mostrar todos os dados da tabela de usuarios:

```
docker container exec postgres-disasterhelp-rm551382 psql -U postgres -d disasterdb -c "SELECT * FROM usuario ORDER BY id;"
```

## Encerrar o ambiente

Parar e remover os containers (mantem o volume com os dados):

```
docker compose down
```

Parar e remover tambem o volume nomeado (apaga os dados do banco):

```
docker compose down -v
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
