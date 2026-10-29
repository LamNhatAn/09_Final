<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><sitemesh:write property='title'/> | UTETra Admin</title>
    <link rel="stylesheet" href="<c:url value='/webjars/bootstrap/5.3.3/css/bootstrap.min.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/app.css'/>">
    <sitemesh:write property='head'/>
</head>
<body class="bg-light">

<div class="d-flex">
    <aside class="utetra-sidebar p-3 text-white">
        <a class="d-block fs-5 fw-bold text-white text-decoration-none mb-4" href="<c:url value='/admin/dashboard'/>">
            🧋 UTETra <span class="badge bg-warning text-dark">Admin</span>
        </a>
        <ul class="nav flex-column gap-1">
            <li class="nav-item"><a class="nav-link text-white" href="<c:url value='/admin/dashboard'/>">Tổng quan</a></li>
            <li class="nav-item"><a class="nav-link text-white" href="<c:url value='/admin/products'/>">Sản phẩm</a></li>
            <li class="nav-item"><a class="nav-link text-white" href="<c:url value='/admin/categories'/>">Danh mục</a></li>
            <li class="nav-item"><a class="nav-link text-white" href="<c:url value='/admin/toppings'/>">Topping</a></li>
            <li class="nav-item"><a class="nav-link text-white" href="<c:url value='/admin/orders'/>">Đơn hàng</a></li>
            <li class="nav-item"><a class="nav-link text-white" href="<c:url value='/admin/feedback'/>">Ý kiến khách hàng</a></li>
            <li class="nav-item"><a class="nav-link text-white" href="<c:url value='/admin/branches'/>">Chi nhánh</a></li>
            <li class="nav-item"><a class="nav-link text-white" href="<c:url value='/admin/users'/>">Người dùng / Nhân viên</a></li>
        </ul>
        <hr>
        <a class="nav-link text-white-50 px-0" href="<c:url value='/'/>">← Về trang web</a>
        <div class="small text-white-50 mt-3 mb-2">👤 <c:out value="${currentUser.fullName}"/></div>
        <form method="post" action="<c:url value='/logout'/>" class="m-0">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button type="submit" class="btn btn-outline-light btn-sm w-100">Đăng xuất</button>
        </form>
    </aside>

    <div class="flex-grow-1 p-4">
        <div class="d-flex justify-content-end mb-2">
            <%@ include file="/WEB-INF/views/fragments/notification-center.jspf" %>
        </div>
        <sitemesh:write property='body'/>
    </div>
</div>

<script src="<c:url value='/webjars/bootstrap/5.3.3/js/bootstrap.bundle.min.js'/>"></script>
<script src="<c:url value='/js/notifications.js'/>"></script>
</body>
</html>
