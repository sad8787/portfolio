CREATE DATABASE IF NOT EXISTS `xcssampletest`;
USE `xcssampletest`;

DROP TABLE IF EXISTS `Pet`;
DROP TABLE IF EXISTS `User`;

--
-- Table structure for table `User`
--
CREATE TABLE `User` (
  `role` varchar(5) NOT NULL,
  `login` varchar(100) NOT NULL,
  `password` varchar(32) NOT NULL,
  PRIMARY KEY (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `Pet`
--
CREATE TABLE `Pet` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `animal` varchar(4) NOT NULL,
  `birth` datetime NOT NULL,
  `name` varchar(100) NOT NULL,
  `owner` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_Pet_Owner` (`owner`),
  CONSTRAINT `FK_Pet_Owner_login` FOREIGN KEY (`owner`) REFERENCES `User` (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- User creation
--
CREATE USER IF NOT EXISTS xcs@localhost IDENTIFIED BY 'xcs';
GRANT ALL PRIVILEGES ON xcssampletest.* TO xcs@localhost; 
FLUSH PRIVILEGES;