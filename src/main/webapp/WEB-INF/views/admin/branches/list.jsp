<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html><head><title>Chi nhánh</title></head><body>
<div class="d-flex justify-content-between align-items-center mb-3">
    <div><h1 class="h3 mb-1">Chi nhánh</h1><p class="text-muted mb-0">Quản lý địa điểm, thời gian hoạt động và trạng thái phục vụ.</p></div>
    <a class="btn btn-utetra" href="<c:url value='/admin/branches/new'/>">+ Thêm chi nhánh</a>
</div>
<%@ include file="/WEB-INF/views/admin/_notice.jspf" %>
<p class="small text-muted">Chi nhánh đã có đơn hàng sẽ được giữ lại để bảo toàn lịch sử. Hãy ngừng hoạt động thay vì xóa.</p>
<div class="table-responsive bg-white rounded-3 shadow-sm"><table class="table table-hover align-middle mb-0">
    <thead class="table-light"><tr><th>Tên chi nhánh</th><th>Địa chỉ</th><th>Liên hệ</th><th>Giờ mở cửa</th><th>Trạng thái</th><th class="text-end">Thao tác</th></tr></thead>
    <tbody><c:forEach var="branch" items="${branches}"><tr>
        <td class="fw-semibold"><c:out value="${branch.name}"/></td><td><c:out value="${branch.address}"/></td>
        <td><c:out value="${empty branch.phone ? '—' : branch.phone}"/></td>
        <td><c:out value="${empty branch.openTime ? '—' : branch.openTime}"/><c:if test="${not empty branch.closeTime}"> – <c:out value="${branch.closeTime}"/></c:if></td>
        <td><span class="badge ${branch.active ? 'text-bg-success' : 'text-bg-secondary'}"><c:out value="${branch.active ? 'Đang hoạt động' : 'Đang ẩn'}"/></span></td>
        <td class="text-end"><div class="d-inline-flex gap-1"><a class="btn btn-sm btn-outline-primary" href="<c:url value='/admin/branches/${branch.id}/edit'/>">Sửa</a>
            <form method="post" action="<c:url value='/admin/branches/${branch.id}/toggle'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-sm btn-outline-secondary" type="submit"><c:out value="${branch.active ? 'Ngừng hoạt động' : 'Kích hoạt'}"/></button></form>
        </div></td>
    </tr></c:forEach><c:if test="${empty branches}"><tr><td colspan="6" class="text-center text-muted py-4">Chưa có chi nhánh nào.</td></tr></c:if></tbody>
</table></div>
</body></html>
