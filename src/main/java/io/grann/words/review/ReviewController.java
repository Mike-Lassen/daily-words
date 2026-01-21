package io.grann.words.review;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@RequestMapping("/reviews")
@SessionAttributes("reviewSession")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/start")
    public String start(Model model) {
        ReviewSession session = reviewService.startSession();

        // If nothing is due, just bounce back to dashboard
        if (session.getTotalCount() == 0) {
            return "redirect:/dashboard";
        }

        model.addAttribute("reviewSession", session);
        return "redirect:/reviews/session";
    }

    @GetMapping("/session")
    public String session(
            @ModelAttribute("reviewSession") ReviewSession session,
            Model model
    ) {
        if (session == null || session.getTotalCount() == 0) {
            return "redirect:/dashboard";
        }

        model.addAttribute("word", session.getCurrentWord());
        model.addAttribute("showAnswer", session.isShowAnswer());
        model.addAttribute("completedCount", session.getCompletedCount());
        model.addAttribute("totalCount", session.getTotalCount());

        return "review-session";
    }

    @PostMapping("/show-answer")
    public String showAnswer(@ModelAttribute("reviewSession") ReviewSession session) {
        session.setShowAnswer(true);
        return "redirect:/reviews/session";
    }

    @PostMapping("/grade")
    public String grade(
            @ModelAttribute("reviewSession") ReviewSession session,
            @RequestParam ReviewRating rating,
            SessionStatus status
    ) {
        reviewService.applyRating(session, rating);

        if (session.isFinished()) {
            status.setComplete(); // clears session
            return "redirect:/dashboard";
        }

        reviewService.advance(session);
        return "redirect:/reviews/session";
    }
}