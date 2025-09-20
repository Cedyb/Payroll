$(document).ready(function () {

    // Open Add Department Modal
    $('#openAddDeptModal').on('click', function () {
        $('#deptAddModal').modal('show');
    });

    // Open Update Department Modal
    $('.js-dept-update').on('click', function () {
        const id = $(this).data('id');
        const name = $(this).data('name');
        const desc = $(this).data('description');

        $('#deptIdUpdate').val(id);
        $('#deptNameUpdate').val(name);
        $('#deptDescUpdate').val(desc);

        $('#deptUpdateModal').modal('show');
    });

    // Advanced search/filter by department name
    $('#deptSearchInput').on('keyup', function () {
        const value = $(this).val().toLowerCase().trim();
        $('#deptTable tbody tr').filter(function () {  // <-- match the table ID
            const deptName = $(this).find('.dept-name').text().toLowerCase();
            $(this).toggle(deptName.includes(value));
        });
    });


});

// Optional helper function (if you still need it)
function editDepartment(id, name, description) {
    document.getElementById('editId').value = id;
    document.getElementById('editName').value = name;
    document.getElementById('editDescription').value = description;

    const modal = new bootstrap.Modal(document.getElementById('editModal'));
    modal.show();
}

// Optional helper for populating department dropdowns
function getDepartments(selected = null) {
    $.ajax({
        url: '/departments/retrieve',
        method: 'GET',
        success: function (data) {
            const dropdown = $('#departmentUpdate');
            dropdown.empty().append('<option value="">Select Department</option>');
            data.forEach(function (dept) {
                const isSelected = selected && dept.name === selected ? 'selected' : '';
                dropdown.append(`<option value="${dept.name}" ${isSelected}>${dept.name}</option>`);
            });
        },
        error: function (xhr, status, error) {
            console.error("Error loading departments:", error);
        }
    });
}
