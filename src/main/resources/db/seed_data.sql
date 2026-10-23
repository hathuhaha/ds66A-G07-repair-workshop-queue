-- =============================================================================
-- seed_data.sql - Dữ liệu mẫu cho demo (contract.md mục 10)
-- Chạy SAU schema.sql. Chạy lại nhiều lần được (xóa sạch dữ liệu rồi nạp lại).
--
-- Giả định "hôm nay" là 2026-10-23. created_at được gán cụ thể cho từng đơn
-- để thứ tự hàng đợi (theo hạn chót) luôn cố định mỗi lần demo.
--
-- Bản đồ đơn:
--   #1 REPAIRING             hạn chót 24/10  -> Demo 3: thử đóng, phải báo lỗi
--   #2 WAITING_FOR_APPROVAL  hạn chót 23/10  -> Demo 2: nhánh DUYỆT báo giá
--   #3 RECEIVED              hạn chót 26/10
--   #4 READY                 hạn chót 25/10  -> Demo 3: đóng thành công
--   #5 DIAGNOSING            hạn chót 26/10  (cùng hạn chót với #3, nhận trước)
--   #6 CLOSED                (báo giá bị từ chối)
--   #7 WAITING_FOR_APPROVAL  hạn chót 28/10  -> Demo 2: nhánh TỪ CHỐI báo giá
-- #2 nhận sau #1 nhưng hạn chót sớm hơn: để thấy xếp theo hạn chót khác xếp
-- theo số ngày.
-- =============================================================================

-- Xóa sạch dữ liệu cũ và đặt lại bộ đếm id
TRUNCATE TABLE status_history, labor_services, parts, estimates,
               repair_jobs, devices, customers
    RESTART IDENTITY;

-- 1. Khách hàng (có người để trống email/địa chỉ để thử field nullable)
INSERT INTO customers (id, full_name, phone, email, address) VALUES
    (1, 'Nguyễn Văn An',  '0901234567', 'an.nguyen@example.com', '12 Láng Hạ, Hà Nội'),
    (2, 'Trần Thị Bình',  '0912345678', NULL,                    '45 Cầu Giấy, Hà Nội'),
    (3, 'Lê Hoàng Cường', '0923456789', 'cuong.le@example.com',  NULL);

-- 2. Thiết bị
INSERT INTO devices (id, customer_id, device_type, brand, model, serial_number) VALUES
    (1, 1, 'Điều hòa', 'Daikin',     'FTKA25',       'DK-0001'),
    (2, 1, 'Máy giặt', 'Samsung',    'WW90T',        NULL),
    (3, 2, 'Tủ lạnh',  'Panasonic',  'NR-BX410',     'PN-7788'),
    (4, 2, 'Điều hòa', 'LG',         'V10ENW',       'LG-2031'),
    (5, 3, 'Laptop',   'Dell',       'Inspiron 15',  'DL-5521'),
    (6, 3, 'Điều hòa', 'Mitsubishi', NULL,           NULL);

-- 3. Đơn sửa chữa
INSERT INTO repair_jobs
    (id, device_id, issue_description, status, desired_days,
     estimated_ready_at, closed_at, created_at, updated_at) VALUES
    (1, 1, 'Điều hòa chạy nhưng không mát',
        'REPAIRING', 5, '2026-10-24 17:00', NULL,
        '2026-10-19 09:00', '2026-10-20 16:00'),
    (2, 3, 'Tủ lạnh không làm lạnh ngăn đông',
        'WAITING_FOR_APPROVAL', 3, '2026-10-25 17:00', NULL,
        '2026-10-20 09:00', '2026-10-20 15:00'),
    (3, 2, 'Máy giặt rung mạnh khi vắt',
        'RECEIVED', 3, NULL, NULL,
        '2026-10-23 09:00', '2026-10-23 09:00'),
    (4, 4, 'Dàn lạnh kêu to, quạt không quay',
        'READY', 7, '2026-10-22 10:00', NULL,
        '2026-10-18 09:00', '2026-10-21 16:00'),
    (5, 5, 'Laptop không lên nguồn',
        'DIAGNOSING', 5, NULL, NULL,
        '2026-10-21 09:00', '2026-10-21 14:00'),
    (6, 1, 'Điều hòa không khởi động được',
        'CLOSED', 2, NULL, '2026-10-17 15:00',
        '2026-10-15 09:00', '2026-10-17 15:00'),
    (7, 6, 'Điều hòa báo lỗi và tự ngắt',
        'WAITING_FOR_APPROVAL', 6, NULL, NULL,
        '2026-10-22 09:00', '2026-10-22 16:00');

-- 4. Báo giá (version 1 cho mỗi đơn có báo giá)
--    #1 và #4 đã được duyệt, #6 bị từ chối (nên đơn #6 đã CLOSED),
--    #2 và #7 còn DRAFT để demo duyệt / từ chối.
INSERT INTO estimates (id, job_id, version, status, note) VALUES
    (1, 1, 1, 'APPROVED', 'Thay tụ và nạp gas'),
    (2, 2, 1, 'DRAFT',    'Thay bo mạch và cảm biến'),
    (3, 4, 1, 'APPROVED', 'Thay quạt dàn lạnh'),
    (4, 6, 1, 'REJECTED', 'Thay block máy nén'),
    (5, 7, 1, 'DRAFT',    'Thay cảm biến');

-- 5. Dòng linh kiện
INSERT INTO parts (id, estimate_id, name, unit_price, quantity) VALUES
    (1, 1, 'Tụ điện máy nén',        180000.00, 1),
    (2, 1, 'Gas R32 (nạp bổ sung)',  250000.00, 1),
    (3, 2, 'Bo mạch điều khiển',     950000.00, 1),
    (4, 2, 'Cảm biến nhiệt',         120000.00, 2),
    (5, 3, 'Quạt dàn lạnh',          420000.00, 1),
    (6, 4, 'Block máy nén',         3200000.00, 1),
    (7, 5, 'Cảm biến dàn nóng',      300000.00, 1);

-- 6. Dòng công thợ
INSERT INTO labor_services (id, estimate_id, description, hours, hourly_rate) VALUES
    (1, 1, 'Công thay tụ và nạp gas',             1.50, 200000.00),
    (2, 2, 'Công thay bo mạch',                   2.00, 200000.00),
    (3, 3, 'Công thay quạt',                      1.00, 200000.00),
    (4, 4, 'Công thay block máy nén',             3.00, 200000.00),
    (5, 5, 'Công kiểm tra và thay cảm biến',      1.00, 200000.00);

-- 7. Lịch sử trạng thái: mỗi đơn có chuỗi lịch sử đi từ lúc tiếp nhận tới
--    trạng thái hiện tại (dòng đầu có old_status = NULL).
INSERT INTO status_history (job_id, old_status, new_status, note, changed_at) VALUES
    -- Đơn #1 (REPAIRING)
    (1, NULL,                   'RECEIVED',             'Tiếp nhận thiết bị',          '2026-10-19 09:00'),
    (1, 'RECEIVED',             'DIAGNOSING',           'Bắt đầu chẩn đoán',           '2026-10-19 14:00'),
    (1, 'DIAGNOSING',           'WAITING_FOR_APPROVAL', 'Đã gửi báo giá',              '2026-10-20 10:00'),
    (1, 'WAITING_FOR_APPROVAL', 'REPAIRING',            'Khách đã duyệt báo giá',      '2026-10-20 16:00'),
    -- Đơn #2 (WAITING_FOR_APPROVAL)
    (2, NULL,                   'RECEIVED',             'Tiếp nhận thiết bị',          '2026-10-20 09:00'),
    (2, 'RECEIVED',             'DIAGNOSING',           'Bắt đầu chẩn đoán',           '2026-10-20 11:00'),
    (2, 'DIAGNOSING',           'WAITING_FOR_APPROVAL', 'Đã gửi báo giá',              '2026-10-20 15:00'),
    -- Đơn #3 (RECEIVED)
    (3, NULL,                   'RECEIVED',             'Tiếp nhận thiết bị',          '2026-10-23 09:00'),
    -- Đơn #4 (READY)
    (4, NULL,                   'RECEIVED',             'Tiếp nhận thiết bị',          '2026-10-18 09:00'),
    (4, 'RECEIVED',             'DIAGNOSING',           'Bắt đầu chẩn đoán',           '2026-10-18 11:00'),
    (4, 'DIAGNOSING',           'WAITING_FOR_APPROVAL', 'Đã gửi báo giá',              '2026-10-18 15:00'),
    (4, 'WAITING_FOR_APPROVAL', 'REPAIRING',            'Khách đã duyệt báo giá',      '2026-10-19 09:00'),
    (4, 'REPAIRING',            'READY',                'Sửa xong, chờ khách nhận',    '2026-10-21 16:00'),
    -- Đơn #5 (DIAGNOSING)
    (5, NULL,                   'RECEIVED',             'Tiếp nhận thiết bị',          '2026-10-21 09:00'),
    (5, 'RECEIVED',             'DIAGNOSING',           'Bắt đầu chẩn đoán',           '2026-10-21 14:00'),
    -- Đơn #6 (CLOSED sau khi khách từ chối báo giá)
    (6, NULL,                   'RECEIVED',             'Tiếp nhận thiết bị',          '2026-10-15 09:00'),
    (6, 'RECEIVED',             'DIAGNOSING',           'Bắt đầu chẩn đoán',           '2026-10-15 13:00'),
    (6, 'DIAGNOSING',           'WAITING_FOR_APPROVAL', 'Đã gửi báo giá',              '2026-10-16 09:00'),
    (6, 'WAITING_FOR_APPROVAL', 'CLOSED',               'Khách từ chối báo giá',       '2026-10-17 15:00'),
    -- Đơn #7 (WAITING_FOR_APPROVAL)
    (7, NULL,                   'RECEIVED',             'Tiếp nhận thiết bị',          '2026-10-22 09:00'),
    (7, 'RECEIVED',             'DIAGNOSING',           'Bắt đầu chẩn đoán',           '2026-10-22 11:00'),
    (7, 'DIAGNOSING',           'WAITING_FOR_APPROVAL', 'Đã gửi báo giá',              '2026-10-22 16:00');

-- 8. Đồng bộ bộ đếm id (vì ở trên đã chèn id cố định).
--    Thiếu bước này thì lần INSERT tiếp theo không chỉ định id sẽ trùng khóa.
SELECT setval(pg_get_serial_sequence('customers',       'id'), (SELECT MAX(id) FROM customers));
SELECT setval(pg_get_serial_sequence('devices',         'id'), (SELECT MAX(id) FROM devices));
SELECT setval(pg_get_serial_sequence('repair_jobs',     'id'), (SELECT MAX(id) FROM repair_jobs));
SELECT setval(pg_get_serial_sequence('estimates',       'id'), (SELECT MAX(id) FROM estimates));
SELECT setval(pg_get_serial_sequence('parts',           'id'), (SELECT MAX(id) FROM parts));
SELECT setval(pg_get_serial_sequence('labor_services',  'id'), (SELECT MAX(id) FROM labor_services));
SELECT setval(pg_get_serial_sequence('status_history',  'id'), (SELECT MAX(id) FROM status_history));
