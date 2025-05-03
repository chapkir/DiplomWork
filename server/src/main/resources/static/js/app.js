// Основной SPA-скрипт
(function() {
  // Инициализация AOS
  AOS.init({
    duration: 800,
    easing: 'ease-out',
    once: false,
    offset: 50,
    delay: 100
  });

  // Вьюхи
  const loginView = document.getElementById('login-view');
  const signupView = document.getElementById('signup-view');
  const feedView = document.getElementById('feed-view');
  const createView = document.getElementById('create-view');
  const detailView = document.getElementById('detail-view');

  // Прелоадер
  const preloader = document.querySelector('.preloader');

  // Скрываем прелоадер после загрузки
  window.addEventListener('load', () => {
    setTimeout(() => {
      preloader.classList.add('hidden');
    }, 500);
  });

  // Меню и элементы управления
  const navbar = document.querySelector('.navbar');
  const profileBtn = document.getElementById('profile-btn');
  const profileMenu = document.getElementById('profile-menu');
  const logoutBtn = document.getElementById('logout-btn');

  // Переключение между входом/регистрацией
  document.getElementById('show-signup').addEventListener('click', e => {
    e.preventDefault();
    showView(signupView);
  });

  document.getElementById('show-login').addEventListener('click', e => {
    e.preventDefault();
    showView(loginView);
  });

  // Формы и кнопки
  const loginForm = document.getElementById('login-form');
  const signupForm = document.getElementById('signup-form');
  const themeToggleBtn = document.getElementById('theme-toggle');
  const feedContainer = document.getElementById('feed');
  const refreshFeedBtn = document.getElementById('refresh-feed');
  const loadMoreBtn = document.getElementById('load-more-btn');
  const createBtn = document.getElementById('create-pin-btn');
  const createForm = document.getElementById('create-form');
  const cancelCreateBtn = document.getElementById('cancel-create');
  const cancelCreateFormBtn = document.getElementById('cancel-create-btn');
  const detailClose = document.getElementById('close-detail');
  const detailTitle = document.getElementById('detail-title');
  const detailBody = document.getElementById('detail-body');

  // Элементы для дропзоны изображений
  const dropzone = document.getElementById('image-dropzone');
  const fileInput = document.getElementById('pin-image');
  const imagePreview = document.getElementById('image-preview');
  const imagePreviewContainer = document.getElementById('image-preview-container');
  const removeImageBtn = document.getElementById('remove-image');

  // Поиск и фильтрация
  const searchInput = document.getElementById('search-input');
  const filterTags = document.querySelectorAll('.filter-tag');

  // State
  let nextCursor = null;
  let token = localStorage.getItem('token');
  let refreshToken = localStorage.getItem('refreshToken');
  let currentFilter = 'all';
  let searchQuery = '';
  let pins = [];

  // Показать указанную вью и скрыть остальные
  function showView(view) {
    [loginView, signupView, feedView, createView, detailView].forEach(v => v.classList.remove('active'));
    view.classList.add('active');
    // Меню
    navbar.style.display = (view === feedView || view === createView || view === detailView) ? 'flex' : 'none';
  }

  // Установка темы
  function setTheme(theme) {
    if (theme === 'dark') document.documentElement.setAttribute('data-theme', 'dark');
    else document.documentElement.removeAttribute('data-theme');
    localStorage.setItem('theme', theme);
  }

  // Переключение темы
  themeToggleBtn.addEventListener('click', () => {
    const current = document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
    setTheme(current === 'dark' ? 'light' : 'dark');
  });

  // Клик по аватару
  profileBtn.addEventListener('click', e => {
    e.stopPropagation();
    profileMenu.classList.toggle('hidden');
  });

  // Закрыть меню при клике вне
  document.addEventListener('click', () => profileMenu.classList.add('hidden'));

  // Logout
  logoutBtn.addEventListener('click', () => {
    fetch('/api/auth/logout', { method: 'POST', headers: authHeaders() })
      .finally(() => {
        localStorage.clear();
        token = null;
        showToast('Выход выполнен', 'Вы успешно вышли из системы', 'success');
        showView(loginView);
      });
  });

  // Авторизационные хэдеры
  function authHeaders() {
    return token ? { 'Authorization': 'Bearer ' + token } : {};
  }

  // Обработка формы входа
  loginForm.addEventListener('submit', e => {
    e.preventDefault();

    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    // Вывести прелоадер
    showLoading();

    fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    })
    .then(res => {
      if (!res.ok) throw new Error('Ошибка авторизации');
      return res.json();
    })
    .then(data => {
      token = data.token;
      refreshToken = data.refreshToken;
      localStorage.setItem('token', token);
      localStorage.setItem('refreshToken', refreshToken);
      showToast('Успешный вход', 'Добро пожаловать в Spotsy!', 'success');
      afterAuth();
    })
    .catch(err => {
      showToast('Ошибка входа', err.message || 'Проверьте имя пользователя и пароль', 'error');
    })
    .finally(() => {
      hideLoading();
    });
  });

  // Обработка формы регистрации
  signupForm.addEventListener('submit', e => {
    e.preventDefault();

    const username = document.getElementById('signup-username').value;
    const email = document.getElementById('signup-email').value;
    const password = document.getElementById('signup-password').value;

    // Вывести прелоадер
    showLoading();

    fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password })
    })
    .then(res => {
      if (!res.ok) throw new Error('Не удалось зарегистрировать пользователя');
      showToast('Успешная регистрация', 'Вы можете войти в систему', 'success');
      showView(loginView);
    })
    .catch(err => {
      showToast('Ошибка регистрации', err.message || 'Проверьте введенные данные', 'error');
    })
    .finally(() => {
      hideLoading();
    });
  });

  // После успешного логина
  function afterAuth() {
    showView(feedView);
    refreshFeed();
  }

  // Загрузка ленты
  function loadFeed(append = true) {
    showLoading();

    let url = '/api/pins?size=20';
    if (nextCursor) url += '&cursor=' + encodeURIComponent(nextCursor);
    if (currentFilter !== 'all' && currentFilter !== '') {
      url += '&filter=' + encodeURIComponent(currentFilter);
    }
    if (searchQuery) {
      url += '&search=' + encodeURIComponent(searchQuery);
    }

    fetch(url, { headers: authHeaders() })
      .then(res => res.json())
      .then(obj => {
        const data = obj.data;
        if (!append) {
          feedContainer.innerHTML = '';
          pins = [];
        }

        // Сохраняем пины в памяти
        pins = [...pins, ...data.content];

        // Рендерим пины с анимацией
        data.content.forEach((pin, index) => {
          setTimeout(() => {
            renderPin(pin);
          }, index * 50); // Ускорим анимацию для более плавного эффекта
        });

        nextCursor = data.hasNext ? data.nextCursor : null;
        loadMoreBtn.style.display = nextCursor ? 'flex' : 'none';

        // Если нет пинов, показываем сообщение
        if (pins.length === 0) {
          feedContainer.innerHTML = `
            <div class="empty-state" data-aos="fade-up">
              <i class="fas fa-map-marked-alt"></i>
              <p>Нет доступных мест</p>
              <button id="create-empty-btn" class="btn btn--primary btn--with-icon">
                <i class="fas fa-plus-circle"></i>
                <span>Добавить место</span>
              </button>
            </div>
          `;
          document.getElementById('create-empty-btn').addEventListener('click', () => showView(createView));
        }
      })
      .catch(err => {
        showToast('Ошибка загрузки', 'Не удалось загрузить данные', 'error');
      })
      .finally(() => {
        hideLoading();
      });
  }

  function refreshFeed() {
    nextCursor = null;
    loadFeed(false);
  }

  if (refreshFeedBtn) {
    refreshFeedBtn.addEventListener('click', () => {
      refreshFeedBtn.classList.add('rotating');
      refreshFeed();
      setTimeout(() => {
        refreshFeedBtn.classList.remove('rotating');
      }, 1000);
    });
  }

  loadMoreBtn.addEventListener('click', () => loadFeed(true));

  // Рендер одной карточки
  function renderPin(pin) {
    const card = document.createElement('div');
    card.className = 'pin-card';
    card.dataset.id = pin.id;
    card.dataset.aos = 'fade-up';

    // Добавляем случайные высоты для эффекта маconry сетки
    const randomHeight = Math.floor(Math.random() * 40) + 180;

    // Используем реальные изображения или высококачественные заполнители
    const imageUrl = pin.imageUrl || `https://source.unsplash.com/random/400x${randomHeight}?${pin.title.split(' ')[0]}`;
    const userImageUrl = pin.userProfileImageUrl || `https://ui-avatars.com/api/?name=${pin.username}&background=random&color=fff&size=150`;

    card.innerHTML = `
      <div class="pin-card-image-container">
        <img src="${imageUrl}" alt="${pin.title}" class="pin-image" style="min-height: ${randomHeight}px">
        <div class="pin-overlay">
          <div class="pin-actions">
            <button class="pin-action-btn" title="Сохранить">
              <i class="fas fa-bookmark"></i>
            </button>
            <button class="pin-action-btn" title="Нравится">
              <i class="fas fa-heart"></i>
            </button>
          </div>
        </div>
      </div>
      <div class="info">
        <div class="title">${pin.title}</div>
        <div class="user">
          <img src="${userImageUrl}" alt="${pin.username}" class="user-avatar">
          <span>${pin.username}</span>
        </div>
        ${pin.location ? `<div class="pin-location"><i class="fas fa-map-marker-alt"></i> ${pin.location}</div>` : ''}
      </div>`;

    // Добавляем эффект при наведении
    card.addEventListener('mouseenter', () => {
      card.classList.add('pin-card-hover');
    });

    card.addEventListener('mouseleave', () => {
      card.classList.remove('pin-card-hover');
    });

    feedContainer.appendChild(card);
  }

  // Поиск
  searchInput.addEventListener('input', debounce(function() {
    searchQuery = this.value.trim();
    refreshFeed();
  }, 500));

  // Фильтры
  filterTags.forEach(tag => {
    tag.addEventListener('click', () => {
      filterTags.forEach(t => t.classList.remove('active'));
      tag.classList.add('active');
      currentFilter = tag.textContent.toLowerCase();
      refreshFeed();
    });
  });

  // Открыть детали пина
  feedContainer.addEventListener('click', e => {
    const card = e.target.closest('.pin-card');
    if (!card) return;

    showLoading();
    const id = card.dataset.id;

    fetch(`/api/pins/detail/${id}`, { headers: authHeaders() })
      .then(res => res.json())
      .then(obj => {
        const pin = obj.data;
        detailTitle.textContent = pin.title;

        // Используем реальные изображения или высококачественные заполнители
        const imageUrl = pin.imageUrl || `https://source.unsplash.com/random/800x600?${pin.title.split(' ')[0]}`;
        const userImageUrl = pin.userProfileImageUrl || `https://ui-avatars.com/api/?name=${pin.username}&background=random&color=fff&size=150`;

        detailBody.innerHTML = `
          <div class="detail-image-container" data-aos="fade-right">
            <img src="${imageUrl}" alt="${pin.title}" class="detail-image">
          </div>
          <div class="detail-content" data-aos="fade-left">
            <h3 class="detail-title">${pin.title}</h3>
            <div class="detail-description">
              ${pin.description ? `<p>${pin.description}</p>` : '<p class="text-muted">Нет описания</p>'}
            </div>
            <div class="pin-metadata">
              <div class="pin-author">
                <img src="${userImageUrl}" alt="${pin.username}" class="author-avatar">
                <div class="author-info">
                  <span class="author-name">${pin.username}</span>
                  <span class="author-role">Путешественник</span>
                </div>
              </div>
              <div class="pin-stats">
                <span class="pin-stat"><i class="far fa-heart"></i> ${pin.likesCount || 0}</span>
                <span class="pin-stat"><i class="far fa-comment"></i> ${pin.commentsCount || 0}</span>
                <span class="pin-stat"><i class="far fa-eye"></i> ${Math.floor(Math.random() * 1000)}</span>
              </div>
            </div>
            ${pin.location ? `
              <div class="pin-location-detail">
                <i class="fas fa-map-marker-alt"></i>
                <span>${pin.location}</span>
                <a href="https://maps.google.com/maps?q=${encodeURIComponent(pin.location)}" target="_blank" class="btn btn--sm btn--outline">
                  <i class="fas fa-external-link-alt"></i> Открыть на карте
                </a>
              </div>` : ''}
            <div class="detail-actions">
              <button class="btn btn--primary btn--with-icon">
                <i class="fas fa-bookmark"></i>
                <span>Сохранить</span>
              </button>
              <button class="btn btn--outline btn--with-icon">
                <i class="fas fa-share-alt"></i>
                <span>Поделиться</span>
              </button>
            </div>
          </div>`;

        // Обновляем счетчики в кнопках
        const likeCount = detailView.querySelector('.like-count');
        const commentCount = detailView.querySelector('.comment-count');
        if (likeCount) likeCount.textContent = pin.likesCount || 0;
        if (commentCount) commentCount.textContent = pin.commentsCount || 0;

        showView(detailView);
      })
      .catch(err => {
        showToast('Ошибка загрузки', 'Не удалось загрузить детали', 'error');
      })
      .finally(() => {
        hideLoading();
      });
  });

  // Закрыть детали
  detailClose.addEventListener('click', () => showView(feedView));

  // Управление дропзоной загрузки изображений
  dropzone.addEventListener('click', () => fileInput.click());

  dropzone.addEventListener('dragover', e => {
    e.preventDefault();
    dropzone.classList.add('dragover');
  });

  dropzone.addEventListener('dragleave', () => {
    dropzone.classList.remove('dragover');
  });

  dropzone.addEventListener('drop', e => {
    e.preventDefault();
    dropzone.classList.remove('dragover');

    if (e.dataTransfer.files.length) {
      fileInput.files = e.dataTransfer.files;
      updateImagePreview(e.dataTransfer.files[0]);
    }
  });

  fileInput.addEventListener('change', () => {
    if (fileInput.files.length) {
      updateImagePreview(fileInput.files[0]);
    }
  });

  function updateImagePreview(file) {
    if (file && file.type.match('image.*')) {
      const reader = new FileReader();
      reader.onload = e => {
        imagePreview.src = e.target.result;
        dropzone.classList.add('hidden');
        imagePreviewContainer.classList.remove('hidden');
      };
      reader.readAsDataURL(file);
    }
  }

  removeImageBtn.addEventListener('click', () => {
    fileInput.value = '';
    imagePreviewContainer.classList.add('hidden');
    dropzone.classList.remove('hidden');
  });

  // Создать пин
  createBtn.addEventListener('click', () => showView(createView));
  cancelCreateBtn.addEventListener('click', () => showView(feedView));

  if (cancelCreateFormBtn) {
    cancelCreateFormBtn.addEventListener('click', () => showView(feedView));
  }

  createForm.addEventListener('submit', e => {
    e.preventDefault();

    const file = document.getElementById('pin-image').files[0];
    const title = document.getElementById('pin-title').value;
    const description = document.getElementById('pin-description').value;
    const location = document.getElementById('pin-location').value;

    if (!file) {
      showToast('Ошибка', 'Выберите изображение', 'error');
      return;
    }

    showLoading();

    const fd = new FormData();
    fd.append('file', file);
    fd.append('title', title);
    fd.append('description', description);
    if (location) fd.append('location', location);

    fetch('/api/pins/upload', {
      method: 'POST',
      headers: authHeaders(),
      body: fd
    })
    .then(res => {
      if (!res.ok) throw new Error('Ошибка создания');
      return res.json();
    })
    .then(() => {
      createForm.reset();
      imagePreviewContainer.classList.add('hidden');
      dropzone.classList.remove('hidden');
      showView(feedView);
      refreshFeed();
      showToast('Успешно', 'Место успешно добавлено', 'success');
    })
    .catch(err => {
      showToast('Ошибка создания', err.message || 'Не удалось создать место', 'error');
    })
    .finally(() => {
      hideLoading();
    });
  });

  // Утилиты
  function showLoading() {
    document.body.classList.add('loading');
  }

  function hideLoading() {
    document.body.classList.remove('loading');
  }

  // Создание и вывод уведомлений
  function showToast(title, message, type = 'info') {
    const toastContainer = document.getElementById('toast-container');

    const toast = document.createElement('div');
    toast.className = `toast toast--${type}`;
    toast.dataset.aos = 'fade-left';

    let iconClass = 'fas fa-info-circle';
    if (type === 'success') iconClass = 'fas fa-check-circle';
    if (type === 'error') iconClass = 'fas fa-exclamation-circle';
    if (type === 'warning') iconClass = 'fas fa-exclamation-triangle';

    toast.innerHTML = `
      <div class="toast-icon">
        <i class="${iconClass}"></i>
      </div>
      <div class="toast-content">
        <div class="toast-title">${title}</div>
        <div class="toast-message">${message}</div>
      </div>
      <button class="toast-close">
        <i class="fas fa-times"></i>
      </button>
      <div class="toast-progress"></div>
    `;

    toastContainer.appendChild(toast);

    // Добавляем анимацию прогресса
    const progress = toast.querySelector('.toast-progress');
    progress.style.animation = 'toast-progress 5s linear forwards';

    // Автоматическое закрытие через 5 секунд
    const timeout = setTimeout(() => {
      toast.classList.add('toast-hiding');
      setTimeout(() => toast.remove(), 300);
    }, 5000);

    // Закрытие по клику
    toast.querySelector('.toast-close').addEventListener('click', () => {
      clearTimeout(timeout);
      toast.classList.add('toast-hiding');
      setTimeout(() => toast.remove(), 300);
    });
  }

  // Debounce функция для поиска
  function debounce(func, wait) {
    let timeout;
    return function() {
      const context = this, args = arguments;
      clearTimeout(timeout);
      timeout = setTimeout(() => {
        func.apply(context, args);
      }, wait);
    };
  }

  // Инициализация
  document.addEventListener('click', e => e);

  // Тема из локалсторедж
  const savedTheme = localStorage.getItem('theme') || 'light';
  setTheme(savedTheme);

  // Добавляем классы для CSS анимаций при скролле
  document.querySelectorAll('.pin-card').forEach((card, index) => {
    card.style.animationDelay = `${index * 0.1}s`;
  });

  // Инициализация слушателей для скролла
  window.addEventListener('scroll', () => {
    if (window.scrollY > 100) {
      navbar.classList.add('navbar--scrolled');
    } else {
      navbar.classList.remove('navbar--scrolled');
    }
  });

  // Проверка авторизации
  if (token) {
    fetch('/api/auth/check', { headers: authHeaders() })
      .then(res => {
        if (res.ok) {
          afterAuth();
        } else {
          showView(loginView);
        }
      })
      .catch(() => {
        showView(loginView);
      });
  } else {
    showView(loginView);
  }
})(); 