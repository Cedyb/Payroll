$(function () {

    $('#openModalBtn').on('click', function () {
        const myModal = new bootstrap.Modal(document.getElementById('myModal'));
        myModal.show();
    });


    $('.js-employee-update').on('click', function () {
        const row = $(this).closest('tr');
        const positionId = row.find('.emp-positionid').text().trim(); // Add this to your table

        $('#employeeIdUpdate').val(row.find('.emp-id').text().trim());
        $('#usernameUpdate').val(row.find('.emp-username').text().trim());
        $('#passwordUpdate').val(row.find('.emp-password').text().trim());
        $('#roleUpdate').val(row.find('.emp-role').text().trim());
        $('#firstNameUpdate').val(row.find('.emp-firstname').text().trim());
        $('#lastNameUpdate').val(row.find('.emp-lastname').text().trim());
        $('#emailUpdate').val(row.find('.emp-email').text().trim());
        $('#addressUpdate').val(row.find('.emp-address').text().trim());
        $('#phoneUpdate').val(row.find('.emp-phone').text().trim());
        $('#hireDateUpdate').val(row.find('.emp-hiredate').text().trim());

        // Load dropdown with current value selected
        getPosition(positionId);

        const modal = new bootstrap.Modal(document.getElementById('myUpdateModal'));
        modal.show();
        getPosition();


    });
function getPosition(selectedId = null) {
    $.ajax({
        url: '/positions/retrieve',
        method: 'GET',
        success: function (data) {
            const dropdown = $('#positionUpdateDropdown');
            dropdown.empty().append('<option value="">Select Position</option>');

            data.forEach(function (position) {
                const isSelected = selectedId && position.positionId == selectedId ? 'selected' : '';
                dropdown.append(`<option value="${position.positionId}" ${isSelected}>${position.title}</option>`);
            });
        },
        error: function (xhr, status, error) {
            console.error("Error retrieving positions:", error);
        }
    });
}

    $('.js-employee-delete').on('click', function () {
        const id = $(this).data('id');
        window.location.href = '/employees/delete/' + id;
    });
});
