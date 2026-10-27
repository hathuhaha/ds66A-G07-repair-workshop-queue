-- =============================================================================
-- schema.sql - Cơ sở dữ liệu air-workshop-queue (PostgreSQL)
-- Khớp với docs/contract.md (mục 2, 4, 5). Chạy lại nhiều lần được:
-- script xóa bảng cũ rồi tạo lại, nên chỉ chạy trên DB dev/test, không chạy
-- trên DB đang có dữ liệu cần giữ.
-- =============================================================================

-- 1. Xóa bảng cũ (bảng con trước, bảng cha sau).
--    CASCADE ở đây chỉ để xóa các đối tượng phụ thuộc, không liên quan đến
--    ON DELETE của khóa ngoại.
DROP TABLE IF EXISTS status_history  CASCADE;
DROP TABLE IF EXISTS labor_services  CASCADE;
DROP TABLE IF EXISTS parts           CASCADE;
DROP TABLE IF EXISTS estimates       CASCADE;
DROP TABLE IF EXISTS repair_jobs     CASCADE;
DROP TABLE IF EXISTS devices         CASCADE;
DROP TABLE IF EXISTS customers       CASCADE;

-- 2. customers (contract 4.1)
CREATE TABLE customers (
    id          SERIAL        PRIMARY KEY,
    full_name   VARCHAR(100)  NOT NULL,
    phone       VARCHAR(20)   NOT NULL,
    email       VARCHAR(100),
    address     VARCHAR(255),
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. devices (contract 4.2)
CREATE TABLE devices (
    id             SERIAL       PRIMARY KEY,
    customer_id    INT          NOT NULL
                   REFERENCES customers (id) ON DELETE RESTRICT,
    device_type    VARCHAR(50)  NOT NULL,
    brand          VARCHAR(50),
    model          VARCHAR(50),
    serial_number  VARCHAR(50),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. repair_jobs (contract 4.3)
--    - desired_days: số ngày khách mong muốn nhận hàng, tính từ created_at.
--      Hạn chót = created_at + desired_days (không lưu, tính khi truy vấn).
--    - estimated_ready_at: ước tính do nhân viên nhập tay, có thể NULL.
--    - chk_closed_at: closed_at có giá trị khi và chỉ khi status = 'CLOSED',
--      nên khi đóng đơn phải UPDATE status và closed_at trong CÙNG một câu lệnh.
CREATE TABLE repair_jobs (
    id                  SERIAL       PRIMARY KEY,
    device_id           INT          NOT NULL
                        REFERENCES devices (id) ON DELETE RESTRICT,
    issue_description   TEXT         NOT NULL,
    status              VARCHAR(30)  NOT NULL DEFAULT 'RECEIVED',
    desired_days        INT          NOT NULL,
    estimated_ready_at  TIMESTAMP,
    closed_at           TIMESTAMP,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_repair_jobs_status CHECK (status IN
        ('RECEIVED', 'DIAGNOSING', 'WAITING_FOR_APPROVAL',
         'REPAIRING', 'READY', 'CLOSED')),
    CONSTRAINT chk_repair_jobs_desired_days CHECK (desired_days > 0),
    CONSTRAINT chk_repair_jobs_closed_at CHECK
        ((status = 'CLOSED') = (closed_at IS NOT NULL))
);

-- 5. estimates (contract 4.4): quan hệ 1-N với repair_jobs, có version.
--    Tổng tiền không lưu ở đây, tính trong Java từ parts và labor_services.
CREATE TABLE estimates (
    id          SERIAL       PRIMARY KEY,
    job_id      INT          NOT NULL
                REFERENCES repair_jobs (id) ON DELETE RESTRICT,
    version     INT          NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    note        TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_estimates_status  CHECK (status IN ('DRAFT', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_estimates_version CHECK (version > 0),
    CONSTRAINT uq_estimates_job_version UNIQUE (job_id, version)
);

-- 6. parts (contract 4.5): dòng linh kiện của một estimate
CREATE TABLE parts (
    id           SERIAL         PRIMARY KEY,
    estimate_id  INT            NOT NULL
                 REFERENCES estimates (id) ON DELETE RESTRICT,
    name         VARCHAR(100)   NOT NULL,
    unit_price   NUMERIC(12,2)  NOT NULL,
    quantity     INT            NOT NULL,

    CONSTRAINT chk_parts_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_parts_quantity   CHECK (quantity > 0)
);

-- 7. labor_services (contract 4.6): dòng công thợ của một estimate
CREATE TABLE labor_services (
    id           SERIAL         PRIMARY KEY,
    estimate_id  INT            NOT NULL
                 REFERENCES estimates (id) ON DELETE RESTRICT,
    description  VARCHAR(200)   NOT NULL,
    hours        NUMERIC(6,2)   NOT NULL,
    hourly_rate  NUMERIC(12,2)  NOT NULL,

    CONSTRAINT chk_labor_hours       CHECK (hours > 0),
    CONSTRAINT chk_labor_hourly_rate CHECK (hourly_rate >= 0)
);

-- 8. status_history (contract 4.7)
--    old_status là NULL ở dòng đầu tiên (lúc tạo job).
CREATE TABLE status_history (
    id          SERIAL       PRIMARY KEY,
    job_id      INT          NOT NULL
                REFERENCES repair_jobs (id) ON DELETE RESTRICT,
    old_status  VARCHAR(30),
    new_status  VARCHAR(30)  NOT NULL,
    note        TEXT,
    changed_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_history_old_status CHECK (old_status IS NULL OR old_status IN
        ('RECEIVED', 'DIAGNOSING', 'WAITING_FOR_APPROVAL',
         'REPAIRING', 'READY', 'CLOSED')),
    CONSTRAINT chk_history_new_status CHECK (new_status IN
        ('RECEIVED', 'DIAGNOSING', 'WAITING_FOR_APPROVAL',
         'REPAIRING', 'READY', 'CLOSED'))
);

-- 9. Index (contract mục 5).
--    PostgreSQL không tự tạo index cho cột khóa ngoại, nên tạo cho các cột
--    hay dùng trong JOIN / WHERE.
--    estimates(job_id) đã được phủ bởi UNIQUE (job_id, version) ở trên.
CREATE INDEX idx_devices_customer_id        ON devices (customer_id);
CREATE INDEX idx_repair_jobs_status         ON repair_jobs (status);
CREATE INDEX idx_repair_jobs_device_id      ON repair_jobs (device_id);
CREATE INDEX idx_parts_estimate_id          ON parts (estimate_id);
CREATE INDEX idx_labor_services_estimate_id ON labor_services (estimate_id);
CREATE INDEX idx_status_history_job_id      ON status_history (job_id);
