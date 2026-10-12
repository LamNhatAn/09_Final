package com.utetra.service;

import com.utetra.dto.ToppingForm;
import com.utetra.entity.Topping;
import com.utetra.repository.CartItemRepository;
import com.utetra.repository.ToppingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Quan ly topping cho admin. Don hang da dat chi luu ban sao ten/gia topping (order_item_toppings)
 * nen xoa topping khong anh huong don cu; chi can chan khi topping dang nam trong gio hang.
 */
@Service
@RequiredArgsConstructor
public class AdminToppingService {

    private final ToppingRepository toppingRepository;
    private final CartItemRepository cartItemRepository;

    public List<Topping> findAll() {
        return toppingRepository.findAll(Sort.by("id"));
    }

    public Optional<Topping> find(Long id) {
        return toppingRepository.findById(id);
    }

    public boolean nameTaken(String name, Long excludeId) {
        String trimmed = name == null ? "" : name.trim();
        return excludeId == null
                ? toppingRepository.existsByName(trimmed)
                : toppingRepository.existsByNameAndIdNot(trimmed, excludeId);
    }

    @Transactional
    public Topping create(ToppingForm form) {
        Topping topping = new Topping();
        apply(topping, form);
        return toppingRepository.save(topping);
    }

    @Transactional
    public Topping update(Topping topping, ToppingForm form) {
        apply(topping, form);
        return toppingRepository.save(topping);
    }

    @Transactional
    public boolean toggleAvailable(Topping topping) {
        topping.setAvailable(!topping.isAvailable());
        return toppingRepository.save(topping).isAvailable();
    }

    @Transactional
    public void delete(Topping topping) {
        if (cartItemRepository.existsByToppingsId(topping.getId())) {
            throw new EntityInUseException("Topping đang nằm trong giỏ hàng của khách");
        }
        toppingRepository.delete(topping);
    }

    private static void apply(Topping topping, ToppingForm form) {
        topping.setName(form.getName().trim());
        topping.setPrice(form.getPrice());
        topping.setAvailable(form.isAvailable());
    }
}
