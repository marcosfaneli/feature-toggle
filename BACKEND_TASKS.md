# Backend Tasks

Lista de tarefas para implementar o servidor de feature toggles no ecossistema Spring.

## Fase 1 — Fundamentos
- [x] Configurar projeto Spring Boot (Gradle/Maven), modules separados se necessário.
- [x] Adicionar dependências: Web, JPA, H2 (dev), PostgreSQL (prod opcional), Validation, Actuator, Lombok (opcional).
- [x] Configurar segurança simples por `X-API-Key` via filtro.
- [x] Estruturar perfis `dev` e `test` (H2), `prod` (Postgres).

## Fase 2 — Domínio e Persistência
- [x] Modelos JPA: `Attribute`, `Toggle`, `AllowListEntry` (ou jsonb), `ClientRegistration`, `AuditLog`.
- [x] Regras: unicidade de nome de atributo e toggle, `Toggle` deve referenciar `Attribute`.
- [x] Repositórios JPA para todas as entidades.
- [x] Migrações (Flyway/Liquibase) para tabelas e índices principais.

## Fase 3 — Serviços e Regras de Negócio
- [x] Serviço de atributos (CRUD com validações).
- [x] Serviço de toggles (CRUD, gestão de allow list).
- [x] Serviço de avaliação: `enabled && value in allowList`; default false se valor ausente.
- [x] Serviço de registro de clientes (inscrição por toggle, remoção).
- [x] Orquestrador de notificação: ao alterar toggle/atributo/allow list, disparar eventos para clientes inscritos.

## Fase 4 — API REST
- [ ] Controllers para atributos (`/api/attributes` CRUD).
- [ ] Controllers para toggles (`/api/toggles` CRUD + allow list endpoints).
- [ ] Endpoint de avaliação (`GET /api/toggles/{name}/evaluate?value=foo`).
- [ ] Endpoints de clientes (`POST /api/clients/register`, `DELETE /api/clients/{id}`, `GET /api/toggles/{name}/clients`).
- [ ] Respostas padronizadas (DTOs) e erros em formato problem-details.

## Fase 5 — Notificações
- [ ] Implementar envio `PUT` assíncrono para clientes inscritos (callback + toggles de interesse).
- [ ] Retentativa básica e registro de falhas.
- [ ] Modelar payload de notificação (`toggle`, `enabled`, `value`).

## Fase 6 — Auditoria e Observabilidade
- [ ] Registrar mudanças em `AuditLog` (ação, recurso, payload resumido, timestamp).
- [ ] Métricas (Micrometer): requests, latência, cache-miss.
- [ ] Logs estruturados e correlação de requisições.

## Fase 7 — Testes e Qualidade
- [ ] Testes unitários do serviço de avaliação (cenários enabled/disabled/allow list).
- [ ] Testes de serviços CRUD e validações.
- [ ] Testes de integração dos endpoints principais (incluindo API key).
- [ ] Teste de notificação simulando cliente webhook.
- [ ] Cobertura mínima e execução em CI (GitHub Actions opcional).

## Fase 8 — Docs e Execução
- [ ] Atualizar README com steps para rodar (profiles, DB, exemplos de curl).
- [ ] Documentar contratos principais (payloads, exemplos de resposta/erro).
- [ ] Opcional: docker-compose com Postgres para desenvolvimento.
