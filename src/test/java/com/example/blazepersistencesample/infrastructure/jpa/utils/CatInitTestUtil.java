package com.example.blazepersistencesample.infrastructure.jpa.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.stereotype.Component;

import com.example.blazepersistencesample.infrastructure.jpa.entity.Cat;
import com.example.blazepersistencesample.infrastructure.jpa.repository.JpaCatRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public final class CatInitTestUtil {

	private final JpaCatRepository repository;

	private RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

	public Cat initializeCat(final String name, final int age, final int noOfKittens) {

		Set<Cat> kittens = initializeKittens(name, noOfKittens);

		// @formatter:off
		return Cat.builder()
				.name(name)
				.age(age)
				.kittens(kittens)
				.build();
		// @formatter:on
	}

	private Set<Cat> initializeKittens(final String parentName, final int noOfKittens) {

		if (noOfKittens <= 0) {
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
}
