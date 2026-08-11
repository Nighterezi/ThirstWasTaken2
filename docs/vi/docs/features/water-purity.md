# Chất lượng nước

Mỗi vật chứa lưu mức nhiễm bẩn và việc nước có mặn hay không. Dòng mô tả và sprite của vật phẩm tóm
tắt mức nhiễm bẩn thành bốn cấp quen thuộc.

| Điểm nhiễm bẩn | Mức sạch |
|---:|---|
| 0 đến 15 | Đã tinh lọc |
| 16 đến 35 | Có thể uống |
| 36 đến 65 | Hơi bẩn |
| 66 đến 100 | Bẩn |

## Lấy mẫu nguồn nước

Nước chỉ được đánh giá một lần khi được múc hoặc uống trực tiếp. Mod không quét môi trường mỗi tick.
Tag biome quyết định điểm nền, sau đó một vài điều kiện tại chỗ điều chỉnh nhẹ kết quả.

| Nguồn | Điểm nhiễm nền | Mức thường gặp |
|---|---:|---|
| Biển hoặc bãi biển | 25, có mặn | Có thể uống về độ sạch, nhưng không giải khát |
| Đầm lầy hoặc đầm lầy ngập mặn | 85 | Bẩn |
| Sông | 42 | Hơi bẩn |
| Núi | 28 | Có thể uống |
| Rừng rậm, xavan hoặc badlands | 70 | Bẩn |
| Biome khác | 55 | Hơi bẩn |

Biome rất nóng cộng 10 điểm, còn biome rất lạnh trừ 10. Nước trên y 100 hoặc dưới y 32 được trừ 5.
Nước chảy chỉ trừ 5, vì vậy thác nước không tự động an toàn. Bùn, rễ đước, đất trồng hoặc composter
trong phạm vi hai block có thể làm nước bẩn hơn.

Modpack có thể thêm biome vào tag `thirstwastaken2:stagnant_water` mà không sửa code. Đồ uống từ mod
khác chưa mang mẫu chất lượng vẫn sử dụng `defaultPurity`.

## Nước mặn

Độ mặn tách biệt với độ sạch. Nước biển có thể trông sạch nhưng vẫn không uống được. Uống nước mặn
không hồi độ khát, làm tăng exhaustion của thanh khát và gây Buồn nôn trong năm giây. Lò nung, lửa
trại và bộ lọc cát không loại bỏ muối.

## Trộn nước và vạc

Waterskin tính trung bình điểm nhiễm theo số phần nước đang có. Nếu một trong hai phía là nước Bẩn,
hỗn hợp bị cộng thêm 10 điểm. Vì vậy một phần nước sạch không thể dễ dàng vô hiệu hóa cả mẻ nước bẩn.
Chỉ cần thêm nước mặn thì cả waterskin sẽ được tính là mặn.

Vạc giữ mức tệ hơn khi trộn hai nguồn và ghi nhớ độ mặn. Nước được múc lại vào chai, xô hoặc
waterskin vẫn mang chất lượng đã lưu.

## Uống nước nhiễm bẩn

Nước ngọt nhiễm bẩn vẫn giải khát. Cơ chế quay hiệu ứng hiện tại không thay đổi.

| Mức | Buồn nôn và Đói | Trúng độc |
|---|---|---|
| Bẩn | 100% | 30% |
| Hơi bẩn | 50% | 10% |
| Có thể uống | 5% | không |
| Đã tinh lọc | không | không |

Buồn nôn kéo dài năm giây, Đói kéo dài ba mươi giây và Trúng độc kéo dài mười giây. Cơ chế nhiễm
bệnh dài hạn chưa nằm trong bản này.

## Làm sạch nước ngọt

Bỏ chai nước ngọt, bát đất nung đựng nước hoặc xô nước vào lò nung hay đặt lên lửa trại.

| Trước | Sau |
|---|---|
| Bẩn | Có thể uống |
| Hơi bẩn | Đã tinh lọc |
| Có thể uống | Đã tinh lọc |

Lò nung mất mười giây và lửa trại mất ba mươi giây. Nước Bẩn cần qua hai lượt để thành Đã tinh lọc.
Vật chứa nước mặn không vào được công thức, thay vì bị khử mặn ngoài ý muốn.

### Bộ lọc cát

::: warning Chưa dùng được
Phần tích hợp Create Fly của Bộ lọc cát vẫn đang bị tắt. Mô tả dưới đây áp dụng khi tích hợp này quay
lại.
:::

Bộ lọc cải thiện nước ngọt hoặc nước mặn thêm một mức nhưng vẫn giữ độ mặn. Nó không thể biến nước
biển thành nước uống.
