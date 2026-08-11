---
outline: [2, 3]
---

# Cấu hình

`config/thirstwastaken2.json` được tạo ở lần chạy đầu tiên. Nếu có Mod Menu, bạn sửa được ngay trong
game tại **Mods > ThirstWasTaken2 > Config**, và màn hình đó tự lưu khi bạn đóng lại. Nút cuối cùng
mở thẳng file, dành cho vài mục quá dài để nhét vừa một thanh trượt.

![ThirstWasTaken2 trong danh sách Mod Menu](/screenshots/mod-menu.png)

| Tiêu hao và uống nước | Nhiễm bệnh, giao diện và giá trị vật phẩm |
|---|---|
| ![Phần trên của màn hình cấu hình](/screenshots/config-1.png) | ![Phần dưới của màn hình cấu hình](/screenshots/config-2.png) |

::: tip
Chỉ mục Giao diện được đọc từ file của chính bạn. Phần còn lại lấy từ máy đang chạy thế giới, nên với
máy chủ riêng thì đó là file của máy chủ. Sửa file bằng tay thì lần khởi động sau mới có hiệu lực.
:::

## Tiêu hao khát nước

### thirstDepletionModifier

Mặc định `1.2`, hiển thị là `120%` trong game. Tốc độ mất nước nền, trước khi quần xã điều chỉnh. Đặt
`0` để độ khát không giảm ở bất cứ đâu.

### netherThirstDepletionModifier

Mặc định `3.0`. Tốc độ dùng ở Nether và mọi chiều không gian làm nước bốc hơi. Nó thay thế phần tính
theo quần xã chứ không cộng dồn.

### fireResistanceDehydrationPercent

Mặc định `50`. Bao nhiêu phần trăm tốc độ bình thường được áp dụng khi đang có Kháng lửa. Càng thấp
càng nhẹ nhàng.

### thirstDepletionInPeaceful

Mặc định `false`, nên ở độ khó Hòa bình độ khát tự hồi lại thay vì giảm. Bật lên nếu bạn muốn Hòa bình
vẫn là một thử thách sinh tồn.

### depletesWhenNauseous

Mặc định `true`. Cộng thêm một mức tiêu hao đều đặn khi đang bị Buồn nôn, đây chính là thứ khiến nước
bẩn gây hại hai lần.

### dehydrationHaltsHealthRegen

Mặc định `true`. Chặn hồi máu tự nhiên khi độ khát chưa gần đầy. Xem
[Khi sắp cạn](/vi/docs/features/thirst-and-quenched#khi-sap-can).

### preventSprintingWhenThirsty

Mặc định `true`. Không cho chạy nhanh khi độ khát còn 6 hoặc thấp hơn.

## Uống nước

### canDrinkRain

Mặc định `true`. Nhìn thẳng lên trời khi mưa để hồi lại từ từ.

### canDrinkByHand

Mặc định `false`. Cho phép người chơi ngồi xuống và dùng tay không lên mặt nước để uống trực tiếp. Tắt
sẵn vì nó khiến nước thành thứ miễn phí ở mọi nơi gần bờ.

### drinkByHandNeedsBothHandsEmpty

Mặc định `false`. Khi bật, uống bằng tay còn đòi hỏi tay còn lại cũng phải trống.

### handDrinkingHydration

Mặc định `1`. Độ khát hồi lại cho mỗi lần uống trực tiếp từ nguồn nước.

### handDrinkingQuenched

Mặc định `1`. Mức đã khát hồi lại cho chính lần uống đó.

### extraHydrationConvertsToQuenched

Mặc định `true`. Lượng nước vượt quá thanh đầy sẽ thành mức dự trữ thay vì bị bỏ đi.

## Độ sạch của nước

### defaultPurity

Mặc định `2`, tức có thể uống. Dùng cho mọi loại nước mà mod không tự xếp hạng được, kể cả đồ uống của
mod khác.

### quenchWhenDebuffed

Mặc định `true`. Nước gây trúng độc vẫn hồi thanh khát. Tắt đi để nước xấu trở thành lỗ vốn hoàn toàn.

### nauseaChance và poisonChance

Hai danh sách bốn phần trăm, mỗi mức sạch một giá trị, từ bẩn đến đã tinh lọc. Giá trị mặc định nằm ở
[Uống nước xấu](/vi/docs/features/water-purity#uong-nuoc-xau). Màn hình cấu hình chia chúng thành tám thanh
trượt riêng.

## Giao diện

Hai mục này luôn đọc từ file cấu hình của chính bạn, kể cả khi chơi trên máy chủ. Viền dự trữ và
kiểu vơi theo từng phần tư giọt luôn bật, không có tùy chọn trong ThirstWasTaken2. Nếu cài AppleSkin,
tùy chọn **Food Exhaustion HUD Underlay** của nó cũng điều khiển dải tiêu hao dạng hạt phía sau thanh
khát.

### thirstBarXOffset

Mặc định `0`. Dịch thanh khát sang ngang, tính bằng pixel, trong khoảng `-200` đến `200`.

### thirstBarYOffset

Mặc định `0`. Dịch thanh khát lên xuống theo cách tương tự. Hữu ích khi một mod khác đã chiếm góc màn
hình đó.

## Giá trị vật phẩm

### drinks và foods

Hai danh sách nằm trong file, không có trên màn hình. Mỗi dòng là một id vật phẩm và một cặp số, lượng
nước đứng trước:

```json
"drinks": {
  "minecraft:potion": [6, 8],
  "thirstwastaken2:terracotta_water_bowl": [4, 5]
}
```

Id của vật phẩm không tồn tại thì được bỏ qua, nhờ vậy mod đặt sẵn giá trị cho cả chục mod đồ ăn mà
không phụ thuộc mod nào. Thêm dòng của riêng bạn vào đây để hỗ trợ một mod chưa được liệt kê.

### itemBlacklist

Mặc định để trống. Danh sách id vật phẩm không hồi gì cả, bất kể hai danh sách trên ghi gì.

### enableKeywordMatching

Mặc định `false`. Đoán giá trị cho vật phẩm lạ dựa vào id của nó, nhờ vậy `strawberry_juice` của bất
kỳ mod nào cũng được coi là đồ uống. Nó tắt sẵn vì một phỏng đoán có thể sai theo cả hai hướng, nhưng
đây là cách nhanh nhất để phủ hết một modpack lớn.

### drinkKeywords, soupKeywords và fruitKeywords

Ba nhóm từ khóa được so với id vật phẩm, mỗi nhóm ngăn cách bằng dấu `|`. Vật phẩm khớp
`drinkKeywords` có giá trị `keywordDrinkValue`, tương tự với `keywordSoupValue` và
`keywordFruitValue`. Thứ tự kiểm tra là đồ uống, rồi canh súp, rồi trái cây.

### keywordBlacklist

Những từ chặn việc dò từ khóa ngay từ đầu, để `melon_seed` hay `pumpkin_pie` không bị nhầm thành thứ
đáng uống. Nó chỉ tác động tới phỏng đoán, không bao giờ đụng tới vật phẩm đã có trong `drinks` hay
`foods`.
