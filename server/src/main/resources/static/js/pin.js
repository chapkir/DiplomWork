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
        showMessage('Идентификатор идеи не найден', 'error');
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
    if (likeBtn) {
        likeBtn.addEventListener('click', toggleLike);
    }

    // Save button
    if (saveBtn) {
        saveBtn.addEventListener('click', savePin);
    }

    // Share button
    if (shareBtn) {
        shareBtn.addEventListener('click', sharePin);
    }

    // Follow button
    if (followBtn) {
        followBtn.addEventListener('click', toggleFollow);
    }

    // Login/Register buttons
    if (loginBtn) {
        loginBtn.addEventListener('click', function() {
            openModal(document.getElementById('loginModal'));
        });
    }

    if (registerBtn) {
        registerBtn.addEventListener('click', function() {
            openModal(document.getElementById('registerModal'));
        });
    }

    // Logout button
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
        const commentForm = document.getElementById('commentForm');
        const loginForComments = document.getElementById('loginForComments');
        if (commentForm) commentForm.style.display = 'block';
        if (loginForComments) loginForComments.style.display = 'none';
    } else {
        // Гость
        if (loginBtn) loginBtn.style.display = 'inline-flex';
        if (registerBtn) registerBtn.style.display = 'inline-flex';
        if (logoutBtn) logoutBtn.style.display = 'none';
        if (profileBtn) profileBtn.style.display = 'none';

        // Скрываем форму комментариев и показываем приглашение войти
        const commentForm = document.getElementById('commentForm');
        const loginForComments = document.getElementById('loginForComments');
        if (commentForm) commentForm.style.display = 'none';
        if (loginForComments) loginForComments.style.display = 'block';
    }
}

async function loadPin(pinId) {
    try {
        showLoading();

        // Fetch pin data
        const response = await fetch(`/api/pins/${pinId}`);

        if (!response.ok) {
            throw new Error('Failed to load pin data');
        }

        const data = await response.json();
        currentPin = data;

        // Load author data
        await loadAuthorData(data.userId);

        // Update UI with pin data
        updatePinUI();

        // Load comments
        await loadComments(pinId);

        // Load related pins
        await loadRelatedPins(data.category);

        hideLoading();
    } catch (error) {
        hideLoading();
        showMessage('Ошибка при загрузке пина', 'error');
        console.error('Error loading pin:', error);
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
    }
}

function updatePinUI() {
    if (pinImage) pinImage.src = currentPin.imageUrl;
    if (pinImage) pinImage.alt = currentPin.description || "Изображение идеи";

    if (pinTitle) {
        pinTitle.textContent = currentPin.description ?
            currentPin.description.substring(0, 50) + (currentPin.description.length > 50 ? '...' : '') :
            "Идея без описания";
    }

    if (pinDescription) pinDescription.textContent = currentPin.description || "Для этой идеи не добавлено описание.";

    if (likesCount) likesCount.textContent = currentPin.likesCount || 0;

    const commentCountValue = currentPin.comments ? currentPin.comments.length : 0;
    if (commentsCount) commentsCount.textContent = commentCountValue;

    const commentCountLabel = document.getElementById('commentCountLabel');
    if (commentCountLabel) commentCountLabel.textContent = `(${commentCountValue})`;

    // Форматируем дату
    const createdAt = document.getElementById('createdAt');
    if (createdAt) {
        createdAt.textContent = currentPin.createdAt ? formatDate(currentPin.createdAt) : "Недавно";
    }

    // Отображаем данные автора
    if (pinAuthor) {
        if (authorName) authorName.textContent = pinAuthor.username || "Неизвестный автор";

        const authorAvatar = document.getElementById('authorAvatar');
        if (authorAvatar) {
            if (pinAuthor.profileImageUrl) {
                authorAvatar.src = pinAuthor.profileImageUrl;
            } else {
                // Генерируем цвет аватара на основе имени пользователя
                const color = getRandomColor(pinAuthor.username);
                authorAvatar.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(pinAuthor.username)}&background=${color.replace('#', '')}&color=fff`;
            }
        }

        const authorBio = document.getElementById('authorBio');
        if (authorBio) {
            authorBio.textContent = pinAuthor.bio || "У этого пользователя нет описания.";
        }
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
    }
}

function renderComments() {
    if (!commentsList) return;

    commentsList.innerHTML = '';

    if (!currentPin.comments || currentPin.comments.length === 0) {
        commentsList.innerHTML = `
            <div class="no-comments">
                <p>Пока нет комментариев. Будьте первым!</p>
            </div>
        `;
        if (commentsCount) commentsCount.textContent = '0';

        const commentCountLabel = document.getElementById('commentCountLabel');
        if (commentCountLabel) commentCountLabel.textContent = '(0)';
        return;
    }

    if (commentsCount) commentsCount.textContent = currentPin.comments.length;

    const commentCountLabel = document.getElementById('commentCountLabel');
    if (commentCountLabel) commentCountLabel.textContent = `(${currentPin.comments.length})`;

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
        let relatedPins = [];

        if (data.content && Array.isArray(data.content)) {
            relatedPins = data.content.filter(relatedPin => relatedPin.id !== currentPin.id).slice(0, 4);
        } else if (Array.isArray(data)) {
            relatedPins = data.filter(relatedPin => relatedPin.id !== currentPin.id).slice(0, 4);
        }

        renderRelatedPins(relatedPins);
    } catch (error) {
        console.error('Error loading related pins:', error);
    }
}

function renderRelatedPins(relatedPins) {
    const relatedPinsGrid = document.querySelector('.related-pins .grid');
    if (!relatedPinsGrid) return;

    if (!relatedPins || relatedPins.length === 0) {
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

    const comment = commentInput.value.trim();

    if (!comment) {
        showMessage('Пожалуйста, введите комментарий', 'error');
        return;
    }

    try {
        commentInput.disabled = true;
        submitCommentBtn.disabled = true;

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
        showMessage('Произошла ошибка при добавлении комментария', 'error');
    } finally {
        commentInput.disabled = false;
        submitCommentBtn.disabled = false;
    }
}

async function toggleLike() {
    if (!isAuthenticated) {
        showMessage('Пожалуйста, войдите в систему, чтобы поставить лайк', 'info');
        openModal(document.getElementById('loginModal'));
        return;
    }

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
        showMessage('Произошла ошибка при обновлении статуса лайка', 'error');
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

    if (!currentPin || !pinAuthor) return;

    try {
        const isFollowed = pinAuthor.isFollowedByCurrentUser;
        const method = isFollowed ? 'DELETE' : 'POST';
        const response = await fetch(`${API_BASE_URL}/api/users/${pinAuthor.id}/follow`, {
            method: method,
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Не удалось обновить статус подписки');
        }

        // Update author data
        pinAuthor.isFollowedByCurrentUser = !isFollowed;

        // Update UI
        if (followBtn) {
            followBtn.textContent = pinAuthor.isFollowedByCurrentUser ? 'Отписаться' : 'Подписаться';
        }

        showMessage(pinAuthor.isFollowedByCurrentUser ?
            `Вы подписались на ${pinAuthor.username}` :
            `Вы отписались от ${pinAuthor.username}`, 'success');
    } catch (error) {
        console.error('Error toggling follow:', error);
        showMessage(error.message || 'Произошла ошибка при обновлении статуса подписки', 'error');
    }
}

async function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    currentUser = null;
    isAuthenticated = false;
    updateAuthUI(false);
    showMessage('Вы успешно вышли из системы', 'success');
    try {
        await fetch('/api/auth/logout');
    } catch (error) {
        showMessage('Произошла ошибка при выходе из системы', 'error');
    }
}

function formatDate(dateString) {
    const date = new Date(dateString);
    const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' };
    return date.toLocaleDateString('ru-RU', options);
}

function showLoading() {
    document.body.classList.add('loading');
}

function hideLoading() {
    document.body.classList.remove('loading');
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

function openModal(modal) {
    if (modal) {
        modal.classList.add('active');
    }
}