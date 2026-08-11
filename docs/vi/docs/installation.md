# Cài đặt

## Tải về

Mở lần chạy thành công gần nhất của
[workflow build](https://github.com/Nighterezi/ThirstWasTaken2/actions/workflows/build.yml) rồi
lấy `ThirstWasTaken2-<phiên bản>.jar` trong phần artifacts. Bỏ qua file `-sources`, đó là bản
dành cho lập trình viên. Không có trình cài đặt, file jar chính là toàn bộ mod.

## Phiên bản được hỗ trợ

Mỗi phiên bản Minecraft có một nhánh riêng, và mỗi nhánh đánh số bản phát hành riêng. Tên bản phát
hành là `<minecraft>-<mod>`, ví dụ `26.2-1.0.0`.

Vì vậy số lớn hơn không đồng nghĩa với bản mới hơn. Nếu nhánh 1.21.11 nhận một bản sửa lỗi mà nhánh
26.2 đã có sẵn, nó sẽ ra mắt dưới tên `1.21.11-1.0.1` trong khi 26.2 vẫn đang ở `26.2-1.0.0`. Hãy
nhìn cột Minecraft trước, rồi lấy bản phát hành cao nhất trong hàng đó.

| Bản phát hành | Nhánh | Minecraft | Fabric Loader | Fabric API | Java | Trạng thái |
|---|---|---|---|---|---|---|
| `26.2-1.0.0` | `main` | 26.2 | 0.19.3 trở lên | 0.156.0+26.2 trở lên | 25 | Đang phát triển |

Nhánh Đang phát triển vẫn nhận bản sửa lỗi. Nhánh Đóng băng vẫn chạy được nhưng tính năng mới chỉ về
các nhánh đang phát triển.

### Cách đọc bảng

- **Fabric Loader** là số hiện trong tên profile của launcher. Bản mới hơn luôn dùng được.
- **Fabric API** phải khớp phiên bản Minecraft. Đuôi `+26.2` chính là phiên bản Minecraft mà nó được
  build cho, nên `0.156.0+26.2` sẽ không nạp được trên 1.21.11.
- **Java** là mức tối thiểu. Minecraft 26.2 đã kèm sẵn Java 25, nên runtime mặc định là đủ, trừ khi
  bạn chạy máy chủ bằng JDK riêng.
- Mod không nhắm tới bản snapshot. Một hàng chỉ xuất hiện khi nhánh đó build được trên bản chính
  thức.

## Mod tương thích

Không có mod nào ở đây là bắt buộc. Mod vẫn chạy y hệt khi thiếu chúng, chỉ là sẽ mở thêm phần hành
vi được liệt kê bên dưới nếu tìm thấy.

| Mod | Phiên bản đã thử | Minecraft | Thêm gì | Nếu thiếu |
|---|---|---|---|---|
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.156.0+26.2 | 26.2 | Bắt buộc. Sự kiện, mạng và các móc HUD mà mod dựa trên. | Mod sẽ không nạp được. |
| [Mod Menu](https://modrinth.com/mod/modmenu) | 20.0.1 | 26.2 | Nút Config trong danh sách Mods, mở [màn hình tùy chỉnh](/vi/docs/configuration). | Sửa `config/thirstwastaken2.json` bằng tay. |

::: warning Chưa hỗ trợ Create Fly
Bộ lọc cát đang lỗi ở bản này nên phần tích hợp Create Fly đã bị tắt. Cài Create cũng không thay đổi
gì: khối không chế tạo được và không thể lọc nước hàng loạt. Phần hỗ trợ sẽ quay lại ở bản sau.
:::

Những mod không nằm trong bảng thì cứ chạy song song bình thường. Mod đồ ăn thường không cần vá: món
nào có tên chứa từ khóa đồ uống, súp hoặc trái cây sẽ tự có giá trị cấp nước, phần còn lại thì khai
báo trong [Cấu hình](/vi/docs/configuration).

## Đặt mod ở đâu

Bỏ file jar vào thư mục `mods` của cả **máy chủ** lẫn **mọi client** tham gia máy chủ đó.

Máy chủ quyết định tốc độ mất nước và giá trị của nước. Client là nơi vẽ thanh khát. Người chơi không
cài mod sẽ không thấy thanh khát nào cả, nên trên máy chủ công khai thì mod này thuộc về bộ pack chứ
không nên để tùy chọn.

## Ngôn ngữ

Mỗi client thấy mod theo đúng ngôn ngữ game của họ. Mod kèm sẵn chín thứ tiếng: Anh, Pháp, Nhật, Hàn,
Ba Lan, Nga, Việt, Trung giản thể và Trung phồn thể. Máy chủ không cần cấu hình gì thêm.

## Lần chạy đầu tiên

Chạy game hoặc máy chủ một lần. Mod sẽ ghi ra `config/thirstwastaken2.json` với giá trị mặc định và ghi
log `ThirstWasTaken2 initialized for Minecraft 26.2`.

Độ khát được lưu trên người chơi nên thế giới cũ vẫn dùng được. Ai chưa từng được ghi nhận sẽ bắt đầu
với thanh khát đầy.
