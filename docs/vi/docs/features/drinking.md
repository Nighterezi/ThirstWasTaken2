# Uống nước

## Thứ gì đáng uống

Mọi vật phẩm hồi độ khát đều ghi rõ trong dòng mô tả bằng hai hàng giọt nước thay vì con số. Mỗi giọt
đáng hai điểm. Hàng trên dùng giọt đầy để biểu thị độ khát được hồi; hàng dưới dùng giọt khung để biểu
thị mức đã khát. Vì vậy chai nước hồi 6 độ khát và 8 đã khát sẽ hiện ba giọt đầy phía trên bốn giọt
khung.

![Dòng mô tả chai nước bẩn hiển thị các giọt độ khát và đã khát](/screenshots/item-tooltip.png)

Đây là các giá trị mod đặt sẵn cho vật phẩm gốc và vật phẩm của chính nó:

| Vật phẩm | Độ khát | Đã khát |
|---|---|---|
| Mọi loại thuốc, kể cả chai nước | 6 | 8 |
| Xô sữa | 6 | 8 |
| Súp củ dền | 5 | 7 |
| Chai mật ong | 4 | 6 |
| Lát dưa hấu | 4 | 5 |
| Bát đất nung đựng nước | 4 | 5 |
| Túi da đựng nước, mỗi lần uống | 4 | 5 |
| Táo, táo vàng, táo vàng phù phép, súp nấm, súp thỏ | 2 | 3 |
| Cà rốt, cà rốt vàng, củ dền, quả mọng ngọt, quả mọng phát sáng | 1 | 2 |

Độ khát làm đầy thanh, đã khát làm đầy phần dự trữ phía sau. Lượng nước vượt quá thanh đầy không bị bỏ
phí, nó chuyển thành dự trữ. Nước thuần tuân theo cách hoạt động của thức ăn vanilla: bạn không thể
dùng chai nước, bát nước hay túi da khi thanh khát đã đầy. Chỉ cần thiếu một điểm là có thể uống, và
phần hồi dư vẫn chuyển thành đã khát. Quy tắc này không chặn thuốc hay thức ăn có công dụng khác.

Hàng chục vật phẩm của Farmer's Delight, Farmer's Respite, Brewin' and Chewin', Collector's Reap và
Tough As Nails cũng đã có sẵn giá trị. Không mod nào trong số đó là bắt buộc, các giá trị
chỉ nằm im cho tới khi vật phẩm tồn tại. Với những thứ khác, xem
[dò theo từ khóa](/vi/docs/configuration#enablekeywordmatching).

## Các loại bát

Mọi thứ mod thêm vào đều nằm trong mục sáng tạo riêng của nó.

![Mục sáng tạo ThirstWasTaken2, chứa bát đất sét, bát đất nung, bát đất nung đựng nước và túi da đựng nước](/screenshots/creative-tab.png)

Mod thêm một loại bát chịu được việc đựng nước.

1. Ba cục đất sét xếp theo hình cái bát, đúng công thức bát gỗ, cho ra bốn **bát đất sét**.
2. Nung bát đất sét thành **bát đất nung**.
3. Cầm bát đất nung và dùng lên mặt nước để múc thành **bát đất nung đựng nước**. Nước chảy cũng được,
   bạn không cần khối nguồn.

Uống xong bạn cầm lại chiếc bát đất nung rỗng.

Mọi công thức mod thêm vào đều hiện trong sách công thức ngay khi bạn nhặt được một nguyên liệu của
nó, nên không cần tra cứu gì bên ngoài trò chơi.

Còn một công thức chế tạo nữa, bát đất nung cộng xô nước, và xô rỗng được trả lại. Nước làm theo cách
đó bị tính là bẩn, vì không có gì cho công thức biết chiếc xô đã đi qua đâu. Múc thẳng từ thế giới vừa
rẻ hơn vừa sạch hơn.

## Túi da đựng nước

Chế tạo **túi da đựng nước** có thể tái sử dụng từ ba miếng da và một sợi dây:

![Công thức túi da đựng nước dùng ba miếng da và một sợi dây](/screenshots/waterskin-recipe.png)

Túi chứa được ba lần uống. Dùng túi lên mặt nước hoặc vạc nước để thêm từng lần uống. Trong túi đồ,
nhấp chuột phải vào túi da bằng chai nước để thêm một lần uống, hoặc bằng xô nước để đổ đầy phần còn
thiếu; chai hay xô rỗng sẽ được trả lại. Ngồi xuống và dùng túi có nước lên một block để đổ bỏ
toàn bộ nước trong túi.

Khi trộn, túi lấy trung bình mức độ sạch của những lần uống bên trong rồi làm tròn xuống, nên thêm một
phần nước bẩn sẽ kéo cả túi đi xuống. Nước mặn là ngoại lệ: chỉ một phần nước mặn là cả túi thành nước
mặn. Mỗi lần uống hồi 4 độ khát và 5 đã khát, còn túi rỗng được giữ lại để nạp tiếp.

## Uống thẳng từ nguồn

**Ngồi xuống và dùng tay không lên mặt nước.** Tính năng này bật sẵn và có thể thay đổi bằng
[canDrinkByHand](/vi/docs/configuration#candrinkbyhand). Nó đáng giá kém hơn một bát nước đầy một chút,
và nó uống đúng thứ nước đang có ở đó, nên vũng nước đầm lầy mang theo đủ rủi ro của một vũng nước đầm
lầy. Giống các loại nước thuần khác, cách này không dùng được khi thanh khát đã đầy.

## Tìm nước ở đâu

Chai nước tự xuất hiện, ở mức có thể uống hoặc đã tinh lọc, mỗi lần từ một đến ba chai:

- Rương trong hầm mỏ bỏ hoang, ngục tối, tàu đắm, pháo đài Nether và thành lũy
- Đổi đồ với Piglin, tuy hiếm hơn nhiều so với trong rương

Chừng đó đủ để giữ mạng một người chơi ở Nether, nơi múc nước từ mặt đất không phải là một lựa chọn.

## Thành tựu

Mod có mục thành tựu riêng. Đây là một chuyến đi qua hệ thống nước chứ không phải danh sách việc phải
làm, nên phần lớn thành tựu có được nhờ uống nước.

| Thành tựu | Cách đạt được |
|---|---|
| Thirst Was Taken | Nhận ngay khi bắt đầu chơi |
| Ngụm đầu tiên | Uống ngụm nước đầu tiên |
| Ngụm nước đắng | Uống nước bẩn |
| Đun sôi | Lọc sạch một bình nước trong lò nung |
| Trong vắt | Uống nước ở mức tinh khiết nhất |
| Mặn chát | Uống nước biển |
| Nóng khô | Uống nước ở Nether, hoặc chiều không gian nào làm nước bốc hơi |
