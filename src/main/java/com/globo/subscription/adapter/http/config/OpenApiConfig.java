package com.globo.subscription.adapter.http.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Configuração do OpenAPI/Swagger para documentação da API.
 * <p>
 * Esta classe configura as informações gerais da API que serão exibidas
 * na interface do Swagger UI, incluindo título, descrição, versão,
 * informações de contato e servidores disponíveis.
 * </p>
 *
 * @author Subscription Management Team
 * @version 1.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configura as informações gerais da API OpenAPI.
     *
     * @return objeto OpenAPI configurado com informações da API
     */
    @Bean
    public OpenAPI subscriptionManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Subscription Management API")
                        .description("""
                            API RESTful para gerenciamento de assinaturas de usuários seguindo arquitetura hexagonal.
                            
                            ## Funcionalidades Principais
                            
                            - **Gerenciamento de Assinaturas**: Criação, cancelamento e atualização de status
                            - **Múltiplos Planos**: Suporte para planos BASIC (R$ 19,90), PREMIUM (R$ 39,90) e FAMILY (R$ 59,90)
                            - **Controle de Pagamentos**: Integração com serviço de carteira externa
                            - **Renovação Automática**: Sistema de renovação com tratamento de falhas
                            - **Mudança de Plano**: Upgrade/downgrade com ajustes financeiros automáticos
                            
                            ## Regras de Negócio
                            
                            - Apenas uma assinatura ativa por usuário
                            - Validação de saldo antes de operações financeiras
                            - Reembolso proporcional no cancelamento
                            - Máximo de 3 tentativas de renovação antes de suspensão
                            
                            ## Arquitetura
                            
                            O sistema segue os princípios da **Arquitetura Hexagonal (Ports & Adapters)**,
                            garantindo separação de responsabilidades e facilitando manutenção e testes.
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Subscription Management Team")
                                .email("support@subscription-management.com")
                                .url("https://github.com/globo/subscription-management"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Servidor de Desenvolvimento Local"))
                .addServersItem(new Server()
                        .url("https://api-dev.subscription-management.com")
                        .description("Servidor de Desenvolvimento"))
                .addServersItem(new Server()
                        .url("https://api.subscription-management.com")
                        .description("Servidor de Produção"));
    }
}
