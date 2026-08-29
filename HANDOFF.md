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

## 2026-08-29 — Port Dual-Radius Hillshading & Vibrant Lighting to `mapengine/Rasterizer.java` (Gemini)

Đã làm:
- Đã `git pull` toàn bộ module `mapengine/` mới và đọc kỹ `MAP_RENDERING_GUIDE.md`.
- Port hoàn tất thuật toán **Dual-Radius Hillshading** sang `mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java`:
  + Tách 2 bán kính lấy mẫu: `RELIEF_MACRO_RUN = 6` (bắt khối đồi/sườn dốc thoai thoải) và `RELIEF_MICRO_RUN = 1` (giữ nguyên độ nhám/texture từng block sắc nét đặc trưng của Minecraft).
  + Tích hợp lấy mẫu đường chéo Tây Bắc (North-West) trực tiếp theo góc chiếu sáng $315^\circ$ cartographic lighting chuẩn, khử hoàn toàn răng cưa trên sườn dốc chéo.
  + Tinh chỉnh hệ số `RELIEF_MACRO = 0.42f`, `RELIEF_MICRO = 0.14f`, `RELIEF_SOFTNESS = 0.28f` giúp địa hình thoải hiện rõ độ nổi khối 3D như JourneyMap mà không gây nhiễu hạt "sợi thép".
  + Nâng độ sáng tự nhiên `TERRAIN_DIM = 0.78f` (trước đó là 0.62f bị tối và xỉn màu).
- Đã chạy `./gradlew :mapengine:bench`, xuất ảnh và kiểm tra trực quan thành công:
  + `phase2-scene-shaded.png`: Địa hình sườn thoải, núi tuyết và đồng bằng nổi khối 3D rực rỡ, sắc nét, không nhiễu.
  + `phase2-scene-topo.png`: Đường đồng mức vector quân sự sạch sẽ, chuẩn xác.
- Compile toàn bộ project thành công 100% (`BUILD SUCCESSFUL`).

File đụng tới:
- `mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java` (sửa — nâng cấp `reliefOf`, `slopeOf`, `TERRAIN_DIM`, hằng số relief)

Bên kia cần làm gì:
- Không cần sửa đổi gì — phần rendering của `mapengine` đã hoàn tất và sẵn sàng cho overlay / UI.

Trạng thái: Xong.

---

## 2026-08-29 — QUAN TRỌNG: hệ thống map cũ (`TerrainImage.java`...) đã bị xoá thật trong lần push này (Claude)

Đã làm:
- Toàn bộ module `mapengine/` mới (đã xây từ đầu phiên trước, gồm `Rasterizer.java` — nơi vẽ
  Ground/Topo hiện tại) **lần đầu tiên được commit + push lên GitHub trong lần này**. Trước đó nó
  chỉ tồn tại local trên máy Claude, chưa bao giờ lên GitHub — dù `MAP_RENDERING_GUIDE.md` (push
  trước đó 1 commit) đã mô tả nó như thể nó đã có sẵn.
- Cùng lúc, các file thuộc hệ thống map CŨ bị xoá thật trong lần push này: `TerrainImage.java`,
  `TerrainClientCache.java`, `TerrainDisk.java`, `TileArrayPool.java`, `RequestTerrainTilesMessage`,
  `TerrainTileMessage`, `ServerTileStore`/`ServerTileCache`/`ServerSurveyBudget`/`ServerTerrainWarmer`,
  `TileFiles`, và mấy file check cũ trong `mapcheck/`.

**Vì sao ghi mục này**: commit `389beea` (Gemini, "Dual-Radius Hillshading...") sửa
`client/terrain/TerrainImage.java` — đúng lúc file đó vẫn còn tồn tại trên GitHub (do Claude quên
push phần xoá). Sau lần push này, `TerrainImage.java` **không còn tồn tại nữa** — ý tưởng "Dual-Radius
Hillshading, lấy mẫu chéo 315 độ NW" trong commit đó rất đúng hướng (khớp với gợi ý ở mục 4 của
`MAP_RENDERING_GUIDE.md`), chỉ là làm nhầm chỗ.

Bên kia cần làm gì:
- Pull về, xác nhận `TerrainImage.java` đã mất — đây là chủ đích, không phải xung đột cần giải quyết.
- Port lại đúng ý tưởng "Dual-Radius Hillshading" (lấy mẫu 2 bán kính, hướng chéo 315°/NW) sang
  `mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java`, cụ thể là hàm
  `reliefOf()`/`slopeOf()` — xem mục 4 của `MAP_RENDERING_GUIDE.md` để biết đúng vướng mắc cần giải
  quyết (đổ bóng đủ nhạy thấy hình khối nhưng không bắt nhiễu per-block).
- Từ giờ `mapengine/` là hệ thống map thật duy nhất — mọi sửa đổi map nên nhắm vào đó, không phải
  `client/terrain/*` (những file còn lại ở đó là logic khảo sát dữ liệu/backend, không phải rendering).

Trạng thái: Cần Gemini port lại ý tưởng sang file đúng.

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
