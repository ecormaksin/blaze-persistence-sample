package com.example.blazepersistencesample.infrastructure.jpa.utilities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.stereotype.Component;

import com.example.blazepersistencesample.infrastructure.jpa.entity.Cat;
import com.example.blazepersistencesample.infrastructure.jpa.repository.JpaCatRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public final class CatTestUtility {

	private final int AGE_MIN = 0;

	/** 猫の最高齢 */
	// 世界一の長生き猫は38歳3日
	// https://www.axa-direct.co.jp/pet/pet-ms/detail/4992/#:~:text=%E4%B8%96%E7%95%8C%E4%B8%80%E3%81%AE%E9%95%B7%E7%94%9F%E3%81%8D%E7%8C%AB,%E3%81%99%E3%82%8B%E3%81%A8%E3%81%AA%E3%82%93%E3%81%A8%E7%B4%84170%E6%AD%B3%EF%BC%81
	private final int AGE_MAX = 38;

	private final JpaCatRepository repository;

	private RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

	public Cat initializeCat(final String name) {
		return initializeCat(name, null, null);
	}

	public Cat initializeCat(final String name, final Integer age, final Integer noOfKittens) {

		Set<Cat> kittens = initializeKittens(name, noOfKittens);

		// @formatter:off
		return Cat.builder()
				.name(name)
				.age(Objects.isNull(age) ? randomAge() : age)
				.kittens(kittens)
				.build();
		// @formatter:on
	}

	private Set<Cat> initializeKittens(final String parentName, final Integer noOfKittens) {

		if (Objects.isNull(noOfKittens) || noOfKittens.intValue() <= 0) {
			return Collections.emptySet();
		}

		List<Cat> kittensList = new ArrayList<>();

		for (int i = 0; i < noOfKittens; i++) {
			// @formatter:off
			kittensList.add( 
					Cat.builder()
						.name(parentName + " Jr." + (i + 1))
						.age(randomDataGenerator.nextInt(0, 1))
						.build()
				);
			// @formatter:on
		}

		kittensList = repository.saveAllAndFlush(kittensList);

		return new HashSet<Cat>(kittensList);
	}

	public void initializeNameOrderedList(final int maxSize) {

		repository.deleteAll();
		List<Cat> catsList = new ArrayList<>();
		// @formatter:off
		for (int i = 1; i <= maxSize; i++) {
			catsList.add(
				Cat.builder()
					.name("Cat" + i)
					.age(randomAge()) 
					.build()
				);
		}
		// @formatter:on
		repository.saveAllAndFlush(catsList);
	}

	private Integer randomAge() {
		return Integer.valueOf(randomDataGenerator.nextInt(AGE_MIN, AGE_MAX));
	}

	public void assertResultListIsNotEmpty(final List<?> resultList) {
		assertNotNull(resultList);
		assertFalse(resultList.isEmpty());
	}

}
