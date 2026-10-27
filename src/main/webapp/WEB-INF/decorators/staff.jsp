<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><sitemesh:write property='title'/> | UTETra Nhân viên</title>
    <link rel="stylesheet" href="<c:url value='/webjars/bootstrap/5.3.3/css/bootstrap.min.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/app.css'/>">
    <sitemesh:write property='head'/>
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark utetra-navbar-staff">
    <div class="container-fluid">
        <a class="navbar-brand fw-bold" href="<c:url value='/staff/dashboard'/>">
            🧋 UTETra <span class="badge bg-light text-dark">Nhân viên</span>
        </a>
        <ul class="navbar-nav flex-row gap-3 me-auto ms-3">
            <li class="nav-item"><a class="nav-link" href="<c:url value='/staff/orders'/>">Đơn hàng</a></li>
            <li class="nav-item"><a class="nav-link disabled" href="#">Chat với khách</a></li>
        </ul>
        <a class="nav-link text-white-50" href="<c:url value='/'/>">Về trang web</a>
        <span class="navbar-text text-white mx-3">👤 <c:out value="${currentUser.fullName}"/></span>
        <form method="post" action="<c:url value='/logout'/>" class="m-0">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button type="submit" class="btn btn-outline-light btn-sm">Đăng xuất</button>
        </form>
    </div>
</nav>

<main class="container-fluid py-4">
    <sitemesh:write property='body'/>
</main>

<script src="<c:url value='/webjars/bootstrap/5.3.3/js/bootstrap.bundle.min.js'/>"></script>
</body>
</html>
