package com.lms.repository;

import com.lms.entity.ProfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileTypeRepository extends JpaRepository<ProfileType, Integer> {

    Optional<ProfileType> findByName(String name);

    boolean existsByName(String name);
}