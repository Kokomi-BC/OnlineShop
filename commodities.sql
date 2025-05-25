/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80041
 Source Host           : localhost:3306
 Source Schema         : commodities

 Target Server Type    : MySQL
 Target Server Version : 80041
 File Encoding         : 65001

 Date: 24/05/2025 15:13:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `cartid` int NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `userid` int NOT NULL COMMENT '客户ID',
  `skuid` int NOT NULL COMMENT '商品SKUID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品类别',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品详情',
  `color` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '颜色',
  `style` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '款式',
  `price` decimal(10, 2) NOT NULL COMMENT '单价',
  `num` int NOT NULL COMMENT '数量',
  PRIMARY KEY (`userid`, `skuid`) USING BTREE,
  UNIQUE INDEX `cartid`(`cartid` ASC) USING BTREE,
  INDEX `skuid`(`skuid` ASC) USING BTREE,
  CONSTRAINT `cart_ibfk_1` FOREIGN KEY (`userid`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `cart_ibfk_2` FOREIGN KEY (`skuid`) REFERENCES `skus` (`skuid`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 49 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cart
-- ----------------------------
INSERT INTO `cart` VALUES (44, 1, 25, '华为puraX', '数码产品', '阔型屏', '白色', '12+512G', 6999.99, 1);
INSERT INTO `cart` VALUES (46, 1, 28, '华为puraX', '数码产品', '阔型屏', '绿色', '12+256', 5999.99, 1);
INSERT INTO `cart` VALUES (39, 1, 29, '洗发水', '日用品', '无', '黑色', '护发款', 69.90, 1);
INSERT INTO `cart` VALUES (48, 1, 32, '连衣裙', '服装', '无', '白色', '默认', 399.00, 1);
INSERT INTO `cart` VALUES (47, 1, 33, 'iPad Pro 2025', '数码产品', '无', '黑色', '12+512', 7999.00, 1);
INSERT INTO `cart` VALUES (34, 9, 25, '华为puraX', '数码产品', '阔型屏', '白色', '12+512G', 6999.99, 1);

-- ----------------------------
-- Table structure for commodities
-- ----------------------------
DROP TABLE IF EXISTS `commodities`;
CREATE TABLE `commodities`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品类别',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品详情',
  `production_date` date NULL DEFAULT NULL COMMENT '生产日期',
  `manufacturer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生产商',
  `origin` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '产地',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of commodities
-- ----------------------------
INSERT INTO `commodities` VALUES (39, 'PuraX', '数码产品', '阔型屏', '2025-05-20', '华为', '中国深圳', '');
INSERT INTO `commodities` VALUES (40, '洗发水', '日用品', '无', '2024-05-19', 'xx工厂', '美国', '');
INSERT INTO `commodities` VALUES (41, 'iPad Pro 2025', '数码产品', '无', '2025-05-21', 'Apple', '中国', '');
INSERT INTO `commodities` VALUES (47, '智能手表', '数码产品', '无', '2025-05-23', '小米', '中国北京', '');
INSERT INTO `commodities` VALUES (48, '连衣裙', '服装', '无', '2025-05-23', '未知制衣厂', '未知地区', '');

-- ----------------------------
-- Table structure for order_details
-- ----------------------------
DROP TABLE IF EXISTS `order_details`;
CREATE TABLE `order_details`  (
  `detail_id` int NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `orderid` int NOT NULL COMMENT '订单ID',
  `skuid` int NOT NULL COMMENT '商品SKUID',
  `quantity` int NOT NULL COMMENT '数量',
  `price` decimal(10, 2) NOT NULL COMMENT '下单时单价',
  PRIMARY KEY (`detail_id`) USING BTREE,
  INDEX `orderid`(`orderid` ASC) USING BTREE,
  CONSTRAINT `order_details_ibfk_1` FOREIGN KEY (`orderid`) REFERENCES `orders` (`orderid`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `order_details_chk_1` CHECK (`quantity` > 0)
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_details
-- ----------------------------
INSERT INTO `order_details` VALUES (36, 15, 28, 1, 5999.99);
INSERT INTO `order_details` VALUES (37, 15, 27, 1, 6999.99);
INSERT INTO `order_details` VALUES (38, 17, 30, 1, 69.90);
INSERT INTO `order_details` VALUES (39, 18, 29, 1, 69.90);

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `orderid` int NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `userid` int NOT NULL COMMENT '客户ID',
  `total_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  `status` enum('待支付','已支付','已发货','已完成','已取消') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '待支付' COMMENT '订单状态',
  `shipping_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货地址',
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `shipped_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `completed_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  PRIMARY KEY (`orderid`) USING BTREE,
  INDEX `userid`(`userid` ASC) USING BTREE,
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`userid`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (15, 1, 12999.98, '已支付', '湖南省株洲市', '支付宝', '2025-05-21 14:50:57', '2025-05-23 11:15:01', '2025-05-22 08:47:03', '2025-05-21 22:27:36', '示例备注');
INSERT INTO `orders` VALUES (16, 1, 13999.98, '已支付', '湖南省株洲市', '默认方式', '2025-05-21 17:02:59', NULL, NULL, NULL, NULL);
INSERT INTO `orders` VALUES (17, 1, 69.90, '已完成', '', '默认方式', '2025-05-22 17:07:57', '2025-05-23 11:42:20', '2025-05-23 11:42:14', '2025-05-23 11:42:29', '');
INSERT INTO `orders` VALUES (18, 1, 69.90, '已发货', '', '默认方式', '2025-05-22 17:08:55', '2025-05-23 11:42:55', '2025-05-23 11:43:00', NULL, '');

-- ----------------------------
-- Table structure for skus
-- ----------------------------
DROP TABLE IF EXISTS `skus`;
CREATE TABLE `skus`  (
  `skuid` int NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  `commodityid` int NOT NULL COMMENT '商品ID',
  `color` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '默认颜色' COMMENT '颜色',
  `style` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '默认款式' COMMENT '款式',
  `price` decimal(10, 2) NOT NULL COMMENT '单价',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  PRIMARY KEY (`skuid`) USING BTREE,
  INDEX `fk_commodity`(`commodityid` ASC) USING BTREE,
  CONSTRAINT `fk_commodity` FOREIGN KEY (`commodityid`) REFERENCES `commodities` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品SKU表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skus
-- ----------------------------
INSERT INTO `skus` VALUES (25, 39, '白色', '12+512G', 6999.99, 8);
INSERT INTO `skus` VALUES (27, 39, '黑色', '12+512G', 6999.99, 5);
INSERT INTO `skus` VALUES (28, 39, '绿色', '12+256', 5999.99, 1);
INSERT INTO `skus` VALUES (29, 40, '黑色', '护发款', 69.90, 11);
INSERT INTO `skus` VALUES (30, 40, '黑色', '普通款', 69.90, 10);
INSERT INTO `skus` VALUES (32, 48, '白色', '默认', 399.00, 1);
INSERT INTO `skus` VALUES (33, 41, '黑色', '12+512', 7999.00, 2);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户名',
  `password` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录密码',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '未填写' COMMENT '地址',
  `balance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '账户余额',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `permission` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modified_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'Kokomi', '$2a$10$cnSQv1QaRM/LibXCLWPN0eq4Z2KbcHLzoOlV.IEkFwt0IOPmjFriu', '13843241100', '北京市西城区西长安街', 0.31, '', 'admin', '2025-05-22 16:51:47', '2025-05-24 14:35:11');
INSERT INTO `users` VALUES (3, 'admin123', '$2a$10$nKJ1.JXVimk0hH7EVHeQSuSycMnburdHLjNYEIcmSc1mMMjJUzV2m', '18070000000', '湖南省株洲市', 100.00, '', 'admin', '2025-05-24 14:31:01', '2025-05-24 14:59:25');

-- ----------------------------
-- Triggers structure for table order_details
-- ----------------------------
DROP TRIGGER IF EXISTS `update_total_after_insert`;
delimiter ;;
CREATE TRIGGER `update_total_after_insert` AFTER INSERT ON `order_details` FOR EACH ROW BEGIN
    UPDATE orders 
    SET total_amount = total_amount + NEW.quantity * NEW.price
    WHERE orderid = NEW.orderid;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table order_details
-- ----------------------------
DROP TRIGGER IF EXISTS `update_total_after_delete`;
delimiter ;;
CREATE TRIGGER `update_total_after_delete` AFTER DELETE ON `order_details` FOR EACH ROW BEGIN
    UPDATE orders 
    SET total_amount = total_amount - OLD.quantity * OLD.price
    WHERE orderid = OLD.orderid;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table order_details
-- ----------------------------
DROP TRIGGER IF EXISTS `update_total_after_update`;
delimiter ;;
CREATE TRIGGER `update_total_after_update` AFTER UPDATE ON `order_details` FOR EACH ROW BEGIN
    DECLARE diff DECIMAL(10,2);
    SET diff = (NEW.quantity * NEW.price) - (OLD.quantity * OLD.price);
    UPDATE orders 
    SET total_amount = total_amount + diff
    WHERE orderid = NEW.orderid;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
