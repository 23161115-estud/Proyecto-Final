INSERT INTO roles (name, description) VALUES
('ADMIN','Acceso total: usuarios y CRUD de todos los módulos'),
('ANALISTA','Gestiona colonias, amenidades y dataset de IA'),
('CLIENTE','Usuario estándar: sus propias simulaciones y perfil');

INSERT INTO neighborhoods (name, city, average_land_cost_m2, construction_cost_m2, zone_factor) VALUES
('Oaxaca Centro','Oaxaca de Juárez',8000.00,1500.00,1.20),
('Reforma','Oaxaca de Juárez',6500.00,1450.00,1.15),
('Xochimilco','Oaxaca de Juárez',5000.00,1400.00,1.10),
('La Paz','Oaxaca de Juárez',4500.00,1450.00,1.05),
('San Felipe','Oaxaca de Juárez',7000.00,1550.00,1.18),
('Jalatlaco','Oaxaca de Juárez',5500.00,1500.00,1.12),
('Santa María','Oaxaca de Juárez',4000.00,1400.00,1.00),
('Trinidad','Oaxaca de Juárez',4200.00,1450.00,1.02),
('Ex-Marquesado','Oaxaca de Juárez',6000.00,1550.00,1.15),
('Volcanes','Oaxaca de Juárez',3500.00,1400.00,0.95),
('Amatlán','Oaxaca de Juárez',3800.00,1400.00,0.98),
('San Jacinto','Oaxaca de Juárez',4800.00,1450.00,1.08);

INSERT INTO amenities (name, description, extra_cost_percentage) VALUES
('Alberca','Piscina comunitaria',5.00),
('Gimnasio','Área de ejercicios equipada',3.50),
('Estacionamiento','Cajones de estacionamiento techados',4.00),
('Seguridad 24/7','Caseta de vigilancia',2.50),
('Área de Asadores','Zona de parrillas',1.50),
('Salón de Eventos','Espacio para reuniones',3.00),
('Jardín','Áreas verdes',2.00),
('Elevador','Elevador para discapacitados',6.00),
('Paneles Solares','Energía renovable',7.50),
('Cisterna','Almacenamiento de agua',2.00);

INSERT INTO ai_training_data (neighborhood_name, apartment_m2, distance_to_university_m, has_nearby_transport, real_monthly_rent) VALUES
('Oaxaca Centro',25.0,300,TRUE,4500.00),
('Oaxaca Centro',30.0,400,TRUE,5000.00),
('Reforma',28.0,1200,FALSE,3500.00),
('Xochimilco',22.0,800,TRUE,3200.00),
('La Paz',30.0,1500,FALSE,3200.00),
('San Felipe',35.0,2500,TRUE,4800.00),
('Jalatlaco',25.0,900,TRUE,3800.00),
('Santa María',20.0,1800,FALSE,2800.00),
('Trinidad',27.0,1100,TRUE,3400.00),
('Ex-Marquesado',32.0,2000,FALSE,4200.00),
('Volcanes',24.0,3000,TRUE,3000.00),
('Amatlán',26.0,3500,FALSE,2900.00);
