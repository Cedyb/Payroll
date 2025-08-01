$(function () {

    // Show Add Modal
    $('#openModalBtn').on('click', function () {
        const myModal = new bootstrap.Modal(document.getElementById('myModal'));
        myModal.show();
    });

    // Show Update Modal
     $('.js-employee-update').on('click', function () {
           const row = $(this).closest('tr');

           const employeeId = row.find('.emp-id').text().trim();
           const username = row.find('.emp-username').text().trim();
           const password = row.find('.emp-password').text().trim();
           const firstName = row.find('.emp-firstname').text().trim();
           const lastName = row.find('.emp-lastname').text().trim();
           const email = row.find('.emp-email').text().trim();
           const address = row.find('.emp-address').text().trim();
           const phone = row.find('.emp-phone').text().trim();
           const hireDate = row.find('.emp-hiredate').text().trim();
           const departmentId = row.find('.emp-departmentid').text().trim();
           const positionId = row.find('.emp-positionid').text().trim();

           // Set all values except position
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

           // Load and populate Position dropdown dynamically
           getPosition(departmentId, positionId);

           const modal = new bootstrap.Modal(document.getElementById('myUpdateModal'));
           modal.show();
       });

       // Dynamically populate Position dropdown based on selected Department
       function getPosition(departmentId, selectedPositionId = null) {
           $.ajax({
               url: '/positions/retrieve', // JSON response with positions
               method: 'GET',
               success: function (data) {
                   const dropdown = $('#positionUpdateDropdown');
                   dropdown.empty().append('<option value="">Select Position</option>');

                   data.forEach(function (position) {
                       if (position.department && position.department.id == departmentId) {
                           const isSelected = (position.positionId == selectedPositionId) ? 'selected' : '';
                           dropdown.append(`<option value="${position.positionId}" ${isSelected}>${position.title}</option>`);
                       }
                   });
               },
               error: function (xhr, status, error) {
                   console.error("Error retrieving positions:", error);
               }
           });
       }

       // Delete Employee
       $('.js-employee-delete').on('click', function () {
           const id = $(this).data('id');
           if (confirm('Are you sure you want to delete this employee?')) {
               window.location.href = '/employees/delete/' + id;
           }
       });

   });