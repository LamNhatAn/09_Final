<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head><title>Ý kiến khách hàng</title></head>
<body>
<div class="mb-4">
    <h1 class="h3 mb-1">Ý kiến khách hàng</h1>
    <p class="text-muted mb-0">Phản hồi về website, thức uống và chất lượng phục vụ.</p>
</div>

<c:choose>
    <c:when test="${empty feedbacks.content}">
        <div class="card border-0 shadow-sm"><div class="card-body text-center text-muted py-5">Chưa có ý kiến nào được gửi.</div></div>
    </c:when>
    <c:otherwise>
        <div class="d-grid gap-3">
            <c:forEach var="item" items="${feedbacks.content}">
                <article class="card border-0 shadow-sm">
                    <div class="card-body">
                        <div class="d-flex flex-wrap justify-content-between gap-2 mb-2">
                            <div>
                                <strong><c:out value="${item.fullName}"/></strong>
                                <span class="badge text-bg-light ms-1"><c:out value="${item.topic.label}"/></span>
                            </div>
                            <time class="small text-muted"><c:out value="${item.createdAt.format(dateTimeFormatter)}"/></time>
                        </div>
                        <div class="text-warning mb-2" aria-label="${item.rating} trên 5 sao">
                            <c:forEach begin="1" end="${item.rating}">★</c:forEach><c:forEach begin="${item.rating + 1}" end="5">☆</c:forEach>
                        </div>
                        <p class="mb-3 feedback-content"><c:out value="${item.content}"/></p>
                        <div class="small text-muted d-flex flex-wrap gap-3">
                            <a href="mailto:<c:out value='${item.email}'/>"><c:out value="${item.email}"/></a>
                            <c:if test="${not empty item.phone}"><a href="tel:<c:out value='${item.phone}'/>"><c:out value="${item.phone}"/></a></c:if>
                            <span><c:out value="${empty item.branch ? 'Website / Chung' : item.branch.name}"/></span>
                        </div>
                    </div>
                </article>
            </c:forEach>
        </div>

        <c:if test="${feedbacks.totalPages > 1}">
            <nav class="mt-4" aria-label="Phân trang ý kiến">
                <ul class="pagination justify-content-center">
                    <c:forEach begin="1" end="${feedbacks.totalPages}" var="p">
                        <li class="page-item ${p == feedbacks.number + 1 ? 'active' : ''}">
                            <a class="page-link" href="<c:url value='/admin/feedback'><c:param name='page' value='${p}'/></c:url>">${p}</a>
                        </li>
                    </c:forEach>
                </ul>
            </nav>
        </c:if>
    </c:otherwise>
</c:choose>
</body>
</html>
