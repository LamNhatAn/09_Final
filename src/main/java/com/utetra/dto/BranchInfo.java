package com.utetra.dto;

import com.utetra.entity.Branch;

/** Chi nhanh kem trang thai "dang mo cua" de JSP hien thi. */
public record BranchInfo(Branch branch, boolean openNow) {

    public Branch getBranch() {
        return branch;
    }

    public boolean isOpenNow() {
        return openNow;
    }
}
