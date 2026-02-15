package com.globo.subscription.adapter.http.controller.subscription.spec;

import com.globo.subscription.adapter.http.dto.ActiveSubscriptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Especificação da API para consulta de assinatura ativa.
 * <p>
 * Interface profissional para documentação Swagger/OpenAPI do endpoint de consulta de assinatura ativa.
 * </p>
 * @author Subscription Management Team
 * @version 1.0
 */
@Tag(
    name = "Assinaturas Ativas",
    description = "API para consulta da assinatura ativa de um usuário. Permite obter detalhes da assinatura vigente, caso exista."
)
public interface GetActiveSubscriptionControllerSpec {

    /**
     * Consulta a assinatura ativa de um usuário.
     * <p>
     * Retorna os dados da assinatura vigente, caso exista. Caso o usuário não possua assinatura ativa, retorna HTTP 404.
     * </p>
     * @param userId UUID do usuário
     * @return ResponseEntity com os dados da assinatura ativa ou 404 se não existir
     */
    @Operation(
        summary = "Consultar assinatura ativa",
        description = "Consulta a assinatura ativa de um usuário pelo seu UUID. Retorna os detalhes da assinatura vigente, incluindo plano, status, datas e identificador. Caso não exista assinatura ativa, retorna 404.",
        parameters = {
            @Parameter(
                name = "userId",
                description = "UUID do usuário para consulta da assinatura ativa",
                required = true,
                example = "123e4567-e89b-12d3-a456-426614174000",
                schema = @Schema(type = "string", format = "uuid")
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Assinatura ativa encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ActiveSubscriptionResponse.class),
                examples = @ExampleObject(
                    name = "Assinatura ativa",
                    description = "Exemplo de resposta de assinatura ativa",
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
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não possui assinatura ativa",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = com.globo.subscription.adapter.http.exception.exceptionhandler.Problem.class),
                                examples = @ExampleObject(
                                        name = "Assinatura não encontrada",
                                        value = """
                                        {
                                            "detail": "Usuário não possui assinatura ativa",
                                            "status": 404,
                                            "timestamp": "2026-02-15T10:30:00.000Z",
                                            "title": "Recurso não encontrado",
                                            "uri": "https://globo.com/resource-not-found",
                                            "userMessage": "Usuário não possui assinatura ativa"
                                        }
                                        """
                                )
                        )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno ao consultar assinatura ativa",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = com.globo.subscription.adapter.http.exception.exceptionhandler.Problem.class),
                                examples = @ExampleObject(
                                        name = "Erro interno",
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
    ResponseEntity<ActiveSubscriptionResponse> getActiveSubscription(@PathVariable UUID userId);
}
