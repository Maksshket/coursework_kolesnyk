
DROP DATABASE IF EXISTS hotel_service;
CREATE DATABASE hotel_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hotel_service;

CREATE TABLE room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10) NOT NULL,
    floor INT NOT NULL,
    room_type VARCHAR(40) NOT NULL,
    capacity INT NOT NULL,
    equipment VARCHAR(255) NOT NULL,
    CONSTRAINT uq_room_number UNIQUE (room_number)
);

CREATE TABLE specialist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    specialty VARCHAR(60) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    CONSTRAINT uq_specialist_phone UNIQUE (phone),
    CONSTRAINT uq_specialist_name_specialty UNIQUE (full_name, specialty)
);

CREATE TABLE request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    specialist_id BIGINT NULL,
    description VARCHAR(500) NOT NULL,
    state VARCHAR(30) NOT NULL,
    f1 DOUBLE NOT NULL,
    f2 DOUBLE NOT NULL,
    f3 DOUBLE NOT NULL,
    priority_index DOUBLE NOT NULL,
    FOREIGN KEY (room_id) REFERENCES room(id),
    FOREIGN KEY (specialist_id) REFERENCES specialist(id)
);

CREATE TABLE execution_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    specialist_id BIGINT NOT NULL,
    work_date VARCHAR(10) NOT NULL,
    hours_spent DOUBLE NOT NULL,
    description VARCHAR(500) NOT NULL,
    FOREIGN KEY (request_id) REFERENCES request(id),
    FOREIGN KEY (specialist_id) REFERENCES specialist(id)
);

INSERT INTO room (room_number, floor, room_type, capacity, equipment) VALUES
('101', 1, 'Стандарт', 2, 'Кондиціонер, телевізор, сейф'),
('203', 2, 'Люкс', 3, 'Кондиціонер, телевізор, міні-бар, джакузі'),
('305', 3, 'Сімейний', 4, 'Кондиціонер, телевізор, холодильник, Wi-Fi роутер');

INSERT INTO specialist (full_name, specialty, phone) VALUES
('Мельник Андрій Олегович', 'Електрик', '+380500000001'),
('Шевченко Ігор Вікторович', 'Сантехнік', '+380500000002'),
('Бондаренко Тетяна Юріївна', 'Мережі та зв’язок', '+380500000003');

INSERT INTO request (room_id, specialist_id, description, state, f1, f2, f3, priority_index) VALUES
(1, NULL, 'Не працює кондиціонер', 'NEW', 70, 60, 50, 61.5),
(2, 2, 'Протікає змішувач у ванній', 'ASSIGNED', 40, 80, 100, 69.0),
(3, 1, 'Не працює телевізор', 'IN_PROGRESS', 60, 40, 0, 38.0),
(1, 3, 'Відсутнє підключення до Wi-Fi', 'DONE', 90, 60, 0, 57.0);

INSERT INTO execution_record (request_id, specialist_id, work_date, hours_spent, description) VALUES
(3, 1, '2026-10-14', 1.5, 'Діагностика телевізора'),
(4, 3, '2026-10-13', 2.0, 'Роутер переналаштовано');
