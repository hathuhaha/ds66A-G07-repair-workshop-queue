Repair Workshop Queue (G07)
Ứng dụng quản lý hàng đợi sửa chữa thiết bị: tiếp nhận thiết bị, lập báo giá, theo dõi trạng thái đơn và xếp hàng ưu tiên theo hạn chót.
Công nghệ: Java 17 + Maven, PostgreSQL, JDBC thuần + HikariCP, test bằng JUnit 5.
Mục lục
Hướng dẫn cài đặt và chạy test
Chạy ứng dụng và kiểm tra nhanh
Tài liệu
Quy tắc khi tạo pull request
Quy định học phần (từ template)
---
Hướng dẫn cài đặt và chạy test
Yêu cầu
JDK 17 trở lên (`pom.xml` đang đặt `maven.compiler.release` = 17)
Maven 3.x
PostgreSQL chạy local (mặc định cổng 5432) và công cụ `psql`
Bước 1. Tạo hai database
Mỗi máy cần hai database trống:
```bash
psql -U postgres -c "CREATE DATABASE repair_shop;"
psql -U postgres -c "CREATE DATABASE repair_shop_test;"
```
Database	Mục đích
`repair_shop`	Chạy ứng dụng và demo, chứa seed data ổn định
`repair_shop_test`	Chạy test, bị xóa và tạo lại dữ liệu mỗi lần test
Bước 2. Chạy hai file SQL
Chạy từ thư mục gốc của project, theo đúng thứ tự `schema.sql` rồi `seed_data.sql`:
```bash
psql -U postgres -d repair_shop -f src/main/resources/db/schema.sql
psql -U postgres -d repair_shop -f src/main/resources/db/seed_data.sql
```
`schema.sql` xóa (`DROP TABLE`) rồi tạo lại toàn bộ bảng, nên chỉ chạy trên database dev/test, không chạy trên database có dữ liệu cần giữ.
`seed_data.sql` xóa sạch dữ liệu rồi nạp lại dữ liệu mẫu cho các kịch bản demo (`docs/contract.md` mục 10). Chạy lại bất cứ lúc nào để đưa `repair_shop` về trạng thái ban đầu.
Database `repair_shop_test` không cần chạy tay hai file này: mỗi test tự chạy lại cả hai file trước khi bắt đầu (`TestDatabase.reset()`).
> **Lưu ý (Windows):** nếu tiếng Việt trong seed bị lỗi font, chạy lệnh sau trước `psql`:
>
> - cmd: `set PGCLIENTENCODING=UTF8`
> - PowerShell: `$env:PGCLIENTENCODING="UTF8"`
Bước 3. Chép hai file cấu hình
Hai file `*.properties` chứa mật khẩu máy của bạn nên không được commit (đã nằm trong `.gitignore`). Chép từ file mẫu rồi sửa `db.user`, `db.password` cho đúng máy mình:
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
cp src/test/resources/application-test.properties.example src/test/resources/application-test.properties
```
(Windows cmd dùng `copy` thay cho `cp`.)
File	Trỏ tới	Dùng cho
`src/main/resources/application.properties`	`repair_shop`	Chạy ứng dụng, `DbSmoke`
`src/test/resources/application-test.properties`	`repair_shop_test`	`mvn test`
`db.url` trong `application-test.properties` bắt buộc kết thúc bằng `_test`; nếu không, test từ chối chạy để tránh xóa nhầm dữ liệu thật.
Bước 4. Chạy test
```bash
mvn test
```
Test cần PostgreSQL đang chạy, database `repair_shop_test` đã được tạo và `application-test.properties` đã được chép.
Lỗi thường gặp
Thông báo	Nguyên nhân
`Config file not found on the classpath: application-test.properties`	Chưa chép file ở Bước 3
`Refusing to run tests on '...'`	`db.url` của file test không kết thúc bằng `_test`
`password authentication failed`	Sai `db.user` / `db.password`
`database "repair_shop_test" does not exist`	Chưa tạo database ở Bước 1
---
Chạy ứng dụng và kiểm tra nhanh
```bash
mvn compile exec:java                 # chạy Main
mvn compile exec:java -Pdbsmoke       # kiểm tra tay trên repair_shop (ghi thêm dữ liệu, xong thì chạy lại 2 file SQL)
```
---
Tài liệu
File	Nội dung
`docs/contract.md`	Hợp đồng dữ liệu giữa các thành viên
`docs/design.md`	Thiết kế, gồm sơ đồ bảng và lý do thiết kế cơ sở dữ liệu
`docs/ai-usage.md`	Khai báo sử dụng AI
---
Quy tắc khi tạo pull request
Không commit các thư mục/file sau (đã có trong `.gitignore`):
`target/`
`src/main/resources/application.properties`
`src/test/resources/application-test.properties`
Chỉ commit hai file `*.properties.example`. Trước khi push, chạy `git status` để chắc chắn ba mục trên không xuất hiện.
