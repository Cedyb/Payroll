$(document).ready(function () {


    $('#openModalBtn').on('click', function () {
        const myModal = new bootstrap.Modal(document.getElementById('myModal'));
        myModal.show();
    });


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


    $('#departmentAddDropdown').on('change', function () {
        getPositions($(this).val(), '#positionAddDropdown');
    });


    $('#departmentUpdateDropdown').on('change', function () {
        getPositions($(this).val(), '#positionUpdateDropdown');
    });


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

        const departmentId = row.find('.emp-departmentid').data('id')?.toString() || '';
        const positionId = row.find('.emp-positionid').data('id')?.toString() || '';
        const systemRole = row.find('.emp-system-role').text().trim();

        $('#departmentUpdateDropdown').val(departmentId);
        getPositions(departmentId, '#positionUpdateDropdown', positionId);
        $('#systemRoleUpdate').val(systemRole);

        const modal = new bootstrap.Modal(document.getElementById('myUpdateModal'));
        modal.show();
    });


    $(document).on('click', '.js-employee-delete', function () {
        const id = $(this).data('id');
        if (confirm('Are you sure you want to delete this employee?')) {
            window.location.href = '/employees/delete/' + id;
        }
    });


    $('#searchDepartment').on('change', function () {
        const deptId = $(this).val();
        getPositions(deptId, '#searchPosition', null, true); // include "All Positions"
    });


    $('#searchBtn').on('click', function () {
        const name = $('#searchName').val().toLowerCase().trim();
        const deptId = $('#searchDepartment').val();
        const posId = $('#searchPosition').val();

        $('table tbody tr').each(function () {
            const row = $(this);
            const rowName = (row.find('.emp-firstname').text() + " " + row.find('.emp-lastname').text()).toLowerCase().trim();
            const rowDept = row.find('.emp-departmentid').data('id')?.toString() || '';
            const rowPos = row.find('.emp-positionid').data('id')?.toString() || '';

            if ((rowName.includes(name) || name === '') &&
                (rowDept === deptId || deptId === '') &&
                (rowPos === posId || posId === '')) {
                row.show();
            } else {
                row.hide();
            }
        });
    });


    $('#searchName, #searchDepartment, #searchPosition').on('input change', function () {
        if ($('#searchName').val() === '' &&
            $('#searchDepartment').val() === '' &&
            $('#searchPosition').val() === '') {
            $('table tbody tr').show();
        }
    });
});