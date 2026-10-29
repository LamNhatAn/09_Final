package com.utetra.service;

import com.utetra.dto.CheckoutForm;
import com.utetra.entity.Branch;
import com.utetra.entity.Cart;
import com.utetra.entity.CartItem;
import com.utetra.entity.Category;
import com.utetra.entity.Order;
import com.utetra.entity.Payment;
import com.utetra.entity.Product;
import com.utetra.entity.Size;
import com.utetra.entity.Topping;
import com.utetra.entity.User;
import com.utetra.entity.enums.OrderStatus;
import com.utetra.entity.enums.PaymentMethod;
import com.utetra.entity.enums.PaymentStatus;
import com.utetra.event.OrderNotificationEvent;
import com.utetra.repository.BranchRepository;
import com.utetra.repository.CartRepository;
import com.utetra.repository.OrderRepository;
import com.utetra.repository.OrderStatusHistoryRepository;
import com.utetra.repository.PaymentRepository;
import com.utetra.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckoutServiceTest {

    private CartRepository cartRepository;
    private UserRepository userRepository;
    private BranchRepository branchRepository;
    private OrderRepository orderRepository;
    private OrderStatusHistoryRepository historyRepository;
    private PaymentRepository paymentRepository;
    private ApplicationEventPublisher eventPublisher;
    private CheckoutService service;

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        userRepository = mock(UserRepository.class);
        branchRepository = mock(BranchRepository.class);
        orderRepository = mock(OrderRepository.class);
        historyRepository = mock(OrderStatusHistoryRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new CheckoutService(cartRepository, userRepository, branchRepository,
                orderRepository, historyRepository, paymentRepository, eventPublisher);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order order = i.getArgument(0);
            order.setId(10L);
            return order;
        });
    }

    @Test
    void placeCodSnapshotsPricesCreatesHistoryAndClearsCart() {
        User user = activeUser();
        Branch branch = activeBranch();
        Cart cart = cart();
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(branchRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(branch));
        when(cartRepository.findDetailedByUserId(7L)).thenReturn(Optional.of(cart));

        Order order = service.placeCod(7L, form());

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(PaymentMethod.COD, order.getPaymentMethod());
        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
        assertEquals(new BigDecimal("90000"), order.getTotalAmount());
        assertEquals(1, order.getItems().size());
        assertEquals(new BigDecimal("40000"), order.getItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("90000"), order.getItems().get(0).getLineTotal());
        assertEquals("ít đá", order.getItems().get(0).getNote());
        assertEquals(1, order.getItems().get(0).getToppings().size());
        assertTrue(order.getCode().startsWith("UTE-"));
        assertTrue(cart.getItems().isEmpty());
        verify(historyRepository).save(any());
        verify(eventPublisher).publishEvent(any(OrderNotificationEvent.class));
    }

    @Test
    void placeCodRejectsEmptyCart() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(activeUser()));
        when(branchRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(activeBranch()));
        when(cartRepository.findDetailedByUserId(7L)).thenReturn(Optional.of(new Cart()));

        assertThrows(CheckoutException.class, () -> service.placeCod(7L, form()));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeVnpayCreatesUnpaidPaymentAttempt() {
        User user = activeUser();
        Cart cart = cart();
        CheckoutForm form = form();
        form.setPaymentMethod(PaymentMethod.VNPAY);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(branchRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(activeBranch()));
        when(cartRepository.findDetailedByUserId(7L)).thenReturn(Optional.of(cart));

        var result = service.place(7L, form);

        assertTrue(result.requiresVnpay());
        assertEquals(PaymentMethod.VNPAY, result.order().getPaymentMethod());
        assertTrue(result.vnpayRequest().transactionRef().startsWith("VNP-"));
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals(PaymentStatus.UNPAID, captor.getValue().getStatus());
        assertEquals(new BigDecimal("90000"), captor.getValue().getAmount());
        assertTrue(cart.getItems().isEmpty());
        verify(eventPublisher, never()).publishEvent(any(OrderNotificationEvent.class));
    }

    @Test
    void placeCodRechecksProductAvailability() {
        Cart cart = cart();
        cart.getItems().get(0).getProduct().setAvailable(false);
        when(userRepository.findById(7L)).thenReturn(Optional.of(activeUser()));
        when(branchRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(activeBranch()));
        when(cartRepository.findDetailedByUserId(7L)).thenReturn(Optional.of(cart));

        assertThrows(CheckoutException.class, () -> service.placeCod(7L, form()));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeCodRejectsSoldOutProductAlreadyInCart() {
        Cart cart = cart();
        cart.getItems().get(0).getProduct().setSoldOut(true);
        when(userRepository.findById(7L)).thenReturn(Optional.of(activeUser()));
        when(branchRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(activeBranch()));
        when(cartRepository.findDetailedByUserId(7L)).thenReturn(Optional.of(cart));

        assertThrows(CheckoutException.class, () -> service.placeCod(7L, form()));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeCodRejectsLegacyToppingOnUnsupportedDrink() {
        Cart cart = cart();
        cart.getItems().get(0).getProduct().getCategory().setAllowToppings(false);
        when(userRepository.findById(7L)).thenReturn(Optional.of(activeUser()));
        when(branchRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(activeBranch()));
        when(cartRepository.findDetailedByUserId(7L)).thenReturn(Optional.of(cart));

        assertThrows(CheckoutException.class, () -> service.placeCod(7L, form()));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void defaultFormUsesCustomerProfile() {
        User user = activeUser();
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        CheckoutForm form = service.defaultForm(7L);

        assertEquals("Nguyễn Văn A", form.getReceiverName());
        assertEquals("0901234567", form.getReceiverPhone());
        assertEquals("1 Võ Văn Ngân", form.getDeliveryAddress());
    }

    @Test
    void findSuccessOnlyReturnsCurrentUsersOrder() {
        Order order = new Order();
        when(orderRepository.findByCodeAndUserId("UTE-1", 7L)).thenReturn(Optional.of(order));

        assertEquals(order, service.findSuccess("UTE-1", 7L));
        assertThrows(CheckoutException.class, () -> service.findSuccess("UTE-1", 8L));
    }

    private static CheckoutForm form() {
        CheckoutForm form = new CheckoutForm();
        form.setBranchId(2L);
        form.setReceiverName(" Nguyễn Văn A ");
        form.setReceiverPhone(" 0901234567 ");
        form.setDeliveryAddress(" 1 Võ Văn Ngân ");
        form.setNote(" Giao giờ hành chính ");
        return form;
    }

    private static User activeUser() {
        User user = new User();
        user.setId(7L);
        user.setFullName("Nguyễn Văn A");
        user.setPhone("0901234567");
        user.setAddress("1 Võ Văn Ngân");
        user.setActive(true);
        return user;
    }

    private static Branch activeBranch() {
        Branch branch = new Branch();
        branch.setId(2L);
        branch.setName("UTETra Võ Văn Ngân");
        branch.setActive(true);
        return branch;
    }

    private static Cart cart() {
        Category category = new Category();
        category.setActive(true);
        category.setAllowToppings(true);
        Product product = new Product();
        product.setId(1L);
        product.setName("Trà sữa");
        product.setBasePrice(new BigDecimal("30000"));
        product.setAvailable(true);
        product.setCategory(category);
        Size size = new Size();
        size.setName("M");
        size.setExtraPrice(new BigDecimal("10000"));
        Topping topping = new Topping();
        topping.setName("Trân châu");
        topping.setPrice(new BigDecimal("5000"));
        topping.setAvailable(true);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setSize(size);
        item.setToppings(Set.of(topping));
        item.setQuantity(2);
        item.setSugarPercent(70);
        item.setIcePercent(50);
        item.setNote("ít đá");
        Cart cart = new Cart();
        cart.getItems().add(item);
        return cart;
    }
}
