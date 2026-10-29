<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head><title>Giỏ hàng</title></head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0">Giỏ hàng của bạn</h1>
    <a href="<c:url value='/menu'/>" class="btn btn-outline-secondary">← Chọn thêm món</a>
</div>

<c:if test="${not empty okMessage}"><div class="alert alert-success"><c:out value="${okMessage}"/></div></c:if>
<c:if test="${not empty errMessage}"><div class="alert alert-danger"><c:out value="${errMessage}"/></div></c:if>

<c:choose>
    <c:when test="${empty cart.lines}">
        <div class="text-center bg-white rounded-3 shadow-sm py-5">
            <div class="display-3">🛒</div>
            <h2 class="h5">Giỏ hàng đang trống</h2>
            <a class="btn btn-utetra mt-2" href="<c:url value='/menu'/>">Xem thực đơn</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="row g-4">
            <div class="col-12 col-lg-8">
                <div class="d-flex flex-column gap-3">
                    <c:forEach var="line" items="${cart.lines}">
                        <div class="card shadow-sm"><div class="card-body">
                            <div class="row g-3 align-items-center">
                                <div class="col-3 col-md-2">
                                    <c:choose>
                                        <c:when test="${not empty line.item.product.imageUrl}">
                                            <img class="img-fluid rounded cart-thumb" src="<c:out value='${line.item.product.imageUrl}'/>"
                                                 alt="<c:out value='${line.item.product.name}'/>">
                                        </c:when>
                                        <c:otherwise><div class="cart-thumb product-img-placeholder rounded">🧋</div></c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="col-9 col-md-5">
                                    <h2 class="h6 mb-1"><c:out value="${line.item.product.name}"/></h2>
                                    <c:if test="${line.item.product.soldOut}"><span class="badge text-bg-danger mb-1">Đã hết – vui lòng xóa khỏi giỏ</span></c:if>
                                    <div class="small text-muted">
                                        Size <c:out value="${line.item.size.name}"/> · Đường ${line.item.sugarPercent}% · Đá ${line.item.icePercent}%
                                    </div>
                                    <c:if test="${not empty line.item.toppings}">
                                        <div class="small text-muted">Topping:
                                            <c:forEach var="t" items="${line.item.toppings}" varStatus="loop"><c:out value="${t.name}"/><c:if test="${!loop.last}">, </c:if></c:forEach>
                                        </div>
                                    </c:if>
                                    <c:if test="${not empty line.item.note}"><div class="small">Ghi chú: <c:out value="${line.item.note}"/></div></c:if>
                                    <div class="fw-semibold product-price mt-1"><fmt:formatNumber value="${line.unitPrice}" type="number"/> đ / ly</div>
                                </div>
                                <div class="col-7 col-md-3">
                                    <form method="post" action="<c:url value='/cart/items/${line.item.id}/quantity'/>" class="d-flex gap-2">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <input class="form-control form-control-sm" type="number" name="quantity"
                                               value="${line.item.quantity}" min="1" max="20" aria-label="Số lượng">
                                        <button class="btn btn-sm btn-outline-secondary" type="submit">Cập nhật</button>
                                    </form>
                                </div>
                                <div class="col-5 col-md-2 text-end">
                                    <div class="fw-bold"><fmt:formatNumber value="${line.lineTotal}" type="number"/> đ</div>
                                    <form method="post" action="<c:url value='/cart/items/${line.item.id}/delete'/>" class="mt-2">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <button class="btn btn-sm btn-link text-danger p-0" type="submit">Xóa</button>
                                    </form>
                                </div>
                            </div>
                        </div></div>
                    </c:forEach>
                </div>
                <form method="post" action="<c:url value='/cart/clear'/>" class="mt-3"
                      onsubmit="return confirm('Xóa toàn bộ giỏ hàng?');">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button class="btn btn-outline-danger btn-sm" type="submit">Xóa toàn bộ</button>
                </form>
            </div>
            <div class="col-12 col-lg-4">
                <div class="card shadow-sm sticky-lg-top" style="top:1rem"><div class="card-body">
                    <h2 class="h5">Tóm tắt</h2>
                    <div class="d-flex justify-content-between mb-2"><span>Số lượng</span><strong>${cart.totalQuantity} ly</strong></div>
                    <div class="d-flex justify-content-between h5 border-top pt-3"><span>Tạm tính</span><strong class="product-price"><fmt:formatNumber value="${cart.subtotal}" type="number"/> đ</strong></div>
                    <p class="small text-muted">Thanh toán khi nhận hàng (COD).</p>
                    <c:choose><c:when test="${cart.soldOutItemPresent}"><div class="alert alert-warning small">Giỏ hàng có món đã hết. Vui lòng xóa hoặc chọn món khác trước khi đặt hàng.</div><a class="btn btn-outline-secondary w-100" href="<c:url value='/menu'/>">Chọn món khác</a></c:when><c:otherwise><a class="btn btn-utetra w-100" href="<c:url value='/checkout'/>">Tiến hành đặt hàng</a></c:otherwise></c:choose>
                </div></div>
            </div>
        </div>
    </c:otherwise>
</c:choose>
</body>
</html>
