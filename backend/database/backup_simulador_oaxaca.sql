-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: simulador_oaxaca
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `simulador_oaxaca`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `simulador_oaxaca` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `simulador_oaxaca`;

--
-- Table structure for table `ai_training_data`
--

DROP TABLE IF EXISTS `ai_training_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_training_data` (
  `id` int NOT NULL AUTO_INCREMENT,
  `neighborhood_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `apartment_m2` decimal(6,2) NOT NULL,
  `distance_to_university_m` int NOT NULL,
  `has_nearby_transport` tinyint(1) NOT NULL,
  `real_monthly_rent` decimal(10,2) NOT NULL,
  `recorded_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_neighborhood` (`neighborhood_name`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_training_data`
--

LOCK TABLES `ai_training_data` WRITE;
/*!40000 ALTER TABLE `ai_training_data` DISABLE KEYS */;
INSERT INTO `ai_training_data` VALUES (1,'Oaxaca Centro',25.00,300,1,4500.00,'2026-11-19 05:19:32'),(2,'Oaxaca Centro',30.00,400,1,5000.00,'2026-11-19 05:19:32'),(3,'Reforma',28.00,1200,0,3500.00,'2026-11-19 05:19:32'),(4,'Xochimilco',22.00,800,1,3200.00,'2026-11-19 05:19:32'),(5,'La Paz',30.00,1500,0,3200.00,'2026-11-19 05:19:32'),(6,'San Felipe',35.00,2500,1,4800.00,'2026-11-19 05:19:32'),(7,'Jalatlaco',25.00,900,1,3800.00,'2026-11-19 05:19:32'),(8,'Santa María',20.00,1800,0,2800.00,'2026-11-19 05:19:32'),(9,'Trinidad',27.00,1100,1,3400.00,'2026-11-19 05:19:32'),(10,'Ex-Marquesado',32.00,2000,0,4200.00,'2026-11-19 05:19:32'),(11,'Volcanes',24.00,3000,1,3000.00,'2026-11-19 05:19:32'),(12,'Amatlán',26.00,3500,0,2900.00,'2026-11-19 05:19:32');
/*!40000 ALTER TABLE `ai_training_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `amenities`
--

DROP TABLE IF EXISTS `amenities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `amenities` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `extra_cost_percentage` decimal(4,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `amenities`
--

LOCK TABLES `amenities` WRITE;
/*!40000 ALTER TABLE `amenities` DISABLE KEYS */;
INSERT INTO `amenities` VALUES (1,'Alberca','Piscina comunitaria',5.00),(2,'Gimnasio','Área de ejercicios equipada',3.50),(3,'Estacionamiento','Cajones de estacionamiento techados',4.00),(4,'Seguridad 24/7','Caseta de vigilancia',2.50),(5,'Área de Asadores','Zona de parrillas',1.50),(6,'Salón de Eventos','Espacio para reuniones',3.00),(7,'Jardín','Áreas verdes',2.00),(8,'Elevador','Elevador para discapacitados',6.00),(9,'Paneles Solares','Energía renovable',7.50),(10,'Cisterna','Almacenamiento de agua',2.00);
/*!40000 ALTER TABLE `amenities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `email_logs`
--

DROP TABLE IF EXISTS `email_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_logs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `email_type` enum('VERIFICATION','PDF_REPORT','WELCOME','PASSWORD_RESET') COLLATE utf8mb4_unicode_ci NOT NULL,
  `recipient_email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('SENT','FAILED','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `error_message` text COLLATE utf8mb4_unicode_ci,
  `sent_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_email_user` (`user_id`),
  KEY `idx_email_status_type` (`status`,`email_type`),
  CONSTRAINT `fk_email_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `email_logs`
--

LOCK TABLES `email_logs` WRITE;
/*!40000 ALTER TABLE `email_logs` DISABLE KEYS */;
INSERT INTO `email_logs` VALUES (1,2,'VERIFICATION','ana@test.com','SENT',NULL,'2026-11-19 05:19:36'),(2,3,'WELCOME','carlos@test.com','SENT',NULL,'2026-11-19 05:19:36'),(3,4,'VERIFICATION','maria@test.com','PENDING',NULL,'2026-11-19 05:19:36'),(4,5,'PDF_REPORT','pedro@test.com','SENT',NULL,'2026-11-19 05:19:36'),(5,6,'PDF_REPORT','luis@test.com','SENT',NULL,'2026-11-19 05:19:36'),(6,7,'WELCOME','elena@test.com','FAILED','Buzón no disponible (dato de prueba)','2026-11-19 05:19:36'),(7,8,'VERIFICATION','jorge@test.com','SENT',NULL,'2026-11-19 05:19:36'),(8,9,'PDF_REPORT','sofia@test.com','SENT',NULL,'2026-11-19 05:19:36'),(9,10,'WELCOME','diego@test.com','SENT',NULL,'2026-11-19 05:19:36'),(10,11,'PDF_REPORT','frida@test.com','SENT',NULL,'2026-11-19 05:19:36'),(11,12,'VERIFICATION','benito@test.com','SENT',NULL,'2026-11-19 05:19:36'),(12,13,'PDF_REPORT','porfirio@test.com','PENDING',NULL,'2026-11-19 05:19:36'),(13,14,'VERIFICATION','emilianoruizduran7@gmail.com','SENT',NULL,'2026-11-20 19:05:55'),(14,14,'PDF_REPORT','emilianoruizduran7@gmail.com','SENT',NULL,'2026-11-20 19:08:36'),(15,14,'PDF_REPORT','emilianoruizduran7@gmail.com','SENT',NULL,'2026-11-21 18:59:46');
/*!40000 ALTER TABLE `email_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1','schema','SQL','V1__schema.sql',969251013,'root','2026-11-19 05:19:32',310,1),(2,'2','seed catalogs','SQL','V2__seed_catalogs.sql',-1787671932,'root','2026-11-19 05:19:32',10,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `neighborhoods`
--

DROP TABLE IF EXISTS `neighborhoods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `neighborhoods` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `city` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Oaxaca de Juárez',
  `average_land_cost_m2` decimal(10,2) NOT NULL,
  `construction_cost_m2` decimal(10,2) NOT NULL,
  `zone_factor` decimal(4,2) NOT NULL DEFAULT '1.00',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_neighborhoods_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `neighborhoods`
--

LOCK TABLES `neighborhoods` WRITE;
/*!40000 ALTER TABLE `neighborhoods` DISABLE KEYS */;
INSERT INTO `neighborhoods` VALUES (1,'Oaxaca Centro','Oaxaca de Juárez',8000.00,1500.00,1.20,'2026-11-19 05:19:32'),(2,'Reforma','Oaxaca de Juárez',6500.00,1450.00,1.15,'2026-11-19 05:19:32'),(3,'Xochimilco','Oaxaca de Juárez',5000.00,1400.00,1.10,'2026-11-19 05:19:32'),(4,'La Paz','Oaxaca de Juárez',4500.00,1450.00,1.05,'2026-11-19 05:19:32'),(5,'San Felipe','Oaxaca de Juárez',7000.00,1550.00,1.18,'2026-11-19 05:19:32'),(6,'Jalatlaco','Oaxaca de Juárez',5500.00,1500.00,1.12,'2026-11-19 05:19:32'),(7,'Santa María','Oaxaca de Juárez',4000.00,1400.00,1.00,'2026-11-19 05:19:32'),(8,'Trinidad','Oaxaca de Juárez',4200.00,1450.00,1.02,'2026-11-19 05:19:32'),(9,'Ex-Marquesado','Oaxaca de Juárez',6000.00,1550.00,1.15,'2026-11-19 05:19:32'),(10,'Volcanes','Oaxaca de Juárez',3500.00,1400.00,0.95,'2026-11-19 05:19:32'),(11,'Amatlán','Oaxaca de Juárez',3800.00,1400.00,0.98,'2026-11-19 05:19:32'),(12,'San Jacinto','Oaxaca de Juárez',4800.00,1450.00,1.08,'2026-11-19 05:19:32');
/*!40000 ALTER TABLE `neighborhoods` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_logs`
--

DROP TABLE IF EXISTS `notification_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_logs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `channel` enum('SMS','WHATSAPP') COLLATE utf8mb4_unicode_ci NOT NULL,
  `recipient` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('SENT','FAILED','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `provider_sid` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_message` text COLLATE utf8mb4_unicode_ci,
  `sent_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_notification_user` (`user_id`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_logs`
--

LOCK TABLES `notification_logs` WRITE;
/*!40000 ALTER TABLE `notification_logs` DISABLE KEYS */;
INSERT INTO `notification_logs` VALUES (1,2,'SMS','+529511957445','FAILED',NULL,'Invalid template name. Trial accounts can only use predefined SMS templates.','2026-11-19 05:20:52'),(2,2,'WHATSAPP','+529511957445','FAILED',NULL,'ContentSid Required','2026-11-19 05:20:56'),(3,2,'SMS','+529511957445','FAILED',NULL,'Invalid template name. Trial accounts can only use predefined SMS templates.','2026-11-19 05:21:36'),(4,2,'SMS','+529511957445','FAILED',NULL,'Invalid template name. Trial accounts can only use predefined SMS templates.','2026-11-19 05:23:32'),(5,2,'WHATSAPP','+529511957445','FAILED',NULL,'ContentSid Required','2026-11-19 05:52:29'),(6,2,'WHATSAPP','+529511957445','SENT','wamid.HBgNNTIxOTUxMTk1NzQ0NRUCABEYEkQ1N0U2RDA5RkFBNTRFNTY3MwA=',NULL,'2026-11-19 05:55:07'),(7,2,'SMS','+529511957445','FAILED',NULL,'Invalid template name. Trial accounts can only use predefined SMS templates.','2026-11-19 05:55:47'),(8,2,'WHATSAPP','+529511957445','SENT','wamid.HBgNNTIxOTUxMTk1NzQ0NRUCABEYEjM1OUI0OThBMzk4NjcxQzZFRgA=',NULL,'2026-11-19 05:57:05'),(9,14,'WHATSAPP','+529511957445','FAILED',NULL,'Meta 401: ','2026-11-21 18:59:49');
/*!40000 ALTER TABLE `notification_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_reset_tokens`
--

DROP TABLE IF EXISTS `password_reset_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_tokens` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `token` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expires_at` datetime NOT NULL,
  `used` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `fk_reset_user` (`user_id`),
  CONSTRAINT `fk_reset_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_reset_tokens`
--

LOCK TABLES `password_reset_tokens` WRITE;
/*!40000 ALTER TABLE `password_reset_tokens` DISABLE KEYS */;
/*!40000 ALTER TABLE `password_reset_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pdf_reports`
--

DROP TABLE IF EXISTS `pdf_reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pdf_reports` (
  `id` int NOT NULL AUTO_INCREMENT,
  `simulation_id` int NOT NULL,
  `file_path` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sent_to_email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `generated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_report_simulation` (`simulation_id`),
  CONSTRAINT `fk_report_simulation` FOREIGN KEY (`simulation_id`) REFERENCES `simulations` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pdf_reports`
--

LOCK TABLES `pdf_reports` WRITE;
/*!40000 ALTER TABLE `pdf_reports` DISABLE KEYS */;
INSERT INTO `pdf_reports` VALUES (1,12,'\\var\\oaxinvest\\pdfs\\report_12_1795065647562.pdf','ana@test.com','2026-11-19 05:20:48'),(2,12,'\\var\\oaxinvest\\pdfs\\report_12_1795067749225.pdf','ana@test.com','2026-11-19 05:55:49'),(3,13,'\\var\\oaxinvest\\pdfs\\report_13_1795201716466.pdf','emilianoruizduran7@gmail.com','2026-11-20 19:08:36'),(4,14,'\\var\\oaxinvest\\pdfs\\report_14_1795287586427.pdf','emilianoruizduran7@gmail.com','2026-11-21 18:59:46');
/*!40000 ALTER TABLE `pdf_reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `revoked_tokens`
--

DROP TABLE IF EXISTS `revoked_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `revoked_tokens` (
  `jti` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expires_at` datetime NOT NULL,
  PRIMARY KEY (`jti`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `revoked_tokens`
--

LOCK TABLES `revoked_tokens` WRITE;
/*!40000 ALTER TABLE `revoked_tokens` DISABLE KEYS */;
INSERT INTO `revoked_tokens` VALUES ('15da2a14-ebdf-480d-bf05-07f609ab4c80','2026-11-21 14:59:12'),('23a9d0e3-6cb6-40ff-8d96-63d333c3887c','2026-11-19 01:20:32'),('4dc38bdc-7795-43a5-845a-907bc0f00dbf','2026-11-20 15:05:07'),('755a5fe1-614a-4bf9-99ac-0ac13a09b3ce','2026-11-22 19:51:53');
/*!40000 ALTER TABLE `revoked_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ADMIN','Acceso total: usuarios y CRUD de todos los módulos'),(2,'ANALISTA','Gestiona colonias, amenidades y dataset de IA'),(3,'CLIENTE','Usuario estándar: sus propias simulaciones y perfil');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `simulation_amenities`
--

DROP TABLE IF EXISTS `simulation_amenities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `simulation_amenities` (
  `simulation_id` int NOT NULL,
  `amenity_id` int NOT NULL,
  PRIMARY KEY (`simulation_id`,`amenity_id`),
  KEY `fk_sim_amenity_amenity` (`amenity_id`),
  CONSTRAINT `fk_sim_amenity_amenity` FOREIGN KEY (`amenity_id`) REFERENCES `amenities` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sim_amenity_sim` FOREIGN KEY (`simulation_id`) REFERENCES `simulations` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `simulation_amenities`
--

LOCK TABLES `simulation_amenities` WRITE;
/*!40000 ALTER TABLE `simulation_amenities` DISABLE KEYS */;
INSERT INTO `simulation_amenities` VALUES (1,1),(4,1),(5,1),(2,2),(5,2),(1,3),(5,3),(3,4),(13,4),(7,5),(6,6),(8,7),(12,8),(10,9),(4,10);
/*!40000 ALTER TABLE `simulation_amenities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `simulations`
--

DROP TABLE IF EXISTS `simulations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `simulations` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `neighborhood_id` int NOT NULL,
  `land_width` decimal(6,2) NOT NULL,
  `land_length` decimal(6,2) NOT NULL,
  `total_area` decimal(8,2) NOT NULL,
  `num_apartments` int NOT NULL,
  `construction_cost_per_m2` decimal(10,2) NOT NULL,
  `monthly_rent_per_apartment` decimal(10,2) NOT NULL,
  `fixed_monthly_expenses` decimal(10,2) NOT NULL,
  `calculated_total_cost` decimal(12,2) NOT NULL,
  `calculated_annual_income` decimal(12,2) NOT NULL,
  `calculated_roi` decimal(5,2) NOT NULL,
  `calculated_payback_months` decimal(6,2) NOT NULL,
  `status_semaphore` enum('VERDE','AMARILLO','ROJO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `ai_analysis_text` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_simulation_neighborhood` (`neighborhood_id`),
  KEY `idx_sim_user_created` (`user_id`,`created_at`),
  KEY `idx_sim_semaphore` (`status_semaphore`),
  CONSTRAINT `fk_simulation_neighborhood` FOREIGN KEY (`neighborhood_id`) REFERENCES `neighborhoods` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_simulation_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `simulations`
--

LOCK TABLES `simulations` WRITE;
/*!40000 ALTER TABLE `simulations` DISABLE KEYS */;
INSERT INTO `simulations` VALUES (1,2,1,10.00,20.00,200.00,10,1500.00,800.00,1000.00,327000.00,84000.00,25.69,46.71,'VERDE','Alta demanda estudiantil y turística en el centro.','2026-11-19 05:19:36'),(2,3,2,12.00,25.00,300.00,12,1450.00,750.00,1200.00,450225.00,93600.00,20.79,57.72,'VERDE',NULL,'2026-11-19 05:19:36'),(3,4,3,8.00,15.00,120.00,6,1400.00,600.00,800.00,172200.00,33600.00,19.51,61.50,'VERDE','Zona en desarrollo: conviene revisar la renta.','2026-11-19 05:19:36'),(4,2,4,10.00,20.00,200.00,10,1450.00,800.00,1000.00,310300.00,84000.00,27.07,44.33,'VERDE',NULL,'2026-11-19 05:19:36'),(5,6,5,15.00,30.00,450.00,15,1550.00,700.00,1500.00,784687.50,108000.00,13.76,87.19,'AMARILLO',NULL,'2026-11-19 05:19:36'),(6,8,6,10.00,20.00,200.00,10,1500.00,650.00,1000.00,309000.00,66000.00,21.36,56.18,'VERDE','Revisar costos de mantenimiento.','2026-11-19 05:19:36'),(7,9,7,9.00,18.00,162.00,9,1400.00,500.00,900.00,230202.00,43200.00,18.77,63.95,'VERDE',NULL,'2026-11-19 05:19:36'),(8,10,8,11.00,22.00,242.00,11,1450.00,750.00,1100.00,357918.00,85800.00,23.97,50.06,'VERDE',NULL,'2026-11-19 05:19:36'),(9,11,9,10.00,20.00,200.00,10,1550.00,850.00,1000.00,310000.00,90000.00,29.03,41.33,'VERDE',NULL,'2026-11-19 05:19:36'),(10,12,10,10.00,20.00,200.00,10,1400.00,600.00,1000.00,301000.00,60000.00,19.93,60.20,'VERDE','Zona con baja plusvalía.','2026-11-19 05:19:36'),(11,13,11,10.00,20.00,200.00,10,1400.00,500.00,1000.00,280000.00,48000.00,17.14,70.00,'VERDE',NULL,'2026-11-19 05:19:36'),(12,2,12,10.00,20.00,200.00,10,1450.00,400.00,1000.00,307400.00,36000.00,11.71,102.47,'AMARILLO',NULL,'2026-11-19 05:19:36'),(13,14,12,10.00,50.00,500.00,4,1450.00,1000.00,2000.00,743125.00,24000.00,3.23,371.56,'ROJO','Rentabilidad baja en San Jacinto (ROI 3.23%). Con estos datos el proyecto no se recomienda. Aún no hay rentas reales registradas de esta colonia para comparar.','2026-11-20 19:08:25'),(14,14,9,124.00,12.00,1488.00,21,1550.00,1222.00,1222.00,2306400.00,293280.00,12.72,94.37,'AMARILLO','Rentabilidad media en Ex-Marquesado (ROI 12.72%). Conviene revisar costos o subir la renta. La renta real promedio registrada en la zona es de $4200 mensuales.','2026-11-21 18:59:39');
/*!40000 ALTER TABLE `simulations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider` enum('LOCAL','GOOGLE','FACEBOOK') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LOCAL',
  `email_verified` tinyint(1) NOT NULL DEFAULT '0',
  `verification_token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `avatar_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role_id` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_users_role` (`role_id`),
  KEY `idx_users_name` (`name`),
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Administrador','admin@oaxinvest.mx','$2a$10$fgKAata4eNVUhghw/JpM3eFn6WYbDzIjoRckNgx5ZRZvDPEaKBXxi','LOCAL',1,NULL,NULL,1,NULL,1,'2026-11-19 05:19:36'),(2,'Ana López','ana@test.com','$2a$10$m.0BWl8eSGjIEBbwfeyur.iIUkBtaKuCctBwRfvYgOJlFVnXNTjKm','LOCAL',1,NULL,'+529511957445',1,NULL,3,'2026-11-19 05:19:36'),(3,'Carlos Ruiz','carlos@test.com',NULL,'GOOGLE',1,NULL,NULL,1,NULL,3,'2026-11-19 05:19:36'),(4,'María Paz','maria@test.com','$2a$10$6W3Jn0PZy8jzFBw3OWtpDOP7dlwoegPKDbDWEPh/Qfxr16D9NKa7O','LOCAL',0,NULL,NULL,1,NULL,3,'2026-11-19 05:19:36'),(5,'Pedro Solis','pedro@test.com','$2a$10$wEmBIWYK.sfT6hU3.zt3..7fkQTtj8/FykmOtn585w8z2VEznRU4y','LOCAL',1,NULL,NULL,1,NULL,2,'2026-11-19 05:19:36'),(6,'Luis Miguel','luis@test.com',NULL,'FACEBOOK',1,NULL,NULL,1,NULL,3,'2026-11-19 05:19:36'),(7,'Elena Poniatowska','elena@test.com','$2a$10$ieBghjkpD4.OM4dWk6ge8egDpNrbAgen5M8MyDFoAkIw/UkkGLlnW','LOCAL',1,NULL,NULL,1,NULL,2,'2026-11-19 05:19:36'),(8,'Jorge Pérez','jorge@test.com','$2a$10$W9i1BdPuPHOJ4TPPBJz1MeLHn7ZZjbChevLiZLpxxIisOh8ZCKbKG','LOCAL',0,NULL,NULL,1,NULL,3,'2026-11-19 05:19:36'),(9,'Sofía Castro','sofia@test.com','$2a$10$iE0EM0thqLOHMpTCYDhD9OkI1IdOSv08fbqUejZumwj8dTboMcCJm','LOCAL',1,NULL,NULL,1,NULL,3,'2026-11-19 05:19:36'),(10,'Diego Rivera','diego@test.com',NULL,'GOOGLE',1,NULL,NULL,1,NULL,3,'2026-11-19 05:19:36'),(11,'Frida Kahlo','frida@test.com','$2a$10$o84x0wc0CYIs4s6/4c8lh.962zQ.SSIlS4o1UlF2ve5U4v7kLF5D6','LOCAL',1,NULL,NULL,1,NULL,3,'2026-11-19 05:19:36'),(12,'Benito Juárez','benito@test.com','$2a$10$S6PJBQfYhFa/jEqfHHYu1.1Qr9c.//YuQjvGU0VRhnNDSuX0a31HO','LOCAL',1,NULL,NULL,1,NULL,3,'2026-11-19 05:19:36'),(13,'Porfirio Díaz','porfirio@test.com',NULL,'GOOGLE',0,NULL,NULL,1,NULL,3,'2026-11-19 05:19:36'),(14,'Ruiz Durán Emiliano','emilianoruizduran7@gmail.com',NULL,'GOOGLE',1,NULL,'+529511957445',1,'/uploads/avatars/14-6f01af3a-ec56-42db-9320-510760afc27a.png',3,'2026-11-20 19:05:48');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'simulador_oaxaca'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-22 17:55:44
