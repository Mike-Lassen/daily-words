$(function () {
    const $area = $("#table-area");
    if ($area.length === 0) {
        return;
    }

    const deckId = $area.data("deck-id");
    const size = Number($area.data("size")) || 200;

    const $q = $(".js-deck-words-q");

    let state = {
        q: ($q.val() || "").toString(),
        page: Number($area.data("page")) || 0
    };

    let debounceTimer = null;
    let requestToken = 0;

    function buildUrl() {
        const params = $.param({
            q: state.q,
            page: state.page
        });
        return `/decks/${deckId}/words/table?${params}`;
    }

    function setLoading(isLoading) {
        $area.toggleClass("is-loading", isLoading);
        $area.find(".js-table-prev, .js-table-next").prop("disabled", isLoading);
    }

    function load() {
        requestToken++;
        const token = requestToken;

        setLoading(true);

        $.get(buildUrl())
            .done(function (html) {
                if (token !== requestToken) {
                    return; // stale response
                }

                // Replace the whole table fragment region (summary + table + pagination)
                $area.html(html);
            })
            .fail(function () {
                if (token !== requestToken) {
                    return;
                }
                // Keep this simple: show a minimal error inline.
                $area.prepend(
                    '<div style="color:#b91c1c; margin-top:0.75rem;">Failed to load table. Please try again.</div>'
                );
            })
            .always(function () {
                if (token !== requestToken) {
                    return;
                }
                setLoading(false);
            });
    }

    // Filter: debounce + reset to page 0
    $q.on("input", function () {
        state.q = ($q.val() || "").toString();
        state.page = 0;

        if (debounceTimer) {
            clearTimeout(debounceTimer);
        }
        debounceTimer = setTimeout(load, 500);
    });

    // Paging: delegated because we replace the fragment
    $area.on("click", ".js-table-prev", function () {
        if (state.page <= 0) {
            return;
        }
        state.page -= 1;
        load();
    });

    $area.on("click", ".js-table-next", function () {
        // We don't know totalCount client-side; rely on server disabling button for last page.
        state.page += 1;
        load();
    });

    // Keep page size fixed (server enforced); suppress unused variable warning.
    void size;
});

