package com.example.blazepersistencesample.infrastructure.jpa.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.example.blazepersistencesample.TestLogUtility;
import com.example.blazepersistencesample.infrastructure.jpa.BlazePersistenceConfiguration;
import com.example.blazepersistencesample.infrastructure.jpa.entity.Cat;
import com.example.blazepersistencesample.infrastructure.jpa.entity.Person;
import com.example.blazepersistencesample.infrastructure.jpa.utilities.CatTestUtility;

import lombok.extern.slf4j.Slf4j;

@DataJpaTest
@Import(value = { BlazePersistenceConfiguration.class, CatTestUtility.class })
@Slf4j
class _03_From_clause {

	@Autowired
	private CriteriaBuilderFactory cbf;

	@Autowired
	private EntityManager em;

	@Autowired
	private JpaCatRepository catRepository;

	@Autowired
	private JpaPersonRepository personRepository;

	@Autowired
	private CatTestUtility catTestUtil;

	private TestLogUtility testLogUtil = new TestLogUtility(log);

	@Test
	@DisplayName("飼い猫のリストを表示する")
	void testGetPetCatList() {

		initializePersonSampleHinata();
		initializePersonSampleAoi();

		// @formatter:off
		CriteriaBuilder<Cat> cb = cbf.create(em, Cat.class)
				.from(Person.class, "person")
				.select("person.kittens");
		// @formatter:on

		final List<Cat> cats = cb.getResultList();
		assertNotNull(cats);
		assertFalse(cats.isEmpty());

		final String processName = "Select cat list from an other root entity";
		testLogUtil.outputResultList(processName, cats);
	}

	private void initializePersonSampleHinata() {

		// @formatter:off
		List<Cat> cats = new ArrayList<>();
		cats.add(catTestUtil.initializeCat("ムギ"));
		cats.add(catTestUtil.initializeCat("ソラ"));
		cats = catRepository.saveAllAndFlush(cats);
		Person person = Person.builder()
							.name("陽葵")
							.kittens(new HashSet<Cat>(cats))
							.build();
		personRepository.saveAndFlush(person);
		// @formatter:on
	}

	private void initializePersonSampleAoi() {

		// @formatter:off
		List<Cat> cats = new ArrayList<>();
		cats.add(catTestUtil.initializeCat("レオ"));
		cats.add(catTestUtil.initializeCat("ココ"));
		cats.add(catTestUtil.initializeCat("マロン"));
		cats = catRepository.saveAllAndFlush(cats);
		Person person = Person.builder()
				.name("蒼")
				.kittens(new HashSet<Cat>(cats))
				.build();
		personRepository.saveAndFlush(person);
		// @formatter:on
	}

}
