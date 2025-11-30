
# Tech Assessment — Feature Toggle (Spring ecosystem)

## Resumo

Este documento descreve os requisitos, arquitetura proposta e critérios de avaliação para uma solução open-source de feature toggles destinada ao ecossistema Spring. A solução consiste em:
- Um servidor (service) que gerencia feature toggles (criação, atualização, auditoria) e expõe uma API HTTP para consulta e notificação de clientes.
- Uma biblioteca cliente para aplicações Spring que consulta o servidor, fornece cache local e expõe um endpoint que o servidor chama para notificar atualizações.

## Objetivo

Fornecer uma solução simples, testável e extensível para habilitar/desabilitar funcionalidades em tempo de execução em aplicações Spring, permitindo definir variáveis de controle genéricas (usuário, moeda, fornecedor, etc.) e usá-las para avaliar toggles de forma flexível.

## Escopo

### Servidor

Incluso:
- **Cadastro de Atributos**: CRUD completo para registrar tipos de atributos (ex.: `userId`, `currency`, `supplierId`) com nomes únicos.
- **Cadastro de Toggles**: CRUD completo com nome, estado, estratégia, vinculação de atributos com valores válidos, e suporte a block list e allow list por atributo.
- **API HTTP REST** para:
  - CRUD de atributos
  - CRUD de toggles
  - Avaliação de toggles (`GET /api/toggles/{name}/evaluate?attr1=value1&attr2=value2`)
- **Notificação de clientes**: ao alterar uma toggle ou atributo, notificar clientes registrados chamando seus endpoints de webhook.
- **Auditoria**: registrar quem, quando e o quê foi alterado.

Fora do escopo (fases posteriores):
- UI avançada de administração (API será suficiente).
- Replicação e alta disponibilidade (single instance será ok).

### Biblioteca Cliente

Incluso:
- **Carregamento de toggles**: dois modos de operação:
  - **Eager**: busca todas as toggles e atributos no startup e mantém em cache local.
  - **Lazy**: consulta o servidor a cada solicitação.
- **Cache local**: armazenar em memória o estado das toggles e atributos.
- **Endpoint de webhook**: expor um endpoint (ex.: `POST /_toggle/notify`) que o servidor chama para notificar atualizações.
- **Atualização automática**: ao receber notificação, recarregar dados do servidor e atualizar cache.
- **API simples**: expor métodos/anotações para a aplicação cliente consultar o estado de uma toggle.
- **Mecanismo de ready state**: indicar quando o cache está pronto (útil para modo eager).

Fora do escopo (fases posteriores):
- UI/Dashboard para gerenciar toggles (responsabilidade do servidor).
- Persistência local de toggles entre restarts (memória será suficiente).

## Requisitos Funcionais

### Servidor

1. **CRUD de Atributos**:
   - Registrar atributo com nome único (ex.: `userId`, `currency`, `supplierId`).
   - Retornar lista de atributos cadastrados.

2. **CRUD de Toggles**:
   - Criar toggle com nome, descrição, status (ON/OFF) e vinculação de variáveis de controle.
   - Cada vinculação define quais valores são válidos para o atributo naquela toggle.
   - Retornar lista de todas as toggles com suas configurações.

3. **Avaliação de Toggle**:
   - Endpoint: `GET /api/toggles/{name}/evaluate?attr1=value1&attr2=value2`
   - Retorna `{ enabled: true/false, reason: "..." }` baseado na estratégia e valores fornecidos.
   - Lógica de avaliação: se o valor está em **allow list**, retorna `true`; se está em **block list**, retorna `false`; senão, aplica a estratégia definida (ex: percentual).

4. **Registro de clientes**: manter lista de clientes registrados (URL de webhook) para notificação de atualizações de toggles e atributos.

5. **Auditoria**: registrar logs de mudanças (quem, quando, o quê).

### Biblioteca Cliente

1. **Inicialização**:
   - Modo eager: buscar todas as toggles e atributos do servidor no startup.
   - Modo lazy: fazer consultas sob demanda, sem carregamento inicial.

2. **Cache Local**:
   - Armazenar toggles e atributos em memória.
   - Invalidar/atualizar quando receber notificações do servidor.

3. **API de Consulta**:
   - Método simples para verificar estado de uma toggle (ex.: `toggleService.isEnabled("feature-x", context)`).
   - Suporte a anotações Spring (ex.: `@FeatureToggle`).

4. **Ready State**:
   - Expor mecanismo para saber se o cache está pronto (importante para eager mode).
 
## Requisitos Não-Funcionais

- Latência de consulta baixa (objetivo: < 20ms para respostas de cache local; < 200ms para consulta remota razoável).
- Alta disponibilidade do servidor (opcionalmente com replicação e balanceamento).
- Segurança: autenticação/autorizações para operações de escrita; leitura pública pode ser opcionalmente protegida.
- Observabilidade: métricas (requests, latência, taxa de cache-miss), logs e traces básicos.
- Documentação e testes automatizados (unit + integração).

## Restrições e Premissas

- Foco inicial em aplicações Java/Spring Boot.
- Comunicação entre cliente e servidor será via HTTP/HTTPS (REST).
- Persistência mínima com banco relacional (ex.: PostgreSQL) ou leve (SQLite) para PoC.

## Arquitetura Proposta

### Servidor

- **API REST** (Spring Boot)
  - Controllers para CRUD de variáveis de controle
  - Controllers para CRUD de toggles
  - Controller de avaliação (`/toggles/{name}/evaluate`)
  - Controller para registro/desregistro de clientes (webhooks)

- **Camada de Serviço**
  - Lógica de avaliação de toggles
  - Publicação de eventos de alteração

- **Camada de Persistência** (JPA)
  - Entidades: `Attribute`, `Toggle`, `ToggleRule`, `AuditLog`

- **Sistema de Notificações**
  - Mecanismo para chamar webhooks de clientes registrados quando há alterações

### Biblioteca Cliente (Spring Boot Starter)

- **Auto-configuração Spring**
  - Bean de `ToggleService` (principal)
  - Bean de cache e scheduler

- **Serviço de Toggle**
  - Interface `ToggleService` com método `isEnabled(name, context)`
  - Implementações para modo eager e lazy

- **Cache Local**
  - Mapa em memória de toggles e variáveis

- **Cliente HTTP**
  - Comunicação com servidor (RestTemplate ou WebClient)

- **Endpoint de Notificação**
  - Expõe `POST /_toggle/notify` para receber notificações do servidor
  - Ao receber, recarrega toggles e atributos

- **Registro automático**: ao iniciar, registra-se no servidor com sua URL de callback

### Endpoints da API

**Servidor - Atributos**:
- `GET /api/attributes` — retorna lista de atributos
- `POST /api/attributes` — cria novo atributo (admin)
- `PUT /api/attributes/{id}` — atualiza atributo (admin)
- `DELETE /api/attributes/{id}` — remove atributo (admin)

**Servidor - Toggles**:
- `GET /api/toggles` — retorna lista de toggles e suas configurações (Eager)
- `GET /api/toggles/{name}` — retorna toggle específica
- `GET /api/toggles/{name}/evaluate?attr1=value1&attr2=value2` — avalia toggle para valores fornecidos
- `POST /api/toggles` — cria toggle (admin)
- `PUT /api/toggles/{name}` — atualiza toggle (admin)
- `DELETE /api/toggles/{name}` — remove toggle (admin)

**Servidor - Allow List e Block List** (por toggle e atributo):
- `POST /api/toggles/{name}/attributes/{attributeId}/allow-list` — adiciona valor à allow list (admin)
- `DELETE /api/toggles/{name}/attributes/{attributeId}/allow-list/{value}` — remove valor da allow list (admin)
- `POST /api/toggles/{name}/attributes/{attributeId}/block-list` — adiciona valor à block list (admin)
- `DELETE /api/toggles/{name}/attributes/{attributeId}/block-list/{value}` — remove valor da block list (admin)

**Servidor - Gerenciamento de Clientes**:
- `POST /api/clients/register` — cliente se registra com sua URL de callback (ex.: `{ "callbackUrl": "http://cliente:8080/_toggle/notify" }`)
- `DELETE /api/clients/{clientId}` — cliente se desregistra
- `GET /api/toggles/{name}/clients` — retorna lista de clientes conectados que dependem da toggle (admin)

**Cliente - Notificação de Atualização** (exposto pela biblioteca):
- `POST /_toggle/notify` — servidor chama para notificar alterações (ex.: `{ "type": "toggle_updated", "toggleId": "..." }`)

## Modelo de Dados (exemplo)

**Attribute**:
- id: UUID
- name: string (unique, ex.: "userId", "currency", "supplierId")
- description: string (opcional)
- dataType: enum (STRING, NUMBER, BOOLEAN, etc.)
- createdAt, updatedAt, updatedBy

**Toggle**:
- id: UUID
- name: string (unique)
- description: string
- enabled: boolean
- strategy: enum (GLOBAL, CONDITIONAL, PERCENTAGE)
- attributeRules: List of { attributeId, allowedValues: List<String>, allowList: List<String>, blockList: List<String> }
  - Exemplo: `{ attributeId: "userId-uuid", allowedValues: ["123", "456"], allowList: ["999"], blockList: ["111"] }`
  - `allowList`: valores que SEMPRE ativam a toggle (override)
  - `blockList`: valores que NUNCA ativam a toggle (override)
  - `allowedValues`: valores base para a estratégia
- createdAt, updatedAt, updatedBy

**Avaliação de Toggle**:
- Uma toggle com GLOBAL retorna sempre `true` se `enabled=true`, exceto se o valor está em **block list**.
- Uma toggle com CONDITIONAL retorna `true` se `enabled=true` **E**:
  - Se o valor está em **allow list**, retorna `true` (override positivo).
  - Se o valor está em **block list**, retorna `false` (override negativo).
  - Senão, verifica se o valor corresponde aos **allowedValues** (aplica a estratégia).
- Uma toggle com PERCENTAGE retorna `true` se `enabled=true` **E**:
  - Se o valor está em **allow list**, retorna `true` (override positivo).
  - Se o valor está em **block list**, retorna `false` (override negativo).
  - Senão, aplica hash do valor para determinar se cai no percentual definido (ex: 10% dos usuários).

## Critérios de Avaliação / Sucesso

- Implementação mínima do servidor com CRUD de atributos e toggles.
- Endpoint de avaliação de toggle funcionando corretamente com múltiplos atributos.
- Biblioteca cliente com `eager` e `lazy` funcionando e cache local.
- Notificações de atualização básicas funcionando entre servidor e cliente (via webhook).
- Testes automatizados cobrindo casos principais (avaliação com múltiplos cenários).
- Documentação com instruções de uso e exemplos (ex.: criar variável de controle, criar toggle, consultar toggle).

## Plano de Entregáveis e Timeline (sugestão)

- Dia 0–1: Especificação detalhada e POC do contrato HTTP.
- Dia 2–4: Implementação do servidor (CRUD + consulta) + persistência.
- Dia 5–7: Implementação do cliente Spring (eager & lazy) + integração básica.
- Dia 8: Testes de integração e ajuste fino.

## Entregáveis

- Repositório com dois módulos (`server` e `client-lib`).
- `README.md` com instruções de execução local e exemplos de uso.
- Coleção de testes automatizados e script de inicialização (Docker Compose opcional).

## Como rodar localmente (exemplo rápido)

1. Rodar banco de dados (Postgres) ou usar H2 para dev.
2. Iniciar servidor:

```bash
# dentro de /server
mvn spring-boot:run
```

3. Iniciar exemplo de aplicação cliente (Spring Boot sample) ou executar testes de integração.

## Próximos passos que eu executo agora

1. Aplicar esta versão do documento em `tech-assessment.md` (já em execução).
2. Se desejar, dividir o projeto em um scaffold inicial (`server` + `client-lib`) com README e PoC.

---

Se quiser, eu já crio o scaffold inicial do projeto (módulos Maven/Gradle, `README`, e um pequeno exemplo de servidor + cliente). Diga se prefere Maven ou Gradle e se quer Docker Compose para o banco.

