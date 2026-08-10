# Cài đặt

## Tải về

Mở lần chạy thành công gần nhất của
[workflow build](https://github.com/Nighterezi/ThirstWasTakenFabric/actions/workflows/build.yml) rồi
lấy `ThirstWasTakenFabric-<phiên bản>.jar` trong phần artifacts. Bỏ qua file `-sources`, đó là bản
dành cho lập trình viên. Không có trình cài đặt, file jar chính là toàn bộ mod.

## Yêu cầu

| Thành phần | Phiên bản |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 trở lên |
| Fabric API | 0.156.0+26.2 trở lên |
| Java | 25 |

## Đặt mod ở đâu

Bỏ file jar vào thư mục `mods` của cả **máy chủ** lẫn **mọi client** tham gia máy chủ đó.

Máy chủ quyết định tốc độ mất nước và giá trị của nước. Client là nơi vẽ thanh khát. Người chơi không
cài mod sẽ không thấy thanh khát nào cả, nên trên máy chủ công khai thì mod này thuộc về bộ pack chứ
không nên để tùy chọn.

## Mod Menu

Không bắt buộc. Nếu có Mod Menu, Thirst Was Taken sẽ có nút Config trong danh sách Mods, mở ra màn
hình tùy chỉnh được mô tả ở [Cấu hình](/vi/docs/configuration). Nếu không có, hãy sửa file cấu hình
bằng tay.

## Create Fly

Cũng không bắt buộc. Nếu có [Create Fly](https://modrinth.com/mod/create-fly), khối Bộ lọc cát sẽ
được đăng ký và chế tạo được. Xem [Độ sạch của nước](/vi/features/water-purity#bo-loc-cat).

Gỡ Create khỏi một thế giới đã đặt sẵn Bộ lọc cát sẽ để lại khối bị thiếu, nên hãy phá chúng trước.

## Ngôn ngữ

Mỗi client thấy mod theo đúng ngôn ngữ game của họ. Mod kèm sẵn chín thứ tiếng: Anh, Pháp, Nhật, Hàn,
Ba Lan, Nga, Việt, Trung giản thể và Trung phồn thể. Máy chủ không cần cấu hình gì thêm.

## Lần chạy đầu tiên

Chạy game hoặc máy chủ một lần. Mod sẽ ghi ra `config/thirstwastaken.json` với giá trị mặc định và ghi
log `Thirst Was Taken Fabric initialized for Minecraft 26.2`.

Độ khát được lưu trên người chơi nên thế giới cũ vẫn dùng được. Ai chưa từng được ghi nhận sẽ bắt đầu
với thanh khát đầy.
