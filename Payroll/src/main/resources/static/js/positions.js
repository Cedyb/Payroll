// positions.js
$(function() {
  $('#openModalBtn').on('click', function() {
    $('#myModal').modal('show');
  });

  $('.js-positions-delete').on('click', function(e) {
    const btn = e.target;
    const id = $(btn).data('id');
    window.location.href = '/positions/delete/'+id;
  });
});