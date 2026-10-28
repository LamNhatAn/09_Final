<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head><title>Đơn hàng của tôi</title></head>
<body>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h1 class="h3 mb-0">Đơn hàng của tôi</h1>
    <a class="btn btn-utetra" href="<c:url value='/menu'/>">Đặt thêm món</a>
</div>

<c:if test="${not empty okMessage}"><div class="alert alert-success"><c:out value="${okMessage}"/></div></c:if>
<c:if test="${not empty errMessage}"><div class="alert alert-danger"><c:out value="${errMessage}"/></div></c:if>

<c:choose>
    <c:when test="${empty orders.content}">
        <div class="text-center bg-white rounded-3 shadow-sm py-5">
            <div class="display-3">🧾</div>
            <h2 class="h5">Bạn chưa có đơn hàng nào</h2>
            <a class="btn btn-utetra mt-2" href="<c:url value='/menu'/>">Xem thực đơn</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="card shadow-sm border-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                    <tr><th>Mã đơn</th><th>Thời gian</th><th>Chi nhánh</th><th>Trạng thái</th><th class="text-end">Tổng tiền</th><th></th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="order" items="${orders.content}">
                        <tr>
                            <td class="fw-semibold"><c:out value="${order.code}"/></td>
                            <td class="text-nowrap"><c:out value="${order.createdAt.format(dateTimeFormatter)}"/></td>
                            <td><c:out value="${order.branch.name}"/></td>
                            <td>
                                <span class="badge ${order.status.badgeClass}"><c:out value="${order.status.label}"/></span>
                                <c:if test="${order.paymentMethod == 'VNPAY'}"><br><span class="badge mt-1 ${order.paymentStatus.badgeClass}"><c:out value="${order.paymentStatus.label}"/></span></c:if>
                            </td>
                            <td class="text-end fw-semibold product-price"><fmt:formatNumber value="${order.totalAmount}" type="number"/> đ</td>
                            <td class="text-end"><a class="btn btn-sm btn-outline-secondary" href="<c:url value='/orders/${order.code}'/>">Chi tiết</a></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <c:if test="${orders.totalPages > 1}">
            <nav class="mt-4" aria-label="Phân trang đơn hàng">
                <ul class="pagination justify-content-center">
                    <c:forEach begin="1" end="${orders.totalPages}" var="p">
                        <li class="page-item ${p == orders.number + 1 ? 'active' : ''}">
                            <a class="page-link" href="<c:url value='/orders'><c:param name='page' value='${p}'/></c:url>">${p}</a>
                        </li>
                    </c:forEach>
                </ul>
            </nav>
        </c:if>
    </c:otherwise>
</c:choose>
</body>
</html>
