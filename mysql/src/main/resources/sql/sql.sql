create database my_lock;

CREATE TABLE `lock_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `resource` char(20) NOT NULL DEFAULT '' COMMENT '站点名称',
  `node` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `resource`(`resource`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;