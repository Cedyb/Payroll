function toggleDropdown(elem) {
    document.querySelectorAll('.dropdown-menu-custom').forEach(menu => {
        if (menu !== elem.nextElementSibling) {
            menu.style.display = 'none';
        }
    });


    const menu = elem.nextElementSibling;
    menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
}


document.addEventListener('click', function (event) {
    const isEllipsis = event.target.classList.contains('ellipsis');
    if (!isEllipsis) {
        document.querySelectorAll('.dropdown-menu-custom').forEach(menu => {
            menu.style.display = 'none';
        });
    }
});