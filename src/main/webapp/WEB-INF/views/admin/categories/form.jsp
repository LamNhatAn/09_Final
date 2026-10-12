<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="isEdit" value="${not empty category}"/>
<html>
<head>
    <title><c:out value="${isEdit ? 'Sửa danh mục' : 'Thêm danh mục'}"/></title>
</head>
<body>
<nav aria-label="breadcrumb">
    <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="<c:url value='/admin/categories'/>">Danh mục</a></li>
        <li class="breadcrumb-item active" aria-current="page"><c:out value="${isEdit ? 'Sửa' : 'Thêm mới'}"/></li>
    </ol>
</nav>
<h1 class="h3 mb-3"><c:out value="${isEdit ? 'Sửa danh mục' : 'Thêm danh mục'}"/></h1>

<c:choose>
    <c:when test="${isEdit}"><c:url var="formAction" value="/admin/categories/${category.id}/edit"/></c:when>
    <c:otherwise><c:url var="formAction" value="/admin/categories/new"/></c:otherwise>
</c:choose>
<form:form method="post" action="${formAction}" modelAttribute="categoryForm" novalidate="novalidate"
           cssClass="bg-white rounded-3 shadow-sm p-4" cssStyle="max-width:640px">
    <div class="mb-3">
        <label class="form-label" for="name">Tên danh mục</label>
        <form:input path="name" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="100"/>
        <form:errors path="name" cssClass="invalid-feedback d-block"/>
    </div>
    <div class="mb-3">
        <label class="form-label" for="description">Mô tả</label>
        <form:textarea path="description" rows="3" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="500"/>
        <form:errors path="description" cssClass="invalid-feedback d-block"/>
    </div>
    <div class="form-check form-switch mb-4">
        <form:checkbox path="active" cssClass="form-check-input" id="active"/>
        <label class="form-check-label" for="active">Hiển thị trên trang khách</label>
    </div>
    <div class="d-flex gap-2">
        <button type="submit" class="btn btn-utetra">Lưu</button>
        <a class="btn btn-outline-secondary" href="<c:url value='/admin/categories'/>">Hủy</a>
    </div>
</form:form>
</body>
</html>
