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
        <c:if test="${product.soldOut}"><span class="badge text-bg-danger mb-2">Đã hết</span></c:if>
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

        <c:if test="${not empty cartErrorMessage}">
            <div class="alert alert-danger mt-3"><c:out value="${cartErrorMessage}"/></div>
        </c:if>

        <c:choose>
            <c:when test="${product.soldOut}">
                <div class="alert alert-warning mt-4"><strong>Món này đang tạm hết.</strong><br>UTETra vẫn giữ món trên thực đơn để bạn tiện theo dõi. Vui lòng chọn món khác hoặc quay lại sau nhé.</div>
                <a class="btn btn-outline-secondary" href="<c:url value='/menu'/>">Chọn món khác</a>
            </c:when>
            <c:when test="${not empty currentUser and currentUser.customer}">
                <form method="post" action="<c:url value='/cart/items'/>" class="mt-4 border rounded-3 bg-white p-3">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <input type="hidden" name="productId" value="${product.id}">

                    <fieldset class="mb-3">
                        <legend class="h6">Chọn kích cỡ</legend>
                        <div class="d-flex flex-wrap gap-2">
                            <c:forEach var="s" items="${sizes}" varStatus="loop">
                                <div>
                                    <input class="btn-check" type="radio" name="sizeId" id="size-${s.id}"
                                           value="${s.id}" ${loop.first ? 'checked' : ''} required>
                                    <label class="btn btn-outline-secondary" for="size-${s.id}">
                                        <c:out value="${s.name}"/>
                                        <c:if test="${s.extraPrice.signum() > 0}">
                                            +<fmt:formatNumber value="${s.extraPrice}" type="number"/> đ
                                        </c:if>
                                    </label>
                                </div>
                            </c:forEach>
                        </div>
                    </fieldset>

                    <c:if test="${not empty toppings}">
                        <fieldset class="mb-3">
                            <legend class="h6">Chọn topping</legend>
                            <div class="row row-cols-1 row-cols-sm-2 g-2">
                                <c:forEach var="t" items="${toppings}">
                                    <div class="col">
                                        <div class="form-check">
                                            <input class="form-check-input" type="checkbox" name="toppingIds"
                                                   value="${t.id}" id="topping-${t.id}">
                                            <label class="form-check-label" for="topping-${t.id}">
                                                <c:out value="${t.name}"/> (+<fmt:formatNumber value="${t.price}" type="number"/> đ)
                                            </label>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </fieldset>
                    </c:if>

                    <div class="row g-2 mb-3">
                        <div class="col-6 col-lg-4">
                            <label class="form-label" for="sugarPercent">Mức đường</label>
                            <select class="form-select" id="sugarPercent" name="sugarPercent">
                                <option value="0">0%</option><option value="30">30%</option>
                                <option value="50">50%</option><option value="70">70%</option>
                                <option value="100" selected>100%</option>
                            </select>
                        </div>
                        <div class="col-6 col-lg-4">
                            <label class="form-label" for="icePercent">Mức đá</label>
                            <select class="form-select" id="icePercent" name="icePercent">
                                <option value="0">0%</option><option value="30">30%</option>
                                <option value="50">50%</option><option value="70">70%</option>
                                <option value="100" selected>100%</option>
                            </select>
                        </div>
                        <div class="col-12 col-lg-4">
                            <label class="form-label" for="quantity">Số lượng</label>
                            <input class="form-control" type="number" id="quantity" name="quantity"
                                   value="1" min="1" max="20" required>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label" for="note">Ghi chú cho món</label>
                        <input class="form-control" id="note" name="note" maxlength="255"
                               placeholder="Ví dụ: ít ngọt, để riêng topping...">
                    </div>
                    <button type="submit" class="btn btn-utetra btn-lg">Thêm vào giỏ hàng</button>
                </form>
            </c:when>
            <c:when test="${empty currentUser}">
                <div class="mt-4">
                    <a class="btn btn-utetra btn-lg"
                       href="<c:url value='/login'><c:param name='next' value='/products/${product.id}'/></c:url>">Đăng nhập để đặt món</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-light border mt-4">Chỉ tài khoản khách hàng mới sử dụng giỏ hàng.</div>
            </c:otherwise>
        </c:choose>
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
