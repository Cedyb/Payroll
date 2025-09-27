$(document).ready(function () {
    // Add Department
    $('#openAddDeptModal').on('click', function (e) {
        if ($(this).hasClass('readonly')) {
            e.preventDefault();
            return;
        }
        $('#deptAddModal').modal('show');
    });

    // Update Department
    $('.js-dept-update').on('click', function () {
        if ($(this).hasClass('readonly')) return;

        const id = $(this).data('id');
        const name = $(this).data('name');
        const desc = $(this).data('description');

        $('#deptIdUpdate').val(id);
        $('#deptNameUpdate').val(name);
        $('#deptDescUpdate').val(desc);

        $('#deptUpdateModal').modal('show');
    });

    // Delete Department
    $('.js-dept-delete').on('click', function (e) {
        if ($(this).hasClass('readonly')) {
            e.preventDefault();
            return false;
        }
    });

    // Search
    $('#deptSearchInput').on('keyup', function () {
        const value = $(this).val().toLowerCase().trim();
        $('#deptTable tbody tr').filter(function () {
            const deptName = $(this).find('.dept-name').text().toLowerCase();
            $(this).toggle(deptName.includes(value));
        });
    });
});
