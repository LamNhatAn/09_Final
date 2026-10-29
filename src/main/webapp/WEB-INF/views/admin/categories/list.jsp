<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Danh mục</title>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0">Danh mục</h1>
    <a class="btn btn-utetra" href="<c:url value='/admin/categories/new'/>">+ Thêm danh mục</a>
</div>

<%@ include file="/WEB-INF/views/admin/_notice.jspf" %>

<p class="text-muted small">Ẩn một danh mục sẽ ẩn luôn các sản phẩm của nó trên trang khách. Chỉ xóa được danh mục chưa có sản phẩm.</p>

<c:choose>
    <c:when test="${empty categories}">
        <div class="alert alert-light border text-center">Chưa có danh mục nào.</div>
    </c:when>
    <c:otherwise>
        <div class="table-responsive bg-white rounded-3 shadow-sm">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th>Tên</th>
                    <th>Mô tả</th>
                    <th class="text-center">Số sản phẩm</th>
                    <th>Topping</th>
                    <th>Trạng thái</th>
                    <th class="text-end" style="width:260px">Thao tác</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="cat" items="${categories}">
                    <tr>
                        <td class="fw-semibold"><c:out value="${cat.name}"/></td>
                        <td class="text-muted small"><c:out value="${cat.description}"/></td>
                        <td class="text-center">
                            <c:choose>
                                <c:when test="${empty productCounts[cat.id]}">0</c:when>
                                <c:otherwise>
                                    <a href="<c:url value='/admin/products'><c:param name='category' value='${cat.id}'/></c:url>">${productCounts[cat.id]}</a>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td><c:choose><c:when test="${cat.allowToppings}"><span class="badge text-bg-info">Được chọn topping</span></c:when><c:otherwise><span class="text-muted small">Không áp dụng</span></c:otherwise></c:choose></td>
                        <td>
                            <c:choose>
                                <c:when test="${cat.active}"><span class="badge text-bg-success">Đang hiển thị</span></c:when>
                                <c:otherwise><span class="badge text-bg-secondary">Đang ẩn</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-end">
                            <div class="d-inline-flex gap-1">
                                <a class="btn btn-sm btn-outline-primary" href="<c:url value='/admin/categories/${cat.id}/edit'/>">Sửa</a>
                                <form method="post" action="<c:url value='/admin/categories/${cat.id}/toggle'/>" class="m-0">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <button type="submit" class="btn btn-sm btn-outline-secondary"><c:out value="${cat.active ? 'Ẩn' : 'Hiện'}"/></button>
                                </form>
                                <form method="post" action="<c:url value='/admin/categories/${cat.id}/delete'/>" class="m-0"
                                      onsubmit="return confirm('Xóa danh mục này?');">
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
