package com.utetra.service;

import com.utetra.dto.AddCartItemForm;
import com.utetra.dto.CartView;
import com.utetra.entity.Cart;
import com.utetra.entity.CartItem;
import com.utetra.entity.Category;
import com.utetra.entity.Product;
import com.utetra.entity.Size;
import com.utetra.entity.Topping;
import com.utetra.entity.User;
import com.utetra.repository.CartItemRepository;
import com.utetra.repository.CartRepository;
import com.utetra.repository.ProductRepository;
import com.utetra.repository.SizeRepository;
import com.utetra.repository.ToppingRepository;
import com.utetra.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartServiceTest {

    private CartRepository cartRepository;
    private CartItemRepository cartItemRepository;
    private ProductRepository productRepository;
    private SizeRepository sizeRepository;
    private ToppingRepository toppingRepository;
    private UserRepository userRepository;
    private CartService service;

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        cartItemRepository = mock(CartItemRepository.class);
        productRepository = mock(ProductRepository.class);
        sizeRepository = mock(SizeRepository.class);
        toppingRepository = mock(ToppingRepository.class);
        userRepository = mock(UserRepository.class);
        service = new CartService(cartRepository, cartItemRepository, productRepository,
                sizeRepository, toppingRepository, userRepository);
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void toViewCalculatesSizeToppingsAndQuantity() {
        Cart cart = new Cart();
        CartItem item = item(1, Set.of(topping(1L, "Trân châu", "5000")));
        item.setQuantity(2);
        cart.getItems().add(item);

        CartView view = CartService.toView(cart);

        assertEquals(new BigDecimal("45000"), view.lines().get(0).unitPrice());
        assertEquals(new BigDecimal("90000"), view.subtotal());
        assertEquals(2, view.totalQuantity());
    }

    @Test
    void addCreatesCartAndNormalizesNote() {
        AddCartItemForm form = form();
        form.setNote("  ít ngọt  ");
        Product product = product();
        Size size = size();
        when(productRepository.findPublicById(1L)).thenReturn(Optional.of(product));
        when(sizeRepository.findById(1L)).thenReturn(Optional.of(size));
        when(cartRepository.findDetailedByUserId(7L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(7L)).thenReturn(new User());

        service.add(7L, form);

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        Cart saved = captor.getAllValues().get(1);
        assertEquals(1, saved.getItems().size());
        assertEquals("ít ngọt", saved.getItems().get(0).getNote());
    }

    @Test
    void addMergesSameConfiguration() {
        Cart cart = new Cart();
        CartItem existing = item(2, Set.of());
        cart.getItems().add(existing);
        when(productRepository.findPublicById(1L)).thenReturn(Optional.of(existing.getProduct()));
        when(sizeRepository.findById(1L)).thenReturn(Optional.of(existing.getSize()));
        when(cartRepository.findDetailedByUserId(7L)).thenReturn(Optional.of(cart));

        service.add(7L, form());

        assertEquals(3, existing.getQuantity());
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void addRejectsInvalidLevelBeforeDatabaseAccess() {
        AddCartItemForm form = form();
        form.setSugarPercent(25);

        assertThrows(CartException.class, () -> service.add(7L, form));
        verify(productRepository, never()).findPublicById(any());
    }

    @Test
    void addRejectsUnavailableTopping() {
        AddCartItemForm form = form();
        form.setToppingIds(List.of(1L, 2L));
        when(productRepository.findPublicById(1L)).thenReturn(Optional.of(product()));
        when(sizeRepository.findById(1L)).thenReturn(Optional.of(size()));
        when(toppingRepository.findByIdInAndAvailableTrue(Set.of(1L, 2L)))
                .thenReturn(List.of(topping(1L, "Trân châu", "5000")));

        assertThrows(CartException.class, () -> service.add(7L, form));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void updateQuantityChecksOwnershipAndLimit() {
        assertThrows(CartException.class, () -> service.updateQuantity(7L, 9L, 21));
        when(cartItemRepository.findOwnedDetailed(9L, 7L)).thenReturn(Optional.empty());
        assertThrows(CartException.class, () -> service.updateQuantity(7L, 9L, 2));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void removeOnlyDeletesOwnedItem() {
        CartItem item = item(1, Set.of());
        when(cartItemRepository.findOwnedDetailed(9L, 7L)).thenReturn(Optional.of(item));

        service.remove(7L, 9L);

        verify(cartItemRepository).delete(item);
    }

    private static AddCartItemForm form() {
        AddCartItemForm form = new AddCartItemForm();
        form.setProductId(1L);
        form.setSizeId(1L);
        form.setSugarPercent(100);
        form.setIcePercent(100);
        form.setQuantity(1);
        return form;
    }

    private static CartItem item(int quantity, Set<Topping> toppings) {
        CartItem item = new CartItem();
        item.setProduct(product());
        item.setSize(size());
        item.setQuantity(quantity);
        item.setSugarPercent(100);
        item.setIcePercent(100);
        item.setToppings(toppings);
        return item;
    }

    private static Product product() {
        Category category = new Category();
        category.setId(1L);
        category.setActive(true);
        Product product = new Product();
        product.setId(1L);
        product.setName("Trà sữa");
        product.setBasePrice(new BigDecimal("30000"));
        product.setAvailable(true);
        product.setCategory(category);
        return product;
    }

    private static Size size() {
        Size size = new Size();
        size.setId(1L);
        size.setName("M");
        size.setExtraPrice(new BigDecimal("10000"));
        return size;
    }

    private static Topping topping(Long id, String name, String price) {
        Topping topping = new Topping();
        topping.setId(id);
        topping.setName(name);
        topping.setPrice(new BigDecimal(price));
        topping.setAvailable(true);
        return topping;
    }
}
