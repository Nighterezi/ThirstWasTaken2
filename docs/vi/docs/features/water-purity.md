# Chất lượng nước

Nước có hai loại. **Nước ngọt** có mức độ sạch, từ bẩn đến tinh khiết. **Nước mặn** không có mức độ
sạch nào cả: không thể làm sạch được, và không bao giờ giải khát. Mỗi vật chứa đều nhớ nó đang đựng
loại nào, và ghi rõ trong dòng mô tả.

![Dòng mô tả chai nước hiển thị các mức độ sạch của nước ngọt](/screenshots/water-purity.png)

## Bốn mức độ sạch

Từ tệ đến tốt: **Bẩn**, **Đục**, **Sạch**, **Tinh khiết**. Mức này được xác định một lần, khi nước
được múc hoặc uống trực tiếp, và đi theo vật chứa từ đó.

| Nguồn nước | Mức thường gặp |
|---|---|
| Đầm lầy hoặc đầm lầy ngập mặn | Bẩn |
| Rừng rậm, xavan hoặc badlands | Bẩn |
| Phần lớn biome khác | Đục |
| Sông | Đục |
| Núi | Sạch |
| Đỉnh núi lạnh | Tinh khiết |

Biome rất nóng làm nước xấu hơn, biome rất lạnh làm nước tốt hơn. Nước trên y 100 hoặc dưới y 32 sạch
hơn một chút, nước chảy cũng vậy, nên thác nước không tự động an toàn. Bùn, rễ đước, đất trồng hoặc
composter trong phạm vi hai block làm nước xấu đi.

Modpack có thể thêm biome vào tag `thirstwastaken2:stagnant_water` mà không cần sửa code. Nước không
mang mức độ sạch riêng, ví dụ đồ uống từ mod khác, sẽ dùng
[defaultPurity](/vi/docs/configuration#defaultpurity).

## Nước mặn

Biển và bãi biển cho nước mặn. Nước mặn có biểu tượng riêng và dòng mô tả riêng, nên nhìn là phân
biệt được với nước ngọt, và không hiển thị giọt nước nào vì nó không hồi gì cả.

Uống nước mặn còn làm tụt độ khát thay vì hồi, kèm theo Buồn nôn trong năm giây. Lò nung và lửa trại
không nhận nước mặn, nên không có cách nào làm nó uống được. Chỉ cần một lần nước mặn đổ vào túi da
hoặc vạc là toàn bộ chỗ nước trong đó thành nước mặn.

## Trộn nước và vạc

Túi da tính trung bình mức độ sạch của những lần uống đang chứa, theo số lượng, rồi làm tròn xuống.
Hai phần nước tinh khiết trộn với một phần nước bẩn cho ra nước sạch, nên một ngụm nước tốt không cứu
được cả mẻ nước xấu.

Vạc giữ mức tệ hơn giữa phần đang chứa và phần đổ vào. Nước múc lại ra chai, xô hoặc túi da vẫn mang
mức đó.

Vạc cũng tự đầy lên, và mỗi cách đầy có mức độ sạch riêng.

| Cách vạc đầy nước | Mức độ sạch |
|---|---|
| Nước mưa | Sạch, đặt bằng [rainwaterPurity](/vi/docs/configuration#rainwaterpurity) |
| Nhũ đá nhọn nhỏ nước xuống | Tinh khiết, đặt bằng [dripstonePurity](/vi/docs/configuration#dripstonepurity) |

Nước mưa là thứ miễn phí và không cần xây gì, nên nó tốt nhưng chưa phải loại tốt nhất. Nhũ đá thì
phải đặt dưới một nguồn nước với vạc ở bên dưới, và nó chảy rất chậm, nhưng nước đã thấm qua đá nên
sạch ngang với việc đun sôi. Cả hai đều không làm sạch phần nước đã có trong vạc: mưa rơi vào nước bẩn
thì nước vẫn bẩn.

## Uống nước xấu

Nước ngọt luôn giải khát, bất kể mức độ sạch. Chỉ có rủi ro là thay đổi.

| Mức | Buồn nôn và Đói | Trúng độc |
|---|---|---|
| Bẩn | 100% | 30% |
| Đục | 50% | 10% |
| Sạch | 5% | không |
| Tinh khiết | không | không |

Buồn nôn kéo dài năm giây, Đói kéo dài ba mươi giây và Trúng độc kéo dài mười giây. Cơ chế nhiễm bệnh
dài hạn chưa nằm trong bản này.

## Làm sạch nước ngọt

Bỏ chai nước ngọt, bát đất nung đựng nước hoặc xô nước vào lò nung hay đặt lên lửa trại.

| Trước | Sau |
|---|---|
| Bẩn | Sạch |
| Đục | Tinh khiết |
| Sạch | Tinh khiết |

Lò nung mất mười giây và lửa trại mất ba mươi giây. Nước bẩn cần qua hai lượt để thành tinh khiết.
