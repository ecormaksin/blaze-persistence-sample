package com.example.blazepersistencesample.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.blazepersistencesample.infrastructure.jpa.entity.Person;

public interface JpaPersonRepository extends JpaRepository<Person, Long> {

}
