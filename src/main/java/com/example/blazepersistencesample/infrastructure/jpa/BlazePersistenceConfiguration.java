package com.example.blazepersistencesample.infrastructure.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;

@Configuration
public class BlazePersistenceConfiguration {

	@PersistenceUnit
	private EntityManagerFactory entityManagerFactory;

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	@Lazy(false)
	public CriteriaBuilderFactory createCriteriaBuilderFactory() {
		CriteriaBuilderConfiguration config = Criteria.getDefault();

		return config.createCriteriaBuilderFactory(entityManagerFactory);
	}
}
