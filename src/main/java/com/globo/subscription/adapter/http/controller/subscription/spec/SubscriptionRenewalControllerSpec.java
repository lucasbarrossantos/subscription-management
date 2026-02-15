package com.globo.subscription.adapter.http.controller.subscription.spec;

import com.globo.subscription.adapter.http.dto.subscription.SubscriptionRenewalResponse;
import com.globo.subscription.adapter.http.exception.exceptionhandler.Problem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Especificação da API para renovação em lote de assinaturas.
 * <p>
 * Interface profissional para documentação Swagger/OpenAPI do endpoint de renovação de assinaturas.
 * </p>
 * @author Subscription Management Team
 * @version 1.0
 */
@Tag(
    name = "Renovação de Assinaturas",
    description = "API para renovação em lote de assinaturas. Permite processar a renovação automática de todas as assinaturas elegíveis, com controle de tentativas e tratamento de exceções de negócio."
)
public interface SubscriptionRenewalControllerSpec {

    /**
     * Processa a renovação em lote de assinaturas.
     * <p>
     * Executa a renovação automática de todas as assinaturas elegíveis, realizando as cobranças e atualizando o status conforme as regras de negócio. Retorna o resumo do processamento.
     * </p>
     * @return ResponseEntity com o resumo da renovação
     */
    @Operation(
        summary = "Renovar assinaturas em lote",
        description = "Processa a renovação automática de todas as assinaturas elegíveis. Para cada assinatura, realiza a cobrança, atualiza o status e registra tentativas. Em caso de falha de pagamento, incrementa o contador de tentativas e pode suspender a assinatura após 3 falhas consecutivas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Renovação processada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubscriptionRenewalResponse.class),
                examples = @ExampleObject(
                    name = "Renovação bem-sucedida",
                    value = """
                    {
                      "total": 10,
                      "renovadas": 8,
                      "assinaturas": [
                        {
                          "id": "987e6543-e21b-12d3-a456-426614174000",
                          "usuarioId": "123e4567-e89b-12d3-a456-426614174000",
                          "plano": "PREMIUM",
                          "dataInicio": "2026-02-15",
                          "dataExpiracao": "2026-03-15",
                          "status": "ACTIVE"
                        }
                      ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Erro de negócio ou validação",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Problem.class),
                examples = @ExampleObject(
                    name = "Erro de negócio",
                    value = """
                    {
                      "detail": "Erro ao consultar carteira do usuário 200cf4e5-2826-4edd-b467-77b5c3c4139b",
                      "status": 400,
                      "timestamp": "2026-02-15T15:50:12.430168-03:00",
                      "title": "Erro de negócio",
                      "uri": "https://globo.com/business-error",
                      "userMessage": "Erro ao consultar carteira do usuário 200cf4e5-2826-4edd-b467-77b5c3c4139b"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno ao processar renovação",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Problem.class),
                examples = @ExampleObject(
                    name = "Erro interno",
                    value = """
                    {
                      "detail": "Erro inesperado ao processar renovação de assinaturas",
                      "status": 500,
                      "timestamp": "2026-02-15T15:50:12.430168-03:00",
                      "title": "Erro interno do servidor",
                      "uri": "https://globo.com/internal-error",
                      "userMessage": "Ocorreu um erro inesperado. Tente novamente mais tarde."
                    }
                    """
                )
            )
        )
    })
    @PostMapping
    ResponseEntity<SubscriptionRenewalResponse> renewal();
}
