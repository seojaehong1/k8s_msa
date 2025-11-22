-- 카테고리 데이터
INSERT INTO CATEGORIES (name, type, description) VALUES
('AMERICANO', 'COFFEE', '아메리카노'),
('LATTE', 'COFFEE', '라떼'),
('ADE', 'ADE', '에이드'),
('TEA', 'TEA', '차');

-- 기존 상품 데이터 (카테고리 없음)
INSERT INTO PRODUCTS (name, description, price, stock, preparation_time, category_id) VALUES
('노트북', '고성능 노트북', 1200000.0, 10, NULL, NULL),
('스마트폰', '최신형 스마트폰', 800000.0, 15, NULL, NULL),
('헤드폰', '무선 헤드폰', 300000.0, 20, NULL, NULL),
('태블릿', '10인치 태블릿', 500000.0, 12, NULL, NULL);

-- 커피 카테고리 상품 (COFFEE - AMERICANO, LATTE)
-- 아메리카노 (category_id = 1)
INSERT INTO PRODUCTS (name, description, price, stock, preparation_time, category_id) VALUES
('아메리카노', '진한 에스프레소와 물', 4500.0, 50, 3, 1),
('아이스 아메리카노', '차가운 아메리카노', 4500.0, 50, 4, 1),
('바닐라 아메리카노', '바닐라 시럽이 들어간 아메리카노', 5000.0, 50, 4, 1),
('헤이즐넛 아메리카노', '헤이즐넛 시럽이 들어간 아메리카노', 5000.0, 50, 4, 1),
('카라멜 아메리카노', '카라멜 시럽이 들어간 아메리카노', 5000.0, 50, 4, 1),
('콜드브루 아메리카노', '콜드브루 원액으로 만든 아메리카노', 5500.0, 50, 5, 1);

-- 라떼 (category_id = 2)
INSERT INTO PRODUCTS (name, description, price, stock, preparation_time, category_id) VALUES
('카페라떼', '에스프레소와 스팀 밀크', 5000.0, 50, 5, 2),
('바닐라라떼', '바닐라 시럽이 들어간 라떼', 5500.0, 50, 5, 2),
('카라멜라떼', '카라멜 시럽이 들어간 라떼', 5500.0, 50, 5, 2),
('헤이즐넛라떼', '헤이즐넛 시럽이 들어간 라떼', 5500.0, 50, 5, 2),
('카페모카', '초콜릿과 커피가 조화로운 라떼', 6000.0, 50, 6, 2),
('아이스 카페라떼', '차가운 카페라떼', 5000.0, 50, 4, 2),
('아이스 바닐라라떼', '차가운 바닐라라떼', 5500.0, 50, 4, 2);

-- 에이드 (category_id = 3)
INSERT INTO PRODUCTS (name, description, price, stock, preparation_time, category_id) VALUES
('레몬에이드', '상큼한 레몬 에이드', 5500.0, 50, 5, 3),
('라임에이드', '신선한 라임 에이드', 5500.0, 50, 5, 3),
('자몽에이드', '달콤한 자몽 에이드', 5500.0, 50, 5, 3),
('청포도에이드', '상큼한 청포도 에이드', 5500.0, 50, 5, 3),
('유자에이드', '달콤새콤한 유자 에이드', 5500.0, 50, 5, 3),
('복숭아에이드', '달콤한 복숭아 에이드', 5500.0, 50, 5, 3);

-- 차 (category_id = 4)
INSERT INTO PRODUCTS (name, description, price, stock, preparation_time, category_id) VALUES
('녹차', '우리나라 전통 녹차', 4500.0, 50, 6, 4),
('캐모마일', '진정 효과가 있는 허브차', 5000.0, 50, 7, 4),
('페퍼민트', '상쾌한 맛의 허브차', 5000.0, 50, 7, 4),
('얼그레이', '베르가못 향이 나는 홍차', 5000.0, 50, 6, 4),
('히비스커스', '신맛이 강한 허브차', 5000.0, 50, 6, 4),
('루이보스', '카페인이 없는 남아프리카 차', 5000.0, 50, 6, 4); 