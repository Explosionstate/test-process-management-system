USE test_process_tracker;

CREATE TABLE IF NOT EXISTS defect_attachment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  defect_id BIGINT NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  stored_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(500) NOT NULL,
  content_type VARCHAR(120),
  file_size BIGINT NOT NULL,
  uploaded_by BIGINT NOT NULL,
  uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_defect_attachment_defect_id (defect_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
