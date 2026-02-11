package io.grann.words.learning;

import io.grann.words.domain.Word;
import io.grann.words.markdown.MarkdownRenderer;
import io.grann.words.repository.WordRepository;
import io.grann.words.session.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.util.List;

@Controller
@RequestMapping("/learning")
@Slf4j
@RequiredArgsConstructor
@SessionAttributes("learningSession")
public class LearningController {
    private final UserSession userSession;
    private final LearningService learningService;
    private final WordRepository wordRepository;
    private final MarkdownRenderer markdownRenderer;


    @PostMapping("/start")
    public String startLearning(Model model) {
        log.info("learning user-session: " + userSession);

        LearningSession session = learningService.startSession(userSession);
        model.addAttribute("learningSession", session);
        return "redirect:/learning/session";
    }

    @GetMapping("/session")
    public String session(
            @ModelAttribute("learningSession") LearningSession session,
            Model model
    ) {
        if (session == null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("phase", session.getPhase());
        model.addAttribute("totalCount", session.getTotalCount());

        if (session.getPhase() == LearningPhase.INTRODUCTION) {
            Word word = findById(session.getIntroWordId());
            if (word.getNote() != null) {
                model.addAttribute(
                        "wordNoteHtml",
                        markdownRenderer.render(word.getNote())
                );
            }
            model.addAttribute("introIndex", session.getIntroIndex());
            model.addAttribute("word", findById(session.getIntroWordId()));
            model.addAttribute("lastIntroWord", session.isLastIntroWord());
        } else {
            model.addAttribute("word", findById(session.getCurrentWord()));
            model.addAttribute("remainingCount", session.getRemainingCount());
            model.addAttribute("totalCount", session.getTotalCount());
            model.addAttribute("showAnswer", session.isShowAnswer());
        }

        return "learning-session";
    }

    private Word findById(Long id) {
        return wordRepository.findById(id).get();
    }

    // ---------- INTRODUCTION ----------

    @PostMapping("/intro/next")
    public String introNext(@ModelAttribute LearningSession session) {
        session.setIntroIndex(session.getIntroIndex() + 1);
        return "redirect:/learning/session";
    }

    @PostMapping("/intro/prev")
    public String introPrev(@ModelAttribute LearningSession session) {
        session.setIntroIndex(session.getIntroIndex() - 1);
        return "redirect:/learning/session";
    }

    @PostMapping("/intro/review")
    public String startReview(@ModelAttribute LearningSession session) {
        learningService.startReview(session);
        return "redirect:/learning/session";
    }

    // ---------- REVIEW ----------

    @PostMapping("/review/show-answer")
    public String showAnswer(@ModelAttribute LearningSession session) {
        session.setShowAnswer(true);
        return "redirect:/learning/session";
    }

    @PostMapping("/review/grade")
    public String grade(
            @ModelAttribute LearningSession session,
            @RequestParam ReviewRating rating,
            SessionStatus status
    ) {
        learningService.applyRating(session, rating);

        if (session.isFinished()) {

            learningService.complete(userSession, session);
            status.setComplete(); // clears session
            return "redirect:/dashboard";
        }
        learningService.advance(session);
        return "redirect:/learning/session";
    }
}
