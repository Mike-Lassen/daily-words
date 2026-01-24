package io.grann.words.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        DashboardService.DashboardSummary summary = dashboardService.getDashboardSummary();

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
