-- Индексы для таблицы пользователей
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- Индексы для таблицы пинов
CREATE INDEX IF NOT EXISTS idx_pins_user_id ON pins (user_id);
CREATE INDEX IF NOT EXISTS idx_pins_board_id ON pins (board_id);
CREATE INDEX IF NOT EXISTS idx_pins_created_at ON pins (created_at);

-- Индексы для таблицы лайков
CREATE INDEX IF NOT EXISTS idx_likes_user_id ON likes (user_id);
CREATE INDEX IF NOT EXISTS idx_likes_pin_id ON likes (pin_id);
CREATE INDEX IF NOT EXISTS idx_likes_photo_id ON likes (photo_id);

-- Индексы для таблицы комментариев
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments (user_id);
CREATE INDEX IF NOT EXISTS idx_comments_pin_id ON comments (pin_id);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments (created_at);

-- Индексы для таблицы досок
CREATE INDEX IF NOT EXISTS idx_boards_user_id ON boards (user_id);

-- Индексы для поиска
CREATE INDEX IF NOT EXISTS idx_pins_description ON pins (description);

-- Индексы для уведомлений
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_id ON notifications (recipient_id);
CREATE INDEX IF NOT EXISTS idx_notifications_pin_id ON notifications (pin_id);