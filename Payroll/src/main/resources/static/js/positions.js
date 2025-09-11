$(document).ready(function () {

const hash=window.location.hash

if (hash == '#updatecomplete')
{
    alert('complete')
}

   /$('#openModalBtn').on('click', function () {
        $('#myModal').modal('show');
   });


    $('.js-positions-update').on('click', function () {
        const id = $(this).data('id');
        const deptId = $(this).data('deptid'); // ✅ get departmentId from data attribute
        const row = $(this).closest('tr');

        const title = row.find('td:eq(1)').text().trim();
        const hourlyRate = row.find('td:eq(3)').text().trim();

        $('#positionIdUpdate').val(id);
        $('#titleUpdate').val(title);
        $('#deptUpdate').val(deptId); // ✅ use departmentId, not name
        $('#hourlyUpdate').val(hourlyRate);

        $('#myUpdateModal').modal('show');
    });



    $('.js-positions-delete').on('click', function () {
        const id = $(this).data('id');
        if (confirm('Are you sure you want to delete this position?')) {
            window.location.href = '/positions/delete/' + id;
        }
    });

});
