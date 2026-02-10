package com.globo.subscription.application.exceptionhandler;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
public class Problem {

	private Integer status;
	private String uri;
	private String title;
	private String detail;
	private String userMessage;
	private OffsetDateTime timestamp;
    private String instance;
	private List<Field> fields;

	@Builder
	@Getter
	public static class Field {
        
		private String name;
        private String userMessage;
	}
}
