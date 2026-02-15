package com.globo.subscription.adapter.http.controller.user.spec;

import com.globo.subscription.adapter.http.dto.PagedResponse;
import com.globo.subscription.adapter.http.dto.user.UserRequest;
import com.globo.subscription.adapter.http.dto.user.UserResponse;
import com.globo.subscription.adapter.http.dto.user.UserUpdateRequest;
import com.globo.subscription.adapter.http.exception.exceptionhandler.Problem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
    name = "Usuários",
    description = "API para gerenciamento de usuários. Permite criar, atualizar, buscar, listar e remover usuários do sistema."
)
public interface UserControllerSpec {

    @Operation(
        summary = "Listar usuários",
        description = "Retorna uma lista paginada de usuários, com filtros opcionais por email e nome."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de usuários retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PagedResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno ao listar usuários",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Problem.class),
                examples = @ExampleObject(
                    name = "Erro interno",
                    value = """
                    {
                      "detail": "Erro ao listar usuários",
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
    @GetMapping
    ResponseEntity<PagedResponse<UserResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name);

    @Operation(
        summary = "Criar usuário",
        description = "Cria um novo usuário no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário criado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos ou usuário já existe",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Problem.class),
                examples = @ExampleObject(
                    name = "Usuário já existe",
                    value = """
                    {
                      "detail": "Usuário já cadastrado com este email",
                      "status": 400,
                      "timestamp": "2026-02-15T15:50:12.430168-03:00",
                      "title": "Erro de negócio",
                      "uri": "https://globo.com/business-error",
                      "userMessage": "Já existe um usuário com este email"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno ao criar usuário",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Problem.class),
                examples = @ExampleObject(
                    name = "Erro interno",
                    value = """
                    {
                      "detail": "Erro ao criar usuário",
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
    ResponseEntity<UserResponse> create(@RequestBody UserRequest userRequest);

    @Operation(
        summary = "Atualizar usuário",
        description = "Atualiza os dados de um usuário existente."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário atualizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Problem.class),
                examples = @ExampleObject(
                    name = "Usuário não encontrado",
                    value = """
                    {
                      "detail": "Usuário não encontrado com ID: 123e4567-e89b-12d3-a456-426614174000",
                      "status": 404,
                      "timestamp": "2026-02-15T15:50:12.430168-03:00",
                      "title": "Recurso não encontrado",
                      "uri": "https://globo.com/resource-not-found",
                      "userMessage": "Usuário não encontrado"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno ao atualizar usuário",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Problem.class),
                examples = @ExampleObject(
                    name = "Erro interno",
                    value = """
                    {
                      "detail": "Erro ao atualizar usuário",
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
    @PutMapping("/{id}")
    ResponseEntity<UserResponse> update(@PathVariable UUID id, @RequestBody UserUpdateRequest userRequest);

    @Operation(
        summary = "Buscar usuário por ID",
        description = "Retorna os dados de um usuário pelo seu identificador único."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Problem.class),
                examples = @ExampleObject(
                    name = "Usuário não encontrado",
                    value = """
                    {
                      "detail": "Usuário não encontrado com ID: 123e4567-e89b-12d3-a456-426614174000",
                      "status": 404,
                      "timestamp": "2026-02-15T15:50:12.430168-03:00",
                      "title": "Recurso não encontrado",
                      "uri": "https://globo.com/resource-not-found",
                      "userMessage": "Usuário não encontrado"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno ao buscar usuário",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Problem.class),
                examples = @ExampleObject(
                    name = "Erro interno",
                    value = """
                    {
                      "detail": "Erro ao buscar usuário",
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
    @GetMapping("/{id}")
    ResponseEntity<UserResponse> findById(@PathVariable UUID id);

    @Operation(
        summary = "Remover usuário",
        description = "Remove um usuário do sistema pelo seu identificador único."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Usuário removido com sucesso"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Problem.class),
                examples = @ExampleObject(
                    name = "Usuário não encontrado",
                    value = """
                    {
                      "detail": "Usuário não encontrado com ID: 123e4567-e89b-12d3-a456-426614174000",
                      "status": 404,
                      "timestamp": "2026-02-15T15:50:12.430168-03:00",
                      "title": "Recurso não encontrado",
                      "uri": "https://globo.com/resource-not-found",
                      "userMessage": "Usuário não encontrado"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno ao remover usuário",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Problem.class),
                examples = @ExampleObject(
                    name = "Erro interno",
                    value = """
                    {
                      "detail": "Erro ao remover usuário",
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
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}
