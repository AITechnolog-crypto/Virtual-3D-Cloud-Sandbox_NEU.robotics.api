package com.june.repository;

import com.june.model.SandboxObject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SandboxObjectRepository extends JpaRepository<SandboxObject, Long> {
}
