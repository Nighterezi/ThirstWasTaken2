
# Cài đặt

## Tải về

Tải mod trực tiếp từ **[Modrinth](https://modrinth.com/mod/thirst-was-taken-2)** (khuyên dùng).

Ngoài ra, bạn cũng có thể mở lần chạy thành công gần nhất của
[workflow build](https://github.com/Nighterezi/ThirstWasTaken2/actions/workflows/build.yml) rồi
lấy `ThirstWasTaken2-<phiên bản>.jar` trong phần artifacts. Bỏ qua file `-sources`, đó là bản
dành cho lập trình viên. Không có trình cài đặt, file jar chính là toàn bộ mod.

## Phiên bản được hỗ trợ

Một bản phát hành bao gồm mọi phiên bản Minecraft được hỗ trợ, và tất cả dùng chung một số phiên bản
mod. Phiên bản Minecraft nằm ở đuôi tên tệp, ví dụ `ThirstWasTaken2-1.0.3+1.21.11.jar`. Hãy lấy tệp
có đuôi khớp với game.

| Minecraft | Đuôi tệp | Fabric Loader | Fabric API | Java |
|---|---|---|---|---|
| 26.2 | `+26.2` | 0.19.3 trở lên | 0.160.0+26.2 trở lên | 25 |
| 26.1, 26.1.1, 26.1.2 | `+26.1.2` | 0.19.3 trở lên | 0.155.3+26.1.2 trở lên | 25 |
| 1.21.11 | `+1.21.11` | 0.19.3 trở lên | 0.141.6+1.21.11 trở lên | 21 |

### Cách đọc bảng

- **Fabric Loader** là số hiện trong tên profile của launcher. Bản mới hơn luôn dùng được.
- **Fabric API** phải khớp phiên bản Minecraft. Đuôi `+26.2` chính là phiên bản Minecraft mà nó được
  build cho, nên `0.160.0+26.2` sẽ không nạp được trên 1.21.11.
- **Java** là mức tối thiểu. Minecraft đã kèm sẵn runtime phù hợp, nên runtime mặc định là đủ, trừ
  khi bạn chạy máy chủ bằng JDK riêng.
- Mod không nhắm tới bản snapshot. Một hàng chỉ xuất hiện khi mod build được trên bản chính thức.

## Mod tương thích

Không có mod nào ở đây là bắt buộc. Mod vẫn chạy y hệt khi thiếu chúng, chỉ là sẽ mở thêm phần hành
vi được liệt kê bên dưới nếu tìm thấy.

Phiên bản đã thử được liệt kê theo từng phiên bản Minecraft, thứ tự 26.2, 26.1.x, 1.21.11.

| Mod | Phiên bản đã thử | Thêm gì | Nếu thiếu |
|---|---|---|---|
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.160.0+26.2, 0.155.3+26.1.2, 0.141.6+1.21.11 | Bắt buộc. Sự kiện, mạng và các móc HUD mà mod dựa trên. | Mod sẽ không nạp được. |
| [Mod Menu](https://modrinth.com/mod/modmenu) | 20.0.1, 18.0.0, 17.0.0 | Nút Config trong danh sách Mods, mở [màn hình tùy chỉnh](/vi/docs/configuration). | Sửa `config/thirstwastaken2.json` bằng tay. |
| [AppleSkin](https://modrinth.com/mod/appleskin) | 3.0.10+mc26.2, 3.0.10+mc26.1.2, 3.0.8+mc1.21.11 | Dải tiêu hao dạng hạt trên thanh khát, theo tùy chọn HUD underlay của AppleSkin. | Thanh khát vẫn hiển thị bình thường nhưng không có dải này. |
| [Cloth Config](https://modrinth.com/mod/cloth-config) | 26.2.155, 26.1.154, 21.11.153 | Màn hình cấu hình AppleSkin bên trong Mod Menu. | AppleSkin vẫn chạy nhưng nút Config của nó không dùng được. |

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
log `ThirstWasTaken2 initialized for Minecraft` kèm phiên bản mà tệp được build cho.

Độ khát được lưu trên người chơi nên thế giới cũ vẫn dùng được. Ai chưa từng được ghi nhận sẽ bắt đầu
với thanh khát đầy.
