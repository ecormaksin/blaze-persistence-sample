package com.example.blazepersistencesample.infrastructure.jpa.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.example.blazepersistencesample.infrastructure.jpa.BlazePersistenceConfiguration;
import com.example.blazepersistencesample.infrastructure.jpa.entity.Cat;
import com.example.blazepersistencesample.infrastructure.jpa.utils.CatInitTestUtil;

import lombok.extern.slf4j.Slf4j;

@DataJpaTest
@Import(value = { BlazePersistenceConfiguration.class, CatInitTestUtil.class })
@Slf4j
class JpaCatRepositoryTest {

	@Autowired
	private JpaCatRepository repository;

	@Autowired
	private CriteriaBuilderFactory cbf;

	@Autowired
	private EntityManager em;

	@Autowired
	private CatInitTestUtil catInitTestUtil;

	@BeforeEach
	void setup() {

		List<Cat> catsList = new ArrayList<>();
		// @formatter:off
		catsList.add(Cat.builder()
					.name("フク")
					.age(5)
					.build());
		catsList.add(Cat.builder()
				.name("あかり")
				.age(3)
				.build());
		// @formatter:on
		repository.saveAllAndFlush(catsList);
	}

	@Test
	@DisplayName("猫のリストを取得できる（JPA版）")
	void testGetCatListByJpa() {

		List<Cat> cats = repository.findAll();

		assertNotNull(cats);
		assertFalse(cats.isEmpty());

		final String processName = "Select cat list by JPA";
		outputProcessStart(processName);
		// @formatter:off
		cats.stream().forEach(c -> {
			log.info(c.toString());
		});
		// @formatter:on
		outputProcessEnd(processName);
	}

	@Test
	@DisplayName("猫のリストを取得できる（Blaze Persistence版）")
	void testGetCatListByBlazePersistence() {

		CriteriaBuilder<Cat> cb = cbf.create(em, Cat.class);

		List<Cat> cats = cb.getResultList();

		assertNotNull(cats);
		assertFalse(cats.isEmpty());

		final String processName = "Select cat list by Blaze Persistence";
		outputProcessStart(processName);
		// @formatter:off
		cats.stream().forEach(c -> {
			log.info(c.toString());
		});
		// @formatter:on
		outputProcessEnd(processName);
	}

	@Test
	@DisplayName("猫の年齢のみを取得する（エイリアスなし）")
	void testGetCatAgeListWithoutAlias() {

		// @formatter:off
		CriteriaBuilder<Integer> cb = cbf.create(em, Integer.class)
				.from(Cat.class)
				.select("cat.age");
		// @formatter:on

		List<Integer> ages = cb.getResultList();

		assertNotNull(ages);
		assertFalse(ages.isEmpty());

		final String processName = "Select cat age list without alias by Blaze Persistence";
		outputProcessStart(processName);
		// @formatter:off
		ages.stream().forEach(a -> {
			log.info("age: {}", a);
		});
		// @formatter:on
		outputProcessEnd(processName);
	}

	@Test
	@DisplayName("猫の年齢のみを取得する（エイリアスあり）")
	void testGetCatAgeListWithAlias() {

		// @formatter:off
		CriteriaBuilder<Integer> cb = cbf.create(em, Integer.class)
				.from(Cat.class, "c")
				.select("c.age");
		// @formatter:on

		List<Integer> ages = cb.getResultList();

		assertNotNull(ages);
		assertFalse(ages.isEmpty());

		final String processName = "Select cat age list with alias by Blaze Persistence";
		outputProcessStart(processName);
		// @formatter:off
		ages.stream().forEach(a -> {
			log.info("age: {}", a);
		});
		// @formatter:on
		outputProcessEnd(processName);
	}

	@Test
	@DisplayName("年齢が5～10歳・2匹以上の仔猫がいる猫を取得する")
	void testGetCatListWith_Age5to10_GreaterThanEquals2Kittens() {

		repository.deleteAll();

		// 年齢も仔猫の匹数も該当する
		// @formatter:off
		final Cat makino = catInitTestUtil.initializeCat("3-牧野歴君", 5, 2);
		final Cat kinako1 = catInitTestUtil.initializeCat("2-きなこ", 6, 3);
		final Cat sakura = catInitTestUtil.initializeCat("1-さくら", 10, 2);
		final Cat kinako2 = catInitTestUtil.initializeCat("2-きなこ", 9, 3);
		// @formatter:on

		final List<Cat> candidates = Arrays.asList(makino, kinako1, sakura, kinako2);
		final List<Cat> expected = Arrays.asList(sakura, kinako1, kinako2, makino);

		// @formatter:off
		final List<Cat> otherCats = Arrays.asList(
				// 年齢のみ該当する
				catInitTestUtil.initializeCat("碧", 5, 1) 
				, catInitTestUtil.initializeCat("琥珀", 10, 0)
				// 仔猫の匹数のみ該当する
				, catInitTestUtil.initializeCat("くるみ", 4, 2) 
				, catInitTestUtil.initializeCat("モカ", 11, 2)
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

		List<Cat> results = cb.getResultList();
		assertEquals(4, results.size());
		assertEquals(expected, results);

		final String processName = "Select matched cat list for complicated query";
		outputProcessStart(processName);
		// @formatter:off
		results.stream().forEach(c -> {
			log.info(c.toString());
		});
		// @formatter:on
		outputProcessEnd(processName);
	}

	private void outputProcessStart(final String processName) {
		outputProcessLabel(processName + " [Start]");
	}

	private void outputProcessEnd(final String processName) {
		outputProcessLabel(processName + " [End]");
	}

	private void outputProcessLabel(final String processName) {
		log.info("■{}", processName);
	}
}
