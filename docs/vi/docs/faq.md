# Câu hỏi thường gặp

## Người chơi có bắt buộc cài mod không?

Có. Thanh khát do client vẽ, nên client thường sẽ không thấy gì trong khi máy chủ vẫn trừ độ khát của
họ. Hãy đưa file jar vào bộ pack.

## Vì sao tôi không chạy nhanh được?

Độ khát đang ở mức 6 hoặc thấp hơn. Uống gì đó, hoặc tắt
[preventSprintingWhenThirsty](/vi/docs/configuration#preventsprintingwhenthirsty).

## Vì sao máu của tôi không hồi?

Cũng vì lý do đó. Hồi máu tự nhiên chờ đến khi độ khát gần đầy, và phần thức ăn lẽ ra bị trừ sẽ được
hoàn lại để bạn không vừa khát vừa đói.

## Tôi đổi cấu hình mà không thấy gì thay đổi

Nếu bạn sửa file bằng tay, hãy khởi động lại game hoặc máy chủ. Nếu bạn dùng màn hình Mod Menu trong
lúc đang chơi trên máy chủ của người khác, chỉ bốn mục Giao diện có tác dụng, phần còn lại do máy chủ
quyết định.

## Tắt độ khát cho một người chơi được không?

Được, bằng [/thirst enable](/vi/docs/commands#thirst-enable). Thiết lập này lưu theo người chơi nên
thoát ra vào lại vẫn còn.

## Đồ uống của mod khác không có tác dụng

Id của nó chưa nằm trong danh sách. Thêm vào `drinks` trong file cấu hình, hoặc bật
[dò theo từ khóa](/vi/docs/configuration#enablekeywordmatching) để mod tự đoán.

## Có bắt buộc cài Create không?

Không. Không phần nào ở đây cần mod khác. Create Fly chỉ thêm Bộ lọc cát, còn Mod Menu chỉ thêm nút
mở màn hình cấu hình.

## Mod có chạy ở độ khó Hòa bình không?

Ở đó độ khát tự hồi lại, trừ khi bạn bật
[thirstDepletionInPeaceful](/vi/docs/configuration#thirstdepletioninpeaceful).

## Còn các tính năng khác của mod gốc thì sao?

Bản port chưa hoàn tất. [FABRIC-PORT.md](https://github.com/Nighterezi/ThirstWasTakenFabric/blob/main/FABRIC-PORT.md)
theo dõi phần nào đã xong, phần nào đang chờ mod khác cập nhật, và phần nào còn thiếu.
