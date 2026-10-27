<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head><title>Đơn hàng ${detail.order.code}</title></head>
<body>
<nav aria-label="breadcrumb"><ol class="breadcrumb">
    <li class="breadcrumb-item"><a href="<c:url value='/staff/orders'/>">Đơn hàng</a></li>
    <li class="breadcrumb-item active"><c:out value="${detail.order.code}"/></li>
</ol></nav>

<c:if test="${not empty okMessage}"><div class="alert alert-success"><c:out value="${okMessage}"/></div></c:if>
<c:if test="${not empty errMessage}"><div class="alert alert-danger"><c:out value="${errMessage}"/></div></c:if>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
    <div>
        <h1 class="h3 mb-1">Đơn <c:out value="${detail.order.code}"/></h1>
        <span class="text-muted">Đặt lúc <c:out value="${detail.order.createdAt.format(dateTimeFormatter)}"/></span>
    </div>
    <div class="d-flex align-items-center gap-2">
        <span class="badge fs-6 ${detail.order.status.badgeClass}"><c:out value="${detail.order.status.label}"/></span>
        <c:if test="${detail.advanceable}">
            <form method="post" action="<c:url value='/staff/orders/${detail.order.code}/advance'/>">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button class="btn btn-success" type="submit" onclick="return confirm('Chuyển đơn sang trạng thái tiếp theo?');">
                    Chuyển sang <c:out value="${detail.nextStatus.label}"/>
                </button>
            </form>
        </c:if>
    </div>
</div>

<div class="row g-4">
    <div class="col-12 col-xl-8">
        <div class="card shadow-sm border-0 mb-4"><div class="card-body">
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
            <div class="d-flex justify-content-between h5 pt-3 mb-0"><span>Tổng cộng</span><strong><fmt:formatNumber value="${detail.order.totalAmount}" type="number"/> đ</strong></div>
        </div></div>

        <div class="card shadow-sm border-0"><div class="card-body">
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

    <div class="col-12 col-xl-4">
        <div class="card shadow-sm border-0"><div class="card-body">
            <h2 class="h5">Khách hàng và giao hàng</h2>
            <dl class="mb-0">
                <dt>Tài khoản</dt><dd><c:out value="${detail.order.user.fullName}"/> · <c:out value="${detail.order.user.email}"/></dd>
                <dt>Người nhận</dt><dd><c:out value="${detail.order.receiverName}"/> · <c:out value="${detail.order.receiverPhone}"/></dd>
                <dt>Địa chỉ</dt><dd><c:out value="${detail.order.deliveryAddress}"/></dd>
                <dt>Chi nhánh</dt><dd><c:out value="${detail.order.branch.name}"/></dd>
                <dt>Thanh toán</dt><dd>${detail.order.paymentMethod == 'COD' ? 'COD – khi nhận hàng' : 'VNPay'}</dd>
                <c:if test="${not empty detail.order.note}"><dt>Ghi chú</dt><dd><c:out value="${detail.order.note}"/></dd></c:if>
            </dl>
        </div></div>
    </div>
</div>
</body>
</html>
