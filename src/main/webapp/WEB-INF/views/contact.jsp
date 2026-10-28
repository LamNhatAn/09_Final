<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html>
<head><title>Liên hệ và góp ý</title></head>
<body>
<section class="contact-hero rounded-4 p-4 p-md-5 mb-4">
    <div class="row align-items-center g-4">
        <div class="col-lg-7">
            <span class="text-uppercase fw-semibold contact-kicker">UTETra lắng nghe bạn</span>
            <h1 class="display-6 fw-bold mt-2 mb-3">Liên hệ và góp ý</h1>
            <p class="lead mb-0">Mỗi phản hồi giúp UTETra hoàn thiện website, thức uống và chất lượng phục vụ tốt hơn.</p>
        </div>
        <div class="col-lg-5">
            <div class="contact-info-card rounded-4 p-4">
                <div class="mb-3">
                    <div class="small text-uppercase text-muted fw-semibold">Điện thoại</div>
                    <a class="fs-5 fw-semibold text-decoration-none" href="tel:0944884068">0944 884 068</a>
                </div>
                <div>
                    <div class="small text-uppercase text-muted fw-semibold">Email</div>
                    <a class="fw-semibold text-decoration-none text-break" href="mailto:2416200@student.hmcute.edu.vn">2416200@student.hmcute.edu.vn</a>
                </div>
            </div>
        </div>
    </div>
</section>

<c:if test="${sent}">
    <div class="alert alert-success" role="alert">
        <strong>Cảm ơn bạn!</strong> UTETra đã nhận được ý kiến và sẽ xem xét phản hồi sớm nhất.
    </div>
</c:if>

<div class="row g-4">
    <div class="col-lg-8">
        <div class="card border-0 shadow-sm">
            <div class="card-body p-4 p-md-5">
                <h2 class="h4 mb-2">Chia sẻ trải nghiệm của bạn</h2>
                <p class="text-muted mb-4">Vui lòng cung cấp thông tin chính xác để UTETra có thể liên hệ khi cần làm rõ phản hồi.</p>

                <form:form method="post" action="${pageContext.request.contextPath}/contact"
                           modelAttribute="feedbackForm" novalidate="novalidate">
                    <form:errors path="*" cssClass="alert alert-danger d-block" element="div"/>

                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label" for="fullName">Họ và tên <span class="text-danger">*</span></label>
                            <form:input path="fullName" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="100"/>
                            <form:errors path="fullName" cssClass="invalid-feedback d-block"/>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label" for="email">Email <span class="text-danger">*</span></label>
                            <form:input path="email" type="email" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="150"/>
                            <form:errors path="email" cssClass="invalid-feedback d-block"/>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label" for="phone">Số điện thoại</label>
                            <form:input path="phone" type="tel" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="20" placeholder="Ví dụ: 0901234567"/>
                            <form:errors path="phone" cssClass="invalid-feedback d-block"/>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label" for="branchId">Chi nhánh liên quan</label>
                            <form:select path="branchId" cssClass="form-select" cssErrorClass="form-select is-invalid">
                                <form:option value="" label="Website / Không thuộc chi nhánh"/>
                                <c:forEach var="b" items="${branches}">
                                    <form:option value="${b.branch.id}" label="${b.branch.name}"/>
                                </c:forEach>
                            </form:select>
                            <form:errors path="branchId" cssClass="invalid-feedback d-block"/>
                        </div>
                    </div>

                    <fieldset class="mt-4">
                        <legend class="form-label fw-semibold">Vấn đề phản hồi <span class="text-danger">*</span></legend>
                        <div class="d-flex flex-wrap gap-2">
                            <c:forEach var="topic" items="${topics}" varStatus="loop">
                                <div>
                                    <form:radiobutton path="topic" value="${topic}" id="topic-${loop.index}" cssClass="btn-check"/>
                                    <label class="btn btn-outline-secondary rounded-pill" for="topic-${loop.index}"><c:out value="${topic.label}"/></label>
                                </div>
                            </c:forEach>
                        </div>
                        <form:errors path="topic" cssClass="text-danger small d-block mt-2"/>
                    </fieldset>

                    <fieldset class="mt-4">
                        <legend class="form-label fw-semibold">Đánh giá chung <span class="text-danger">*</span></legend>
                        <div class="rating-options d-flex flex-wrap gap-2">
                            <c:forEach begin="1" end="5" var="star">
                                <form:radiobutton path="rating" value="${star}" id="rating-${star}" cssClass="btn-check"/>
                                <label class="btn btn-outline-warning" for="rating-${star}">${star} ★</label>
                            </c:forEach>
                        </div>
                        <form:errors path="rating" cssClass="text-danger small d-block mt-2"/>
                    </fieldset>

                    <div class="mt-4">
                        <label class="form-label fw-semibold" for="content">Nội dung phản hồi <span class="text-danger">*</span></label>
                        <form:textarea path="content" rows="6" cssClass="form-control" cssErrorClass="form-control is-invalid"
                                       maxlength="2000" placeholder="Hãy cho UTETra biết trải nghiệm của bạn..."/>
                        <form:errors path="content" cssClass="invalid-feedback d-block"/>
                    </div>

                    <div class="form-check mt-4">
                        <form:checkbox path="privacyConsent" cssClass="form-check-input" cssErrorClass="form-check-input is-invalid"/>
                        <label class="form-check-label" for="privacyConsent">
                            Tôi xác nhận thông tin trên là chính xác và đồng ý để UTETra sử dụng thông tin này nhằm xử lý phản hồi.
                        </label>
                        <form:errors path="privacyConsent" cssClass="text-danger small d-block"/>
                    </div>

                    <button class="btn btn-utetra px-4 mt-4" type="submit">Gửi ý kiến</button>
                </form:form>
            </div>
        </div>
    </div>

    <aside class="col-lg-4">
        <div class="card border-0 shadow-sm">
            <div class="card-body p-4">
                <h2 class="h5">Bảo mật thông tin</h2>
                <p class="small text-muted">UTETra chỉ sử dụng thông tin bạn cung cấp để tiếp nhận, xác minh và giải quyết phản hồi.</p>
                <ul class="small text-muted ps-3 mb-0">
                    <li class="mb-2">Không công khai nội dung và thông tin liên hệ của khách hàng.</li>
                    <li class="mb-2">Chỉ quản trị viên được xem các ý kiến đã gửi.</li>
                    <li>Phản hồi gửi vào cuối tuần hoặc ngày lễ có thể được xử lý chậm hơn.</li>
                </ul>
            </div>
        </div>
    </aside>
</div>
</body>
</html>
