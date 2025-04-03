// Global variables
let pins = [];
let currentPage = 0;
let currentSize = 20;
let isLoading = false;
let hasMorePins = true;
let searchQuery = '';
let selectedCategory = '';
let sortBy = 'createdAt';
let sortDirection = 'desc';
let currentUser = null;

// DOM elements
const pinsGrid = document.getElementById('pinsGrid');
const searchInput = document.getElementById('searchInput');
const searchButton = document.getElementById('searchButton');
const categorySelect = document.getElementById('categorySelect');
const sortSelect = document.getElementById('sortSelect');
const loadMoreBtn = document.getElementById('loadMoreBtn');
const exploreTitle = document.getElementById('exploreTitle');

// Authorization buttons
const loginBtn = document.getElementById('loginBtn');
const registerBtn = document.getElementById('registerBtn');
const profileBtn = document.getElementById('profileBtn');
const logoutBtn = document.getElementById('logoutBtn');

document.addEventListener('DOMContentLoaded', initApp);

function initApp() {
    checkAuth();
    checkUrlParams();
    setupEventListeners();
    loadCategories();
    loadPins();
    setupInfiniteScroll();
    updateTitle();
}

function setupEventListeners() {
    // Authorization button listeners
    loginBtn.addEventListener('click', () => window.location.href = '/?login=true');
    registerBtn.addEventListener('click', () => window.location.href = '/?register=true');
    profileBtn.addEventListener('click', () => window.location.href = '/profile.html');
    logoutBtn.addEventListener('click', logout);

    // Search handler
    searchButton?.addEventListener('click', handleSearch);
    searchInput?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
    });

    // Category filter
    categorySelect?.addEventListener('change', () => {
        selectedCategory = categorySelect.value;
        resetAndReload();
    });

    // Sort filter
    sortSelect?.addEventListener('change', () => {
        const [newSortBy, newSortDirection] = sortSelect.value.split(':');
        sortBy = newSortBy;
        sortDirection = newSortDirection;
        resetAndReload();
    });

    // Load more pins
    loadMoreBtn?.addEventListener('click', loadPins);
}

async function checkAuth() {
    try {
        const response = await fetch('/api/auth/check', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '',
            }
        });

        if (response.ok) {
            const data = await response.json();
            currentUser = data;
            updateUIForUser(data);
        } else {
            updateUIForGuest();
        }
    } catch (error) {
        console.error('Ошибка проверки авторизации:', error);
        updateUIForGuest();
    }
}

function updateUIForGuest() {
    updateAuthUI(false);
}

function updateUIForUser(user) {
    updateAuthUI(true);
    if (profileBtn) {
        profileBtn.innerHTML = `<i class="fas fa-user"></i> ${user.username}`;
    }
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

function loadCategories() {
    const categories = getMockCategories();
    populateCategorySelect(categories);
}

function populateCategorySelect(categories) {
    if (!categorySelect) return;

    categorySelect.innerHTML = '<option value="">Все категории</option>';

    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.id;
        option.textContent = category.name;
        categorySelect.appendChild(option);
    });
}

function checkUrlParams() {
    const params = new URLSearchParams(window.location.search);

    // Check for search query
    if (params.has('query')) {
        searchQuery = params.get('query');
        if (searchInput) {
            searchInput.value = searchQuery;
        }
    }

    // Check for category
    if (params.has('category')) {
        selectedCategory = params.get('category');
        if (categorySelect) {
            categorySelect.value = selectedCategory;
        }
    }

    // Check for sort
    if (params.has('sortBy')) sortBy = params.get('sortBy');
    if (params.has('sortDirection')) sortDirection = params.get('sortDirection');

    // Set sort value
    if (sortSelect && sortBy && sortDirection) {
        sortSelect.value = `${sortBy}:${sortDirection}`;
    }
}

function updateTitle() {
    if (!exploreTitle) return;

    if (searchQuery) {
        exploreTitle.textContent = `Результаты поиска: "${searchQuery}"`;
    } else if (selectedCategory) {
        const categories = getMockCategories();
        const category = categories.find(c => c.id.toString() === selectedCategory.toString());

        if (category) {
            exploreTitle.textContent = category.name;
        } else {
            exploreTitle.textContent = 'Обзор пинов';
        }
    } else {
        exploreTitle.textContent = 'Обзор пинов';
    }
}

function setupInfiniteScroll() {
    window.addEventListener('scroll', function() {
        if (isLoading || !hasMorePins) return;

        const scrollHeight = document.documentElement.scrollHeight;
        const scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
        const clientHeight = document.documentElement.clientHeight;

        if (scrollTop + clientHeight >= scrollHeight - 300) {
            loadPins();
        }
    });
}

async function loadPins() {
    if (isLoading || !hasMorePins) return;

    isLoading = true;
    showLoading();

    try {
        const searchEndpoint = searchQuery
            ? `/api/search/pins?query=${encodeURIComponent(searchQuery)}&page=${currentPage}&size=${currentSize}&sortBy=${sortBy}&sortDirection=${sortDirection}`
            : `/api/search/pins?page=${currentPage}&size=${currentSize}&sortBy=${sortBy}&sortDirection=${sortDirection}`;

        const response = await fetch(searchEndpoint, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '',
            }
        });

        if (!response.ok) {
            throw new Error('Не удалось загрузить пины');
        }

        const data = await response.json();
        const pinsData = data.data || data;

        if (pinsData.content && Array.isArray(pinsData.content)) {
            if (currentPage === 0) {
                pinsGrid.innerHTML = '';
            }

            renderPins(pinsData.content);
            hasMorePins = !pinsData.last;

            if (loadMoreBtn) {
                loadMoreBtn.style.display = hasMorePins ? 'block' : 'none';
            }

            if (pinsData.content.length === 0 && currentPage === 0) {
                pinsGrid.innerHTML = `
                    <div class="no-results">
                        <i class="fas fa-search"></i>
                        <h3>Нет результатов</h3>
                        <p>По запросу "${searchQuery}" ничего не найдено</p>
                    </div>
                `;
                if (loadMoreBtn) {
                    loadMoreBtn.style.display = 'none';
                }
            }

            currentPage++;
        } else {
            console.error('Неверный формат данных:', pinsData);
            showMessage('Неверный формат данных от сервера', 'error');
        }
    } catch (error) {
        console.error('Ошибка загрузки пинов:', error);
        showMessage('Не удалось загрузить пины', 'error');
    } finally {
        isLoading = false;
        hideLoading();
    }
}

function renderPins(pins) {
    if (!pinsGrid) return;

    pins.forEach((pin, index) => {
        const pinElement = document.createElement('div');
        pinElement.className = 'image-card';
        pinElement.style.animationDelay = `${(index % 10) * 0.1}s`;

        const imageUrl = pin.imageUrl || 'img/placeholder.svg';
        const description = pin.description || 'Без описания';
        const username = pin.username || 'Неизвестный автор';
        const userProfileImage = pin.userProfileImageUrl || '';

        // Create HTML structure for pin card
        pinElement.innerHTML = `
            <a href="/pin.html?id=${pin.id}">
                <div class="image-container">
                    <img src="${imageUrl}" alt="${description}" onerror="this.src='img/placeholder.svg';">
                    <div class="image-overlay">
                        <div class="image-actions">
                            <button class="like-btn" data-pin-id="${pin.id}" title="Нравится">
                                <i class="${pin.isLikedByCurrentUser ? 'fas' : 'far'} fa-heart"></i>
                            </button>
                            <button class="save-btn" data-pin-id="${pin.id}" title="Сохранить">
                                <i class="far fa-bookmark"></i>
                            </button>
                            <button class="share-btn" data-pin-id="${pin.id}" title="Поделиться">
                                <i class="fas fa-share-alt"></i>
                            </button>
                        </div>
                    </div>
                </div>
                <div class="image-info">
                    <h3 title="${description}">${description.length > 30 ? description.substring(0, 30) + '...' : description}</h3>
                    <p>
                        ${userProfileImage ? `<img src="${userProfileImage}" alt="${username}" class="user-avatar">` : ''}
                        <span>${username}</span>
                    </p>
                </div>
            </a>
        `;

        // Add event listeners
        const likeBtn = pinElement.querySelector('.like-btn');
        const saveBtn = pinElement.querySelector('.save-btn');
        const shareBtn = pinElement.querySelector('.share-btn');

        likeBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            likePin(pin.id, likeBtn);
        });

        saveBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            savePin(pin.id);
        });

        shareBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            const shareUrl = `${window.location.origin}/pin.html?id=${pin.id}`;
            navigator.clipboard.writeText(shareUrl)
                .then(() => showMessage('Ссылка скопирована в буфер обмена', 'success'))
                .catch(() => showMessage('Не удалось скопировать ссылку', 'error'));
        });

        pinsGrid.appendChild(pinElement);
    });
}

function handleSearch() {
    if (!searchInput) return;

    searchQuery = searchInput.value.trim();

    // Update URL with search parameters
    const url = new URL(window.location);

    if (searchQuery) {
        url.searchParams.set('query', searchQuery);
    } else {
        url.searchParams.delete('query');
    }

    window.history.pushState({}, '', url);

    // Reset and reload pins
    resetAndReload();
}

function resetAndReload() {
    currentPage = 0;
    hasMorePins = true;
    pinsGrid.innerHTML = '';
    updateTitle();
    loadPins();
}

function savePin(pinId) {
    if (!currentUser) {
        showMessage('Для сохранения пина необходимо авторизоваться', 'info');
        return;
    }

    showMessage('Функция сохранения будет доступна в следующем обновлении', 'info');
}

async function likePin(pinId, button) {
    if (!currentUser) {
        showMessage('Для оценки пина необходимо авторизоваться', 'info');
        return;
    }

    try {
        const isLiked = button.querySelector('i').classList.contains('fas');

        // Optimistic UI update
        button.querySelector('i').className = isLiked ? 'far fa-heart' : 'fas fa-heart';

        const endpoint = `/api/pins/${pinId}/likes`;
        const method = isLiked ? 'DELETE' : 'POST';

        const response = await fetch(endpoint, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '',
            }
        });

        if (!response.ok) {
            // Rollback optimistic update in case of error
            button.querySelector('i').className = isLiked ? 'fas fa-heart' : 'far fa-heart';
            throw new Error('Не удалось выполнить действие');
        }

        showMessage(isLiked ? 'Пин удалён из понравившихся' : 'Пин добавлен в понравившиеся', 'success');
    } catch (error) {
        console.error('Ошибка при лайке пина:', error);
        showMessage('Не удалось выполнить действие', 'error');
    }
}

async function logout() {
    localStorage.removeItem('token');
    currentUser = null;
    updateUIForGuest();
    showMessage('Вы вышли из аккаунта', 'success');
}

function showLoading() {
    document.body.classList.add('loading');
}

function hideLoading() {
    document.body.classList.remove('loading');
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

function getMockCategories() {
    return [
        { id: 1, name: 'Искусство' },
        { id: 2, name: 'Фотография' },
        { id: 3, name: 'Дизайн' },
        { id: 4, name: 'Мода' },
        { id: 5, name: 'Путешествия' },
        { id: 6, name: 'Еда' },
        { id: 7, name: 'Архитектура' },
        { id: 8, name: 'Природа' },
        { id: 9, name: 'Технологии' },
        { id: 10, name: 'Спорт' }
    ];
}