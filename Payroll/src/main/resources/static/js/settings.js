document.addEventListener("DOMContentLoaded", function () {

  // ==========================
  // Function to create checkbox with remove button
  // ==========================
  function createCheckbox(containerId, type, id, name) {
    const container = document.getElementById(containerId);
    const div = document.createElement("div");
    div.classList.add("form-check", "d-flex", "align-items-center", "mb-1", "justify-content-between");

    const leftDiv = document.createElement("div");
    leftDiv.classList.add("d-flex", "align-items-center");

    const input = document.createElement("input");
    input.type = "checkbox";
    input.classList.add("form-check-input");
    input.name = type;
    input.id = `${type}_${id}`;
    input.value = id;

    const label = document.createElement("label");
    label.classList.add("form-check-label", "ms-2");
    label.htmlFor = input.id;
    label.textContent = name;

    leftDiv.appendChild(input);
    leftDiv.appendChild(label);

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
        else console.error("Failed to deactivate " + type);
      })
      .catch(err => console.error(err));
    });

    div.appendChild(leftDiv);
    div.appendChild(removeBtn);
    container.appendChild(div);
  }

  // ==========================
  // Add remove buttons to existing checkboxes on page load
  // ==========================
  function addRemoveButtons(containerId, type) {
    const container = document.getElementById(containerId);
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

  addRemoveButtons("earningsContainer", "EARNING");
  addRemoveButtons("deductionsContainer", "DEDUCTION");

  // ==========================
  // Add Earning via AJAX
  // ==========================
  const addEarningForm = document.querySelector("#addEarningModal form");
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

  // ==========================
  // Add Deduction via AJAX
  // ==========================
  const addDeductionForm = document.querySelector("#addDeductionModal form");
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

});
