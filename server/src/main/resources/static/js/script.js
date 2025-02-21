// Modal functionality
const loginBtn = document.getElementById('loginBtn');
const registerBtn = document.getElementById('registerBtn');
const loginModal = document.getElementById('loginModal');
const registerModal = document.getElementById('registerModal');
const closeBtns = document.querySelectorAll('.close-btn');

// Open modals
loginBtn.addEventListener('click', () => {
    loginModal.classList.add('active');
});

registerBtn.addEventListener('click', () => {
    registerModal.classList.add('active');
});

// Close modals
closeBtns.forEach(btn => {
    btn.addEventListener('click', () => {
        loginModal.classList.remove('active');
        registerModal.classList.remove('active');
    });
});

// Close modal when clicking outside
window.addEventListener('click', (e) => {
    if (e.target === loginModal) {
        loginModal.classList.remove('active');
    }
    if (e.target === registerModal) {
        registerModal.classList.remove('active');
    }
});

// Form submission (prevent default for demo)
const forms = document.querySelectorAll('.auth-form');
forms.forEach(form => {
    form.addEventListener('submit', (e) => {
        e.preventDefault();
        console.log('Form submitted');
    });
});

// Image Grid Demo
const imageGrid = document.getElementById('imageGrid');

// Добавим вызов функции loadPins при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    loadPins(); // Загружаем пины при старте

    // Добавим логирование для отладки
    console.log("Начинаем загрузку пинов...");
});

// Обновленная функция загрузки пинов
async function loadPins() {
    try {
        console.log("Отправляем запрос на получение пинов...");
        const response = await fetch('http://localhost:8081/api/pins/all'); // Используем эндпоинт /all

        if (response.ok) {
            const pins = await response.json();
            console.log("Получены пины:", pins);

            const imageGrid = document.getElementById('imageGrid');
            imageGrid.innerHTML = ''; // Очищаем сетку
            // Задаем контейнеру сетки стиль grid с 3 колонками и зазором 12px
            imageGrid.style.display = 'grid';
            imageGrid.style.gridTemplateColumns = 'repeat(3, 1fr)';
            imageGrid.style.gap = '12px';

            if (pins && pins.length > 0) {
                // Перебираем пины и определяем шаблон по размерам изображения
                pins.forEach((pin) => {
                    const card = document.createElement('div');
                    card.className = 'image-card';
                    // Для grid‑ячейки не нужно задавать фиксированную ширину – карточка заполнит всю ячейку
                    // Пока показываем placeholder
                    card.innerHTML = '<div class="loading">Loading...</div>';
                    imageGrid.appendChild(card);

                    // Создаем временный объект для определения размеров фотографии
                    const tempImg = new Image();
                    tempImg.onload = function() {
                        const ratio = tempImg.naturalWidth / tempImg.naturalHeight;
                        let templateHtml = '';
                        if (ratio >= 1.2) {
                            // Template B: горизонтальное изображение, aspect-ratio 2:1
                            templateHtml = `
                                <div class="image-card-content template-b" style="background: transparent;">
                                    <img src="${pin.imageUrl}" alt="${pin.description || 'Pin image'}"
                                         onerror="this.src='https://via.placeholder.com/150'"
                                         style="width: 100%; aspect-ratio: 2 / 1; object-fit: cover;"/>
                                    <p>${pin.description || 'No description'}</p>
                                    <div class="actions">
                                        <button onclick="likePin(${pin.id})">Like (${pin.likesCount})</button>
                                        <button onclick="toggleCommentForm(${pin.id})">Comment</button>
                                    </div>
                                    <div id="comments-${pin.id}" class="comments">
                                        ${pin.comments && pin.comments.length > 0
                                            ? pin.comments.map(comment => `<p><strong>${comment.username}:</strong> ${comment.text}</p>`).join('')
                                            : ''}
                                    </div>
                                    <div id="comment-form-${pin.id}" class="comment-form" style="display:none;">
                                        <input type="text" id="comment-input-${pin.id}" placeholder="Введите комментарий">
                                        <button onclick="submitComment(${pin.id})">Send</button>
                                    </div>
                                </div>
                            `;
                        } else {
                            // Template A: вертикальное/квадратное изображение, aspect-ratio 1:1
                            templateHtml = `
                                <div class="image-card-content template-a" style="background: transparent;">
                                    <img src="${pin.imageUrl}" alt="${pin.description || 'Pin image'}"
                                         onerror="this.src='https://via.placeholder.com/150'"
                                         style="width: 100%; aspect-ratio: 1 / 1; object-fit: cover;"/>
                                    <p>${pin.description || 'No description'}</p>
                                    <div class="actions">
                                        <button onclick="likePin(${pin.id})">Like (${pin.likesCount})</button>
                                        <button onclick="toggleCommentForm(${pin.id})">Comment</button>
                                    </div>
                                    <div id="comments-${pin.id}" class="comments">
                                        ${pin.comments && pin.comments.length > 0
                                            ? pin.comments.map(comment => `<p><strong>${comment.username}:</strong> ${comment.text}</p>`).join('')
                                            : ''}
                                    </div>
                                    <div id="comment-form-${pin.id}" class="comment-form" style="display:none;">
                                        <input type="text" id="comment-input-${pin.id}" placeholder="Введите комментарий">
                                        <button onclick="submitComment(${pin.id})">Send</button>
                                    </div>
                                </div>
                            `;
                        }
                        card.innerHTML = templateHtml;
                    };
                    tempImg.onerror = function() {
                        // Если не удалось загрузить изображение, используем шаблон A по умолчанию
                        card.innerHTML = `
                            <div class="image-card-content template-a" style="background: transparent;">
                                <img src="https://via.placeholder.com/150" alt="Image not available"
                                     style="width: 100%; aspect-ratio: 1 / 1; object-fit: cover;"/>
                                <p>${pin.description || 'No description'}</p>
                                <div class="actions">
                                    <button onclick="likePin(${pin.id})">Like (${pin.likesCount})</button>
                                    <button onclick="toggleCommentForm(${pin.id})">Comment</button>
                                </div>
                                <div id="comments-${pin.id}" class="comments">
                                    ${pin.comments && pin.comments.length > 0
                                        ? pin.comments.map(comment => `<p><strong>${comment.username}:</strong> ${comment.text}</p>`).join('')
                                        : ''}
                                </div>
                                <div id="comment-form-${pin.id}" class="comment-form" style="display:none;">
                                    <input type="text" id="comment-input-${pin.id}" placeholder="Введите комментарий">
                                    <button onclick="submitComment(${pin.id})">Send</button>
                                </div>
                            </div>
                        `;
                    };
                    tempImg.src = pin.imageUrl;
                });
            } else {
                imageGrid.innerHTML = '<p>No pins found</p>';
            }
        } else {
            console.error('Error loading pins:', response.status);
            const errorData = await response.text();
            console.error('Error details:', errorData);
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

// Floating button click handler
const floatingBtn = document.querySelector('.floating-btn');
floatingBtn.addEventListener('click', () => {
    console.log('Add new pin clicked');
});

// Search functionality
const searchInput = document.querySelector('.search-bar input');
searchInput.addEventListener('input', (e) => {
    console.log('Searching for:', e.target.value);
});

document.addEventListener('DOMContentLoaded', function() {
    // Если пользователь авторизован, заменить кнопки Login/Register на ссылку "Профиль"
    const token = localStorage.getItem('token');
    const authButtons = document.getElementById('authButtons');
    if (token) {
        authButtons.innerHTML = '<a href="profile.html" class="btn btn-outline">Профиль</a>';
    }

    // Элементы кнопок и модальных окон
    const loginBtn = document.getElementById('loginBtn');
    const registerBtn = document.getElementById('registerBtn');
    const loginModal = document.getElementById('loginModal');
    const registerModal = document.getElementById('registerModal');
    const closeBtns = document.querySelectorAll('.close-btn');

    // Функция открытия модального окна
    function openModal(modal) {
        modal.style.display = 'block';
    }

    // Функция закрытия модального окна
    function closeModal(modal) {
        modal.style.display = 'none';
    }

    // Открытие модального окна при клике на кнопку "Login" или "Register"
    if (loginBtn) {
        loginBtn.addEventListener('click', function() {
            openModal(loginModal);
        });
    }

    if (registerBtn) {
        registerBtn.addEventListener('click', function() {
            openModal(registerModal);
        });
    }

    // Закрытие модального окна при клике на кнопку "Закрыть"
    closeBtns.forEach(function(btn) {
        btn.addEventListener('click', function() {
            const modal = btn.closest('.modal');
            closeModal(modal);
        });
    });

    // Обработка отправки формы для логина
    const loginForm = loginModal.querySelector('form.auth-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const username = document.getElementById('loginUsername').value;
            const password = document.getElementById('loginPassword').value;

            try {
                const response = await fetch('http://localhost:8081/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password })
                });

                if (response.ok) {
                    const data = await response.json();
                    // Сохраняем токен и выводим сообщение об успехе
                    localStorage.setItem('token', data.token);
                    alert('Вход выполнен успешно!');
                    closeModal(loginModal);
                    // Редирект или обновление страницы
                    window.location.reload();
                } else {
                    const errorData = await response.json();
                    alert(`Ошибка входа: ${errorData.message}`);
                }
            } catch (error) {
                console.error(error);
                alert('Ошибка соединения с сервером');
            }
        });
    }

    // Обработка отправки формы для регистрации
    const registerForm = registerModal ? registerModal.querySelector('form.auth-form') : null;
    if (registerForm) {
        registerForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const username = document.getElementById('registerUsername').value;
            const email = document.getElementById('registerEmail').value;
            const password = document.getElementById('registerPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (password !== confirmPassword) {
                alert('Пароли не совпадают');
                return;
            }

            try {
                const response = await fetch('http://localhost:8081/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, email, password })
                });

                if (response.ok) {
                    alert('Регистрация прошла успешно! Теперь выполните вход.');
                    closeModal(registerModal);
                } else {
                    const errorData = await response.json();
                    alert(`Ошибка регистрации: ${errorData.message}`);
                }
            } catch (error) {
                console.error(error);
                alert('Ошибка соединения с сервером');
            }
        });
    }

    // Закрытие модального окна при клике вне его области
    window.addEventListener('click', function(e) {
        if (e.target.classList.contains('modal')) {
            closeModal(e.target);
        }
    });
});

const addPinForm = document.getElementById('addPinForm');
if (addPinForm) {
    addPinForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        const imageUrl = document.getElementById('pinImageUrl').value;
        const description = document.getElementById('pinDescription').value;
        const token = localStorage.getItem('token');

        try {
            const response = await fetch('http://localhost:8081/api/pins', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ imageUrl, description })
            });
            if (response.ok) {
                alert('Пин успешно добавлен!');
                loadPins();
            } else {
                const errorData = await response.json();
                alert(`Ошибка: ${errorData.message}`);
            }
        } catch (error) {
            console.error(error);
            alert('Ошибка соединения с сервером');
        }
    });
}

// Лайк пина
async function likePin(pinId) {
    const token = localStorage.getItem('token');
    if (!token) {
        alert("Пожалуйста, войдите, чтобы лайкать.");
        return;
    }
    try {
        const response = await fetch(`http://localhost:8081/api/pins/${pinId}/like`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const data = await response.json();
            alert(data.liked ? 'Лайк поставлен!' : 'Лайк удалён!');
            loadPins();
        } else {
            const errorData = await response.json();
            alert(`Ошибка: ${errorData.message}`);
        }
    } catch (error) {
        console.error(error);
        alert('Ошибка соединения с сервером');
    }
}

// Добавление комментария
async function addComment(pinId, text) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`http://localhost:8081/api/pins/${pinId}/comments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ text, userId: 1 }) // Замените на реальный ID пользователя
        });

        if (response.ok) {
            alert('Комментарий добавлен!');
        } else {
            const errorData = await response.json();
            alert(`Ошибка: ${errorData.message}`);
        }
    } catch (error) {
        console.error(error);
        alert('Ошибка соединения с сервером');
    }
}

function toggleCommentForm(pinId) {
    const formElem = document.getElementById(`comment-form-${pinId}`);
    if (formElem.style.display === 'none' || formElem.style.display === '') {
        formElem.style.display = 'flex';
    } else {
        formElem.style.display = 'none';
    }
}

// Функция для отправки комментария
async function submitComment(pinId) {
    const token = localStorage.getItem('token');
    const commentInput = document.getElementById(`comment-input-${pinId}`);
    const text = commentInput.value.trim();
    if (!text) {
        alert('Введите комментарий');
        return;
    }

    try {
        const response = await fetch(`http://localhost:8081/api/pins/${pinId}/comments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ text })
        });
        if(response.ok) {
            const newComment = await response.json();
            alert('Комментарий добавлен!');
            commentInput.value = "";
            const commentContainer = document.getElementById(`comments-${pinId}`);
            commentContainer.innerHTML += `<p><strong>${newComment.username}:</strong> ${newComment.text}</p>`;
            document.getElementById(`comment-form-${pinId}`).style.display = 'none';
        } else {
            const errorData = await response.json();
            alert(`Ошибка: ${errorData.message}`);
        }
    } catch(err) {
        console.error(err);
        alert('Ошибка соединения с сервером');
    }
}

const token = localStorage.getItem('token');
fetch('http://localhost:8081/api/profile', {
    headers: {
        'Authorization': `Bearer ${token}`
    }
});