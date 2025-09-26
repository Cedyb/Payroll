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

  document.getElementById("addUserBtn").addEventListener("click", () => {
    userForm.reset();
    userForm.action = "/usermanagement/create";
    userForm.method = "post";
    document.querySelector("#userModal .modal-title").textContent = "Add User";
    saveUserBtn.textContent = "Save";
  });

  document.querySelectorAll(".js-edit-user").forEach(btn => {
    btn.addEventListener("click", () => {
      userForm.reset();
      userForm.action = `/usermanagement/${btn.dataset.id}/update`;
      userForm.method = "post";

      userForm.querySelector("input[name='id']").value = btn.dataset.id;
      userForm.querySelector("input[name='firstName']").value = btn.dataset.firstname;
      userForm.querySelector("input[name='lastName']").value = btn.dataset.lastname;
      userForm.querySelector("input[name='email']").value = btn.dataset.email;
      userForm.querySelector("input[name='address']").value = btn.dataset.address || '';
      userForm.querySelector("input[name='phone']").value = btn.dataset.phone || '';
      userForm.querySelector("select[name='system_role']").value = btn.dataset.role;

      if (btn.dataset.positionid) {
        userForm.querySelector("select[name='positionId']").value = btn.dataset.positionid;
      }
      if (btn.dataset.departmentid) {
        userForm.querySelector("select[name='departmentId']").value = btn.dataset.departmentid;
      }
      if (btn.dataset.hiredate) {
        userForm.querySelector("input[name='hireDate']").value = btn.dataset.hiredate;
      }

      document.querySelector("#userModal .modal-title").textContent = "Edit User";
      saveUserBtn.textContent = "Update";
      userModal.show();
    });
  });

  saveUserBtn.addEventListener("click", () => {
    userForm.submit();
  });
});
