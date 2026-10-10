<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Không có quyền truy cập</title>
</head>
<body>
<div class="text-center py-5">
    <div class="display-1 fw-bold text-danger">403</div>
    <h1 class="h4 mb-3">Bạn không có quyền truy cập trang này</h1>
    <p class="text-muted">Tài khoản hiện tại không được phép xem nội dung này.</p>
    <a class="btn btn-success" href="<c:url value='/'/>">Về trang chủ</a>
</div>
</body>
</html>
