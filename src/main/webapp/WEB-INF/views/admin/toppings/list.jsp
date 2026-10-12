<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head>
    <title>Topping</title>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0">Topping</h1>
    <a class="btn btn-utetra" href="<c:url value='/admin/toppings/new'/>">+ Thêm topping</a>
</div>

<%@ include file="/WEB-INF/views/admin/_notice.jspf" %>

<p class="text-muted small">Topping ngừng bán sẽ không hiện cho khách chọn. Đơn hàng đã đặt vẫn giữ nguyên tên và giá lúc đặt.</p>

<c:choose>
    <c:when test="${empty toppings}">
        <div class="alert alert-light border text-center">Chưa có topping nào.</div>
    </c:when>
    <c:otherwise>
        <div class="table-responsive bg-white rounded-3 shadow-sm">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th>Tên</th>
                    <th class="text-end">Giá</th>
                    <th>Trạng thái</th>
                    <th class="text-end" style="width:260px">Thao tác</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="t" items="${toppings}">
                    <tr>
                        <td class="fw-semibold"><c:out value="${t.name}"/></td>
                        <td class="text-end"><fmt:formatNumber value="${t.price}" type="number" groupingUsed="true"/> đ</td>
                        <td>
                            <c:choose>
                                <c:when test="${t.available}"><span class="badge text-bg-success">Đang bán</span></c:when>
                                <c:otherwise><span class="badge text-bg-secondary">Ngừng bán</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-end">
                            <div class="d-inline-flex gap-1">
                                <a class="btn btn-sm btn-outline-primary" href="<c:url value='/admin/toppings/${t.id}/edit'/>">Sửa</a>
                                <form method="post" action="<c:url value='/admin/toppings/${t.id}/toggle'/>" class="m-0">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <button type="submit" class="btn btn-sm btn-outline-secondary"><c:out value="${t.available ? 'Ẩn' : 'Hiện'}"/></button>
                                </form>
                                <form method="post" action="<c:url value='/admin/toppings/${t.id}/delete'/>" class="m-0"
                                      onsubmit="return confirm('Xóa topping này?');">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <button type="submit" class="btn btn-sm btn-outline-danger">Xóa</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>
</body>
</html>
