<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="isEdit" value="${not empty branch}"/>
<html><head><title><c:out value="${isEdit ? 'Sửa chi nhánh' : 'Thêm chi nhánh'}"/></title></head><body>
<nav><ol class="breadcrumb"><li class="breadcrumb-item"><a href="<c:url value='/admin/branches'/>">Chi nhánh</a></li><li class="breadcrumb-item active"><c:out value="${isEdit ? 'Sửa' : 'Thêm mới'}"/></li></ol></nav>
<h1 class="h3 mb-3"><c:out value="${isEdit ? 'Sửa chi nhánh' : 'Thêm chi nhánh'}"/></h1>
<c:choose><c:when test="${isEdit}"><c:url var="formAction" value="/admin/branches/${branch.id}/edit"/></c:when><c:otherwise><c:url var="formAction" value="/admin/branches/new"/></c:otherwise></c:choose>
<form:form method="post" action="${formAction}" modelAttribute="branchForm" novalidate="novalidate" cssClass="card border-0 shadow-sm p-4" cssStyle="max-width:760px">
    <div class="mb-3"><label class="form-label" for="name">Tên chi nhánh</label><form:input path="name" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="150"/><form:errors path="name" cssClass="invalid-feedback d-block"/></div>
    <div class="mb-3"><label class="form-label" for="address">Địa chỉ</label><form:input path="address" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="255"/><form:errors path="address" cssClass="invalid-feedback d-block"/></div>
    <div class="row g-3 mb-3"><div class="col-md-4"><label class="form-label" for="phone">Số điện thoại</label><form:input path="phone" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="20"/><form:errors path="phone" cssClass="invalid-feedback d-block"/></div>
        <div class="col-md-4"><label class="form-label" for="openTime">Giờ mở cửa</label><form:input path="openTime" type="time" cssClass="form-control" cssErrorClass="form-control is-invalid"/><form:errors path="openTime" cssClass="invalid-feedback d-block"/></div>
        <div class="col-md-4"><label class="form-label" for="closeTime">Giờ đóng cửa</label><form:input path="closeTime" type="time" cssClass="form-control" cssErrorClass="form-control is-invalid"/><form:errors path="closeTime" cssClass="invalid-feedback d-block"/></div></div>
    <div class="form-check form-switch mb-4"><form:checkbox path="active" cssClass="form-check-input"/><label class="form-check-label" for="active">Đang hoạt động</label><form:errors path="active" cssClass="text-danger small d-block"/></div>
    <div class="d-flex gap-2"><button class="btn btn-utetra" type="submit">Lưu</button><a class="btn btn-outline-secondary" href="<c:url value='/admin/branches'/>">Hủy</a></div>
</form:form></body></html>
