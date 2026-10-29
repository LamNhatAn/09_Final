<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html><head><title>Tình trạng món</title></head><body>
<div class="d-flex justify-content-between align-items-start mb-3"><div><h1 class="h3 mb-1">Tình trạng món</h1><p class="text-muted mb-0">Đánh dấu món tạm hết hoặc mở bán lại. Sản phẩm hết vẫn hiển thị trên thực đơn để khách dễ theo dõi.</p></div><a class="btn btn-outline-secondary" href="<c:url value='/menu'/>">Xem thực đơn khách</a></div>
<c:if test="${not empty okMessage}"><div class="alert alert-success"><c:out value="${okMessage}"/></div></c:if>
<form method="get" action="<c:url value='/staff/products'/>" class="card card-body border-0 shadow-sm mb-3"><div class="row g-2 align-items-end">
    <div class="col-md-5"><label class="form-label" for="q">Tên món</label><input class="form-control" type="search" id="q" name="q" maxlength="100" value="<c:out value='${q}'/>" placeholder="Tìm món cần cập nhật"></div>
    <div class="col-md-4"><label class="form-label" for="category">Danh mục</label><select class="form-select" id="category" name="category"><option value="">Tất cả danh mục</option><c:forEach var="cat" items="${categories}"><option value="${cat.id}" ${selectedCategory == cat.id ? 'selected' : ''}><c:out value="${cat.name}"/></option></c:forEach></select></div>
    <div class="col-md-3 d-flex gap-2"><button class="btn btn-success" type="submit">Lọc</button><a class="btn btn-outline-secondary" href="<c:url value='/staff/products'/>">Xóa lọc</a></div>
</div></form>
<div class="table-responsive bg-white rounded-3 shadow-sm"><table class="table table-hover align-middle mb-0">
    <thead class="table-light"><tr><th style="width:84px">Ảnh</th><th>Tên món</th><th>Danh mục</th><th class="text-end">Giá</th><th>Tình trạng</th><th class="text-end">Cập nhật</th></tr></thead>
    <tbody><c:forEach var="p" items="${products.content}"><tr class="${p.soldOut ? 'table-light' : ''}">
        <td><c:choose><c:when test="${not empty p.imageUrl}"><img src="<c:out value='${p.imageUrl}'/>" alt="" width="64" height="64" class="rounded" style="object-fit:cover"></c:when><c:otherwise><span class="fs-2">🧋</span></c:otherwise></c:choose></td>
        <td class="fw-semibold"><c:out value="${p.name}"/></td><td><c:out value="${p.category.name}"/></td><td class="text-end"><fmt:formatNumber value="${p.basePrice}" type="number"/> đ</td>
        <td><c:choose><c:when test="${p.soldOut}"><span class="badge text-bg-danger">Đã hết</span></c:when><c:otherwise><span class="badge text-bg-success">Đang bán</span></c:otherwise></c:choose></td>
        <td class="text-end"><form method="post" action="<c:url value='/staff/products/${p.id}/toggle-sold-out'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="category" value="${selectedCategory}"><input type="hidden" name="q" value="<c:out value='${q}'/>"><input type="hidden" name="page" value="${products.number + 1}"><button type="submit" class="btn btn-sm ${p.soldOut ? 'btn-success' : 'btn-outline-danger'}"><c:out value="${p.soldOut ? 'Mở bán lại' : 'Đánh dấu đã hết'}"/></button></form></td>
    </tr></c:forEach><c:if test="${empty products.content}"><tr><td colspan="6" class="text-center text-muted py-4">Không tìm thấy món phù hợp.</td></tr></c:if></tbody>
</table></div>
<c:if test="${products.totalPages > 1}"><nav class="mt-4"><ul class="pagination justify-content-center"><c:forEach begin="1" end="${products.totalPages}" var="p"><li class="page-item ${p == products.number + 1 ? 'active' : ''}"><a class="page-link" href="<c:url value='/staff/products'><c:param name='category' value='${selectedCategory}'/><c:param name='q' value='${q}'/><c:param name='page' value='${p}'/></c:url>">${p}</a></li></c:forEach></ul></nav></c:if>
</body></html>
