// Глобальные переменные
let currentPage = 0;
let isLoading = false;
let hasMorePins = true;
let searchQuery = '';
let currentUser = null;
let isIntersectionObserverSupported = 'IntersectionObserver' in window;

// DOM элементы
const imageGrid = document.querySelector('.image-grid');
const searchInput = document.querySelector('.search-bar input');
const loginModal = document.getElementById('loginModal');
const registerModal = document.getElementById('registerModal');
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const loginBtn = document.getElementById('loginBtn');
const registerBtn = document.getElementById('registerBtn');
const logoutBtn = document.getElementById('logoutBtn');
const profileBtn = document.getElementById('profileBtn');
const addPinBtn = document.getElementById('addPinBtn');
const addPinModal = document.getElementById('addPinModal');
const addPinForm = document.getElementById('addPinForm');
const closeBtns = document.querySelectorAll('.close-btn');
const loadingIndicator = document.createElement('div');

// Настройка индикатора загрузки
loadingIndicator.className = 'loading-indicator';
loadingIndicator.innerHTML = `
    <div class="spinner">
        <div class="bounce1"></div>
        <div class="bounce2"></div>
        <div class="bounce3"></div>
    </div>
    <p>Загрузка...</p>
`;

// Инициализация
document.addEventListener('DOMContentLoaded', () => {
    initApp();
    setupEventListeners();
    checkAuthStatus();

    // Добавляем анимацию появления для элементов героя
    animateHeroElements();
});

// Функция инициализации приложения
function initApp() {
    // Загрузка первой страницы пинов
    loadPins();

    // Настройка Intersection Observer для бесконечной прокрутки
    if (isIntersectionObserverSupported) {
        setupInfiniteScroll();
    }

    // Добавляем темную тему, если пользователь предпочитает ее
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        document.body.classList.add('dark-theme');
    }

    // Анимация появления элементов при загрузке
    animateOnScroll();
}

// Настройка обработчиков событий
function setupEventListeners() {
    // Модальные окна
    document.getElementById('loginBtn')?.addEventListener('click', () => openModal(document.getElementById('loginModal')));
    document.getElementById('registerBtn')?.addEventListener('click', () => openModal(document.getElementById('registerModal')));
    document.querySelectorAll('.close-btn').forEach(btn => btn.addEventListener('click', closeModal));

    // Обработка кликов вне модального окна
    window.addEventListener('click', (e) => {
        const modals = document.querySelectorAll('.modal');
        modals.forEach(modal => {
            if (e.target === modal) {
                closeModal();
            }
        });
    });

    // Поиск
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');

    if (searchInput && searchButton) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                handleSearch(e);
            }
        });
        searchButton.addEventListener('click', handleSearch);
    }

    // Обработчики для тегов
    document.querySelectorAll('.tag').forEach(tag => {
        tag.addEventListener('click', () => {
            if (searchInput) {
                searchInput.value = tag.textContent;
                handleSearch();
            }
        });
    });

    // Плавающая кнопка
    document.getElementById('addPinBtn')?.addEventListener('click', () => openModal(document.getElementById('addPinModal')));

    // Формы
    document.getElementById('loginForm')?.addEventListener('submit', handleLogin);
    document.getElementById('registerForm')?.addEventListener('submit', handleRegister);
    document.getElementById('addPinForm')?.addEventListener('submit', handleAddPin);

    // Кнопка выхода
    document.getElementById('logoutBtn')?.addEventListener('click', handleLogout);

    // Обработчик прокрутки для анимации элементов
    window.addEventListener('scroll', debounce(() => {
        animateOnScroll();
    }, 100));
}

// Функция для анимации элементов героя
function animateHeroElements() {
    const heroTitle = document.querySelector('.hero h1');
    const heroText = document.querySelector('.hero p');
    const searchBar = document.querySelector('.search-bar');

    if (heroTitle) {
        heroTitle.style.opacity = '0';
        heroTitle.style.transform = 'translateY(20px)';
        setTimeout(() => {
            heroTitle.style.transition = 'opacity 0.8s ease, transform 0.8s ease';
            heroTitle.style.opacity = '1';
            heroTitle.style.transform = 'translateY(0)';
        }, 100);
    }

    if (heroText) {
        heroText.style.opacity = '0';
        heroText.style.transform = 'translateY(20px)';
        setTimeout(() => {
            heroText.style.transition = 'opacity 0.8s ease, transform 0.8s ease';
            heroText.style.opacity = '1';
            heroText.style.transform = 'translateY(0)';
        }, 300);
    }

    if (searchBar) {
        searchBar.style.opacity = '0';
        searchBar.style.transform = 'translateY(20px)';
        setTimeout(() => {
            searchBar.style.transition = 'opacity 0.8s ease, transform 0.8s ease';
            searchBar.style.opacity = '1';
            searchBar.style.transform = 'translateY(0)';
        }, 500);
    }
}

// Функция для анимации элементов при прокрутке
function animateOnScroll() {
    const animatedElements = document.querySelectorAll('.animate-on-scroll:not(.animated)');

    animatedElements.forEach(element => {
        const elementPosition = element.getBoundingClientRect().top;
        const windowHeight = window.innerHeight;

        if (elementPosition < windowHeight - 100) {
            element.classList.add('animated');
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }
    });
}

// Настройка бесконечной прокрутки с Intersection Observer
function setupInfiniteScroll() {
    const options = {
        root: null,
        rootMargin: '0px',
        threshold: 0.1
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting && !isLoading && hasMorePins) {
                loadMorePins();
            }
        });
    }, options);

    // Создаем и добавляем сентинел-элемент в конец сетки
    const sentinel = document.createElement('div');
    sentinel.className = 'sentinel';
    imageGrid.appendChild(sentinel);

    // Наблюдаем за сентинел-элементом
    observer.observe(sentinel);
}

// Функция загрузки пинов
async function loadPins(page = 0, size = 12, search = '') {
    if (isLoading || !hasMorePins) return;

    isLoading = true;
    showLoading();

    try {
        const url = search
            ? `/api/pins?page=${page}&size=${size}&search=${encodeURIComponent(search)}`
            : `/api/pins?page=${page}&size=${size}`;

        console.log('Загрузка пинов: ' + url);
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log('Получено пинов: ' + (data.content?.length || 0));

        if (data.content.length === 0) {
            hasMorePins = false;
            hideLoading();

            if (page === 0) {
                showNoResultsMessage(search);
            }

            return;
        }

        // Обработка URL изображений для каждого пина
        const processedPins = data.content.map(pin => {
            // Если URL изображения битый или содержит Яндекс Диск, заменяем на прокси
            if (pin.imageUrl && (pin.imageUrl.includes('yandex') || pin.imageUrl.includes('disk.'))) {
                pin.imageUrl = `/api/pins/proxy-image?url=${encodeURIComponent(pin.imageUrl)}`;
                console.log('Обработанный URL изображения: ' + pin.imageUrl);
            }
            return pin;
        });

        renderPins(processedPins);
        currentPage = page + 1;
        hasMorePins = !data.last;

    } catch (error) {
        console.error('Error loading pins:', error);
        showErrorMessage('Не удалось загрузить пины. Пожалуйста, попробуйте позже.');
    } finally {
        isLoading = false;
        hideLoading();
    }
}

// Функция для загрузки дополнительных пинов
function loadMorePins() {
    loadPins();
}

// Функция отображения пинов
function renderPins(pins) {
    pins.forEach(pin => {
        const pinElement = createPinElement(pin);
        imageGrid.appendChild(pinElement);
    });
}

// Создание элемента пина
function createPinElement(pin) {
    const pinElement = document.createElement('div');
    pinElement.className = 'image-card';
    pinElement.setAttribute('data-id', pin.id);

    // Проверяем и обрабатываем URL изображения
    let imageUrl = pin.imageUrl || '';

    // Добавляем логгинг для отладки изображений
    console.log('Pin ID: ' + pin.id + ', Image URL: ' + imageUrl);

    // Если ссылка битая или отсутствует, заменяем на заглушку
    if (!imageUrl || imageUrl === 'null' || imageUrl === 'undefined') {
        imageUrl = 'https://via.placeholder.com/300x400?text=Изображение+отсутствует';
    }

    pinElement.innerHTML = `
        <a href="/pin.html?id=${pin.id}" class="image-link">
            <div class="image-container">
                <img src="${imageUrl}" alt="${pin.title}" loading="lazy" onerror="this.src='https://via.placeholder.com/300x400?text=Ошибка+загрузки'">
                <div class="image-overlay">
                    <div class="image-actions">
                        <button class="save-btn" title="Сохранить">
                            <i class="fas fa-bookmark"></i>
                        </button>
                        <button class="like-btn" title="Нравится">
                            <i class="fas fa-heart"></i>
                        </button>
                        <button class="share-btn" title="Поделиться">
                            <i class="fas fa-share"></i>
                        </button>
                    </div>
                </div>
            </div>
            <div class="image-info">
                <h3>${pin.title}</h3>
                ${pin.description ? `<p class="pin-description">${pin.description.substring(0, 100)}${pin.description.length > 100 ? '...' : ''}</p>` : ''}
                <p class="pin-author">
                    <i class="fas fa-user"></i> ${pin.user ? pin.user.username : 'Пользователь'}
                </p>
            </div>
        </a>
    `;

    // Добавляем обработчики событий
    const saveBtn = pinElement.querySelector('.save-btn');
    const likeBtn = pinElement.querySelector('.like-btn');
    const shareBtn = pinElement.querySelector('.share-btn');

    saveBtn.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        handleSavePin(pin.id);
    });

    likeBtn.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        handleLike(pin.id);
    });

    shareBtn.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        handleShare(pin.id);
    });

    return pinElement;
}

// Обработка поиска
function handleSearch(e) {
    if (e) e.preventDefault();
    const searchQuery = document.getElementById('searchInput').value.trim();

    if (searchQuery) {
        resetPinGrid();
        showLoading();

        // Добавляем класс, показывающий активный поиск
        document.querySelector('.search-bar').classList.add('active-search');

        // Меняем заголовок секции
        const sectionTitle = document.querySelector('.section-title h2');
        if (sectionTitle) {
            sectionTitle.textContent = `Результаты поиска: "${searchQuery}"`;
        }

        // Вызываем загрузку с поисковым запросом
        loadPins(0, 20, searchQuery)
            .then(() => {
                if (document.querySelector('.image-grid').children.length === 0) {
                    showNoResultsMessage(searchQuery);
                }
                hideLoading();
            })
            .catch(err => {
                console.error('Ошибка при поиске:', err);
                showErrorMessage('Произошла ошибка при поиске идей. Пожалуйста, попробуйте еще раз.');
                hideLoading();
            });
    } else {
        // Если поле поиска пустое, просто обновляем все пины
        resetPinGrid();
        document.querySelector('.search-bar').classList.remove('active-search');

        // Возвращаем исходный заголовок
        const sectionTitle = document.querySelector('.section-title h2');
        if (sectionTitle) {
            sectionTitle.textContent = 'Последние идеи';
        }

        loadPins();
    }
}

// Сброс сетки пинов
function resetPinGrid() {
    imageGrid.innerHTML = '';
    currentPage = 0;
    hasMorePins = true;
}

// Показать индикатор загрузки
function showLoading() {
    if (!document.querySelector('.loading-indicator')) {
        imageGrid.appendChild(loadingIndicator);
    }
}

// Скрыть индикатор загрузки
function hideLoading() {
    const indicator = document.querySelector('.loading-indicator');
    if (indicator) {
        indicator.remove();
    }
}

// Показать сообщение об отсутствии результатов
function showNoResultsMessage(searchQuery) {
    const message = document.createElement('div');
    message.className = 'no-results';
    message.innerHTML = `
        <i class="fas fa-search"></i>
        <h3>Ничего не найдено</h3>
        <p>Попробуйте изменить поисковый запрос: "${searchQuery}"</p>
    `;
    imageGrid.appendChild(message);

    // Анимация появления сообщения
    message.style.opacity = '0';
    message.style.transform = 'translateY(20px)';

    setTimeout(() => {
        message.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
        message.style.opacity = '1';
        message.style.transform = 'translateY(0)';
    }, 100);
}

// Показать сообщение об ошибке
function showErrorMessage(message) {
    const errorElement = document.createElement('div');
    errorElement.className = 'error-message';
    errorElement.textContent = message;

    document.body.appendChild(errorElement);

    // Анимация появления и исчезновения
    setTimeout(() => {
        errorElement.classList.add('show');
    }, 10);

    setTimeout(() => {
        errorElement.classList.remove('show');
        setTimeout(() => {
            errorElement.remove();
        }, 300);
    }, 3000);
}

// Открытие модального окна
function openModal(modal) {
    closeModal(); // Закрываем все открытые модальные окна

    if (modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden'; // Блокируем прокрутку страницы

        // Анимация появления модального окна
        const modalContent = modal.querySelector('.modal-content');
        if (modalContent) {
            modalContent.style.opacity = '0';
            modalContent.style.transform = 'translateY(20px)';

            setTimeout(() => {
                modalContent.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
                modalContent.style.opacity = '1';
                modalContent.style.transform = 'translateY(0)';
            }, 10);
        }
    }
}

// Закрытие модального окна
function closeModal() {
    const activeModals = document.querySelectorAll('.modal.active');

    activeModals.forEach(modal => {
        const modalContent = modal.querySelector('.modal-content');

        if (modalContent) {
            modalContent.style.opacity = '0';
            modalContent.style.transform = 'translateY(20px)';

            setTimeout(() => {
                modal.classList.remove('active');
                document.body.style.overflow = ''; // Разблокируем прокрутку страницы
            }, 300);
        } else {
            modal.classList.remove('active');
            document.body.style.overflow = '';
        }
    });
}

// Обработка входа
async function handleLogin(e) {
    e.preventDefault();

    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    if (!username || !password) {
        showErrorMessage('Пожалуйста, заполните все поля');
        return;
    }

    try {
        // Добавим логирование для отладки
        console.log('Отправка запроса на аутентификацию...');

        // Показываем индикатор загрузки в кнопке
        const submitButton = document.querySelector('#loginForm button[type="submit"]');
        const originalButtonText = submitButton.innerHTML;
        submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Вход...';
        submitButton.disabled = true;

        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        console.log('Статус ответа:', response.status);

        // Восстанавливаем кнопку
        submitButton.innerHTML = originalButtonText;
        submitButton.disabled = false;

        if (!response.ok) {
            let errorMessage = 'Ошибка при входе';

            try {
                const errorData = await response.json();
                console.error('Данные ошибки:', errorData);
                errorMessage = errorData.message || 'Неверное имя пользователя или пароль';
            } catch (e) {
                console.error('Не удалось разобрать JSON ответ:', e);

                if (response.status === 403) {
                    errorMessage = 'Доступ запрещен. Проверьте правильность учетных данных.';
                } else if (response.status === 401) {
                    errorMessage = 'Неверное имя пользователя или пароль';
                } else if (response.status === 500) {
                    errorMessage = 'Внутренняя ошибка сервера. Пожалуйста, попробуйте позже.';
                }
            }

            showErrorMessage(errorMessage);
            return;
        }

        const data = await response.json();
        console.log('Успешный вход, токен получен');

        // Сохраняем токен в localStorage
        localStorage.setItem('token', data.token);
        console.log('Токен сохранен в localStorage');

        // Закрываем модальное окно
        closeModal();

        // Обновляем UI
        await checkAuthStatus();

        showSuccessMessage('Вы успешно вошли в систему');

    } catch (error) {
        console.error('Ошибка входа:', error);
        showErrorMessage('Произошла ошибка при соединении с сервером. Пожалуйста, проверьте соединение и попробуйте снова.');
    }
}

// Обработчик регистрации
async function handleRegister(e) {
    e.preventDefault();

    const username = document.getElementById('registerUsername').value;
    const email = document.getElementById('registerEmail').value;
    const password = document.getElementById('registerPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (!username || !email || !password || !confirmPassword) {
        showErrorMessage('Пожалуйста, заполните все поля');
        return;
    }

    if (password !== confirmPassword) {
        showErrorMessage('Пароли не совпадают');
        return;
    }

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, email, password })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Ошибка при регистрации');
        }

        closeModal();
        showSuccessMessage('Вы успешно зарегистрировались! Теперь вы можете войти в аккаунт.');

        // Открываем модальное окно входа
        setTimeout(() => {
            openModal(loginModal);
        }, 1500);

    } catch (error) {
        console.error('Register error:', error);
        showErrorMessage(error.message || 'Ошибка при регистрации');
    }
}

// Обработка выхода из аккаунта
async function handleLogout() {
    try {
        const token = localStorage.getItem('token');

        if (!token) {
            updateUIForGuest();
            return;
        }

        const response = await fetch('/api/auth/logout', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        // Удаляем токен из localStorage
        localStorage.removeItem('token');

        // Обновляем UI
        updateUIForGuest();

        showSuccessMessage('Вы успешно вышли из системы');

    } catch (error) {
        console.error('Logout error:', error);
        // Даже если произошла ошибка, все равно удаляем токен и обновляем UI
        localStorage.removeItem('token');
        updateUIForGuest();
    }
}

// Проверка статуса аутентификации
async function checkAuthStatus() {
    try {
        // Получаем токен из localStorage
        const token = localStorage.getItem('token');
        console.log('Проверка аутентификации...');

        if (!token) {
            console.log('Токен не найден, пользователь не аутентифицирован');
            updateUIForGuest();
            return;
        }

        console.log('Токен найден, проверяем валидность...');

        const response = await fetch('/api/auth/check', {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        console.log('Статус ответа проверки аутентификации:', response.status);

        if (!response.ok) {
            // Если статус указывает на проблемы с авторизацией
            if (response.status === 401 || response.status === 403) {
                console.log('Токен недействителен или просрочен');
                localStorage.removeItem('token'); // Удаляем невалидный токен
                updateUIForGuest();
                return;
            }

            // Для других ошибок пробуем продолжить работу
            console.error('Ошибка проверки авторизации:', response.statusText);
            return;
        }

        const userData = await response.json();
        console.log('Пользователь аутентифицирован:', userData.username);
        currentUser = userData;

        updateUIForUser(userData);

    } catch (error) {
        console.error('Ошибка при проверке аутентификации:', error);

        // В случае проблем с сетью, сохраняем состояние на основе наличия токена
        if (localStorage.getItem('token')) {
            console.log('Сохраняем предыдущее состояние аутентификации из-за ошибки сети');
            // Если есть сохраненное состояние пользователя, используем его
            if (currentUser) {
                updateUIForUser(currentUser);
            }
        } else {
            updateUIForGuest();
        }
    }
}

// Обновление UI для гостя
function updateUIForGuest() {
    currentUser = null;

    if (loginBtn) loginBtn.style.display = 'inline-flex';
    if (registerBtn) registerBtn.style.display = 'inline-flex';
    if (logoutBtn) logoutBtn.style.display = 'none';
    if (profileBtn) profileBtn.style.display = 'none';
    if (addPinBtn) addPinBtn.style.display = 'none';
}

// Обновление UI для авторизованного пользователя
function updateUIForUser(user) {
    if (loginBtn) loginBtn.style.display = 'none';
    if (registerBtn) registerBtn.style.display = 'none';
    if (logoutBtn) logoutBtn.style.display = 'inline-flex';
    if (profileBtn) {
        profileBtn.style.display = 'inline-flex';
        profileBtn.href = `/profile.html?username=${user.username}`;
        profileBtn.innerHTML = '<i class="fas fa-user"></i> Личный кабинет';
    }
    if (addPinBtn) addPinBtn.style.display = 'inline-flex';
}

// Обработчик добавления пина
async function handleAddPin(e) {
    e.preventDefault();

    const title = document.getElementById('pinTitle').value;
    const description = document.getElementById('pinDescription').value;
    const imageFile = document.getElementById('pinImage').files[0];

    if (!imageFile) {
        showErrorMessage('Пожалуйста, выберите изображение');
        return;
    }

    const formData = new FormData();
    formData.append('title', title);
    formData.append('description', description);
    formData.append('image', imageFile);

    const token = localStorage.getItem('token');

    if (!token) {
        showErrorMessage('Вы должны войти в систему, чтобы добавить пин');
        return;
    }

    try {
        showLoading();

        const response = await fetch('/api/pins', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        if (!response.ok) {
            throw new Error('Не удалось добавить пин');
        }

        // Сбрасываем форму
        addPinForm.reset();

        // Закрываем модальное окно
        closeModal();

        // Обновляем сетку пинов
        resetPinGrid();
        loadPins();

        showSuccessMessage('Пин успешно добавлен!');

    } catch (error) {
        console.error('Add pin error:', error);
        showErrorMessage(error.message);
    } finally {
        hideLoading();
    }
}

// Обработчик лайка пина
async function handleLike(pinId) {
    if (!currentUser) {
        showErrorMessage('Для лайка пина необходимо войти в аккаунт');
        openModal(loginModal);
        return;
    }

    try {
        const response = await fetch(`/api/pins/${pinId}/likes`, {
            method: 'POST',
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Ошибка при лайке пина');
        }

        showSuccessMessage('Лайк успешно добавлен');

        // Обновляем счетчик лайков на странице
        const likeBtn = document.querySelector(`.image-card[data-id="${pinId}"] .like-btn`);
        if (likeBtn) {
            const icon = likeBtn.querySelector('i');
            icon.classList.add('liked');

            // Анимация лайка
            icon.classList.add('pulse');
            setTimeout(() => {
                icon.classList.remove('pulse');
            }, 500);
        }

    } catch (error) {
        console.error('Like pin error:', error);
        showErrorMessage(error.message || 'Ошибка при лайке пина');
    }
}

// Открытие деталей пина
function openPinDetails(pinId) {
    window.location.href = `/pin.html?id=${pinId}`;
}

// Обработчик поделиться пином
function handleShare(pinId) {
    const url = `${window.location.origin}/pin.html?id=${pinId}`;

    if (navigator.share) {
        navigator.share({
            title: 'Поделиться пином',
            url: url
        })
        .then(() => {
            console.log('Успешно поделились');
        })
        .catch(error => {
            console.error('Ошибка при попытке поделиться:', error);
            copyToClipboard(url);
        });
    } else {
        copyToClipboard(url);
    }
}

// Функция копирования в буфер обмена
function copyToClipboard(text) {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    document.body.appendChild(textarea);
    textarea.select();

    try {
        document.execCommand('copy');
        showSuccessMessage('Ссылка скопирована в буфер обмена');
    } catch (err) {
        console.error('Не удалось скопировать ссылку:', err);
        showErrorMessage('Не удалось скопировать ссылку');
    }

    document.body.removeChild(textarea);
}

// Показать сообщение об успехе
function showSuccessMessage(message) {
    const successElement = document.createElement('div');
    successElement.className = 'success-message';
    successElement.innerHTML = `
        <i class="fas fa-check-circle"></i>
        <span>${message}</span>
    `;

    document.body.appendChild(successElement);

    // Анимация появления и исчезновения
    setTimeout(() => {
        successElement.classList.add('show');
    }, 10);

    setTimeout(() => {
        successElement.classList.remove('show');
        setTimeout(() => {
            successElement.remove();
        }, 300);
    }, 3000);
}

// Функция debounce для предотвращения слишком частых вызовов функции
function debounce(func, wait) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

// Добавляем стили для новых элементов
function addDynamicStyles() {
    const style = document.createElement('style');
    style.textContent = `
        .loading-indicator {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 2rem;
            width: 100%;
        }

        .spinner {
            display: flex;
            justify-content: center;
            margin-bottom: 1rem;
        }

        .spinner > div {
            width: 12px;
            height: 12px;
            background-color: var(--primary-color);
            border-radius: 100%;
            display: inline-block;
            animation: bounce 1.4s infinite ease-in-out both;
            margin: 0 3px;
        }

        .spinner .bounce1 {
            animation-delay: -0.32s;
        }

        .spinner .bounce2 {
            animation-delay: -0.16s;
        }

        @keyframes bounce {
            0%, 80%, 100% {
                transform: scale(0);
            } 40% {
                transform: scale(1.0);
            }
        }

        .no-results {
            text-align: center;
            padding: 3rem;
            width: 100%;
        }

        .no-results i {
            font-size: 3rem;
            color: var(--text-light);
            margin-bottom: 1rem;
        }

        .no-results h3 {
            font-size: 1.5rem;
            margin-bottom: 0.5rem;
            color: var(--text-color);
        }

        .no-results p {
            color: var(--text-light);
        }

        .error-message, .success-message {
            position: fixed;
            bottom: 20px;
            left: 50%;
            transform: translateX(-50%) translateY(100px);
            padding: 12px 20px;
            border-radius: var(--border-radius);
            color: white;
            font-size: 0.9rem;
            z-index: 1000;
            display: flex;
            align-items: center;
            gap: 8px;
            opacity: 0;
            transition: transform 0.3s ease, opacity 0.3s ease;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        .error-message {
            background-color: var(--error-color);
        }

        .success-message {
            background-color: var(--success-color);
        }

        .error-message.show, .success-message.show {
            transform: translateX(-50%) translateY(0);
            opacity: 1;
        }

        .animate-on-scroll {
            opacity: 0;
            transform: translateY(20px);
            transition: opacity 0.5s ease, transform 0.5s ease;
        }

        .animate-on-scroll.animated {
            opacity: 1;
            transform: translateY(0);
        }

        .like-btn.liked {
            animation: pulse 0.5s ease-out;
        }

        @keyframes pulse {
            0% {
                transform: scale(1);
            }
            50% {
                transform: scale(1.2);
            }
            100% {
                transform: scale(1);
            }
        }

        .sentinel {
            width: 100%;
            height: 20px;
        }
    `;

    document.head.appendChild(style);
}

// Вызываем функцию добавления стилей
addDynamicStyles();

// Обработчик сохранения пина
async function handleSavePin(pinId) {
    if (!currentUser) {
        showErrorMessage('Для сохранения пина необходимо войти в аккаунт');
        openModal(loginModal);
        return;
    }

    try {
        const response = await fetch(`/api/pins/${pinId}/save`, {
            method: 'POST',
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Ошибка при сохранении пина');
        }

        showSuccessMessage('Пин успешно сохранен');

    } catch (error) {
        console.error('Save pin error:', error);
        showErrorMessage(error.message || 'Ошибка при сохранении пина');
    }
}