package com.utetra.repository;

import com.utetra.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SizeRepository extends JpaRepository<Size, Long> {

    List<Size> findAllByOrderByExtraPriceAscIdAsc();
}
