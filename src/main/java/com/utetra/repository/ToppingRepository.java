package com.utetra.repository;

import com.utetra.entity.Topping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToppingRepository extends JpaRepository<Topping, Long> {

    List<Topping> findByAvailableTrueOrderByIdAsc();
}
