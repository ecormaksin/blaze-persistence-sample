package com.example.blazepersistencesample.infrastructure.jpa.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.blazepersistencesample.infrastructure.jpa.entity.Cat;

import lombok.extern.slf4j.Slf4j;

@DataJpaTest
@Slf4j
class JpaCatRepositoryTest {

	@Autowired
	private JpaCatRepository repository;

	@Test
	@DisplayName("猫のリストを取得できる")
	void testGetCatList() {

		// @formatter:off
		repository.save(
				Cat.builder()
					.id(1L)
					.name("フク")
					.age(5)
					.build()
				);
		repository.save(
				Cat.builder()
					.id(2L)
					.name("あかり")
					.age(3)
					.build()
				);
		// @formatter:on

		List<Cat> cats = repository.findAll();

		assertNotNull(cats);
		assertEquals(2, cats.size());

		// @formatter:off
		cats.stream().forEach(c -> {
			log.info(c.toString());
		});
		// @formatter:on
	}

}
