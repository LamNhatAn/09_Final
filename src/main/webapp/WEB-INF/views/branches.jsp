<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head>
    <title>Chi nhánh</title>
</head>
<body>
<h1 class="h3 mb-1">Hệ thống chi nhánh</h1>
<p class="text-muted mb-4">Chọn chi nhánh gần bạn nhất khi đặt hàng.</p>

<c:choose>
    <c:when test="${empty branches}">
        <p class="text-muted">Hiện chưa có chi nhánh nào đang hoạt động.</p>
    </c:when>
    <c:otherwise>
        <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-3">
            <c:forEach var="b" items="${branches}">
                <div class="col">
                    <div class="card h-100 shadow-sm">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h2 class="h5 mb-0"><c:out value="${b.branch.name}"/></h2>
                                <c:choose>
                                    <c:when test="${b.openNow}"><span class="badge text-bg-success">Đang mở cửa</span></c:when>
                                    <c:otherwise><span class="badge text-bg-secondary">Đã đóng cửa</span></c:otherwise>
                                </c:choose>
                            </div>
                            <p class="mb-1">📍 <c:out value="${b.branch.address}"/></p>
                            <c:if test="${not empty b.branch.phone}">
                                <p class="mb-1">📞 <a href="tel:<c:out value='${b.branch.phone}'/>"><c:out value="${b.branch.phone}"/></a></p>
                            </c:if>
                            <c:if test="${not empty b.branch.openTime and not empty b.branch.closeTime}">
                                <p class="mb-3">🕒 <c:out value="${b.branch.openTime}"/> – <c:out value="${b.branch.closeTime}"/></p>
                            </c:if>
                            <c:url var="mapUrl" value="https://www.google.com/maps/search/">
                                <c:param name="api" value="1"/>
                                <c:param name="query" value="${b.branch.address}"/>
                            </c:url>
                            <a class="btn btn-outline-secondary btn-sm" href="${mapUrl}" target="_blank" rel="noopener noreferrer">Xem bản đồ</a>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>
</body>
</html>
