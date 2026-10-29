<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head><title>Đơn mới cần nhận</title></head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-3">
    <div>
        <h1 class="h3 mb-1">Đơn mới cần nhận</h1>
        <p class="text-muted mb-0">Chỉ hiển thị đơn chưa nhận, ưu tiên đơn được đặt trước.</p>
    </div>
    <form method="get" action="<c:url value='/staff/orders'/>" class="staff-order-search d-flex gap-2">
        <label class="visually-hidden" for="q">Mã đơn hoặc số điện thoại</label>
        <input class="form-control" id="q" name="q" value="<c:out value='${q}'/>" maxlength="100"
               placeholder="Mã đơn hoặc số điện thoại">
        <button class="btn btn-primary" type="submit">Tìm</button>
        <c:if test="${not empty q}"><a class="btn btn-outline-secondary" href="<c:url value='/staff/orders'/>">Xóa</a></c:if>
    </form>
</div>

<nav class="nav nav-pills gap-2 mb-3" aria-label="Khu vực xử lý đơn">
    <a class="nav-link active" href="<c:url value='/staff/orders'/>">Đơn mới cần nhận <span class="badge text-bg-light ms-1">${orders.size()}</span></a>
    <a class="nav-link" href="<c:url value='/staff/orders/processing'/>">Đang pha chế / giao</a>
</nav>

<c:if test="${not empty okMessage}"><div class="alert alert-success py-2"><c:out value="${okMessage}"/></div></c:if>
<c:if test="${not empty errMessage}"><div class="alert alert-danger py-2"><c:out value="${errMessage}"/></div></c:if>

<div class="alert alert-info border-0">
    Kiểm tra kỹ món, ghi chú và thông tin giao hàng. Nếu có vấn đề, hãy gọi, nhắn SMS hoặc gửi email cho khách trước khi nhận đơn.
</div>

<c:choose>
    <c:when test="${empty orders}">
        <div class="card border-0 shadow-sm"><div class="card-body text-center text-muted py-5">Hiện không có đơn mới cần xác nhận.</div></div>
    </c:when>
    <c:otherwise>
        <div class="staff-work-list">
            <c:forEach var="order" items="${orders}">
                <c:set var="actionUrl" value="/staff/orders/${order.code}/accept"/>
                <c:set var="actionLabel" value="Nhận đơn & bắt đầu pha chế"/>
                <c:set var="actionHelp" value="Xác nhận đã kiểm tra đơn và bắt đầu pha chế. Đơn sẽ được chuyển sang trang xử lý."/>
                <c:set var="actionReturn" value=""/>
                <%@ include file="_work-order-card.jspf" %>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>
</body>
</html>
