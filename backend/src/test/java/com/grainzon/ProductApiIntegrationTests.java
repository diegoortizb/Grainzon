package com.grainzon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import tools.jackson.databind.json.JsonMapper;
import com.grainzon.entity.Product;
import com.grainzon.repository.ProductRepository;

/**
 * End-to-end through every layer against a real Postgres in Docker (Testcontainers), with the real Flyway migrations.
 * This is what the unit tests can't catch: migrations, the entity-to-table mapping and Postgres-specific behavior.
 * Needs Docker running.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ProductApiIntegrationTests {

	// Same major version as docker-compose.yml. Shared by all tests in this class; the table is emptied before each.
	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

	private static final JsonMapper json = new JsonMapper();

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ProductRepository repository;

	@BeforeEach
	void emptyTable() {
		repository.deleteAll();
	}

	@Test
	void createdProductIsStoredTrimmedAndListed() throws Exception {
		int id = create("  Cordless Drill  ");

		// Read back through JPA as well as the API, so Hibernate maps a real row to the entity.
		Product stored = repository.findById(id).orElseThrow();
		assertThat(stored.getName()).isEqualTo("Cordless Drill");

		mvc.perform(get("/api/products"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].id").value(id))
			.andExpect(jsonPath("$.content[0].name").value("Cordless Drill"))
			.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void namesRoundTripExactly() throws Exception {
		List<String> names = List.of("3/4\" Hex Bolt", "Nut & Washer Kit", "O'Brien Pliers", "Café Mug", "M8×1.25 Screw",
				"Toolbox 🧰", "Robert'); DROP TABLE products; --", "a".repeat(256));
		for (String name : names) {
			create(name);
		}

		mvc.perform(get("/api/products").param("size", "100"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[*].name").value(contains(names.toArray())));
		assertThat(repository.count()).isEqualTo(names.size());
	}

	@Test
	void listIsSortedByIdAndPaged() throws Exception {
		for (int i = 1; i <= 12; i++) {
			create("Product " + i);
		}

		mvc.perform(get("/api/products").param("page", "1").param("size", "5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[*].name").value(contains("Product 6", "Product 7", "Product 8", "Product 9",
					"Product 10")))
			.andExpect(jsonPath("$.totalElements").value(12))
			.andExpect(jsonPath("$.totalPages").value(3));

		mvc.perform(get("/api/products").param("page", "3").param("size", "5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isEmpty())
			.andExpect(jsonPath("$.totalPages").value(3));
	}

	@Test
	void invalidNamesAreRejectedAndNothingIsStored() throws Exception {
		// Too long, and NUL, which Postgres itself would refuse with a 500 if validation let it through.
		for (String name : List.of("a".repeat(257), "bad\u0000name")) {
			mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body(name)))
				.andExpect(status().isBadRequest());
		}

		assertThat(repository.count()).isZero();
	}

	@Test
	void mainStartsTheApplication() {
		// Runs the real entry point against the test database on a random port, then shuts it down once it is ready.
		AtomicBoolean ready = new AtomicBoolean();
		SpringApplication.withHook(application -> new SpringApplicationRunListener() {
			@Override
			public void ready(ConfigurableApplicationContext context, Duration timeTaken) {
				ready.set(true);
				context.close();
			}
		}, () -> BackendApplication.main(new String[] { "--server.port=0",
				"--spring.datasource.url=" + postgres.getJdbcUrl(), "--spring.datasource.username=" + postgres.getUsername(),
				"--spring.datasource.password=" + postgres.getPassword() }));

		assertThat(ready).isTrue();
	}

	private int create(String name) throws Exception {
		String response = mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body(name)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return json.readTree(response).get("id").asInt();
	}

	private static String body(String name) throws Exception {
		return json.writeValueAsString(new Name(name));
	}

	private record Name(String name) {
	}

}
