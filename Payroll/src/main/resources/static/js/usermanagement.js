document.addEventListener("DOMContentLoaded", function() {
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

  // Add User
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

  // Edit User
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
});
