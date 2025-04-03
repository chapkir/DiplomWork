// Глобальные переменные
let currentUser = null;

// DOM элементы
const loginBtn = document.getElementById('loginBtn');
const registerBtn = document.getElementById('registerBtn');
const profileBtn = document.getElementById('profileBtn');
const logoutBtn = document.getElementById('logoutBtn');

// Инициализация приложения
document.addEventListener('DOMContentLoaded', initApp);

function initApp() {
    checkAuth();
    setupEventListeners();
    animateElements();
}

function setupEventListeners() {
    loginBtn.addEventListener('click', () => window.location.href = '/?login=true');
    registerBtn.addEventListener('click', () => window.location.href = '/?register=true');
    profileBtn.addEventListener('click', () => window.location.href = '/profile.html');
    logoutBtn.addEventListener('click', logout);
}

function animateElements() {
    const elements = [
        document.querySelector('.about-banner'),
        document.querySelector('.about-section'),
        document.querySelector('.team-section'),
        document.querySelector('.join-section')
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
    .catch(() => {
        showMessage('Ошибка при выходе из аккаунта', 'error');
    });
}

function showMessage(message, type = 'info') {
    const existingMessage = document.querySelector('.message-toast');
    if (existingMessage) {
        existingMessage.remove();
    }

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

    setTimeout(() => {
        messageEl.style.transform = 'translateY(0)';
        messageEl.style.opacity = '1';
    }, 10);

    const timeout = setTimeout(() => {
        hideMessage(messageEl);
    }, 5000);

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