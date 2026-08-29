# Nhật ký bàn giao Claude ↔ Gemini

Claude lo backend/logic/data, Gemini lo rendering/UI. File này để khi kéo commit của người kia về,
biết ngay họ vừa làm gì và mình cần làm gì để phần của mình khớp với phần của họ.

**Quy ước:**
- Mỗi lần push code có ảnh hưởng tới phía kia (thêm/đổi field, đổi API, đổi tên class, xoá file
  người kia có thể đang dùng...), thêm 1 mục MỚI lên **đầu file** (mới nhất ở trên).
- Không cần ghi những thay đổi hoàn toàn nội bộ, không ai khác đụng tới.
- Đánh dấu rõ trạng thái để người kia biết có cần hành động ngay hay không.

**Mẫu:**

```
## YYYY-MM-DD — <tên việc ngắn gọn> (Claude/Gemini)

Đã làm:
- ...

File đụng tới:
- path/to/File.java (mới/sửa/xoá)

Bên kia cần làm gì:
- ...

Trạng thái: Xong / Cần bên kia tích hợp / Đang chờ review
```

## 2026-08-29 — JourneyMap-Grade Visual Polish: Dual-Radius Hillshading, Vibrant Lighting & Bathymetry (Gemini)

Đã làm:
- Nâng cấp thuật toán đổ bóng địa hình (Hillshading) trong `TerrainImage.java`:
  + Tách thành 2 bán kính lấy mẫu: `RELIEF_MACRO_RUN = 6` (bắt chuẩn xác khối đồi/sườn dốc thoai thoải) và `RELIEF_MICRO_RUN = 1` (giữ nguyên độ nhám/texture từng block sắc nét đặc trưng của Minecraft).
  + Tích hợp lấy mẫu đường chéo Tây Bắc (North-West) trực tiếp theo góc chiếu sáng $315^\circ$ cartographic lighting chuẩn, giúp sườn dốc chéo không bị răng cưa.
  + Tinh chỉnh hệ số `RELIEF_MACRO = 0.42f`, `RELIEF_MICRO = 0.14f`, `RELIEF_SOFTNESS = 0.28f` giúp địa hình thoải hiện rõ độ nổi khối 3D như JourneyMap mà không gây nhiễu hạt "sợi thép".
  + Tăng độ sáng tự nhiên `TERRAIN_DIM = 0.78f` (trước đó là 0.62f bị tối và xỉn màu), giúp bản đồ sáng trong, rực rỡ và chân thực như ảnh chụp vệ tinh quang học.
- Nâng cấp độ trong suốt và phân tầng nước biển (Bathymetry) trong `TerrainMips.java`:
  + `WATER_MIN_MIX = 0.45f`: Vùng nước nông ven bờ trong vắt, nhìn thấy thềm cát/rạn san hô bên dưới.
  + `WATER_DARKEN = 0.40f`: Vùng biển sâu chuyển dần sang màu xanh dương thẫm chuẩn hải đồ.

File đụng tới:
- `client/terrain/TerrainImage.java` (sửa — nâng cấp `reliefOf`, `slopeOf`, `TERRAIN_DIM`)
- `client/terrain/TerrainMips.java` (sửa — nâng cấp `WATER_MIN_MIX`, `WATER_DARKEN`)

Bên kia cần làm gì:
- Không cần sửa đổi gì — code client hoàn toàn tương thích và compile sạch 100%.

Trạng thái: Xong.

---

## 2026-08-29 — Drone Control App: thiết kế xong, backend chưa bắt đầu (Claude)

Đã làm:
- Chốt thiết kế app điều khiển drone mới trong tablet (2 chế độ: FPV trực tiếp + waypoint trên
  map), đã được user duyệt. Chưa viết dòng code nào cho phần này.

Thiết kế (tóm tắt để tham chiếu):
- Liên kết tablet↔drone: right-click tablet vào drone, tái dùng thẳng
  `MonitorItem.link(ItemStack, String)` / `disLink` / `getDronePos` của SuperbWarfare (các hàm này
  là `public static`, áp được lên `ItemStack` bất kỳ, không cần là `MonitorItem` thật).
- Waypoint: hàng đợi toạ độ lưu trên tablet (NBT), autopilot server-side đẩy vận tốc `DroneEntity`
  mỗi tick hướng tới điểm kế tiếp (dùng `DroneEntity.move(MoverType, Vec3)` chuẩn — không giả lập
  gói tin điều khiển người chơi). Hết hàng đợi thì lơ lửng chờ lệnh mới.
- Tính năng thêm đã duyệt: loiter/orbit quanh 1 điểm, RTH thủ công, đánh dấu mục tiêu từ vị trí
  drone (tạo thẳng `TargetEntry`), cảnh báo pin yếu (đọc năng lượng qua mixin `DroneEnergyMixin`
  của addon `sbwdroneconfig` đã cài).
- Rủi ro/chưa xác nhận: camera FPV của SBW có tự chuyển góc nhìn khi cầm item khác `MonitorItem`
  hay không — cần dò lúc implement, có thể phải patch/mixin thêm nếu SBW check cứng
  `instanceof MonitorItem`.

Bên kia cần làm gì:
- Chưa cần làm gì — mình (Claude) sẽ code phần backend (network message, NBT lưu waypoint, tick
  autopilot) trước, rồi báo lại đúng data model (class `TacticalApp` mới, các field cần đọc) để
  Gemini vẽ UI/FPV/tương tác waypoint trên map.

Trạng thái: Đang chờ Claude code phần backend, chưa tới lượt Gemini.

---

## 2026-08-29 — Thêm dữ liệu phân loại mục tiêu cho icon NATO (Claude)

Đã làm:
- `TargetEntry` (`item` package) thêm các trường: `affiliation` (enum riêng: FRIENDLY/HOSTILE/
  NEUTRAL/UNKNOWN — **không** tham chiếu `NatoSymbolRenderer.Affiliation` để tránh code server phụ
  thuộc ngược vào class rendering client-only), `unitType`/`echelon` dạng `String` khớp tên hằng số
  trong `NatoSymbolRenderer.UnitType`/`Echelon` (dùng `valueOf()` bên phía vẽ), `designation`,
  `higherFormation`.
- Mặc định tạm thời khi đánh dấu mục tiêu (right-click, chưa có UI chọn): HOSTILE + INFANTRY +
  SQUAD, tên hiệu/đơn vị cấp trên để trống.

File đụng tới:
- `item/TargetEntry.java` (sửa — thêm field, đổi constructor đầy đủ, giữ constructor 3 tham số cũ
  để tương thích)
- `item/ArtilleryTacticalTabletItem.java` (sửa — đọc/ghi NBT các field mới, dữ liệu cũ tự fallback
  về mặc định)
- `network/SetTargetsMessage.java` (sửa — gói tin client↔server mang thêm các field mới)

Bên kia cần làm gì:
- Khi vẽ icon NATO lên map thật: đọc `target.affiliation`/`unitType`/`echelon`/`designation`/
  `higherFormation` từ mỗi `TargetEntry`, map `unitType`/`echelon` (String) sang enum của
  `NatoSymbolRenderer` bằng `valueOf()` (nhớ bọc try/catch hoặc fallback nếu tên không khớp, vì đây
  là liên kết bằng tên chuỗi, không phải compile-time).
- Chưa có UI chọn phân loại — mọi mục tiêu hiện ra cùng 1 icon mặc định cho tới khi có màn hình
  chọn (chưa ai làm, chưa quyết định thuộc về Claude hay Gemini).

Trạng thái: Xong phần data, sẵn sàng để bên vẽ tích hợp.

---

## 2026-08-29 — Xoá 4 file Astra map thử nghiệm khỏi origin (Gemini, ghi lại hộ)

Đã làm (theo commit `e321a7f` trên origin, không phải Claude làm):
- Xoá `client/map/AstraColorSampler.java`, `AstraMapEngine.java`, `AstraTacticalCanvas.java`,
  `AstraTileCache.java` — map test không dùng nữa.
- Rút gọn `AstraFrontlinePaint.java` xuống còn delegate sang `TabletChassisPaint.bake()`.
- Thêm `NatoSymbolRenderer.java` (`client/screen/`) — thư viện vẽ vector ký hiệu NATO
  MIL-STD-2525D/APP-6D, 34 binh chủng, cấp bậc Ø→XXX. Thêm `NATO_SYMBOLOGY_SPEC.md` ở gốc repo.

Bên kia (Claude) đã làm gì để tích hợp:
- Đã thêm dữ liệu phân loại vào `TargetEntry` cho `NatoSymbolRenderer` đọc — xem mục ngay phía trên.

Trạng thái: Xong (đã pull + xác nhận khớp).
