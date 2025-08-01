function editDepartment(id, name, description) {
    document.getElementById('editId').value = id;
    document.getElementById('editName').value = name;
    document.getElementById('editDescription').value = description;

    const modal = new bootstrap.Modal(document.getElementById('editModal'));
    modal.show();
}
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
