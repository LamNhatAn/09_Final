<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head>
    <title>Trang chủ</title>
</head>
<body>
<div class="p-5 mb-4 bg-white rounded-3 shadow-sm">
    <h1 class="display-5 fw-bold">🧋 Chào mừng đến với UTETra</h1>
    <p class="lead mb-0">Khung dự án đã chạy: JSP + SiteMesh + Bootstrap + Spring Data JPA + SQL Server.</p>
</div>

<h2 class="h5 mb-3">Kiểm tra kết nối cơ sở dữ liệu</h2>
<div class="row g-3 mb-4">
    <div class="col-6 col-md-3">
        <div class="card stat-card text-center shadow-sm"><div class="card-body">
            <div class="display-6"><c:out value="${branchCount}"/></div><div>Chi nhánh</div>
        </div></div>
    </div>
    <div class="col-6 col-md-3">
        <div class="card stat-card text-center shadow-sm"><div class="card-body">
            <div class="display-6"><c:out value="${categoryCount}"/></div><div>Danh mục</div>
        </div></div>
    </div>
    <div class="col-6 col-md-3">
        <div class="card stat-card text-center shadow-sm"><div class="card-body">
            <div class="display-6"><c:out value="${productCount}"/></div><div>Sản phẩm</div>
        </div></div>
    </div>
    <div class="col-6 col-md-3">
        <div class="card stat-card text-center shadow-sm"><div class="card-body">
            <div class="display-6"><c:out value="${userCount}"/></div><div>Người dùng</div>
        </div></div>
    </div>
</div>

<h2 class="h5 mb-3">Thử 3 layout (SiteMesh)</h2>
<a class="btn btn-utetra me-2" href="<c:url value='/'/>">Layout khách (main)</a>
<a class="btn btn-success me-2" href="<c:url value='/staff/dashboard'/>">Layout nhân viên (staff)</a>
<a class="btn btn-dark me-2" href="<c:url value='/admin/dashboard'/>">Layout quản trị (admin)</a>
<a class="btn btn-outline-secondary" href="<c:url value='/health'/>">/health (JSON)</a>
</body>
</html>
