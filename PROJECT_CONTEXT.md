# Project Context – Daily Words

## 1. Mission Statement

**Daily Words** is a personal-first vocabulary learning application designed to support *long-term, sustainable language learning*.

The project is motivated by extensive real-world use of existing tools such as Anki and Duolingo. While Anki excels at serious, customizable spaced repetition, it provides little support for preventing burnout. Duolingo, on the other hand, prioritizes gamification and multiple-choice interactions that often undermine deep recall and long-term retention.

Daily Words aims to occupy a deliberate middle ground:

> **Not faster learning, but learning that people can realistically maintain over months and years.**

The core design goal is to protect the learner from common failure modes such as over‑eager early learning, unbounded review queues, and punitive behavior after breaks.

---

## 2. Core Design Principles

1. **Explicit user agency**
   Learning new words is a conscious action, separate from reviewing existing ones.

2. **Burnout prevention over optimization**
   The system favors predictability and calm progress over maximal efficiency.

3. **Minimalism and seriousness**
   The UI is intentionally simple, distraction-free, and non-gamified.

4. **Structure over infinite queues**
   Vocabulary is partitioned into decks and levels that provide clear stopping points.

5. **Explicit data over inference**
   Linguistic information (e.g. kana readings) is stored explicitly rather than inferred via dictionaries or heuristics.

---

## 3. Target User (Initial Phase)

* The primary user is the developer themself.
* Experienced language learner
* Familiar with tools like Anki
* Prefers calm, serious tools
* Values long-term consistency over novelty

Multi-user support and onboarding are explicitly out of scope for the MVP.

---

## 4. Vocabulary Model Overview

The core domain model follows a clear hierarchy:

```
Deck → Level → Word
```

* **Deck**: A semantic collection of vocabulary (e.g. *Genki I*, *Manga Vocabulary*).
* **Level**: A deck-specific subdivision (e.g. *Lesson 1*, *Chapter 3*).
* **Word**: A vocabulary item with meaning and learning state.

Orthogonal concerns:

* **ReviewState**: Spaced repetition scheduling (introduced after learning).
* **WordAnnotation**: Optional linguistic metadata (kana, furigana, notes).

---

## 5. Japanese Language Support

Daily Words explicitly supports Japanese as a first-class target language.

### Kana Reading

* Each word may have a **kana annotation** (hiragana-only reading).
* Example:

  * Word: 勉強する
  * Kana: べんきょうする

Kana readings:

* are supplied manually during data entry
* serve as canonical linguistic data
* enable future features such as typing quizzes and furigana derivation

### Furigana

* Furigana is considered a *presentation concern*, not canonical data.
* It may later be derived from kanji + kana or overridden explicitly.

### UI Usage

* Kana can be temporarily revealed by hovering over the word.
* Default display remains the kanji form to encourage recall.

---

## 6. Learning Flow (MVP)

Learning new words is intentionally structured into two phases.

### Phase A – Introduction

* Exactly **5 new words** per session
* Foreign text and meaning shown together
* User navigates using *Previous / Next*
* No grading, no pressure
* On the last word, *Next* becomes *Review*

### Phase B – Review

* Same 5 words
* One word at a time
* Recall → Show Answer → Again / Good
* Review order is deterministic
* Words form a queue:

  * **Good** → word removed
  * **Again** → word moved to back of queue

The session ends when the queue is empty.

---

## 7. Spaced Repetition System (Post-Learning)

Words enter the SRS **only after** completing a learning session.

### SRS Levels

| Level | Interval | Tag       |
| ----: | -------- | --------- |
|     1 | 1 day    | Trainee   |
|     2 | 2 days   | Trainee   |
|     3 | 5 days   | Trainee   |
|     4 | 12 days  | Expert    |
|     5 | 4 weeks  | Expert    |
|     6 | 9 weeks  | Expert    |
|     7 | —        | Graduated |

Rules:

* Words become *Expert* only after passing the 5‑day review.
* Trainee failures drop one level.
* Expert failures drop two levels.
* Graduated words leave the review queue entirely.

SRS persistence is introduced **after** the learning flow is validated.

---

## 8. Dashboard Philosophy

The dashboard is the single entry point to the app.

Responsibilities:

* Start learning sessions
* (Later) start review sessions
* Provide visibility into workload

The dashboard avoids:

* statistics overload
* streaks
* gamification

---

## 9. Technology Choices

### Backend

* **Java 21**
* **Spring Boot 4.x**
* **Spring MVC**
* **Spring Data JPA**
* **Hibernate**
* **PostgreSQL**
* **Lombok**

### Frontend

* **Thymeleaf** (server-side rendering)
* **Bootstrap** (layout & basic styling)
* **Minimal jQuery** (small UI interactions only)

This stack was chosen to:

* maximize iteration speed
* minimize frontend complexity
* keep the system easy to reason about

---

## 10. Development Approach

* Domain-first, object-oriented design
* ORM schema generated from Java classes
* Explicit boundaries between:

  * domain logic
  * service logic
  * UI/session flow

One-off tooling (e.g. CSV importers) is run explicitly via separate `main()` entry points, not on application startup.

---

## 11. MVP Roadmap

### Phase 1 – Core Foundations (Completed / In Progress)

* Domain model (Deck, Level, Word, Annotation)
* CSV import for Genki vocabulary
* Dashboard
* Learning session UI
* Kana hover display

### Phase 2 – Learning Validation

* Daily personal usage
* Fine-tune pacing and UI calmness
* Confirm 5‑word batch size

### Phase 3 – Review Persistence

* Persist `ReviewState` after learning
* Implement review sessions from dashboard
* Display progress counts

### Phase 4 – Burnout Prevention

* Vacation mode (pause SRS time)
* Level completion rules (e.g. 80% expert)
* Smoother re-entry after breaks

### Phase 5 – UX & Language Features

* Optional kana toggle
* Furigana rendering
* Typing-based reviews

### Phase 6 – Platform Exploration (Future)

* REST API extraction
* SPA frontend (e.g. React/Vue)
* Native iOS client

---

## 12. Out of Scope (Intentionally)

* Gamification
* Streaks
* Leaderboards
* AI-generated content
* Social features
* Multi-user accounts (initially)

---

## 13. Success Criteria

The MVP is successful if:

* It is used daily for weeks or months
* Review queues feel manageable
* Breaks do not lead to abandonment
* The app feels calm, predictable, and non-punitive

Everything else is secondary.
