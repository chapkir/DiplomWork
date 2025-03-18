/**
 * Утилитные функции для сайта ИнспирЭхо
 */

// Добавление декоративных SVG паттернов на фон
function addDecorativeElements() {
    const patterns = [
        createDottedPattern(),
        createWavePattern(),
        createCirclePattern(),
        createTrianglePattern()
    ];

    patterns.forEach(pattern => {
        document.body.appendChild(pattern);
    });

    // Добавляем кнопку "Вверх" (Back to Top)
    addBackToTopButton();

    // Инициализируем эффект скролла для шапки
    initHeaderScroll();
}

// Создание паттерна с точками
function createDottedPattern() {
    const dotPattern = document.createElement('div');
    dotPattern.className = 'decorative-pattern dotted-pattern';
    dotPattern.innerHTML = `
        <svg width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
            <pattern id="dotPattern" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                <circle cx="2" cy="2" r="1" fill="#9c6644" opacity="0.3" />
            </pattern>
            <rect x="0" y="0" width="100%" height="100%" fill="url(#dotPattern)" />
        </svg>
    `;
    return dotPattern;
}

// Создание волнистого паттерна
function createWavePattern() {
    const wavePattern = document.createElement('div');
    wavePattern.className = 'decorative-pattern wave-pattern';
    wavePattern.innerHTML = `
        <svg width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
            <pattern id="wavePattern" x="0" y="0" width="100" height="20" patternUnits="userSpaceOnUse">
                <path d="M0,10 C30,15 70,5 100,10 L100,0 L0,0 Z" fill="#e6d7cc" opacity="0.4" />
            </pattern>
            <rect x="0" y="0" width="100%" height="100%" fill="url(#wavePattern)" />
        </svg>
    `;
    return wavePattern;
}

// Создание паттерна с кругами
function createCirclePattern() {
    const circlePattern = document.createElement('div');
    circlePattern.className = 'decorative-pattern circle-pattern';
    circlePattern.innerHTML = `
        <svg width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
            <pattern id="circlePattern" x="0" y="0" width="50" height="50" patternUnits="userSpaceOnUse">
                <circle cx="25" cy="25" r="8" fill="none" stroke="#b58863" stroke-width="1" opacity="0.2" />
            </pattern>
            <rect x="0" y="0" width="100%" height="100%" fill="url(#circlePattern)" />
        </svg>
    `;
    return circlePattern;
}

// Создание паттерна с треугольниками
function createTrianglePattern() {
    const trianglePattern = document.createElement('div');
    trianglePattern.className = 'decorative-pattern triangle-pattern';
    trianglePattern.innerHTML = `
        <svg width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
            <pattern id="trianglePattern" x="0" y="0" width="30" height="30" patternUnits="userSpaceOnUse">
                <path d="M15,5 L25,25 L5,25 Z" fill="#d6ad86" opacity="0.15" />
            </pattern>
            <rect x="0" y="0" width="100%" height="100%" fill="url(#trianglePattern)" />
        </svg>
    `;
    return trianglePattern;
}

// Инициализация утилит
document.addEventListener('DOMContentLoaded', () => {
    addDecorativeElements();
});

/**
 * Добавление кнопки "Вверх"
 */
function addBackToTopButton() {
    const backToTopBtn = document.querySelector('.back-to-top');
    if (!backToTopBtn) return;

    // Обработчик прокрутки для показа/скрытия кнопки
    window.addEventListener('scroll', () => {
        if (window.pageYOffset > 300) {
            backToTopBtn.classList.add('visible');
        } else {
            backToTopBtn.classList.remove('visible');
        }
    });

    // Обработчик клика для прокрутки вверх
    backToTopBtn.addEventListener('click', () => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
}

/**
 * Эффект изменения шапки при прокрутке
 */
function initHeaderScroll() {
    const header = document.querySelector('.header');
    if (!header) return;

    window.addEventListener('scroll', () => {
        if (window.pageYOffset > 50) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
    });
}

/**
 * Показ уведомлений
 * @param {string} message - Текст уведомления
 * @param {string} type - Тип уведомления (info, success, error, warning)
 * @param {number} duration - Длительность показа в миллисекундах
 * @returns {HTMLElement} - DOM-элемент уведомления
 */
function showNotification(message, type = 'info', duration = 3000) {
    const notification = document.createElement('div');
    notification.className = `message message-${type}`;

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

    // Анимация появления
    setTimeout(() => {
        notification.classList.add('visible');
    }, 10);

    // Автоматическое скрытие через указанное время
    const hideTimeout = setTimeout(() => {
        hideNotification(notification);
    }, duration);

    // Обработчик клика для закрытия уведомления
    const closeBtn = notification.querySelector('.message-close');
    closeBtn.addEventListener('click', () => {
        clearTimeout(hideTimeout);
        hideNotification(notification);
    });

    return notification;
}

/**
 * Скрытие уведомления
 * @param {HTMLElement} notification - DOM-элемент уведомления
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
 * Создание разделителя секций
 * @param {string} containerSelector - CSS-селектор контейнера
 * @param {string} icon - Название иконки FontAwesome
 */
function createSectionDivider(containerSelector, icon = 'feather') {
    const container = document.querySelector(containerSelector);
    if (!container) return;

    const divider = document.createElement('div');
    divider.className = 'section-divider';
    divider.innerHTML = `
        <div class="divider-icon">
            <i class="fas fa-${icon}"></i>
        </div>
    `;

    container.appendChild(divider);
}

/**
 * Дебаунс функции
 * @param {Function} func - Функция для дебаунса
 * @param {number} wait - Время задержки в миллисекундах
 * @returns {Function} - Функция с дебаунсом
 */
function debounce(func, wait = 300) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

/**
 * Форматирование даты
 * @param {string} dateString - Строка с датой в формате ISO
 * @returns {string} - Отформатированная дата
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;

    // Если меньше 24 часов - показываем "N часов назад"
    if (diff < 24 * 60 * 60 * 1000) {
        const hours = Math.round(diff / (60 * 60 * 1000));
        return hours === 0 ? 'Только что' : `${hours} ${declOfNum(hours, ['час', 'часа', 'часов'])} назад`;
    }

    // Если меньше 30 дней - показываем "N дней назад"
    if (diff < 30 * 24 * 60 * 60 * 1000) {
        const days = Math.round(diff / (24 * 60 * 60 * 1000));
        return `${days} ${declOfNum(days, ['день', 'дня', 'дней'])} назад`;
    }

    // Иначе показываем дату в формате "DD.MM.YYYY"
    return `${date.getDate().toString().padStart(2, '0')}.${(date.getMonth() + 1).toString().padStart(2, '0')}.${date.getFullYear()}`;
}

/**
 * Склонение числительных
 * @param {number} number - Число
 * @param {Array} titles - Массив вариантов склонения ['час', 'часа', 'часов']
 * @returns {string} - Правильное склонение
 */
function declOfNum(number, titles) {
    const cases = [2, 0, 1, 1, 1, 2];
    return titles[
        (number % 100 > 4 && number % 100 < 20)
            ? 2
            : cases[(number % 10 < 5) ? number % 10 : 5]
    ];
}

/**
 * Получение случайного цвета на основе строки
 * @param {string} str - Строка для генерации цвета
 * @returns {string} - HEX-код цвета
 */
function getRandomColor(str) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }

    const colors = [
        '#5E72E4', '#11CDEF', '#2DCE89', '#F6AD55',
        '#F5365C', '#9F7AEA', '#4FD1C5', '#FB6340'
    ];

    return colors[Math.abs(hash) % colors.length];
}

/**
 * Отложенная загрузка изображений
 */
function initLazyLoading() {
    if ('IntersectionObserver' in window) {
        const lazyImages = document.querySelectorAll('img[data-src]');

        const imageObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    img.src = img.dataset.src;
                    img.removeAttribute('data-src');
                    imageObserver.unobserve(img);
                }
            });
        });

        lazyImages.forEach(img => {
            imageObserver.observe(img);
        });
    } else {
        // Fallback для браузеров без поддержки IntersectionObserver
        const lazyImages = document.querySelectorAll('img[data-src]');
        lazyImages.forEach(img => {
            img.src = img.dataset.src;
            img.removeAttribute('data-src');
        });
    }
}

// Экспорт функций
window.utils = {
    showNotification,
    hideNotification,
    createSectionDivider,
    debounce,
    formatDate,
    getRandomColor,
    initLazyLoading
};

// Функция для форматирования чисел (например, для отображения лайков)
function formatNumber(num) {
    if (num === undefined || num === null) return '0';
    if (num < 1000) return num.toString();
    if (num < 1000000) return (num / 1000).toFixed(1) + 'K';
    return (num / 1000000).toFixed(1) + 'M';
}

// Функция для защиты от XSS
function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// Функция для копирования текста в буфер обмена
function copyToClipboard(text) {
    return new Promise((resolve, reject) => {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(text)
                .then(() => resolve(true))
                .catch(err => reject(err));
        } else {
            // Fallback для старых браузеров
            const textarea = document.createElement('textarea');
            textarea.value = text;
            textarea.style.position = 'fixed';  // Не влияет на layout
            document.body.appendChild(textarea);
            textarea.select();

            try {
                const successful = document.execCommand('copy');
                if (successful) {
                    resolve(true);
                } else {
                    reject(new Error('Не удалось скопировать текст'));
                }
            } catch (err) {
                reject(err);
            } finally {
                document.body.removeChild(textarea);
            }
        }
    });
}

// Функция для корректного отображения времени "прошло с момента публикации"
function timeAgo(date) {
    const seconds = Math.floor((new Date() - new Date(date)) / 1000);

    let interval = Math.floor(seconds / 31536000);
    if (interval > 1) {
        return interval + ' лет назад';
    } else if (interval === 1) {
        return 'год назад';
    }

    interval = Math.floor(seconds / 2592000);
    if (interval > 1) {
        return interval + ' месяцев назад';
    } else if (interval === 1) {
        return 'месяц назад';
    }

    interval = Math.floor(seconds / 86400);
    if (interval > 1) {
        return interval + ' дней назад';
    } else if (interval === 1) {
        return 'вчера';
    }

    interval = Math.floor(seconds / 3600);
    if (interval > 1) {
        return interval + ' часов назад';
    } else if (interval === 1) {
        return 'час назад';
    }

    interval = Math.floor(seconds / 60);
    if (interval > 1) {
        return interval + ' минут назад';
    } else if (interval === 1) {
        return 'минуту назад';
    }

    if (seconds < 10) {
        return 'только что';
    }

    return Math.floor(seconds) + ' секунд назад';
}

// Функция для создания случайного ID
function generateRandomId(length = 10) {
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let result = '';
    const charactersLength = characters.length;
    for (let i = 0; i < length; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

// Функция для проверки поддержки веб-хранилища
function isLocalStorageAvailable() {
    try {
        const test = 'test';
        localStorage.setItem(test, test);
        localStorage.removeItem(test);
        return true;
    } catch(e) {
        return false;
    }
}

// Функция для определения типа устройства
function isMobileDevice() {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
}

// Функция для преобразования первой буквы в заглавную
function capitalizeFirstLetter(string) {
    if (!string) return '';
    return string.charAt(0).toUpperCase() + string.slice(1);
}

// Функция для извлечения доменного имени из URL
function extractDomain(url) {
    if (!url) return '';

    try {
        const domain = new URL(url).hostname;
        return domain.replace('www.', '');
    } catch (e) {
        return '';
    }
}

// Функция для валидации электронной почты
function isValidEmail(email) {
    const re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}

// Функция для валидации пароля (минимум 6 символов, хотя бы 1 буква и 1 цифра)
function isValidPassword(password) {
    return /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$/.test(password);
}