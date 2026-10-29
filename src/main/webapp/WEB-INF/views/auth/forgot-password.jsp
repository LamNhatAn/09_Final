<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html><head><title>Quên mật khẩu</title></head><body><div class="row justify-content-center"><div class="col-md-7 col-lg-5"><div class="card shadow-sm border-0"><div class="card-body p-4 p-lg-5">
    <h1 class="h4 text-center mb-2">Quên mật khẩu</h1><p class="text-muted text-center mb-4">Nhập email đã đăng ký. UTETra sẽ gửi mã OTP để bạn tạo mật khẩu mới.</p>
    <form:form method="post" action="${pageContext.request.contextPath}/forgot-password" modelAttribute="forgotPasswordForm" novalidate="novalidate">
        <form:errors path="*" element="div" cssClass="alert alert-danger"/><div class="mb-3"><label class="form-label" for="email">Email tài khoản</label><form:input path="email" type="email" autocomplete="email" cssClass="form-control" cssErrorClass="form-control is-invalid" autofocus="autofocus"/><form:errors path="email" cssClass="invalid-feedback d-block"/></div>
        <button class="btn btn-success w-100" type="submit">Gửi mã OTP</button>
    </form:form><p class="text-center mt-3 mb-0"><a href="<c:url value='/login'/>">← Quay lại đăng nhập</a></p>
</div></div></div></div></body></html>
