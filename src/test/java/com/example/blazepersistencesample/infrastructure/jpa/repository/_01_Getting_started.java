package com.example.blazepersistencesample.infrastructure.jpa.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import com.example.blazepersistencesample.TestLogUtility;
import com.example.blazepersistencesample.infrastructure.jpa.BlazePersistenceConfiguration;
import com.example.blazepersistencesample.infrastructure.jpa.entity.Cat;
import com.example.blazepersistencesample.infrastructure.jpa.utilities.CatTestUtility;

import lombok.extern.slf4j.Slf4j;

@DataJpaTest
@Import(value = { BlazePersistenceConfiguration.class, CatTestUtility.class })
@Slf4j
class _01_Getting_started {

	private static final int MAX_CATS_SIZE = 23;

	@Autowired
	private JpaCatRepository repository;

	@Autowired
	private CriteriaBuilderFactory cbf;

	@Autowired
	private EntityManager em;

	@Autowired
	private CatTestUtility catTestUtil;

	private TestLogUtility testLogUtil = new TestLogUtility(log);

	@BeforeEach
	void setup() {
		catTestUtil.initializeNameOrderedList(MAX_CATS_SIZE);
	}

	@Test
	@DisplayName("猫のリストを取得できる（JPA版）")
	void testGetCatListByJpa() {

		final List<Cat> cats = repository.findAll();

		catTestUtil.assertResultListIsNotEmpty(cats);

		final String processName = "Select cat list by JPA";
		testLogUtil.outputResultList(processName, cats);
	}

	@Test
	@DisplayName("猫のリストを取得できる（Blaze Persistence版）")
	void testGetCatListByBlazePersistence() {

		CriteriaBuilder<Cat> cb = cbf.create(em, Cat.class);
		final List<Cat> cats = cb.getResultList();

		catTestUtil.assertResultListIsNotEmpty(cats);

		final String processName = "Select cat list by Blaze Persistence";
		testLogUtil.outputResultList(processName, cats);
	}

	@Test
	@DisplayName("猫の年齢のみを取得する（エイリアスなし）")
	void testGetCatAgeListWithoutAlias() {

		// @formatter:off
		CriteriaBuilder<Integer> cb = cbf.create(em, Integer.class)
				.from(Cat.class)
				.select("cat.age");
		// @formatter:on

		final List<Integer> ages = cb.getResultList();

		catTestUtil.assertResultListIsNotEmpty(ages);

		final String processName = "Select cat age list without alias by Blaze Persistence";
		testLogUtil.outputResultList(processName, ages);
	}

	@Test
	@DisplayName("猫の年齢のみを取得する（エイリアスあり）")
	void testGetCatAgeListWithAlias() {

		// @formatter:off
		CriteriaBuilder<Integer> cb = cbf.create(em, Integer.class)
				.from(Cat.class, "c")
				.select("c.age");
		// @formatter:on

		final List<Integer> ages = cb.getResultList();

		catTestUtil.assertResultListIsNotEmpty(ages);

		final String processName = "Select cat age list with alias by Blaze Persistence";
		testLogUtil.outputResultList(processName, ages);
	}

	@Test
	@DisplayName("年齢が5～10歳・2匹以上の仔猫がいる猫を取得する")
	void testGetCatListWith_Age5to10_GreaterThanEquals2Kittens() {

		repository.deleteAll();

		// 年齢も仔猫の匹数も該当する
		// @formatter:off
		final Cat makino = catTestUtil.initializeCat("3-牧野歴君", 5, 2);
		final Cat kinako1 = catTestUtil.initializeCat("2-きなこ", 6, 3);
		final Cat sakura = catTestUtil.initializeCat("1-さくら", 10, 2);
		final Cat kinako2 = catTestUtil.initializeCat("2-きなこ", 9, 3);
		// @formatter:on

		final List<Cat> candidates = Arrays.asList(makino, kinako1, sakura, kinako2);
		final List<Cat> expected = Arrays.asList(sakura, kinako1, kinako2, makino);

		// @formatter:off
		final List<Cat> otherCats = Arrays.asList(
				// 年齢のみ該当する
				catTestUtil.initializeCat("碧", 5, 1) 
				, catTestUtil.initializeCat("琥珀", 10, 0)
				// 仔猫の匹数のみ該当する
				, catTestUtil.initializeCat("くるみ", 4, 2) 
				, catTestUtil.initializeCat("モカ", 11, 2)
				);
		// @formatter:on
		List<Cat> entries = new ArrayList<>();
		entries.addAll(candidates);
		entries.addAll(otherCats);
		repository.saveAllAndFlush(entries);

		// @formatter:off
		CriteriaBuilder<Cat> cb = cbf.create(em, Cat.class, "c")
				.where("c.age").betweenExpression("5").andExpression("10")
				.where("SIZE(c.kittens)").geExpression("2")
				.orderByAsc("c.name")
				.orderByAsc("c.id");
		// @formatter:on

		List<Cat> cats = cb.getResultList();
		assertEquals(4, cats.size());
		assertEquals(expected, cats);

		final String processName = "Select matched cat list for complicated query";
		testLogUtil.outputResultList(processName, cats);
	}

	@RepeatedTest(value = 3, name = "{displayName} {currentRepetition}/{totalRepetitions}")
	@DisplayName("ページングを利用した猫のリストを取得する")
	void testGetCatListWithPaging(RepetitionInfo repetitionInfo) {

		final int pagingSize = 10;
		final int currentRepetition = repetitionInfo.getCurrentRepetition();
		final int startIndex = (currentRepetition - 1) * pagingSize;
		final int tempEndIndex = currentRepetition * pagingSize;
		final int endIndex = tempEndIndex > MAX_CATS_SIZE ? MAX_CATS_SIZE : tempEndIndex;
		final int expectedSize = endIndex - startIndex;

		List<String> expectedCatNames = new ArrayList<>();
		for (int i = startIndex; i < endIndex; i++) {
			expectedCatNames.add("Cat" + (i + 1));
		}

		// @formatter:off
		CriteriaBuilder<Cat> cb = cbf.create(em, Cat.class, "c")
				.orderByAsc("c.id");
		// @formatter:on
		PagedList<Cat> cats = cb.page(startIndex, pagingSize).getResultList();

		// @formatter:off
		List<String> actualCatNames = cats.stream()
			.map(Cat::getName)
			.collect(Collectors.toList());
		// @formatter:on

		assertNotNull(cats);
		assertEquals(expectedSize, cats.size());
		assertEquals(expectedCatNames, actualCatNames);
		assertEquals(startIndex, cats.getFirstResult(), "`getFirstResult` equals startIndex");
		assertEquals(pagingSize, cats.getMaxResults(), "`getMaxResults` equals pagingSize");
		assertEquals(currentRepetition, cats.getPage(), "`getPage` equals currentRepetition");
		assertEquals(expectedSize, cats.getSize(), "`getSize` equals size at current page");
		assertEquals(repetitionInfo.getTotalRepetitions(), cats.getTotalPages(),
				"`getTotalPages` equals totalRepetitions");
		assertEquals(MAX_CATS_SIZE, cats.getTotalSize(), "`getTotalSize` equals total list size");

		final String processName = "Select cat paging list by Blaze Persistence";
		testLogUtil.outputResultList(processName, cats);
		log.info("cats.getFirstResult(): {}", cats.getFirstResult());
		log.info("cats.getKeysetPage(): {}",
				Objects.isNull(cats.getKeysetPage()) ? "not defined" : cats.getKeysetPage().toString());
		log.info("cats.getMaxResults(): {}", cats.getMaxResults());
		log.info("cats.getPage(): {}", cats.getPage());
		log.info("cats.getSize(): {}", cats.getSize());
		log.info("cats.getTotalPages(): {}", cats.getTotalPages());
		log.info("cats.getTotalSize(): {}", cats.getTotalSize());
	}
}
