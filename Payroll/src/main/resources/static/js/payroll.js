document.addEventListener("DOMContentLoaded", function () {
  // ==========================
  // Elements
  // ==========================
  const selectAll = document.getElementById("selectAll");
  const actionButton = document.getElementById("actionButton");
  const role = document.body.getAttribute("data-role");
  const searchInput = document.getElementById("searchInput");
  const tableBody = document.getElementById("employeeTableBody");

  const departmentDropdown = document.querySelector('select[name="departmentId"]');
  const positionDropdown = document.querySelector('select[name="positionId"]');
  const statusDropdown = document.querySelector('select[name="status"]');
  const paginationWrapper = document.querySelector(".pagination-wrapper");

  let currentPage = 0;

  // ==========================
  // Set action button text & style
  // ==========================
  if (role === "CLERK") {
    actionButton.textContent = "Generate Selection";
    actionButton.classList.remove("btn-success");
    actionButton.classList.add("btn-warning");
  } else if (role === "SUPER_ADMIN") {
    actionButton.textContent = "Approve Selection";
    actionButton.classList.remove("btn-warning");
    actionButton.classList.add("btn-success");
  } else {
    actionButton.textContent = "Perform Action";
  }

  // ==========================
  // Utility functions
  // ==========================
  function getEmployeeCheckboxes() {
    return document.querySelectorAll(".employee-checkbox");
  }

  function updateButtonVisibility() {
    const checkedCount = document.querySelectorAll(".employee-checkbox:checked").length;
    actionButton.style.display = checkedCount > 0 ? "block" : "none";
  }

  function rebindTableEvents() {
    const checkboxes = getEmployeeCheckboxes();
    checkboxes.forEach(cb => {
      cb.addEventListener("change", () => {
        if (selectAll) {
          selectAll.checked = Array.from(checkboxes).every(c => c.checked);
        }
        updateButtonVisibility();
      });
    });
  }

  // ==========================
  // Select All functionality
  // ==========================
  if (selectAll) {
    selectAll.addEventListener("change", function () {
      getEmployeeCheckboxes().forEach(cb => cb.checked = selectAll.checked);
      updateButtonVisibility();
    });
  }

  // ==========================
  // Action Button Click Handler
  // ==========================
  if (actionButton) {
    actionButton.addEventListener("click", function () {
      const selectedIds = Array.from(getEmployeeCheckboxes())
        .filter(cb => cb.checked)
        .map(cb => parseInt(cb.dataset.employeeId || cb.value));

      if (selectedIds.length === 0) {
        alert("Please select at least one employee.");
        return;
      }

      const url = role === "SUPER_ADMIN" ? "/payroll/approve" : "/payroll/generate";
      const payload = { employeeIds: selectedIds };

      fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      })
        .then(res => res.json())
        .then(data => {
          if (data.success) {
            alert(data.message);
            fetchFilteredEmployees(currentPage); // refresh table
          } else {
            alert(data.message || "Failed to update payrolls.");
          }
        })
        .catch(err => {
          console.error("Error:", err);
          alert("Something went wrong!");
        });
    });
  }

  // ==========================
  // Fetch employees via AJAX
  // ==========================
  function fetchFilteredEmployees(page = 0) {
    currentPage = page;

    if (!tableBody) return;

    tableBody.innerHTML = '<tr><td colspan="7">Loading...</td></tr>';

    const params = new URLSearchParams({
      keyword: searchInput.value.trim(),
      departmentId: departmentDropdown.value,
      positionId: positionDropdown.value,
      status: statusDropdown.value.toUpperCase(),
      page: page
    });

    fetch(`/payroll/filter?${params.toString()}`, {
      headers: { "X-Requested-With": "XMLHttpRequest" }
    })
      .then(res => res.text())
      .then(html => {
        tableBody.innerHTML = html;
        rebindTableEvents();
        updateButtonVisibility();
        highlightCurrentPage(page);
      })
      .catch(err => console.error("Filter error:", err));
  }

  // ==========================
  // Pagination - Event Delegation
  // ==========================
  if (paginationWrapper) {
    paginationWrapper.addEventListener("click", function (e) {
      if (e.target.classList.contains("page-link")) {
        e.preventDefault();
        const page = parseInt(e.target.dataset.page);
        if (!isNaN(page)) fetchFilteredEmployees(page);
      }
    });
  }

  function highlightCurrentPage(page) {
    if (!paginationWrapper) return;
    const items = paginationWrapper.querySelectorAll(".page-item");
    items.forEach(item => item.classList.remove("active"));

    const activeLink = paginationWrapper.querySelector(`.page-link[data-page='${page}']`);
    if (activeLink) activeLink.parentElement.classList.add("active");
  }

  // ==========================
  // Search & Filter listeners
  // ==========================
  if (searchInput) searchInput.addEventListener("input", () => fetchFilteredEmployees(0));
  if (positionDropdown) positionDropdown.addEventListener("change", () => fetchFilteredEmployees(0));
  if (statusDropdown) statusDropdown.addEventListener("change", () => fetchFilteredEmployees(0));

  // ==========================
  // Department -> Position dependent dropdown
  // ==========================
  if (departmentDropdown) {
    departmentDropdown.addEventListener("change", function () {
      const deptId = departmentDropdown.value;
      positionDropdown.innerHTML = '<option value="">All Positions</option>';

      const url = deptId ? `/payroll/positions?departmentId=${deptId}` : `/payroll/positions`;
      fetch(url)
        .then(res => res.json())
        .then(data => {
          data.forEach(pos => {
            const option = document.createElement("option");
            option.value = pos.id;
            option.textContent = pos.title;
            positionDropdown.appendChild(option);
          });
          fetchFilteredEmployees(0); // refresh table
        })
        .catch(err => console.error("Position fetch error:", err));
    });
  }

  // ==========================
  // Initial fetch
  // ==========================
  fetchFilteredEmployees(0);
});
