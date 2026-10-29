package com.utetra.service;

import com.utetra.dto.CartLine;
import com.utetra.dto.CartView;
import com.utetra.dto.CheckoutForm;
import com.utetra.dto.CheckoutResult;
import com.utetra.dto.VnpayPaymentRequest;
import com.utetra.entity.Branch;
import com.utetra.entity.Cart;
import com.utetra.entity.CartItem;
import com.utetra.entity.Order;
import com.utetra.entity.OrderItem;
import com.utetra.entity.OrderItemTopping;
import com.utetra.entity.OrderStatusHistory;
import com.utetra.entity.Payment;
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
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class CheckoutService {

    private static final DateTimeFormatter CODE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter PAYMENT_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CheckoutService(CartRepository cartRepository, UserRepository userRepository,
                           BranchRepository branchRepository, OrderRepository orderRepository,
                           OrderStatusHistoryRepository historyRepository, PaymentRepository paymentRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public CheckoutForm defaultForm(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CheckoutException("Không tìm thấy tài khoản"));
        CheckoutForm form = new CheckoutForm();
        form.setReceiverName(user.getFullName());
        form.setReceiverPhone(user.getPhone());
        form.setDeliveryAddress(user.getAddress());
        return form;
    }

    @Transactional
    public Order placeCod(Long userId, CheckoutForm form) {
        form.setPaymentMethod(PaymentMethod.COD);
        return placeOrder(userId, form).order();
    }

    @Transactional
    public CheckoutResult place(Long userId, CheckoutForm form) {
        if (form.getPaymentMethod() == null) {
            throw new CheckoutException("Phương thức thanh toán không hợp lệ");
        }
        return placeOrder(userId, form);
    }

    private CheckoutResult placeOrder(Long userId, CheckoutForm form) {
        User user = userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new CheckoutException("Tài khoản không còn hoạt động"));
        Branch branch = branchRepository.findByIdAndActiveTrue(form.getBranchId())
                .orElseThrow(() -> new CheckoutException("Chi nhánh không hợp lệ"));
        Cart cart = cartRepository.findDetailedByUserId(userId)
                .filter(c -> !c.getItems().isEmpty())
                .orElseThrow(() -> new CheckoutException("Giỏ hàng đang trống"));

        for (CartItem item : cart.getItems()) {
            if (!item.getProduct().isAvailable() || !item.getProduct().getCategory().isActive()) {
                throw new CheckoutException("Có sản phẩm đã ngừng bán, vui lòng kiểm tra lại giỏ hàng");
            }
            if (item.getProduct().isSoldOut()) {
                throw new CheckoutException("Có món đang tạm hết, vui lòng chọn món khác hoặc thử lại sau");
            }
            if (!item.getProduct().getCategory().isAllowToppings() && !item.getToppings().isEmpty()) {
                throw new CheckoutException("Giỏ hàng có topping không phù hợp với loại thức uống, vui lòng kiểm tra lại");
            }
            if (item.getToppings().stream().anyMatch(topping -> !topping.isAvailable())) {
                throw new CheckoutException("Có topping đã ngừng phục vụ, vui lòng kiểm tra lại giỏ hàng");
            }
        }

        CartView cartView = CartService.toView(cart);
        Order order = new Order();
        order.setCode(generateCode());
        order.setUser(user);
        order.setBranch(branch);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(form.getPaymentMethod());
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setTotalAmount(cartView.subtotal());
        order.setReceiverName(form.getReceiverName().trim());
        order.setReceiverPhone(form.getReceiverPhone().trim());
        order.setDeliveryAddress(form.getDeliveryAddress().trim());
        order.setNote(normalize(form.getNote()));

        for (CartLine line : cartView.lines()) {
            order.getItems().add(snapshot(order, line.item()));
        }
        Order saved = orderRepository.save(order);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(saved);
        history.setStatus(OrderStatus.PENDING);
        history.setChangedBy(userId);
        historyRepository.save(history);

        VnpayPaymentRequest vnpayRequest = null;
        if (form.getPaymentMethod() == PaymentMethod.VNPAY) {
            Payment payment = new Payment();
            payment.setOrder(saved);
            payment.setMethod(PaymentMethod.VNPAY);
            payment.setAmount(saved.getTotalAmount());
            payment.setStatus(PaymentStatus.UNPAID);
            payment.setVnpTxnRef(generatePaymentReference());
            paymentRepository.save(payment);
            vnpayRequest = new VnpayPaymentRequest(payment.getVnpTxnRef(), saved.getCode(), payment.getAmount());
        }

        cart.getItems().clear();
        if (saved.getPaymentMethod() == PaymentMethod.COD) {
            eventPublisher.publishEvent(OrderNotificationEvent.newOrder(
                    saved.getId(), saved.getCode(), branch.getId(), user.getId(), PaymentMethod.COD));
        }
        return new CheckoutResult(saved, vnpayRequest);
    }

    @Transactional(readOnly = true)
    public Order findSuccess(String code, Long userId) {
        return orderRepository.findByCodeAndUserId(code, userId)
                .orElseThrow(() -> new CheckoutException("Không tìm thấy đơn hàng"));
    }

    private static OrderItem snapshot(Order order, CartItem cartItem) {
        BigDecimal baseWithSize = cartItem.getProduct().getBasePrice().add(cartItem.getSize().getExtraPrice());
        BigDecimal toppingTotal = cartItem.getToppings().stream()
                .map(Topping::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(cartItem.getProduct().getId());
        item.setProductName(cartItem.getProduct().getName());
        item.setSizeName(cartItem.getSize().getName());
        item.setUnitPrice(baseWithSize);
        item.setSugarPercent(cartItem.getSugarPercent());
        item.setIcePercent(cartItem.getIcePercent());
        item.setQuantity(cartItem.getQuantity());
        item.setNote(cartItem.getNote());
        item.setLineTotal(baseWithSize.add(toppingTotal).multiply(BigDecimal.valueOf(cartItem.getQuantity())));

        for (Topping topping : cartItem.getToppings()) {
            OrderItemTopping snapshot = new OrderItemTopping();
            snapshot.setOrderItem(item);
            snapshot.setToppingName(topping.getName());
            snapshot.setPrice(topping.getPrice());
            item.getToppings().add(snapshot);
        }
        return item;
    }

    private static String generateCode() {
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        return "UTE-" + LocalDateTime.now().format(CODE_TIME) + "-" + random;
    }

    private static String generatePaymentReference() {
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        return "VNP-" + LocalDateTime.now().format(PAYMENT_TIME) + "-" + random;
    }

    private static String normalize(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
