document.addEventListener('DOMContentLoaded', async function(){
    const token = localStorage.getItem('token');
    if(!token){
        window.location.href = 'index.html';
        return;
    }

    // Загрузка информации профиля через эндпоинт /api/profile
    try {
        const response = await fetch('http://localhost:8081/api/profile', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if(response.ok){
            // Вместо сразу response.json(), получим текст и выведем его
            const responseText = await response.text();
            console.log("Response text:", responseText);
            // Попробуем разобрать его как JSON
            const profileData = JSON.parse(responseText);

            // Отобразить информацию пользователя
            document.getElementById('username').textContent = "Пользователь: " + profileData.username;
            document.getElementById('email').textContent = "Email: " + profileData.email;

            // Если у пользователя есть пины, отображаем их
            if (profileData.pins && profileData.pins.length > 0) {
                const pinsContainer = document.getElementById('pinsContainer');
                profileData.pins.forEach(pin => {
                    const pinDiv = document.createElement('div');
                    pinDiv.classList.add('pin-item');
                    pinDiv.innerHTML = `<img src="${pin.imageUrl}" alt="${pin.description}" />
                                        <p>${pin.description}</p>`;
                    pinsContainer.appendChild(pinDiv);
                });
            }
        } else {
            alert('Ошибка получения профиля');
        }
    } catch (err) {
        console.error("Ошибка при загрузке профиля:", err);
        alert('Ошибка соединения с сервером');
    }

    // Обработка отправки формы для добавления пина
    const addPinForm = document.getElementById('addPinForm');
    addPinForm.addEventListener('submit', async function(e){
        e.preventDefault();
        const imageUrl = document.getElementById('pinImageUrl').value;
        const description = document.getElementById('pinDescription').value;
        // Если потребуется добавить boardId, можно передать его дальше; здесь ставим null
        const pinRequest = { imageUrl, description, boardId: null };

        try{
            const response = await fetch('http://localhost:8081/api/pins', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(pinRequest)
            });
            if(response.ok){
                const newPin = await response.json();
                // Отображаем новый пин в списке
                const pinDiv = document.createElement('div');
                pinDiv.classList.add('pin-item');
                pinDiv.innerHTML = `<img src="${newPin.imageUrl}" alt="${newPin.description}" />
                                    <p>${newPin.description}</p>`;
                document.getElementById('pinsContainer').appendChild(pinDiv);
                addPinForm.reset();
                alert("Пин успешно добавлен!");
            } else {
                const errorData = await response.json();
                alert(`Ошибка добавления пина: ${errorData.message}`);
            }
        } catch(err) {
            console.error(err);
            alert("Ошибка при добавлении пина");
        }
    });

    // Обработка выхода из профиля
    document.getElementById('logoutBtn').addEventListener('click', function(){
        localStorage.removeItem('token');
        window.location.href = 'index.html';
    });
});

