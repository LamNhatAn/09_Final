package com.utetra.service;

import com.utetra.dto.FeedbackForm;
import com.utetra.entity.Branch;
import com.utetra.entity.Feedback;
import com.utetra.entity.enums.FeedbackTopic;
import com.utetra.repository.BranchRepository;
import com.utetra.repository.FeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FeedbackServiceTest {

    private FeedbackRepository feedbackRepository;
    private BranchRepository branchRepository;
    private FeedbackService service;

    @BeforeEach
    void setUp() {
        feedbackRepository = mock(FeedbackRepository.class);
        branchRepository = mock(BranchRepository.class);
        service = new FeedbackService(feedbackRepository, branchRepository);
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void submitNormalizesAndStoresFeedback() {
        Branch branch = new Branch();
        branch.setId(2L);
        when(branchRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(branch));

        FeedbackForm form = validForm();
        form.setBranchId(2L);
        form.setFullName("  Nguyễn Văn An  ");
        form.setEmail("  AN@EXAMPLE.COM ");
        form.setPhone(" 0944884068 ");
        form.setContent("  Phục vụ rất tốt  ");

        Feedback saved = service.submit(form);

        assertEquals("Nguyễn Văn An", saved.getFullName());
        assertEquals("an@example.com", saved.getEmail());
        assertEquals("0944884068", saved.getPhone());
        assertEquals("Phục vụ rất tốt", saved.getContent());
        assertSame(branch, saved.getBranch());
        assertTrue(saved.isPrivacyConsent());
    }

    @Test
    void submitAllowsGeneralWebsiteFeedbackWithoutBranch() {
        FeedbackForm form = validForm();
        form.setBranchId(null);
        form.setPhone(" ");

        Feedback saved = service.submit(form);

        assertNull(saved.getBranch());
        assertNull(saved.getPhone());
        verifyNoInteractions(branchRepository);
    }

    @Test
    void submitRejectsInactiveOrUnknownBranch() {
        FeedbackForm form = validForm();
        form.setBranchId(99L);
        when(branchRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.submit(form));
        verify(feedbackRepository, never()).save(any());
    }

    private static FeedbackForm validForm() {
        FeedbackForm form = new FeedbackForm();
        form.setFullName("Khách hàng");
        form.setEmail("user@example.com");
        form.setTopic(FeedbackTopic.WEBSITE);
        form.setRating(5);
        form.setContent("Dễ sử dụng");
        form.setPrivacyConsent(true);
        return form;
    }
}
