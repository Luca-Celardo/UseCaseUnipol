CREATE TABLE IF NOT EXISTS emailoutcomes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    outcome ENUM('ACCEPTED', 'SUCCESS', 'FAILED', 'NONE') NOT NULL
);