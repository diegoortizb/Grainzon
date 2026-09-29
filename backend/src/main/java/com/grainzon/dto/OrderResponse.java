package com.grainzon.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public record OrderResponse(
		Integer id,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
		LocalDate orderDate,
		double orderValue,
		List<OrderItemResponse> items) {

}
