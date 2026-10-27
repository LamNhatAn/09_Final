<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
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
                <li class="nav-item"><a class="nav-link" href="<c:url value='/menu'/>">Thực đơn</a></li>
                <li class="nav-item"><a class="nav-link" href="<c:url value='/branches'/>">Chi nhánh</a></li>
            </ul>
            <ul class="navbar-nav align-items-lg-center">
                <c:if test="${empty currentUser or currentUser.customer}">
                    <li class="nav-item"><a class="nav-link" href="<c:url value='/cart'/>">🛒 Giỏ hàng</a></li>
                </c:if>
                <c:if test="${not empty currentUser and currentUser.customer}">
                    <li class="nav-item"><a class="nav-link" href="<c:url value='/orders'/>">🧾 Đơn hàng</a></li>
                </c:if>
                <c:choose>
                    <c:when test="${not empty currentUser}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                👤 <c:out value="${currentUser.fullName}"/>
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <c:if test="${currentUser.admin}">
                                    <li><a class="dropdown-item" href="<c:url value='/admin/dashboard'/>">Trang quản trị</a></li>
                                </c:if>
                                <c:if test="${currentUser.staff}">
                                    <li><a class="dropdown-item" href="<c:url value='/staff/dashboard'/>">Trang nhân viên</a></li>
                                </c:if>
                                <li>
                                    <form method="post" action="<c:url value='/logout'/>" class="m-0">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <button type="submit" class="dropdown-item">Đăng xuất</button>
                                    </form>
                                </li>
                            </ul>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item"><a class="nav-link" href="<c:url value='/login'/>">Đăng nhập</a></li>
                        <li class="nav-item ms-lg-2"><a class="btn btn-warning btn-sm" href="<c:url value='/register'/>">Đăng ký</a></li>
                    </c:otherwise>
                </c:choose>
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
