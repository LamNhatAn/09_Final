<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="isEdit" value="${not empty product}"/>
<html>
<head>
    <title><c:out value="${isEdit ? 'Sửa sản phẩm' : 'Thêm sản phẩm'}"/></title>
</head>
<body>
<nav aria-label="breadcrumb">
    <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="<c:url value='/admin/products'/>">Sản phẩm</a></li>
        <li class="breadcrumb-item active" aria-current="page"><c:out value="${isEdit ? 'Sửa' : 'Thêm mới'}"/></li>
    </ol>
</nav>
<h1 class="h3 mb-3"><c:out value="${isEdit ? 'Sửa sản phẩm' : 'Thêm sản phẩm'}"/></h1>

<c:if test="${not imageConfigured}">
    <div class="alert alert-warning">
        Chưa cấu hình Cloudinary nên chưa thể tải ảnh lên. Vẫn lưu được sản phẩm không có ảnh.
        Xem README, mục "Cloudinary".
    </div>
</c:if>

<%-- Token CSRF cung dat tren URL: neu anh vuot gioi han server thi Tomcat bo dang phan tich body va khong doc duoc
     truong an _csrf, khien nguoi dung thay trang 403 thay vi thong bao "anh qua lon" --%>
<c:choose>
    <c:when test="${isEdit}"><c:url var="formAction" value="/admin/products/${product.id}/edit"><c:param name="_csrf" value="${_csrf.token}"/></c:url></c:when>
    <c:otherwise><c:url var="formAction" value="/admin/products/new"><c:param name="_csrf" value="${_csrf.token}"/></c:url></c:otherwise>
</c:choose>
<form:form method="post" action="${formAction}" modelAttribute="productForm" enctype="multipart/form-data"
           novalidate="novalidate" cssClass="bg-white rounded-3 shadow-sm p-4">
    <div class="row g-3">
        <div class="col-12 col-md-6">
            <label class="form-label" for="name">Tên sản phẩm</label>
            <form:input path="name" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="150"/>
            <form:errors path="name" cssClass="invalid-feedback d-block"/>
        </div>
        <div class="col-12 col-md-6">
            <label class="form-label" for="categoryId">Danh mục</label>
            <form:select path="categoryId" cssClass="form-select" cssErrorClass="form-select is-invalid">
                <form:option value="" label="-- Chọn danh mục --"/>
                <c:forEach var="cat" items="${categories}">
                    <form:option value="${cat.id}"><c:out value="${cat.name}"/><c:if test="${not cat.active}"> (đang ẩn)</c:if></form:option>
                </c:forEach>
            </form:select>
            <form:errors path="categoryId" cssClass="invalid-feedback d-block"/>
        </div>
        <div class="col-12 col-md-6">
            <label class="form-label" for="basePrice">Giá gốc (VNĐ, size S)</label>
            <form:input path="basePrice" type="number" min="1000" step="500" cssClass="form-control" cssErrorClass="form-control is-invalid"/>
            <form:errors path="basePrice" cssClass="invalid-feedback d-block"/>
        </div>
        <div class="col-12 col-md-6 d-flex align-items-end">
            <div class="form-check form-switch">
                <form:checkbox path="available" cssClass="form-check-input" id="available"/>
                <label class="form-check-label" for="available">Đang bán</label>
            </div>
        </div>
        <div class="col-12">
            <label class="form-label" for="description">Mô tả</label>
            <form:textarea path="description" rows="3" cssClass="form-control" cssErrorClass="form-control is-invalid" maxlength="1000"/>
            <form:errors path="description" cssClass="invalid-feedback d-block"/>
        </div>

        <div class="col-12">
            <label class="form-label" for="image">Ảnh sản phẩm (JPG, PNG, WEBP, tối đa 2 MB)</label>
            <div class="d-flex align-items-start gap-3 flex-wrap">
                <div>
                    <c:choose>
                        <c:when test="${isEdit and not empty product.imageUrl}">
                            <img id="preview" src="<c:out value='${product.imageUrl}'/>" alt="Ảnh hiện tại"
                                 class="rounded border" style="width:160px;height:160px;object-fit:cover">
                        </c:when>
                        <c:otherwise>
                            <img id="preview" src="" alt="Xem trước" class="rounded border d-none"
                                 style="width:160px;height:160px;object-fit:cover">
                            <div id="noImage" class="rounded border d-flex align-items-center justify-content-center fs-1 bg-light"
                                 style="width:160px;height:160px">🧋</div>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="flex-grow-1">
                    <input type="file" name="image" id="image" accept="image/jpeg,image/png,image/webp"
                           class="form-control" ${imageConfigured ? '' : 'disabled'}>
                    <form:errors path="image" cssClass="text-danger small d-block"/>
                    <div id="sizeError" class="text-danger small d-none">Ảnh lớn hơn 2 MB, vui lòng chọn ảnh khác.</div>
                    <c:if test="${isEdit and not empty product.imageUrl}">
                        <div class="form-check mt-2">
                            <form:checkbox path="removeImage" cssClass="form-check-input" id="removeImage"/>
                            <label class="form-check-label" for="removeImage">Xóa ảnh hiện tại (nếu không chọn ảnh mới)</label>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <div class="mt-4 d-flex gap-2">
        <button type="submit" class="btn btn-utetra">Lưu</button>
        <a class="btn btn-outline-secondary" href="<c:url value='/admin/products'/>">Hủy</a>
    </div>
</form:form>

<script>
    // Xem truoc anh va bao som neu anh > 2MB (server van kiem tra lai, day chi de tien cho nguoi dung)
    (function () {
        var input = document.getElementById('image');
        if (!input) return;
        input.addEventListener('change', function () {
            var file = input.files[0];
            var tooBig = file && file.size > 2 * 1024 * 1024;
            document.getElementById('sizeError').classList.toggle('d-none', !tooBig);
            if (!file || tooBig) { if (tooBig) input.value = ''; return; }
            var img = document.getElementById('preview');
            img.src = URL.createObjectURL(file);
            img.classList.remove('d-none');
            var none = document.getElementById('noImage');
            if (none) none.classList.add('d-none');
        });
    })();
</script>
</body>
</html>
