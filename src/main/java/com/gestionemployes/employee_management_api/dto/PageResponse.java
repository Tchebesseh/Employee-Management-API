package com.gestionemployes.employee_management_api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PageResponse<T> extends PageImpl<T> {

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public PageResponse(@JsonProperty("content") List<T> content, @JsonProperty("number") int number,
			@JsonProperty("size") int size, @JsonProperty("totalElements") Long totalElements,
			@JsonProperty("pageable") JsonNode pageable, @JsonProperty("last") boolean last,
			@JsonProperty("totalPages") int totalPages, @JsonProperty("sort") JsonNode sort,
			@JsonProperty("first") boolean first, @JsonProperty("numberOfElements") int numberOfElements) {
		super(content, PageRequest.of(number, size), totalElements);
	}

	public PageResponse(List<T> content) {
		super(content);
	}

	public PageResponse() {
		super(List.of());
	}
}
