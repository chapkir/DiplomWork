// Global variables
let currentPin = null;
let pinAuthor = null;
let isAuthenticated = false;
let currentUser = null;
const API_BASE_URL = window.location.origin;

// DOM elements
const pinImage = document.getElementById('pinImage');
const pinTitle = document.getElementById('pinTitle');
const pinDescription = document.getElementById('pinDescription');
const authorName = document.getElementById('authorName');
const likesCount = document.getElementById('likesCount');
const commentsCount = document.getElementById('commentsCount');
const commentsList = document.getElementById('commentsList');
const commentInput = document.getElementById('commentInput');
const submitCommentBtn = document.getElementById('submitCommentBtn');
const saveBtn = document.getElementById('saveBtn');
const likeBtn = document.getElementById('likeBtn');
const shareBtn = document.getElementById('shareBtn');
const followBtn = document.getElementById('followBtn');
const relatedPinsGrid = document.getElementById('relatedPinsGrid');

// Login/Register buttons
const loginBtn = document.getElementById('loginBtn');
const registerBtn = document.getElementById('registerBtn');
const profileBtn = document.getElementById('profileBtn');
const logoutBtn = document.getElementById('logoutBtn');

// Initialize application
document.addEventListener('DOMContentLoaded', initApp);

function initApp() {
    // Get pin ID from URL
    const urlParams = new URLSearchParams(window.location.search);
    const pinId = urlParams.get('id');

    if (!pinId) {
        showError('Идентификатор идеи не найден');
        return;
    }

    // Setup event listeners
    setupEventListeners();

    // Check authentication status
    checkAuth().then(() => {
        // Load pin data
        loadPin(pinId);

        // Animate elements
        animateElements();
    });
}

function setupEventListeners() {
    // Comment form
    const commentForm = document.getElementById('commentForm');
    if (commentForm) {
        commentForm.addEventListener('submit', function(e) {
            e.preventDefault();
            submitComment();
        });
    }

    // Like button
    const likeBtn = document.getElementById('likeBtn');
    if (likeBtn) {
        likeBtn.addEventListener('click', toggleLike);
    }

    // Save button
    const saveBtn = document.getElementById('saveBtn');
    if (saveBtn) {
        saveBtn.addEventListener('click', savePin);
    }

    // Share button
    const shareBtn = document.getElementById('shareBtn');
    if (shareBtn) {
        shareBtn.addEventListener('click', sharePin);
    }

    // Follow button
    const followBtn = document.getElementById('followBtn');
    if (followBtn) {
        followBtn.addEventListener('click', toggleFollow);
    }

    // Login/Register buttons
    const loginBtn = document.getElementById('loginBtn');
    if (loginBtn) {
        loginBtn.addEventListener('click', function() {
            openModal(document.getElementById('loginModal'));
        });
    }

    const registerBtn = document.getElementById('registerBtn');
    if (registerBtn) {
        registerBtn.addEventListener('click', function() {
            openModal(document.getElementById('registerModal'));
        });
    }

    // Logout button
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }

    // Modal buttons
    document.getElementById('commentLoginBtn')?.addEventListener('click', (e) => {
        e.preventDefault();
        openModal(document.getElementById('loginModal'));
    });

    document.querySelectorAll('.close-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const modal = btn.closest('.modal');
            if (modal) modal.classList.remove('active');
        });
    });

    // Login form submission
    document.getElementById('loginForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        // Login logic...
    });

    // Register form submission
    document.getElementById('registerForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        // Registration logic...
    });
}

function animateElements() {
    const pinContainer = document.querySelector('.pin-container');
    if (pinContainer) {
        pinContainer.classList.add('animate');
    }

    const relatedPinsSection = document.querySelector('.related-pins');
    if (relatedPinsSection) {
        relatedPinsSection.classList.add('animate');
    }
}

async function checkAuth() {
    try {
        const token = localStorage.getItem('token');

        if (!token) {
            updateAuthUI(false);
            return;
        }

        const response = await fetch('/api/auth/check', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const userData = await response.json();
            currentUser = userData;
            localStorage.setItem('currentUser', JSON.stringify(userData));
            isAuthenticated = true;
            updateAuthUI(true);
        } else {
            localStorage.removeItem('token');
            localStorage.removeItem('currentUser');
            isAuthenticated = false;
            updateAuthUI(false);
        }
    } catch (error) {
        console.error('Ошибка при проверке авторизации:', error);
        isAuthenticated = false;
        updateAuthUI(false);
    }
}

function updateAuthUI(isAuthenticated) {
    const loginBtn = document.getElementById('loginBtn');
    const registerBtn = document.getElementById('registerBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    const profileBtn = document.getElementById('profileBtn');
    const commentForm = document.getElementById('commentForm');
    const loginForComments = document.getElementById('loginForComments');

    if (isAuthenticated) {
        // Пользователь авторизован
        if (loginBtn) loginBtn.style.display = 'none';
        if (registerBtn) registerBtn.style.display = 'none';
        if (logoutBtn) logoutBtn.style.display = 'inline-flex';
        if (profileBtn) {
            profileBtn.style.display = 'inline-flex';
            // Устанавливаем ссылку на профиль пользователя
            profileBtn.href = `/profile.html?username=${encodeURIComponent(currentUser.username)}`;
        }

        // Показываем форму комментариев и скрываем приглашение войти
        if (commentForm) commentForm.style.display = 'block';
        if (loginForComments) loginForComments.style.display = 'none';
    } else {
        // Гость
        if (loginBtn) loginBtn.style.display = 'inline-flex';
        if (registerBtn) registerBtn.style.display = 'inline-flex';
        if (logoutBtn) logoutBtn.style.display = 'none';
        if (profileBtn) profileBtn.style.display = 'none';

        // Скрываем форму комментариев и показываем приглашение войти
        if (commentForm) commentForm.style.display = 'none';
        if (loginForComments) loginForComments.style.display = 'block';
    }
}

async function loadPin(pinId) {
    showLoading();
    try {
        const token = localStorage.getItem('token');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};

        const response = await fetch(`/api/pins/${pinId}`, { headers });

        if (!response.ok) {
            if (response.status === 404) {
                throw new Error("Идея не найдена");
            }
            throw new Error("Ошибка при загрузке идеи");
        }

        const data = await response.json();
        currentPin = data;

        // Обновляем метаданные для SEO
        document.title = `${data.description ? data.description.substring(0, 50) + '...' : 'Просмотр идеи'} | ИнспирЭхо`;

        // Загружаем данные об авторе
        await loadAuthorData(data.user);

        // Обновляем UI
        updatePinUI();

        // Загружаем комментарии для пина
        await loadComments(pinId);

        // Загружаем похожие пины (потенциально на основе тегов или категории)
        const category = data.description ? data.description.split(' ')[0] : '';
        await loadRelatedPins(category);

        hideLoading();
    } catch (error) {
        console.error('Ошибка при загрузке пина:', error);
        hideLoading();
        showError(error.message || "Произошла ошибка при загрузке идеи");
    }
}

async function loadAuthorData(userId) {
    try {
        const response = await fetch(`/api/users/${userId}`);
        if (!response.ok) {
            throw new Error("Не удалось загрузить информацию об авторе");
        }

        const data = await response.json();
        pinAuthor = data;
    } catch (error) {
        console.error('Ошибка при загрузке данных автора:', error);
        // Ошибка загрузки автора не должна прерывать отображение пина
    }
}

function updatePinUI() {
    const pinImage = document.getElementById('pinImage');
    const pinTitle = document.getElementById('pinTitle');
    const pinDescription = document.getElementById('pinDescription');
    const likesCount = document.getElementById('likesCount');
    const commentsCount = document.getElementById('commentsCount');
    const commentCountLabel = document.getElementById('commentCountLabel');
    const createdAt = document.getElementById('createdAt');
    const authorName = document.getElementById('authorName');
    const authorAvatar = document.getElementById('authorAvatar');
    const authorBio = document.getElementById('authorBio');

    // Отображаем данные пина
    pinImage.src = currentPin.imageUrl;
    pinImage.alt = currentPin.description || "Изображение идеи";

    pinTitle.textContent = currentPin.description ?
        currentPin.description.substring(0, 50) + (currentPin.description.length > 50 ? '...' : '') :
        "Идея без описания";

    pinDescription.textContent = currentPin.description || "Для этой идеи не добавлено описание.";

    likesCount.textContent = currentPin.likesCount || 0;
    commentsCount.textContent = currentPin.comments ? currentPin.comments.length : 0;
    commentCountLabel.textContent = `(${currentPin.comments ? currentPin.comments.length : 0})`;

    // Форматируем дату
    if (currentPin.createdAt) {
        createdAt.textContent = formatDate(currentPin.createdAt);
    } else {
        createdAt.textContent = "Недавно";
    }

    // Отображаем данные автора
    if (pinAuthor) {
        authorName.textContent = pinAuthor.username || "Неизвестный автор";

        if (pinAuthor.profileImageUrl) {
            authorAvatar.src = pinAuthor.profileImageUrl;
        } else {
            // Генерируем цвет аватара на основе имени пользователя
            const color = getRandomColor(pinAuthor.username);
            authorAvatar.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(pinAuthor.username)}&background=${color.replace('#', '')}&color=fff`;
        }

        authorBio.textContent = pinAuthor.bio || "У этого пользователя нет описания.";
    }

    // Обновляем статус кнопок лайка и сохранения
    updateLikeButtonStatus();
}

// Функция для генерации случайного цвета на основе имени пользователя
function getRandomColor(username) {
    if (!username) return '#F76F53'; // дефолтный цвет

    let hash = 0;
    for (let i = 0; i < username.length; i++) {
        hash = username.charCodeAt(i) + ((hash << 5) - hash);
    }

    const colors = ['#F76F53', '#36a2eb', '#4bc0c0', '#9966ff', '#ff9f40'];
    const index = Math.abs(hash) % colors.length;
    return colors[index];
}

async function loadComments(pinId) {
    try {
        const response = await fetch(`/api/pins/${pinId}/comments`);

        if (!response.ok) {
            throw new Error('Не удалось загрузить комментарии');
        }

        const commentsData = await response.json();
        currentPin.comments = commentsData;

        renderComments();
    } catch (error) {
        console.error('Ошибка при загрузке комментариев:', error);
        // Ошибка загрузки комментариев не должна прерывать отображение пина
    }
}

function renderComments() {
    const commentsList = document.getElementById('commentsList');
    const commentsCount = document.getElementById('commentsCount');
    const commentCountLabel = document.getElementById('commentCountLabel');

    if (!commentsList) return;

    commentsList.innerHTML = '';

    if (!currentPin.comments || currentPin.comments.length === 0) {
        commentsList.innerHTML = `
            <div class="no-comments">
                <p>Пока нет комментариев. Будьте первым!</p>
            </div>
        `;
        commentsCount.textContent = '0';
        commentCountLabel.textContent = '(0)';
        return;
    }

    commentsCount.textContent = currentPin.comments.length;
    commentCountLabel.textContent = `(${currentPin.comments.length})`;

    currentPin.comments.forEach(comment => {
        const commentElement = document.createElement('div');
        commentElement.className = 'comment-item';
        commentElement.innerHTML = `
            <div class="comment-header">
                <a href="/profile.html?username=${encodeURIComponent(comment.username)}" class="comment-author">${comment.username}</a>
                <span class="comment-date">${formatDate(comment.createdAt || new Date())}</span>
            </div>
            <div class="comment-content">${comment.text}</div>
        `;
        commentsList.appendChild(commentElement);
    });
}

async function loadRelatedPins(category) {
    try {
        const response = await fetch(`${API_BASE_URL}/api/pins?category=${category}&size=4`);

        if (!response.ok) {
            throw new Error('Не удалось загрузить похожие изображения');
        }

        const data = await response.json();

        if (data.content && Array.isArray(data.content)) {
            relatedPins = data.content.filter(relatedPin => relatedPin.id !== pin.id).slice(0, 4);
        } else if (Array.isArray(data)) {
            relatedPins = data.filter(relatedPin => relatedPin.id !== pin.id).slice(0, 4);
        }

        renderRelatedPins();
    } catch (error) {
        console.error('Error loading related pins:', error);
    }
}

function renderRelatedPins() {
    const relatedPinsGrid = document.querySelector('.related-pins .grid');
    if (!relatedPinsGrid) return;

    if (relatedPins.length === 0) {
        relatedPinsGrid.innerHTML = '<p class="no-related">Нет похожих изображений</p>';
        return;
    }

    relatedPinsGrid.innerHTML = '';

    relatedPins.forEach(relatedPin => {
        const pinElement = document.createElement('div');
        pinElement.className = 'image-card';

        pinElement.innerHTML = `
            <a href="/pin.html?id=${relatedPin.id}">
                <div class="image-container">
                    <img src="${relatedPin.imageUrl}" alt="${relatedPin.description || 'Изображение'}">
                </div>
                <div class="image-info">
                    <h3>${relatedPin.description || 'Без описания'}</h3>
                    <p><i class="fas fa-heart"></i> ${relatedPin.likesCount || 0}</p>
                </div>
            </a>
        `;

        relatedPinsGrid.appendChild(pinElement);
    });
}

async function submitComment() {
    if (!isAuthenticated) {
        showMessage('Пожалуйста, войдите в систему, чтобы оставить комментарий', 'info');
        openModal(document.getElementById('loginModal'));
        return;
    }

    const commentInput = document.getElementById('commentInput');
    const submitButton = document.getElementById('submitCommentBtn');

    const comment = commentInput.value.trim();

    if (!comment) {
        showError('Пожалуйста, введите комментарий');
        return;
    }

    try {
        commentInput.disabled = true;
        submitButton.disabled = true;

        const token = localStorage.getItem('token');
        const response = await fetch(`/api/pins/${currentPin.id}/comments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ text: comment })
        });

        if (!response.ok) {
            throw new Error('Не удалось добавить комментарий');
        }

        const data = await response.json();

        // Очищаем поле ввода
        commentInput.value = '';

        // Добавляем новый комментарий к списку
        const currentUser = JSON.parse(localStorage.getItem('currentUser'));
        if (!currentPin.comments) currentPin.comments = [];

        currentPin.comments.push({
            id: Date.now(), // Временный ID до перезагрузки
            text: comment,
            username: currentUser?.username || 'Вы',
            createdAt: new Date()
        });

        // Перерисовываем комментарии
        renderComments();

        showMessage('Комментарий успешно добавлен', 'success');
    } catch (error) {
        console.error('Ошибка при добавлении комментария:', error);
        showError('Произошла ошибка при добавлении комментария');
    } finally {
        commentInput.disabled = false;
        submitButton.disabled = false;
    }
}

async function toggleLike() {
    if (!isAuthenticated) {
        showMessage('Пожалуйста, войдите в систему, чтобы поставить лайк', 'info');
        openModal(document.getElementById('loginModal'));
        return;
    }

    const likeBtn = document.getElementById('likeBtn');
    const likesCount = document.getElementById('likesCount');

    try {
        likeBtn.disabled = true; // Отключаем кнопку на время запроса

        const token = localStorage.getItem('token');
        const method = currentPin.isLikedByCurrentUser ? 'DELETE' : 'POST';
        const url = `/api/pins/${currentPin.id}/likes`;

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Не удалось выполнить действие');
        }

        const data = await response.json();

        // Обновляем статус лайка и счетчик
        currentPin.isLikedByCurrentUser = !currentPin.isLikedByCurrentUser;
        currentPin.likesCount = data.likesCount;

        // Обновляем UI
        updateLikeButtonStatus();
        likesCount.textContent = data.likesCount;

        // Показываем сообщение
        const message = currentPin.isLikedByCurrentUser ?
            'Идея добавлена в избранное' :
            'Идея удалена из избранного';
        showMessage(message, 'success');

    } catch (error) {
        console.error('Ошибка при изменении статуса лайка:', error);
        showError('Произошла ошибка при обновлении статуса лайка');
    } finally {
        likeBtn.disabled = false; // Включаем кнопку после завершения запроса
    }
}

async function savePin() {
    const token = localStorage.getItem('token');
    if (!token) {
        showMessage('Пожалуйста, войдите в систему, чтобы сохранять изображения', 'warning');
        return;
    }

    // In a real application, you would send a request to save the pin
    // For now, we'll just show a success message
    showMessage('Изображение сохранено в вашей коллекции', 'success');
}

function sharePin() {
    const shareUrl = window.location.href;

    // Check if Web Share API is available
    if (navigator.share) {
        navigator.share({
            title: document.title,
            url: shareUrl
        }).catch(error => {
            console.error('Error sharing:', error);
            copyToClipboard(shareUrl);
        });
    } else {
        copyToClipboard(shareUrl);
    }
}

function copyToClipboard(text) {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    document.body.appendChild(textarea);
    textarea.select();

    try {
        document.execCommand('copy');
        showMessage('Ссылка скопирована в буфер обмена', 'success');
    } catch (error) {
        console.error('Error copying to clipboard:', error);
        showMessage('Не удалось скопировать ссылку', 'error');
    } finally {
        document.body.removeChild(textarea);
    }
}

async function toggleFollow() {
    const token = localStorage.getItem('token');
    if (!token) {
        showMessage('Пожалуйста, войдите в систему, чтобы подписываться на пользователей', 'warning');
        return;
    }

    if (!pin || !pin.user) return;

    try {
        const method = pin.user.isFollowedByCurrentUser ? 'DELETE' : 'POST';
        const response = await fetch(`${API_BASE_URL}/api/users/${pin.user.id}/follow`, {
            method: method,
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Не удалось обновить статус подписки');
        }

        // Update pin data
        pin.user.isFollowedByCurrentUser = !pin.user.isFollowedByCurrentUser;

        // Update UI
        const followBtn = document.getElementById('followBtn');
        if (followBtn) {
            followBtn.textContent = pin.user.isFollowedByCurrentUser ? 'Отписаться' : 'Подписаться';
        }

        showMessage(pin.user.isFollowedByCurrentUser ? `Вы подписались на ${pin.user.username}` : `Вы отписались от ${pin.user.username}`, 'success');
    } catch (error) {
        console.error('Error toggling follow:', error);
        showMessage(error.message || 'Произошла ошибка при обновлении статуса подписки', 'error');
    }
}

async function logout() {
    try {
        localStorage.removeItem('token');
        localStorage.removeItem('currentUser');
        currentUser = null;
        updateAuthUI(false);
        showMessage('Вы успешно вышли из системы', 'success');
    } catch (error) {
        console.error('Error during logout:', error);
        showMessage('Произошла ошибка при выходе из системы', 'error');
    }
}

function formatDate(dateString) {
    if (!dateString) return '';

    const date = new Date(dateString);

    // Check if date is valid
    if (isNaN(date.getTime())) return '';

    // Format date as DD.MM.YYYY HH:MM
    return `${date.toLocaleDateString('ru-RU')} ${date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}`;
}

function showLoading() {
    const loadingIndicator = document.createElement('div');
    loadingIndicator.className = 'loading-indicator';
    loadingIndicator.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
    document.body.appendChild(loadingIndicator);
}

function hideLoading() {
    const loadingIndicator = document.querySelector('.loading-indicator');
    if (loadingIndicator) {
        loadingIndicator.remove();
    }
}

function showError(message) {
    showMessage(message, 'error');
}

function showMessage(message, type = 'info') {
    // Remove any existing message
    const existingMessage = document.querySelector('.message');
    if (existingMessage) {
        existingMessage.remove();
    }

    // Create message element
    const messageElement = document.createElement('div');
    messageElement.className = `message message-${type}`;
    messageElement.innerHTML = `
        <div class="message-content">
            <i class="fas ${type === 'success' ? 'fa-check-circle' :
                           type === 'error' ? 'fa-exclamation-circle' :
                           type === 'warning' ? 'fa-exclamation-triangle' : 'fa-info-circle'}"></i>
            <span>${message}</span>
        </div>
        <button class="message-close"><i class="fas fa-times"></i></button>
    `;

    // Add to DOM
    document.body.appendChild(messageElement);

    // Add event listener for close button
    const closeButton = messageElement.querySelector('.message-close');
    if (closeButton) {
        closeButton.addEventListener('click', function() {
            hideMessage(messageElement);
        });
    }

    // Auto-hide after 5 seconds
    setTimeout(() => {
        hideMessage(messageElement);
    }, 5000);
}

function hideMessage(messageEl) {
    messageEl.classList.add('message-hiding');
    setTimeout(() => {
        messageEl.remove();
    }, 300);
}

function updateLikeButtonStatus() {
    const likeBtn = document.getElementById('likeBtn');

    if (likeBtn) {
        // Обновляем иконку в зависимости от статуса лайка
        const likeIcon = likeBtn.querySelector('i');
        if (likeIcon && currentPin.isLikedByCurrentUser) {
            likeIcon.className = 'fas fa-heart'; // Заполненное сердце
            likeBtn.classList.add('active');
        } else if (likeIcon) {
            likeIcon.className = 'far fa-heart'; // Контурное сердце
            likeBtn.classList.remove('active');
        }
    }
}