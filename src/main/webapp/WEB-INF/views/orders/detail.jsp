<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head><title>Đơn hàng ${detail.order.code}</title></head>
<body>
<nav aria-label="breadcrumb"><ol class="breadcrumb">
    <li class="breadcrumb-item"><a href="<c:url value='/orders'/>">Đơn hàng của tôi</a></li>
    <li class="breadcrumb-item active"><c:out value="${detail.order.code}"/></li>
</ol></nav>

<c:if test="${not empty okMessage}"><div class="alert alert-success"><c:out value="${okMessage}"/></div></c:if>
<c:if test="${not empty errMessage}"><div class="alert alert-danger"><c:out value="${errMessage}"/></div></c:if>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
    <div>
        <h1 class="h3 mb-1">Đơn <c:out value="${detail.order.code}"/></h1>
        <span class="text-muted">Đặt lúc <c:out value="${detail.order.createdAt.format(dateTimeFormatter)}"/></span>
    </div>
    <span class="badge fs-6 ${detail.order.status.badgeClass}"><c:out value="${detail.order.status.label}"/></span>
</div>

<div class="row g-4">
    <div class="col-12 col-lg-8">
        <div class="card shadow-sm border-0 mb-4"><div class="card-body p-4">
            <h2 class="h5">Các món đã đặt</h2>
            <c:forEach var="item" items="${detail.order.items}">
                <div class="d-flex justify-content-between gap-3 border-bottom py-3">
                    <div>
                        <strong><c:out value="${item.productName}"/> × ${item.quantity}</strong>
                        <div class="small text-muted">Size <c:out value="${item.sizeName}"/> · Đường ${item.sugarPercent}% · Đá ${item.icePercent}%</div>
                        <c:if test="${not empty item.toppings}"><div class="small text-muted">Topping: <c:forEach var="t" items="${item.toppings}" varStatus="loop"><c:out value="${t.toppingName}"/><c:if test="${!loop.last}">, </c:if></c:forEach></div></c:if>
                        <c:if test="${not empty item.note}"><div class="small">Ghi chú: <c:out value="${item.note}"/></div></c:if>
                    </div>
                    <strong class="text-nowrap"><fmt:formatNumber value="${item.lineTotal}" type="number"/> đ</strong>
                </div>
            </c:forEach>
            <div class="d-flex justify-content-between h5 pt-3 mb-0"><span>Tổng cộng</span><strong class="product-price"><fmt:formatNumber value="${detail.order.totalAmount}" type="number"/> đ</strong></div>
        </div></div>

        <div class="card shadow-sm border-0"><div class="card-body p-4">
            <h2 class="h5">Lịch sử trạng thái</h2>
            <div class="order-timeline">
                <c:forEach var="entry" items="${detail.history}">
                    <div class="order-timeline-item">
                        <span class="badge ${entry.status.badgeClass}"><c:out value="${entry.status.label}"/></span>
                        <span class="text-muted ms-2"><c:out value="${entry.changedAt.format(dateTimeFormatter)}"/></span>
                    </div>
                </c:forEach>
            </div>
        </div></div>
    </div>

    <div class="col-12 col-lg-4">
        <div class="card shadow-sm border-0"><div class="card-body p-4">
            <h2 class="h5">Thông tin giao hàng</h2>
            <dl class="mb-0">
                <dt>Chi nhánh</dt><dd><c:out value="${detail.order.branch.name}"/></dd>
                <dt>Người nhận</dt><dd><c:out value="${detail.order.receiverName}"/> · <c:out value="${detail.order.receiverPhone}"/></dd>
                <dt>Địa chỉ</dt><dd><c:out value="${detail.order.deliveryAddress}"/></dd>
                <dt>Thanh toán</dt>
                <dd>
                    ${detail.order.paymentMethod == 'COD' ? 'COD – khi nhận hàng' : 'VNPay'}
                    <c:if test="${detail.order.paymentMethod == 'VNPAY'}">
                        <br><span class="badge ${detail.order.paymentStatus.badgeClass}"><c:out value="${detail.order.paymentStatus.label}"/></span>
                    </c:if>
                </dd>
                <c:if test="${not empty detail.order.note}"><dt>Ghi chú</dt><dd><c:out value="${detail.order.note}"/></dd></c:if>
            </dl>
            <c:if test="${detail.payable}">
                <form method="post" action="<c:url value='/payments/vnpay/${detail.order.code}/retry'/>" class="mt-3">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button class="btn btn-utetra w-100" type="submit">Thanh toán lại qua VNPay</button>
                </form>
            </c:if>
            <c:if test="${detail.cancellable}">
                <form method="post" action="<c:url value='/orders/${detail.order.code}/cancel'/>" class="mt-3"
                      onsubmit="return confirm('Bạn chắc chắn muốn hủy đơn này?');">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button class="btn btn-outline-danger w-100" type="submit">Hủy đơn hàng</button>
                </form>
                <p class="small text-muted mt-2 mb-0">Chỉ có thể hủy trước khi chi nhánh xác nhận.</p>
            </c:if>
        </div></div>
    </div>
</div>
</body>
</html>
