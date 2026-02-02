# Incremental Table Loading

## Purpose
Provide a fast, responsive way to display and interact with a large dataset in a table by loading and updating only the needed HTML fragments (rows, pagination, summary) instead of reloading the full page.

## Core idea
Treat the table as a set of independently replaceable fragments. User actions (typing a filter, changing page, sorting) trigger an AJAX request. The server returns just the updated fragment (or fragments), and jQuery swaps them into the existing DOM.

## Inputs
- **User intent**: filter text, selected facets, sort column/direction, page number, page size.
- **Client state**: current query params, debounce timing (e.g., 500ms), last request token.
- **Server contract**: endpoints that can render partial HTML for the table body and related UI.

## Outputs
- Updated **table body** (rows)
- Updated **pagination controls**
- Optional updated **result count / summary**
- Updated **URL query string** (optional history/pushState)

---

## System overview

### Actors
- **User**
- **Browser UI** (table + controls)
- **jQuery Incremental Loader** (event binding, debounce, request management, DOM swapping)
- **Spring Boot server** (controllers + view rendering)
- **Database** (paged + filtered queries)

### User journeys
1. **Filter**: user types → debounce 500ms or Enter → request → swap rows + reset page to 1.
2. **Paging**: user clicks next/prev/page number → request → swap rows + pagination.
3. **Sort**: user clicks header → request → swap rows + header indicators + pagination.

### Client responsibilities
- Bind events on filter inputs, pagination links, sort headers.
- Maintain a single source of truth for params (q, page, size, sort).
- Debounce/filter requests and cancel or ignore stale responses.
- Swap fragments into targets (e.g., `tbody`, pagination container).
- Preserve focus/caret and avoid layout jumps where possible.
- Optionally update browser URL and handle back/forward.

### Server responsibilities
- Accept query parameters (filter, page, size, sort).
- Perform efficient paged queries.
- Render **partial views**: table rows fragment, pagination fragment, count fragment.
- Return HTML fragments (single payload or multi-part) suitable for direct DOM insertion.

### Data contract (example shape)
- Request: `GET /items?filter=...&page=...&size=...&sort=...`
- Response: HTML containing either:
  - one fragment (just rows), or
  - multiple fragments (rows + pagination + summary) with identifiable wrapper elements.

---

## Key constraints / qualities
- **Performance**: avoid full page reload; minimal HTML transfer.
- **Correctness**: paging/sorting/filtering are consistent; no stale updates.
- **UX**: loading indicator, disabled controls during request, preserve scroll position optionally.
- **Accessibility**: announce updates (aria-live region) when content changes.
- **Resilience**: handle empty results, errors, and slow responses gracefully.

---

## Typical DOM layout (conceptual)
- Container: `#table-area`
  - Summary: `#table-summary`
  - Table: `#table`
    - Body target: `#table-body`
  - Pagination target: `#table-pagination`

---

## What “incremental” means here
Only the **incremental parts** of the table UI re-render per interaction—usually rows and paging—while the surrounding page and form controls remain intact. This keeps interactions fast even when the underlying dataset is large.
