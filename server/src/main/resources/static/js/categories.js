// Глобальные переменные
let currentUser = null;
let categories = [];

// DOM элементы
const categoriesGrid = document.getElementById('categoriesGrid');

// Кнопки авторизации
const loginBtn = document.getElementById('loginBtn');
const registerBtn = document.getElementById('registerBtn');
const profileBtn = document.getElementById('profileBtn');
const logoutBtn = document.getElementById('logoutBtn');

// Инициализация приложения
document.addEventListener('DOMContentLoaded', initApp);

function initApp() {
    // Проверка авторизации
    checkAuth();

    // Загрузка категорий
    loadCategories();

    // Настройка обработчиков событий
    setupEventListeners();

    // Анимация элементов
    animateElements();
}

function setupEventListeners() {
    // Обработчики для кнопок авторизации
    loginBtn.addEventListener('click', () => window.location.href = '/?login=true');
    registerBtn.addEventListener('click', () => window.location.href = '/?register=true');
    profileBtn.addEventListener('click', () => window.location.href = '/profile.html');
    logoutBtn.addEventListener('click', logout);
}

function animateElements() {
    // Анимация появления элементов с задержкой
    const elements = [
        document.querySelector('.categories-header'),
        document.querySelector('.categories-grid')
    ];

    elements.forEach((el, index) => {
        if (el) {
            el.style.opacity = '0';
            el.style.transform = 'translateY(20px)';

            setTimeout(() => {
                el.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                el.style.opacity = '1';
                el.style.transform = 'translateY(0)';
            }, 100 * index);
        }
    });
}

// Проверка авторизации
function checkAuth() {
    fetch('/api/auth/check', {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => {
        if (response.ok) return response.json();
        throw new Error('Не авторизован');
    })
    .then(data => {
        currentUser = data;
        updateAuthUI(true);
    })
    .catch(() => {
        currentUser = null;
        updateAuthUI(false);
    });
}

// Обновление UI в зависимости от статуса авторизации
function updateAuthUI(isAuthenticated) {
    if (isAuthenticated) {
        loginBtn.style.display = 'none';
        registerBtn.style.display = 'none';
        profileBtn.style.display = 'inline-flex';
        logoutBtn.style.display = 'inline-flex';
    } else {
        loginBtn.style.display = 'inline-flex';
        registerBtn.style.display = 'inline-flex';
        profileBtn.style.display = 'none';
        logoutBtn.style.display = 'none';
    }
}

// Загрузка категорий
function loadCategories() {
    showLoading();

    // Если API для категорий не реализован, используем моковые данные
    // В реальном приложении здесь будет запрос к API
    setTimeout(() => {
        categories = getMockCategories();
        renderCategories();
        hideLoading();
    }, 500);

    // Для реального API:
    /*
    fetch('/api/categories')
        .then(response => {
            if (!response.ok) throw new Error('Ошибка загрузки категорий');
            return response.json();
        })
        .then(data => {
            categories = data;
            renderCategories();
            hideLoading();
        })
        .catch(error => {
            showError(error.message);
            hideLoading();
        });
    */
}

// Отображение категорий
function renderCategories() {
    categoriesGrid.innerHTML = '';

    if (categories.length === 0) {
        categoriesGrid.innerHTML = '<p class="no-categories">Категории не найдены</p>';
        return;
    }

    categories.forEach((category, index) => {
        const categoryEl = document.createElement('div');
        categoryEl.className = 'category-card';
        categoryEl.style.animationDelay = `${index * 0.05}s`;
        categoryEl.innerHTML = `
            <img src="${category.imageUrl}" alt="${category.name}">
            <div class="category-overlay">
                <h3 class="category-name">${category.name}</h3>
                <p class="category-count">${category.pinsCount} пинов</p>
            </div>
        `;

        categoryEl.addEventListener('click', () => {
            window.location.href = `/explore.html?category=${category.id}`;
        });

        categoriesGrid.appendChild(categoryEl);
    });
}

// Выход из аккаунта
function logout() {
    fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include'
    })
    .then(() => {
        currentUser = null;
        updateAuthUI(false);
        showMessage('Вы вышли из аккаунта', 'success');
    })
    .catch(error => {
        showMessage('Ошибка при выходе из аккаунта', 'error');
    });
}

// Вспомогательные функции
function getMockCategories() {
    return [
        {
            id: 1,
            name: 'Искусство',
            pinsCount: 1245,
            imageUrl: 'https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=80'
        },
        {
            id: 2,
            name: 'Фотография',
            pinsCount: 2367,
            imageUrl: 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=80'
        },
        {
            id: 3,
            name: 'Дизайн',
            pinsCount: 1876,
            imageUrl: 'https://images.unsplash.com/photo-1561070791-2526d30994b5?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=80'
        },
        {
            id: 4,
            name: 'Мода',
            pinsCount: 3421,
            imageUrl: 'https://images.unsplash.com/photo-1445205170230-053b83016050?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=80'
        },
        {
            id: 5,
            name: 'Путешествия',
            pinsCount: 2789,
            imageUrl: 'https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=80'
        },
        {
            id: 6,
            name: 'Еда',
            pinsCount: 1932,
            imageUrl: 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=80'
        },
        {
            id: 7,
            name: 'Архитектура',
            pinsCount: 1543,
            imageUrl: 'https://images.unsplash.com/photo-1487958449943-2429e8be8625?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=80'
        },
        {
            id: 8,
            name: 'Природа',
            pinsCount: 2156,
            imageUrl: 'https://images.unsplash.com/photo-1501854140801-50d01698950b?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=80'
        },
        {
            id: 9,
            name: 'Технологии',
            pinsCount: 1678,
            imageUrl: 'https://images.unsplash.com/photo-1518770660439-4636190af475?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=80'
        },
        {
            id: 10,
            name: 'Спорт',
            pinsCount: 1432,
            imageUrl: 'https://images.unsplash.com/photo-1461896836934-ffe607ba8211?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=80'
        },
        {
            id: 11,
            name: 'Животные',
            pinsCount: 1876,
            imageUrl: 'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=80'
        },
        {
            id: 12,
            name: 'Интерьер',
            pinsCount: 1543,
            imageUrl: 'https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=80'
        }
    ];
}

function showLoading() {
    // Создаем элемент загрузки, если его нет
    if (!document.getElementById('loading-overlay')) {
        const loadingOverlay = document.createElement('div');
        loadingOverlay.id = 'loading-overlay';
        loadingOverlay.innerHTML = `
            <div class="loading-spinner">
                <i class="fas fa-spinner fa-spin"></i>
                <span>Загрузка...</span>
            </div>
        `;
        document.body.appendChild(loadingOverlay);
    }

    document.getElementById('loading-overlay').style.display = 'flex';
}

function hideLoading() {
    const loadingOverlay = document.getElementById('loading-overlay');
    if (loadingOverlay) {
        loadingOverlay.style.display = 'none';
    }
}

function showError(message) {
    showMessage(message, 'error');
}

function showMessage(message, type = 'info') {
    // Удаляем предыдущее сообщение, если оно есть
    const existingMessage = document.querySelector('.message-toast');
    if (existingMessage) {
        existingMessage.remove();
    }

    // Создаем новое сообщение
    const messageEl = document.createElement('div');
    messageEl.className = `message-toast ${type}`;
    messageEl.innerHTML = `
        <div class="message-content">
            <i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle'}"></i>
            <span>${message}</span>
        </div>
        <button class="close-btn"><i class="fas fa-times"></i></button>
    `;

    document.body.appendChild(messageEl);

    // Анимация появления
    setTimeout(() => {
        messageEl.style.transform = 'translateY(0)';
        messageEl.style.opacity = '1';
    }, 10);

    // Автоматическое скрытие через 5 секунд
    const timeout = setTimeout(() => {
        hideMessage(messageEl);
    }, 5000);

    // Обработчик для кнопки закрытия
    messageEl.querySelector('.close-btn').addEventListener('click', () => {
        clearTimeout(timeout);
        hideMessage(messageEl);
    });
}

function hideMessage(messageEl) {
    messageEl.style.transform = 'translateY(-20px)';
    messageEl.style.opacity = '0';

    setTimeout(() => {
        messageEl.remove();
    }, 300);
}

// Добавляем стили для элементов, созданных динамически
const dynamicStyles = document.createElement('style');
dynamicStyles.textContent = `
    #loading-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(255, 255, 255, 0.8);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 1000;
        backdrop-filter: blur(3px);
    }

    .loading-spinner {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 1rem;
        color: var(--primary-color);
        font-size: 1.5rem;
    }

    .loading-spinner i {
        font-size: 3rem;
    }

    .message-toast {
        position: fixed;
        bottom: 2rem;
        left: 50%;
        transform: translateX(-50%) translateY(20px);
        background-color: white;
        color: var(--text-color);
        padding: 1rem 1.5rem;
        border-radius: var(--border-radius);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
        z-index: 1000;
        opacity: 0;
        transition: transform 0.3s ease, opacity 0.3s ease;
        max-width: 90%;
        width: auto;
    }

    .message-toast.success {
        border-left: 4px solid var(--success-color);
    }

    .message-toast.error {
        border-left: 4px solid var(--error-color);
    }

    .message-toast.info {
        border-left: 4px solid var(--primary-color);
    }

    .message-content {
        display: flex;
        align-items: center;
        gap: 0.75rem;
    }

    .message-content i {
        font-size: 1.25rem;
    }

    .message-toast.success i {
        color: var(--success-color);
    }

    .message-toast.error i {
        color: var(--error-color);
    }

    .message-toast.info i {
        color: var(--primary-color);
    }

    .close-btn {
        background: none;
        border: none;
        cursor: pointer;
        color: var(--text-light);
        padding: 0.25rem;
        font-size: 0.9rem;
        transition: color 0.2s;
    }

    .close-btn:hover {
        color: var(--text-color);
    }

    @media (prefers-color-scheme: dark) {
        #loading-overlay {
            background-color: rgba(17, 24, 39, 0.8);
        }

        .message-toast {
            background-color: var(--background-alt);
        }
    }
`;

document.head.appendChild(dynamicStyles);