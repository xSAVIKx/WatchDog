-- -----------------------------------------------------
-- Table watchdog_website
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `watchdog_website` (
  `website_id` INT NOT NULL ,
  `website_url` VARCHAR(255) NOT NULL DEFAULT '',
  `lastverified` DATETIME NULL DEFAULT NULL ,
  `frequency` INT NOT NULL DEFAULT 1440 ,
  `admin_internal_notes` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`website_id`) )
ENGINE = MyISAM;

-- -----------------------------------------------------
-- Table watchdog_website_log
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `watchdog_website_log` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `website_id` INT NOT NULL ,
  `first_encountered` DATETIME NULL DEFAULT NULL ,
  `last_encountered` DATETIME NULL DEFAULT NULL ,
  `return_status_code` INT(3) NULL DEFAULT NULL ,
  `return_status_text` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table watchdog_timestamp
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `watchdog_timestamp` (
  `website_id` INT NOT NULL ,
  `website_url` VARCHAR(255) NOT NULL DEFAULT '',
  `lastverified` DATETIME NULL DEFAULT NULL ,
  `frequency` INT NOT NULL DEFAULT 1440 ,
  `maximum_age` INT NOT NULL DEFAULT 60,
  `admin_internal_notes` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`website_id`) )
ENGINE = MyISAM;

-- -----------------------------------------------------
-- Table watchdog_timestamp_log
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `watchdog_timestamp_log` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `website_id` INT NOT NULL ,
  `first_encountered` DATETIME NULL DEFAULT NULL ,
  `last_encountered` DATETIME NULL DEFAULT NULL ,
  `return_status_code` INT(3) NULL DEFAULT NULL ,
  `return_status_text` VARCHAR(255) NULL DEFAULT NULL ,
  `timestamp_found` TINYINT(1)  NOT NULL DEFAULT false,
  PRIMARY KEY (`id`) )
ENGINE = MyISAM;
