// Минимальный SPA-скрипт
// Авторизация и переключение между view

let token = localStorage.getItem('token');

// Пагинация пинов
let pinNextCursor = null;
const pinPageSize = 12; // Увеличено количество показываемых пинов
const pinSkeletonCount = 6; // Увеличено количество скелетонов
// Курсорная пагинация постов
let postNextCursor = null;
const postPageSize = 12; // Увеличено количество показываемых постов

// Кэширование DOM элементов
const DOM = {
    loader: document.getElementById('global-loader'),
    loaderText: document.querySelector('.loader-text'),
    feed: document.getElementById('feed'),
    postsFeed: document.getElementById('posts-feed'),
    profileContent: document.getElementById('profile-content'),
    loadMorePins: document.getElementById('load-more-pins'),
    loadMorePosts: document.getElementById('load-more-posts')
};

// Состояние приложения
let state = {
    token: localStorage.getItem('token'),
    pinNextCursor: null,
    postNextCursor: null,
    isAuthenticated: false
};

// Утилиты для работы с API
const API = {
    async fetch(url, options = {}) {
        const defaultOptions = {
            headers: {
                'Authorization': state.token ? `Bearer ${state.token}` : '',
                'Content-Type': 'application/json'
            }
        };
        
        try {
            const response = await fetch(url, { ...defaultOptions, ...options });
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }
};

function authHeaders() {
  return token ? { 'Authorization': 'Bearer ' + token } : {};
}

function showView(viewId) {
  const view = document.getElementById(viewId);
  if (!view) return;

  document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
  view.classList.add('active');

  // Обновляем активную вкладку в навигации
  if (viewId !== 'login-view' && viewId !== 'signup-view') {
    const navId = viewId.replace('-view', '');
    document.querySelectorAll('.nav-link').forEach(link => link.classList.remove('active'));
    const activeLink = document.querySelector(`.nav-link[data-view="${navId}"]`);
    if (activeLink) activeLink.classList.add('active');
  }
}

// Модифицированная функция для управления глобальным лоадером, добавляю анимацию текста
function showLoader(message = 'Загрузка') {
  if (DOM.loaderText) DOM.loaderText.textContent = message;
  if (DOM.loader) DOM.loader.style.display = 'flex';
}

function hideLoader() {
  if (DOM.loader) DOM.loader.style.display = 'none';
}

// Функция для скрытия всех элементов интерфейса кроме формы авторизации
function hideAllExceptAuth() {
  // Скрываем все секции контента кроме форм авторизации и регистрации
  document.querySelectorAll('.content-view').forEach(section => {
    section.style.display = 'none';
  });

  // Скрываем навигационные ссылки, оставляя только кнопку логина
  document.querySelectorAll('.nav-link:not(.auth-action)').forEach(link => {
    link.style.display = 'none';
  });

  // Показываем только форму логина
  const loginView = document.getElementById('login-view');
  if (loginView) {
    loginView.classList.add('active');
    loginView.style.display = 'flex';
  }
}

// Функция для показа всех элементов интерфейса после авторизации
function showAllContent() {
  // Показываем все секции контента
  document.querySelectorAll('.content-view').forEach(section => {
    section.style.display = '';
  });

  // Показываем навигационные ссылки
  document.querySelectorAll('.nav-link:not(.auth-action)').forEach(link => {
    link.style.display = '';
  });
}

// Главная функция переключения между состояниями авторизации
function updateAuthState(isAuth) {
  const landingPage = document.getElementById('landing-page');
  const appViews = document.getElementById('app-views');
  const logoutBtn = document.getElementById('logout-btn');
  const loginBtns = document.querySelectorAll('.nav-link.auth-action');

  if (isAuth) {
    // Скрываем форму логина и регистрации
    const loginView = document.getElementById('login-view');
    const signupView = document.getElementById('signup-view');
    if (loginView) loginView.style.display = 'none';
    if (signupView) signupView.style.display = 'none';
    console.log('Пользователь авторизован, токен:', token?.substring(0, 10) + '...');
    if (landingPage) landingPage.style.display = 'none';
    appViews.style.display = 'block';
    if (logoutBtn) logoutBtn.style.display = 'block';
    loginBtns.forEach(btn => {
      if (btn.id !== 'logout-btn') {
        btn.style.display = 'none';
      }
    });

    // Показываем весь контент для авторизованных пользователей
    showAllContent();
    showView('posts-view');
  } else {
    console.log('Пользователь не авторизован');
    if (landingPage) landingPage.style.display = 'none';
    appViews.style.display = 'block';
    if (logoutBtn) logoutBtn.style.display = 'none';
    loginBtns.forEach(btn => {
      if (btn.id !== 'logout-btn') {
        btn.style.display = 'block';
      }
    });
    hideAllExceptAuth();
    showView('login-view');
  }
}

// Проверка статуса авторизации через API
async function checkAuthStatus() {
  console.log('Проверка статуса авторизации');
  try {
    if (!token) {
      console.log('Токен отсутствует, пользователь не авторизован');
      return false;
    }

    const res = await fetch('/api/auth/check', {
      headers: authHeaders()
    });

    if (!res.ok) {
      console.log('Ошибка проверки авторизации, статус:', res.status);
      // Токен недействителен, удаляем его
      token = null;
      localStorage.removeItem('token');
      return false;
    }

    const user = await res.json();
    console.log('Пользователь авторизован:', user.username);
    return true;
  } catch (err) {
    console.error('Ошибка при проверке авторизации:', err);
    token = null;
    localStorage.removeItem('token');
    return false;
  }
}

// Обработчики форм логина/регистрации и выхода
async function handleLogin(e) {
  e.preventDefault();
  const username = document.getElementById('login-username').value;
  const password = document.getElementById('login-password').value;

  if (!username || !password) {
    alert('Пожалуйста, заполните все поля');
    return;
  }

  try {
    showLoader();
    console.log('Отправка запроса авторизации для пользователя:', username);

    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });

    console.log('Получен ответ от сервера, статус:', res.status);

    const responseText = await res.text();
    console.log('Ответ от сервера (текст):', responseText);

    if (!res.ok) {
      console.error('Ошибка авторизации, статус:', res.status);
      hideLoader();
      // Показываем сообщение ошибки от сервера, если оно есть
      let errorMsg = 'Ошибка авторизации: ' + res.status;
      try {
        const errObj = JSON.parse(responseText);
        if (errObj && errObj.message) errorMsg = errObj.message;
      } catch (parseErr) {
        console.warn('Не удалось распарсить сообщение об ошибке:', parseErr);
      }
      alert(errorMsg);
      return;
    }

    // Парсим ответ как JSON
    let data;
    try {
      data = JSON.parse(responseText);
      console.log('Ответ от сервера (объект):', data);
    } catch (e) {
      console.error('Ошибка парсинга JSON:', e);
      hideLoader();
      alert('Ошибка формата ответа сервера');
      return;
    }

    if (!data.token) {
      console.error('Не получен токен в ответе:', data);
      hideLoader();
      alert('Ошибка авторизации: неверный формат ответа сервера');
      return;
    }

    // Сохраняем токен и обновляем состояние
    console.log('Успешная авторизация, сохраняем токен');
    token = data.token;
    localStorage.setItem('token', token);

    // Сохраняем refresh токен, если он есть
    if (data.refreshToken) {
      localStorage.setItem('refreshToken', data.refreshToken);
    }

    hideLoader();

    updateAuthState(true); // Передаем true в updateAuthState
    loadPosts(false); // Загружаем посты
  } catch (err) {
    hideLoader();
    console.error('Ошибка при авторизации:', err);
    alert('Ошибка при авторизации: ' + err.message);
  }
}

async function handleSignup(e) {
  e.preventDefault();
  const username = document.getElementById('signup-username').value;
  const email = document.getElementById('signup-email').value;
  const password = document.getElementById('signup-password').value;

  if (!username || !email || !password) {
    alert('Пожалуйста, заполните все поля');
    return;
  }

  try {
    showLoader();
    console.log('Отправка запроса регистрации для пользователя:', username);

    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password })
    });

    console.log('Получен ответ от сервера, статус:', res.status);

    const responseText = await res.text();
    console.log('Ответ от сервера (текст):', responseText);

    if (!res.ok) {
      console.error('Ошибка регистрации, статус:', res.status);
      hideLoader();
      alert('Ошибка регистрации. Возможно, такой пользователь уже существует.');
      return;
    }

    hideLoader();
    alert('Регистрация успешна. Теперь вы можете войти со своими учетными данными.');
    showView('login-view');
  } catch (err) {
    hideLoader();
    console.error('Ошибка при регистрации:', err);
    alert('Ошибка при регистрации: ' + err.message);
  }
}

document.addEventListener('DOMContentLoaded', async () => {
  console.log('DOM загружен, инициализация приложения...');

  // Сначала скрываем всё и показываем загрузчик
  hideAllExceptAuth();
  showLoader();

  // Проверяем авторизацию
  const isAuth = await checkAuthStatus();
  console.log('Статус авторизации:', isAuth ? 'авторизован' : 'не авторизован');

  // Обновляем состояние UI в зависимости от статуса авторизации
  updateAuthState(isAuth);
  hideLoader();

  // Загружаем данные только если пользователь авторизован
  if (isAuth) {
    loadPosts(false);
  }

  // Login/Signup в формах
  const showSignupLink = document.getElementById('show-signup');
  if (showSignupLink) {
    showSignupLink.addEventListener('click', e => {
      e.preventDefault();
      showView('signup-view');
    });
  }

  const showLoginLink = document.getElementById('show-login');
  if (showLoginLink) {
    showLoginLink.addEventListener('click', e => {
      e.preventDefault();
      showView('login-view');
    });
  }

  const loginForm = document.getElementById('login-form');
  if (loginForm) {
    loginForm.addEventListener('submit', handleLogin);
  }

  const signupForm = document.getElementById('signup-form');
  if (signupForm) {
    signupForm.addEventListener('submit', handleSignup);
  }

  // Кнопка выхода
  const logoutBtn = document.getElementById('logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      console.log('Выход из аккаунта');
      token = null;
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      updateAuthState(false);
    });
  }

  // Кнопка входа в верхнем меню
  const loginBtn = document.querySelector('.nav-link.auth-action:not(#logout-btn)');
  if (loginBtn) {
    loginBtn.addEventListener('click', (e) => {
      e.preventDefault();
      showView('login-view');
    });
  }

  // Навигация
  const navLinks = document.querySelectorAll('.nav-link[data-view]');
  console.log('Nav links:', navLinks.length);
  navLinks.forEach(link => {
    console.log('Setting up click listener for:', link.dataset.view);
    link.addEventListener('click', e => {
      e.preventDefault();

      const view = link.dataset.view;
      console.log('Clicked on navigation:', view);

      // Проверяем авторизацию перед переходом
      if (!token) {
        console.log('Попытка перехода без авторизации - показываем форму логина');
        showView('login-view');
        return;
      }

      showView(view + '-view');

      if (view === 'feed') {
        pinNextCursor = null; // сброс пагинации
        loadFeed(false);
      }
      if (view === 'posts') {
        postNextCursor = null;
        loadPosts(false);
      }
      if (view === 'profile') loadProfile();
    });
  });

  // Обработчик клика по логотипу
  const logoLink = document.querySelector('.logo');
  if (logoLink) {
    logoLink.addEventListener('click', e => {
      e.preventDefault();

      if (!token) {
        showView('login-view');
      } else {
        showView('posts-view');
        postNextCursor = null;
        loadPosts(false);
      }
    });
  }

  // Кнопки Load More
  const loadMorePins = document.getElementById('load-more-pins');
  if (loadMorePins) {
    loadMorePins.addEventListener('click', () => loadFeed(true));
  }

  const loadMorePosts = document.getElementById('load-more-posts');
  if (loadMorePosts) {
    loadMorePosts.addEventListener('click', () => loadPosts(true));
  }
});

// Функция для создания skeleton-карточки
function createSkeletonCard() {
  const div = document.createElement('div');
  div.className = 'card skeleton';
  return div;
}

// Загрузка пинов с cursor-пагинацией
async function loadFeed(append = false) {
  console.log('Loading feed, append:', append);
  showLoader();
  const container = document.getElementById('feed');
  if (!container) {
    console.error('Feed container not found');
    hideLoader();
    return;
  }

  const btn = document.getElementById('load-more-pins');
  if (!append) {
    container.innerHTML = '';
    if (btn) btn.style.display = 'none';
  }
  // Показать skeleton
  for (let i = 0; i < pinSkeletonCount; i++) container.appendChild(createSkeletonCard());
  try {
    let url = `/api/pins?size=${pinPageSize}`;
    if (pinNextCursor) url += `&cursor=${encodeURIComponent(pinNextCursor)}`;

    console.log('Fetching pins from URL:', url);
    const res = await fetch(url, { headers: authHeaders() });

    if (!res.ok) {
      console.error('Ошибка HTTP при загрузке пинов:', res.status);
      if (!append) container.innerHTML = `<p class='error'>Не удалось загрузить пины: ${res.status}</p>`;
      hideLoader();
      return;
    }

    const json = await res.json();
    console.log('Pins API response:', json);

    // Парсинг HATEOAS-ответа
    const page = json.data || {};
    const pins = page.content || [];
    pinNextCursor = page.nextCursor;
    const hasNext = page.hasNext;

    // Удалить все skeleton
    const skeletons = container.querySelectorAll('.card.skeleton');
    skeletons.forEach(el => el.remove());

    // Если не append и есть старый контент - очистить контейнер
    if (!append) container.innerHTML = '';

    if (pins.length === 0) {
      container.innerHTML = `<p class='empty-message'>Пины не найдены</p>`;
      hideLoader();
      return;
    }

    pins.forEach(pin => {
      // Предпочитаем WebP формат, если доступен thumbnailImageUrl
      const src = pin.thumbnailImageUrl || pin.imageUrl;
      const card = createCard(src, pin.title, pin.username);
      card.dataset.id = pin.id; // Сохраняем ID пина для возможности открытия деталей
      card.classList.add('fade-in');
      container.appendChild(card);
    });

    // Показываем кнопку Load More, если есть следующий курсор
    if (btn) btn.style.display = hasNext ? 'block' : 'none';
    hideLoader();
  } catch (e) {
    console.error('Ошибка в обработке пинов', e);
    container.innerHTML = `<p class='error'>Ошибка при загрузке пинов</p>`;
    hideLoader();
  }
}

// Оптимизированная функция создания карточки
function createCard(img, title, subtitle) {
    const template = document.createElement('template');
    template.innerHTML = `
        <div class="card">
            <div class="card-image-wrapper">
                <div class="card-image-skeleton"></div>
                <img class="card-image" loading="lazy" alt="${title || 'Image'}" src="${img || '/img/image-placeholder.png'}">
            </div>
            <div class="card-content">
                ${title ? `<h3 class="card-title">${title}</h3>` : ''}
                ${subtitle ? `<p class="card-subtitle">${subtitle}</p>` : ''}
            </div>
        </div>
    `.trim();
    
    return template.content.firstElementChild;
}

// Оптимизированная функция загрузки изображений
function loadImage(img) {
    return new Promise((resolve, reject) => {
        if (img.complete) {
            resolve(img);
        } else {
            img.onload = () => resolve(img);
            img.onerror = reject;
        }
    });
}

// Оптимизированная функция загрузки постов
async function loadPosts(append = false) {
    if (!append) {
        DOM.postsFeed.innerHTML = '';
        state.postNextCursor = null;
        if (DOM.loadMorePosts) DOM.loadMorePosts.style.display = 'none';
    }

    showLoader('Загружаем посты...');
    
    try {
        const url = `/api/posts/cursor?size=${PAGINATION.POST_PAGE_SIZE}${state.postNextCursor ? `&cursor=${encodeURIComponent(state.postNextCursor)}` : ''}`;
        const data = await API.fetch(url);
        
        if (!append) DOM.postsFeed.innerHTML = '';
        
        if (!data.content || data.content.length === 0) {
            DOM.postsFeed.innerHTML = createEmptyMessage('Посты не найдены');
            return;
        }

        const fragment = document.createDocumentFragment();
        data.content.forEach((post, index) => {
            const card = createCard(
                post.thumbnailImageUrl || post.fullhdImageUrl || post.imageUrl,
                post.title || 'Без названия',
                post.username
            );
            card.style.animationDelay = `${0.1 * (index % 12)}s`;
            card.classList.add('fade-in', 'post-card');
            fragment.appendChild(card);
        });

        DOM.postsFeed.appendChild(fragment);
        state.postNextCursor = data.nextCursor;
        
        if (DOM.loadMorePosts) {
            DOM.loadMorePosts.style.display = data.hasNext ? 'block' : 'none';
        }
    } catch (error) {
        DOM.postsFeed.innerHTML = createErrorMessage(error.message);
    } finally {
        hideLoader();
    }
}

// Оптимизированная функция загрузки профиля
async function loadProfile() {
    if (!state.token) {
        DOM.profileContent.innerHTML = createAuthRequiredMessage();
        return;
    }

    showLoader('Загружаем профиль...');
    
    try {
        const user = await API.fetch('/api/auth/me');
        const posts = await API.fetch(`/api/posts/user/${user.id}`);
        
        DOM.profileContent.innerHTML = `
            <div class="profile-header">
                <div class="profile-avatar">
                    <img src="${user.profileImageUrl || '/img/avatar-placeholder.png'}" alt="${user.username}">
                </div>
                <div class="profile-info">
                    <h1>${user.username}</h1>
                    <p>${user.email || ''}</p>
                    <div class="profile-stats">
                        <div class="stat-item">
                            <div class="stat-number">0</div>
                            <div class="stat-label">Подписчики</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-number">0</div>
                            <div class="stat-label">Подписки</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-number">${posts.length || 0}</div>
                            <div class="stat-label">Публикации</div>
                        </div>
                    </div>
                </div>
            </div>
        `;

        if (posts.length > 0) {
            const grid = document.createElement('div');
            grid.className = 'masonry-grid';
            
            const fragment = document.createDocumentFragment();
            posts.forEach((post, index) => {
                const card = createCard(
                    post.thumbnailImageUrl || post.fullhdImageUrl || post.imageUrl,
                    post.title || post.text || 'Без названия',
                    user.username
                );
                card.style.animationDelay = `${0.1 * (index % 12)}s`;
                card.classList.add('fade-in', 'post-card');
                fragment.appendChild(card);
            });
            
            grid.appendChild(fragment);
            DOM.profileContent.appendChild(grid);
        } else {
            DOM.profileContent.appendChild(createEmptyMessage('У вас пока нет публикаций'));
        }
    } catch (error) {
        DOM.profileContent.innerHTML = createErrorMessage(error.message);
    } finally {
        hideLoader();
    }
}

// Вспомогательные функции для создания сообщений
function createEmptyMessage(text) {
    return `
        <div class="empty-message">
            <div class="empty-icon">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF5F40" stroke-width="2"/>
                    <path d="M8 15C8 15 9 17 12 17C15 17 16 15 16 15" stroke="#FF5F40" stroke-width="2" stroke-linecap="round"/>
                    <circle cx="9" cy="10" r="1" fill="#FF5F40"/>
                    <circle cx="15" cy="10" r="1" fill="#FF5F40"/>
                </svg>
            </div>
            <p>${text}</p>
        </div>
    `;
}

function createErrorMessage(message) {
    return `
        <div class="error-container">
            <div class="error-icon">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF5F40" stroke-width="2"/>
                    <path d="M12 8V12" stroke="#FF5F40" stroke-width="2" stroke-linecap="round"/>
                    <circle cx="12" cy="16" r="1" fill="#FF5F40"/>
                </svg>
            </div>
            <p class='error'>${message}</p>
            <button class="btn btn-primary retry-btn" onclick="window.location.reload()">Повторить</button>
        </div>
    `;
}

function createAuthRequiredMessage() {
    return `
        <div class="empty-message">
            <div class="empty-icon">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF5F40" stroke-width="2"/>
                    <path d="M8 12H16" stroke="#FF5F40" stroke-width="2" stroke-linecap="round"/>
                    <path d="M12 8V16" stroke="#FF5F40" stroke-width="2" stroke-linecap="round"/>
                </svg>
            </div>
            <h3>Необходимо войти</h3>
            <p>Для просмотра профиля необходимо авторизоваться</p>
            <button class="btn btn-primary show-login-link">Войти</button>
        </div>
    `;
}