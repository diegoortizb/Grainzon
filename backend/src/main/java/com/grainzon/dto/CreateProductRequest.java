package com.grainzon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
		@NotBlank
		@Size(max = CreateProductRequest.NAME_MAX_LENGTH, message = "must be at most {max} characters")
		@Pattern(regexp = "\\P{Cc}*", message = "must not contain control characters")
		String name) {

	// The only place the limit is enforced on the server: the column is TEXT, so changing it needs no migration.
	public static final int NAME_MAX_LENGTH = 256;

	// Trim before validation runs, so the length limit applies to the name that is actually stored.
	public CreateProductRequest {
		name = name == null ? null : name.strip();
	}

}
