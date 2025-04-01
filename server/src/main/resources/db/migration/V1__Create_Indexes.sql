-- Create indexes for commonly queried fields

-- User Indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Pin Indexes
CREATE INDEX IF NOT EXISTS idx_pins_user_id ON pins(user_id);
CREATE INDEX IF NOT EXISTS idx_pins_board_id ON pins(board_id);
CREATE INDEX IF NOT EXISTS idx_pins_created_at ON pins(created_at);
CREATE INDEX IF NOT EXISTS idx_pins_description_gin ON pins USING gin(to_tsvector('english', description));

-- Board Indexes
CREATE INDEX IF NOT EXISTS idx_boards_user_id ON boards(user_id);
CREATE INDEX IF NOT EXISTS idx_boards_title ON boards(title);

-- Follow Indexes
CREATE INDEX IF NOT EXISTS idx_follows_follower_id ON follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following_id ON follows(following_id);
CREATE INDEX IF NOT EXISTS idx_follows_composite ON follows(follower_id, following_id);

-- Like Indexes
CREATE INDEX IF NOT EXISTS idx_likes_user_id ON likes(user_id);
CREATE INDEX IF NOT EXISTS idx_likes_pin_id ON likes(pin_id);
CREATE INDEX IF NOT EXISTS idx_likes_composite ON likes(user_id, pin_id);

-- Comment Indexes
CREATE INDEX IF NOT EXISTS idx_comments_pin_id ON comments(pin_id);
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);

-- Notification Indexes
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);

-- Refresh Token Indexes
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expiry_date ON refresh_tokens(expiry_date);