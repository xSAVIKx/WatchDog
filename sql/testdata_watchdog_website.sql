CREATE TABLE IF NOT EXIST `watchdog_website` (
  `website_id` int(11) NOT NULL,
  `website_url` varchar(255) NOT NULL DEFAULT '',
  `lastverified` datetime DEFAULT NULL,
  `frequency` int(11) NOT NULL DEFAULT '1440',
  `admin_internal_notes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`website_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

INSERT INTO `watchdog_website` VALUES
(1,'http://www.google.com',NULL,30,'should always be ok'),
(2,'http://www.bettingsherlock.info',NULL,360,'should always be ok'),
(3,'http://www.pool-trax.net/default.aspx',NULL,1440,'DNS error ?'),
(4,'http://www.asdgasfgafg.com',NULL,1440,'DNS error ?'),
(5,'http://www.asianvolleyball.org/',NULL,1440,'timeout ?'),
(6,'http://www.pro-stats.com.au/',NULL,1440,'timeout ?'),
(7,'http://www.myafltips.com.au/wp/',NULL,1440,'timeout ?'),
(8,'https://www.youtube.com/channel/UCEVYtKoQdpmiAnUKK0nAbYg/featured',NULL,1440,'404 not found'),
(9,'http://www.lassen.co.nz/pickandgo',NULL,1440,'404 not found'),
(10,'http://www.hockey-asia.com/',NULL,1440,'403 forbidden'),
(11,'http://www.afl.com.au/',NULL,1440,'403 forbidden'),
(12,'https://www.bangthebook.com/picks/s/?s=NBA&c=1&h=1',NULL,1440,'500 server error'),
(13,'https://www.bangthebook.com/mlb-picks/',NULL,1440,'500 server error'),
(14,'http://www.azlanshahcup.com/',NULL,1440,'503 temporarily out'),
(15,'http://bettingzone.oddschecker.com/swimming/commonwealth-games',NULL,1440,'301 moved permanently'),
(16,'http://spectorshockey.net/blog/',NULL,1440,'301 moved permanently'),
(17,'http://www.thegrandstand.net/forum/viewforum.php?f=3',NULL,1440,'302 found'),
(18,'http://www.ehftv.com/',NULL,1440,'302 moved permanently'),
(19,'http://www.betbrain.com/badminton/',NULL,1440,'307 temporary redirect');
