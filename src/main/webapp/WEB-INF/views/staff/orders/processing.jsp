<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head><title>Đang pha chế và giao</title></head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-3">
    <div>
        <h1 class="h3 mb-1">Đang pha chế và giao</h1>
        <p class="text-muted mb-0">Theo dõi riêng các đơn đã nhận, không lẫn với đơn mới.</p>
    </div>
    <form method="get" action="<c:url value='/staff/orders/processing'/>" class="staff-order-search d-flex gap-2">
        <label class="visually-hidden" for="q">Mã đơn hoặc số điện thoại</label>
        <input class="form-control" id="q" name="q" value="<c:out value='${q}'/>" maxlength="100"
               placeholder="Mã đơn hoặc số điện thoại">
        <button class="btn btn-primary" type="submit">Tìm</button>
        <c:if test="${not empty q}"><a class="btn btn-outline-secondary" href="<c:url value='/staff/orders/processing'/>">Xóa</a></c:if>
    </form>
</div>

<nav class="nav nav-pills gap-2 mb-3" aria-label="Khu vực xử lý đơn">
    <a class="nav-link" href="<c:url value='/staff/orders'/>">Đơn mới cần nhận</a>
    <a class="nav-link active" href="<c:url value='/staff/orders/processing'/>">Đang pha chế / giao
        <span class="badge text-bg-light ms-1">${preparingOrders.size() + deliveringOrders.size()}</span>
    </a>
</nav>

<c:if test="${not empty okMessage}"><div class="alert alert-success py-2"><c:out value="${okMessage}"/></div></c:if>
<c:if test="${not empty errMessage}"><div class="alert alert-danger py-2"><c:out value="${errMessage}"/></div></c:if>

<section class="mb-5" aria-labelledby="preparing-title">
    <div class="d-flex align-items-center gap-2 mb-3">
        <h2 class="h4 mb-0" id="preparing-title">Đang pha chế</h2>
        <span class="badge text-bg-primary">${preparingOrders.size()}</span>
    </div>
    <c:choose>
        <c:when test="${empty preparingOrders}"><div class="alert alert-light border">Không có đơn đang pha chế.</div></c:when>
        <c:otherwise>
            <div class="staff-work-list">
                <c:forEach var="order" items="${preparingOrders}">
                    <c:set var="actionUrl" value="/staff/orders/${order.code}/advance"/>
                    <c:set var="actionReturn" value="processing"/>
                    <c:choose>
                        <c:when test="${order.status == 'CONFIRMED'}">
                            <c:set var="actionLabel" value="Bắt đầu pha chế"/>
                            <c:set var="actionHelp" value="Đơn cũ đã xác nhận nhưng chưa bắt đầu pha chế."/>
                        </c:when>
                        <c:otherwise>
                            <c:set var="actionLabel" value="Pha chế xong · bắt đầu giao"/>
                            <c:set var="actionHelp" value="Chỉ xác nhận sau khi đã hoàn tất đầy đủ các món."/>
                        </c:otherwise>
                    </c:choose>
                    <%@ include file="_work-order-card.jspf" %>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<section aria-labelledby="delivering-title">
    <div class="d-flex align-items-center gap-2 mb-3">
        <h2 class="h4 mb-0" id="delivering-title">Đang giao</h2>
        <span class="badge text-bg-secondary">${deliveringOrders.size()}</span>
    </div>
    <c:choose>
        <c:when test="${empty deliveringOrders}"><div class="alert alert-light border">Không có đơn đang giao.</div></c:when>
        <c:otherwise>
            <div class="staff-work-list">
                <c:forEach var="order" items="${deliveringOrders}">
                    <c:set var="actionUrl" value="/staff/orders/${order.code}/advance"/>
                    <c:set var="actionReturn" value="processing"/>
                    <c:set var="actionLabel" value="Đã giao · hoàn thành đơn"/>
                    <c:set var="actionHelp" value="Xác nhận khi khách đã nhận hàng."/>
                    <%@ include file="_work-order-card.jspf" %>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>
</body>
</html>
