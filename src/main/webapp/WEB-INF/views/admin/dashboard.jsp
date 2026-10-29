<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head><title>Tổng quan</title></head>
<body>
<div class="d-flex justify-content-between align-items-start mb-4">
    <div><h1 class="h3 mb-1">Tổng quan quản trị</h1><p class="text-muted mb-0">Theo dõi nhanh hoạt động của toàn hệ thống UTETra.</p></div>
    <a class="btn btn-utetra" href="<c:url value='/admin/orders'/>">Quản lý đơn hàng</a>
</div>
<div class="row g-3 mb-4">
    <div class="col-sm-6 col-xl-3"><div class="card border-0 shadow-sm h-100 stat-card"><div class="card-body"><div class="text-muted small">Doanh thu hôm nay</div><div class="display-6 fs-3"><fmt:formatNumber value="${stats.revenueToday}" type="number"/> đ</div><div class="small text-muted">${stats.completedToday} đơn hoàn thành</div></div></div></div>
    <div class="col-sm-6 col-xl-3"><div class="card border-0 shadow-sm h-100 stat-card"><div class="card-body"><div class="text-muted small">Đơn chờ xác nhận</div><div class="display-6">${stats.pendingOrders}</div><a class="small" href="<c:url value='/admin/orders'><c:param name='status' value='PENDING'/></c:url>">Xem đơn đang chờ</a></div></div></div>
    <div class="col-sm-6 col-xl-3"><div class="card border-0 shadow-sm h-100 stat-card"><div class="card-body"><div class="text-muted small">Đơn đang xử lý</div><div class="display-6">${stats.processingOrders}</div><div class="small text-muted">Đã nhận, pha chế hoặc đang giao</div></div></div></div>
    <div class="col-sm-6 col-xl-3"><div class="card border-0 shadow-sm h-100 stat-card"><div class="card-body"><div class="text-muted small">Tổng doanh thu</div><div class="display-6 fs-3"><fmt:formatNumber value="${stats.totalRevenue}" type="number"/> đ</div><div class="small text-muted">Từ các đơn đã hoàn thành</div></div></div></div>
</div>
<div class="row g-3 mb-4">
    <div class="col-6 col-lg"><a class="card border-0 shadow-sm text-decoration-none h-100" href="<c:url value='/admin/users'><c:param name='role' value='USER'/></c:url>"><div class="card-body"><div class="text-muted small">Khách hàng</div><div class="h3 text-dark mb-0">${stats.customers}</div></div></a></div>
    <div class="col-6 col-lg"><a class="card border-0 shadow-sm text-decoration-none h-100" href="<c:url value='/admin/users'><c:param name='role' value='STAFF'/></c:url>"><div class="card-body"><div class="text-muted small">Nhân viên hoạt động</div><div class="h3 text-dark mb-0">${stats.activeStaff}</div></div></a></div>
    <div class="col-6 col-lg"><a class="card border-0 shadow-sm text-decoration-none h-100" href="<c:url value='/admin/branches'/>"><div class="card-body"><div class="text-muted small">Chi nhánh hoạt động</div><div class="h3 text-dark mb-0">${stats.activeBranches}</div></div></a></div>
    <div class="col-6 col-lg"><a class="card border-0 shadow-sm text-decoration-none h-100" href="<c:url value='/admin/products'/>"><div class="card-body"><div class="text-muted small">Sản phẩm đang bán</div><div class="h3 text-dark mb-0">${stats.availableProducts}</div></div></a></div>
    <div class="col-6 col-lg"><a class="card border-0 shadow-sm text-decoration-none h-100" href="<c:url value='/admin/feedback'/>"><div class="card-body"><div class="text-muted small">Ý kiến khách hàng</div><div class="h3 text-dark mb-0">${stats.feedbacks}</div></div></a></div>
</div>
<div class="card border-0 shadow-sm">
    <div class="card-header bg-white d-flex justify-content-between align-items-center py-3"><h2 class="h5 mb-0">Đơn hàng mới nhất</h2><a href="<c:url value='/admin/orders'/>">Xem tất cả</a></div>
    <c:choose><c:when test="${empty recentOrders}"><div class="card-body text-muted">Chưa có đơn hàng nào.</div></c:when><c:otherwise>
        <div class="table-responsive"><table class="table table-hover align-middle mb-0">
            <thead class="table-light"><tr><th>Mã đơn</th><th>Khách hàng</th><th>Chi nhánh</th><th>Trạng thái</th><th class="text-end">Tổng tiền</th><th></th></tr></thead>
            <tbody><c:forEach var="order" items="${recentOrders}"><tr>
                <td><c:out value="${order.code}"/><br><span class="small text-muted"><c:out value="${order.createdAt.format(dateTimeFormatter)}"/></span></td>
                <td><c:out value="${order.user.fullName}"/></td><td><c:out value="${order.branch.name}"/></td>
                <td><span class="badge ${order.status.badgeClass}"><c:out value="${order.status.label}"/></span></td>
                <td class="text-end fw-semibold"><fmt:formatNumber value="${order.totalAmount}" type="number"/> đ</td>
                <td class="text-end"><a class="btn btn-sm btn-outline-primary" href="<c:url value='/admin/orders/${order.code}'/>">Xem</a></td>
            </tr></c:forEach></tbody>
        </table></div>
    </c:otherwise></c:choose>
</div>
</body>
</html>
