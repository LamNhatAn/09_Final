<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="isEdit" value="${not empty topping}"/>
<html>
<head>
    <title><c:out value="${isEdit ? 'Sửa topping' : 'Thêm topping'}"/></title>
</head>
<body>
<nav aria-label="breadcrumb">
    <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="<c:url value='/admin/toppings'/>">Topping</a></li>
        <li class="breadcrumb-item active" aria-current="page"><c:out value="${isEdit ? 'Sửa' : 'Thêm mới'}"/></li>
    </ol>
</nav>
<h1 class="h3 mb-3"><c:out value="${isEdit ? 'Sửa topping' : 'Thêm topping'}"/></h1>

<c:choose>
    <c:when test="${isEdit}"><c:url var="formAction" value="/admin/toppings/${topping.id}/edit"/></c:when>
    <c:otherwise><c:url var="formAction" value="/admin/toppings/new"/></c:otherwise>
</c:choose>
<form:form method="post" action="${formAction}" modelAttribute="toppingForm" novalidate="novalidate"
           cssClass="bg-white rounded-3 shadow-sm p-4" cssStyle="max-width:640px">
    <div class="mb-3">
        <label class="form-label" for="name">Tên topping</label>
        <form:input path="name" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="100"/>
        <form:errors path="name" cssClass="invalid-feedback d-block"/>
    </div>
    <div class="mb-3">
        <label class="form-label" for="price">Giá (VNĐ)</label>
        <form:input path="price" type="number" min="0" step="500" cssClass="form-control" cssErrorClass="form-control is-invalid"/>
        <form:errors path="price" cssClass="invalid-feedback d-block"/>
    </div>
    <div class="form-check form-switch mb-4">
        <form:checkbox path="available" cssClass="form-check-input" id="available"/>
        <label class="form-check-label" for="available">Đang bán</label>
    </div>
    <div class="d-flex gap-2">
        <button type="submit" class="btn btn-utetra">Lưu</button>
        <a class="btn btn-outline-secondary" href="<c:url value='/admin/toppings'/>">Hủy</a>
    </div>
</form:form>
</body>
</html>
