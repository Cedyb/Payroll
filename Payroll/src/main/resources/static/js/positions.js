$(document).ready(function () {
    // Update button
    $('.js-positions-update').on('click', function (e) {
        if ($(this).hasClass('readonly')) return; // block readonly
        e.preventDefault();

        const id = $(this).attr('data-id');
        const deptId = $(this).attr('data-deptid');
        const title = $(this).attr('data-title');
        const hourlyRate = $(this).attr('data-hourly-rate');

        $('#positionIdUpdate').val(id || '');
        $('#titleUpdate').val(title || '');
        $('#hourlyUpdate').val(hourlyRate || '');
        $('#deptUpdate').val(deptId || '');

        const updateModal = bootstrap.Modal.getOrCreateInstance(document.getElementById('myUpdateModal'));
        updateModal.show();
    });

    // Delete button
    $('.js-positions-delete').on('click', function () {
        if ($(this).hasClass('readonly')) return; // block readonly

        const id = $(this).attr('data-id');
        if (confirm('Are you sure you want to delete this position?')) {
            window.location.href = '/positions/delete/' + id;
        }
    });

    // Add Position button
    $('#openModalBtn').on('click', function (e) {
        if ($(this).hasClass('readonly')) {
            e.preventDefault(); // block modal opening
            return;
        }
    });
});
