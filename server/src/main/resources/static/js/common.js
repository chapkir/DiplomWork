/**
 * Общий JavaScript файл для всех страниц сайта
 */

document.addEventListener('DOMContentLoaded', function() {
    // Инициализация компонентов
    initializeHeader();
    setupModals();
    setupActionButtons();
    setupForms();
    setupThemeToggle();
    initScrollEffects();
});

/**
 * Инициализация шапки сайта
 */
function initializeHeader() {
    const header = document.querySelector('.header');
    const menuToggle = document.querySelector('.menu-toggle');
    const nav = document.querySelector('.nav');

    if (menuToggle && nav) {
        menuToggle.addEventListener('click', () => {
            menuToggle.classList.toggle('active');
            nav.classList.toggle('active');
        });
    }

    // Подсветка активного пункта меню
    highlightActiveMenuItem();
}

/**
 * Подсветка активного пункта меню
 */
function highlightActiveMenuItem() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-link');

    navLinks.forEach(link => {
        const linkPath = link.getAttribute('href');
        if (linkPath === currentPath ||
            (currentPath === '/' && linkPath === '/') ||
            (currentPath !== '/' && linkPath !== '/' && currentPath.includes(linkPath))) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}

/**
 * Настройка модальных окон
 */
function setupModals() {
    // Кнопки открытия модальных окон
    const modalTriggers = document.querySelectorAll('[data-modal]');
    const closeButtons = document.querySelectorAll('[data-close-modal], .modal-backdrop');

    // Обработчики кнопок открытия
    modalTriggers.forEach(trigger => {
        trigger.addEventListener('click', (e) => {
            e.preventDefault();
            const modalId = trigger.getAttribute('data-modal');
            const modal = document.getElementById(modalId);
            if (modal) {
                openModal(modal);

                // Закрываем предыдущее модальное окно, если открывается новое через ссылку внутри модалки
                if (trigger.closest('.modal')) {
                    closeModal(trigger.closest('.modal'));
                }
            }
        });
    });

    // Обработчики кнопок закрытия
    closeButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            const modal = button.closest('.modal');
            if (modal) {
                closeModal(modal);
            }
        });
    });

    // Закрытие по клику на бэкдроп
    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal-backdrop')) {
            const modal = e.target.closest('.modal');
            if (modal) {
                closeModal(modal);
            }
        }
    });

    // Закрытие по Escape
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            const openModals = document.querySelectorAll('.modal.visible');
            if (openModals.length > 0) {
                closeModal(openModals[openModals.length - 1]);
            }
        }
    });
}

/**
 * Открытие модального окна
 */
function openModal(modal) {
    if (!modal) return;
    modal.classList.add('visible');
    document.body.style.overflow = 'hidden';

    // Фокус на первый input в модальном окне
    setTimeout(() => {
        const firstInput = modal.querySelector('input, button:not(.close-modal)');
        if (firstInput) {
            firstInput.focus();
        }
    }, 100);
}

/**
 * Закрытие модального окна
 */
function closeModal(modal) {
    if (!modal) return;
    modal.classList.remove('visible');

    // Проверяем, открыты ли еще модальные окна
    const visibleModals = document.querySelectorAll('.modal.visible');
    if (visibleModals.length === 0) {
        document.body.style.overflow = '';
    }
}

/**
 * Настройка кнопок действий (лайк, сохранение и т.д.)
 */
function setupActionButtons() {
    // Лайки
    const likeButtons = document.querySelectorAll('.like-btn');
    likeButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            const pinId = button.dataset.id || button.closest('[data-id]')?.dataset.id;
            if (pinId) {
                toggleLike(pinId, button);
            }
        });
    });

    // Сохранение в избранное
    const favoriteButtons = document.querySelectorAll('.favorite-btn');
    favoriteButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            const pinId = button.dataset.id || button.closest('[data-id]')?.dataset.id;
            if (pinId) {
                toggleFavorite(pinId, button);
            }
        });
    });

    // Кнопка копирования ссылки
    const copyButton = document.getElementById('copyBtn');
    if (copyButton) {
        copyButton.addEventListener('click', () => {
            const shareUrl = document.getElementById('shareUrl');
            if (shareUrl) {
                copyToClipboard(shareUrl.value);
                showNotification('Ссылка скопирована в буфер обмена', 'success');
            }
        });
    }

    // Кнопки шаринга
    const shareButtons = document.querySelectorAll('.share-btn, [data-share]');
    shareButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            const pinId = button.dataset.id || button.closest('[data-id]')?.dataset.id;
            if (pinId) {
                openShareModal(pinId);
            }
        });
    });
}

/**
 * Переключение лайка
 */
function toggleLike(pinId, button) {
    // Проверка авторизации
    if (!isAuthenticated()) {
        showLoginPrompt();
        return;
    }

    // Визуальный эффект
    button.classList.toggle('active');
    button.classList.add('animate-like');
    setTimeout(() => {
        button.classList.remove('animate-like');
    }, 500);

    const isLiked = button.classList.contains('active');
    const likeCount = button.querySelector('.like-count');
    if (likeCount) {
        let count = parseInt(likeCount.textContent);
        likeCount.textContent = isLiked ? count + 1 : Math.max(0, count - 1);
    }

    // Отправка на сервер
    fetch(`/api/pins/${pinId}/like`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getToken()}`
        },
        body: JSON.stringify({ liked: isLiked })
    })
    .catch(error => {
        console.error('Ошибка при лайке:', error);
        showNotification('Не удалось выполнить действие', 'error');
        // Отмена визуального эффекта при ошибке
        button.classList.toggle('active');
        if (likeCount) {
            let count = parseInt(likeCount.textContent);
            likeCount.textContent = !isLiked ? count + 1 : Math.max(0, count - 1);
        }
    });
}

/**
 * Переключение добавления в избранное
 */
function toggleFavorite(pinId, button) {
    // Проверка авторизации
    if (!isAuthenticated()) {
        showLoginPrompt();
        return;
    }

    // Визуальный эффект
    button.classList.toggle('active');
    button.classList.add('animate-favorite');
    setTimeout(() => {
        button.classList.remove('animate-favorite');
    }, 500);

    const isFavorite = button.classList.contains('active');

    // Отправка на сервер
    fetch(`/api/pins/${pinId}/favorite`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getToken()}`
        },
        body: JSON.stringify({ favorite: isFavorite })
    })
    .catch(error => {
        console.error('Ошибка при добавлении в избранное:', error);
        showNotification('Не удалось выполнить действие', 'error');
        // Отмена визуального эффекта при ошибке
        button.classList.toggle('active');
    });
}

/**
 * Открытие модального окна для шаринга
 */
function openShareModal(pinId) {
    const shareModal = document.getElementById('shareModal');
    if (!shareModal) return;

    // Формирование URL для шаринга
    const shareUrl = `${window.location.origin}/pin.html?id=${pinId}`;
    const shareUrlInput = document.getElementById('shareUrl');
    if (shareUrlInput) {
        shareUrlInput.value = shareUrl;
    }

    // Обновление URL в кнопках соцсетей
    updateSocialShareUrls(shareUrl);

    // Открываем модальное окно
    openModal(shareModal);
}

/**
 * Обновление URL в кнопках социальных сетей
 */
function updateSocialShareUrls(url) {
    const encodedUrl = encodeURIComponent(url);
    const encodedTitle = encodeURIComponent(document.title);

    // ВКонтакте
    const vkButton = document.querySelector('.social-share-btn.vk');
    if (vkButton) {
        vkButton.href = `https://vk.com/share.php?url=${encodedUrl}&title=${encodedTitle}`;
    }

    // Telegram
    const telegramButton = document.querySelector('.social-share-btn.telegram');
    if (telegramButton) {
        telegramButton.href = `https://t.me/share/url?url=${encodedUrl}&text=${encodedTitle}`;
    }

    // WhatsApp
    const whatsappButton = document.querySelector('.social-share-btn.whatsapp');
    if (whatsappButton) {
        whatsappButton.href = `https://api.whatsapp.com/send?text=${encodedTitle} ${encodedUrl}`;
    }
}

/**
 * Настройка форм
 */
function setupForms() {
    // Форма входа
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', (e) => {
            e.preventDefault();

            const email = document.getElementById('loginEmail').value;
            const password = document.getElementById('loginPassword').value;
            const rememberMe = document.getElementById('rememberMe').checked;

            if (!validateForm(loginForm)) return;

            // Имитация отправки формы (в реальном проекте здесь был бы запрос на сервер)
            showNotification('Вход выполнен успешно', 'success');
            closeModal(document.getElementById('loginModal'));
        });
    }

    // Форма регистрации
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', (e) => {
            e.preventDefault();

            const name = document.getElementById('registerName').value;
            const email = document.getElementById('registerEmail').value;
            const password = document.getElementById('registerPassword').value;
            const passwordConfirm = document.getElementById('registerPasswordConfirm').value;

            if (!validateForm(registerForm)) return;

            // Имитация отправки формы (в реальном проекте здесь был бы запрос на сервер)
            showNotification('Регистрация выполнена успешно', 'success');
            closeModal(document.getElementById('registerModal'));
        });
    }
}

/**
 * Валидация формы
 */
function validateForm(form) {
    let isValid = true;

    // Проверка всех обязательных полей
    const requiredInputs = form.querySelectorAll('[required]');
    requiredInputs.forEach(input => {
        if (!input.value.trim()) {
            markInvalid(input, 'Это поле обязательно для заполнения');
            isValid = false;
        } else {
            markValid(input);
        }
    });

    // Специфичные проверки для email
    const emailInputs = form.querySelectorAll('input[type="email"]');
    emailInputs.forEach(input => {
        if (input.value.trim() && !isValidEmail(input.value)) {
            markInvalid(input, 'Введите корректный email');
            isValid = false;
        }
    });

    // Проверка паролей при регистрации
    const passwordInput = form.querySelector('#registerPassword');
    const confirmInput = form.querySelector('#registerPasswordConfirm');
    if (passwordInput && confirmInput && passwordInput.value !== confirmInput.value) {
        markInvalid(confirmInput, 'Пароли не совпадают');
        isValid = false;
    }

    return isValid;
}

/**
 * Валидация email
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * Отметка поля как невалидного
 */
function markInvalid(input, message) {
    input.classList.add('error');

    // Удаляем предыдущее сообщение об ошибке, если оно есть
    const parent = input.parentElement;
    const existingError = parent.querySelector('.error-message');
    if (existingError) {
        parent.removeChild(existingError);
    }

    // Создаем и добавляем новое сообщение об ошибке
    const errorMessage = document.createElement('div');
    errorMessage.className = 'error-message';
    errorMessage.textContent = message;
    parent.appendChild(errorMessage);
}

/**
 * Отметка поля как валидного
 */
function markValid(input) {
    input.classList.remove('error');

    // Удаляем сообщение об ошибке, если оно есть
    const parent = input.parentElement;
    const existingError = parent.querySelector('.error-message');
    if (existingError) {
        parent.removeChild(existingError);
    }
}

/**
 * Показ уведомления
 */
function showNotification(message, type = 'info') {
    // Проверяем, есть ли utils.js
    if (window.utils && window.utils.showNotification) {
        window.utils.showNotification(message, type);
        return;
    }

    // Резервная реализация, если utils.js не подключен
    const notification = document.createElement('div');
    notification.className = `message message-${type} visible`;

    let icon = 'info-circle';
    if (type === 'success') icon = 'check-circle';
    if (type === 'error') icon = 'exclamation-circle';
    if (type === 'warning') icon = 'exclamation-triangle';

    notification.innerHTML = `
        <div class="message-content">
            <i class="fas fa-${icon}"></i>
            <p>${message}</p>
        </div>
        <button class="message-close" aria-label="Закрыть"><i class="fas fa-times"></i></button>
    `;

    document.body.appendChild(notification);

    // Автоматическое скрытие через 3 секунды
    const hideTimeout = setTimeout(() => {
        hideNotification(notification);
    }, 3000);

    // Обработчик клика для закрытия уведомления
    const closeBtn = notification.querySelector('.message-close');
    closeBtn.addEventListener('click', () => {
        clearTimeout(hideTimeout);
        hideNotification(notification);
    });
}

/**
 * Скрытие уведомления
 */
function hideNotification(notification) {
    notification.classList.add('message-hiding');
    notification.classList.remove('visible');

    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 300);
}

/**
 * Копирование текста в буфер обмена
 */
function copyToClipboard(text) {
    // Создаем временный элемент input
    const tempInput = document.createElement('input');
    tempInput.value = text;
    document.body.appendChild(tempInput);

    // Выделяем и копируем текст
    tempInput.select();
    document.execCommand('copy');

    // Удаляем временный элемент
    document.body.removeChild(tempInput);
}

/**
 * Проверка авторизации
 */
function isAuthenticated() {
    return !!getToken();
}

/**
 * Получение токена из localStorage
 */
function getToken() {
    return localStorage.getItem('token');
}

/**
 * Показ запроса на авторизацию
 */
function showLoginPrompt() {
    const loginModal = document.getElementById('loginModal');
    if (loginModal) {
        openModal(loginModal);
    } else {
        showNotification('Необходимо войти для выполнения этого действия', 'info');
    }
}

/**
 * Настройка переключателя темы
 */
function setupThemeToggle() {
    const themeToggle = document.getElementById('themeToggle');
    if (!themeToggle) return;

    // Проверяем текущую тему
    const isDarkMode = localStorage.getItem('darkMode') === 'true';
    updateThemeIcon(themeToggle, isDarkMode);

    if (isDarkMode) {
        document.documentElement.classList.add('dark-mode');
    }

    // Обработчик клика
    themeToggle.addEventListener('click', () => {
        const isDark = document.documentElement.classList.toggle('dark-mode');
        localStorage.setItem('darkMode', isDark);
        updateThemeIcon(themeToggle, isDark);
    });
}

/**
 * Обновление иконки переключателя темы
 */
function updateThemeIcon(button, isDarkMode) {
    const icon = button.querySelector('i');
    if (icon) {
        icon.className = isDarkMode ? 'fas fa-sun' : 'fas fa-moon';
    }
}

/**
 * Инициализация эффектов при скролле
 */
function initScrollEffects() {
    // Кнопка "Наверх"
    const backToTopButton = document.querySelector('.back-to-top');
    if (backToTopButton) {
        window.addEventListener('scroll', () => {
            if (window.pageYOffset > 300) {
                backToTopButton.classList.add('visible');
            } else {
                backToTopButton.classList.remove('visible');
            }
        });

        backToTopButton.addEventListener('click', () => {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    }

    // Эффект анимации при прокрутке для шапки
    const header = document.querySelector('.header');
    if (header) {
        window.addEventListener('scroll', () => {
            if (window.pageYOffset > 50) {
                header.classList.add('scrolled');
            } else {
                header.classList.remove('scrolled');
            }
        });
    }
}

// Экспорт функций для использования в других файлах
window.common = {
    openModal,
    closeModal,
    showNotification,
    hideNotification,
    copyToClipboard,
    isAuthenticated,
    getToken
};