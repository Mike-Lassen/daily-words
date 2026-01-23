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

        LocalDateTime now = LocalDateTime.now(clock);

        long newWordsAvailable = wordRepository.countByStatus(WordStatus.LEARNING);
        long reviewsDue = wordRepository.countWordsDueForReview(now);

        model.addAttribute("newWordsAvailable", newWordsAvailable);
        model.addAttribute("reviewsDue", reviewsDue);

        return "dashboard";
    }
}
