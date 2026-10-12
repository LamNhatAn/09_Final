<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<html>
<head>
    <title>Sản phẩm</title>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0">Sản phẩm</h1>
    <a class="btn btn-utetra" href="<c:url value='/admin/products/new'/>">+ Thêm sản phẩm</a>
</div>

<%@ include file="/WEB-INF/views/admin/_notice.jspf" %>

<form method="get" action="<c:url value='/admin/products'/>" class="row g-2 mb-3">
    <div class="col-12 col-md-5">
        <input type="search" name="q" value="<c:out value='${q}'/>" maxlength="100" class="form-control"
               placeholder="Tìm theo tên (gõ không dấu cũng được)" aria-label="Từ khóa">
    </div>
    <div class="col-8 col-md-4">
        <select name="category" class="form-select" aria-label="Danh mục" onchange="this.form.submit()">
            <option value="">Tất cả danh mục</option>
            <c:forEach var="cat" items="${categories}">
                <option value="${cat.id}" ${selectedCategoryId == cat.id ? 'selected' : ''}><c:out value="${cat.name}"/></option>
            </c:forEach>
        </select>
    </div>
    <div class="col-4 col-md-2 d-grid">
        <button type="submit" class="btn btn-outline-secondary">Lọc</button>
    </div>
</form>

<c:choose>
    <c:when test="${products.totalElements == 0}">
        <div class="alert alert-light border text-center">Không có sản phẩm nào phù hợp.</div>
    </c:when>
    <c:otherwise>
        <p class="text-muted small mb-2">Tổng ${products.totalElements} sản phẩm</p>
        <div class="table-responsive bg-white rounded-3 shadow-sm">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr>
                    <th style="width:70px">Ảnh</th>
                    <th>Tên</th>
                    <th>Danh mục</th>
                    <th class="text-end">Giá gốc</th>
                    <th>Trạng thái</th>
                    <th class="text-end" style="width:260px">Thao tác</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="p" items="${products.content}">
                    <tr>
                        <td>
                            <c:choose>
                                <c:when test="${not empty p.imageUrl}">
                                    <%-- Cloudinary tu cat anh 80x80: chen phep bien doi vao URL, khong tai anh goc --%>
                                    <img src="<c:out value="${fn:replace(p.imageUrl, '/upload/', '/upload/c_fill,w_80,h_80,q_auto/')}"/>"
                                         alt="" width="56" height="56" class="rounded" style="object-fit:cover">
                                </c:when>
                                <c:otherwise><span class="fs-2">🧋</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="fw-semibold"><c:out value="${p.name}"/></td>
                        <td>
                            <c:out value="${p.category.name}"/>
                            <c:if test="${not p.category.active}"><span class="badge text-bg-secondary">danh mục ẩn</span></c:if>
                        </td>
                        <td class="text-end"><fmt:formatNumber value="${p.basePrice}" type="number" groupingUsed="true"/> đ</td>
                        <td>
                            <c:choose>
                                <c:when test="${p.available}"><span class="badge text-bg-success">Đang bán</span></c:when>
                                <c:otherwise><span class="badge text-bg-secondary">Ngừng bán</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-end">
                            <div class="d-inline-flex gap-1">
                                <a class="btn btn-sm btn-outline-primary" href="<c:url value='/admin/products/${p.id}/edit'/>">Sửa</a>

                                <form method="post" action="<c:url value='/admin/products/${p.id}/toggle'/>" class="m-0">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <input type="hidden" name="category" value="${selectedCategoryId}">
                                    <input type="hidden" name="q" value="<c:out value='${q}'/>">
                                    <input type="hidden" name="page" value="${products.number + 1}">
                                    <button type="submit" class="btn btn-sm btn-outline-secondary">
                                        <c:out value="${p.available ? 'Ẩn' : 'Hiện'}"/>
                                    </button>
                                </form>

                                <form method="post" action="<c:url value='/admin/products/${p.id}/delete'/>" class="m-0"
                                      onsubmit="return confirm('Xóa sản phẩm này? Ảnh trên Cloudinary cũng bị xóa.');">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <input type="hidden" name="category" value="${selectedCategoryId}">
                                    <input type="hidden" name="q" value="<c:out value='${q}'/>">
                                    <input type="hidden" name="page" value="${products.number + 1}">
                                    <button type="submit" class="btn btn-sm btn-outline-danger">Xóa</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>

        <c:if test="${products.totalPages > 1}">
            <nav class="mt-3" aria-label="Phân trang">
                <ul class="pagination justify-content-center flex-wrap">
                    <c:forEach var="i" begin="1" end="${products.totalPages}">
                        <c:url var="pageUrl" value="/admin/products">
                            <c:if test="${not empty selectedCategoryId}"><c:param name="category" value="${selectedCategoryId}"/></c:if>
                            <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
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
