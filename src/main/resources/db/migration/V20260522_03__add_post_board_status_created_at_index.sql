CREATE INDEX idx_post_board_status_created_at
    ON post (board_id, status, created_at DESC);



SHOW INDEX FROM post;