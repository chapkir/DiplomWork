// Global variables
let pins = [];
let currentPage = 0;
let isLoading = false;
let hasMorePins = true;
let searchQuery = '';
let selectedCategory = '';
let selectedSort = 'newest';
let currentUser = null;
const API_BASE_URL = window.location.origin;

document.addEventListener('DOMContentLoaded', function() {
    initApp();
});

function initApp() {
    // Check URL parameters
    checkUrlParams();

    // Update page title based on search/category
    updateTitle();

    // Setup event listeners
    setupEventListeners();

    // Check authentication status
    checkAuth();

    // Load categories for filter
    loadCategories();

    // Load initial pins
    loadPins();

    // Setup infinite scroll
    setupInfiniteScroll();
}

function setupEventListeners() {
    // Search form
    const searchForm = document.getElementById('searchForm');
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleSearch();
        });
    }

    // Category filter
    const categorySelect = document.getElementById('categoryFilter');
    if (categorySelect) {
        categorySelect.addEventListener('change', function() {
            selectedCategory = this.value;
            resetAndReload();
        });
    }

    // Sort filter
    const sortSelect = document.getElementById('sortFilter');
    if (sortSelect) {
        sortSelect.addEventListener('change', function() {
            selectedSort = this.value;
            resetAndReload();
        });
    }

    // Login/Register buttons
    const loginBtn = document.getElementById('loginBtn');
    if (loginBtn) {
        loginBtn.addEventListener('click', function() {
            window.location.href = '/?login=true';
        });
    }

    const registerBtn = document.getElementById('registerBtn');
    if (registerBtn) {
        registerBtn.addEventListener('click', function() {
            window.location.href = '/?register=true';
        });
    }

    // Logout button
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }
}

async function checkAuth() {
    try {
        const token = localStorage.getItem('token');

        if (!token) {
            updateUIForGuest();
            return;
        }

        const response = await fetch(`${API_BASE_URL}/api/auth/check`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const data = await response.json();
            currentUser = data;
            updateUIForUser(data);
        } else {
            // Token is invalid or expired
            localStorage.removeItem('token');
            updateUIForGuest();
        }
    } catch (error) {
        console.error('Error checking authentication:', error);
        updateUIForGuest();
    }
}

function updateAuthUI(isAuthenticated) {
    const loginBtn = document.getElementById('loginBtn');
    const registerBtn = document.getElementById('registerBtn');
    const profileBtn = document.getElementById('profileBtn');
    const logoutBtn = document.getElementById('logoutBtn');

    if (isAuthenticated) {
        if (loginBtn) loginBtn.style.display = 'none';
        if (registerBtn) registerBtn.style.display = 'none';
        if (profileBtn) profileBtn.style.display = 'inline-block';
        if (logoutBtn) logoutBtn.style.display = 'inline-block';
    } else {
        if (loginBtn) loginBtn.style.display = 'inline-block';
        if (registerBtn) registerBtn.style.display = 'inline-block';
        if (profileBtn) profileBtn.style.display = 'none';
        if (logoutBtn) logoutBtn.style.display = 'none';
    }
}

function updateUIForGuest() {
    updateAuthUI(false);
}

function updateUIForUser(user) {
    updateAuthUI(true);
    const profileBtn = document.getElementById('profileBtn');
    if (profileBtn) {
        profileBtn.href = `/profile.html?username=${user.username}`;
    }
}

function loadCategories() {
    // In a real application, you would fetch categories from the server
    // For now, we'll use mock data
    const categories = getMockCategories();
    populateCategorySelect(categories);
}

function populateCategorySelect(categories) {
    const categorySelect = document.getElementById('categoryFilter');
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
    const urlParams = new URLSearchParams(window.location.search);

    // Check for search query
    if (urlParams.has('search')) {
        searchQuery = urlParams.get('search');
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.value = searchQuery;
        }
    }

    // Check for category
    if (urlParams.has('category')) {
        selectedCategory = urlParams.get('category');
        const categorySelect = document.getElementById('categoryFilter');
        if (categorySelect) {
            categorySelect.value = selectedCategory;
        }
    }

    // Check for sort
    if (urlParams.has('sort')) {
        selectedSort = urlParams.get('sort');
        const sortSelect = document.getElementById('sortFilter');
        if (sortSelect) {
            sortSelect.value = selectedSort;
        }
    }
}

function updateTitle() {
    const titleElement = document.querySelector('.explore-header h1');
    if (!titleElement) return;

    if (searchQuery) {
        titleElement.textContent = `Результаты поиска: "${searchQuery}"`;
    } else if (selectedCategory) {
        const categoryName = document.querySelector(`#categoryFilter option[value="${selectedCategory}"]`)?.textContent || 'Категория';
        titleElement.textContent = `Обзор: ${categoryName}`;
    } else {
        titleElement.textContent = 'Обзор всех изображений';
    }
}

function setupInfiniteScroll() {
    window.addEventListener('scroll', function() {
        if (isLoading || !hasMorePins) return;

        const scrollHeight = document.documentElement.scrollHeight;
        const scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
        const clientHeight = document.documentElement.clientHeight;

        if (scrollTop + clientHeight >= scrollHeight - 200) {
            loadMorePins();
        }
    });
}

async function loadPins() {
    if (isLoading) return;

    isLoading = true;
    showLoading();

    try {
        let url = `${API_BASE_URL}/api/pins?page=${currentPage}&size=20`;

        if (searchQuery) {
            url += `&search=${encodeURIComponent(searchQuery)}`;
        }

        if (selectedCategory) {
            url += `&category=${encodeURIComponent(selectedCategory)}`;
        }

        if (selectedSort) {
            url += `&sort=${encodeURIComponent(selectedSort)}`;
        }

        const token = localStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json'
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(url, {
            method: 'GET',
            headers: headers
        });

        if (!response.ok) {
            throw new Error('Failed to fetch pins');
        }

        const data = await response.json();

        if (data.content && Array.isArray(data.content)) {
            if (currentPage === 0) {
                pins = data.content;
            } else {
                pins = [...pins, ...data.content];
            }

            hasMorePins = !data.last;
            currentPage++;

            renderPins();
        } else {
            console.error('Invalid response format:', data);
            showMessage('Произошла ошибка при загрузке данных', 'error');
        }
    } catch (error) {
        console.error('Error loading pins:', error);
        showMessage('Не удалось загрузить изображения. Пожалуйста, попробуйте позже.', 'error');
    } finally {
        isLoading = false;
        hideLoading();
    }
}

function loadMorePins() {
    loadPins();
}

function renderPins() {
    const imageGrid = document.querySelector('.image-grid');
    if (!imageGrid) return;

    if (currentPage === 1) {
        imageGrid.innerHTML = '';
    }

    if (pins.length === 0) {
        imageGrid.innerHTML = `
            <div class="no-results">
                <i class="fas fa-search"></i>
                <h3>Ничего не найдено</h3>
                <p>Попробуйте изменить параметры поиска или категорию</p>
            </div>
        `;
        return;
    }

    pins.forEach(pin => {
        const pinElement = document.createElement('div');
        pinElement.className = 'image-card';
        pinElement.setAttribute('data-id', pin.id);

        pinElement.innerHTML = `
            <a href="/pin.html?id=${pin.id}">
                <div class="image-container">
                    <img src="${pin.imageUrl}" alt="${pin.description || 'Изображение'}">
                </div>
                <div class="image-info">
                    <h3>${pin.description || 'Без описания'}</h3>
                    <p><i class="fas fa-heart"></i> ${pin.likesCount || 0}</p>
                </div>
                <div class="image-overlay">
                    <div class="image-actions">
                        <button class="like-btn" data-id="${pin.id}" title="Нравится">
                            <i class="fas ${pin.isLikedByCurrentUser ? 'fa-heart' : 'fa-heart-o'}"></i>
                        </button>
                        <button class="save-btn" data-id="${pin.id}" title="Сохранить">
                            <i class="fas fa-bookmark"></i>
                        </button>
                    </div>
                </div>
            </a>
        `;

        imageGrid.appendChild(pinElement);

        // Add event listeners for like and save buttons
        const likeBtn = pinElement.querySelector('.like-btn');
        if (likeBtn) {
            likeBtn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                likePin(pin.id);
            });
        }

        const saveBtn = pinElement.querySelector('.save-btn');
        if (saveBtn) {
            saveBtn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                savePin(pin.id);
            });
        }
    });
}

function handleSearch() {
    const searchInput = document.getElementById('searchInput');
    if (!searchInput) return;

    searchQuery = searchInput.value.trim();

    // Update URL with search parameter
    const url = new URL(window.location.href);
    if (searchQuery) {
        url.searchParams.set('search', searchQuery);
    } else {
        url.searchParams.delete('search');
    }
    window.history.pushState({}, '', url);

    resetAndReload();
}

function resetAndReload() {
    currentPage = 0;
    hasMorePins = true;
    pins = [];
    updateTitle();
    loadPins();
}

function savePin(pinId) {
    const token = localStorage.getItem('token');
    if (!token) {
        showMessage('Пожалуйста, войдите в систему, чтобы сохранять изображения', 'warning');
        return;
    }

    // In a real application, you would send a request to save the pin
    // For now, we'll just show a success message
    showMessage('Изображение сохранено в вашей коллекции', 'success');
}

async function likePin(pinId) {
    const token = localStorage.getItem('token');
    if (!token) {
        showMessage('Пожалуйста, войдите в систему, чтобы ставить лайки', 'warning');
        return;
    }

    try {
        const pin = pins.find(p => p.id === pinId);
        if (!pin) return;

        const method = pin.isLikedByCurrentUser ? 'DELETE' : 'POST';
        const url = `${API_BASE_URL}/api/pins/${pinId}/likes`;

        const response = await fetch(url, {
            method: method,
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            // Update pin in local array
            pin.isLikedByCurrentUser = !pin.isLikedByCurrentUser;
            pin.likesCount = pin.isLikedByCurrentUser ? (pin.likesCount + 1) : (pin.likesCount - 1);

            // Update UI
            const likeBtn = document.querySelector(`.like-btn[data-id="${pinId}"] i`);
            if (likeBtn) {
                likeBtn.className = `fas ${pin.isLikedByCurrentUser ? 'fa-heart' : 'fa-heart-o'}`;
            }

            const likesCount = document.querySelector(`.image-card[data-id="${pinId}"] .image-info p`);
            if (likesCount) {
                likesCount.innerHTML = `<i class="fas fa-heart"></i> ${pin.likesCount}`;
            }
        } else {
            throw new Error('Failed to update like status');
        }
    } catch (error) {
        console.error('Error liking pin:', error);
        showMessage('Не удалось обновить статус лайка. Пожалуйста, попробуйте позже.', 'error');
    }
}

async function logout() {
    try {
        localStorage.removeItem('token');
        updateUIForGuest();
        showMessage('Вы успешно вышли из системы', 'success');
    } catch (error) {
        console.error('Error during logout:', error);
        showMessage('Произошла ошибка при выходе из системы', 'error');
    }
}

function showLoading() {
    const loadingIndicator = document.querySelector('.loading-indicator');
    if (loadingIndicator) loadingIndicator.style.display = 'flex';
}

function hideLoading() {
    const loadingIndicator = document.querySelector('.loading-indicator');
    if (loadingIndicator) loadingIndicator.style.display = 'none';
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

// Mock data for categories (in a real app, this would come from the server)
function getMockCategories() {
    return [
        { id: 'nature', name: 'Природа' },
        { id: 'architecture', name: 'Архитектура' },
        { id: 'travel', name: 'Путешествия' },
        { id: 'food', name: 'Еда' },
        { id: 'art', name: 'Искусство' },
        { id: 'technology', name: 'Технологии' },
        { id: 'animals', name: 'Животные' },
        { id: 'fashion', name: 'Мода' }
    ];
}