  document.addEventListener("DOMContentLoaded", function() {
    // Add Earning
    const addEarningForm = document.querySelector("#addEarningModal form");
    addEarningForm.addEventListener("submit", function(e) {
      e.preventDefault();
      const name = addEarningForm.querySelector('input[name="name"]').value;
      if(name) {
        const container = document.getElementById("earningsContainer");

        const div = document.createElement("div");
        div.classList.add("form-check", "d-flex", "align-items-center", "mb-1");

        const input = document.createElement("input");
        input.type = "checkbox";
        input.classList.add("form-check-input");
        input.name = "earnings";
        input.id = "earning_" + name;
        input.value = name;

        const label = document.createElement("label");
        label.classList.add("form-check-label", "ms-2");
        label.htmlFor = "earning_" + name;
        label.textContent = name;

        const removeBtn = document.createElement("button");
        removeBtn.type = "button";
        removeBtn.classList.add("btn", "btn-sm", "btn-danger", "ms-auto");
        removeBtn.textContent = "X";
        removeBtn.addEventListener("click", () => div.remove());

        div.appendChild(input);
        div.appendChild(label);
        div.appendChild(removeBtn);
        container.appendChild(div);

        addEarningForm.reset();
        bootstrap.Modal.getInstance(document.getElementById('addEarningModal')).hide();
      }
    });

    // Add Deduction
    const addDeductionForm = document.querySelector("#addDeductionModal form");
    addDeductionForm.addEventListener("submit", function(e) {
      e.preventDefault();
      const name = addDeductionForm.querySelector('input[name="name"]').value;
      if(name) {
        const container = document.getElementById("deductionsContainer");

        const div = document.createElement("div");
        div.classList.add("form-check", "d-flex", "align-items-center", "mb-1");

        const input = document.createElement("input");
        input.type = "checkbox";
        input.classList.add("form-check-input");
        input.name = "deductions";
        input.id = "deduction_" + name;
        input.value = name;

        const label = document.createElement("label");
        label.classList.add("form-check-label", "ms-2");
        label.htmlFor = "deduction_" + name;
        label.textContent = name;

        const removeBtn = document.createElement("button");
        removeBtn.type = "button";
        removeBtn.classList.add("btn", "btn-sm", "btn-danger", "ms-auto");
        removeBtn.textContent = "X";
        removeBtn.addEventListener("click", () => div.remove());

        div.appendChild(input);
        div.appendChild(label);
        div.appendChild(removeBtn);
        container.appendChild(div);

        addDeductionForm.reset();
        bootstrap.Modal.getInstance(document.getElementById('addDeductionModal')).hide();
      }
    });
  });