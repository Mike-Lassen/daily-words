package io.grann.words.dashboard;

import io.grann.words.session.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

@RequiredArgsConstructor
@Controller
@Slf4j
public class DashboardController {
    private final UserSession userSession;
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        if (userSession.getDeckProgressId() == null) {
            return "redirect:/";
        }

        DashboardService.DashboardSummary summary = dashboardService.getDashboardSummary(userSession.getDeckProgressId());

        model.addAttribute("newWordsAvailable", summary.newWordsAvailable());
        model.addAttribute("reviewsDue", summary.reviewsDue());

        model.addAttribute("currentLevelName", summary.levelName());
        model.addAttribute("currentLevelTotalWords", summary.total());
        model.addAttribute("currentLevelNotInReviewCount", summary.notInReview());
        model.addAttribute("currentLevelTraineeCount", summary.trainee());
        model.addAttribute("currentLevelExpertCount", summary.expert());

        long safeTotal = Math.max(summary.total(), 1L);
        model.addAttribute("currentLevelNotInReviewPercent", (summary.notInReview() * 100) / safeTotal);
        model.addAttribute("currentLevelTraineePercent", (summary.trainee() * 100) / safeTotal);
        model.addAttribute("currentLevelExpertPercent", (summary.expert() * 100) / safeTotal);

        return "dashboard";
    }
}