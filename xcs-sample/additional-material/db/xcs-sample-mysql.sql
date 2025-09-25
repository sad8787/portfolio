CREATE DATABASE IF NOT EXISTS `xcs`;
USE `xcs`;

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
GRANT ALL PRIVILEGES ON xcs.* TO xcs@localhost; 
FLUSH PRIVILEGES;

--
-- Data for table `User`
--
INSERT INTO `User` (role, login, password) VALUES
	('ADMIN','jose','a3f6f4b40b24e2fd61f08923ed452f34'),
	('OWNER','ana','22beeae33e9b2657f9610621502cd7a4'),
	('OWNER','juan','b4fbb95580592697dc71488a1f19277e'),
	('OWNER','lorena','05009e420932c21e5a68f5ef1aadd530'),
	('OWNER','pepe','b43b4d046860b2bd945bca2597bf9f07');

--
-- Data for table `Pet`
--
INSERT INTO `Pet` (animal, birth, name, owner) VALUES
	('CAT','2000-01-01 01:01:01','Pepecat','pepe'),
	('CAT','2000-01-01 01:01:01','Max','juan'),
	('DOG','2000-01-01 01:01:01','Juandog','juan'),
	('CAT','2000-01-01 01:01:01','Anacat','ana'),
	('DOG','2000-01-01 01:01:01','Max','ana'),
	('BIRD','2000-01-01 01:01:01','Anabird','ana');
