<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html><head><title>Người dùng và nhân viên</title></head><body>
<div class="d-flex justify-content-between align-items-center mb-3"><div><h1 class="h3 mb-1">Người dùng / Nhân viên</h1><p class="text-muted mb-0">Tra cứu khách hàng, tạo tài khoản nhân viên và quản lý quyền truy cập.</p></div><a class="btn btn-utetra" href="<c:url value='/admin/users/new'/>">+ Thêm nhân viên</a></div>
<%@ include file="/WEB-INF/views/admin/_notice.jspf" %>
<form method="get" action="<c:url value='/admin/users'/>" class="card card-body border-0 shadow-sm mb-3"><div class="row g-2 align-items-end">
    <div class="col-md-3"><label class="form-label" for="role">Vai trò</label><select class="form-select" id="role" name="role"><option value="">Tất cả</option><option value="USER" ${selectedRole == 'USER' ? 'selected' : ''}>Khách hàng</option><option value="STAFF" ${selectedRole == 'STAFF' ? 'selected' : ''}>Nhân viên</option><option value="ADMIN" ${selectedRole == 'ADMIN' ? 'selected' : ''}>Quản trị viên</option></select></div>
    <div class="col-md-3"><label class="form-label" for="active">Trạng thái</label><select class="form-select" id="active" name="active"><option value="">Tất cả</option><option value="true" ${selectedActive == true ? 'selected' : ''}>Đang hoạt động</option><option value="false" ${selectedActive == false ? 'selected' : ''}>Đã khóa</option></select></div>
    <div class="col-md-4"><label class="form-label" for="q">Tên, email hoặc số điện thoại</label><input class="form-control" id="q" name="q" value="<c:out value='${q}'/>" maxlength="100"></div>
    <div class="col-md-2 d-flex gap-2"><button class="btn btn-primary" type="submit">Lọc</button><a class="btn btn-outline-secondary" href="<c:url value='/admin/users'/>">Xóa lọc</a></div>
</div></form>
<div class="table-responsive bg-white rounded-3 shadow-sm"><table class="table table-hover align-middle mb-0">
    <thead class="table-light"><tr><th>Tài khoản</th><th>Vai trò</th><th>Chi nhánh</th><th>Liên hệ</th><th>Ngày tạo</th><th>Trạng thái</th><th class="text-end">Thao tác</th></tr></thead>
    <tbody><c:forEach var="u" items="${users.content}"><tr>
        <td class="fw-semibold"><c:out value="${u.fullName}"/><br><span class="small text-muted"><c:out value="${u.email}"/></span></td>
        <td><c:choose><c:when test="${u.role == 'USER'}">Khách hàng</c:when><c:when test="${u.role == 'STAFF'}">Nhân viên</c:when><c:otherwise>Quản trị viên</c:otherwise></c:choose></td>
        <td><c:out value="${empty u.branch ? '—' : u.branch.name}"/></td><td><c:out value="${empty u.phone ? '—' : u.phone}"/></td>
        <td class="text-nowrap"><c:out value="${u.createdAt.format(dateTimeFormatter)}"/></td><td><span class="badge ${u.active ? 'text-bg-success' : 'text-bg-secondary'}"><c:out value="${u.active ? 'Hoạt động' : 'Đã khóa'}"/></span></td>
        <td class="text-end"><c:if test="${u.role != 'ADMIN'}"><div class="d-inline-flex gap-1"><a class="btn btn-sm btn-outline-primary" href="<c:url value='/admin/users/${u.id}/edit'/>">Sửa</a><form method="post" action="<c:url value='/admin/users/${u.id}/toggle'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-sm btn-outline-secondary" type="submit"><c:out value="${u.active ? 'Khóa' : 'Mở khóa'}"/></button></form></div></c:if><c:if test="${u.role == 'ADMIN'}"><span class="small text-muted">Được bảo vệ</span></c:if></td>
    </tr></c:forEach><c:if test="${empty users.content}"><tr><td colspan="7" class="text-center text-muted py-4">Không tìm thấy tài khoản phù hợp.</td></tr></c:if></tbody>
</table></div>
<c:if test="${users.totalPages > 1}"><nav class="mt-4"><ul class="pagination justify-content-center"><c:forEach begin="1" end="${users.totalPages}" var="p"><li class="page-item ${p == users.number + 1 ? 'active' : ''}"><a class="page-link" href="<c:url value='/admin/users'><c:param name='role' value='${selectedRole}'/><c:param name='active' value='${selectedActive}'/><c:param name='q' value='${q}'/><c:param name='page' value='${p}'/></c:url>">${p}</a></li></c:forEach></ul></nav></c:if>
</body></html>
