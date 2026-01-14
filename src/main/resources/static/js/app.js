// Основной JavaScript файл

document.addEventListener('DOMContentLoaded', function() {
    // Инициализация подсказок Bootstrap
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Подтверждение удаления
    const deleteButtons = document.querySelectorAll('.delete-btn');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!confirm('Are you sure you want to delete this item?')) {
                e.preventDefault();
            }
        });
    });

    // Динамическое обновление статуса запасов
    const stockInputs = document.querySelectorAll('.stock-input');
    stockInputs.forEach(input => {
        input.addEventListener('input', function() {
            const minStock = this.dataset.min;
            const currentStock = this.value;
            const statusElement = this.closest('tr').querySelector('.stock-status');

            if (currentStock <= minStock) {
                statusElement.innerHTML = '<span class="badge bg-danger">Low Stock</span>';
            } else if (currentStock <= minStock * 1.5) {
                statusElement.innerHTML = '<span class="badge bg-warning">Warning</span>';
            } else {
                statusElement.innerHTML = '<span class="badge bg-success">Normal</span>';
            }
        });
    });

    // Автозаполнение даты
    const dateInputs = document.querySelectorAll('.date-input');
    dateInputs.forEach(input => {
        if (!input.value) {
            input.value = new Date().toISOString().split('T')[0];
        }
    });

    // Поиск с задержкой
    const searchInput = document.querySelector('#searchInput');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                this.form.submit();
            }, 500);
        });
    }

    // Валидация форм
    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });

    // Обновление общей стоимости при изменении количества или цены
    const calculateTotal = () => {
        const quantityInputs = document.querySelectorAll('.quantity-input');
        const priceInputs = document.querySelectorAll('.price-input');
        const totalElement = document.querySelector('#totalValue');

        if (totalElement) {
            let total = 0;
            for (let i = 0; i < quantityInputs.length; i++) {
                const quantity = parseFloat(quantityInputs[i].value) || 0;
                const price = parseFloat(priceInputs[i].value) || 0;
                total += quantity * price;
            }
            totalElement.textContent = '$' + total.toFixed(2);
        }
    };

    // Добавление обработчиков для пересчета
    document.querySelectorAll('.quantity-input, .price-input').forEach(input => {
        input.addEventListener('input', calculateTotal);
    });

    // Инициализация при загрузке
    calculateTotal();

    // Toast уведомления
    window.showToast = function(message, type = 'success') {
        const toastContainer = document.getElementById('toastContainer') || createToastContainer();
        const toastId = 'toast-' + Date.now();

        const toastHTML = `
            <div id="${toastId}" class="toast align-items-center text-white bg-${type} border-0" role="alert">
                <div class="d-flex">
                    <div class="toast-body">
                        ${message}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>
        `;

        toastContainer.innerHTML += toastHTML;
        const toastElement = document.getElementById(toastId);
        const toast = new bootstrap.Toast(toastElement);
        toast.show();

        // Удаление после скрытия
        toastElement.addEventListener('hidden.bs.toast', function () {
            this.remove();
        });
    };

    function createToastContainer() {
        const container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        document.body.appendChild(container);
        return container;
    }

    // Показ уведомления при наличии параметра в URL
    const urlParams = new URLSearchParams(window.location.search);
    const notification = urlParams.get('notification');
    if (notification) {
        showToast(decodeURIComponent(notification), 'success');
    }
});