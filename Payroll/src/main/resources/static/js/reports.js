
    document.addEventListener("DOMContentLoaded", function () {
    // Preserve active tab on reload
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

    // AJAX Pagination
    const auditContainer = document.getElementById('auditTableContainer');

    function loadAuditPage(page) {
    fetch(`/reports/audit?page=${page}`)
    .then(response => response.text())
    .then(html => {
    // Replace only tbody to avoid nested tables
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, 'text/html');
    const newTbody = doc.querySelector('tbody');
    if (newTbody) {
    const oldTbody = auditContainer.querySelector('tbody');
    oldTbody.replaceWith(newTbody);
}

    attachPaginationEvents(); // reattach events
    setActivePage(page);
});
}

    function handlePageClick(e) {
    e.preventDefault();
    const page = this.getAttribute('data-page');
    if (page !== null) loadAuditPage(page);
}

    function attachPaginationEvents() {
    // Attach events for pagination links outside container
    document.querySelectorAll('.pagination-wrapper .page-link').forEach(link => {
    link.removeEventListener('click', handlePageClick);
    link.addEventListener('click', handlePageClick);
});
}

    function setActivePage(page) {
    document.querySelectorAll('.pagination-wrapper .page-item').forEach(li => li.classList.remove('active'));
    const activeLink = document.querySelector(`.pagination-wrapper .page-link[data-page="${page}"]`);
    if (activeLink) activeLink.parentElement.classList.add('active');
}

    attachPaginationEvents(); // initial attach
});
