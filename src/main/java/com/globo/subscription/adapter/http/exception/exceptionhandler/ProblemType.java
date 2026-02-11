package com.globo.subscription.adapter.http.exception.exceptionhandler;

import lombok.Getter;

@Getter
public enum ProblemType {

	INTERNAL_SERVER_ERROR("/internal-server-error", "Erro interno do servidor"),
    BAD_REQUEST("/bad-request", "Requisição inválida"),
	VALIDATION_ERROR("/validation-error", "Erro de validação"), 
	USER_NOT_FOUND("/user-not-found", "Usuário não encontrado"),
	DUPLICATE_ENTITY("/duplicate-entity", "Entidade duplicada"),
	REGISTER_NOT_FOUND("/register-not-found", "Registro não encontrado"),
	BUSINESS_ERROR("/business-error", "Erro de negócio"),
    METHOD_NOT_ALLOWED("/method-not-allowed", "Método não permitido"),
    INVALID_DATA("/invalid-data", "Dados inválidos");

	private final String title;
	private final String uri;

	ProblemType(String path, String title) {
		this.uri = "https://globo.com" + path;
		this.title = title;
	}
}