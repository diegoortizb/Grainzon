package com.grainzon.product;

public record ProductResponse(Integer id, String name) {

	static ProductResponse from(Product product) {
		return new ProductResponse(product.getId(), product.getName());
	}

}
