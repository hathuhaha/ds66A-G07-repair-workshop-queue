# AI Usage

Khai báo theo quy định học phần (README mục 6). Phần "Bước E" do Thành viên 2 khai; các thành viên khác bổ sung phần của mình.

---

## Bước E: Tài liệu và bàn giao (Thành viên 2)

### AI tools used (if any)

Claude (Anthropic), qua giao diện chat.

### Prompt mẫu

> "Dựa trên file zip tôi đã gửi, hãy làm Bước E: Tài liệu và bàn giao"

Kèm yêu cầu:

- `README`: cách tạo hai database, chạy hai file SQL, chép hai file `*.properties.example`, chạy `mvn test`.
- `docs/contract.md`: mục L16, trạng thái `[ĐÃ CHỐT]` của `version`, nhật ký mục 12.
- `docs/design.md`: sơ đồ bảng, lý do `RESTRICT`, `version`, hạn chót tính theo biểu thức.
- `docs/ai-usage.md`.
- Nhắc không commit `target/`, `application.properties`, `application-test.properties`.

### Code nào do AI gợi ý?

Không có code Java hay SQL nào được viết trong bước này. AI soạn nội dung văn bản cho:

- `README.md`
- `docs/design.md` (mục 6)
- `docs/contract.md` (mục 4.4, 11, 12)
- file này

dựa trên nội dung `schema.sql`, `seed_data.sql`, `EstimateRepositoryImpl`, `RepairJobRepositoryImpl` và `contract.md` hiện có.

### Code nào tự chỉnh sửa?

Thành viên 2 đọc lại, đối chiếu với code thật và chịu trách nhiệm về nội dung các tài liệu trên. Chưa chạy `mvn test` sau khi AI soạn tài liệu.

---

## Các phần khác của project

Chưa khai trong file này. Mỗi thành viên ghi theo mẫu: AI tool đã dùng, prompt mẫu, file/đoạn code có AI hỗ trợ, phần tự viết hoặc tự sửa.

| Thành viên | Phụ trách                                    | Trạng thái                                                              |
| ---------- | -------------------------------------------- | ----------------------------------------------------------------------- |
| Thành viên 1 | Domain/Model, Service                      | Chưa điền                                                               |
| Thành viên 2 | Database, Repository, test repository      | Điền nếu đã dùng AI cho `schema.sql`, `seed_data.sql`, mapper, repository, test |
| Thành viên 3 | UI                                         | Chưa điền                                                               |
