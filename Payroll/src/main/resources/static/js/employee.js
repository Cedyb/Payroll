
    $(document).ready(function () {


    $('#openModalBtn').on('click', function () {
        const myModal = new bootstrap.Modal(document.getElementById('myModal'));
        myModal.show();
    });


    function getPositions(departmentId, dropdownSelector, selectedPositionId = null) {
    const dropdown = $(dropdownSelector);
    dropdown.empty().append('<option value="">Select Position</option>');
    if (!departmentId) return;

    $.ajax({
    url: '/positions/retrieve',
    method: 'GET',
    data: { departmentId: departmentId },
    dataType: 'json',
    success: function (data) {
    data.forEach(function (position) {
    const posDeptId = (position.department && (position.department.departmentId || position.department.id)) || position.departmentId || '';
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
    const deptId = $(this).val();
    getPositions(deptId, '#positionAddDropdown');
});

    $('#departmentUpdateDropdown').on('change', function () {
    const deptId = $(this).val();
    getPositions(deptId, '#positionUpdateDropdown');
});


    $(document).on('click', '.js-employee-update', function () {
    const row = $(this).closest('tr');
    const employeeId = row.find('.emp-id').text().trim();
    const username = row.find('.emp-username').text().trim();
    const password = '';
    const firstName = row.find('.emp-firstname').text().trim();
    const lastName = row.find('.emp-lastname').text().trim();
    const email = row.find('.emp-email').text().trim();
    const address = row.find('.emp-address').text().trim();
    const phone = row.find('.emp-phone').text().trim();
    const hireDate = row.find('.emp-hiredate').text().trim();
    const departmentId = row.find('.emp-departmentid').data('id')?.toString() || '';
    const positionId = row.find('.emp-positionid').data('id')?.toString() || '';

    $('#employeeIdUpdate').val(employeeId);
    $('#usernameUpdate').val(username);
    $('#passwordUpdate').val(password);
    $('#firstNameUpdate').val(firstName);
    $('#lastNameUpdate').val(lastName);
    $('#emailUpdate').val(email);
    $('#addressUpdate').val(address);
    $('#phoneUpdate').val(phone);
    $('#hireDateUpdate').val(hireDate);
    $('#departmentUpdateDropdown').val(departmentId);
    getPositions(departmentId, '#positionUpdateDropdown', positionId);

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
    const posDropdown = $('#searchPosition');
    posDropdown.empty().append('<option value="">All Positions</option>');
    if (!deptId) return;

    $.ajax({
    url: '/positions/retrieve',
    method: 'GET',
    data: { departmentId: deptId },
    dataType: 'json',
    success: function (data) {
    data.forEach(function (position) {
    posDropdown.append($('<option>', {
    value: position.positionId,
    text: position.title
}));
});
}
});
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
    if ($('#searchName').val() === '' && $('#searchDepartment').val() === '' && $('#searchPosition').val() === '') {
    $('table tbody tr').show();
}
});

});
