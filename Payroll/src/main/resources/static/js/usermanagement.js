document.addEventListener("DOMContentLoaded", function() {
  // -------------------------
  // Toast for reset password
  // -------------------------
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.has("resetSuccess")) {
    const toastEl = document.getElementById("resetToast");
    const toast = new bootstrap.Toast(toastEl);
    toast.show();
    window.history.replaceState({}, document.title, window.location.pathname);
  }

  // -------------------------
  // User Modal setup (unchanged logic)
  // -------------------------
  const userModal = new bootstrap.Modal(document.getElementById("userModal"));
  const userForm = document.getElementById("userForm");
  const saveUserBtn = document.getElementById("saveUserBtn");
  const departmentDropdown = userForm.querySelector("select[name='departmentId']");
  const positionDropdown = userForm.querySelector("select[name='positionId']");
  const allPositions = Array.from(positionDropdown.querySelectorAll("option[data-department]"));

  function filterPositions(selectedDept) {
    positionDropdown.innerHTML = '<option value="">Select Position</option>';
    allPositions.forEach(opt => {
      if (opt.dataset.department === selectedDept) positionDropdown.appendChild(opt);
    });
  }

  departmentDropdown.addEventListener("change", () => {
    filterPositions(departmentDropdown.value);
    positionDropdown.value = "";
  });

  // -------------------------
  // Add User
  // -------------------------
  document.getElementById("addUserBtn").addEventListener("click", () => {
    if (document.getElementById("addUserBtn").classList.contains("readonly")) return;
    userForm.reset();
    userForm.action = "/usermanagement/create";
    userForm.method = "post";
    userForm.querySelector("select[name='system_role']").value = "";
    departmentDropdown.value = "";
    positionDropdown.value = "";
    document.querySelector("#userModal .modal-title").textContent = "Add User";
    saveUserBtn.textContent = "Save";
    userModal.show();
  });

  // -------------------------
  // Edit User
  // -------------------------
  document.querySelectorAll(".js-edit-user").forEach(btn => {
    btn.addEventListener("click", () => {
      if (btn.classList.contains("readonly")) return;
      userForm.reset();
      userForm.action = `/usermanagement/${btn.dataset.id}/update`;
      userForm.method = "post";
      userForm.querySelector("input[name='id']").value = btn.dataset.id;
      userForm.querySelector("input[name='firstName']").value = btn.dataset.firstname;
      userForm.querySelector("input[name='lastName']").value = btn.dataset.lastname;
      userForm.querySelector("input[name='email']").value = btn.dataset.email;
      userForm.querySelector("input[name='address']").value = btn.dataset.address || '';
      userForm.querySelector("input[name='phone']").value = btn.dataset.phone || '';
      userForm.querySelector("input[name='hireDate']").value = btn.dataset.hiredate || '';
      userForm.querySelector("select[name='system_role']").value = btn.dataset.role || '';
      departmentDropdown.value = btn.dataset.departmentid || '';
      filterPositions(departmentDropdown.value);
      positionDropdown.value = btn.dataset.positionid || '';
      document.querySelector("#userModal .modal-title").textContent = "Edit User";
      saveUserBtn.textContent = "Update";
      userModal.show();
    });
  });

  saveUserBtn.addEventListener("click", () => userForm.submit());

  // -------------------------
  // Advanced Table Filtering (fixed & robust)
  // -------------------------
  const filters = {
    id: document.getElementById('filterID'),
    name: document.getElementById('filterName'),
    position: document.getElementById('filterPosition'),
    department: document.getElementById('filterDepartment'),
    role: document.getElementById('filterRole')
  };

  const table = document.getElementById('userTable');
  if (table) {
    const tbody = table.querySelector('tbody');

    // Build a stable list-of-positions from the server-rendered options
    // (we'll recreate <option> elements from this list when department changes)
    const initialPosOpts = [];
    if (filters.position) {
      Array.from(filters.position.querySelectorAll('option')).forEach(opt => {
        const val = opt.value || "";
        const txt = (opt.textContent || "").trim();
        const dept = opt.dataset ? (opt.dataset.department || "") : "";
        // skip the blank "All Positions" option
        if (val !== "") initialPosOpts.push({ value: val, text: txt, dept: String(dept) });
      });
    }

    // Map positionId -> title (fallback matching by text if needed)
    const posIdToText = new Map(initialPosOpts.map(p => [p.value, p.text]));

    // Map departmentId -> departmentName (for fallback display matching)
    const deptIdToName = new Map();
    if (filters.department) {
      Array.from(filters.department.querySelectorAll('option')).forEach(opt => {
        if (opt.value !== "") deptIdToName.set(String(opt.value), (opt.textContent || "").trim());
      });
    }

    // Rebuild the Position search dropdown based on selected department
    function filterSearchPositions(selectedDept) {
      if (!filters.position) return;
      filters.position.innerHTML = '<option value="">All Positions</option>';
      initialPosOpts.forEach(item => {
        if (selectedDept === "" || String(item.dept) === String(selectedDept)) {
          const o = document.createElement('option');
          o.value = item.value;
          o.textContent = item.text;
          o.setAttribute('data-department', item.dept);
          filters.position.appendChild(o);
        }
      });
    }

    // Initial populate of position filter (show all)
    filterSearchPositions(filters.department && filters.department.value ? filters.department.value : "");

    // Core row filtering:
    function filterTable() {
      const idVal = filters.id ? (filters.id.value || "").toLowerCase() : "";
      const nameVal = filters.name ? (filters.name.value || "").toLowerCase() : "";
      const positionVal = filters.position ? (filters.position.value || "") : "";
      const deptVal = filters.department ? (filters.department.value || "") : "";
      const roleVal = filters.role ? (filters.role.value || "") : "";

      tbody.querySelectorAll('tr').forEach(row => {
        const cells = row.querySelectorAll('td');
        if (cells.length < 2) {
          row.style.display = ''; // nothing to match against
          return;
        }

        // ID / Name checks (text)
        const matchID = idVal === "" || (cells[0].textContent || "").toLowerCase().includes(idVal);
        const matchName = nameVal === "" || (cells[1].textContent || "").toLowerCase().includes(nameVal);

        // For position & department, prefer to use the data-* attributes on the Edit button (already present in your row)
        const editBtn = row.querySelector('.js-edit-user');
        const rowPosId = editBtn ? (editBtn.dataset.positionid || "") : "";
        const rowDeptId = editBtn ? (editBtn.dataset.departmentid || "") : "";

        // Fallback: If no data-* present, compare visible cell text to option text
        const posCellText = (cells[2] && cells[2].textContent || "").trim();
        const deptCellText = (cells[3] && cells[3].textContent || "").trim();
        const rowRoleText = (cells[4] && cells[4].textContent || "").trim();

        // Position match:
        let matchPosition = false;
        if (positionVal === "") {
          matchPosition = true;
        } else if (rowPosId && positionVal === rowPosId) {
          matchPosition = true;
        } else {
          // fallback: get the text for selected positionId and compare to cell text
          const selectedPosText = posIdToText.get(positionVal) || "";
          if (selectedPosText && selectedPosText === posCellText) matchPosition = true;
        }

        // Department match:
        let matchDepartment = false;
        if (deptVal === "") {
          matchDepartment = true;
        } else if (rowDeptId && deptVal === rowDeptId) {
          matchDepartment = true;
        } else {
          // fallback: compare dept name
          const selectedDeptName = deptIdToName.get(String(deptVal)) || "";
          if (selectedDeptName && selectedDeptName === deptCellText) matchDepartment = true;
        }

        // Role match (role option text should match the cell text)
        const matchRole = roleVal === "" || rowRoleText === roleVal;

        if (matchID && matchName && matchPosition && matchDepartment && matchRole) {
          row.style.display = '';
        } else {
          row.style.display = 'none';
        }
      });
    }

    // Rebuild positions when department filter changes, and re-run filter
    if (filters.department) {
      filters.department.addEventListener("change", () => {
        filterSearchPositions(filters.department.value);
        if (filters.position) filters.position.value = "";
        filterTable();
      });
    }

    // Trigger filtering on inputs/selects
    Object.values(filters).forEach(input => {
      if (!input) return;
      input.addEventListener('input', filterTable);
      input.addEventListener('change', filterTable);
    });

    // Run initial filter to reflect any default selections
    filterTable();
  }
});
