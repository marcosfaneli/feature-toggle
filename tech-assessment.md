
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
- **Cadastro de Toggles**: CRUD completo com nome, estado, vinculação a um único atributo e manutenção de uma allow list de valores autorizados para esse atributo.
- **API HTTP REST** para:
  - CRUD de atributos
  - CRUD de toggles
  - Avaliação de toggles (`GET /api/toggles/{name}/evaluate?value=foo`)
- **Notificação de clientes**: clientes se registram via `POST /api/clients/register` informando um endpoint `PUT` e a lista de toggles que acompanham; ao alterar uma toggle/atributo, o servidor notifica somente os inscritos com uma requisição `PUT` contendo o novo valor.
- **Auditoria**: registrar quando e o quê foi alterado (sem rastrear usuário nesta fase).

Fora do escopo (fases posteriores):
- UI avançada de administração (API será suficiente).
- Replicação e alta disponibilidade (single instance será ok).

### Biblioteca Cliente

Incluso:
- **Carregamento de toggles**: dois modos de operação:
  - **Eager**: busca todas as toggles e atributos no startup e mantém em cache local.
  - **Lazy**: consulta o servidor a cada solicitação.
- **Cache local**: armazenar em memória o estado das toggles e atributos.
- **Endpoint de webhook**: expor um endpoint `PUT` (ex.: `PUT /_toggle/notify`) que recebe payload JSON com campos como `toggle`, `enabled` e `value` enviados pelo servidor a cada atualização.
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
   - Criar toggle com nome, descrição, status (ON/OFF) e exatamente um `attributeId` previamente cadastrado.
   - Após criada, permitir adicionar/remover valores da allow list para esse atributo via endpoints específicos.
   - Retornar lista de todas as toggles com suas configurações (estado, atributo e allow list).
   - Exemplo de payload de criação:

```json
{
  "name": "toggle-teste",
  "description": "exemplo",
  "enabled": true,
  "attributeId": "attribute-uuid",
  "allowList": ["foo", "bar"]
}
```

3. **Avaliação de Toggle**:
   - Endpoint: `GET /api/toggles/{name}/evaluate?value=foo`
   - Retorna `{ enabled: true/false, reason: "..." }` baseado apenas em `enabled` e na allow list da toggle.
   - Lógica de avaliação: se a toggle estiver `enabled=true` e o valor estiver presente na allow list, retorna `true`; caso contrário, `false`.

4. **Registro de clientes**:
   - Clientes chamam `POST /api/clients/register` informando URL de callback `PUT` e as toggles que acompanham.
   - O servidor mantém essa lista e, ao detectar alteração, chama apenas os clientes inscritos com uma requisição `PUT` contendo o novo valor no corpo.

5. **Auditoria**: registrar logs de mudanças com quando e o quê foi alterado (sem identificação de usuário nesta fase).

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
- Segurança: autenticação simples via API key enviada em header (`X-API-Key`) obrigatória para todas as chamadas (leitura e escrita).
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
  - Entidades: `Attribute`, `Toggle`, `AuditLog`

- **Sistema de Notificações**
  - Mecanismo para chamar webhooks `PUT` dos clientes registrados e inscritos naquela toggle, enviando o novo valor e metadados da atualização

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
  - Expõe `PUT /_toggle/notify` para receber notificações do servidor com payload JSON (`toggle`, `enabled`, `value`, etc.).
  - Ao receber, recarrega toggles e atributos e aplica o novo valor recebido.

- **Registro automático**: ao iniciar, registra-se no servidor com sua URL de callback `PUT` e lista das toggles de interesse.

### Endpoints da API

**Servidor - Atributos**:
- `GET /api/attributes` — retorna lista de atributos
- `POST /api/attributes` — cria novo atributo (admin)
- `PUT /api/attributes/{id}` — atualiza atributo (admin)
- `DELETE /api/attributes/{id}` — remove atributo (admin)

**Servidor - Toggles**:
- `GET /api/toggles` — retorna lista de toggles e suas configurações (Eager)
- `GET /api/toggles/{name}` — retorna toggle específica
- `GET /api/toggles/{name}/evaluate?value=foo` — avalia toggle para o valor fornecido do atributo vinculado
- `POST /api/toggles` — cria toggle (admin)
- `PUT /api/toggles/{name}` — atualiza toggle (admin)
- `DELETE /api/toggles/{name}` — remove toggle (admin)

**Servidor - Allow List** (por toggle):
- `POST /api/toggles/{name}/allow-list` — adiciona valor à allow list (admin)
- `DELETE /api/toggles/{name}/allow-list/{value}` — remove valor da allow list (admin)

**Servidor - Gerenciamento de Clientes**:
- `POST /api/clients/register` — cliente se registra informando sua URL de callback `PUT` e as toggles que deseja acompanhar (ex.: `{ "callbackUrl": "http://cliente:8080/_toggle/notify", "toggles": ["feature-a","feature-b"] }`)
- `DELETE /api/clients/{clientId}` — cliente se desregistra
- `GET /api/toggles/{name}/clients` — retorna lista de clientes conectados que dependem da toggle (admin)

**Cliente - Notificação de Atualização** (exposto pela biblioteca):
- `PUT /_toggle/notify` — servidor chama para notificar alterações enviando payload JSON com `toggle`, `enabled` e `value`.

## Modelo de Dados (exemplo)

**Attribute**:
- id: UUID
- name: string (unique, ex.: "userId", "currency", "supplierId")
- description: string (opcional)
- dataType: enum (STRING, NUMBER, DATE)
- createdAt, updatedAt

**Toggle**:
- id: UUID
- name: string (unique)
- description: string
- enabled: boolean
- attributeId: UUID de um atributo previamente cadastrado (obrigatório no ato da criação da toggle)
- allowList: List<String> com valores autorizados para o atributo
- createdAt, updatedAt

**Avaliação de Toggle**:
- Cada toggle avalia somente o valor do atributo vinculado. Caso o parâmetro correspondente não seja enviado, o resultado padrão é `false`.
- Se `enabled=false`, sempre retorna `false`.
- Se `enabled=true`, retorna `true` quando o valor informado aparece na allow list; caso contrário, `false`.

## Critérios de Avaliação / Sucesso

- Implementação mínima do servidor com CRUD de atributos e toggles.
- Endpoint de avaliação de toggle funcionando corretamente considerando o atributo vinculado, allow list e flag `enabled`.
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
