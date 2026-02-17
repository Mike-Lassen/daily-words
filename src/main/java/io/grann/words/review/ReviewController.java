package io.grann.words.review;

import io.grann.words.domain.ReviewState;
import io.grann.words.repository.ReviewStateRepository;
import io.grann.words.session.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
@SessionAttributes("reviewSession")
@Slf4j
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

    @PostMapping("/exit")
    public String exit(SessionStatus status) {
        status.setComplete();
        return "redirect:/dashboard";
    }

    @GetMapping("/session")
    public String session(
            @SessionAttribute(value = "reviewSession", required = false) ReviewSession session,
            Model model
    ) {
        if (userSession.getDeckProgressId() == null) {
            return "redirect:/";
        }
        if (session == null || session.getTotalCount() == 0 || session.isFinished()) {
            return "redirect:/dashboard";
        }

        Long id = session.getCurrentReviewStateId();
        ReviewState reviewState = reviewStateRepository.findById(id).orElseThrow();

        model.addAttribute("word", reviewState.getWord());
        model.addAttribute("showAnswer", session.isShowAnswer());
        model.addAttribute("completedCount", session.getCompletedCount() + 1);
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
            reviewService.complete(userSession, session);
            status.setComplete();
            return "redirect:/dashboard";
        }

        return "redirect:/reviews/session";
    }
}
