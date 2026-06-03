-- ============================================================
--  GLOCERIMEX — Khởi tạo CSDL và dữ liệu mẫu (tiếng Việt)
--  Tạo: 2026-05-23
-- ============================================================

DROP DATABASE IF EXISTS glocerimex;
CREATE DATABASE glocerimex CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE glocerimex;

-- ============================================================
-- 1. BẢNG User
-- ============================================================
CREATE TABLE User (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    role          ENUM('admin','sales','overseas_order','warehouse') NOT NULL,
    created_date  DATE         NOT NULL,
    is_active     TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- ============================================================
-- 2. BẢNG Merchandise
-- ============================================================
CREATE TABLE Merchandise (
    merchandise_code VARCHAR(20)  PRIMARY KEY,
    merchandise_name VARCHAR(200) NOT NULL,
    default_unit     VARCHAR(20)  NOT NULL
) ENGINE=InnoDB;

-- ============================================================
-- 3. BẢNG ImportSite
-- ============================================================
CREATE TABLE ImportSite (
    site_code    VARCHAR(10)  PRIMARY KEY,
    site_name    VARCHAR(200) NOT NULL,
    country      VARCHAR(100) NOT NULL,
    contact_info VARCHAR(255)
) ENGINE=InnoDB;

-- ============================================================
-- 4. BẢNG SiteMerchandise
-- ============================================================
CREATE TABLE SiteMerchandise (
    site_code          VARCHAR(10) NOT NULL,
    merchandise_code   VARCHAR(20) NOT NULL,
    in_stock_quantity  INT         NOT NULL DEFAULT 0,
    unit               VARCHAR(20) NOT NULL,
    stock_updated_date DATE        NOT NULL,
    PRIMARY KEY (site_code, merchandise_code),
    FOREIGN KEY (site_code)        REFERENCES ImportSite(site_code)  ON UPDATE CASCADE,
    FOREIGN KEY (merchandise_code) REFERENCES Merchandise(merchandise_code) ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 5. BẢNG SiteTransportInfo
-- ============================================================
CREATE TABLE SiteTransportInfo (
    transport_id INT AUTO_INCREMENT PRIMARY KEY,
    site_code    VARCHAR(10) NOT NULL,
    ship_days    TINYINT     NOT NULL,
    air_days     TINYINT     NOT NULL,
    other_info   TEXT,
    updated_date DATE        NOT NULL,
    FOREIGN KEY (site_code) REFERENCES ImportSite(site_code) ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 6. BẢNG SalesRequest
-- ============================================================
CREATE TABLE SalesRequest (
    request_id   INT AUTO_INCREMENT PRIMARY KEY,
    created_by   INT  NOT NULL,
    created_date DATE NOT NULL,
    status       ENUM('pending','processing','completed','error') NOT NULL DEFAULT 'pending',
    FOREIGN KEY (created_by) REFERENCES User(user_id)
) ENGINE=InnoDB;

-- ============================================================
-- 7. BẢNG SalesRequestItem
-- ============================================================
CREATE TABLE SalesRequestItem (
    item_id               INT AUTO_INCREMENT PRIMARY KEY,
    request_id            INT         NOT NULL,
    merchandise_code      VARCHAR(20) NOT NULL,
    quantity_ordered      INT         NOT NULL,
    unit                  VARCHAR(20) NOT NULL,
    desired_delivery_date DATE        NOT NULL,
    FOREIGN KEY (request_id)       REFERENCES SalesRequest(request_id) ON DELETE CASCADE,
    FOREIGN KEY (merchandise_code) REFERENCES Merchandise(merchandise_code)
) ENGINE=InnoDB;

-- ============================================================
-- 8. BẢNG PurchaseOrder
-- ============================================================
CREATE TABLE PurchaseOrder (
    order_id       INT AUTO_INCREMENT PRIMARY KEY,
    request_id     INT         NOT NULL,
    site_code      VARCHAR(10) NOT NULL,
    created_by     INT         NOT NULL,
    order_date     DATE        NOT NULL,
    delivery_means ENUM('ship delivery','air delivery') NOT NULL,
    status         ENUM('draft','sent','confirmed','delivered','cancelled') NOT NULL DEFAULT 'draft',
    FOREIGN KEY (request_id) REFERENCES SalesRequest(request_id),
    FOREIGN KEY (site_code)  REFERENCES ImportSite(site_code),
    FOREIGN KEY (created_by) REFERENCES User(user_id)
) ENGINE=InnoDB;

-- ============================================================
-- 9. BẢNG PurchaseOrderItem
-- ============================================================
CREATE TABLE PurchaseOrderItem (
    order_item_id    INT AUTO_INCREMENT PRIMARY KEY,
    order_id         INT         NOT NULL,
    merchandise_code VARCHAR(20) NOT NULL,
    quantity_ordered INT         NOT NULL,
    unit             VARCHAR(20) NOT NULL,
    FOREIGN KEY (order_id)         REFERENCES PurchaseOrder(order_id) ON DELETE CASCADE,
    FOREIGN KEY (merchandise_code) REFERENCES Merchandise(merchandise_code)
) ENGINE=InnoDB;