-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Aug 24, 2024 at 06:30 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `railway`
--

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `LoadTrainSchedules` ()   BEGIN
    SELECT * FROM train_schedules;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `passenger_bookings`
--

CREATE TABLE `passenger_bookings` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `age` int(11) NOT NULL,
  `gender` varchar(10) NOT NULL,
  `source` varchar(255) NOT NULL,
  `destination` varchar(255) NOT NULL,
  `travel_date` date NOT NULL,
  `travel_class` int(11) NOT NULL,
  `mobile_number` varchar(15) NOT NULL,
  `train_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `passenger_bookings`
--

INSERT INTO `passenger_bookings` (`id`, `name`, `age`, `gender`, `source`, `destination`, `travel_date`, `travel_class`, `mobile_number`, `train_id`) VALUES
(1, 'mahek', 18, 'female', 'ahmedabad', 'surat', '2024-08-21', 1, '9510916689', 1),
(2, 'kinjal', 36, 'female', 'ahmedabad', 'surat', '2024-08-21', 1, '9510916692', 1),
(5, 'khushi', 12, 'female', 'ahmedabad', 'surat', '2025-08-21', 1, '8401009681', 1),
(11, 'amrish', 40, 'male', 'ahmedabad', 'surat', '2024-08-21', 1, '202020202020', 1);

-- --------------------------------------------------------

--
-- Table structure for table `train_schedules`
--

CREATE TABLE `train_schedules` (
  `train_id` int(11) NOT NULL,
  `train_name` varchar(255) NOT NULL,
  `source` varchar(255) NOT NULL,
  `destination` varchar(255) NOT NULL,
  `departure_time` time NOT NULL,
  `arrival_time` time NOT NULL,
  `total_seats` int(11) NOT NULL,
  `available_seats` int(11) NOT NULL,
  `travel_date` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `train_schedules`
--

INSERT INTO `train_schedules` (`train_id`, `train_name`, `source`, `destination`, `departure_time`, `arrival_time`, `total_seats`, `available_seats`, `travel_date`) VALUES
(1, 'vande bharat', 'ahmedabad', 'surat', '06:00:00', '10:00:00', 30, 15, '2024-08-21'),
(2, 'karnavati Exp', 'ahmedabad', 'mumbai', '05:00:00', '12:20:00', 40, 30, '2024-08-22'),
(3, 'rajdhani', 'ahmedabad', 'new delhi', '18:00:00', '07:30:00', 100, 80, '2024-08-23'),
(4, 'shatabdi', 'mumbai', 'ahmedabad', '06:10:00', '12:25:00', 70, 60, '2024-08-24'),
(5, 'Swran rajdhani', 'new delhi', 'ahmedabad', '19:55:00', '08:05:00', 50, 40, '2024-08-25'),
(6, 'howrah duronto', 'nagpur', 'howrah', '04:15:00', '20:20:00', 45, 25, '2024-08-26'),
(7, 'gujarat mail', 'ahmedabad', 'mumbai', '22:00:00', '06:30:00', 45, 35, '2024-08-27'),
(8, 'tejas exp', 'mumbai', 'ahmedabad', '15:45:00', '22:10:00', 75, 65, '2024-08-28'),
(9, 'nagpur duronto', 'howrah', 'nagpur', '16:50:00', '07:30:00', 20, 10, '2024-08-29'),
(10, 'shanti exp', 'indore', 'ahmedabad', '23:00:00', '06:15:00', 25, 15, '2024-08-30'),
(11, 'shanti exp', 'ahmedabad', 'indore', '19:30:00', '06:30:00', 55, 35, '2024-08-31'),
(12, 'somnath exp', 'jabalpur', 'ahmedabad', '18:30:00', '07:30:00', 85, 65, '2024-09-01'),
(13, 'sabarmati', 'varanshi', 'ahmedabad', '23:00:00', '08:30:00', 50, 30, '2024-09-02'),
(14, 'tapovan exp', 'bhopal', 'indore', '06:00:00', '09:00:00', 25, 20, '2024-09-03'),
(15, 'jodhpur exp', 'pali marwar', 'abu road', '16:30:00', '04:30:00', 45, 25, '2024-09-04'),
(16, 'rajdhani', 'delhi', 'bangalore', '08:00:00', '07:00:00', 30, 26, '2024-08-22');

--
-- Triggers `train_schedules`
--
DELIMITER $$
CREATE TRIGGER `trg_train_schedules_delete` AFTER DELETE ON `train_schedules` FOR EACH ROW BEGIN
    INSERT INTO train_schedules_audit (train_id, operation_type, old_train_name, old_source, old_destination, old_departure_time, old_arrival_time, old_total_seats, old_available_seats, old_travel_date)
    VALUES (OLD.train_id, 'DELETE', OLD.train_name, OLD.source, OLD.destination, OLD.departure_time, OLD.arrival_time, OLD.total_seats, OLD.available_seats, OLD.travel_date);
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_train_schedules_insert` AFTER INSERT ON `train_schedules` FOR EACH ROW BEGIN
    INSERT INTO train_schedules_audit (train_id, operation_type)
    VALUES (NEW.train_id, 'INSERT');
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_train_schedules_update` AFTER UPDATE ON `train_schedules` FOR EACH ROW BEGIN
    INSERT INTO train_schedules_audit (train_id, operation_type, old_train_name, old_source, old_destination, old_departure_time, old_arrival_time, old_total_seats, old_available_seats, old_travel_date)
    VALUES (OLD.train_id, 'UPDATE', OLD.train_name, OLD.source, OLD.destination, OLD.departure_time, OLD.arrival_time, OLD.total_seats, OLD.available_seats, OLD.travel_date);
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `train_schedules_audit`
--

CREATE TABLE `train_schedules_audit` (
  `audit_id` int(11) NOT NULL,
  `train_id` int(11) DEFAULT NULL,
  `operation_type` varchar(10) DEFAULT NULL,
  `operation_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `old_train_name` varchar(255) DEFAULT NULL,
  `old_source` varchar(255) DEFAULT NULL,
  `old_destination` varchar(255) DEFAULT NULL,
  `old_departure_time` time DEFAULT NULL,
  `old_arrival_time` time DEFAULT NULL,
  `old_total_seats` int(11) DEFAULT NULL,
  `old_available_seats` int(11) DEFAULT NULL,
  `old_travel_date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `train_schedules_audit`
--

INSERT INTO `train_schedules_audit` (`audit_id`, `train_id`, `operation_type`, `operation_time`, `old_train_name`, `old_source`, `old_destination`, `old_departure_time`, `old_arrival_time`, `old_total_seats`, `old_available_seats`, `old_travel_date`) VALUES
(1, 17, 'INSERT', '2024-08-23 11:57:08', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(2, 17, 'UPDATE', '2024-08-23 12:06:22', 'Tejas EXP', 'delhi', 'lucknow', '08:00:00', '12:30:00', 50, 25, '2024-08-30'),
(3, 17, 'DELETE', '2024-08-23 12:06:56', 'duranto', 'delhi', 'lucknow', '08:00:00', '12:30:00', 50, 30, '2024-08-30');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `passenger_bookings`
--
ALTER TABLE `passenger_bookings`
  ADD PRIMARY KEY (`id`),
  ADD KEY `passenger_bookings_train_id_fr` (`train_id`);

--
-- Indexes for table `train_schedules`
--
ALTER TABLE `train_schedules`
  ADD PRIMARY KEY (`train_id`);

--
-- Indexes for table `train_schedules_audit`
--
ALTER TABLE `train_schedules_audit`
  ADD PRIMARY KEY (`audit_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `passenger_bookings`
--
ALTER TABLE `passenger_bookings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `train_schedules`
--
ALTER TABLE `train_schedules`
  MODIFY `train_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `train_schedules_audit`
--
ALTER TABLE `train_schedules_audit`
  MODIFY `audit_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `passenger_bookings`
--
ALTER TABLE `passenger_bookings`
  ADD CONSTRAINT `passenger_bookings_train_id_fr` FOREIGN KEY (`train_id`) REFERENCES `train_schedules` (`train_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
