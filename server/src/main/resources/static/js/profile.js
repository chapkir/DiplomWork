// Global variables
let currentUser = null;
let profileUser = null;
let userPins = [];
let isOwnProfile = false;
const API_BASE_URL = window.location.origin;

// DOM elements
const profileDetails = document.querySelector('.profile-details');
const pinsGrid = document.querySelector('.pins-list .grid');
const addPinForm = document.getElementById('addPinForm');
const logoutBtn = document.getElementById('logoutBtn');
const loadingIndicator = document.createElement('div');

// Setup loading indicator
loadingIndicator.className = 'loading-indicator';
loadingIndicator.innerHTML = `
    <div class="spinner">
        <div class="bounce1"></div>
        <div class="bounce2"></div>
        <div class="bounce3"></div>
    </div>
    <p>Загрузка...</p>
`;

document.addEventListener('DOMContentLoaded', function() {
    initApp();
});

function initApp() {
    // Get username from URL
    const urlParams = new URLSearchParams(window.location.search);
    const username = urlParams.get('username');

    if (!username) {
        // If no username provided, check if user is logged in
        checkAuthStatus().then(isLoggedIn => {
            if (isLoggedIn) {
                // If logged in, show own profile
                loadUserProfile(currentUser.username);
            } else {
                // If not logged in, redirect to login page
                window.location.href = '/?login=true';
            }
        });
    } else {
        // Load profile for specified username
        loadUserProfile(username);
    }

    // Setup event listeners
    setupEventListeners();

    // Animate elements
    animateElements();

    // Setup scroll animations
    window.addEventListener('scroll', animateOnScroll);

    // Add dynamic styles
    addDynamicStyles();
}

function setupEventListeners() {
    // Add pin button
    const addPinBtn = document.getElementById('addPinBtn');
    if (addPinBtn) {
        addPinBtn.addEventListener('click', handleAddPin);
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
        logoutBtn.addEventListener('click', handleLogout);
    }
}

function animateElements() {
    const elementsToAnimate = [
        { element: profileDetails, delay: 100 },
        { element: document.querySelector('.add-pin'), delay: 300 },
        { element: document.querySelector('.pins-list'), delay: 500 }
    ];

    elementsToAnimate.forEach(({ element, delay }) => {
        if (element) {
            element.style.opacity = '0';
            element.style.transform = 'translateY(20px)';

            setTimeout(() => {
                element.style.transition = 'opacity 0.8s ease, transform 0.8s ease';
                element.style.opacity = '1';
                element.style.transform = 'translateY(0)';
            }, delay);
        }
    });
}

function animateOnScroll() {
    const animatedElements = document.querySelectorAll('.animate-on-scroll:not(.animated)');

    animatedElements.forEach(element => {
        const elementPosition = element.getBoundingClientRect().top;
        const windowHeight = window.innerHeight;

        if (elementPosition < windowHeight - 100) {
            element.classList.add('animated');
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }
    });
}

async function checkAuthStatus() {
    try {
        const token = localStorage.getItem('token');

        if (!token) {
            updateUIForGuest();
            return false;
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
            return true;
        } else {
            // Token is invalid or expired
            localStorage.removeItem('token');
            updateUIForGuest();
            return false;
        }
    } catch (error) {
        console.error('Error checking authentication:', error);
        updateUIForGuest();
        return false;
    }
}

function updateUIForGuest() {
    const loginBtn = document.getElementById('loginBtn');
    const registerBtn = document.getElementById('registerBtn');
    const profileBtn = document.getElementById('profileBtn');
    const logoutBtn = document.getElementById('logoutBtn');

    if (loginBtn) loginBtn.style.display = 'inline-block';
    if (registerBtn) registerBtn.style.display = 'inline-block';
    if (profileBtn) profileBtn.style.display = 'none';
    if (logoutBtn) logoutBtn.style.display = 'none';

    // Hide add pin section for guests
    const addPinSection = document.querySelector('.add-pin');
    if (addPinSection) {
        addPinSection.style.display = 'none';
    }

    isOwnProfile = false;
}

function updateUIForUser(user) {
    const loginBtn = document.getElementById('loginBtn');
    const registerBtn = document.getElementById('registerBtn');
    const profileBtn = document.getElementById('profileBtn');
    const logoutBtn = document.getElementById('logoutBtn');

    if (loginBtn) loginBtn.style.display = 'none';
    if (registerBtn) registerBtn.style.display = 'none';
    if (profileBtn) {
        profileBtn.style.display = 'inline-block';
        profileBtn.href = `/profile.html?username=${user.username}`;
    }
    if (logoutBtn) logoutBtn.style.display = 'inline-block';

    // Show add pin section only if viewing own profile
    const addPinSection = document.querySelector('.add-pin');
    if (addPinSection) {
        if (profileUser && user.username === profileUser.username) {
            addPinSection.style.display = 'block';
            isOwnProfile = true;
        } else {
            addPinSection.style.display = 'none';
            isOwnProfile = false;
        }
    }
}

async function loadUserProfile(username) {
    showLoading(profileDetails);

    try {
        // First, get user profile data
        const userResponse = await fetch(`${API_BASE_URL}/api/users/${username}`);

        if (!userResponse.ok) {
            if (userResponse.status === 404) {
                showErrorMessage('Пользователь не найден');
                return;
            }
            throw new Error('Failed to load user profile');
        }

        profileUser = await userResponse.json();

        // Update profile info
        updateProfileInfo(profileUser);

        // Check if this is the current user's profile
        if (currentUser && currentUser.username === profileUser.username) {
            isOwnProfile = true;

            // Show add pin section
            const addPinSection = document.querySelector('.add-pin');
            if (addPinSection) {
                addPinSection.style.display = 'block';
            }
        } else {
            isOwnProfile = false;

            // Hide add pin section
            const addPinSection = document.querySelector('.add-pin');
            if (addPinSection) {
                addPinSection.style.display = 'none';
            }
        }

        // Load user pins
        await loadUserPins(profileUser.id);
    } catch (error) {
        console.error('Error loading user profile:', error);
        showErrorMessage('Не удалось загрузить профиль пользователя. Пожалуйста, попробуйте позже.');
    } finally {
        hideLoading(profileDetails);
    }
}

function updateProfileInfo(user) {
    // Update page title
    document.title = `${user.username} | Kiruha Chlen`;

    // Update profile header
    const profileHeader = document.querySelector('.profile-details h2');
    if (profileHeader) {
        profileHeader.textContent = user.username;
    }

    // Update profile info
    const profileInfo = document.querySelector('.profile-details p');
    if (profileInfo) {
        profileInfo.textContent = user.email || '';

        // Add additional profile info if available
        if (user.bio) {
            const bioElement = document.createElement('p');
            bioElement.textContent = user.bio;
            bioElement.className = 'user-bio';
            profileInfo.parentNode.insertBefore(bioElement, profileInfo.nextSibling);
        }

        if (user.registrationDate) {
            const dateElement = document.createElement('p');
            dateElement.className = 'registration-date';
            dateElement.textContent = `Дата регистрации: ${new Date(user.registrationDate).toLocaleDateString('ru-RU')}`;
            profileInfo.parentNode.insertBefore(dateElement, profileInfo.nextSibling);
        }
    }

    // Update profile image if available
    if (user.profileImageUrl) {
        const profileImage = document.createElement('img');
        profileImage.src = user.profileImageUrl;
        profileImage.alt = user.username;
        profileImage.className = 'profile-image';

        const profileDetails = document.querySelector('.profile-details');
        if (profileDetails) {
            profileDetails.insertBefore(profileImage, profileDetails.firstChild);
        }
    } else {
        // Create default profile image with user's initials
        const profileImage = document.createElement('div');
        profileImage.className = 'profile-image profile-initials';
        profileImage.textContent = user.username.substring(0, 2).toUpperCase();
        profileImage.style.backgroundColor = getRandomColor(user.username);

        const profileDetails = document.querySelector('.profile-details');
        if (profileDetails) {
            profileDetails.insertBefore(profileImage, profileDetails.firstChild);
        }
    }
}

// Function to generate a consistent color based on username
function getRandomColor(username) {
    let hash = 0;
    for (let i = 0; i < username.length; i++) {
        hash = username.charCodeAt(i) + ((hash << 5) - hash);
    }

    const colors = [
        '#E16C5B', '#F27D6C', '#E74C3C', '#F39C12',
        '#2ECC71', '#3498DB', '#9B59B6', '#1ABC9C'
    ];

    return colors[Math.abs(hash) % colors.length];
}

async function loadUserPins(userId) {
    showLoading(pinsGrid);

    try {
        const token = localStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json'
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(`${API_BASE_URL}/api/pins?userId=${userId}`, {
            method: 'GET',
            headers: headers
        });

        if (!response.ok) {
            throw new Error('Failed to load user pins');
        }

        const data = await response.json();

        if (data.content && Array.isArray(data.content)) {
            userPins = data.content;
        } else if (Array.isArray(data)) {
            userPins = data;
        } else {
            userPins = [];
        }

        renderPins(userPins);
    } catch (error) {
        console.error('Error loading user pins:', error);
        showErrorMessage('Не удалось загрузить изображения пользователя. Пожалуйста, попробуйте позже.');
    } finally {
        hideLoading(pinsGrid);
    }
}

function renderPins(pins) {
    const pinsGrid = document.querySelector('.pins-list .grid');
    if (!pinsGrid) return;

    pinsGrid.innerHTML = '';

    if (pins.length === 0) {
        showNoPinsMessage();
        return;
    }

    pins.forEach((pin, index) => {
        const pinElement = document.createElement('div');
        pinElement.className = 'pin-item';
        pinElement.setAttribute('data-id', pin.id);

        pinElement.innerHTML = `
            <a href="/pin.html?id=${pin.id}">
                <img src="${pin.imageUrl}" alt="${pin.description || 'Изображение'}">
                <p>${pin.description || 'Без описания'}</p>
            </a>
            ${isOwnProfile ? `
                <div class="pin-actions">
                    <button class="edit-btn" data-id="${pin.id}"><i class="fas fa-edit"></i></button>
                    <button class="delete-btn" data-id="${pin.id}"><i class="fas fa-trash"></i></button>
                </div>
            ` : ''}
        `;

        pinsGrid.appendChild(pinElement);

        // Add event listeners for edit and delete buttons
        if (isOwnProfile) {
            const editBtn = pinElement.querySelector('.edit-btn');
            if (editBtn) {
                editBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    handleEditPin(pin.id);
                });
            }

            const deleteBtn = pinElement.querySelector('.delete-btn');
            if (deleteBtn) {
                deleteBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    handleDeletePin(pin.id);
                });
            }
        }
    });
}

function createPinElement(pin) {
    const pinElement = document.createElement('div');
    pinElement.className = 'pin-item';
    pinElement.setAttribute('data-id', pin.id);

    pinElement.innerHTML = `
        <a href="/pin.html?id=${pin.id}">
            <img src="${pin.imageUrl}" alt="${pin.description || 'Изображение'}">
            <p>${pin.description || 'Без описания'}</p>
        </a>
        ${isOwnProfile ? `
            <div class="pin-actions">
                <button class="edit-btn" data-id="${pin.id}"><i class="fas fa-edit"></i></button>
                <button class="delete-btn" data-id="${pin.id}"><i class="fas fa-trash"></i></button>
            </div>
        ` : ''}
    `;

    return pinElement;
}

function showNoPinsMessage() {
    const pinsGrid = document.querySelector('.pins-list .grid');
    if (!pinsGrid) return;

    const message = document.createElement('div');
    message.className = 'no-pins-message';

    if (isOwnProfile) {
        message.innerHTML = `
            <i class="fas fa-image"></i>
            <h3>У вас пока нет изображений</h3>
            <p>Нажмите кнопку "Добавить изображение", чтобы загрузить свое первое изображение</p>
            <button id="addFirstPinBtn" class="btn btn-primary">Добавить изображение</button>
        `;
    } else {
        message.innerHTML = `
            <i class="fas fa-image"></i>
            <h3>У пользователя пока нет изображений</h3>
            <p>Загляните позже, возможно, появятся новые изображения</p>
        `;
    }

    pinsGrid.appendChild(message);

    // Add event listener for add first pin button
    const addFirstPinBtn = document.getElementById('addFirstPinBtn');
    if (addFirstPinBtn) {
        addFirstPinBtn.addEventListener('click', handleAddPin);
    }
}

async function handleLogout() {
    try {
        localStorage.removeItem('token');
        currentUser = null;
        isOwnProfile = false;
        updateUIForGuest();
        showSuccessMessage('Вы успешно вышли из системы');

        // Redirect to home page after logout
        setTimeout(() => {
            window.location.href = '/';
        }, 1500);
    } catch (error) {
        console.error('Error during logout:', error);
        showErrorMessage('Произошла ошибка при выходе из системы');
    }
}

async function handleAddPin(e) {
    e.preventDefault();

    if (!currentUser) {
        showErrorMessage('Пожалуйста, войдите в систему, чтобы добавлять изображения');
        return;
    }

    // Create modal for adding pin
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h2>Добавить изображение</h2>
                <button class="close-btn">&times;</button>
            </div>
            <form id="addPinForm" class="form">
                <div class="form-group">
                    <label for="pinImage">Изображение</label>
                    <input type="file" id="pinImage" accept="image/*" required>
                    <div id="imagePreview" class="image-preview"></div>
                </div>
                <div class="form-group">
                    <label for="pinDescription">Описание</label>
                    <textarea id="pinDescription" rows="3" placeholder="Добавьте описание..."></textarea>
                </div>
                <button type="submit" class="btn btn-primary">Добавить</button>
            </form>
        </div>
    `;

    document.body.appendChild(modal);

    // Show modal
    setTimeout(() => {
        modal.classList.add('active');
    }, 10);

    // Add event listener for close button
    const closeBtn = modal.querySelector('.close-btn');
    closeBtn.addEventListener('click', () => {
        modal.classList.remove('active');
        setTimeout(() => {
            modal.remove();
        }, 300);
    });

    // Add event listener for image preview
    const pinImage = document.getElementById('pinImage');
    const imagePreview = document.getElementById('imagePreview');

    pinImage.addEventListener('change', function() {
        const file = this.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                imagePreview.innerHTML = `<img src="${e.target.result}" alt="Preview">`;
            };
            reader.readAsDataURL(file);
        }
    });

    // Add event listener for form submission
    const addPinForm = document.getElementById('addPinForm');
    addPinForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const file = pinImage.files[0];
        const description = document.getElementById('pinDescription').value.trim();

        if (!file) {
            showErrorMessage('Пожалуйста, выберите изображение');
            return;
        }

        try {
            const token = localStorage.getItem('token');
            if (!token) {
                showErrorMessage('Пожалуйста, войдите в систему, чтобы добавлять изображения');
                return;
            }

            // Create form data
            const formData = new FormData();
            formData.append('file', file);
            formData.append('description', description);

            // Show loading indicator
            const submitBtn = addPinForm.querySelector('button[type="submit"]');
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Загрузка...';

            // Upload image
            const response = await fetch(`${API_BASE_URL}/api/pins/upload`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            if (!response.ok) {
                throw new Error('Failed to upload image');
            }

            const newPin = await response.json();

            // Add new pin to the list
            userPins.unshift(newPin);

            // Re-render pins
            renderPins(userPins);

            // Close modal
            modal.classList.remove('active');
            setTimeout(() => {
                modal.remove();
            }, 300);

            showSuccessMessage('Изображение успешно добавлено');
        } catch (error) {
            console.error('Error adding pin:', error);
            showErrorMessage('Не удалось добавить изображение. Пожалуйста, попробуйте позже.');
        }
    });
}

function handleEditPin(pinId) {
    // In a real application, you would implement pin editing functionality
    showErrorMessage('Функция редактирования изображения пока не реализована');
}

async function handleDeletePin(pinId) {
    if (!currentUser) {
        showErrorMessage('Пожалуйста, войдите в систему, чтобы удалять изображения');
        return;
    }

    if (!confirm('Вы уверены, что хотите удалить это изображение?')) {
        return;
    }

    try {
        const token = localStorage.getItem('token');
        if (!token) {
            showErrorMessage('Пожалуйста, войдите в систему, чтобы удалять изображения');
            return;
        }

        const response = await fetch(`${API_BASE_URL}/api/pins/${pinId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to delete pin');
        }

        // Remove pin from the list
        userPins = userPins.filter(pin => pin.id !== pinId);

        // Remove pin element from the DOM
        const pinElement = document.querySelector(`.pin-item[data-id="${pinId}"]`);
        if (pinElement) {
            pinElement.classList.add('removing');
            setTimeout(() => {
                pinElement.remove();

                // Show no pins message if there are no pins left
                if (userPins.length === 0) {
                    showNoPinsMessage();
                }
            }, 300);
        }

        showSuccessMessage('Изображение успешно удалено');
    } catch (error) {
        console.error('Error deleting pin:', error);
        showErrorMessage('Не удалось удалить изображение. Пожалуйста, попробуйте позже.');
    }
}

function showLoading(container) {
    if (!container) return;

    const existingIndicator = container.querySelector('.loading-indicator');
    if (!existingIndicator) {
        const indicator = loadingIndicator.cloneNode(true);
        container.appendChild(indicator);
    }
}

function hideLoading(container) {
    if (!container) return;

    const indicator = container.querySelector('.loading-indicator');
    if (indicator) {
        indicator.remove();
    }
}

function showErrorMessage(message) {
    const errorElement = document.createElement('div');
    errorElement.className = 'error-message';
    errorElement.innerHTML = `
        <i class="fas fa-exclamation-circle"></i>
        <span>${message}</span>
    `;

    document.body.appendChild(errorElement);

    // Анимация появления и исчезновения
    setTimeout(() => {
        errorElement.classList.add('show');
    }, 10);

    setTimeout(() => {
        errorElement.classList.remove('show');
        setTimeout(() => {
            errorElement.remove();
        }, 300);
    }, 3000);
}

function showSuccessMessage(message) {
    const successElement = document.createElement('div');
    successElement.className = 'success-message';
    successElement.innerHTML = `
        <i class="fas fa-check-circle"></i>
        <span>${message}</span>
    `;

    document.body.appendChild(successElement);

    // Анимация появления и исчезновения
    setTimeout(() => {
        successElement.classList.add('show');
    }, 10);

    setTimeout(() => {
        successElement.classList.remove('show');
        setTimeout(() => {
            successElement.remove();
        }, 300);
    }, 3000);
}

function debounce(func, wait) {
    let timeout;
    return function() {
        const context = this, args = arguments;
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(context, args), wait);
    };
}

function addDynamicStyles() {
    const style = document.createElement('style');
    style.textContent = `
        .pin-item.removing {
            animation: fadeOut 0.3s ease-out forwards;
        }

        @keyframes fadeOut {
            to {
                opacity: 0;
                transform: scale(0.8);
            }
        }

        .no-pins-message {
            grid-column: 1 / -1;
            text-align: center;
            padding: 3rem;
            background-color: var(--white-color);
            border-radius: var(--border-radius);
            box-shadow: 0 4px 6px var(--shadow-color);
        }

        .no-pins-message i {
            font-size: 3rem;
            color: var(--secondary-color);
            margin-bottom: 1rem;
        }

        .no-pins-message h3 {
            font-size: 1.5rem;
            margin-bottom: 0.5rem;
            color: var(--text-color);
        }

        .no-pins-message p {
            color: var(--secondary-color);
            margin-bottom: 1.5rem;
        }

        .image-preview {
            margin-top: 1rem;
            max-width: 100%;
            overflow: hidden;
            border-radius: var(--border-radius);
        }

        .image-preview img {
            max-width: 100%;
            max-height: 300px;
            object-fit: contain;
        }

        .profile-image {
            width: 100px;
            height: 100px;
            border-radius: 50%;
            object-fit: cover;
            margin-bottom: 1rem;
            border: 3px solid var(--primary-color);
        }

        .registration-date {
            font-size: 0.9rem;
            color: var(--secondary-color);
            margin-top: 0.5rem;
        }

        .pin-actions {
            position: absolute;
            top: 0.5rem;
            right: 0.5rem;
            display: flex;
            gap: 0.5rem;
            opacity: 0;
            transition: opacity 0.3s ease;
        }

        .pin-item:hover .pin-actions {
            opacity: 1;
        }

        .pin-actions button {
            background-color: var(--white-color);
            color: var(--text-color);
            border: none;
            border-radius: 50%;
            width: 2rem;
            height: 2rem;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: background-color 0.3s ease, color 0.3s ease;
        }

        .pin-actions .edit-btn:hover {
            background-color: var(--accent-color);
            color: var(--primary-color);
        }

        .pin-actions .delete-btn:hover {
            background-color: var(--error-color);
            color: var(--white-color);
        }
    `;

    document.head.appendChild(style);
}

