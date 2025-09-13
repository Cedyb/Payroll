$(document).ready(function () {

    // Update handler: populate fields, then open modal
    $('.js-positions-update').on('click', function (e) {
        e.preventDefault();

        // Use attr(...) to avoid any subtle jQuery data-name mapping issues
        const id = $(this).attr('data-id');
        const deptId = $(this).attr('data-deptid');
        const title = $(this).attr('data-title');
        const hourlyRate = $(this).attr('data-hourly-rate');

        $('#positionIdUpdate').val(id || '');
        $('#titleUpdate').val(title || '');
        $('#hourlyUpdate').val(hourlyRate || '');

        // Ensure dept select is set (convert to string)
        if (typeof deptId !== 'undefined' && deptId !== null && deptId !== '') {
            $('#deptUpdate').val(deptId.toString());
        } else {
            $('#deptUpdate').val('');
        }

        // Show Bootstrap 5 modal programmatically
        const updateModalEl = document.getElementById('myUpdateModal');
        const updateModal = bootstrap.Modal.getOrCreateInstance(updateModalEl);
        updateModal.show();
    });

    // Delete button
    $('.js-positions-delete').on('click', function () {
        const id = $(this).attr('data-id');
        if (confirm('Are you sure you want to delete this position?')) {
            window.location.href = '/positions/delete/' + id;
        }
    });

    // Optional: focus first input when modal shown
    $('#myUpdateModal').on('shown.bs.modal', function () {
        $('#titleUpdate').trigger('focus');
    });
});
