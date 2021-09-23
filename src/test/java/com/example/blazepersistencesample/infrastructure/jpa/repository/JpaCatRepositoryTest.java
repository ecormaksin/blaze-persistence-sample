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

import lombok.extern.slf4j.Slf4j;

@DataJpaTest
@Import(BlazePersistenceConfiguration.class)
@Slf4j
class JpaCatRepositoryTest {

	@Autowired
	private JpaCatRepository repository;

	@Autowired
	private CriteriaBuilderFactory cbf;

	@Autowired
	private EntityManager em;

	@BeforeEach
	void setup() {

		// @formatter:off
		repository.saveAndFlush(
				Cat.builder()
					.name("フク")
					.age(5)
					.build()
				);
		repository.saveAndFlush(
				Cat.builder()
					.name("あかり")
					.age(3)
					.build()
				);
		// @formatter:on
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

		// // 年齢も仔猫の匹数も該当する
		final Cat makino = Cat.builder().name("4－牧野歴君").age(5).kittens(2).build();
		final Cat momiji = Cat.builder().name("3－椛").age(6).kittens(3).build();
		final Cat kinako = Cat.builder().name("2－きなこ").age(9).kittens(3).build();
		final Cat sakura = Cat.builder().name("1－さくら").age(10).kittens(2).build();

		final List<Cat> candidates = Arrays.asList(makino, momiji, kinako, sakura);
		final List<Cat> expected = Arrays.asList(sakura, kinako, momiji, makino);

		// @formatter:off
		final List<Cat> otherCats = Arrays.asList(
				// 年齢のみ該当する
				Cat.builder().name("碧").age(5).kittens(1).build() 
				, Cat.builder().name("琥珀").age(10).kittens(0).build()
				// 仔猫の匹数のみ該当する
				, Cat.builder().name("くるみ").age(4).kittens(2).build() 
				, Cat.builder().name("モカ").age(11).kittens(2).build()
				);
		// @formatter:on
		List<Cat> entries = new ArrayList<>();
		entries.addAll(candidates);
		entries.addAll(otherCats);
		repository.saveAllAndFlush(entries);

		CriteriaBuilder<Cat> cb;

		cb = cbf.create(em, Cat.class);
		List<Cat> cats = cb.getResultList();
		assertNotNull(cats);
		assertEquals(8, cats.size());

		String processName = "Select cat all list for complicated query";
		outputProcessStart(processName);
		// @formatter:off
		cats.stream().forEach(c -> {
			log.info(c.toString());
		});
		// @formatter:on
		outputProcessEnd(processName);

		// @formatter:off
		cb = cbf.create(em, Cat.class, "c")
				.where("c.age").betweenExpression("5").andExpression("10")
				.where("SIZE(c.kittens)").geExpression("2")
				.orderByAsc("c.name")
				.orderByAsc("c.id");
		// @formatter:on

		List<Cat> results = cb.getResultList();
		assertEquals(expected, results);

		processName = "Select matched cat list for complicated query";
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

		log.info("■ {}", processName);
	}
}
