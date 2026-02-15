package com.globo.subscription.adapter.http.controller.subscription.spec;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.globo.subscription.adapter.http.dto.subscription.SubscriptionRequest;
import com.globo.subscription.adapter.http.dto.subscription.SubscriptionResponse;
import com.globo.subscription.adapter.http.dto.subscription.UpdateSubscriptionStatusRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Especificação da API de Gerenciamento de Assinaturas.
 * <p>
 * Esta interface define os contratos e documentação OpenAPI/Swagger para todas as operações
 * relacionadas ao gerenciamento de assinaturas no sistema.
 * </p>
 *
 * @author Subscription Management Team
 * @version 1.0
 */
@Tag(
    name = "Assinaturas",
    description = "API para gerenciamento de assinaturas de usuários. " +
                  "Permite criar, cancelar e atualizar status de assinaturas com diferentes planos (BASIC, PREMIUM, FAMILY). " +
                  "O sistema suporta apenas uma assinatura ativa por usuário e integra-se com serviço de carteira para processamento de pagamentos."
)
public interface SubscriptionControllerSpec {

    /**
     * Cria uma nova assinatura para um usuário.
     * <p>
     * Este endpoint permite a criação de uma nova assinatura vinculada a um usuário existente.
     * Antes de criar a assinatura, o sistema verifica se o usuário já possui uma assinatura ativa.
     * Apenas uma assinatura ativa é permitida por usuário. O sistema também valida o saldo disponível
     * na carteira do usuário antes de processar a transação financeira.
     * </p>
     *
     * @param request objeto contendo os dados necessários para criar a assinatura
     * @return ResponseEntity com os dados da assinatura criada e status HTTP 201 (Created)
     */
    @Operation(
        summary = "Criar nova assinatura",
        description = "Cria uma nova assinatura para um usuário com o plano selecionado. " +
                      "Valida se o usuário existe, se não possui assinatura ativa e se possui saldo suficiente na carteira. " +
                      "Após a validação, debita o valor do plano da carteira e ativa a assinatura.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da assinatura a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubscriptionRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Plano Básico",
                        description = "Exemplo de criação de assinatura com plano BASIC",
                        value = """
                        {
                          "usuarioId": "123e4567-e89b-12d3-a456-426614174000",
                          "plano": "BASIC"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Plano Premium",
                        description = "Exemplo de criação de assinatura com plano PREMIUM",
                        value = """
                        {
                          "usuarioId": "123e4567-e89b-12d3-a456-426614174000",
                          "plano": "PREMIUM"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Plano Família",
                        description = "Exemplo de criação de assinatura com plano FAMILY",
                        value = """
                        {
                          "usuarioId": "123e4567-e89b-12d3-a456-426614174000",
                          "plano": "FAMILY"
                        }
                        """
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Assinatura criada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubscriptionResponse.class),
                examples = @ExampleObject(
                    name = "Assinatura criada",
                    description = "Exemplo de resposta de assinatura criada com sucesso",
                    value = """
                    {
                      "id": "987e6543-e21b-12d3-a456-426614174000",
                      "usuarioId": "123e4567-e89b-12d3-a456-426614174000",
                      "plano": "PREMIUM",
                      "dataInicio": "2026-02-15",
                      "dataExpiracao": "2026-03-15",
                      "status": "ACTIVE"
                    }
                    """
                )
            ),
            headers = {
                @io.swagger.v3.oas.annotations.headers.Header(
                    name = "Location",
                    description = "URI da assinatura criada",
                    schema = @Schema(type = "string", example = "/subscriptions/987e6543-e21b-12d3-a456-426614174000")
                )
            }
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos ou usuário já possui assinatura ativa",
            content = @Content(
                mediaType = "application/json",
                                schema = @Schema(implementation = com.globo.subscription.adapter.http.exception.exceptionhandler.Problem.class),
                                examples = {
                                        @ExampleObject(
                                                name = "Assinatura já existe",
                                                description = "Erro quando usuário já possui assinatura ativa",
                                                value = """
                                                {
                                                    "detail": "Usuário já possui uma assinatura ativa",
                                                    "status": 400,
                                                    "timestamp": "2026-02-15T10:30:00.000Z",
                                                    "title": "Erro de negócio",
                                                    "uri": "https://globo.com/business-error",
                                                    "userMessage": "Usuário já possui uma assinatura ativa"
                                                }
                                                """
                                        ),
                                        @ExampleObject(
                                                name = "Validação falhou",
                                                description = "Erro quando dados obrigatórios não são fornecidos",
                                                value = """
                                                {
                                                    "detail": "Dados obrigatórios não informados",
                                                    "status": 400,
                                                    "timestamp": "2026-02-15T10:30:00.000Z",
                                                    "title": "Erro de validação",
                                                    "uri": "https://globo.com/validation-error",
                                                    "userMessage": "Preencha todos os campos obrigatórios",
                                                    "fields": [
                                                        {"name": "usuarioId", "userMessage": "não pode ser nulo"},
                                                        {"name": "plano", "userMessage": "não pode ser nulo"}
                                                    ]
                                                }
                                                """
                                        )
                                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content(
                mediaType = "application/json",
                                schema = @Schema(implementation = com.globo.subscription.adapter.http.exception.exceptionhandler.Problem.class),
                                examples = @ExampleObject(
                                        name = "Usuário não encontrado",
                                        value = """
                                        {
                                            "detail": "Usuário não encontrado com ID: 123e4567-e89b-12d3-a456-426614174000",
                                            "status": 404,
                                            "timestamp": "2026-02-15T10:30:00.000Z",
                                            "title": "Recurso não encontrado",
                                            "uri": "https://globo.com/resource-not-found",
                                            "userMessage": "Usuário não encontrado"
                                        }
                                        """
                                )
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Saldo insuficiente na carteira",
            content = @Content(
                mediaType = "application/json",
                                schema = @Schema(implementation = com.globo.subscription.adapter.http.exception.exceptionhandler.Problem.class),
                                examples = @ExampleObject(
                                        name = "Saldo insuficiente",
                                        value = """
                                        {
                                            "detail": "Saldo insuficiente na carteira para processar a assinatura",
                                            "status": 422,
                                            "timestamp": "2026-02-15T10:30:00.000Z",
                                            "title": "Erro de negócio",
                                            "uri": "https://globo.com/business-error",
                                            "userMessage": "Saldo insuficiente para concluir a operação"
                                        }
                                        """
                                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(
                mediaType = "application/json",
                                schema = @Schema(implementation = com.globo.subscription.adapter.http.exception.exceptionhandler.Problem.class),
                                examples = @ExampleObject(
                                        name = "Erro interno",
                                        value = """
                                        {
                                            "detail": "Erro ao processar a solicitação",
                                            "status": 500,
                                            "timestamp": "2026-02-15T10:30:00.000Z",
                                            "title": "Erro interno do servidor",
                                            "uri": "https://globo.com/internal-error",
                                            "userMessage": "Ocorreu um erro inesperado. Tente novamente mais tarde."
                                        }
                                        """
                                )
            )
        )
    })
    ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody SubscriptionRequest request);

    /**
     * Cancela uma assinatura existente.
     * <p>
     * Este endpoint permite o cancelamento de uma assinatura através do seu identificador único.
     * Ao cancelar, o sistema calcula o reembolso proporcional baseado no tempo restante da assinatura
     * e credita o valor na carteira do usuário. A assinatura é marcada como CANCELLED e não pode mais ser reativada.
     * </p>
     *
     * @param id identificador único da assinatura a ser cancelada
     * @return ResponseEntity com status HTTP 204 (No Content)
     */
    @Operation(
        summary = "Cancelar assinatura",
        description = "Cancela uma assinatura ativa e processa o reembolso proporcional ao tempo restante. " +
                      "O valor do reembolso é calculado automaticamente e creditado na carteira do usuário. " +
                      "Após o cancelamento, a assinatura não pode ser reativada.",
        parameters = {
            @Parameter(
                name = "id",
                description = "UUID da assinatura a ser cancelada",
                required = true,
                example = "987e6543-e21b-12d3-a456-426614174000",
                schema = @Schema(type = "string", format = "uuid")
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Assinatura cancelada com sucesso (sem conteúdo retornado)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Assinatura não encontrada",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Assinatura não encontrada",
                    value = """
                    {
                        "detail": "Assinatura não encontrada com id: 97c7a894-8fa6-4fb1-afe3-c7e0aa717a9c",
                        "status": 404,
                        "timestamp": "2026-02-15T16:12:05.375344-03:00",
                        "title": "Registro não encontrado",
                        "uri": "https://globo.com/register-not-found",
                        "userMessage": "Assinatura não encontrada com id: 97c7a894-8fa6-4fb1-afe3-c7e0aa717a9c"
                    }
                    """,
                    description = "Erro quando a assinatura a ser cancelada não é encontrada",
                    summary = "Assinatura não encontrada"
                )
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Assinatura não pode ser cancelada (já cancelada ou em status inválido)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Assinatura já cancelada",
                    value = """
                    {
                        "detail": "Assinatura a22ce7bc-bf9b-4982-a50d-9e471468e1e5 já foi cancelada.",
                        "status": 422,
                        "timestamp": "2026-02-15T16:00:41.509775-03:00",
                        "title": "Erro de negócio",
                        "uri": "https://globo.com/business-error",
                        "userMessage": "Assinatura a22ce7bc-bf9b-4982-a50d-9e471468e1e5 já foi cancelada."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno ao processar o cancelamento",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Erro no reembolso",
                    value = """
                    {
                        "detail": "Erro ao consultar assinatura ativa",
                        "status": 500,
                        "timestamp": "2026-02-15T10:30:00.000Z",
                        "title": "Erro interno do servidor",
                        "uri": "https://globo.com/internal-error",
                        "userMessage": "Ocorreu um erro inesperado. Tente novamente mais tarde."
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Void> cancel(@PathVariable UUID id);

    /**
     * Atualiza o status de uma assinatura.
     * <p>
     * Este endpoint permite atualizar o status de uma assinatura existente.
     * É utilizado principalmente para processos internos como renovação automática,
     * suspensão por falha de pagamento ou reativação. Os status possíveis são:
     * ACTIVE, CANCELLED, SUSPENDED, EXPIRED.
     * </p>
     *
     * @param request objeto contendo o ID da assinatura e o novo status
     * @return ResponseEntity com status HTTP 204 (No Content)
     */
    @Operation(
        summary = "Atualizar status da assinatura",
        description = "Atualiza o status de uma assinatura existente. Este endpoint é utilizado para operações internas " +
                      "como renovação automática, suspensão por falha de pagamento ou reativação de assinatura. " +
                      "Requer permissões administrativas para execução.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados para atualização do status da assinatura",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UpdateSubscriptionStatusRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Ativar assinatura",
                        description = "Atualiza status para ACTIVE",
                        value = """
                        {
                          "subscriptionId": "987e6543-e21b-12d3-a456-426614174000",
                          "status": "ACTIVE"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Suspender assinatura",
                        description = "Atualiza status para SUSPENDED (após falhas de renovação)",
                        value = """
                        {
                          "subscriptionId": "987e6543-e21b-12d3-a456-426614174000",
                          "status": "SUSPENDED"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Expirar assinatura",
                        description = "Atualiza status para EXPIRED",
                        value = """
                        {
                          "subscriptionId": "987e6543-e21b-12d3-a456-426614174000",
                          "status": "EXPIRED"
                        }
                        """
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Status atualizado com sucesso (sem conteúdo retornado)"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Status inválido ou transição de status não permitida",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Status inválido",
                    value = """
                    {
                        "detail": "O corpo da requisição está inválido. Verifique erro de sintaxe.",
                        "status": 400,
                        "timestamp": "2026-02-15T16:02:53.998511-03:00",
                        "title": "Dados inválidos",
                        "uri": "https://globo.com/invalid-data",
                        "userMessage": "O corpo da requisição está inválido. Verifique erro de sintaxe."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Assinatura não encontrada",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Assinatura não encontrada",
                    value = """
                    {
                        "detail": "Assinatura não encontrada com id: 97c7a894-8fa6-4fb1-afe3-c7e0aa717a9c",
                        "status": 404,
                        "timestamp": "2026-02-15T16:10:07.309817-03:00",
                        "title": "Registro não encontrado",
                        "uri": "https://globo.com/register-not-found",
                        "userMessage": "Assinatura não encontrada com id: 97c7a894-8fa6-4fb1-afe3-c7e0aa717a9c"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflito ao atualizar assinatura (status já é o mesmo ou transição não permitida)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Conflito ao atualizar assinatura",
                    value = """
                    {
                        "detail": "Status da assinatura a22ce7bc-bf9b-4982-a50d-9e471468e1e5 já é PENDING",
                        "status": 409,
                        "timestamp": "2026-02-15T16:08:11.869616-03:00",
                        "title": "Erro de negócio",
                        "uri": "https://globo.com/business-error",
                        "userMessage": "Status da assinatura a22ce7bc-bf9b-4982-a50d-9e471468e1e5 já é PENDING"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno ao atualizar status",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Erro interno",
                    value = """
                    {
                      "timestamp": "2026-02-15T10:30:00.000Z",
                      "status": 500,
                      "error": "Internal Server Error",
                      "message": "Erro ao atualizar status da assinatura",
                      "path": "/subscriptions/status"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Void> updateStatus(@Valid @RequestBody UpdateSubscriptionStatusRequest request);
}
