<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><sitemesh:write property='title'/> | UTETra</title>
    <link rel="stylesheet" href="<c:url value='/webjars/bootstrap/5.3.3/css/bootstrap.min.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/app.css'/>">
    <sitemesh:write property='head'/>
</head>
<body class="d-flex flex-column min-vh-100">

<nav class="navbar navbar-expand-lg navbar-dark utetra-navbar">
    <div class="container">
        <a class="navbar-brand fw-bold" href="<c:url value='/'/>">🧋 UTETra</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav"
                aria-controls="mainNav" aria-expanded="false" aria-label="Menu">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNav">
            <ul class="navbar-nav me-auto">
                <li class="nav-item"><a class="nav-link" href="<c:url value='/'/>">Trang chủ</a></li>
                <li class="nav-item"><a class="nav-link disabled" href="#">Thực đơn</a></li>
                <li class="nav-item"><a class="nav-link disabled" href="#">Chi nhánh</a></li>
            </ul>
            <ul class="navbar-nav">
                <li class="nav-item"><a class="nav-link disabled" href="#">Giỏ hàng</a></li>
                <li class="nav-item"><a class="nav-link disabled" href="#">Đăng nhập</a></li>
            </ul>
        </div>
    </div>
</nav>

<main class="container py-4 flex-grow-1">
    <sitemesh:write property='body'/>
</main>

<footer class="utetra-footer text-center py-3">
    <small>&copy; 2026 UTETra - Chuỗi trà sữa UTETra</small>
</footer>

<script src="<c:url value='/webjars/bootstrap/5.3.3/js/bootstrap.bundle.min.js'/>"></script>
</body>
</html>
