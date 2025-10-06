document.addEventListener("DOMContentLoaded", function () {

  // ==========================
  // Utility: Create checkbox with remove button
  // ==========================
  function createCheckbox(containerId, type, id, name, checked = false, removable = true) {
      const container = document.getElementById(containerId);
      if (!container) return;

      const div = document.createElement("div");
      div.classList.add("form-check", "d-flex", "align-items-center", "justify-content-between", "mb-1");

      // Left side: checkbox + label
      const leftDiv = document.createElement("div");
      leftDiv.classList.add("d-flex", "align-items-center");

      const input = document.createElement("input");
      input.type = "checkbox";
      input.classList.add("form-check-input");
      input.name = type.toLowerCase();
      input.id = `${type}_${id}`;
      input.value = id;
      if (checked) input.checked = true;

      const label = document.createElement("label");
      label.classList.add("form-check-label", "ms-2");
      label.htmlFor = input.id;
      label.textContent = name;

      leftDiv.appendChild(input);
      leftDiv.appendChild(label);
      div.appendChild(leftDiv);

      // Only add remove button if removable
      if (removable) {
          const removeBtn = document.createElement("button");
          removeBtn.type = "button";
          removeBtn.classList.add("btn", "btn-sm", "btn-danger", "remove-btn");
          removeBtn.textContent = "X";
          removeBtn.addEventListener("click", () => {
              fetch(`/settings/deactivate/${type}/${id}`, {
                  method: "POST",
                  headers: { "Content-Type": "application/json" },
                  body: JSON.stringify({ active: false })
              })
              .then(res => res.json())
              .then(data => {
                  if (data.success) div.remove();
              })
              .catch(err => console.error(err));
          });
          div.appendChild(removeBtn);
      }
      container.appendChild(div);
  }

  // ==========================
  // Initialize remove buttons for existing items
  // ==========================
  function addRemoveButtons(containerId, type) {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.querySelectorAll(".form-check").forEach(div => {
      if (!div.querySelector(".remove-btn")) {
        const input = div.querySelector("input");
        const label = div.querySelector("label");

        const leftDiv = document.createElement("div");
        leftDiv.classList.add("d-flex", "align-items-center");
        leftDiv.appendChild(input);
        leftDiv.appendChild(label);

        const id = input.value;
        const removeBtn = document.createElement("button");
        removeBtn.type = "button";
        removeBtn.classList.add("btn", "btn-sm", "btn-danger", "remove-btn");
        removeBtn.textContent = "X";
        removeBtn.addEventListener("click", () => {
          fetch(`/settings/deactivate/${type}/${id}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ active: false })
          })
          .then(res => res.json())
          .then(data => {
            if (data.success) div.remove();
          })
          .catch(err => console.error(err));
        });

        div.innerHTML = "";
        div.classList.add("d-flex", "align-items-center", "justify-content-between");
        div.appendChild(leftDiv);
        div.appendChild(removeBtn);
      }
    });
  }

  // Apply remove buttons on load
  addRemoveButtons("earningsContainer", "EARNING");
  addRemoveButtons("deductionsContainer", "DEDUCTION");

  // ==========================
  // Add Earning via AJAX
  // ==========================
  const addEarningForm = document.querySelector("#addEarningModal form");
  if (addEarningForm) {
    addEarningForm.addEventListener("submit", function (e) {
      e.preventDefault();
      const name = this.querySelector('input[name="name"]').value.trim();
      const description = this.querySelector('input[name="description"]').value.trim();
      if (!name) return;

      fetch("/settings/add-earning-ajax", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, description }),
      })
      .then(res => res.json())
      .then(data => {
        createCheckbox("earningsContainer", "EARNING", data.id, data.name);
        bootstrap.Modal.getInstance(document.getElementById("addEarningModal")).hide();
        addEarningForm.reset();
      })
      .catch(err => console.error(err));
    });
  }

  // ==========================
  // Add Deduction via AJAX
  // ==========================
  const addDeductionForm = document.querySelector("#addDeductionModal form");
  if (addDeductionForm) {
    addDeductionForm.addEventListener("submit", function (e) {
      e.preventDefault();
      const name = this.querySelector('input[name="name"]').value.trim();
      const description = this.querySelector('textarea[name="description"]').value.trim();
      if (!name) return;

      fetch("/settings/add-deduction-ajax", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, description }),
      })
      .then(res => res.json())
      .then(data => {
        createCheckbox("deductionsContainer", "DEDUCTION", data.id, data.name);
        bootstrap.Modal.getInstance(document.getElementById("addDeductionModal")).hide();
        addDeductionForm.reset();
      })
      .catch(err => console.error(err));
    });
  }

  // ==========================
  // Edit Payslip Configuration
  // ==========================
  document.querySelectorAll(".edit-config-btn").forEach(btn => {
    btn.addEventListener("click", function () {
      const configId = this.getAttribute("data-config-id");
      if (!configId) return console.error('Config ID not found');

      fetch(`/settings/config/get/${configId}`)
        .then(res => {
          if (!res.ok) throw new Error('Network response was not ok');
          return res.json();
        })
        .then(data => {
          document.getElementById("editConfigId").value = data.id;
          document.getElementById("editConfigPosition").value = data.positionTitle;

          const earningsContainer = document.getElementById("editEarningsContainer");
          const deductionsContainer = document.getElementById("editDeductionsContainer");
          earningsContainer.innerHTML = "";
          deductionsContainer.innerHTML = "";

          data.allEarnings.forEach(e => {
            createCheckbox("editEarningsContainer", "EARNING", e.id, e.name, data.earnings.includes(e.id));
          });

          data.allDeductions.forEach(d => {
            createCheckbox("editDeductionsContainer", "DEDUCTION", d.id, d.name, data.deductions.includes(d.id));
          });

          const modalEl = document.getElementById("editConfigModal");
          if (!modalEl) return console.error('Edit Modal element not found');
          const modal = new bootstrap.Modal(modalEl);
          modal.show();
        })
        .catch(err => console.error('Error fetching config:', err));
    });
  });

  // ==========================
  // Save Edited Configuration
  // ==========================
  const editForm = document.getElementById("editConfigForm");
  if (editForm) {
    editForm.addEventListener("submit", function (e) {
      e.preventDefault();
      const configId = document.getElementById("editConfigId").value;

      // Collect selected earnings and deductions
      const earnings = Array.from(document.querySelectorAll("#editEarningsContainer input[type='checkbox']:checked"))
                            .map(cb => cb.value);
      const deductions = Array.from(document.querySelectorAll("#editDeductionsContainer input[type='checkbox']:checked"))
                              .map(cb => cb.value);

      // Send to server
      fetch(`/settings/config/update/${configId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ earnings, deductions })
      })
      .then(res => res.json())
      .then(data => {
        if (data.success) {
          bootstrap.Modal.getInstance(document.getElementById("editConfigModal")).hide();
          alert("Configuration updated successfully!");
          // Optionally: refresh the table or update the row dynamically
        } else {
          alert("Failed to update configuration.");
        }
      })
      .catch(err => console.error(err));
    });
  }

});
