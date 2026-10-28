<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head><title>Quản lý đơn hàng</title></head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h1 class="h3 mb-1">Đơn hàng</h1>
        <p class="text-muted mb-0">Lọc, tìm kiếm và theo dõi đơn của toàn hệ thống.</p>
    </div>
</div>

<form method="get" action="<c:url value='/admin/orders'/>" class="card card-body border-0 shadow-sm mb-3">
    <div class="row g-2 align-items-end">
        <div class="col-md-3">
            <label class="form-label" for="branch">Chi nhánh</label>
            <select class="form-select" id="branch" name="branch">
                <option value="0">Tất cả chi nhánh</option>
                <c:forEach var="b" items="${branches}">
                    <option value="${b.branch.id}" ${selectedBranch == b.branch.id ? 'selected' : ''}><c:out value="${b.branch.name}"/></option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-3">
            <label class="form-label" for="status">Trạng thái</label>
            <select class="form-select" id="status" name="status">
                <option value="">Tất cả trạng thái</option>
                <c:forEach var="s" items="${statuses}">
                    <option value="${s}" ${selectedStatus == s ? 'selected' : ''}><c:out value="${s.label}"/></option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-4">
            <label class="form-label" for="q">Mã đơn hoặc số điện thoại</label>
            <input class="form-control" id="q" name="q" value="<c:out value='${q}'/>" maxlength="100">
        </div>
        <div class="col-md-2 d-flex gap-2">
            <button class="btn btn-primary" type="submit">Lọc</button>
            <a class="btn btn-outline-secondary" href="<c:url value='/admin/orders'/>">Xóa lọc</a>
        </div>
    </div>
</form>

<c:choose>
    <c:when test="${empty orders.content}">
        <div class="alert alert-light border">Chưa có đơn hàng nào.</div>
    </c:when>
    <c:otherwise>
        <div class="card shadow-sm border-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                    <tr><th>Mã đơn</th><th>Thời gian</th><th>Khách hàng</th><th>Chi nhánh</th><th>Trạng thái</th><th class="text-end">Tổng tiền</th><th></th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="order" items="${orders.content}">
                        <tr>
                            <td class="fw-semibold"><c:out value="${order.code}"/></td>
                            <td class="text-nowrap"><c:out value="${order.createdAt.format(dateTimeFormatter)}"/></td>
                            <td><c:out value="${order.user.fullName}"/><br><span class="small text-muted"><c:out value="${order.user.email}"/></span></td>
                            <td><c:out value="${order.branch.name}"/></td>
                            <td>
                                <span class="badge ${order.status.badgeClass}"><c:out value="${order.status.label}"/></span>
                                <c:if test="${order.paymentMethod == 'VNPAY'}"><br><span class="badge mt-1 ${order.paymentStatus.badgeClass}"><c:out value="${order.paymentStatus.label}"/></span></c:if>
                            </td>
                            <td class="text-end fw-semibold"><fmt:formatNumber value="${order.totalAmount}" type="number"/> đ</td>
                            <td class="text-end"><a class="btn btn-sm btn-outline-primary" href="<c:url value='/admin/orders/${order.code}'/>">Xem</a></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <c:if test="${orders.totalPages > 1}">
            <nav class="mt-4" aria-label="Phân trang đơn hàng">
                <ul class="pagination justify-content-center">
                    <c:forEach begin="1" end="${orders.totalPages}" var="p">
                        <li class="page-item ${p == orders.number + 1 ? 'active' : ''}">
                            <a class="page-link" href="<c:url value='/admin/orders'><c:param name='branch' value='${selectedBranch}'/><c:param name='status' value='${selectedStatus}'/><c:param name='q' value='${q}'/><c:param name='page' value='${p}'/></c:url>">${p}</a>
                        </li>
                    </c:forEach>
                </ul>
            </nav>
        </c:if>
    </c:otherwise>
</c:choose>
</body>
</html>
