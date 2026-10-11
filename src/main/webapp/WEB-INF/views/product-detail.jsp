<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<html>
<head>
    <title><c:out value="${product.name}"/></title>
</head>
<body>
<nav aria-label="breadcrumb">
    <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="<c:url value='/menu'/>">Thực đơn</a></li>
        <li class="breadcrumb-item">
            <a href="<c:url value='/menu'><c:param name='category' value='${product.category.id}'/></c:url>"><c:out value="${product.category.name}"/></a>
        </li>
        <li class="breadcrumb-item active" aria-current="page"><c:out value="${product.name}"/></li>
    </ol>
</nav>

<div class="row g-4 mb-5">
    <div class="col-12 col-md-5">
        <c:choose>
            <c:when test="${not empty product.imageUrl}">
                <img src="<c:out value='${product.imageUrl}'/>" alt="<c:out value='${product.name}'/>"
                     class="img-fluid rounded-3 shadow-sm w-100 product-detail-img">
            </c:when>
            <c:otherwise>
                <div class="product-detail-img product-img-placeholder rounded-3 shadow-sm">🧋</div>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="col-12 col-md-7">
        <span class="badge text-bg-warning mb-2"><c:out value="${product.category.name}"/></span>
        <h1 class="h2"><c:out value="${product.name}"/></h1>
        <div class="h3 product-price mb-3">
            <fmt:formatNumber value="${product.basePrice}" type="number" groupingUsed="true"/> đ
            <small class="text-muted fs-6">(size S)</small>
        </div>
        <p class="text-muted"><c:out value="${product.description}"/></p>

        <h2 class="h6 mt-4">Kích cỡ</h2>
        <ul class="list-inline">
            <c:forEach var="s" items="${sizes}">
                <li class="list-inline-item border rounded px-3 py-2 bg-white mb-2">
                    <strong><c:out value="${s.name}"/></strong>
                    <span class="text-muted small">
                        <c:choose>
                            <c:when test="${s.extraPrice.signum() == 0}">giá gốc</c:when>
                            <c:otherwise>+<fmt:formatNumber value="${s.extraPrice}" type="number" groupingUsed="true"/> đ</c:otherwise>
                        </c:choose>
                    </span>
                </li>
            </c:forEach>
        </ul>

        <c:if test="${not empty toppings}">
            <h2 class="h6 mt-3">Topping</h2>
            <ul class="list-inline">
                <c:forEach var="t" items="${toppings}">
                    <li class="list-inline-item border rounded px-3 py-2 bg-white mb-2">
                        <c:out value="${t.name}"/>
                        <span class="text-muted small">+<fmt:formatNumber value="${t.price}" type="number" groupingUsed="true"/> đ</span>
                    </li>
                </c:forEach>
            </ul>
        </c:if>

        <div class="mt-4">
            <button type="button" class="btn btn-utetra btn-lg" disabled>Thêm vào giỏ hàng</button>
            <div class="form-text">Chức năng đặt hàng sẽ được mở ở bước tiếp theo.</div>
        </div>
    </div>
</div>

<c:if test="${not empty related}">
    <h2 class="h5 mb-3">Đồ uống cùng loại</h2>
    <div class="row row-cols-2 row-cols-md-4 g-3">
        <c:forEach var="p" items="${related}">
            <%@ include file="/WEB-INF/views/fragments/product-card.jspf" %>
        </c:forEach>
    </div>
</c:if>
</body>
</html>
