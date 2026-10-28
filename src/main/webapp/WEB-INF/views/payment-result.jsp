<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head><title>Kết quả thanh toán VNPay</title></head>
<body>
<div class="card shadow-sm mx-auto" style="max-width:720px">
    <div class="card-body p-5 text-center">
        <c:choose>
            <c:when test="${outcome.paid}">
                <div class="display-3">✅</div>
                <h1 class="h3 mt-3">Thanh toán thành công</h1>
                <p class="text-muted">VNPay đã xác nhận giao dịch của đơn
                    <strong><c:out value="${outcome.orderCode}"/></strong>.</p>
            </c:when>
            <c:when test="${outcome.type == 'INVALID_SIGNATURE'}">
                <div class="display-3">⚠️</div>
                <h1 class="h3 mt-3">Chữ ký không hợp lệ</h1>
                <p class="text-muted">Dữ liệu phản hồi không được chấp nhận. Trạng thái thanh toán không bị thay đổi.</p>
            </c:when>
            <c:when test="${outcome.type == 'INVALID_AMOUNT'}">
                <div class="display-3">⚠️</div>
                <h1 class="h3 mt-3">Số tiền không khớp</h1>
                <p class="text-muted">Hệ thống đã từ chối cập nhật để bảo vệ đơn hàng.</p>
            </c:when>
            <c:otherwise>
                <div class="display-3">❌</div>
                <h1 class="h3 mt-3">Thanh toán chưa thành công</h1>
                <p class="text-muted">Bạn có thể mở đơn hàng và thử thanh toán lại.</p>
            </c:otherwise>
        </c:choose>
        <div class="d-flex justify-content-center gap-2 mt-4">
            <c:if test="${not empty outcome.orderCode}">
                <a class="btn btn-utetra" href="<c:url value='/orders/${outcome.orderCode}'/>">Xem đơn hàng</a>
            </c:if>
            <a class="btn btn-outline-secondary" href="<c:url value='/orders'/>">Đơn hàng của tôi</a>
        </div>
    </div>
</div>
</body>
</html>
