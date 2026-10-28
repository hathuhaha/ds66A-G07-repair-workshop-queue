Repair Workshop Queue (G07)
Ứng dụng quản lý hàng đợi sửa chữa thiết bị: tiếp nhận thiết bị, lập báo giá, theo dõi trạng thái đơn và xếp hàng ưu tiên theo hạn chót. Java 17 + Maven, PostgreSQL, JDBC thuần + HikariCP, test bằng JUnit 5.
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
Nếu tiếng Việt trong seed bị lỗi font trên Windows, chạy `set PGCLIENTENCODING=UTF8` (cmd) hoặc `$env:PGCLIENTENCODING="UTF8"` (PowerShell) trước lệnh `psql`.
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
Chạy ứng dụng và kiểm tra nhanh
```bash
mvn compile exec:java                 # chạy Main
mvn compile exec:java -Pdbsmoke       # kiểm tra tay trên repair_shop (ghi thêm dữ liệu, xong thì chạy lại 2 file SQL)
```
Tài liệu
`docs/contract.md`: hợp đồng dữ liệu giữa các thành viên
`docs/design.md`: thiết kế, gồm sơ đồ bảng và lý do thiết kế cơ sở dữ liệu
`docs/ai-usage.md`: khai báo sử dụng AI
Quy tắc khi tạo pull request
Không commit các thư mục/file sau (đã có trong `.gitignore`): `target/`, `src/main/resources/application.properties`, `src/test/resources/application-test.properties`. Chỉ commit hai file `*.properties.example`. Trước khi push, chạy `git status` để chắc chắn ba mục trên không xuất hiện.
---
Quy định học phần (từ template)
OOP Java Project – Repository Setup Guide
Tài liệu này hướng dẫn các bước bắt buộc để sinh viên tạo repository cho project từ template chính thức của học phần, cũng như các quy định về đặt tên, phân quyền và làm việc nhóm.
⚠️ Không tuân thủ các bước dưới đây sẽ bị trừ điểm project.
---
1. Tạo repository từ template (BẮT BUỘC)
Bước 1: Sử dụng template
Truy cập Template Project Repository
.
Nhấn nút Use this template.
Chọn:
Owner: `fda-oop-java-anhpt`.
Repository visibility: Private.
---
Bước 2: Đặt tên repository (BẮT BUỘC)
Tên repository phải theo đúng định dạng sau:
`<class>-pXX-<project-keyword>`

Trong đó:
Thành phần	Ý nghĩa	Ví dụ
`<class>`	Mã lớp <ai66a, ai66b>	`ai66a, ai66b`
`pXX`	Mã project (01 → 10)	`p01`
`project-keyword`	Tên project viết thường, không dấu, dùng dấu `-`	`yummy-catering`
Ví dụ hợp lệ:
```
ai66a-p01-yummy-catering
ai66b-p04-library-system
```
---
2. Thêm giảng viên vào repository (BẮT BUỘC)
Sau khi tạo repository, nhóm phải thêm giảng viên làm collaborator với quyền Admin.
Các bước:
Vào Settings → Collaborators.
Thêm GitHub username của giảng viên: `anhpt204`
Chọn quyền Admin.
Gửi lời mời và đảm bảo giảng viên đã accept.
⚠️ Không thêm giảng viên = coi như chưa nộp bài.
---
3. Thêm các thành viên trong nhóm
Yêu cầu:
Mỗi sinh viên phải có tài khoản GitHub cá nhân.
Không dùng chung tài khoản GitHub.
Thực hiện:
Vào Settings → Collaborators.
Thêm GitHub username của từng thành viên.
Cấp quyền Write cho các thành viên.
---
4. Khai báo thông tin project và nhóm (BẮT BUỘC)
Sau khi tạo repository, nhóm phải cập nhật đầy đủ các file sau:
4.1. `docs/project-info.md`
Cần điền đầy đủ:
Project ID
Tên project
Lớp
Nhóm
Danh sách thành viên và vai trò
---
4.2. `docs/requirements.md`
Copy toàn bộ nội dung đề bài project được giao.
Không được để trống file này.
---
5. Quy định làm việc với Git (BẮT BUỘC)
5.1. Commit
Mỗi thành viên: tối thiểu 20 commit.
Commit message phải rõ ràng, có ý nghĩa.
Ví dụ commit hợp lệ:
```
Add abstract class User
Implement login validation
Refactor OrderService logic
```
Ví dụ commit KHÔNG chấp nhận:
```
update
fix
done
```

---
5.2. Thời gian commit
Không dồn toàn bộ commit vào 1–2 ngày cuối.
Mỗi tuần ít nhất 1 commit
Lịch sử commit sẽ được sử dụng để đánh giá mức độ đóng góp cá nhân.
---
6. Sử dụng AI coding tools
Sinh viên được phép sử dụng các AI coding tools (ChatGPT, Copilot, v.v.).
BẮT BUỘC:
Phải khai báo trung thực trong file: `docs/ai-usage.md`
Nội dung cần ghi:
AI tool đã sử dụng.
Prompt mẫu.
Phần code có AI hỗ trợ.
Phần code do sinh viên tự viết hoặc chỉnh sửa.
⚠️ Không khai báo AI usage được xem là vi phạm học thuật.
---
7. Checklist trước khi nộp bài
Trước khi nộp, nhóm phải tự kiểm tra:
[ ] Repository được tạo từ template chính thức.
[ ] Tên repository đúng định dạng.
[ ] Đã thêm giảng viên với quyền Admin.
[ ] Đã thêm đầy đủ các thành viên trong nhóm.
[ ] `docs/project-info.md` đã điền đầy đủ.
[ ] `docs/requirements.md` không để trống.
[ ] Có commit đều của các thành viên.
---
8. Lưu ý quan trọng
Mỗi nhóm chỉ có 01 repository duy nhất.
Không đổi tên repository sau khi đã bắt đầu làm project.
Mọi chỉnh sửa và trao đổi kỹ thuật phải thể hiện qua commit trên GitHub.
Repository là sản phẩm học tập chính thức của nhóm.
Giảng viên sẽ đánh giá dựa trên code, lịch sử commit và khả năng giải thích khi bảo vệ project.
Các vấn đề phát sinh do không đọc kỹ README sẽ không được giải quyết ngoại lệ.