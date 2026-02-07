(function () {
  function initWordDisplay(root) {
    const els = (root || document).querySelectorAll(".js-word-display");
    els.forEach((el) => {
      const kanji = el.dataset.kanji || el.textContent;
      const kana = el.dataset.kana;

      if (!kana) return;

      el.addEventListener("mouseenter", () => { el.textContent = kana; });
      el.addEventListener("mouseleave", () => { el.textContent = kanji; });
    });
  }

  // Initial page load
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", () => initWordDisplay(document));
  } else {
    initWordDisplay(document);
  }

  // Optional: expose in case you inject fragments dynamically later
  window.DailyWords = window.DailyWords || {};
  window.DailyWords.initWordDisplay = initWordDisplay;
})();
