package com.example.blazepersistencesample.infrastructure.jpa.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.blazepersistencesample.infrastructure.jpa.entity.Cat;

public interface JpaCatRepository extends CrudRepository<Cat, Long> {

	List<Cat> findAll();
}
