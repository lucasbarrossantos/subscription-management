# Subscription Management

Sistema de gerenciamento de assinaturas desenvolvido com Spring Boot seguindo a arquitetura hexagonal (Ports & Adapters). O sistema permite gerenciar assinaturas de usuários com múltiplos planos de assinatura e integra-se com um serviço externo de carteira para processamento de pagamentos.

## Funcionalidades

### Gerenciamento de Assinaturas
- **Criação de Assinaturas**: Permite criar novas assinaturas para usuários com planos específicos (BASIC, PREMIUM, FAMILY).
- **Cancelamento de Assinaturas**: Cancela assinaturas ativas, com reembolso proporcional se aplicável.
- **Renovação Automática**: Sistema de renovação automática com tentativas de cobrança (máximo 3 tentativas).
- **Suspensão por Falha**: Após 3 falhas consecutivas de renovação, a assinatura é suspensa.
- **Mudança de Plano**: Suporte para upgrade/downgrade de planos com ajustes financeiros:
  - **Upgrade**: Cobre apenas a diferença de preço.
  - **Downgrade**: Reembolsa a diferença.
  - **Mesmo preço**: Sem transação financeira.

### Gerenciamento de Usuários
- Cadastro e gerenciamento de usuários.
- Validação de dados e regras de negócio.

### Integração com Carteira Externa
- Validação de saldo antes de operações.
- Processamento de transações financeiras via serviço de carteira.
- Tratamento de erros de integração (saldo insuficiente, etc.).

### Outras Funcionalidades
- **Cache Redis**: Cache de assinaturas ativas para performance.
- **Eventos Kafka**: Publicação de eventos de pagamento para processamento assíncrono.
- **Logs Estruturados**: Logging detalhado para auditoria e debugging.
- **Internacionalização**: Suporte a mensagens em português e inglês.
- **Migrações de Banco**: Flyway para controle de versão do schema.

## Arquitetura

O projeto segue estritamente a **Arquitetura Hexagonal (Ports & Adapters)**:

```
core/
  ├── domain/          # Entidades de negócio (Subscription, User, enums)
  ├── port/
  │   ├── in/          # Portas de entrada (interfaces de casos de uso)
  │   └── out/         # Portas de saída (interfaces de repositório, serviço externo)
  ├── usecase/         # Lógica de negócio (implementação dos casos de uso)
  └── exception/       # Exceções de negócio

adapter/
  ├── datasource/      # Adaptadores de banco de dados (JPA, entidades, mappers)
  ├── http/            # Adaptadores REST (controladores, DTOs, mappers)
  └── integration/     # Adaptadores de integração externa (Feign clients)
```

### Princípios
- **Separação de Responsabilidades**: Core não depende de adapters.
- **Injeção de Dependências**: Uso de construtores com Lombok.
- **Mappers**: MapStruct para conversão entre camadas.
- **Exceções**: Tratamento centralizado com `@RestControllerAdvice`.

## Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.2+**
- **Spring Data JPA** com Hibernate
- **PostgreSQL** (banco de dados)
- **Flyway** (migrações)
- **MapStruct** (mapeamento de objetos)
- **Lombok** (redução de boilerplate)
- **OpenFeign** (clientes HTTP)
- **SpringDoc OpenAPI** (documentação Swagger/OpenAPI 3)
- **Redis** (cache)
- **Kafka** (eventos)
- **Maven** (gerenciamento de build)
- **Docker** (containerização)

## Pré-requisitos

- **Java 21** ou superior
- **Maven 3.8+**
- **Docker** e **Docker Compose** (para execução completa)
- **PostgreSQL** (ou usar Docker)
- **Redis** (ou usar Docker)
- **Kafka** (ou usar Docker)

## Instalação

### Clonagem do Repositório
```bash
git clone <url-do-repositorio>
cd subscription-management
```

### Configuração do Ambiente

1. **Variáveis de Ambiente**: Configure as seguintes variáveis no `application.yaml` ou via environment:
   - `spring.datasource.url`: URL do PostgreSQL
   - `spring.datasource.username`: Usuário do banco
   - `spring.datasource.password`: Senha do banco
   - `integrations.wallet.api.url`: URL da API de carteira externa
   - `spring.redis.host`: Host do Redis
   - `spring.kafka.bootstrap-servers`: Servidores Kafka

2. **Docker Compose**: Use o `compose.yaml` para subir os serviços necessários:
   ```bash
   docker-compose up -d
   ```

### Build do Projeto
```bash
mvn clean install
```

## Como Usar

### Executando a Aplicação

#### Via Maven
```bash
mvn spring-boot:run
```

#### Via Docker
```bash
docker build -t subscription-management .
docker run -p 8080:8080 subscription-management
```

A aplicação estará disponível em `http://localhost:8080`.

### Documentação da API (Swagger/OpenAPI)

A documentação interativa da API está disponível através do Swagger UI:
- **Swagger UI**: `http://localhost:8080/subscription-management/api/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/subscription-management/api/v3/api-docs`

A documentação inclui:
- Descrição detalhada de todos os endpoints
- Exemplos de requisições e respostas
- Códigos de status HTTP e cenários de erro
- Schemas de dados (DTOs)
- Possibilidade de testar os endpoints diretamente pela interface

### Endpoints da API

#### Assinaturas
- `POST /assinaturas` - Criar nova assinatura
  - Body: `{"usuarioId": "uuid", "plano": "BASIC|PREMIUM|FAMILY"}`
- `DELETE /assinaturas/{id}` - Cancelar assinatura
- `GET /assinaturas/ativas/{usuarioId}` - Obter assinatura ativa do usuário
- `POST /assinaturas/{id}/renovar` - Renovar assinatura manualmente

#### Usuários
- `POST /usuarios` - Criar usuário
  - Body: `{"nome": "string", "email": "string"}`
- `GET /usuarios/{id}` - Obter usuário por ID

### Usando o Postman

Importe a coleção Postman localizada em `scripts/collections_postman/Desafio Globo.postman_collection.json` e o ambiente `dev.postman_environment.json`.

### Testes

Execute os testes com Maven:
```bash
mvn test
```

Para testes de integração:
```bash
mvn verify
```

### Logs e Monitoramento

- Logs são estruturados com SLF4J.
- Níveis: INFO para operações bem-sucedidas, WARN para problemas recuperáveis, ERROR para exceções.
- Exemplo: `log.info("Assinatura criada com sucesso - id: {}", subscription.getId());`

## Regras de Negócio

- **Assinatura Única**: Um usuário pode ter apenas uma assinatura ativa por vez.
- **Planos**: 
  - BASIC: R$ 19,90/mês
  - PREMIUM: R$ 39,90/mês
  - FAMILY: R$ 59,90/mês
- **Transações**: Sempre validar saldo antes de debitar.
- **Renovação**: Máximo 3 tentativas; suspensão após falhas.

## Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

### Padrões de Código
- Siga os padrões definidos no `copilot-instructions.md`.
- Use commits descritivos.
- Mantenha cobertura de testes acima de 80%.

## Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

---

**Nota**: Este projeto é parte de um desafio técnico da Globo. Para dúvidas, consulte a documentação ou abra uma issue.