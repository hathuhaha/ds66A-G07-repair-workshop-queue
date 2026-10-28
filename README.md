# Repair Workshop Queue

Ứng dụng Java Swing quản lý khách hàng, thiết bị, quy trình sửa chữa, báo giá,
bảo hành và lịch sử trạng thái. Project được biên dịch trực tiếp bằng `javac`,
không yêu cầu Maven.

## Cấu trúc project

```text
.
├── database/                         # SQL dùng để khởi tạo database local
│   ├── schema.sql
│   └── seed_data.sql
├── docs/                             # Tài liệu yêu cầu và thiết kế
├── lib/                              # PostgreSQL JDBC driver
├── src/
│   ├── com/oop/project/              # Java source root
│   │   ├── Main.java                 # Entry point, mở giao diện Home
│   │   ├── adapter/                  # Chuyển đổi/import dữ liệu legacy
│   │   ├── config/                   # Kết nối database và AppContext
│   │   ├── exception/                # Exception nghiệp vụ và truy cập dữ liệu
│   │   ├── model/                    # Entity và domain model
│   │   ├── repository/               # Repository interfaces, JDBC implementations,
│   │   │                              # row mappers
│   │   ├── service/                  # Nghiệp vụ repair job và estimate
│   │   ├── ui/                       # Swing screens, helpers và theme
│   │   ├── sample_data/              # Dữ liệu mẫu
│   │   └── util/                     # Tiện ích dùng chung
│   └── main/resources/db/            # Bản schema và seed được đóng cùng resources
│       ├── schema.sql
│       └── seed_data.sql
├── .env                              # Cấu hình database local (không commit)
└── README.md
```

`src/com/oop/project` là source root Java hiện tại; `src/main/resources/db` giữ
các script SQL ở vị trí resources. Hai bộ script trong `database/` và
`src/main/resources/db/` hiện có cùng nội dung.

## Những chức năng đã triển khai

- **Customers/Devices**: tìm kiếm và quản lý thông tin khách hàng, thiết bị;
  xem lịch sử repair của khách hàng và tạo repair job cho thiết bị.
- **Repair status**: cập nhật trạng thái từ Repair Queue hoặc lịch sử repair
  trong màn Customer; kiểm tra transition qua service và lưu status history.
- **Repair Queue**: lọc danh sách và xem trạng thái/hạn dự kiến của các repair.
- **Estimate**: chọn repair job, nhập parts và labor, xem tổng tiền, lưu/tải
  estimate, duyệt hoặc từ chối estimate. Trường nhập kiểm tra định dạng và range
  trước khi lưu.
- **Warranty**: xem/tìm kiếm warranties và repairs đủ điều kiện; tạo warranty
  từ repair được chọn; import dữ liệu legacy. Repair đã có warranty không còn là
  candidate để tạo thêm warranty.
- **Job History**: xem và lọc lịch sử chuyển trạng thái.
- **Input validation**: hiển thị lỗi ngay cạnh trường nhập cho ngày Warranty,
  ngày import, số liệu Estimate và số ngày khi tạo repair job.

Tài liệu này hướng dẫn **các bước bắt buộc** để sinh viên tạo repository cho project từ **template chính thức của học phần**, cũng như các **quy định về đặt tên, phân quyền và làm việc nhóm**.

⚠️ Không tuân thủ các bước dưới đây sẽ bị trừ điểm project.

---

## 1. Tạo repository từ template (BẮT BUỘC)

### Bước 1: Sử dụng template
1. Truy cập [Template Project Repository](https://github.com/fda-oop-java-anhpt/template-project)
.
2. Nhấn nút **Use this template**.
3. Chọn:
   - **Owner**: `fda-oop-java-anhpt`.
   - **Repository visibility**: Private.

---

### Bước 2: Đặt tên repository (BẮT BUỘC)

Tên repository phải theo **đúng định dạng sau**:
`<class>-pXX-<project-keyword>`


Trong đó:

| Thành phần | Ý nghĩa | Ví dụ |
|----------|--------|-------|
| `<class>` | Mã lớp <ai66a, ai66b> | `ai66a, ai66b` |
| `pXX` | Mã project (01 → 10) | `p01` |
| `project-keyword` | Tên project viết thường, không dấu, dùng dấu `-` | `yummy-catering` |

### Ví dụ hợp lệ:
```
ai66a-p01-yummy-catering
ai66b-p04-library-system
```

---

## 2. Thêm giảng viên vào repository (BẮT BUỘC)

Sau khi tạo repository, nhóm phải **thêm giảng viên làm collaborator với quyền Admin**.

### Các bước:
1. Vào **Settings** → **Collaborators**.
2. Thêm GitHub username của giảng viên: `anhpt204`
3. Chọn quyền **Admin**.
4. Gửi lời mời và đảm bảo giảng viên đã accept.

⚠️ Không thêm giảng viên = coi như **chưa nộp bài**.

---

## 3. Thêm các thành viên trong nhóm

### Yêu cầu:
- Mỗi sinh viên phải có **tài khoản GitHub cá nhân**.
- Không dùng chung tài khoản GitHub.

### Thực hiện:
1. Vào **Settings** → **Collaborators**.
2. Thêm GitHub username của từng thành viên.
3. Cấp quyền **Write** cho các thành viên.

---

## 4. Khai báo thông tin project và nhóm (BẮT BUỘC)

Sau khi tạo repository, nhóm phải cập nhật đầy đủ các file sau:

### 4.1. `docs/project-info.md`
Cần điền đầy đủ:
- Project ID
- Tên project
- Lớp
- Nhóm
- Danh sách thành viên và vai trò

---

### 4.2. `docs/requirements.md`
- Copy **toàn bộ nội dung đề bài project** được giao.
- Không được để trống file này.

---

## 5. Quy định làm việc với Git (BẮT BUỘC)

### 5.1. Commit
- Mỗi thành viên: **tối thiểu 20 commit**.
- Commit message phải rõ ràng, có ý nghĩa.

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

### 5.2. Thời gian commit
- Không dồn toàn bộ commit vào 1–2 ngày cuối.
- Mỗi tuần ít nhất 1 commit
- Lịch sử commit sẽ được sử dụng để đánh giá mức độ đóng góp cá nhân.

---

## 6. Sử dụng AI coding tools

Sinh viên **được phép** sử dụng các AI coding tools (ChatGPT, Copilot, v.v.).

### BẮT BUỘC:
- Phải khai báo trung thực trong file: `docs/ai-usage.md`

Nội dung cần ghi:
- AI tool đã sử dụng.
- Prompt mẫu.
- Phần code có AI hỗ trợ.
- Phần code do sinh viên tự viết hoặc chỉnh sửa.

⚠️ Không khai báo AI usage được xem là **vi phạm học thuật**.

---

## 7. Checklist trước khi nộp bài

Trước khi nộp, nhóm phải tự kiểm tra:

- [ ] Repository được tạo từ template chính thức.
- [ ] Tên repository đúng định dạng.
- [ ] Đã thêm giảng viên với quyền Admin.
- [ ] Đã thêm đầy đủ các thành viên trong nhóm.
- [ ] `docs/project-info.md` đã điền đầy đủ.
- [ ] `docs/requirements.md` không để trống.
- [ ] Có commit đều của các thành viên.

---

## 8. Lưu ý quan trọng

- Mỗi nhóm chỉ có **01 repository duy nhất**.
- Không đổi tên repository sau khi đã bắt đầu làm project.
- Mọi chỉnh sửa và trao đổi kỹ thuật phải thể hiện qua **commit trên GitHub**.

- Repository là sản phẩm học tập chính thức của nhóm.  
- Giảng viên sẽ đánh giá dựa trên code, lịch sử commit và khả năng giải thích khi bảo vệ project.  
- Các vấn đề phát sinh do không đọc kỹ README sẽ không được giải quyết ngoại lệ.

## Cấu hình database

Tạo database PostgreSQL `repair-workshop-queue`. Chạy script theo thứ tự:

1. `src/main/resources/db/schema.sql` (hoặc bản tương ứng trong `database/`).
2. `src/main/resources/db/seed_data.sql` (hoặc bản tương ứng trong `database/`).

**Lưu ý:** các script tạo lại schema và dữ liệu mẫu, có thể xóa dữ liệu đang có.
Chỉ chạy trên database mới/dev/test, không chạy trên database có dữ liệu cần giữ.

## Chạy ứng dụng (không dùng Maven)

Tạo file `.env` tại thư mục gốc:

```text
DB_URL=jdbc:postgresql://localhost:5432/repair-workshop-queue
DB_USERNAME=your_postgresql_username
DB_PASSWORD=your_postgresql_password
```

Đặt PostgreSQL JDBC driver trong `lib`, rồi biên dịch và chạy từ PowerShell:

```powershell
Get-ChildItem .\src -Recurse -Filter *.java |
    ForEach-Object { $_.FullName } |
    Set-Content -Encoding ASCII .\sources.txt

javac -encoding UTF-8 -cp ".\lib\*" -d ".\out" "@sources.txt"
java -cp ".\out;.\lib\*" com.oop.project.Main
```

Các biến `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` cũng có thể được đặt trong
environment thay cho file `.env`. PostgreSQL JDBC driver cần có trong `lib/`.

File legacy bảo hành có một bản ghi mỗi dòng, theo định dạng
`deviceId|COVERED|description`, `deviceId|EXPIRED|description` hoặc
`deviceId|NOT_COVERED|description`. Khi nhập, điền ngày bắt đầu/kết thúc thực tế
cho các bản ghi `COVERED` và `EXPIRED`; `NOT_COVERED` không được lưu thành bảo
hành. Nhập lại cùng file sẽ nhận diện các dòng đã nhập trước đó.
