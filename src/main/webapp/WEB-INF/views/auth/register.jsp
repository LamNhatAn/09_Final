<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<head>
    <title>Đăng ký</title>
</head>
<body>
<div class="row justify-content-center">
    <div class="col-md-8 col-lg-6">
        <div class="card shadow-sm">
            <div class="card-body p-4">
                <h1 class="h4 mb-3 text-center">Tạo tài khoản</h1>
                <p class="text-muted text-center">Bạn sẽ cần nhập mã OTP được gửi tới email để hoàn tất đăng ký.</p>

                <spring:hasBindErrors name="registerForm">
                    <c:forEach items="${errors.globalErrors}" var="err">
                        <div class="alert alert-danger"><c:out value="${err.defaultMessage}"/></div>
                    </c:forEach>
                </spring:hasBindErrors>

                <form:form method="post" action="${pageContext.request.contextPath}/register" modelAttribute="registerForm" novalidate="novalidate">
                    <div class="mb-3">
                        <label class="form-label" for="fullName">Họ và tên</label>
                        <form:input path="fullName" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="100" autocomplete="name"/>
                        <form:errors path="fullName" cssClass="invalid-feedback d-block"/>
                    </div>
                    <div class="mb-3">
                        <label class="form-label" for="email">Email</label>
                        <form:input path="email" type="email" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="150" autocomplete="email"/>
                        <form:errors path="email" cssClass="invalid-feedback d-block"/>
                    </div>
                    <div class="mb-3">
                        <label class="form-label" for="phone">Số điện thoại <span class="text-muted">(không bắt buộc)</span></label>
                        <form:input path="phone" type="tel" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="20" autocomplete="tel"/>
                        <form:errors path="phone" cssClass="invalid-feedback d-block"/>
                    </div>
                    <div class="mb-3">
                        <label class="form-label" for="password">Mật khẩu</label>
                        <form:password path="password" cssClass="form-control" cssErrorClass="form-control is-invalid" autocomplete="new-password"/>
                        <div class="form-text">Ít nhất 8 ký tự, gồm cả chữ và số.</div>
                        <form:errors path="password" cssClass="invalid-feedback d-block"/>
                    </div>
                    <div class="mb-3">
                        <label class="form-label" for="confirmPassword">Nhập lại mật khẩu</label>
                        <form:password path="confirmPassword" cssClass="form-control" cssErrorClass="form-control is-invalid" autocomplete="new-password"/>
                        <form:errors path="confirmPassword" cssClass="invalid-feedback d-block"/>
                    </div>
                    <button type="submit" class="btn btn-success w-100">Gửi mã OTP</button>
                </form:form>

                <p class="text-center mt-3 mb-0">
                    Đã có tài khoản? <a href="<c:url value='/login'/>">Đăng nhập</a>
                </p>
            </div>
        </div>
    </div>
</div>
</body>
</html>
