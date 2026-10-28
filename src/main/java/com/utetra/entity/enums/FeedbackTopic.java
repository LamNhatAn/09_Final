package com.utetra.entity.enums;

public enum FeedbackTopic {
    COMPLAINT("Than phiền"),
    PRAISE("Khen ngợi"),
    SUGGESTION("Đề xuất"),
    WEBSITE("Trải nghiệm website"),
    DRINK("Chất lượng thức uống"),
    SERVICE("Chất lượng phục vụ"),
    OTHER("Vấn đề khác");

    private final String label;

    FeedbackTopic(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
