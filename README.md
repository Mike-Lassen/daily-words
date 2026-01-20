# Vocabulary Training App

*A sustainable, burnout-resistant vocabulary learning tool.*

---

## Mission

This project was created out of long-term frustration with existing vocabulary learning tools.

I have used Anki extensively for learning Japanese and deeply value its serious, distraction-free interface and emphasis on active recall. Compared to heavily gamified apps, it better supports long-term retention. However, Anki also exposes a common failure pattern: learners often start too enthusiastically, accumulate large review queues, take a break, and then abandon the tool entirely when returning feels overwhelming.

This app is an attempt to address that problem directly.

The mission is **not faster learning**, but **learning that can realistically be sustained over months and years**. The system is designed to reduce burnout, make review workload predictable, and treat breaks in learning as normal rather than as failures.

---

## What This Project Is

This is a personal vocabulary training application built around:

- Explicit separation of learning new words and reviewing known ones
- A finite, level-based spaced repetition system
- Self-graded recall without gamification
- Predictable progression and clear completion states

The project is currently focused on **Japanese vocabulary**, but the underlying model is language-agnostic.

---

## MVP Scope

The current MVP focuses on being **personally usable** and stable enough for daily use.

### Core Concepts

- Vocabulary is structured into three states:
  - **Learning** – actively introduced but not yet in SRS
  - **Reviewing** – part of the spaced repetition system
  - **Graduated** – completed and removed from reviews

- Learning new words is an explicit, user-initiated action
- Reviews are self-graded with simple *Good / Again* choices
- Words progress through fixed SRS levels with predefined intervals
- Words eventually graduate and leave the review queue

### Design Principles

- Predictable review workload
- No punishment for stopping early or taking breaks
- Minimal interface and feature set
- Transparency over engagement tricks

---

## Explicit Non-Goals (for now)

To keep the MVP focused, the following are intentionally excluded:

- Gamification (streaks, XP, leaderboards)
- Review limits or daily caps
- Vacation mode
- Statistics and analytics
- Audio, images, or hints
- Multi-language support

These may be revisited later, but are out of scope for the current phase.

---

## Tech Stack (Current)

- **Backend:** Java, Spring Boot
- **Persistence:** JPA / Hibernate
- **Frontend:** Early stage / evolving
- **Database:** Local development focused

Details may change as the project matures.

---

## Project Status

This project is under active personal development and experimentation.

The primary goal at this stage is **long-term daily usage** by the author to validate the design decisions. The codebase may change rapidly and is not yet optimised for external contributions.

---

## Contributing

This repository is currently a personal project.

If you’re interested in the ideas behind it, feel free to open an issue to discuss design decisions, edge cases, or alternative SRS models. More formal contribution guidelines may be added later.
