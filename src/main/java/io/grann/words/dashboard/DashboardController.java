package io.grann.words.dashboard;

import io.grann.words.domain.WordStatus;
import io.grann.words.repository.WordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Clock;
import java.time.LocalDateTime;

@Controller
public class DashboardController {

    private final WordRepository wordRepository;
    private final Clock clock;

    public DashboardController(WordRepository wordRepository, Clock clock) {
        this.wordRepository = wordRepository;
        this.clock = clock;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {

        var now = LocalDateTime.now(clock);
        long reviewsDue = wordRepository.countWordsDueForReview(now);
        long learnableWordsCount = wordRepository.countByStatus(WordStatus.LEARNING);
        boolean newWordsAvailable = learnableWordsCount >= 5;

        model.addAttribute("newWordsAvailable", newWordsAvailable);
        model.addAttribute("reviewsDue", reviewsDue);
        model.addAttribute("learnableWordsCount", learnableWordsCount);

        return "dashboard";
    }
}
