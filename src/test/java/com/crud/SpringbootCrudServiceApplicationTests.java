package com.crud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // activates application-test.yaml (H2 in-memory, Flyway disabled)
class SpringbootCrudServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
