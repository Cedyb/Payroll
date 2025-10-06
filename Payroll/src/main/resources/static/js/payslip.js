document.addEventListener("DOMContentLoaded", function () {
    const toggleBtn = document.getElementById("toggleEditBtn");
    const basicPayInput = document.querySelector("input[name='basicPay']");
    const totalEarningsInput = document.querySelector("input[name='subtotal']");
    const totalDeductionsInput = document.querySelector("input[name='totalDeductions']");
    const netPayInput = document.querySelector("input[name='netPay']");

    // Get all editable earnings and deductions inputs
    function getDynamicFields() {
        const editableInputs = Array.from(document.querySelectorAll('input.editable-input'));
        const earningsFields = editableInputs.filter(input => input.name.startsWith('earning_'));
        const deductionsFields = editableInputs.filter(input => input.name.startsWith('deduction_'));
        return { earningsFields, deductionsFields };
    }

    // Compute totals including Basic Pay
    function computeTotals() {
        const { earningsFields, deductionsFields } = getDynamicFields();

        // Basic Pay is included in earnings total
        let earningsTotal = parseFloat(basicPayInput?.value || 0);
        let deductionsTotal = 0;

        earningsFields.forEach(field => {
            earningsTotal += parseFloat(field.value || 0);
        });

        deductionsFields.forEach(field => {
            deductionsTotal += parseFloat(field.value || 0);
        });

        totalEarningsInput.value = earningsTotal.toFixed(2);
        totalDeductionsInput.value = deductionsTotal.toFixed(2);
        netPayInput.value = (earningsTotal - deductionsTotal).toFixed(2);
    }

    // Disable all editable inputs on page load
    const editableInputs = Array.from(document.querySelectorAll(".editable-input"));
    editableInputs.forEach(input => input.disabled = true);

    // Enable/disable edit mode for editable inputs
    toggleBtn.addEventListener("click", function () {
        const isDisabled = editableInputs[0].disabled;

        editableInputs.forEach(input => input.disabled = !isDisabled);

        toggleBtn.textContent = isDisabled ? "Disable Edit Mode" : "Enable Edit Mode";
        toggleBtn.classList.toggle("btn-success", isDisabled);
        toggleBtn.classList.toggle("btn-primary", !isDisabled);
    });

    // Add input event listener to all editable fields AND Basic Pay
    function addInputListeners() {
        editableInputs.forEach(input => input.addEventListener("input", computeTotals));

        if (basicPayInput) {
            basicPayInput.addEventListener("input", computeTotals);
        }
    }

    // Initial setup
    addInputListeners();
    computeTotals();
});
