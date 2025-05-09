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
  const loader = document.getElementById('global-loader');
  const loaderText = loader ? loader.querySelector('.loader-text') : null;

  if (loaderText) {
    loaderText.textContent = message;
  }

  if (loader) {
    loader.style.display = 'flex';
  }
}

function hideLoader() {
  const loader = document.getElementById('global-loader');
  if (loader) loader.style.display = 'none';
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

// Загрузка постов с постраничной пагинацией и обработкой ошибок
async function loadPosts(append = false) {
  console.log('Loading posts, append:', append);
  showLoader('Загружаем посты...');
  const container = document.getElementById('posts-feed');
  if (!container) {
    console.error('Posts container not found');
    hideLoader();
    return;
  }

  const btn = document.getElementById('load-more-posts');
  if (!append) {
    container.innerHTML = '';
    if (btn) btn.style.display = 'none';
    postNextCursor = null;
  }

  // Показываем скелетоны во время загрузки
  for (let i = 0; i < postPageSize; i++) container.appendChild(createSkeletonCard());

  try {
    // Cursor-based pagination для постов
    let url = `/api/posts/cursor?size=${postPageSize}`;
    if (postNextCursor) url += `&cursor=${encodeURIComponent(postNextCursor)}`;
    console.log('Fetching posts from URL:', url);
    const res = await fetch(url, {
      headers: authHeaders(),
      signal: AbortSignal.timeout(15000)
    });

    if (!res.ok) {
      console.error('Ошибка HTTP при загрузке постов:', res.status);

      // Отображаем сообщение об ошибке в соответствии со статусом
      let errorMessage = `Не удалось загрузить посты: ${res.status}`;

      if (res.status === 500) {
        errorMessage = 'Внутренняя ошибка сервера. Пожалуйста, попробуйте позже.';
      } else if (res.status === 401) {
        errorMessage = 'Необходима авторизация для просмотра контента.';
      } else if (res.status === 404) {
        errorMessage = 'Посты не найдены. Попробуйте позже.';
      }

      if (!append) {
        container.innerHTML = `
          <div class="error-container">
            <div class="error-icon">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF5F40" stroke-width="2"/>
                <path d="M12 8V12" stroke="#FF5F40" stroke-width="2" stroke-linecap="round"/>
                <circle cx="12" cy="16" r="1" fill="#FF5F40"/>
              </svg>
            </div>
            <p class='error'>${errorMessage}</p>
            <button class="btn btn-primary retry-btn" onclick="loadPosts(false)">Повторить</button>
          </div>
        `;
      }
      hideLoader();
      return;
    }

    const json = await res.json();
    console.log('Posts API response:', json);

    // Удалить все skeleton
    const skeletons = container.querySelectorAll('.card.skeleton');
    skeletons.forEach(el => el.remove());

    // Получаем посты из ответа API и обновляем курсор
    const posts = json.content || [];
    postNextCursor = json.nextCursor;

    // Если не append и есть старый контент - очистить контейнер
    if (!append) container.innerHTML = '';

    if (posts.length === 0) {
      container.innerHTML = `
        <div class="empty-message">
          <div class="empty-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF5F40" stroke-width="2"/>
              <path d="M8 15C8 15 9 17 12 17C15 17 16 15 16 15" stroke="#FF5F40" stroke-width="2" stroke-linecap="round"/>
              <circle cx="9" cy="10" r="1" fill="#FF5F40"/>
              <circle cx="15" cy="10" r="1" fill="#FF5F40"/>
            </svg>
          </div>
          <p>Посты не найдены</p>
        </div>
      `;
      hideLoader();
      return;
    }

    // Создаем карточки для каждого поста, предпочитая WebP формат
    posts.forEach((post, index) => {
      // Предпочитаем WebP формат изображения, используя все доступные URL
      const imgSrc = post.thumbnailImageUrl || post.fullhdImageUrl || post.imageUrl || '';

      // Создаем карточку с изображением и текстом поста
      const card = createCard(imgSrc, post.title || 'Без названия', post.username || 'Автор');

      // Добавляем дополнительную информацию к карточке
      const contentDiv = card.querySelector('.card-content');
      if (contentDiv && post.text) {
        const descDiv = document.createElement('div');
        descDiv.className = 'card-description';
        descDiv.textContent = post.text.length > 100 ? post.text.substring(0, 100) + '...' : post.text;
        contentDiv.appendChild(descDiv);
      }

      // Сохраняем ID поста для возможности открытия деталей
      card.dataset.id = post.id;
      card.classList.add('fade-in');
      card.classList.add('post-card');

      // Добавляем тень и эффект при наведении
      card.style.animationDelay = `${0.1 * (index % 12)}s`;

      // Добавляем событие при клике
      card.addEventListener('click', () => {
        // TODO: Показывать детали поста
        console.log('Открытие деталей поста:', post.id);
      });

      container.appendChild(card);
    });

    // Показываем кнопку Load More, если есть следующая страница
    if (btn) {
      btn.style.display = json.hasNext ? 'block' : 'none';
      btn.innerText = 'Загрузить ещё';
    }

    hideLoader();
  } catch (e) {
    console.error('Ошибка в обработке постов', e);
    container.innerHTML = `
      <div class="error-container">
        <div class="error-icon">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF5F40" stroke-width="2"/>
            <path d="M12 8V12" stroke="#FF5F40" stroke-width="2" stroke-linecap="round"/>
            <circle cx="12" cy="16" r="1" fill="#FF5F40"/>
          </svg>
        </div>
        <p class='error'>Произошла ошибка при загрузке постов: ${e.message || 'Неизвестная ошибка'}</p>
        <button class="btn btn-primary retry-btn" onclick="loadPosts(false)">Повторить</button>
      </div>
    `;
    hideLoader();
  }
}

// Загрузка профиля с улучшенным интерфейсом
async function loadProfile() {
  console.log('Loading profile');
  showLoader('Загружаем профиль...');
  const content = document.getElementById('profile-content');

  if (!content) {
    console.error('Profile content container not found');
    hideLoader();
    return;
  }

  content.innerHTML = '';

  if (!token) {
    // Если пользователь не авторизован, показываем сообщение
    content.innerHTML = `
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
      </div>`;

    const loginBtn = content.querySelector('.show-login-link');
    if (loginBtn) {
      loginBtn.addEventListener('click', (e) => {
        e.preventDefault();
        showView('login-view');
      });
    }
    hideLoader();
    return;
  }

  try {
    const resUser = await fetch('/api/auth/me', { headers: authHeaders() });

    if (!resUser.ok) {
      console.error('Ошибка при загрузке профиля:', resUser.status);
      content.innerHTML = `
        <div class="error-container">
          <div class="error-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF5F40" stroke-width="2"/>
              <path d="M12 8V12" stroke="#FF5F40" stroke-width="2" stroke-linecap="round"/>
              <circle cx="12" cy="16" r="1" fill="#FF5F40"/>
            </svg>
          </div>
          <p class='error'>Ошибка авторизации. Попробуйте войти снова.</p>
          <button class="btn btn-primary retry-btn" onclick="showView('login-view')">Войти</button>
        </div>`;
      hideLoader();
      return;
    }

    const user = await resUser.json();
    console.log('User profile data:', user);

    // Создаем header профиля с улучшенным дизайном
    const header = document.createElement('div');
    header.className = 'profile-header';
    header.innerHTML = `
      <div class="profile-avatar">
        <img src="${user.profileImageUrl || '/img/avatar-placeholder.png'}" alt="${user.username || 'User'}">
      </div>
      <div class="profile-info">
        <h1>${user.username || 'Пользователь'}</h1>
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
            <div class="stat-number" id="posts-count">0</div>
            <div class="stat-label">Публикации</div>
          </div>
        </div>
      </div>
    `;
    content.appendChild(header);

    // Загружаем посты пользователя
    try {
      showLoader('Загружаем публикации...');
      const resPosts = await fetch(`/api/posts/user/${user.id}`, { headers: authHeaders() });

      if (!resPosts.ok) {
        console.error('Ошибка при загрузке постов пользователя:', resPosts.status);
        const sectionTitle = document.createElement('h3');
        sectionTitle.className = 'section-title';
        sectionTitle.textContent = 'Мои публикации';
        content.appendChild(sectionTitle);

        const errorMsg = document.createElement('div');
        errorMsg.className = 'error-container';
        errorMsg.innerHTML = `
          <div class="error-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF5F40" stroke-width="2"/>
              <path d="M12 8V12" stroke="#FF5F40" stroke-width="2" stroke-linecap="round"/>
              <circle cx="12" cy="16" r="1" fill="#FF5F40"/>
            </svg>
          </div>
          <p class='error'>Не удалось загрузить публикации</p>
          <button class="btn btn-primary retry-btn" onclick="loadProfile()">Повторить</button>
        `;
        content.appendChild(errorMsg);
        hideLoader();
        return;
      }

      const posts = await resPosts.json();
      console.log('User posts:', posts);

      // Обновляем счетчик публикаций
      const postsCount = document.getElementById('posts-count');
      if (postsCount) {
        postsCount.textContent = posts ? posts.length : 0;
      }

      const gridHeader = document.createElement('h3');
      gridHeader.className = 'section-title';
      gridHeader.textContent = 'Мои публикации';
      content.appendChild(gridHeader);

      if (!posts || posts.length === 0) {
        const emptyMessage = document.createElement('div');
        emptyMessage.className = 'empty-message';
        emptyMessage.innerHTML = `
          <div class="empty-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF5F40" stroke-width="2"/>
              <path d="M8 15C8 15 9 17 12 17C15 17 16 15 16 15" stroke="#FF5F40" stroke-width="2" stroke-linecap="round"/>
              <circle cx="9" cy="10" r="1" fill="#FF5F40"/>
              <circle cx="15" cy="10" r="1" fill="#FF5F40"/>
            </svg>
          </div>
          <p>У вас пока нет публикаций</p>
          <a href="#" class="btn btn-primary" onclick="showView('post-view')">Создать пост</a>
        `;
        content.appendChild(emptyMessage);
      } else {
        const grid = document.createElement('div');
        grid.className = 'masonry-grid';

        posts.forEach((post, index) => {
          // Предпочитаем WebP формат изображения, используя все доступные URL
          const imgSrc = post.thumbnailImageUrl || post.fullhdImageUrl || post.imageUrl || '';

          const card = createCard(imgSrc, post.title || post.text || 'Без названия', user.username);
          card.style.animationDelay = `${0.1 * (index % 12)}s`;
          card.classList.add('fade-in');
          card.classList.add('post-card');

          // Добавляем описание, если есть текст
          const contentDiv = card.querySelector('.card-content');
          if (contentDiv && post.text && post.text.length > 0) {
            const descDiv = document.createElement('div');
            descDiv.className = 'card-description';
            descDiv.textContent = post.text.length > 100 ? post.text.substring(0, 100) + '...' : post.text;
            contentDiv.appendChild(descDiv);
          }

          grid.appendChild(card);
        });
        content.appendChild(grid);
      }
    } catch (e) {
      console.error('Ошибка загрузки постов пользователя', e);
      const errorMsg = document.createElement('div');
      errorMsg.className = 'error-container';
      errorMsg.innerHTML = `
        <div class="error-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF5F40" stroke-width="2"/>
            <path d="M12 8V12" stroke="#FF5F40" stroke-width="2" stroke-linecap="round"/>
            <circle cx="12" cy="16" r="1" fill="#FF5F40"/>
          </svg>
        </div>
        <p class='error'>Произошла ошибка: ${e.message || 'Ошибка загрузки данных'}</p>
      `;
      content.appendChild(errorMsg);
    }

    hideLoader();
  } catch (e) {
    console.error('Ошибка загрузки профиля', e);
    hideLoader();

    content.innerHTML = `
      <div class="error-container">
        <div class="error-icon">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF5F40" stroke-width="2"/>
            <path d="M12 8V12" stroke="#FF5F40" stroke-width="2" stroke-linecap="round"/>
            <circle cx="12" cy="16" r="1" fill="#FF5F40"/>
          </svg>
        </div>
        <p class='error'>Ошибка загрузки профиля: ${e.message || 'Неизвестная ошибка'}</p>
        <button class="btn btn-primary retry-btn" onclick="loadProfile()">Повторить</button>
      </div>`;
  }
}

// Создание карточки с изображением и текстом
function createCard(img, title, subtitle) {
  const card = document.createElement('div');
  card.className = 'card';

  // Обертка для изображения с эффектом загрузки
  const imageWrapper = document.createElement('div');
  imageWrapper.className = 'card-image-wrapper';

  // Создаем миниатюру (скелетон) для изображения
  const imageSkeleton = document.createElement('div');
  imageSkeleton.className = 'card-image-skeleton';
  imageWrapper.appendChild(imageSkeleton);

  // Создаем изображение с отложенной загрузкой и обработкой ошибок
  if (img) {
    const image = document.createElement('img');
    // Добавляем обработчики событий
    image.onload = function() {
      // Скрываем skeleton и показываем загруженное изображение с анимацией
      imageSkeleton.style.opacity = '0';
      image.classList.add('loaded');

      // Обновляем соотношение сторон, если нужно
      const aspectRatio = image.naturalHeight / image.naturalWidth;
      if (aspectRatio > 1.5) {
        // Для вертикальных изображений
        image.style.objectPosition = 'center top';
      } else if (aspectRatio < 0.6) {
        // Для очень широких изображений
        image.style.objectPosition = 'center center';
      }
    };

    image.onerror = function() {
      // Показываем плейсхолдер при ошибке загрузки
      image.src = '/img/image-placeholder.png';
      imageSkeleton.style.opacity = '0';
      image.classList.add('loaded');
    };

    // Устанавливаем базовые атрибуты и стили
    image.loading = 'lazy'; // Используем нативную ленивую загрузку
    image.className = 'card-image';
    image.alt = title || 'Image';
    image.src = img; // Устанавливаем src в последнюю очередь

    imageWrapper.appendChild(image);
  } else {
    // Если изображение отсутствует, добавляем плейсхолдер
    const placeholderImg = document.createElement('img');
    placeholderImg.src = '/img/image-placeholder.png';
    placeholderImg.alt = 'No image';
    placeholderImg.className = 'card-image loaded';
    imageWrapper.appendChild(placeholderImg);
    imageSkeleton.style.opacity = '0';
  }

  card.appendChild(imageWrapper);

  // Добавляем содержимое карточки
  const content = document.createElement('div');
  content.className = 'card-content';

  // Добавляем заголовок, если он есть
  if (title) {
    const titleEl = document.createElement('h3');
    titleEl.className = 'card-title';
    titleEl.textContent = title;
    content.appendChild(titleEl);
  }

  // Добавляем подзаголовок, если он есть
  if (subtitle) {
    const subtitleEl = document.createElement('p');
    subtitleEl.className = 'card-subtitle';
    subtitleEl.textContent = subtitle;
    content.appendChild(subtitleEl);
  }

  card.appendChild(content);

  return card;
}