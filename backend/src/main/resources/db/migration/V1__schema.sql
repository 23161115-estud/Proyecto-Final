CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(150) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NULL,
    provider ENUM('LOCAL','GOOGLE','FACEBOOK') NOT NULL DEFAULT 'LOCAL',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verification_token VARCHAR(255) NULL,
    phone VARCHAR(20) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    avatar_url VARCHAR(255) NULL,
    role_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
    INDEX idx_users_role (role_id),
    INDEX idx_users_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE neighborhoods (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL DEFAULT 'Oaxaca de Juárez',
    average_land_cost_m2 DECIMAL(10,2) NOT NULL,
    construction_cost_m2 DECIMAL(10,2) NOT NULL,
    zone_factor DECIMAL(4,2) NOT NULL DEFAULT 1.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_neighborhoods_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE amenities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    extra_cost_percentage DECIMAL(4,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE simulations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    neighborhood_id INT NOT NULL,
    land_width DECIMAL(6,2) NOT NULL,
    land_length DECIMAL(6,2) NOT NULL,
    total_area DECIMAL(8,2) NOT NULL,
    num_apartments INT NOT NULL,
    construction_cost_per_m2 DECIMAL(10,2) NOT NULL,
    monthly_rent_per_apartment DECIMAL(10,2) NOT NULL,
    fixed_monthly_expenses DECIMAL(10,2) NOT NULL,
    calculated_total_cost DECIMAL(12,2) NOT NULL,
    calculated_annual_income DECIMAL(12,2) NOT NULL,
    calculated_roi DECIMAL(5,2) NOT NULL,
    calculated_payback_months DECIMAL(6,2) NOT NULL,
    status_semaphore ENUM('VERDE','AMARILLO','ROJO') NOT NULL,
    ai_analysis_text TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_simulation_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_simulation_neighborhood FOREIGN KEY (neighborhood_id) REFERENCES neighborhoods(id) ON DELETE RESTRICT,
    INDEX idx_sim_user_created (user_id, created_at),
    INDEX idx_sim_semaphore (status_semaphore)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE simulation_amenities (
    simulation_id INT NOT NULL,
    amenity_id INT NOT NULL,
    PRIMARY KEY (simulation_id, amenity_id),
    CONSTRAINT fk_sim_amenity_sim FOREIGN KEY (simulation_id) REFERENCES simulations(id) ON DELETE CASCADE,
    CONSTRAINT fk_sim_amenity_amenity FOREIGN KEY (amenity_id) REFERENCES amenities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pdf_reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    simulation_id INT NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    sent_to_email VARCHAR(150) NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_simulation FOREIGN KEY (simulation_id) REFERENCES simulations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE email_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    email_type ENUM('VERIFICATION','PDF_REPORT','WELCOME','PASSWORD_RESET') NOT NULL,
    recipient_email VARCHAR(150) NOT NULL,
    status ENUM('SENT','FAILED','PENDING') NOT NULL DEFAULT 'PENDING',
    error_message TEXT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_email_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_email_status_type (status, email_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notification_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    channel ENUM('SMS','WHATSAPP') NOT NULL,
    recipient VARCHAR(30) NOT NULL,
    status ENUM('SENT','FAILED','PENDING') NOT NULL DEFAULT 'PENDING',
    provider_sid VARCHAR(64) NULL,
    error_message TEXT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ai_training_data (
    id INT AUTO_INCREMENT PRIMARY KEY,
    neighborhood_name VARCHAR(100) NOT NULL,
    apartment_m2 DECIMAL(6,2) NOT NULL,
    distance_to_university_m INT NOT NULL,
    has_nearby_transport BOOLEAN NOT NULL,
    real_monthly_rent DECIMAL(10,2) NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_neighborhood (neighborhood_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE password_reset_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(100) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE revoked_tokens (
    jti VARCHAR(64) PRIMARY KEY,
    expires_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
