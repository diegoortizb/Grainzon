package com.grainzon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String name;

	protected Product() {
	}

	public Product(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
