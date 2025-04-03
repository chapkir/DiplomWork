// Global variables
let currentUser = null;
let profileUser = null;
let userPins = [];
let isOwnProfile = false;

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

document.addEventListener('DOMContentLoaded', initApp);

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

    setupEventListeners();
    animateElements();
    window.addEventListener('scroll', animateOnScroll);
}

function setupEventListeners() {
    // Add pin button
    document.getElementById('addPinBtn')?.addEventListener('click', handleAddPin);

    // Login/Register buttons
    document.getElementById('loginBtn')?.addEventListener('click', () => {
        window.location.href = '/?login=true';
    });

    document.getElementById('registerBtn')?.addEventListener('click', () => {
        window.location.href = '/?register=true';
    });

    // Logout button
    logoutBtn?.addEventListener('click', handleLogout);
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

        const response = await fetch('/api/auth/check', {
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
    if (addPinSection && profileUser) {
        if (user.username === profileUser.username) {
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
        const userResponse = await fetch(`/api/users/${username}`);

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
            const addPinSection = document.querySelector('.add-pin');
            if (addPinSection) {
                addPinSection.style.display = 'block';
            }
        } else {
            isOwnProfile = false;
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

    // Update profile image
    const profileDetails = document.querySelector('.profile-details');
    if (profileDetails) {
        if (user.profileImageUrl) {
            const profileImage = document.createElement('img');
            profileImage.src = user.profileImageUrl;
            profileImage.alt = user.username;
            profileImage.className = 'profile-image';
            profileDetails.insertBefore(profileImage, profileDetails.firstChild);
        } else {
            // Create default profile image with user's initials
            const profileImage = document.createElement('div');
            profileImage.className = 'profile-image profile-initials';
            profileImage.textContent = user.username.substring(0, 2).toUpperCase();
            profileImage.style.backgroundColor = getRandomColor(user.username);
            profileDetails.insertBefore(profileImage, profileDetails.firstChild);
        }
    }
}

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

        const response = await fetch(`/api/pins?userId=${userId}`, {
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
    if (!pinsGrid) return;

    pinsGrid.innerHTML = '';

    if (pins.length === 0) {
        showNoPinsMessage();
        return;
    }

    pins.forEach((pin) => {
        const pinElement = createPinElement(pin);
        pinsGrid.appendChild(pinElement);
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

    // Add event listeners for edit and delete buttons
    if (isOwnProfile) {
        pinElement.querySelector('.edit-btn').addEventListener('click', (e) => {
            e.preventDefault();
            handleEditPin(pin.id);
        });

        pinElement.querySelector('.delete-btn').addEventListener('click', (e) => {
            e.preventDefault();
            handleDeletePin(pin.id);
        });
    }

    return pinElement;
}

function showNoPinsMessage() {
    if (!pinsGrid) return;

    const messageElement = document.createElement('div');
    messageElement.className = 'no-pins-message';
    messageElement.innerHTML = `
        <i class="fas fa-image"></i>
        <h3>Нет пинов</h3>
        <p>${isOwnProfile ? 'У вас пока нет пинов. Добавьте свой первый пин!' : 'У этого пользователя пока нет пинов.'}</p>
        ${isOwnProfile ? '<button class="add-pin-btn">Добавить пин</button>' : ''}
    `;

    pinsGrid.appendChild(messageElement);

    // Add event listener for add pin button
    if (isOwnProfile) {
        messageElement.querySelector('.add-pin-btn').addEventListener('click', handleAddPin);
    }
}

async function handleLogout() {
    try {
        localStorage.removeItem('token');
        currentUser = null;
        showSuccessMessage('Вы успешно вышли из системы');
        setTimeout(() => {
            window.location.href = '/';
        }, 1000);
    } catch (error) {
        console.error('Error during logout:', error);
        showErrorMessage('Ошибка при выходе из системы');
    }
}

async function handleAddPin(e) {
    if (e) e.preventDefault();

    const addPinModal = document.getElementById('addPinModal');
    if (!addPinModal) return;

    // Clear form
    const form = addPinModal.querySelector('form');
    if (form) form.reset();

    // Show modal
    addPinModal.style.display = 'flex';

    // Close modal when clicking outside
    addPinModal.addEventListener('click', (e) => {
        if (e.target === addPinModal) {
            addPinModal.style.display = 'none';
        }
    });

    // Close button
    const closeBtn = addPinModal.querySelector('.close-btn');
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            addPinModal.style.display = 'none';
        });
    }

    // Form submission
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            const formData = new FormData(form);
            const image = formData.get('image');
            const description = formData.get('description');

            if (!image || !description) {
                showErrorMessage('Пожалуйста, заполните все поля');
                return;
            }

            try {
                showLoading(form);

                const token = localStorage.getItem('token');
                if (!token) {
                    showErrorMessage('Необходимо авторизоваться');
                    hideLoading(form);
                    return;
                }

                const formDataToSend = new FormData();
                formDataToSend.append('image', image);
                formDataToSend.append('description', description);

                const response = await fetch('/api/pins', {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    },
                    body: formDataToSend
                });

                if (!response.ok) {
                    throw new Error('Failed to add pin');
                }

                const newPin = await response.json();

                // Add new pin to the list
                userPins.unshift(newPin);
                renderPins(userPins);

                showSuccessMessage('Пин успешно добавлен');
                addPinModal.style.display = 'none';
            } catch (error) {
                console.error('Error adding pin:', error);
                showErrorMessage('Ошибка при добавлении пина');
            } finally {
                hideLoading(form);
            }
        });
    }
}

function handleEditPin(pinId) {
    // Find the pin to edit
    const pin = userPins.find(p => p.id == pinId);
    if (!pin) return;

    // This function would be implemented to edit an existing pin
    console.log('Edit pin:', pin);
}

async function handleDeletePin(pinId) {
    if (!confirm('Вы уверены, что хотите удалить этот пин?')) return;

    try {
        showLoading(document.querySelector(`[data-id="${pinId}"]`));

        const token = localStorage.getItem('token');
        if (!token) {
            showErrorMessage('Необходимо авторизоваться');
            return;
        }

        const response = await fetch(`/api/pins/${pinId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to delete pin');
        }

        // Remove pin from list
        userPins = userPins.filter(p => p.id != pinId);

        // Update UI
        const pinElement = document.querySelector(`[data-id="${pinId}"]`);
        if (pinElement) {
            pinElement.remove();
        }

        if (userPins.length === 0) {
            showNoPinsMessage();
        }

        showSuccessMessage('Пин успешно удален');
    } catch (error) {
        console.error('Error deleting pin:', error);
        showErrorMessage('Ошибка при удалении пина');
    } finally {
        hideLoading(document.querySelector(`[data-id="${pinId}"]`));
    }
}

function showLoading(container) {
    if (!container) return;

    loadingIndicator.style.display = 'flex';
    container.appendChild(loadingIndicator);
}

function hideLoading(container) {
    if (!container) return;

    loadingIndicator.style.display = 'none';
    if (container.contains(loadingIndicator)) {
        container.removeChild(loadingIndicator);
    }
}

function showErrorMessage(message) {
    const toast = document.createElement('div');
    toast.className = 'toast toast-error';
    toast.innerHTML = `
        <div class="toast-content">
            <i class="fas fa-exclamation-circle"></i>
            <span>${message}</span>
        </div>
        <button class="toast-close"><i class="fas fa-times"></i></button>
    `;

    document.body.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('show');
    }, 10);

    toast.querySelector('.toast-close').addEventListener('click', () => {
        toast.classList.remove('show');
        setTimeout(() => {
            toast.remove();
        }, 300);
    });

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 5000);
}

function showSuccessMessage(message) {
    const toast = document.createElement('div');
    toast.className = 'toast toast-success';
    toast.innerHTML = `
        <div class="toast-content">
            <i class="fas fa-check-circle"></i>
            <span>${message}</span>
        </div>
        <button class="toast-close"><i class="fas fa-times"></i></button>
    `;

    document.body.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('show');
    }, 10);

    toast.querySelector('.toast-close').addEventListener('click', () => {
        toast.classList.remove('show');
        setTimeout(() => {
            toast.remove();
        }, 300);
    });

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 5000);
}

