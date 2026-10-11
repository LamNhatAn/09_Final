<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Không tìm thấy trang</title>
</head>
<body>
<div class="text-center py-5">
    <div class="display-1 fw-bold text-warning">404</div>
    <h1 class="h4 mb-3">Không tìm thấy nội dung bạn cần</h1>
    <p class="text-muted">Đường dẫn không đúng hoặc sản phẩm đã ngừng bán.</p>
    <a class="btn btn-utetra me-2" href="<c:url value='/menu'/>">Xem thực đơn</a>
    <a class="btn btn-outline-secondary" href="<c:url value='/'/>">Về trang chủ</a>
</div>
</body>
</html>
