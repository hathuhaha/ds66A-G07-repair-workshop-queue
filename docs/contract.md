Hợp đồng dữ liệu (Data Contract)
Dự án: air-workshop-queue (package gốc `com.oop.project`)
Phiên bản: 0.1 (bản nháp để họp chốt)
Phạm vi: thống nhất giữa Thành viên 1 (Domain/Model, Service), Thành viên 2 (Database, Repository), Thành viên 3 (UI).
> Cách dùng: các mục đánh dấu **[CẦN CHỐT]** là đề xuất của Thành viên 2, cần cả nhóm xác nhận khi họp. Sau khi chốt, đổi thành **[ĐÃ CHỐT]** và ghi ngày. Mọi thay đổi sau này phải sửa file này và báo cả nhóm.
---
1. Công nghệ và môi trường
Hạng mục	Giá trị
Ngôn ngữ	Java (ghi rõ phiên bản cả nhóm dùng: `___`)
Công cụ build	Maven
Cơ sở dữ liệu	PostgreSQL (chạy local)
Truy cập dữ liệu	JDBC thuần + HikariCP
Test	JUnit 5
Hai database trên mỗi máy:
Database	Mục đích
`repair_shop`	Chạy ứng dụng và demo, chứa seed data ổn định
`repair_shop_test`	Chạy test, được phép xóa và tạo lại dữ liệu
Cấu hình kết nối: mỗi người copy `src/main/resources/application.properties.example` thành `application.properties` và điền thông tin máy mình. File `application.properties` không được commit.
Dựng database: chạy lần lượt `db/schema.sql` rồi `db/seed_data.sql` trong `src/main/resources/db/`. Mọi người dùng chung hai file này để có dữ liệu giống nhau.
---
2. Quy ước chung
Chủ đề	Quy ước	Trạng thái
Đặt tên Java	`camelCase` cho field, `PascalCase` cho class	[CẦN CHỐT]
Đặt tên DB	`snake_case` cho bảng và cột, tên bảng ở dạng số nhiều	[CẦN CHỐT]
Khóa chính	`SERIAL` ở DB, `int` ở Java, tên cột là `id`	[CẦN CHỐT]
Khóa ngoại	tên `<bảng_số_ít>_id`, ví dụ `customer_id`	[CẦN CHỐT]
Tiền tệ	`NUMERIC(12,2)` ở DB, `BigDecimal` ở Java. Không dùng `double`/`float`	[CẦN CHỐT]
Thời gian	`TIMESTAMP` ở DB, `LocalDateTime` ở Java	[CẦN CHỐT]
Chuỗi	`VARCHAR(n)` ở DB, `String` ở Java	[CẦN CHỐT]
Giá trị null	Field cho phép null được liệt kê ở mục 4.8; các field này đều là kiểu đối tượng (`String`, `LocalDateTime`, `JobStatus`) nên để `null` khi không có giá trị. Nếu sau này thêm field số nullable thì dùng `Integer`/`BigDecimal` thay cho `int`	[CẦN CHỐT]
Xóa dữ liệu	`ON DELETE RESTRICT` cho mọi khóa ngoại, không dùng `CASCADE`, để không mất lịch sử sửa chữa	[CẦN CHỐT]
Audit	Bảng chính có `created_at` (mặc định `CURRENT_TIMESTAMP`) và `updated_at`	[CẦN CHỐT]
---
3. Enum
3.1 `JobStatus` (trạng thái đơn sửa chữa)
DB lưu dạng `VARCHAR` kèm `CHECK`, giá trị trùng chính xác tên hằng trong Java.
Giá trị	Ý nghĩa
`RECEIVED`	Đã tiếp nhận thiết bị
`DIAGNOSING`	Đang chẩn đoán
`WAITING_FOR_APPROVAL`	Đã có báo giá, chờ khách duyệt
`REPAIRING`	Đang sửa chữa
`READY`	Sửa xong, chờ khách nhận
`CLOSED`	Đã đóng đơn
3.2 `EstimateStatus` (trạng thái bản báo giá) [CẦN CHỐT]
Giá trị	Ý nghĩa
`DRAFT`	Bản nháp, đang soạn
`APPROVED`	Khách đã duyệt
`REJECTED`	Khách từ chối
---
4. Thực thể và trường dữ liệu
Cột "Bắt buộc": có = `NOT NULL`, không = cho phép null.
4.1 `Customer` ↔ bảng `customers`
Field Java	Kiểu Java	Cột DB	Kiểu DB	Bắt buộc	Ghi chú
`id`	`int`	`id`	`SERIAL` PK	có	tự sinh
`fullName`	`String`	`full_name`	`VARCHAR(100)`	có	
`phone`	`String`	`phone`	`VARCHAR(20)`	có	
`email`	`String`	`email`	`VARCHAR(100)`	không	
`address`	`String`	`address`	`VARCHAR(255)`	không	
`createdAt`	`LocalDateTime`	`created_at`	`TIMESTAMP`	có	mặc định `CURRENT_TIMESTAMP`
`updatedAt`	`LocalDateTime`	`updated_at`	`TIMESTAMP`	có	
4.2 `Device` ↔ bảng `devices`
Field Java	Kiểu Java	Cột DB	Kiểu DB	Bắt buộc	Ghi chú
`id`	`int`	`id`	`SERIAL` PK	có	
`customer`	`Customer`	`customer_id`	`INT` FK → `customers.id`	có	Java giữ object, DB giữ id
`deviceType`	`String`	`device_type`	`VARCHAR(50)`	có	ví dụ: điều hòa, máy giặt
`brand`	`String`	`brand`	`VARCHAR(50)`	không	
`model`	`String`	`model`	`VARCHAR(50)`	không	
`serialNumber`	`String`	`serial_number`	`VARCHAR(50)`	không	
`createdAt` / `updatedAt`	`LocalDateTime`	`created_at` / `updated_at`	`TIMESTAMP`	có	
4.3 `RepairJob` ↔ bảng `repair_jobs`
Field Java	Kiểu Java	Cột DB	Kiểu DB	Bắt buộc	Ghi chú
`id`	`int`	`id`	`SERIAL` PK	có	
`device`	`Device`	`device_id`	`INT` FK → `devices.id`	có	
`issueDescription`	`String`	`issue_description`	`TEXT`	có	mô tả lỗi khách báo
`status`	`JobStatus`	`status`	`VARCHAR(30)` + `CHECK`	có	mặc định `RECEIVED`
`desiredDays`	`int`	`desired_days`	`INT` + `CHECK (desired_days > 0)`	có	[CẦN CHỐT] số ngày khách mong muốn nhận hàng, tính từ lúc tiếp nhận. Cùng với `createdAt` tạo thành hạn chót dùng để xếp hàng ưu tiên (mục 5.1)
`estimatedReadyAt`	`LocalDateTime`	`estimated_ready_at`	`TIMESTAMP`	không	[ĐÃ CHỐT] thời điểm ước tính (giả định) khách nhận được hàng, do nhân viên nhập thủ công, hệ thống không tự tính. `null` cho tới khi tiệm đưa ra ước tính. Có thể sửa nhiều lần qua `updateEstimatedReadyAt`, không ràng buộc với hạn chót và không ảnh hưởng thứ tự hàng đợi (mục 5.1)
`closedAt`	`LocalDateTime`	`closed_at`	`TIMESTAMP`	không	chỉ có giá trị khi `CLOSED`
`createdAt` / `updatedAt`	`LocalDateTime`	`created_at` / `updated_at`	`TIMESTAMP`	có	
4.4 `Estimate` ↔ bảng `estimates`
[CẦN CHỐT] Quan hệ Job ↔ Estimate là 1-N: một job có thể có nhiều bản báo giá theo thời gian (`version` tăng dần). Bản đang có hiệu lực là bản có `version` lớn nhất.
Field Java	Kiểu Java	Cột DB	Kiểu DB	Bắt buộc	Ghi chú
`id`	`int`	`id`	`SERIAL` PK	có	
`jobId`	`int`	`job_id`	`INT` FK → `repair_jobs.id`	có	
`version`	`int`	`version`	`INT`	có	bắt đầu từ 1, `UNIQUE (job_id, version)`
`status`	`EstimateStatus`	`status`	`VARCHAR(20)` + `CHECK`	có	mặc định `DRAFT`
`note`	`String`	`note`	`TEXT`	không	
`parts`	`List<Part>`	(bảng `parts`)			nạp kèm khi đọc estimate
`laborServices`	`List<LaborService>`	(bảng `labor_services`)			nạp kèm khi đọc estimate
`createdAt` / `updatedAt`	`LocalDateTime`	`created_at` / `updated_at`	`TIMESTAMP`	có	
[CẦN CHỐT] Tổng tiền không lưu ở DB. Class `Estimate` tính `getTotalCost()` = tổng `getCost()` của mọi dòng (các part và các labor service). Như vậy không bao giờ lệch giữa tổng và chi tiết.
4.4.1 `EstimateItem`: interface chung của `Part` và `LaborService` [CẦN CHỐT]
```java
public interface EstimateItem {
    String getName();      // tên hiển thị của dòng
    BigDecimal getCost();  // thành tiền của dòng
}
```
`Part` và `LaborService` cùng `implements EstimateItem`, để giao diện hiển thị và `Estimate` tính tổng bằng một vòng lặp trên mọi dòng.
Đây là interface chỉ có hàm tính toán, không có cột tương ứng ở DB nên không ảnh hưởng schema và mapper. Repository chỉ đọc/ghi các field ở mục 4.5 và 4.6, không dùng `getName()` hay `getCost()`.
Quy ước tính: `Part.getName()` = `name`, `Part.getCost()` = `unitPrice × quantity`; `LaborService.getName()` = `description`, `LaborService.getCost()` = `hours × hourlyRate`.
`Part` và `LaborService` phải giữ nguyên các field và setter ở mục 4.5, 4.6 vì mapper dựa vào chúng.
4.5 `Part` ↔ bảng `parts`
[CẦN CHỐT] `parts` là các dòng linh kiện thuộc một estimate (không phải danh mục kho).
Field Java	Kiểu Java	Cột DB	Kiểu DB	Bắt buộc	Ghi chú
`id`	`int`	`id`	`SERIAL` PK	có	
`estimateId`	`int`	`estimate_id`	`INT` FK → `estimates.id`	có	
`name`	`String`	`name`	`VARCHAR(100)`	có	
`unitPrice`	`BigDecimal`	`unit_price`	`NUMERIC(12,2)`	có	`CHECK (unit_price >= 0)`
`quantity`	`int`	`quantity`	`INT`	có	`CHECK (quantity > 0)`
4.6 `LaborService` ↔ bảng `labor_services`
[CẦN CHỐT] Tương tự `parts`: các dòng công thợ thuộc một estimate.
Field Java	Kiểu Java	Cột DB	Kiểu DB	Bắt buộc	Ghi chú
`id`	`int`	`id`	`SERIAL` PK	có	
`estimateId`	`int`	`estimate_id`	`INT` FK → `estimates.id`	có	
`description`	`String`	`description`	`VARCHAR(200)`	có	
`hours`	`BigDecimal`	`hours`	`NUMERIC(6,2)`	có	`CHECK (hours > 0)`
`hourlyRate`	`BigDecimal`	`hourly_rate`	`NUMERIC(12,2)`	có	`CHECK (hourly_rate >= 0)`
4.7 `StatusHistory` ↔ bảng `status_history`
Field Java	Kiểu Java	Cột DB	Kiểu DB	Bắt buộc	Ghi chú
`id`	`int`	`id`	`SERIAL` PK	có	
`jobId`	`int`	`job_id`	`INT` FK → `repair_jobs.id`	có	
`oldStatus`	`JobStatus`	`old_status`	`VARCHAR(30)`	không	`null` ở lần ghi đầu tiên (lúc tạo job)
`newStatus`	`JobStatus`	`new_status`	`VARCHAR(30)` + `CHECK`	có	
`note`	`String`	`note`	`TEXT`	không	
`changedAt`	`LocalDateTime`	`changed_at`	`TIMESTAMP`	có	mặc định `CURRENT_TIMESTAMP`
4.8 Danh sách field cho phép null [CẦN CHỐT]
Tổng hợp từ các bảng ở trên. Mọi field không nằm trong danh sách này là bắt buộc (`NOT NULL`): Thành viên 1 phải đảm bảo chúng có giá trị trước khi gọi `save()`, nếu không DB báo lỗi và Repository ném `DataAccessException`.
Class	Field Java	Kiểu Java	Cột DB	Khi nào là `null`
`Customer`	`email`	`String`	`email`	Khách không cung cấp email
`Customer`	`address`	`String`	`address`	Khách không cung cấp địa chỉ
`Device`	`brand`	`String`	`brand`	Không rõ hãng
`Device`	`model`	`String`	`model`	Không rõ model
`Device`	`serialNumber`	`String`	`serial_number`	Không có số serial
`RepairJob`	`estimatedReadyAt`	`LocalDateTime`	`estimated_ready_at`	Tiệm chưa đưa ra ước tính ngày nhận hàng
`RepairJob`	`closedAt`	`LocalDateTime`	`closed_at`	Đơn chưa `CLOSED`
`Estimate`	`note`	`String`	`note`	Không có ghi chú
`StatusHistory`	`oldStatus`	`JobStatus`	`old_status`	Dòng history đầu tiên, lúc tạo job
`StatusHistory`	`note`	`String`	`note`	Không có ghi chú
Hai quy ước kèm theo:
Với field tùy chọn kiểu `String`, chuỗi rỗng `""` được coi là `null`. Nơi nhận dữ liệu từ UI (Thành viên 1 hoặc 3) chuyển `""` thành `null` trước khi lưu, để dữ liệu trong DB nhất quán.
`id` của entity mới chưa lưu là `0`. Hàm `save()` trả về entity đã có `id` do DB sinh.
---
5. Sơ đồ quan hệ
```
customers 1 ──── N devices 1 ──── N repair_jobs 1 ──── N estimates 1 ──┬── N parts
                                        │                              └── N labor_services
                                        └──── N status_history
```
Mọi khóa ngoại dùng `ON DELETE RESTRICT`.
Index bắt buộc: `repair_jobs(status)`, `repair_jobs(device_id)`, `estimates(job_id)`, `status_history(job_id)`. Hàng đợi sắp xếp theo biểu thức hạn chót nên chưa cần index riêng, với lượng đơn của một tiệm sửa chữa thì `repair_jobs(status)` là đủ.
5.1 Quy tắc hàng đợi ưu tiên [ĐÃ CHỐT]
Hạn chót = `created_at` + `desired_days` ngày. Hạn chót không lưu ở DB. Class `RepairJob` có hàm `getDeadline()` trả về `createdAt.plusDays(desiredDays)` để UI hiển thị.
Các đơn được xếp theo thứ tự ưu tiên sau (hạn chót đến trước thì được trước):
Hạn chót tăng dần: hạn chót càng sớm thì càng được ưu tiên.
Nếu bằng nhau, `created_at` tăng dần: đơn tiếp nhận trước được làm trước.
Nếu vẫn bằng nhau, `id` tăng dần (để thứ tự luôn ổn định).
Ví dụ: đơn A nhận cách đây 4 ngày với `desired_days = 5` có hạn chót sau 1 ngày nữa, nên được xếp trước đơn B nhận hôm nay với `desired_days = 3` (hạn chót sau 3 ngày nữa).
Phạm vi hàng đợi [ĐÃ CHỐT]: có hai bản, cùng dùng chung một quy tắc sắp xếp.
`findQueue()`: mọi đơn chưa `CLOSED` (dùng cho màn hình tổng quan).
`findQueue(statuses)`: chỉ các trạng thái được truyền vào, luôn bỏ qua `CLOSED` (dùng cho màn hình "việc của thợ", ví dụ `RECEIVED`, `DIAGNOSING`, `REPAIRING`).
Đề xuất: Thành viên 1 định nghĩa tập trạng thái "việc của thợ" ở một chỗ duy nhất (ví dụ hằng số trong `JobStatus`) để các màn hình không định nghĩa lệch nhau.
Việc sắp xếp do Thành viên 2 thực hiện trong Repository (hàm `findQueue()` ở mục 7) bằng:
```sql
ORDER BY (created_at + desired_days * INTERVAL '1 day') ASC, created_at ASC, id ASC
```
Thành viên 1 và 3 chỉ cần dùng thứ tự danh sách trả về, không tự sắp xếp lại.
---
6. Luật chuyển trạng thái đơn sửa chữa
[ĐÃ CHỐT] Luồng hợp lệ:
```
RECEIVED → DIAGNOSING → WAITING_FOR_APPROVAL → REPAIRING → READY → CLOSED

Nhánh từ chối báo giá (đi tắt):
WAITING_FOR_APPROVAL ──(estimate REJECTED)──► CLOSED
```
Luật	Nội dung
Chỉ đi tới	Không được nhảy cóc hoặc lùi trạng thái. Ngoại lệ duy nhất là nhánh đi tắt tới `CLOSED` ở dòng "Từ chối báo giá" bên dưới
Đóng đơn	`CLOSED` chỉ đạt được từ `READY` (khách đã nhận máy), hoặc từ `WAITING_FOR_APPROVAL` khi estimate mới nhất bị `REJECTED`. Đóng từ `RECEIVED`, `DIAGNOSING` hoặc `REPAIRING` bị từ chối và ném lỗi (dùng cho Demo 3)
Duyệt báo giá	Chuyển sang `REPAIRING` khi bản estimate mới nhất ở trạng thái `APPROVED`
Từ chối báo giá	[ĐÃ CHỐT] Khi estimate bị `REJECTED`, job chuyển thẳng sang `CLOSED` và `closedAt` được ghi nhận. Việc đổi trạng thái estimate, đổi trạng thái job và ghi `status_history` (note gợi ý: "Khách từ chối báo giá") nằm trong một transaction
Ai làm gì:
Việc	Người phụ trách
Kiểm tra luật chuyển trạng thái, ném lỗi nghiệp vụ	Thành viên 1 (domain/service)
Lưu trạng thái mới và ghi lịch sử trong một transaction	Thành viên 2
Hiển thị lỗi cho người dùng	Thành viên 3
---
7. Hợp đồng tầng Repository
Package: `com.oop.project.repository` (interface), `com.oop.project.repository.impl` (hiện thực JDBC).
[CẦN CHỐT] Tên gọi: dùng hậu tố `Repository` (ví dụ `CustomerRepository`). Trong tài liệu môn học, Repository chính là tầng DAO.
```java
public interface GenericRepository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);          // trả về entity đã có id được sinh
    void update(T entity);
    void delete(ID id);
}

public interface CustomerRepository extends GenericRepository<Customer, Integer> {
    List<Customer> findByPhone(String phone);
    List<Customer> searchByName(String keyword);
}

public interface DeviceRepository extends GenericRepository<Device, Integer> {
    List<Device> findByCustomerId(int customerId);
}

public interface RepairJobRepository extends GenericRepository<RepairJob, Integer> {
    List<RepairJob> findByStatus(JobStatus status);
    List<RepairJob> findByDeviceId(int deviceId);
    List<RepairJob> findByCustomerId(int customerId);

    // Hàng đợi ưu tiên: mọi đơn chưa CLOSED, sắp xếp theo mục 5.1
    List<RepairJob> findQueue();

    // Như trên nhưng chỉ lấy các trạng thái được truyền vào (bỏ qua CLOSED nếu có).
    // Tập rỗng hoặc null trả về danh sách rỗng.
    List<RepairJob> findQueue(Collection<JobStatus> statuses);

    // Lưu đơn mới + ghi dòng history đầu tiên (oldStatus = null, newStatus = RECEIVED) trong 1 transaction.
    // Đơn mới phải có status null hoặc RECEIVED. Trả về đơn đã có id. Dùng cho createJob.
    RepairJob saveWithInitialHistory(RepairJob job, String note);

    // Cập nhật status, closedAt, updatedAt của đơn + ghi dòng history trong 1 transaction.
    // history.newStatus phải khớp job.status. Đơn CLOSED phải có closedAt, đơn chưa CLOSED để closedAt = null.
    // Dùng cho updateJobStatusWithHistory.
    void updateWithHistory(RepairJob job, StatusHistory history);
}

public interface EstimateRepository {
    Estimate save(Estimate estimate);                    // lưu kèm parts và laborServices
    Optional<Estimate> findById(int id);                 // nạp kèm parts và laborServices
    List<Estimate> findByJobId(int jobId);
    Optional<Estimate> findLatestByJobId(int jobId);
    void updateStatus(int estimateId, EstimateStatus status);

    // Đổi trạng thái báo giá + cập nhật đơn (status, closedAt, updatedAt) + ghi history trong 1 transaction.
    // Báo giá phải thuộc đơn được truyền vào, nếu không ném EntityNotFoundException.
    // Dùng cho approveEstimate (APPROVED, đơn sang REPAIRING) và rejectEstimate (REJECTED, đơn sang CLOSED).
    void updateEstimateStatusAndJob(int estimateId, EstimateStatus status, RepairJob job, StatusHistory history);
}

public interface StatusHistoryRepository {
    List<StatusHistory> findByJobId(int jobId);          // sắp xếp theo changedAt tăng dần
}
```
Quy ước khi đọc dữ liệu:
`RepairJob` luôn được nạp kèm `Device`, và `Device` kèm `Customer` (bằng `JOIN`, không gọi từng DAO riêng).
`findById` trả `Optional.empty()` khi không có, không trả `null`.
`findAll` và các hàm `findBy...` trả danh sách rỗng khi không có, không trả `null`.
---
8. Hợp đồng Service và Transaction
Package: `com.oop.project.service`. Thành viên 1 và 3 chỉ gọi các hàm này cho thao tác cần transaction, không tự quản lý `Connection`.
```java
public interface JobService {
    // Tạo job và ghi dòng history đầu tiên (oldStatus = null, newStatus = RECEIVED) trong 1 transaction
    RepairJob createJob(int deviceId, String issueDescription, int desiredDays);

    // Đổi trạng thái và ghi history trong 1 transaction.
    // Ném InvalidStatusTransitionException nếu vi phạm luật ở mục 6.
    void updateJobStatusWithHistory(int jobId, JobStatus newStatus, String note);

    // Lưu estimate mới (version tự tăng) kèm các dòng chi tiết trong 1 transaction
    Estimate createEstimate(int jobId, List<Part> parts, List<LaborService> laborServices, String note);

    // Duyệt estimate và chuyển job sang REPAIRING trong 1 transaction
    void approveEstimate(int estimateId);

    // Từ chối estimate, chuyển job sang CLOSED (ghi closedAt) và ghi history trong 1 transaction
    void rejectEstimate(int estimateId, String note);

    // Cập nhật thời điểm ước tính khách nhận được hàng (không đổi trạng thái job)
    void updateEstimatedReadyAt(int jobId, LocalDateTime estimatedReadyAt);
}
```
[CẦN CHỐT] Hàm nào kiểm tra luật chuyển trạng thái: đề xuất đặt trong `JobService` (Thành viên 1 viết logic kiểm tra, Thành viên 2 gọi Repository để lưu).
Hàm service gọi hàm repository nào (mỗi hàm repository ở dưới tự chạy trong một transaction):
Hàm `JobService`	Hàm repository	Ghi chú
`createJob`	`RepairJobRepository.saveWithInitialHistory(job, note)`	Ghi đơn mới và dòng history đầu tiên
`updateJobStatusWithHistory`	`RepairJobRepository.updateWithHistory(job, history)`	Service kiểm tra luật mục 6, gán `status` (và `closedAt` nếu `CLOSED`) cho `job`, tạo `history` rồi gọi
`createEstimate`	`EstimateRepository.save(estimate)`	Đã nằm trong một transaction
`approveEstimate`	`EstimateRepository.updateEstimateStatusAndJob(id, APPROVED, job, history)`	`job.status = REPAIRING`
`rejectEstimate`	`EstimateRepository.updateEstimateStatusAndJob(id, REJECTED, job, history)`	`job.status = CLOSED`, `job.closedAt` có giá trị
`updateEstimatedReadyAt`	`RepairJobRepository.update(job)`	Không đổi trạng thái nên không cần history
---
9. Xử lý lỗi
Loại lỗi	Class	Package	Loại exception	Người ném
Lỗi truy cập dữ liệu (bọc `SQLException`)	`DataAccessException`	`exception`	unchecked (`RuntimeException`)	Thành viên 2
Không tìm thấy dữ liệu cần thiết	`EntityNotFoundException`	`exception`	unchecked	Thành viên 2 / 1
Chuyển trạng thái không hợp lệ	`InvalidStatusTransitionException` (kế thừa `DomainException`)	`exception`	unchecked	Thành viên 1
Lớp cha của mọi lỗi nghiệp vụ	`DomainException`	`exception`	unchecked	Thành viên 1
Quy tắc:
Tầng trên không import `java.sql.*`.
Mọi lỗi nghiệp vụ (vi phạm luật của hệ thống) kế thừa `DomainException`, nên giao diện bắt một loại là hiển thị được cho người dùng. `DataAccessException` là lỗi kỹ thuật, không kế thừa lớp này.
Message lỗi nghiệp vụ viết bằng tiếng Việt, đủ rõ để Thành viên 3 hiển thị thẳng cho người dùng (ví dụ: "Chỉ đơn ở trạng thái READY mới được đóng").
---
10. Kịch bản demo và dữ liệu mẫu
Thành viên 2 viết `seed_data.sql` dựa trên bảng này. [CẦN CHỐT] với Thành viên 1 và 3.
Mã	Kịch bản	Dữ liệu cần có sẵn
Demo 1	Tạo đơn và theo dõi lịch sử trạng thái	Ít nhất 2 customer, mỗi người 1–2 device
Demo 2	Luồng báo giá (Estimate)	Một job đang `WAITING_FOR_APPROVAL`, có estimate `DRAFT` gồm khoảng 2 part và 1 labor service
Demo 3	Bắt lỗi khi đóng job sai điều kiện	Một job đang `REPAIRING` (chưa `READY`) để thử đóng và nhận lỗi; một job `READY` để đóng thành công
Demo 2 (nhánh từ chối)	Từ chối báo giá, job tự đóng	Một job thứ hai ở `WAITING_FOR_APPROVAL` có estimate `DRAFT` riêng, tách khỏi job dùng cho nhánh duyệt để hai demo không dùng chung dữ liệu
Hàng đợi ưu tiên	Kiểm tra thứ tự theo hạn chót rồi đến thứ tự tiếp nhận	Ít nhất 4 job chưa `CLOSED`, trong đó có: một cặp mà đơn nhận sau lại có hạn chót sớm hơn đơn nhận trước (ví dụ nhận cách đây 4 ngày với `desired_days = 5` và nhận hôm nay với `desired_days = 3`), để thấy sắp xếp theo hạn chót khác sắp xếp theo số ngày; và một cặp có cùng hạn chót để thấy luật phá hòa. Seed phải gán `created_at` cụ thể cho từng job (không dùng giá trị mặc định) để thứ tự ổn định. Một số job có `estimated_ready_at`, một số để `null`
Khác	Hiển thị danh sách	Ít nhất 1 job ở mỗi trạng thái của `JobStatus`
Mọi job trong seed data phải có dòng `status_history` tương ứng với trạng thái hiện tại.
---
11. Phân công và quy trình
Thành viên	Phụ trách chính	Bàn giao cho nhóm
1	Package `model`, enum, luật nghiệp vụ, `service`	Class domain, `JobService`, các exception nghiệp vụ
2	`config`, `repository`, `resources/db`, test repository	Interface repository (sớm nhất có thể), `schema.sql`, `seed_data.sql`, README cách dựng DB
3	Package `ui`	Danh sách chức năng UI cần gọi, yêu cầu dữ liệu cho demo
Quy trình thay đổi hợp đồng:
Người muốn đổi sửa file này và ghi vào mục 12.
Báo cả nhóm trước khi merge.
Thành viên 2 cập nhật `schema.sql`, `seed_data.sql` và mapper tương ứng.
---
12. Nhật ký thay đổi
Ngày	Người sửa	Nội dung
2026-10-22	Thành viên 2	Tạo bản nháp 0.1
2026-10-22	Thành viên 2	Thêm mục 4.8 (danh sách field nullable), sửa quy ước null ở mục 2
2026-10-22	Thành viên 2	Chốt mục 6: estimate bị `REJECTED` thì job sang `CLOSED`; thêm `rejectEstimate` vào `JobService` (mục 8); thêm dữ liệu seed cho nhánh từ chối (mục 10)
2026-10-22	Thành viên 2	Thêm cột `desired_days`, `estimated_ready_at` cho `RepairJob` (mục 4.3, 4.8); thêm quy tắc hàng đợi ưu tiên (mục 5.1), `findQueue()` (mục 7), đổi `createJob` và thêm `updateEstimatedReadyAt` (mục 8), thêm seed hàng đợi (mục 10)
2026-10-22	Thành viên 2	Chốt mục 5.1: hàng đợi xếp theo hạn chót (`created_at` + `desired_days`), thêm `getDeadline()`, đổi câu `ORDER BY`, bỏ index tổ hợp `desired_days`, cập nhật seed hàng đợi (mục 10)
2026-10-22	Thành viên 2	Chốt `estimated_ready_at` là giá trị ước tính do nhân viên nhập thủ công, không ràng buộc với hạn chót và không ảnh hưởng hàng đợi (mục 4.3)
2026-10-22	Thành viên 2	Chốt phạm vi hàng đợi: thêm `findQueue(Collection<JobStatus>)` bên cạnh `findQueue()` (mục 5.1, 7)
2026-10-22	Thành viên 1 + 2	Thêm `DomainException` làm lớp cha các lỗi nghiệp vụ, `InvalidStatusTransitionException` kế thừa nó (mục 9)
2026-10-22	Thành viên 2	Thêm `saveWithInitialHistory`, `updateWithHistory` (RepairJobRepository) và `updateEstimateStatusAndJob` (EstimateRepository), mỗi hàm chạy trong 1 transaction; thêm bảng ánh xạ service sang repository (mục 7, 8)
2026-10-22	Thành viên 1 + 2	Ghi nhận interface `EstimateItem` (chung cho `Part`, `LaborService`) ở mục 4.4.1; `Estimate.getTotalCost()` tính từ `getCost()`
---
13. Danh sách việc cần chốt khi họp
[ ] Phiên bản Java dùng chung
[ ] Quy ước đặt tên và kiểu dữ liệu ở mục 2
[ ] Quan hệ Job ↔ Estimate là 1-N, có `version`
[ ] `parts` và `labor_services` là dòng chi tiết của estimate
[ ] Tổng tiền tính trong Java, không lưu ở DB
[x] Luật chuyển trạng thái (estimate bị từ chối thì job sang `CLOSED`)
[ ] Hậu tố `Repository` hay `Dao`
[ ] Ai kiểm tra luật chuyển trạng thái
[ ] Danh sách dữ liệu seed cho Demo 1, 2, 3
[x] Quy tắc hàng đợi ưu tiên (mục 5.1): hạn chót (`created_at` + `desired_days`) đến trước thì được trước
[x] Phạm vi hàng đợi: `findQueue()` (mọi đơn chưa `CLOSED`) và `findQueue(statuses)` (lọc theo trạng thái)
[x] `estimated_ready_at` là giá trị ước tính do nhân viên nhập thủ công, không ảnh hưởng hàng đợi (mục 4.3)