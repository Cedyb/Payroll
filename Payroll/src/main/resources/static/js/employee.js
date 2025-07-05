$(function () {

    $('#openModalBtn').on('click', function () {
        const myModal = new bootstrap.Modal(document.getElementById('myModal'));
        myModal.show();
    });


    $('.js-employee-update').on('click', function () {
        const row = $(this).closest('tr');

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

        const modal = new bootstrap.Modal(document.getElementById('myUpdateModal'));
        modal.show();
        getPosition();


    });
function getPosition(){
$.ajax({
url: '/positions/retrieve', // endpoint
method: 'GET',
success: function (data) {
// Assuming data is an array of position objects or strings
data.forEach(function (position) {
$('#positions-list').append('<li>' + position.title + '</li>');
});
},
error: function (xhr, status, error) {
console.error("Error retrieving positions:", error);
}
});}



    $('.js-employee-delete').on('click', function () {
        const id = $(this).data('id');
        window.location.href = '/employees/delete/' + id;
    });
});
