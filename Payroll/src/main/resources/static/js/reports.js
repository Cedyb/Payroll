document.addEventListener("DOMContentLoaded", function () {

    // =============================
    // Element refs
    // =============================
    const auditContainer = document.getElementById('auditTableContainer');
    const startDate = document.getElementById("auditDate");
    const endDate = document.getElementById("auditEndDate");
    const searchInput = document.getElementById("auditSearch");

    if (!auditContainer) {
        console.error("auditTableContainer not found in DOM.");
        return;
    }

    // =============================
    // Tab persistence
    // =============================
    const tabButtons = document.querySelectorAll('button[data-bs-toggle="tab"]');
    tabButtons.forEach(button => {
        button.addEventListener('shown.bs.tab', e => {
            const targetId = e.target.getAttribute('data-bs-target');
            history.replaceState(null, null, targetId);
        });
    });
    const hash = window.location.hash;
    if (hash) {
        const activeTab = document.querySelector(`button[data-bs-target="${hash}"]`);
        if (activeTab) new bootstrap.Tab(activeTab).show();
    }

    // =============================
    // AJAX fetch for Audit Logs
    // =============================
    function fetchAudit(page = 0) {
        const start = startDate ? startDate.value : "";
        const end = endDate ? endDate.value : "";
        const search = searchInput ? searchInput.value.trim() : "";

        const params = new URLSearchParams();
        params.append("page", page);
        if (start) params.append("startDate", start);
        if (end) params.append("endDate", end);
        if (search) params.append("search", search);

        const url = `/reports/audit?${params.toString()}`;

        // debug: show exactly what's being requested
        console.debug("[fetchAudit] requesting:", url);

        // preserve active tab in URL (so refresh keeps Audit tab)
        history.replaceState(null, null, '#audit');

        fetch(url, { method: 'GET', credentials: 'same-origin' })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Network response was not ok: " + response.status);
                }
                return response.text();
            })
            .then(html => {
                // replace container fragment (table + pagination)
                auditContainer.innerHTML = html;
                attachPaginationEvents(); // rebind events on new pagination
            })
            .catch(err => {
                console.error("fetchAudit error:", err);
            });
    }

    // =============================
    // Pagination
    // =============================
    function handlePageClick(e) {
        e.preventDefault();
        // Use currentTarget to be safe if listener bound with remove/add
        const page = e.currentTarget.getAttribute('data-page');
        const pageNum = page !== null ? parseInt(page, 10) : 0;
        fetchAudit(pageNum);
    }

    function attachPaginationEvents() {
        // only search within the fragment we've replaced
        const links = auditContainer.querySelectorAll('.pagination a.page-link');
        links.forEach(link => {
            // remove then add to avoid duplicate handlers
            link.removeEventListener('click', handlePageClick);
            link.addEventListener('click', handlePageClick);
        });
    }

    // =============================
    // Filtering (dates + search)
    // =============================
    // we want date changes to trigger immediately, and live search with debounce
    const debounce = (fn, ms) => {
        let t;
        return (...args) => {
            clearTimeout(t);
            t = setTimeout(() => fn(...args), ms);
        };
    };

    const debouncedFetch = debounce(() => fetchAudit(), 250);

    // Some date pickers emit 'input' rather than 'change', so attach both
    if (startDate) {
        startDate.addEventListener('change', () => fetchAudit());
        startDate.addEventListener('input', debouncedFetch);
    }
    if (endDate) {
        endDate.addEventListener('change', () => fetchAudit());
        endDate.addEventListener('input', debouncedFetch);
    }

    if (searchInput) {
        // Enter still triggers immediately
        searchInput.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') fetchAudit();
        });
        // live search with debounce
        searchInput.addEventListener('input', debouncedFetch);
    }

    // =============================
    // Initial binding (in case initial HTML has pagination)
    // =============================
    attachPaginationEvents();
});
