-- ============================================
-- 404-Shop 数据库初始化脚本
-- 适用数据库: MySQL 5.7+
-- ============================================
SET NAMES utf8mb4;

-- ============================================
-- 1. shop_user_1024 用户数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS `shop_user_1024` DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE `shop_user_1024`;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL COMMENT '昵称',
  `pwd` varchar(128) DEFAULT NULL COMMENT '密码',
  `head_img` varchar(256) DEFAULT NULL COMMENT '头像',
  `slogan` varchar(128) DEFAULT NULL COMMENT '用户签名',
  `sex` tinyint(4) DEFAULT NULL COMMENT '0女 1男',
  `points` int(11) DEFAULT '0' COMMENT '积分',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mail` varchar(64) DEFAULT NULL COMMENT '邮箱',
  `secret` varchar(64) DEFAULT NULL COMMENT '盐',
  `admin` int(11) DEFAULT '0' COMMENT '是否管理员 0普通用户 1管理员',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 地址表
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `default_status` int(11) DEFAULT '0' COMMENT '是否默认 0否 1是',
  `receive_name` varchar(64) DEFAULT NULL COMMENT '收件人',
  `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
  `province` varchar(32) DEFAULT NULL COMMENT '省份',
  `city` varchar(32) DEFAULT NULL COMMENT '城市',
  `region` varchar(32) DEFAULT NULL COMMENT '区县',
  `detail_address` varchar(256) DEFAULT NULL COMMENT '详细地址',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户示例数据 (密码均为 123456)
INSERT INTO `user` (`id`, `name`, `pwd`, `head_img`, `slogan`, `sex`, `points`, `create_time`, `mail`, `secret`, `admin`) VALUES
(1, '测试用户', '$1$Zx9kLm2P$pQL58vz1hcMVr9Y/xu67T1', NULL, '这个人很懒，什么都没留下', 1, 100, NOW(), 'test@example.com', '$1$Zx9kLm2P', 0),
(2, '演示管理员', '$1$Zx9kLm2P$pQL58vz1hcMVr9Y/xu67T1', NULL, '项目演示管理员账号', 1, 1000, NOW(), 'admin@example.com', '$1$Zx9kLm2P', 1);

INSERT INTO `address` (`user_id`, `default_status`, `receive_name`, `phone`, `province`, `city`, `region`, `detail_address`) VALUES
(1, 1, '演示用户', '13800138000', '黑龙江省', '哈尔滨市', '南岗区', '学府路演示地址');

-- ============================================
-- 2. shop_product_1024 商品数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS `shop_product_1024` DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE `shop_product_1024`;

-- 商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(128) DEFAULT NULL COMMENT '标题',
  `cover_img` varchar(256) DEFAULT NULL COMMENT '封面图',
  `detail` text COMMENT '详情',
  `old_amount` decimal(10,2) DEFAULT '0.00' COMMENT '原价',
  `amount` decimal(10,2) DEFAULT '0.00' COMMENT '现价',
  `stock` int(11) DEFAULT '0' COMMENT '库存',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `lock_stock` int(11) DEFAULT '0' COMMENT '锁定库存',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 轮播图表
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `img` varchar(256) DEFAULT NULL COMMENT '图片',
  `url` varchar(256) DEFAULT NULL COMMENT '跳转地址',
  `weight` int(11) DEFAULT '0' COMMENT '权重',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 商品库存任务表
DROP TABLE IF EXISTS `product_task`;
CREATE TABLE `product_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) DEFAULT NULL COMMENT '商品id',
  `buy_num` int(11) DEFAULT NULL COMMENT '购买数量',
  `product_name` varchar(128) DEFAULT NULL COMMENT '商品标题',
  `lock_state` varchar(32) DEFAULT NULL COMMENT '锁定状态',
  `out_trade_no` varchar(64) DEFAULT NULL COMMENT '订单号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 商品示例数据
INSERT INTO `product` (`title`, `cover_img`, `detail`, `old_amount`, `amount`, `stock`, `lock_stock`) VALUES
('简约蓝牙耳机', 'https://picsum.photos/seed/bt-earphone/400/400', '高品质蓝牙耳机，支持降噪，续航24小时。', 199.00, 129.00, 100, 0),
('机械键盘', 'https://picsum.photos/seed/keyboard/400/400', '青轴机械键盘，87键，RGB背光。', 399.00, 259.00, 50, 0),
('无线鼠标', 'https://picsum.photos/seed/mouse/400/400', '静音无线鼠标，人体工学设计，2.4G连接。', 89.00, 49.00, 200, 0),
('智能手表', 'https://picsum.photos/seed/smart-watch/400/400', '智能运动手表，心率监测，IP68防水。', 599.00, 399.00, 80, 0),
('Type-C扩展坞', 'https://picsum.photos/seed/dock/400/400', '7合1 Type-C扩展坞，HDMI 4K输出。', 159.00, 99.00, 150, 0),
('笔记本支架', 'https://picsum.photos/seed/laptop-stand/400/400', '铝合金笔记本支架，可调节高度，散热快。', 129.00, 79.00, 120, 0),
('USB-C充电器', 'https://picsum.photos/seed/charger/400/400', '65W GaN氮化镓充电器，支持PD快充。', 149.00, 89.00, 90, 0),
('移动电源', 'https://picsum.photos/seed/powerbank/400/400', '20000mAh大容量移动电源，支持22.5W快充。', 179.00, 109.00, 60, 0),
('显示器挂灯', 'https://picsum.photos/seed/monitor-light/400/400', '屏幕挂灯，无频闪，色温可调。', 249.00, 159.00, 70, 0),
('桌面收纳盒', 'https://picsum.photos/seed/organizer/400/400', '多功能桌面收纳盒，分区设计。', 69.00, 39.00, 300, 0);

-- 轮播图示例数据
INSERT INTO `banner` (`img`, `url`, `weight`) VALUES
('https://placehold.co/1200x400/667eea/white?text=Summer+Sale+Up+to+50%25+Off&font=raleway', '/products', 1),
('https://placehold.co/1200x400/e74c3c/white?text=New+Arrivals+Shop+Now&font=raleway', '/products', 2),
('https://placehold.co/1200x400/2D9CDB/white?text=Free+Shipping+On+All+Orders&font=raleway', '/products', 3);

-- ============================================
-- 3. shop_coupon_1024 优惠券数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS `shop_coupon_1024` DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE `shop_coupon_1024`;

-- 优惠券表
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category` varchar(32) DEFAULT NULL COMMENT '优惠券类型',
  `publish` varchar(32) DEFAULT NULL COMMENT '发布状态',
  `coupon_img` varchar(256) DEFAULT NULL COMMENT '优惠券图片',
  `coupon_title` varchar(128) DEFAULT NULL COMMENT '优惠券标题',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '优惠金额',
  `user_limit` int(11) DEFAULT '1' COMMENT '每人限领',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `publish_count` int(11) DEFAULT '0' COMMENT '发放总数',
  `stock` int(11) DEFAULT '0' COMMENT '剩余库存',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `condition_price` decimal(10,2) DEFAULT '0.00' COMMENT '使用门槛',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 优惠券领取记录表
DROP TABLE IF EXISTS `coupon_record`;
CREATE TABLE `coupon_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `coupon_id` bigint(20) DEFAULT NULL COMMENT '优惠券id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `use_state` varchar(32) DEFAULT NULL COMMENT '使用状态 NEW USED EXPIRED',
  `user_id` bigint(20) DEFAULT NULL,
  `user_name` varchar(64) DEFAULT NULL,
  `coupon_title` varchar(128) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT '0.00',
  `condition_price` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 优惠券任务表
DROP TABLE IF EXISTS `coupon_task`;
CREATE TABLE `coupon_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `coupon_record_id` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `out_trade_no` varchar(64) DEFAULT NULL,
  `lock_state` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 优惠券示例数据
INSERT INTO `coupon` (`category`, `publish`, `coupon_title`, `price`, `user_limit`, `start_time`, `end_time`, `publish_count`, `stock`, `condition_price`) VALUES
('NEW_USER', 'PUBLISH', '新人专享券', 10.00, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1000, 999, 0.00),
('PROMOTION', 'PUBLISH', '满199减20', 20.00, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 500, 498, 199.00),
('PROMOTION', 'PUBLISH', '满499减50', 50.00, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 200, 199, 499.00);

-- ============================================
-- 4. shop_order_1024 订单数据库
-- ============================================
CREATE DATABASE IF NOT EXISTS `shop_order_1024` DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE `shop_order_1024`;

-- 订单表
DROP TABLE IF EXISTS `product_order`;
CREATE TABLE `product_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `out_trade_no` varchar(64) DEFAULT NULL COMMENT '订单唯一标识',
  `state` varchar(32) DEFAULT NULL COMMENT '状态 NEW PAY CANCEL',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `total_amount` decimal(10,2) DEFAULT '0.00',
  `pay_amount` decimal(10,2) DEFAULT '0.00',
  `pay_type` varchar(32) DEFAULT NULL,
  `nickname` varchar(64) DEFAULT NULL,
  `head_img` varchar(256) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `del` int(11) DEFAULT '0',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `order_type` varchar(32) DEFAULT NULL,
  `receiver_address` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单项表
DROP TABLE IF EXISTS `product_order_item`;
CREATE TABLE `product_order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_order_id` bigint(20) DEFAULT NULL,
  `out_trade_no` varchar(64) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `product_name` varchar(128) DEFAULT NULL,
  `product_img` varchar(256) DEFAULT NULL,
  `buy_num` int(11) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `total_amount` decimal(10,2) DEFAULT '0.00',
  `amount` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
