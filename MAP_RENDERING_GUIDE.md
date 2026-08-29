# Hướng dẫn tiếp quản phần vẽ map (Gemini)

Tài liệu này để Gemini nắm được kiến trúc, luồng dữ liệu, và logic vẽ hiện tại của bản đồ trong
tablet (`ArtilleryTacticalTablet`), trước khi tiếp quản phần chỉnh sửa hình ảnh/rendering của nó.
Claude (mình) viết tài liệu này, không tự sửa tiếp phần vẽ nữa từ đây — đúng ranh giới đã thống nhất:
Claude lo dữ liệu/logic backend, Gemini lo rendering.

## 1. Tổng quan kiến trúc

Bản đồ chạy trên module `mapengine` — 1 subproject Gradle **thuần Java, không phụ thuộc Minecraft**
(`ArtilleryTacticalTablet/mapengine/`). Lý do tách riêng: test/benchmark chạy được mà không cần mở
game (`./gradlew :mapengine:bench`), nên có thể tự nhìn ảnh kết quả và đo hiệu năng trong vài giây,
không phải chờ ~1 phút load game mỗi lần đổi 1 con số.

Phần "cầu nối" giữa `mapengine` và Minecraft nằm ở
`ArtilleryTacticalTablet/src/main/java/net/nazarick/artillerytablet/client/terrain/mapengine/`.

**Luồng dữ liệu, từ mặt đất thật tới pixel trên màn hình:**

```
Minecraft chunk (client đã load)
      │  ServerTerrainProvider.sampleLive()  [backend — Claude]
      ▼
TerrainTile (height/groundHeight/block/biome/depth, 64×64 cột)
      │  ForgeColumnSource.copyInto()
      ▼
ColumnBuffer (mapengine, 512×512 cột/region)
      │  Pyramid.reduce()  — thu nhỏ dần cho các mức zoom xa (level 1..6)
      ▼
RegionStore (cache RAM + ghi đĩa qua RegionFile/RegionShardFile)
      │  Rasterizer.rasterize() / rasterizeHypsometric()   ← ĐÂY LÀ PHẦN VẼ
      ▼
int[] pixels (ARGB)
      │  MapEngineOverlay.textureFor()  — upload lên GPU texture
      ▼
Vẽ lên màn hình tablet
```

**Việc của Gemini nằm gần như toàn bộ trong 1 file**:
`ArtilleryTacticalTablet/mapengine/src/main/java/net/nazarick/mapengine/raster/Rasterizer.java`

Đây là nơi DUY NHẤT quyết định màu pixel cuối cùng. Không cần đụng gì tới các bước trước đó (đọc
dữ liệu, cache, GPU upload) trừ khi thực sự cần thêm 1 loại dữ liệu mới (xem mục 5).

## 2. Các file liên quan, theo vai trò

| File | Vai trò | Ai đụng vào |
|---|---|---|
| `mapengine/raster/Rasterizer.java` | **Vẽ pixel** — toàn bộ logic màu sắc, đổ bóng, đường viền | **Gemini** |
| `mapengine/core/ColumnBuffer.java` | Struct dữ liệu 1 region (height, groundHeight, block, biome, depth) | Claude (đọc, không sửa cấu trúc nếu không cần) |
| `mapengine/core/BlockStyle.java` | Interface: block id → màu, có phải hazard không | Claude định nghĩa, Gemini implement phần render dùng nó |
| `client/terrain/mapengine/ForgeBlockStyle.java` | Implement `BlockStyle` cho Minecraft thật, đọc màu từ `BlockPalette` | Ranh giới — đổi màu cơ bản của từng block thì sửa ở đây hoặc `BlockPalette` |
| `client/terrain/BlockPalette.java` | Bảng màu từng block (trung bình texture, hoặc màu bản đồ đơn giản hoá) | Claude |
| `client/terrain/mapengine/MapEngineOverlay.java` | Vòng lặp vẽ, chọn filter (Ground/Topo), upload texture lên GPU | Cả 2, tuỳ việc |
| `mapengine/bench/Bench.java` | Sinh ảnh PNG test không cần mở game | Dùng để tự kiểm tra khi sửa `Rasterizer.java` |

## 3. Logic vẽ hiện tại — 2 filter

Tablet có 2 chế độ hiển thị map, mỗi chế độ là 1 hàm riêng trong `Rasterizer.java`:

### 3.1. Ground (vệ tinh) — `rasterize()` / `shadeCell()`

- Màu nền: `style.columnColour(...)` — màu block thật (đã đơn giản hoá về ~62 tông theo `MapColor`
  của Minecraft, xem `BlockPalette.simpleColourOf`), có tint theo biome.
- Đổ bóng địa hình (hillshade): `reliefOf()` → `slopeOf()` → `squash()`.
  - So độ cao cột hiện tại với cột phía Bắc và phía Tây (`RELIEF_RUN = 3` cột cho "macro", 1 cột cho
    "micro"), hai độ dốc này gộp lại thành 1 hệ số sáng/tối.
  - `squash()` là 1 bảng tra `tanh` dựng sẵn (không tính trực tiếp mỗi pixel vì tanh tốn CPU) — nén
    độ dốc về khoảng [-1, 1].
  - Cây cối, hoa cỏ vẫn hiện đúng như thật (không lọc bỏ) — đây là điểm khác biệt có chủ đích với
    Topo (xem 3.2).

### 3.2. Topo (bản đồ đường đồng mức) — `rasterizeHypsometric()` / `hypsoCell()`

- **Không có đổ bóng địa hình.** Xác nhận trực tiếp từ dev JourneyMap (tham khảo trong lịch sử — họ
  chỉ vẽ đường tại ranh giới dải độ cao, không đổ bóng gì cả) — mọi lần thử thêm hillshade vào đây
  đều bị chê là "nhiễu/lùng bùng" và bị bỏ.
- Màu nền: gradient tuyến tính 2 điểm theo độ cao (`HYPSO_LAND_HEIGHTS`/`HYPSO_LAND_COLOURS`) —
  hiện đang là tông **đơn sắc/monochrome** (đen-xám-trắng), theo yêu cầu gần nhất của user.
- Đường đồng mức: **so trực tiếp "dải độ cao" (band index) giữa cột hiện tại và cột lân cận**
  (`topoBand()`/`topoLineStrength()`) — không nội suy gradient liên tục, đúng như JM thực sự làm
  ("cắt lát", không tính toán phức tạp).
- Đường có bóng mờ 1 phía (phía thấp hơn) để tạo chiều sâu nhẹ — `contourCoverage()`, dùng gradient
  cục bộ CHỈ để tính khoảng cách tới đường, không dùng để đổ bóng toàn mặt đất.
- Bờ biển: `coastLineStrength()` — vẽ 1 đường y hệt kiểu đường đồng mức, tại ranh giới đất/nước.
- Mặt nước: chấm mịn (`isWaterStipple()`) — lưới chấm so le, không phải màu phẳng, để phân biệt với
  đất mà không cần thêm màu.
- **Dùng `ColumnBuffer#groundHeight`, không dùng `height`.** Đây là độ cao mặt đất thật (đã xuyên
  qua lá cây, cỏ, hoa lúc khảo sát — xem `ServerTerrainProvider.sampleLive`), khác với `height`
  (Ground dùng — độ cao "nhìn từ trên xuống", tính cả cây/cỏ). Hai trường tách biệt CÓ CHỦ ĐÍCH.

## 4. Vướng mắc kỹ thuật hiện tại (Ground) — ĐANG CHỜ GEMINI

User so sánh 1 vùng đất bằng phẳng, thoải với ảnh JourneyMap cùng vị trí: **JM vẫn hiện rõ hình
khối/độ cao, còn Ground của mình gần như phẳng lì, không thấy gì.**

Đã thử tăng độ nhạy đổ bóng (`RELIEF_MACRO`, `RELIEF_MICRO` tăng lên, `RELIEF_SOFTNESS` giảm xuống)
để lộ rõ sườn dốc nhẹ hơn — **test lại bằng bench thì bị phản tác dụng**: địa hình Minecraft thật
(kể cả chỗ "phẳng") vẫn có nhiễu nhỏ theo từng block (do noise sinh địa hình), và bất kỳ cách đổ
bóng nào đủ nhạy để lộ ra 1 sườn dốc thoải cũng đồng thời lộ luôn nhiễu đó — tái hiện đúng lỗi
"sợi thép" từng gặp và bị chê ở Topo trước đây. Đã hoàn tác về giá trị gốc.

**Gốc rễ**: `reliefOf()` hiện tại chỉ dùng 1 khoảng lấy mẫu (`RELIEF_RUN = 3` cột) cho cả 2 mục đích
khác nhau — vừa phải đủ THÔ để không bắt nhiễu vặt, vừa phải đủ NHẠY để thấy hình khối lớn. Không
con số nào thoả mãn được cả 2 cùng lúc.

**Hướng gợi ý (chưa làm, để Gemini tự quyết định cách tiếp cận):**
- **Tách 2 số hạng đổ bóng theo 2 bán kính khác nhau**: 1 số hạng lấy mẫu RỘNG (ví dụ 8-12 cột, đã
  làm mượt trước khi so sánh — giống hệt kỹ thuật `smoothFloatField` đang dùng cho Topo) để bắt
  đúng hình khối lớn (đồi, thung lũng) mà không bị nhiễu; 1 số hạng lấy mẫu HẸP, RAW (1 cột, y hệt
  hiện tại) để giữ texture/độ nhám từng block ("chất Minecraft" mà user thích, đã xác nhận nhiều
  lần trong phiên trước). Cộng 2 số hạng lại — vừa có hình khối vừa có texture, không đánh đổi.
- Hoặc: 1 hàm hillshade thật kiểu bản đồ địa hình (Lambertian, dùng vector pháp tuyến từ độ cao đã
  làm mượt) thay cho phép so 2 điểm N/W hiện tại — chuẩn cartography hơn nhưng tốn công viết lại.
- **KHÔNG làm mượt toàn bộ dữ liệu độ cao rồi đổ bóng trên đó** (đã thử, đã bị user chê "giống bản
  đồ chất lượng kém, mất chất Minecraft, viền pixel mới là chất Minecraft") — đây là ràng buộc cứng,
  không phải gợi ý.

## 5. Ràng buộc/quy ước cần biết trước khi sửa

- **Thứ tự byte màu**: `BlockPalette`/`TerrainMips` đóng gói màu kiểu `0xAABBGGRR` (khớp
  `NativeImage` của Minecraft). `mapengine` (kể cả `Rasterizer.java`) dùng ARGB chuẩn `0xAARRGGBB`.
  `ForgeBlockStyle.swapRedBlue()` đổi qua lại ở đúng 2 điểm cầu nối — đổi sai 1 trong 2 chỗ này thì
  nước/đất đổi màu cho nhau (đã từng dính lỗi này, mất cả buổi mới tìm ra).
- **Giữ "chất Minecraft"**: user nhiều lần từ chối làm mượt/blur, thích viền pixel sắc/gãy khúc.
  Bất kỳ thay đổi nào làm mờ/mượt cần rất cẩn thận, tốt nhất là hỏi trước.
- **Ground và Topo phải độc lập**: sửa 1 bên không được ảnh hưởng bên kia (từng bị lỗi này, user
  phàn nàn "sửa Topo lại làm liên luỵ Ground"). `groundHeight` (Topo dùng) và `height` (Ground dùng)
  tách biệt hoàn toàn — đừng gộp lại.
- **Không sửa `ColumnBuffer`/`TerrainTile`/`ServerTerrainProvider`** trừ khi thực sự cần thêm 1
  trường dữ liệu mới — đây là phần backend, nếu cần thêm dữ liệu gì thì ghi vào `HANDOFF.md` để
  Claude làm, không tự thêm field ở phía Minecraft-side data (an toàn dedicated-server build).

## 6. Cách tự kiểm tra khi sửa `Rasterizer.java`

```bash
# Không cần mở game — sinh ảnh PNG trong vài giây
./gradlew :mapengine:bench
```

Ảnh xuất ra ở `ArtilleryTacticalTablet/mapengine/build/mapengine/`, đáng chú ý nhất:
- `phase2-scene-shaded.png` — Ground, dựng từ địa hình dựng tay (đồi/núi/thung lũng rõ hình, không
  nhiễu như địa hình ngẫu nhiên) để dễ đánh giá bằng mắt.
- `phase2-scene-hypso.png` — Topo, cùng địa hình đó.

Lưu ý: dữ liệu test tổng hợp KHÔNG có nhiễu per-block giống Minecraft thật (xem mục 4) — muốn kiểm
tra đúng cảm giác thật thì vẫn phải mở game (`./gradlew runClient`), và **nhớ xoá cache map cũ**
trước khi test nếu đổi cách tính dữ liệu (không cần nếu chỉ đổi màu/cách vẽ thuần):

```bash
rm -rf run/artillerytablet/mapengine
```

## 7. Lịch sử/tham khảo

`HANDOFF.md` (gốc repo) có nhật ký các lần bàn giao qua lại giữa Claude và Gemini — đọc để biết bối
cảnh gần nhất trước khi bắt đầu.
