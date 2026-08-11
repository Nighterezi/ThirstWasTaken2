# Khát và Đã khát

![Thanh khát nằm trên thanh đói, đã vơi một phần](/screenshots/thirst-bar.png)

## Hai con số

Độ khát chạy từ 0 đến 20 và được vẽ thành mười giọt nước, nên một giọt bằng hai điểm. Người chơi mới
bắt đầu với thanh đầy.

Mức đã khát là phần dự trữ nằm chồng lên trên, đúng như độ no nằm trên thanh đói. Nó được vẽ thành
viền sáng phủ lên các giọt nước và bắt đầu ở mức 5. Mọi thứ lẽ ra trừ vào độ khát sẽ ăn vào phần dự
trữ trước, và đó là lý do một ngụm nước tốt trụ được lâu hơn ngụm nước xấu dù cả hai cùng làm đầy
thanh.

Khi phần dự trữ cạn, các giọt nước rung lên, đúng kiểu cảnh báo của thanh đói.

## Cái gì làm nó vơi đi

Độ khát dùng chung mức tiêu hao mà trò chơi vốn đã tính cho cơn đói. Chạy nhanh, nhảy, bơi, đào, tấn
công và ăn sát thương đều tính. Cứ 4 điểm tiêu hao thì mất một điểm đã khát, hoặc một điểm khát khi
phần dự trữ đã cạn.

Khi cài AppleSkin, phía sau các giọt của thanh khát còn có một dải tiêu hao dạng hạt. Dải này đầy dần
từ phải sang trái khi mức tiêu hao tiến tới 4 và tuân theo tùy chọn **Food Exhaustion HUD Underlay**
của AppleSkin. AppleSkin không bắt buộc; nếu không cài, độ khát và mức đã khát vẫn hoạt động và hiển
thị bình thường.

Ngồi trên ngựa, thuyền hay xe mỏ thì không tốn gì. Người chơi ở chế độ Sáng tạo và Khán giả được bỏ
qua hoàn toàn.

Bên cạnh đó, nơi bạn đang đứng nhân thêm chi phí:

| Bạn đang ở đâu | Ảnh hưởng |
|---|---|
| Quần xã nóng hoặc khô | Vơi nhanh hơn |
| Quần xã lạnh hoặc nhiều mưa | Vơi chậm hơn |
| Nether, hoặc chiều không gian nào làm nước bốc hơi | Một mức cố định, nặng hơn hẳn |

Hai hiệu ứng đẩy lùi điều đó. Kháng lửa giảm một nửa tốc độ mất nước, còn Chống lửa trên giáp giảm
thêm nữa, sâu nhất là còn một phần tư mức bình thường. Buồn nôn thì ngược lại, cộng thêm một mức tiêu
hao đều đặn suốt thời gian hiệu ứng. Đói thì không, vì phần tiêu hao mà nó vốn gây ra chỉ được tính
một lần chứ không tính hai.

Ở độ khó Hòa bình, thanh khát tự hồi lại thay vì vơi đi, trừ khi máy chủ tắt điều đó.

Mọi tốc độ kể trên đều là một tùy chỉnh. Xem [Cấu hình](/vi/docs/configuration#tieu-hao-khat-nuoc).

## Khi sắp cạn

Từ mức 6 trở xuống bạn không thể bắt đầu chạy nhanh, đúng ngưỡng trò chơi dùng cho cơn đói.

Hồi máu tự nhiên dừng lại cho tới khi độ khát gần đầy. Ở mức 19 nó chạy lại, tuy phần hồi máu nhanh
theo độ no chỉ kích hoạt với tần suất bằng một phần tám. Lượng máu bạn không hồi cũng không tốn thức
ăn, nên người chơi đang khát không âm thầm chết đói theo.

## Khi cạn sạch

![Thanh khát rỗng, máu chỉ còn hai trái tim rưỡi](/screenshots/dehydration.png)

Thanh rỗng lấy của bạn nửa trái tim mỗi hai giây. Sát thương này bỏ qua giáp, và dòng thông báo khi
chết ghi là `đã chết vì mất nước`.

Độ khó quyết định nó đi xa tới đâu. Ở Dễ, sát thương dừng lại khi bạn còn năm trái tim. Ở Thường và
Khó, nó giết bạn.
