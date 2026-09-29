package com.grainzon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
		@NotBlank
		@Size(max = CreateProductRequest.NAME_MAX_LENGTH, message = "must be at most {max} characters")
		@Pattern(regexp = "\\P{Cc}*", message = "must not contain control characters")
		String name,
		@PositiveOrZero
		Double itemPrice) {

	public static final int NAME_MAX_LENGTH = 256;

	// Trim before validation runs, so the length limit applies to the name that is actually stored.
	// A missing price defaults to 0, matching the column default.
	public CreateProductRequest {
		name = name == null ? null : name.strip();
		itemPrice = itemPrice == null ? 0.0 : itemPrice;
	}

}
