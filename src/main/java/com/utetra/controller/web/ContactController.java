package com.utetra.controller.web;

import com.utetra.dto.FeedbackForm;
import com.utetra.entity.enums.FeedbackTopic;
import com.utetra.security.AppUserDetails;
import com.utetra.service.CatalogService;
import com.utetra.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactController {

    private final FeedbackService feedbackService;
    private final CatalogService catalogService;

    public ContactController(FeedbackService feedbackService, CatalogService catalogService) {
        this.feedbackService = feedbackService;
        this.catalogService = catalogService;
    }

    @GetMapping("/contact")
    public String form(@AuthenticationPrincipal AppUserDetails user,
                       @RequestParam(required = false) String sent, Model model) {
        FeedbackForm form = new FeedbackForm();
        if (user != null) {
            form.setFullName(user.getFullName());
            form.setEmail(user.getEmail());
        }
        model.addAttribute("feedbackForm", form);
        model.addAttribute("sent", sent != null);
        addOptions(model);
        return "contact";
    }

    @PostMapping("/contact")
    public String submit(@Valid @ModelAttribute("feedbackForm") FeedbackForm form,
                         BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                feedbackService.submit(form);
                return "redirect:/contact?sent=1";
            } catch (IllegalArgumentException e) {
                result.rejectValue("branchId", "invalid", e.getMessage());
            }
        }
        model.addAttribute("sent", false);
        addOptions(model);
        return "contact";
    }

    private void addOptions(Model model) {
        model.addAttribute("branches", catalogService.activeBranches());
        model.addAttribute("topics", FeedbackTopic.values());
    }
}
