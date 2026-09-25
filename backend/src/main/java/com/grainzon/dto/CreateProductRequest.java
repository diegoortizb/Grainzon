package com.grainzon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.grainzon.entity.Product;

public record CreateProductRequest(@NotBlank @Size(max = Product.NAME_MAX_LENGTH) String name) {

	// Trim before validation runs, so the length limit applies to the name that is actually stored.
	public CreateProductRequest {
		name = name == null ? null : name.strip();
	}

}
