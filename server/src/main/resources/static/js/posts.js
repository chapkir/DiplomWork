document.addEventListener('DOMContentLoaded', function() {
    const loginSection = document.getElementById('loginSection');
    const postFormSection = document.getElementById('postFormSection');
    const postsContainer = document.getElementById('postsContainer');
    const loginForm = document.getElementById('loginForm');
    const createPostForm = document.getElementById('createPostForm');
    const loginBtn = document.getElementById('loginBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    const registerBtn = document.getElementById('registerBtn');
    const profileBtn = document.getElementById('profileBtn');

    // Базовый URL API
    const API_BASE_URL = '/api';

    // Проверка авторизации
    function checkAuth() {
        const token = localStorage.getItem('token');
        if (token) {
            // Пользователь авторизован
            loginSection.style.display = 'none';
            postFormSection.style.display = 'block';
            loginBtn.style.display = 'none';
            registerBtn.style.display = 'none';
            profileBtn.style.display = 'inline-block';
            logoutBtn.style.display = 'inline-block';
            loadPosts();
        } else {
            // Пользователь не авторизован
            loginSection.style.display = 'block';
            postFormSection.style.display = 'none';
            loginBtn.style.display = 'inline-block';
            registerBtn.style.display = 'inline-block';
            profileBtn.style.display = 'none';
            logoutBtn.style.display = 'none';
            postsContainer.innerHTML = '<div style="text-align: center; padding: 2rem;"><p>Войдите, чтобы просматривать публикации</p></div>';
        }
    }

    // Загрузка постов
    function loadPosts() {
        postsContainer.innerHTML = '<div style="text-align: center; padding: 2rem;"><i class="fas fa-spinner fa-spin"></i> Загрузка публикаций...</div>';

        fetch(`${API_BASE_URL}/posts`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Ошибка при загрузке постов');
                }
                return response.json();
            })
            .then(posts => {
                if (posts.length === 0) {
                    postsContainer.innerHTML = '<div style="text-align: center; padding: 2rem;"><p>Пока нет публикаций</p></div>';
                    return;
                }

                postsContainer.innerHTML = '';
                posts.forEach(post => {
                    renderPost(post);
                });
            })
            .catch(error => {
                console.error('Ошибка:', error);
                postsContainer.innerHTML = `<div style="text-align: center; padding: 2rem;"><p>Ошибка при загрузке публикаций: ${error.message}</p></div>`;
            });
    }

    // Отрисовка поста
    function renderPost(post) {
        const postDate = new Date(post.createdAt).toLocaleDateString('ru-RU', {
            day: 'numeric',
            month: 'long',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });

        const postElement = document.createElement('div');
        postElement.className = 'post-card';
        postElement.dataset.postId = post.id;

        let postHTML = `
            <div class="post-header">
                <img src="${post.author?.avatarUrl || '/default-avatar.svg'}" alt="Аватар" class="user-avatar">
                <div class="post-user-info">
                    <h4>${post.author?.username || 'Пользователь'}</h4>
                    <span class="post-date">${postDate}</span>
                </div>
            </div>
            <div class="post-content">
                <p class="post-text">${post.text}</p>
        `;

        if (post.imageUrl) {
            postHTML += `<img src="${post.imageUrl}" alt="Изображение поста" class="post-image">`;
        }

        postHTML += `
            </div>
            <div class="post-actions">
                <button class="post-action-btn like-btn ${post.liked ? 'liked' : ''}" onclick="likePost(${post.id})">
                    <i class="${post.liked ? 'fas' : 'far'} fa-heart"></i> ${post.likesCount || 0} Нравится
                </button>
                <button class="post-action-btn" onclick="toggleComments(${post.id})">
                    <i class="far fa-comment"></i> ${post.commentsCount || 0} Комментарии
                </button>
            </div>
        `;

        // Добавляем секцию комментариев (скрытую по умолчанию)
        postHTML += `
            <div class="comments-section" id="comments-${post.id}" style="display: none;">
                <div class="comment-form">
                    <input type="text" class="comment-input" id="comment-input-${post.id}" placeholder="Напишите комментарий...">
                    <button class="comment-btn" onclick="addComment(${post.id})">
                        <i class="fas fa-paper-plane"></i>
                    </button>
                </div>
                <div class="comments-list" id="comments-list-${post.id}">
                    <!-- Комментарии будут добавлены через JavaScript -->
                </div>
            </div>
        `;

        postElement.innerHTML = postHTML;
        postsContainer.appendChild(postElement);
    }

    // Обработка входа
    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const username = document.getElementById('loginUsername').value;
        const password = document.getElementById('loginPassword').value;

        // Запрос на авторизацию
        fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Неверное имя пользователя или пароль');
            }
            return response.json();
        })
        .then(data => {
            localStorage.setItem('token', data.token);
            localStorage.setItem('username', username);
            localStorage.setItem('userId', data.userId);

            // Очищаем форму
            document.getElementById('loginUsername').value = '';
            document.getElementById('loginPassword').value = '';

            showToast('Вы успешно вошли в систему', 'success');
            checkAuth();
        })
        .catch(error => {
            console.error('Ошибка:', error);
            showToast(error.message, 'error');
        });
    });

    // Обработка создания поста
    createPostForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const text = document.getElementById('postText').value;
        const imageFile = document.getElementById('postImage').files[0];

        // Показываем индикатор загрузки
        const loadingToast = showToast('Загрузка публикации...', 'info', false);

        // Проверяем, есть ли файл
        if (imageFile) {
            // Если есть файл, используем тестовый endpoint
            const formData = new FormData();
            formData.append('file', imageFile);
            formData.append('text', text);

            console.log('Sending post request to /api/posts/test-upload-image');
            console.log('File name:', imageFile.name);
            console.log('File size:', imageFile.size);
            console.log('File type:', imageFile.type);

            // Используем тестовый эндпоинт без авторизации
            fetch(`${API_BASE_URL}/posts/test-upload-image`, {
                method: 'POST',
                body: formData
            })
            .then(response => {
                console.log('Response status:', response.status);
                if (!response.ok) {
                    return response.text().then(text => {
                        try {
                            // Попытка преобразовать текст в JSON
                            const errorData = JSON.parse(text);
                            throw new Error(errorData.error || 'Ошибка при создании публикации с изображением');
                        } catch (e) {
                            // Если не удалось преобразовать в JSON, вернуть текст ошибки
                            throw new Error('Ошибка: ' + text);
                        }
                    });
                }
                return response.json();
            })
            .then(post => {
                console.log('Post created successfully:', post);
                handleSuccessfulPost(post, loadingToast);
            })
            .catch(error => {
                console.error('Error creating post:', error);
                handlePostError(error, loadingToast);
            });
        } else {
            // Если файла нет, используем JSON без явного Bearer токена
            fetch(`${API_BASE_URL}/posts`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    text: text
                })
            })
            .then(response => {
                if (response.status === 401) {
                    showToast('Срок сессии истек. Пожалуйста, войдите снова', 'error');
                    localStorage.removeItem('token');
                    localStorage.removeItem('username');
                    localStorage.removeItem('userId');
                    setTimeout(() => {
                        checkAuth();
                    }, 1000);
                    throw new Error('Необходима авторизация для создания публикации');
                }

                if (!response.ok) {
                    throw new Error('Ошибка при создании публикации');
                }
                return response.json();
            })
            .then(post => {
                handleSuccessfulPost(post, loadingToast);
            })
            .catch(error => {
                handlePostError(error, loadingToast);
            });
        }
    });

    // Обработка успешного создания поста
    function handleSuccessfulPost(post, loadingToast) {
        // Удаляем индикатор загрузки
        if (loadingToast) {
            loadingToast.remove();
        }

        // Добавляем новый пост в начало списка
        renderPost(post);
        document.getElementById('postText').value = '';
        document.getElementById('postImage').value = '';
        showToast('Публикация успешно создана', 'success');

        // Перемещаем новый пост в начало списка
        const newPost = document.querySelector(`.post-card[data-post-id="${post.id}"]`);
        if (newPost && postsContainer.firstChild) {
            postsContainer.insertBefore(newPost, postsContainer.firstChild);
        }
    }

    // Обработка ошибки при создании поста
    function handlePostError(error, loadingToast) {
        // Удаляем индикатор загрузки
        if (loadingToast) {
            loadingToast.remove();
        }

        console.error('Ошибка:', error);
        showToast(error.message, 'error');
    }

    // Обработчики для кнопок заголовка
    loginBtn.addEventListener('click', function() {
        loginSection.style.display = 'block';
    });

    logoutBtn.addEventListener('click', function() {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        localStorage.removeItem('userId');
        showToast('Вы вышли из системы', 'info');
        checkAuth();
    });

    // Функция для отображения уведомлений
    function showToast(message, type = 'info', autoClose = true) {
        // Проверяем, существует ли контейнер для уведомлений
        let toastContainer = document.getElementById('toast-container');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.id = 'toast-container';
            toastContainer.style.position = 'fixed';
            toastContainer.style.bottom = '20px';
            toastContainer.style.right = '20px';
            toastContainer.style.zIndex = '1000';
            document.body.appendChild(toastContainer);
        }

        // Создаем элемент уведомления
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.style.backgroundColor = type === 'error' ? '#f44336' : type === 'success' ? '#4CAF50' : '#2196F3';
        toast.style.color = 'white';
        toast.style.padding = '12px 20px';
        toast.style.marginBottom = '10px';
        toast.style.borderRadius = '4px';
        toast.style.boxShadow = '0 2px 5px rgba(0,0,0,0.2)';
        toast.style.minWidth = '200px';
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s ease-in-out';
        toast.textContent = message;

        toastContainer.appendChild(toast);

        // Анимация появления
        setTimeout(() => {
            toast.style.opacity = '1';
        }, 10);

        // Автоматическое закрытие через 3 секунды
        if (autoClose) {
            setTimeout(() => {
                toast.style.opacity = '0';
                setTimeout(() => {
                    toast.remove();
                }, 300);
            }, 3000);
        }

        return toast;
    }

    // Глобальные функции для лайков и комментариев
    window.likePost = function(postId) {
        const token = localStorage.getItem('token');
        if (!token) {
            showToast('Необходимо войти в систему', 'error');
            return;
        }

        const likeBtn = document.querySelector(`.post-card[data-post-id="${postId}"] .like-btn`);
        const isLiked = likeBtn.classList.contains('liked');

        // Метод запроса зависит от текущего состояния лайка
        const method = isLiked ? 'DELETE' : 'POST';

        fetch(`${API_BASE_URL}/posts/${postId}/likes`, {
            method: method,
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при обновлении лайка');
            }
            return response.json();
        })
        .then(data => {
            likeBtn.classList.toggle('liked');

            // Обновляем иконку и счетчик
            const heartIcon = isLiked ? 'far' : 'fas';
            likeBtn.innerHTML = `<i class="${heartIcon} fa-heart"></i> ${data.likesCount} Нравится`;

            if (isLiked) {
                showToast('Вы убрали лайк', 'info');
            } else {
                showToast('Вам понравилась публикация', 'success');
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            showToast(error.message, 'error');
        });
    };

    window.toggleComments = function(postId) {
        const commentsSection = document.getElementById(`comments-${postId}`);
        const isVisible = commentsSection.style.display !== 'none';

        if (!isVisible) {
            // Загружаем комментарии при открытии
            loadComments(postId);
        }

        commentsSection.style.display = isVisible ? 'none' : 'block';
    };

    window.loadComments = function(postId) {
        const commentsList = document.getElementById(`comments-list-${postId}`);
        commentsList.innerHTML = '<div style="text-align: center; padding: 0.5rem;"><i class="fas fa-spinner fa-spin"></i> Загрузка комментариев...</div>';

        fetch(`${API_BASE_URL}/posts/${postId}/comments`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Ошибка при загрузке комментариев');
                }
                return response.json();
            })
            .then(comments => {
                if (comments.length === 0) {
                    commentsList.innerHTML = '<div style="text-align: center; padding: 0.5rem;">Нет комментариев</div>';
                    return;
                }

                commentsList.innerHTML = '';
                comments.forEach(comment => {
                    renderComment(comment, commentsList);
                });
            })
            .catch(error => {
                console.error('Ошибка:', error);
                commentsList.innerHTML = `<div style="text-align: center; color: red; padding: 0.5rem;">Ошибка: ${error.message}</div>`;
            });
    };

    window.renderComment = function(comment, container) {
        const commentElement = document.createElement('div');
        commentElement.className = 'comment';
        commentElement.innerHTML = `
            <img src="${comment.author?.avatarUrl || '/default-avatar.svg'}" alt="Аватар" class="comment-avatar">
            <div class="comment-content">
                <div class="comment-author">${comment.author?.username || 'Пользователь'}</div>
                <div class="comment-text">${comment.text}</div>
            </div>
        `;
        container.appendChild(commentElement);
    };

    window.addComment = function(postId) {
        const token = localStorage.getItem('token');
        if (!token) {
            showToast('Необходимо войти в систему', 'error');
            return;
        }

        const commentInput = document.getElementById(`comment-input-${postId}`);
        const commentText = commentInput.value.trim();

        if (!commentText) return;

        fetch(`${API_BASE_URL}/posts/${postId}/comments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                text: commentText
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при добавлении комментария');
            }
            return response.json();
        })
        .then(comment => {
            const commentsList = document.getElementById(`comments-list-${postId}`);

            // Если это первый комментарий, очищаем сообщение "Нет комментариев"
            if (commentsList.innerHTML.includes('Нет комментариев')) {
                commentsList.innerHTML = '';
            }

            renderComment(comment, commentsList);
            commentInput.value = '';

            // Обновляем счетчик комментариев в кнопке
            const commentBtn = document.querySelector(`.post-card[data-post-id="${postId}"] .post-action-btn:nth-child(2)`);
            const currentCount = parseInt(commentBtn.textContent.match(/\d+/)[0] || 0);
            commentBtn.innerHTML = `<i class="far fa-comment"></i> ${currentCount + 1} Комментарии`;

            showToast('Комментарий добавлен', 'success');
        })
        .catch(error => {
            console.error('Ошибка:', error);
            showToast(error.message, 'error');
        });
    };

    // CSS для уведомлений
    const style = document.createElement('style');
    style.textContent = `
        #toast-container {
            position: fixed;
            bottom: 20px;
            right: 20px;
            z-index: 1000;
        }

        .toast {
            padding: 12px 20px;
            margin-bottom: 10px;
            border-radius: 4px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.2);
            min-width: 200px;
            opacity: 0;
            transition: opacity 0.3s ease-in-out;
            color: white;
        }

        .toast.error {
            background-color: #f44336;
        }

        .toast.success {
            background-color: #4CAF50;
        }

        .toast.info {
            background-color: #2196F3;
        }
    `;
    document.head.appendChild(style);

    // Инициализация
    checkAuth();

    // Проверяем, есть ли якорь в URL для открытия нужного поста и его комментариев
    if (window.location.hash) {
        const postId = window.location.hash.replace('#post-', '');
        if (postId && !isNaN(parseInt(postId))) {
            // После загрузки постов и отрисовки - открываем нужный пост и его комментарии
            const waitForPost = setInterval(() => {
                const postElement = document.querySelector(`.post-card[data-post-id="${postId}"]`);
                if (postElement) {
                    clearInterval(waitForPost);
                    // Прокрутка к посту
                    postElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    // Выделяем пост визуально
                    postElement.style.boxShadow = '0 0 15px rgba(0, 123, 255, 0.5)';
                    setTimeout(() => {
                        postElement.style.boxShadow = '';
                    }, 2000);
                    // Открываем комментарии
                    toggleComments(postId);
                }
            }, 500);
        }
    }
});