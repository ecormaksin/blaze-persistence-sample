package com.example.blazepersistencesample.infrastructure.jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.blazepersistencesample.infrastructure.jpa.entity.Cat;

public interface JpaCatRepository extends JpaRepository<Cat, Long> {

	List<Cat> findAll();
}
