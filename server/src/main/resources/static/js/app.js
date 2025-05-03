// Минимальный SPA-скрипт
// Авторизация и переключение между view

let token = localStorage.getItem('token');

// Пагинация
let pinNextCursor = null;
const pinPageSize = 6;
const pinSkeletonCount = 4;
let postPageNo = 0;
const postPageSize = 6;

function authHeaders() {
  return token ? { 'Authorization': 'Bearer ' + token } : {};
}

function showView(viewId) {
  document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
  document.getElementById(viewId).classList.add('active');
}

// Добавляю функции управления глобальным лоадером
function showLoader() {
  const loader = document.getElementById('global-loader');
  if (loader) loader.style.display = 'flex';
}

function hideLoader() {
  const loader = document.getElementById('global-loader');
  if (loader) loader.style.display = 'none';
}

// Прогресс-бары для загрузки
function showFeedProgress() {
  const bar = document.getElementById('feed-progress-bar');
  if (bar) bar.classList.add('active');
}

function hideFeedProgress() {
  const bar = document.getElementById('feed-progress-bar');
  if (bar) bar.classList.remove('active');
}

function showPostsProgress() {
  const bar = document.getElementById('posts-progress-bar');
  if (bar) bar.classList.add('active');
}

function hidePostsProgress() {
  const bar = document.getElementById('posts-progress-bar');
  if (bar) bar.classList.remove('active');
}

// Обработчики форм логина/регистрации и выхода
async function handleLogin(e) {
  e.preventDefault();
  const username = document.getElementById('login-username').value;
  const password = document.getElementById('login-password').value;
  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    if (!res.ok) throw new Error(res.status);
    const data = await res.json();
    token = data.token;
    localStorage.setItem('token', token);
    showView('feed-view');
    loadFeed();
  } catch (err) {
    alert('Ошибка авторизации');
    console.error(err);
  }
}

async function handleSignup(e) {
  e.preventDefault();
  const username = document.getElementById('signup-username').value;
  const email = document.getElementById('signup-email').value;
  const password = document.getElementById('signup-password').value;
  try {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password })
    });
    if (!res.ok) throw new Error(res.status);
    alert('Регистрация успешна. Войдите.');
    showView('login-view');
  } catch (err) {
    alert('Ошибка регистрации');
    console.error(err);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  // Login/Signup link toggles
  document.getElementById('show-signup').addEventListener('click', e => { e.preventDefault(); showView('signup-view'); });
  document.getElementById('show-login').addEventListener('click', e => { e.preventDefault(); showView('login-view'); });
  document.getElementById('login-form').addEventListener('submit', handleLogin);
  document.getElementById('signup-form').addEventListener('submit', handleSignup);
  document.getElementById('logout-btn').addEventListener('click', () => {
    token = null;
    localStorage.removeItem('token');
    showView('login-view');
  });

  // Навигация
  const navLinks = document.querySelectorAll('.nav-link[data-view]');
  navLinks.forEach(link => {
    link.addEventListener('click', e => {
      e.preventDefault();
      if (!token) return; // без авторизации
      navLinks.forEach(l => l.classList.remove('active'));
      link.classList.add('active');
      const view = link.dataset.view;
      showView(view + '-view');
      if (view === 'feed') {
        pinNextCursor = null; // сброс пагинации
        loadFeed(false);
      }
      if (view === 'posts') {
        postPageNo = 0;
        loadPosts(false);
      }
      if (view === 'profile') loadProfile();
    });
  });

  // Обработчик клика по логотипу
  const logoLink = document.getElementById('logo-link');
  if (logoLink) {
    logoLink.addEventListener('click', e => {
      e.preventDefault();
      if (!token) return; // без авторизации
      navLinks.forEach(l => l.classList.remove('active'));
      const feedLink = document.querySelector('.nav-link[data-view="feed"]');
      if (feedLink) feedLink.classList.add('active');
      pinNextCursor = null;
      showView('feed-view');
      loadFeed(false);
    });
  }

  // Кнопки Load More
  document.getElementById('load-more-pins').addEventListener('click', () => loadFeed(true));
  document.getElementById('load-more-posts').addEventListener('click', () => loadPosts(true));

  // Начальная вью
  if (token) {
    showView('feed-view');
    loadFeed(false);
  } else {
    showView('login-view');
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
  showFeedProgress();
  const container = document.getElementById('feed');
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
    const res = await fetch(url, { headers: authHeaders() });
    if (!res.ok) {
      console.error('Ошибка HTTP при загрузке пинов:', res.status);
      if (!append) container.innerHTML = `<p class='error'>Не удалось загрузить пины: ${res.status}</p>`;
      hideFeedProgress();
      return;
    }
    const json = await res.json();
    // Парсинг HATEOAS-ответа
    const page = json.data;
    const pins = page.content;
    pinNextCursor = page.nextCursor;
    const hasNext = page.hasNext;
    // Удалить все skeleton
    const skeletons = container.querySelectorAll('.card.skeleton');
    skeletons.forEach(el => el.remove());
    // Если не append и есть старый контент - очистить контейнер
    if (!append) container.innerHTML = '';
    pins.forEach(pin => {
      const card = createCard(pin.imageUrl, pin.title, pin.username);
      card.classList.add('fade-in');
      container.appendChild(card);
    });
    // Показываем кнопку Load More, если есть следующий курсор
    btn.style.display = hasNext ? 'block' : 'none';
    hideFeedProgress();
  } catch (e) {
    console.error('Ошибка в обработке пинов', e);
    container.innerHTML = `<p class='error'>Ошибка при загрузке пинов</p>`;
    hideFeedProgress();
  }
}

// Загрузка постов с постраничной пагинацией
async function loadPosts(append = false) {
  showPostsProgress();
  const container = document.getElementById('posts-feed');
  if (!append) container.innerHTML = '';
  for (let i = 0; i < postPageSize; i++) container.appendChild(createSkeletonCard());
  try {
    const res = await fetch(`/api/posts/paged?page=${postPageNo}&size=${postPageSize}`, { headers: authHeaders() });
    if (!res.ok) {
      console.error('Ошибка HTTP при загрузке постов:', res.status);
      if (!append) container.innerHTML = `<p class='error'>Не удалось загрузить посты: ${res.status}</p>`;
      hidePostsProgress();
      return;
    }
    const json = await res.json();
    console.log('posts paged response', json);
    const posts = json.content || [];
    postPageNo++;
    if (!append) container.innerHTML = '';
    else container.querySelectorAll('.card.skeleton').forEach(el => el.remove());
    posts.forEach(post => {
      const card = createCard(post.imageUrl, post.text, '');
      card.classList.add('fade-in');
      container.appendChild(card);
    });
    document.getElementById('load-more-posts').style.display = !json.last ? 'block' : 'none';
    hidePostsProgress();
  } catch (e) {
    console.error('Ошибка в обработке постов', e);
    container.innerHTML = `<p class='error'>Ошибка при загрузке постов</p>`;
    hidePostsProgress();
  }
}

// Загрузка профиля
async function loadProfile() {
  const content = document.getElementById('profile-content');
  content.innerHTML = '';
  try {
    const resUser = await fetch('/api/auth/me', { headers: authHeaders() });
    if (!resUser.ok) throw new Error('auth error');
    const user = await resUser.json();
    const header = document.createElement('div');
    header.className = 'profile-header';
    header.innerHTML = `
      <img src="${user.profileImageUrl}" alt="Avatar">
      <h1>${user.username}</h1>
      <p>${user.email}</p>
    `;
    content.appendChild(header);
    const resPosts = await fetch(`/api/posts/user/${user.id}`, { headers: authHeaders() });
    if (!resPosts.ok) throw new Error('posts error');
    const posts = await resPosts.json();
    const grid = document.createElement('div');
    grid.className = 'masonry-grid';
    posts.forEach(post => {
      const card = createCard(post.imageUrl, post.text, '');
      card.classList.add('fade-in');
      grid.appendChild(card);
    });
    content.appendChild(grid);
  } catch (e) {
    console.error('Ошибка загрузки профиля', e);
    showView('login-view');
  }
}

// Создание карточки
function createCard(img, title, subtitle) {
  const div = document.createElement('div');
  div.className = 'card';
  div.innerHTML = `
    ${img ? `<img src="${img}" alt="" class="lazy-img" loading="lazy">` : ''}
    <div class="card-content">
      <div class="card-title">${title || ''}</div>
      ${subtitle ? `<div class="card-subtitle">${subtitle}</div>` : ''}
    </div>
  `;
  // Плавное снятие размытия по загрузке
  if (img) {
    const image = div.querySelector('img');
    image.addEventListener('load', () => image.classList.add('loaded'));
  }
  return div;
}