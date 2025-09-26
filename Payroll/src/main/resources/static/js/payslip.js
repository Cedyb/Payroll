    document.addEventListener("DOMContentLoaded", function () {
        const toggleBtn = document.getElementById("toggleEditBtn");
        const editableInputs = document.querySelectorAll(".editable-input");

        const earningsFields = ["basicPay", "otPay", "leavePay", "regularHolidayPay", "specialHolidayPay", "colaAllowance", "allowance", "adjustment"];
        const deductionsFields = ["savings", "sss", "philhealth", "pagibig", "canteen", "cashAdvance", "medical", "insurance", "utilities"];

        const totalEarningsInput = document.querySelector("input[name='subtotal']");
        const totalDeductionsInput = document.querySelector("input[name='totalDeductions']");
        const netPayInput = document.querySelector("input[name='netPay']");

        function computeTotals() {
            let earningsTotal = 0;
            let deductionsTotal = 0;

            earningsFields.forEach(name => {
                const field = document.querySelector(`input[name='${name}']`);
                if (field) earningsTotal += parseFloat(field.value) || 0;
            });

            deductionsFields.forEach(name => {
                const field = document.querySelector(`input[name='${name}']`);
                if (field) deductionsTotal += parseFloat(field.value) || 0;
            });


            totalEarningsInput.value = earningsTotal.toFixed(2);
            totalDeductionsInput.value = deductionsTotal.toFixed(2);
            netPayInput.value = (earningsTotal - deductionsTotal).toFixed(2);
        }

        editableInputs.forEach(input => {
            input.addEventListener("input", computeTotals);
        });

        toggleBtn.addEventListener("click", function () {
            const isDisabled = editableInputs[0].disabled;
            editableInputs.forEach(input => input.disabled = !isDisabled);
            toggleBtn.textContent = isDisabled ? "Disable Edit Mode" : "Enable Edit Mode";
            toggleBtn.classList.toggle("btn-success", isDisabled);
            toggleBtn.classList.toggle("btn-primary", !isDisabled);
        });

        computeTotals();
    });
