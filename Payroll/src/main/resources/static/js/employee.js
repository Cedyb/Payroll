$(function () {
    $('#openModalBtn').on('click', function () {
        var myModal = new bootstrap.Modal(document.getElementById('myModal'));
        myModal.show();
    });

    $('.js-employee-delete').on('click', function (e) {
        const id = $(e.target).data('id');
        window.location.href = '/employees/delete/' + id;
    });
});
