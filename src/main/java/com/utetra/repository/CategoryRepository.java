package com.utetra.repository;

import com.utetra.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByActiveTrueOrderByIdAsc();

    // Cot name dung collation khong phan biet hoa thuong, nen "Trà sữa" va "trà SỮA" coi la trung
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
