$(document).ready(function () {

    // ==========================
    // Open Add Employee Modal
    // ==========================
    $('#openModalBtn').on('click', function () {
        const myModal = new bootstrap.Modal(document.getElementById('myModal'));
        myModal.show();
    });

    // ==========================
    // Retrieve positions based on department
    // ==========================
    function getPositions(departmentId, dropdownSelector, selectedPositionId = null, includeAll = false) {
        const dropdown = $(dropdownSelector);
        dropdown.empty();

        if (includeAll) {
            dropdown.append('<option value="">All Positions</option>');
        } else {
            dropdown.append('<option value="">Select Position</option>');
        }

        if (!departmentId) return;

        $.ajax({
            url: '/positions/retrieve',
            method: 'GET',
            data: { departmentId: departmentId },
            dataType: 'json',
            success: function (data) {
                data.forEach(function (position) {
                    const posDeptId = (position.department && (position.department.departmentId || position.department.id))
                        || position.departmentId || '';

                    if (String(posDeptId) === String(departmentId)) {
                        dropdown.append($('<option>', {
                            value: position.positionId,
                            text: position.title || position.name || ('Position ' + position.positionId)
                        }));
                    }
                });

                if (selectedPositionId) dropdown.val(String(selectedPositionId));
            }
        });
    }

    // ==========================
    // Cascading dropdowns
    // ==========================
    $('#departmentAddDropdown').on('change', function () {
        getPositions($(this).val(), '#positionAddDropdown');
    });

    $('#departmentUpdateDropdown').on('change', function () {
        getPositions($(this).val(), '#positionUpdateDropdown');
    });

    $('#searchDepartment').on('change', function () {
        const deptId = $(this).val();
        getPositions(deptId, '#searchPosition', null, true); // include "All Positions"
        if (!deptId) $('#searchPosition').val(''); // reset position if no department selected
        filterEmployees(); // live filter
    });

    $('#searchPosition').on('change', function () {
        filterEmployees(); // live filter
    });

    $('#searchName').on('input', function () {
        filterEmployees(); // live filter
    });

    // ==========================
    // Open Update Employee Modal
    // ==========================
    $(document).on('click', '.js-employee-update', function () {
        const row = $(this).closest('tr');

        $('#employeeIdUpdate').val(row.find('.emp-id').text().trim());
        $('#usernameUpdate').val(row.find('.emp-username').text().trim());
        $('#passwordUpdate').val('');
        $('#firstNameUpdate').val(row.find('.emp-firstname').text().trim());
        $('#lastNameUpdate').val(row.find('.emp-lastname').text().trim());
        $('#emailUpdate').val(row.find('.emp-email').text().trim());
        $('#addressUpdate').val(row.find('.emp-address').text().trim());
        $('#phoneUpdate').val(row.find('.emp-phone').text().trim());
        $('#hireDateUpdate').val(row.find('.emp-hiredate').text().trim());

        const departmentId = row.find('.emp-departmentid').attr('data-id') || '';
        const positionId = row.find('.emp-positionid').attr('data-id') || '';
        const systemRole = row.find('.emp-system-role').text().trim();

        $('#departmentUpdateDropdown').val(departmentId);
        getPositions(departmentId, '#positionUpdateDropdown', positionId);
        $('#systemRoleUpdate').val(systemRole);

        const modal = new bootstrap.Modal(document.getElementById('myUpdateModal'));
        modal.show();
    });

    // ==========================
    // Delete Employee
    // ==========================
    $(document).on('click', '.js-employee-delete', function () {
        const id = $(this).data('id');
        if (confirm('Are you sure you want to delete this employee?')) {
            window.location.href = '/employees/delete/' + id;
        }
    });

    // ==========================
    // Employee Filtering Function
    // ==========================
    function filterEmployees() {
        const name = $('#searchName').val().toLowerCase().trim();
        const deptId = $('#searchDepartment').val();
        const posId = $('#searchPosition').val();

        $('table tbody tr').each(function () {
            const row = $(this);

            const rowName = (row.find('.emp-firstname').text() + " " + row.find('.emp-lastname').text()).toLowerCase().trim();
            const rowDept = row.find('.emp-departmentid').attr('data-id') || '';
            const rowPos = row.find('.emp-positionid').attr('data-id') || '';

            const matchesName = name === '' || rowName.includes(name);
            const matchesDept = deptId === '' || rowDept === deptId;
            const matchesPos = posId === '' || rowPos === posId;

            if (matchesName && matchesDept && matchesPos) {
                row.show();
            } else {
                row.hide();
            }
        });
    }
});