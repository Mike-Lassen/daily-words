   $(function () {
        const $word = $(".word-display");

        if ($word.length === 0) {
            return;
        }

        const kanji = $word.data("kanji");
        const kana = $word.data("kana");

        if (!kana) {
            return; // no kana → no behavior
        }

        $word.hover(
            function () { $word.text(kana); },
            function () { $word.text(kanji); }
        );
    });