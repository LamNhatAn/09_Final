<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<head>
    <title>Đăng nhập</title>
</head>
<body>
<div class="row justify-content-center">
    <div class="col-md-6 col-lg-5">
        <div class="card shadow-sm">
            <div class="card-body p-4">
                <h1 class="h4 mb-3 text-center">Đăng nhập</h1>

                <c:if test="${param.registered != null}">
                    <div class="alert alert-success">Đăng ký thành công! Hãy đăng nhập.</div>
                </c:if>
                <c:if test="${param.logout != null}">
                    <div class="alert alert-info">Bạn đã đăng xuất.</div>
                </c:if>
                <c:if test="${param['password-reset'] != null}">
                    <div class="alert alert-success">Đổi mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới.</div>
                </c:if>
                <spring:hasBindErrors name="loginForm">
                    <c:forEach items="${errors.globalErrors}" var="err">
                        <div class="alert alert-danger"><c:out value="${err.defaultMessage}"/></div>
                    </c:forEach>
                </spring:hasBindErrors>

                <form:form method="post" action="${pageContext.request.contextPath}/login" modelAttribute="loginForm" novalidate="novalidate">
                    <form:hidden path="next"/>
                    <div class="mb-3">
                        <label class="form-label" for="email">Email</label>
                        <form:input path="email" type="email" cssClass="form-control" cssErrorClass="form-control is-invalid" autocomplete="username"/>
                        <form:errors path="email" cssClass="invalid-feedback d-block"/>
                    </div>
                    <div class="mb-3">
                        <label class="form-label" for="password">Mật khẩu</label>
                        <form:password path="password" cssClass="form-control" cssErrorClass="form-control is-invalid" autocomplete="current-password"/>
                        <form:errors path="password" cssClass="invalid-feedback d-block"/>
                        <div class="text-end mt-1"><a class="small" href="<c:url value='/forgot-password'/>">Quên mật khẩu?</a></div>
                    </div>
                    <button type="submit" class="btn btn-success w-100">Đăng nhập</button>
                </form:form>

                <p class="text-center mt-3 mb-0">
                    Chưa có tài khoản? <a href="<c:url value='/register'/>">Đăng ký</a>
                </p>
            </div>
        </div>
    </div>
</div>
</body>
</html>
