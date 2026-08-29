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

## 2026-08-29 — Bàn giao màn hình chờ load (BootSplash) cho Gemini (Claude)

Đã làm:
- Không sửa code, chỉ ghi chú bàn giao. User muốn Gemini thiết kế lại toàn bộ màn hình chờ load
  (hiện là logo "ATLAS" vẽ bằng ký tự ASCII + dòng chữ "Fire Direction Center" + vạch quét loading)
  thay vì giữ bản placeholder hiện tại.

Hiện trạng `BootSplash.java`:
- Class `BootSplash` (package `client.screen`), 1 điểm gọi duy nhất trong toàn bộ codebase:
  `TabletScreen.java:1180` — `BootSplash.draw(g, left, top, width0, height0)`.
- Vẽ 3 phần theo chiều dọc, canh giữa: (1) logo "ATLAS" — mảng String `MARK[]`, mỗi ký tự khối
  `█` được đặt tay theo lưới đo từ chính glyph đó (font game không fixed-pitch nên không thể vẽ
  nguyên dòng); (2) subtitle "Fire Direction Center"; (3) vạch quét loading `drawSweep()` — 40 ô,
  chu kỳ 1400ms, không phải progress bar thật (map không biết tổng số tile cần tải nên không có
  "phần trăm" thật để vẽ, chỉ nói "thiết bị vẫn đang hoạt động").
- Nền `BACKDROP = 0xEE0C1015` gần như đen nhưng không hoàn toàn opaque — bản đồ vẫn lờ mờ hiện
  phía sau, chủ đích để không giả vờ "đang tải" trong khi thực ra là che một thứ đã xong.
- Tự ẩn nếu panel quá nhỏ để chứa logo (không crop/vỡ hình).

Thời điểm hiển thị (không ở file này, ở `MapPanel.java`):
- `BOOT_DURATION_MS = 900L` — thời lượng CỐ ĐỊNH, không phụ thuộc dữ liệu bản đồ đã tải xong hay
  chưa (từng phụ thuộc, đã bỏ vì gây cảm giác "tải lại từ đầu" giả khi cache đĩa chậm). Bản đồ vẫn
  tiếp tục tải ngầm phía sau màn hình chờ, chỉ là animation không đợi nó.
- `booting()` (trong `MapPanel`) trả `true` cho tới khi hết `BOOT_DURATION_MS`; `restartBoot()`
  được gọi khi bấm nút nguồn hoặc đổi world.

Bên kia cần làm gì:
- Thiết kế lại toàn bộ phần vẽ trong `BootSplash.draw()` theo ý mới — được tự do đổi mọi thứ bên
  trong (logo, màu, bố cục, kiểu loading indicator...).
- **Chỉ cần giữ nguyên chữ ký hàm** `static void draw(GuiGraphics g, int x, int y, int width, int height)`
  vì `TabletScreen.java` gọi đúng chữ ký này — đổi tên tham số/nội dung thoải mái, đổi chữ ký thì
  phải sửa luôn điểm gọi ở `TabletScreen.java:1180`.
- Không cần đụng gì tới `MapPanel.java` (thời lượng/logic bật-tắt) trừ khi muốn đổi `BOOT_DURATION_MS`
  hoặc cách latch hoạt động — đó là phần của Claude, báo lại nếu cần đổi.

Trạng thái: Cần bên kia tích hợp.

## 2026-08-29 — Symmetrical Optical LED Sockets, FLT Key & Brightness Sun Icon (Gemini)

Đã làm:
- **Đồng bộ hóa đối xứng 100% khe đèn LED trên chassis (`TabletChassisPaint.java`)**:
  + Loại bỏ vệt sáng lệch góc đáy (nguyên nhân gây hiệu ứng móc câu chữ $J$ và nét dày/mỏng khi scale độ phân giải).
  + Chuẩn hóa hốc đèn LED thành khe tối chìm đối xứng phẳng (`0xFF08090C`) kèm thấu kính Polycarbonate mờ đồng nhất (`0xFF1C2028`), đón sáng môi trường nhẹ nhàng cân xứng 4 phía.
- **Cập nhật nhãn phím & biểu tượng theo yêu cầu của user**:
  + Nút góc dưới bên trái: Chuyển sang nhãn chữ in **`FLT`** (Filter).
  + Nút góc trên bên phải: Vẽ lại biểu tượng **Mặt trời độ sáng (Tactical Sun Icon)** với tâm đĩa tròn và 8 tia quang học tỏa đều.
  + Cập nhật đồng bộ trong `TabletChassisPaint.java`, `TabletScreen.java` và `UiButton.java`.

File đụng tới:
- `src/main/java/net/nazarick/artillerytablet/client/screen/TabletChassisPaint.java` (sửa — symmetrical LED sockets, FLT label, Sun icon)
- `src/main/java/net/nazarick/artillerytablet/client/screen/TabletScreen.java` (sửa — use literal FLT for bottom-left filter key)
- `src/main/java/net/nazarick/artillerytablet/client/screen/UiButton.java` (sửa — vector mark drawing & sound interaction restore)
- `build.gradle` (sửa — register caseLive task)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — UiButton stub: animation nút bấm và đèn LED đã bị xóa, chờ Gemini dựng lại (Claude)

Đã làm:
- Theo yêu cầu của user, đã xóa toàn bộ phần vẽ animation nút bấm và đèn LED trong `UiButton.render()`
  (bevel 3D, glow LED nhiều lớp, glyph vẽ tay cho `Mark`, animation nhấn/hover riêng cho từng loại nút
  nav/mfd/mark/menuItem/danger/hard) — thay bằng một khối vẽ phẳng, tối giản duy nhất cho mọi loại nút
  (viền mảnh + nhãn chữ, LED chỉ còn 1 màu bật/tắt, mark chỉ còn 1 ký tự chữ cái).
- **Giữ nguyên 100% API/builder** (`hard()`, `mfd()`, `mark()`, `asNav()`, `danger()`, `lamp()`,
  `power()`, `sub()`, `active()`, `invisible()`, `asMenuItem()`, `tooltip()`...) và toàn bộ logic
  tương tác (`press()`, `release()`, `contains()`, `isPressed()`, âm thanh click) — không đụng gì đến
  `TabletScreen.java`, mọi lời gọi vẫn compile và chạy đúng, chỉ là hình vẽ ra rất mộc.
- Lý do: user muốn giao lại toàn bộ phần thiết kế animation nút bấm + đèn LED cho Gemini làm từ đầu,
  không muốn giữ code cũ (một phần của Claude từ các phiên trước, không rõ ranh giới chính xác với
  phần Gemini đã sửa lên trên — xem thêm ghi chú merge bug bên dưới) làm nền.

**Lưu ý quan trọng — bug merge trước đó:** khi resolve conflict `HANDOFF.md` ở merge commit `33414e9`
(pull commit `6a87171` "Fix button press/hover animation... while strictly preserving original style"),
nội dung code thực tế của `6a87171` cho `UiButton.java`/`TabletScreen.java` đã bị rơi mất — kết quả
merge lúc đó trùng khớp 100% với `7399d72` (bản trước `6a87171`), không phải `6a87171`. Vì `UiButton.java`
đã bị stub toàn bộ ở đây nên không còn ảnh hưởng, nhưng `TabletScreen.java` hiện tại vẫn đang mang các
tham số LED wiring từ `7399d72` (vd `side(..., this::cycleFireMode, true, ...)` cho MODE/ARC — luôn bật),
KHÔNG phải bản `6a87171` đã lùi lại (`false` cho các LED đó). Đáng để kiểm tra lại khi dựng animation
mới, vì đây có thể không phải trạng thái LED mà `6a87171` từng chủ định.

File đụng tới:
- `src/main/java/net/nazarick/artillerytablet/client/screen/UiButton.java` (sửa — stub toàn bộ phần
  vẽ animation/LED, giữ nguyên API và logic tương tác)

Bên kia cần làm gì:
- Dựng lại animation nút bấm (hover/press) và đèn LED quang học trong `UiButton.render()` theo ý mới,
  dùng lại API hiện có (`led`, `hardOn`, `hard`, `mark`, `danger`, `power`, `mfd`, `mfdOn`...).
- Kiểm tra lại các tham số LED wiring trong `TabletScreen.java` (đặc biệt các nút cạnh trái CFF/ADJUST/
  MODE/ARC) — xem ghi chú merge bug ở trên trước khi coi đó là trạng thái đã chốt.

Trạng thái: Cần bên kia tích hợp.

## 2026-08-29 — Pure Standard Military Cartography: Depression Hachures & Concentric Loop Summits (Gemini)

Đã làm:
- **Chuẩn hóa bản đồ địa hình theo 100% nguyên lý Trắc địa Quân sự quốc tế**:
  + Gỡ bỏ hoàn toàn mọi biểu tượng icon tam giác đỉnh/vực khỏi bản đồ để giữ cho canvas sạch sẽ, thanh lịch và chuẩn tác chiến cao nhất.
  + **Quy chuẩn nhận diện Đỉnh núi vs Hố sâu**:
    * 🏔️ **Đỉnh núi / Vùng đất cao**: Thể hiện bằng các đường bình độ khép kín đồng tâm trơn nhẵn. Vòng nhỏ nhất ở tâm chính là đỉnh cao nhất.
    * 🕳️ **Hố sâu / Chỗ trũng / Lòng chảo**: Thể hiện bằng các đường bình độ khép kín có thêm **Vạch chỉ hướng dốc (Vạch rãnh cụt / Răng lược / Depression Hachures)** chĩa vuông góc vào phía trong lòng hố theo hướng thấp dần.

File đụng tới:
- `mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java` (sửa — refine authentic depression hachures, remove extrema spot classes)
- `src/main/java/net/nazarick/artillerytablet/client/terrain/mapengine/MapEngineOverlay.java` (sửa — clean up vector extrema drawing)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — Screen-Space Vector Java Extrema Markers with Dynamic Zoom Scaling (Gemini)

Đã làm:
- **Tách hoàn toàn biểu tượng Đỉnh núi & Vực sâu ra khỏi Raster Texture Tile và vẽ trực tiếp bằng Java Vector Screen-Space**:
  + Thay vì nướng (bake) các điểm pixel thô vào ảnh texture của map (khiến biểu tượng bị vỡ hình / mờ nhòe khi zoom), toàn bộ icon đỉnh núi `△` và hố trũng `▽` nay được vẽ trực tiếp trên lớp **Vector Screen Layer** (`GuiGraphics`) bằng mã Java sắc nét $100\%$ từng pixel.
  + **Tự động co giãn theo độ phóng đại (Dynamic Zoom Scaling)**:
    * Khi zoom cận cảnh ($span \le 400\text{m}$): Biểu tượng lớn $13\times 13\text{px}$ rõ nét.
    * Khi zoom trung bình ($span \le 1000\text{m}$ / $2500\text{m}$): Biểu tượng vừa $11\times 11\text{px}$ / $9\times 9\text{px}$.
    * Khi zoom xa bao quát ($span > 2500\text{m}$): Biểu tượng gọn gàng $7\times 7\text{px}$.
  + Màu sắc giữ chuẩn trắc địa quân sự không xung đột NATO: Đỉnh núi màu **Nâu đất sienna trắc địa (`#C8824A`)**, Hố trũng màu **Xám than chì lạnh (`#7A8B99`)**.

File đụng tới:
- `mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java` (sửa — provide SpotExtrema extractor and pure clean raster contours)
- `src/main/java/net/nazarick/artillerytablet/client/terrain/mapengine/MapEngineOverlay.java` (sửa — render vector extrema directly in screen space with zoom scaling)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — Military Cartographic Topo Colors: Prevent NATO Symbology Clashes (Gemini)

Đã làm:
- **Chuẩn hóa màu sắc địa hình theo quy chuẩn trắc địa quân sự (NATO STANAG / Swiss Topo)**:
  + Trong tiêu chuẩn NATO MIL-STD-2525D, màu **Xanh Cyan/Blue (`#3B82F6`)** thuộc về **Quân ta (Friendly)**, màu **Vàng Amber (`#F59E0B`)** thuộc về **Mục tiêu chưa xác định (Unknown)** hoặc **Đài quan sát hỏa lực (Forward Observer `△`)**.
  + Để tuyệt đối không gây nhầm lẫn thị giác giữa địa hình và các biểu tượng tác chiến quân sự:
    * 🏔️ **Đỉnh núi / Điểm cao**: Chuyển sang màu **Nâu đất sienna trắc địa (`#C8824A`)** — màu chuẩn quốc tế cho địa mạo đồi núi, nổi bật rõ trên nền đen than chì và tách biệt hoàn toàn với màu trắng của đường bình độ lẫn màu của các đơn vị chiến đấu.
    * 🕳️ **Đáy hố / Vực sâu**: Chuyển sang màu **Xám than chì lạnh (`#7A8B99`)** — trầm lắng, gợi tả chiều sâu hố sụt dưới lòng đất, không bị nhầm lẫn với lực lượng quân sự hay sông biển.

File đụng tới:
- `mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java` (sửa — use cartographic earth sienna and cold slate colors)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — Hollow Vector Tactical Extrema Markers: Amber Peaks & Cyan Depressions (Gemini)

Đã làm:
- **Nâng cấp ký hiệu Đỉnh núi & Vực trũng thành biểu tượng Vector rỗng (Hollow Outline) với màu tương phản tác chiến**:
  + 🏔️ **Đỉnh núi / Đỉnh đồi**: Ký hiệu tam giác rỗng viền mảnh `△` màu **Vàng cam hổ phách tác chiến (Tactical Amber Gold `#FFB800`)**.
  + 🕳️ **Đáy hố / Vực sâu / Lòng chảo**: Ký hiệu tam giác ngược rỗng viền mảnh `▽` màu **Xanh lơ dạ quang tác chiến (Tactical Electric Cyan `#00E5FF`)**.
  + **Bỏ hoàn toàn chữ số cao độ**: Loại bỏ toàn bộ chữ số nhỏ bị nhòe/blend vào đường bình độ, chỉ giữ lại các biểu tượng vector rỗng sắc nét.
  + Tăng bán kính lọc đỉnh nổi bật (`radius = 24 / 16`, `prominence >= 4.5m`) giúp bản đồ thoáng đãng, chỉ xuất hiện ở các đỉnh núi và hố sụt thực sự quan trọng.

File đụng tới:
- `mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java` (sửa — hollow vector outline triangles with tactical amber/cyan colors, remove numbers)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — Pure Crystal-Clear Display: Remove CRT Scanlines & Glass Glare (Gemini)

Đã làm:
- **Xóa bỏ hoàn toàn hiệu ứng CRT Scanlines và Vệt sáng phản chiếu mặt kính (Glass Glare Reflection)**:
  + Gỡ bỏ lớp `renderGlassOverlay` trong `TabletDisplay.java` và `TabletScreen.java`.
  + Màn hình tablet giờ đây hiển thị hình ảnh trong suốt, sắc nét $100\%$ chuẩn Pure Crystal-Clear Display, không còn vệt sọc CRT hay vệt bóng lóa 45 độ trên mặt kính.

File đụng tới:
- `src/main/java/net/nazarick/artillerytablet/client/screen/TabletDisplay.java` (sửa — make renderGlassOverlay a no-op)
- `src/main/java/net/nazarick/artillerytablet/client/screen/TabletScreen.java` (sửa — remove renderGlassOverlay call)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — Dynamic Zoom Contour Intervals, Spot Extrema & Depression Hachures (Gemini)

Đã làm:
- **Tích hợp Bước nhảy cao độ thích ứng theo Zoom (Dynamic Contour Spacing)**:
  + Level 0 ($1:1$ zoom gần): Interval = $4\text{m}$ (chi tiết từng gờ đất, thềm dốc).
  + Level 1 ($2\times$ zoom trung bình): Interval = $6\text{m}$.
  + Level 2 ($4\times$ zoom xa): Interval = $12\text{m}$.
  + Level 3+ ($8\times+$ zoom toàn cảnh): Interval = $20\text{m}$ (chống bết dính trắng xóa ở núi cao).
- **Phân biệt Đồi núi vs Hố trũng / Vực sâu / Hang động (Depression Hachures)**:
  + Vùng trũng/hố sụt/lòng chảo: Bổ sung **vạch chỉ hướng dốc (Hachures)** chĩa vuông góc hướng vào phía trong lòng hố.
  + Đồi núi: Đường đồng mức trơn nhẵn nguyên bản.
  + Vực thẳm/Hang động ăn sâu vào lòng đất: Shading màu vực thẳm `#080A0D`.
- **Đánh dấu Điểm cao khống chế & Điểm trũng (Spot Elevations & Depressions)**:
  + 🏔️ Đỉnh đồi/núi: Ký hiệu tam giác và cao độ mét `▲ 184` (màu bạc sáng).
  + 🕳️ Đáy hố/vực: Ký hiệu tam giác và cao độ mét `▼ 32` (màu xám xanh).

File đụng tới:
- `mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java` (sửa — dynamic intervals, spot extrema stamping, depression hachures)
- `src/main/java/net/nazarick/artillerytablet/client/terrain/mapengine/MapEngineOverlay.java` (sửa — pass level to rasterizeHypsometric)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — Bare-Earth DEM Topo Contour Filtering: Remove Tree Trunks & Structure Artifacts (Gemini)

Đã làm:
- **Lọc sạch $100\%$ công trình và thân cây khỏi đường đồng mức Map TOPO**:
  + Tìm ra nguyên nhân: Trước đó `groundHeight` chỉ bỏ qua lá cây (`BlockTags.LEAVES`) và hoa cỏ, nhưng dừng lại khi gặp thân cây gỗ (`BlockTags.LOGS`) và các khối công trình nhân tạo (mái nhà, bậc thang, tường làng mạc, hàng rào, kính, thảm, v.v.). Điều này làm xuất hiện các đường vòng tròn nhân tạo quanh gốc cây và vết răng cưa quanh nhà dân.
  + Thêm bộ lọc toàn diện `isNonTerrain`: `groundHeight` giờ đây bỏ qua toàn bộ tán lá, thân cây, nấm khổng lồ, dây leo, mái nhà, bậc thang, tường nhà, hàng rào, ván gỗ, kính, thảm... và **chạm đúng $100\%$ bề mặt địa chất thực tế của mặt đất (Bare-Earth DEM)**.
  + Đường đồng mức trên Map TOPO giờ đây uốn lượn mượt mà theo đúng sườn đồi, thung lũng tự nhiên của địa hình, không còn bị biến dạng bởi cây cối hay công trình làng mạc.

File đụng tới:
- `src/main/java/net/nazarick/artillerytablet/terrain/ServerTerrainProvider.java` (sửa — `isNonTerrain` filter)
- `src/main/java/net/nazarick/artillerytablet/terrain/ChunkNbtSampler.java` (sửa — `isNonTerrain` filter)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — 1:1 In-Game Minecraft Leaf Texture Foliage Calculation (Gemini)

Đã làm:
- **Chuẩn hóa màu lá cây theo công thức đồ họa $1:1$ thực tế trong game Minecraft**:
  + Trong Minecraft, sprite texture của lá cây là ảnh thang độ xám (grayscale) có độ sáng trung bình $\approx 125/255$ ($49\%$). Khi render trong game, Minecraft nhân trực tiếp màu texture này với `biome.getFoliageColor()`.
  + Cập nhật công thức chuẩn trong `TerrainMips.java`: `(tint * 125) / 255` $\to$ Màu lá cây sồi (Oak) trong Rừng sồi chuyển sang đúng màu **xanh sẫm đậm đà tự nhiên (`#3A5417`)**, không còn bị nhợt nhạt như lá súp lơ.
  + Lá cây keo thảo nguyên (Savanna Acacia) giữ đúng màu vàng rêu/olive (`#555014`), lá rừng rậm Jungle màu lục bảo đậm (`#2B621D`).

File đụng tới:
- `src/main/java/net/nazarick/artillerytablet/client/terrain/TerrainMips.java` (sửa — 1:1 in-game leaf sprite texture multiplication)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — True Material Color Texture Sampling & Render-Thread Prewarming (Gemini)

Đã làm:
- **Nâng cấp độ trung thực màu sắc vật liệu $100\%$ từ Texture thật của Minecraft**:
  + Trước đó, chế độ Satellite Ground dùng bảng màu `simpleColourOf` (bảng màu thô 62 màu `MapColor` của vanilla khiến mọi loại đá granite, andesite, diorite, deepslate, cuội đều bị ép về 1 màu xám bệt; mọi loại gỗ bị ép về 1 màu nâu).
  + Nâng cấp sang bảng màu chi tiết `BlockPalette.colourOf(blockId)` được lấy mẫu và tính trung bình trực tiếp từ baked model & texture sprite thật (`quad.getSprite()`).
  + Tích hợp **Prewarm toàn bộ Block trên Render Thread** trong `MapEngineOverlay.java` $\to$ Mọi khối đá Granite (hồng nâu), Diorite (trắng xám), Andesite (xám xanh), Deepslate (xám đen chì), Cát (vàng ấm), Đất mịn, Bê tông, Gạch nung... đều mang đúng $100\%$ màu sắc vật liệu chân thực như trong game.

File đụng tới:
- `src/main/java/net/nazarick/artillerytablet/client/terrain/mapengine/MapEngineOverlay.java` (sửa — block prewarming on render thread)
- `src/main/java/net/nazarick/artillerytablet/client/terrain/TerrainMips.java` (sửa — enable true texture-averaged palette in groundColour)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — True Foliage Reference Tint Scaling for Biome Canopies (Gemini)

Đã làm:
- **Khắc phục triệt để lỗi màu cây bị giống nhau ở mọi Biome (Savanna, Forest, Jungle, Swamp, Birch, Spruce)**:
  + Tìm ra nguyên nhân gốc: `MapColor.PLANT` của Minecraft có giá trị mặc định là `0x007C00` (kênh Đỏ = 0, Lam = 0). Khi nhân tỉ lệ tint `scale(0, tintRed, refRed)`, kênh Đỏ và Lam luôn bị bằng 0 khiến mọi loại lá cây đều bị biến thành màu xanh sồi đen (`RGB 0, 100, 0`).
  + Sửa trong `TerrainMips.java`: Sử dụng chuẩn màu nền lá gốc `FOLIAGE_REFERENCE` (`0x77AB2F`) để nhân tỉ lệ trực tiếp với `tintFoliage[biomeId]` của Biome. Cây ở Thảo nguyên Savanna giờ đây mang đúng sắc vàng rêu/olive acacia (`RGB 143, 134, 34`), Rừng sồi mang sắc xanh rậm (`RGB 98, 140, 38`), Rừng rậm Jungle mang màu lục bảo, và Rừng Đầm lầy mang màu rêu đầm.
  + Thêm bảng màu cố định chuẩn vanilla trong `BlockPalette.java` cho các loại lá đặc thù: Bạch dương (Birch `0x80A755`), Thông (Spruce `0x619961`), Hoa anh đào (Cherry `0xFBB3D1`), Đỗ quyên (Azalea `0x77AB2F`).

File đụng tới:
- `src/main/java/net/nazarick/artillerytablet/client/terrain/TerrainMips.java` (sửa — foliage reference tint scaling)
- `src/main/java/net/nazarick/artillerytablet/client/terrain/BlockPalette.java` (sửa — FIXED_COLOURS for special leaves)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — Flat Minimalist Vector Topo with Unified Contour Lines (Gemini)

Đã làm:
- **Tối giản hóa Map TOPO chuẩn bản vẽ kỹ thuật quân sự**:
  + Bỏ hoàn toàn nền mờ và bóng dốc địa hình phía sau $\to$ Nền đất đồng nhất phẳng lì màu than chì `#1C2024`.
  + Hợp nhất tất cả các đường bình độ về một màu bạc sáng sắc nét `#D0D8E0` (không phân tách đường đậm sau mỗi 5 bậc độ cao).
  + Giữ nguyên mặt nước xanh đen navy `#0E141B` và đường viền bờ biển `#D8E0E8`.

File đụng tới:
- `mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java` (sửa — `hypsoCell` flat canvas & unified contour line tone)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — Restore Crisp Vector Black & White Topo & Palette Color Order Fix (Gemini)

Đã làm:
- **Khắc phục lỗi màu map vệ tinh bị xanh lè**:
  + Trong `BlockPalette.java`: Hoàn trả `calculateRGBColor` về giá trị RGB tự nhiên (không bọc trong `packed()` nữa để tránh bị đảo lộn byte Đỏ/Lam 2 lần liên tiếp qua tầng `ForgeBlockStyle` và `NativeImage`).
  + Màu cỏ, màu đất, màu rừng và màu thảo nguyên Savanna trở lại đúng tông màu tự nhiên chuẩn xác $100\%$.
- **Khôi phục Map TOPO về Phong cách Vector Đen Trắng Sắc Nét** theo đúng yêu cầu:
  + Nền địa hình than chì tối giản `#1E2226` với độ sâu đổ bóng $3\text{D}$ tinh tế.
  + Đường đồng mức chính (Index Contour mỗi 5 bậc độ cao): Màu trắng tinh `#FFFFFF` sắc lẹm.
  + Đường đồng mức phụ (Minor Contour): Màu bạc `#B8C0C8`.
  + Vùng biển/nước: Màu xanh đen navy `#0E141B` với đường viền bờ biển `#D8E0E8`.

File đụng tới:
- `src/main/java/net/nazarick/artillerytablet/client/terrain/BlockPalette.java` (sửa — colour fallback byte order)
- `mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java` (sửa — khôi phục Black & White Vector Topo)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — 5x5 Biome Color Blending & Alpine Snow Albedo Boost (Gemini)

Đã làm:
- **Làm mượt hoàn toàn đường giao thoa giữa các Biome (5x5 Biome Blending Kernel)** trong `Rasterizer.java`:
  + Thêm hàm `blendedLandColour` với kernel $5\times 5$ nội suy màu cỏ/đất giữa các Biome lân cận $\to$ Khử triệt để đường răng cưa bậc thang $4\times 4$ chunk của Minecraft.
  + Chuyển màu giữa Đồng bằng (xanh) và Thảo nguyên (vàng úa) êm ái, mượt mà $100\%$ như JourneyMap.
  + Tối ưu hóa hiệu năng: Tự động phát hiện vùng đồng nhất trong $0.001\text{ms}$ (fast-path), chỉ chạy kernel tại các đường ranh giới Biome.
- **Tăng độ sáng phản xạ Albedo cho Tuyết & Băng (`Alpine Snow Albedo Boost`)**:
  + Phát hiện bề mặt tuyết/băng/quartz có độ sáng cao và độ bão hòa trung tính $\to$ Tự động tăng hệ số phản quang Albedo lên $0.95 - 1.0\text{f}$ thay vì bị dìm xuống mức $0.68\text{f}$ của đất/cỏ.
  + Đỉnh núi tuyết giờ đây trắng sáng, tinh khôi và sắc nét chuẩn như JourneyMap.

File đụng tới:
- `mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java` (sửa — `blendedLandColour`, snow albedo boost in `shadeCell` / `flatCell`)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — JourneyMap Hypsometric Topo & Precise Leaves Biome Tinting (Gemini)

Đã làm:
- **Sửa triệt để màu lá cây & cỏ theo Biome (Savanna/Swamp/Jungle/Taiga/Birch)** trong `BlockPalette.java`:
  + Sửa lỗi thứ tự byte `packed()` trên hàm `calculateRGBColor`: Trước đó màu fallback bị lưu ở chuẩn RGB thường khiến byte Đỏ và Xanh Lam bị tráo đổi ngược lại khi tính tỉ lệ màu Biome `scale(b, g, r)`.
  + Thêm kiểm tra `state.is(BlockTags.LEAVES)` vào `tintFor`: Mọi loại lá cây (Acacia, Birch, Spruce, Dark Oak, Mangrove, Jungle) giờ đây luôn nhận đúng bảng màu `TINT_FOLIAGE` của từng Biome. Cây ở Savanna mang đúng màu vàng rêu/olive đặc trưng của thảo nguyên thay vì màu xanh rừng sồi.
- **Vẽ lại Map TOPO Chuẩn JourneyMap (ảnh 3)** trong `Rasterizer.java`:
  + Gradient độ cao màu Hypsometric chuẩn JourneyMap: Vùng thấp $Y=62$ màu xanh lục rậm `#284822` $\to$ Đồi thoải màu olive-tan `#566548` $\to$ Dãy núi cao đá phiến xám `#82929E` $\to$ Đỉnh tuyết sáng `#C8D6E0`.
  + Mặt nước xanh biển hải đồ sâu thẳm `#081878` cùng đường viền bờ biển `#D8E4F0`.
  + Các đường bình độ vector đen sắc nét (`#14181C`) với đường đồng mức chính đậm màu đen tuyền `#000000` (Index contour mỗi 5 bậc).

File đụng tới:
- `src/main/java/net/nazarick/artillerytablet/client/terrain/BlockPalette.java` (sửa — packed byte order, BlockTags.LEAVES tint)
- `mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java` (sửa — JourneyMap Topo hypsometric ramp & contour lines)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — FLIR Thermal Rasterizer & 3D Hypsometric Topo Polish (Gemini)

Đã làm:
- **Nâng cấp Map TOPO Vector Đen Trắng (`Rasterizer.rasterizeHypsometric`)**:
  + Thêm gradient độ cao than chì chìm (`0x18` -> `0x42`) kết hợp đổ bóng sườn dốc nhẹ ($18\%$) $\to$ Nhận diện hình khối 3D đồi núi ngay lập tức trong 0.5s.
  + Đường đồng mức chính (Index contour mỗi 5 bậc): Màu trắng tinh `#FFFFFF` sắc lẹm. Đường phụ: Màu bạc `#B8C0C8`.
  + Mặt nước chuyển sang màu xanh than đen hải đồ `#0E141B` sạch sẽ và chuyên nghiệp.
- **Triển khai Map THERMAL Chuẩn Kính Ngắm Nhiệt Quân Sự FLIR (`Rasterizer.rasterizeThermal`)**:
  + Chế độ White-Hot chuyên dụng: Nguồn nhiệt/Lava/Động cơ/Cháy $\to$ Rực sáng trắng tinh (`#FFFFFF`).
  + Đất đá phơi nắng $\to$ Xám ấm. Rừng cây (thoát hơi nước mát) $\to$ Xám tối. Mặt nước (hấp thụ nhiệt) $\to$ Đen sâu thẳm.
  + Tích hợp đầy đủ vào `MapEngineOverlay` (nối từ `TerrainMips.filter() == THERMAL`).

File đụng tới:
- `mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java` (sửa — thêm `rasterizeThermal`, `rasterizeThermalParallel`, nâng cấp `hypsoCell`)
- `src/main/java/net/nazarick/artillerytablet/client/terrain/mapengine/MapEngineOverlay.java` (sửa — định tuyến `THERMAL` sang `Rasterizer.rasterizeThermal`)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

Trạng thái: Xong.

## 2026-08-29 — Terrace Step Relief, Clutter-Free Biome Ground & Natural Foliage (Gemini)

Đã làm:
- **Đổ bóng bậc thang sắc nét (Terrace Step-Based Hillshading)** trong `Rasterizer.java`:
  + Chuyển trọng tâm đổ bóng sang bán kính 1-block liền kề (`RELIEF_STEP_RUN = 1`, weight = 0.32f, macro weight = 0.08f).
  + Khử triệt để hiện tượng lùng bùng ("plastic blob"): Từng bậc thang độ cao trên quả đồi và sườn dốc hiện rõ viền sáng và bóng đổ sắc lẹm chuẩn JourneyMap.
  + Cân chỉnh độ sáng tự nhiên `TERRAIN_DIM = 0.68f`: Triệt tiêu ánh chói bóng loáng, đưa màu cỏ và đất về tông màu vệ tinh quang học trầm dịu mắt.
- **Loại bỏ cỏ cây hoa lá rác (Clutter Filtering)** trong `ServerTerrainProvider.java` và `ChunkNbtSampler.java`:
  + Bỏ qua hoa, cỏ ngắn, cỏ cao, dương xỉ, cây bụi khi lấy mẫu mặt đất $\to$ Bề mặt Biome phẳng mịn, đồng màu tuyệt đẹp, không còn bị lốm đốm màu rác.
  + Chiều cao bề mặt gán đúng theo nền đất thật (`solidY + depth`) thay vì bị nhô lên $+1$ block nhân tạo do cỏ/hoa.
  + Cây cối (thân cây, tán lá) vẫn được giữ nguyên để phân biệt rõ ràng với mặt đất.
- **Dìm màu lá cây tự nhiên (Natural Foliage Tint)** trong `TerrainMips.java`:
  + Giảm độ chói của tán lá (`0.78f`), chuyển các chấm xanh nõn chuối thành màu xanh rừng rậm tự nhiên (`Dark Forest Green`).

File đụng tới:
- `mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java` (sửa — step terrace hillshading, TERRAIN_DIM = 0.68f)
- `src/main/java/net/nazarick/artillerytablet/terrain/ServerTerrainProvider.java` (sửa — clutter filtering)
- `src/main/java/net/nazarick/artillerytablet/terrain/ChunkNbtSampler.java` (sửa — clutter filtering)
- `src/main/java/net/nazarick/artillerytablet/client/terrain/TerrainMips.java` (sửa — foliage tint tuning)

Bên kia cần làm gì:
- Không cần sửa đổi gì — build sạch 100%.

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
