<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<html>
<head>
    <title>Trang chủ</title>
</head>
<body>
<section class="p-4 p-md-5 mb-4 bg-white rounded-3 shadow-sm hero-box">
    <h1 class="display-5 fw-bold">🧋 UTETra - Trà sữa chuẩn vị</h1>
    <p class="lead">Chọn đồ uống yêu thích, đặt tại chi nhánh gần bạn nhất.</p>
    <form method="get" action="<c:url value='/menu'/>" class="row g-2">
        <div class="col-12 col-md-8 col-lg-6">
            <input type="search" name="q" maxlength="100" class="form-control form-control-lg"
                   placeholder="Bạn muốn uống gì hôm nay?" aria-label="Tìm đồ uống">
        </div>
        <div class="col-auto">
            <button type="submit" class="btn btn-utetra btn-lg">Tìm kiếm</button>
        </div>
    </form>
</section>

<c:if test="${not empty categories}">
    <h2 class="h5 mb-3">Danh mục</h2>
    <div class="d-flex flex-wrap gap-2 mb-4">
        <c:forEach var="cat" items="${categories}">
            <a class="btn btn-outline-secondary"
               href="<c:url value='/menu'><c:param name='category' value='${cat.id}'/></c:url>"><c:out value="${cat.name}"/></a>
        </c:forEach>
    </div>
</c:if>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h2 class="h5 mb-0">Đồ uống mới nhất</h2>
    <a href="<c:url value='/menu'/>">Xem tất cả →</a>
</div>
<c:choose>
    <c:when test="${empty featured}">
        <p class="text-muted">Chưa có sản phẩm nào.</p>
    </c:when>
    <c:otherwise>
        <div class="row row-cols-2 row-cols-md-3 row-cols-lg-4 g-3 mb-5">
            <c:forEach var="p" items="${featured}">
                <%@ include file="/WEB-INF/views/fragments/product-card.jspf" %>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<c:if test="${not empty branches}">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h2 class="h5 mb-0">Chi nhánh</h2>
        <a href="<c:url value='/branches'/>">Xem tất cả →</a>
    </div>
    <div class="row row-cols-1 row-cols-md-3 g-3">
        <c:forEach var="b" items="${branches}">
            <div class="col">
                <div class="card h-100 shadow-sm"><div class="card-body">
                    <h3 class="h6"><c:out value="${b.branch.name}"/>
                        <c:if test="${b.openNow}"><span class="badge text-bg-success ms-1">Đang mở</span></c:if>
                    </h3>
                    <p class="small text-muted mb-0">📍 <c:out value="${b.branch.address}"/></p>
                </div></div>
            </div>
        </c:forEach>
    </div>
</c:if>
</body>
</html>
