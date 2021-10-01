package com.example.blazepersistencesample.infrastructure.jpa.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

	@Test
	@DisplayName("クエリー ルートに別名を付与できる`create`メソッドのオーバーロードを使うことにより、明示的にクエリー ルートを指定する（短縮版）")
	void testGetCatListWithAliasedRootWithShorthand() {

		final int arbitraryCatListSize = 10;
		catTestUtil.initializeNameOrderedList(arbitraryCatListSize);

		CriteriaBuilder<Cat> cb = cbf.create(em, Cat.class, "myCat");
		final List<Cat> cats = cb.getResultList();

		catTestUtil.assertResultListIsNotEmpty(cats);
		assertEquals(arbitraryCatListSize, cats.size());

		final String processName = "Select cat list with aliased query root with shorthand by Blaze Persistence";
		testLogUtil.outputResultList(processName, cats);
	}

	@Test
	@DisplayName("クエリー ルートに別名を付与できる`create`メソッドのオーバーロードを使うことにより、明示的にクエリー ルートを指定する（非短縮版）")
	void testGetCatListWithAliasedRootWithoutShorthand() {

		final int arbitraryCatListSize = 10;
		catTestUtil.initializeNameOrderedList(arbitraryCatListSize);

		// @formatter:off
		CriteriaBuilder<Cat> cb = cbf.create(em, Cat.class)
				.from(Cat.class, "myCat");
		// @formatter:on

		final List<Cat> cats = cb.getResultList();

		catTestUtil.assertResultListIsNotEmpty(cats);
		assertEquals(arbitraryCatListSize, cats.size());

		final String processName = "Select cat list with aliased query root without shorthand by Blaze Persistence";
		testLogUtil.outputResultList(processName, cats);
	}

	@Test
	@DisplayName("相対パスの表現を使う（単一のルート エンティティのため、クエリー ルートの指定は不要）")
	void testGetCatNameListWithRelativePath() {

		final int arbitraryCatListSize = 10;
		catTestUtil.initializeNameOrderedList(arbitraryCatListSize);

		// @formatter:off
		CriteriaBuilder<Cat> cb = cbf.create(em, Cat.class, "myCat")
				.select("name");
		// @formatter:on

		final List<Cat> cats = cb.getResultList();

		catTestUtil.assertResultListIsNotEmpty(cats);
		assertEquals(arbitraryCatListSize, cats.size());

		final String processName = "Select cat list with relative path by Blaze Persistence";
		testLogUtil.outputResultList(processName, cats);
	}

	@Test
	@DisplayName("複数のクエリー ルートを使用している時に、相対パスを使うと例外が発生する")
	void testThownExceptionWhenUsingRelativePathWithMultipleQueryRoots() {

		// @formatter:off
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			CriteriaBuilder<String> cb = cbf.create(em, String.class)
					.from(Cat.class, "c")
					.from(Person.class, "p")
					.select("name");
			List<String> cats = cb.getResultList();
		});
		// @formatter:on
		log.info("★exception: {}", exception.toString());
	}
}
