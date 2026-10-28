<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html>
<head><title>Đặt hàng và thanh toán</title></head>
<body>
<nav aria-label="breadcrumb"><ol class="breadcrumb">
    <li class="breadcrumb-item"><a href="<c:url value='/cart'/>">Giỏ hàng</a></li>
    <li class="breadcrumb-item active">Đặt hàng</li>
</ol></nav>
<h1 class="h3 mb-4">Thông tin giao hàng</h1>

<div class="row g-4">
    <div class="col-12 col-lg-7">
        <form:form method="post" action="${pageContext.request.contextPath}/checkout"
                   modelAttribute="checkoutForm" cssClass="card shadow-sm" novalidate="novalidate">
            <div class="card-body p-4">
                <form:errors path="*" cssClass="alert alert-danger d-block" element="div"/>
                <div class="mb-3">
                    <label class="form-label" for="branchId">Chi nhánh nhận đơn</label>
                    <form:select path="branchId" cssClass="form-select" cssErrorClass="form-select is-invalid">
                        <form:option value="" label="-- Chọn chi nhánh --"/>
                        <c:forEach var="b" items="${branches}">
                            <form:option value="${b.branch.id}" label="${b.branch.name}"/>
                        </c:forEach>
                    </form:select>
                    <form:errors path="branchId" cssClass="invalid-feedback d-block"/>
                </div>
                <div class="mb-3">
                    <label class="form-label d-block">Phương thức thanh toán</label>
                    <div class="form-check border rounded p-3 ps-5 mb-2">
                        <form:radiobutton path="paymentMethod" value="COD" cssClass="form-check-input"/>
                        <label class="form-check-label" for="paymentMethod1">
                            <strong>COD</strong> – thanh toán khi nhận hàng
                        </label>
                    </div>
                    <div class="form-check border rounded p-3 ps-5">
                        <form:radiobutton path="paymentMethod" value="VNPAY" cssClass="form-check-input"
                                          disabled="${!vnpayConfigured}"/>
                        <label class="form-check-label" for="paymentMethod2">
                            <strong>VNPay</strong> – thẻ ATM, tài khoản ngân hàng hoặc VNPAY-QR
                        </label>
                        <c:if test="${!vnpayConfigured}">
                            <div class="small text-danger">Máy này chưa cấu hình khóa VNPay sandbox.</div>
                        </c:if>
                    </div>
                    <form:errors path="paymentMethod" cssClass="text-danger small d-block mt-1"/>
                </div>
                <div class="row g-3">
                    <div class="col-md-7">
                        <label class="form-label" for="receiverName">Tên người nhận</label>
                        <form:input path="receiverName" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="100"/>
                        <form:errors path="receiverName" cssClass="invalid-feedback d-block"/>
                    </div>
                    <div class="col-md-5">
                        <label class="form-label" for="receiverPhone">Số điện thoại</label>
                        <form:input path="receiverPhone" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="20"/>
                        <form:errors path="receiverPhone" cssClass="invalid-feedback d-block"/>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="deliveryAddress">Địa chỉ giao hàng</label>
                        <form:textarea path="deliveryAddress" rows="2" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="255"/>
                        <form:errors path="deliveryAddress" cssClass="invalid-feedback d-block"/>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="note">Ghi chú đơn hàng</label>
                        <form:textarea path="note" rows="3" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="500"/>
                        <form:errors path="note" cssClass="invalid-feedback d-block"/>
                    </div>
                </div>
            </div>
            <div class="card-footer bg-white p-4 d-flex gap-2">
                <button class="btn btn-utetra" type="submit">Xác nhận đặt hàng</button>
                <a class="btn btn-outline-secondary" href="<c:url value='/cart'/>">Quay lại giỏ</a>
            </div>
        </form:form>
    </div>
    <div class="col-12 col-lg-5">
        <div class="card shadow-sm"><div class="card-body">
            <h2 class="h5">Đơn hàng (${cart.totalQuantity} ly)</h2>
            <c:forEach var="line" items="${cart.lines}">
                <div class="d-flex justify-content-between gap-3 border-bottom py-2 small">
                    <span><c:out value="${line.item.product.name}"/> × ${line.item.quantity}<br><span class="text-muted">Size <c:out value="${line.item.size.name}"/></span></span>
                    <strong class="text-nowrap"><fmt:formatNumber value="${line.lineTotal}" type="number"/> đ</strong>
                </div>
            </c:forEach>
            <div class="d-flex justify-content-between h5 pt-3"><span>Tổng cộng</span><strong class="product-price"><fmt:formatNumber value="${cart.subtotal}" type="number"/> đ</strong></div>
        </div></div>
    </div>
</div>
</body>
</html>
