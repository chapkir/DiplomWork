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

// Обновляю константы для поддержки новых элементов UI
const PAGINATION = {
  PIN_PAGE_SIZE: 12,
  PIN_SKELETON_COUNT: 6,
  POST_PAGE_SIZE: 12
};

// Кэширование DOM элементов
const DOM = {
    loader: document.getElementById('global-loader'),
    loaderText: document.querySelector('.loader-text'),
    feed: document.getElementById('feed'),
    postsFeed: document.getElementById('posts-feed'),
    postsFeedMain: document.getElementById('posts-feed-main'),
    profileContent: document.getElementById('profile-content'),
    loadMorePins: document.getElementById('load-more-pins'),
    loadMorePosts: document.getElementById('load-more-posts'),
    categoryButtons: document.querySelectorAll('.category-btn'),
    spotModal: document.getElementById('spot-modal'),
    spotDetails: document.getElementById('spot-details'),
    closeModal: document.querySelector('.close-modal'),
    favoritesContainer: document.getElementById('favorites-container'),
    nearbyPlaces: document.getElementById('nearby-places'),
    getLocationBtn: document.getElementById('get-location-btn')
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

  // Находим текущий активный вид и плавно скрываем его
  const currentView = document.querySelector('.view.active');
  if (currentView && currentView !== view) {
    currentView.classList.add('fade-out');
    
    // Даем анимации время на выполнение перед скрытием элемента
    setTimeout(() => {
      currentView.classList.remove('active');
      currentView.classList.remove('fade-out');
      
      // Показываем новый вид
      view.classList.add('active');
    }, 300); // Это время должно соответствовать длительности CSS-анимации fade-out
  } else {
    // Если нет активного вида или он совпадает с запрошенным, просто показываем
    view.classList.add('active');
  }

  // Обновляем активную вкладку в навигации
  if (viewId !== 'login-view' && viewId !== 'signup-view') {
    const navId = viewId.replace('-view', '');
    document.querySelectorAll('.nav-link').forEach(link => link.classList.remove('active'));
    const activeLink = document.querySelector(`.nav-link[data-view="${navId}"]`);
    if (activeLink) activeLink.classList.add('active');
  }
  
  // Прокручиваем страницу вверх при смене раздела
  window.scrollTo({ top: 0, behavior: 'smooth' });
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

// Инициализация обработчиков событий при загрузке документа
document.addEventListener('DOMContentLoaded', async () => {
  console.log('DOM загружен, инициализация приложения...');
  
  // Сначала скрываем всё и показываем загрузчик
  hideAllExceptAuth();
  showLoader();
  
  // Проверяем кэш на актуальность
  checkForDataUpdates();
  
  // Проверяем авторизацию
  const isAuth = await checkAuthStatus();
  console.log('Статус авторизации:', isAuth ? 'авторизован' : 'не авторизован');
  
  // Обновляем состояние UI в зависимости от статуса авторизации
  state.isAuthenticated = isAuth;
  updateAuthState(isAuth);
  hideLoader();
  
  // Загружаем данные
  if (isAuth) {
    loadFeed();
    loadPosts();
    loadProfile();
    loadFavorites();
  } else {
    loadFeed(); // Даже для неавторизованных пользователей показываем пины
  }

  // Инициализация категорий
  initCategoriesFilter();
  
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
  navLinks.forEach(link => {
    link.addEventListener('click', e => {
      e.preventDefault();
      
      const view = link.dataset.view;
      console.log('Переход к разделу:', view);
      
      // Проверяем авторизацию перед переходом
      if (!token && (view === 'favorites' || view === 'profile')) {
        console.log('Попытка перехода без авторизации - показываем форму логина');
        showView('login-view');
        return;
      }
      
      showView(view + '-view');
      
      // Загружаем данные для выбранного раздела
      if (view === 'feed') {
        pinNextCursor = null; // сброс пагинации
        loadFeed(false);
      }
      if (view === 'posts') {
        postNextCursor = null;
        loadPosts(false);
      }
      if (view === 'profile' && token) loadProfile();
      if (view === 'favorites' && token) loadFavorites();
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
        showView('feed-view');
        pinNextCursor = null;
        loadFeed(false);
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
  
  // Закрытие модального окна
  const closeModal = document.querySelector('.close-modal');
  if (closeModal) {
    closeModal.addEventListener('click', closeSpotDetails);
  }
  
  // Закрытие модального окна по клику за его пределами
  const spotModal = document.getElementById('spot-modal');
  if (spotModal) {
    window.addEventListener('click', (e) => {
      if (e.target === spotModal) {
        closeSpotDetails();
      }
    });
  }
  
  // Закрытие модального окна по нажатию Escape
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && spotModal && spotModal.style.display === 'block') {
      closeSpotDetails();
    }
  });
});

// Инициализация фильтров категорий
function initCategoriesFilter() {
  const categoryButtons = document.querySelectorAll('.category-btn');
  
  categoryButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      // Убираем активный класс со всех кнопок
      categoryButtons.forEach(b => b.classList.remove('active'));
      
      // Добавляем активный класс к нажатой кнопке
      btn.classList.add('active');
      
      // Сохраняем выбранную категорию в локальном хранилище для персистентности
      const category = btn.dataset.category;
      localStorage.setItem('selectedCategory', category);
      
      // Сбрасываем курсор пагинации
      pinNextCursor = null;
      
      // Очищаем кэш пинов при смене категории
      ApiCache.clear('/api/pins');
      
      // Загружаем места по выбранной категории
      loadFeed();
    });
  });
  
  // Восстанавливаем выбранную категорию из локального хранилища
  const savedCategory = localStorage.getItem('selectedCategory');
  if (savedCategory) {
    const categoryBtn = document.querySelector(`.category-btn[data-category="${savedCategory}"]`);
    if (categoryBtn) {
      categoryButtons.forEach(b => b.classList.remove('active'));
      categoryBtn.classList.add('active');
    }
  }
}

// Добавляем обработчик для кнопки "Загрузить еще"
document.getElementById('load-more-pins')?.addEventListener('click', () => {
  loadFeed(true); // true - добавляем к существующим пинам
});

// Добавляем обработчик для кнопки "Определить мое местоположение"
document.getElementById('get-location-btn')?.addEventListener('click', () => {
  if (navigator.geolocation) {
    showLoader('Определяем ваше местоположение...');
    navigator.geolocation.getCurrentPosition(position => {
      const latitude = position.coords.latitude;
      const longitude = position.coords.longitude;
      
      // Загружаем места рядом с пользователем
      loadNearbyPlaces(latitude, longitude);
    }, error => {
      hideLoader();
      const errorContainer = document.getElementById('nearby-places');
      if (errorContainer) {
        errorContainer.innerHTML = createErrorMessage(`Ошибка при определении местоположения: ${error.message}`);
      }
    });
  } else {
    const errorContainer = document.getElementById('nearby-places');
    if (errorContainer) {
      errorContainer.innerHTML = createErrorMessage('Геолокация не поддерживается вашим браузером');
    }
  }
});

// Функция для загрузки мест рядом с пользователем
async function loadNearbyPlaces(lat, lng) {
  const container = document.getElementById('nearby-places');
  if (!container) {
    hideLoader();
    return;
  }
  
  container.innerHTML = '';
  
  // Добавляем скелетоны для индикации загрузки
  for (let i = 0; i < PAGINATION.PIN_SKELETON_COUNT; i++) {
    const skeleton = createSkeletonCard();
    skeleton.classList.add('spot-card');
    container.appendChild(skeleton);
  }
  
  try {
    // Строим URL запроса с GPS координатами
    const url = `/api/pins/nearby?lat=${lat}&lng=${lng}&radius=10000`;
    
    try {
      // Используем кэширование с коротким TTL для геолокационных данных
      const spots = await ApiCache.get(url, {
        ttl: 5 * 60 * 1000, // 5 минут кэширования
        headers: authHeaders()
      });
      
      // Удаляем скелетоны
      const skeletons = container.querySelectorAll('.card.skeleton');
      skeletons.forEach(el => el.remove());
      
      if (spots.length === 0) {
        container.innerHTML = createEmptyMessage('Рядом с вами не найдено интересных мест');
        hideLoader();
        return;
      }
      
      renderSpots(spots, container);
      
    } catch (apiError) {
      console.error('Ошибка при загрузке мест рядом:', apiError);
      
      // Создаем моковые данные для геолокации, имитируя места рядом
      const mockNearbySpots = generateMockNearbySpots(lat, lng, 6);
      
      // Удаляем скелетоны
      const skeletons = container.querySelectorAll('.card.skeleton');
      skeletons.forEach(el => el.remove());
      
      renderSpots(mockNearbySpots, container);
      
      // Добавляем уведомление, что это демо-данные
      const noticeElement = document.createElement('div');
      noticeElement.className = 'notice-message';
      noticeElement.innerHTML = `
        <div class="notice-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="2"/>
            <path d="M12 8V12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <circle cx="12" cy="16" r="1" fill="currentColor"/>
          </svg>
        </div>
        <p>Примечание: отображаются демонстрационные данные, так как API не вернул реальные места.</p>
      `;
      container.insertAdjacentElement('beforebegin', noticeElement);
    }
    
    hideLoader();
  } catch (error) {
    console.error('Общая ошибка при загрузке мест рядом:', error);
    container.innerHTML = createErrorMessage(`Не удалось загрузить места: ${error.message}`);
    hideLoader();
  }
}

// Генерация моковых мест рядом с переданными координатами
function generateMockNearbySpots(lat, lng, count) {
  const spots = [];
  
  // Определяем случайные смещения относительно центральной точки
  // В пределах примерно 1-2 км
  for (let i = 1; i <= count; i++) {
    // Случайные смещения по широте и долготе (примерно ±0.01)
    const latOffset = (Math.random() - 0.5) * 0.02;
    const lngOffset = (Math.random() - 0.5) * 0.02;
    
    const categoryIndex = Math.floor(Math.random() * MOCK_DATA.CATEGORIES.length);
    const category = MOCK_DATA.CATEGORIES[categoryIndex];
    
    const titleIndex = Math.floor(Math.random() * MOCK_DATA.TITLES.length);
    const addressIndex = Math.floor(Math.random() * MOCK_DATA.ADDRESSES.length);
    
    spots.push({
      id: `near-${i}`,
      title: MOCK_DATA.TITLES[titleIndex],
      description: `Место рядом с вами. Расстояние: примерно ${Math.floor(Math.random() * 1500) + 100}м.`,
      imageUrl: `/img/placeholder.svg`,
      address: MOCK_DATA.ADDRESSES[addressIndex],
      tags: [category],
      latitude: lat + latOffset,
      longitude: lng + lngOffset,
      rating: 3.5 + Math.floor(Math.random() * 15) / 10,
      user: { username: 'Система' }
    });
  }
  
  return spots;
}

// Функция для создания skeleton-карточки
function createSkeletonCard() {
  const div = document.createElement('div');
  div.className = 'card skeleton';
  return div;
}

// Система кэширования для API-запросов
const ApiCache = {
  // Хранилище для кэша
  cache: {},
  
  // Время жизни кэша по умолчанию в миллисекундах (10 минут)
  defaultTTL: 10 * 60 * 1000,
  
  // Получение данных из кэша или через API-запрос
  async get(url, options = {}) {
    const cacheKey = this._getCacheKey(url, options);
    const cachedData = this._getFromCache(cacheKey);
    
    // Если есть валидный кэш, возвращаем его
    if (cachedData) {
      console.log(`Cache hit for ${url}`);
      return cachedData;
    }
    
    // Иначе делаем запрос и кэшируем результат
    console.log(`Cache miss for ${url}, fetching data`);
    try {
      // Установим TTL либо из опций, либо используем значение по умолчанию
      const ttl = options.ttl || this.defaultTTL;
      
      // Формируем заголовки запроса
      const headers = {
        'Content-Type': 'application/json',
        ...authHeaders(),
        ...options.headers
      };
      
      // Делаем запрос к серверу
      const response = await fetch(url, { 
        ...options, 
        headers 
      });
      
      // Если ответ не успешен, выбрасываем ошибку
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      
      // Парсим JSON из ответа
      const data = await response.json();
      
      // Кэшируем данные, если это не POST/PUT/DELETE запрос
      if (options.method === undefined || options.method === 'GET') {
        this._saveToCache(cacheKey, data, ttl);
      }
      
      return data;
    } catch (error) {
      console.error(`Error fetching ${url}:`, error);
      throw error;
    }
  },
  
  // Очистка кэша полностью или по URL
  clear(url = null) {
    if (url) {
      // Очищаем кэш только для указанного URL или шаблона URL
      Object.keys(this.cache).forEach(key => {
        if (key.includes(url)) {
          console.log(`Clearing cache for ${key}`);
          delete this.cache[key];
        }
      });
    } else {
      // Очищаем весь кэш
      console.log('Clearing all cache');
      this.cache = {};
    }
  },
  
  // Генерация ключа кэша из URL и опций запроса
  _getCacheKey(url, options) {
    const params = new URLSearchParams(url.split('?')[1] || '').toString();
    const method = options.method || 'GET';
    return `${method}:${url.split('?')[0]}?${params}`;
  },
  
  // Получение данных из кэша с проверкой на валидность
  _getFromCache(key) {
    const item = this.cache[key];
    
    // Проверка наличия элемента в кэше и его валидности по времени
    if (item && item.expiry > Date.now()) {
      return item.data;
    }
    
    // Если элемент просрочен, удаляем его из кэша
    if (item) {
      delete this.cache[key];
    }
    
    return null;
  },
  
  // Сохранение данных в кэш
  _saveToCache(key, data, ttl) {
    this.cache[key] = {
      data,
      expiry: Date.now() + ttl
    };
    
    console.log(`Cached data for ${key}, expires in ${ttl/1000}s`);
  }
};

// Обновляем функцию loadFeed для использования кэша
async function loadFeed(append = false) {
  console.log('Loading feed of spots, append:', append);
  showLoader('Загружаем интересные места...');
  
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
  for (let i = 0; i < PAGINATION.PIN_SKELETON_COUNT; i++) {
    const skeleton = createSkeletonCard();
    skeleton.classList.add('spot-card');
    container.appendChild(skeleton);
  }
  
  try {
    // Получаем активную категорию
    const activeCategory = document.querySelector('.category-btn.active');
    let category = activeCategory ? activeCategory.dataset.category : 'all';
    
    // Формируем URL запроса для получения мест (пинов)
    let url = `/api/pins?size=${PAGINATION.PIN_PAGE_SIZE}`;
    if (pinNextCursor) url += `&cursor=${encodeURIComponent(pinNextCursor)}`;
    if (category && category !== 'all') url += `&category=${category}`;

    console.log('Fetching spots from URL:', url);
    
    try {
      // Пробуем получить данные из кэша или с сервера
      const data = await ApiCache.get(url, { ttl: 5 * 60 * 1000 }); // 5 минут кэширования
      console.log('Spots API response:', data);
      
      // Парсинг ответа с учетом разных возможных форматов
      let spots = [];
      let hasNext = false;
      
      if (data.data && data.data.content) {
        // Формат HATEOAS
        const page = data.data;
        spots = page.content || [];
        pinNextCursor = page.nextCursor;
        hasNext = page.hasNext;
      } else if (Array.isArray(data)) {
        // Формат простого массива
        spots = data;
        hasNext = spots.length >= PAGINATION.PIN_PAGE_SIZE;
      } else if (data.content) {
        // Другой формат пагинации
        spots = data.content || [];
        hasNext = data.hasNext || false;
      }
      
      // Удалить все skeleton
      const skeletons = container.querySelectorAll('.card.skeleton');
      skeletons.forEach(el => el.remove());
      
      // Если не append и есть старый контент - очистить контейнер
      if (!append) container.innerHTML = '';
      
      if (spots.length === 0) {
        container.innerHTML = createEmptyMessage('Места не найдены');
        hideLoader();
        return;
      }
      
      // Улучшаем объекты мест перед рендерингом
      spots = spots.map(spot => {
        // Убедимся, что у каждого места есть нужные поля
        return {
          id: spot.id,
          title: spot.title || 'Интересное место',
          description: spot.description || '',
          imageUrl: spot.imageUrl || spot.thumbnailUrl || '/img/placeholder.svg',
          address: spot.address || 'Адрес не указан',
          tags: spot.tags || [],
          likes: spot.likes || [],
          user: spot.user || { username: 'Пользователь' },
          rating: spot.rating || 0
        };
      });
      
      renderSpots(spots, container);
      
      // Показываем кнопку Load More, если есть следующая страница
      if (btn) btn.style.display = hasNext ? 'block' : 'none';
      
    } catch (apiError) {
      console.error('Ошибка HTTP при загрузке мест:', apiError);
      
      // Пробуем альтернативные API-эндпоинты
      console.log('Trying alternative endpoint: /api/pins/all');
      try {
        // Используем кэш для альтернативного эндпоинта
        const spots = await ApiCache.get('/api/pins/all', { 
          ttl: 10 * 60 * 1000 // 10 минут кэширования
        });
        
        // Удалить все skeleton
        const skeletons = container.querySelectorAll('.card.skeleton');
        skeletons.forEach(el => el.remove());

        if (!append) container.innerHTML = '';
        
        if (spots.length === 0) {
          container.innerHTML = createEmptyMessage('Места не найдены');
          hideLoader();
          return;
        }
        
        renderSpots(spots, container);
        if (btn) btn.style.display = 'none'; // скрываем кнопку "Загрузить еще" для альтернативного API
      } catch (altError) {
        console.error('Ошибка при загрузке мест из альтернативного источника:', altError);
        
        // Если у нас нет данных ни из основного, ни из альтернативного источника - используем моковые данные
        const mockSpots = generateMockSpots(6);
        
        // Удалить все skeleton
        const skeletons = container.querySelectorAll('.card.skeleton');
        skeletons.forEach(el => el.remove());
        
        if (!append) container.innerHTML = '';
        renderSpots(mockSpots, container);
        
        if (btn) btn.style.display = 'none';
      }
    }
    
    hideLoader();
  } catch (e) {
    console.error('Общая ошибка при загрузке мест:', e);
    container.innerHTML = createErrorMessage('Ошибка при загрузке мест: ' + e.message);
    hideLoader();
  }
}

// Константы для моковых данных
const MOCK_DATA = {
  CATEGORIES: ['food', 'nature', 'culture', 'entertainment', 'sport', 'architecture'],
  TITLES: [
    'Уютное кафе в центре', 
    'Смотровая площадка', 
    'Исторический музей', 
    'Современный кинотеатр',
    'Спортивный комплекс', 
    'Архитектурный памятник', 
    'Городской парк',
    'Картинная галерея',
    'Ресторан национальной кухни',
    'Озеро в лесу',
    'Старинная крепость',
    'Научный центр'
  ],
  ADDRESSES: [
    'ул. Ленина, 25', 
    'пр. Мира, 17', 
    'Северный проспект, 122', 
    'ул. Пушкина, 45',
    'Южная набережная, 8', 
    'ул. Театральная, 11', 
    'пл. Революции, 3',
    'Зелёный бульвар, 47',
    'ул. Солнечная, 15',
    'Цветочная аллея, 7',
    'Речной переулок, 22',
    'Горный проспект, 54'
  ]
};

// Функция для подготовки более реалистичных моковых данных
function generateMockSpots(count) {
  const spots = [];
  
  for (let i = 1; i <= count; i++) {
    const categoryIndex = Math.floor(Math.random() * MOCK_DATA.CATEGORIES.length);
    const category = MOCK_DATA.CATEGORIES[categoryIndex];
    
    // Выбираем случайный заголовок и адрес из предопределенных массивов
    const titleIndex = Math.floor(Math.random() * MOCK_DATA.TITLES.length);
    const addressIndex = Math.floor(Math.random() * MOCK_DATA.ADDRESSES.length);
    
    spots.push({
      id: i,
      title: MOCK_DATA.TITLES[titleIndex],
      description: `Это отличное место для ${category === 'food' ? 'обеда' : 
                   category === 'nature' ? 'отдыха на природе' : 
                   category === 'culture' ? 'культурного просвещения' : 
                   category === 'entertainment' ? 'развлечений' : 
                   category === 'sport' ? 'занятий спортом' : 'осмотра архитектуры'}. 
                   Здесь вы сможете отлично провести время.`,
      imageUrl: `/img/placeholder.svg`,
      address: MOCK_DATA.ADDRESSES[addressIndex],
      tags: [category],
      rating: 3.5 + Math.floor(Math.random() * 15) / 10, // Случайный рейтинг от 3.5 до 5.0
      user: { username: 'Система' }
    });
  }
  
  return spots;
}

// Обработчик для проверки наличия обновлений данных
function checkForDataUpdates() {
  // Проверяем, не было ли перезагружено приложение или внесены изменения на сервере
  const lastCheck = localStorage.getItem('last_data_check');
  const now = Date.now();
  
  // Если прошло более 5 минут с последней проверки или проверки не было
  if (!lastCheck || now - parseInt(lastCheck) > 5 * 60 * 1000) {
    console.log('Clearing cache for data refresh');
    localStorage.setItem('last_data_check', now.toString());
    
    // Очищаем кэш данных, которые могли измениться
    ApiCache.clear('/api/pins');
    ApiCache.clear('/api/posts');
  }
}

// Функция для рендеринга мест на странице
function renderSpots(spots, container) {
  const fragment = document.createDocumentFragment();
  spots.forEach((spot, index) => {
    const card = createSpotCard(spot);
    card.style.animationDelay = `${0.1 * (index % 12)}s`;
    card.classList.add('fade-in');
    
    // Обработчик клика для открытия модального окна
    card.addEventListener('click', () => showSpotDetails(spot));
    
    fragment.appendChild(card);
  });
  
  container.appendChild(fragment);
}

// Функция для создания карточки места
function createSpotCard(spot) {
  // Определяем категорию
  let categoryName = 'Место';
  let categoryClass = '';
  
  if (spot.tags && spot.tags.length > 0) {
    categoryName = spot.tags[0];
    categoryClass = spot.tags[0].toLowerCase();
  } else if (spot.category) {
    // Если у пина есть поле category
    categoryName = spot.category;
    categoryClass = spot.category.toLowerCase();
  }
  
  // Обработка категорий (для совместимости с разными форматами данных)
  switch (categoryClass) {
    case 'food':
    case 'foodanddrink':
    case 'еда':
    case 'еда и напитки':
      categoryClass = 'food';
      categoryName = 'Еда и напитки';
      break;
    case 'nature':
    case 'природа':
      categoryClass = 'nature';
      categoryName = 'Природа';
      break;
    case 'culture':
    case 'культура':
      categoryClass = 'culture';
      categoryName = 'Культура';
      break;
    case 'entertainment':
    case 'развлечения':
      categoryClass = 'entertainment';
      categoryName = 'Развлечения';
      break;
    case 'sport':
    case 'спорт':
      categoryClass = 'sport';
      categoryName = 'Спорт';
      break;
    case 'architecture':
    case 'архитектура':
      categoryClass = 'architecture';
      categoryName = 'Архитектура';
      break;
    default:
      // Если категория не определена, оставляем как "Место"
      categoryClass = 'place';
      break;
  }
  
  // Определяем положение места
  let locationInfo = spot.address || 'Адрес не указан';

  // Создаем элемент карточки
  const card = document.createElement('div');
  card.className = `spot-card category-${categoryClass}`;
  card.dataset.id = spot.id;
  
  // URL изображения с фолбэком
  const imageUrl = spot.imageUrl || spot.thumbnailUrl || '/img/placeholder.svg';
  
  // Создаем содержимое карточки
  card.innerHTML = `
    <div class="spot-image-wrapper">
      <img src="${imageUrl}" alt="${spot.title}" class="spot-image" loading="lazy">
    </div>
    <div class="spot-content">
      <span class="spot-category ${categoryClass}">${categoryName}</span>
      <h3 class="spot-title">${spot.title}</h3>
      <p class="spot-address">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 110-5 2.5 2.5 0 010 5z" fill="currentColor"/>
        </svg>
        ${locationInfo}
      </p>
    </div>
    <div class="spot-meta">
      <div class="spot-rating">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z" fill="currentColor"/>
        </svg>
        ${spot.rating ? spot.rating.toFixed(1) : '4.5'}
      </div>
      <button class="spot-save" data-id="${spot.id}">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" xmlns="http://www.w3.org/2000/svg">
          <path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2v16z" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  `;
  
  // Добавляем обработчики событий
  const saveBtn = card.querySelector('.spot-save');
  if (saveBtn) {
    saveBtn.addEventListener('click', (e) => {
      e.stopPropagation(); // Останавливаем всплытие, чтобы не открылась карточка места
      toggleFavorite(spot.id, saveBtn);
    });
    
    // Проверяем, находится ли место в избранном
    if (token) {
      // Проверяем избранное синхронно
      const isLiked = isFavorite(spot.id);
      if (isLiked) {
        saveBtn.classList.add('active');
        saveBtn.querySelector('svg').setAttribute('fill', 'currentColor');
      }
    }
  }
  
  // Установка lazy loading для изображения
  const img = card.querySelector('img');
  if (img) {
    if ('loading' in HTMLImageElement.prototype) {
      img.loading = 'lazy';
    } else {
      loadImage(img);
    }
  }
  
  return card;
}

// Открытие модального окна с деталями места
function showSpotDetails(spot) {
  if (!spot) return;
  
  // Подготавливаем информацию о месте
  let categoryName = 'Место';
  if (spot.tags && spot.tags.length > 0) {
    categoryName = spot.tags[0];
  }
  
  // Определяем положение места
  let locationInfo = '';
  if (spot.address) {
    locationInfo = spot.address;
  } else if (spot.placeName) {
    locationInfo = spot.placeName;
  } else if (spot.latitude && spot.longitude) {
    locationInfo = `${spot.latitude.toFixed(6)}, ${spot.longitude.toFixed(6)}`;
  }
  
  // Формируем HTML для модального окна
  DOM.spotDetails.innerHTML = `
    <div class="spot-details-header">
      <img src="${spot.fullhdImageUrl || spot.imageUrl || '/img/placeholder.svg'}" class="spot-details-img" alt="${spot.title || 'Интересное место'}">
      <div class="spot-details-overlay">
        <div class="spot-details-category">${categoryName}</div>
        <h2 class="spot-details-title">${spot.title || 'Интересное место'}</h2>
      </div>
    </div>
    <div class="spot-details-body">
      <div class="spot-details-info">
        <div class="spot-details-section">
          <h3>Местоположение</h3>
          <div class="spot-details-address">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 13C13.6569 13 15 11.6569 15 10C15 8.34315 13.6569 7 12 7C10.3431 7 9 8.34315 9 10C9 11.6569 10.3431 13 12 13Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 22C16 18 20 14.4183 20 10C20 5.58172 16.4183 2 12 2C7.58172 2 4 5.58172 4 10C4 14.4183 8 18 12 22Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>${locationInfo || 'Неизвестное местоположение'}</span>
          </div>
          ${spot.latitude && spot.longitude ? `
            <div id="spot-map" class="spot-map" style="height: 200px; border-radius: 8px; margin-top: 16px;"></div>
          ` : ''}
        </div>
        <div class="spot-details-section">
          <h3>Информация</h3>
          <p>${spot.description || 'Нет дополнительной информации об этом месте.'}</p>
          <div class="spot-rating" style="margin-top: 16px; font-size: 1.2rem;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>${spot.rating ? spot.rating.toFixed(1) : '5.0'} рейтинг</span>
          </div>
        </div>
      </div>
      ${spot.fullhdImages && spot.fullhdImages.length > 0 ? `
        <div class="spot-details-section">
          <h3>Фотографии</h3>
          <div class="spot-gallery">
            ${spot.fullhdImages.map(img => `
              <div class="gallery-item">
                <img src="${img}" alt="${spot.title || 'Изображение места'}" loading="lazy">
              </div>
            `).join('')}
          </div>
        </div>
      ` : ''}
      <div class="spot-details-actions">
        <button class="btn btn-primary favorite-btn" data-id="${spot.id}">
          ${isFavorite(spot.id) ? 'Удалить из избранного' : 'Добавить в избранное'}
        </button>
        ${spot.latitude && spot.longitude ? `
          <a href="https://maps.google.com/?q=${spot.latitude},${spot.longitude}" class="btn btn-secondary" target="_blank">
            Открыть в Google Maps
          </a>
        ` : ''}
      </div>
    </div>
  `;
  
  // Показываем модальное окно
  DOM.spotModal.style.display = 'block';
  document.body.style.overflow = 'hidden'; // Блокируем прокрутку страницы
  
  // Добавляем обработчик для кнопки избранное в модальном окне
  const favoriteBtn = DOM.spotDetails.querySelector('.favorite-btn');
  if (favoriteBtn) {
    favoriteBtn.addEventListener('click', () => {
      toggleFavorite(spot.id);
      // Обновляем текст кнопки
      favoriteBtn.textContent = isFavorite(spot.id) ? 'Удалить из избранного' : 'Добавить в избранное';
    });
  }
}

// Закрытие модального окна
function closeSpotDetails() {
  DOM.spotModal.style.display = 'none';
  document.body.style.overflow = ''; // Разблокируем прокрутку страницы
}

// Функции для работы с избранными местами
function toggleFavorite(spotId, button) {
  if (!spotId) return;
  
  let favorites = JSON.parse(localStorage.getItem('favorites') || '[]');
  
  if (isFavorite(spotId)) {
    // Удаляем из избранного
    favorites = favorites.filter(id => id !== spotId);
    if (button) button.classList.remove('active');
  } else {
    // Добавляем в избранное
    favorites.push(spotId);
    if (button) button.classList.add('active');
  }
  
  localStorage.setItem('favorites', JSON.stringify(favorites));
  
  // Если пользователь находится на странице избранного, обновляем список
  if (document.getElementById('favorites-view').classList.contains('active')) {
    loadFavorites();
  }
}

function isFavorite(spotId) {
  if (!spotId) return false;
  
  const favorites = JSON.parse(localStorage.getItem('favorites') || '[]');
  return favorites.includes(spotId);
}

// Загрузка избранных мест
async function loadFavorites() {
  const container = DOM.favoritesContainer;
  if (!container) {
    console.error('Favorites container not found');
    return;
  }
  
  showLoader('Загружаем избранные места...');
  container.innerHTML = '';
  
  const favorites = JSON.parse(localStorage.getItem('favorites') || '[]');
  
  if (favorites.length === 0) {
    container.innerHTML = createEmptyMessage('У вас пока нет избранных мест');
    hideLoader();
    return;
  }
  
  try {
    const fragment = document.createDocumentFragment();
    let loadedCount = 0;
    
    for (const spotId of favorites) {
      try {
        const res = await fetch(`/api/pins/${spotId}`, { headers: authHeaders() });
        
        if (!res.ok) {
          console.warn(`Ошибка при загрузке места с ID ${spotId}: ${res.status}`);
          continue;
        }
        
        const spot = await res.json();
        const card = createSpotCard(spot);
        card.style.animationDelay = `${0.1 * (loadedCount % 12)}s`;
        card.classList.add('fade-in');
        
        // Обработчик клика для открытия модального окна
        card.addEventListener('click', () => showSpotDetails(spot));
        
        fragment.appendChild(card);
        loadedCount++;
      } catch (err) {
        console.error(`Ошибка при загрузке места с ID ${spotId}:`, err);
      }
    }
    
    if (loadedCount === 0) {
      container.innerHTML = createEmptyMessage('Не удалось загрузить избранные места');
    } else {
      container.appendChild(fragment);
    }
  } catch (err) {
    console.error('Ошибка при загрузке избранных мест:', err);
    container.innerHTML = createErrorMessage('Ошибка при загрузке избранных мест');
  }
  
  hideLoader();
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

// Оптимизированная функция загрузки постов с кэшированием
async function loadPosts(append = false) {
    // Определяем какой контейнер постов нужно использовать в зависимости от активного view
    let container;
    const postsViewActive = document.getElementById('posts-view')?.classList.contains('active');
    
    if (postsViewActive) {
        container = DOM.postsFeedMain;
    } else {
        container = DOM.postsFeed;
    }
    
    // Если контейнер не найден, попробуем найти контейнер postsFeedMain, возможно мы в разделе публикаций
    if (!container) {
        console.log('Posts container not found, trying alternatives');
        
        // Проверяем различные варианты контейнеров
        container = document.getElementById('posts-feed-main') || document.getElementById('posts-feed');
        
        // Если нашли контейнер, обновляем DOM reference
        if (container && container.id === 'posts-feed-main') {
            DOM.postsFeedMain = container;
        } else if (container && container.id === 'posts-feed') {
            DOM.postsFeed = container;
        }
    }
    
    // Если контейнер всё ещё не найден, выходим
    if (!container) {
        console.error('Cannot find or create posts container');
        hideLoader();
        return;
    }
    
    if (!append) {
        container.innerHTML = '';
        state.postNextCursor = null;
        if (DOM.loadMorePosts) DOM.loadMorePosts.style.display = 'none';
    }

    showLoader('Загружаем публикации...');
    
    try {
        const url = `/api/posts/cursor?size=${PAGINATION.POST_PAGE_SIZE}${state.postNextCursor ? `&cursor=${encodeURIComponent(state.postNextCursor)}` : ''}`;
        
        try {
            // Используем кэширование для загрузки постов
            const data = await ApiCache.get(url, { ttl: 5 * 60 * 1000 }); // 5 минут кэширования
            console.log('Posts API response:', data);
            
            let posts = [];
            let hasNext = false;
            
            if (data.content) {
                // Стандартный формат пагинации
                posts = data.content;
                state.postNextCursor = data.nextCursor;
                hasNext = data.hasNext;
            } else if (Array.isArray(data)) {
                // Формат простого массива
                posts = data;
                hasNext = posts.length >= PAGINATION.POST_PAGE_SIZE;
            }
            
            if (!append) container.innerHTML = '';
            
            if (posts.length === 0) {
                container.innerHTML = createEmptyMessage('Публикации не найдены');
                hideLoader();
                return;
            }
            
            renderPosts(posts, container);
            
            if (DOM.loadMorePosts) {
                DOM.loadMorePosts.style.display = hasNext ? 'block' : 'none';
            }
        } catch (apiError) {
            console.error('Ошибка при загрузке публикаций:', apiError);
            
            try {
                // Пробуем альтернативный API-эндпоинт
                const posts = await ApiCache.get('/api/posts', { 
                    ttl: 10 * 60 * 1000 // 10 минут кэширования
                });
                
                if (!append) container.innerHTML = '';
                
                if (!posts || posts.length === 0) {
                    container.innerHTML = createEmptyMessage('Публикации не найдены');
                    hideLoader();
                    return;
                }
                
                renderPosts(posts, container);
            } catch (altError) {
                console.error('Ошибка при загрузке публикаций из альтернативного источника:', altError);
                
                // Создаем моковые данные
                const mockPosts = generateMockPosts(6);
                
                if (!append) container.innerHTML = '';
                renderPosts(mockPosts, container);
                
                if (DOM.loadMorePosts) {
                    DOM.loadMorePosts.style.display = 'none';
                }
            }
        }
    } catch (error) {
        console.error('Общая ошибка при загрузке публикаций:', error);
        container.innerHTML = createErrorMessage(error.message || 'Ошибка при загрузке публикаций');
    } finally {
        hideLoader();
    }
}

// Функция для создания моковых публикаций
function generateMockPosts(count) {
    const posts = [];
    
    for (let i = 1; i <= count; i++) {
        posts.push({
            id: i,
            title: `Публикация ${i}`,
            text: `Пример текста публикации ${i}. Здесь мог бы быть пост пользователя.`,
            imageUrl: '/img/placeholder.svg',
            username: `Пользователь_${i}`,
            createdAt: new Date(Date.now() - i * 86400000).toISOString() // i дней назад
        });
    }
    
    return posts;
}

// Функция для рендеринга постов
function renderPosts(posts, container) {
    const fragment = document.createDocumentFragment();
    
    posts.forEach((post, index) => {
        const card = createCard(
            post.thumbnailImageUrl || post.fullhdImageUrl || post.imageUrl,
            post.title || post.text || 'Без названия',
            post.username || 'Пользователь'
        );
        
        card.style.animationDelay = `${0.1 * (index % 12)}s`;
        card.classList.add('fade-in', 'post-card');
        fragment.appendChild(card);
    });
    
    container.appendChild(fragment);
}

// Оптимизированная функция загрузки профиля с использованием кэша
async function loadProfile() {
    const profileContent = document.getElementById('profile-content');
    
    if (!profileContent) {
        console.error('Profile content container not found');
        return;
    }
    
    if (!state.token) {
        profileContent.innerHTML = createAuthRequiredMessage();
        return;
    }

    showLoader('Загружаем профиль...');
    
    try {
        // Используем кэширование для получения данных пользователя
        const user = await ApiCache.get('/api/auth/me', { ttl: 10 * 60 * 1000 }); // Кэшируем на 10 минут
        console.log('User profile data:', user);
        
        try {
            // Получаем посты пользователя
            const posts = await ApiCache.get(`/api/posts/user/${user.id}`, { ttl: 5 * 60 * 1000 });
            console.log('User posts:', posts);
            
            profileContent.innerHTML = `
                <div class="profile-header">
                    <div class="profile-avatar">
                        <img src="${user.profileImageUrl || '/img/placeholder.svg'}" alt="${user.username}">
                    </div>
                    <div class="profile-info">
                        <h1>${user.username}</h1>
                        <p>${user.email || ''}</p>
                        <div class="profile-stats">
                            <div class="stat-item">
                                <div class="stat-number">${user.followersCount || 0}</div>
                                <div class="stat-label">Подписчики</div>
                            </div>
                            <div class="stat-item">
                                <div class="stat-number">${user.followingCount || 0}</div>
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
                grid.className = 'spots-grid';
                
                const fragment = document.createDocumentFragment();
                posts.forEach((post, index) => {
                    try {
                        const card = createSpotCard({
                            id: post.id,
                            title: post.title || post.text || 'Публикация пользователя',
                            description: post.text,
                            imageUrl: post.thumbnailImageUrl || post.fullhdImageUrl || post.imageUrl || '/img/placeholder.svg',
                            address: post.location || '',
                            rating: post.rating || 4.5,
                            user: { username: user.username },
                            tags: post.tags || []
                        });

                        if (card) {
                            card.style.animationDelay = `${0.1 * (index % 12)}s`;
                            card.classList.add('fade-in');
                            fragment.appendChild(card);
                        }
                    } catch (cardError) {
                        console.error('Ошибка при создании карточки:', cardError);
                    }
                });

                grid.appendChild(fragment);
                profileContent.appendChild(grid);
            } else {
                profileContent.appendChild(document.createRange().createContextualFragment(createEmptyMessage('У вас пока нет публикаций')));
            }
        } catch (postsError) {
            console.error('Ошибка при загрузке публикаций пользователя:', postsError);

            // Продолжаем отображать профиль, но с сообщением об ошибке для постов
            profileContent.innerHTML = `
                <div class="profile-header">
                    <div class="profile-avatar">
                        <img src="${user.profileImageUrl || '/img/placeholder.svg'}" alt="${user.username}">
                    </div>
                    <div class="profile-info">
                        <h1>${user.username}</h1>
                        <p>${user.email || ''}</p>
                    </div>
                </div>
            `;

            const errorElement = document.createElement('div');
            errorElement.innerHTML = createErrorMessage('Не удалось загрузить публикации');
            profileContent.appendChild(errorElement);
        }
    } catch (error) {
        console.error('Ошибка при загрузке профиля:', error);
        profileContent.innerHTML = createErrorMessage('Не удалось загрузить данные профиля');
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

// Добавим скрипт для очистки кеша браузера при загрузке страницы
(function clearBrowserCache() {
  console.log('Clearing browser cache for app.js');

  // Добавляем случайный параметр к URL скрипта при следующей загрузке
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.getRegistrations().then(function(registrations) {
      for (let registration of registrations) {
        registration.unregister();
      }
    });
  }

  // Очищаем кеш API
  ApiCache.clear();

  // Устанавливаем версию приложения для отслеживания изменений
  localStorage.setItem('appVersion', '1.0.1');
})();