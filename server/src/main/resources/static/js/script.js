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


// Create and append image cards
// Функция загрузки пинов с бэкенда и динамическое создание карточек
async function loadPins() {
    try {
        const response = await fetch('http://localhost:8081/api/pins');
        if (response.ok) {
            const pins = await response.json();
            console.log("Fetched pins:", pins); // Отладочное сообщение: выводим полученные пины
            const imageGrid = document.getElementById('imageGrid');
            imageGrid.innerHTML = ''; // Очищаем сетку перед добавлением новых пинов
            pins.forEach(pin => {
                const card = document.createElement('div');
                card.className = 'image-card';
                card.innerHTML = `
                    <img src="${pin.imageUrl}" alt="${pin.description}">
                    <p>${pin.description}</p>
                    <p>Загружено пользователем: ${pin.user ? pin.user.username : 'Неизвестно'}</p>
                    <div class="actions">
                        <button onclick="likePin(${pin.id})">Like (${pin.likes ? pin.likes.length : 0})</button>
                        <button onclick="toggleCommentForm(${pin.id})">Comment</button>
                    </div>
                    <div id="comments-${pin.id}" class="comments">
                        ${pin.comments && pin.comments.length > 0
                    ? pin.comments.map(comment => `<p><strong>${comment.user ? comment.user.username : 'Неизвестно'}:</strong> ${comment.text}</p>`).join('')
                    : ''
                }
                    </div>
                    <div id="comment-form-${pin.id}" class="comment-form" style="display:none;">
                        <input type="text" id="comment-input-${pin.id}" placeholder="Ваш комментарий">
                        <button onclick="submitComment(${pin.id})">Отправить</button>
                    </div>
                `;
                imageGrid.appendChild(card);
            });
        } else {
            console.error('Ошибка при загрузке пинов');
        }
    } catch (error) {
        console.error('Ошибка соединения с сервером', error);
    }
}

// Функция для отправки лайка
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

        if(response.ok){
            const data = await response.json();
            alert(data.liked ? 'Лайк поставлен!' : 'Лайк удалён!');
            loadPins(); // Обновляем пины для отображения актуального счёта лайков
        } else {
            const errorData = await response.json();
            alert(`Ошибка: ${errorData.message}`);
        }
    } catch(err) {
        console.error(err);
        alert('Ошибка соединения с сервером');
    }
}

// Функция для показа/скрытия формы комментария
function toggleCommentForm(pinId) {
    const formDiv = document.getElementById(`comment-form-${pinId}`);
    formDiv.style.display = (formDiv.style.display === 'none' || formDiv.style.display === '') ? 'block' : 'none';
}

// Функция отправки комментария
async function submitComment(pinId) {
    const token = localStorage.getItem('token');
    if (!token) {
        alert("Пожалуйста, войдите, чтобы оставлять комментарии.");
        return;
    }
    const inputField = document.getElementById(`comment-input-${pinId}`);
    const commentText = inputField.value.trim();
    if (!commentText) {
        alert("Комментарий не может быть пустым");
        return;
    }
    try {
        const response = await fetch(`http://localhost:8081/api/pins/${pinId}/comments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ text: commentText })
        });
        if(response.ok){
            alert('Комментарий добавлен!');
            inputField.value = '';
            loadPins(); // Обновляем пины для отображения новых комментариев
        } else {
            const errorData = await response.json();
            alert(`Ошибка: ${errorData.message}`);
        }
    } catch (err) {
        console.error(err);
        alert('Ошибка соединения с сервером');
    }
}

// Привязка загрузки пинов к событию загрузки страницы
document.addEventListener('DOMContentLoaded', loadPins);

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

const token = localStorage.getItem('token');
fetch('http://localhost:8081/api/profile', {
    headers: {
        'Authorization': `Bearer ${token}`
    }
});