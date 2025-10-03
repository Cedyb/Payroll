document.addEventListener("DOMContentLoaded", function () {
  const selectAll = document.getElementById("selectAll");
  const actionButton = document.getElementById("actionButton");
  const role = document.body.getAttribute("data-role");
  const searchInput = document.getElementById("searchInput");
  const tableBody = document.getElementById("employeeTableBody");

  const departmentDropdown = document.querySelector('select[name="departmentId"]');
  const positionDropdown = document.querySelector('select[name="positionId"]');
  const statusDropdown = document.querySelector('select[name="status"]');
  const paginationWrapper = document.querySelector(".pagination-wrapper");

  // Set action button text & style based on role
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

  function getEmployeeCheckboxes() {
    return document.querySelectorAll(".employee-checkbox");
  }

  function updateButtonVisibility() {
    const checkedCount = document.querySelectorAll(".employee-checkbox:checked").length;
    actionButton.style.display = checkedCount > 0 ? "block" : "none";
  }

  // ==========================
  // Select All functionality
  // ==========================
  if (selectAll) {
    selectAll.addEventListener("change", function () {
      getEmployeeCheckboxes().forEach(cb => cb.checked = selectAll.checked);
      updateButtonVisibility();
    });

    document.addEventListener("change", function (e) {
      if (e.target.classList.contains("employee-checkbox")) {
        const checkboxes = getEmployeeCheckboxes();
        selectAll.checked = Array.from(checkboxes).every(cb => cb.checked);
        updateButtonVisibility();
      }
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
            fetchFilteredEmployees(currentPage); // refresh table with current page
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
  // Fetch employees for search/filter/pagination
  // ==========================
  let currentPage = 0; // track current page
  function fetchFilteredEmployees(page = 0) {
    currentPage = page;

    const params = new URLSearchParams({
      keyword: searchInput.value.trim(),
      departmentId: departmentDropdown.value,
      positionId: positionDropdown.value,
      status: statusDropdown.value,
      page: page
    });

    fetch(`/payroll/filter?${params.toString()}`, {
      headers: { "X-Requested-With": "XMLHttpRequest" }
    })
      .then(res => res.text())
      .then(html => {
        if (tableBody) tableBody.innerHTML = html;
        updateButtonVisibility();
        updatePagination();
        highlightCurrentPage(page); // highlight correct page
      })
      .catch(err => console.error("Filter error:", err));
  }

  // ==========================
  // Highlight active pagination page
  // ==========================
  function highlightCurrentPage(page) {
    if (!paginationWrapper) return;
    const items = paginationWrapper.querySelectorAll(".page-item");
    items.forEach(item => item.classList.remove("active"));

    const activeLink = paginationWrapper.querySelector(`.page-link[data-page='${page}']`);
    if (activeLink) activeLink.parentElement.classList.add("active");
  }

  // ==========================
  // Search & Filter Event Listeners
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

    departmentDropdown.dispatchEvent(new Event("change"));
  }

  // ==========================
  // AJAX Pagination
  // ==========================
  function updatePagination() {
    if (!paginationWrapper) return;

    const links = paginationWrapper.querySelectorAll(".page-link");
    links.forEach(link => {
      const page = parseInt(link.getAttribute("data-page"));
      link.addEventListener("click", function (e) {
        e.preventDefault();
        if (!isNaN(page)) fetchFilteredEmployees(page);
      });
    });
  }

  // Trigger initial fetch
  fetchFilteredEmployees(0);
});
