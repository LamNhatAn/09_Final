package com.utetra.repository;

import com.utetra.entity.Topping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface ToppingRepository extends JpaRepository<Topping, Long> {

    List<Topping> findByAvailableTrueOrderByIdAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<Topping> findByIdInAndAvailableTrue(Set<Long> ids);
}
