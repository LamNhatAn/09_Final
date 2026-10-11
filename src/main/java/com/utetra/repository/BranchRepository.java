package com.utetra.repository;

import com.utetra.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findByActiveTrueOrderByIdAsc();
}
