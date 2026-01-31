package io.grann.words.review;

import io.grann.words.domain.ReviewState;
import io.grann.words.repository.ReviewStateRepository;
import io.grann.words.repository.WordRepository;
import io.grann.words.session.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
@SessionAttributes("reviewSession")
public class ReviewController {
    private final UserSession userSession;
    private final ReviewService reviewService;
    private final ReviewStateRepository reviewStateRepository;


    @PostMapping("/start")
    public String start(Model model) {
        ReviewSession session = reviewService.startSession(userSession);

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
        if (session == null || session.getTotalCount() == 0 || session.isFinished()) {
            return "redirect:/dashboard";
        }

        Long id = session.getCurrentReviewStateId();
        ReviewState reviewState = reviewStateRepository.findById(id).orElseThrow();

        model.addAttribute("word", reviewState.getWord());
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
            status.setComplete();
            return "redirect:/dashboard";
        }

        return "redirect:/reviews/session";
    }
}
