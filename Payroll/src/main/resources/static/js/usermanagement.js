document.addEventListener("DOMContentLoaded", function() {
  // ================= Show toast when resetSuccess is in URL =================
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.has("resetSuccess")) {
    const toastEl = document.getElementById("resetToast");
    const toast = new bootstrap.Toast(toastEl);
    toast.show();
    window.history.replaceState({}, document.title, window.location.pathname);
  }

  const userModal = new bootstrap.Modal(document.getElementById("userModal"));
  const userForm = document.getElementById("userForm");
  const saveUserBtn = document.getElementById("saveUserBtn");

  const departmentDropdown = userForm.querySelector("select[name='departmentId']");
  const positionDropdown = userForm.querySelector("select[name='positionId']");
  const allPositions = Array.from(positionDropdown.querySelectorAll("option[data-department]"));

  // ================= Function to filter positions based on department =================
  function filterPositions(selectedDept) {
    // Clear current options
    positionDropdown.innerHTML = '<option value="">Select Position</option>';

    // Add only positions that belong to selected department
    allPositions.forEach(opt => {
      if (opt.dataset.department === selectedDept) {
        positionDropdown.appendChild(opt);
      }
    });
  }

  // Trigger filter when department changes
  departmentDropdown.addEventListener("change", () => {
    filterPositions(departmentDropdown.value);
    positionDropdown.value = ""; // reset position selection
  });

  // ================= Add User =================
  document.getElementById("addUserBtn").addEventListener("click", () => {
    userForm.reset();
    userForm.action = "/usermanagement/create";
    userForm.method = "post";

    // Reset Role, Department, Position dropdowns
    userForm.querySelector("select[name='system_role']").value = "";
    departmentDropdown.value = "";
    positionDropdown.value = "";

    document.querySelector("#userModal .modal-title").textContent = "Add User";
    saveUserBtn.textContent = "Save";
    userModal.show();
  });

  // ================= Edit User =================
  document.querySelectorAll(".js-edit-user").forEach(btn => {
    btn.addEventListener("click", () => {
      userForm.reset();
      userForm.action = `/usermanagement/${btn.dataset.id}/update`;
      userForm.method = "post";

      // Fill basic inputs
      userForm.querySelector("input[name='id']").value = btn.dataset.id;
      userForm.querySelector("input[name='firstName']").value = btn.dataset.firstname;
      userForm.querySelector("input[name='lastName']").value = btn.dataset.lastname;
      userForm.querySelector("input[name='email']").value = btn.dataset.email;
      userForm.querySelector("input[name='address']").value = btn.dataset.address || '';
      userForm.querySelector("input[name='phone']").value = btn.dataset.phone || '';
      userForm.querySelector("input[name='hireDate']").value = btn.dataset.hiredate || '';

      // ================= Role Dropdown =================
      const roleDropdown = userForm.querySelector("select[name='system_role']");
      roleDropdown.value = btn.dataset.role || "";

      // ================= Department & Position =================
      const deptValue = btn.dataset.departmentid || "";
      departmentDropdown.value = deptValue;

      // Filter positions based on department
      filterPositions(deptValue);

      const posValue = btn.dataset.positionid || "";
      positionDropdown.value = posValue;

      // Update modal UI
      document.querySelector("#userModal .modal-title").textContent = "Edit User";
      saveUserBtn.textContent = "Update";
      userModal.show();
    });
  });

  // ================= Save button =================
  saveUserBtn.addEventListener("click", () => {
    userForm.submit();
  });
});