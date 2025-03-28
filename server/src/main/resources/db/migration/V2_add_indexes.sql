-- Индексы для таблицы pins
CREATE INDEX IF NOT EXISTS idx_pins_description ON pins USING gin (to_tsvector('russian', description));
CREATE INDEX IF NOT EXISTS idx_pins_user_id ON pins(user_id);
CREATE INDEX IF NOT EXISTS idx_pins_board_id ON pins(board_id);
CREATE INDEX IF NOT EXISTS idx_pins_created_at ON pins(created_at);

-- Индексы для таблицы users
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Индексы для таблицы follows
CREATE INDEX IF NOT EXISTS idx_follows_follower_id ON follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following_id ON follows(following_id);
CREATE INDEX IF NOT EXISTS idx_follows_created_at ON follows(created_at);

-- Индексы для таблицы likes
CREATE INDEX IF NOT EXISTS idx_likes_pin_id ON likes(pin_id);
CREATE INDEX IF NOT EXISTS idx_likes_user_id ON likes(user_id);

-- Индексы для таблицы comments
CREATE INDEX IF NOT EXISTS idx_comments_pin_id ON comments(pin_id);
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at);
CREATE INDEX IF NOT EXISTS idx_comments_text ON comments(text);

-- Индексы для таблицы boards
CREATE INDEX IF NOT EXISTS idx_boards_user_id ON boards(user_id);
CREATE INDEX IF NOT EXISTS idx_boards_title ON boards(title);

-- Индексы для таблицы notifications
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);

-- Индексы для таблицы refresh_tokens
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expiry_date ON refresh_tokens(expiry_date);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_revoked ON refresh_tokens(revoked);

-- Индексы для таблицы fcm_tokens
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_user_id ON fcm_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_active ON fcm_tokens(active);