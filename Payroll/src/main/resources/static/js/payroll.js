// ========================
// Dropdown Toggle Function
// ========================
function toggleDropdown(elem) {
  document.querySelectorAll('.dropdown-menu-custom').forEach(menu => {
    if (menu !== elem.nextElementSibling) {
      menu.style.display = 'none';
    }
  });

  const menu = elem.nextElementSibling;
  menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
}

// Close dropdown when clicking outside
document.addEventListener('click', function (event) {
  const isEllipsis = event.target.classList.contains('ellipsis');
  if (!isEllipsis) {
    document.querySelectorAll('.dropdown-menu-custom').forEach(menu => {
      menu.style.display = 'none';
    });
  }
});

// ========================
// Init after DOM is loaded
// ========================
document.addEventListener("DOMContentLoaded", function () {
  const selectAll = document.getElementById("selectAll");
  const actionButton = document.getElementById("actionButton");
  const role = document.body.getAttribute("data-role");
  const searchInput = document.getElementById("searchInput");
  const tableBody = document.querySelector("tbody");

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

  // ========================
  // Select All functionality
  // ========================
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

  // ========================
  // Action Button Click Handler
  // ========================
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
            window.location.reload(); // refresh payroll table
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

  // ========================
  // Real-time Search
  // ========================
  if (searchInput) {
    searchInput.addEventListener("input", function () {
      const keyword = searchInput.value.trim();

      fetch(`/payroll/search?keyword=${encodeURIComponent(keyword)}`, {
        headers: { "X-Requested-With": "XMLHttpRequest" }
      })
        .then(res => res.text())
        .then(html => {
          if (tableBody) {
            tableBody.innerHTML = html;
          }
          updateButtonVisibility(); // update checkbox visibility after search
        })
        .catch(err => console.error("Search error:", err));
    });
  }
});
