# Design Document

## 1. Danh sách các lớp và vai trò (Class List & Responsibilities)

Liệt kê các class chính trong hệ thống và mô tả ngắn gọn vai trò của từng class.

| Class | Package | Vai trò |
| ----- | ------- | ------- |
|       |         |         |
|       |         |         |
|       |         |         |

---

## 2. Áp dụng các nguyên lý OOP

Mô tả rõ từng nguyên lý OOP được áp dụng ở đâu trong hệ thống.

### 2.1. Encapsulation

- Các thuộc tính nào được khai báo `private`?
- Truy cập thông qua getter/setter nào?
- Lý do áp dụng encapsulation?

**Mô tả:**

> …

### 2.2. Inheritance

- Class cha là gì?
- Các class con kế thừa từ đâu?
- Lý do sử dụng kế thừa?

**Mô tả:**

> …

### 2.3. Polymorphism

- Phương thức nào được override?
- Được gọi thông qua reference kiểu cha ở đâu?

**Mô tả:**

> …

### 2.4. Interface

- Interface nào được sử dụng?
- Vai trò của interface trong thiết kế?

**Mô tả:**

> …

### 2.5. Abstraction

- Abstract class / method nào được sử dụng?
- Phần chi tiết nào được ẩn đi?

**Mô tả:**

> …

---

## 3. Design Patterns được sử dụng

Liệt kê các design pattern (nếu có) và giải thích ngắn gọn cách áp dụng.

| Design Pattern | Áp dụng ở đâu | Mục đích |
| -------------- | ------------- | -------- |
|                |               |          |
|                |               |          |

> Nếu không sử dụng design pattern nào, hãy giải thích lý do.

---

## 4. Luồng hoạt động chính (Main Application Flows)

Mô tả các luồng xử lý chính của hệ thống theo dạng từng bước.

### 4.1. Login

1. Người dùng nhập username và password.
2. `LoginView` gửi thông tin đăng nhập đến `AuthService`.
3. `AuthService` kiểm tra thông tin người dùng.
4. Nếu hợp lệ, hệ thống chuyển sang `MenuView`.

### 4.2. [Tên luồng chức năng khác]

1. …
2. …
3. …

---

## 5. Class Diagram

Vẽ class diagram cho hệ thống bằng draw.io. Sơ đồ phải thể hiện:

- Quan hệ kế thừa
- Quan hệ association / composition (nếu có)
- Interface và class implement

📌 **Yêu cầu:** xuất sơ đồ thành file ảnh (PNG hoặc JPG), lưu tại `docs/class-diagram.png`.

---

## 6. Thiết kế lưu trữ dữ liệu (Database / File Design)

### 6.1. Hình thức lưu trữ

- [ ] In-memory
- [ ] File (txt / csv / json)
- [x] Database (PostgreSQL)

**Lý do lựa chọn:**

> Dữ liệu có nhiều quan hệ (khách hàng, thiết bị, đơn sửa chữa, báo giá, lịch sử) và cần transaction: đổi trạng thái đơn phải đi cùng việc ghi lịch sử, duyệt/từ chối báo giá phải đi cùng việc đổi trạng thái đơn. PostgreSQL cho khóa ngoại, `CHECK` và transaction sẵn có. Truy cập bằng JDBC thuần + HikariCP. Mỗi máy dùng hai database: `repair_shop` (demo, seed ổn định) và `repair_shop_test` (test, được phép xóa và tạo lại).

### 6.2. Cấu trúc dữ liệu lưu trữ

| Tên bảng         | Mô tả                                  | Dữ liệu chính                                                                                     |
| ---------------- | -------------------------------------- | ------------------------------------------------------------------------------------------------- |
| `customers`      | Khách hàng                             | `full_name`, `phone`, `email` (null), `address` (null)                                            |
| `devices`        | Thiết bị của khách                     | `customer_id`, `device_type`, `brand`, `model`, `serial_number`                                   |
| `repair_jobs`    | Đơn sửa chữa                           | `device_id`, `issue_description`, `status`, `desired_days`, `estimated_ready_at` (null), `closed_at` (null) |
| `estimates`      | Báo giá của một đơn (nhiều phiên bản)  | `job_id`, `version`, `status`, `note`                                                             |
| `parts`          | Dòng linh kiện của một báo giá         | `estimate_id`, `name`, `unit_price`, `quantity`                                                   |
| `labor_services` | Dòng công thợ của một báo giá          | `estimate_id`, `description`, `hours`, `hourly_rate`                                              |
| `status_history` | Lịch sử đổi trạng thái của đơn         | `job_id`, `old_status` (null), `new_status`, `note`, `changed_at`                                 |

Mọi bảng (trừ `parts`, `labor_services`, `status_history`) có `created_at`, `updated_at`; `status_history` có `changed_at`. Khóa chính là `SERIAL id`. Định nghĩa đầy đủ ở `src/main/resources/db/schema.sql`, mô tả từng cột ở `docs/contract.md` mục 4.

### 6.3. Sơ đồ bảng

```
customers 1 ──── N devices 1 ──── N repair_jobs 1 ──── N estimates 1 ──┬── N parts
                                        │                              └── N labor_services
                                        └──── N status_history
```

**Khóa ngoại:**

| Khóa ngoại                   | Bảng cha            |
| ---------------------------- | ------------------- |
| `devices.customer_id`        | `customers.id`      |
| `repair_jobs.device_id`      | `devices.id`        |
| `estimates.job_id`           | `repair_jobs.id`    |
| `parts.estimate_id`          | `estimates.id`      |
| `labor_services.estimate_id` | `estimates.id`      |
| `status_history.job_id`      | `repair_jobs.id`    |

**Ràng buộc đáng chú ý:**

- `status` của job, estimate và history có `CHECK` theo danh sách giá trị enum.
- `desired_days > 0`; `unit_price >= 0`, `quantity > 0`, `hours > 0`, `hourly_rate >= 0`.
- `UNIQUE (job_id, version)` trên `estimates`.
- `chk_repair_jobs_closed_at` buộc `closed_at` có giá trị khi và chỉ khi `status = 'CLOSED'`.

**Index:** `repair_jobs(status)`, `repair_jobs(device_id)`, `status_history(job_id)`, `devices(customer_id)`, `parts(estimate_id)`, `labor_services(estimate_id)`. `estimates(job_id)` đã được `UNIQUE (job_id, version)` phủ.

### 6.4. Lý do thiết kế

#### Khóa ngoại `ON DELETE RESTRICT`, không dùng `CASCADE`

Đơn sửa chữa, báo giá và lịch sử là hồ sơ cần giữ. Với `CASCADE`, xóa nhầm một khách hàng sẽ kéo theo mất toàn bộ thiết bị, đơn, báo giá và lịch sử của khách đó mà không có cảnh báo. Với `RESTRICT`, DB từ chối xóa khi còn dữ liệu con, lỗi lộ ra ngay và được bọc thành `DataAccessException`.

> Từ khóa `CASCADE` trong `DROP TABLE ... CASCADE` của `schema.sql` chỉ để xóa đối tượng phụ thuộc khi dựng lại DB, không liên quan đến `ON DELETE`.

#### Báo giá có `version`, quan hệ Job ↔ Estimate là 1-N

Khi khách chưa đồng ý, tiệm có thể báo giá lại. Thay vì sửa đè báo giá cũ (mất lịch sử), mỗi lần báo giá là một dòng mới với `version` tăng dần; bản có hiệu lực là bản `version` lớn nhất (`findLatestByJobId`). `UNIQUE (job_id, version)` đảm bảo không có hai bản cùng số.

`version` do Repository gán chứ không do Service/UI: trong một transaction, `EstimateRepository.save()` khóa dòng job (`FOR UPDATE`) rồi lấy `MAX(version) + 1`. Khóa dòng job làm hai lần lưu đồng thời của cùng một đơn phải xếp hàng nhau thay vì cùng nhận một số. Việc gán nằm cùng chỗ với câu `INSERT` nên không thể lệch với dữ liệu thực.

#### Hạn chót tính theo biểu thức, không lưu cột

Hạn chót = `created_at + desired_days` ngày. Nếu lưu thành cột `deadline`, cột này có thể lệch khi `created_at` hoặc `desired_days` đổi, và phải nhớ cập nhật ở mọi nơi ghi. Tính khi truy vấn thì luôn đúng:

```sql
ORDER BY (created_at + desired_days * INTERVAL '1 day') ASC, created_at ASC, id ASC
```

Hai tiêu chí sau để phá hòa và giữ thứ tự ổn định giữa các lần chạy. Với lượng đơn của một tiệm sửa chữa, `repair_jobs(status)` là đủ nên không tạo index riêng cho biểu thức. Phía Java, `RepairJob.getDeadline()` trả `createdAt.plusDays(desiredDays)` để UI hiển thị.

#### Tổng tiền không lưu ở DB

`Estimate.getTotalCost()` cộng `getCost()` của mọi dòng (`unit_price × quantity` và `hours × hourly_rate`), nên tổng không bao giờ lệch với chi tiết.

#### Tiền dùng `NUMERIC`, không dùng số thực

`NUMERIC(12,2)` ở DB và `BigDecimal` ở Java để tránh sai số làm tròn; `hours` dùng `NUMERIC(6,2)`.

#### Trạng thái lưu `VARCHAR` + `CHECK`, không dùng kiểu enum của PostgreSQL

Giá trị trùng tên hằng trong Java (`JobStatus`, `EstimateStatus`). Thêm trạng thái chỉ cần sửa `CHECK` và enum, không phải `ALTER TYPE`.

#### Transaction ở tầng Repository

Các thao tác cần nhiều bước (`saveWithInitialHistory`, `updateWithHistory`, `updateEstimateStatusAndJob`, `EstimateRepository.save`) mỗi hàm tự chạy trong một transaction, nên tầng trên không quản lý `Connection` và không import `java.sql.*`. Service kiểm tra luật nghiệp vụ trước rồi mới gọi.

#### Seed data cố định

`seed_data.sql` gán `created_at` cụ thể cho từng đơn để thứ tự hàng đợi không đổi giữa các lần demo, và nạp lại `setval` cho các bộ đếm `SERIAL` sau khi chèn id cố định.

---

## 7. Nhận xét về thiết kế (Optional)

- **Ưu điểm của thiết kế hiện tại:** …
- **Hạn chế:** …
- **Hướng cải tiến trong tương lai (nếu có):** …

---

## 8. Kết luận

Tóm tắt ngắn gọn cách thiết kế hệ thống và cách áp dụng OOP trong project.
