<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html>
<head><title>Xác minh email</title></head>
<body>
<div class="row justify-content-center">
    <div class="col-md-7 col-lg-5">
        <div class="card shadow-sm border-0">
            <div class="card-body p-4 p-lg-5">
                <div class="text-center mb-4">
                    <div class="otp-mail-icon" aria-hidden="true">✉</div>
                    <h1 class="h4 mt-3 mb-2">Xác minh email</h1>
                    <p class="text-muted mb-0">Nhập mã gồm 6 chữ số đã gửi tới <strong><c:out value="${maskedEmail}"/></strong>.</p>
                    <div class="small text-muted mt-1">Mã có hiệu lực trong 10 phút.</div>
                </div>

                <c:if test="${not empty sentMessage}"><div class="alert alert-success"><c:out value="${sentMessage}"/></div></c:if>
                <c:if test="${not empty resendError}"><div class="alert alert-danger"><c:out value="${resendError}"/></div></c:if>

                <form:form method="post" action="${pageContext.request.contextPath}/register/verify"
                           modelAttribute="otpForm" novalidate="novalidate">
                    <form:hidden path="email"/>
                    <div class="mb-3">
                        <label class="form-label" for="code">Mã OTP</label>
                        <form:input path="code" inputmode="numeric" autocomplete="one-time-code" maxlength="6"
                                    cssClass="form-control form-control-lg text-center otp-code-input"
                                    cssErrorClass="form-control form-control-lg text-center otp-code-input is-invalid"
                                    placeholder="000000" autofocus="autofocus"/>
                        <form:errors path="code" cssClass="invalid-feedback d-block"/>
                    </div>
                    <button type="submit" class="btn btn-success w-100">Xác minh và tạo tài khoản</button>
                </form:form>

                <form method="post" action="<c:url value='/register/resend'/>" class="mt-3 text-center">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <input type="hidden" name="email" value="<c:out value='${otpForm.email}'/>">
                    <span class="text-muted small">Chưa nhận được mã?</span>
                    <button type="submit" class="btn btn-link btn-sm px-1">Gửi lại OTP</button>
                </form>

                <p class="text-center mt-3 mb-0"><a href="<c:url value='/register'/>">← Thay đổi thông tin đăng ký</a></p>
            </div>
        </div>
    </div>
</div>
</body>
</html>
