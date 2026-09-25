package com.grainzon.dto;

import com.grainzon.entity.Product;

public record ProductResponse(Integer id, String name) {

	public static ProductResponse from(Product product) {
		return new ProductResponse(product.getId(), product.getName());
	}

}
