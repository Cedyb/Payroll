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
    // Unified modal edit handler
    // ==========================
    function openEditModal(type, id, row) {
        const modalId = type === "EARNING" ? "addEarningModal" : "addDeductionModal";
        const modalEl = document.getElementById(modalId);
        if (!modalEl) return;
        const modal = new bootstrap.Modal(modalEl);
        const form = modalEl.querySelector("form");

        // Modal inputs
        const nameInput = form.querySelector("input[name='name']");
        const descInput = form.querySelector("[name='description']");
        const calcTypeSelect = form.querySelector("select[name='calcType']");
        const valueInput = form.querySelector("input[name='value']");
        const startTimeInput = form.querySelector("input[name='startTime']");
        const endTimeInput = form.querySelector("input[name='endTime']");
        const idInput = form.querySelector("input[type='hidden']");

        if (row) {
            nameInput.value = row.querySelector(".name")?.textContent.trim() || row.children[1]?.textContent.trim() || "";
            descInput.value = row.querySelector(".description")?.textContent.trim() || row.children[6]?.textContent.trim() || "";
            calcTypeSelect.value = row.querySelector(".calcType")?.textContent.trim() || row.children[2]?.textContent.trim() || "";
            valueInput.value = row.querySelector(".value")?.textContent.trim() || row.children[3]?.textContent.trim() || "";
            if (startTimeInput) startTimeInput.value = row.querySelector(".startTime")?.textContent.trim() || row.children[4]?.textContent.trim() || "";
            if (endTimeInput) endTimeInput.value = row.querySelector(".endTime")?.textContent.trim() || row.children[5]?.textContent.trim() || "";
        }

        idInput.value = id;
        modalEl.querySelector(".modal-title").textContent = `Edit ${type.charAt(0) + type.slice(1).toLowerCase()}`;
        modal.show();
    }

    document.querySelectorAll(".edit-btn, #earningsDeductionTable button.btn-warning").forEach(btn => {
        btn.addEventListener("click", function () {
            const type = btn.dataset.type || btn.getAttribute("data-type");
            const id = btn.dataset.id || btn.getAttribute("data-id");
            const row = btn.closest("tr");
            openEditModal(type, id, row);
        });
    });

    ["addEarningModal", "addDeductionModal"].forEach(modalId => {
        const modalEl = document.getElementById(modalId);
        if (!modalEl) return;
        const form = modalEl.querySelector("form");

        form.addEventListener("submit", function (e) {
            e.preventDefault();
            const type = modalId === "addEarningModal" ? "EARNING" : "DEDUCTION";
            const id = form.querySelector("input[type='hidden']").value;

            const payload = {
                name: form.querySelector("[name='name']").value.trim(),
                description: form.querySelector("[name='description']").value.trim(),
                calculationType: form.querySelector("[name='calcType']").value,
                value: form.querySelector("[name='value']")?.value || "",
                startTime: form.querySelector("[name='startTime']")?.value || null,
                endTime: form.querySelector("[name='endTime']")?.value || null
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
                if (!data || (!data.id && !data.success)) return alert("Failed to save item.");

                // Add new checkbox if adding
                if (!id && data.id) {
                    const containerId = type === "EARNING" ? "earningsContainer" : "deductionsContainer";
                    createCheckbox(containerId, type, data.id, data.name);
                }

                // Update table row if editing
                if (id && data.success) {
                    const row = document.querySelector(`#${type}_${id}`)?.closest("tr");
                    if (row) {
                        row.children[1].textContent = payload.name;
                        row.children[2].textContent = payload.calculationType;
                        row.children[3].textContent = payload.value;
                        if (type === "EARNING") {
                            row.children[4].textContent = payload.startTime;
                            row.children[5].textContent = payload.endTime;
                        }
                        row.children[6].textContent = payload.description;
                    }
                }

                bootstrap.Modal.getInstance(modalEl).hide();
                form.reset();
                form.querySelector("input[type='hidden']").value = "";
            })
            .catch(err => console.error(err));
        });
    });


        // ==========================
        // Delete row
        // ==========================
        document.querySelectorAll(".delete-btn").forEach(btn => {
            btn.addEventListener("click", function () {
                const type = btn.dataset.type; // "EARNING" or "DEDUCTION"
                const id = btn.dataset.id;

                if (!confirm("Are you sure you want to delete this item?")) return;

                fetch(`/settings/delete/${type}/${id}`, {
                    method: "POST"
                })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        const row = btn.closest("tr");
                        if (row) row.remove();
                    } else {
                        alert("Failed to delete item: " + (data.message || ""));
                    }
                })
                .catch(err => console.error(err));
            });
        });

    // ==========================
    // Payslip Config Edit Modal
    // ==========================
    document.querySelectorAll(".edit-config-btn").forEach(btn => {
        btn.addEventListener("click", function () {
            const configId = btn.dataset.configId;
            if (!configId) return console.error('Config ID not found');

            fetch(`/settings/config/get/${configId}`)
                .then(res => res.json())
                .then(data => {
                    const earningsContainer = document.getElementById("editEarningsContainer");
                    const deductionsContainer = document.getElementById("editDeductionsContainer");
                    earningsContainer.innerHTML = "";
                    deductionsContainer.innerHTML = "";

                    data.allEarnings.forEach(e => createCheckbox("editEarningsContainer", "EARNING", e.id, e.name, data.earnings.includes(Number(e.id))));
                    data.allDeductions.forEach(d => createCheckbox("editDeductionsContainer", "DEDUCTION", d.id, d.name, data.deductions.includes(Number(d.id))));

                    document.getElementById("editConfigId").value = data.id;
                    document.getElementById("editConfigPosition").value = data.positionTitle;

                    new bootstrap.Modal(document.getElementById("editConfigModal")).show();
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
