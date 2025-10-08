document.addEventListener("DOMContentLoaded", function () {

    // ==========================
    // Utility: Create checkbox with remove button
    // ==========================
    function createCheckbox(containerId, type, id, name, checked = false, removable = true) {
        const container = document.getElementById(containerId);
        if (!container) return;

        const div = document.createElement("div");
        div.classList.add("form-check", "d-flex", "align-items-center", "justify-content-between", "mb-1");

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
    ["earningsContainer", "deductionsContainer"].forEach(containerId => {
        const type = containerId.includes("earnings") ? "EARNING" : "DEDUCTION";
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
    });

    // ==========================
    // Open modal for Edit (Earning/Deduction)
    // ==========================
    document.querySelectorAll(".edit-btn").forEach(btn => {
        btn.addEventListener("click", function () {
            const type = this.getAttribute("data-type");
            const id = this.getAttribute("data-id");
            if (!type || !id) return console.error("Missing type or id for edit");

            const modalId = type === "EARNING" ? "addEarningModal" : "addDeductionModal";
            const modalEl = document.getElementById(modalId);
            const modal = new bootstrap.Modal(modalEl);

            const form = modalEl.querySelector("form");
            const nameInput = form.querySelector("input[name='name']");
            const descInput = form.querySelector("[name='description']");
            const calcTypeSelect = form.querySelector("select[name='calcType']");
            const valueInput = form.querySelector("input[name='value']");
            const startTimeInput = form.querySelector("input[name='startTime']");
            const endTimeInput = form.querySelector("input[name='endTime']");
            const idInput = form.querySelector("input[type='hidden']");

            fetch(`/settings/get/${type.toLowerCase()}/${id}`)
                .then(res => res.json())
                .then(data => {
                    nameInput.value = data.name || "";
                    descInput.value = data.description || "";
                    calcTypeSelect.value = data.calculationType || "";
                    valueInput.value = data.value != null ? data.value : "";
                    if (startTimeInput) startTimeInput.value = data.startTime || "";
                    if (endTimeInput) endTimeInput.value = data.endTime || "";
                    idInput.value = data.id;

                    modalEl.querySelector(".modal-title").textContent = `Edit ${type.charAt(0) + type.slice(1).toLowerCase()}`;
                    modal.show();
                })
                .catch(err => console.error("Error fetching data for edit:", err));
        });
    });

    // ==========================
    // Submit Add/Edit via AJAX
    // ==========================
    ["addEarningModal", "addDeductionModal"].forEach(modalId => {
        const modalEl = document.getElementById(modalId);
        if (!modalEl) return;
        const form = modalEl.querySelector("form");

        form.addEventListener("submit", function (e) {
            e.preventDefault();
            const type = modalId === "addEarningModal" ? "EARNING" : "DEDUCTION";
            const id = this.querySelector("input[type='hidden']").value;

            const payload = {
                name: this.querySelector("[name='name']").value.trim(),
                description: this.querySelector("[name='description']").value.trim(),
                calculationType: this.querySelector("[name='calcType']").value,
                value: this.querySelector("[name='value']").value,
                startTime: this.querySelector("[name='startTime']") ? this.querySelector("[name='startTime']").value : null,
                endTime: this.querySelector("[name='endTime']") ? this.querySelector("[name='endTime']").value : null
            };

            const endpoint = id
                ? `/settings/update-${type.toLowerCase()}/${id}`
                : `/settings/add-${type.toLowerCase()}-ajax`;

            fetch(endpoint, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            })
                .then(res => res.json())
                .then(data => {
                    if (!data || !data.id) return console.error("Invalid response from server");

                    const containerId = type === "EARNING" ? "earningsContainer" : "deductionsContainer";
                    const existing = document.getElementById(`${type}_${data.id}`);
                    if (existing) {
                        const label = existing.nextElementSibling;
                        if (label) label.textContent = data.name;
                    } else {
                        createCheckbox(containerId, type, data.id, data.name);
                    }

                    bootstrap.Modal.getInstance(modalEl).hide();
                    form.reset();
                    form.querySelector("input[type='hidden']").value = "";
                })
                .catch(err => console.error(err));
        });
    });

    // ==========================
    // Edit Payslip Configuration Modal
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
                    const earningsContainer = document.getElementById("editEarningsContainer");
                    const deductionsContainer = document.getElementById("editDeductionsContainer");

                    if (!earningsContainer || !deductionsContainer) {
                        console.error('Earnings or Deductions container not found');
                        return;
                    }

                    earningsContainer.innerHTML = "";
                    deductionsContainer.innerHTML = "";

                    const selectedEarnings = data.earnings.map(Number);
                    data.allEarnings.forEach(e => {
                        createCheckbox(
                            "editEarningsContainer",
                            "EARNING",
                            e.id,
                            e.name,
                            selectedEarnings.includes(Number(e.id))
                        );
                    });

                    const selectedDeductions = data.deductions.map(Number);
                    data.allDeductions.forEach(d => {
                        createCheckbox(
                            "editDeductionsContainer",
                            "DEDUCTION",
                            d.id,
                            d.name,
                            selectedDeductions.includes(Number(d.id))
                        );
                    });

                    document.getElementById("editConfigId").value = data.id;
                    document.getElementById("editConfigPosition").value = data.positionTitle;

                    const modalEl = document.getElementById("editConfigModal");
                    if (!modalEl) return console.error('Edit Modal element not found');
                    const modal = new bootstrap.Modal(modalEl);
                    modal.show();
                })
                .catch(err => console.error('Error fetching config:', err));
        });
    });

    const editForm = document.getElementById("editConfigForm");
    if (editForm) {
        editForm.addEventListener("submit", function (e) {
            e.preventDefault();
            const configId = document.getElementById("editConfigId").value;

            const earnings = Array.from(document.querySelectorAll("#editEarningsContainer input[type='checkbox']:checked")).map(cb => cb.value);
            const deductions = Array.from(document.querySelectorAll("#editDeductionsContainer input[type='checkbox']:checked")).map(cb => cb.value);

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
                    } else {
                        alert("Failed to update configuration.");
                    }
                })
                .catch(err => console.error(err));
        });
    }

    // ==========================
    // CHANGE PASSWORD
    // ==========================
    const changePasswordForm = document.getElementById("changePasswordForm");
    const passwordFeedback = document.getElementById("passwordFeedback");
    if (changePasswordForm) {
        changePasswordForm.addEventListener("submit", async function (e) {
            e.preventDefault();
            const currentPassword = document.getElementById("currentPassword").value.trim();
            const newPassword = document.getElementById("newPassword").value.trim();
            const confirmPassword = document.getElementById("confirmPassword").value.trim();

            passwordFeedback.textContent = "";
            if (!currentPassword || !newPassword || !confirmPassword) return passwordFeedback.textContent = "All fields are required.";

            if (newPassword !== confirmPassword) return passwordFeedback.textContent = "New passwords do not match.";

            try {
                const res = await fetch("/settings/update-credentials", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ currentPassword, newPassword })
                });
                const result = await res.json();
                if (result.success) {
                    passwordFeedback.textContent = "Password changed successfully!";
                    changePasswordForm.reset();
                } else {
                    passwordFeedback.textContent = result.message || "Current password incorrect.";
                }
            } catch (err) {
                passwordFeedback.textContent = "Error occurred. Try again.";
            }
        });
    }

    // ==========================
    // CHANGE EMAIL
    // ==========================
    const changeEmailForm = document.getElementById("changeEmailForm");
    const emailFeedback = document.getElementById("emailFeedback");
    if (changeEmailForm) {
        changeEmailForm.addEventListener("submit", async function (e) {
            e.preventDefault();
            const currentPassword = document.getElementById("currentPasswordForEmail").value.trim();
            const newEmail = document.getElementById("newEmail").value.trim();
            const confirmEmail = document.getElementById("confirmEmail").value.trim();

            emailFeedback.textContent = "";
            if (!currentPassword || !newEmail || !confirmEmail) return emailFeedback.textContent = "All fields are required.";
            if (newEmail !== confirmEmail) return emailFeedback.textContent = "New emails do not match.";

            try {
                const res = await fetch("/settings/update-credentials", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ currentPassword, newEmail })
                });
                const result = await res.json();
                emailFeedback.textContent = result.success ? "Email changed successfully!" : (result.message || "Current password incorrect.");
            } catch (err) {
                emailFeedback.textContent = "Error occurred. Try again.";
                console.error(err);
            }
        });
    }

});
