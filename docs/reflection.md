## Contribution

* Thành viên 1: Domain model + Service (nghiệp vụ) + Warranty adapter — *\[Thành viên 1 bổ sung]*
* Thành viên 2 – *Đinh Vân Anh*: Database + Repository (JDBC)

  * **Thiết kế cơ sở dữ liệu:** viết `schema.sql` cho 7 bảng (`customers`, `devices`, `repair\_jobs`, `estimates`, `parts`, `labor\_services`, `status\_history`) với khóa chính, khóa ngoại `ON DELETE RESTRICT`, ràng buộc `NOT NULL` / `CHECK`, kiểu `NUMERIC` cho tiền và index cho các cột hay truy vấn. Viết `seed\_data.sql` theo kịch bản demo (7 đơn ở nhiều trạng thái, báo giá được duyệt / bị từ chối, lịch sử trạng thái đầy đủ cho từng đơn).
  * **Hợp đồng dữ liệu:** soạn và duy trì `docs/contract.md` (thực thể, kiểu dữ liệu, field cho phép null, luật chuyển trạng thái, interface repository và service, xử lý lỗi, nhật ký thay đổi) để ba thành viên làm song song mà không lệch nhau.
  * **Kết nối cơ sở dữ liệu:** `DatabaseConnection` dùng HikariCP, đọc cấu hình từ file properties (không hardcode mật khẩu), và `AppContext` để giao diện lấy repository và service từ một chỗ duy nhất.
  * **Tầng Repository:** các interface (`GenericRepository`, `CustomerRepository`, `DeviceRepository`, `RepairJobRepository`, `EstimateRepository`, `StatusHistoryRepository`), phần hiện thực JDBC, các `RowMapper`, và lớp dùng chung `AbstractJdbcRepository`.
  * **Transaction và lịch sử trạng thái:** `saveWithInitialHistory`, `updateWithHistory`, `updateEstimateStatusAndJob` (mỗi hàm là một transaction: ghi đơn / báo giá và ghi `status\_history` cùng lúc), lưu báo giá kèm các dòng chi tiết trong một transaction.
  * **Hàng đợi ưu tiên:** `findQueue()` và `findQueue(statuses)` sắp xếp theo hạn chót (`created\_at + desired\_days`).
  * **Kiểm thử:** bộ test JUnit chạy trên một database riêng (`CustomerRepositoryTest`, `DeviceRepositoryTest`, `EstimateRepositoryTest`, `RepairJobRepositoryTest`, `JobServiceTest`) cùng `TestDatabase` để dựng lại schema và seed trước mỗi lần chạy.
  * **Tích hợp:** gộp nhánh `feature/db-jdbc` và `feature/domain-adapter`, xử lý xung đột, và cập nhật tài liệu (`README.md`, `docs/design.md`, `docs/contract.md`).
* Thành viên 3: Giao diện (GUI) — *\[Thành viên 3 bổ sung]*

## Design Decisions

### Vì sao dùng inheritance thay vì if-else?

**Thành viên 2 (Repository):**

* **Lớp cha `AbstractJdbcRepository`.** Mọi repository đều lặp lại cùng một việc: lấy connection từ pool, tạo `PreparedStatement`, chạy truy vấn, đổi `SQLException` thành `DataAccessException`, đóng tài nguyên, và chạy một transaction. Đặt các việc này ở lớp cha thì mỗi lớp con (`CustomerRepositoryImpl`, `RepairJobRepositoryImpl`, ...) chỉ còn chứa câu SQL của riêng nó. Nếu không dùng kế thừa, phải chép đoạn mã này vào từng repository, hoặc viết một lớp truy cập dữ liệu duy nhất với chuỗi `if (entity instanceof Customer) ... else if (entity instanceof Device) ...`. Cách đó vừa dài, vừa phải sửa lại lớp cũ mỗi khi thêm bảng mới.
* **Interface và hiện thực tách riêng.** `RepairJobRepository` là interface, `RepairJobRepositoryImpl` là hiện thực. Tầng service và giao diện chỉ phụ thuộc vào interface (nguyên lý Dependency Inversion), nên có thể thay cơ sở dữ liệu hoặc dùng đối tượng giả khi kiểm thử mà không sửa code gọi nó.
* **`GenericRepository<T, ID>`** gom các thao tác CRUD chung (`findById`, `findAll`, `save`, `update`, `delete`) để các repository cụ thể chỉ khai báo thêm hàm riêng của mình, ví dụ `findQueue` hay `findByPhone`.
* **Hệ thống exception.** `InvalidStatusTransitionException` và `EntityNotFoundException` cùng kế thừa `DomainException`. Giao diện chỉ cần `catch (DomainException e)` để báo cho người dùng sửa thao tác, thay vì kiểm tra từng loại lỗi bằng `if`. Lỗi kỹ thuật (`DataAccessException`) được tách riêng, không kế thừa lớp này, để không bị hiện nhầm cho người dùng như lỗi nghiệp vụ.
* **Dùng thành phần (composition) khi kế thừa không hợp lý.** Một `RepairJob` chứa `Device`, `Device` chứa `Customer`, nên `RepairJobMapper` gọi `DeviceMapper`, và `DeviceMapper` gọi `CustomerMapper`, thay vì kế thừa nhau, vì quan hệ giữa chúng là "chứa", không phải "là một".

**Thành viên 1 (Domain / Service):** *\[bổ sung sau]*

**Thành viên 3 (GUI):** *\[bổ sung sau]*

### Phần nào khó nhất?

**Thành viên 2 (Database / Repository):**

1. **Transaction khi đổi trạng thái đơn.** Việc cập nhật trạng thái đơn và ghi lịch sử phải thành công cùng nhau hoặc không có gì được lưu. Cần tắt auto-commit, `commit` khi thành công, `rollback` khi lỗi, và luôn trả connection về pool để không bị rò rỉ. Ràng buộc `CHECK` ở bảng `repair\_jobs` còn buộc `status` và `closed\_at` đổi trong cùng một câu lệnh, nên thiết kế hàm repository phải tính đến điều này. Khi tạo báo giá mới, phải khóa dòng của đơn bằng `SELECT ... FOR UPDATE` để hai người cùng tạo không bị trùng số version.
2. **Thứ tự hàng đợi.** Ban đầu ưu tiên theo số ngày khách mong muốn, nhưng cách này xếp sai các đơn đã chờ lâu. Cả nhóm đổi sang hạn chót (`created\_at + desired\_days`), kèm hai lớp phá hòa (thời gian tiếp nhận, rồi `id`). Seed data phải cố ý có các cặp đơn cho thấy sự khác nhau giữa hai cách xếp để kiểm tra được.
3. **Ánh xạ đối tượng lồng nhau.** Dựng `RepairJob` kèm `Device` và `Customer` bằng một câu `JOIN` (tránh truy vấn N+1) đòi hỏi đặt bí danh cột có tiền tố (`j\_`, `d\_`, `c\_`) và giữ cho tên cột trong SQL luôn khớp với mapper.
4. **Phối hợp nhóm.** Hai nhánh phát triển song song (`db-jdbc` và `domain-adapter`) trôi xa nhau, dẫn đến xung đột khi gộp ở `.gitignore`, `contract.md` và các lớp exception. Bài học là chốt hợp đồng dữ liệu sớm, ghi lại mọi thay đổi, và gộp nhánh thường xuyên hơn.

**Thành viên 1 (Domain / Service):** *\[bổ sung sau]*

**Thành viên 3 (GUI):** *\[bổ sung sau]*

### Các quyết định thiết kế khác (và lý do)

**Thành viên 2 (Database / Repository):**

*Thiết kế cơ sở dữ liệu*

* **Dùng `NUMERIC(12,2)` và `BigDecimal` cho tiền, không dùng `FLOAT`/`DOUBLE`.** Kiểu dấu phẩy động chỉ lưu xấp xỉ, nên cộng nhiều dòng báo giá có thể lệch vài đồng; `NUMERIC` lưu chính xác. *Đánh đổi:* code Java phải dùng `add`, `multiply` của `BigDecimal` thay cho toán tử thông thường, dài hơn nhưng đúng.
* **Khóa ngoại dùng `ON DELETE RESTRICT`, không dùng `CASCADE`.** Lịch sử đơn và báo giá là dữ liệu cần giữ (bảo hành, đối soát). Với `CASCADE`, chỉ cần xóa nhầm một khách hàng là mất toàn bộ thiết bị, đơn và báo giá của họ. Với `RESTRICT`, DB từ chối xóa và repository đổi lỗi này thành thông báo dễ hiểu. *Đánh đổi:* muốn xóa thật phải xóa dữ liệu con trước; nếu sau này cần "xóa mềm" thì phải thêm cột đánh dấu.
* **Lưu trạng thái bằng `VARCHAR` + `CHECK`, không dùng kiểu `ENUM` của PostgreSQL.** Giá trị trùng tên hằng trong Java Enum nên chỉ cần `JobStatus.valueOf(...)`, và thêm trạng thái mới chỉ cần sửa ràng buộc `CHECK` (sửa kiểu `ENUM` phức tạp hơn khi migration). *Đánh đổi:* danh sách trạng thái lặp ở nhiều ràng buộc nên phải sửa đồng bộ.
* **Quan hệ Job – Estimate là 1-N có `version`, thay vì 1-1.** Báo giá có thể được điều chỉnh nhiều lần nên cần giữ các bản cũ; `UNIQUE (job\_id, version)` ngăn trùng, bản mới nhất là bản có hiệu lực. *Đánh đổi:* cần thêm `findLatestByJobId`, và khi tạo version mới phải khóa dòng của đơn (`SELECT ... FOR UPDATE`) để hai người tạo cùng lúc không bị trùng.
* **Không lưu những giá trị suy ra được: tổng tiền báo giá và hạn chót.** Tổng tiền là tổng các dòng chi tiết, hạn chót là `created\_at + desired\_days`. Nếu lưu sẵn thì có nguy cơ lệch khi dòng chi tiết hoặc số ngày thay đổi; tính khi cần thì luôn đúng. *Đánh đổi:* phải tính lại mỗi lần truy vấn, nhưng với số đơn của một tiệm sửa chữa thì không đáng kể nên chưa cần index riêng cho biểu thức hạn chót.
* **Đặt các ràng buộc nhất quán ngay trong DB** (ví dụ `closed\_at` có giá trị khi và chỉ khi `status = 'CLOSED'`, `desired\_days > 0`, `quantity > 0`) bên cạnh việc kiểm tra ở service. Service có thể có lỗi hoặc bị bỏ qua, DB là lớp phòng thủ cuối cùng. *Đánh đổi:* lỗi từ DB khó đọc, nên repository kiểm tra sớm một số điều kiện và dịch mã lỗi (SQLState) sang thông báo tiếng Việt.

*Truy cập dữ liệu*

* **Transaction nằm ở tầng repository, qua các hàm có tên nghiệp vụ** (`saveWithInitialHistory`, `updateWithHistory`, `updateEstimateStatusAndJob`), thay vì để service tự mở connection. Service không phải biết `Connection` và không import `java.sql`; tính nguyên tử (cập nhật đơn và ghi lịch sử cùng lúc) được bảo đảm ở một chỗ duy nhất và kiểm thử riêng được. *Đánh đổi:* interface repository mang tên gắn với nghiệp vụ, và mỗi nghiệp vụ transaction mới cần thêm một hàm.
* **Ghi lịch sử bằng code ứng dụng, không dùng trigger của PostgreSQL.** Logic nằm trong Java nên dễ đọc, dễ kiểm thử, cho phép lưu kèm ghi chú, và thể hiện rõ cơ chế transaction của JDBC. *Đánh đổi:* nếu ai đó sửa DB trực tiếp mà không qua ứng dụng thì sẽ không có dòng lịch sử.
* **Dùng `PreparedStatement` cho mọi truy vấn.** Tham số luôn được xử lý như giá trị thuần, không thể thay đổi cấu trúc câu lệnh nên chống được SQL Injection; các ký tự đặc biệt của `LIKE` cũng được thoát khi tìm theo tên.
* **Dùng connection pool (HikariCP) và truyền `DataSource` qua constructor,** thay vì một biến `static Connection` hoặc mở kết nối mới mỗi lần. Một connection dùng chung dễ lỗi khi nhiều luồng cùng dùng hoặc khi bị đóng, còn mở mới mỗi lần thì chậm. Truyền `DataSource` từ ngoài vào còn cho phép test trỏ sang DB test mà không sửa repository. *Đánh đổi:* thêm một thư viện, và phải luôn trả connection về pool (try-with-resources).
* **Bọc `SQLException` thành `DataAccessException` (unchecked).** Tầng trên không phải khai báo `throws SQLException` hay phụ thuộc `java.sql`, nên sau này đổi công nghệ lưu trữ cũng ít ảnh hưởng. *Đánh đổi:* chi tiết lỗi gốc nằm trong `cause`, nên cần ghi log ở nơi bắt lỗi.
* **Hai phiên bản `findQueue` dùng chung một quy tắc sắp xếp** (`findQueue()` cho mọi đơn chưa đóng, `findQueue(statuses)` cho "việc của thợ"). Hai màn hình cần danh sách khác nhau nhưng cùng thứ tự ưu tiên; nếu viết hai hàm riêng thì dễ làm hai nơi sắp xếp lệch nhau.

*Cấu hình và kiểm thử*

* **Không commit mật khẩu:** cấu hình thật nằm trong `application.properties` (bị `.gitignore` chặn), repo chỉ chứa file `.example`. Mỗi thành viên có cấu hình riêng mà bí mật không lên GitHub.
* **Test chạy trên một DB riêng và dựng lại schema + seed trước mỗi lần chạy;** `TestDatabase` từ chối chạy nếu tên DB không kết thúc bằng `\_test`. Cách này tránh làm hỏng DB demo, và mọi test bắt đầu từ cùng một trạng thái nên kết quả lặp lại được. Seed data cũng có `created\_at` cố định để thứ tự hàng đợi không phụ thuộc ngày chạy. *Đánh đổi:* chạy chậm hơn so với chỉ rollback sau mỗi test.

**Thành viên 1 (Domain / Service):** *\[bổ sung sau]*

**Thành viên 3 (GUI):** *\[bổ sung sau]*

