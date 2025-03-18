<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>О нас | Kiruha Chlen</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap">
    <link rel="stylesheet" href="css/styles.css">
    <link rel="stylesheet" href="css/about.css">
    <link rel="icon" type="image/png" href="https://cdn-icons-png.flaticon.com/512/2521/2521616.png">
</head>
<body>
<!-- Шапка сайта -->
<header class="header">
    <div class="container header-content">
        <div class="logo">
            <a href="/">
                <i class="fas fa-thumbtack"></i>
                <span>Kiruha Chlen</span>
            </a>
        </div>
        <nav class="nav">
            <a href="/" class="nav-link">Главная</a>
            <a href="/explore.html" class="nav-link">Обзор</a>
            <a href="/categories.html" class="nav-link">Категории</a>
            <a href="/about.html" class="nav-link active">О нас</a>
        </nav>
        <div class="auth-buttons">
            <button id="loginBtn" class="btn btn-outline" style="margin-right: 10px; display: none;">
                <i class="fas fa-sign-in-alt"></i> Войти
            </button>
            <button id="registerBtn" class="btn btn-primary" style="display: none;">
                <i class="fas fa-user-plus"></i> Регистрация
            </button>
            <a href="#" id="profileBtn" class="btn btn-outline" style="display: none; margin-right: 10px;">
                <i class="fas fa-user"></i> Личный кабинет
            </a>
            <button id="logoutBtn" class="btn btn-primary" style="display: none;">
                <i class="fas fa-sign-out-alt"></i> Выйти
            </button>
        </div>
    </div>
</header>

<!-- Основное содержимое -->
<main>
    <!-- Баннер -->
    <section class="about-banner">
        <div class="container">
            <h1>О нашем проекте</h1>
            <p>Мы создаем платформу для вдохновения и творчества</p>
        </div>
    </section>

    <!-- О нас -->
    <section class="about-section">
        <div class="container">
            <div class="about-grid">
                <div class="about-content">
                    <h2>Наша миссия</h2>
                    <p>Kiruha Chlen — это платформа, которая помогает людям находить вдохновение для своих проектов и идей. Мы стремимся создать пространство, где каждый может делиться своим творчеством, находить новые идеи и вдохновляться работами других.</p>
                    <p>Наша цель — сделать процесс поиска и сохранения идей максимально простым и удобным, чтобы вы могли сосредоточиться на том, что действительно важно: на воплощении своих идей в жизнь.</p>

                    <h2>Наши ценности</h2>
                    <ul class="values-list">
                        <li>
                            <i class="fas fa-lightbulb"></i>
                            <div>
                                <h3>Творчество</h3>
                                <p>Мы верим, что каждый человек обладает творческим потенциалом, и наша задача — помочь раскрыть его.</p>
                            </div>
                        </li>
                        <li>
                            <i class="fas fa-users"></i>
                            <div>
                                <h3>Сообщество</h3>
                                <p>Мы создаем дружелюбное и поддерживающее сообщество, где каждый может делиться своими идеями и получать обратную связь.</p>
                            </div>
                        </li>
                        <li>
                            <i class="fas fa-shield-alt"></i>
                            <div>
                                <h3>Безопасность</h3>
                                <p>Мы заботимся о безопасности наших пользователей и их данных, обеспечивая надежную защиту и конфиденциальность.</p>
                            </div>
                        </li>
                        <li>
                            <i class="fas fa-universal-access"></i>
                            <div>
                                <h3>Доступность</h3>
                                <p>Мы стремимся сделать нашу платформу доступной для всех, независимо от их технических навыков или возможностей.</p>
                            </div>
                        </li>
                    </ul>
                </div>
                <div class="about-image">
                    <img src="https://images.unsplash.com/photo-1522202176988-66273c2fd55f?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80" alt="Наша команда">
                </div>
            </div>
        </div>
    </section>

    <!-- Наша команда -->
    <section class="team-section">
        <div class="container">
            <h2>Наша команда</h2>
            <p class="section-description">Мы — команда увлеченных профессионалов, объединенных общей целью: создать лучшую платформу для вдохновения и творчества.</p>

            <div class="team-grid">
                <div class="team-member">
                    <div class="member-avatar">
                        <img src="/Images/Kiruha.jpg" alt="Кирилл Анатольевич">
                    </div>
                    <h3>Кирилл Анатольевич</h3>
                    <p class="member-role">Разработчик Frontend части проекта</p>
                    <p class="member-description">Кирилл имеет более 10 лет опыта в разработке продуктов и управлении командами. Он верит в силу визуального вдохновения и стремится сделать его доступным для всех.</p>
                    <div class="member-social">
                        <a href="#"><i class="fab fa-linkedin"></i></a>
                        <a href="#"><i class="fab fa-twitter"></i></a>
                    </div>
                </div>

                <div class="team-member">
                    <div class="member-avatar">
                        <img src="/Images/Andryxa.jpg" alt="Александр Михайлович">
                    </div>
                    <h3>Александр Михайлович</h3>
                    <p class="member-role">Разработчик Backend части проекта</p>
                    <p class="member-description">Александр — опытный разработчик с глубокими знаниями в области веб-технологий. Он отвечает за техническую сторону проекта и постоянно ищет способы улучшить пользовательский опыт.</p>
                    <div class="member-social">
                        <a href="#"><i class="fab fa-github"></i></a>
                        <a href="#"><i class="fab fa-linkedin"></i></a>
                    </div>
                </div>

                <div class="team-member">
                    <div class="member-avatar">
                        <img src="/Images/Andryxa.jpg" alt="Андрей Дмитреевич">
                    </div>
                    <h3>Андрей Дмитреевич</h3>
                    <p class="member-role">Дэппер</p>
                    <p class="member-description">Андрей депает весь наш бюджет</p>
                    <div class="member-social">
                        <a href="#"><i class="fab fa-dribbble"></i></a>
                        <a href="#"><i class="fab fa-behance"></i></a>
                    </div>
                </div>

                <div class="team-member">
                    <div class="member-avatar">
                        <img src="/Images/Oleg.jpg" alt="Олег Витальевич">
                    </div>
                    <h3>Олег Витальевич</h3>
                    <p class="member-role">Лицо компании</p>
                    <p class="member-description">Олег отвечает за продвижение нашей платформы и привлечение новых пользователей. Он увлечен цифровым маркетингом и всегда в курсе последних трендов в этой области.</p>
                    <div class="member-social">
                        <a href="#"><i class="fab fa-twitter"></i></a>
                        <a href="#"><i class="fab fa-instagram"></i></a>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Присоединяйтесь к нам -->
    <section class="join-section">
        <div class="container">
            <div class="join-content">
                <h2>Присоединяйтесь к нам</h2>
                <p>Станьте частью нашего сообщества и начните делиться своими идеями и вдохновением уже сегодня.</p>
                <div class="join-buttons">
                    <a href="/?register=true" class="btn btn-primary">Зарегистрироваться</a>
                    <a href="/?login=true" class="btn btn-outline">Войти</a>
                </div>
            </div>
        </div>
    </section>
</main>

<!-- Подвал сайта -->
<footer class="footer">
    <div class="container">
        <div class="footer-content">
            <div class="footer-column">
                <h3>О проекте</h3>
                <ul class="footer-links">
                    <li><a href="#">О нас</a></li>
                    <li><a href="#">Блог</a></li>
                    <li><a href="#">Карьера</a></li>
                    <li><a href="#">Контакты</a></li>
                </ul>
            </div>
            <div class="footer-column">
                <h3>Сообщество</h3>
                <ul class="footer-links">
                    <li><a href="#">Правила</a></li>
                    <li><a href="#">Помощь</a></li>
                    <li><a href="#">Форум</a></li>
                    <li><a href="#">Партнеры</a></li>
                </ul>
            </div>
            <div class="footer-column">
                <h3>Правовая информация</h3>
                <ul class="footer-links">
                    <li><a href="#">Условия использования</a></li>
                    <li><a href="#">Политика конфиденциальности</a></li>
                    <li><a href="#">Cookies</a></li>
                    <li><a href="#">Авторские права</a></li>
                </ul>
            </div>
        </div>
        <div class="footer-bottom">
            <p>&copy; 2025 Kiruha Chlen. Все права защищены.</p>
            <div class="social-icons">
                <a href="#"><i class="fab fa-facebook"></i></a>
                <a href="#"><i class="fab fa-twitter"></i></a>
                <a href="#"><i class="fab fa-instagram"></i></a>
            </div>
        </div>
    </div>
</footer>

<script src="js/about.js"></script>
</body>
</html>