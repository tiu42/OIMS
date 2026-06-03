-- ============================================================
-- DỮ LIỆU MẪU
-- ============================================================

-- ------------------------------------------------------------
-- User (15 bản ghi — admin, sales, overseas_order)
-- ------------------------------------------------------------
INSERT INTO User (username, password_hash, full_name, email, role, created_date, is_active) VALUES
('admin01',       '12345678',   N'Nguyễn Văn An',       'an.nguyen@glocerimex.vn',      'admin',          '2023-01-10', 1),
('admin02',       '12345678',   N'Trần Thị Bích',       'bich.tran@glocerimex.vn',      'admin',          '2023-01-10', 1),
('sales01',       '12345678',   N'Lê Minh Châu',        'chau.le@glocerimex.vn',        'sales',          '2023-02-01', 1),
('sales02',       '12345678',   N'Phạm Thị Dung',       'dung.pham@glocerimex.vn',      'sales',          '2023-02-01', 1),
('sales03',       '12345678',   N'Hoàng Văn Đức',       'duc.hoang@glocerimex.vn',      'sales',          '2023-03-15', 1),
('sales04',       '12345678',   N'Vũ Thị Hà',           'ha.vu@glocerimex.vn',          'sales',          '2023-03-15', 1),
('sales05',       '12345678',   N'Đỗ Quang Hải',        'hai.do@glocerimex.vn',         'sales',          '2023-04-01', 1),
('sales06',       '12345678',   N'Ngô Thị Hương',       'huong.ngo@glocerimex.vn',      'sales',          '2023-04-01', 0),
('warehouse01',	  '12345678',   N'Lê Minh Hiếu',       	'hieu.le@glocerimex.vn',      	'warehouse',      '2023-01-10', 1),
('warehouse02',   '12345678',   N'Nguyễn Thùy Dung',    'dung.nguyen@glocerimex.vn', 	'warehouse',      '2023-01-10', 1),
('overseas01',    '12345678',    N'Bùi Thanh Hùng',      'hung.bui@glocerimex.vn',       'overseas_order', '2023-01-15', 1),
('overseas02',    '12345678',    N'Đinh Thị Lan',        'lan.dinh@glocerimex.vn',       'overseas_order', '2023-01-15', 1),
('overseas03',    '12345678',    N'Lý Văn Long',         'long.ly@glocerimex.vn',        'overseas_order', '2023-02-20', 1),
('overseas04',    '12345678',    N'Trịnh Thị Mai',       'mai.trinh@glocerimex.vn',      'overseas_order', '2023-02-20', 1),
('overseas05',    '12345678',    N'Phan Văn Nam',        'nam.phan@glocerimex.vn',       'overseas_order', '2023-05-01', 1),
('overseas06',    '12345678',    N'Tạ Thị Nga',          'nga.ta@glocerimex.vn',         'overseas_order', '2023-05-01', 1),
('overseas07',    '12345678',    N'Cao Văn Phúc',        'phuc.cao@glocerimex.vn',       'overseas_order', '2023-06-10', 0);


-- ------------------------------------------------------------
-- Merchandise (50 bản ghi)
-- ------------------------------------------------------------
INSERT INTO Merchandise (merchandise_code, merchandise_name, default_unit) VALUES
('MH001', N'Rượu vang đỏ Pháp',            'chai'),
('MH002', N'Rượu whisky Scotland',           'chai'),
('MH003', N'Bia nhập khẩu Đức',             'thùng'),
('MH004', N'Dầu ô liu nguyên chất Ý',       'lít'),
('MH005', N'Pho mát Gouda Hà Lan',          'kg'),
('MH006', N'Sô cô la đen Bỉ',              'hộp'),
('MH007', N'Cà phê Arabica Ethiopia',       'kg'),
('MH008', N'Trà Darjeeling Ấn Độ',         'hộp'),
('MH009', N'Mật ong Manuka New Zealand',    'hũ'),
('MH010', N'Bơ lạt Úc',                    'kg'),
('MH011', N'Nước khoáng Evian Pháp',        'thùng'),
('MH012', N'Nước ngọt tăng lực Monster Mỹ','thùng'),
('MH013', N'Sữa hạnh nhân Califia Mỹ',     'thùng'),
('MH014', N'Đường mía hữu cơ Brazil',       'kg'),
('MH015', N'Gạo Jasmine Thái Lan',          'kg'),
('MH016', N'Mì ống Barilla Ý',             'thùng'),
('MH017', N'Xúc xích hun khói Đức',        'kg'),
('MH018', N'Cá hồi hun khói Na Uy',        'kg'),
('MH019', N'Tôm đông lạnh Ecuador',        'kg'),
('MH020', N'Mực khô Hàn Quốc',            'kg'),
('MH021', N'Nấm linh chi Nhật Bản',        'hộp'),
('MH022', N'Yến sào Thái Lan',             'hộp'),
('MH023', N'Nhân sâm Hàn Quốc',           'hộp'),
('MH024', N'Vitamin C Blackmores Úc',       'hộp'),
('MH025', N'Omega 3 Nature Made Mỹ',        'hộp'),
('MH026', N'Kem dưỡng da Nivea Đức',       'hộp'),
('MH027', N'Nước hoa Chanel No.5 Pháp',    'chai'),
('MH028', N'Son môi MAC Canada',            'cái'),
('MH029', N'Kem chống nắng Anessa Nhật',   'hộp'),
('MH030', N'Dầu gội Pantene Mỹ',           'thùng'),
('MH031', N'Máy lọc không khí Xiaomi TQ',  'cái'),
('MH032', N'Loa bluetooth JBL Mỹ',         'cái'),
('MH033', N'Tai nghe Sony Nhật Bản',       'cái'),
('MH034', N'Đồng hồ Casio Nhật Bản',      'cái'),
('MH035', N'Bút máy Parker Anh',           'cái'),
('MH036', N'Vali kéo Samsonite Bỉ',        'cái'),
('MH037', N'Giày thể thao Nike Mỹ',        'đôi'),
('MH038', N'Áo khoác North Face Mỹ',       'cái'),
('MH039', N'Kính râm Ray-Ban Ý',           'cái'),
('MH040', N'Ví da Fossil Mỹ',              'cái'),
('MH041', N'Bộ dao nhà bếp Victorinox TL', 'bộ'),
('MH042', N'Nồi sứ Le Creuset Pháp',       'cái'),
('MH043', N'Máy pha cà phê DeLonghi Ý',    'cái'),
('MH044', N'Chảo chống dính Tefal Pháp',   'cái'),
('MH045', N'Bình giữ nhiệt Stanley Mỹ',    'cái'),
('MH046', N'Đồ chơi Lego Đan Mạch',        'hộp'),
('MH047', N'Sách giáo dục Pearson Anh',    'cuốn'),
('MH048', N'Vở học sinh Moleskine Ý',      'cuốn'),
('MH049', N'Mực in HP Mỹ',                 'hộp'),
('MH050', N'Giấy in A4 Double A Thái Lan', 'thùng');


-- ------------------------------------------------------------
-- ImportSite (20 bản ghi — đủ để tạo 50 SiteMerchandise & transport)
-- ------------------------------------------------------------
INSERT INTO ImportSite (site_code, site_name, country, contact_info) VALUES
('SITE01', N'Paris Luxury Imports',          N'Pháp',        'paris.luxury@plimports.fr'),
('SITE02', N'Rhine Valley Trading',          N'Đức',         'contact@rhinevalley.de'),
('SITE03', N'Amsterdam Global Supply',       N'Hà Lan',      'info@amsglobal.nl'),
('SITE04', N'Rome Fine Foods',               N'Ý',           'orders@romefinefood.it'),
('SITE05', N'Tokyo Premium Goods',           N'Nhật Bản',    'tokyo.premium@tpg.co.jp'),
('SITE06', N'Seoul K-Trade',                 N'Hàn Quốc',   'ktrade@seoulexport.kr'),
('SITE07', N'Sydney Pacific Exports',        N'Úc',          'export@sydneypacific.com.au'),
('SITE08', N'New York World Import',         N'Mỹ',          'nyc@worldimport.com'),
('SITE09', N'London Heritage Trade',         N'Anh',         'trade@londonheritage.co.uk'),
('SITE10', N'Bangkok Siam Trading',          N'Thái Lan',    'siam@bangkoktrading.th'),
('SITE11', N'Shanghai Orient Supplies',      N'Trung Quốc',  'orient@shanghaisupply.cn'),
('SITE12', N'Mumbai Spice Route',            N'Ấn Độ',       'spice@mumbairoute.in'),
('SITE13', N'São Paulo Tropical Goods',      N'Brazil',      'tropical@saopaulogoods.br'),
('SITE14', N'Oslo Nordic Import',            N'Na Uy',       'nordic@osloimport.no'),
('SITE15', N'Wellington South Pacific',      N'New Zealand', 'southpacific@wellington.nz'),
('SITE16', N'Brussels Diamond Trade',        N'Bỉ',          'diamond@brusselstrade.be'),
('SITE17', N'Toronto Maple Imports',         N'Canada',      'maple@torontoimports.ca'),
('SITE18', N'Copenhagen Nordic Style',       N'Đan Mạch',    'style@copenhagennordic.dk'),
('SITE19', N'Quito Andean Exports',          N'Ecuador',     'andean@quitoexports.ec'),
('SITE20', N'Zurich Swiss Precision',        N'Thụy Sĩ',     'precision@zurichswiss.ch');


-- ------------------------------------------------------------
-- SiteMerchandise (50 bản ghi)
-- ------------------------------------------------------------
INSERT INTO SiteMerchandise (site_code, merchandise_code, in_stock_quantity, unit, stock_updated_date) VALUES
('SITE01','MH001',  850, 'chai',   '2026-05-10'),
('SITE01','MH027',  320, 'chai',   '2026-05-12'),
('SITE01','MH042',  140, 'cái',    '2026-05-01'),
('SITE02','MH002',  500, 'chai',   '2026-05-08'),
('SITE02','MH003', 1200, 'thùng',  '2026-05-15'),
('SITE02','MH017',  670, 'kg',     '2026-05-14'),
('SITE02','MH026',  900, 'hộp',    '2026-05-11'),
('SITE03','MH005',  430, 'kg',     '2026-05-09'),
('SITE03','MH011',  780, 'thùng',  '2026-05-13'),
('SITE03','MH036',  210, 'cái',    '2026-05-05'),
('SITE04','MH004',  560, 'lít',    '2026-05-10'),
('SITE04','MH016',  990, 'thùng',  '2026-05-16'),
('SITE04','MH039',  175, 'cái',    '2026-05-07'),
('SITE04','MH043',   88, 'cái',    '2026-05-03'),
('SITE05','MH021',  340, 'hộp',    '2026-05-12'),
('SITE05','MH029',  650, 'hộp',    '2026-05-15'),
('SITE05','MH033',  280, 'cái',    '2026-05-14'),
('SITE05','MH034',  195, 'cái',    '2026-05-10'),
('SITE06','MH020',  410, 'kg',     '2026-05-08'),
('SITE06','MH023',  230, 'hộp',    '2026-05-11'),
('SITE07','MH009',  520, 'hũ',     '2026-05-09'),
('SITE07','MH010',  380, 'kg',     '2026-05-13'),
('SITE07','MH024',  610, 'hộp',    '2026-05-16'),
('SITE08','MH012', 1500, 'thùng',  '2026-05-15'),
('SITE08','MH013',  840, 'thùng',  '2026-05-14'),
('SITE08','MH025',  720, 'hộp',    '2026-05-12'),
('SITE08','MH030',  930, 'thùng',  '2026-05-11'),
('SITE08','MH032',  310, 'cái',    '2026-05-10'),
('SITE08','MH037',  460, 'đôi',    '2026-05-09'),
('SITE08','MH038',  290, 'cái',    '2026-05-08'),
('SITE08','MH040',  175, 'cái',    '2026-05-07'),
('SITE08','MH045',  540, 'cái',    '2026-05-06'),
('SITE08','MH049',  820, 'hộp',    '2026-05-05'),
('SITE09','MH035',  260, 'cái',    '2026-05-10'),
('SITE09','MH047',  700, 'cuốn',   '2026-05-13'),
('SITE10','MH015', 2000, 'kg',     '2026-05-15'),
('SITE10','MH050',  650, 'thùng',  '2026-05-14'),
('SITE11','MH031',  480, 'cái',    '2026-05-12'),
('SITE12','MH008',  590, 'hộp',    '2026-05-11'),
('SITE13','MH014', 1800, 'kg',     '2026-05-10'),
('SITE14','MH018',  370, 'kg',     '2026-05-09'),
('SITE15','MH009',  440, 'hũ',     '2026-05-08'),
('SITE16','MH006',  960, 'hộp',    '2026-05-16'),
('SITE16','MH036',  130, 'cái',    '2026-05-15'),
('SITE17','MH028',  850, 'cái',    '2026-05-14'),
('SITE18','MH046', 1100, 'hộp',    '2026-05-13'),
('SITE19','MH019',  730, 'kg',     '2026-05-12'),
('SITE20','MH041',  240, 'bộ',     '2026-05-11'),
('SITE01','MH044',  310, 'cái',    '2026-05-10'),
('SITE05','MH048',  420, 'cuốn',   '2026-05-09');


-- ------------------------------------------------------------
-- SiteTransportInfo (20 bản ghi — 1 bản ghi / site)
-- ------------------------------------------------------------
INSERT INTO SiteTransportInfo (site_code, ship_days, air_days, other_info, updated_date) VALUES
('SITE01', 28, 3,  N'Xuất phát từ cảng Le Havre, hàng không từ CDG',              '2026-01-05'),
('SITE02', 32, 4,  N'Cảng Hamburg, hàng không Frankfurt',                          '2026-01-10'),
('SITE03', 30, 3,  N'Cảng Rotterdam, hàng không Amsterdam Schiphol',               '2026-01-08'),
('SITE04', 35, 4,  N'Cảng Genova, hàng không Roma Fiumicino',                      '2026-01-12'),
('SITE05', 18, 2,  N'Cảng Yokohama, hàng không Narita',                            '2026-02-01'),
('SITE06', 10, 1,  N'Cảng Busan, hàng không Incheon',                              '2026-02-05'),
('SITE07', 22, 3,  N'Cảng Sydney, hàng không Kingsford Smith',                     '2026-01-20'),
('SITE08', 25, 3,  N'Cảng Los Angeles / New York, hàng không JFK',                 '2026-01-15'),
('SITE09', 30, 4,  N'Cảng Felixstowe, hàng không Heathrow',                        '2026-01-18'),
('SITE10',  4, 1,  N'Cảng Laem Chabang, hàng không Suvarnabhumi',                  '2026-02-10'),
('SITE11',  7, 1,  N'Cảng Thượng Hải, hàng không Pudong',                          '2026-02-15'),
('SITE12', 15, 2,  N'Cảng Nhava Sheva, hàng không Mumbai',                         '2026-03-01'),
('SITE13', 40, 5,  N'Cảng Santos, hàng không Guarulhos',                           '2026-01-25'),
('SITE14', 26, 3,  N'Cảng Oslo, hàng không Gardermoen',                            '2026-02-20'),
('SITE15', 24, 3,  N'Cảng Auckland, hàng không Wellington',                        '2026-02-25'),
('SITE16', 29, 3,  N'Cảng Antwerp, hàng không Bruxelles',                          '2026-03-05'),
('SITE17', 27, 4,  N'Cảng Vancouver, hàng không Toronto Pearson',                  '2026-03-10'),
('SITE18', 31, 4,  N'Cảng Aarhus, hàng không Copenhagen Kastrup',                  '2026-03-15'),
('SITE19', 38, 5,  N'Cảng Guayaquil, hàng không Quito Mariscal Sucre',             '2026-03-20'),
('SITE20', 33, 3,  N'Cảng Basel–Mulhouse, hàng không Zürich',                      '2026-04-01');


-- ------------------------------------------------------------
-- SalesRequest (15 bản ghi)
-- ------------------------------------------------------------
INSERT INTO SalesRequest (created_by, created_date, status) VALUES
(3,  '2026-01-08', 'completed'),
(4,  '2026-01-15', 'completed'),
(3,  '2026-01-22', 'completed'),
(5,  '2026-02-03', 'completed'),
(6,  '2026-02-10', 'completed'),
(7,  '2026-02-18', 'completed'),
(4,  '2026-03-01', 'completed'),
(3,  '2026-03-10', 'processing'),
(5,  '2026-03-20', 'processing'),
(6,  '2026-04-02', 'pending'),
(7,  '2026-04-15', 'pending'),
(4,  '2026-04-25', 'error'),
(3,  '2026-05-05', 'pending'),
(5,  '2026-05-10', 'pending'),
(6,  '2026-05-18', 'pending');


-- ------------------------------------------------------------
-- SalesRequestItem (50 bản ghi)
-- ------------------------------------------------------------
INSERT INTO SalesRequestItem (request_id, merchandise_code, quantity_ordered, unit, desired_delivery_date) VALUES
-- Request 1
(1, 'MH001', 100, 'chai',   '2026-02-15'),
(1, 'MH004',  50, 'lít',    '2026-02-15'),
(1, 'MH016', 200, 'thùng',  '2026-02-20'),
-- Request 2
(2, 'MH003', 300, 'thùng',  '2026-02-28'),
(2, 'MH017', 150, 'kg',     '2026-02-28'),
-- Request 3
(3, 'MH005',  80, 'kg',     '2026-03-10'),
(3, 'MH006', 120, 'hộp',    '2026-03-10'),
(3, 'MH009',  60, 'hũ',     '2026-03-15'),
-- Request 4
(4, 'MH018',  90, 'kg',     '2026-03-20'),
(4, 'MH019', 200, 'kg',     '2026-03-20'),
(4, 'MH020', 100, 'kg',     '2026-03-25'),
-- Request 5
(5, 'MH021',  50, 'hộp',    '2026-03-28'),
(5, 'MH023',  40, 'hộp',    '2026-03-28'),
(5, 'MH024', 150, 'hộp',    '2026-04-01'),
-- Request 6
(6, 'MH029', 200, 'hộp',    '2026-04-05'),
(6, 'MH033',  30, 'cái',    '2026-04-10'),
(6, 'MH034',  25, 'cái',    '2026-04-10'),
-- Request 7
(7, 'MH037', 100, 'đôi',    '2026-04-20'),
(7, 'MH038',  60, 'cái',    '2026-04-20'),
(7, 'MH039',  45, 'cái',    '2026-04-25'),
-- Request 8
(8, 'MH007', 300, 'kg',     '2026-05-01'),
(8, 'MH008', 200, 'hộp',    '2026-05-01'),
(8, 'MH015', 500, 'kg',     '2026-05-05'),
-- Request 9
(9, 'MH025', 180, 'hộp',    '2026-05-10'),
(9, 'MH026', 250, 'hộp',    '2026-05-10'),
(9, 'MH030', 100, 'thùng',  '2026-05-15'),
-- Request 10
(10,'MH031',  20, 'cái',    '2026-05-20'),
(10,'MH032',  35, 'cái',    '2026-05-20'),
(10,'MH043',  10, 'cái',    '2026-05-25'),
-- Request 11
(11,'MH041',  50, 'bộ',     '2026-06-01'),
(11,'MH042',  30, 'cái',    '2026-06-01'),
(11,'MH044',  40, 'cái',    '2026-06-05'),
-- Request 12
(12,'MH046', 200, 'hộp',    '2026-06-10'),
(12,'MH047', 300, 'cuốn',   '2026-06-10'),
-- Request 13
(13,'MH011', 400, 'thùng',  '2026-06-15'),
(13,'MH012', 350, 'thùng',  '2026-06-15'),
(13,'MH013', 200, 'thùng',  '2026-06-20'),
-- Request 14
(14,'MH014', 500, 'kg',     '2026-06-25'),
(14,'MH050', 300, 'thùng',  '2026-06-25'),
-- Request 15
(15,'MH027',  80, 'chai',   '2026-07-01'),
(15,'MH028', 120, 'cái',    '2026-07-01'),
(15,'MH035',  60, 'cái',    '2026-07-05'),
(15,'MH036',  25, 'cái',    '2026-07-05'),
(15,'MH040',  50, 'cái',    '2026-07-05'),
(15,'MH045', 100, 'cái',    '2026-07-10'),
(15,'MH048', 150, 'cuốn',   '2026-07-10'),
(15,'MH049', 200, 'hộp',    '2026-07-10'),
(15,'MH022',  30, 'hộp',    '2026-07-10'),
(15,'MH010',  80, 'kg',     '2026-07-10');


-- ------------------------------------------------------------
-- PurchaseOrder (23 bản ghi)
-- ------------------------------------------------------------
INSERT INTO PurchaseOrder (request_id, site_code, created_by, order_date, delivery_means, status) VALUES
(1,  'SITE01', 9,  '2026-01-09', 'ship delivery', 'delivered'),
(1,  'SITE04', 9,  '2026-01-09', 'ship delivery', 'delivered'),
(2,  'SITE02', 10, '2026-01-16', 'ship delivery', 'delivered'),
(3,  'SITE03', 9,  '2026-01-23', 'ship delivery', 'delivered'),
(3,  'SITE16', 9,  '2026-01-23', 'air delivery',  'delivered'),
(3,  'SITE15', 10, '2026-01-23', 'air delivery',  'delivered'),
(4,  'SITE14', 11, '2026-02-04', 'ship delivery', 'delivered'),
(4,  'SITE19', 11, '2026-02-04', 'ship delivery', 'delivered'),
(5,  'SITE05', 12, '2026-02-11', 'air delivery',  'delivered'),
(5,  'SITE06', 12, '2026-02-11', 'air delivery',  'delivered'),
(6,  'SITE05', 9,  '2026-02-19', 'air delivery',  'delivered'),
(7,  'SITE08', 10, '2026-03-02', 'ship delivery', 'delivered'),
(8,  'SITE10', 11, '2026-03-11', 'ship delivery', 'confirmed'),
(8,  'SITE12', 11, '2026-03-11', 'air delivery',  'confirmed'),
(9,  'SITE08', 12, '2026-03-21', 'ship delivery', 'sent'),
(9,  'SITE07', 12, '2026-03-21', 'air delivery',  'sent'),
(10, 'SITE11', 9,  '2026-04-03', 'air delivery',  'draft'),
(11, 'SITE20', 10, '2026-04-16', 'ship delivery', 'draft'),
(11, 'SITE01', 10, '2026-04-16', 'ship delivery', 'draft'),
(13, 'SITE08', 13, '2026-05-06', 'ship delivery', 'draft'),
(10, 'SITE11', 9,  '2026-04-05', 'air delivery',  'cancelled'),
(11, 'SITE20', 10, '2026-04-18', 'ship delivery', 'cancelled'),
(13, 'SITE08', 13, '2026-05-08', 'ship delivery', 'cancelled');


-- ------------------------------------------------------------
-- PurchaseOrderItem (56 bản ghi)
-- ------------------------------------------------------------
INSERT INTO PurchaseOrderItem (order_id, merchandise_code, quantity_ordered, unit) VALUES
-- Order 1 (SITE01 — MH001, MH027 cho Request 1)
(1,  'MH001', 100, 'chai'),
-- Order 2 (SITE04 — MH004, MH016 cho Request 1)
(2,  'MH004',  50, 'lít'),
(2,  'MH016', 200, 'thùng'),
-- Order 3 (SITE02 — MH003, MH017 cho Request 2)
(3,  'MH003', 300, 'thùng'),
(3,  'MH017', 150, 'kg'),
-- Order 4 (SITE03 — MH005 cho Request 3)
(4,  'MH005',  80, 'kg'),
-- Order 5 (SITE16 — MH006 cho Request 3)
(5,  'MH006', 120, 'hộp'),
-- Order 6 (SITE15 — MH009 cho Request 3)
(6,  'MH009',  60, 'hũ'),
-- Order 7 (SITE14 — MH018 cho Request 4)
(7,  'MH018',  90, 'kg'),
-- Order 8 (SITE19 — MH019 cho Request 4)
(8,  'MH019', 200, 'kg'),
(8,  'MH020', 100, 'kg'),
-- Order 9 (SITE05 — MH021 cho Request 5)
(9,  'MH021',  50, 'hộp'),
-- Order 10 (SITE06 — MH023 cho Request 5)
(10, 'MH023',  40, 'hộp'),
(10, 'MH024', 150, 'hộp'),
-- Order 11 (SITE05 — MH029, MH033, MH034 cho Request 6)
(11, 'MH029', 200, 'hộp'),
(11, 'MH033',  30, 'cái'),
(11, 'MH034',  25, 'cái'),
-- Order 12 (SITE08 — MH037, MH038, MH039 cho Request 7)
(12, 'MH037', 100, 'đôi'),
(12, 'MH038',  60, 'cái'),
(12, 'MH039',  45, 'cái'),
-- Order 13 (SITE10 — MH015 cho Request 8)
(13, 'MH015', 500, 'kg'),
-- Order 14 (SITE12 — MH008 cho Request 8)
(14, 'MH008', 200, 'hộp'),
-- Order 15 (SITE08 — MH025, MH030 cho Request 9)
(15, 'MH025', 180, 'hộp'),
(15, 'MH030', 100, 'thùng'),
-- Order 16 (SITE07 — MH026 cho Request 9)
(16, 'MH026', 250, 'hộp'),
-- Order 17 (SITE11 — MH031, MH032 cho Request 10)
(17, 'MH031',  20, 'cái'),
(17, 'MH032',  35, 'cái'),
(17, 'MH043',  10, 'cái'),
-- Order 18 (SITE20 — MH041 cho Request 11)
(18, 'MH041',  50, 'bộ'),
(18, 'MH044',  40, 'cái'),
-- Order 19 (SITE01 — MH042 cho Request 11)
(19, 'MH042',  30, 'cái'),
-- Order 20 (SITE08 — MH011,MH012,MH013 cho Request 13)
(20, 'MH011', 400, 'thùng'),
(20, 'MH012', 350, 'thùng'),
(20, 'MH013', 200, 'thùng'),
-- Bổ sung để đủ 50 bản ghi
(1,  'MH027',  50, 'chai'),
(4,  'MH009',  30, 'hũ'),
(5,  'MH007', 100, 'kg'),
(7,  'MH019',  80, 'kg'),
(9,  'MH022',  20, 'hộp'),
(10, 'MH020',  60, 'kg'),
(11, 'MH048', 100, 'cuốn'),
(12, 'MH045',  50, 'cái'),
(13, 'MH050', 200, 'thùng'),
(14, 'MH014', 300, 'kg'),
(15, 'MH025',  80, 'hộp'),
(16, 'MH024',  70, 'hộp'),
(17, 'MH035',  40, 'cái'),
(18, 'MH046', 120, 'hộp'),
(19, 'MH036',  15, 'cái'),
(20, 'MH049', 150, 'hộp'),
(21, 'MH031',  10, 'cái'),
(21, 'MH032',  15, 'cái'),
(22, 'MH041',  20, 'bộ'),
(22, 'MH044',  25, 'cái'),
(23, 'MH011',  150, 'thùng'),
(23, 'MH012',  100, 'thùng');

-- ============================================================
-- Kiểm tra nhanh
-- ============================================================
SELECT 'User'             AS bang, COUNT(*) AS so_ban_ghi FROM User
UNION ALL
SELECT 'Merchandise',               COUNT(*) FROM Merchandise
UNION ALL
SELECT 'ImportSite',                COUNT(*) FROM ImportSite
UNION ALL
SELECT 'SiteMerchandise',           COUNT(*) FROM SiteMerchandise
UNION ALL
SELECT 'SiteTransportInfo',         COUNT(*) FROM SiteTransportInfo
UNION ALL
SELECT 'SalesRequest',              COUNT(*) FROM SalesRequest
UNION ALL
SELECT 'SalesRequestItem',          COUNT(*) FROM SalesRequestItem
UNION ALL
SELECT 'PurchaseOrder',             COUNT(*) FROM PurchaseOrder
UNION ALL
SELECT 'PurchaseOrderItem',         COUNT(*) FROM PurchaseOrderItem;	