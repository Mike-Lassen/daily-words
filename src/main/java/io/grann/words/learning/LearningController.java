package io.grann.words.learning;

import io.grann.words.domain.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.util.List;

@Controller
@RequestMapping("/learning")
@RequiredArgsConstructor
@SessionAttributes("learningSession")
public class LearningController {

    private final LearningService learningService;

    // TEMP: inject words manually or via repository later
    @PostMapping("/start")
    public String startLearning(Model model) {

        List<Word> words = List.of(
                Word.builder().id(1L).foreignText("今").nativeText("now").build(),
                Word.builder().id(2L).foreignText("英語").nativeText("English").build(),
                Word.builder().id(3L).foreignText("学生").nativeText("student").build(),
                Word.builder().id(4L).foreignText("午後").nativeText("P.M.").build(),
                Word.builder().id(5L).foreignText("午前").nativeText("A.M.").build()
        );

        LearningSession session = learningService.startSession(words);
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
            model.addAttribute("introIndex", session.getIntroIndex());
            model.addAttribute("introWord", session.getIntroWord());
            model.addAttribute("lastIntroWord", session.isLastIntroWord());
        } else {
            model.addAttribute("currentWord", session.getCurrentWord());
            model.addAttribute("passedCount", session.getPassedCount());
            model.addAttribute("showAnswer", session.isShowAnswer());
        }

        return "learning-session";
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
            status.setComplete(); // clears session
            return "redirect:/dashboard";
        }

        return "redirect:/learning/session";
    }
}
