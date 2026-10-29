<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="isEdit" value="${not empty managedUser}"/>
<html><head><title><c:out value="${isEdit ? 'Sửa tài khoản' : 'Thêm nhân viên'}"/></title></head><body>
<nav><ol class="breadcrumb"><li class="breadcrumb-item"><a href="<c:url value='/admin/users'/>">Người dùng / Nhân viên</a></li><li class="breadcrumb-item active"><c:out value="${isEdit ? 'Sửa' : 'Thêm mới'}"/></li></ol></nav>
<h1 class="h3 mb-3"><c:out value="${isEdit ? 'Sửa tài khoản' : 'Thêm nhân viên'}"/></h1>
<c:choose><c:when test="${isEdit}"><c:url var="formAction" value="/admin/users/${managedUser.id}/edit"/></c:when><c:otherwise><c:url var="formAction" value="/admin/users/new"/></c:otherwise></c:choose>
<form:form method="post" action="${formAction}" modelAttribute="userForm" novalidate="novalidate" cssClass="card border-0 shadow-sm p-4" cssStyle="max-width:760px">
    <form:errors path="*" element="div" cssClass="alert alert-danger"/>
    <div class="row g-3"><div class="col-md-6"><label class="form-label" for="fullName">Họ và tên</label><form:input path="fullName" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="100"/><form:errors path="fullName" cssClass="invalid-feedback d-block"/></div>
        <div class="col-md-6"><label class="form-label" for="email">Email đăng nhập</label><form:input path="email" type="email" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="150"/><form:errors path="email" cssClass="invalid-feedback d-block"/></div>
        <div class="col-md-6"><label class="form-label" for="phone">Số điện thoại</label><form:input path="phone" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="20"/><form:errors path="phone" cssClass="invalid-feedback d-block"/></div>
        <div class="col-md-6"><label class="form-label" for="role">Vai trò</label><form:select path="role" cssClass="form-select" cssErrorClass="form-select is-invalid"><form:option value="STAFF">Nhân viên</form:option><form:option value="USER">Khách hàng</form:option></form:select><form:errors path="role" cssClass="invalid-feedback d-block"/></div>
        <div class="col-12"><label class="form-label" for="branchId">Chi nhánh của nhân viên</label><form:select path="branchId" cssClass="form-select" cssErrorClass="form-select is-invalid"><form:option value="">-- Không áp dụng cho khách hàng --</form:option><c:forEach var="b" items="${branches}"><form:option value="${b.id}"><c:out value="${b.name}"/></form:option></c:forEach></form:select><form:errors path="branchId" cssClass="invalid-feedback d-block"/></div>
        <div class="col-12"><label class="form-label" for="address">Địa chỉ</label><form:input path="address" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="255"/><form:errors path="address" cssClass="invalid-feedback d-block"/></div>
        <div class="col-12"><label class="form-label" for="password"><c:out value="${isEdit ? 'Mật khẩu mới (để trống nếu không đổi)' : 'Mật khẩu'}"/></label><form:password path="password" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="72" autocomplete="new-password"/><form:errors path="password" cssClass="invalid-feedback d-block"/><div class="form-text">Từ 8 ký tự trở lên.</div></div>
    </div>
    <div class="form-check form-switch my-4"><form:checkbox path="active" cssClass="form-check-input"/><label class="form-check-label" for="active">Tài khoản đang hoạt động</label></div>
    <div class="d-flex gap-2"><button class="btn btn-utetra" type="submit">Lưu</button><a class="btn btn-outline-secondary" href="<c:url value='/admin/users'/>">Hủy</a></div>
</form:form></body></html>
