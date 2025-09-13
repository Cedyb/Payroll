$(function () {

    // Show Add Modal
    $('#openModalBtn').on('click', function () {
        const myModal = new bootstrap.Modal(document.getElementById('myModal'));
        myModal.show();
    });

    // Delegated handler so it still works after pagination / DOM changes
    $(document).on('click', '.js-employee-update', function () {
        const $btn = $(this);
        const row = $btn.closest('tr');

        const employeeId = row.find('.emp-id').text().trim();
        const username = row.find('.emp-username').length ? row.find('.emp-username').text().trim() : '';
        const password = row.find('.emp-password').length ? row.find('.emp-password').text().trim() : '';
        const firstName = row.find('.emp-firstname').text().trim();
        const lastName = row.find('.emp-lastname').text().trim();
        const email = row.find('.emp-email').text().trim();
        const address = row.find('.emp-address').text().trim();
        const phone = row.find('.emp-phone').text().trim();
        const hireDate = row.find('.emp-hiredate').text().trim();

        // Primary source: hidden <td> values; fallback: data attributes
        let departmentId = row.find('.emp-departmentid').text().trim();
        let positionId = row.find('.emp-positionid').text().trim();

        if (!departmentId) departmentId = row.data('department-id') || $btn.data('department-id') || '';
        if (!positionId) positionId = row.data('position-id') || $btn.data('position-id') || '';

        // Set form values
        $('#employeeIdUpdate').val(employeeId);
        $('#usernameUpdate').val(username);
        $('#passwordUpdate').val(password);
        $('#firstNameUpdate').val(firstName);
        $('#lastNameUpdate').val(lastName);
        $('#emailUpdate').val(email);
        $('#addressUpdate').val(address);
        $('#phoneUpdate').val(phone);
        $('#hireDateUpdate').val(hireDate);

        // Set department first (so user sees it)
        if (departmentId) {
            $('#departmentUpdateDropdown').val(String(departmentId));
        } else {
            $('#departmentUpdateDropdown').val('');
        }

        // Populate position dropdown via AJAX and select the correct one
        getPosition(departmentId, positionId);

        const modal = new bootstrap.Modal(document.getElementById('myUpdateModal'));
        modal.show();
    });

    // Loads positions and selects the provided positionId if present
    function getPosition(departmentId, selectedPositionId = null) {
        const dropdown = $('#positionUpdateDropdown');
        dropdown.empty().append('<option value="">Select Position</option>');
        if (!departmentId) return;

        $.ajax({
            url: '/positions/retrieve',
            method: 'GET',
            data: { departmentId: departmentId }, // send dept id in case backend supports filtering
            dataType: 'json',
            success: function (data) {
                console.log('positions retrieved:', data);
                // data may be a list of position objects. Try to be defensive about structure.
                data.forEach(function (position) {
                    // robustly find department id on the position object
                    const posDeptId = (position.department && (position.department.departmentId || position.department.id)) || position.departmentId || '';
                    // if server already filtered by department this check still works
                    if (String(posDeptId) === String(departmentId) || !posDeptId) {
                        dropdown.append($('<option>', {
                            value: position.positionId,
                            text: position.title || position.name || ('Position ' + position.positionId)
                        }));
                    }
                });

                if (selectedPositionId) {
                    dropdown.val(String(selectedPositionId));
                }
            },
            error: function (xhr, status, error) {
                console.error('Error retrieving positions:', error, xhr.responseText);
            }
        });
    }

    // When Department changes in modal → reload positions
    $(document).on('change', '#departmentUpdateDropdown', function () {
        const deptId = $(this).val();
        getPosition(deptId);
    });

    // Delegated Delete
    $(document).on('click', '.js-employee-delete', function () {
        const id = $(this).data('id');
        if (confirm('Are you sure you want to delete this employee?')) {
            window.location.href = '/employees/delete/' + id;
        }
    });

});
