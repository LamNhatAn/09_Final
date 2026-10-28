<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head>
    <title>Thực đơn</title>
</head>
<body>
<div class="text-center mb-4">
    <p class="text-uppercase small fw-semibold menu-kicker mb-1">Khám phá UTETra</p>
    <h1 class="display-6 fw-bold mb-2">Thực đơn</h1>
    <p class="text-muted mb-0">Chọn dòng thức uống bạn yêu thích.</p>
</div>

<section class="mb-5" aria-labelledby="menu-categories-title">
    <div class="d-flex justify-content-between align-items-end mb-3">
        <h2 class="h4 mb-0" id="menu-categories-title">Danh mục thức uống</h2>
        <c:if test="${not empty selectedCategoryId}">
            <a class="small" href="<c:url value='/menu'/>">Xem tất cả</a>
        </c:if>
    </div>
    <div class="row row-cols-2 row-cols-md-4 g-3">
        <c:forEach var="card" items="${categoryCards}">
            <div class="col">
                <c:url var="categoryCardUrl" value="/menu">
                    <c:param name="category" value="${card.id}"/>
                </c:url>
                <a class="menu-category-card card h-100 border-0 shadow-sm text-decoration-none ${selectedCategoryId == card.id ? 'is-active' : ''}"
                   href="${categoryCardUrl}" aria-label="Xem danh mục ${card.name}">
                    <c:choose>
                        <c:when test="${not empty card.imageUrl}">
                            <img class="menu-category-img" src="<c:out value='${card.imageUrl}'/>" alt="" loading="lazy">
                        </c:when>
                        <c:otherwise>
                            <div class="menu-category-img product-img-placeholder" aria-hidden="true">🧋</div>
                        </c:otherwise>
                    </c:choose>
                    <div class="card-body text-center py-3">
                        <h3 class="h5 mb-0"><c:out value="${card.name}"/></h3>
                    </div>
                </a>
            </div>
        </c:forEach>
    </div>
</section>

<%-- Tim kiem + sap xep (GET): giu lai danh muc dang chon --%>
<form method="get" action="<c:url value='/menu'/>" class="row g-2 mb-3">
    <c:if test="${not empty selectedCategoryId}">
        <input type="hidden" name="category" value="${selectedCategoryId}">
    </c:if>
    <div class="col-12 col-md-7">
        <input type="search" name="q" value="<c:out value='${q}'/>" maxlength="100" class="form-control"
               placeholder="Tìm đồ uống theo tên, ví dụ: tra sua, matcha..." aria-label="Từ khóa tìm kiếm">
    </div>
    <div class="col-8 col-md-3">
        <select name="sort" class="form-select" aria-label="Sắp xếp" onchange="this.form.submit()">
            <option value="newest" ${sort == 'newest' ? 'selected' : ''}>Mới nhất</option>
            <option value="price_asc" ${sort == 'price_asc' ? 'selected' : ''}>Giá tăng dần</option>
            <option value="price_desc" ${sort == 'price_desc' ? 'selected' : ''}>Giá giảm dần</option>
            <option value="name" ${sort == 'name' ? 'selected' : ''}>Tên A → Z</option>
        </select>
    </div>
    <div class="col-4 col-md-2 d-grid">
        <button type="submit" class="btn btn-utetra">Tìm</button>
    </div>
</form>

<c:choose>
    <c:when test="${products.totalElements == 0}">
        <div class="text-center text-muted py-5">
            <div class="display-3">🔍</div>
            <p class="mb-3">Không tìm thấy đồ uống phù hợp<c:if test="${not empty q}"> với "<strong><c:out value="${q}"/></strong>"</c:if>.</p>
            <a class="btn btn-outline-secondary" href="<c:url value='/menu'/>">Xem tất cả</a>
        </div>
    </c:when>
    <c:otherwise>
        <p class="text-muted small">Có ${products.totalElements} đồ uống</p>
        <div class="row row-cols-2 row-cols-md-3 row-cols-lg-4 g-3">
            <c:forEach var="p" items="${products.content}">
                <%@ include file="/WEB-INF/views/fragments/product-card.jspf" %>
            </c:forEach>
        </div>

        <%-- Phan trang: products.number tinh tu 0, URL tinh tu 1 --%>
        <c:if test="${products.totalPages > 1}">
            <nav class="mt-4" aria-label="Phân trang">
                <ul class="pagination justify-content-center flex-wrap">
                    <c:forEach var="i" begin="1" end="${products.totalPages}">
                        <c:url var="pageUrl" value="/menu">
                            <c:if test="${not empty selectedCategoryId}"><c:param name="category" value="${selectedCategoryId}"/></c:if>
                            <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
                            <c:param name="sort" value="${sort}"/>
                            <c:param name="page" value="${i}"/>
                        </c:url>
                        <li class="page-item ${i == products.number + 1 ? 'active' : ''}">
                            <a class="page-link" href="${pageUrl}">${i}</a>
                        </li>
                    </c:forEach>
                </ul>
            </nav>
        </c:if>
    </c:otherwise>
</c:choose>
</body>
</html>
