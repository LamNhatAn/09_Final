<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head><title>Đặt hàng thành công</title></head>
<body>
<div class="text-center mb-4">
    <div class="display-3">✅</div>
    <h1 class="h3">Đặt hàng thành công</h1>
    <p class="text-muted">Mã đơn: <strong><c:out value="${order.code}"/></strong></p>
</div>
<div class="card shadow-sm mx-auto" style="max-width:760px"><div class="card-body p-4">
    <div class="row g-3 mb-3">
        <div class="col-md-6"><span class="text-muted">Chi nhánh</span><br><strong><c:out value="${order.branch.name}"/></strong></div>
        <div class="col-md-6"><span class="text-muted">Thanh toán</span><br><strong>${order.paymentMethod == 'COD' ? 'COD – khi nhận hàng' : 'VNPay'}</strong></div>
        <div class="col-md-6"><span class="text-muted">Người nhận</span><br><c:out value="${order.receiverName}"/> · <c:out value="${order.receiverPhone}"/></div>
        <div class="col-md-6"><span class="text-muted">Địa chỉ</span><br><c:out value="${order.deliveryAddress}"/></div>
    </div>
    <h2 class="h6 border-top pt-3">Các món đã đặt</h2>
    <c:forEach var="item" items="${order.items}">
        <div class="d-flex justify-content-between gap-3 border-bottom py-2">
            <div>
                <strong><c:out value="${item.productName}"/> × ${item.quantity}</strong>
                <div class="small text-muted">Size <c:out value="${item.sizeName}"/> · Đường ${item.sugarPercent}% · Đá ${item.icePercent}%</div>
                <c:if test="${not empty item.toppings}"><div class="small text-muted">Topping: <c:forEach var="t" items="${item.toppings}" varStatus="loop"><c:out value="${t.toppingName}"/><c:if test="${!loop.last}">, </c:if></c:forEach></div></c:if>
                <c:if test="${not empty item.note}"><div class="small">Ghi chú: <c:out value="${item.note}"/></div></c:if>
            </div>
            <strong class="text-nowrap"><fmt:formatNumber value="${item.lineTotal}" type="number"/> đ</strong>
        </div>
    </c:forEach>
    <div class="d-flex justify-content-between h5 pt-3"><span>Tổng cộng</span><strong class="product-price"><fmt:formatNumber value="${order.totalAmount}" type="number"/> đ</strong></div>
    <div class="alert alert-warning mb-0">Đơn đang chờ chi nhánh xác nhận. Bạn có thể theo dõi hoặc hủy đơn trước khi chi nhánh xác nhận.</div>
</div></div>
<div class="text-center mt-4 d-flex justify-content-center gap-2">
    <a class="btn btn-utetra" href="<c:url value='/orders/${order.code}'/>">Theo dõi đơn hàng</a>
    <a class="btn btn-outline-secondary" href="<c:url value='/menu'/>">Tiếp tục xem thực đơn</a>
</div>
</body>
</html>
