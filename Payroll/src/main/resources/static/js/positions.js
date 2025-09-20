$(document).ready(function () {


    $('.js-positions-update').on('click', function (e) {
        e.preventDefault();
        const id = $(this).attr('data-id');
        const deptId = $(this).attr('data-deptid');
        const title = $(this).attr('data-title');
        const hourlyRate = $(this).attr('data-hourly-rate');

        $('#positionIdUpdate').val(id || '');
        $('#titleUpdate').val(title || '');
        $('#hourlyUpdate').val(hourlyRate || '');
        $('#deptUpdate').val(deptId || '');

        const updateModalEl = document.getElementById('myUpdateModal');
        const updateModal = bootstrap.Modal.getOrCreateInstance(updateModalEl);
        updateModal.show();
    });


    $('.js-positions-delete').on('click', function () {
        const id = $(this).attr('data-id');
        if (confirm('Are you sure you want to delete this position?')) {
            window.location.href = '/positions/delete/' + id;
        }
    });

    function filterPositions() {
        const title = $('#searchTitle').val().toLowerCase().trim();
        const deptId = $('#searchDepartment').val();

        $('table tbody tr').each(function() {
            const row = $(this);
            const rowTitle = row.find('td:nth-child(2)').text().toLowerCase().trim();
            const rowDeptId = row.find('td:nth-child(3)').data('id') || row.find('td:nth-child(3)').text();

            const matchesTitle = rowTitle.includes(title) || title === '';
            const matchesDept = (rowDeptId == deptId) || deptId === '';

            if (matchesTitle && matchesDept) {
                row.show();
            } else {
                row.hide();
            }
        });
    }


    $('#searchTitle').on('input', filterPositions);
    $('#searchDepartment').on('change', filterPositions);


    $('#searchBtn').on('click', function(e) {
        e.preventDefault();
        filterPositions();
    });
});
