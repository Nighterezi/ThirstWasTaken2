# Lệnh

Tất cả nằm dưới `/thirst`. Cả ba lệnh con đều cần quyền cấp 2, mức mà quản trị viên và khối lệnh vẫn
có, nên người chơi thường không dùng được.

## /thirst query

```
/thirst query <người chơi>
```

In ra mức khát và mức đã khát của người chơi đó, chỉ cho người gõ lệnh thấy.

## /thirst set

```
/thirst set <những người chơi> <độ khát> <đã khát>
```

Hai con số nằm trong khoảng 0 đến 20, và phần người chơi nhận bộ chọn nên `@a` dùng được.

Mức đã khát không bao giờ vượt quá mức khát, nên `/thirst set @s 5 20` để lại người chơi ở mức 5 và 5.
Kết quả được thông báo cho các quản trị viên, giống như lệnh gốc của trò chơi.

## /thirst enable

```
/thirst enable <những người chơi> <true|false>
```

Tắt hẳn hệ thống cho những người chơi đó. Khi đang tắt, họ không mất nước, không bị sát thương vì mất
nước, và giữ nguyên mức mà thanh khát đang hiển thị lúc bạn tắt.

Thiết lập này theo từng người chơi và được lưu cùng họ, nên đây là công cụ hợp lý cho người xem, cho
thợ xây trên máy chủ sinh tồn, hoặc cho quản trị viên không muốn thấy thanh khát.

Nếu muốn tắt độ khát cho tất cả mọi người, hãy để nguyên trạng thái bật rồi đặt các tốc độ mất nước
về `0` trong [Cấu hình](/vi/docs/configuration#thirstdepletionmodifier).
