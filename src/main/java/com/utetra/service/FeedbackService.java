package com.utetra.service;

import com.utetra.dto.FeedbackForm;
import com.utetra.entity.Branch;
import com.utetra.entity.Feedback;
import com.utetra.repository.BranchRepository;
import com.utetra.repository.FeedbackRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    public static final int PAGE_SIZE = 15;

    private final FeedbackRepository feedbackRepository;
    private final BranchRepository branchRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, BranchRepository branchRepository) {
        this.feedbackRepository = feedbackRepository;
        this.branchRepository = branchRepository;
    }

    @Transactional
    public Feedback submit(FeedbackForm form) {
        Branch branch = null;
        if (form.getBranchId() != null) {
            branch = branchRepository.findByIdAndActiveTrue(form.getBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Chi nhánh không hợp lệ"));
        }

        Feedback feedback = new Feedback();
        feedback.setFullName(form.getFullName().trim());
        feedback.setEmail(form.getEmail().trim().toLowerCase());
        feedback.setPhone(blankToNull(form.getPhone()));
        feedback.setBranch(branch);
        feedback.setTopic(form.getTopic());
        feedback.setRating(form.getRating());
        feedback.setContent(form.getContent().trim());
        feedback.setPrivacyConsent(form.isPrivacyConsent());
        return feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public Page<Feedback> list(int page) {
        return feedbackRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(Math.max(page, 1) - 1, PAGE_SIZE));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
