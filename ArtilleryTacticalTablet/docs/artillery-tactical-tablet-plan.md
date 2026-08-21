# Add-on "Artillery Tactical Tablet" cho SuperbWarfare — Kế hoạch triển khai & Nhật ký tiến trình

## ▶ BẮT ĐẦU TỪ ĐÂY (cho phiên làm việc mới)

*(Cập nhật 2026-08-19 cuối phiên. Đọc mục này trước; mục 5 là nhật ký chi tiết nếu cần tra lý do.)*

### 🚩 BÀN GIAO — đọc trước khi gõ bất cứ thứ gì

**1. Cây làm việc CÓ THAY ĐỔI CHƯA COMMIT** (bản dựng lại vỏ máy ngày 2026-08-19: `TabletFrame`,
`UiButton`, `TabletScreen`, `CaseView`, tài liệu này). Nhánh `server-terrain-map-and-tablet-ui`.
`master` vẫn ở `0157ca8`.

**2. 🔴 VỎ MÁY — MỌI THIẾT KẾ CŨ ĐÃ BỊ XOÁ, ĐỌC KỸ ĐOẠN NÀY**

Ngày 2026-08-19 người dùng yêu cầu **xoá sạch mọi mô tả thiết kế vỏ máy** khỏi tài liệu này, xoá bản
phác `docs/mfcs-bezel-mockup.svg`, và dựng lại vỏ **chỉ từ tấm ảnh mẫu người dùng gửi**. Lý do rất cụ
thể: tài liệu đã tích lại nhiều đoạn văn giải thích *vì sao* vỏ trông như nó đang trông — gờ nổi,
hốc phím, khối góc, biển tên — và mỗi phiên sau lại đọc những đoạn đó rồi **sửa theo lời văn thay vì
theo ảnh**. Một lời giải thích cho một quyết định sai vẫn nghe rất hợp lý, nên nó sống lâu hơn cái
quyết định. Lần gần nhất nó khiến hốc phím bị **xoá** trong khi ảnh mẫu có hốc — chỉ là hốc phải tối,
còn code cũ tô nó sáng hơn mặt vỏ.

🔴 **LUẬT DUY NHẤT CHO VỎ MÁY, KHÔNG CÓ LUẬT NÀO KHÁC:**

> **Ảnh mẫu là nguồn sự thật. Tài liệu này KHÔNG mô tả vỏ máy.** Muốn biết vỏ phải trông thế nào thì
> mở ảnh mẫu ra nhìn, không đọc ở đây. Nếu một phiên sau thấy đoạn văn nào trong tài liệu này mô tả
> hình dáng vỏ máy, đó là đoạn văn bò ngược vào — xoá nó đi.

Cách kiểm, không cần bật Minecraft (~20 giây):

```bash
./gradlew caseView
```

Ra `build/mapcheck/case.png` cùng vài khung cắt phóng to. Nó vẽ bằng **chính code sản phẩm** qua
đường nối `client/screen/Paint.java` — trong game là `GuiPaint` bọc `GuiGraphics`, trong harness là
bộ ghi hình — nên ảnh nó cho ra **là** cái vỏ, không phải hình dung về cái vỏ. Dựng ở cửa sổ
1920×1080 để chồng thẳng lên ảnh mẫu mà so.

**Hai giới hạn của harness phải nhớ khi phán:**
- **Không có font.** Chữ trên nắp phím ra thành khối xám đúng bề rộng. Cỡ chữ thật **chỉ trong game
  mới thấy** — và đây là chỗ đã suýt lọt một lỗi: vỏ co giãn theo cửa sổ còn font Minecraft cao cố
  định 8px, nên nắp phím càng to thì chữ càng trông nhỏ.
- Nó không dựng bản đồ, nên ô kính luôn đen.

**Phần chrome bên trong màn hình** (thanh icon tròn, chip GPS, la bàn, thước, ô Cursor trong ảnh
mẫu) — người dùng đã chốt **không làm**.

🔴 **Cập nhật quan trọng nhất (2026-08-19, lượt 2): ba vòng chỉnh-theo-mắt liên tiếp đều sai, kể cả
sau khi xoá hết mô tả cũ — vì đọc ảnh bằng mắt không đủ.** Đã đổi cách: viết công cụ Java rời đo ảnh
mẫu bằng pixel thật (đọc `docs/ChatGPT Image 05_00_26 19 thg 8, 2026.png`, quét màu đổi, cắt-phóng-
kẻ-lưới để đọc toạ độ chính xác). Phát hiện cốt lõi giải thích cả ba lần sai: **khe giữa hai phím một
hàng rộng hơn chính cái phím** (~100px so với ~70px) — bản nào cũng làm ngược. Đã bỏ hẳn vòng hốc
quanh phím (ảnh mẫu không có, chỉ có viền mỏng + bóng một điểm ảnh). Chi tiết số đo và bảng tỉ lệ mới
ở entry cuối mục 5. **Nếu định đo lại ảnh mẫu, dùng cách này (Java + ImageIO), đừng nhìn bằng mắt.**

**Việc xếp hàng SAU đó** — *đất cũ còn sót sau khi thoát và vào lại cùng một thế giới*. **KHÔNG phải
lỗi** — hai đánh đổi đã chốt gặp nhau: server chỉ khảo sát lại khi có chunk đang nạp, client thì
*"ô đầy đủ không bao giờ hỏi lại"*. Ít nhất ba hướng khác giá nhau (server tự bỏ ô khi chunk được
ghi xuống đĩa / hết hạn theo tuổi / lệnh làm mới thủ công). Bắt đầu bằng `ServerTileCache.get` +
`ServerTileStore`, trình bày giá từng hướng rồi để người dùng chọn.

**3. Chỉ còn MỘT việc chưa test**: bắn thử sau khi gom `LaunchSolution` (mục #15). Mục #12 và #14 đã
được người dùng xác nhận xong 2026-08-18.

**4. Người dùng KHÔNG chơi hộ được ngay.** Claude không chạy được Minecraft; mọi xác nhận đều phải
qua người dùng tự chạy client. Luôn kết thúc bàn giao bằng khối `bash` có lệnh chạy.

**5. Quy ước làm việc người dùng đã chốt (2026-08-18) — áp dụng ngay, không cần hỏi lại:**
- Comment ngắn cho sửa nhỏ lặp khuôn (văn phong đầy đủ chỉ giữ cho quyết định kiến trúc thật sự mới)
- Kiểm bằng cách rẻ nhất trước khi làm việc đắt (đọc trang web trước khi chạy `runClient`/build đầy đủ)
- Chủ đề không liên quan → phiên mới, đừng nối dài một thread
- **Người dùng chạy `runClient` rồi bảo "đọc log đi" — Claude tự đọc `run/logs/latest.log`.** Cách rẻ
  nhất là gắn một watcher vào `map trace` thay vì hỏi người dùng chép lại
- 🔴 **Sửa vỏ máy thì chạy `./gradlew caseView` và TỰ NHÌN ẢNH, đừng nhờ người dùng bật game.** Nó
  vẽ bằng code sản phẩm nên không thể trôi khỏi thứ game vẽ, và ảnh phóng 6× là thứ duy nhất đủ để
  phán một đường gờ rộng 9px — ở 1 điểm ảnh/1 điểm ảnh, một cái bậc **chưa hề được vẽ** đã sống sót
  qua bốn lần nhìn

**6. 🔴 KHÔNG PHẢI LỖI NÀO CŨNG LÀ LỖI CỦA MOD.** Người dùng báo "xé hình khi kéo bản đồ"; hình học
đã được kiểm là đúng, nghi phạm còn lại là `MAG nearest` (một mục *"đừng bàn lại"*). Thay vì sửa,
Claude đưa ra **một dự đoán kiểm được** ("nếu đúng thì phải mạnh ở 250m và biến mất từ 2000m") —
người dùng thử và phát hiện đó là **vsync bên máy họ**. Nếu đã sửa theo chẩn đoán thì đã phá một
quyết định kiến trúc để chữa một thứ không tồn tại.

### ⚠️ Đánh giá thẳng: mod CÓ phình ra, và ở đúng một chỗ

**Không phải ở phần vẽ** — phần đó đơn giản và nhanh (~0,6 ms/khung), không còn gì để lấy.

**Phình ở LOGIC ĐIỀU KHIỂN của đường lấy đất** — bảy lỗi cùng một hình dạng (hai phần không biết
nhau), xem nhật ký 2026-08-16/17 nếu cần chi tiết từng lỗi. Đã dồn về `terrain/SurveyLimits.java`
(suy ra từ nhau) + `mapcheck/Limits.java` (khẳng định lại mỗi lần `mapCheck`).

**Và một mảng THỨ HAI mới lộ ra ở phiên 2026-08-18: việc đổi thế giới.** Sáu lỗi khác nhau — kho
server, đọc đĩa client, bake nền, ngân sách khảo sát, con trỏ hâm nóng, lệnh bắn đang chờ — đều cùng
một nguyên nhân gốc: **không có nơi nào trong mod định nghĩa "một thế giới bắt đầu/kết thúc"**, nên
mỗi lớp tự đoán và mỗi lớp đoán sai một kiểu. Đã vá từng chỗ; **chưa có một điểm phát tín hiệu chung**
— nếu còn gặp lỗi kiểu "đất thế giới cũ" lần nữa, đây là hướng nên nghĩ tới trước, không phải vá thêm
một lớp thứ bảy.

**⇒ Nếu định đổi một con số của đường lấy đất, đổi TRONG `SurveyLimits`. Nếu định thêm state tĩnh
theo dimension, hỏi trước: "cái này dọn thế nào khi server dừng?"**

### 🔴 BẢNG SỐ ĐIỀU CHỈNH — phần lớn giờ là SUY RA, không phải chỉnh tay

| Số | Suy ra thế nào | Giá trị |
|---|---|---|
| `TOTAL` | chọn tay — trần bảo vệ server | 256 |
| `PER_PLAYER` | chọn tay — phần của một người | 96 |
| `TILES_PER_REQUEST` | chọn tay — sức chứa một gói tin | 32 |
| `MAX_IN_FLIGHT` | `PER_PLAYER + 2 × TILES_PER_REQUEST` | 160 |
| `RETRY_AFTER_MS` | `MAX_IN_FLIGHT ÷ 2 ô/giây` (chậm nhất từng đo) | 80 s |
| `MAX_QUEUED_PER_PLAYER` | `= MAX_IN_FLIGHT` | 160 |
| `REQUEST_BATCH` (client) | `= TILES_PER_REQUEST` | 32 |
| `SPARE_FOR_WARMING` | `TOTAL / 2` | 128 |
| `PER_PASS_QUIET` (warmer) | `SPARE_FOR_WARMING / PASSES_TO_FILL` ⇒ 64 ô/giây khi rỗi | 16/lượt |
| `PER_PASS_BUSY` (warmer) | chọn tay ⇒ 16 ô/giây khi có người nhìn | 4/lượt |

Còn chỉnh tay, ràng buộc phải nhớ: `PATCH_MAX_PERCENT`, `MAX_BUILDS_PER_FRAME`, `MAX_LIVE_TEXTURES`,
`TILES_PER_SHEET`/`MAX_QUADS_ACROSS`, `SCAN_TILES`, `MAX_REMEMBERED` — chi tiết ở nhật ký 2026-08-17.

### Đường đi của MỘT Ô — 10 chặng, dừng ở chặng đầu tiên trả lời được

1. Chunk client đang giữ — `ClientTerrainSampler`, tức thì
2. Kho đĩa client — `TerrainDisk`, `<gameDir>/artillerytablet/<world>/<dim>/` (tên thư mục = **tên
   thư mục save**, không phải tên hiển thị — sửa 2026-08-18, xem lý do trong nhật ký)
3. Cửa sổ mạng — `MAX_IN_FLIGHT`, xin gần trước
4. Ngân sách server — `ServerSurveyBudget`, có hàng đợi
5. Cache RAM server — `ServerTileCache`, 2 giây
6. Kho đĩa server — `ServerTileStore`, có chỉ mục RAM
7. Chunk server đang nạp — `getChunkNow`
8. 4 hàng đọc riêng — `TerrainChunkReader`
9. `IOWorker` của level — hỏi khi (8) trả rỗng
10. Hâm nóng nền — `ServerTerrainWarmer`, **thích ứng**: 64 ô/giây khi rỗi, 16 khi có người nhìn
    (`ServerSurveyBudget.anyoneWatching()`), xin chỗ qua `beginWarming()`

### Việc tiếp theo

**Đã xong (2026-08-17/18)**: Bước 0 (đo — kho server 0% hit trong một phiên, đúng thiết kế), Bước 1
(chỉ mục section theo Y), Bước 2 (hâm nóng thích ứng — **xác nhận qua log thật**: `320 warmed
(quiet)` = 64 ô/giây), gộp núm chỉnh, nhận diện thế giới đúng, gom công thức ngắm về một chỗ.

**Bước 0b — dedicated server: 🕒 vẫn hoãn**, chỉ nhắc khi đụng `ServerSurveyBudget`/`ServerTileStore`/
`SurveyLimits` hoặc người dùng chủ động hỏi.

**Ba hướng đã bàn cuối phiên 2026-08-18, CHƯA đo, CHƯA làm** — nếu người dùng muốn tiếp tục giảm độ
trễ lần khảo sát đầu tiên (khác với việc dựng sẵn ở Bước 2):
1. **Tách dữ liệu độ cao khỏi màu** — độ cao đọc từ `Heightmaps.WORLD_SURFACE` (rẻ), màu phải giải
   mã từng section (đắt hơn nhiều lần, chưa đo tỉ lệ thật). Trả lời sớm cho ballistics/cảnh báo vướng
   đạn, màu về sau. Đánh giá: đáng đầu tư nhất, nhưng **đo trước khi làm**.
2. **Lệnh khảo sát trước tuỳ chọn** (`/artillerytablet survey <bán kính>`) — chỉ giúp đất **đã sinh
   nhưng chưa khảo sát**, không giúp đất chưa ai từng tới. Phần lớn giá trị đã có sẵn nhờ Bước 2.
3. **Ô nhỏ hơn** (32×32 thay 64×64) — tổng việc không đổi, nhịp lấp mượt hơn. Đổi lấy nhiều gói tin/
   sổ sách hơn.

### 🔬 CHƯA TEST — cập nhật 2026-08-18

| # | Cái gì | Trạng thái |
|---|---|---|
| 1–7, 9–11, 13 | Xem chi tiết nhật ký 2026-08-17 | ✅ đã test OK, hoặc chưa đủ ưu tiên để lo |
| 12 | **Đất thế giới cũ không lọt sang thế giới mới** | ✅ **Người dùng xác nhận xong 2026-08-18.** Sửa **3 lần** (generation stamp → tên thư mục save → dấu chấm trong path). Phần **kiểm được ngoài game giờ có kiểm thật** trong `mapCheck` (con dấu `epoch` + tên thư mục save), và cả hai đã được xác nhận đỏ khi phá guard. Sáu tầng dọn khi đổi thế giới đã rà lại bằng đọc code. **Còn lại phải chơi mới biết**: đổi thế giới trong game rồi mở tablet. |
| 14 | **Mờ đóng băng ở 4000m đổ xuống** | ✅ **Tìm ra và xác nhận bằng số đo** (2026-08-18): ô vừa tạo không đặt `lastDrawn` ⇒ bị đuổi ngay cuối khung hình tạo ra nó ⇒ mỗi khung dựng 4 ô rồi vứt đúng 4 ô đó. Log sau khi sửa: mờ tụt về 0 trong ~8s, `evicted/frame` từ 4.0 cố định xuống ~0. Xem nhật ký + bài học 26 |
| 15 | **Bắn thử sau khi gom `LaunchSolution`** | 🔴 **Việc chưa test duy nhất còn lại.** Code chỉ đổi *chỗ* công thức nằm, không đổi công thức — nhưng chưa ai bắn lại để chắc chắn |
| 16 | **Nước hết răng cưa ở biên quần xã** | ✅ Người dùng xác nhận 2026-08-18. Lưu ý: `mapCheck` **không kiểm được** đường này (harness không có `Minecraft.getInstance()` ⇒ tint luôn rơi về fallback) |
| — | ~~Xé hình khi kéo~~ | ⚪ **Không phải lỗi của mod** — vsync bên máy người dùng. Xem điểm 6 phần bàn giao |

### Chưa xác nhận / còn treo
- Sinh địa hình từ seed: đã bác, đồng ý — không mở lại.
- `BlockPalette.forget()` chỉ gọi khi đổi thế giới, không khi đổi resource pack (lỗi nhỏ, chưa ai
  phàn nàn, để khi cần).
- Kho server làm đất rất xa mọi người chơi có thể là ảnh cũ — đánh đổi đã chốt.
- **BlueMap: đã thử, không dùng được** (đòi Java 21, dự án khoá Java 17). Đừng thử lại trừ khi tìm
  được bản BlueMap cũ hơn còn hỗ trợ Java 17. Toạ độ CurseForge giữ lại trong `gradle.properties`
  (`bluemap_curse_file`) nếu cần tra lại.

### Kiến trúc — những quyết định đã chốt, đừng bàn lại

| Chủ đề | Quyết định | Vì sao |
|---|---|---|
| Nguồn địa hình | **Server tự khảo sát**, không dùng mod bản đồ nào | Client chỉ biết thứ nó đã được gửi. **Tác giả JourneyMap đã xác nhận đúng kiến trúc này** (2026-08-15) |
| Chunk chưa sinh | **Tuyệt đối không sinh mới** — chỉ `getChunkNow` và `chunkMap.read` | Sinh chunk là một chiều, ghi xuống đĩa, phình thế giới hàng chục GB |
| Tích hợp mod bản đồ | **ĐÃ ĐÓNG.** Xaero/JourneyMap/WhyMap chỉ là **thước đo** | Xem nhật ký 2026-08-15. Không mở lại nếu không có tin mới từ tác giả |
| Phụ thuộc mod | **Chỉ SuperbWarfare** | Mọi mod khác trong `build.gradle` đều `runtimeOnly` — trình biên dịch từ chối code chạm vào chúng |
| Kênh mạng | **`ModNetwork` riêng**, ID viết tay | Dùng chung kênh SBW làm ID dịch chỗ khi SBW cập nhật |
| **Phiên bản gói tin** | `PROTOCOL_VERSION = "1." + TerrainTile.FORMAT_VERSION` | Nửa sau **không viết tay được** — đổi bố cục ô mà quên bump đã xảy ra một lần rồi |
| **Nguồn màu bản đồ** | **Block id + trung bình texture mặt trên**; bảng màu vanilla chỉ là fallback | 62 màu là quá ít: mọi loại gỗ một nâu, mọi loại đá một xám |
| **Thứ tự tìm texture** | quad mặt trên → quad bất kỳ → **particle icon** → bảng màu | Particle icon của `grass_block` là **đất** — nó chỉ đúng làm bậc cuối, cho chất lỏng |
| **Vòng đời sheet bản đồ** | Thuộc về **thế giới**, không thuộc màn hình | Đóng tablet mà huỷ sheet nghĩa là mỗi lần mở đều bắt đầu từ số không |
| **Đổi mức khi zoom** | Vẽ **lượt phủ thô hơn 2 mức nằm dưới** cho tới khi lượt tinh kín | Đất không thiếu, chỉ chưa dựng ở mức mới. Mờ rồi nét, thay vì đen rồi từng mảnh |
| Đăng ký packet | Luôn trong `event.enqueueWork` | `FMLCommonSetupEvent` chạy song song giữa các mod |
| Giao diện | **Điểm ảnh logic + font Minecraft**, tự viết `UiButton`/`UiField` | Widget vanilla bị loại vì texture nút của nó. *(Chỉ nói về cơ chế vẽ. Hình dáng vỏ máy: xem luật ở mục bàn giao.)* |
| **Lớp bản đồ** | Vẽ ở **điểm ảnh màn hình** (`1/guiScale`) | Lưới GUI là trần cứng 563 điểm ảnh; màn hình cho 1689 |
| **Chữ** | **Tuyệt đối không đi qua ma trận điểm ảnh vật lý** | Font là bitmap nướng cho lưới giao diện. Nhãn lưới được gom trong ma trận rồi vẽ ở ngoài |
| Chuyển động bản đồ | Tâm là **số thực**; mọi lớp dựng từ **gốc nguyên** rồi **dịch chung một phép biến đổi** | Mỗi lớp tự làm tròn thì chúng trượt lên nhau — nguyên nhân thật của cả gợn sóng lẫn giật |
| **Thu nhỏ bản đồ** | **Kim tự tháp mip, trung bình 2×2 trong ánh sáng tuyến tính** | Lấy mẫu thưa chỉ nhìn 1,6% mặt đất. Trung bình nhìn 100%, và **rẻ hơn** |
| **Bộ lọc texture** | **MAG nearest, MIN linear** | Phóng to mà làm mượt là bịa ra đất chưa khảo sát; thu nhỏ mà lấy một mẫu là răng cưa |
| **Chọn mức** | Stride phải **≤ số block mỗi điểm ảnh màn hình** | Dữ liệu thô hơn màn hình thì trông mềm dù lọc kiểu gì |
| Cập nhật địa hình | Ô **đầy đủ** không bao giờ hỏi lại; **không hiện hố đạn** | Bản đồ chỉ huy hoả lực thật không tự cập nhật thiệt hại |
| Ngôn ngữ | **Chỉ tiếng Anh** (`en_us.json`) | Người dùng chốt 2026-08-14 |
| Màu | Xám trung tính + 4 màu có nghĩa | Màu mang nghĩa, không trang trí — xem `TabletTheme` |
| Nấc zoom | **Số tròn quân sự** 250…32000, lưới theo thang 1-2-5 | Lưới tồn tại để **đếm cự ly bằng mắt**; không ai đếm theo 128 |

### Bản đồ file

| Đường dẫn | Vai trò |
|---|---|
| **`terrain/SurveyLimits.java`** | **Đọc TRƯỚC khi đổi bất kỳ số nào của đường lấy đất.** Nơi duy nhất giữ nhóm số đó, suy ra từ nhau, có `static {}` tự kiểm. Chung cho cả hai phía — cửa sổ client và phần server là **hai đầu của một thoả thuận**, tách file là chúng bắt đầu trôi |
| **`src/mapcheck/Limits.java`** | Biến bảng ràng buộc thành kiểm chạy được: đọc hằng số thật qua reflection, khẳng định từng quan hệ **kèm câu giải thích hậu quả nếu phá** |
| `terrain/` | `TerrainTile` (định dạng + `FORMAT_VERSION` + nén + `contentHash`/`isComplete` + **`idOf`**; **7 byte/cột: block id 2B, cao độ 2B, độ sâu 1B, biome 2B**, mỗi nửa của trường 2 byte là **một dải riêng**), `ChunkNbtSampler`, `ServerTerrainProvider`, `ServerTileCache` |
| `client/terrain/` | `TerrainClientCache` (kho ô + mip + mã băm + `generation`), **`BlockPalette`** (block id → màu + loại tint, lười, quên khi đổi thế giới), **`TerrainMips`** (chuỗi mip 2×2 + **toàn bộ việc quyết định màu**), `TerrainImage` (sheet, mức, bộ lọc, đổ bóng, đặt vị trí), `Light` (LINEAR/encode dùng chung) |
| `client/screen/` | `Ui` (vẽ + `inDevicePixels` + `batched`), `UiButton`/`UiField`, `TabletTheme`, `TabletScreen`, `TabletPanels`, `MapPanel`, `TabletTab` |
| `fire/` | `FireMode`, `ReachabilityCheck`, `FlightProfile`, `ArtilleryAimTracker` |
| `network/` | `ModNetwork` + 15 packet |
| `src/mapcheck/` | Bốn harness chạy ngoài Minecraft — xem "Cách làm việc" |

**Chú ý về sở hữu** — bốn thứ, mỗi thứ đúng một chủ:
- `TerrainMips.groundColour` quyết định màu một cột (nền + tint quần xã + lớp nước). `TerrainImage.shade`
  chỉ còn đổ bóng và làm tối. Hai nơi cùng tính màu làm bản đồ **đổi diện mạo đúng ở nấc zoom đổi mức**.
- `BlockPalette` quyết định màu *nền* của một block, và **nói ra** màu đó đến từ texture hay từ bảng
  màu (`isPreTinted`) — vì hai nguồn cần **hai luật tint khác nhau**.
- `Light` là nơi duy nhất bẻ ánh sáng. Trung bình texture và trung bình mip phải bẻ giống hệt nhau.
- `TerrainTile.idOf` là nơi duy nhất biến block thành số, vì **hai** sampler cùng nuôi một ô.

### Cách làm việc đã thống nhất

- Claude **không chơi được Minecraft** → code xong phải để người dùng tự chạy client test và báo lại.
- **Luôn kèm lệnh chạy client** trong khối `bash` khi bàn giao để test.
- **Luôn xác minh API bằng `javap` trên jar thật đang dùng**, không tin source GitHub hay tài liệu.
- Sau mỗi việc đáng kể, **ghi thêm entry mới vào mục 5** (chỉ thêm, không sửa xoá entry cũ).
- **`gradlew build` + boot sạch KHÔNG phải bảo chứng.** Chúng chứng minh mã **biên dịch và nạp**
  được. `runClient` chỉ đi tới màn hình chính — nó **không mở tablet**, nên không chạy một dòng nào
  của đường vẽ bản đồ. Nói rõ điều này khi bàn giao thay vì để "boot sạch" nghe như đã test.
- 🔴 **`./gradlew mapCheck` phải chạy VÀ ĐỌC KẾT QUẢ sau mỗi lần đổi tên/đổi chữ ký trong
  `TerrainImage`, `TerrainClientCache`, `TerrainDisk`, `ServerTileStore`.** Harness tìm hàm **bằng
  tên lúc chạy**, nên đổi tên **không làm hỏng build** — nó làm hỏng harness, im lặng, và kéo theo
  mọi kiểm đứng sau nó. Đã xảy ra: `gather`→`gatherInto` giết `mapCheck` và **bốn kiểm cuối không
  chạy lần nào** cho tới khi có người tình cờ nhìn (2026-08-17). "Build sạch" **không** thay được
  dòng `map checks passed`.
- **Harness chạy ngoài Minecraft là kênh kiểm chứng thật sự.** Đã vào repo — chạy bằng:

  ```bash
  ./gradlew mapCheck
  ```

  - **`RoundTrip`** → ghi một ô rồi đọc lại, và **in số byte trên dây**. Đây là thứ khiến đổi bố cục
    ô an toàn để làm: sai một offset thì **không có gì ném ra**, ô vẫn giải mã được.
  - **`Bounds`** → đọc hằng số thật qua reflection, chạy đúng vòng lặp `build()` và đúng các mẫu
    `slope()` lấy, khẳng định mọi chỉ số nằm trong mảng. **Đã bắt crash `MARGIN`.**
  - **`Evict`** → nhồi cache vượt trần rồi gọi `evictSurplus`. **Đã bắt crash fastutil.**
  - **`Zooms`** → báo cáo, không khẳng định: mỗi nấc zoom chọn mức nào, dữ liệu có thô hơn màn hình không.
  - **`TileRender`** → dựng đường tô **thật** ra PNG (`build/mapcheck/`) để **nhìn được bản đồ**. Bắt
    được lava-thành-nước, kênh R/B, nước bùng vân.
  - Tất cả gọi **code sản phẩm qua reflection**, không viết lại gì, nên không thể trôi khỏi thứ game vẽ.
  - Chúng chạy được vì `TerrainImage` không đụng GL trong constructor. Runner gọi `Bootstrap.bootStrap()`
    vì registry block không tồn tại ngoài game.
  - **Giới hạn phải nói ra**: không có `Minecraft.getInstance()` nên **`BlockPalette` luôn đi fallback
    bảng màu**. Đường tra texture **không** được harness nào chạm tới.

### Những bài học lặp lại — đọc trước khi sửa bất cứ thứ gì

**Về tích hợp mod khác**

1. **Đừng tái hiện điều kiện tiên quyết của mod khác — hãy quan sát trạng thái sau khi gọi.** Dính 4
   lần: `setTarget`, `modifyGunData`, `canRequestReload_unsynced`, và ép thứ tự vẽ bằng `flush()`.
2. **Xác minh API bằng `javap` trên jar thật, đừng đoán.** Lỗi hoán kênh R/B sống sót nhiều ngày vì
   tôi tin một comment mình tự viết thay vì đọc bytecode.
3. **Tài liệu mô tả ý định lúc viết; code mô tả ý định hiện tại.** Khi hai thứ mâu thuẫn, **code
   thắng**. Tôi đọc `Server Docs` của JourneyMap (bản 5.7.1) rồi kết luận sai về code 6.0.1 — trong
   khi chính tôi đã đọc code và thấy dấu vết ngược lại. **Và tài liệu này cũng là tài liệu**: mục
   "việc tiếp theo" kỳ trước ghi lý do sai cho món byte phiên bản (dẫn WhyMap/JourneyMap, vốn ghi ô
   xuống đĩa — ta thì không). Đọc lại code trước khi làm theo ghi chú của chính mình.

**Về hình học giao diện — nhóm gây lỗi nhiều nhất trong dự án**

4. **Một vùng hình học chỉ được định nghĩa ở ĐÚNG MỘT NƠI.** Dính ~7 lần. Lần gần nhất: thêm viền
   texel làm vùng tô lớn ra mà `MARGIN` không lớn theo → **crash ngay khung hình đầu tiên**.
   Sửa bằng cách cho suy ra (`MARGIN = BORDER + RELIEF_RUN`), không viết tay.
5. **Mọi mảng nền phải vẽ xong TRƯỚC khi kẻ bất cứ đường nào.** Dính 3 lần.
6. **Thứ căn giữa bằng pixel thì hộp chứa nó phải cùng tính chẵn lẻ.**
7. **Đổi kích thước một thứ thì phải xem lại nội dung bên trong nó.**
8. **Dọn dẹp phải đi hết chuỗi sở hữu.** Xoá dữ liệu không chạm tới ảnh đã dựng từ dữ liệu đó.
8b. **Hai nguồn cho cùng một giá trị thường cần hai luật xử lý, không phải một.** Màu nền của một
    block đến từ texture (chưa tint, phải **nhân**) hoặc từ bảng màu (đã nướng sẵn xanh plains, phải
    **tỉ lệ theo reference**). Dùng nhầm luật sai rất nặng — nên nguồn phải được **nói ra**
    (`isPreTinted`), không được đoán từ giá trị.

**Về hiệu năng**

9. **Bỏ chi phí lớn nhất thì chi phí kế tiếp thành lớn nhất.** Sau mỗi lần tối ưu phải **liệt kê lại**
   những gì còn chạy mỗi khung hình.
10. **Lỗi chỉ xuất hiện ở khoảng GIỮA một dải là dấu hiệu của một nhánh code chỉ chạy ở khoảng giữa.**
    Tương tự: **lỗi chỉ ở đúng hai nấc zoom là một dấu vân tay**, không phải một triệu chứng — nó loại
    hết mọi nguyên nhân tỉ lệ thuận với zoom.
11. **Mọi lớp chuyển động cùng nhau phải chia chung một gốc và một phép dịch.**
12. **Một lời gọi API trông rẻ không có nghĩa là nó rẻ — đọc nó ra.** `GuiGraphics.fill` là **một
    lệnh vẽ riêng** mỗi lần gọi.
13. **Đổi `HashMap` sang map khoá nguyên thuỷ KHÔNG phải thay thế trong suốt.** Nó đánh đổi việc cấp
    phát lấy **những quy tắc về cách được phép dùng các view của nó**. Entry set của fastutil tái sử
    dụng một đối tượng — copy nó ra list là crash, và crash đó **ngủ đông vài tuần** chờ đủ dữ liệu.
13b. **Cache đúng hay sai nằm ở chỗ "cái gì đổi thì phải làm lại", và câu đó phải hỏi về DỮ LIỆU,
    không phải về đối tượng.** `coverStamp` dùng identity của tile: một ô khảo sát lại ra y hệt từng
    byte vẫn là đối tượng mới → 16 sheet dựng lại → 259 lần dựng trong 2 giây mà không một ô nào mới.

**Về cách làm việc**

0. **Không đo thì không được tối ưu.** Ba lỗi hiệu năng của lớp bản đồ đều có nguyên nhân **khác thứ
   nó trông giống**. Cách rẻ nhất để biết là **đếm ngay trong code sản phẩm** — `-Dartillerytablet.mapTrace`
   mất mười lăm phút viết và trả lời được câu hỏi mà tôi đã đoán sai hai lần trước đó.

14. **Chốt an toàn "cho chắc" có thể tự nó là hành vi sai.** Ở thiết bị bắn, *không chọn gì* an toàn
    hơn *chọn hộ một thứ khác*.
15. **Phác thảo trước khi code, cho mọi thay đổi bố cục.**
16. **Phân biệt thay đổi GÂY RA lỗi với thay đổi LÀM LỘ lỗi.** Dính 3 lần. Lần gần nhất tôi vừa làm
    lộ vừa làm nặng thêm cùng lúc.
17. **Một phép thử sai buộc tội code những lỗi nó không có.** Dính 2 lần trong cùng một phiên: cảnh
    thử dựng bằng tổng các hàm sin sinh moiré khi thu nhỏ (trông y hệt lỗi bộ lọc), và harness gán
    nhãn `SOFT` cho stride 1 (vốn là **sàn**, không phải "quá thô"). **Nghi ngờ phép thử trước khi
    nghi ngờ code.**
18. **Khi cố ý bỏ một thứ rồi tự viết "chắc không sao", đó là lúc phải đưa nó thành câu hỏi cần
    kiểm** — không phải một ghi chú trấn an. Damping nước ở mức thô bị bỏ đúng theo kiểu đó và hỏng
    ngay ở nấc zoom dễ nhìn nhất.

19. **Một BẢNG ràng buộc trong tài liệu không phải là cách chữa — nó chỉ là danh sách những chỗ sắp
    hỏng.** Bảng ở đầu tài liệu này được viết ra chính vì sáu lỗi cùng loại, và **lỗi thứ bảy vẫn
    xảy ra ngay dưới nó** (`REQUEST_BATCH = 64` / gói tin 32) — bảng không ngăn được gì, vì người
    sửa số đi tới **chỗ số được dùng**, chứ không tới chỗ nó được chép lại. Cách chữa thật có hai
    nửa: (a) **suy ra từ nhau** ở một file, để giá trị phá quan hệ là thứ *không viết được*; (b) một
    **kiểm chạy được** đọc hằng số thật, để người cố tình tách chúng ra lại bị chặn ở `mapCheck`.
    Cùng quy tắc với luật số 4 về hình học, chỉ khác là áp cho các con số điều khiển.

20. **Cắt bớt trong im lặng là cách một lỗi tự giấu mình lâu nhất.** `encode` nhận 64 ô, ghi 32, và
    **không nói gì** — nên bên gửi tiếp tục tin rằng nó đã hỏi về ngần ấy đất. Ở đâu có `Math.min`
    trên dữ liệu người khác đưa xuống, ở đó phải hỏi: *nếu số này thật sự nhỏ hơn thì ai là người
    đang tin sai?* Nếu câu trả lời là "chính người gọi", thì phải **kêu lên**, không được cắt lặng.

21. **Cache tĩnh sống lâu hơn thế giới sinh ra nó.** Single player nạp thế giới này rồi thế giới khác
    trong **cùng một tiến trình**, và `minecraft:overworld` là **cùng một khoá** ở cả hai. Ba chỗ đã
    dính: kho server, cache ô server, và lượt đọc đĩa client đang bay khi đổi thế giới. Luật: mọi
    `static` map khoá theo dimension đều phải có **một chỗ dọn khi server dừng**.

22. **Khi đưa việc sang luồng khác, thứ phải đi cùng nó không phải là dữ liệu — mà là CÂU TRẢ LỜI
    cho mọi thứ nó sẽ hỏi.** Bake được đưa sang luồng nền cùng dữ liệu ô (có khoá, đúng), nhưng
    không cùng hai bảng tra mà nó vẫn phải hỏi game — và game chỉ trả lời trên render thread. Chữa
    đúng là **làm cho nó không còn gì để hỏi** (warm lúc ô về), không phải thêm khoá. Một cái khoá
    không biến việc-thuộc-luồng-khác thành an toàn, nó chỉ biến việc đó thành **tuần tự**.

23. **Khi thêm một trạng thái trung gian, mọi câu hỏi nhị phân về trạng thái cũ đều thành nghi
    phạm.** Dựng sheet ở luồng nền thêm trạng thái **"đang chờ"** vào giữa "trống" và "đã vẽ". Câu
    hỏi hai nhánh `empty` không diễn tả nổi ba trạng thái, nên **mỗi** chỗ hỏi nó sai một kiểu: chỗ
    vẽ **crash**, chỗ lối tắt **kẹt vĩnh viễn**, chỗ budget **chậm gấp bốn**. Ba triệu chứng không
    giống nhau chút nào, một dòng sửa.

24. 🔴 **MỌI việc bất đồng bộ phải mang theo dấu THẾ GIỚI nó được giao, không chỉ dấu dữ liệu.**
    Dính **ba lần trong hai ngày**, ở ba tầng khác nhau, và mỗi lần đều hiện ra là *một mảng đất của
    thế giới cũ nằm trên bản đồ thế giới mới*: kho server (chữa bằng dọn ở `ServerStoppingEvent`),
    lượt đọc đĩa client (chữa bằng `epoch`), và lượt bake nền (chữa bằng `generation`). Một câu hỏi
    duy nhất, hỏi ở mọi chỗ có việc chạy nền: ***câu trả lời này là về thế giới nào?*** Dọn hàng đợi
    lúc đổi thế giới **không đủ** — thứ nguy hiểm là thứ **chưa xong**.

25. **Một cái tên người dùng đặt được không phải là một danh tính.** Kho đĩa client xếp theo **tên
    hiển thị** của thế giới, mà "New World" là tên game gợi ý mọi lần ⇒ hai thế giới cùng tên dùng
    chung một kho. Khi cần một danh tính, hãy lấy thứ **hệ thống tự sinh và tự bảo đảm không trùng**
    (ở đây: tên thư mục save), đừng lấy thứ người dùng gõ vào.

26. **Khi đã gom một sự thật "mỗi lối ra phải nói một câu" vào một chỗ, hãy đi tìm những sự thật CÙNG
    HÌNH DẠNG còn đang bị chép lại ở từng lối ra.** `TerrainImage` đã học đúng bài này một lần với
    `drawable()`: câu hỏi "ô này vẽ được chưa" từng được viết lại ở tám lối ra, một trạng thái mới ra
    đời làm sai một trong tám, và cách chữa là dồn mọi lối ra qua `drawableOrNull`. **Nhưng `lastDrawn`
    là y hệt cái hình dạng đó** — "ô này đang được cần" — và vẫn nằm rải rác ở từng lối ra. Nó thiếu ở
    đúng một chỗ (nhánh tạo ô mới), và hậu quả nặng hơn hẳn lần trước: bản đồ dựng đất rồi tự vứt đi,
    vĩnh viễn. Cùng luật số 4 về hình học, chỉ khác là áp cho **thuộc tính vòng đời**, không phải toạ
    độ: một ô vừa được yêu cầu phải **tự nói ra rằng nó đang được cần**, và phải nói **trước khi** có
    bất cứ thứ gì được phép dọn chỗ.

---


> **Cập nhật 2026-08-13**: Đổi hướng — Claude tự triển khai toàn bộ addon (không bàn giao Gemini
> nữa). Tài liệu này từ giờ đóng vai trò vừa là **kế hoạch/lộ trình**, vừa là **nhật ký tiến
> trình + lỗi** để theo dõi xuyên suốt dự án. Cập nhật mục 5 (Nhật ký) sau mỗi phiên làm việc.

Repo tham chiếu:
- Mod gốc: https://github.com/Mercurows/SuperbWarfare (Kotlin 79%/Java 21%, Forge, MC 1.20.1/1.21.1)
- Addon mẫu chính chủ: https://github.com/LightQuanta/SuperbWarfareExtensionModExample

Quy ước: ✅ Đã xác nhận từ source thật · ⚠️ Còn mở/cần quyết định khi implement · 🔧 Việc cần làm

---

## 1. Mục tiêu & phạm vi (đã chốt ngày 2026-08-13)

Add-on thêm item **"Artillery Tactical Tablet"**, cho phép điều khiển pháo tự hành SBW theo kiểu
gián tiếp (indirect fire) thay vì ngắm trực tiếp:

- Chọn mục tiêu, bắn theo nhiều mode: đơn phát / ripple / salvo (MRSI để sau, xem mục 4 — Phase 9).
- Dùng **đạn có sẵn của SBW** — **không cần thêm loại đạn mới** (đã chốt, loại bỏ rủi ro đụng vào
  enum `Ammo` đóng cứng).
- **Không cần minimap ngay từ v1** — sẽ tích hợp map thật (Xaero's Minimap / JourneyMap...) ở giai
  đoạn sau, dựa trên các mod minimap phổ biến đã có sẵn trên thị trường thay vì tự vẽ map từ đầu.

## 2. Quyết định kiến trúc đã chốt (dựa trên khảo sát source thật)

| Vấn đề | Quyết định | Căn cứ |
|---|---|---|
| Phụ thuộc SBW | Kéo qua **CurseMaven** jar deobf, giống hệt `addon-example/build.gradle`: `implementation fg.deobf("curse.maven:superb-warfare-...:<build-id>")` | ✅ Đây là pattern chính chủ duy nhất được author demo |
| Version target | **Forge 47.2.0, MC 1.20.1, Java 17** | ✅ Theo `addon-example` |
| Ngôn ngữ addon | **Java** (không dùng Kotlin plugin) — bám sát addon mẫu, giảm rủi ro config Gradle lạ; interop với API Kotlin của SBW vẫn gọi được bình thường qua `@JvmStatic`/getter-setter compiled | ✅ Addon mẫu dùng Java thuần, chứng minh đủ dùng |
| Ballistic solver | Gọi thẳng `com.atsuishio.superbwarfare.tools.TrajectoryCalculator.calculateLaunchVector(start, target, v, g, isDepressed)` (public `object`, `@JvmStatic`) — **không viết lại** | ✅ |
| Binding tablet ↔ pháo | Tái dùng pattern NBT của `ArtilleryIndicatorItem` (`open class`, `bind/addCannon/removeCannon/setTarget` public, lưu UUID trong NBT list `Cannons`) | ✅ |
| Gửi lệnh bắn | Gọi `VehicleEntity.vehicleShoot(...)` (`open`, override ở `ArtilleryEntity`/`MortarEntity`) từ packet handler phía server | ✅ |
| Networking | Đăng ký packet riêng (`ArtilleryTabletFireMessage`...) qua `NetworkRegistry.PACKET_HANDLER` — `public static final SimpleChannel`, `playToClient/playToServer` public static, ID tự tăng không đụng độ | ✅ |
| Đọc đạn theo mục tiêu | Đọc `CustomData`/`GunData.DATA_CACHE`/`GunResource.RESOURCE_CACHE` (public static) — chỉ liệt kê & gợi ý trong enum `Ammo` có sẵn (HANDGUN/RIFLE/SHOTGUN/SNIPER/HEAVY), không thêm loại mới | ✅ |
| UI chọn mục tiêu v1 | Chưa map — dùng **raycast "look-and-mark"**: người chơi ngắm vào vị trí muốn bắn, bấm phím → lấy tọa độ world từ raycast, thêm vào target queue. UI danh sách target text-based (giống `FiringParametersScreen` nhưng multi-target) | 🔧 Thiết kế mới, không có sẵn trong SBW |
| Minimap thật | Hoãn sang Phase 8 (xem mục 4), tích hợp API mod minimap ngoài thay vì tự vẽ | ⚠️ Cần khảo sát API Xaero's Minimap/JourneyMap khi tới phase đó |

## 3. Giới hạn cần lưu ý khi làm việc với Claude

- **Claude không chơi được Minecraft trực tiếp** — không có client để bấm nút, ngắm bắn, xem HUD
  bằng mắt. Claude sẽ: viết code, chạy `gradle build`/compile check, viết logic có thể unit-test
  được (vd. hàm tính target queue, hàm chọn đạn). **Người dùng cần tự chạy `runClient`, test
  trong game, và báo lại kết quả/lỗi** để Claude sửa tiếp theo từng phase.
- Phụ thuộc CurseMaven là **jar cụ thể theo build ID** — khi SBW ra bản mới, method signature có
  thể đổi (mod đang phát triển tích cực). Cần khóa version rõ ràng trong `build.gradle` và chỉ
  nâng cấp khi có lý do, kiểm tra lại các class đã liệt kê ở mục 2.
- Ammo `enum` đóng cứng — không phải vấn đề vì đã chốt không cần đạn mới, nhưng nếu sau này đổi ý
  thì đây sẽ là điểm nghẽn cần patch/mixin.

## 4. Lộ trình triển khai theo Phase

Mỗi phase có: mục tiêu, việc Claude sẽ làm, cách verify, và ai làm gì.

**Phase 0 — Khung dự án**
- 🔧 Tạo Gradle project addon (copy cấu trúc từ `addon-example`), cấu hình CurseMaven dependency
  SBW, Forge 47.2.0/MC 1.20.1/Java 17, mod id + group riêng.
- Verify: `gradlew build` chạy sạch, `runClient` khởi động được (Claude chạy lệnh, không chơi).

**Phase 1 — Item cơ bản + Screen tọa độ thủ công (MVP)**
- 🔧 Đăng ký item Tablet, right-click mở Screen hiển thị danh sách target (text-based, chưa map),
  giống `FiringParametersScreen` nhưng hỗ trợ nhiều target.
- Verify: người dùng test trong game — item xuất hiện, mở được Screen.

**Phase 2 — Binding pháo**
- 🔧 Bind tablet với 1 hoặc nhiều pháo, tái dùng pattern NBT của `ArtilleryIndicatorItem`.
- Verify: người dùng test bind/unbind trong game.

**Phase 3 — Target queue + Fire modes**
- 🔧 SINGLE / RIPPLE / SALVO — logic gửi lệnh bắn tuần tự/đồng loạt tới các pháo đã bind.
- Verify: người dùng test từng mode, quan sát pháo bắn đúng thứ tự/thời điểm không.

**Phase 4 — Ammo selector**
- 🔧 Đọc ammo khả dụng của pháo đang bind, gợi ý loại đạn theo tag mục tiêu (entity/block).
- Verify: người dùng test chọn đạn, xác nhận đạn bắn ra đúng loại chọn.

**Phase 5 — Networking hoàn chỉnh**
- 🔧 Packet `FireCommand` client→server mang target list + ammo + fire mode, đăng ký trên
  `NetworkRegistry.PACKET_HANDLER`.
- Verify: test multiplayer/dedicated server nếu có, hoặc singleplayer (vẫn qua network layer).

**Phase 6 — HUD feedback**
- 🔧 Time-of-flight đếm ngược, marker điểm rơi dự kiến, cảnh báo danger-close (check khoảng cách
  tới đồng đội).
- Verify: người dùng quan sát HUD trong game.

**Phase 7 — Raycast "look-and-mark" nâng cao**
- 🔧 Tinh chỉnh UX chỉ định mục tiêu bằng raycast (phím tắt, feedback trực quan khi ngắm/mark).

**Phase 8 — Tích hợp minimap thật (hoãn tới khi các phase trên ổn định)**
- ⚠️ Khảo sát API Xaero's Minimap/JourneyMap để bắt sự kiện click trên map → world coord, thay
  thế/bổ sung raycast.

**Phase 9 — MRSI (gác lại, độ phức tạp cao)**
- ⚠️ Cần nhiều viên với thời gian bay khác nhau cùng rơi một lúc.
- **Kết luận sau khi có số liệu PLZ-05**: với sơ tốc **cố định**, MRSI gần như **bất khả thi** — xe
  này chỉ dùng được quỹ đạo căng ở hầu hết cự ly (cầu vồng đòi ≥4150 block), nên không đủ dư địa góc
  để tạo ra hai thời gian bay khác nhau tới cùng một điểm.
- 💡 **Ý tưởng của người dùng (2026-08-13): hệ thống liều phóng (charges).** Pháo binh thật không
  dùng cố định một liều — tuỳ cự ly/điều kiện mà nạp số liều khác nhau để đổi **sơ tốc đầu nòng**.
  Đây chính là cách giải đúng của bài toán, và nó **giải quyết luôn vấn đề Cầu Vồng vô dụng**: liều
  nhỏ → sơ tốc thấp → tầm tối đa ngắn lại → cầu vồng dùng được ở cự ly gần thay vì đòi 4150 block.
  Một tính năng gỡ được hai nút thắt.
- 🔍 **Câu hỏi khả thi phải kiểm tra ĐẦU TIÊN khi quay lại phase này**: sơ tốc có **ghi đè được theo
  từng phát bắn** không, hay cố định trong datapack (`Weapons.Main.Velocity`)? Cần cả hai chỗ:
  (a) lúc **tính phần tử bắn** — `TrajectoryCalculator` nhận `v` làm tham số nên chỗ này chắc chắn
  làm được; (b) lúc **phóng đạn thật** — `vehicleShoot`/`afterShoot` đọc `getProjectileVelocity()`
  từ `GunData`. Nếu (b) không ghi đè được thì đạn vẫn bay theo sơ tốc gốc và toàn bộ hệ thống sụp đổ.
  Có thể thử qua `modifyGunData` (cơ chế đã dùng ở Phase 4 để đổi loại đạn).

**Phase 10 — Polish & đóng gói**
- 🔧 Cloth Config (nếu cần bật/tắt tính năng), packaging, kiểm tra tương thích các version SBW.

## 5. Nhật ký tiến trình & lỗi

> Cập nhật entry mới sau mỗi phiên làm việc đáng kể — không sửa xóa entry cũ, chỉ thêm mới, để
> giữ lịch sử.

### 2026-08-13 — Khảo sát tính khả thi (hoàn tất)
- **Đã làm**: Clone shallow `SuperbWarfare` và `SuperbWarfareExtensionModExample` về máy, khảo sát
  source thật (không chỉ đọc README) để xác nhận: cách addon mẫu phụ thuộc SBW, visibility của
  `ArtilleryIndicatorItem`/`MortarEntity`/`TrajectoryCalculator`/`GunData`/`NetworkRegistry`, hệ
  ammo/datapack, và xác nhận **không có minimap sẵn** trong mod gốc.
- **Lỗi gặp phải**: `git clone` lần đầu lỗi `Filename too long` trên Windows (path advancement
  recipes quá dài) → khắc phục bằng `git -c core.longpaths=true clone`.
- **Quyết định chốt trong phiên này**: (1) không cần đạn mới, dùng ammo có sẵn; (2) minimap hoãn
  sang phase sau, dùng raycast look-and-mark cho v1; (3) Claude đảm nhận toàn bộ implementation
  thay vì bàn giao Gemini.
- **Trạng thái**: Chưa viết dòng code nào của addon. Sẵn sàng bắt đầu Phase 0.

### 2026-08-13 — Phase 0 hoàn tất: khung dự án dựng xong, build thành công
- **Đã làm**: Tạo project Gradle mới tại `FDC/ArtilleryTacticalTablet/`, copy cấu trúc từ
  `addon-example` (gradle wrapper, `.gitignore`, pattern `build.gradle`/`mods.toml`). Định danh
  mod: `mod_id=artillerytablet`, group `net.nazarick.artillerytablet`, tên hiển thị "Artillery
  Tactical Tablet". Pin SBW qua CurseMaven `curse.maven:superb-warfare-1218165:6529319` (build id
  giống hệt `addon-example`, đã ghi rõ trong `gradle.properties` để dễ tra khi cần bump version).
  Bỏ plugin Mixin (không cần theo kết luận khảo sát Phase khả thi — không có class private/internal
  nào chặn đường). Giữ nguyên dependency `curios`/`geckolib` vì SBW cần chúng để load được trong
  dev environment. Khởi tạo git repo cục bộ trong thư mục project (`git init`, đã stage các file
  scaffold, **chưa commit** — chờ yêu cầu rõ ràng để commit theo quy tắc làm việc).
- **Lỗi gặp phải**: Không có lỗi. `./gradlew build` chạy sạch ngay lần đầu (1m24s, `BUILD
  SUCCESSFUL`), tạo ra `build/libs/artillerytablet-0.1.0-SNAPSHOT.jar`. Xác nhận CurseMaven resolve
  đúng jar SBW đã pin, GeckoLib/Curios repo cũng resolve OK.
- **Trạng thái**: Phase 0 xong. Project compile được nhưng **chưa test `runClient` trong game** —
  cần người dùng tự chạy thử (Claude không chơi được Minecraft, xem mục 3). Sẵn sàng bắt đầu
  Phase 1 (Item cơ bản + Screen tọa độ thủ công) khi được yêu cầu.

### 2026-08-13 — Sửa lỗi crash `runClient`: thiếu Mixin Gradle plugin
- **Lỗi gặp phải**: `./gradlew runClient` crash ngay khi bootstrap với
  `org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException: Critical injection
  failure: @Inject annotation on handleKeybinds could not find any targets matching
  'Lnet/minecraft/client/Minecraft;m_91279_()V'` — lỗi từ `mixins.superbwarfare.json:MinecraftMixin`
  (mixin của chính SBW, không phải của addon).
- **Nguyên nhân**: Ở Phase 0 tôi đã bỏ plugin `org.spongepowered.mixin` khỏi `build.gradle` vì nghĩ
  addon không tự viết mixin nên không cần. Sai lầm: plugin này (dù không cấu hình mixin riêng) là
  thứ khiến ForgeGradle wire JVM property `mixin.env.remapRefMap`/`refMapRemappingFile` cho **toàn
  bộ** hệ Mixin lúc chạy — thiếu nó thì mixin của *bất kỳ mod nào* (kể cả SBW, một dependency) đều
  không resolve được target SRG-name trong dev environment. Đã xác nhận qua kiểm tra trực tiếp file
  mapping cache (`srg_to_parchment_2023.08.13-1.20.1.tsrg`): `m_91279_` đúng là ánh xạ tới
  `handleKeybinds ()V`, nên bản thân mapping data không sai — vấn đề thuần túy là thiếu cơ chế remap
  refmap lúc runtime.
- **Cách khắc phục**: Thêm lại `buildscript{}` (maven spongepowered) + `id
  'org.spongepowered.mixin' version '0.7.+'` vào `build.gradle`, y hệt `addon-example`, dù addon
  chưa có mixin riêng nào. Rerun `runClient` (giới hạn 90s vì không chơi được) — không còn lỗi FATAL,
  log cho thấy SBW + addon load xong, sound engine/texture atlas khởi tạo bình thường.
- **Bài học cho các phase sau**: Đừng bỏ bớt cấu hình boilerplate của `addon-example` chỉ vì "trông
  như không cần dùng" — một số plugin có side-effect toàn cục (JVM system property) không thấy rõ
  qua việc đọc riêng `build.gradle`. Giữ nguyên các plugin/dependency của bản mẫu trừ khi có lý do
  cụ thể để bỏ.
- **Trạng thái**: `runClient` giờ khởi động sạch. Phase 0 coi như xác nhận đầy đủ (build + boot đều
  qua). Người dùng đã tự xác nhận vào được client thành công (2026-08-13). **Phase 0 hoàn tất và
  verify đầy đủ end-to-end.** Sẵn sàng Phase 1.

### 2026-08-13 — Phase 1: Item + Screen tọa độ thủ công + target queue
- **Đã làm**: Tạo `ArtilleryTacticalTabletItem` (implement `ItemScreenProvider` của SBW — cơ chế
  này tự động mở Screen khi người chơi cầm item và bấm phím "Edit Mode" của SBW, mặc định phím
  **H**, hoàn toàn miễn phí, không cần tự viết KeyMapping/event handler). Tạo
  `ArtilleryTacticalTabletScreen` (MVP: nhập tay X/Y/Z, danh sách tối đa 8 target, nút Add/Remove,
  nút Done gửi 1 packet duy nhất khi đóng — theo đúng pattern `ArtilleryIndicatorScreen` của SBW:
  sửa cục bộ, submit 1 lần, tránh vấn đề stack tham chiếu bị stale giữa lúc sửa). Đăng ký packet
  `SetTargetsMessage` (client→server) trực tiếp trên `NetworkRegistry.PACKET_HANDLER` của SBW (không
  tự tạo channel riêng). Có icon placeholder 16x16 tự vẽ bằng script (chưa phải art thật, xem mục
  còn thiếu bên dưới). Có bản dịch cả `en_us.json` và `vi_vn.json`.
- **Lỗi gặp phải #1 — build không compile**: `com.atsuishio.superbwarfare.item.ItemScreenProvider`
  và `NetworkRegistry` kiểu SimpleChannel "cannot find symbol". **Nguyên nhân**: file CurseMaven đã
  pin từ Phase 0 (`6529319`, copy nguyên từ `addon-example`) là bản **SBW 0.8.0, phát hành 5/2025**
  — cũ hơn hơn 1 năm so với source tôi khảo sát ở git HEAD lúc research tính khả thi. Bản 0.8.0
  hoàn toàn chưa có `ItemScreenProvider`, `ArtilleryIndicatorItem`, hay `NetworkRegistry` kiểu mới —
  cấu trúc code khác hẳn (item phẳng không có subpackage `misc`, dùng `ClientPacketHandler` thay vì
  `NetworkRegistry`). Nói cách khác: **`addon-example` đã lỗi thời**, không thể tin tưởng mù quáng
  theo cấu hình của nó. **Cách khắc phục**: Tra CurseForge tìm bản 1.20.1 mới nhất
  (`superbwarfare-0.8.9-final-mc1.20.1-6effe4385-all.jar`, file id **8104849**, phát hành 5/2026),
  cập nhật `sbw_curse_file` trong `gradle.properties`. Tiện thể sửa luôn lỗi nhỏ
  `Tag.TAG_COMPOUND.toInt()` (cú pháp Kotlin lẫn vào code Java — Java tự widen byte→int, không cần
  `.toInt()`).
- **Lỗi gặp phải #2 — crash lúc `runClient`**: "Mod File ... needs language provider
  kotlinforforge:4.11.0 or above. We have found none" + báo `superbwarfare not installed` (hệ quả
  dây chuyền từ lỗi trên, không phải lỗi thật thứ hai). **Nguyên nhân**: bản SBW 0.8.9 mới yêu cầu
  mod loader "Kotlin For Forge" ở runtime — bản 0.8.0 cũ không cần. **Cách khắc phục**: đọc thẳng
  `build.gradle.kts` của SBW (repo đã clone) để lấy đúng coordinate, thêm vào `build.gradle`:
  `implementation 'thedarkcolour:kotlinforforge:4.11.0'` + repo
  `https://thedarkcolour.github.io/KotlinForForge/` (không `fg.deobf()` vì đây là language
  provider, không phải mod thường).
- **Bài học quan trọng cho các phase sau**: Không được tin tưởng mù quáng cấu hình/thông tin đã
  nghiên cứu trước đó (kể cả nghiên cứu của chính mình) mà không xác minh lại bằng build/run thật —
  source code git HEAD và jar CurseForge đã pin **không đồng bộ theo thời gian**. Từ giờ, mỗi khi
  cần đối chiếu API SBW, phải kiểm tra class thực tế bên trong jar đang dùng (`unzip -l`), không chỉ
  đọc source git. `gradlew build` + `runClient` là bước xác minh bắt buộc trước khi coi bất kỳ phase
  nào là "xong", không suy luận suông.
- **Trạng thái**: `gradlew build` sạch, `gradlew runClient` không còn FATAL — addon chạy
  `commonSetup`, load cùng SBW 0.8.9 ổn. Icon chỉ là placeholder tự vẽ, chưa phải art thật.
- **Người dùng test tay (2026-08-13), cả 7 bước đạt**: item xuất hiện đúng trong creative tab; cầm
  item + bấm H mở được Screen; nhập X/Y/Z + Add Target thêm đúng dòng vào danh sách; Done đóng
  Screen; mở lại bằng H thấy target đã lưu (xác nhận packet `SetTargetsMessage` ghi NBT phía server
  đúng, không chỉ là state ảo trên client); xoá target bằng nút "x" + Done + mở lại xác nhận đã mất.
  **Phase 1 hoàn tất và verify đầy đủ end-to-end.**

### 2026-08-13 — Phase 2: Sở hữu pháo (ownership) + bind vào tablet
- **Phát hiện quan trọng trước khi code**: kiểm tra bytecode thật (`javap`) của bản SBW 0.8.9 đang
  dùng — `ArtilleryEntity.interact()` hardcode `instanceof ArtilleryIndicatorItem` ngay trong
  bytecode, **không phải interface mở** (`IVehicleInteract` mà git HEAD từng có đã biến mất khỏi
  build này). Kết luận: **không thể** hook "right-click vào pháo để bind" từ addon mà không dùng
  Mixin. Đã hỏi và thống nhất với người dùng: (1) không dùng radius-scan để liệt kê pháo (rủi ro lộ
  pháo địch trong PvP), (2) không dùng raycast thủ công (cảm giác gượng), mà xây **hệ sở hữu
  (ownership) riêng của addon** — SBW không có khái niệm "chủ sở hữu" thật (chỉ có
  `LastDriverUUID`, bị ghi đè liên tục).
- **Thiết kế đã triển khai**:
  - `ArtilleryOwnershipData` (SavedData riêng của addon, lưu trên Overworld, không đụng gì tới dữ
    liệu SBW) — map `entityUUID → ownerUUID`, "ai lái/mount trước thì sở hữu" (first-come-first-
    served, không tự động chuyển chủ).
  - `ArtilleryOwnershipHandler` — lắng nghe `EntityMountEvent` (event **vanilla/Forge công khai**,
    xác nhận qua javap rằng `VehicleEntity` override `addPassenger/removePassenger` chuẩn vanilla
    nên event này chắc chắn bắn ra) — hoàn toàn không cần Mixin, không đụng bytecode SBW.
  - Tablet có thêm màn hình phụ **"Pháo Của Tôi"** (`ArtilleryRosterScreen`, mở từ nút trong màn
    hình Target Queue) — gửi packet xin danh sách pháo sở hữu, server trả về (kèm toạ độ nếu chunk
    đang load), người chơi bấm Bind/Unbind từng khẩu vào tablet đang cầm.
  - `BoundArtillery` NBT list trên item (tối đa 4 khẩu), server luôn tự kiểm tra lại quyền sở hữu
    trước khi cho bind (không tin dữ liệu client gửi lên).
  - 3 packet mới: `RequestOwnedArtilleryMessage` (C2S), `OwnedArtilleryResponseMessage` (S2C),
    `BindArtilleryMessage` (C2S) — đều đăng ký trên channel có sẵn của SBW.
- **Lỗi gặp phải**: `rebuildWidgets()` trong `ArtilleryRosterScreen` bị trùng tên + yếu quyền truy
  cập so với method `protected rebuildWidgets()` đã có sẵn trong vanilla `Screen` — Java báo lỗi
  compile "attempting to assign weaker access privileges". Đổi tên thành `rebuildRows()`, hết lỗi.
  Bài học: khi đặt tên method trong class kế thừa từ vanilla, nên kiểm tra nhanh xem tên có trùng gì
  không (đặc biệt các tên chung chung như `rebuild`, `refresh`, `update`).
- **Trạng thái**: `gradlew build` sạch, `runClient` boot không FATAL.

### 2026-08-13 — Phase 2 sửa lại: bỏ ownership, chuyển sang bán kính + fix lỗi bind không lưu
- **Người dùng test tay, phát hiện lỗi thật**: danh sách hiện đúng tên pháo sau khi lái, bấm Bind
  đổi label thành Unbind, nhưng **thoát ra vào lại Roster thì label quay lại Bind** — bind không
  được lưu bền.
- **Nguyên nhân (xác định qua đọc lại code, không phải đoán)**: `ArtilleryTacticalTabletScreen`
  giữ field `stack` được chụp **một lần duy nhất** lúc mở Tablet Screen (khi bấm phím H). Nút "Pháo
  Của Tôi" dùng lại đúng field này để tạo `ArtilleryRosterScreen` mỗi lần mở — kể cả lần thứ hai
  sau khi đã bind. Khi server ghi NBT bind vào stack và đồng bộ về client, Minecraft **thay thế
  hẳn object ItemStack trong slot** (không sửa tại chỗ), nên field `stack` cũ đã capture từ trước
  trở thành tham chiếu lỗi thời — `ArtilleryRosterScreen` đọc lại `BoundArtillery` từ NBT của
  object cũ này nên luôn thấy rỗng. Đây là đúng loại lỗi mà comment trong code Phase 1 đã cảnh báo
  trước ("held ItemStack reference can go stale") nhưng lúc viết Phase 2 lại quên áp dụng cho luồng
  điều hướng Screen→Screen mới.
- **Cách khắc phục**: `openRoster()` trong `ArtilleryTacticalTabletScreen` giờ lấy lại stack **sống**
  từ `Minecraft.getInstance().player.getMainHandItem()/getOffhandItem()` ngay tại thời điểm bấm nút,
  thay vì dùng field đã lưu — đảm bảo Roster luôn đọc đúng trạng thái NBT mới nhất mỗi lần mở.
- **Đổi thiết kế theo yêu cầu người dùng**: bỏ hẳn hệ ownership (`ArtilleryOwnershipData`,
  `ArtilleryOwnershipHandler`, `EntityMountEvent` claim) — xoá file, không giữ lại code chết.
  Thay bằng liệt kê theo **bán kính 128 block quanh người chơi** (`RequestNearbyArtilleryMessage`,
  đổi tên toàn bộ `Owned*` → `Nearby*`), sắp xếp gần nhất trước, hiển thị khoảng cách từng dòng.
  `BindArtilleryMessage` đổi validation từ "kiểm tra chủ sở hữu" sang "kiểm tra lại đúng bán kính +
  cùng dimension" phía server (không tin dữ liệu client gửi lên, dù danh sách đã được server lọc
  sẵn — phòng trường hợp client bị sửa gửi UUID tuỳ ý).
- **Đánh đổi đã xác nhận với người dùng**: cách này sẽ hiện cả pháo của người chơi khác nếu ở gần
  (rủi ro PvP mà bản thân đã cảnh báo trước) — người dùng chủ động chấp nhận đánh đổi này để đổi lấy
  UX đơn giản, tự nhiên hơn.
- **Người dùng test tay lại (2026-08-13), cả 6 bước đạt**: danh sách hiện đúng pháo gần kèm khoảng
  cách; Bind đổi label đúng; **thoát Roster rồi mở lại giữ đúng trạng thái Unbind** (xác nhận lỗi
  stale-stack đã hết); đóng hẳn Screen (Esc) rồi mở lại từ đầu (phím H) vẫn giữ đúng state. **Phase
  2 hoàn tất và verify đầy đủ end-to-end.**

### 2026-08-13 — Phase 3: Fire modes SINGLE/SALVO/RIPPLE
- **Đã xác nhận qua bytecode thật** (không đoán từ git source): `ArtilleryEntity.setTarget(ItemStack,
  Entity, String)` đọc `FiringParametersItemKt.getFiringParameters(stack)` (extension property Kotlin,
  đọc/ghi NBT trên **bất kỳ** ItemStack nào, không nhất thiết là item Firing Parameters thật) — tự
  tính đạn đạo qua `TrajectoryCalculator.calculateLaunchVector(...)`, set `ShootVec`. Rồi
  `vehicleShoot(LivingEntity shooter, String weaponName)` bắn thật. `"Main"` là tên weapon-slot
  hardcode nội bộ của `ArtilleryEntity` (thấy trong `beforeShoot()`). → Chỉ cần tạo 1 ItemStack tạm
  (`new ItemStack(Items.PAPER)`), set `FiringParameters` bằng record Java
  `FiringParametersItem.Parameters(pos, radius, isDepressed)`, gọi `setTarget` rồi `vehicleShoot` —
  không cần người chơi thật sự đứng gần hay ở trong pháo (đúng tinh thần indirect fire).
- **Đã triển khai**:
  - `FireMode` enum (SINGLE/SALVO/RIPPLE), `FireScheduler` (bộ đếm ngược tick riêng của addon, dùng
    `TickEvent.ServerTickEvent`, không phụ thuộc task-queue nội bộ của SBW) để so le RIPPLE.
  - `FireCommandMessage` (C2S): SINGLE bắn khẩu đầu tiên trong danh sách bind; SALVO bắn tất cả cùng
    lúc; RIPPLE bắn từng khẩu cách nhau 10 tick (~0.5s).
  - Screen: mỗi dòng target có thêm nút "Fire", nút chuyển chế độ bắn (cycle SINGLE→SALVO→RIPPLE).
  - **Tự sync trước khi bắn**: `fireTarget()` gửi `SetTargetsMessage` NGAY TRƯỚC `FireCommandMessage`
    trong cùng 1 lần bấm — tránh lỗi index lệch nếu người chơi thêm target nhưng chưa bấm Done (server
    đọc danh sách target theo index của chính nó, không phải danh sách tạm phía client).
- **Trạng thái**: `gradlew build` sạch **ngay lần đầu** (không lỗi compile) — nhờ verify kỹ API qua
  javap trước khi viết thay vì đoán theo git source như các phase trước. `runClient` boot không FATAL.
  **Chưa test tay** — cần ít nhất 1 pháo đã bind (từ Phase 2) để thử Fire.

### 2026-08-13 — Phase 3 sửa lỗi: 4 vấn đề người dùng phát hiện khi test
- **Lỗi 1 & 2 (UI reset)**: tọa độ đang gõ dở và chế độ bắn đều reset khi đóng/mở lại GUI.
  **Nguyên nhân**: cả hai chỉ là biến tạm trong Screen, chưa hề được lưu. **Khắc phục**: thêm
  `TAG_FIRE_MODE`/`TAG_DEPRESSED`/`TAG_INPUT_X/Y/Z` vào NBT item + packet `SetTabletSettingsMessage`
  (C2S), gọi khi đổi mode, đổi quỹ đạo, mở Roster, bấm Done, và cả `onClose()`. Screen đọc lại state
  từ NBT lúc khởi tạo. `init()` cũng giữ lại nội dung đang gõ khi rebuild widget (trước đây mỗi lần
  Add/Remove target là mất chữ đang gõ).
- **Lỗi 3a — pháo không xoay về hướng mục tiêu (NGHIÊM TRỌNG)**. **Nguyên nhân thật** (tìm qua đọc
  bytecode `ArtilleryEntity.baseTick()`): `setTarget()` **chỉ lưu** vector ngắm vào `ShootVec` và
  bỏ `lockTurret`; nòng pháo sau đó mới **xoay dần từng tick** về hướng đó qua
  `turretAutoAimFromVector()` bên trong `baseTick()`. Code cũ gọi `vehicleShoot()` ngay lập tức
  trong cùng tick → đạn bay theo hướng nòng **cũ**, chưa xoay. Đối chiếu
  `ArtilleryIndicatorFireMessage` của SBW thì thấy nó cũng **không bắn thẳng** mà hoãn qua
  `Mod.queueServerWork((i % 5) + 1, ...)`. **Khắc phục**: tách làm 2 giai đoạn — `setTarget()` trước,
  chờ `AIM_SETTLE_TICKS = 30` tick cho nòng xoay xong, rồi mới `vehicleShoot()`.
- **Lỗi 3b — bắn liên tục như súng máy, không có thời gian nạp**. **Nguyên nhân**: giới hạn nhịp bắn
  (RPM) của SBW được áp **phía client** (timer của phím bắn trong `ClientEventHandler`), không nằm
  trong `vehicleShoot()`. Gọi `vehicleShoot()` thẳng từ nút GUI nên bỏ qua hoàn toàn mọi giới hạn.
  **Khắc phục**: tự áp cooldown phía server — map `LAST_SHOT_TICK` theo UUID từng khẩu, khoảng cách
  tối thiểu = `AIM_SETTLE_TICKS + 1200/vehicleWeaponRpm("Main")` (đọc RPM thật của chính khẩu pháo
  đó, không hardcode). Thêm kiểm tra `data.ammo.get() > 0` (giống đúng cách SBW kiểm tra) và
  `isWreck()`. Nếu tất cả pháo đều bận/hết đạn thì báo chat cho người chơi thay vì im lặng.
- **Lỗi 4 — thiếu Lofted/Depressed**: trước đây hardcode `isDepressed = false` nên pháo ngoài tầm
  bắn cầu vồng sẽ spam cảnh báo. **Khắc phục**: thêm nút chuyển quỹ đạo trong GUI (Cầu Vồng ↔ Căng),
  lưu vào NBT, truyền đúng vào `FiringParametersItem.Parameters(pos, radius, isDepressed)`.
- **Bài học**: gọi được API public của mod khác **không đồng nghĩa** với việc đã tái hiện đúng
  *luồng* của nó. Lần này phải đọc cả `baseTick()` (để hiểu ngắm là quá trình nhiều tick) lẫn
  packet gốc của SBW (để thấy nó cũng hoãn lệnh bắn) mới ra được nguyên nhân. Lần sau khi tích hợp
  hành vi có tính thời gian (animation, xoay, nạp đạn), phải kiểm tra xem trạng thái có cần nhiều
  tick để hoàn tất không, và giới hạn tốc độ nằm ở client hay server.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot không FATAL.
- **Người dùng test lại**: 5/6 mục đạt. Còn lại mục 3 — xem entry kế tiếp.

### 2026-08-13 — Phase 3 sửa lần 2: bỏ delay cứng, theo dõi góc nòng thật
- **Vấn đề còn lại người dùng phát hiện**: delay cố định 30 tick (1.5s) không đủ khi mục tiêu ở xa
  khiến nòng phải xoay quãng dài — pháo khai hoả **trước khi** nòng vào đúng vị trí. Người dùng nhận
  xét đúng bản chất: "delay cố định là ý tưởng không hay khi nòng đôi khi phải traverse lâu".
- **Khắc phục — `ArtilleryAimTracker`**: thay vì đoán thời gian, **đo góc thật mỗi tick**. So
  `getShootVec("Main", 1f)` (hướng nòng hiện tại — chính là vector mà `vehicleShoot` dùng để phóng
  đạn) với hướng đã lệnh trong `ShootVec` (do `setTarget` đặt), qua
  `VectorToolKt.angleTo(Vec3, Vec3)` — hàm public static trả về **độ** (đã kiểm tra bytecode: trả
  90.0 khi vuông góc). Bắn ngay khi lệch **≤ 1°**.
  - Lưu ý đã tránh được cái bẫy: `VehicleVecUtils.calculateAngle` **bỏ trục Y** (chỉ tính góc ngang,
    `multiply(1,0,1)`) nên không dùng được cho pháo — góc tà mới là thứ quan trọng nhất. Phải dùng
    `VectorToolKt.angleTo` (góc 3D đầy đủ).
  - **Phát hiện nòng kẹt**: nếu góc ngừng giảm (<0.05°/tick trong 8 tick liên tiếp) thì nòng đã xoay
    hết cỡ cơ khí — nếu vẫn lệch >12° thì báo "Pháo không thể xoay tới mục tiêu đó" và **không bắn**
    (thay vì bắn bừa cho lệch), đồng thời không treo chờ tới hết timeout.
  - Timeout an toàn 200 tick cho trường hợp bất thường.
- **Thêm cờ `BUSY`**: chặn việc bấm Fire dồn dập tạo nhiều lệnh chồng nhau lên cùng một khẩu khi nó
  đang xoay nòng. Cooldown RPM giờ tính từ thời điểm **bắn thật**, không phải lúc bấm nút (trước đây
  cộng nhầm cả `AIM_SETTLE_TICKS` vào cooldown, làm nhịp bắn chậm hơn RPM thật của pháo).
- **Bài học**: khi tích hợp với hành vi diễn ra qua nhiều tick, **đừng đoán thời lượng bằng hằng số**
  — tìm đại lượng có thể đo được (ở đây là góc giữa 2 vector) rồi theo dõi tới khi đạt điều kiện, kèm
  cơ chế phát hiện "không thể đạt được nữa" và timeout. Hằng số delay chỉ đúng cho đúng một tình
  huống thử nghiệm.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot không FATAL (`FATAL_COUNT=0`).
- **Người dùng test lại: cả 4 mục đạt** (mục tiêu xa/gần đều bắn đúng lúc nòng vào vị trí, ca nòng
  không xoay tới được báo đúng và không bắn bừa, bấm Fire dồn dập không dồn lệnh).

### 2026-08-13 — Phase 3 sửa lần 3: ngắm trước, chờ đạn sau
- **Yêu cầu người dùng**: khi pháo chưa có đạn nạp sẵn, hiện tại lệnh bắn bị từ chối hoàn toàn nên
  **nòng không hề xoay** — phải đợi nạp xong mới xoay được sang toạ độ mới. Mong muốn: nòng vẫn xoay
  vào phần tử ngay, chờ nạp xong thì tự bắn (đúng như tổ pháo thật vẫn lấy phần tử trong lúc nạp).
- **Khắc phục**: tách rời việc **ngắm** khỏi việc **có đạn**. Bỏ điều kiện `ammo > 0` khỏi
  `readyToFire()` (giờ chỉ còn từ chối khi pháo hỏng, không có `GunData`, đang bận lệnh khác, hoặc
  chưa hết cooldown RPM). Luồng mới: `setTarget` → theo dõi nòng tới khi vào hướng → nếu có đạn thì
  bắn ngay, nếu chưa thì báo "Đã ngắm xong - đang chờ nạp đạn" và **giữ nguyên hướng nòng**, theo dõi
  tới khi có đạn thì bắn. Nòng tự giữ hướng trong lúc chờ vì `setTarget` đã bỏ `lockTurret`, nên
  `baseTick` của SBW liên tục lái nòng về vector đã lệnh mỗi tick — không cần làm gì thêm.
- Timeout chờ đạn **1200 tick (60s)** rồi huỷ lệnh, báo "Huỷ lệnh bắn: không được tiếp đạn" — tránh
  treo lệnh vô hạn giữ cờ `BUSY` khiến khẩu pháo đó không nhận lệnh mới được nữa.
- Sửa lại nội dung `all_guns_busy` thành "Tất cả pháo đang thực hiện lệnh bắn khác" cho đúng nghĩa
  mới (hết đạn không còn là lý do từ chối).
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (không exception).
- **Người dùng test: cả 4 mục đạt.**

### 2026-08-13 — Phase 3 sửa lần 4: đổi mục tiêu tức thì (lệnh mới thay thế lệnh cũ)
- **Yêu cầu người dùng**: đang bắn mục tiêu A, nòng xoay được nửa đường thì đổi ý muốn chuyển sang
  mục tiêu A2 (ngược 180°) — pháo phải đổi theo **ngay lập tức**, không phải đợi lệnh cũ xong.
- **Nguyên nhân chặn**: cờ `BUSY` (thêm ở lần sửa 2 để chống bấm Fire dồn dập) từ chối luôn mọi lệnh
  mới khi pháo đang xoay — vô tình chặn cả trường hợp chính đáng là đổi phần tử bắn.
- **Khắc phục — cơ chế "thế hệ lệnh" (`ORDER_GEN`)**: mỗi khẩu pháo giữ một số đếm lệnh. Lệnh mới
  tăng số này lên; mọi callback đang chờ (đang xoay nòng / chờ đạn / chờ cooldown) đều kiểm tra
  `isCurrent(gun, generation)` trước khi làm gì, thấy mình lỗi thời thì **lặng lẽ dừng** (không báo
  lỗi, không bắn). Nhờ vậy nòng lập tức quay sang mục tiêu mới từ vị trí hiện tại.
- **Bỏ hẳn cờ `BUSY`** — chính nó là thứ gây ra hạn chế này. Việc chống bắn dồn dập giờ do
  `canShootNow()` đảm nhiệm (đủ đạn **và** đã hết cooldown RPM), gộp chung vào cùng một vòng chờ với
  việc chờ đạn. Kết quả: bấm Fire liên tục vẫn không tạo loạt liên thanh (mỗi lệnh chỉ *thay thế*
  lệnh cũ và vẫn phải đợi RPM), nhưng đổi phần tử bắn được bất cứ lúc nào.
- Gộp luôn điều kiện cooldown vào vòng chờ thay vì từ chối lệnh: giờ pháo còn trong thời gian nạp vẫn
  nhận lệnh, xoay nòng vào phần tử mới, chờ hết cooldown rồi tự bắn — nhất quán với hành vi "ngắm
  trước, chờ đạn sau" ở lần sửa 3.
- Đổi `all_guns_busy` → `all_guns_unavailable` ("Không có pháo khả dụng - tất cả đã hỏng hoặc không
  có vũ khí") vì giờ chỉ còn từ chối khi pháo thật sự không dùng được.
- **Bài học**: cơ chế khoá (lock/busy flag) thêm vào để chống thao tác lặp dễ vô tình chặn cả thao
  tác chính đáng của người dùng. Với hành vi kéo dài nhiều tick, "thế hệ lệnh + huỷ lệnh cũ" là mô
  hình đúng hơn "khoá không cho lệnh mới".
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL_COUNT=0`).
- **Người dùng test tay, cả 4 mục đạt**: đổi mục tiêu giữa lúc nòng đang xoay → nòng đổi hướng ngay
  và chỉ bắn 1 phát vào mục tiêu mới; đổi mục tiêu trong lúc chờ nạp đạn → bắn đúng mục tiêu mới;
  bấm Fire liên tục vẫn đúng nhịp RPM, không liên thanh; SALVO/RIPPLE nhiều khẩu vẫn đúng.
  **PHASE 3 HOÀN TẤT** — đã verify đầy đủ end-to-end sau 4 lần sửa.

### 2026-08-13 — Phase 4: Chọn loại đạn
- **Bối cảnh quan trọng người dùng cung cấp trước khi làm**: trong lúc test, các xe pháo đang gắn
  **Creative Ammo Box** (đạn vô hạn, mọi loại đạn đều khả dụng) — vật phẩm này **bị cấm và không tồn
  tại trong môi trường survival multiplayer**, chỉ dùng để test cho thuận tiện. Đây là cảnh báo rất
  giá trị: nếu không biết, rất dễ viết code chỉ đúng khi luôn có sẵn mọi loại đạn.
- **Đã xác nhận qua javap**: `GunProp.AMMO_CONSUMER` trả về `List<AmmoConsumer>` (các loại đạn khẩu
  pháo hỗ trợ, định nghĩa trong datapack), `GunData.selectedAmmoType` là chỉ số đang chọn,
  `GunData.changeAmmoConsumer(int, Entity)` để đổi. Đối chiếu `EditMessage` của SBW: với xe pháo thì
  gọi `changeAmmoConsumer(i, vehicle.getAmmoSupplier())` rồi `data.save()` — đã làm y hệt.
- **Ba điểm chủ động phòng sai do Creative Box**:
  1. Số đạn khả dụng đọc từ `AmmoConsumer.count(data, artillery.getAmmoSupplier())` — nguồn tiếp đạn
     thật gắn với xe, **không** đọc từ inventory người chơi. Với Creative Box con số rất lớn nên hiển
     thị "(vô hạn)" khi ≥10.000 thay vì in số vô nghĩa.
  2. Danh sách loại đạn lấy từ **định nghĩa datapack của khẩu pháo**, không phải từ những gì đang có
     trong hòm — nên trong survival vẫn liệt kê đúng các loại pháo hỗ trợ, chỉ khác ở số lượng.
  3. Khi chọn đạn, khớp theo **id đạn** (`AmmoConsumer.getAmmo()`) chứ không theo chỉ số danh sách —
     phòng khi bind nhiều khẩu khác loại có thứ tự đạn khác nhau. Khẩu nào không có loại đạn đó thì
     báo rõ "Chỉ x trên y khẩu pháo có loại đạn đó".
- **Đã triển khai**: `AmmoTool` (helper đọc/đổi đạn), 3 packet
  (`RequestAmmoOptionsMessage` C2S / `AmmoOptionsResponseMessage` S2C / `SelectAmmoMessage` C2S),
  màn hình `AmmoSelectScreen` mở từ nút "Đạn Dược" trong tablet — liệt kê loại đạn kèm số lượng, đánh
  dấu loại đang nạp, chọn thì áp cho **toàn bộ** pháo đã bind.
- **Chưa làm (khác kế hoạch gốc)**: phần "gợi ý loại đạn theo mục tiêu" (đọc entity/block tại toạ độ
  để đề xuất HE/xuyên giáp...) — cần thảo luận thêm với người dùng về tiêu chí gợi ý trước khi làm,
  tránh đoán mò. Phần chọn đạn thủ công đã đủ dùng.
- **Trạng thái**: `gradlew build` sạch **ngay lần đầu**, `runClient` boot sạch (`FATAL_COUNT=0`).
- **Người dùng test**: mục 1, 2 đạt (danh sách hiện đúng, bấm Chọn đổi được hiển thị) nhưng **mục 3
  thất bại** — thoát ra vào lại vẫn là đạn cũ, tức chưa lưu thật. Mục 4, 5 chưa test được vì phụ
  thuộc mục 3. Người dùng cũng chốt: **không cần tính năng gợi ý đạn theo mục tiêu**, chọn tay là
  tối ưu cho tốc độ làm việc → gạch khỏi phạm vi dự án.

### 2026-08-13 — Phase 4 sửa lỗi: đổi đạn không lưu (phải dùng modifyGunData)
- **Nguyên nhân**: `artillery.getGunData("Main")` của **xe pháo** trả về một **bản sao tách rời**.
  Gọi `changeAmmoConsumer(...)` + `data.save()` trên đó chạy không báo lỗi nhưng **không ghi ngược về
  entity**, nên mở lại thấy đạn cũ. Cách đúng với xe là bọc trong
  `artillery.modifyGunData("Main", gunData -> gunData.changeAmmoConsumer(index, artillery.getAmmoSupplier()))`
  — chính là hàm mà `vehicleShoot` cũng dùng để trừ đạn.
- **Điều đáng rút kinh nghiệm**: tôi *đã* đọc `EditMessage` của SBW từ đầu, nhưng chỉ nhìn nhánh xử
  lý **súng cầm tay** (nhánh này đúng là dùng `save()`), bỏ sót nhánh xử lý **xe** nằm ngay phía trên
  dùng `modifyGunData`. Đây là **lần thứ hai** trong dự án mắc đúng kiểu lỗi này (lần đầu: Phase 3,
  gọi đúng `vehicleShoot` nhưng sai luồng thời gian). Quy tắc cho các phase sau: khi mod gốc có
  **nhiều nhánh xử lý cho các ngữ cảnh khác nhau** (cầm tay vs gắn xe, client vs server), phải xác
  định rõ mình thuộc nhánh nào và đọc **đúng nhánh đó**, đừng lấy nhánh đầu tiên tìm thấy.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL_COUNT=0`).
- **Người dùng test lại, cả 3 mục còn lại đạt**: loại đạn đã chọn giữ nguyên sau khi thoát/vào lại
  (lưu thật vào entity); bắn ra đúng loại đạn đã chọn (2 loại có hiệu ứng khác nhau rõ rệt); chọn đạn
  áp đúng cho tất cả pháo đã bind. **PHASE 4 HOÀN TẤT** — verify đầy đủ end-to-end.

### 2026-08-13 — Phase 5 bỏ qua (đã làm sẵn từ các phase trước)
- Lộ trình gốc tách "Phase 5 — Networking hoàn chỉnh" thành một giai đoạn riêng, nhưng thực tế
  networking đã được làm đúng chuẩn ngay từ Phase 1 thay vì dồn lại: mọi packet đều đăng ký trên
  channel có sẵn của SBW, mọi thao tác quan trọng đều validate lại phía server (không tin client),
  và toàn bộ trạng thái nằm trong NBT/entity chứ không phải biến tạm phía client. Không còn việc gì
  để làm ở phase này → bỏ qua, đi thẳng Phase 6.

### 2026-08-13 — Phase 6: Khung HUD cố định khi cầm tablet
- **Người dùng chọn**: khung thông tin cố định trên màn hình (không phải chữ trên action bar).
- **Đã triển khai**: `TabletHudOverlay` đăng ký qua `RegisterGuiOverlaysEvent` (vẽ phía trên hotbar),
  hiện ở mép trái giữa màn hình, chỉ khi đang cầm tablet ở tay chính hoặc tay phụ, tự ẩn khi người
  chơi ẩn giao diện (F1). Nội dung: số pháo đã bind, chế độ bắn / quỹ đạo, danh sách mục tiêu (kèm
  cờ **[GẦN QUÂN TA]**), và mục "Lệnh Bắn" liệt kê trạng thái từng khẩu đang thực hiện lệnh.
- **Ba quyết định thiết kế đáng ghi lại**:
  1. **Phần lớn HUD không tốn packet nào** — pháo đã bind, chế độ bắn, quỹ đạo, danh sách mục tiêu
     đều đọc thẳng từ NBT của tablet đang cầm (server vốn đã đồng bộ ItemStack sẵn). Chỉ trạng thái
     lệnh bắn đang diễn ra mới cần đẩy từ server qua `FireMissionStatusMessage`.
  2. **Time-of-flight**: server tính lúc bắn (quãng ngang ÷ tốc độ ngang của đạn, lấy từ
     `getProjectileVelocity("Main")` trên `VehicleEntity`) rồi gửi **thời điểm chạm đất tuyệt đối**
     (`impactGameTime`), client tự đếm ngược theo `level.getGameTime()` của nó. Cách này miễn nhiễm
     với độ trễ mạng — nếu gửi "còn 4 giây" thì con số sẽ lệch dần theo lag. Lưu ý đã ghi trong code:
     ước lượng này **chỉ dùng cho HUD**, tuyệt đối không dùng cho việc ngắm bắn.
  3. **Danger close tính phía client** dựa trên danh sách người chơi mà client nhìn thấy, bán kính
     24 block → là **cảnh báo tham khảo**, không phải phán quyết chính xác tuyệt đối. Đánh đổi có ý
     thức: đổi độ chính xác tuyệt đối lấy việc không tốn packet nào cho một tính năng chỉ mang tính
     nhắc nhở.
- `FireMissionClientState` tự dọn các entry cũ (lệnh đã chạm đất/huỷ giữ lại 3 giây rồi biến mất,
  lệnh đang chờ mà mất tín hiệu cập nhật quá 90 giây cũng tự rơi) — tránh HUD đọng rác vĩnh viễn.
- **Trạng thái**: `gradlew build` sạch **ngay lần đầu**, `runClient` boot sạch (`FATAL_COUNT=0` —
  bước này cũng xác nhận việc đăng ký overlay không lỗi).
- **Người dùng test**: mục 1,2,3,4,5 đạt. Mục 6 chưa xác nhận được (người dùng chưa rõ cách tái hiện,
  có thể nhầm sang lỗi ở mục 4 — đã giải thích lại). Phát hiện **lỗi mới ở mục 4**, xem entry dưới.

### 2026-08-13 — Phase 6 sửa lỗi: mục tiêu quá gần vẫn khai hoả
- **Lỗi người dùng phát hiện**: chọn mục tiêu **quá gần**, hệ thống có cảnh báo danger close, nhưng
  pháo **vẫn khai hoả** — và nòng thì không hề quay về phía mục tiêu đã chọn. (Người dùng chủ động
  nói rõ việc nòng không quay là chấp nhận được, chỉ cần **không cho khai hoả** là đủ.)
- **Nguyên nhân**: khi mục tiêu quá gần, `TrajectoryCalculator.calculateLaunchVector` trả về `null`
  (không có nghiệm). Đọc lại bytecode `setTarget` thì thấy: trong nhánh `null` nó chỉ hiện cảnh báo
  "out_of_range" và **giữ nguyên `ShootVec` cũ**, không xoá. Hậu quả dây chuyền với code của tôi:
  `ArtilleryAimTracker` so nòng với `ShootVec` — mà `ShootVec` vẫn là phần tử **cũ** và nòng vốn đã ở
  đúng đó → tracker kết luận "đã ngắm xong" **ngay lập tức** và bắn theo phần tử cũ.
- **Khắc phục**: tự kiểm tra tính khả thi **trước khi** ra lệnh, bằng chính
  `TrajectoryCalculator.calculateLaunchVector(...)` (public static, cùng hàm mà `setTarget` gọi bên
  trong) — nên kết quả khớp đúng với hành vi thật của pháo. Không có nghiệm thì báo "Không có phần
  tử bắn - mục tiêu quá gần hoặc ngoài tầm", đặt trạng thái ABORTED, và **không bắn**.
- **Bài học (biến thể mới của bài học cũ)**: khi mod gốc xử lý lỗi bằng cách **im lặng giữ nguyên
  trạng thái cũ** thay vì xoá/báo lỗi rõ ràng, code gọi nó rất dễ hiểu nhầm "không có gì thay đổi"
  thành "mọi thứ đã sẵn sàng". Phải kiểm tra điều kiện tiên quyết **trước**, đừng suy ra thành công
  từ việc không thấy lỗi.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL_COUNT=0`).
- **Người dùng test lại**: **mục 6 đạt cả 2 trạng thái** ("Giữ hướng - đang nạp đạn" và "Đã huỷ lệnh
  bắn"). Nhưng **mục 4 vẫn lỗi** — pháo vẫn khai hoả khi mục tiêu quá gần.

### 2026-08-13 — Phase 6 sửa lần 2: chặn khai hoả khi pháo từ chối phần tử bắn
- **Vì sao bản sửa lần 1 chưa đủ**: tôi chỉ chặn **một** nhánh từ chối (`calculateLaunchVector` trả
  `null`). Đọc kỹ lại bytecode `setTarget` thì thấy nó có **nhiều nhánh từ chối** độc lập:
  (a) không có nghiệm đạn đạo → cảnh báo `out_of_range`;
  (b) giải được nghiệm nhưng **góc tà vượt giới hạn nòng** (`getTurretMaxPitch`/`MinPitch`) → cảnh
  báo `mortar.warn` / `ballistics.warn` / `ballistics.warn2`.
  Ca "mục tiêu quá gần" của người dùng rơi vào **nhánh (b)** — cần bắn quá dốc — nên bản sửa lần 1
  không bắt được. Cả hai nhánh đều **giữ nguyên `ShootVec` cũ**.
- **Khắc phục (đổi hẳn cách tiếp cận)**: thay vì đoán và tái hiện từng luật từ chối của SBW (dễ sót
  như vừa rồi, và sẽ hỏng nếu SBW thêm luật mới), giờ **kiểm chứng kết quả sau khi gọi**: tự giải
  phần tử bắn bằng `TrajectoryCalculator`, gọi `setTarget`, rồi so hướng ngắm thực tế
  (`getShootVec()`) với nghiệm vừa tính — lệch quá **5°** nghĩa là pháo đã từ chối và vẫn giữ hướng
  cũ → huỷ lệnh, không bắn. So theo **góc** chứ không so bằng nhau tuyệt đối vì `setTarget` giải trên
  một điểm ngắm lệch nhẹ của riêng nó; còn khi bị từ chối thì hướng cũ luôn khác xa nên hai ca không
  bao giờ lẫn. Cách này cũng vẫn đúng khi bắn lặp lại cùng một mục tiêu (hướng vốn đã đúng → khớp).
- **Bài học**: khi cần biết "lệnh gọi vào mod khác có thành công không", **kiểm chứng trạng thái sau
  khi gọi** đáng tin hơn nhiều so với việc tái hiện điều kiện tiên quyết của nó — vì điều kiện có thể
  có nhiều nhánh mình chưa thấy hết, và có thể thay đổi ở bản sau.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL_COUNT=0`).
- **Người dùng test lại**: mục tiêu quá gần đã **không bắn nữa** (đúng), nhưng phát sinh lỗi ngược
  lại **nghiêm trọng hơn**: mục tiêu **hợp lệ cũng bị chặn**, không bắn được.

### 2026-08-13 — Phase 6 sửa lần 3: dùng vector rỗng làm dấu thay vì tự giải lại nghiệm
- **Vì sao lần 2 hỏng**: tôi tự giải nghiệm bằng `TrajectoryCalculator` rồi so với hướng ngắm mà
  `setTarget` nhận. Nhưng nghiệm của tôi **không thể khớp** với nghiệm của `setTarget`, vì hàm đó:
  1. Giải trên **điểm ngắm lệch**, không phải tâm ô mục tiêu:
     `randomPos(tâm, radius) + (0, −1.0 − 0.0015 × khoảng_cách, 0)`
     (đã kiểm tra `randomPos` với radius = 0 thì trả về đúng tâm, nên chỉ còn phần lệch trục Y).
  2. **Đảo cờ `depressed`** trước khi giải: `setDepressed(!parameters.isDepressed())` rồi mới giải
     bằng `getDepressed()`.
  Sai lệch cộng lại vượt ngưỡng 5° ở nhiều ca hợp lệ → chặn nhầm.
- **Khắc phục (bỏ hẳn hướng tự giải lại)**: đặt `setShootVec(new Vector3f())` (vector rỗng) làm dấu
  **trước khi** gọi `setTarget`, rồi kiểm tra sau đó:
  - hướng ngắm vẫn rỗng → `setTarget` đã từ chối (bất kể vì lý do gì) → huỷ lệnh, không bắn;
  - hướng ngắm đã được ghi đè → chấp nhận, tiến hành như bình thường.
  Vector phóng thật không bao giờ bằng 0 nên dấu này không thể nhầm; cả 3 bước chạy trong **cùng một
  tick** nên không có gì kịp đọc giá trị rỗng tạm thời. Xoá luôn `solveShot`/`acceptedBearing` và
  ngưỡng 5° — không còn cần tới `TrajectoryCalculator` lẫn `VectorToolKt` trong file này nữa.
- **Bài học (quan trọng nhất Phase 6)**: khi cần biết lệnh gọi vào mod khác có thành công không,
  đừng **tái hiện điều kiện tiên quyết** của nó (nhiều nhánh, nhiều chi tiết ẩn như điểm ngắm lệch
  hay cờ bị đảo — rất dễ sót, và sẽ hỏng khi mod cập nhật). Hãy tìm cách **quan sát trạng thái sau
  khi gọi**, tốt nhất là đặt một giá trị dấu mà kết quả hợp lệ không bao giờ trùng.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL_COUNT=0`).
- **Người dùng test lại: cả 5 mục đạt** (mục tiêu hợp lệ bắn được, bắn lặp lại được, mục tiêu quá gần
  bị chặn đúng, Cầu Vồng/Căng đều chạy, SALVO/RIPPLE đúng).

### 2026-08-13 — Xác định thông số đạn đạo PLZ-05 (giải thích vì sao Cầu Vồng "vô dụng")
- Người dùng dùng xe **PLZ-05**. Tra `data/superbwarfare/sbw/vehicles/plz_05.json`:
  vận tốc đạn **18**, trọng lực **0.06**, `TurretPitchRange = [-3, 65]` (trần góc tà **65°**),
  `TurretTurnSpeed = [2, 1.5]` (2°/tick ngang).
- Quỹ đạo cầu vồng luôn cần góc **> 45°**, càng gần càng dốc. Tính ra: với PLZ-05, Cầu Vồng **chỉ hợp
  lệ từ ~4150 block trở ra** (tầm tối đa ~5400 block). Người dùng đã test 3 toạ độ ở 4300 / 4690 /
  4950 block → **cả 3 đều bắn thành công**, xác nhận tính toán đúng.
- **Kết luận**: Cầu Vồng gần như không dùng được trong thực tế với PLZ-05 — đây là hệ quả từ thông số
  của chính SBW (trần 65° quá thấp), **không phải lỗi addon**. Trong thực tế sẽ luôn dùng Căng.
- Tiện thể: `TurretTurnSpeed = 2°/tick` nghĩa là xoay 180° mất 90 tick (4.5 giây) — xác nhận việc bỏ
  delay cố định 30 tick ở bản sửa trước là bắt buộc (30 tick chỉ đủ xoay 60°).

### 2026-08-13 — Phase 6 sửa lần 4: 3 vấn đề người dùng phát hiện khi test tầm xa
- **Vấn đề A — phát đầu tiên vào toạ độ mới không tới đích, phát thứ hai trở đi mới trúng.**
  **Nguyên nhân**: `ArtilleryAimTracker` bắn **ngay khi góc lệch ≤ 1°**, tức lúc nòng **vẫn đang
  xoay**. `vehicleShoot` phóng đạn theo hướng nòng thực tế tại thời điểm đó (có nội suy giữa 2 tick),
  nên phát đầu lệch; từ phát thứ hai nòng đã đứng yên sẵn nên chuẩn. **Khắc phục**: bỏ hẳn điều kiện
  "đủ gần thì bắn", chỉ bắn khi nòng **dừng hẳn** (5 tick liên tiếp góc không đổi quá 0.05°). Việc
  này gộp luôn với cơ chế phát hiện nòng kẹt: nòng chạm giới hạn cơ khí cũng "dừng", chỉ khác là dừng
  khi còn lệch xa → nếu lệch > 12° thì huỷ lệnh.
- **Vấn đề B — toạ độ mới thêm bị mất nếu không bấm Fire.** **Nguyên nhân**: `onClose()` chỉ gửi
  `SetTabletSettingsMessage` (chế độ bắn/quỹ đạo/ô nhập), **không** gửi `SetTargetsMessage`; danh sách
  mục tiêu chỉ được lưu khi bấm Done hoặc Fire. **Khắc phục**: tách hàm `pushTargets()` và gọi ngay
  khi **thêm/xoá** mục tiêu, cộng thêm ở `onClose()` và khi mở màn hình phụ. Tốn thêm 1 packet mỗi
  lần sửa, nhưng đổi lại không bao giờ mất hàng đợi mục tiêu — đáng.
- **Vấn đề C — cảnh báo Danger Close ở toạ độ hợp lệ.** Đọc lại code không thấy sai; hỏi lại thì
  người dùng xác nhận **lúc đó đang đứng gần điểm rơi** để xem đạn nổ → cảnh báo chạy **đúng**. Đã
  làm rõ với người dùng: Danger Close = có người chơi trong 24 block quanh **điểm rơi** (cảnh báo bắn
  vào quân mình), hoàn toàn khác với "Không có phần tử bắn" = pháo không bắn tới được. Hai thứ độc
  lập, có thể hiện cùng lúc nên dễ nhầm.
- **Tính năng mới theo yêu cầu — đánh dấu đỏ toạ độ không bắn được**: thêm `ReachabilityCheck` +
  cặp packet `RequestTargetReachabilityMessage`/`TargetReachabilityMessage`. Server tính bằng **đúng
  công thức `setTarget` dùng** (điểm ngắm lệch `-1.0 - 0.0015×d`, cờ `depressed` bị đảo, so góc tà với
  `getTurretMinPitch/MaxPitch`), client tô đỏ kèm chữ `[NGOÀI TẦM BẮN]`.
  - Client chỉ hỏi lại khi có thay đổi thật (mục tiêu / quỹ đạo / pháo đã bind) nhờ một **mã chữ ký**,
    không hỏi định kỳ.
  - **Nguyên tắc tự đặt ra**: vì đây là bản sao logic nội bộ SBW nên **chỉ dùng để tô màu cảnh báo,
    tuyệt đối không dùng để quyết định bắn**. Quyết định bắn vẫn là hỏi thẳng khẩu pháo (cơ chế vector
    rỗng). Nếu SBW đổi luật, tệ nhất là dấu hiệu HUD sai, pháo vẫn không bắn bậy.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL_COUNT=0`).
- **Người dùng test: cả 9 mục đạt** (phát đầu trúng đích ngay, tọa độ lưu bền khi Esc/mở màn hình
  phụ, đánh dấu đỏ đổi đúng theo quỹ đạo, chưa bind pháo thì không báo bừa).

### 2026-08-13 — Phase 6 tinh chỉnh: cảnh báo có lý do cụ thể thay vì "không tới được"
- **Yêu cầu 1**: đổi chữ "OUT OF REACH" thành cảnh báo về **tầm bắn tối thiểu** cho tự nhiên hơn.
- **Yêu cầu 2**: có mục tiêu chế độ Căng không tới nhưng Cầu Vồng tới được — hệ thống không phân
  biệt, vẫn cho bắn ở chế độ Căng dù đạn không tới nơi. Đề nghị cảnh báo nên chọn quỹ đạo nào.
- **Đã làm**: đổi `ReachabilityCheck` từ trả về đúng/sai sang trả về **lý do** (`TargetStatus`):
  - `MIN_RANGE` — có nghiệm đạn đạo nhưng nòng không ngóc đủ dốc (ca "quá gần" kinh điển) →
    `[DƯỚI TẦM BẮN TỐI THIỂU]`
  - `MAX_RANGE` — không có nghiệm ở bất kỳ góc nào → `[VƯỢT TẦM BẮN TỐI ĐA]`
  - `USE_OTHER_ARC` — quỹ đạo đang chọn không tới nhưng quỹ đạo kia tới được →
    `[HÃY CHUYỂN SANG CẦU VỒNG]` / `[HÃY CHUYỂN SANG CĂNG]` (nêu đích danh quỹ đạo cần đổi, vì chỉ
    nói "sai quỹ đạo" thì người chơi vẫn phải tự đoán)
  Server tính trạng thái tốt nhất trong cả dàn pháo (khẩu nào bắn được thì coi là bắn được), packet
  đổi từ mảng boolean sang mảng byte ordinal.
- **Giới hạn đã nói rõ với người dùng (chưa giải quyết)**: phép kiểm tra này trả lời *"nòng có ngóc
  tới góc cần thiết không"*, **không mô phỏng địa hình trên đường đạn bay** cũng như lực cản không
  khí. Đạn bắn căng ở cự ly xa bay là là mặt đất nên rất dễ đâm vào đồi giữa đường — cả SBW lẫn addon
  đều tính là "bắn tới được" (đúng về đạn đạo), chỉ là viên đạn bị chặn. Đây **có thể** mới là nguyên
  nhân thật của yêu cầu 2. Đã **hỏi người dùng toạ độ cụ thể** để xác định là do địa hình chặn hay do
  phép tính sai — chưa kết luận, tránh đoán mò. Nếu đúng do địa hình thì muốn cảnh báo được phải mô
  phỏng đường bay và kiểm tra va chạm (làm được nhưng tốn kém, và không chính xác với chunk chưa tải).
- **Yêu cầu 3** (toạ độ tôi đưa vô tình khiến xe pháo này bắn vào xe kia): người dùng nói rõ **không
  coi là lỗi**, chỉ thấy buồn cười — tôi không biết bố trí xe trong game. Không hành động gì.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (0 lỗi). Chờ test tay.

### 2026-08-13 — Sửa lỗi UX: không gỡ bind được pháo ở xa + rút gọn tên xe
- **Lỗi người dùng phát hiện**: muốn gỡ bind 2 xe cũ để bind 2 xe mới, nhưng danh sách **chỉ hiện xe
  ở gần** (bán kính 128 block) nên 2 xe cũ ở xa **không xuất hiện** → buộc phải bay về tận nơi mới gỡ
  được. Đây là lỗi thiết kế của tôi từ Phase 2: coi danh sách chỉ là "pháo gần đây" mà quên rằng nó
  cũng là nơi duy nhất để **quản lý** các ràng buộc hiện có.
- **Khắc phục**: danh sách giờ = **toàn bộ pháo đã bind (ở bất kỳ đâu)** + pháo chưa bind trong tầm.
  - Pháo đã bind mà **chunk chưa tải** vẫn hiện, lấy loại xe từ dữ liệu đã lưu trong tablet
    (`BoundArtillery.typeId`), ghi "(chunk chưa tải)" thay cho toạ độ — quan trọng là nút **Gỡ Bỏ vẫn
    hoạt động**. May mắn là `BindArtilleryMessage` vốn **chỉ giới hạn tầm khi bind, không giới hạn khi
    gỡ**, nên không phải sửa gì phía server.
  - Dòng của pháo đã bind tô **xanh**; nút "Ràng Buộc" bị **làm mờ** khi pháo ngoài tầm 128 block —
    trước đây bấm vào thì server lặng lẽ từ chối mà người chơi không hiểu vì sao.
- **Rút gọn tên xe** (`ArtilleryLabel`): tên dịch đầy đủ quá dài, chiếm hết dòng nên không còn chỗ
  hiển thị thông tin khác. Giờ lấy id đăng ký (`plz_05` → **`PLZ05`**), nhờ đó mỗi dòng hiện được cả
  **toạ độ + khoảng cách**. Hàm rút gọn nhận cả id đăng ký lẫn translation key (vì pháo đã bind lưu
  theo description id, còn pháo đang tải thì đọc thẳng từ entity type).
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL_COUNT=0`).

### 2026-08-13 — Điều tra: đạn không tới đích ở cự ly cực xa (KHÔNG phải lỗi addon)
- **Hiện tượng người dùng báo**, ở cự ly 4500 block, thế giới **phẳng** (đã loại trừ địa hình chặn):
  - Cầu Vồng: phát 1 hỏng, các phát sau đều tới.
  - Căng: phát 1 hỏng, phát 2 tới, các phát sau lại hỏng.
- **Manh mối quyết định: kết quả KHÔNG nhất quán.** Lỗi tính toán thì phải sai giống nhau mọi lần.
  Sai lúc được lúc không ⇒ nguyên nhân môi trường, không phải công thức.
- **Đọc bytecode viên đạn**, tìm được 2 cơ chế:
  1. `ProjectileLife = 800` tick (40s) — hết hạn thì đạn tự nổ tại chỗ rồi biến mất. Tính ra ở 4500
     block: Căng bay ~284 tick, Cầu Vồng ~529 tick, **đều dưới 800** ⇒ **loại trừ**.
  2. `FastThrowableProjectile.tick()` gọi `keepChunkLoaded(vị_trí + hướng × 16)` — đạn tự giữ chunk
     phía trước **nhưng chỉ 16 block**, trong khi tốc độ đạn PLZ-05 là **18 block/tick**. **Đạn bay
     nhanh hơn tầm nó tự dọn đường** ⇒ có tick nó rơi vào chunk chưa tải và biến mất.
- **Cơ chế (2) giải thích được toàn bộ chuỗi quan sát**, kể cả đoạn tưởng như vô lý: phát 1 hỏng vì
  đường bay qua vùng chunk "lạnh"; phát 2 tới vì viên đầu đã tải sẵn một hành lang chunk; phát 3 trở
  đi lại hỏng vì hành lang đó đã unload. Việc người dùng dịch chuyển giữa lúc đạn đang bay càng làm
  chunk tải/hủy thất thường.
- **Thí nghiệm tách biến (do tôi đề xuất, người dùng đã chạy)**: bắn ở cự ly ~200 block — nơi toàn bộ
  đường bay nằm trong chunk đã tải — đứng yên tại chỗ pháo, không dịch chuyển. **Kết quả: mọi phát
  đều trúng, kể cả phát đầu tiên.**
- **Kết luận**: phần ngắm bắn của addon **đúng hoàn toàn** (bản sửa "chỉ bắn khi nòng dừng hẳn" đã
  giải quyết triệt để lỗi phát-đầu-trượt). Hiện tượng ở cự ly cực xa là **giới hạn của chính SBW**,
  addon không sửa được sạch sẽ — muốn ép thì phải tự force-load hành lang chunk dài hàng nghìn block,
  vừa nặng vừa nguy hiểm cho server. **Không hành động thêm.**
- **Bài học về phương pháp**: khi lỗi biểu hiện **không nhất quán**, đừng đi sửa công thức — hãy
  **tách biến** trước. Mọi quan sát trước đó đều ở cự ly cực xa, nơi hai nguyên nhân (ngắm sai và
  chunk) trộn lẫn; chỉ cần một phép thử ở cự ly ngắn là phân tách được ngay.

### 2026-08-13 — Phase 7: Chỉ định mục tiêu bằng cách ngắm (look-and-mark)
- **Đã làm**: chuột phải khi cầm tablet → ghi ô đang ngắm làm mục tiêu, tầm quét 512 block, báo xác
  nhận trên action bar kèm tiếng "ping". Tôn trọng giới hạn 8 mục tiêu, báo rõ khi đầy hoặc khi
  không ngắm trúng gì.
- **Hai quyết định đáng ghi lại**:
  1. **Raycast chạy phía server**, không phải client. Nếu quét ở client thì tầm chỉ điểm bị giới hạn
     bởi render distance — block ngoài tầm nhìn đơn giản là không tồn tại để mà bắn trúng. Server có
     toàn bộ thế giới đã tải.
  2. **Không cần packet nào cả** — `Item.use()` vốn đã chạy ở **cả hai phía**, nên phía client chỉ
     việc không làm gì, còn phía server ghi thẳng vào NBT. Client tự nhận NBT mới qua cơ chế đồng bộ
     ItemStack sẵn có. Đây là phase đơn giản nhất từ trước tới nay, ngược hẳn với dự đoán ban đầu của
     người dùng là "cũng rất khó".
- **Trạng thái**: `gradlew build` sạch **ngay lần đầu**, `runClient` boot sạch (`FATAL_COUNT=0`).
  Chờ test tay.

### 2026-08-13 — Phase 8 bước 1: Marker mục tiêu/pháo trên JourneyMap
- **Chọn JourneyMap thay vì Xaero's** — quyết định dựa trên khảo sát API thật (clone repo
  `TeamJM/journeymap-api`, nhánh `1.20.x_1.9-forge`), không phải cảm tính:
  | Thứ cần | JourneyMap | Xaero's |
  |---|---|---|
  | Click bản đồ → toạ độ world | `FullscreenMapEvent.ClickEvent.getLocation()` → `BlockPos`, và cả `IOverlayListener.onMouseClick(...)` | không có API công khai |
  | Menu chuột phải trên bản đồ | `ModPopupMenu.addMenuItem(label, Action)` | không |
  | Marker / vùng tô | `MarkerOverlay`, `PolygonOverlay`, `ImageOverlay` | chỉ waypoint |
  Điểm quyết định: ý tưởng gốc *"chọn mục tiêu trên bản đồ"* **chỉ JourneyMap làm được**.
- **Phiên bản đã chốt**: JourneyMap **5.10.3** (CurseForge project 32274, file **5789363**) + API
  **1.9** (`info.journeymap:journeymap-api:1.20-1.9-SNAPSHOT`, repo `https://jm.gserv.me/...`).
  Cố ý **không** dùng JourneyMap 6.x mới hơn vì nó chuyển sang API 2.0.0 với annotation đăng ký
  plugin khác hẳn (`JourneyMapPlugin` thay vì `ClientPlugin`) — không trộn lẫn được.
- **Kiến trúc giữ phụ thuộc mềm**: `compileOnly` cho API (bản triển khai nằm trong chính JourneyMap).
  Lớp `ArtilleryTabletJourneymapPlugin` **không được addon tham chiếu ở bất kỳ đâu** — JourneyMap tự
  quét annotation `@ClientPlugin` và khởi tạo nó. Nhờ vậy khi không cài JourneyMap thì class này đơn
  giản là không bao giờ được nạp, addon chạy bình thường (mất phần bản đồ, còn nhập toạ độ tay và
  ngắm-để-gán vẫn nguyên). `JourneyMapSupport` cố ý **không import gì của JourneyMap** để làm lớp
  đệm an toàn.
- **Đã làm**: marker vòng tròn ngắm (đỏ, đánh số 1-8) cho mục tiêu, marker mũi nhọn (xanh) cho pháo
  đã bind; icon tự vẽ bằng script, để trắng để JourneyMap tô màu đè. Chỉ vẽ lại khi **chữ ký** của
  kế hoạch bắn đổi, tránh dựng lại overlay mỗi tick (vừa phí vừa gây nhấp nháy).
- **Hạn chế đã biết (đã báo trước cho người dùng)**: tablet chỉ lưu **UUID** pháo, mà client **không
  tra được vị trí entity từ UUID** — phải hỏi server. Hiện lấy toạ độ từ bản tin trả về của màn hình
  roster, nên **marker pháo chỉ hiện sau khi mở "Pháo Gần Đây" ít nhất một lần**, và **nếu xe di
  chuyển thì marker đứng ở vị trí cũ** cho tới lần mở roster kế tiếp. Với pháo tự hành đây là hạn chế
  thật; nếu test thấy khó chịu thì bổ sung tự làm mới theo chu kỳ.
- **Trạng thái**: `gradlew build` sạch **ngay lần đầu**, `runClient` boot sạch (`FATAL=0`). Log xác
  nhận `[journeymap] Found @ClientPlugin: ...ArtilleryTabletJourneymapPlugin` → `JourneyMap
  integration active`. Chờ test tay.

### 2026-08-13 — Phase 8 bước 2: Chọn mục tiêu bằng cách click lên bản đồ
- **Khảo sát trước**: `ModPopupMenu` (menu chuột phải) **chỉ dùng được khi click trúng overlay của
  chính mình**, và `FullscreenDisplayEvent` chỉ cho thêm nút toolbar chứ không có menu cho vị trí bất
  kỳ. Nhưng `ClientEvent.Type.MAP_CLICKED` được khai báo `cancellable = true` và mang theo
  `FullscreenMapEvent.ClickEvent` có `getLocation() → BlockPos` + `getButton()` → đủ để tự xử lý.
- **Thiết kế tương tác**:
  - **Chuột phải lên bản đồ khi đang cầm tablet** → gán mục tiêu tại đó. Việc *cầm tablet* chính là
    thứ đưa bản đồ vào "chế độ chỉ huy hoả lực"; khi không cầm thì JourneyMap giữ nguyên hành vi gốc,
    addon không chiếm quyền của nó.
  - **Chuột phải lại lên ô đã gán** → xoá mục tiêu đó. Một nút làm cả hai chiều, không phải thêm phím
    hay menu để giải thích.
  - Huỷ sự kiện sau khi xử lý để JourneyMap không mở menu riêng đè lên.
- **Hai vấn đề kỹ thuật và cách giải**:
  1. **Bản đồ không có độ cao** — click chỉ cho X/Z có nghĩa. Lấy **độ cao của chính người chơi** làm
     Y. Hợp với thế giới phẳng, và dù sao pháo cũng tự giải lại phần tử bắn từ vị trí thật.
  2. Vì (1), việc **xoá mục tiêu so khớp theo cột X/Z**, không đòi click trúng đúng Y đã lưu — nếu
     không thì gần như không bao giờ xoá được.
- **Khác Phase 7, lần này bắt buộc phải có packet** (`MarkTargetMessage`): Phase 7 bám vào
  `Item.use()` vốn đã chạy sẵn ở server, còn click bên trong màn hình JourneyMap là sự kiện **thuần
  client**, server không hề hay biết.
- **Trạng thái**: `gradlew build` sạch **ngay lần đầu**, `runClient` boot sạch (`FATAL=0`), log xác
  nhận plugin nạp đúng.
- **Người dùng test**: 6/7 mục đạt. **Mục 3 thất bại** — click lại vào marker để xoá gần như không
  bao giờ trúng, chủ yếu lại đè thêm mục tiêu mới lên trên mục tiêu cũ.

### 2026-08-13 — Phase 8 sửa lỗi: dung sai click tính theo điểm ảnh, không theo block
- **Nguyên nhân**: tôi so khớp **chính xác từng ô X/Z**. Ở mức thu nhỏ của bản đồ, **một điểm ảnh phủ
  hàng chục block**, nên xác suất click trúng đúng ô đã lưu gần như bằng không → luôn rơi vào nhánh
  "thêm mới" thay vì "xoá".
- **Khắc phục**: đọc `UIState.blockSize` (số điểm ảnh trên mỗi block ở mức phóng hiện tại) qua
  `IClientAPI.getUIState(Context.UI.Fullscreen)`, rồi quy đổi **10 điểm ảnh** ra số block. Nhờ vậy
  vùng bấm trúng luôn rộng như nhau **trên màn hình** ở mọi mức zoom: thu nhỏ thì dung sai tự nới ra
  hàng chục block, phóng to thì thu hẹp lại để vẫn đặt được hai mục tiêu sát nhau.
- **Vì sao không dùng số block cố định**: sẽ hỏng ở một trong hai đầu — nhỏ quá thì thu nhỏ bản đồ
  không bấm trúng (đúng lỗi vừa gặp), lớn quá thì phóng to lại không đặt nổi hai mục tiêu gần nhau.
  Có giá trị dự phòng 8 block cho trường hợp không đọc được `UIState`.
- Hai chi tiết kèm theo: khi có nhiều mục tiêu trong vùng dung sai thì chọn **cái gần con trỏ nhất**
  (không phải cái đầu tiên tìm thấy); và client gửi lên **toạ độ của mục tiêu đã lưu** chứ không phải
  toạ độ vừa click, nên phía server vẫn xoá bằng so khớp chính xác, không cần nới lỏng.
- **Bài học**: với thao tác chuột trên giao diện có thể phóng to/thu nhỏ, **dung sai phải tính theo
  đơn vị màn hình rồi quy đổi**, không bao giờ tính theo đơn vị thế giới.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`).
- **Người dùng test lại, cả 5 mục đạt**: xoá marker dễ dàng ở mọi mức zoom; thu nhỏ hết cỡ vẫn bấm
  trúng; phóng to hết cỡ vẫn đặt được hai mục tiêu cách nhau vài block; click giữa hai mục tiêu thì
  xoá đúng cái gần con trỏ hơn. **PHASE 8 HOÀN TẤT cả hai bước.**

### 2026-08-13 — ĐỔI HƯỚNG LỚN: gộp tất cả vào một giao diện tablet
- **Người dùng làm rõ tầm nhìn thật**: những gì làm tới giờ mới là **linh kiện moi ra được từ SBW và
  JourneyMap**, chưa phải sản phẩm. Thứ họ muốn là **một chiếc máy tính bảng thật sự**: bấm H mở ra
  một giao diện duy nhất chứa các tab thông tin, **bản đồ ở chính giữa**, và các nút chức năng xung
  quanh — chứ không phải HUD dán màn hình + bản đồ JourneyMap riêng + mấy màn hình con rời rạc.
- **Phát hiện kỹ thuật quyết định**: `IClientAPI.requestMapTile(..., Consumer<NativeImage> callback)`
  — JourneyMap **trả về ảnh bản đồ dạng `NativeImage`** cho mod khác tự vẽ. Nghĩa là **nhúng được bản
  đồ thật vào giao diện của mình**, JourneyMap lùi về vai trò "nguồn cấp ảnh bản đồ" chạy ngầm.
- **Ba quyết định của người dùng**:
  1. HUD **giữ nhưng thu gọn** — khi đóng tablet chỉ hiện lệnh bắn đang chạy + đếm ngược đạn rơi, vì
     lúc bắn xong phải đóng tablet để nhìn chiến trường.
  2. JourneyMap thành **phụ thuộc bắt buộc** → bỏ được toàn bộ lớp cách ly phòng khi thiếu nó.
  3. Đủ **5 tab**: Mục Tiêu, Pháo, Đạn, Trạng Thái, Nhật Ký Bắn.
- **Chia 3 bước**: (1) khung tablet + chuyển chức năng cũ vào tab; (2) nhúng bản đồ thật + click chọn
  mục tiêu ngay trong tablet; (3) tab Trạng thái/Nhật ký + thu gọn HUD.

### 2026-08-13 — Bước 1: Khung tablet gộp
- **Đã làm**: `TabletScreen` bố cục 3 cột (thanh tab / bản đồ / nội dung tab) + dải hàng đợi mục tiêu
  chạy ngang dưới cùng, mỗi dòng có nút **Khai hoả** và **xoá** riêng. Toàn bộ chức năng cũ đã chuyển
  vào tab; **xoá hẳn** 3 màn hình rời cũ (`ArtilleryTacticalTabletScreen`, `ArtilleryRosterScreen`,
  `AmmoSelectScreen`) thay vì để lại code chết.
- **Thay đổi kiến trúc đáng ghi**: trước đây mỗi bản tin trả lời từ server tìm màn hình đang mở bằng
  cách ép kiểu `Minecraft.getInstance().screen`. Với giao diện gộp nhiều tab, cách đó **sẽ hỏng** —
  trả lời về sau khi người chơi đã chuyển tab thì bị vứt đi. Giờ mọi trả lời đổ vào
  `TabletClientData`, các tab tự đọc trạng thái hiện có.
- Thêm `FireLog` (bộ đệm vòng 32 mục, **chỉ trong phiên**): cố ý **không** lưu vào NBT vì nhật ký
  phình to sẽ bị đồng bộ lại cho client mỗi lần đổi, cái giá đó lớn hơn nhiều so với việc mất nhật ký
  khi thoát game.
- Chỗ bản đồ hiện để **ô trống ghi rõ "làm ở bước 2"** thay vì giả vờ là bản đồ.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`).
- **Người dùng test**: 7/9 mục đạt. Lỗi ở tab **Pháo** (không thấy nút bind/gỡ và nút Làm Mới) và tab
  **Đạn** (tên đạn tràn ra ngoài khung, không thấy nút đổi đạn).

### 2026-08-13 — Bước 1 sửa lỗi: nút không hiện & chữ tràn khung
- **Nguyên nhân lỗi nút (cả hai tab cùng gốc)** — không phải bố cục mà là **thứ tự thời gian**: nút
  chỉ dựng được khi đã có danh sách, nhưng lúc mở tab thì yêu cầu vừa gửi đi, dữ liệu chưa về. Phần
  **vẽ** đọc dữ liệu ngay lúc vẽ nên hiện thông tin bình thường, còn **nút** thì không ai dựng lại
  sau khi trả lời tới.
  - Đây là loại lỗi mà giao diện **gộp** dễ mắc hơn giao diện rời: trước kia mỗi màn hình con tự gọi
    `rebuild` khi nhận đúng dữ liệu của riêng nó.
  - **Khắc phục**: `TabletClientData` đếm **phiên bản** mỗi lần dữ liệu đổi; `TabletScreen.tick()`
    theo dõi số này và dựng lại giao diện khi có trả lời mới.
- **Tên đạn tràn khung**: cắt theo **bề rộng thật** còn lại bên cạnh nút (`font.plainSubstrByWidth`),
  thêm `...` để người chơi biết tên đã bị cắt chứ không tưởng đó là tên đầy đủ. Nút cũng lùi vào 8
  điểm ảnh cho khỏi sát mép.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`).
- **Người dùng test lại**: nút đã hiện đủ, nhưng **phản hồi rất kém** — bấm bind/gỡ hoặc chọn đạn thì
  giao diện không đổi ngay, phải chuyển tab hoặc tắt/mở tablet mới thấy đúng.

### 2026-08-13 — Bước 1 sửa lỗi: hiển thị tức thời (optimistic UI)
- **Nguyên nhân**: giao diện hiển thị theo **dữ liệu đã xác nhận từ server**, nên phải chờ trọn một
  vòng gửi–nhận mới đổi:
  - Trạng thái **bind** đọc từ NBT của item → chỉ đổi khi server đồng bộ ItemStack về, mà lúc đó
    **không có gì kích hoạt vẽ lại**.
  - Trạng thái **đạn** đọc từ bản tin trả lời cũ → sau khi chọn không hề hỏi lại server, nên giữ
    nguyên giá trị cũ tới khi mở lại tablet (lúc đó mới hỏi lại).
- **Khắc phục — hiển thị ngay, dữ liệu thật đè lên sau**:
  - Màn hình giữ tập `boundIds` riêng, đổi **tức thì** khi bấm nút; đọc lại từ NBT khi dữ liệu thật
    về. Nhờ vậy nếu server **từ chối** thao tác thì giao diện **tự sửa lại**, chứ không nói dối mãi.
  - Tương tự `pendingAmmoId` cho việc chọn đạn, bị xoá khi danh sách mới từ server tới.
- **Cải thiện kèm theo (chưa ai báo lỗi nhưng cùng gốc nguyên nhân)**: màn hình giờ theo dõi **chữ ký
  trạng thái của item**, nên tablet đang mở mà mục tiêu thay đổi **từ bên ngoài** (ngắm-để-gán, click
  chọn trên bản đồ, server sửa) thì hàng đợi cũng tự cập nhật. Trước đây sẽ đứng im.
- **Bài học**: giao diện gộp một màn hình nhiều tab **không tự động vẽ lại** như các màn hình rời
  trước đây (mỗi màn hình con vốn tự xử lý dữ liệu của riêng nó). Khi gộp, phải chủ động theo dõi
  **mọi nguồn dữ liệu** (bản tin server + NBT item) và vẽ lại khi chúng đổi.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). Chờ test lại.

### 2026-08-13 — Rút gọn tên đạn + Bước 2: Nhúng bản đồ vào giữa tablet
- **Sửa tên đạn**: tên item đều bắt đầu bằng cỡ nòng ("Large Caliber AP Shell", "Large Caliber HE
  Shell"...) nên cắt theo bề rộng xong **mọi dòng đều thành "Large Calibe..."** — vô dụng. Đổi sang
  đặt tên theo **hậu tố của ammo id** (`_he` → "HE Shell", `_ap` → "AP Shell", `_cm` → "Cluster",
  `_wp` → "Phosphorus", `_smoke`, `_illum`...). Loại lạ chưa biết thì bỏ tiền tố cỡ nòng rồi làm đẹp
  chuỗi còn lại, để pháo nào mod chưa gặp vẫn có dòng đọc được thay vì id thô.
- **Bước 2 — bản đồ nhúng (`MapPanel`)**:
  - **JourneyMap vẽ hộ, tablet chỉ đi xin ảnh**: `IClientAPI.requestMapTile(...)` trả về
    `NativeImage` của vùng chunk quanh tâm; nạp thành `DynamicTexture` rồi vẽ vào giữa tablet, marker
    vẽ đè lên trên. Lời gọi **bất đồng bộ và tốn kém** nên chỉ gọi khi vùng nhìn **thật sự đổi** (đổi
    tâm/zoom/thế giới), theo dõi bằng một khoá chuỗi — không gọi mỗi khung hình.
  - Thao tác ngay trong tablet: **chuột trái** gán mục tiêu, **chuột phải** xoá mục tiêu gần nhất
    (dung sai lại **tính theo điểm ảnh quy đổi ra block**, đúng bài học đã rút ở lần trước), **lăn
    chuột** phóng to/thu nhỏ. Bản đồ tự bám theo người chơi, chấm trắng là vị trí người chơi.
  - Marker: ô vuông xanh = pháo đã bind, chữ thập đỏ đánh số = mục tiêu, **đổi vàng nếu ngoài tầm
    bắn** — tận dụng luôn dữ liệu tầm bắn đã có từ trước.
  - `JourneyMapSupport` giờ giữ luôn `IClientAPI` (bỏ ràng buộc "không import JourneyMap" vì nó đã
    thành phụ thuộc bắt buộc) — vì handle API chỉ có khi JourneyMap khởi tạo plugin, màn hình tablet
    không có đường nào khác để lấy.
  - Giải phóng texture trong `removed()` để không rò rỉ khi đóng tablet.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`).
- **Người dùng test: bản đồ không hiện, kẹt ở "Đang tải bản đồ..."** → callback không bao giờ chạy.

### 2026-08-13 — ⚠️ Phát hiện quan trọng: `requestMapTile` là API bị khuyến cáo không dùng
- **Đọc kỹ javadoc `IClientAPI.requestMapTile`** (điều lẽ ra phải làm TRƯỚC khi xây cả bản đồ lên nó):
  > *"This method **IS NOT SUPPORTED for most mods**. Misuse will lead to severe performance issues.
  > Talk to Techbrew if you need to use this function."*
  > *Requests may be **throttled**, so use sparingly. The largest image size that will be returned is
  > **512x512 px**. If it returns **null**, then no image available.*
- **Hai lỗi của tôi lộ ra từ đó**:
  1. **Vùng xin quá lớn**: ở zoom 2 tôi xin 33×33 chunk = 528 block; số điểm ảnh =
     `chunks × 16 × 2^zoom` vượt xa 512 → JourneyMap trả `null`. Đã sửa: bán kính chunk **co lại theo
     zoom** để luôn vừa 512px.
  2. **Xử lý `null` sai**: reset khoá rồi tick sau xin lại → **vòng lặp xin liên tục** đúng vào API bị
     điều tiết. Đã sửa: ghi nhớ khoá thất bại, không xin lại. Đây là lỗi phải sửa **bất kể** đi hướng
     nào, vì nó có thể gây đúng "vấn đề hiệu năng nghiêm trọng" mà tài liệu cảnh báo.
  3. Thêm ghi log lúc xin và lúc thất bại, để lần sau chẩn đoán được thay vì đoán.
- **Vấn đề còn lại mang tính nguyên tắc**: kể cả khi chạy được, ta vẫn dựa vào một API mà **chính tác
  giả bảo đừng tự ý dùng**, có thể bị điều tiết hoặc chặn bất cứ lúc nào. Đã trình bày 3 hướng cho
  người dùng chọn:
  - **A. Tiếp tục `requestMapTile`** — có ảnh địa hình thật, nhưng dùng API bị khuyến cáo.
  - **B. Tự vẽ bản đồ chiến thuật** (lưới toạ độ + marker + vòng tầm bắn, không ảnh địa hình) — tự
    chủ hoàn toàn, nhẹ, và **giống bản đồ pháo binh thật** (vốn là lưới toạ độ với ký hiệu, không
    phải ảnh vệ tinh). **Đây là hướng tôi nghiêng về.**
  - **C. Tự vẽ từ chunk phía client** — có địa hình nhưng chỉ trong tầm render, vô dụng cho mục tiêu
    ở hàng nghìn block.
- **Bài học**: **đọc javadoc của API trước khi xây kiến trúc lên nó**, không chỉ đọc chữ ký hàm. Chữ
  ký cho biết gọi được, javadoc mới cho biết **có nên gọi hay không**.
- **Người dùng chọn hướng C**: tự vẽ bản đồ từ dữ liệu chunk phía client.

### 2026-08-13 — Bản đồ tự vẽ (hướng C), bỏ hẳn `requestMapTile`
- **Đã làm**: `MapPanel` viết lại hoàn toàn, không còn phụ thuộc JourneyMap cho phần bản đồ:
  - Với mỗi điểm ảnh: tra `Heightmap.WORLD_SURFACE` → lấy block trên cùng → lấy **map colour** của
    nó (đúng bảng màu Minecraft dùng cho item bản đồ, nên nhìn quen mắt). **Đổ bóng theo độ dốc** so
    với ô phía bắc để địa hình nổi khối.
  - Vẽ lại **khi cần**, không phải mỗi khung hình: khi đi đủ xa, đổi zoom, hoặc mỗi 2 giây (vì chunk
    vẫn đang tải dần).
  - 5 mức zoom **64/128/256/512/1024 block**, có ghi tỉ lệ ở góc bản đồ để ước lượng cự ly.
  - Texture cố định 256×256 → zoom lớn thì lấy mẫu thưa, mất chi tiết nhưng **chi phí vẽ không đổi**.
- **Điểm thiết kế quan trọng nhất**: vùng client chưa tải được vẽ thành **nền tối kèm lưới ô 64
  block**, khác hẳn vùng trống thật. Bản đồ **không được phép giả vờ biết thứ nó không biết** — với
  thiết bị bắn xa hàng nghìn block thì đây là ranh giới phải nhìn thấy được.
- **Hạn chế cố hữu đã báo trước người dùng**: client chỉ có chunk trong tầm render, nên mục tiêu ở
  4500 block sẽ nằm trong vùng tối. Đây là giới hạn của hướng C, không sửa được.
- **Người dùng test hướng C: "không có tiềm năng"** → chuyển sang hướng B.

### 2026-08-13 — Bản đồ chiến thuật dạng lưới toạ độ (hướng B) — CHỐT
- **Lý do đi tới đây** (đã thử và loại 2 hướng trước, ghi lại để không ai làm lại):
  - **Hướng A (`requestMapTile` của JourneyMap)**: javadoc ghi rõ "IS NOT SUPPORTED for most mods",
    bị điều tiết, giới hạn ảnh 512×512 → loại.
  - **Hướng C (tự lấy màu từ chunk client)**: chỉ có dữ liệu trong tầm render, mà đây là thiết bị bắn
    xa hàng nghìn block → người dùng test và kết luận không dùng được.
  - **Hướng B**: lưới toạ độ, **không phụ thuộc dữ liệu địa hình nào cả** → đọc như nhau ở 50 block
    lẫn 5000 block, đúng tầm hoạt động thật của thiết bị. Cũng đúng cách pháo binh thật làm việc: báo
    toạ độ lưới và phương vị, không phải ảnh chụp.
- **Đã làm**:
  - **Lưới thích ứng**: khoảng cách lưới tự đổi theo zoom (~8 ô ngang màn), có **nhãn toạ độ thật**,
    trục X=0/Z=0 tô riêng. **6 mức zoom 128 → 4096 block**.
  - **Đường bắn** mờ từ mỗi khẩu tới mỗi mục tiêu → thấy được hình học trận địa, đúng công dụng của
    bảng tác xạ. Đổi màu theo việc mục tiêu có bắn tới được không.
  - **Cự ly + phương vị** hiện ở góc (`T1 4300m 87°`) — hai con số pháo thủ thật sự cần.
  - **Chuột giữa kéo** để xem chỗ khác, nút "Về Vị Trí" hiện khi đang rời khỏi vị trí người chơi.
    Chuột trái/phải giữ nguyên cho gán/xoá mục tiêu.
  - Không còn texture nào → `close()` không phải giải phóng gì, và không có chi phí vẽ lại định kỳ.
- **Người dùng test hướng B: cũng loại.**

### 2026-08-13 — Khảo sát Xaero + quay lại JourneyMap: bản đồ LAI (lưới + ảnh)
- **Khảo sát 2 mod Xaero theo yêu cầu người dùng** (tải jar về đọc thật, không dựa vào tìm kiếm web
  như lần trước — lần đó tôi loại Xaero hơi vội):
  - **Xaero KHÔNG có gói API công khai nào** — toàn bộ nằm trong `xaero.map.*`, là code nội bộ.
  - Nhưng **không bị làm rối tên**, nên vẫn gọi được: `WorldMapSession.getCurrentSession()` (public
    static) → `getMapProcessor()`, và `MapRegion extends LeveledRegion<LeafRegionTexture>` cho thấy
    **mỗi vùng có sẵn texture OpenGL** có thể vẽ lại.
  - **Kết luận**: dùng được, nhưng là gọi thẳng vào ruột mod khác, không có cam kết tương thích —
    Xaero đổi cấu trúc ở bản sau là hỏng. JourneyMap ít nhất *có* API và ghi rõ cái nào không nên
    dùng; Xaero không hứa gì cả.
- **Người dùng chỉ lại repo `TeamJM/journeymap-api`** — chính repo tôi đã clone và đang dùng cho
  marker/click. Nhưng nhắc này làm lộ ra **một việc còn dang dở quan trọng**: sau khi tôi sửa lỗi
  kích thước ảnh (xin vùng > 512px → trả `null`), **bản sửa đó CHƯA BAO GIỜ ĐƯỢC TEST** — người dùng
  chuyển sang hướng C ngay sau đó. Tức là hướng A đang bị loại dựa trên một thất bại mà nguyên nhân
  đã được khắc phục.
- **Đã làm — bản đồ LAI, không đặt cược vào một nguồn duy nhất**:
  - **Lưới toạ độ luôn có** làm nền: không phụ thuộc ai, hoạt động ở mọi cự ly.
  - **Ảnh địa hình JourneyMap phủ lên** khi xin được, chỉ xin ở zoom ≤ 512 block (trong giới hạn
    API); zoom xa hơn thì dùng lưới.
  - JourneyMap từ chối thì **mất chi tiết, không mất bản đồ**.
  - **Đây là điều lẽ ra phải làm ngay từ đầu** thay vì để cả tính năng phụ thuộc một lời gọi có thể
    trả `null`.
- **Bài học**: khi một tính năng dựa vào nguồn dữ liệu ngoài tầm kiểm soát, hãy thiết kế sao cho
  **nguồn đó hỏng thì suy giảm chứ không sụp đổ**. Ba lần thử trước đều là "được ăn cả ngã về không".
- **Trạng thái**: `gradlew build` sạch. Chờ test tay.

### 2026-08-13 — Sửa pan bản đồ + khảo sát đường vào Xaero (VIỆC ĐANG DANG DỞ)
- **Sửa lỗi pan**: tôi gán pan vào **chuột giữa** (lựa chọn tồi) và **quên vẽ lại** sau khi pan nên
  nút "Về Vị Trí" không bao giờ hiện. Đổi sang **kéo chuột trái**, phân biệt kéo với bấm bằng ngưỡng
  3 điểm ảnh — bấm không di chuyển thì gán mục tiêu, kéo thì dịch bản đồ. Chuột phải vẫn xoá.
- **`requestMapTile` thất bại lần 2** (lần này kích thước đã đúng giới hạn). Kết luận: nhiều khả năng
  JourneyMap **chủ động không phục vụ mod chưa được tác giả duyệt**, không phải lỗi tham số. Không
  nên mò tiếp ở đây.

#### 🔧 ĐIỂM DỪNG — hướng Xaero đã khảo sát xong, CHƯA triển khai
Người dùng chọn thử hướng Xaero. Đã xác nhận **khả thi về mặt kỹ thuật**, tìm được đủ mắt xích:

| Mắt xích | API (jar `xaeroworldmap-forge-1.20.1-1.44.2`, CurseForge project **317780** file **8449908**) |
|---|---|
| Vào phiên bản đồ | `xaero.map.WorldMapSession.getCurrentSession()` → `.getMapProcessor()` |
| Lấy vùng | `MapProcessor.getMapRegion(int, int, int, boolean)` |
| **Lấy texture** | `xaero.map.region.texture.RegionTexture.getGlColorTexture()` — **public final, trả GL texture id** |
| Minimap (nếu cần) | CurseForge project **263420** file **8449808** |

**Việc còn phải làm khi quay lại:**
1. Texture nằm ở cấp **`MapTileChunk` (64×64 block)**, không phải cả vùng `MapRegion` (512×512) —
   phải duyệt các tile-chunk trong vùng và ghép lại. Cần javap thêm `MapRegion` / `MapTileChunk` để
   tìm hàm duyệt chunk và lấy `LeafRegionTexture` của từng cái.
2. Viết qua **reflection**, không compile-time dependency, để addon không chết khi thiếu Xaero (giống
   cách `JourneyMapSupport` từng làm trước khi JourneyMap thành phụ thuộc bắt buộc).
3. Vẽ texture bằng GL id vào đúng toạ độ thế giới đã có sẵn trong `MapPanel.worldToScreen`.

**Rủi ro đã thống nhất với người dùng**: đây là gọi thẳng vào code nội bộ Xaero, **không có cam kết
tương thích** — Xaero đổi cấu trúc ở bản sau là hỏng. Đổi lại nó có đúng thứ cần (texture vùng đã
khám phá, không giới hạn tầm render) với công sức vừa phải.

**Lưu ý**: lớp lưới toạ độ hiện tại **phải giữ làm nền**, texture Xaero chỉ phủ lên — đúng bài học đã
rút: nguồn dữ liệu ngoài tầm kiểm soát thì hỏng phải **suy giảm chứ không sụp đổ**.

### 2026-08-13 — Hướng Xaero: đã triển khai xong lớp địa hình phủ lên lưới
- **Đã làm đủ cả 3 việc còn treo ở điểm dừng trước** (`XaeroMapSupport` mới + `MapPanel` viết lại
  phần nền + phụ thuộc runtime trong `build.gradle`).
- **Khảo sát bytecode trước khi viết (javap trên đúng jar 1.44.2), tìm được đường ngắn hơn dự kiến**:
  | Việc | Cách làm thật |
  |---|---|
  | Vào phiên | `WorldMapSession.getCurrentSession()` → `getMapProcessor()` |
  | Lấy vùng | `MapProcessor.getMapRegion(caveLayer, rx, rz, create)` — **thứ tự tham số là (tầng hang, X, Z, có tạo không)**, xác nhận qua constructor `MapRegion` bên trong `getLeafMapRegion` |
  | Tầng hang | `MapProcessor.getCurrentCaveLayer()` (public) — không phải tự đoán hằng số |
  | Lấy texture | `MapRegion.getChunk(i, j)` → `MapTileChunk.getLeafTexture()` → `getGlColorTexture()`, trả `-1` khi chưa nạp lên GPU |
  - **Không phải tự ghép ảnh như lo ngại ở điểm dừng**: mỗi tile-chunk là **một quad riêng 64×64
    block** vẽ đúng vị trí thế giới — chính là cách `GuiMap` của Xaero tự vẽ (đọc được vòng lặp
    `bipush 8` × `bipush 8` rồi `renderTexturedModalRectWithLighting3` trong bytecode).
  - Kích thước xác nhận bằng hằng số thật: `MapTileChunk.SIDE_LENGTH = 4` (tile) và
    `MapRegion.SIDE_LENGTH = 8` (tile-chunk) → 64 block/tile-chunk, 512 block/vùng.
- **Ba chi tiết bắt buộc học từ chính code Xaero, không tự nghĩ ra được**:
  1. **Phải giữ khoá `MapProcessor.renderThreadPauseSync`** suốt lúc đọc + vẽ. Xaero xoá texture ở
     luồng khác; cả `GuiMap` lẫn cầu nối minimap đều bọc toàn bộ vòng vẽ trong khoá này. Không giữ
     khoá thì sớm muộn cũng vẽ vào texture vừa bị xoá.
  2. **Yêu cầu nạp vùng tối đa 1 lần/khung hình**: `MapSaveLoad.requestLoad(region, lý_do, false)` +
     `setNextToLoadByViewing(region)`, chỉ khi `getLoadState() == 0`. Con số 1 là **đọc ra từ
     bytecode minimap** (biến đếm so với hằng `iconst_1`), không phải tự chọn — đây là hàng đợi đọc
     đĩa dùng chung với bản đồ của chính Xaero.
  3. **`bumpLoadedRegion`** (qua `getMapWorld().getCurrentDimension().getLayeredMapRegions()`) để
     vùng đang xem không bị LRU của Xaero đẩy ra — nếu không, kéo bản đồ ra xa người chơi sẽ thấy
     địa hình nhấp nháy do liên tục bị huỷ rồi nạp lại.
- **Quyết định tắt blend khi vẽ texture** (đáng ghi vì trông như lỗi): kênh alpha của texture Xaero
  **không phải độ trong suốt** — nó chở dữ liệu ánh sáng cho shader riêng của Xaero (thấy qua cờ
  `getTextureHasLight()` chọn giữa 2 renderer khác nhau). Nếu bật blend thì vùng tối sẽ mờ đi vô cớ.
  Vẽ đục hoàn toàn bỏ qua kênh đó và chỉ lấy màu. Đổi lại ta mất hiệu ứng đổ bóng của Xaero — chấp
  nhận được với bản đồ chiến thuật.
- **Lưới đổi màu khi có địa hình**: lưới vốn là nét sáng trên nền tối; đè lên ảnh địa hình thì màu cũ
  chìm nghỉm. Giờ có địa hình → lưới thành **trắng trong suốt** (đọc được trên cỏ, đá lẫn biển sâu),
  nhãn toạ độ sáng hơn và có bóng chữ. Không có địa hình → giữ nguyên bảng màu cũ.
- **Giới hạn zoom ≤ 1024 block cho địa hình** (mỗi tile-chunk là 1 lệnh vẽ → 1024 block đã là tới
  17×17 = 289 quad). Xa hơn thì chỉ còn lưới — vốn cũng là mức Xaero khó có sẵn dữ liệu đã nạp.
  Góc bản đồ hiện rõ `ĐỊA HÌNH: XAERO` hay `ĐỊA HÌNH: CHỈ CÓ LƯỚI` để biết đang xem gì.
- **Bỏ hẳn `requestMapTile`** của JourneyMap khỏi `MapPanel` (đã thất bại 2 lần, xem entry trước).
  JourneyMap **vẫn là phụ thuộc** cho marker và click chọn mục tiêu trên bản đồ toàn màn hình của
  nó; chỉ phần ảnh nền là chuyển sang Xaero.
- **Giữ đúng nguyên tắc "hỏng thì suy giảm, không sụp đổ"**: `XaeroMapSupport` toàn bộ bằng
  reflection, `runtimeOnly` chứ **không** đưa Xaero lên compile classpath (để không thể lỡ tay
  hard-link). Thiếu mod, sai chữ ký, hay lỗi lúc chạy → ghi log **đúng một lần** rồi tắt vĩnh viễn
  lớp địa hình; lưới toạ độ chạy tiếp như chưa có gì. `MapPanel.close()` giờ rỗng vì texture là của
  Xaero, addon chỉ mượn để vẽ nên không thể rò rỉ.
- **Phụ thuộc mới**: `xaero_worldmap_curse_file=curse.maven:xaeros-world-map-317780:8449908`
  (1.44.2). `xaerolib` đi kèm sẵn dạng jar-in-jar, không phải thêm riêng.
- **Trạng thái**: `gradlew build` sạch **ngay lần đầu**, `runClient` boot sạch (`FATAL=0`), log xác
  nhận Xaero World Map 1.44.2 + XaeroLib 1.7.1 nạp cùng addon. **Chưa test tay.**
- **Cần người dùng kiểm khi test**: (1) mở tablet ở nơi đã đi qua — có thấy địa hình dưới lưới
  không; (2) kéo bản đồ sang vùng đã khám phá ở xa — địa hình có hiện ra sau một lúc không (nạp dần,
  1 vùng/khung hình); (3) vùng chưa từng đi qua phải là **nền tối có lưới**, không được vẽ bừa;
  (4) zoom ra 2048/4096 → chuyển sang chỉ-lưới, chữ góc bản đồ đổi theo; (5) lưới và nhãn toạ độ có
  còn đọc được khi đè lên địa hình sáng/tối không; (6) mở bản đồ Xaero thật rồi quay lại tablet xem
  có gì hỏng không.

### 2026-08-14 — Sửa lỗi Xaero: địa hình mới không bao giờ hiện (tự đơn giản hoá điều kiện nạp)
- **Hiện tượng người dùng phát hiện**: địa hình **có hiện** (một cụm làng), nhưng đứng ở khu vực mới
  vài phút thì chỗ đó vẫn trống trơn. Tức không phải lỗi căn toạ độ như tôi nghi ban đầu.
- **Manh mối quyết định**: cụm hiện được và cụm không hiện **nằm trong cùng một region 512 block**.
  Nên không thể là lỗi tính region/tile — phải là chuyện một số tile-chunk có texture, số khác không.
- **Nguyên nhân thật (đọc bytecode, không đoán)**: vòng đời region của Xaero có `loadState`:
  `0` = chưa đọc · `1–3` = đang xử lý (`shouldBeProcessed()` đúng) · `4` = xong,
  `onProcessingEnd()` đặt `loadState = 4` và **loại region khỏi `toProcessLevels`**.
  Chỉ region nằm trong danh sách đó mới được `onRenderProcess()` gọi `uploadBuffer()` để biến dữ
  liệu thành texture GPU. Hệ quả: **region đã xử lý xong thì không còn gì dựng lại texture nữa** —
  `MapWriter` vẫn ghi dữ liệu chunk mới vào region, nhưng dữ liệu đó **không bao giờ lên GPU**.
  → Đất đi qua *trước* khi region kết thúc xử lý thì hiện; đất đi qua *sau* đó thì đen vĩnh viễn,
  đứng bao lâu cũng vô ích. Khớp chính xác 100% với những gì người dùng thấy.
- **Lỗi của tôi**: điều kiện xin nạp tôi viết là `loadState == 0`. Nhưng chính Xaero có sẵn hàm
  `MapRegion.canRequestReload_unsynced()` và nó cho phép **cả `loadState == 4`** lẫn
  `loadState == 2 && isBeingWritten()`. Tôi đã **tự viết lại điều kiện tiên quyết của mod khác** —
  đúng cái bẫy mà Phase 6 đã rút ra bài học và ghi vào tài liệu này. Lần đó là `setTarget`, lần này
  là `requestLoad`. Rút gọn "chỉ nạp khi chưa nạp" nghe hợp lý nhưng bỏ mất đúng ca quan trọng nhất.
- **Cách khắc phục — dùng tín hiệu của Xaero thay vì suy luận của mình**:
  - Điều kiện "có nên xin nữa không" → gọi thẳng `canRequestReload_unsynced()`, không tự liệt kê.
  - Điều kiện "có gì mới để xin không" → `MapTileChunk.getToUpdateBuffers()`, chính là cờ mà
    `MapWriter` bật khi ghi dữ liệu chưa thành texture (xác nhận qua danh sách class gọi
    `setToUpdateBuffers`). Đây là **quan sát trạng thái**, không phải tái hiện điều kiện.
  - `loadState == 2` → `region.requestRefresh(processor)` (dựng lại từ dữ liệu **trong bộ nhớ**);
    còn lại → `requestLoad` (đọc từ file lưu). Phân nhánh này chép từ cầu nối minimap của Xaero —
    quan trọng vì đọc file sẽ **không có** chunk mới nhất mà `MapWriter` chưa kịp lưu.
  - Thêm **cooldown 2 giây mỗi region** làm chốt chặn cứng: đây là code nội bộ không có cam kết
    tương thích, nếu một trong hai tín hiệu trên đổi nghĩa ở bản Xaero sau thì cooldown vẫn chặn
    được vòng lặp xin liên tục. Vẫn giữ tối đa 1 yêu cầu/khung hình.
- **Bài học (biến thể thứ ba của cùng một bài học)**: khi tích hợp với mod khác, **đừng rút gọn điều
  kiện tiên quyết của nó cho "gọn hơn"**. Nếu mod đó có sẵn hàm trả lời đúng câu hỏi mình đang hỏi
  (ở đây là `canRequestReload_unsynced`), hãy gọi hàm đó. Điều kiện tự viết luôn thiếu đúng nhánh
  mình chưa nghĩ tới, và triệu chứng thì trông y hệt lỗi ở chỗ khác (tôi đã nghi lệch toạ độ).
- **Cách kiểm chứng nhanh chẩn đoán này** (nếu cần xác nhận lại): mở bản đồ Xaero thật (`M`) ngắm
  vùng đang đứng rồi đóng đi — GuiMap của Xaero sẽ kích hoạt đúng cơ chế refresh này, sau đó mở
  tablet sẽ thấy địa hình hiện ra. Bản sửa chỉ là làm việc đó tự động.
- **Trạng thái**: `gradlew build` sạch. Chờ test tay.

### 2026-08-14 — Người dùng tìm được maven chính chủ của Xaero (chưa dùng, ghi lại để sau)
- `https://chocolateminecraft.com/maven/xaero/` — maven **chính chủ**, có `lib/ map/ minimap/ pvp/ pac/`.
  Đường dẫn đúng phiên bản đang dùng:
  `xaero/map/xaeroworldmap-forge-1.20.1/1.44.2/` — có cả `.jar`, **`-dev.jar`** và `.pom`.
- **Vì sao đáng giá**: có `-dev.jar` nghĩa là **compile thẳng vào Xaero được** thay vì reflection.
  Lợi ích lớn nhất không phải code gọn hơn mà là: Xaero đổi chữ ký ở bản sau thì **`gradlew build`
  báo lỗi ngay**, thay vì im lặng rơi về chế độ chỉ-lưới lúc chạy mà không ai biết. Vẫn giữ được
  tính phụ thuộc mềm bằng `compileOnly` + `ModList.isLoaded` + bắt `NoClassDefFoundError`, đúng
  kiểu đã làm với JourneyMap API trước đây.
- **Cố ý CHƯA đổi ngay**: đang giữa lúc sửa lỗi hiển thị: đổi cả cách phụ thuộc cùng lúc sẽ trộn hai
  biến, không biết kết quả test là do bản sửa hay do đổi dependency. Làm sau khi địa hình chạy đúng.
- Lưu ý khi làm: `-dev.jar` đã deobf sẵn nên **không bọc `fg.deobf()`**; jar phát hành ở CurseForge
  vẫn giữ nguyên cho `runtimeOnly`.

### 2026-08-14 — ĐỔI HƯỚNG: bỏ hết mod bản đồ ngoài, tự khảo sát địa hình phía SERVER
- **Bối cảnh quyết định**: bản sửa Xaero không ăn thua. Sau khá nhiều bytecode tôi vẫn không nắm chắc
  vòng đời texture của nó. Người dùng đánh giá "tiền sửa quá tiền mua mới" và đề xuất tự làm. Đồng ý:
  chi phí thật không phải sửa vài dòng mà là **mỗi lần hỏng lại phải dịch ngược code nội bộ mod khác**.
  Bốn lần thử mod ngoài (JourneyMap ×2, Xaero ×2) đều chết vì cùng một lý do.
- **Chẩn đoán lại vì sao hướng C từng chết** (quan trọng, nếu hiểu sai sẽ chết lại y hệt): **không
  phải vì tự vẽ là bất khả thi, mà vì nó không có tầng LƯU TRỮ**. Xaero/JourneyMap cũng chỉ đọc được
  chunk trong tầm render — toàn bộ mánh của chúng là ghi ra đĩa rồi đọc lại.
- **Nhưng lần này đi xa hơn: nguồn dữ liệu là SERVER, không phải client.** Lý do đổi so với đề xuất
  ban đầu của tôi — tôi từng lập luận "làm client trước vì nó chạy được cả khi server không cài
  addon". **Lập luận đó sai**: addon này bắt buộc phải có phía server mới dùng được (packet, hàng đợi
  lệnh bắn, theo dõi góc nòng đều chạy phía server từ Phase 2–3), nên không tồn tại tình huống đó.
  Phần quét lại giống hệt nhau ở hai phía, khác mỗi chỗ lấy chunk từ đâu → làm server không đắt hơn
  bao nhiêu mà được nhiều hơn hẳn.
- **Người dùng đã chốt (2026-08-14)**: nguồn **server làm chủ ngay**, độ phân giải **1 điểm ảnh =
  1 block**. Bối cảnh sử dụng: chơi đơn hoặc server vài chục người; server lớn tính sau.
- **Ba trạng thái của một vùng đất — nền tảng của toàn bộ thiết kế**:
  `[1] chưa sinh` (chỉ có công thức seed, **0 byte trên đĩa**) → `[2] đang tải` (trong RAM, mới nhất)
  → `[3] đã cất` (file `.mca`, đầy đủ từng block). Đọc được 2 và 3; **tuyệt đối không đụng 1**.
- **API đã xác minh bằng javap trên jar MC thật** (không đoán):
  | Cần | Cách làm |
  |---|---|
  | Chunk đang tải | `ServerChunkCache.getChunkNow(x,z)` — trả null nếu chưa tải, **không bao giờ sinh** |
  | Chunk trên đĩa | `chunkMap.read(ChunkPos)` → `CompletableFuture<Optional<CompoundTag>>`, **public**, chỉ đọc file |
  | Giải bit | `new SimpleBitStorage(bits, size, long[])` — **constructor public**, khỏi tự viết bit math |
  | Màu | `MapColor.byId(int)` + `calculateRGBColor(Brightness)` — đúng bảng màu item bản đồ vanilla |
  - **Bẫy off-by-one đã tránh**: `ChunkAccess.getHeight(...)` trả `getFirstAvailable() - 1` tức
    **chính block trên cùng**, còn heightmap trong NBT lưu `firstAvailable` tức **ô trống phía trên**.
    Hai đường phải lệch nhau đúng 1. Đã kiểm bytecode chứ không suy đoán.
- **Định dạng ô (`TerrainTile`)**: 64×64 block, mỗi cột **1 byte màu** (id bảng màu vanilla) +
  **2 byte độ cao**. 12 KB thô, deflate còn vài KB — vừa một packet.
  - **Lưu độ cao ngay từ bản đầu dù chưa dùng**: đây là thứ sẽ giải được hạn chế còn treo từ Phase 6
    (đạn bắn căng đâm vào đồi giữa đường mà không ai cảnh báo). Thêm sau = vứt toàn bộ dữ liệu đã thu.
  - **Cố ý KHÔNG nướng sẵn đổ bóng vào ô**: đổ bóng cần so với cột phía bắc, mà cột đó nằm ở ô bên
    cạnh — nướng sẵn thì cứ 64 block lại có một đường nối lộ rõ. Client giữ độ cao của các ô lân cận
    nên tự đổ bóng liền mạch được.
- **Đã triển khai**: `TerrainTile` (định dạng + nén) · `ChunkNbtSampler` (giải NBT chunk đã lưu,
  thuần dữ liệu, không đụng world, chạy được ngoài luồng server) · `ServerTerrainProvider` (ghép 2
  nguồn) · `RequestTerrainTilesMessage`/`TerrainTileMessage` · `TerrainClientCache` ·
  `TerrainImage` (dựng texture 512×512) · `MapPanel` vẽ dưới lưới. **Xoá hẳn `compat/xaero`.**
- **Bốn quyết định đáng ghi lại**:
  1. **Kéo chứ không đẩy**: server không stream bản đồ cho ai cả, chỉ trả lời khi có tablet đang mở
     và đang nhìn vào đâu đó. Chi phí tỉ lệ với việc dùng, không tỉ lệ với số người online.
  2. **Ô rỗng vẫn là câu trả lời**, không phải thất bại. Client ghi nhận "chỗ này chưa từng được
     sinh ra" và **không hỏi lại** — nếu coi là lỗi thì sẽ thành vòng lặp hỏi đúng vào vùng tốn kém
     nhất cho server.
  3. **Cầm tablet mới được hỏi** (kiểm tra phía server). Không có chốt này thì addon thành công cụ
     quét thế giới cho bất kỳ client nào.
  4. **Vùng chưa biết để trong suốt hoàn toàn**, lưới xuyên qua. Bản đồ không được phép hoá trang một
     lỗ hổng thành đất bằng phẳng.
- **Ba rủi ro tự phát hiện khi đọc lại code và đã sửa trước khi build**:
  - Độ rộng mỗi ô heightmap **phải tính theo chiều cao của dimension**, không hardcode 9 bit — đọc
    sai độ rộng sẽ ra số liệu trông hợp lý nhưng sai, khó phát hiện hơn nhiều so với lỗi rõ ràng.
  - `blit` vẽ **thẳng lên màn hình** còn `fill` thì gom lô — không `flush()` trước thì nền panel sẽ
    đè lên địa hình.
  - Mỗi ô về là một lần tăng phiên bản; vẽ lại 262144 điểm ảnh cho từng ô sẽ giật. Giới hạn: dữ liệu
    mới gộp vào mỗi 250 ms, còn **đổi khung nhìn thì vẽ lại ngay**.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). **Chưa test tay.**
- **Xaero tạm giữ trong `build.gradle`** (`runtimeOnly`) — addon **không dùng nữa**, chỉ để mở phím
  `M` đối chiếu bản đồ của ta với nó lúc test. Gỡ hẳn khi đã tin tưởng.
- **Cần người dùng kiểm khi test**:
  1. Mở tablet ở chỗ đang đứng — có địa hình dưới lưới không, có khớp vị trí với lưới không.
  2. **Kéo bản đồ tới vùng bạn CHƯA từng đi** nhưng thế giới đã sinh (vd. gần chỗ đã đi qua) —
     phải hiện ra sau 1–2 giây. **Đây là phép thử quyết định**, thứ mà cả 4 lần trước đều không làm được.
  3. Vùng thật sự chưa ai tới bao giờ → phải là **nền tối + lưới**, không vẽ bừa.
  4. Bắn sập gì đó rồi mở lại tablet — hố đạn có hiện không (không cần bay tới xem).
  5. Zoom 128 → 4096, ảnh có bám đúng lưới ở mọi mức không.
  6. Bấm `M` xem bản đồ Xaero, so vị trí địa hình hai bên có trùng khớp không.
  7. Có giật/tụt khung hình lúc kéo bản đồ nhanh không.

### 2026-08-14 — Sửa lỗi tỉ lệ địa hình lệch khỏi lưới + giật khi kéo bản đồ
- **Người dùng quay video test.** Lần này tôi **xem được bằng chứng thật** thay vì suy đoán: trích
  khung hình bằng `ffmpeg` rồi đo trực tiếp. Giữa hai khung cách nhau vài giây trong cùng một thao
  tác kéo: nhãn lưới `3072` dịch **+98 px**, còn cụm địa hình chỉ dịch **~+15 px**. Lưới trượt trên
  địa hình ~4 lần nhanh hơn. Nhãn góc ghi **128m** — đúng điều kiện kích hoạt lỗi.
- **Nguyên nhân (lỗi một dòng)**: `blocksPerPixel = Math.max(1.0, span / TEXTURE_SIZE)`. Cái
  `max(1.0)` đó khiến khi **span < 512** (mức zoom 128 và 256), texture vẫn phủ trọn
  `512 × 1 = 512 block`, trong khi `blit` lại kéo nó vừa khít khung mà lưới bảo là 128 block.
  → địa hình bị vẽ **thu nhỏ 4 lần** so với lưới. Ở span ≥ 512 thì trùng khớp, nên lần chụp màn hình
  đầu tiên (512m) trông vẫn ổn và tôi đã không phát hiện ra.
- **Khắc phục**: bỏ hẳn cái kẹp. Vùng texture được lấp giờ là `min(512, span)` với **đúng 1 điểm ảnh
  = 1 block**, `blit` chỉ lấy đúng ô vuông đó. Zoom gần thì ảnh vuông nhỏ hơn rồi để GPU phóng to —
  vừa đúng tỉ lệ ở mọi mức zoom, vừa **rẻ hơn** (span 128 chỉ còn 16 nghìn điểm ảnh thay vì 262 nghìn).
- **Nguyên nhân giật khi kéo**: kéo bản đồ = khung nhìn đổi mỗi khung hình = dựng lại toàn bộ
  262144 điểm ảnh, **mỗi điểm ảnh 3 lần tra HashMap** (màu + độ cao tại chỗ + độ cao phía bắc)
  → gần **800 nghìn lần tra mỗi khung hình**.
- **Khắc phục giật, ba việc**:
  1. **Tra ô theo hàng, không theo điểm ảnh**: đọc cả một hàng đông-tây một lượt, mỗi ô chỉ tra một
     lần khi con trỏ đi qua ranh giới ô. 4608 lần tra thay vì 786000 — **giảm ~170 lần**.
  2. **Đổ bóng dùng lại hàng vừa vẽ**: hàng trước **chính là** hàng cách một bước về phía bắc, nên
     không cần lấy mẫu thêm lần nào cho việc đổ bóng. Vừa nhanh hơn vừa chính xác hơn.
  3. **Giãn nhịp quét ô thiếu** (`ensureCovered`): chỉ chạy khi khung nhìn đổi hoặc mỗi 500 ms —
     trước đây mỗi khung hình đều duyệt toàn bộ toạ độ ô trong tầm nhìn.
- **Sửa kèm**: nhãn `128m` và `ĐỊA HÌNH: ...` chuyển sang **góc dưới bên phải** (trước đây đè lên
  nhãn toạ độ trục Z chạy dọc mép trái, thấy rõ trong video), và có bóng chữ khi nằm trên địa hình.
- **Bài học**: một hằng số phòng thủ đặt sai chỗ (`max(1.0, ...)`) tạo ra lỗi **chỉ xuất hiện ở một
  nửa dải tham số**. Ảnh chụp đầu tiên ở 512m trùng khớp hoàn hảo nên tôi tưởng phần toạ độ đã đúng.
  Với thứ có nhiều mức zoom, **phải kiểm ở cả hai đầu dải**, không chỉ mức mặc định.
- **Bài học về cách chẩn đoán**: video của người dùng giải quyết trong vài phút cái mà trao đổi bằng
  lời đã không làm rõ được. Trích khung hình rồi **đo bằng số** (98 px so với 15 px) biến "cảm giác
  sai sai" thành một tỉ lệ cụ thể chỉ thẳng vào dòng code có lỗi.
- **Trạng thái**: `gradlew build` sạch. Chờ test lại.

### 2026-08-14 — Crash lúc tải mod: cuộc đua đăng ký packet (lỗi tiềm ẩn từ Phase 1)
- **Hiện tượng**: `runClient` crash ở màn "Error loading mods", **cả SBW lẫn addon** cùng báo
  `ArrayIndexOutOfBoundsException: Index 16 out of bounds for length 16` trong `common_setup`.
- **Stack trace chỉ thẳng nguyên nhân** (đọc file crash, không đoán):
  `Short2ObjectArrayMap.findKey` ← `.put` ← `IndexedMessageCodec$MessageHandler.<init>` ←
  `SimpleChannel.registerMessage`. Và **cả hai** stack đều kết thúc ở `ForkJoinWorkerThread.run`.
- **Nguyên nhân thật — chạy song song, không phải hết chỗ**:
  - Forge chạy `FMLCommonSetupEvent` của **mọi mod SONG SONG** trên một pool luồng.
  - Packet của addon đăng ký lên **channel của chính SBW**, mà SBW cũng đang đổ packet của nó vào
    đúng channel đó từ common setup của nó → **hai luồng cùng ghi vào một `Short2ObjectArrayMap`**.
  - Map này **không an toàn đa luồng**. Ghi chồng nhau làm `size` vượt quá độ dài mảng → chỉ số 16
    trên mảng dài 16. Cả hai mod đều là nạn nhân của cùng một cuộc đua.
- **Giả định sai đã nằm trong code từ Phase 1**: comment cũ ghi *"mods.toml khai báo superbwarfare là
  phụ thuộc AFTER, nên SBW đã đăng ký xong trước khi hàm này chạy"*. **Sai.** Thứ tự phụ thuộc quyết
  định thứ tự mod được **điều phối**, chứ **không** khiến các hàm setup chạy tuần tự — chúng chồng
  lấn nhau. Cuộc đua tồn tại từ Phase 1 và chỉ đơn giản là **chưa bao giờ thua** cho tới khi thêm 2
  packet địa hình làm rộng cửa sổ va chạm ra.
- **Khắc phục**: bọc toàn bộ phần đăng ký vào `event.enqueueWork(...)`. Việc trong hàng đợi này chạy
  **sau khi mọi handler song song của giai đoạn đó đã xong**, trên một luồng duy nhất → hết chồng
  lấn. Xác nhận qua log: `registered 15 packets` giờ in ra từ **Render thread** (luồng chính) thay
  vì worker.
- **Bài học**: sự kiện vòng đời của Forge **mặc định chạy song song**. Bất cứ khi nào chạm vào trạng
  thái dùng chung với mod khác trong `commonSetup` (channel mạng, registry, cache tĩnh), phải
  `enqueueWork` — thứ tự nạp mod **không** bảo vệ được gì. Lỗi loại này im lặng rất lâu rồi mới nổ,
  và khi nổ thì đổ vạ cho mod khác chứ không phải mình.
- **Việc nên làm sau (chưa làm, cố ý)**: **tự tạo SimpleChannel riêng** thay vì mượn channel của SBW.
  Đó mới là cách sửa tận gốc — hiện tại ID packet của ta phụ thuộc vào số packet SBW đăng ký, nên
  SBW thêm/bớt packet ở bản sau là ID của ta **âm thầm dịch chỗ**. Chưa làm ngay vì đang giữa lúc
  test bản đồ: đổi cả tầng mạng cùng lúc sẽ trộn hai biến, đúng bài học đã rút nhiều lần trong tài
  liệu này. Làm sau khi bản đồ chạy đúng. Việc này chạm ~15 file (mọi chỗ gọi
  `NetworkRegistry.PACKET_HANDLER`), mechanical nhưng không nhỏ.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không còn lỗi tải mod).

### 2026-08-14 — Kéo bản đồ dính trục + lấy ô quá chậm ở zoom rộng
- **Người dùng test lại**: địa hình đã bám lưới ở mọi zoom ✅, kéo nhanh hết giật ✅. Còn 2 vấn đề.
- **Vấn đề 1 — kéo bản đồ ở 128m bị "dính theo trục", lúc bị lúc không.**
  - **Nguyên nhân**: `panByPixels` làm tròn **từng lần gọi một** rồi vứt phần lẻ. Ở 128m, khung rộng
    ~633 px nên **1 pixel ≈ 0,2 block** — mà chuột kéo gửi về từng bước 1–3 pixel. `Math.round(0,2)`
    = 0, nên phần lớn các bước **mất trắng**. Tệ hơn: nó làm tròn **riêng từng trục**, nên bước
    "3 px ngang, 1 px dọc" thành "1 block ngang, 0 dọc" → bản đồ **bò dọc theo trục nào chuột nghiêng
    về**, đúng như mô tả. Chỗ "lúc bị lúc không" là do đôi khi cả hai trục đều đủ lớn để làm tròn lên.
  - **Khắc phục**: cộng dồn phần lẻ giữa các lần gọi (`dragRemainderX/Z`), chỉ dịch tâm khi tích đủ
    một block, giữ lại phần dư. Mọi pixel di chuyển đều được tính. Xoá phần dư khi zoom hoặc bấm
    "Về Vị Trí".
  - **Giới hạn còn lại đã biết**: ở 128m thì 1 block ≈ 5 px nên bản đồ vẫn nhích theo bậc 5 px. Dữ
    liệu vốn là mỗi block một điểm ảnh nên đây là bậc tự nhiên; muốn mượt hẳn phải cho tâm bản đồ
    nhận số thực rồi lệch cả ba lớp (lưới/địa hình/marker) theo phần lẻ — chưa làm, chưa đáng.
- **Vấn đề 2 — "vùng chưa khám phá không hiện": tìm ra một lỗi thật ĐANG che lấp câu trả lời.**
  - `REQUEST_BATCH = 12` ô mỗi 500 ms. Ở zoom 4096m khung nhìn phủ **64×64 = 4096 ô** → phải mất
    **~170 giây** mới lấp đầy. Từ phía người chơi thì không phân biệt được với "bản đồ hỏng".
  - Tệ hơn: vòng lặp lồng nhau duyệt **từ góc tây-bắc**, nên khung rộng được lấp dần theo một **vệt
    dọc bò ngang** — phần đang nhìn (ở giữa) lại là phần về sau cùng.
  - **Khắc phục**: nâng lô lên **32 ô**, và **sắp xếp theo khoảng cách tới tâm khung nhìn** rồi mới
    cắt lô. Giờ chỗ đang nhìn được lấp trước, lan dần ra ngoài.
  - **Bài học**: một hàng đợi lấy dữ liệu quá thận trọng, cộng với thứ tự duyệt sai, **trông y hệt
    một tính năng hỏng**. Khi người dùng báo "không hiện", phải phân biệt *không có dữ liệu* với
    *có nhưng chưa tới lượt*.
- **Chưa kết luận được phần còn lại của vấn đề 2**: sau khi sửa tốc độ lấy ô, nếu vùng đó **vẫn**
  trống thì đó là **hành vi đúng**, không phải lỗi — vì đang chơi đơn, chunk chỉ tồn tại ở nơi người
  chơi đã từng tới gần. Server không thể đưa ra thứ chưa được sinh, và ta cố ý không sinh mới.
  - **Phép thử phân biệt đã đề xuất cho người dùng**: bay thẳng ra ~1000 block rồi quay về. Kéo bản
    đồ **dọc theo đường vừa bay** → phải hiện đủ. Kéo **vuông góc ra khỏi đường bay ~500 block** →
    phải trống. Nếu đúng cả hai thì hệ thống chạy chuẩn.
- **Trạng thái**: `gradlew build` sạch. Chờ test lại.

### 2026-08-14 — "Đến vùng mới không hiện gì": câu trả lời rỗng bị coi là vĩnh viễn
- **Người dùng test**: kéo bản đồ hết dính trục ✅, nhưng phát sinh lỗi mới — **đến vùng mới thì bản
  đồ trống trơn**. Và nó **nặng lên đúng sau thay đổi trước của tôi**, đó chính là manh mối.
- **Nguyên nhân**: client lưu câu trả lời "ô này rỗng" vào một `Set BLANK` và **không bao giờ hỏi
  lại**. Chuỗi sự kiện:
  1. Kéo bản đồ sang vùng chưa sinh → server trả "rỗng" (đúng, lúc đó chưa có gì) → đánh dấu BLANK.
  2. Bay tới chính vùng đó → **chunk được sinh ra thật**.
  3. Nhưng client đã chốt "chỗ này rỗng" nên **không hỏi lại nữa** → trống vĩnh viễn, đúng ngay chỗ
     người chơi vừa tới.
  - **Vì sao nặng lên sau thay đổi trước**: tôi vừa nâng lô từ 12 lên 32 ô và cho quét từ tâm ra —
    nghĩa là **nhiều ô bị đánh dấu rỗng nhanh hơn hẳn**. Bản sửa hiệu năng vô tình biến một lỗi âm ỉ
    thành lỗi lộ rõ mỗi lần chơi.
- **Cùng một lỗi còn có hai biến thể tôi chưa ai báo nhưng đã sửa luôn**:
  - Ô lấy về lúc mới sinh **được một nửa** thì nằm trong cache mãi ở trạng thái nửa vời.
  - **Hố đạn do chính mình bắn** ở xa sẽ không bao giờ hiện — tức là mất đúng tính năng đã hứa khi
    chọn hướng server.
- **Khắc phục — bỏ khái niệm "câu trả lời vĩnh viễn"**: gộp `BLANK` và `TILES` thành một sổ
  `ANSWERED` ghi **thời điểm** trả lời. Mọi câu trả lời **hết hạn sau 10 giây**, dù rỗng hay không.
  Không có gì về mặt đất là vĩnh viễn cả.
- **Chống việc hết hạn biến thành dòng yêu cầu liên tục** (rủi ro rõ ràng của cách trên), hai lớp:
  1. **Ưu tiên cứng**: ô **chưa từng thấy** luôn được xếp trước ô **cần làm mới**. Một khung nhìn
     đầy ô cũ không bao giờ chen được chỗ của vùng đất thật sự mới.
  2. **Chỉ được quét làm mới mỗi 5 giây**, còn lấy ô mới thì vẫn mỗi 500 ms. Làm mới tiêu phần ngân
     sách còn thừa, không phải phần chính.
- **Bài học**: lưu "không có gì ở đây" **là lưu một sự thật có hạn sử dụng**, không phải một dữ kiện
  bất biến — nhất là trong thế giới mà chính người chơi tạo ra đất mới bằng cách đi tới. Bộ nhớ đệm
  cho câu trả lời phủ định phải có hạn dùng, y như câu trả lời khẳng định.
- **Bài học thứ hai**: bản sửa hiệu năng trước đó **không tạo ra** lỗi này, nó chỉ làm lỗi lộ ra.
  Khi một lỗi mới xuất hiện ngay sau một thay đổi, cần phân biệt *thay đổi gây ra lỗi* với *thay đổi
  làm lộ lỗi sẵn có* — hướng sửa của hai ca này khác hẳn nhau.
- **Trạng thái**: `gradlew build` sạch. Chờ test lại.

### 2026-08-14 — ✅ BẢN ĐỒ ĐỊA HÌNH HOÀN TẤT — verify đầy đủ end-to-end
- **Người dùng test, cả 3 mục đạt**:
  1. Bay tới vùng mới → địa hình hiện trong vài giây.
  2. Kéo bản đồ ra vùng chưa sinh rồi bay tới đó → hiện ra bình thường, không còn kẹt trống.
  3. **Bắn sập mục tiêu ở xa rồi mở tablet → hố đạn hiện ra mà không cần bay tới xem.**
- Mục 3 là thứ **không hướng nào trước đây làm được**, kể cả Xaero lẫn JourneyMap — nó chỉ khả thi vì
  dữ liệu đến từ server. Cộng với các lần test trước (địa hình bám lưới ở mọi zoom, kéo không giật,
  kéo không dính trục) thì bản đồ coi như **xong và verify đầy đủ**.
- **Tổng kết đường đi**: 5 hướng đã thử — JourneyMap `requestMapTile` (bị tác giả khuyến cáo không
  dùng), tự vẽ từ chunk client (chỉ có tầm render), lưới toạ độ thuần (người dùng loại), Xaero
  (gọi vào code nội bộ, hỏng không sửa được), và cuối cùng **tự khảo sát phía server** — hướng duy
  nhất giải được bài toán gốc: *nhìn được nơi mình chưa tới*.
- **Điều lẽ ra nên nhận ra sớm hơn**: bốn hướng đầu đều là biến thể của "đi mượn dữ liệu ai đó đã
  thu thập hộ", và đều vấp đúng một bức tường — **client không thể biết thứ nó chưa từng nhận**.
  Chỉ khi đổi câu hỏi từ *"lấy ảnh bản đồ ở đâu?"* sang *"ai thật sự nắm dữ liệu thế giới?"* thì mới
  ra được lời giải. Câu trả lời là server, và nó đã nằm sẵn ở đó suốt.

#### 📋 Việc còn lại sau khi bản đồ xong (chưa làm)
1. **Cảnh báo đạn đâm địa hình** — lý do chính khiến ta lưu cột độ cao ngay từ bản đầu. Giải được
   hạn chế treo từ Phase 6: bắn căng cự ly xa thì đạn bay là là mặt đất, đâm vào đồi giữa đường mà
   cả SBW lẫn addon đều tính là "bắn tới được". **Đây là phần có giá trị chiến thuật cao nhất.**
2. **Tự tạo SimpleChannel riêng** thay vì mượn của SBW — ID packet của ta hiện phụ thuộc số packet
   SBW đăng ký, SBW cập nhật là ID **âm thầm dịch chỗ**. Chạm ~15 file, mechanical. Phải làm trước
   khi phát hành.
3. **Gỡ JourneyMap và Xaero** — cả hai giờ là gánh nặng chết. Lưu ý: `JourneyMapSupport` đang kiêm
   luôn **bộ nhớ đệm vị trí pháo** mà chính bản đồ tablet dùng (`TabletScreen` đọc
   `lastKnownGunPosition`), nên phải tách phần đó ra trước khi xoá.
4. **Kéo bản đồ mượt dưới mức một block** ở zoom 128m (hiện nhích theo bậc ~5 px). Cần cho tâm bản
   đồ nhận số thực rồi lệch cả ba lớp theo phần lẻ. Nhỏ, chưa gấp.

### 2026-08-14 — Bước 2+3: gỡ hết mod bản đồ ngoài, tách kênh mạng riêng
- **Thứ tự cố ý là 2 → 3 → 1**: dọn dẹp trước thì bước 3 sửa ít file hơn, và làm bước 3 trước bước 1
  tránh phải chuyển tầng mạng hai lần khi tính năng đạn đạo cần thêm packet.
- **Bước 2 — gỡ JourneyMap + Xaero**:
  - Xoá `compat/journeymap/` (4 file) và `MarkTargetMessage` (chỉ JourneyMap dùng).
  - **Việc phải làm trước khi xoá được**: `JourneyMapSupport` kiêm luôn **bộ nhớ đệm vị trí pháo** mà
    chính bản đồ tablet dùng. Đã chuyển sang `TabletClientData` — đúng chỗ của nó, vì đó là nơi tập
    trung mọi câu trả lời từ server.
  - Gỡ khỏi `build.gradle`/`gradle.properties`: JourneyMap API + jar, maven repo của nó, và Xaero.
    **SBW giờ là phụ thuộc mod duy nhất của addon.**
  - Dọn kèm: nhánh "không có JourneyMap" trong `TabletScreen`, hàm `drawCentred` thành code chết,
    2 khoá dịch không còn dùng.
- **Bước 3 — `ModNetwork`, kênh SimpleChannel riêng**:
  - `NetworkRegistry.newSimpleChannel(artillerytablet:main, ...)` với `PROTOCOL_VERSION`, 14 packet,
    **ID viết tay rõ ràng** chứ không đếm bằng biến — đổi thứ tự đăng ký không thể âm thầm đánh số lại.
  - Chuyển toàn bộ 20 chỗ gửi packet sang `ModNetwork.toServer/toPlayer`.
  - **Gỡ được hai vấn đề cùng lúc**: ID không còn phụ thuộc số packet SBW đăng ký (SBW cập nhật là
    ID dịch chỗ, hỏng dạng packet méo chứ không phải lỗi nói rõ nguyên nhân), và không còn ghi chung
    vào registry của mod khác từ giai đoạn setup song song.
  - Vẫn giữ `enqueueWork` dù giờ đã an toàn — thói quen mới là thứ giữ cho nó an toàn.
- **Trạng thái**: build sạch, `runClient` boot sạch (`FATAL=0`), log xác nhận 14 packet trên kênh
  riêng, JourneyMap và Xaero đã biến mất khỏi danh sách mod.

### 2026-08-14 — Bước 1: cảnh báo đạn đâm địa hình (giải hạn chế treo từ Phase 6)
- **Bài toán**: `ReachabilityCheck` trả lời *"nòng có ngóc tới góc cần thiết không"*, hoàn toàn không
  biết gì về quả đồi nằm giữa đường. Ở cự ly xa quỹ đạo căng bay là là mặt đất nên "pháo với tới" và
  "đạn tới nơi" là **hai câu trả lời khác nhau** — và chính chỗ khác nhau đó là lý do quỹ đạo cầu
  vồng tồn tại. Chính comment trong `ReachabilityCheck` đã tự ghi đây là phần còn thiếu.
- **Thiết kế — chia đôi theo nơi dữ liệu thật sự nằm**:
  - **Đạn đạo ở SERVER**: chỉ server có pháo, sơ tốc, trọng lực và bộ giải của SBW. Client tự tính
    lại đường bay sẽ là **lần thứ ba lặp lại đúng sai lầm** đã rút bài học hai lần trong tài liệu này
    (tái hiện logic nội bộ của mod khác).
  - **Địa hình ở CLIENT**: client vốn đã giữ sẵn kho ô địa hình cho bản đồ.
  - → Server gửi **hình dạng đường bay** (`FlightProfile`: vị trí pháo + 48 mẫu độ cao dọc đường
    ngang), client tự so với đồi. Khoảng 770 byte cho 8 mục tiêu.
- **Chi tiết đáng ghi**:
  - **Mô phỏng đúng cách game di chuyển đạn** — cộng vận tốc rồi trừ trọng lực, mỗi tick một lần —
    chứ không giải parabol dạng đóng. Bộ giải của SBW giả định đúng cách bước đó; tính kiểu khác sẽ
    lệch dần khỏi nơi viên đạn thật sự bay tới.
  - Một tick bay tới 18 block nên **điền mọi mẫu mà bước nhảy vượt qua**, nội suy độ cao trong bước,
    chứ không phải mỗi tick một mẫu.
  - **Bỏ qua 4% đầu và 6% cuối**: đầu đường bay nằm trong thân xe pháo, cuối đường bay là đạn đang
    chạm mục tiêu — vốn phải chạm đất.
  - **Dung sai 2 block**: dữ liệu là một mẫu mỗi cột block, nên đạn sượt trong vòng 1 block mặt đất
    là nằm trong nhiễu; kết luận "thông thoáng" ở đó là câu trả lời chắc chắn hơn dữ liệu cho phép.
  - **Không bao giờ báo "thông thoáng" trên đất chưa khảo sát** — trả `UNKNOWN` riêng.
  - **Tự đi khảo sát hành lang bay** (`ensureLineCovered`): hỏi câu hỏi chính là thứ làm nó trả lời
    được. Hành lang xếp hàng **sau** vùng đang nhìn, nên kiểm tra đường bắn không bao giờ làm chậm
    bản đồ người chơi đang xem.
  - Chỉ hiện cảnh báo khi mục tiêu **vốn bắn tới được** — mục tiêu đã ngoài tầm thì không cần chồng
    thêm lý do thứ hai.
- **Hiển thị**: `[VƯỚNG ĐỊA HÌNH ~1200m]` trên cả hàng đợi trong tablet lẫn HUD thu gọn.
- **Trạng thái**: build sạch, `runClient` boot sạch (`FATAL=0`). **Chưa test tay.**
- **Cần người dùng kiểm**:
  1. Chọn mục tiêu bên kia một quả đồi, để **quỹ đạo Căng** → phải hiện `[VƯỚNG ĐỊA HÌNH ~...m]`,
     và con số phải khớp cỡ khoảng cách tới quả đồi đó.
  2. Cùng mục tiêu đó chuyển sang **Cầu Vồng** → cảnh báo phải **biến mất** (đạn bay qua trên đỉnh).
  3. Mục tiêu trên đồng bằng trống → **không** cảnh báo (không báo bừa).
  4. Mục tiêu ở hướng chưa từng đi qua → lúc đầu không cảnh báo, sau vài giây (khi hành lang được
     khảo sát xong) mới kết luận. Không được báo bừa lúc chưa có dữ liệu.
  5. Bắn thử một mục tiêu bị cảnh báo → xác nhận đạn đúng là đâm vào đồi chứ không tới đích.

### 2026-08-14 — Chia việc: module địa hình giao cho agent khác
- **Người dùng quyết định** tách phần bản đồ cho một agent riêng (Gemini), Claude tập trung vào các
  chức năng chính của tablet và giao diện. Đã viết bản giao ước tại
  `ATC map module/INTERFACE-CONTRACT.md`.
- **Ý kiến đã nêu thẳng với người dùng trước khi làm** (ghi lại để sau này biết đánh đổi là có ý thức):
  - Bản đồ lại đúng là phần **vừa xong và đã verify hôm nay** — bàn giao hệ thống đang chạy tốt cho
    agent phải dựng lại ngữ cảnh từ đầu là kiểu chia việc rủi ro cao nhất, lợi ích thấp nhất. Tài
    liệu này cũng đã từng ghi nhận đảo ngược đúng ý tưởng bàn giao đó ngày 2026-08-13.
  - Đường cắt "minimap" chạy **xuyên qua `MapPanel`** — file nửa UI nửa bản đồ — và packet địa hình
    nằm trong tầng mạng vừa dựng lại.
- **Đường cắt được đề xuất thay thế, và đã dùng làm ranh giới trong bản giao ước**: giao **tầng sản
  xuất dữ liệu** (`terrain/`: `TerrainTile`, `ChunkNbtSampler`, `ServerTerrainProvider`) thay vì cả
  bản đồ. Vào là thế giới + toạ độ ô, ra là màu + độ cao. Không UI, không mạng, không SBW, đúng ba
  điểm chạm. Giữ lại phía Claude: packet, cache client, `TerrainImage`, `MapPanel`, và toàn bộ điều
  khiển hoả lực.
- **Ba lỗi/điểm yếu trong code của chính tôi đã liệt kê thẳng vào bản giao ước** thay vì để agent kia
  tự mò: `stateAt` quét tuyến tính section cho từng cột; `Deflater.deflate` gọi một lần mà không lặp
  tới `finished()` (lỗi tiềm ẩn thật, chưa ai gặp); server không có cache ô nên đọc lại 16 chunk mỗi
  lần dù client làm mới mỗi 10 giây.
- Bản giao ước cũng liệt kê **4 hướng đã thử và loại** kèm lý do, vì rủi ro lớn nhất với một agent
  mới là đi lại đúng những con đường đã chết.
- **Lưu ý phối hợp**: cả hai agent cùng ghi vào mục 5 của tài liệu này. Chỉ thêm entry mới, không sửa
  xoá entry cũ, và báo người dùng nếu thấy nguy cơ giẫm chân.

### 2026-08-14 — Hoãn bàn giao, và làm lại bố cục tablet theo kiểu máy quân sự thật
- **Người dùng hoãn ý tưởng bàn giao module** vì khối lượng đã gần xong và độ phức tạp cao. Bản giao
  ước ở `ATC map module/INTERFACE-CONTRACT.md` giữ lại làm tài liệu tham khảo, chưa dùng tới.
- **Yêu cầu mới**: người dùng đưa ảnh một máy tính bảng quân sự (MilDef) — màn hình chiếm gần trọn
  mặt máy, phím chức năng nằm trên viền hai bên. Nhận xét bố cục hiện tại "hỗn loạn, không công thái
  học". **Đúng, và đo được**: dải tab trái 74px + bảng phải 148px trên mặt 480px → bản đồ chỉ còn
  **~40% diện tích**.
- **Ba quyết định người dùng đã chốt** (tôi đưa phác thảo so sánh trước khi hỏi):
  1. Tablet **lấp ~92% cửa sổ game** thay vì cố định 480×270.
  2. **Viền hẹp 30px + mã 2–3 chữ + tooltip**, tên đầy đủ hiện ở thanh trên khi mở bảng.
  3. Nội dung tab thành **bảng phủ lên bản đồ, bấm lại phím đó thì đóng**.
- **Nguyên tắc bố cục: trái = xem gì, phải = làm gì.**
  - Viền trái: 5 phím tab (MT/PH/ĐẠ/TT/NK).
  - Viền phải: **BẮN** (cao nhất, đứng đầu), chế độ bắn, quỹ đạo, phóng to, thu nhỏ, về vị trí.
  - Thanh trên 14px: tên thiết bị + toạ độ tâm bản đồ + chế độ + quỹ đạo + số pháo đã bind.
  - Thanh dưới: hàng đợi, **chỉ cao bằng số dòng thật**.
- **Thay đổi đáng ghi lại về mặt thao tác**: chế độ bắn, quỹ đạo và nút khai hoả **chuyển từ bảng ra
  viền phải**. Đó là những thứ dùng *trong lúc đang nhìn bản đồ*, nên bắt phải mở một bảng lên mới
  với tới được là sai ngay từ đầu. Giờ không cần mở bảng nào vẫn bắn được.
- **Mặc định không có bảng nào mở** → bản đồ trọn vẹn. Bảng chỉ hiện khi người chơi gọi.
- **Một cái bẫy đã xử lý trước khi nó thành lỗi**: bảng phủ **lên** bản đồ, nên vùng bấm của bảng
  phải bị loại khỏi vùng bấm của bản đồ — nếu không, với tay bấm nút trong danh sách pháo sẽ đồng
  thời **thả một mục tiêu xuống mặt đất bên dưới**.
- **Kết quả**: bản đồ từ ~40% lên **~80% diện tích**, và trên màn hình lớn thì lớn hơn nhiều lần vì
  tablet giờ giãn theo cửa sổ.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). **Chưa test tay.**
- **Cần người dùng kiểm**: (1) tablet có giãn hợp lý ở các mức GUI scale khác nhau không; (2) phím
  viền có bấm trúng và tooltip có hiện không; (3) bấm lại phím đang mở có đóng bảng không; (4) bấm
  nút trong bảng có vô tình thả mục tiêu xuống bản đồ không; (5) BẮN / chế độ / quỹ đạo trên viền
  phải có hoạt động đúng khi **không** mở bảng nào; (6) hàng đợi rỗng có thu nhỏ lại không.

### 2026-08-14 — Bố cục mới: 4 lỗi người dùng phát hiện, 4 cơ chế khác nhau
- **Người dùng test bố cục mới**: chức năng chạy ổn, nhưng báo 4 vấn đề kèm ảnh chụp. Bốn cái này
  **không cùng một gốc** như thoạt nhìn — mỗi cái một nguyên nhân riêng.
- **Lỗi 1 — bấm nút thì bản đồ tưởng đang đặt mục tiêu** (cả nút viền phải lẫn nút trong cửa sổ mờ).
  - **Nguyên nhân**: `mouseReleased` kiểm tra `overMap(pressX, pressY)`, mà `pressX/pressY` **chỉ được
    ghi khi lần nhấn đó trúng bản đồ**. Nhấn một nút thì hai biến giữ nguyên giá trị **từ lần nhấn
    bản đồ trước đó** → thả chuột trên nút vẫn thoả điều kiện và đặt mục tiêu. Không phải hitbox
    tràn như phỏng đoán ban đầu — hitbox đúng, cái sai là **trạng thái cũ còn sót lại**.
  - **Khắc phục hai lớp**: (a) thêm cờ `mapPress` ghi rõ "lần nhấn này có thật sự vào bản đồ không",
    dùng nó thay vì suy ra từ toạ độ cũ; (b) **cho widget quyền ưu tiên tuyệt đối** — gọi
    `super.mouseClicked` **trước**, bản đồ chỉ nhận những cú click không ai nhận. Trước đây bản đồ
    chặn click trước cả widget.
- **Lỗi 2 — đổi đặt/xoá mục tiêu sang chuột phải** (người dùng đề xuất). Đã làm: **chuột trái chỉ
  kéo bản đồ và bấm nút; chuột phải đặt mục tiêu, bấm lại lên mục tiêu đã có thì xoá**. Một nút làm
  cả hai chiều — chính là cách bản đồ JourneyMap từng dùng trước đây, nên thao tác không lạ.
  Việc tách hẳn hai nút cũng **loại bỏ tận gốc cả lớp lỗi nhầm lẫn** chứ không chỉ vá lỗi 1.
- **Lỗi 3 — số toạ độ lưới đè lên cửa sổ mờ.** Cửa sổ được vẽ **sau** bản đồ, lẽ ra phải che.
  - **Nguyên nhân**: Minecraft gom chữ vào một render type riêng và vẽ **sau toàn bộ ô màu**, bất kể
    thứ tự gửi vào. Nên chữ của bản đồ luôn nổi lên trên mọi thứ vẽ sau nó — và tệ hơn, nó cũng
    **thoát khỏi vùng scissor** vì scissor đã tắt trước lúc chữ được vẽ thật.
  - **Khắc phục**: `g.flush()` **trong lúc scissor còn hiệu lực** (cuối `MapPanel.render`), và
    `g.flush()` lần nữa trước khi vẽ cửa sổ mờ.
- **Lỗi 4 — chữ lệch hàng so với nút tương ứng** (thấy rõ ở tab Pháo và Đạn).
  - **Nguyên nhân**: `buildPanel` dựng nút từ **đỉnh cửa sổ**, còn `renderPanel` vẽ chữ từ **dưới
    dòng tiêu đề** — lệch nhau đúng chiều cao tiêu đề, 12px, ở mọi hàng.
  - **Khắc phục**: một hằng số `PANEL_TITLE` dùng chung, cả hai bên xuất phát từ cùng một gốc. Đây là
    lỗi kiểu "hai chỗ tính cùng một thứ theo hai cách" — đã đưa vào hằng số để không tái diễn.
- **Bài học**: bốn triệu chứng trông như một vấn đề bố cục, nhưng gồm một lỗi **trạng thái sót lại**,
  một **quyết định thiết kế thao tác**, một đặc tính **thứ tự vẽ của engine**, và một lỗi **hai nguồn
  sự thật cho cùng một toạ độ**. Nếu gom chung mà "sửa bố cục" thì không cái nào được sửa đúng.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). Chờ test lại.

### 2026-08-14 — Số lưới vẫn đè: bỏ hẳn cách dựa vào thứ tự vẽ. Cùng đợt: phong cách HUD
- **Người dùng test**: lỗi đặt mục tiêu đã hết ✅, chuột phải đặt/xoá chạy ✅, chữ đã thẳng hàng ✅,
  nhưng **số lưới VẪN đè lên cửa sổ mờ**.
- **Vì sao bản sửa trước thất bại**: tôi chèn `g.flush()` để ép thứ tự vẽ. Nhưng cách đó **phụ thuộc
  vào chi tiết nội bộ của engine** mà tôi không kiểm soát: chữ nằm ở render type riêng, thứ tự vẽ
  giữa các render type do Minecraft quyết định, và `flush()` còn tự tắt depth test bên trong. Sửa
  bằng cách *cố ép một hành vi mình không nắm chắc* là sai cách tiếp cận, không chỉ sai chi tiết.
- **Khắc phục — đổi hẳn cách nghĩ**: thay vì vẽ nhãn rồi cố che đi, **báo cho bản đồ biết vùng nào đã
  bị chiếm và đừng vẽ nhãn vào đó** (`MapPanel.reserve`). Không vẽ ra là hành vi mà **không chi tiết
  hiển thị nào có thể lật ngược được**.
- **Bài học**: khi cách sửa của mình dựa trên "thứ tự vẽ chắc là thế này", đó là dấu hiệu nên đổi
  sang cách không cần biết thứ tự. Đây là biến thể của bài học Phase 6 (quan sát trạng thái thay vì
  tái hiện điều kiện), áp vào tầng hiển thị.
- **Lỗi 2 — không đóng được cửa sổ từ trong tablet**: lỗi do chính tôi. Comment ghi "phím vẫn bấm
  được để đóng" nhưng code lại `key.active = false` cho đúng phím đang mở. Giờ mọi phím luôn bấm
  được, **và** thêm nút `x` ngay cạnh tiêu đề cửa sổ — bấm lại phím viền là thứ phải có người chỉ mới
  biết, dấu `x` thì không.
- **Đề xuất 1 — bỏ SIN/SAL/RIP**: gộp thành **một phím `MOD`**, tên chế độ đầy đủ đọc ở thanh trên và
  ở tooltip. Ba chữ viết tắt vừa khó đọc vừa không cần thiết khi đã có chỗ ghi đầy đủ.
- **Đề xuất 3 — giải thích ký hiệu thanh trên**: hai số là **toạ độ tâm bản đồ** (đổi khi kéo bản đồ,
  không phải vị trí người chơi), số cuối là **số pháo đã ràng buộc**. Đã gắn nhãn thẳng vào:
  `TÂM 2323 3515 · RIPPLE · Căng · PHÁO 1`. Một dãy số không nhãn thì người đọc phải giải mã chứ
  không phải liếc.
- **Đề xuất 4 — phong cách nút**: thêm `HudButton`, bỏ texture nút vanilla. Nền `#0a1410` hơi trong,
  viền 1px `#38ef7d`, chữ `#55ff99`; **hover thì đảo hẳn** — nền xanh đậm đặc, chữ trắng. Chọn đảo
  màu thay vì chỉ sáng lên vì trên mặt dày đặc thế này hover mờ rất dễ bỏ sót, mà bấm nhầm phím trên
  thiết bị chỉ huy hoả lực không phải lỗi nhỏ. Ô nhập toạ độ cũng bỏ khung vanilla, dùng viền mỏng
  cùng bộ. Nút và hàng thu nhỏ lại để lấy thêm chỗ.
- **Giới hạn đã biết về "scale chữ"**: cỡ chữ Minecraft là bitmap cố định, không thu nhỏ mượt được.
  Tôi đã giảm kích thước nút và khoảng cách hàng; muốn chữ nhỏ hơn nữa so với bản đồ thì phải hạ
  **GUI Scale** trong cài đặt game.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). Chờ test lại.

### 2026-08-14 — Font JetBrains Mono + bảng màu tactical hiện đại, bỏ tiếng Việt
- **Người dùng quyết định**: đổi phong cách từ xanh lục retro sang tactical hiện đại, dùng font
  **JetBrains Mono**, giao diện **chỉ tiếng Anh**.
- **Font — không cần thiết kế ngoài.** Hệ font Minecraft là dữ liệu: khai báo provider `ttf` trong
  `assets/<ns>/font/<tên>.json` trỏ tới file `.ttf` đóng gói cùng mod. Đã xác minh lớp
  `TrueTypeGlyphProvider` **có thật trong jar 1.20.1** trước khi làm (grep trên mục lục jar, vì công
  cụ chạy lệnh lúc đó tạm hỏng).
- **Đã thêm**: `tactical.json` (Medium) và `tactical_bold.json` (Bold), cỡ 9.0, oversample 4.
- **Chi tiết quan trọng về API**: **font chỉ gắn được vào `Component`, không gắn được vào `String`.**
  Chuỗi trần không mang style nên sẽ **âm thầm rơi về font mặc định của Minecraft** — đúng cái lệch
  mà việc này sinh ra để loại bỏ. Vì vậy mọi lời gọi vẽ chữ (34 chỗ, 5 file) đi qua
  `TabletTheme.draw`, và mọi phép đo bề rộng đi qua `TabletTheme.width` — đo bằng font vanilla trong
  khi vẽ bằng font khác thì căn lề sẽ sai ở mọi chỗ.
- **Bảng màu — nguyên tắc: màu mang nghĩa, không trang trí.** Bảng cũ để mọi thứ cùng một sắc xanh
  lục, nên **không gì nổi bật được** — một cảnh báo trông y hệt một nhãn toạ độ. Trên thiết bị chỉ
  huy hoả lực đó là khuyết điểm thật.
  | Vai trò | Màu | Ý nghĩa |
  |---|---|---|
  | Khung/mặt/viền | `#0F1216` `#171C22` `#2A333D` | xám trung tính, đứng ngoài đường |
  | Xanh dương `#4DA3FF` | quân ta | pháo đã bind, phím đang trỏ, người chơi |
  | Đỏ `#FF5A52` | đối phương | mục tiêu |
  | Hổ phách `#FFB454` | cần quyết định | ngoài tầm, vướng địa hình, gần quân ta |
  | Xanh lục `#5FD08A` | sẵn sàng | chỉ còn nghĩa "ổn", không còn là màu của mọi thứ |
  Theo quy ước ký hiệu quân sự (APP-6/2525) mà ATAK và SitaWare dùng, nên đọc đúng như người dùng
  quen. Gom hết vào `TabletTheme` thay vì rải hằng số khắp nơi.
- **Hover của phím đổi theo**: nền xanh dương đặc + chữ tối, thay cho xanh lục.
- **Lỗi tự gây rồi tự bắt**: tôi dặn người dùng giữ nguyên tên `OFL.txt`, quên rằng đường dẫn tài
  nguyên không nhận chữ hoa → log báo `Invalid path in pack: artillerytablet:font/OFL.txt, ignoring`.
  Chuyển ra gốc jar thành `LICENSE-JetBrainsMono.txt` — vừa hết cảnh báo, vừa đúng chỗ quy ước cho
  giấy phép đi kèm. (OFL **bắt buộc** kèm bản giấy phép khi phân phối lại.)
- **Đã xoá `vi_vn.json`**, `en_us.json` là nguồn duy nhất.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, `Invalid path` = 0, không
  lỗi nào liên quan font/glyph). **Chưa test tay.**
- **Hai số dễ chỉnh nếu nhìn không vừa mắt**, đều một dòng: `size` trong hai file JSON font (đang
  9.0) và `FILL_HOVER`/`LABEL_HOVER` trong `HudButton`.

### 2026-08-14 — Chốt hướng UI theo Anduril Lattice (mới thiết kế, CHƯA code)
- **Người dùng đưa ảnh Lattice** (phần mềm chỉ huy UAV của Anduril) làm tham chiếu, và yêu cầu bàn
  thiết kế trước khi code. Lập luận của người dùng: *"thiết kế từ một công ty quốc phòng chuyên
  nghiệp luôn tốt hơn ngồi tự mò"* — đúng, nhưng cái đáng học **không phải màu sắc mà là cách tổ
  chức thông tin**.
- **Bốn nguyên tắc rút ra từ ảnh**:
  1. **Bản đồ tràn viền, bảng nổi đè lên** — không đóng khung bản đồ giữa hai cột.
  2. **Ba vùng vai trò rõ rệt**: trái = *cái gì đang tồn tại*, phải = *chuyện gì đang xảy ra và làm
     gì tiếp*, dưới = *chi tiết thứ đang chọn*, trên = chuyển chế độ.
  3. **Mỗi dòng là một thẻ có cấu trúc**, 4 tầng thông tin (loại → tên → phân loại → dữ liệu) trong
     một khối ~40px, không phải một dòng chữ.
  4. **Màu chỉ dành cho trạng thái.** Đáng chú ý: **nút hành động của Lattice không có màu**, chỉ
     viền xám; màu dành riêng cho *từ trạng thái*. Kỷ luật hơn bản màu tôi vừa làm → sẽ bỏ nền xanh
     khi hover, chỉ sáng viền.
- **Ánh xạ mạnh nhất: "Tasks" của Lattice ≡ "lệnh bắn" của ta.** Cùng cấu trúc: một đơn vị đang thực
  hiện việc gì đó, có trạng thái, có đếm ngược, có nút can thiệp. **Đây là vùng chưa từng tồn tại
  trong tablet** — hiện lệnh bắn đang chạy bị giấu sau tab Trạng Thái, tức thứ *đang diễn ra ngay
  lúc này* lại khó thấy nhất.
- **Bốn vấn đề đã nêu thẳng với người dùng trước khi chốt**:
  1. **Diện tích** — Lattice chạy 1920×1080; tablet ta ở GUI scale 3 chỉ ~640×360 điểm ảnh logic.
     Hai bảng như bản gốc chiếm 350px, còn ~290px cho bản đồ, **tệ hơn bố cục hiện tại**. Không sao
     chép nguyên chiều rộng được.
  2. **Chưa có khái niệm "đang chọn"** — toàn bộ bố cục Lattice xoay quanh nó. Thêm vào là đổi hành
     vi, không phải đổi hình.
  3. **Danh sách sẽ tràn** — thẻ 3–4 dòng, 8 mục tiêu không vừa, cần cuộn (widget chưa có).
  4. **Không có bộ icon** — vẽ được vài hình cơ bản bằng code, không có bộ đầy đủ.
- **Người dùng chốt (2026-08-14)**:
  - **Bảng phủ đè lên bản đồ, và tự thu về 0 khi rỗng** — bảng phải biến mất hoàn toàn khi không có
    lệnh bắn, bảng trái mặc định đóng. Đây là cách duy nhất giữ được mật độ kiểu Lattice trên màn
    hình nhỏ.
  - **Có thêm khái niệm "mục tiêu đang chọn"**, làm ở đợt B.
- **Chia ba đợt, cố ý không đập hết một lượt** vì bố cục hiện tại đang chạy được:
  | Đợt | Nội dung |
  |---|---|
  | **A** | Thanh trên có tên đầy đủ (bỏ viết tắt TGT/BTY/AMM) + hàng đợi thành thẻ ở bảng trái |
  | **B** | Bảng Fire Missions bên phải + "đang chọn" + thanh chi tiết — **giá trị cao nhất** |
  | **C** | Cuộn danh sách, marker, tinh chỉnh mật độ |
- **Trạng thái**: mới thiết kế, **chưa viết dòng code nào**. Đợt A là việc tiếp theo.

### 2026-08-14 — ✅ Cảnh báo đạn đâm địa hình HOÀN TẤT + Đợt A của bố cục Lattice
- **Người dùng test cảnh báo đạn đạo: cả 5 mục đạt.** Hạn chế treo từ Phase 6 — *"không mô phỏng địa
  hình trên đường đạn bay"* — **đã giải quyết xong**. Đây cũng là phần thu hồi được khoản đầu tư từ
  quyết định lưu cột độ cao ngay từ bản đầu của bản đồ, dù lúc đó chưa ai dùng tới nó.
- **Đợt A đã làm**:
  - **Bỏ hẳn viền trái**. 5 phím `TGT/BTY/AMM/STA/LOG` thành **mục có tên đầy đủ trên thanh trên**,
    mục đang mở được gạch chân màu nhấn thay vì trông như bị ấn lõm. Gỡ đúng thứ người dùng phàn nàn
    (viết tắt khó hiểu) **và** trả lại 30px bề ngang cho bản đồ.
  - **Bỏ dải hàng đợi dưới đáy**. Mục tiêu thành **thẻ trong bảng trái**: dòng trạng thái, toạ độ ô
    lưới, cự ly + phương vị, kèm nút Bắn/xoá của riêng nó.
  - **Trạng thái viết bằng chữ, không phải bằng màu**: `READY`, `MIN RANGE`, `MAX RANGE`,
    `USE LOFTED`, `TERRAIN 1200m`. Dòng đỏ chỉ nói "có gì đó sai"; *"dưới tầm tối thiểu"* và
    *"vướng địa hình ở 1200m"* là **hai vấn đề khác nhau với hai cách xử lý khác nhau** — cái thứ hai
    sửa bằng đổi quỹ đạo chứ không phải dời mục tiêu.
  - **Thanh trên thành các cặp nhãn–giá trị**: `CTR`, `MODE`, `ARC`, `GUNS`, `TGT`. **Tự bỏ bớt cặp
    từ bên phải** nếu tên các mục không còn chỗ — nhãn đè lên menu tệ hơn là thiếu một con số mà
    người chơi tra được ở nơi khác.
  - **Nút bỏ nền màu khi hover, chỉ sáng viền.** Theo kỷ luật của Lattice: tô nền nút khi rê chuột
    biến con trỏ thành thứ sáng nhất màn hình, trong khi màu lẽ ra chỉ để chỉ trạng thái.
  - **Bản đồ giờ chạy tới cả hai mép** trái và dưới, chỉ còn viền phải cho phím hành động.
- **Đã xoá code chết**: `renderQueue`, `buildQueueControls`, `queueHeight`, `buildLeftBezel` — không
  để lại nhánh cũ.
- **Giới hạn đã biết, để đợt C**: danh sách chưa cuộn được. Thẻ cao 36px nên khoảng 5–6 mục tiêu là
  vừa màn; quá thì hiện `+N more`. Trung thực còn hơn cắt cụt im lặng, nhưng vẫn cần cuộn.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không lỗi tài nguyên, không
  thiếu khoá dịch). **Chưa test tay.**
- **Cần người dùng kiểm**: (1) menu trên bấm đúng, gạch chân đúng mục đang mở; (2) bấm lại mục đang
  mở thì đóng bảng; (3) thẻ mục tiêu hiện đúng trạng thái và cự ly/phương vị; (4) nút Bắn/xoá trên
  thẻ hoạt động và **không** thả mục tiêu xuống bản đồ; (5) thanh trên tự bỏ bớt cặp khi hẹp thay vì
  đè chữ; (6) bản đồ có thật sự rộng ra tới mép trái và mép dưới không.

### 2026-08-14 — Đợt B: bảng Fire Missions, chọn mục tiêu, thanh chi tiết, và huỷ lệnh bắn
- **Người dùng test đợt A xong, cho làm tiếp đợt B.**
- **Bảng Fire Missions (phải)** — vùng chưa từng tồn tại. Mỗi lệnh một thẻ: tên pháo, trạng thái
  (`AIMING` / `HOLDING` / `IN FLIGHT` / `ABORTED`), đếm ngược chạm đất, và phím huỷ.
  **Không có lệnh nào thì bảng không vẽ gì cả** — đúng quyết định "tự thu khi rỗng", và đó là thứ
  khiến bố cục mật độ cao kiểu Lattice nhét vừa màn hình nhỏ hơn nhiều lần bản gốc.
- **Huỷ lệnh bắn — tính năng mới, không chỉ là giao diện.** Trước đây cách duy nhất dừng một khẩu là
  giao cho nó mục tiêu khác, câu trả lời tồi khi điều đúng cần làm là **đừng bắn nữa**.
  - **Không phải viết cơ chế mới**: `ORDER_GEN` đã có từ Phase 3 lần sửa 4. Một lệnh mới tăng bộ đếm
    và mọi callback đang chờ tự kiểm tra bộ đếm trước khi làm gì. **Huỷ chỉ là cú tăng đó mà không
    có lệnh nào theo sau** — khẩu pháo dừng lại mà không có gì phải gỡ ngược.
  - Packet `AbortMissionMessage` (C2S), **kiểm phía server rằng khẩu đó thật sự thuộc tablet đang
    cầm** — thiếu chốt này thì client sửa đổi có thể bắt cả pháo của người khác đứng im.
- **Khái niệm "mục tiêu đang chọn"**:
  - Bấm thân thẻ để chọn (vùng bấm phủ cả thẻ, chọn không cần nhắm vào cái gì cụ thể); phím Bắn/xoá
    nằm riêng và không bị nuốt.
  - Thẻ đang chọn đổi nền + có viền; **marker trên bản đồ đeo khung** để danh sách và bản đồ luôn nói
    cùng một thứ.
  - **Phím BẮN giờ bắn mục tiêu đang chọn**, không còn luôn là mục đầu hàng đợi.
  - **Chốt an toàn**: xoá một mục tiêu làm mọi chỉ số sau nó dịch lên, nên chỉ số cũ sẽ trỏ vào thứ
    vừa trám vào chỗ trống — tức phím bắn nhắm sai toạ độ. Đã kẹp lại chỉ số ở mỗi lần dựng giao diện.
- **Thanh chi tiết (dưới)**: chỉ hiện khi có mục tiêu được chọn, phủ lên bản đồ chứ không đẩy. Các
  trường đúng thứ pháo thủ đọc trên bảng tác xạ: ô lưới, cự ly/phương vị, quỹ đạo, tình trạng. Tự bỏ
  bớt trường nếu hết chỗ.
- **`MapPanel.reserve` giờ nhận nhiều vùng** — bảng trái và cột lệnh bắn có thể cùng mở, cả hai đều
  phải được chừa nhãn lưới.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`), 15 packet.
  **Chưa test tay.**
- **Cần người dùng kiểm**: (1) bắn xong cột phải hiện ra, hết lệnh thì biến mất hoàn toàn; (2) đếm
  ngược chạm đất chạy đúng; (3) **bấm Abort thì pháo dừng thật** — không xoay tiếp, không bắn;
  (4) bấm thẻ để chọn, marker tương ứng trên bản đồ đeo khung; (5) phím BẮN bắn đúng mục tiêu đang
  chọn; (6) xoá mục tiêu đang chọn xong bấm BẮN **không** bắn nhầm mục tiêu khác; (7) thanh chi tiết
  chỉ hiện khi có chọn.

### 2026-08-14 — Đợt B test: 5/7 đạt, 2 lỗi thật + yêu cầu đổi hướng UI toàn diện
- **Người dùng test đợt B**: mục 2, 4, 5, 7 đạt. Mục 1 chạy được nhưng **giao diện quá vướng víu**.
  Mục 3 và 6 là lỗi thật.
- **Lỗi 3 — nút Abort hiện quá muộn, xuất hiện khi pháo đã bắn xong nên vô dụng.**
  - **Nguyên nhân**: thẻ lệnh bắn **vẽ** mỗi khung hình nên hiện ngay, nhưng nút Abort là **widget**,
    mà widget chỉ được dựng trong `rebuild()`. Cập nhật lệnh bắn đến bằng packet riêng, **không** đụng
    `TabletClientData.version()` cũng không đụng NBT item — hai thứ duy nhất kích hoạt rebuild. Nên
    nút chỉ xuất hiện khi có việc *không liên quan* vô tình gọi rebuild.
  - **Khắc phục**: thêm chữ ký trạng thái lệnh bắn (id pháo + trạng thái), đổi thì rebuild.
  - **Đây là biến thể thứ hai của cùng một lỗi** đã gặp ở "Bước 1 sửa lỗi: nút không hiện" — phần vẽ
    đọc dữ liệu ngay lúc vẽ nên luôn đúng, phần dựng nút thì không ai gọi lại. **Bài học lặp lại**:
    thêm một nguồn dữ liệu mới thì phải nối nó vào cơ chế dựng lại giao diện, không chỉ vào phần vẽ.
- **Lỗi 6 — xoá mục tiêu thì pháo tự nhảy sang mục tiêu khác.**
  - **Nguyên nhân là chốt an toàn tôi tự thêm ở đợt B**: khi chỉ số đang chọn vượt quá danh sách, tôi
    kẹp về mục cuối. Ý định là chống trỏ vào chỗ trống, nhưng hệ quả là **phím BẮN âm thầm đổi nghĩa
    sang một toạ độ khác** với thứ người chơi đang nhìn.
  - **Khắc phục**: xoá thứ đang chọn thì **bỏ chọn hẳn**; xoá thứ đứng trước thì lùi chỉ số một bậc.
  - **Bài học**: một chốt an toàn "cho chắc" có thể tự nó là hành vi sai. Ở thiết bị bắn, **không
    chọn gì** luôn an toàn hơn **chọn hộ một thứ khác**.
- **Sửa kèm — số lưới vẫn lọt lên bảng**: bảng đặt cách mép trên bản đồ 6px, nhãn trục X vẽ ở +2px,
  nên nhãn nằm lọt trong khe hở đó và thoát khỏi vùng chừa. Khe 6px vốn không đủ chứa một nhãn, nên
  vùng chừa giờ kéo thẳng lên mép trên bản đồ.
- **Trạng thái**: `gradlew build` sạch. Chờ test lại.

#### 🔧 Yêu cầu mới của người dùng (mục 8): thoát khỏi cảm giác giao diện Minecraft
Người dùng muốn **thiết kế UI bên ngoài rồi đưa vào game**, chữ/bố cục/số phải tự nhiên như phần mềm
thật, không bị pixel hoá; sẵn sàng tự dựng model 3D tablet bằng Blockbench. Đã phân tích và trả lời
riêng — chưa code. Tóm tắt kết luận để phiên sau không phải phân tích lại:
- **Nguyên nhân pixel hoá là GUI Scale**, không phải font. Minecraft vẽ giao diện ở "điểm ảnh logic"
  rồi **phóng to 2–4 lần**. Mọi thứ ta vẽ đều bị phóng theo.
- **Cách thoát**: vẽ ở **điểm ảnh vật lý** (`pose().scale(1/guiScale)`), font TTF cỡ lớn hơn. Được
  chữ sắc nét thật, **gấp 3–4 lần diện tích bố cục**, và bản đồ lấy mẫu 1:1 thay vì bị phóng.
- **Cái giá**: widget vanilla dựng và bắt chuột theo toạ độ logic, nên **phải tự viết lớp nút + bắt
  chuột riêng**. Bỏ hẳn widget của Minecraft.
- **"Thiết kế bên ngoài"** thực tế = ảnh PNG độ phân giải cao làm nền (vẽ ở Figma/Photoshop tuỳ ý) +
  chữ và số vẽ động đè lên. **HTML/CSS không dựng được** trong MC nếu không nhúng trình duyệt (MCEF)
  — nặng, mong manh, không đáng.
- **Model 3D tablet**: nên làm, nhưng cho **vật phẩm cầm trên tay/trong thế giới**, không phải cho
  thao tác. Vẽ UI sống lên mặt model được (render-to-texture) nhưng bấm vào nó phải raycast lên mặt
  phẳng, và đọc màn hình nghiêng thì tệ hơn giao diện phẳng. Tách hai việc.

### 2026-08-14 — Vẽ giao diện ở ĐIỂM ẢNH VẬT LÝ: gốc rễ của cảm giác "giao diện Minecraft"
- **Người dùng làm rõ yêu cầu**: không cần bo góc, không cần trang trí — cần **mọi thứ scale đúng,
  chữ nằm giữa ô, bố cục vừa mắt**. Đó là chuyện thủ pháp, và nó gộp lại thành đúng ba việc.
- **Nguyên nhân gốc, đã xác định rõ**: Minecraft dựng giao diện bằng "điểm ảnh logic" rồi **phóng to
  2–4 lần**. Một đường kẻ 1px thành **3 điểm ảnh thật** ở GUI scale 3; font TTF nướng cho 9px bị kéo
  ra 27px. **Không phải lỗi font, không phải lỗi bảng màu** — là phép phóng.
- **Cách làm**: vẽ trong ma trận đã nhân `1/guiScale`, toàn bộ bố cục tính bằng **điểm ảnh thật**.
  Đường kẻ 1px lại là 1 điểm ảnh, font nướng đúng cỡ hiển thị, và bố cục có **gấp 3–4 lần diện tích**.
- **Cái giá đã trả**: widget của Minecraft không đi theo được — chúng vẽ ngoài ma trận đó và bắt chuột
  theo toạ độ logic, nên sẽ hiện ở **1/3 kích thước, sai vị trí**. Đã tự viết `UiButton` và `UiField`
  thay thế, bỏ hẳn `Button`/`EditBox` của vanilla. Đây không phải phức tạp thừa — nó **chính là** thứ
  mua được kết quả nét.
- **Ba bẫy đã xử lý**:
  1. **Chuột**: `mouseClicked/Dragged/Scrolled` nhận toạ độ logic, phải nhân `guiScale` trước khi so
     với bố cục.
  2. **Scissor không theo ma trận.** Minecraft tự tính vùng cắt từ cửa sổ và scale của nó, nên đưa
     toạ độ thật vào sẽ cắt vùng lớn gấp mấy lần. Đã thêm `Ui.scissor` quy đổi ngược. **Quên chỗ này
     thì không có lỗi gì cả — chỉ là im lặng không cắt.**
  3. **Tooltip vẽ ngoài ma trận**, vì Minecraft đặt nó theo toạ độ cửa sổ của riêng nó.
- **Căn chữ — thứ người dùng nêu đích danh**: gom về **một hàm duy nhất** `Ui.textIn/textCentred/
  textRight`, căn giữa theo `Ui.TEXT_HEIGHT`. Trước đây mỗi chỗ gọi tự đoán một con số (`(h-7)/2`,
  `(h-8)/2`), nên chữ lệch mỗi nơi một kiểu. **`TEXT_HEIGHT` giờ là con số duy nhất cần chỉnh** nếu
  chữ trông cao hoặc thấp.
- **Thang khoảng cách** `Ui.GAP_XS/SM/MD/LG` (4/8/14/22) thay cho các số tuỳ tiện rải khắp nơi.
- **Font**: `size` 9 → **13** (nướng đúng cỡ hiển thị), `oversample` 4 → **2** (ở tỉ lệ 1:1 thì 4 chỉ
  phí bộ nhớ atlas).
- **Sửa kèm**: tablet **xin danh sách pháo ngay khi mở**. Trước đây cự ly/phương vị trên thẻ mục tiêu
  hiện `--` cho tới khi người chơi tình cờ mở tab Pháo, vì đó là chỗ duy nhất client biết pháo ở đâu.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không lỗi font). **Chưa test
  tay.** Đây là thay đổi lớn nhất về hiển thị từ trước tới nay — mọi hằng số bố cục đã nhân ~2,5 lần.
- **Cần người dùng kiểm**: (1) chữ có **nét** không, còn cảm giác pixel không; (2) chữ có nằm **giữa
  ô** trong các nút không; (3) bấm có **trúng** không (đây là chỗ dễ sai nhất — toạ độ chuột đổi hệ);
  (4) ô nhập toạ độ gõ/xoá được không; (5) kéo và zoom bản đồ còn đúng không; (6) bản đồ có bị cắt
  sai vùng không; (7) tooltip hiện đúng chỗ con trỏ không; (8) thử **đổi GUI Scale** trong cài đặt —
  tablet phải trông **gần như không đổi**, vì giờ nó không còn phụ thuộc vào scale đó nữa.

### 2026-08-14 — Sửa ngay: đổi sang điểm ảnh vật lý nhưng quên nhân phần lớn số trong code
- **Người dùng test, hai lỗi nghiêm trọng**: (a) trong tablet mọi thứ **quá bé**; (b) chữ ở HUD thu
  gọn **rỗ nặng**.
- **Lỗi (a) — nguyên nhân**: tôi nhân các hằng số **khung chứa** (`BEZEL`, `PANEL_WIDTH`,
  `CARD_HEIGHT`…) nhưng **quên hàng loạt số viết thẳng trong thân hàm** — chiều cao phím vẫn là
  `20/14/12` của thời điểm ảnh logic. Khung to ra gấp 2,5 lần còn nút và chữ thì không, nên tỉ lệ vỡ.
  **Bài học**: đổi hệ đơn vị thì phải quét *mọi* con số, không chỉ những cái đã đặt tên. Hằng số có
  tên thì dễ thấy; số viết thẳng trong lời gọi mới là chỗ sót.
- **Lỗi (b) — nguyên nhân**: `TabletHudOverlay` **vẫn vẽ ở không gian logic**. Font vừa tăng cỡ cho
  điểm ảnh thật nên ở đó nó bị phóng 3 lần — **tệ hơn cả trước khi đổi font**. Đã chuyển HUD sang
  cùng ma trận điểm ảnh vật lý.
- **Đã sửa**: font 13 → **17**, oversample 2 → **4**, `Ui.TEXT_HEIGHT` 10 → **13**; toàn bộ chiều cao
  phím viền phải, thanh trên, nút đóng, và mọi khoảng cách trong thanh trên chuyển sang thang
  `Ui.GAP_*`; HUD dùng chung `Ui.textIn` và có `LINE_HEIGHT` 22.
- **Trạng thái**: build sạch. Chờ test lại — cỡ chữ và cỡ nút giờ là thứ cần đánh giá bằng mắt.

### 2026-08-14 — Kết phiên: tóm tắt bàn giao
- Phiên này sắp hết ngân sách ngữ cảnh. Đã viết lại toàn bộ mục **"▶ BẮT ĐẦU TỪ ĐÂY"** ở đầu tài
  liệu để phản ánh đúng trạng thái hiện tại — phiên mới **đọc mục đó trước**, mục 5 chỉ để tra lý do.
- **Trạng thái treo**: toàn bộ công việc ngày 2026-08-14 **chưa commit**. Git mới tới
  `0157ca8 Rebuild the UI as one integrated tablet`. Hỏi người dùng trước khi commit.
- **Việc đầu tiên của phiên sau**: người dùng test 8 mục của bản vẽ-ở-điểm-ảnh-vật-lý. Không có gì
  để code thêm cho tới khi có kết quả đó — đây là thay đổi hiển thị lớn nhất từ trước tới nay và
  chạm vào cả hệ toạ độ chuột, nên rất có thể sẽ phải sửa vài chỗ.
- **Hai núm chỉnh nhanh** nếu người dùng chê chữ, đã ghi ở đầu tài liệu: `Ui.TEXT_HEIGHT` (chữ lệch
  trên/dưới trong ô) và `size` trong hai file JSON font (chữ to/nhỏ).
- **Đã commit**: `bf560cb` trên nhánh `server-terrain-map-and-tablet-ui`, 44 file, +3253/−1090.
  Cây làm việc sạch. `master` vẫn ở `0157ca8` — merge được bằng fast-forward khi người dùng muốn.
  Dọn kèm trước khi commit: xoá `HudButton` (đã bị `UiButton` thay thế hoàn toàn), thêm `bin/` vào
  `.gitignore`.

### 2026-08-14 — Sửa lỗi nén `TerrainTile` còn treo + đưa tài liệu vào git
- **Bối cảnh**: phiên mới đọc lại toàn bộ tài liệu. Hai việc lộ ra, cả hai đều là **nợ đã biết mà
  chưa ai trả**, không phải phát hiện mới.
- **Lỗi `Deflater` — được ghi nhận ngày 2026-08-14 rồi bị bỏ quên.** Nó nằm trong danh sách 3 điểm
  yếu tôi tự liệt kê vào `INTERFACE-CONTRACT.md` cho agent nhận module địa hình. Bàn giao bị huỷ vài
  giờ sau đó, và **lỗi ở lại cùng với bản giao ước không ai dùng**. Đây là cách một lỗi đã biết biến
  mất: nó được ghi vào đúng chỗ cho đúng người, rồi người đó không tới.
  - **Lỗi thật**: `TerrainTile.write` gọi `deflater.deflate(packed)` **đúng một lần** vào buffer bằng
    kích thước đầu vào (12288 byte), không kiểm `finished()`. Một lời gọi `deflate()` chỉ hứa lấp đầy
    buffer được đưa cho, **không hứa đã nén xong**. Đất nén kém có thể vượt buffer → ô bị **cắt cụt
    âm thầm phía server**.
  - **Triệu chứng nếu nổ**: phía đọc *có* kiểm (`read` so số byte giải nén với kích thước mong đợi)
    nên nó **ném ngoại lệ ở client** chứ không âm thầm vẽ địa hình sai — nhẹ hơn nhiều so với lo ngại
    ban đầu. Nhưng lỗi sẽ hiện ra dưới dạng "một ô bản đồ mất vĩnh viễn kèm exception khó hiểu", ở
    **cách xa nguyên nhân thật** (bên kia đường truyền, trong mã giải nén).
  - **Khắc phục**: lặp `deflate(packed, size, packed.length - size)` tới khi `finished()`, nhân đôi
    buffer khi đầy. Nới buffer **trước** khi gọi nên luôn còn chỗ trống → không thể lặp vô hạn.
- **Vì sao "build sạch + boot sạch" không bao giờ bắt được nó**: đây là lỗi chỉ nổ ở **một đầu của
  dải dữ liệu** (đất gần như không nén được), mà mọi lần chạy thử đều dùng đất bình thường nén rất
  tốt. Cùng dạng với lỗi `max(1.0, ...)` ở bản đồ — **hằng số phòng thủ sai chỉ lộ ở một nửa dải
  tham số**. Một test roundtrip với dữ liệu ngẫu nhiên bắt được ngay lần đầu; **dự án hiện chưa có
  test tự động nào** và đây là bằng chứng cụ thể cho cái giá của việc đó.
- **Đưa tài liệu vào repo**: chuyển từ `FDC/artillery-tactical-tablet-plan.md` sang
  `ArtilleryTacticalTablet/docs/`. **Chuyển hẳn, không copy** — hai bản sẽ lệch nhau, và một tài
  liệu lệch còn tệ hơn không có tài liệu. Giờ nó có lịch sử, đi cùng nhánh với code nó mô tả, và
  không mất theo một lệnh xoá nhầm.
- **Sửa kèm**: mục "BẮT ĐẦU TỪ ĐÂY" ghi cỡ font đang là 13 — số cũ, thực tế đã nâng lên **17** ở lần
  sửa cuối phiên trước. Núm chỉnh nhanh mà ghi sai số thì tệ hơn không ghi.
- **Trạng thái**: `gradlew build` sạch. Phần cần test tay **không đổi** — vẫn là 8 mục của bản vẽ ở
  điểm ảnh vật lý. Lỗi `Deflater` không quan sát được bằng mắt trong game, đừng tốn công thử.

### 2026-08-14 — Người dùng loại hướng "thoát khỏi cảm giác Minecraft". Đợt 1: quay về font gốc
- **Người dùng test bản điểm-ảnh-vật-lý và kết luận hướng thiết kế sai**: nên bám thẩm mỹ font và
  vibe của Minecraft thay vì đi ngược bản chất của nó. **Vẫn kiên định mục tiêu tablet chuẩn NATO** —
  cái bị loại là *cách* đạt tới, không phải đích.
- **Nhận định đáng ghi**: hai phiên trước tôi gộp hai quyết định làm một — *font TTF* và *hệ toạ độ
  vẽ*. Điểm ảnh vật lý tồn tại **để phục vụ** TTF. Bỏ TTF thì nó mất lý do, và còn có hại: font
  bitmap của Minecraft được nướng cho không gian logic, đặt vào không gian vật lý thì bé bằng 1/3.
  Nên đây là **quay về không gian logic**, không phải đổi mỗi file font.
- **Điều đã giữ lại từ đợt điểm ảnh vật lý** (không phải phí công): `Ui.textIn/textCentred/textRight`
  một hàm căn chữ duy nhất, thang `Ui.GAP_*`, và `UiButton`/`UiField` tự viết. Lý do bỏ widget vanilla
  vẫn còn nguyên — chính texture nút vanilla mới là thứ trông "mod Minecraft" nhất.
- **Đã làm**: bỏ ma trận `scale(1/guiScale)`, quy đổi chuột và quy đổi scissor (cả ba thành phép đồng
  nhất, xoá hẳn); `TabletTheme` bỏ `withFont`, giữ nguyên API nên 31 chỗ gọi không phải đụng;
  xoá 2 file JSON font + 2 TTF + `LICENSE-JetBrainsMono.txt`; `TEXT_HEIGHT` 13 → **8**;
  `GAP_*` 4/8/14/22 → **2/4/6/10**; mọi hằng số bố cục chia ~2,5; HUD ngoài màn hình **thôi đăng ký**.
- **Bẫy đã tránh — không phải số nào cũng chia được**: `PANEL_TITLE` chia 2,5 ra 5, **thấp hơn một
  dòng chữ 8px**, tiêu đề sẽ chui xuống dưới nội dung của chính nó. Sàn của nó là chiều cao chữ, nên
  đặt 10. Cùng loại với các ô chứa chữ: chia máy móc là hỏng.
- **Phòng đúng lỗi đã dính lần trước**: lần đổi *sang* điểm ảnh vật lý hỏng vì tôi nhân hằng số **có
  tên** mà sót số **viết thẳng trong thân hàm**. Lần này quét literal bằng regex trước khi sửa, và
  **đặt tên cho những số còn trần**: `KEY_FIRE`/`KEY_TALL`/`KEY_ROW` (chiều cao phím) và `CARD_LINE`
  (một dòng trong thẻ). Số có tên thì lần đổi đơn vị sau không thể sót.
- **HUD giữ lại, không xoá**: `ClientSetup.registerOverlays` giờ rỗng, class còn nguyên. Phần đáng
  giữ của nó là **đếm ngược đạn chạm đất** — bắn xong phải đóng tablet để nhìn chiến trường. Nó sẽ
  quay lại dưới dạng đúng một dòng đó, không phải cái bảng đã phình ra.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, 15 packet, **không lỗi
  `Invalid path`** → không còn tham chiếu treo tới font đã xoá). **Chưa test tay.**
- **Cần người dùng kiểm — mục 1 là mục quyết định**:
  1. **Vibe đã đúng chưa** — font Minecraft trong bố cục này có hợp không. Cả các đợt sau xây trên
     câu trả lời này, nên nếu vẫn sai thì dừng ở đây, đừng làm tiếp.
  2. Chữ có nằm giữa ô trong các nút không (nếu lệch: sửa `Ui.TEXT_HEIGHT`, vẫn là con số duy nhất).
  3. Bấm có trúng không — toạ độ chuột vừa đổi hệ lần thứ hai.
  4. Có chỗ nào chữ tràn khung hoặc nút chồng nhau không (dấu hiệu còn sót literal chưa chia).
  5. Bản đồ kéo/zoom còn đúng không, marker mục tiêu có còn bám đúng chỗ không.
  6. Đổi GUI Scale — giờ tablet **có** đổi theo (ngược với lần trước), đó là hành vi đúng bây giờ.
- **Đã chốt cho các đợt sau** (người dùng trả lời trong cùng phiên):
  - Phân công mục tiêu theo nghĩa **kế hoạch hoả lực tuần tự**, không phải phạm vi lệnh.
  - Áp quy ước NATO: **mil** thay độ, **định danh `AB1001`** thay `T1`, **hô hiệu SHOT/SPLASH**.
    *Không* dùng toạ độ lưới kiểu MGRS — giữ XYZ.
  - **Có tách `LAY`** khỏi `FIRE` (ngắm sẵn giữ nòng, bấm FIRE thì cả khẩu đội cùng khai hoả).
- **Lộ trình đã thống nhất**: đợt 2 = bố cục mới (box FIRE/MOD/ARC góc phải trên, nút zoom trên bản
  đồ, khẩu đội bên trái, mục tiêu bên phải, **cuộn danh sách**, nâng `MAX_TARGETS`/`MAX_BOUND`, mil +
  AB1001) · đợt 3 = trạng thái khẩu đội + chọn nhiều xe + đạn riêng từng xe · đợt 4 = kế hoạch hoả
  lực + LAY + SHOT/SPLASH + sheaf.
- **Ghi lại một phát hiện cho đợt 4**: `FireCommandMessage.java` đang truyền `radius = 0` cho
  `FiringParametersItem.Parameters`. Đó **chính là bán kính tản mát** mà `setTarget` dùng — tức khái
  niệm *sheaf* của pháo binh đã nằm sẵn đó, chỉ là chưa ai dùng. Nhiều khẩu cùng bắn một mục tiêu
  hiện đang dồn hết vào đúng một điểm.

### 2026-08-14 — Đợt 1 test: vibe ĐẠT. Thu gọn chrome + 2 lỗi chồng chữ
- **Người dùng test 6 mục: vibe đúng ✅, chữ căn giữa ✅, bấm trúng ✅, GUI Scale ✅.** Hướng font
  Minecraft được chốt — các đợt sau xây trên nền này. Còn 3 việc.
- **Mâu thuẫn thật giữa hai yêu cầu, đã nêu thẳng với người dùng**: họ muốn (1) mọi thứ nhỏ đi 20–30%
  và (2) chữ trông đỡ chật trong ô. **Font Minecraft là bitmap cố định 8px, không thu nhỏ được**, nên
  thu chrome lại làm chữ chiếm tỉ lệ *lớn hơn* — tức làm (2) tệ đi. Các nút đã sát sàn: cao 11px chứa
  chữ 8px, còn đúng 1px đệm mỗi bên.
  - **Cách giải**: thu chỗ **có dư** (bảng 156→124, viền 38→32, thẻ 34→30, lệnh bắn 128→104, thanh
    trên 18→15…) và **nới chỗ đã sát sàn** (mọi nút chứa chữ 11→12px). Hai chiều ngược nhau nhưng
    đúng: chrome gọn lại, chữ hết chật.
  - **Đòn bẩy thật cho "nhỏ đi nữa" là hạ GUI Scale**, đã nói rõ với người dùng. Không gian logic rộng
    ra thì cùng font 8px đó chiếm tỉ lệ nhỏ hơn hẳn, và vẫn nét. Không có cách nào khác trong khi vẫn
    giữ font bitmap — thu bằng ma trận sẽ lấy mẫu lại font và làm nó rỗ.
- **Lỗi chồng chữ ở tab Đạn (người dùng chụp ảnh)**: mỗi mục vẽ **hai dòng** (tên, rồi số lượng ở
  `+9`) nhưng bước hàng chỉ là **một dòng** (`rowHeight() + GAP_SM`), nên dòng số lượng đè 2px vào tên
  của mục kế tiếp.
  - **Tìm thêm đúng lỗi đó ở tab Pháo** — người dùng không báo vì chỉ chụp tab Đạn, nhưng
    `renderBattery` cũng vẽ tên + vị trí theo đúng kiểu ấy với đúng bước sai ấy.
  - **Khắc phục**: một cặp hằng số dùng chung `ROW_LINE`/`ROW_TWO_LINE` cho **cả bốn** chỗ (mỗi danh
    sách có một hàm dựng nút và một hàm vẽ chữ tính vị trí **riêng**). Đây là **lần thứ ba** trong dự
    án dính lỗi "hai chỗ tính cùng một thứ theo hai cách" — hai lần trước là `PANEL_TITLE` và cặp
    `buildPanel`/`renderPanel`. Mẫu hình đã rõ: **danh sách nào có builder và renderer tách rời thì
    mọi con số vị trí phải là hằng số dùng chung, không được viết lại ở hai bên.**
- **Chưa sửa — bản đồ và lưới "trôi lệch nhau" khi kéo nhanh (người dùng mục 5)**. Đã chẩn đoán,
  **cố ý không sửa vội** vì nó tinh vi và đáng một lượt riêng:
  - Tâm bản đồ chỉ dịch theo **bội số nguyên của block** (`panByPixels` giữ phần lẻ lại). Ở zoom 128m
    một block ≈ 5 điểm ảnh.
  - Lưới vẽ bằng `Math.round` ra **toạ độ điểm ảnh nguyên**, còn địa hình là texture `blit` phóng to
    bằng **nearest-neighbour**: mỗi texel chiếm khi 4 khi 5 điểm ảnh, và *texel nào được 5* thay đổi
    mỗi lần gốc ảnh dịch. Hai lớp vì thế nhích theo hai nhịp hơi khác nhau — đúng cảm giác "delay"
    người dùng mô tả.
  - **Đây chính là mục 4 trong danh sách còn tồn đọng** ("kéo bản đồ mượt dưới mức một block"), người
    dùng đã tự phát hiện triệu chứng của nó. Lời giải vẫn như đã ghi: cho tâm bản đồ nhận **số thực**
    rồi lệch cả ba lớp (lưới / địa hình / marker) theo phần lẻ.
- **Trạng thái**: `gradlew build` sạch. Chờ test lại.

### 2026-08-14 — Bảng thò ra ngoài tablet: một hình chữ nhật được vẽ theo ba cách
- **Người dùng test**: GUI Scale 3 hợp lý ✅, hết chồng chữ ✅. Báo "mấy tab bị lệch một ít ra khỏi
  GUI tablet", kèm ảnh — trong ảnh còn có **một dòng chữ mờ `m 185°` nằm đè dưới tiêu đề bảng**.
- **Hai triệu chứng, một gốc.** `renderPanel` vẽ nền tràn ra **4px mỗi phía**
  (`p[0] - 4 … p[0] + p[2] + 4`), trong khi `panelArea()` chỉ thụt vào 2px từ mép bản đồ. Nên nền
  bảng **thò 2px** ra khỏi mép trái, mép dưới, và lấn 2px lên thanh trên — đúng thứ người dùng thấy.
  - **Chữ ma cũng từ đó**: bảng **khai báo** với bản đồ một hình (`MapPanel.reserve`) nhưng **vẽ** ra
    một hình lớn hơn. Nhãn lưới rơi vào dải 4px dôi ra thì không được chừa chỗ nên vẫn vẽ, rồi bị nền
    bảng (đục 96%) phủ mờ lên — thành ra một dòng chữ nhoè không rõ của ai.
  - Số `4` là **literal điểm-ảnh-vật-lý còn sót**. Regex quét literal ở đợt trước bắt số 2–3 chữ số
    nên bỏ lọt nó. Bài học bổ sung: quét literal phải tính cả **số một chữ số**, vì offset nhỏ vẫn đủ
    lớn để phá vỡ quan hệ giữa hai hình chữ nhật.
- **Tìm thêm hai chỗ cùng lỗi mà người dùng không báo**:
  - Cột **Fire Missions** bên phải mang **y hệt** cái bleed 4px đó.
  - **Vùng bấm** `overPanel()` vẫn dùng `± 4`. Sau khi nền thôi tràn, vùng bấm sẽ **rộng hơn phần
    nhìn thấy**: bản đồ mất phản hồi trên một dải cạnh bảng, trong khi mắt thấy bảng đã hết. Đây đúng
    lớp lỗi "bấm nút mà bản đồ tưởng đang đặt mục tiêu" đã tốn hai lượt sửa trước đây.
- **Khắc phục — một hình chữ nhật, không phải ba**: `panelArea()` giờ là hình **trùng khít** với vùng
  được vẽ, vùng khai báo cho bản đồ, và vùng dựng nút. Phần đệm trong chuyển thành hằng số
  `PANEL_PAD` mà **cả hàm dựng lẫn hàm vẽ đều đọc**, cộng `contentWidth()` cho các thẻ đo theo bề
  rộng dùng được thay vì bề rộng bảng.
- **Đây là lần thứ tư** dự án dính "nhiều chỗ mô tả cùng một hình theo cách riêng"
  (`PANEL_TITLE` · `buildPanel`/`renderPanel` · bước hàng hai dòng · lần này). **Quy tắc rút ra và
  nên áp từ giờ: một vùng hình học chỉ được định nghĩa ở đúng một nơi; mọi thứ khác — vẽ, bắt chuột,
  chừa chỗ, dựng widget — phải hỏi lại nơi đó, không được tự cộng trừ thêm.**
- **Cũng đã chốt**: người dùng thấy **GUI Scale 3 là cỡ hợp lý**, không cần thu nhỏ thêm nữa.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, 15 packet). Chờ test lại.

### 2026-08-14 — Đợt 2: bố cục hai cột thường trực, box hành động, cuộn danh sách, mil
- **Người dùng test đợt 1 xong hết, cho làm đợt 2.**
- **Một xung đột trong spec đã phải tự giải**: cột **Fire Missions** (làm ở đợt B) đang chiếm bên
  phải, mà spec mới đặt Targets ở đó. Đã chuyển Missions xuống **đáy cột trái**, lập luận: một fire
  mission là *"khẩu X đang làm gì"* — nó thuộc về khẩu đội, không phải về mục tiêu. Đợt 3 khi thẻ xe
  mang trạng thái riêng thì cột này gộp hẳn vào thẻ xe và biến mất.
- **Đổi mô hình nền tảng: tab → cột thường trực.** Trước đây cả 5 mục đều là bảng bật/tắt từ menu
  trên. Giờ **Pháo (trái)** và **Mục tiêu (phải)** luôn mở — chúng không phải thứ để "chuyển sang
  xem", chúng *là* màn hình của một sở chỉ huy hoả lực. Chỉ Đạn/Trạng thái/Nhật ký còn là bảng gọi
  lên rồi đóng. `TabletTab` có thêm cờ `inNav` để phân biệt hai loại.
- **Bỏ hẳn viền phải.** FIRE/MOD/ARC vào **box góc phải trên** dưới thanh trên: MOD và ARC xếp chồng
  bên trái, FIRE chiếm nửa phải với đúng tổng chiều cao đó. Bản đồ giờ chạy tới **cả bốn mép**.
- **FIRE là ngoại lệ màu đỏ duy nhất**, và được ghi thành luật trong `UiButton.danger()`: đỏ vốn có
  nghĩa "đối phương" trong bảng màu, nên chỉ đúng một phím trên thiết bị được phép dùng. Vị trí giữ
  nó tách khỏi marker mục tiêu — nó luôn ở khung điều khiển, không bao giờ trên bản đồ.
  - **Nút nói rõ nó sắp làm gì**: `FIRE T3` chứ không phải `FIRE`. Một phím to và đỏ mà có thể mang
    nghĩa bất kỳ mục tiêu nào trong 32 là kiểu "rõ ràng" sai — đúng bài học đã rút ở đợt B.
- **Nút phóng to / thu nhỏ / về vị trí** thành ba ô **vuông nổi trên bản đồ**, góc dưới phải, ngay
  cạnh cột mục tiêu. Lỗi "bấm nút mà bản đồ tưởng đang đặt mục tiêu" không tái diễn vì hai lớp chốt
  đã có từ trước vẫn còn: widget được quyền ưu tiên tuyệt đối, và `overAnyPanel()` giờ loại **mọi**
  vùng đứng trên bản đồ (box hành động, hai cột, cột lệnh bắn) khỏi vùng bấm của bản đồ.
- **Cuộn danh sách** — mục tồn đọng số 1, giờ bắt buộc vì hai danh sách đã thành thường trực. Lăn
  chuột **trên danh sách nào thì cuộn danh sách đó**, trên bản đồ thì phóng to/thu nhỏ; con trỏ quyết
  định, không cần phím bổ trợ. Có cắt vùng (`Ui.scissor`) nên hàng cuộn không tràn ra ngoài cột.
- **Nâng giới hạn**: `MAX_TARGETS` 8 → **32**, `MAX_BOUND` 4 → **8**. Ví dụ của người dùng (10 mục
  tiêu, 3 xe) vượt cả hai mức cũ.
- **Phương vị bằng mil**: `95°` → `1689 mil`. NATO chia vòng tròn thành 6400 mil; một bảng tác xạ ghi
  độ là đang nói sai ngôn ngữ với người từng lấy phần tử bắn.
- **Đã xoá code chết**: `buildRightBezel`, `BEZEL`, `KEY_FIRE`, `KEY_TALL`, `MISSION_WIDTH`,
  `coverTop`, `panelBounds` — không để lại nhánh cũ.
- **Chưa làm trong đợt này, cố ý**:
  1. **Định danh `AB1001`** — cần thêm trường vào `TargetEntry` + NBT + packet, tức chạm tầng dữ
     liệu. Để chung đợt 3 vốn đã sửa tầng đó, tránh chuyển hai lần.
  2. **Bản đồ/lưới trôi lệch** — vẫn là mục tồn đọng số 4. Đã hẹn làm cùng đợt này nhưng đợt 2 đã đủ
     lớn; trộn thêm một thay đổi về hệ toạ độ bản đồ vào cùng một lượt test là đúng thứ tài liệu này
     đã cảnh báo nhiều lần.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, 15 packet). **Chưa test tay.**
- **Cần người dùng kiểm**:
  1. Hai cột có đứng đúng chỗ không, bản đồ có chạy tới cả bốn mép không.
  2. Box FIRE/MOD/ARC: FIRE có đỏ/chữ trắng, có ghi đúng mục tiêu đang chọn, mờ đi khi chưa có mục
     tiêu nào không.
  3. Ba nút vuông trên bản đồ có bấm trúng và **không** thả mục tiêu xuống bản đồ không.
  4. **Lăn chuột trên cột trái / cột phải / bản đồ có ra ba hành vi khác nhau đúng không.**
  5. Danh sách cuộn có bị tràn ra ngoài cột không.
  6. Bind quá 4 xe và thêm quá 8 mục tiêu có được không.
  7. Cự ly/phương vị đã hiện `mil` chưa.
  8. Bắn thử: cột lệnh bắn có hiện ở **đáy cột trái** và đẩy danh sách pháo thu lại không, hết lệnh
     có biến mất hoàn toàn không.

### 2026-08-14 — Đợt 2 test: 2 lỗi bố cục + đảo lại quyết định "cột thường trực"
- **Người dùng gửi ảnh.** Hai lỗi thật, cộng ba yêu cầu — trong đó một yêu cầu **đảo ngược quyết định
  trung tâm của đợt 2**, và đảo đúng.
- **Lỗi 1 — dòng chữ mờ cạnh tiêu đề "Battery"**: `TabletScreen:1137`, một dòng đọc cự ly/phương vị
  cũ vẽ ở góc trái bản đồ. Nó hỏng theo **ba cách cùng lúc**: hardcode `T1` từ thời chưa có khái niệm
  "mục tiêu đang chọn", vẫn dùng **độ** sau khi mọi chỗ khác đã chuyển sang mil, và nằm dưới cột trái
  nên hiện thành vệt mờ xuyên qua bảng. **Đã xoá** — thẻ mục tiêu và thanh chi tiết đều nói đủ rồi.
  - Đáng ghi: đây là **mảnh sót của hướng B** (bản đồ lưới thuần, 2026-08-13), sống sót qua bốn lần
    viết lại giao diện vì chưa ai nhìn vào đúng góc đó. Đổi đơn vị hàng loạt (độ → mil) chỉ sửa được
    những chỗ mình biết là có.
- **Lỗi 2 — nút `Fire` trên thẻ đè lên dòng cự ly**: thẻ rộng 116px không đủ cho ba dòng số cộng hai
  nút. **Bỏ hẳn nút Fire trên thẻ** thay vì nhồi cho vừa: nó đã **thừa** từ lúc FIRE ở box hành động
  biết đọc tên mục tiêu đang chọn (`FIRE T3`). Cùng một hành động nói hai lần. Bỏ nó vừa hết đè, vừa
  cho phép **thu bảng 124 → 106**.
- **Đảo quyết định: bỏ cột thường trực, quay lại bảng gọi-lên-rồi-đóng.** Đợt 2 đặt Pháo và Mục tiêu
  đứng mở vĩnh viễn với lập luận "sở chỉ huy hoả lực không giấu chúng sau tab". Lập luận đó đúng trên
  màn hình rộng và **sai trên màn hình Minecraft**: hai cột 124px ăn hết 248px trong khoảng 590px,
  chôn mất bản đồ — mà bản đồ mới là thiết bị. Người dùng nhìn ảnh và nhận ra ngay.
  - Giờ **mặc định không có gì mở**, bản đồ trống trơn. Trái (Pháo/Đạn/Trạng thái/Nhật ký) và phải
    (Mục tiêu) gọi từ menu trên, bấm lại thì đóng, **mở đồng thời được** vì ở hai bên.
  - `TabletTab` đổi cờ `inNav` thành **`onRight`**: bên nào một view mở ra là thuộc tính của chính nó
    (trái = *cái gì đang tồn tại*, phải = *đang bắn vào cái gì*), không phải chi tiết bố cục.
- **Gộp fire mission vào thẻ xe** (người dùng đề xuất, và đúng): xoá hẳn cột riêng —
  `renderMissions`, `buildMissionCards`, `missionArea`, `MISSION_CARD`, `leftPanelArea`. Mỗi xe giờ
  ba dòng: tên · toạ độ + cự ly · **trạng thái lệnh bắn**, kèm nút **Abort** ngay trên thẻ khi có
  lệnh đang chạy.
  - Lý do sâu hơn chỉ là tiết kiệm chỗ: **một fire mission không có nghĩa tách rời khỏi khẩu pháo
    đang thực hiện nó.** Tách thành bảng riêng buộc người đọc tự ghép hai danh sách lại với nhau.
  - Đây cũng là bước dọn đường cho đợt 3: khi có packet trạng thái xe (góc nâng, đạn, cooldown) thì
    nó đổ thẳng vào dòng thứ ba này, không phải dựng chỗ mới.
- **Bài học về quy trình**: đợt 2 xây một quyết định bố cục lớn (cột thường trực) **rồi mới** đưa cho
  người dùng nhìn. Một tấm ảnh đã lật nó trong một câu. Với thứ chỉ đánh giá được bằng mắt, phác thảo
  trước rẻ hơn code trước — đúng cái người dùng đã tự yêu cầu ở đợt Lattice và tôi quên áp lần này.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, 15 packet, không lỗi tài
  nguyên, không thiếu khoá dịch). **Chưa test tay.**
- **Cần người dùng kiểm**:
  1. Mở tablet: **không bảng nào hiện**, chỉ box FIRE/MOD/ARC, bản đồ trọn vẹn.
  2. Menu trên mở/đóng đúng từng bảng, mở Pháo và Mục tiêu **cùng lúc** được.
  3. Thẻ mục tiêu: dòng cự ly **không còn bị nút đè**, nút `x` ở góc trên phải xoá đúng mục.
  4. Thẻ xe có đủ ba dòng, dòng trạng thái đổi đúng khi bắn (AIMING → đếm ngược → hết).
  5. **Nút Abort trên thẻ xe** dừng pháo thật.
  6. Không còn vệt chữ mờ nào ở góc trái bản đồ.
  7. Lăn chuột cuộn đúng bảng đang trỏ vào; bản đồ vẫn zoom.
- **Vẫn nợ từ đợt 2**: định danh `AB1001` (để đợt 3, chạm tầng dữ liệu) và bản đồ/lưới trôi lệch
  (mục tồn đọng số 4).

### 2026-08-14 — Phác thảo bố cục NATO, và đợt 3A: đường ống trạng thái xe + phần tử bắn
- **Người dùng yêu cầu phác thảo trước khi code** (đúng như tôi đã hứa ở lượt trước). Đã vẽ toàn cảnh
  GUI **ở đúng tỉ lệ điểm ảnh thật** (589×331 logic, phóng 2×) thay vì một bản vẽ đẹp không nhét vừa.
- **Ba chỗ chật chỉ lộ ra khi vẽ đúng tỉ lệ** — đây chính là giá trị của việc phác thảo trước:
  1. Dòng 3 của thẻ xe **không chứa nổi** `IN FLIGHT 4.2s` cộng nút ABORT ở bề rộng 106px.
  2. Dòng lệnh bắn trong box hành động chỉ chứa ~16 ký tự → viết tắt bò trở lại, đúng thứ người dùng
     từng phàn nàn ở bố cục MilDef.
  3. **Thanh chi tiết là chỗ duy nhất còn dư nhiều** (dùng ~65% bề ngang) → đó là lý do phần tử bắn
     phải nằm ở đó chứ không nhồi vào cột.
- **Người dùng chốt**: bố cục OK, **thẻ xe cứ cao thêm nếu thiếu chỗ** — điều này giải luôn cả (1) và
  (2): thẻ lên 4 dòng, dòng lệnh bắn xuống hai dòng thay vì viết tắt.
- **Khoảng trống lớn nhất đã xác định**: tablet **chưa bao giờ hiện phần tử bắn**. Mọi khí tài NATO
  thật — từ Gun Display Unit trên M109/M777 tới AFATDS — đều xoay quanh **phương vị + góc tà**. Tablet
  chỉ hiện cự ly/phương vị *tới mục tiêu*, tức ngôn ngữ của **người quan sát**, không phải của khẩu
  đội. Mỉa mai là `ArtilleryAimTracker` **đã đo hai vector đó mỗi tick từ Phase 3** mà chưa ai hiện ra.
- **Quyết định kiến trúc đáng ghi — không thêm packet mới**: mở rộng `NearbyArtilleryEntry` thay vì
  dựng packet trạng thái riêng. Bản tin roster vốn đã tồn tại, đã chạy theo kiểu **kéo chứ không đẩy**,
  và đã mang dữ kiện từng khẩu; góc lay chỉ là thêm dữ kiện. Client hỏi lại **5 tick/lần khi tablet
  đang mở** — đó là thứ biến một danh sách thành một bảng đo sống. Rẻ hơn hẳn một tầng packet mới.
- **Đã làm**:
  - `NearbyArtilleryEntry` mang thêm: phương vị/góc tà **hiện tại và đã lệnh** (đơn vị **mil**), cờ
    `laid`, số đạn + tên loại đạn. Có `offMil()` tính độ lệch còn lại, xử lý **vòng qua 6400**
    (6390 → 10 là 20 mil, không phải 6380).
  - **Cờ `laid` là chi tiết quan trọng**: khẩu chưa được lệnh thì không có vector, hiện 0 sẽ đọc
    thành *"đã lệnh hướng bắc, nòng phẳng"* — một phần tử bắn nó không hề có.
  - Server đọc thẳng `getShootVec(Main, 1f)` (hướng nòng thật, chính vector `vehicleShoot` dùng) và
    `getShootVec()` (hướng đã lệnh do `setTarget` ghi). **Không tự tính lại** — đó là tái hiện nội bộ
    SBW, cái bẫy đã dính ba lần trong dự án này.
  - Thẻ xe lên **4 dòng**: tên + bind · toạ độ + cự ly · **phần tử bắn** · trạng thái + Abort. Dòng
    phần tử bắn **đổi màu hổ phách khi nòng còn lệch > 18 mil**.
  - **Chọn được xe**; thanh chi tiết **đổi theo thứ đang chọn**: chọn xe → `GUN · AZIMUTH · ELEVATION
    · OFF · AMMO`; chọn mục tiêu → dữ liệu mục tiêu như cũ. Chọn cái này thì bỏ chọn cái kia — một
    thanh nói một thứ, chứ không phải hai thứ tranh nhau cùng một dải.
  - Số dùng **trường cố định, số 0 đứng đầu** (`AZ 1689→1691`) theo đúng quy ước màn hình pháo: con
    số không nhảy chỗ khi đổi, nên nòng xoay đọc thành *chuyển động* chứ không phải chữ chạy lung tung.
- **Lỗi tự gây rồi tự sửa**: tôi đoán đường dẫn import SBW (`item.gun.data.GunData`) và sai — đúng là
  `data.gun.GunData`. Lấy lại từ chính `AmmoTool` của dự án. Nhắc lại bài học cũ: **xác minh API bằng
  file thật, đừng đoán.**
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, 15 packet). **Chưa test tay.**
- **Cần người dùng kiểm**:
  1. Mở bảng Pháo: thẻ xe có **4 dòng**, dòng `AZ ...` có hiện không.
  2. Ra lệnh bắn rồi nhìn thẻ: `AZ 1689→1691` có **chạy theo nòng** và hai số tiến về trùng nhau không.
  3. Khẩu **chưa từng được lệnh** phải hiện `AZ 0000 QE 0000` (một số), **không** hiện mũi tên.
  4. Bấm thân thẻ xe → thanh chi tiết dưới đáy đổi sang `GUN / AZIMUTH / ELEVATION / OFF / AMMO`.
  5. Bấm thẻ mục tiêu → thanh chi tiết đổi lại về dữ liệu mục tiêu, thẻ xe bỏ chọn.
  6. `OFF` có về 0 và **đổi xanh** khi nòng vào vị trí không.
  7. `AMMO` có đúng loại đạn và số lượng không (Creative Box phải ra `∞`).
  8. Bấm bind/unbind và Abort **vẫn hoạt động**, không bị vùng chọn thẻ nuốt mất.
- **Còn lại của đợt 3, chưa làm**: vòng tầm bắn + đường bắn trên bản đồ · thanh trên đổi sang trạng
  thái khẩu đội và `CTR` xuống góc bản đồ · dòng lệnh bắn hai dòng · chọn nhiều xe + đạn riêng từng
  xe + số hiệu cố định · định danh `AB1001` · bản đồ/lưới trôi lệch · giảm bão hoà màu bản đồ.

### 2026-08-14 — Đợt 3B: vòng tầm bắn, thanh trên đổi vai, bản đồ bạc màu
- **Đã làm nốt phần bản đồ và khung của đợt 3.**
- **Vòng tầm bắn** — thứ tôi xếp giá trị cao thứ hai sau phần tử bắn:
  - Bốn accessor cần thiết **đã có sẵn** trong `ReachabilityCheck` (`getProjectileVelocity`,
    `getProjectileGravity`, `getTurretMaxPitch/MinPitch`), nên chỉ cần gửi 4 số qua roster rồi tính
    phía client — vòng đổi **ngay lập tức** khi bấm ARC, không phải hỏi lại server.
  - Công thức: tầm = v²·sin(2θ)/g, xa nhất ở 45°. **Điểm mấu chốt: hai quỹ đạo có cùng tầm tối đa**
    (đều ở 45°, ranh giới giữa chúng); khác nhau ở **mép gần** — căng bị giới hạn bởi độ hạ nòng,
    cầu vồng bởi độ ngóc nòng. Đây chính là lý do cầu vồng vô dụng ở cự ly gần với trần 65°: mép
    gần của nó là 5400·sin(130°) ≈ 4136, **khớp với con số 4150 đo được ngày 2026-08-13**.
  - Vòng ngoài liền, vòng trong đứt nét. Ghi rõ trong code là **chỉ để tham khảo**: bỏ qua chênh cao
    và điểm ngắm lệch của SBW, nên nó nói *đại khái bắn tới đâu*, không quyết định có bắn hay không.
    Đúng nguyên tắc đã đặt từ Phase 6.
- **Nhầm lẫn của tôi đã tự sửa**: tôi nói với người dùng rằng **đường bắn khẩu→mục tiêu bị mất** khi
  viết lại giao diện. **Sai** — nó vẫn nằm nguyên trong `renderMapMarkers`. Cái làm nó không hiện là
  client chưa biết vị trí pháo cho tới khi mở bảng Pháo; **bản làm mới roster 4 lần/giây ở đợt 3A đã
  vô tình sửa luôn**. Bài học: kiểm code trước khi khẳng định thứ gì đó đã mất.
- **Thanh trên đổi vai**: bỏ `CTR` (dữ kiện *bản đồ*, đã chuyển xuống góc bản đồ cạnh tỉ lệ và
  `TERRAIN`), bỏ MODE/ARC (đã có trong box hành động). Thay bằng **trạng thái khẩu đội**:
  `GUNS 2/3 · 14 HE · IN FLIGHT 1 · TGT 3`. Đó là thứ thanh trạng thái của một FDC thật mang.
- **Box hành động có dòng lệnh bắn hai dòng** (`2 GUNS · SALVO` / `Depressed`) thay vì một dòng viết
  tắt — đúng quyết định "thẻ cứ cao thêm" của người dùng, áp cho cả box. Đỏ cảnh báo khi chưa bind
  khẩu nào: lệnh bắn không có pháo là lệnh rỗng.
- **Bản đồ bạc màu**: kéo màu về phía xám một nửa rồi hạ còn 2/3 độ sáng. Bảng màu bản đồ vanilla
  dựng cho vật phẩm bản đồ cầm tay, nơi cỏ xanh rực là mục đích; dưới một lớp ký hiệu nó là thứ to
  tiếng nhất màn hình và màu đỏ/xanh mang nghĩa chìm nghỉm. Bản đồ chiến thuật thật nhạt là có chủ ý.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, 15 packet). **Chưa test tay.**
- **Cần người dùng kiểm**:
  1. Vòng tầm bắn có hiện quanh pháo đã bind không, có **đổi ngay** khi bấm ARC không.
  2. Ở chế độ Cầu Vồng, vòng trong phải rất lớn (~4100 block) — khớp với việc cầu vồng chỉ dùng được
     ở cự ly xa.
  3. Đặt mục tiêu ngoài vòng ngoài / trong vòng trong → thẻ mục tiêu phải báo đúng `MAX RANGE` /
     `MIN RANGE`, tức **vòng vẽ khớp với phán quyết thật**.
  4. Đường bắn từ pháo tới mục tiêu có hiện không (giờ phải hiện ngay, không cần mở bảng Pháo trước).
  5. Thanh trên có hiện `GUNS x/y`, loại đạn, số lệnh đang bay không; `CTR` đã xuống góc bản đồ chưa.
  6. Box hành động hai dòng lệnh bắn, chưa bind pháo thì dòng đó **đỏ**.
  7. Bản đồ đã bớt chói chưa, ký hiệu đỏ/xanh có nổi lên rõ hơn không.
  8. Zoom ra 4096m — vòng tầm bắn có vẽ quá nặng không (có chốt chặn ở bán kính > 4000px).

#### 📋 Đợt 3 còn nợ (nhóm dữ liệu, chưa làm)
Bốn mục còn lại đều chạm **tầng dữ liệu/NBT**, khác hẳn nhóm hiển thị vừa xong, và là nền cho kế
hoạch hoả lực của đợt 4 — nên gom vào một lượt riêng thay vì nhét vào cuối đợt này:
1. **Chọn nhiều xe** + áp thao tác hàng loạt.
2. **Chọn đạn riêng từng xe** (hiện chọn đạn áp cho toàn bộ pháo đã bind).
3. **Số hiệu xe cố định** — cấp lúc bind, không dồn lại khi gỡ. Cùng lớp lỗi với việc kẹp chỉ số mục
   tiêu đã dính ở đợt B.
4. **Định danh mục tiêu `AB1001`** — cần thêm trường vào `TargetEntry` + NBT + packet.

Ngoài ra vẫn treo: **bản đồ/lưới trôi lệch khi kéo nhanh** (mục tồn đọng số 4, cần tâm bản đồ nhận
số thực).

### 2026-08-14 — Đợt 3C: rail icon bên trái, menu chuột phải trên khẩu pháo, 4 lỗi
- **Bốn lỗi người dùng báo, đều xác nhận được:**
  1. **Bảng đóng lại mỗi lần mở tablet** — `leftTab`/`rightOpen` là field của `TabletScreen`, mà
     Screen bị **tạo mới mỗi lần bấm H**. Tôi hiểu sai "mặc định ẩn": ý người dùng là *lần đầu*,
     không phải *mỗi lần*. Đổi thành **static phía client** — đây là trạng thái *nhìn*, không phải
     trạng thái *thiết bị*; vào NBT thì mỗi lần mở bảng lại đồng bộ cả ItemStack qua mạng, đúng lý
     do `FireLog` cố ý không vào NBT.
  2. **Nút Unbind đè chữ**: `ROW_LINE = 9`, nút cao `12`, dòng 2 bắt đầu ở 9 → **đè 3px**.
  3. Chữ "New target" thừa — tiêu đề bảng và nút "Add Target" đã nói đủ.
  4. **Bảng đè lên thanh chi tiết** — bảng cao bằng cả vùng bản đồ mà thanh chi tiết nằm ở đáy đó.
- **Lỗi 2 là lần thứ ba cùng một lớp lỗi** (nút và chữ tranh chỗ trong thẻ hẹp). Ý tưởng của người
  dùng giải nó **tận gốc**: đưa mọi thao tác vào menu chuột phải → **thẻ xe thành chữ thuần**, không
  nút, không thể đè. Nguyên nhân gốc là thẻ đang cố vừa làm bảng đọc vừa làm bảng điều khiển trong
  106px; tách hai vai đó ra là lời giải đúng, tốt hơn hẳn phương án "chia hai cột" tôi định làm.
- **Bố cục mới theo đề xuất của người dùng (tham chiếu ảnh Lattice)**:
  - **Rail 14px bên trái**, luôn hiện, 5 phím cho 5 view; bảng mở **ngay cạnh rail**.
  - **Bên phải giải phóng hoàn toàn** — chỉ còn box MOD/ARC/FIRE ở góc trên và 3 nút zoom ở góc dưới.
  - **Bỏ menu khỏi thanh trên** — rail đã mang việc đó, hai chỗ làm một việc là thừa.
- **Cảnh báo đã nêu trước khi làm, và cách tránh**: đây **đúng là bố cục MilDef từng bị loại**
  (14/08) vì phím viết tắt `MT/PH/ĐẠ` khó hiểu. Khác biệt quyết định: lần này dùng **hình vẽ + tooltip**,
  không dùng chữ tắt. Ký hiệu Pháo là **hình chữ nhật có chấm tròn đặc — đúng ký hiệu APP-6 của pháo
  binh thật**, nên nó là thứ bớt phải học chứ không phải thêm.
- **Menu chuột phải trên khẩu pháo** (`⋯` cũng mở được — chuột phải một mình là thao tác vô hình):
  Đạn dược · Huỷ lệnh bắn · Gỡ ràng buộc. Khẩu **chưa bind** giữ nguyên nút Bind trên thẻ, không giấu
  sau menu — không hợp lý khi bắt mở menu của một thứ chưa thuộc khẩu đội.
- **`gunCardAt()` tính chỉ số thẻ từ đúng `TabletPanels.rowPitch()`** mà bảng dùng để xếp thẻ, không
  tự viết lại con số — lỗi kinh niên của file này là hai chỗ tính cùng một vị trí.
- **Chưa làm, cố ý**: mục **"Phân công mục tiêu"** trong menu. Nó cần mô hình dữ liệu phân công
  khẩu↔mục tiêu, tức chính là đợt 4. Không đặt nút rỗng vào menu.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, 15 packet). **Chưa test tay.**
- **Cần người dùng kiểm**:
  1. Mở bảng nào đó, **tắt tablet, mở lại** — bảng phải còn nguyên ở đó.
  2. Rail: 5 ký hiệu có **đọc ra được** không, tooltip đúng tên không, ký hiệu view đang mở có nổi bật không.
  3. Thẻ xe **không còn nút nào** ngoài `⋯` (hoặc Bind với khẩu chưa bind) — chữ hết bị đè chưa.
  4. **Chuột phải vào thẻ xe** mở menu; bấm `⋯` cũng mở; bấm ra ngoài thì đóng.
  5. Ba mục trong menu chạy đúng (Đạn mở tab Đạn, Huỷ lệnh dừng pháo thật, Gỡ ràng buộc gỡ đúng khẩu).
  6. Bảng **không còn đè lên thanh chi tiết**; nút zoom cũng không.
  7. Bảng Targets không còn chữ "New target".
  8. Chuột phải **trên bản đồ** vẫn đặt/xoá mục tiêu như cũ (không bị menu cướp).

### 2026-08-14 — Phím kiểu MFD, và ba chỗ va chạm ở hai góc dưới
- **Người dùng đổi hướng phím rail: bỏ icon vẽ, quay lại chữ viết tắt — nhưng trong ô vuông, kiểu
  MFD trên buồng lái tiêm kích.** Đây là lần thứ hai chủ đề "viết tắt hay không" quay lại, và lần này
  câu trả lời khác vì **lý do khác**:
  - Lần đầu (MilDef, 14/08) bị loại vì **2 ký tự** trần, không khung, không tooltip.
  - Icon vẽ bằng code ở cỡ 12px đọc ra thành *hình trang trí*, không thành ký hiệu.
  - **3 ký tự trong ô có viền + tooltip** là thứ khí tài thật dùng, và chở được nhiều hơn hẳn 2 ký tự.
  → `BTY / TGT / AMO / STA / LOG`. Kiểu vẽ `UiButton.mfd()`: nền cùng màu panel, **viền mang trạng
  thái** (xanh lục khi view đang mở), chữ không nền. Một dãy như thế đọc thành *một khí cụ*, không
  phải một hàng chip màu.
- **Cụm FIRE/MOD/ARC thành rail phải**, xếp dọc, cùng kiểu ô. FIRE cao hơn hẳn và giữ nền đỏ — vẫn là
  ngoại lệ màu duy nhất trên thiết bị.
  - **Hệ quả**: box hành động từ 106px rộng co xuống **26px**. Ba dòng chữ nó từng chở (số pháo, chế
    độ, quỹ đạo) không còn chỗ — nhưng cũng **không cần**: thanh trên đã ghi cả ba, và tooltip từng
    phím nói rõ bấm vào sẽ chọn gì. Bản đồ được thêm 80px bề ngang.
- **Ba chỗ va chạm người dùng chụp được**:
  1. **Cột function trái đè chữ thanh chi tiết** — nút `Refresh` ở đáy bảng nằm sát `bodyBottom()`,
     không có khe nào. Thêm khe `GAP_XS`.
  2. **Nút zoom bị toạ độ tâm bản đồ đè** — góc dưới phải có **ba dòng** (`CTR`, `TERRAIN`, tỉ lệ)
     xếp chồng sâu 30px, mà cụm nút cũng neo vào đúng góc đó. Đẩy cụm nút lên trên bằng hằng số
     `MAP_CORNER_TEXT`.
  3. **Chữ thanh chi tiết chạm mép** — cao 18px chứa hai dòng 8px, đệm trên 2, đệm dưới 0. Nâng lên
     22px, chữ vào trong.
- **Đáng ghi về hằng số**: `ACTION_HEIGHT` suy ra từ `RAIL_FIRE` nhưng được khai báo **trước** nó →
  `illegal forward reference`. Java bắt được ngay, nhưng nó nhắc rằng thứ tự khai báo hằng số giờ
  **mang ý nghĩa** khi chúng bắt đầu suy ra lẫn nhau — trước đây toàn số rời nên không thành vấn đề.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, 15 packet). **Chưa test tay.**
- **Cần người dùng kiểm**:
  1. Rail trái: 5 ô `BTY/TGT/AMO/STA/LOG` có **đọc rõ** không, viền có sáng lên đúng view đang mở không.
  2. Rail phải: FIRE đỏ và cao hơn, MOD/ARC dưới nó, cả ba cùng kiểu ô.
  3. **Nút zoom không còn bị chữ góc bản đồ đè.**
  4. Thanh chi tiết: chữ có khoảng thở, **không bị bảng bên trái đè**.
  5. Bản đồ có rộng ra rõ rệt không (box hành động từ 106 xuống 26px).
  6. Tooltip của MOD/ARC vẫn cho biết đang ở chế độ/quỹ đạo nào chứ?

### 2026-08-15 — Footbar cố định, phím vuông, và toạ độ gắn thẳng lên mark
- **Năm yêu cầu của người dùng sau khi test, đều làm.**
- **Thanh chi tiết luôn hiện, rỗng khi không có gì để nói.** Trước đây nó xuất hiện/biến mất theo lựa
  chọn, mà `bodyBottom()` lại suy ra từ nó — nên **mỗi lần chọn một thứ là toàn bộ bảng và mọi phím
  trên bản đồ nhảy 20px**. Giờ nó là một phần của khung, không phải thứ chen vào khung.
- **Phím rail thành hình vuông thật** (`RAIL_KEY × RAIL_KEY`), cách đều nhau. Trước đó chúng cao
  `KEY_ROW` = 12 nhưng rộng 22 → hình chữ nhật dẹt, không ra bezel key.
- **Màu phím đổi theo yêu cầu**: chưa chọn thì **viền trắng chữ trắng**, đang chọn thì **viền xanh
  chữ xanh**. Viền và chữ đi cùng nhau — trạng thái đọc được bằng liếc mắt, không phải bằng cách so
  sánh riêng màu viền.
- **Bỏ toạ độ tâm bản đồ và dòng `TERRAIN: SURVEYED` ở góc**, chỉ giữ tỉ lệ. Lý do đáng ghi:
  - `TERRAIN: SURVEYED` là **dữ kiện về đường ống dữ liệu**, không phải về chiến trường. Nó có ích
    hồi đang gỡ lỗi bản đồ; giờ bản đồ chạy rồi thì nó chỉ chiếm chỗ.
  - Toạ độ tâm trả lời một câu **không ai hỏi**. Thứ pháo thủ cần là toạ độ *của thứ đang nhìn* —
    nên nó chuyển thành **nhãn nhỏ gắn ngay dưới từng mark** (pháo và mục tiêu), nền tối chữ trắng.
    Khoảng cách ngắn nhất giữa một ký hiệu và con số của nó là **bằng không**.
- **Thêm hai mức zoom: 8192 và 16384 block** (gấp 4 mức cũ, người dùng xin ~3 lần để thử). Hai mức
  này nằm ngoài tầm vẽ được địa hình nên là **lưới thuần** — và đó chính là công dụng: một khẩu ném
  được 5400 block cần nhìn thấy mình nằm ở đâu trong cái lớn hơn thế.
- **Thẻ đang chọn trong danh sách có viền màu**, không chỉ nền. Nền đơn thuần chỉ lệch một sắc so với
  bảng phía sau — không đủ để tìm ra bằng liếc mắt trên một danh sách dày.
- **Dọn kèm**: `renderGunDetail`/`renderDetail` từng **tự tô nền thanh chi tiết mỗi hàm**, giờ nền do
  đúng một chỗ vẽ (tô hai lần bằng màu có alpha sẽ đậm hơn dự tính). Xoá 2 khoá dịch không còn dùng.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không thiếu khoá dịch).
  **Chưa test tay.**
- **Cần người dùng kiểm**:
  1. Thanh chi tiết **luôn hiện**, rỗng khi mở BTY mà chưa chọn xe nào; bố cục **không nhảy** khi chọn.
  2. Phím hai rail có **vuông** và cách đều không.
  3. Phím chưa chọn **trắng**, đang chọn **xanh** — cả viền lẫn chữ.
  4. Nhãn toạ độ dưới mark pháo và mark mục tiêu: có đọc được trên nền cỏ không, có **quá to** không.
  5. Zoom ra tới 16384m có chạy không, lưới còn đọc được không.
  6. Thẻ xe đang chọn có **viền** rõ không.
  7. Góc dưới phải giờ chỉ còn tỉ lệ — nút zoom hết bị đè chưa.

### 2026-08-15 — Năm chỉnh nhỏ: khe hở, nhãn nửa cỡ, phím vuông đều, gạch ngăn, tỉ lệ về với nút
- **Khe hở giữa bảng và thanh chi tiết**: `bodyBottom()` trừ thêm `GAP_XS` nên còn một sợi bản đồ lọt
  qua giữa hai thứ. Bỏ khe — thanh chi tiết giờ đã có đệm trong nên không cần khe ngoài nữa.
- **Nhãn toạ độ nhỏ đi một nửa, và có thêm Y.** Font Minecraft là bitmap cố định, không nướng nhỏ hơn
  được, nên cách duy nhất là **vẽ trong ma trận nhân 0.5**. Đáng ghi lý do nó vẫn đọc được: giao diện
  đang chạy ở GUI Scale 3, nên chữ nửa cỡ vẫn còn **1,5 điểm ảnh thật cho mỗi điểm ảnh logic** — nhiều
  hơn cả chữ không thu nhỏ ở GUI Scale 1. Ở scale 1 nó sẽ khó đọc; đó là đánh đổi có ý thức.
- **FIRE trở lại hình vuông** như mọi phím khác. Nó từng cao hơn để "tìm được mà không cần đọc", nhưng
  ở một rail rộng 22px thì cao hơn chỉ thành **hình chữ nhật dọc**, không thành nổi bật. Nền đỏ đã làm
  đúng việc đó rồi.
- **Gạch mờ ngăn giữa các phím** (`railRules`), vẽ từ **đúng `RAIL_STEP` mà phím được xếp theo** — hai
  bên không thể lệch nhau. Cố ý mờ: việc của nó là nhóm cột thành từng phím riêng, không phải thêm
  một đường nữa để đọc.
- **Tỉ lệ bản đồ chuyển về dưới cụm nút zoom**, thay vì nằm ở góc xa. Nó là con số **ba phím kia thay
  đổi**, nên đọc ở cạnh chúng. Góc dưới phải giờ trống hẳn.
- **Nút Centre bỏ chữ `CTR`, thành ký hiệu chữ thập có khe giữa.** Ba ký tự trong ô 12px là chạm viền;
  ký hiệu nói đúng chừng ấy trong đúng chỗ có. Xoá khoá dịch không còn dùng.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không thiếu khoá dịch).
  **Chưa test tay.**
- **Cần người dùng kiểm**:
  1. Không còn khe bản đồ giữa bảng và thanh chi tiết.
  2. Nhãn toạ độ dưới mark: nhỏ đi một nửa, có đủ **X Y Z**, còn đọc được không.
  3. FIRE vuông bằng MOD/ARC, vẫn nền đỏ.
  4. Gạch mờ giữa các phím ở **cả hai rail**, khoảng cách trông đều.
  5. Tỉ lệ (`4096m`) nằm dưới nút Centre; góc dưới phải không còn chữ nào khác.
  6. Nút Centre hiện ký hiệu chữ thập, bấm vẫn về vị trí người chơi.

### 2026-08-15 — Bỏ nhãn toạ độ trên bản đồ, gạch ngăn đủ thấy, sửa đệm tính hai lần
- **Nhãn toạ độ dưới mark: làm xong rồi bỏ.** Chúng đọc tốt khi nhìn *một cái một*, nhưng nhìn
  **toàn cảnh** thì thành rối — ngược hẳn công dụng của một lớp phủ bản đồ. Ghi lại vì đây là một
  tính năng đi trọn vòng trong hai lượt: đề xuất → làm → thu nhỏ một nửa → loại. Con số vẫn có ở thẻ
  và thanh chi tiết, tức đúng lúc đang *làm việc* với một mark thay vì lúc đang *nhìn bao quát*.
- **Gạch ngăn giữa các phím không hề hiện** — không phải sai toạ độ mà **sai màu**: `LINE_SOFT` là
  xám đậm ở nửa alpha, vốn để kẻ trên nền sáng; đặt lên rail vốn đã tối thì bằng không. Đổi sang
  trắng mờ (`RAIL_RULE`). Bài học nhỏ: một màu "kẻ nhạt" chỉ nhạt **so với nền mà nó được chọn cho**.
- **Ô nhập toạ độ và nút Add Target bị dí sát mép phải** — `buildTargetsControls` nhận `x` **đã trừ
  đệm rồi** nhưng lại cộng `GAP_SM` vào mọi vị trí, tức **đệm bị tính hai lần**: cả hàng lệch phải
  4px và nút Add Target tràn qua mép panel. Bỏ phần cộng thừa, các ô giờ chia đều đúng bề rộng dùng
  được (`3 × 30 + 2 × 4 = 98`, khít).
  - Cùng họ với các lỗi "hai chỗ mô tả một hình" đã ghi bốn lần trước đó, nhưng biến thể khác: **một
    chỗ áp cùng một phép đệm hai lần** vì không rõ toạ độ nhận vào đã trừ đệm hay chưa. Cách phòng
    trong tương lai: hàm dựng nội dung luôn nhận **gốc đã trừ đệm** và không bao giờ tự cộng thêm.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không thiếu khoá dịch).
  **Chưa test tay.**
- **Cần người dùng kiểm**: (1) bản đồ hết nhãn toạ độ, nhìn toàn cảnh thoáng chưa; (2) gạch ngăn giữa
  các phím ở **cả hai rail** đã thấy rõ chưa, có quá đậm không; (3) ba ô toạ độ và nút Add Target nằm
  cân trong bảng, không tràn mép.

### 2026-08-15 — Gạch ngăn đều hai bên, ký hiệu vẽ thay chữ, ký hiệu hướng cho người chơi
- **Gạch ngăn lệch và quá sát**: khe giữa hai phím là `GAP_MD` = 6, gạch 1px đặt giữa → **3px trên,
  2px dưới**. Khe chẵn thì không chia đôi được quanh một đường lẻ pixel. Đổi `RAIL_GAP` thành **số
  lẻ** (`GAP_MD * 3 + 1` = 19) → 9 trên, 1 gạch, 9 dưới, chia đúng, và rộng gấp ba như yêu cầu.
- **Rail phải không có gạch nào** dù code có gọi vẽ: `railRules` chạy ở `renderDevice` **trước**
  `renderActionBox`, mà hàm đó tô nền toàn bộ vùng — **tô đè lên chính các gạch vừa vẽ**. Chuyển lời
  gọi vào trong `renderActionBox`, sau phần tô nền. Cùng họ với lỗi "thứ tự vẽ" đã gặp ở nhãn lưới,
  nhưng lần này là tự mình đè lên mình.
- **Ký hiệu vẽ thay cho chữ ở các nút nhỏ** (`UiButton.Mark`: PLUS / MINUS / CENTRE). Lý do không chỉ
  là thẩm mỹ: một glyph từ font **nằm ở đâu là do metrics của chính nó quyết định**, nên `+`, `-` và
  `CTR` mỗi cái lệch một kiểu trong ô 12px. Vẽ thì cả ba căn theo **cùng một phép tính**, và dùng
  `(w - 1) / 2` để tâm rơi đúng pixel giữa thay vì lệch nửa pixel.
- **Dấu `x` đóng bảng đổi thành dấu trừ**, cùng cơ chế vẽ nên chắc chắn nằm giữa ô.
- **Nội dung bảng sát đỉnh**: `PANEL_TITLE` = 10 mà tiêu đề đã chiếm 2..10 → nội dung dính ngay dưới
  chữ. Nâng lên **18**.
- **Viền thẻ đang chọn là xám**: thẻ mục tiêu gọi `outline(...)` bản 4 tham số, vốn dùng
  `TabletTheme.LINE` — màu kẻ chung. Truyền màu nhấn vào.
- **Chấm người chơi thành ký hiệu có hướng**: cuống theo hướng nhìn + hai ngạnh vuốt về sau. Ghi lại
  phần dễ sai: Minecraft đo yaw **từ hướng nam**, còn bản đồ vẽ **+Z xuống dưới** — hai điều đó triệt
  tiêu nhau, nên `(-sin, cos)` dùng thẳng được cho `(x, y)` màn hình mà không cần hiệu chỉnh thêm.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không thiếu khoá dịch).
  **Chưa test tay.**
- **Cần người dùng kiểm**: (1) gạch ngăn cách đều **trên bằng dưới** và rộng hơn hẳn; (2) rail phải
  giờ **có** gạch; (3) ba nút zoom/centre và dấu trừ nằm chính giữa ô; (4) nội dung bảng không còn
  dính tiêu đề; (5) thẻ đang chọn viền **màu**; (6) ký hiệu người chơi quay đúng hướng đang nhìn.

### 2026-08-15 — Ô cạnh lẻ, chevron thay mũi tên, tiêu đề có khoảng thở
- **Ký hiệu trong nút zoom lệch tâm — KHÔNG phải giới hạn Minecraft, là số học của tôi.** Ô rộng
  **12px** có các pixel `x..x+11`, tâm rơi **giữa** pixel thứ 5 và 6 (`x+5.5`). Một ký hiệu vẽ bằng
  pixel chỉ đặt được ở `x+5` hoặc `x+6` → luôn lệch **nửa pixel**, mà ở GUI Scale 3 nửa pixel logic
  là **1,5 pixel thật**, đủ để mắt thấy.
  - **Lời giải: ô phải có cạnh lẻ.** `MAP_KEY` 12 → **13**: pixel `x..x+12`, tâm là `x+6`, một pixel
    có thật. Nút đóng bảng dùng chung hằng số này.
  - **Quy tắc tổng quát đáng nhớ**: bất cứ thứ gì căn giữa bằng pixel thì hộp chứa nó phải **cùng
    tính chẵn lẻ** với thứ được căn. Đây là biến thể thứ hai của cùng một chuyện trong hai lượt —
    lần trước là khe chẵn không chia đôi được quanh gạch 1px.
- **Mũi tên người chơi dị dạng**: nó là **hai ký hiệu tranh nhau vài pixel** (cuống + ngạnh), và tệ
  hơn, ngạnh xoay bằng một công thức **không khớp** với công thức lái cuống — nên cả hình vẹo. Thay
  bằng **một chevron chữ V duy nhất**, hai cánh xoay từ **cùng một vector hướng**, không còn hai
  nguồn để lệch nhau.
- **Khe rail 19px quá rộng** → `GAP_MD * 2 + 1` = **13**, vẫn lẻ để chia đôi đúng quanh gạch.
- **Tiêu đề bảng dính đỉnh**: nâng từ `GAP_XS` lên `GAP_MD`. `PANEL_TITLE` = 18 đã đủ chứa.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không thiếu khoá dịch).
  **Chưa test tay.**
- **Cần người dùng kiểm**: (1) tiêu đề bảng có khoảng thở với đỉnh; (2) khe rail vừa mắt chưa; (3)
  ký hiệu người chơi là chữ V gọn, quay đúng hướng; (4) ba ký hiệu zoom/centre và dấu trừ **nằm đúng
  tâm ô**.

### 2026-08-15 — HỒI QUY: không gõ được toạ độ. Cộng vẽ vector, tự xuống dòng
- **Lỗi nghiêm trọng nhất, và là hồi quy do chính tôi gây ra ở đợt 3A.** Ba ô nhập toạ độ không gõ
  được: con trỏ nhấp nháy rồi biến mất.
  - **Nguyên nhân**: `rebuild()` **xoá và tạo lại** mọi `UiField`. Ở đợt 3A tôi thêm việc **hỏi lại
    roster 5 tick/lần** để thẻ xe có phần tử bắn sống — mà mỗi câu trả lời làm tăng
    `TabletClientData.version()`, tức **kích hoạt `rebuild()` 4 lần mỗi giây**. Ô nhập bị thay mới
    liên tục, cuốn theo cả nội dung đang gõ lẫn tiêu điểm.
  - **Đáng chú ý**: tài liệu này đã ghi đúng bài học đó từ **Phase 3 lần sửa 1** ("`init()` giữ lại
    nội dung đang gõ khi dựng lại widget"). Cơ chế giữ đó **bị mất trong một lần viết lại giao diện**
    và không ai nhận ra, vì lúc ấy chưa có gì gọi `rebuild()` thường xuyên. **Một tính năng mới
    (làm mới định kỳ) đã hồi sinh một lỗi cũ đã từng được sửa.**
  - **Khắc phục**: chụp lại giá trị ba ô + ô nào đang có tiêu điểm **trước khi** xoá, đặt lại sau khi
    dựng. Chiều cao mặc định của người chơi giờ chỉ điền **lần đầu mở bảng** — trước đây nó ghi đè ô
    Y mỗi lần dựng, tức vài lần mỗi giây.
- **Ký hiệu người chơi chuyển sang vẽ vector.** Người dùng hỏi thẳng có làm được không — **có**:
  `Ui.triangle` đẩy ba đỉnh cho card qua `Tesselator` + `POSITION_COLOR`. Mọi thứ khác trong file này
  dựng từ ô chữ nhật thẳng trục, vốn tốt cho hộp và đường kẻ nhưng **vô vọng với hình biết xoay** —
  đó là lý do mọi phiên bản trước của mũi tên đều vẹo ở một góc nào đó. Giờ cạnh được phân giải ở
  **độ phân giải màn hình**, không phải độ phân giải giao diện.
  - **Bẫy đã xử lý**: `GuiGraphics` gom các lệnh `fill` lại rồi mới xả, còn lệnh vẽ này **chạy ngay**.
    Không gọi `g.flush()` trước thì tam giác chui xuống dưới mọi thứ đã xếp hàng trước nó.
- **Tự xuống dòng cho thông báo trong bảng** (`Ui.textWrapped`). Bảng thì hẹp mà thông báo là **câu
  văn**; cắt cụt ở mép bảng là mất đúng nửa nói phải làm gì.
- **Sửa nhỏ**: rail phải cao hơn hàng phím một khe thừa → `ACTION_HEIGHT` tính từ ba phím và hai khe
  thay vì ba bước. Chú thích nút thu bảng đổi từ `x` sang **`Minimise`**.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không thiếu khoá dịch).
  **Chưa test tay.**
- ⚠️ **Boot sạch KHÔNG chứng minh được phần vẽ vector**: nó chỉ chạy khi tablet mở ra và bản đồ vẽ.
  Nếu ký hiệu người chơi mất hẳn hoặc màn hình có vệt lạ thì nguyên nhân gần như chắc chắn nằm ở
  trạng thái shader quanh `Ui.triangle`.
- **Cần người dùng kiểm**: (1) **gõ được toạ độ vào cả ba ô**, chữ không bị mất khi đang gõ; (2) ký
  hiệu người chơi mượt ở mọi hướng và mọi mức zoom; (3) không có vệt màu lạ trên bản đồ; (4) thông
  báo dài trong bảng tự xuống dòng, không tràn mép; (5) nút ARC sát đáy rail phải; (6) tooltip nút
  thu bảng ghi "Minimise".

### 2026-08-15 — Vector thất bại, quét dòng thay thế. Dồn nút hành động về rail trái
- **Ký hiệu người chơi vẽ bằng GPU KHÔNG hiện gì.** `Ui.triangle` qua `Tesselator` +
  `POSITION_COLOR` biên dịch sạch, boot sạch, và trên màn hình không có gì.
  - **Quyết định về phương pháp, đáng ghi**: trạng thái quanh một lời gọi như thế — shader nào đang
    gắn, blend bật hay tắt, nó rơi vào đâu so với một mẻ `fill` chưa xả — là thứ **không suy luận
    được từ bên ngoài một game đang chạy**. Tôi có thể đoán và bảo người dùng test lại, nhiều lần.
    Thay vào đó **đổi sang đường vẽ mà phần còn lại của giao diện đã chứng minh là chạy**: tự **quét
    dòng** tam giác thành các `g.fill` ngang, mỗi hàng tính nhịp từ **cạnh thật ở đúng độ cao đó**.
  - Nó vẫn xoay mượt: hình được phân giải **theo từng hàng** chứ không phải lắp từ các ô chữ nhật đã
    xoay — đó mới là nguyên nhân khiến mọi phiên bản trước bị vẹo, chứ không phải chuyện GPU hay không.
  - **Bài học**: khi không quan sát được trạng thái, đừng đoán — chọn con đường đã có bằng chứng.
- **Ô nhập tràn khi toạ độ dài**: giờ **cuộn ngang**, giữ phần đuôi trong tầm nhìn và cho phần đầu
  trôi ra — hiện phần đầu thì giấu mất chính chữ số đang gõ.
- **Nhãn lưới đè lên nút**: `isReserved` chỉ kiểm **điểm neo** của nhãn, nên một nhãn bắt đầu ngoài
  vùng chừa vẫn thò **hai chữ số cuối** vào dưới rail. Giờ kiểm **cả hai đầu** của chuỗi.
- **Đề xuất lớn của người dùng: dồn FIRE/MOD/ARC về rail trái.** Đã làm, và cách xếp có lý do:
  - **Nhóm xem (BTY/TGT/AMO/STA/LOG) neo ở ĐỈNH rail; nhóm hành động (MOD/ARC/FIRE) neo ở ĐÁY**, FIRE
    dưới cùng.
  - **Vì sao tách xa nhất có thể**: phím bấm thường xuyên nhất là phím chuyển view, còn FIRE là thao
    tác **không rút lại được**. Đặt chúng cạnh nhau là công thức bấm nhầm. Neo hai đầu cũng khiến bố
    cục không vỡ khi cửa sổ thấp — hai nhóm chỉ gặp nhau ở cỡ cực nhỏ thay vì tràn ngay.
  - **Bên phải giải phóng hoàn toàn**: bỏ hẳn `actionArea`, `buildActionBox`, `renderActionBox`,
    `ACTION_HEIGHT`. Bản đồ chạy tới **mép phải**.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không thiếu khoá dịch).
  **Chưa test tay.**
- **Cần người dùng kiểm**: (1) **ký hiệu người chơi đã hiện chưa**, có mượt khi xoay không; (2) gõ
  toạ độ dài — chữ cuộn trong ô, không tràn; (3) nhãn lưới không còn đè lên rail; (4) ba nút
  MOD/ARC/FIRE ở đáy rail trái, FIRE dưới cùng và vẫn đỏ; (5) bản đồ có chạy tới mép phải không;
  (6) gạch ngăn có ở cả hai nhóm phím.

### 2026-08-15 — Vì sao ký hiệu bị răng cưa, và cách JourneyMap/Xaero né được
- **Người dùng hỏi thẳng: làm sao để đường thẳng / vòng tròn / mũi tên mượt như JourneyMap hay Xaero.**
  Đây là câu hỏi đúng, và câu trả lời giải thích luôn tại sao ba lần sửa mũi tên trước đều thất bại.
- **Nguyên nhân thật, không phải ở hình**: toàn bộ giao diện vẽ ở **điểm ảnh logic** rồi Minecraft
  **phóng to 3 lần**. Một chevron 6px được dựng trên lưới 6px rồi phóng thành 18px — **bậc thang là
  của phép phóng**. Đổi cách dựng hình (rect → GPU → quét dòng) không sửa được điều đó, vì cả ba đều
  dựng trên cùng cái lưới thô.
- **JourneyMap/Xaero mượt vì chúng vẽ mũi tên bằng texture PNG xoay theo ma trận**, không phải bằng
  hình học — texture có biên alpha mềm nên xoay thế nào cũng mượt.
- **Cách đã chọn, rẻ hơn nhiều và dự án đã có sẵn kinh nghiệm**: `Ui.inDevicePixels` — vẽ **riêng các
  ký hiệu bản đồ** trong ma trận nhân `1/guiScale`, toạ độ nhân ngược lại. Chevron giờ có **18 điểm
  ảnh thật** để xoay thay vì 6.
  - **Vì sao áp riêng cho ký hiệu là hợp lý**: lý do dự án từng **bỏ** hướng điểm ảnh vật lý là
    **chữ** — font bitmap nướng cho lưới logic. Ký hiệu thì không phải chữ, nên lý do đó không áp
    dụng. Javadoc của helper ghi rõ **cấm cho chữ đi qua**.
  - **Giới hạn phải nói rõ**: đây **không phải khử răng cưa thật**. Biên vẫn cứng, chỉ là cứng ở cỡ
    một điểm ảnh màn hình thay vì một khối đã phóng. Muốn mượt thật thì phải đi đường texture có
    biên alpha mềm như hai mod kia.
- **Cụm khai hoả chuyển lên ĐỈNH rail, FIRE trên cùng** (người dùng đổi ý so với lượt trước). Lập
  luận đảo lại: **tầm với thắng khoảng cách an toàn** — FIRE là phím cả thiết bị tồn tại để bấm, chôn
  nó ở đáy cột là đẩy nó xa tay nhất. Khe rộng giữa hai nhóm giữ vai trò cảnh báo, và FIRE vẫn là
  phím đỏ duy nhất, vẫn từ chối kích hoạt khi chưa chọn mục tiêu.
- **Bảng rộng thêm**: `PANEL_WIDTH` 106 → **126**.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không thiếu khoá dịch).
  **Chưa test tay.**
- **Cần người dùng kiểm**: (1) **ký hiệu người chơi có hiện và có mượt hơn rõ rệt không** — đây là
  phép thử của cả hướng device-pixel; (2) FIRE trên cùng rail, MOD/ARC dưới nó, view ở dưới cùng;
  (3) bảng rộng hơn, thông tin đỡ chèn ép; (4) gạch ngăn đúng chỗ ở cả hai nhóm.

### 2026-08-15 — `FIRE` tràn ô, đổi thành `FFE`
- **Chữ `FIRE` tràn khỏi ô vuông 22px.** Số học font xác nhận: `F`=6 + `I`=4 + `R`=6 + `E`=6 =
  **đúng 22px**, bằng cả bề rộng ô — mà viền ăn 1px mỗi bên, nên thừa đúng 2px. Đây là hệ quả tất yếu
  của việc chuyển sang phím vuông cỡ ba ký tự: **mọi nhãn phải là ba ký tự**, và `FIRE` là cái duy
  nhất còn sót bốn.
- **Đổi thành `FFE` — Fire For Effect.** Đây là **khẩu lệnh thật của pháo binh NATO**, nghĩa đúng là
  *bắn hiệu lực*, tức chính việc phím này làm. Nó cũng khớp quy ước ba ký tự của mọi phím còn lại
  (BTY/TGT/AMO/STA/LOG/MOD/ARC) và rộng 18px nên còn dư 1px mỗi bên.
  - **Sai lệch nhỏ đã cân nhắc**: trong doctrine, FFE đi **sau** giai đoạn hiệu chỉnh
    (adjust fire → FFE). Ta không có giai đoạn hiệu chỉnh nên dùng hơi rộng nghĩa — chấp nhận, vì nó
    là khẩu lệnh "bắn thật" mà ai trong nghề cũng hiểu ngay, và tooltip nói đủ nghĩa.
  - Phương án thay thế đã cân nhắc rồi bỏ: `EXE` (execute) — rõ nhưng không mang chất pháo binh.
- **Tooltip viết đủ nghĩa** để cái tắt không phải đoán: *"Fire for effect — T3"*, và khi chưa chọn
  mục tiêu thì *"Fire for effect — select a target first"*.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không thiếu khoá dịch).
  **Chưa test tay.**

### 2026-08-15 — Rail căn giữa, thêm phím ADJ, và bàn về bộ icon PNG
- **Cả khối phím căn giữa theo chiều dọc rail** (người dùng đề xuất, tôi đồng ý). Neo ở đỉnh thì nó
  đọc như thứ **rơi về một đầu cột**; căn giữa thì rail đọc như một bảng khí cụ, và phím nằm gần chỗ
  con trỏ vốn đã ở đó thay vì ở mép. Có kẹp `Math.max` để cửa sổ thấp thì tụt về đỉnh chứ không tràn.
- **Thêm phím `ADJ` — Adjust Fire.** Trong pháo binh thật, hiệu chỉnh là **một viên thăm dò, quan sát,
  rồi mới FFE**. Cặp `ADJ → FFE` chính là hai nửa của một call for fire chuẩn, nên hai phím này đứng
  cạnh nhau ở đầu nhóm.
  - **Không cần cơ chế mới**: `FireCommandMessage` vốn đã nhận `FireMode` làm tham số, nên ADJ chỉ là
    một lệnh bắn **ép về SINGLE** bất kể chế độ đang đặt. Cả tính năng là một tham số bị ghi đè.
  - Nhật ký ghi `ADJ` thay vì `SINGLE`, để đọc lại thấy rõ đâu là viên thăm dò.
  - **Chưa làm, và nói rõ**: phần *hiệu chỉnh* thật (ADD 200 / LEFT 50 rồi bắn lại) cần giao diện
    nhập lượng sửa. Hiện người chơi tự nhìn điểm rơi rồi dời mục tiêu — đúng về mặt thao tác, chỉ là
    thủ công.
- **Nhóm hành động lên 4 phím** (FFE, ADJ, MOD, ARC) khiến tổng chiều cao vượt rail → `RAIL_GAP` giảm
  từ 13 xuống **9**, vẫn lẻ để chia đôi đúng quanh gạch 1px. Đây là lần thứ hai một phím mới ép phải
  tính lại chiều cao cả cột; giờ chiều cao được **tính ra từ số phím** chứ không viết tay.
- **Đã bàn với người dùng về bộ icon PNG** (chưa làm): xem danh sách ưu tiên trong trao đổi. Điểm kỹ
  thuật quan trọng nhất đã nêu: vẽ **trắng trên nền trong suốt**, để code tô màu bằng
  `setColor` — một file phục vụ được cả xanh quân ta / đỏ đối phương / hổ phách cảnh báo, thay vì ba file.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không thiếu khoá dịch).
  **Chưa test tay.**
- **Cần người dùng kiểm**: (1) khối phím nằm giữa rail theo chiều dọc; (2) `ADJ` hiện đúng chỗ, mờ
  khi chưa chọn mục tiêu; (3) bấm ADJ **chỉ bắn một viên từ một khẩu**, kể cả khi mode đang là
  SALVO/RIPPLE; (4) nhật ký ghi `ADJ`; (5) khe phím hẹp lại có còn thoáng không.

### 2026-08-15 — Sửa hiểu sai về ADJ: nó là LAY, không phải viên thăm dò
- **Tôi hiểu sai yêu cầu ở lượt trước.** Người dùng muốn ADJ = *"xác nhận mọi khẩu đã vào đúng vị trí
  trước khi khai hoả"*. Tôi làm thành *"bắn một viên thăm dò"* (ép mode về SINGLE) — sai hẳn nghĩa.
- **Đáng ghi: đây chính là `LAY` đã thống nhất từ 2026-08-14 mà chưa bao giờ làm.** Người dùng tự đi
  tới lại đúng khái niệm đó và gọi nó bằng tên khác. Một mục còn nợ nằm im đủ lâu thì nó sẽ quay lại
  dưới dạng một "yêu cầu mới".
- **Đã làm — không cần cơ chế mới**: một lệnh lay **giống hệt** một lệnh bắn cho tới đúng khoảnh khắc
  nòng dừng lại, nên nó là **một cờ trên `FireCommandMessage`** chứ không phải một đường đi riêng.
  Tới điểm đó, thay vì `waitUntilReadyThenShoot` thì báo trạng thái và dừng.
  - **Lay luôn là cả khẩu đội**: bỏ qua cả việc thu về một khẩu của `SINGLE` lẫn độ trễ so le của
    `RIPPLE` — chế độ bắn nói *rót đạn thế nào*, mà ở đây không có viên nào được rót.
  - Nòng **tự giữ hướng** sau đó: `setTarget` để `lockTurret` trống nên `baseTick` của SBW tiếp tục
    lái nòng về vector đã lệnh mỗi tick, cho tới khi có lệnh mới thay thế.
- **Thêm trạng thái `LAID`, không dùng lại `WAITING`.** Hai thứ này **ngược nhau** trên một bảng điều
  khiển hoả lực: `WAITING` là khẩu **muốn bắn mà không bắn được** (hết đạn / chưa hết cooldown) —
  một vấn đề; `LAID` là khẩu **đã sẵn sàng và đang chờ lệnh** — điều tốt. Cho chúng đọc giống nhau là
  nói dối người chỉ huy. Màu cũng ngược: `LAID` xanh lục, `WAITING` hổ phách.
- **Cặp thao tác giờ đúng doctrine**: `ADJ` (lay cả khẩu đội, giữ nòng) → nhìn xác nhận → `FFE`
  (khai hoả). Lợi ích thật: đạn rời nòng **cùng lúc** thay vì nhỏ giọt theo thứ tự từng khẩu xoay xong.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không thiếu khoá dịch).
  **Chưa test tay.**
- **Cần người dùng kiểm**: (1) bấm ADJ → **mọi khẩu xoay nòng và dừng lại, KHÔNG bắn**; (2) thẻ xe
  hiện `LAID` màu xanh khi đã vào vị trí; (3) sau đó bấm FFE → bắn ngay, không phải chờ xoay lại;
  (4) ADJ khi mode là SALVO/RIPPLE vẫn lay **tất cả**, không thu về một khẩu; (5) `WAITING` (hết đạn)
  vẫn màu hổ phách và đọc khác hẳn `LAID`.

### 2026-08-15 — Rail chạy suốt mặt máy, thanh trên dày lên, thanh dưới mỏng đi
- **Rail giờ chiếm trọn mép trái từ đỉnh tới đáy thiết bị**, thanh tiêu đề bắt đầu **sau** rail thay
  vì trải ngang bên trên nó. Khối phím vì thế căn giữa theo **cả mặt máy**, không phải theo phần còn
  lại dưới tiêu đề. Đây là thay đổi về *thứ chiếm chỗ nào*, không phải về trang trí: rail là bộ điều
  khiển của thiết bị, nên nó là một cạnh liền mạch chứ không phải thứ nhét vào dưới một cái mũ.
- **Thanh trên 15 → 24, thanh chi tiết 22 → 12.** Người dùng nhận xét: từ khi có cửa sổ pop-up cho
  từng khẩu, thanh chi tiết dưới đáy gần như **không còn việc gì** — thông tin chi tiết đã có chỗ tốt
  hơn. Giữ lại vì còn dùng sau, nhưng trả bề dày lại cho thanh trên.
- **Thanh chi tiết đổi từ hai dòng sang một dòng**: cao 12px thì không xếp nổi nhãn nhỏ **trên** giá
  trị nữa, nên giờ là `NHÃN giá_trị` nằm ngang. Nếu chỉ hạ chiều cao mà giữ nguyên cách vẽ thì dòng
  giá trị sẽ bị cắt cụt — đúng loại lỗi "đổi kích thước mà quên nội dung bên trong" đã dính vài lần.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không thiếu khoá dịch).
  **Chưa test tay.**
- **Cần người dùng kiểm**: (1) rail chạy suốt từ đỉnh tới đáy, tiêu đề nằm bên phải nó; (2) khối phím
  căn giữa theo cả chiều cao thiết bị; (3) thanh trên dày hơn, chữ trong đó vẫn căn giữa theo chiều
  dọc; (4) thanh chi tiết mỏng, nội dung nằm **một dòng** và không bị cắt; (5) bảng và bản đồ vẫn bắt
  đầu đúng sau rail, không bị lệch.

### 2026-08-15 — Viền trên đứt ở góc rail, và bàn về thẩm mỹ khung máy
- **Người dùng phát hiện đường viền xanh trên cùng bị đứt đúng đoạn rail.** Nguyên nhân là **thứ tự
  vẽ**: nền rail vẽ **sau** đường viền nên tô đè lên 26px đầu tiên của nó. Chuyển nền rail lên trước
  phần kẻ viền → đường viền liền một mạch từ góc này sang góc kia.
  - **Đây là lần thứ ba lỗi "tô nền sau khi kẻ" trong dự án** (nhãn lưới, gạch rail phải, giờ là viền
    trên). Mẫu hình đã đủ rõ để thành luật: **mọi mảng nền phải vẽ xong trước khi kẻ bất cứ đường
    nào**, không xen kẽ.
- **Thêm hai thứ rẻ mà đổi hẳn cảm giác chiều sâu**:
  - **Vùng sống lõm xuống**: hairline tối dọc mép trên + trái, hairline sáng dọc mép dưới + phải. Đó
    là cách một hình chữ nhật phẳng được đọc thành *lõm*, vì đúng là những gì một chỗ lõm làm dưới
    ánh sáng chiếu từ trên. Bốn lệnh `fill`, và bản đồ thôi trông như được sơn lên cùng mặt phẳng với
    hàng phím quanh nó.
  - **Hairline dọc mép phải rail**, để cột phím đọc như một **cạnh nhô của vỏ máy** chứ không phải
    một mảng màn hình tối màu hơn.
- **Cố ý làm rẻ**: khung máy thật là **ảnh nine-slice** — vốn đã nằm trong danh sách còn tồn đọng từ
  lâu. Phần vẽ tay này chỉ đứng tạm cho tới khi có ảnh đó, nên không đáng đầu tư thêm.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không thiếu khoá dịch).
  **Chưa test tay.**

### 2026-08-15 — Bản đồ: một texture mỗi ô, thay vì dựng lại một texture lớn mỗi khung hình
- **Người dùng hỏi vì sao World Map của Xaero mượt như trên 120FPS.** Câu trả lời (dựa trên chính đợt
  dịch ngược Xaero của dự án hồi 13–14/08): **Xaero không dựng lại điểm ảnh nào khi kéo hay zoom.**
  Bản đồ của họ đã sẵn là **một tập texture GPU, mỗi tile-chunk 64×64 block một cái**
  (`MapTileChunk.getLeafTexture().getGlColorTexture()`), và vẽ một khung hình là vẽ lại 8×8 quad mỗi
  region ở toạ độ thế giới tương ứng. Kéo = cộng toạ độ đích. Zoom = nhân ma trận. **Không có công
  việc CPU nào.**
- **Ta làm ngược lại**: giữ ô dưới dạng *dữ liệu* rồi **dựng lại một texture 512×512 trên CPU mỗi khi
  khung nhìn đổi**. Đã vá triệu chứng hai lần (đọc theo hàng, giãn nhịp quét) nhưng **chưa đụng
  nguyên nhân** — kéo chuột vẫn là một lần dựng lại đầy đủ mỗi khung hình.
- **Đã đổi kiến trúc `TerrainImage`**: mỗi ô 64×64 giờ là **một `DynamicTexture` riêng, dựng đúng một
  lần** khi ô về, sau đó chỉ còn `blit` vào vị trí tính ra. Điểm ảnh của một ô **chỉ đổi khi dữ liệu
  của chính ô đó đổi**, mà điều đó hiếm.
- **Ba chi tiết bắt buộc phải đúng, ghi lại kẻo quên:**
  1. **Đổ bóng buộc phải nướng vào ô, mà đổ bóng cần cột phía bắc** — vốn nằm ở ô bên cạnh. Đây
     **chính là lý do bản cũ dùng một texture lớn**. Cách gỡ: lúc dựng, đọc **hàng dưới cùng của ô
     phía bắc**; nếu ô đó chưa về thì đánh dấu `northKnown = false` và **dựng lại khi nó tới**. Thiếu
     mắt xích này thì cứ 64 block lại lộ một đường nối.
  2. **Hai mép của ô phải quy đổi riêng**, không lấy "góc + bề rộng". Làm tròn bề rộng độc lập với vị
     trí khiến hai ô cạnh nhau bất đồng về chỗ đường biên chung nằm ở đâu — hiện ra thành **khe nứt
     1px** chạy ngang bản đồ.
  3. **`tileAt` trả về đúng thể hiện đã cache**, nên so sánh danh tính (`sheet.source == tile`) là
     phép kiểm "dữ liệu có mới không" đủ tin. Nếu nó dựng lại đối tượng mỗi lần gọi thì cả thiết kế
     này sụp — mỗi khung hình một lần dựng lại, tệ hơn bản cũ.
- **Giới hạn bộ nhớ**: tối đa 512 texture sống (16 KB/cái ≈ 8 MB), LRU thả cái lâu không vẽ nhất —
  đúng lý do Xaero có `bumpLoadedRegion`.
- **CHƯA làm, và phải nói rõ**: thay đổi này bỏ **chi phí CPU** (nguồn giật), nhưng bản đồ **vẫn nhích
  theo bậc một block** khi kéo, vì `MapPanel` vẫn giữ tâm là `BlockPos` nguyên. Đó là mục tồn đọng số
  4 (kéo mượt dưới mức một block) và giờ nó **rẻ hơn nhiều** vì đích của mỗi quad đã là số tính ra.
  Cố ý tách ra: người dùng yêu cầu làm một mình việc này.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). **Chưa test tay.**
- **Cần người dùng kiểm**: (1) **kéo bản đồ có hết giật không** — đây là phép thử chính; (2) zoom ra
  vào có mượt không; (3) **có khe nứt hay đường nối nào giữa các ô không** (mục 1 và 2 ở trên);
  (4) đổ bóng địa hình còn liền mạch qua mép ô không; (5) kéo đi thật xa rồi quay lại — địa hình có
  vẽ lại đúng không (phép thử của LRU); (6) vùng chưa khảo sát vẫn trong suốt cho lưới xuyên qua.

### 2026-08-15 — Zoom xa lag nặng: thiếu tầng chi tiết. Thêm kim tự tháp mức
- **Người dùng test: ≤512m mượt, cao hơn thì cực kỳ lag.** Con số chỉ thẳng nguyên nhân — số ô hiển
  thị tăng theo **bình phương** span: 512m → 64 ô, 2048m → 1024, 4096m → 4096, 16384m → **65 536**.
  Mà giới hạn là 512 texture, nên từ 1024m trở lên **mỗi khung hình dựng cả nghìn texture rồi vứt đi
  cả nghìn**. Không phải chậm — là tự huỷ.
- **Đây chính là mảnh còn thiếu so với Xaero, và nó có tên**: `MapRegion extends **LeveledRegion**` —
  ghi trong nhật ký từ 13/08 mà lúc đó tôi không hiểu ý nghĩa. Zoom xa thì Xaero vẽ **một** texture
  cho cả vùng, không phải 64 texture con. Kim tự tháp mức chi tiết.
- **Đã làm**: mỗi mức phủ **gấp 8 lần** diện tích của mức dưới với **1/8 độ chi tiết**, texture vẫn
  64×64. Khung nhìn chọn mức sao cho số quad ngang luôn ≤ 16.
  | Span | Mức | Ô/texture | Số quad |
  |---|---|---|---|
  | 512m | 0 | 64 block | 81 |
  | 1024m | 0 | 64 block | 289 |
  | 4096m | 1 | 512 block | 81 |
  | 16384m | 2 | 4096 block | 25 |
  Từ 65 536 quad xuống **tối đa 289**, và không bao giờ vượt trần texture nữa.
- **Cái bẫy đã bịt trước khi giao, không phải sau**: sheet thô dựng lại theo `version()` của cache —
  mà version **nhảy mỗi lần bất kỳ ô nào về**, và zoom xa thì client xin ô liên tục. Gate bằng version
  không thôi sẽ đưa thẳng về lại lỗi vừa sửa, chỉ khác tầng. Thêm **cooldown 1,5 giây mỗi sheet**;
  đất mới hiện chậm một hai giây ở mức zoom đó là không nhìn ra.
- **Đổ bóng ở mức thô** tra qua `heightAt` của cache thay vì qua dữ liệu của chính sheet, nên vệt sáng
  tối chạy liền qua mép sheet thay vì đứt ở mọi đường nối.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). **Chưa test tay.**
- **Cần người dùng kiểm**: (1) **zoom 1024m trở lên đã hết lag chưa**; (2) chuyển giữa các mức
  (1024→2048, 8192→16384) có thấy nhảy chi tiết đột ngột không; (3) ở mức thô, đất mới có hiện ra sau
  một hai giây không; (4) có khe nứt hay đường nối nào ở mức thô không; (5) mức 512m trở xuống vẫn
  mượt như trước chứ.

### 2026-08-15 — Vẫn lag: thủ phạm thật là quét tìm ô, không phải phần vẽ
- **Người dùng báo vẫn giật sau khi thêm kim tự tháp mức.** Tôi đã đoán nguyên nhân **hai lần** và
  trúng cả hai (dựng lại texture, số quad bùng nổ) — nhưng cả hai đều không phải thứ lớn nhất. Lần
  này đi liệt kê **mọi thứ chạy mỗi khung hình** trong đường vẽ bản đồ thay vì đoán tiếp.
- **Thủ phạm: `TerrainClientCache.ensureCovered`.** Nó duyệt **mọi ô 64-block trong khung nhìn**, cấp
  phát một mảng cho mỗi ứng viên, rồi **sắp xếp** cả danh sách theo khoảng cách tới tâm.
  - Ở span 16384m: **65 536 vòng lặp, hàng chục nghìn lần cấp phát, và một lần sort** — **mỗi khung
    hình khi kéo**.
  - Nó chạy mỗi khung hình vì điều kiện là *"khung nhìn đã đổi HOẶC quá 500ms"*. Kéo chuột thì khung
    nhìn đổi mỗi khung hình, nên vế thứ hai không bao giờ có tác dụng. **Cái throttle tôi viết không
    hề throttle gì cả.**
  - Việc này **có từ trước cả hai lượt tối ưu vừa rồi**; nó bị che khuất vì lúc đó dựng lại texture
    còn tốn hơn. Bỏ chi phí lớn hơn đi thì chi phí kế tiếp lộ ra — và tôi đã không đi tìm nó.
- **Hai lỗi trong một, sửa cả hai:**
  1. **Kích hoạt sai điều kiện** → chỉ còn theo đồng hồ. Chậm nửa giây khi xin đất mà chưa ai kéo tới
     là không nhìn ra.
  2. **Phạm vi quét không có trần** → kẹp ở **48 ô mỗi trục** quanh tâm khung nhìn (~3000 block). Quá
     mức đó thì bản đồ hiện những gì đang có. Việc xin toàn bộ 65 nghìn ô vốn **vô nghĩa**: mỗi lượt
     chỉ xin vài chục ô, và server không thể giao hết phần còn lại.
- **Bài học về phương pháp, đáng ghi hơn cả bản sửa**: khi bỏ đi chi phí lớn nhất, **chi phí kế tiếp
  trở thành chi phí lớn nhất**. Sau mỗi lần tối ưu phải **liệt kê lại** những gì còn chạy mỗi khung
  hình, chứ không mặc định là đã xong. Ba lượt liền tôi sửa đúng một thứ rồi dừng, thay vì hỏi *"giờ
  thì cái gì còn chạy mỗi khung hình?"*
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). **Chưa test tay.**
- **Cần người dùng kiểm**: (1) **kéo ở 4096m và 16384m đã hết giật chưa** — đây là phép thử; (2) đất
  mới có còn hiện ra khi kéo tới vùng chưa xem (chậm nửa giây là bình thường); (3) ở zoom cực xa, rìa
  bản đồ trống hơn trước là **hành vi đúng** — ngoài 3000 block quanh tâm thì không xin nữa.

### 2026-08-15 — Giật ở dải zoom GIỮA: ba lỗi trong hàm dựng sheet thô
- **Người dùng đo được thứ loại bỏ giả thuyết của tôi**: 512m mượt, 16000m mượt, **ở giữa thì giật
  cục**. Số quad không giải thích nổi — 2048m chỉ có **25 quad** mà giật, còn 512m có **81 quad** thì
  mượt. Vậy chi phí không nằm ở phần vẽ mà ở **việc dựng sheet thô**, thứ chỉ tồn tại ở dải giữa.
  - Ghi lại vì đây là kiểu dữ liệu tốt nhất người dùng có thể đưa: **hai đầu đều ổn, giữa thì hỏng**
    loại bỏ mọi nguyên nhân tỉ lệ thuận với kích thước, và chỉ vào thứ *chỉ xảy ra ở khoảng giữa*.
- **Lỗi 1 — tôi tự phá chính tối ưu mình vừa viết.** Hàm dựng cache thể hiện ô theo hàng để tránh tra
  map mỗi điểm ảnh... rồi **ngay dòng sau** gọi `heightAt` cho mẫu phía bắc, mà hàm đó **tra map mỗi
  lần gọi**. Bốn nghìn lần tra mỗi sheet. Javadoc của chính `tileAt` cảnh báo đúng điều này
  (*"vài nghìn lần tra thay vì gần một triệu"*) — tôi đọc nó, viết cache theo nó, rồi vô hiệu hoá nó
  ở dòng kế tiếp. Giờ hàng phía bắc có handle riêng.
- **Lỗi 2 — đất trống bị quét lại toàn bộ mỗi khung hình.** Sheet không có dữ liệu bị `release` rồi
  `remove`, nên khung hình sau lại quét đủ 4096 điểm ảnh để lại phát hiện không có gì. Ở zoom rộng
  **phần lớn khung nhìn là đất chưa khảo sát**, tức phần lớn sheet, mỗi khung hình. Giờ giữ lại và
  đánh dấu `empty`.
- **Lỗi 3 — các lần dựng lại đồng bộ với nhau.** Mọi sheet vào tầm nhìn cùng một khung hình sẽ **hết
  hạn cùng một khung hình**, nên cứ 1,5 giây là một cú khựng đồng loạt. Đó chính là *"giật cục"* chứ
  không phải *"chậm"*. Giờ hạn dùng **lệch nhau theo vị trí sheet** (băm từ khoá), nên việc dựng lại
  rải đều thay vì dồn cục.
- **Bài học**: ba lỗi này đều **chỉ tồn tại ở mức thô**, nên chúng vô hình ở cả hai đầu dải zoom — mức
  0 không có sheet thô, mức 2 thì gần như mọi sheet đều trống và (nhờ lỗi 2) bị bỏ qua nhanh. Một lỗi
  hiệu năng **chỉ xuất hiện ở khoảng giữa** là dấu hiệu của một nhánh code chỉ chạy ở khoảng giữa.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). **Chưa test tay.**
- **Cần người dùng kiểm**: (1) **1024m / 2048m / 4096m / 8192m đã hết giật cục chưa**; (2) còn cú
  khựng định kỳ nào không (nếu còn, lỗi 3 chưa đủ); (3) đổ bóng ở mức thô còn đúng không — mẫu phía
  bắc vừa đổi cách tra; (4) đất mới vẫn hiện ra ở mức thô sau một hai giây.

### 2026-08-15 — Server cache ô + trả lời "không đổi" (mục 1 của kế hoạch làm mới địa hình)
- **Bối cảnh**: người dùng hỏi cập nhật hố đạn thời gian thực có ăn vào độ mượt không. Trả lời sau
  khi kiểm code: hố đạn **hiện không phải thời gian thực** — client cho câu trả lời **hết hạn sau 10
  giây** rồi hỏi lại. Và cách thăm dò đó **vừa chậm hơn vừa tốn hơn** cách đúng.
- **Ba chỗ tốn, đã xác minh chứ không đoán**:
  1. `ServerTerrainProvider` **không có cache nào** — mỗi lần làm mới là **đọc lại 16 chunk và giải
     NBT từ đầu**, kể cả khi không có gì đổi. Điểm yếu này tôi tự ghi vào bản giao ước 14/08 rồi bàn
     giao bị huỷ và nó ở lại **suốt từ đó**.
  2. Mỗi ô làm mới là một packet ~vài KB, kể cả khi nội dung y hệt.
  3. **Chỗ chạm vào độ mượt**: mỗi ô về đều tăng **`version()` toàn cục**, mà cổng dựng lại sheet thô
     so bằng chính biến đó — nên **một ô bất kỳ về ở bất kỳ đâu cũng làm mọi sheet thô hết hạn**.
     Sheet thô không bao giờ ổn định được.
- **Đã làm (mục 1)**:
  - **`ServerTileCache`**: giữ ô đã lấy mẫu 2 giây. Không phải để giữ lâu — mà để **một loạt yêu cầu
    trùng nhau, thứ mà việc kéo bản đồ sinh ra, tốn một lần khảo sát chứ không phải hàng chục**.
  - **`TerrainTile.contentHash()`** — mã băm nội dung, **định nghĩa đúng một chỗ** vì hai đầu phải
    khớp tuyệt đối. Đây là lỗi kinh niên của dự án (hai nơi tính cùng một giá trị rồi lệch nhau) nên
    lần này đặt ngay trong lớp dữ liệu dùng chung.
  - **Client gửi kèm mã băm nó đang giữ**; server trả `unchanged` — chỉ toạ độ, vài byte.
  - **`TerrainClientCache.confirm()`** ghi nhận ô còn hiệu lực mà **cố ý không tăng `version()`**.
    Đây mới là mấu chốt của độ mượt: một lần làm mới xác nhận *không có gì đổi* thì không được phép
    nói ngược lại.
  - Mã băm được **lưu lúc nhận** chứ không tính lại mỗi lượt quét — nó duyệt 12 nghìn giá trị, mà một
    lượt quét cân nhắc hàng chục ô.
- **Chưa làm (cố ý, người dùng yêu cầu chỉ làm mục 1)**: vô hiệu hoá chính xác theo ô thay vì
  `version()` toàn cục (mục 2), và đẩy theo sự kiện khi đạn chạm đất (mục 3).
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, 15 packet). **Chưa test tay.**
- **Cần người dùng kiểm**: (1) bản đồ có **mượt hơn khi đứng yên / kéo chậm** không — đây là chỗ việc
  làm mới định kỳ từng quấy rầy; (2) hố đạn **vẫn hiện** sau khoảng 10 giây chứ (không được mất tính
  năng); (3) đi tới vùng mới, đất vẫn hiện bình thường; (4) chơi một lúc lâu xem có rò bộ nhớ hay
  chậm dần không (cache server có trần 4096 ô).

### 2026-08-15 — Màu bản đồ: làm tối, thôi khử màu
- **Người dùng nhận xét màu bản đồ "không trung thực".** Đúng, và đó là **lỗi trong công thức của
  tôi chứ không phải trong ý đồ**. Ý đồ vẫn đúng: mặt đất phải nằm dưới lớp ký hiệu, nếu không thì đỏ
  và xanh dương mang nghĩa sẽ chìm nghỉm vào cỏ.
- **Sai ở cách đạt ý đồ đó**: công thức cũ kéo mỗi màu **nửa đường về xám** rồi hạ còn 2/3 sáng. Mà
  kéo về xám **chính là thứ triệt tiêu khác biệt giữa các loại mặt đất**. Đo thử trên bảng màu thật:
  | Loại | Vanilla | Công thức cũ | Công thức mới |
  |---|---|---|---|
  | cỏ | (127,178,56) | (92,108,68) | (79,110,35) |
  | nước | (64,64,255) | **(49,49,113)** | (40,40,158) |
  | cát | (247,233,163) | (158,154,130) | (153,144,101) |
  Nước ở công thức cũ ra **(49,49,113)** — xanh xám nhợt, gần như không phân biệt được với đá tối.
  Cát ra gần như xám. Cả bản đồ hội tụ về một tông xanh rêu, đúng như người dùng thấy.
- **Cách sửa: nhân đều cả ba kênh với một hệ số duy nhất.** Việc đó **giữ nguyên sắc màu và tương
  quan bão hoà** — nước vẫn là nước, cát vẫn là cát — chỉ làm cả bản đồ dịu xuống. Đạt đúng ý đồ ban
  đầu mà không phải trả giá bằng thông tin.
- **`TERRAIN_DIM` (0.62) là con số duy nhất** điều khiển diện mạo mặt đất: hạ xuống thì địa hình lùi
  sâu hơn sau ký hiệu, nâng lên gần 1 thì bản đồ thành đúng item bản đồ của game.
- **Bài học nhỏ nhưng đáng ghi**: "làm dịu để thứ khác nổi lên" và "khử màu" **không phải một việc**.
  Tôi đã gộp chúng làm một và mất thông tin địa hình suốt từ đó.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). **Chưa test tay.**
- **Cần người dùng kiểm**: (1) nước có ra **xanh dương** rõ ràng không; (2) cát, đá, cỏ, gỗ có phân
  biệt được với nhau không; (3) ký hiệu đỏ/xanh/hổ phách **vẫn nổi** trên mặt đất chứ; (4) nếu tổng
  thể quá sáng hoặc quá tối, `TERRAIN_DIM` là con số duy nhất cần chỉnh.

### 2026-08-15 — Nước hiện màu ĐỎ: kênh R và B bị hoán đổi, có từ đầu
- **Người dùng báo**: nước ra **màu đỏ**, mái gỗ sồi vàng nhạt ra **xám xanh**. Nước đỏ là manh mối
  quyết định — đỏ ở chỗ đáng lẽ xanh dương nghĩa là **hoán kênh R/B**. Kiểm bằng số:
  | Khối | Map colour | Sau khi hoán R/B | Người dùng thấy |
  |---|---|---|---|
  | Nước | (64,64,255) xanh | (255,64,64) | **đỏ** ✓ |
  | Gỗ sồi | (143,119,72) nâu | (72,119,143) | **xám xanh** ✓ |
- **Xác minh bằng `javap` trên jar Minecraft thật**, không đoán — đúng quy tắc dự án. Bytecode của
  `MapColor.calculateRGBColor` lấy `col >> 16 & 255` (**đỏ**) vào biến 3, `col & 255` (**xanh
  dương**) vào biến 5, rồi trả `0xFF000000 | biến5 << 16 | biến4 << 8 | biến3` — tức **xanh dương ở
  byte cao, đỏ ở byte thấp**.
  → **Hàm này đã trả sẵn dạng ABGR**, chính vì nó sinh ra để đưa thẳng vào `NativeImage`.
- **Lỗi**: comment trong code của tôi ghi *"bảng màu trả về RGB"* và tôi hoán R/B thêm một lần nữa —
  **đổi ngược lại thành sai**. Giờ giữ nguyên thứ tự nhận được; làm tối không cần biết kênh nào là
  kênh nào, chỉ cần áp đều cả ba.
- **Điều đáng ghi nhất — vì sao nó sống sót lâu thế**: lỗi này **có từ ngày viết bản đồ server**.
  Công thức kéo-về-xám cũ **làm phẳng sắc màu**, nên một sắc bị hoán trông chẳng khác một sắc đúng là
  bao — cỏ vẫn ra xanh xám, nước ra xanh xám. Chính việc **bỏ lớp khử màu để mặt đất trung thực hơn**
  là thứ phơi nó ra. Một bản sửa thẩm mỹ đã lộ ra một lỗi dữ liệu.
  - Cùng họ với bài học đã ghi trước đó: *"phân biệt thay đổi GÂY RA lỗi với thay đổi LÀM LỘ lỗi"*.
    Đây là ca thứ hai, và lần này thứ bị lộ đã nằm im từ đầu dự án.
- **Trả lời câu hỏi của người dùng ("tham khảo bộ màu chuẩn ở đâu")**: không cần đi mượn của Xaero
  hay JourneyMap. **Bảng màu vanilla chính là bảng chuẩn** và ta đã dùng đúng nó từ đầu — sai nằm ở
  **cách đóng gói byte**, không nằm ở bảng màu.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). **Chưa test tay.**
- **Cần người dùng kiểm**: (1) nước ra **xanh dương**; (2) mái gỗ sồi ra **nâu vàng**; (3) cỏ, cát,
  đá, lá cây đọc đúng như trên item bản đồ vanilla, chỉ tối hơn; (4) ký hiệu vẫn nổi trên nền đó.

### 2026-08-15 — Block bị kéo dãn ngang: một `span` chia cho cả hai trục
- **Người dùng nhận xét block trên bản đồ bị kéo dãn theo chiều ngang.** Đúng, và số học xác nhận
  ngay: `MapPanel` lấy **một** giá trị `span` rồi chia nó cho **cả `width` lẫn `height`**. Vùng bản
  đồ rộng ~563 cao ~295 — tỉ lệ gần **2:1** — nên mỗi block được vẽ **rộng gần gấp đôi chiều cao**.
  Ở 256m: 2,20 điểm ảnh/block theo ngang nhưng chỉ 1,15 theo dọc.
- **Hệ quả không chỉ là thẩm mỹ**: ô lưới không vuông, khoảng cách đọc trên bản đồ **sai theo hướng**,
  và với một thiết bị mà người dùng ước lượng cự ly bằng mắt thì đó là sai số có hại.
- **Khắc phục**: `span` giờ có nghĩa rõ ràng là **số block theo chiều NGANG** — đúng con số mà nhãn
  tỉ lệ đang ghi. Phủ theo chiều dọc **suy ra từ đó** (`span × height / width`), nên **một block là
  một hình vuông** bất kể khung có tỉ lệ nào. Ở 4096m, bản đồ phủ 4096 block ngang và 2146 block dọc,
  cùng một tỉ lệ điểm ảnh.
- **Chạm bảy chỗ**, vì `span` được dùng lại ở mỗi phép quy đổi: lưới, `worldToScreen` (hai bản),
  `screenToWorld`, kéo bản đồ, và `TerrainImage.draw` (giờ nhận **hai** span thay vì một).
  - Đây lại là họ lỗi quen thuộc của dự án: **một đại lượng mang hai nghĩa**. `span` vừa được hiểu là
    "bề ngang" vừa là "bề dọc", và không chỗ nào sai rõ ràng cho tới khi khung thôi vuông.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). **Chưa test tay.**
- **Cần người dùng kiểm**: (1) ô lưới có **vuông** không; (2) công trình vuông trong game (nhà, ruộng)
  có ra vuông trên bản đồ không; (3) nhãn tỉ lệ vẫn đúng theo **chiều ngang**; (4) đặt/xoá mục tiêu
  bằng chuột có rơi đúng chỗ không — `screenToWorld` vừa đổi; (5) kéo bản đồ theo chiều dọc có đi
  đúng quãng không.

### 2026-08-15 — Bỏ làm mới hố đạn, mark lệch nửa block, và mảnh bản đồ thế giới cũ
- **1. Bỏ cơ chế làm mới hố đạn** (người dùng quyết định, lập luận đã ghi ở entry trước: bản đồ chỉ
  huy hoả lực thật **không tự cập nhật thiệt hại** — quan sát viên báo về). Nhưng **không bỏ hết hết
  hạn**, vì nó còn giữ một bản sửa khác: ô trả lời "rỗng" rồi người chơi bay tới đó và chunk **được
  sinh ra** — nếu không hỏi lại thì trống vĩnh viễn.
  - Luật mới theo **loại câu trả lời**: ô **đầy đủ mọi cột** → không hỏi lại nữa; ô **rỗng hoặc thiếu
    cột** → vẫn hết hạn và hỏi lại. `TerrainTile.isComplete()` quyết định.
  - Đây là chỗ toàn bộ lưu lượng làm mới biến mất, mà **không mất bản sửa nào đã có**.
- **2. Mark rơi vào giao điểm bốn block thay vì giữa block** — hai nửa của cùng một lỗi:
  - Khi **vẽ**: `worldToScreen` quy đổi thẳng toạ độ block, mà toạ độ block chỉ ra **góc** của nó.
    Cộng nửa block → mark nằm giữa ô.
  - Khi **bấm**: `screenToWorld` dùng `Math.round`, nên **nửa sau của một block bị làm tròn sang block
    kế tiếp**. Đổi sang `Math.floor` — mọi điểm ảnh trong một block trả về đúng block đó.
  - Hai lỗi ngược chiều nhau nên chúng **che bớt nhau**: đặt mục tiêu rồi nhìn thấy nó ở gần đúng chỗ,
    chỉ lệch nửa ô. Sửa một cái mà quên cái kia thì sai sẽ **rõ hơn** chứ không đỡ hơn.
- **3. Mảnh bản đồ thế giới cũ còn sót khi sang thế giới mới**: `checkWorld()` xoá **dữ liệu** trong
  cache, nhưng **texture đã nằm trên card đồ hoạ không thuộc cache** — chúng ở `TerrainImage` và
  không ai xoá. Thêm bộ đếm `generation()`, `TerrainImage` theo dõi và **vứt toàn bộ sheet** khi nó
  đổi. Cũng xoá nốt `HASHES` và `COMPLETE` vốn bị bỏ quên ở cùng chỗ.
  - Bài học: **dọn dẹp phải đi hết chuỗi sở hữu.** Dữ liệu ở một nơi, ảnh dựng từ nó ở nơi khác, và
    xoá nơi thứ nhất không chạm tới nơi thứ hai.
- **4. CHƯA làm — bản đồ gợn sóng khi kéo chậm.** Chẩn đoán: tâm bản đồ vẫn là **số nguyên block**,
  và mỗi ô lại **làm tròn riêng** hai mép của nó ra điểm ảnh nguyên. Ở 256m một block ≈ 2,2 điểm ảnh,
  nên mỗi bước kéo dịch mọi ô cùng lúc ~2,2px — nhưng **ô này làm tròn lên còn ô kia làm tròn xuống**,
  nên chúng **xê dịch tương đối với nhau 1px**. Đó chính là gợn sóng: ảnh không dịch chuyển như một
  khối.
  - Lời giải đúng: cho tâm bản đồ nhận **số thực**, dựng ô theo gốc **nguyên** như hiện tại, rồi
    **tịnh tiến cả lớp** theo phần lẻ bằng ma trận. Khi đó mọi ô dịch **cùng nhau**, quan hệ giữa
    chúng không bao giờ đổi, và chuyển động mượt dưới mức một điểm ảnh.
  - Tách riêng vì đây là thay đổi cách bản đồ **chuyển động**, và phép thử của nó là cảm giác kéo —
    trộn vào lượt này sẽ không biết cái gì cải thiện cái gì.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). **Chưa test tay.**
- **Cần người dùng kiểm**: (1) hố đạn **không còn hiện** (đúng ý muốn), nhưng bay tới vùng mới đất
  **vẫn hiện**; (2) đặt mục tiêu bằng chuột phải — chữ thập nằm **giữa ô**, và đúng ô đã bấm;
  (3) sang thế giới khác — **không còn mảnh bản đồ cũ**; (4) bản đồ có mượt hơn khi đứng yên không
  (bỏ làm mới đáng lẽ giúp phần này).

### 2026-08-15 — Tâm bản đồ nhận số thực, và lưới bị khoá vào địa hình
- **Người dùng đưa ra nhận xét quan trọng nhất của cả đợt tối ưu**: sau khi sửa gợn sóng thì **mọi
  mức zoom đều mượt hẳn**, kể cả những mức mà ba lượt tối ưu trước vẫn còn giật. Tức **gợn sóng không
  phải một lỗi thẩm mỹ riêng — nó chính là cùng một bệnh** với thứ tôi đã đuổi theo suốt: *có gì đó
  bị tính lại hoặc bị làm tròn riêng ở mỗi khung hình khi kéo.*
  - Ghi lại vì nó **đảo ngược thứ tự chẩn đoán của tôi**. Tôi coi độ mượt là bài toán hiệu năng và
    gợn sóng là chuyện hiển thị, nên đuổi theo chi phí CPU ba lượt liền. Người dùng nhìn ra chúng là
    **một bài toán**, và lời giải là *hình học nhất quán*, không phải *ít việc hơn*.
- **Đã làm — tâm bản đồ nhận số thực.** Trước đây tâm là `BlockPos` nguyên, `panByPixels` dồn phần lẻ
  rồi mới nhích một block. Ở 256m một block ≈ 2,2 điểm ảnh, nên mỗi bước dịch cả ảnh 2,2px **cùng
  lúc** — mà mỗi ô lại **làm tròn riêng** hai mép của nó, nên ô này tròn lên ô kia tròn xuống và
  chúng trượt lên nhau. Giờ tâm giữ phần lẻ, không còn `dragRemainder`, **mọi điểm ảnh chuột đều
  được tính**.
- **Cách dựng, và đây là mấu chốt**: các lớp vẫn dựng từ **gốc nguyên** để mép ô khớp tuyệt đối, còn
  **phần lẻ dịch cả lớp** bằng một phép tịnh tiến ma trận. Ô cứng với nhau, cả tấm trôi mượt.
- **Rồi lỗi đó lặp lại ngay một tầng trên**: địa hình trôi bằng ma trận, nhưng **lưới vẫn tự làm tròn
  ra điểm ảnh nguyên** — nên hai lớp dịch theo hai nhịp và **doãng ra rồi khép lại 1px** khi kéo. Giờ
  **địa hình và lưới nằm chung một phép tịnh tiến duy nhất**, dựng từ chung một gốc nguyên.
  - **Quy tắc rút ra**: mọi thứ chuyển động cùng mặt đất phải **chia chung một gốc và một phép dịch**.
    Sửa riêng từng lớp chỉ đẩy chỗ trượt sang ranh giới kế tiếp.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). **Chưa test tay.**
- **Cần người dùng kiểm**: (1) kéo chậm — **lưới và địa hình có dính chặt vào nhau** không, hay còn
  thấy chúng doãng ra; (2) nhãn toạ độ lưới có bị nhoè khi kéo không (chúng nằm trong phép tịnh tiến,
  nên có thể rơi vào vị trí không nguyên — nếu khó chịu thì tách chúng ra ngoài được); (3) mark mục
  tiêu và pháo có lệch so với địa hình không (chúng **chưa** nằm trong phép tịnh tiến); (4) mọi mức
  zoom còn mượt như bạn vừa thấy chứ.

### 2026-08-15 — Kết phiên: tóm tắt bàn giao
- **Phiên này sắp hết ngân sách ngữ cảnh.** Đã viết lại toàn bộ mục **"▶ BẮT ĐẦU TỪ ĐÂY"**, bảng
  **kiến trúc**, **bản đồ file**, danh sách **còn tồn đọng** và mục **bài học** — phiên mới đọc mục đó
  trước, mục 5 chỉ để tra lý do.
- **Đã thêm bốn dòng vào bảng kiến trúc** vì chúng là quyết định nền tảng mới của phiên này và không
  nên bị bàn lại: điểm ảnh logic + font Minecraft, ký hiệu bản đồ vẽ ở điểm ảnh vật lý, chuyển động
  bản đồ dùng gốc nguyên + một phép dịch chung, và việc **không hiện hố đạn**.
- **Mục bài học viết lại thành bốn nhóm** (tích hợp mod ngoài / hình học giao diện / hiệu năng / cách
  làm việc) thay vì ba mục rời. Nhóm **hình học giao diện** là nhóm gây lỗi nhiều nhất của cả dự án —
  riêng luật "một vùng hình học chỉ định nghĩa ở đúng một nơi" đã bị vi phạm khoảng sáu lần.
- **Phân tích video**: đã trích khung hình bằng `ffmpeg` (36 khung ở 2fps từ clip 17,7 giây). Khung
  hình **xác nhận** màu đã đúng (nước xanh dương, cát nâu, cỏ xanh, phân biệt rõ), ô lưới vuông, vùng
  chưa khảo sát trong suốt. **Không kết luận được về độ mượt** — đó là hiện tượng giữa các khung, cần
  so hai khung liên tiếp trong lúc kéo, và tôi chưa biết đoạn nào trong video là lúc kéo chậm.
  - **Nghi vấn duy nhất thấy được từ ảnh**: hai đường ngang sáng bất thường ở sát mép trên và mép dưới
    vùng bản đồ. Đã ghi vào "Việc tiếp theo" — chưa xác minh, đừng coi là đã biết nguyên nhân.
- **Việc đầu tiên của phiên sau**: chờ kết quả test của người dùng cho bốn commit cuối, rồi xử lý ba
  nghi vấn hiển thị, rồi mới sang đợt 4.

### 2026-08-15 — Mỗi hình chữ nhật là một lệnh vẽ: bão draw call của lớp ký hiệu
- **Làm đúng theo bài học số 9 thay vì đoán tiếp**: đi liệt kê lại *mọi thứ còn chạy mỗi khung hình*
  sau bốn lượt tối ưu trước. Lần này thứ lớn nhất **không nằm ở bản đồ** mà ở lớp ký hiệu vẽ đè lên nó.
- **Xác minh bằng source giải mã của chính Minecraft 1.20.1** (`GuiGraphics`, không đoán):
  `fill` đẩy bốn đỉnh vào buffer chung rồi gọi `flushIfUnmanaged` — mà hàm đó **xả buffer xuống card
  ngay** trừ khi đang mở một khối `drawManaged`.
  ```java
  private void m_286081_() { if (!this.f_285610_) { this.m_280262_(); } }   // flushIfUnmanaged → flush
  ```
  → **Một `g.fill` = một lần upload + một lệnh vẽ.** Tra bảng ánh xạ xác nhận `m_286007_` chính là
  `drawManaged`, `m_280262_` là `flush`.
- **Đếm thử mỗi khung hình**:
  | Thứ vẽ | Số `fill` |
  |---|---|
  | `ring()` — 2 vòng × tối đa 720 điểm, mỗi khẩu | ~960 × số khẩu (≈3 800 với 4 khẩu) |
  | `drawLine()` — **một fill mỗi điểm ảnh**, mỗi cặp khẩu × mục tiêu | ~640 × số cặp (≈20 000 với 4 khẩu × 8 mục tiêu) |
  | Chevron qua `Ui.triangle` | ~50 (ở điểm ảnh vật lý nên gấp ba số hàng) |
  | Lưới + vỏ máy | ~300 |
  | `blit` địa hình (đường riêng, **không gộp được**) | ~324 |
  Tức **hàng chục nghìn lệnh vẽ mỗi khung hình** — lớn hơn tất cả những gì bốn lượt tối ưu trước
  đụng vào **cộng lại**. Và nó chỉ hiện ra khi đã **có kế hoạch hoả lực trên bản đồ**, nên mọi phép
  thử "kéo bản đồ trống" trước đây đều không chạm tới nó.
- **Đã làm — `Ui.batched(g, ...)`** bọc `drawManaged`: fill dồn vào một buffer, xả một lần. Áp ở ba
  chỗ nóng, **không đổi một dòng nào của phần vẽ**: cụm đường bắn + ký hiệu khẩu pháo, `renderRangeRings`,
  và chevron.
  - **Giới hạn đã ghi vào javadoc, và nó là thật**: chỉ dùng cho **hình, và chỉ khi không lẫn chữ**.
    Chữ được gộp theo render type riêng và **luôn ra sau** khối hình dù nộp vào lúc nào, nên một
    `fill` nộp sau một nhãn ở trong khối sẽ đè lên nhãn đó thay vì nằm dưới. Ngoài khối thì hai thứ
    đan xen đúng thứ tự nộp — đó là điều phần còn lại của tablet đang dựa vào. Vì thế **cụm mục tiêu
    (có số thứ tự) và lưới (có nhãn toạ độ) cố ý để nguyên**, và chúng cũng chỉ vài chục fill.
  - Cố ý **không** đổi `drawLine` sang một quad xoay (1 lệnh vẽ thay vì 640). Nó rẻ hơn nữa nhưng là
    **thay đổi diện mạo** mà tôi không tự kiểm được — theo bài học 13. Gộp batch cho gần hết phần
    lợi mà **không đổi một điểm ảnh nào**.
- **Kèm một chỗ nhỏ**: `TerrainImage` gọi `System.currentTimeMillis()` **một lần mỗi ô** trong vòng
  lặp vẽ; giờ đọc một lần mỗi khung hình rồi truyền vào.
- **Còn chạy mỗi khung hình sau lượt này** (viết ra đây vì bài học 9 nói phải liệt kê lại, không
  được mặc định là đã xong):
  1. **~324 lệnh `blit` cho các ô địa hình.** Không gộp được — mỗi ô một texture. Muốn giảm thì phải
     đi đường Xaero thật sự: **một texture lớn cho cả vùng** (vd. 256×256 phủ 4×4 ô) và **upload
     từng phần** khi một ô về. Đây là mục lớn kế tiếp.
  2. **Fill của vỏ máy** (phím, thẻ, bảng) — mỗi cái vẫn một lệnh vẽ, nhưng có chữ đan xen nên phải
     tách hình/chữ trước khi gộp được.
  3. **Đóng hộp `Long`** ở `Map<Long, Sheet>` và `TILES` — ~1000 cấp phát nhỏ mỗi khung hình.
     `Long2ObjectOpenHashMap` (fastutil có sẵn trong Minecraft) bỏ sạch.
  4. `getBoundArtillery(liveStack())` **đọc NBT hai lần mỗi khung hình** trong đường vẽ ký hiệu.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, `ERROR=0`). **Chưa test tay.**
  **Chưa commit** — chờ người dùng.
- **Cần người dùng kiểm**: (1) **bind vài khẩu + đặt vài mục tiêu rồi kéo bản đồ** — đây mới là phép
  thử của lượt này, kéo bản đồ trống sẽ không thấy khác biệt gì; (2) **vòng tầm bắn, đường từ pháo tới
  mục tiêu, ký hiệu khẩu pháo, mũi tên người chơi phải trông y hệt trước** — nếu có thứ gì đổi thứ tự
  đè lên nhau thì đó chính là giới hạn "chữ ra sau hình" nói ở trên; (3) số thứ tự mục tiêu vẫn nằm
  trên chữ thập; (4) mở/đóng các bảng, menu chuột phải vẫn bình thường.

### 2026-08-15 — Cài Xaero để đối chiếu, và đổ bóng chuyển từ ba bậc sang liên tục
- **Xaero's World Map cài lại, nhưng KHÔNG phải tích hợp.** Người dùng muốn một thước đo đặt cạnh
  bản đồ của ta. Pin qua CurseMaven như SBW (project 317780, file 8449908 = 1.44.2 Forge 1.20.1), khai
  báo **`runtimeOnly`** — nó nạp trong dev client nhưng **trình biên dịch sẽ từ chối mọi code chạm vào
  nó**. Hàng rào đó quan trọng hơn cái pin: hai lần trước map mod vào dự án dưới danh nghĩa "chỉ tham
  khảo" rồi thành phụ thuộc. Xoá đúng một dòng là addon trở lại y như cũ.
  - Xaero 1.44.2 **tự đóng gói `xaerolib` 1.7.1** bên trong nên không cần pin thêm.
  - Boot: `FATAL=0`. Ba dòng `ERROR` đều vô hại — hai cái là log4j không xoay được `latest.log` vì
    tiến trình boot trước còn giữ file, một cái là Xaero tự kiểm tra phiên bản bị timeout mạng.
- **Đổ bóng: bỏ ba bậc của bảng màu vanilla, chuyển sang liên tục.** Vấn đề thật không phải "ít bậc"
  mà là **câu hỏi sai**: `MapColor.Brightness` chỉ có LOW/NORMAL/HIGH và code so **đúng một block về
  phía bắc**. Mà mặt đất Minecraft **lượng tử hoá theo block**, nên so qua một block chỉ trả lời được
  *cao hơn / bằng / thấp hơn* — và trên địa hình bình thường gần như cột nào cũng lệch hàng xóm đúng
  1. Kết quả là **nhiễu ở cỡ một block đắp lên tấm bản đồ đọc ở cỡ một cây số**.
- **Ba thay đổi, mỗi cái sửa một nửa của bài toán**:
  1. **So qua `RELIEF_RUN = 3` mẫu thay vì 1.** Đổi câu hỏi từ *"block phía bắc có thấp hơn không"*
     thành *"mặt đất này đổ về hướng nào, dốc bao nhiêu"* — câu sau có vô số đáp án và đúng là câu mà
     người chọn trận địa hay đọc vùng khuất đang hỏi. Khoảng so **nhân theo `stride`** nên một sườn
     dốc đọc như nhau ở mọi mức zoom.
  2. **Ánh sáng từ TÂY BẮC, không phải từ chính bắc.** Một trục đơn **không thể** hiện một sống núi
     chạy dọc theo chính trục đó — một mỏm bắc–nam chiếu từ đúng hướng bắc thì phẳng lì, mà đó lại
     chính là loại địa hình người ta giấu pháo sau lưng.
  3. **`tanh` thay vì tuyến tính.** Địa hình có vài bậc khổng lồ và rất nhiều bậc nhỏ; mọi công thức
     tuyến tính sẽ tiêu hết dải sáng vào mấy vách đá rồi vẽ phần đất thoải — chỗ pháo thật sự đứng —
     thành một tông phẳng. Dốc hơn thì bị nén chứ không bị cắt, nên vách đá và sườn núi vẫn phân biệt được.
- **Cái bẫy đã bịt trước, không phải sau**: ánh sáng giờ với sang **cả ô phía tây**, nên `northKnown`
  đổi thành `neighboursKnown` và ô được dựng lại khi **một trong hai** hàng xóm về. Bỏ sót là mép
  trái mọi ô phẳng lì vĩnh viễn — đúng cái bẫy đã ghi ở entry "một texture mỗi ô" (mục 1).
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`). **Chưa test tay. Chưa commit.**
- **Cần người dùng kiểm**: (1) **mở Xaero (phím M) rồi mở tablet, so hai bên**; (2) sườn dốc có ra
  **chuyển sắc mượt** thay vì lốm đốm không; (3) sống núi / thung lũng / bờ sông có **đọc ra hình khối**
  không; (4) chỗ nối giữa các ô có liền không — **nhất là mép TRÁI**, vì đó là trục vừa thêm; (5) mức
  zoom xa đổ bóng có còn thấy được không; (6) nếu quá đậm/quá nhạt thì `RELIEF_STRENGTH` là con số duy
  nhất cần chỉnh, `RELIEF_RUN` chỉnh độ "mịn" của địa hình.

### 2026-08-15 — Khảo sát: bộ lọc bản đồ trên màn hình chỉ huy hoả lực thật, và đề xuất cho ta
*(Trả lời câu hỏi của người dùng. Chưa code gì. Nguồn: AFATDS/MCWP 3-16, FM 3-06.11, FM 3-21.9, tài
liệu ATAK 5.3 / CivTAK.)*

- **Đính chính một tiền đề**: **thermal KHÔNG phải một lớp bản đồ của FDC.** Nó là chế độ của **khí tài
  quan sát** (kính ngắm trinh sát viên, feed UAV). Trên màn hình chỉ huy hoả lực nó xuất hiện dưới dạng
  **ảnh/feed cảm biến chồng lên**, không phải bộ lọc của bản đồ. Ghi lại để khỏi đuổi nhầm hướng.
- **Thứ thật sự có trên màn hình FDC/ATAK**, và ta có gì để làm:
  | Lớp thật | Có ở đâu | Ta có dữ liệu chưa |
  |---|---|---|
  | **Đường bình độ** (contour) | ATAK "computed contour maps" | ✅ `TerrainTile.height` từng cột |
  | **Lớp tô theo độ cao** (hypsometric/heatmap, thấp xanh → cao đỏ) | ATAK Heatmap trên nền DTED | ✅ cùng dữ liệu |
  | **Độ dốc** (slope) | phân tích địa hình | ✅ đã tính sẵn trong `relief()` |
  | **Viewshed / vùng khuất** (dead space) | ATAK Viewshed; FM 3-21.9 | ✅ đủ dữ liệu, cần thuật toán |
  | **Mặt cắt đường bay** (elevation profile) | ATAK "dynamic profiling" | ✅ **`FlightProfile` đã tính rồi, chỉ chưa vẽ** |
  | **FSCM**: NFA / RFA / FFA / CFL / FSCL | AFATDS vẽ lên bản đồ + cảnh báo vi phạm | ➖ dữ liệu của riêng ta, chưa có |
  | **Danger close** | doctrine | ✅ có vị trí thực thể |
- **Điểm mấu chốt về kiến trúc**: **ta đã có sẵn một mô hình độ cao đầy đủ** (tương đương DTED, một mẫu
  mỗi block, server khảo sát) và **đang chỉ dùng nó để đổ bóng**. Gần hết danh sách trên là *hiển thị
  thứ đã có*, không phải *thu thập thứ mới*. Đó là lý do nhóm 1 rẻ đến vậy.
- **Đề xuất theo thứ tự giá trị/chi phí** (chi tiết đã trao đổi với người dùng):
  1. **Đường bình độ + lớp độ cao + lớp độ dốc** — đều nướng vào chính texture ô, **chi phí mỗi khung
     hình bằng 0**, đổi một hàm màu. Rẻ nhất, và bình độ là thứ "bản đồ pháo binh" nhất có thể làm.
  2. **Mặt cắt đường bay** — `FlightProfile` đã tính; đây thuần tuý là vẽ ra thứ ta đã biết. Tỉ lệ
     giá trị/công sức tốt nhất trong nhóm khó.
  3. **Vùng khuất / mask** cho từng khẩu — giá trị chiến thuật cao nhất, nhưng là viewshed thật, phải
     tính nền và giới hạn bán kính.
  4. **FSCM (NFA/RFA/FFA)** — không cần toán địa hình nào, thuần bookkeeping, và có nghĩa thật trong
     multiplayer: `FFE` từ chối bắn vào vùng cấm.
- **Cảnh báo cái bẫy khi làm nhóm 1**: đổi chế độ màu **phải vứt toàn bộ sheet đã dựng**, đúng như
  `generation()` làm khi đổi thế giới. Texture nằm ở `TerrainImage`, chế độ nằm ở chỗ khác — lại đúng
  bài học **"dọn dẹp phải đi hết chuỗi sở hữu"**.
- **Ghi rõ để không lẫn lộn ưu tiên**: đây là **đề xuất**, không phải quyết định. Đợt 4 (kế hoạch hoả
  lực) vẫn là mục đã chốt trước; người dùng chọn thứ tự.

### 2026-08-15 — Vì sao bản đồ ta thua Xaero về chi tiết, và ba nguyên nhân đã sửa
- **Người dùng đặt hai ảnh cạnh nhau** (Xaero 3,6x vs. ta 512m) và nhận xét: cách biệt lớn về chi
  tiết lẫn màu, "cảm giác Xaero là một ảnh PNG nhất quán chứ không phải hiện thẳng pixel".
- **Trực giác đó đúng về Xaero nhưng KHÔNG phải nguyên nhân.** Họ có cache ảnh vùng ra đĩa + texture
  GPU — nhưng **ta cũng làm y hệt** từ lượt "một texture mỗi ô". Khác biệt nằm ở **cái gì đi vào từng
  pixel**, không nằm ở đường vẽ. Ghi lại vì đuổi nhầm nguyên nhân là kiểu thất bại kinh niên của dự án.
- **Nguyên nhân 1 — nguồn màu.** Kiểm trong jar Xaero (`xaero/map/region/MapPixel`): nó tham chiếu
  `BlockTextureColorUtils` (màu trung bình của **texture thật**), `blockColors`, `getBiomeColor`, và
  `getMapColor` **chỉ là dự phòng**. `BlockTintProvider implements BlockAndTintGetter` — tức đi qua
  đúng hệ tô màu theo quần xã của vanilla. Ta thì dùng `getMapColor` — **62 màu phẳng, không tint**.
  Mọi block cỏ ở mọi biome đều đúng một giá trị.
- **Nguyên nhân 2 — nước.** Kiểm trong `MapItem` giải mã của vanilla: khi block bề mặt có chất lỏng
  nó **đi xuống đếm độ sâu**, rồi `if (mapcolor == WATER) { d2 = depth*0.1 + ...; }` — **nước đổ bóng
  theo ĐỘ SÂU**. Ta lấy `WORLD_SURFACE` → màu = WATER, height = **mực nước biển**, mà mực nước biển
  là hằng số → `relief()` ra 0 khắp nơi → **tấm xanh phẳng lì**, và đó là nửa màn hình.
- **Nguyên nhân 3 — chính `RELIEF_RUN = 3` của tôi.** Tôi cố ý so qua 3 block để lấy hình khối địa
  mạo, và cái giá đúng bằng vân bề mặt cỡ 1 block — thứ mà bãi cát của Xaero hiện thành từng đường
  thềm mảnh. "Đổ bóng đẹp" và "thiếu chi tiết" là **cùng một thay đổi nhìn từ hai phía**.
- **Đã sửa cả ba (Đợt A):**
  1. **`TerrainTile` mang thêm `depth` (byte) và `biome` (short)** — 6 byte/cột thay vì 3. Sampler đi
     xuống qua nước tới đáy, giữ **màu của đáy**, đếm độ sâu riêng. Client tô đáy rồi **phủ nước theo
     độ sâu** + làm tối dần, và **relief lấy theo đáy** nên đáy biển có hình khối.
     - **`height` cố ý KHÔNG đổi nghĩa** — vẫn là **mặt** nước. Đạn đạo đọc nó: một quả đạn bay qua
       biển bị mặt nước chặn, còn một `height` trỏ xuống đáy sẽ **âm thầm báo thừa khoảng trống**.
       Đáy suy ra bằng `height - depth` ở chỗ cần vẽ. Đây là chỗ dễ sai nhất của cả đợt.
  2. **Tint quần xã**: gửi biome id (an toàn vì registry động được server đồng bộ cho client, đúng
     cách gói chunk của vanilla làm), client tô cỏ/lá/nước theo biome. Tint bằng **tỉ lệ so với màu
     tham chiếu của plains** chứ không thay hẳn — giữ nguyên sáng/tối của bảng màu, chỉ dịch sắc.
  3. **Đổ bóng hai thang**: `RELIEF_MACRO` (3 mẫu, hình khối) **+** `RELIEF_MICRO` (1 mẫu, vân bề
     mặt). Thang rộng mang hình, thang hẹp trả lại grain.
- **Tái cấu trúc `build()` — gom trước, tô sau.** Relief giờ so với **bốn** cột, làm qua handle từng
  pixel là 4 lần kiểm biên + 4 lần tra cache mỗi pixel. Giờ đọc **một vùng có lề** (`MARGIN = RELIEF_RUN`)
  vào mảng phẳng, rồi tô là thuần số học trên bộ nhớ liền. Lề khiến chuyện "với sang ô bên cạnh" **rơi
  ra từ hình học** thay vì phải có ngoại lệ ở bốn cạnh.
- **Đã bịt**: `neighboursKnown` giờ đòi **cả** ô bắc lẫn ô tây; memo biome bị xoá trong `close()` vì
  biome id mang nghĩa khác ở thế giới khác.
- **Đã CHẠY THẬT phép mã hoá/giải mã, không chỉ compile.** Boot sạch chỉ chứng minh client lên tới
  màn hình chính — nó **không hề chạm** vào đường tile. Mà `TerrainTile` mã hoá/giải mã đúng là hàm
  mà nhật ký đã ghi là từng giấu một con bọ `Deflater` nhiều ngày, và tôi vừa đổi định dạng của nó.
  Nên đã viết một harness tạm chạy ngoài Minecraft, đối chiếu cả 4096 cột × 4 trường + `contentHash`
  + buffer phải rỗng sau khi đọc:
  | Trường hợp | Trên dây |
  |---|---|
  | raw (chưa nén) | 24 576 B (trước là 12 288) |
  | dữ liệu ngẫu nhiên (xấu nhất) | 20 570 B |
  | địa hình thực tế (dốc mượt, vài vật liệu, hai biome) | **2 173 B** |
  | đồng nhất hoàn toàn | 143 B |
  → **Gấp đôi số byte thô gần như không tốn gì trên dây**, vì `depth` và `biome` lặp rất mạnh và
  deflate ăn hết. Đây là con số cần biết trước khi lo về băng thông.
  - **Harness là đồ tạm trong scratchpad, KHÔNG nằm trong repo.** Mục tồn đọng số 5 (không có test tự
    động) **vẫn còn nguyên** — đừng đọc entry này thành "đã có test".
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch, mã hoá/giải mã chạy qua. **Chưa test
  tay. Chưa commit.**

### 2026-08-15 — Đính chính: vẽ ở điểm ảnh vật lý giúp ít hơn tôi nói
- Tôi đã nói vùng bản đồ chỉ có ~563 điểm ảnh GUI (scale 3) trong khi Xaero có ~1690, và ngụ ý đó là
  một cách biệt lớn. **Làm phép tính theo từng mức zoom thì hẹp hơn nhiều:**
  | Span | Mức | Texel có | Điểm ảnh GUI | Kết luận |
  |---|---|---|---|---|
  | 512m | 0 | 512 | 563 | **đã ngang** — không mất gì |
  | 1024m | 0 | 1024 | 563 | **mất một nửa cột** ← chỗ duy nhất bị đau |
  | 2048m | 1 | 256 | 563 | **thừa điểm ảnh**, ràng buộc là kim tự tháp mức chứ không phải lưới GUI |
- **Nên**: ở 2048m trở lên, vẽ ở điểm ảnh vật lý **không giúp gì cả** — thứ giúp là cho `levelFor`
  cân nhắc mật độ điểm ảnh (cho phép nhiều quad hơn), hoặc sheet lớn hơn.
- **Và giá của nó cao**: địa hình, lưới và ký hiệu **phải cùng chuyển sang một lúc**, nếu không hai
  lớp làm tròn theo hai nhịp — **đúng con bọ đã sửa hôm nay bằng "một gốc, một phép dịch"**.
- **Đề xuất**: hoãn mục này, làm "levelFor theo mật độ điểm ảnh" trước vì rẻ hơn và đúng chỗ đau hơn.
  Người dùng quyết.

### 2026-08-15 — Biển bớt lộ đáy, mức thô thôi lấy mẫu điểm, và trần dựng lại mỗi khung hình
- **Người dùng test Đợt A**: màu và tint đã tốt, nhưng **đáy biển nổi quá** — bản đồ ra như một tấm
  bản đồ địa hình của đáy biển. Yêu cầu: *"chỉ cần nhìn được là được, thêm một lớp mặt biển mờ mờ"*.
- **Chẩn đoán — tôi sửa đúng một nửa.** Việc pha màu về phía nước che được **màu** của đáy nhưng để
  nguyên **đổ bóng** của nó ở đầy đủ tương phản. Mà đổ bóng mới là thứ đang gào lên: một bãi cạn đọc
  to hơn cả một sườn đồi trên cạn. **Màu và bóng là hai kênh riêng, và tôi chỉ hạ một.**
  - Thêm `WATER_RELIEF_SHALLOW/DEEP` — dưới nước relief chỉ còn 45% ở mép và 12% ở chỗ sâu. Cố ý
    **không về 0**: mặt biển phẳng tuyệt đối thì thôi đọc ra là một mặt nước, và mất luôn bãi cạn với
    lạch sâu — hai thứ đáng biết.
  - `WATER_MIN_MIX` 0,35 → **0,6** và `WATER_OPAQUE_AT` 14 → **8**, nên ngay chỗ nông cũng đã là nước
    có gì đó bên dưới, chứ không phải đất nhìn qua kính lọc xanh.
- **Mức thô đang LẤY MẪU ĐIỂM — đây là nguyên nhân thật của "zoom xa mất hình dáng đảo".** Mỗi pixel
  thô lấy đúng cột nằm ở góc ô của nó và **vứt toàn bộ phần còn lại**: ở mức 2 là vứt 4095 trên 4096
  cột. Bờ biển vì thế vỡ thành bậc thang và đảo nhỏ **chớp tắt theo lúc kéo**.
  - **Trên một thiết bị dùng để lấy phần tử bắn, đó không phải ảnh xấu — đó là ảnh SAI.** Người dùng
    nói đúng khi gọi nó là vấn đề độ chính xác chứ không phải thẩm mỹ.
  - **Đã sửa**: mỗi pixel thô giờ **trung bình 4×4 mẫu** (`COARSE_SAMPLES`), cố định chứ không tăng
    theo mức — mức 2 phủ 4096 block/pixel, lấy hết là hàng triệu lần đọc mỗi sheet.
  - **Chiều cao trung bình được; vật liệu thì không.** Nửa cát nửa nước không phải một vật liệu ở
    giữa, nên **màu phổ biến nhất thắng pixel** — đúng cách map item của vanilla thu nhỏ
    (`Multisets.copyHighestCountFirst`), vì cùng một lý do.
- **Hai bản tối ưu đi kèm, bắt buộc chứ không phải tuỳ chọn** — vì việc trung bình hoá **nhân 16 lần**
  chi phí dựng một sheet thô:
  1. **Trần dựng lại mỗi khung hình** (`MAX_REBUILDS_PER_FRAME = 2`). Việc lệch hạn dùng chỉ **rải**
     tải chứ **không chặn trần**, và trần mới là thứ giữ cho một khung hình xui không bị khựng.
     **Chỉ áp cho dựng LẠI**: sheet chưa từng dựng là đất người chơi không nhìn thấy gì cả, không
     ngân sách nào đáng đổi lấy một lỗ trống.
  2. **`Long2ObjectOpenHashMap` cho `TILES` và `sheets`.** Trước đây đóng hộp `Long` là chuyện nhỏ;
     giờ đường vẽ tra `TILES` **~78 nghìn lần mỗi sheet thô** nên nó thành cấp phát thật.
- **Mã hoá/giải mã chạy lại**: vẫn qua, địa hình thực tế **2 173 B** trên dây.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch. **Chưa test tay. Chưa commit.**
- **Cần người dùng kiểm**: (1) **biển giờ chỉ còn gợi ý đáy** chứ không phải bản đồ đáy biển;
  (2) chỗ nông gần bờ vẫn phân biệt được với chỗ sâu; (3) **zoom ra 2048m/4096m — bờ biển và đảo nhỏ
  có giữ được hình dáng** không, có còn chớp tắt khi kéo không; (4) zoom xa có khựng không (trần mới);
  (5) mức 512m trở xuống **không được đổi gì cả** — ở đó `sub = 1`, đường code y hệt trước.

### 2026-08-15 — Hệ tỉ lệ bản đồ: pháo binh thật dùng gì
*(Trả lời câu hỏi của người dùng. Chưa code gì.)*
- **Pháo binh thật dùng phân số biểu thị (representative fraction), không phải bội số phóng đại.**
  `FM 6-40` (Firing Charts): lưới của target grid **khớp với firing chart 1:25 000**, chia ô lưới
  1 000 m thành các ô 100 m. Bản đồ chiến thuật nói chung là **1:25 000 – 1:100 000**.
- **Ba hệ, và ta đang ở hệ nào**:
  | Hệ | Ví dụ | Đo được cự ly? |
  |---|---|---|
  | Bội số phóng đại (Xaero) | `2.083x` | **Không** — không nói gì về mặt đất |
  | Bề ngang mặt đất (ta) | `1024m` | **Có** |
  | Phân số biểu thị (thật) | `1:25 000` | Có, nhưng **cần biết kích thước vật lý của tờ giấy** |
- **Kết luận**: `1:25 000` **vô nghĩa trên màn hình** vì nó phụ thuộc DPI. Nên **hệ của ta đúng họ
  hơn Xaero** — nó đo mặt đất. Nhưng ta còn thiếu hai thứ thật:
  1. **Thước tỉ lệ (graphic scale bar)** — thứ người ta *thực sự đo bằng* trên bản đồ giấy, và thứ
     mọi bản đồ chiến thuật số (ATAK) đều có. Đây là đề xuất đáng làm nhất.
  2. **Nấc zoom theo số tròn quân sự.** Nấc hiện tại là luỹ thừa 2 (128/256/512/1024…) và bước lưới
     là `span/8`, nên ở 1024m bước lưới ra **128m** — không phải số nào trong nghề dùng. Đổi sang
     nấc 250/500/1000/2000/4000 với lưới **100m/1000m** sẽ khiến bản đồ đọc như một firing chart
     thật, và toạ độ lưới thành chuẩn.
- **Chưa làm, cố ý**: cả hai đều là thay đổi bố cục/hành vi → theo luật dự án phải **phác thảo trước**.

### 2026-08-15 — Lọc texture: nguyên nhân thật của bờ biển góc cạnh và pixel hoá khi zoom xa
- **Người dùng báo ba thứ**: bờ biển thành cụm pixel lớn góc cạnh (Xaero cong và mượt hơn hẳn); vẫn
  **gợn sóng / uốn éo khi kéo chậm, rõ nhất ở zoom xa**; và zoom xa vẫn pixel hoá nặng.
- **Xác minh trong bytecode Xaero (`xaero.map.region.texture.RegionTexture`)** — không đoán:
  ```
  glTexParameteri(GL_TEXTURE_2D, 10240 /*MAG*/, 9728 /*NEAREST*/)
  glTexParameteri(GL_TEXTURE_2D, 10241 /*MIN*/, 9729 /*LINEAR*/)
  ```
  **Phóng to thì NEAREST, thu nhỏ thì LINEAR.** Ta dùng NEAREST cho cả hai.
- **Vì sao cặp đó đúng, và đây là điểm đáng ghi**: hai chiều là **hai bài toán khác nhau**.
  - *Phóng to*: một texel **là** một block. Làm mượt ở đây là **bịa ra mặt đất chưa từng khảo sát** —
    một bản đồ chỉ huy hoả lực không được phép làm thế.
  - *Thu nhỏ*: nhiều texel rơi vào **một** điểm ảnh màn hình. Chọn một cái rồi vứt phần còn lại
    **không phải trung thực, mà là răng cưa** — bờ biển vỡ thành bậc thang, vật thể nhỏ chớp tắt.
    Trung bình chúng mới là câu trả lời đúng ở chiều này.
  - Phải **đặt lại sau MỖI lần upload**: `NativeImage.upload` tự đặt cả hai bộ lọc theo cờ blur của
    nó, nên thứ đặt một lần lúc tạo texture bị xoá ngay lần dựng lại đầu tiên.
- **Viền một texel quanh mỗi sheet** (`TEXTURE_PIXELS = SHEET_PIXELS + 2`). Lọc tuyến tính đọc cả
  texel bên kia mép, mà bên kia mép của một texture là **texel mép lặp lại** — nên nếu không có viền
  thì mọi ranh giới sheet sẽ nhoè ra thành đường nối. Lề đã gom sẵn rồi nên viền gần như miễn phí.
- **`LEVEL_FACTOR` 8 → 4.** Bước nhảy tám lần khiến mức được chọn thường chỉ có **một phần tư** độ chi
  tiết mà panel đủ chỗ hiện. Bảng sau mỗi nấc zoom mới:
  | Span | Mức | Texel ngang | Panel (~563 px) |
  |---|---|---|---|
  | 500m | 0 | 500 | vừa khít |
  | 1000m | 0 | 1000 | thu nhỏ 1,8× → LINEAR làm mượt |
  | 2000m | 1 | 500 | vừa khít |
  | 4000m | 1 | 1000 | thu nhỏ |
  | 16000m | 2 | **1000** | trước đây chỉ 250 → **gấp 4 lần chi tiết** |
  Số quad vẫn ≤ 16 ngang như cũ, nên **không tốn thêm một lệnh vẽ nào**.
- **Gợn sóng — tìm ra chỗ cuối cùng còn làm tròn riêng.** Việc cho tâm bản đồ nhận số thực đã sửa
  **cả lớp**, nhưng `place()` vẫn **làm tròn hai mép của TỪNG sheet** ra điểm ảnh nguyên. Mọi sheet
  dịch cùng một lượng thật, nhưng `round()` của các giá trị khác nhau **vượt ngưỡng ở những khoảnh
  khắc khác nhau** → cứ mỗi lần tâm vượt qua ranh giới một block là các sheet lệch nhau 1px.
  - **Ở zoom xa một block chỉ là một phần nhỏ của điểm ảnh, nên việc vượt ranh giới xảy ra liên tục**
    — đúng như người dùng mô tả "rõ nhất khi kéo ở zoom xa".
  - **Đã sửa**: bỏ `place()`, vẽ mọi sheet **trong một phép biến đổi chung** từ block sang màn hình
    (`translate` + `scale`), toạ độ truyền vào là **số block nguyên**. Không còn chỗ nào để làm tròn
    riêng nữa.
  - **Và lỗi đó lại lặp lại một tầng trên, đúng như luật đã ghi**: lưới vẫn tự làm tròn từng đường.
    Địa hình trôi mượt mà lưới snap thì lưới sẽ **bơi trên** địa hình. Giờ mỗi đường lưới đặt theo
    phần lẻ (`drawFine`), cùng một khung với mặt đất.
- **Nấc zoom theo số tròn quân sự** (người dùng chấp thuận): `250/500/1000/2000/4000/8000/16000/32000`
  thay cho luỹ thừa 2. Bước lưới snap về thang **1-2-5** (100/200/500/1000…) thay vì `span/8` — trước
  đây ở 1024m bước lưới ra **128m**, đọc được nhưng vô dụng: lưới tồn tại để **đếm cự ly bằng mắt**,
  mà không ai đếm theo 128.
- **Con bọ lava (người dùng phát hiện): hồ dung nham ra màu xanh nước.** Tôi viết vòng lặp đi xuống là
  `!getFluidState().isEmpty()` — **mọi chất lỏng**, nên nó lội qua cả dung nham, trả về đá bên dưới
  kèm một `depth`, và phần vẽ — vốn chỉ có **một** khái niệm về `depth` — tô nó thành vũng nước.
  Sửa thành `is(FluidTags.WATER)`. **Không nhìn xuyên được dung nham**: nó là một bề mặt, và màu của
  chính nó mới đúng. Sửa ở **cả hai** đường lấy mẫu (chunk lưu trên đĩa và chunk đang nạp).
- **JourneyMap cài thêm** (người dùng yêu cầu), cùng cơ chế `runtimeOnly` như Xaero — project 32274,
  file 8620163 (6.0.1+forge). Hai thước đo tốt hơn một: chỗ **cả hai mod đồng ý** với nhau là tín
  hiệu mạnh về cái gì là quy ước, mạnh hơn nhiều so với chỗ một trong hai khác ta.
- **Trạng thái**: `gradlew build` sạch. **Chưa test tay. Chưa commit.**
- **Cần người dùng kiểm**: (1) **kéo chậm ở zoom xa — còn uốn éo/gợn sóng không** (phép thử chính);
  (2) bờ biển ở zoom xa có **mượt và cong** hơn không; (3) 16000m có giữ được hình đảo không;
  (4) **zoom gần vẫn phải NÉT**, không được nhoè — nếu nhoè là bộ lọc MAG bị sai; (5) có đường nối
  nào giữa các sheet không (phép thử của viền); (6) **hồ dung nham ra ĐỎ**; (7) nhãn lưới giờ là số
  tròn (100/200/500/1000…); (8) mở JourneyMap (phím J) so ba bên.

### 2026-08-15 — Cài mod tối ưu vào dev client, và vì sao ImmediatelyFast không nằm trong đó
- **Đã cài, `runtimeOnly` như hai mod bản đồ** (không phải phụ thuộc, không compile vào):
  Embeddium 0.3.31 (dựng chunk), Canary 0.3.3 (logic game), ModernFix 5.27.72, FerriteCore 6.0.1.
- **Vì sao chúng liên quan tới addon này, chỗ không hiển nhiên**: **một `Screen` KHÔNG dừng việc dựng
  thế giới phía sau.** Toàn bộ cảnh 3D vẫn được vẽ mỗi khung hình khi tablet đang mở, nên chi phí
  dựng thế giới **là một phần** thời gian khung hình của tablet. Suốt mấy lượt tôi đuổi theo lệnh vẽ
  của chính mình mà chưa từng tính tới việc cảnh phía sau có thể tốn gấp nhiều lần.
- **ImmediatelyFast cố ý KHÔNG cài — và lý do đáng ghi để không ai thêm lại.** Nó làm **crash dev
  client**: mixin config của nó **không khai báo refMap**, nên các target tên SRG (`m_285795_`…)
  không ánh xạ được sang tên Parchment mà dev runtime đang dùng → `Critical injection failure`.
  - **Chữ ký đó CÓ tồn tại trong 1.20.1** (dòng 199 của `GuiGraphics` giải mã) — mod **không** khắc
    với game, nó khắc với **môi trường ForgeGradle dev**, và cài vào client thật thì chạy bình thường.
    Cùng họ lỗi với vụ thiếu Mixin plugin ở Phase 0.
  - **Không mất gì**: nó tối ưu đúng đường vẽ immediate-mode mà addon vừa gộp batch bằng tay, nên nó
    là mod **tệ nhất** để cài khi muốn biết công sức đó có đáng không.
- **Hai chỗ tôi kết luận sai giữa chừng, ghi lại vì cả hai đều là lỗi phương pháp**:
  1. Nói "Embeddium không nạp" — nó có nạp. Grep của tôi khớp `with {embeddium}` trong khi log ghi
     `with {embeddium,rubidium}`. **Một mẫu tìm kiếm hụt trông y hệt một mod hỏng.**
  2. Nói lỗi là do bản 1.5.3 build cho 1.20.4 nên lệch chữ ký. Sai — chuỗi phiên bản đúng là
     `1.5.3+1.20.4` nhưng nguyên nhân nằm ở **ba chữ cuối thông báo lỗi**: `No refMap loaded`.
     **Đọc hết dòng lỗi trước khi suy luận từ tên file.**
- **Quy tắc đo từ giờ**: đo code của **chính ta** thì **tắt** nhóm mod này; bật lên để xem thứ người
  chơi thật sự thấy. Trộn hai việc thì không kết luận được gì.
- **Trạng thái**: `gradlew build` sạch, `runClient` boot sạch (`FATAL=0`, không lỗi mixin nào, tám
  mod nạp đủ).

### 2026-08-15 — CRASH khi mở tablet: viền texel nới vùng tô mà không nới lề
- **Người dùng mở tablet → game crash ngay, bản đồ không hiện.**
  `ArrayIndexOutOfBoundsException: Index -68 out of bounds for length 4900` tại `TerrainImage.slope`.
- **Nguyên nhân, và nó là lỗi kinh điển của dự án**: khi thêm **viền một texel** cho lọc tuyến tính,
  tôi mở rộng vùng **được tô** ra ngoài một texel — nhưng `MARGIN` vẫn để bằng `RELIEF_RUN`. Texel
  viền cũng được tô, mà mỗi ô được tô lại đọc thêm `RELIEF_RUN` mẫu nữa về phía bắc/tây → vượt ra
  ngoài vùng đã gom. Với `MARGIN=3`, `FIELD=70`: `fz=2` → mẫu bắc `2*70 + 2 - 210 = **-68**`.
  - **Đúng bài học số 6 đã ghi**: *"đổi kích thước một thứ thì phải xem lại nội dung bên trong nó"*,
    và số 3: *"một vùng hình học chỉ được định nghĩa ở ĐÚNG MỘT NƠI"*. `MARGIN` là một con số viết
    tay, nên nó không biết rằng vùng tô vừa lớn ra.
- **Sửa bằng cách cho tất cả suy ra từ một chỗ**: thêm hằng `BORDER = 1`, rồi
  `TEXTURE_PIXELS = SHEET_PIXELS + 2*BORDER` và **`MARGIN = BORDER + RELIEF_RUN`**. Giờ đổi một trong
  hai thì cái kia tự đúng theo. Mọi chỗ dùng số `1` cho viền cũng thay bằng `BORDER`.
- **Vì sao boot test không bắt được, và đây mới là điều đáng ghi**: `runClient` chỉ chứng minh client
  **lên tới màn hình chính**. Nó **không mở tablet**, nên **không chạy một dòng nào** của đường vẽ bản
  đồ. Suốt phiên này tôi coi "boot sạch" như một bảo chứng, mà nó chưa bao giờ là.
  - Đã làm cái đáng lẽ phải làm từ đầu: một harness **đọc hằng số thật qua reflection** rồi chạy đúng
    vòng lặp của `build()` và đúng các mẫu `slope()` lấy, khẳng định mọi chỉ số nằm trong mảng.
    Kết quả sau khi sửa: `4356 texel, chỉ số 3..4964 của 5184, dư 3 mỗi đầu`.
  - **Harness này tính ra đúng `-68` với hằng số cũ** — tức nó đã bắt được lỗi nếu viết trước.
  - Vẫn là đồ tạm trong scratchpad, chưa vào repo. Mục tồn đọng số 5 vẫn còn.
- **Bài học mới, nên thành luật**: *`gradlew build` + boot sạch chứng minh mã **biên dịch và nạp**
  được, không chứng minh nó **chạy** được.* Với mã có số học chỉ số, phải kiểm bằng thứ thật sự chạy
  số học đó — kể cả khi không dựng nổi cả game để chạy.

### 2026-08-15 — Gợn sóng đã hết. Đính chính nguyên nhân, và sửa speckle ở zoom cực xa
- **Người dùng xác nhận: bản đồ chạy, gợn sóng biến mất hoàn toàn, kéo mượt ở mọi mức zoom.** Đây là
  lần đầu mục "kéo mượt" được xác nhận sau năm lượt đuổi theo nó.
- **Người dùng đoán nguyên nhân là nấc zoom số tròn (1000/2000/…). Đó KHÔNG phải nguyên nhân**, và
  cần ghi rõ kẻo sau này ai đó tưởng phép biến đổi chung là thừa rồi gỡ đi.
  - **Lập luận quyết định**: trong lúc kéo, `span` **không đổi**. Gợn sóng xảy ra **giữa các khung
    hình ở cùng một mức zoom**. Nên *giá trị* của span — tròn hay không — không thể là thứ đã đổi.
    Thứ đổi là **cách tính vị trí khi tâm dịch chuyển**.
  - Nguyên nhân thật là bỏ `place()`: trước đây **mỗi sheet tự làm tròn hai mép ra điểm ảnh nguyên**,
    nên khi tâm vượt ranh giới một block, các sheet vượt ngưỡng làm tròn ở những khoảnh khắc khác
    nhau và trượt lên nhau 1px. Giờ mọi sheet nằm trong **một phép biến đổi chung**, không còn gì để
    làm tròn riêng.
  - **Phép thử bác bỏ, rẻ và dứt điểm**: đặt một nấc zoom thành số lẻ (vd. 1234) — bản đồ vẫn phải
    mượt. Nếu nó mượt thì số tròn không liên quan.
  - Nấc số tròn vẫn đáng giữ, nhưng vì lý do khác hẳn: **đọc cự ly trên lưới**.
- **Người dùng báo 32000m pixel hoá nặng, 16000m vẫn giữ được chi tiết tối thiểu.** Đo bằng harness
  đọc hằng số thật và gọi thẳng `levelFor`:
  | Span | Mức | Texel | So với panel | **Đất thực sự được nhìn / texel** |
  |---|---|---|---|---|
  | 500m | 0 | 500 | 89% | 100% |
  | 2000m | 1 | 500 | 89% | 100% |
  | 8000m | 2 | 500 | 89% | **6,25%** (16 / 256) |
  | 32000m | 3 | 500 | 89% | **0,4%** (16 / 4096) |
- **Số liệu bác bỏ chẩn đoán hiển nhiên**: 32000m có **đúng bằng** số texel của 500m/2000m/8000m so
  với panel (89%). **Không phải thiếu texel.** Thứ sụp đổ là **nội dung của từng texel**: ở mức 3 một
  texel phủ 4096 block mà chỉ lấy 16 mẫu — bốn phần nghìn — nên hai texel cạnh nhau bất đồng ngẫu
  nhiên. Đó chính là speckle, và nó là bài toán **lấy mẫu**, không phải bài toán **độ phân giải**.
- **Đã sửa**: số mẫu mỗi texel giờ **theo stride** (`samplesFor`), trần 8×8 thay vì cố định 4×4.
  | Span | Trước | Sau |
  |---|---|---|
  | 2000m/4000m | 100% | 100% (mức 1 giờ **chính xác tuyệt đối**) |
  | 8000m/16000m | 6,25% | **25%** |
  | 32000m | 0,4% | **1,6%** |
  Mức 0 không đổi một phép tính nào (`sub = 1`), nên zoom gần không tốn thêm gì.
- **Giới hạn còn lại, nói thẳng**: ở 32000m, panel rộng ~563 điểm ảnh phải chứa 32 km — **57 block
  mỗi điểm ảnh**. Không renderer nào vượt qua được con số đó. Lấy mẫu dày hơn làm ảnh **ổn định và
  trung thực hơn**, không làm nó **chi tiết hơn**. Nếu 32000m vẫn vô dụng thì câu hỏi đúng là *nấc
  đó có đáng tồn tại không*, chứ không phải *vẽ nó thế nào*.
- **Trạng thái**: build sạch, boot sạch, harness chỉ số vẫn qua (`3..4964 của 5184`).

### 2026-08-15 — Kim tự tháp mip: bỏ hẳn việc lấy mẫu, chuyển sang trung bình
- **Người dùng đính chính tôi**: bản đồ đen ở zoom xa là vì **chunk chưa từng được sinh ra**, không
  phải do `SCAN_TILES`. Đúng — server chỉ đọc chunk **đã lưu**, và để trống chỗ chưa ai tới là hành
  vi đã chốt trong bảng kiến trúc. Tôi đã quy sai nguyên nhân; bỏ luôn ý định nới giới hạn quét.
- **Bóc bytecode JourneyMap để trả lời "họ làm thế nào"** (`journeymap.client.texture.RegionLodGenerator`):
  ```
  public static NativeImage[] generate(NativeImage, int)   ← trả về CẢ CHUỖI LOD
  private static int alphaBlend(int, int, int, int)        ← trộn ĐÚNG 4 pixel
  private static final float[] GAMMA_POW_2_2               ← trộn trong ánh sáng tuyến tính
  ```
  Bytecode xác nhận `width >> shift` rồi **bốn** `getPixelRGBA` với hệ số `iconst_2` — **hộp 2×2**.
  `RegionTexture` giữ `NativeImage[] lodImages`; `LodRegionCache.writeLodChainBytes` **ghi cả chuỗi
  xuống đĩa**.
- **Khác biệt cấu trúc, và nó lớn**: trong chuỗi 2×2, **mọi block đều góp vào pixel cuối với trọng số
  bằng nhau**. Ta thì dựng **mỗi mức độc lập từ dữ liệu gốc** bằng cách lấy mẫu thưa — 64 trên 4096.
- **Chỗ ngược đời, và là lý do phải làm**: kim tự tháp **RẺ HƠN** thứ nó thay thế.
  | | Cũ (mức 3) | Kim tự tháp |
  |---|---|---|
  | Số lần đọc | 64/texel × 4356 = **279 000 mỗi lần dựng sheet** | 4/texel, mức L đọc mức L−1 |
  | Tổng cho một ô | — | **~5 460 lần, một lần duy nhất khi ô về** |
  | Đất được nhìn | 1,6% | **100%** |
- **Đã làm — `TerrainMips`**: mỗi ô khi về dựng luôn chuỗi 32→16→8→4→2→1. **Không giữ mức 0** (ô đã
  là mức 0; giữ thêm bản sao là gấp đôi bộ nhớ cache mà không được gì).
  - Ánh xạ đúng khít: stride 1→mức 0, 4→2, 16→4, 64→6. **100% ở mọi mức.**
  - **Trộn trong ánh sáng tuyến tính** (bảng `pow(c/255, 2.2)`), như họ. Trung bình trong không gian
    sRGB làm mọi ảnh thu nhỏ tối và đục đi — đó là lý do bản đồ zoom xa hay "bẩn".
  - Texel chưa khảo sát bị **loại khỏi trung bình** chứ không tính là đen, nên mép đất vẫn là mép
    thay vì nhoè thành viền tối.
- **Hệ quả thiết kế bắt buộc, đã nói trước khi làm**: chuỗi mip trung bình **màu đã hoàn thiện**, nên
  bảng màu + tint quần xã + lớp nước phải tính **ở mức 0**. Vì thế toàn bộ phần đó **chuyển sang
  `TerrainMips.groundColour`** và `TerrainImage.shade` giờ chỉ còn làm hai việc phụ thuộc vào lân cận
  và vào mức: **đổ bóng** và **làm tối tổng thể**.
  - **Đây là chỗ dễ sinh lỗi nhất và đã bịt**: nếu hai đầu tự tính màu riêng thì bản đồ sẽ **đổi diện
    mạo đúng ở nấc zoom mà mức thay đổi**. Một định nghĩa, một nơi.
  - Đã xoá code chết ở `TerrainImage` (tint, nước, tally màu phổ biến, `samplesFor`) — không để hai
    bản của cùng một logic.
- **Mất một thứ, ghi rõ**: damping đổ bóng dưới nước chỉ còn ở **mức 0**, vì các mức thô không giữ độ
  sâu trung bình. Từ 4 block/texel trở lên, đổ bóng đáy biển vốn đã là tiếng thì thầm và màu đã mang
  sẵn nước.
- **Trạng thái**: build sạch, boot sạch (`FATAL=0`), harness chỉ số qua (`3..4964 của 5184`), ánh xạ
  mip kiểm bằng reflection: **100% ở cả bốn stride**. **Chưa test tay. Chưa commit.**
- **Cần người dùng kiểm**: (1) **8000m/16000m/32000m — bờ biển có còn kết cấu, hết lốm đốm chưa**;
  (2) kéo ở zoom xa có ổn định không (đảo nhỏ không được chớp tắt); (3) **các mức gần phải y hệt** —
  mức 0 đi đúng đường cũ; (4) **màu ở mức thô có khớp với mức gần không** khi zoom qua lại quanh nấc
  đổi mức (2000m↔4000m, 8000m↔16000m) — đây là phép thử của "một định nghĩa màu"; (5) nước ở zoom xa
  vẫn ra nước chứ không thành xám.

### 2026-08-15 — Gỡ Xaero, và đọc source WhyMap (mã nguồn mở) xem học được gì
- **Đã gỡ Xaero khỏi `build.gradle`** theo yêu cầu. Toạ độ vẫn giữ trong `gradle.properties` — một
  dòng `runtimeOnly` là quay lại. JourneyMap còn nguyên. Boot sạch, `FATAL=0`.
- **WhyMap KHÔNG cài được**: project 815690 chỉ có bản **Fabric**, không có một bản Forge nào cho bất
  kỳ phiên bản Minecraft nào. Ghi lại để không ai tìm lại lần nữa.
- **Nhưng nó mã nguồn mở**, nên đọc thẳng (`tiles/region/MapArea.kt`).
- **Điều quan trọng nhất, và nó ngược với kỳ vọng**: **khâu thu nhỏ của họ là LẤY MẪU ĐIỂM.**
  `_render(regionThumbnailScaleLog)` duyệt cột với bước `scale = 1 shl scaleLog` và **lấy đúng một
  cột cho mỗi điểm ảnh**. Đó chính xác là thứ ta **vừa vứt đi** hôm nay để thay bằng kim tự tháp
  trung bình.
  - Nghĩa là ở trục "zoom xa giữ kết cấu", **ta đang đứng trên JourneyMap và trên WhyMap**. Thế mạnh
    thật của WhyMap nằm ở **chiều zoom VÀO** (trình duyệt + Leaflet + tile JPEG, xem được tận texture
    từng block), không phải chiều zoom ra.
  - Ghi lại vì đây là loại kết luận ngăn được một đợt làm việc vô ích.
- **Ba thứ đáng học, xếp theo giá trị:**
  1. **Họ lưu `blockId` (16 bit), không lưu mã màu bảng.** Màu tra ở lúc vẽ qua bảng tra theo block
     id. Bảng màu vanilla chỉ có 62 mục — đó là trần cứng về màu mà ta đã đụng phải khi so với Xaero.
     **Đường nâng cấp cụ thể cho ta**: server gửi **block id** thay cho mã màu (1 byte → 2 byte),
     client tra màu trung bình của **texture thật**. Đây đúng là thứ cả Xaero lẫn WhyMap đều làm, và
     là khác biệt màu sắc lớn nhất còn lại giữa ta và họ.
  2. **Họ lưu lớp phủ RIÊNG** (`blockOverlayIdMap`) — khối nền và khối nước/thực vật là **hai lớp**,
     ghép lại lúc vẽ theo trọng số độ sâu. Ta nướng nước thẳng vào màu ở mức 0. Cách của họ mềm dẻo
     hơn (tắt/bật lớp nước, đổi công thức mà không phải khảo sát lại).
  3. **Họ có `formatVersion` + thứ tự byte trong header, và remap block/biome khi nạp bản cũ.**
     **`TerrainTile` của ta KHÔNG có trường phiên bản** — mà hôm nay ta vừa đổi định dạng từ 3 sang 6
     byte mỗi cột. Không vỡ vì client không lưu ô xuống đĩa, nhưng `ServerTileCache` và bất kỳ cache
     đĩa nào sau này sẽ dính ngay. **Thêm một byte phiên bản là rẻ; đi tìm lỗi vì thiếu nó thì không.**
- **Hai thứ đã cân nhắc rồi bỏ:**
  - **Nén LZMA2 (XZ) thay Deflate.** Tỉ lệ tốt hơn thật, nhưng ô của ta đã xuống **2 173 byte** cho
    địa hình thực tế, lại còn được `contentHash` + trả lời "không đổi" chặn bớt. Thêm một thư viện
    ngoài để tiết kiệm vài trăm byte là đổi sai chiều.
  - **Đổ bóng bằng vector pháp tuyến + bảng tra `atan`.** Đúng hơn về mặt vật lý so với `tanh` của ta,
    nhưng nó giải bài toán *hướng dốc*, còn hai thang đo của ta giải bài toán *lượng tử hoá theo
    block* — hai vấn đề khác nhau. Cân nhắc lại nếu đổ bóng còn thiếu, không phải bây giờ.
- **Kiến trúc trình duyệt của họ không chuyển sang được**: tablet là một thiết bị trong game, không
  phải một tab trình duyệt.

### 2026-08-15 — Biển nổi vân ở 2000m: độ sâu không đi qua chuỗi mip
- **Người dùng thấy diện mạo đổi hẳn giữa 1000m và 2000m**, và 32000m vẫn "deform nặng".
- **Lỗi thật, và tôi đã tự viết ra nó ở lượt trước rồi tự trấn an**: chuỗi mip không mang `depth`, nên
  `fieldMurk = 0` ở mọi mức thô → **damping đổ bóng dưới nước biến mất hoàn toàn từ 2000m trở lên**.
  Tôi đã ghi lý do là *"từ 4 block/texel trở lên, đổ bóng đáy biển vốn đã là tiếng thì thầm"*. **Sai.**
  Đáy biển là địa hình gồ ghề nhất thế giới, và bỏ damping làm cả mặt biển nổi vân đúng ở nấc đổi mức
  — tức đúng chỗ dễ nhìn ra nhất.
  - **Bài học**: khi cố ý bỏ một thứ và tự viết "chắc không sao", đó là lúc phải **ghi vào danh sách
    cần kiểm**, không phải lúc để tự thuyết phục. Tôi đã ghi nó vào phần "Cần người dùng kiểm" nhưng
    dưới dạng một ghi chú, không phải một câu hỏi.
- **Đã sửa**: `TerrainMips` mang thêm mảng `depth` qua từng mức, trung bình **chỉ trên các texel có
  đất** (một texel nửa bãi biển nửa nước nông phải ra nông, không ra sâu như phần nước trong nó).
  Thêm ~1365 byte mỗi ô — không đáng kể.
- **Còn thứ KHÔNG phải lỗi, cần phân biệt**: từ 2000m trở lên, một điểm ảnh phủ **16 block trở lên**,
  nên rừng + cỏ + bãi trống trộn thành một tông trung bình. Ở 1000m ta nhìn từng block, ở 2000m ta
  nhìn trung bình. **Đó là ý nghĩa của việc một điểm ảnh phủ 16 block**, không phải bệnh — và mọi
  bản đồ có kim tự tháp đều có bước chuyển này.
- **32000m**: vùng đã khảo sát chỉ chiếm khoảng **30 texel ngang** trên toàn màn hình (57 block mỗi
  điểm ảnh, 64 block mỗi texel). Không có gì để "deform" — chỉ đơn giản là không còn điểm ảnh nào để
  vẽ. Nếu nấc đó không dùng được thì câu hỏi đúng là **có nên giữ nấc 32000m không**, chứ không phải
  vẽ nó thế nào.
- **Trạng thái**: build sạch, boot sạch, harness chỉ số qua. **Chưa test tay. Chưa commit.**

### 2026-08-15 — Harness dựng PNG ngoài Minecraft: cuối cùng cũng NHÌN được bản đồ
- **Người dùng hỏi có nên đập đi làm lại.** Trả lời: **không** — nhưng lý do mới là phần đáng ghi.
  Đếm lại lỗi cả phiên (kênh R/B, lava thành nước, `MARGIN` gây crash, damping nước biến mất, bảng đo
  vòng tròn, quy sai bản đồ đen, báo nhầm Embeddium): **gần như tất cả là suy luận sai từ kiểm chứng
  thiếu, không phải kiến trúc sai.** Viết lại từ đầu tái tạo đúng loại lỗi đó với đúng tần suất.
  - Nguyên nhân thật: **Claude không nhìn thấy bản đồ.** Mọi lỗi hiển thị phải đi trọn một vòng qua
    người dùng. Đó là nút thắt, và đập đi làm lại không đụng tới nó.
- **Đã làm — `TileRender` (scratchpad)**: dựng đường tô **thật** ra PNG, **ngoài Minecraft**.
  - Gọi code sản phẩm chứ không phải bản sao: ô tổng hợp đi vào `TerrainClientCache.accept` thật
    (nên chuỗi mip thật được dựng), rồi `gather` thật và `shade` thật quyết định từng điểm ảnh, tất
    cả qua reflection. **Không có gì được viết lại, nên nó không thể trôi khỏi thứ game vẽ.**
  - Chạy được vì hai thứ: `TerrainImage` không đụng GL trong constructor, và `groundColour` với
    `biome = NO_BIOME` không gọi `Minecraft.getInstance()`. Tint quần xã vì thế **chưa kiểm được** —
    đó là lỗ hổng duy nhất còn lại của harness.
- **Ngay lần chạy đầu nó đã dạy một bài học về chính phép thử**: cảnh thử đầu tiên dựng bằng tổng các
  hàm sin — tức vài **tần số thuần** — và thu nhỏ một tần số thuần thì sinh **moiré**. Ảnh ra sọc chéo
  rõ mồn một và **trông y hệt một lỗi bộ lọc**. Đổi sang nhiễu giá trị băm (broadband, như địa hình
  thật) thì sọc biến mất sạch.
  - **Bài học: một phép thử sai có thể buộc tội code những lỗi nó không có.** Suýt nữa tôi đi sửa bộ
    lọc đang đúng.
- **Bốn mức cạnh nhau (stride 1 / 4 / 16 / 64) xác nhận bằng mắt**: lava ra **đỏ**; nước xanh sâu và
  **không bùng vân** ở mức thô (bản sửa `depth`-qua-mip đúng); bờ biển ở stride 16 và 64 vẫn **liền
  mạch, không vỡ bậc thang**; **màu không nhảy giữa các mức** — phép thử của "một định nghĩa màu" đi qua.
- **Còn tồn đọng của harness**: khung stride 1 và 4 rơi trọn vào biển vì sheet gốc (0,0) chỉ phủ 64 và
  256 block mà bờ ở x≈300 — cần cho phép chọn tâm. Và nó vẫn ở scratchpad, **chưa vào repo**; mục tồn
  đọng số 5 vẫn còn.

### [Chưa có entry tiếp theo]

### 2026-08-15 — Món 1: vẽ bản đồ ở độ phân giải màn hình
- **Người dùng nói thẳng điều họ muốn**: hình ảnh, cảm giác zoom và hiệu năng của JourneyMap. Đối
  chiếu lại thì **hiệu năng đã xong** (chính họ xác nhận "mượt ở mọi khoảng cách"), và trong danh sách
  kỹ thuật của các mod kia ta **chỉ còn thiếu hai món**: vẽ ở điểm ảnh màn hình, và màu từ texture
  block thật. Không phải hai mươi món.
- **Đã làm món 1.** Giao diện được dựng bằng điểm ảnh riêng của nó rồi **phóng to theo GUI scale**,
  nên ở scale 3 một vùng bản đồ trông rộng 1689 điểm ảnh thật ra chỉ có **563 điểm ảnh để vẽ vào**.
  Một texel không bao giờ nhỏ hơn 3 điểm ảnh màn hình được — và ở một nửa số nấc zoom, dữ liệu ta có
  **gần gấp đôi cái trần đó**, tức bị vứt đi trước khi kịp vẽ.
- **Kết quả đo bằng harness đọc hằng số thật:**
  | Span | Texel có | Hiện được TRƯỚC | Hiện được SAU | |
  |---|---|---|---|---|
  | 1000m | 1000 | 563 | **1000** | **+78%** |
  | 4000m | 1000 | 563 | **1000** | **+78%** |
  | 16000m | 1000 | 563 | **1000** | **+78%** |
  | 250/500/2000/8000/32000m | 250–500 | như cũ | như cũ | — |
- **Nói rõ giới hạn**: năm nấc còn lại **không tăng texel**, vì ở đó ta vốn đã có ít hơn cả trần cũ.
  Muốn tăng thì phải chọn mức mịn hơn, mà mức mịn hơn ở sheet 64 texel làm số lệnh vẽ nổ tung — đó là
  **món 2 (sheet to hơn)**, không phải món này. Nhưng cả tám nấc đều được **chuyển động mịn gấp ba**,
  vì giờ hình rasterise trên lưới màn hình thay vì lưới GUI.
- **Ràng buộc bắt buộc, và nó là luật cũ của dự án**: *chữ tuyệt đối không đi qua ma trận điểm ảnh
  vật lý* — font là bitmap nướng cho lưới giao diện. Nên nhãn toạ độ lưới giờ được **thu thập trong
  ma trận rồi vẽ ở ngoài**, bằng điểm ảnh giao diện. Đường lưới giữ nguyên bề dày biểu kiến
  (`rule = round(guiScale)` điểm ảnh màn hình) chứ không mảnh đi ba lần — đó sẽ là đổi diện mạo, không
  phải việc của lượt này.
- **Không đụng tới**: `worldToScreen`/`screenToWorld` vẫn ở điểm ảnh giao diện, nên chuột và ký hiệu
  không đổi một dòng nào. Hai hệ vẫn khớp vì cùng suy ra từ một phép ánh xạ liên tục.
- **Trạng thái**: build sạch, boot sạch (`FATAL=0`). **Chưa test tay.**
- **Cần người dùng kiểm**: (1) **1000m / 4000m / 16000m có nét hơn rõ rệt không** — đây là phép thử;
  (2) các nấc còn lại **không được xấu đi**; (3) **nhãn toạ độ lưới có còn sắc nét** không (nếu nhoè
  là chữ đã lọt vào ma trận); (4) đường lưới có mảnh đi bất thường không; (5) kéo chậm còn mượt chứ;
  (6) đặt mục tiêu bằng chuột vẫn rơi đúng chỗ.

### 2026-08-15 — Món 2: sheet to gấp đôi, và cú nhảy mờ ở 2000m biến mất
- **Người dùng báo giữa lúc làm**: *"từ 1000 sang 2000 cảm giác map bị làm nhoè và mờ đi"*. Đúng, và
  đó chính xác là thứ món 2 sửa: ở 1000m ta vẽ **từng block một** (mức 0), sang 2000m ta nhảy thẳng
  sang **trung bình 16 block một texel** (mức 1). **Ta trung bình sớm hơn một nấc so với mức cần.**
- **Nguyên nhân là kích thước sheet, không phải bộ lọc.** Mức được chọn sao cho số quad ngang ≤ 16,
  nên sheet nhỏ **ép phải chọn mức thô rất lâu trước khi panel hết điểm ảnh**. Ở 2000m, mức 0 sẽ cần
  **1089 quad** với sheet 64 texel — nên ta buộc phải lùi về mức 1 dù panel dư chỗ.
- **Đã làm: `TILES_PER_SHEET = 2`**, tức sheet 128 texel thay vì 64. Mỗi quad phủ gấp đôi mặt đất, nên
  cùng một zoom giờ với tới được mức **mịn gấp bốn** mà **không thêm một lệnh vẽ nào** (vẫn ≤ 289 blit).
  | Span | Mức trước | Mức sau | Hiện được trước | Sau | |
  |---|---|---|---|---|---|
  | 1000m | 0 (stride 1) | 0 (stride 1) | 563 | 1000 | +78% |
  | **2000m** | **1 (stride 4)** | **0 (stride 1)** | 563 | **1689** | **+200%** |
  | 4000m | 1 | 1 | 563 | 1000 | +78% |
  | **8000m** | **2 (stride 16)** | **1 (stride 4)** | 563 | **1689** | **+200%** |
  | **32000m** | **3 (stride 64)** | **2 (stride 16)** | 563 | **1689** | **+200%** |
  → **Cú nhảy mờ dời từ 1000→2000 sang 2000→4000**, và ở 4000m thì trung bình là **đúng**: một điểm
  ảnh ở đó phủ 2,4 block thật.
- **Phần khó, và nó buộc một đơn giản hoá**: sheet mức 0 giờ phủ **4 ô** thay vì 1, nên phép kiểm
  "dữ liệu có mới không" bằng danh tính một ô không dùng được nữa. Thay bằng **`coverStamp`** — băm
  danh tính của **mọi ô sheet đọc**, kể cả lề mà ánh sáng với sang (16 ô).
  - **Nó nuốt luôn một ca đặc biệt**: `neighboursKnown` bị xoá, vì hàng xóm về **cũng làm đổi băm** y
    như mọi thứ khác. Hai nhánh logic gộp thành một.
  - Chỉ dùng ở mức 0. Sheet thô phủ hàng trăm ô nên vẫn dùng `version()` + cooldown.
- **Rủi ro mới đã chặn trước, không phải sau**: một sheet giờ **nặng gấp 4 lần** khi dựng. Trong lúc
  chơi việc lấp đầy vốn bị mạng ghìm nhịp (ô về vài chục cái một), nhưng **zoom ra trên đất đã khảo
  sát thì đòi ba trăm sheet cùng lúc** — không có trần thì đó là một khung hình làm việc của một phần
  ba giây. Thêm **`MAX_BUILDS_PER_FRAME = 16`** cho lần dựng **đầu tiên**, tách khỏi trần dựng lại.
  - Hai trần khác nhau vì hai bài toán khác nhau: sheet chưa từng dựng là **lỗ trống trên bản đồ**,
    phải lấp nhanh; sheet chờ làm mới chỉ là **cũ một giây**, không ai thấy.
- **Bộ nhớ**: texture 130×130×4 = 66 KB, trần 384 sheet ≈ **25 MB** (trước: 512 × 16 KB = 8 MB).
- **Trạng thái**: build sạch, boot sạch (`FATAL=0`), harness chỉ số qua (`3..18084 của 18496`), ảnh
  PNG dựng lại **không đổi** so với trước — đúng như mong đợi, vì đường tô màu không bị đụng tới.
- **Cần người dùng kiểm**: (1) **1000m → 2000m còn nhoè không** — đây là phép thử chính; (2) 8000m và
  32000m có nét hơn rõ rệt không; (3) **zoom ra nhanh trên vùng đã khảo sát có khựng không** (trần
  dựng mới); (4) có thấy ô trống lấp dần trong khoảng một phần năm giây sau khi đổi zoom không —
  **đó là hành vi đúng**, không phải lỗi; (5) mép sheet có đường nối nào không (kích thước sheet vừa
  đổi nên biên cũng đổi).

### 2026-08-15 — Đúng hai mức mờ, và đó là một dấu vân tay
- **Người dùng báo sau món 2**: *"4000m và 16000m bị mờ nhoè, tất cả các mức còn lại đạt yêu cầu"*.
  **Hai mức, không phải một dải** — đó là dữ liệu tốt nhất có thể có, vì nó loại hết mọi nguyên nhân
  tỉ lệ thuận với zoom.
- **Đặt cạnh tiêu chí "block trên mỗi điểm ảnh màn hình" thì lộ ngay:**
  | Span | blocks/px | stride | |
  |---|---|---|---|
  | 2000m | 1,18 | 1 | ✅ |
  | **4000m** | **2,37** | **4** | ❌ thô hơn cần 1,7× |
  | 8000m | 4,74 | 4 | ✅ |
  | **16000m** | **9,47** | **16** | ❌ thô hơn cần 1,7× |
  | 32000m | 18,9 | 16 | ✅ |
  **Đúng hai mức báo mờ là đúng hai mức có stride thô hơn mật độ điểm ảnh.** Không có gì khác trong
  tám nấc phân biệt được hai cái đó.
- **Quy luật rút ra, và nó nên thành luật**: *dữ liệu thô hơn màn hình thì trông mềm dù lọc kiểu gì;
  dữ liệu mịn hơn màn hình thì trông sắc.* Bộ lọc không cứu được việc chọn sai mức.
- **Nguyên nhân: `LEVEL_FACTOR = 4`.** Các mức cách nhau bốn lần, nên ở 4000m lựa chọn chỉ có stride 1
  (31 quad — quá nhiều) hoặc stride 4 (quá thô). **Không tồn tại stride 2.** Việc chọn mức luôn lấy
  mức mịn nhất còn vừa ngân sách quad, nên bước giữa các mức càng thô thì lựa chọn càng rơi xa "vừa đủ".
- **Đã sửa: `LEVEL_FACTOR` 4 → 2.** Giờ mọi zoom đều với tới được một mức đúng tầm:
  | Span | stride | texel | |
  |---|---|---|---|
  | 250 / 500 / 1000 / 2000m | 1 | 250–2000 | **mịn nhất thế giới cho phép** |
  | 4000m | **2** | 2000 | sharp |
  | 8000m | 4 | 2000 | sharp |
  | 16000m | **8** | 2000 | sharp |
  | 32000m | 16 | 2000 | sharp |
  Số quad vẫn bị kẹp như cũ (≤ 16 ngang), nên **không thêm lệnh vẽ nào**. Chuỗi mip đã sẵn có đủ mọi
  mức luỹ thừa hai nên không phải đụng gì.
- **Harness suýt lừa tôi lần thứ hai.** Tiêu chí `stride <= blocks/px` gán nhãn `SOFT` cho 250m/500m/
  1000m — nhưng ở đó stride đã là **1**, tức mịn nhất thế giới có. Đó không phải "quá thô", đó là
  **sàn**. Đã sửa nhãn thành `finest`. *Một phép thử gán nhãn sai là một phép thử buộc tội code những
  lỗi nó không thể tránh* — đúng bài học của cảnh thử moiré, lặp lại trong cùng một phiên.
- **Trạng thái**: build sạch, boot sạch (`FATAL=0`), harness chỉ số qua, **cả tám nấc đạt tiêu chí**.
- **Cần người dùng kiểm**: (1) **4000m và 16000m đã hết mờ chưa** — phép thử chính; (2) sáu nấc còn
  lại không được xấu đi; (3) zoom qua lại giữa các nấc có thấy nhảy màu không (giờ có nhiều mức hơn,
  tức nhiều ranh giới đổi mức hơn); (4) bộ nhớ/độ mượt khi zoom ra xa có ổn không (nhiều mức hơn nghĩa
  là nhiều bộ sheet được cache hơn).

### 2026-08-15 — HỒI QUY: bản đồ load cực chậm ở zoom xa. Ba lỗi quanh ô rỗng
- **Người dùng báo ngay sau lượt trước**: *"map load siêu chậm và lag, tôi còn ko cả nhìn được map
  load ở các mức zoom xa"*. Hồi quy do chính tôi gây ra.
- **Con số chỉ thẳng nguyên nhân** (đếm bằng script, không đoán):
  | Span | Sheet trong tầm nhìn | Có thể có đất | **Rỗng** |
  |---|---|---|---|
  | 8000m | 289 | ~49 | **240** |
  | 16000m | 289 | ~16 | **273** |
  | 32000m | 289 | ~4 | **285** |
  Ở zoom xa, **gần như toàn bộ sheet là rỗng** — vì `SCAN_TILES` chỉ xin đất trong ~3000 block, mà
  một sheet ở mức 4 đã phủ 2048 block.
- **Ba lỗi chồng lên nhau, cả ba đều xoay quanh ô rỗng:**
  1. **Ô rỗng không bao giờ được đánh dấu "đang trong tầm nhìn".** `draw()` chỉ đặt `lastDrawn` cho
     sheet **được vẽ**, mà sheet rỗng trả về `null` nên bỏ qua dòng đó. LRU vì thế thấy chúng cổ lỗ
     nhất → **đuổi đi → khung hình sau dựng lại → lại rỗng → lại đuổi.** Mỗi lần dựng lại là 18 496 ô
     để phát hiện lại rằng không có gì.
  2. **Ô rỗng vẫn giữ nguyên một texture 66 KB.** 285 × 66 KB ≈ **19 MB ảnh trắng**, và chúng chiếm
     chỗ trong trần 384 — **các ô trắng đang chen chỗ của đất thật**.
  3. **Trần dựng mới (16/khung hình) bị vòng quay đó ăn sạch**, nên sheet có đất thật không bao giờ
     tới lượt. Đó chính xác là *"không nhìn được map load"* — không phải chậm, mà là **không bao giờ
     được dựng**.
- **Vì sao nó chỉ bùng lên bây giờ**: lỗi 1 có từ lâu, nhưng trước đây một sheet rẻ gấp bốn, số sheet
  rỗng ít hơn, và **chưa có trần dựng mới để mà bị ăn**. Ba thay đổi của lượt trước biến một chỗ rò
  âm thầm thành chỗ vỡ. **Lại đúng bài học "thay đổi LÀM LỘ lỗi khác với thay đổi GÂY RA lỗi"** — chỉ
  có điều lần này tôi vừa làm lộ vừa làm nặng thêm.
- **Đã sửa cả ba**: `lastDrawn` đặt trong `sheetFor` trên **mọi** đường trả về (ô rỗng trong tầm nhìn
  vẫn là ô trong tầm nhìn); ô rỗng **trả lại texture** nhưng **giữ bản ghi** (bản ghi là ký ức đã nhìn
  và không thấy gì — bỏ nó đi là xét lại mỗi khung hình); và ô rỗng **chờ lâu gấp 10 lần** trước khi
  được xét lại, vì đất mọc ra chỗ trước đó trống là chuyện hiếm và đi theo nhịp mạng.
- **Trạng thái**: build sạch, boot sạch, harness chỉ số qua, ảnh PNG không đổi.
- **Cần người dùng kiểm**: (1) **zoom xa có load bình thường trở lại không** — phép thử chính; (2) độ
  nét ở 4000m/16000m vẫn giữ như lượt trước chứ; (3) kéo ở zoom xa có mượt không; (4) đi tới vùng
  chưa khảo sát rồi quay lại — đất có hiện ra không (phép thử của "chờ lâu gấp 10 lần": chậm vài giây
  là đúng, không bao giờ hiện là sai).

### 2026-08-15 — CRASH sau vài phút chơi: bẫy entry set của fastutil
- **Người dùng**: zoom xa load lại bình thường (chậm vài giây, đúng như thiết kế), nhưng **chơi vài
  phút thì game crash**.
- **`NullPointerException` trong `evictSurplus`**: `Map.Entry.getValue()` trả về `null`.
- **Nguyên nhân — và nó là lỗi tôi gieo từ mấy commit trước, không phải từ lượt vừa rồi.** Khi đổi
  `sheets` sang `Long2ObjectOpenHashMap` để bỏ việc đóng hộp `Long`, tôi để nguyên
  `new ArrayList<>(sheets.entrySet())`. **Entry set của fastutil phát ra MỘT đối tượng Entry được tái
  sử dụng** trong lúc duyệt — nên cái list đó là N tham chiếu tới cùng một entry, và entry đó **hết
  hiệu lực ngay khi vòng lặp đi tiếp**. Duyệt xong, `getValue()` trả `null`.
- **Vì sao mãi tới giờ mới nổ**: `evictSurplus` chỉ chạy khi `sheets.size() > MAX_LIVE_TEXTURES`. Với
  cấu hình cũ trần đó gần như không bao giờ chạm; sau khi sheet to gấp đôi và số mức tăng, nó chạm
  được — nhưng phải khám phá bản đồ **vài phút** mới đủ tích luỹ. **Một lỗi ngủ đông chờ đủ dữ liệu.**
- **Bài học, và nó đáng thành luật**: *đổi một `HashMap` sang map khoá nguyên thuỷ **không phải** thay
  thế trong suốt.* Nó đánh đổi việc cấp phát lấy **những quy tắc về cách được phép dùng các view của
  nó**. Tôi coi đó là thay đổi hiệu năng thuần và không đọc hợp đồng.
- **Đã sửa**: `evictSurplus` giờ làm việc trên **bản sao của khoá**, không bao giờ trên entry set.
  Việc đóng hộp ở đây là **cố ý và vô hại** — hàm chỉ chạy khi vượt trần, khác hẳn các lượt tra cứu
  mà map được đổi sang nguyên thuỷ để phục vụ.
- **Đã viết harness `Evict`** — nhồi cache vượt trần rồi gọi thẳng `evictSurplus`, không cần đồ hoạ.
  Kết quả: `576 → 384, giữ hết những gì trong tầm nhìn, bỏ cái cũ nhất`. **Với code cũ nó ném NPE
  ngay lần chạy đầu.**
  - Đây là đường mà **build sạch và boot sạch không bao giờ chạm tới được** — nó chỉ chạy sau nhiều
    phút chơi. Đúng loại lỗ hổng mà harness sinh ra để lấp.
- **Trạng thái**: build sạch, boot sạch, ba harness (`Bounds`, `Evict`, `TileRender`) đều qua.

### 2026-08-15 — Tác giả JourneyMap trả lời: ĐÓNG hướng này, và kiến trúc của ta được xác nhận
- **Người dùng đã hỏi thẳng tác giả (Mysticdrew) trên Discord.** Ghi lại nguyên văn vì đây là kết luận
  cuối của một câu hỏi đã ngốn nhiều lượt:
  > *"Currently, we are not doing any mapping via the server. But, I've started adding in logic to
  > support it in the future."*
  >
  > **"You'll need to add a server component to your mod that sends the heightmaps to your client mod"**
  >
  > *"I added the network prep, to compress the snapshot for the network when I added the new caching
  > a while back. The new caching is designed to be a minimal chunk that can be used for regenerating
  > the map, and server side mapping."*
- **Ba suy luận từ bytecode đều đúng**: server hiện không lập bản đồ; `NETWORK_FORMAT_VERSION` là hạ
  tầng chuẩn bị chưa có gói tin dùng; `JMChunkSnapshot` là chunk tối giản mang heightmap.
- **Và câu quan trọng nhất là một xác nhận kiến trúc.** Khi được hỏi nên làm thế nào, tác giả kê
  **đúng thứ dự án này đã chạy**: một thành phần phía server gửi heightmap về client —
  `ServerTerrainProvider` → `TerrainTile` → `ModNetwork`. Không phải xác nhận về thẩm mỹ, mà về
  **cấu trúc**, từ người hiểu bài toán này nhất.
- **TÔI ĐÃ SAI khi khuyên đừng gửi thư.** Tôi đọc `Server Docs` thấy *"not a server-side mapping mod...
  nor even contemplated"* rồi coi câu hỏi là đã đóng. Tài liệu đó thuộc thời **5.7.1**; code là **6.0.1**
  và tác giả đã bắt đầu làm. **Tài liệu lỗi thời, và tôi kết luận từ nó thay vì từ code — trong khi
  chính tôi đã đọc code và thấy dấu vết.** Người dùng gửi thư là đúng.
  - Bài học: **tài liệu mô tả ý định lúc viết, code mô tả ý định hiện tại.** Khi hai thứ mâu thuẫn,
    code thắng — và ở đây chính tôi đã tìm ra mâu thuẫn rồi lại bỏ qua nó.
- **Kết luận thực hành: đóng hướng tích hợp JourneyMap.**
  - Không có mốc thời gian cho server-side mapping của họ.
  - Kể cả khi ra, nó phục vụ **bản đồ của họ**; việc có API cho addon lấy heightmap là chuyện tách
    biệt và chưa ai hứa.
  - Ta vẫn hơn ở điểm cốt lõi **ngay hôm nay**: server của ta đọc được đất **bất kỳ ai** từng đi qua.
- **Đáng để mắt về lâu dài, không đáng chờ**: `JMChunkSnapshot` mang `LevelChunkSection[]`, tức
  **block thật** chứ không chỉ heightmap. Nếu nó từng mở ra cho addon, đó sẽ là nguồn cho món "màu
  theo block id" — thứ còn lại duy nhất giữa ta và họ về hình ảnh.

### 2026-08-16 — Byte phiên bản cho ô, và tại sao nó chưa đủ
- **Việc số 2 trong danh sách tồn đọng.** Làm trước vì món "màu theo block id" sẽ đổi định dạng lần
  nữa, và thứ tự đúng là **dựng hàng rào trước khi trèo qua nó**.
- **Nhưng lý do ban đầu ghi trong kế hoạch là sai một nửa.** Ghi chú cũ dẫn WhyMap/JourneyMap: cả hai
  có `formatVersion` vì chúng **ghi ô xuống đĩa** rồi nạp lại ở bản sau. Ta **không ghi đĩa** — ô chỉ
  đi qua mạng. Nên byte phiên bản trong `TerrainTile` một mình không cứu được ai.
- **Lỗ thật nằm chỗ khác, và nó đang mở**: `ModNetwork.PROTOCOL_VERSION` vẫn là `"1"` **suốt cả lần
  đổi định dạng thêm hai trường mỗi cột ở phiên trước**. Forge từ chối bắt tay khi hai bên khác chuỗi
  này — nhưng chuỗi không đổi, nên một client cũ vẫn **bắt tay thành công** rồi giải mã 12 KB rác.
- **Đã sửa cả hai đầu, và nối chúng lại**: `TerrainTile.FORMAT_VERSION` ghi ở đầu mỗi ô và kiểm lúc
  đọc; `PROTOCOL_VERSION = "1." + FORMAT_VERSION`. Nửa sau **không viết tay** — đó là điểm chính: đổi
  bố cục ô mà quên bump là chuyện đã xảy ra một lần rồi, giờ nó **không thể xảy ra** nữa. Nửa đầu vẫn
  phải bump tay cho mọi packet khác.
- **Bài học**: *một ghi chú kế hoạch cũng là tài liệu, và tài liệu mô tả ý định lúc viết.* Món này
  được ghi là "rẻ, mười lăm phút, chặn cả một lớp lỗi" — đúng về giá, sai về lớp lỗi nó chặn. Đọc lại
  code trước khi làm theo ghi chú của chính mình.

### 2026-08-16 — Ba harness vào repo: `./gradlew mapCheck`
- **Việc số 3.** Chúng đang nằm ở scratchpad của một phiên **khác** (`7d210c80…`), tức là cách một
  lần dọn thư mục tạm là mất — sau khi mỗi cái đã bắt một crash mà build sạch và boot sạch đều cho qua.
- **Cách làm**: source set riêng `src/mapcheck/java`, kế thừa classpath của `main` (ForgeGradle đặt
  jar Minecraft ở đó, nên đó là thứ cho phép chúng chạm `MapColor`), một `JavaExec` tên `mapCheck`.
  **Không để trong `test`**: chúng không phải unit test — chúng lái code sản phẩm bằng reflection, và
  một cái *vẽ một bức ảnh để nhìn* chứ không khẳng định gì.
- **Giữ nguyên reflection, có chủ ý.** Đổi sang gọi thẳng thì phải nới `private` của `TerrainImage`,
  mà nguyên tắc "một vùng hình học chỉ định nghĩa ở đúng một nơi" nói không. Đổi lại: đổi tên field
  làm chúng vỡ lúc chạy chứ không lúc biên dịch — chấp nhận được, vì bù lại chúng **không thể nói dối**.
- **Thêm cái thứ tư, `RoundTrip`** (cũng từ scratchpad cũ): ghi một ô rồi đọc lại, và **in ra số byte
  trên dây**. Đây là thứ khiến việc đổi bố cục ô ở entry sau **an toàn để làm**: sai một offset thì
  không có gì ném ra — ô giải mã được, mọi cột có giá trị, và bản đồ vẽ cao độ thành quần xã.
- **Phải gọi `Bootstrap.bootStrap()`** trong runner: registry block không tồn tại ngoài game. Cùng
  cặp lệnh mà data generator dùng — không cửa sổ, không thế giới, không mod.
- **Trạng thái**: `./gradlew mapCheck` chạy, cả bốn qua, PNG ghi ra `build/mapcheck/`.

### 2026-08-16 — Màu theo block id: gửi *block gì*, thay vì một trong sáu mươi hai màu
- **Việc số 1, thứ duy nhất còn lại giữa ta và JourneyMap/Xaero về hình ảnh.** Bảng màu vanilla có
  **62 màu**: mọi loại gỗ là **một** màu nâu, mọi loại đá là **một** màu xám. Không renderer nào bù
  được chuyện đó.
- **Đã đổi**: cột mang **id registry của block** (2 byte, đọc **không dấu** — modpack lớn vượt 32767)
  thay cho id màu bản đồ (1 byte). Client tra `BakedModel` của block, lấy **quad mặt trên** — mặt duy
  nhất bản đồ nhìn thấy — rồi **trung bình texture trong ánh sáng tuyến tính**.
  - **Không dùng particle icon**, dù nó là đường dễ nhất: particle icon của `grass_block` là **đất**,
    nên mọi đồng cỏ trên thế giới sẽ ra màu nâu.
- **Tint đổi cách quyết định.** Trước: dựa vào **id màu bản đồ** là GRASS hay PLANT. Giờ: **model tự
  nói** — quad có `tintIndex` nghĩa là texture của nó xám và đang chờ nhân với màu quần xã.
- **Và có HAI luật tint khác nhau, không phải một.** Đây là chỗ dễ sai nhất:
  | Nguồn màu nền | Texture đã có màu chưa | Luật đúng |
  |---|---|---|
  | Texture (grass_block_top) | **Chưa** — xám | **Nhân**: `kênh × tint / 255` |
  | Bảng màu (fallback) | **Rồi** — đã nướng sẵn xanh của plains | **Tỉ lệ**: `kênh × tint / reference` |
  Áp luật này cho trường hợp kia sai rất nặng. Nên `BlockPalette.isPreTinted` **nói ra** thay vì đoán.
- **Fallback là bảng màu cũ**, không phải lỗ hổng: chất lỏng được game vẽ bằng đường riêng nên không
  có quad, và một block trượt fallback trông **đúng như tuần trước**.
- **Nhân tiện, sửa luôn cách nén.** Mỗi trường hai byte giờ ghi **hai nửa thành hai dải riêng**, thay
  vì xen kẽ hi,lo,hi,lo. Id block nằm ở vài nghìn, nên byte cao là **4096 byte giống hệt nhau liên
  tiếp** — deflate lấy gần như miễn phí. Xen kẽ thì nửa thay đổi che mất chỗ lặp đó. Ô địa hình thật:
  **2104 byte trên dây** (28672 byte thô).
- **`TerrainTile.idOf` là nơi duy nhất** biến block thành số, vì **hai** sampler cùng nuôi một ô
  (NBT đã lưu, và chunk đã nạp) và cột từ hai đường phải không phân biệt được — nếu không, đường viền
  vùng đã nạp sẽ hiện thành một vệt trên bản đồ.
- **Gộp `LINEAR`/`encode` vào `Light`**: hai chỗ trung bình màu (chuỗi mip, và trung bình texture) phải
  bẻ ánh sáng **giống hệt nhau**, nếu không một block sẽ **đổi màu đúng ở nấc zoom đổi mức** — đúng
  loại lỗi dự án này đã sinh ra nhiều lần.
- **Bằng chứng thật, không phải "build sạch"**: `TileRender` giờ dựng cảnh bằng **block thật**
  (`Blocks.SAND`, `Blocks.OAK_LEAVES`, `Blocks.LAVA`…) và PNG ra **giống hệt từng điểm ảnh** với bản
  trước khi đổi. Nghĩa là **mọi thứ phía sau quyết định màu không đổi**, và đường ống id chạy đúng.
- **Trạng thái**: build sạch, boot sạch (15 packet, 0 ERROR, 0 FATAL), `mapCheck` cả bốn qua.
- **⚠️ Chưa được kiểm — và đây là phần quan trọng nhất**: **đường texture chưa từng chạy.** Harness
  chạy ngoài Minecraft nên `Minecraft.getInstance()` là null, tức nó đi **fallback bảng màu** cho mọi
  cột. Cái PNG giống hệt là bằng chứng cho đường ống, **không** phải cho việc tra texture.
- **Cần người dùng kiểm** (mở tablet, nhìn bản đồ):
  1. **Gỗ/đá có ra nhiều màu khác nhau không** — phép thử chính. Rừng sồi và rừng vân sam phải khác
     nhau; đá, granite, diorite phải khác nhau.
  2. **Cỏ có xanh không** (không xám → tint không chạy; không xanh quá đậm → tint chạy hai lần).
  3. **Nước, dung nham** vẫn đúng màu (đây là đường fallback).
  4. **Lần mở tablet đầu tiên có khựng không** — bảng tra dựng lười, mỗi block một lần.
  5. **Màu có nhảy khi zoom qua ranh giới đổi mức không** — phép thử của "một định nghĩa màu".

### 2026-08-16 — Test màu block id: 3/5 đạt ngay, 2 lỗi thật
- **Người dùng test.** Gỗ/đá ra nhiều màu ✅, cỏ xanh đúng ✅, không nhảy màu khi đổi mức ✅. **Đường
  tra texture chạy đúng** — đó là điều chưa ai kiểm được cho tới lúc này.
- **Lỗi 1 — dung nham đỏ thẫm như máu.** Và nó **không phải lỗi mới**: bảng màu vanilla cho lava là
  `MapColor.FIRE` = đỏ thuần, đã thế từ đầu; PNG harness từ trước cũng ra đúng cục đỏ thẫm đó. Đổi
  sang block id chỉ làm nó **nổi bật lên** giữa những màu khác giờ đã đúng.
  - **Nguyên nhân**: chất lỏng **không có quad nào** — game vẽ chúng bằng đường riêng — nên chúng rơi
    thẳng xuống fallback bảng màu.
  - **Đã sửa**: thêm một bậc **trước** fallback — `model.getParticleIcon()`. Với chất lỏng, particle
    texture **chính là ảnh tĩnh của nó** (`lava_still`), đúng thứ bản đồ cần.
  - **Vẫn chỉ là bậc cuối**, không được thử trước: particle icon của `grass_block` là **đất**. Thứ tự
    đúng là **quad mặt trên → quad bất kỳ → particle icon → bảng màu**, và lý do của thứ tự đó phải
    nằm ngay trong code.
- **Lỗi 2 — chớp đen rồi hiện từng phần, mỗi lần mở VÀ mỗi lần zoom.** Hai nguyên nhân riêng biệt cho
  cùng một triệu chứng:
  1. **`TabletScreen.removed()` gọi `map.close()`**, huỷ toàn bộ sheet; mà `TabletScreen` được dựng
     mới mỗi lần mở → `new MapPanel()` → `new TerrainImage()`. **Mỗi lần mở đều bắt đầu từ số không.**
     Đây không phải "dựng chậm" — công việc vừa bị vứt đi vài giây trước **không vì lý do gì**: đất
     không đổi trong lúc màn hình đóng.
     - **Đã sửa**: `TerrainImage.shared()`, một instance sống theo **thế giới**, giải phóng khi
       `generation` đổi (đường đã có sẵn). `removed()` bị xoá hẳn.
  2. **Đổi nấc zoom là đổi mức**, và mức mới **chưa từng dựng sheet nào** → đen.
     - **Đã sửa**: **lượt phủ thô vẽ trước, nằm dưới**. Đất không hề thiếu — nó nằm ở **2 mức thô hơn**,
       nơi một sheet phủ **16 lần** diện tích, nên **~20 sheet phủ hết tầm nhìn trong 1–2 khung hình**.
       Mắt thấy bản đồ **hiện mờ rồi nét dần**, thay vì hiện từng mảnh.
     - **Chỉ chạy khi lượt tinh còn lỗ.** Xong rồi thì nó đang dựng thứ **không ai nhìn xuyên qua
       được**, mà giá dựng thì bằng sheet thật.
     - **Ô đã biết là rỗng KHÔNG tính là lỗ** — lượt thô cũng sẽ không tìm ra đất ở đó.
- **Thêm bất biến vào harness**: với **mọi** mức 0..`MAX_LEVEL` (kể cả mức của lượt phủ, vốn cao hơn
  bất kỳ nấc zoom nào), `strideFor` và `levelForStride` phải là **nghịch đảo chính xác**. Sai chỗ này
  **không ném ra và không trông như hỏng** — nó trông như đất bình thường ở sai tỉ lệ.
- **Trạng thái**: build sạch, boot sạch (15 packet, 0 ERROR, 0 FATAL), `mapCheck` cả năm qua.
- **Cần người dùng kiểm**:
  1. **Dung nham có ra màu cam nóng không** (không còn đỏ máu).
  2. **Mở/đóng tablet nhiều lần** — lần thứ hai trở đi phải **hiện ngay**, không chớp đen.
  3. **Zoom ra/vào** — phải thấy **mờ rồi nét**, không thấy đen rồi từng mảnh.
  4. **Kéo bản đồ ở zoom xa** có còn mượt không (giờ có thêm một lượt vẽ).
  5. Đi sang thế giới/dimension khác rồi mở tablet — đất thế giới cũ **không được** còn trên màn hình.

### 2026-08-16 — Dung nham thành ký hiệu, và lượt vá chỉ vá đúng chỗ thủng
- **Người dùng test đợt sửa trước**: zoom mờ-rồi-nét ✅ đúng ý; kéo ở zoom xa vẫn mượt ✅; đổi dimension
  đã sạch từ lâu ✅. Còn hai điểm.
- **Điểm 1 — dung nham lấy từ particle texture vẫn "dễ lẫn với công trình khác".** Người dùng đưa dải
  màu đề nghị: `#ff2500 #ff6600 #f2f217 #ea5c0f #e56520`.
  - **Đây là chỗ trung thực thua công dụng.** Trung bình `lava_still` ra một màu cam xỉn, nằm lẫn giữa
    các sắc nâu của đất, gỗ, mái nhà. Mà việc **duy nhất** một bản đồ chỉ huy hoả lực phải làm với hồ
    dung nham là khiến **không thể bỏ sót** nó.
  - **Đã làm**: bảng `HAZARDS` trong `BlockPalette`, tra **trước** cả model. Lava = **`#ff6600`** —
    giữa dải người dùng đưa: đủ nóng để đọc ra lửa, đủ xa đỏ để không lẫn với các mark tablet vẽ bằng đỏ.
  - **Giữ đúng MỘT mục, có chủ ý.** Mỗi mục thêm vào đó là một chỗ bản đồ **thôi nói thật** về thế
    giới; nó phải tự chứng minh mới được vào.
- **Điểm 2 — mở đi mở lại thấy bản đồ mờ nhẹ rồi mới nét.** Lỗi thiết kế của chính lượt phủ vừa thêm:
  nó là **được ăn cả ngã về không** — hễ lượt tinh thiếu **một** ô là cả tầm nhìn bị phủ thô.
  - **Vì sao nó bung ra đúng lúc mở lại**: `TabletScreen` dựng mới → `new MapPanel()` → **tâm bản đồ
    về lại vị trí người chơi**. Người chơi nhích một chút là dải ô lệch đi một hàng → vài ô rìa chưa
    dựng → **cả bản đồ bị phủ thô** dù 320 ô kia đang sắc nét.
  - **Đã sửa**: đổi mức thì vẫn phủ **cả tầm nhìn** (vì lúc đó **không có gì**); mọi trường hợp khác
    **vá đúng những ô khung hình trước không vẽ được**.
  - **Vá theo khung hình trước, có chủ ý**: trễ một khung hình thì mắt không thấy, đổi lại lượt thô
    **được chia budget dựng trước** — đúng thứ nó nên có, vì một sheet của nó thay cho mười sáu.
  - **Ô đã biết là rỗng KHÔNG phải lỗ thủng** — vá nó là bôi một vệt trung bình của hàng xóm lên vùng
    chưa ai khảo sát.
- **Phép tính vá là số học chính xác, nên harness kiểm được**: một sheet thô phủ **đúng một số nguyên**
  sheet tinh, cả hai đều xếp từ gốc thế giới, nên offset vào texture thô là **số nguyên texel**. `Bounds`
  giờ kiểm điều đó cho mọi mức, **kể cả toạ độ ô âm** (nửa thế giới có toạ độ âm, và tất cả dựa trên
  `floorDiv` chứ không phải phép chia cắt cụt). Lệch một texel **không crash** — nó trông như bản đồ
  bình thường đặt sai chỗ.
- **Trạng thái**: build sạch, boot sạch (15 packet, 0 ERROR, 0 FATAL), `mapCheck` cả năm qua (thêm
  54 hình chữ nhật vá được kiểm).
- **Cần người dùng kiểm**:
  1. **Dung nham có nổi bật hẳn lên không**, không lẫn với công trình.
  2. **Mở đi mở lại nhiều lần** — phần bản đồ đã nét phải **nét ngay**, không mờ đi rồi nét lại.
  3. **Zoom** vẫn mờ-rồi-nét như lượt trước (đường này không đổi).
  4. **Kéo bản đồ tới vùng mới** — rìa mới phải hiện mờ rồi nét, và **không** có mảnh nào lệch chỗ.

### 2026-08-16 — Dung nham vẫn tối: thủ phạm là `TERRAIN_DIM`, không phải mã màu
- **Người dùng**: *"cho màu sáng hơn là đẹp, một số hố lava vẫn hơi tối"*.
- **Đọc code trước khi đổi số, và may là đã đọc**: `TERRAIN_DIM = 0.62`. Kênh đỏ sáng nhất đi qua
  đường đó là `255 × 0.62 = 158`. **Không mã màu nào cho ra dung nham sáng** khi phép làm tối còn áp
  lên nó. Đổi hex là vô ích — đây là lỗi kiến trúc nhỏ, không phải lỗi thẩm mỹ.
- **Và "một số hố tối hơn" là một manh mối riêng**: đó là **relief**. Hố dung nham nằm trong lòng đất,
  nên độ nghiêng của đất quanh nó quyết định nó sáng hay tối. Hai hố khác nhau → hai độ sáng khác nhau.
- **Cả hai thứ đó tồn tại để làm ĐỊA HÌNH dễ đọc, và cả hai đều phá một KÝ HIỆU.** Nên: hazard vẽ ở
  **cường độ đầy đủ, không làm tối, không đổ bóng**.
  - **Trộn theo tỉ lệ, không phải bật/tắt**: một texel thô chứa nửa hồ dung nham ra nửa đường, nên
    mark không **bật ra** ở một nấc zoom.
  - **Cờ hazard đi suốt chuỗi mip**, đặt cạnh `depth` — và ghi chú ở `depth` chính là lý lẽ: *lý do để
    xử lý một texel khác đi phải sống sót qua phép trung bình cùng với màu mà nó áp lên*. Bỏ nó ở mức
    thô là **đúng bài học 18**, thứ đã hỏng một lần rồi với damping nước.
  - `dim(channel, relief)` đổi tên thành `light(channel, lit)`: giờ nó nhận **độ sáng cuối cùng**, chứ
    không phải một hệ số rồi tự nhân thêm hằng số bên trong. Một nơi quyết định độ sáng, không phải hai.
- **Harness CHẠM được đường này** (khác đường tra texture): bảng `HAZARDS` được tra **trước** mọi model
  nên nó trả lời như nhau ngoài game. PNG `build/mapcheck/` giờ cho thấy hồ dung nham **phẳng và sáng**
  ở mức tinh, và **nhoà dần vào hàng xóm** ở mức thô nhất — đúng thứ cần thấy.
- **Giữ nguyên `#ff6600`**: nó vốn không hề nhạt, chỉ đang bị nhân 0.62. Bỏ phép nhân đó là đã sáng
  hơn 62%. Nếu vẫn muốn sáng nữa thì đi về phía `#ea5c0f → #f2f217`, nhưng sẽ mất chất "lửa".
- **Trạng thái**: build sạch, boot sạch (15 packet, 0 ERROR, 0 FATAL), `mapCheck` cả năm qua.

### 2026-08-16 — Trả lời: "vì sao trước đây không gặp map load chậm / mờ rồi mới nét?"
Câu hỏi của người dùng, ghi lại vì câu trả lời sửa một hiểu nhầm dễ mắc.

- **Việc dựng lại từ đầu mỗi lần mở KHÔNG mới.** `TabletScreen.removed()` gọi `map.close()` đã có từ
  lâu, và `new TabletScreen(...)` chạy mỗi lần dùng item → `new MapPanel()` → `new TerrainImage()`.
  Trước phiên này, **mỗi lần mở tablet cũng dựng lại toàn bộ sheet**.
- **Cái mới là thứ lấp vào chỗ trống.** Trước: không có gì → **đen rồi hiện từng mảnh** — chính người
  dùng đã báo đúng câu đó ở lượt test đầu phiên này ("chớp đen rồi render từng phần"). Giờ: có lượt
  phủ thô → **mờ rồi nét**. **Cùng một độ chậm, khác cách trình bày.**
- **Nhưng có hai thứ tôi làm chậm đi thật, phải nói ra:**
  1. **Lượt phủ ăn chung budget dựng** (`MAX_BUILDS_PER_FRAME = 16`). Sheet thô lấy một phần, nên
     **sheet tinh nét chậm hơn trước**. Đổi lại có cái để nhìn ngay.
  2. **Sheet thô chiếm chỗ trong trần 384**, nên ít sheet tinh sống sót hơn khi kéo bản đồ.
- **Thứ tôi KHÔNG làm chậm** (đã kiểm bằng cách đọc code, không đoán): `groundColour` giờ là ba lần
  đọc mảng thay cho một phép tính `MapColor` — nếu có thì **nhanh hơn**. Việc tra texture chỉ chạy
  **một lần cho mỗi block id** rồi nhớ, nên nó không thể gây chậm kéo dài.
- **Việc có thể làm nếu vẫn thấy chậm** (chưa làm, đừng làm nếu chưa ai kêu): tách budget riêng cho
  lượt phủ thay vì dùng chung, và không cho sheet thô chen chỗ sheet tinh trong trần LRU.

### 2026-08-16 — "Load một lần, sau đó instant": ba thứ chặn, hai sửa được
- **Yêu cầu người dùng**: mở lần đầu load, các lần sau **instant, không mờ, không đợi**.
- **Giữ sheet qua các lần mở (đã làm) mới chỉ là một nửa.** `MapPanel` dựng lại cùng màn hình, nên
  `zoomIndex` và tâm bản đồ **về mặc định mỗi lần mở**. Sheet vẫn còn nguyên — **khung nhìn đã trượt
  khỏi chúng**, nên nó đòi một bộ ô khác và bộ đang giữ nằm không.
  - **Đã sửa**: trạng thái khung nhìn (`zoomIndex`, `centreX/Z`, `centreFollowsPlayer`) thành **static**,
    cùng lý lẽ với `TerrainImage.shared()`: **nó thuộc về phiên, không thuộc về màn hình**.
  - Và nó **đúng cho một khí tài** độc lập với chuyện tốc độ: bản đồ quên mất anh đang chĩa vào đâu là
    bản đồ phải ngắm lại mỗi lần liếc.
  - **Quên khi đổi thế giới** — toạ độ tâm ở thế giới này vô nghĩa ở thế giới khác. `MapPanel` tự theo
    dõi `generation`, thay vì để cache thò tay lên tầng màn hình.
- **Trần texture 384 với working set 360 là con số giả.** Tầm nhìn rộng nhất ở mức tinh cần **324** ô,
  cộng ~36 ô của lượt phủ. Nó **đọc ra như có chỗ dư mà không có chỗ dư nào**: kéo bản đồ một màn hình
  là đã đuổi mất đất sắp được dùng lại.
  - **Đã nâng lên 768** — gấp đôi working set. **Dưới mức đó thì nó là hàng đợi, không phải cache.**
  - Giá: ~50 MB trên card và ~50 MB cho ảnh giữ kèm. Nói ra để ai đọc sau biết mình đang mua gì.
- **Thứ KHÔNG sửa được, và phải nói thẳng**: đất **chưa từng vẽ** vẫn phải dựng. Nếu `centreFollowsPlayer`
  đang bật và người chơi đi bộ giữa hai lần mở, rìa mới là đất mới — nó sẽ hiện mờ rồi nét. Đó không
  phải cache trượt, đó là công việc thật lần đầu.
- **Trạng thái**: build sạch, boot sạch (15 packet, 0 ERROR, 0 FATAL), `mapCheck` cả năm qua
  (`evict` giờ chạy 1152 → 768).
- **Cần người dùng kiểm**:
  1. **Đứng yên, mở/đóng tablet liên tục** — từ lần thứ hai phải **instant hoàn toàn**, không mờ.
  2. **Zoom tới một nấc, đóng, mở lại** — phải mở ra **đúng nấc đó**, và instant.
  3. **Kéo bản đồ đi xa rồi tắt follow, đóng, mở lại** — phải quay lại **đúng chỗ đang xem**.
  4. **Đi bộ một đoạn rồi mở** — rìa mới hiện mờ rồi nét (đúng), phần giữa phải nét ngay (đúng).
  5. **Kéo đi một màn hình rồi kéo về** — phần vừa xem phải còn nguyên, không dựng lại.

### 2026-08-16 — Đo thay vì đoán: chi phí thật là DỰNG LẠI ô đã có
- **Người dùng hỏi "sao map nặng thế, có phải do harness không".** Harness: **không** — source set riêng,
  **0 class** trong jar mod, `runClient` không nạp. Nó chỉ chạy khi gõ `./gradlew mapCheck`.
- **Còn "vì sao nặng" thì tôi không biết, nên tôi đo.** Thêm `TerrainImage` một khối đếm: ms trong
  `draw`, số lần dựng/khung hình, số blit/khung hình, số sheet đang sống. Bật bằng
  `-Dartillerytablet.mapTrace=true`, và property đó **chỉ đặt ở dev client** trong `build.gradle`.
- **Số liệu người dùng gửi về, và nó chia đôi bài toán ngay:**
  | Tình huống | blit | dựng | ms/khung hình | fps |
  |---|---|---|---|---|
  | Đứng yên, đã nạp xong | 17–175 | **0.0** | **0.16–1.5** | ~100–125 |
  | Có dựng | 40–131 | 1.5–2.4 | **5.2–9.0** | 50–75 |
- **Hai chi phí, tách bạch rõ:**
  1. **Blit ≈ 8,4 µs/cái.** 170 blit = **1,4 ms/khung hình**, và đó là **sàn** khi mở bản đồ. Đúng
     bài học 12: mỗi `g.blit` là **một lệnh vẽ riêng**.
  2. **Một lần dựng sheet ≈ 3 ms.** `MAX_REBUILDS_PER_FRAME = 2` nghĩa là **6 ms/khung hình** chỉ để
     dựng lại. Đây là thủ phạm của mọi cú tụt fps.
- **Và đây là chỗ số liệu nói thứ mà đọc code không thấy**: lúc 15:28:51 có **2,4 dựng/khung hình ×
  108 khung hình = 259 lần dựng trong 2 giây**, mà số sheet sống **không nhúc nhích** (768/768). Tức
  **gần như toàn bộ là DỰNG LẠI ô đã vẽ rồi**, không phải ô mới.
- **Nguyên nhân**: `coverStamp` dùng **`System.identityHashCode` của đối tượng tile**. Lý lẽ ghi trong
  code là "cache thay instance mỗi lần khảo sát lại, không bao giờ sửa tại chỗ" — **đúng, và lạc đề**.
  Một ô khảo sát lại ra **y hệt từng byte** vẫn là một đối tượng mới, nên **cả 16 sheet phủ lên nó
  đều restamp và dựng lại**. Câu hỏi mà stamp phải trả lời là **đất có đổi không**, và cache **đã tính
  sẵn** `contentHash` lúc ô về.
  - **Đã sửa**: stamp theo `TerrainClientCache.hashAt()`. Đổi luôn `HASHES` sang `Long2IntOpenHashMap`
    vì câu hỏi này được hỏi **16 lần/sheet/khung hình** — khoá đóng hộp sẽ cấp phát một `Long` cho mỗi lần.
- **Hai thứ nhỏ trên cùng những khung hình đắt đó:**
  - `MAX_REBUILDS_PER_FRAME` **2 → 1**: phần dựng lại còn sót lại tốn 3 ms thay vì 6 ms.
  - `evictSurplus` thôi sort mảng `Long[]` bằng comparator **tra map trong từng phép so sánh** (vài
    nghìn lượt tra + một đối tượng mỗi sheet). Giờ gói (tuổi, chỉ số) vào một `long` và sort nguyên
    thuỷ. Nó **chỉ chạy khi có ô mới**, tức đúng những khung hình đang đắt sẵn.
- **Bài học, và nó thuộc nhóm "cách làm việc"**: *một lý lẽ đúng vẫn có thể trả lời sai câu hỏi.*
  Comment ở `coverStamp` nói một điều **đúng về cache** rồi dùng nó để biện minh cho một lựa chọn
  **sai về mục đích**. Không có dòng trace thì không cách nào nhìn ra — đọc code chỉ thấy một nhận
  định hợp lý.
- **Trạng thái**: build sạch, boot sạch (15 packet, 0 ERROR, 0 FATAL), `mapCheck` cả năm qua —
  `Evict` là cái kiểm đúng đoạn code vừa viết lại.
- **Cần người dùng kiểm**: chạy lại, gửi lại dòng trace. Kỳ vọng: **`builds/frame` về gần 0 khi đứng
  yên**, và các cú 5–9 ms biến mất. Nếu `ms/frame` vẫn cao mà `builds` đã bằng 0 thì chi phí còn lại
  là **blit**, và việc kế tiếp là gộp sheet (`TILES_PER_SHEET` 2 → 4) để cắt số lệnh vẽ xuống 1/4.

### 2026-08-16 — Chia đồng hồ làm ba, và thủ phạm là MỘT DÒNG: `Math.tanh`
- **Đợt đo trước chỉ ra "một lần dựng ≈ 3 ms" nhưng không nói được phải sửa gì.** Nên chia đồng hồ
  trong `build()` làm ba pha: `gather`, `shade`, `upload`. Một lần chạy là ra đáp án.
  | Pha | ms mỗi lần dựng |
  |---|---|
  | `upload` (đẩy 66 KB lên card) | **0.01–0.04** — gần như bằng không |
  | `gather` | 0.2–0.7 |
  | **`shade`** | **2.4–3.0** |
- **Trong `shade` chỉ có đúng một dòng không phải số học trên mảng**: `Math.tanh`. Nó là hàm siêu việt
  **double**, và `relief()` gọi `slope()` **hai lần mỗi texel** → **33.800 lần mỗi ô**. 2,5 ms ÷ 33.800
  ≈ 74 ns/lần — đúng bằng giá một `tanh`.
- **Đã đổi thành bảng tra 1024 bước** trên đoạn đường cong còn thay đổi (±4; ngoài đó `tanh` phẳng tới
  0,0007). Vẫn **đúng đường cong đó**, không phải một hàm thay thế.
- **Kiểm hai đường, không chỉ khẳng định suông:**
  1. Harness mới **`Relief`**: so bảng với `Math.tanh` trên cả dải, **sai số 0.00377 / dung sai
     0.00391** — và dung sai **suy ra từ bước bảng**, không phải chọn cho vừa đủ đậu.
  2. **Ảnh PNG dựng lại**: **7280/269824 điểm ảnh đổi (2,7%), mỗi điểm lệch đúng 1/255.**
- **Sàn còn lại đã đo được rõ**: khi không dựng gì, `ms/frame ≈ 7,7 µs × số blit`. Ở mức 0 với 155
  blit là **1,1–1,3 ms/khung hình**; với 12 blit là **0,10 ms**. Tuyến tính hoàn hảo theo số lệnh vẽ —
  lại là bài học 12.
- **Việc kế tiếp NẾU vẫn cần** (đừng làm nếu chưa ai kêu): gộp sheet `TILES_PER_SHEET` 2 → 4 để cắt
  số blit còn 1/4. Đánh đổi: một lần dựng đắt gấp 4, nên chỉ đáng làm khi giá dựng đã đủ rẻ — mà giờ
  thì nó vừa rẻ đi.
- **Bài học**: *đo một lần rồi vẫn có thể chưa đủ.* Lần đo đầu nói "dựng là thủ phạm" và tôi đã suýt
  đi tối ưu vòng lặp `gather`. Chia nhỏ thêm một tầng mới chỉ ra chỗ thật, và nó **không phải một
  vòng lặp** — nó là một lời gọi hàm trông vô hại. Đúng bài học 12 mở rộng: **`Math.tanh` trông như
  một phép toán, thực chất là hàng chục nano giây.**
- **Trạng thái**: build sạch, boot sạch (15 packet, 0 FATAL; một ERROR là loot table của SuperbWarfare
  trỏ tới Patchouli không cài — có sẵn từ trước, không liên quan), `mapCheck` cả sáu qua.
- **Cần người dùng kiểm**: chạy lại, gửi dòng trace. Kỳ vọng **`shade` tụt từ ~2,6 ms xuống dưới
  0,3 ms**, và `per build` từ ~3 ms xuống **dưới 1 ms**. Nhìn mắt: đổ bóng địa hình phải **y hệt** —
  nếu thấy khác là bảng sai, không phải mắt sai.

### 2026-08-16 — Khảo sát: vì sao JourneyMap load nhanh đến thế (đọc bytecode 6.0.1)
Câu hỏi của người dùng. Trả lời bằng `javap` trên jar thật đang nằm trong dev client, không bằng trí nhớ.

**Năm quyết định kiến trúc, và không cái nào là "thuật toán hay hơn":**

1. **Bản đồ được dựng LÚC CHƠI, không phải lúc mở.** `TaskController.performTasks` đẩy
   `RunnableTask` vào một **`ScheduledExecutorService`** — pool nền, không phải render thread.
   `MapPlayerTask` chạy liên tục, vẽ **từng chunk** quanh người chơi, có `getMaxRuntime()` và
   `getEffectiveBatchSize()` để tự ghìm nhịp. **Mở bản đồ chỉ là hiển thị thứ đã xong từ lâu.**
   → Ta dựng **khi mở tablet**, **trên render thread**. Đây là khác biệt lớn nhất.

2. **Đơn vị ảnh là REGION 512 block, không phải ô nhỏ.** `RegionCoord.chunkSqRt = 2^5 = 32` chunk,
   và `RegionImageHandler.getBlank512x512ImageFile()` xác nhận **512×512**. Một texture phủ 512 block;
   sheet của ta phủ **128**. → **16 lần diện tích mỗi lệnh vẽ.** Đo được của ta: 7,7 µs mỗi blit,
   155 blit = 1,2 ms. Với 512 thì chỉ còn ~10 blit.

3. **KHÔNG BAO GIỜ DỰNG LẠI — chỉ GHI ĐÈ.** `ImageHolder.partialImageUpdate(NativeImage, int, int)`
   chép một chunk 16×16 vào đúng chỗ của nó trong ảnh lớn, rồi ghi tên chunk đó vào
   `RegionTexture.dirtyChunks`. Không có gì bị dựng lại. → Ta **dựng lại cả sheet 130×130** mỗi khi
   bất kỳ ô nào dưới nó đổi.

4. **Upload cũng chỉ phần bẩn.** `RegionLodGenerator.updateAndUpload(NativeImage[], Set<ChunkPos>)`
   gọi `NativeImage.upload` **dạng 9 tham số có offset** — tức `glTexSubImage2D` đúng vùng 16×16 vừa
   đổi. → Ta upload **cả 130×130** mỗi lần.

5. **Ghi PNG xuống đĩa mỗi region.** `ImageHolder.writeToDisk`, `RegionImageCache.flushToDiskAsync`,
   `RegionImageHandler.readRegionImage`, `PngjHelper`. Đất đã vẽ phiên trước **nạp lại từ ảnh**, không
   phải suy ra lại. → Ta không lưu gì.

**Ngoài ra, một thứ ta ĐÃ làm giống họ**: bảng màu dựng sẵn theo block state (`ColorManager`,
`BlockStateColor`, `ColorPalette`, `ColoredSprite`, `BlockMD`) — đúng thứ `BlockPalette` của ta làm.
Và chuỗi LOD per-region cập nhật tăng dần, tương ứng `TerrainMips` của ta.

**Điều KHÔNG chuyển thẳng sang được, phải nói rõ**: JourneyMap vẽ **chunk mà client đã nạp**, nên nó
tăng dần một cách tự nhiên — chunk về từng cái một khi người chơi đi. Đất của ta về theo **ô 64×64 từ
server, theo cụm**, cho vùng người chơi chưa từng tới. Nên "tăng dần theo chunk" không áp thẳng được.

**Nhưng ý CỐT LÕI thì áp được, và nó lớn**: *khi một ô về, ghi 64×64 điểm ảnh của nó vào các sheet
đang phủ nó rồi upload đúng vùng đó — thay vì dựng lại cả sheet.* Vướng một chỗ đã biết: đổ bóng của
ta đọc hàng xóm xa `RELIEF_RUN = 3`, nên phải ghi kèm viền 3 texel và vá lại rìa của sheet bên cạnh.

**Ba việc rút ra, xếp theo giá trị (CHƯA LÀM, đừng làm nếu chưa đo lại sau lần sửa `tanh`):**
1. **Ghi đè thay vì dựng lại** khi một ô về. Bỏ gần hết chi phí dựng khỏi khung hình.
2. **`TILES_PER_SHEET` 2 → 4** (sheet 256 block): số blit còn 1/4. Chỉ đáng làm sau khi (1) xong, vì
   sheet to thì dựng lại càng đắt — mà (1) làm cho việc dựng lại gần như biến mất.
3. **Lưu ô xuống đĩa.** Đây là thứ khiến JourneyMap "instant" qua các lần khởi động lại.

### 2026-08-16 — Áp phương pháp JourneyMap vào ràng buộc của ta: 5 commit
Người dùng chốt "triển khai tất cả". Ghi lại theo từng phương pháp của họ, và **cái nào chuyển được,
cái nào không**.

| Phương pháp của JourneyMap | Ta làm được gì | Trạng thái |
|---|---|---|
| Ghi đè vùng vừa đổi, không dựng lại | Ô về → **vá đúng hình chữ nhật nó phủ** | ✅ |
| Upload chỉ vùng bẩn | `NativeImage.upload` **9 tham số có offset** | ✅ |
| Ảnh đơn vị to (region 512) | Sheet **4 tile = 256 block**, quad budget giảm nửa | ✅ |
| Lưu ảnh xuống đĩa mỗi region | **Một file mỗi ô**, theo thế giới + dimension | ✅ |
| Bảng màu dựng sẵn theo block state | `BlockPalette` (đã làm phiên trước) | ✅ |
| Dựng bản đồ **trên thread nền lúc chơi** | **Chỉ chuyển được phần I/O đĩa** | ⚠️ một phần |

**1. Vá thay vì dựng lại** — cache giờ ghi **ô nào** đổi và ở version nào, chứ không chỉ *có gì đó*
đổi. Một sheet hỏi "từ lúc tôi vẽ tới giờ có gì đổi" và nhận một trong ba câu trả lời: *không có gì
dưới tôi* (trường hợp thường gặp, **miễn phí**); *vài ô* → vá hợp của chúng và upload đúng vùng đó;
hoặc *đổi nhiều quá* → dựng lại cả sheet.
- **Bỏ hẳn** `coverStamp` (mức tinh) và bộ đếm giờ `COARSE_REBUILD_MS` (mức thô). Một con số version
  trả lời được ở mọi mức.
- **Hai dải phải đúng tuyệt đối và không cái nào lỗi ồn ào**: (a) ô ảnh hưởng tới texel nào — của
  chính nó, **nới về nam và đông** vì đổ bóng nhìn ngược lên bắc/tây, nên texel cách 3 vẫn đọc đất vừa
  đổi; (b) gather phải với tới đâu để những texel đó có cái mà đọc. Sai → **vệt 3 pixel ánh sáng cũ dọc
  mọi biên ô**, trông như đặc điểm địa hình.
- **Harness `Patch`** so từng texel giữa vá và dựng cả sheet, ở 4 mức, và **đầu độc mảng scratch trước**
  — vì trong game chúng giữ dữ liệu của sheet vẽ trước, nên vá đọc lố một ô sẽ lấy đất của sheet khác
  mà **vẫn trông hợp lý**. Kết quả: mức 0 vá **4489/66564 texel**, mức 4 vá **49**.

**2. Sheet to gấp đôi cạnh** — `TILES_PER_SHEET` 2→4, `MAX_QUADS_ACROSS` 16→8, trần 768→192.
- **Đính chính điều tôi nói khi đề xuất**: sheet to **một mình không giảm gì**, vì `levelFor` chọn mức
  để giữ số quad trong ngân sách, nên tầm nhìn lại đầy lên đúng số cũ ở mức mịn hơn. **Phải hạ ngân
  sách quad cùng lúc.** Số học ra vừa khít: **mọi nấc zoom giữ nguyên mức và stride**, tầm nhìn từ
  **289 sheet xuống 81**, bộ nhớ vẫn ~50 MB mỗi phía.
- Một lần dựng đắt gấp 4, nên `MAX_BUILDS_PER_FRAME` 16→4. **Chỉ kham được vì (1) đã xong**: giá một
  lần vá do **kích thước ô** quyết định, mà ô thì vẫn thế dù sheet to bao nhiêu.
- **Ảnh PNG không đổi một byte.**

**3. Lưu ô xuống đĩa** — `TerrainDisk`, một file mỗi ô, dưới `<gameDir>/artillerytablet/<thế giới>/<dimension>/`.
- **Định danh thế giới bắt buộc phải đúng**: hai server phát cùng toạ độ ô cho hai vùng đất khác hẳn.
- **Mọi thao tác file trên một thread nền**; kết quả đọc về qua hàng đợi, cache rút trên render thread
  — vì mọi thứ phía sau "một ô vừa về" thuộc về thread đó.
- **Ghi ra file tạm rồi `move` vào chỗ**: một ô ghi dở không phải là một ô, và nó sẽ lộ ra hàng tuần
  sau ở vùng đất không ai quay lại.
- File không đọc được (bản cũ, hoặc hỏng) → **xoá và hỏi lại server**. Đây chính là thứ `FORMAT_VERSION`
  sinh ra để làm.
- **Sweep hỏi đĩa TRƯỚC khi hỏi server.**
- **Harness `Store`**: 6 ô, có **toạ độ âm cả hai trục** — sharding chia cho luỹ thừa 2, chia cắt cụt
  thay vì `floorDiv` sẽ **nhét hai ô vào một file**, và lỗi đó không ném ra: nó chỉ khiến **không bao
  giờ tìm thấy gì**, và bản đồ trông y như lúc chưa có kho.

**4. Chuỗi mip giờ dựng LƯỜI.** Trước: dựng ngay lúc ô về — **32 ô một lượt, trên render thread, trong
packet handler** — mà các nấc zoom gần **không đọc một chút nào** (mức tinh vẽ thẳng từ ô). Giờ dựng
khi một tầm nhìn rộng thật sự hỏi, nên nó được **chia nhịp bởi chính ngân sách dựng**, và ai không
zoom ra thì không trả gì.

**Thứ CHƯA chuyển được, nói rõ**: JourneyMap vẽ bản đồ trên **thread nền lúc chơi**, nên lúc mở là đã
xong. Ta chưa: `gather`+`shade` vẫn trên render thread. Chuyển được về nguyên tắc, nhưng vướng thật:
mảng scratch dùng chung một bộ, `TerrainClientCache` không đồng bộ hoá, và `BlockPalette` lần tra đầu
tiên chạm vào model manager của Minecraft. **Đó là việc lớn tiếp theo nếu số đo còn đòi.**

### 2026-08-16 — Zoom vẫn mờ-rồi-nét: dựng sẵn mức kế tiếp. Và màn hình chờ ATLAS
- **Người dùng test đợt trước**: mở/đóng tab **hết phải đợi** ✅ (vẫn muốn cải thiện thêm). Còn zoom
  **8000→4000** và **4000→2000** vẫn mờ rồi nét dần.
- **Cả hai đều là ĐỔI MỨC** (2→1 và 1→0), và ở mức mới **chưa có sheet nào từng được dựng**. Bản đồ
  không hỏng — **công việc chỉ đơn giản là chưa được bắt đầu** cho tới đúng lúc cần nó.
- **Đã sửa: `warmNextZoom`** — khung hình nào không xài hết ngân sách dựng thì đổ phần thừa vào **mức
  dưới**. Đây chính là nguyên tắc của JourneyMap ("dựng lúc chơi, không phải lúc mở"), áp cho zoom.
  - **Dựng trên khoảng span mà nấc kế tiếp SẼ dùng** (nửa span hiện tại, cùng tâm), **không phải** span
    hiện tại: mức mịn hơn phủ 1/4 diện tích mỗi sheet, nên hâm nóng span hiện tại sẽ dựng **gấp 4** thứ
    mà không nấc zoom nào hỏi tới, và đuổi mất bản đồ để làm việc đó.
  - Sheet hâm nóng được đánh dấu `lastDrawn = drawClock - 1` → khi phải đuổi, nó **đi trước** thứ đang
    trên màn hình. Đúng thứ tự: ô chưa ai nhìn đáng giá thấp hơn ô đang được nhìn.
  - **Trần 192 → 256**: giờ phải chứa **ba** bộ chứ không phải một — mức đang hiện, lượt phủ thô dưới
    nó, và mức đang hâm nóng. Trần đặt vừa khít working set thì mỗi khung hình lại đuổi mất một bộ.
- **Màn hình chờ `BootSplash`** — người dùng đặt tên **ATLAS**, dưới là *Fire Direction Center*, phong
  cách ký tự kiểu terminal (logo thật sẽ thiết kế sau).
  - **Mọi ô đặt tay theo bước cố định.** Font của game **không đều nét** — dấu cách hẹp hơn ô đặc — nên
    vẽ cả dòng thì hình sẽ **trượt lệch từng hàng**, và trông như lỗi render chứ không như dùng sai
    công cụ. Bước được đo **một lần từ chính glyph ô đặc**. Đây là cách một terminal làm, và là lý do
    tranh ký tự giữ được hình.
  - **Chỉ hiện khi thật sự có thứ đang trên đường về** (`TerrainClientCache.isWaiting()`). Đất chưa
    từng được sinh ra thì không có gì để vẽ và **sẽ không bao giờ có** — đặt màn hình chờ lên đó là hứa
    một thứ không tới.
  - **Thanh chờ không phải phần trăm**, có chủ ý: bản đồ được báo về từng lô và **không có tổng nào để
    lấy làm mẫu số**. Một thanh khoe phần trăm là một con số bịa.
  - **Harness `Splash`** dựng đúng cách màn hình dựng rồi ghi ra PNG — tranh bằng ký tự là thứ **duy
    nhất trong dự án không thể phán đoán từ source**: thừa một dấu cách thì nhìn code không thấy, nhìn
    ảnh thấy ngay.
- **Trạng thái**: build sạch, boot sạch, `mapCheck` **9 kiểm**.
- **Cần người dùng kiểm**: (1) zoom 8000→4000→2000 lần thứ hai trở đi có **nét ngay** không (lần đầu
  vẫn phải dựng); (2) logo ATLAS có **thẳng hàng** không, hay bị trượt (nếu trượt là bước cố định sai);
  (3) logo có biến mất ngay khi đất hiện không; (4) ở vùng chưa khảo sát bao giờ, logo **không được**
  hiện mãi.

### 2026-08-16 — Màn hình chờ tắt quá sớm: hỏi nhầm câu hỏi
- **Người dùng**: zoom **đã ổn** ✅. Nhưng màn hình chờ *"biến mất quá nhanh trong khi bản đồ còn chưa
  load xong"*.
- **Nguyên nhân**: điều kiện cũ là `!terrainShown`, mà `terrainShown` thành `true` ngay khi **một** ô
  được vẽ — một trên tám mươi. **Hai câu hỏi khác nhau**: *"có gì trên màn hình không"* và *"bản đồ
  xong chưa"*, và tôi hỏi nhầm cái đầu.
- **Đã sửa — chờ CẢ HAI thứ im lặng**: (1) không còn ô nào đang được lấy về (`isWaiting()`), và
  (2) lượt vẽ tinh **không còn ô nào nó muốn mà không có** (`TerrainImage.isComplete()`, đọc từ danh
  sách `missing` vốn đã có sẵn cho lượt vá).
  - **Ô đã soi và thấy rỗng thoả mãn cả hai** — nên vùng đất chưa từng sinh ra **không giữ màn hình
    chờ mãi mãi**. Đây là tính chất "khoảng trống thành thật" của bản đồ, giữ nguyên.
- **Dùng chốt (latch), không phải kiểm tra liên tục.** Khởi động là chuyện xảy ra **một lần**; còn một
  khoảng trống ở rìa lúc kéo là chuyện thường. Màn hình chờ mà **quay lại đè lên bản đồ mỗi lần kéo
  nhanh hơn tốc độ lấy đất** thì còn tệ hơn cái tắt sớm. Chốt reset khi đổi thế giới.
- **Nền gần đục thay vì đục hẳn** (`0xEE`), vì giờ nó nằm **trên** một bản đồ đang hiện dần. Nhìn thấy
  lờ mờ đất đang về là phiên bản **thành thật** của màn hình chờ: nó cho thấy **thứ đang được chờ**,
  thay vì che đi rồi tự nhận là đang có gì đó diễn ra.
- **Thứ tự vẽ đã kiểm lại chứ không đoán**: blit vẽ ngay, `fill` được gom, chữ được gom vào render
  type riêng và **luôn vẽ sau mọi quad tô** — nên thứ tự ra đúng là **địa hình < nền chờ < chữ**, không
  cần `flush()` thêm.
- **Trạng thái**: build sạch, boot sạch, `mapCheck` 9 kiểm.
- **Cần người dùng kiểm**: (1) màn hình chờ giữ tới khi bản đồ **thật sự** xong; (2) đứng ở vùng chưa
  khảo sát bao giờ — nó **phải** tắt, không được treo; (3) kéo bản đồ tới vùng mới — nó **không được**
  quay lại; (4) sang thế giới khác rồi mở tab — nó **phải** hiện lại.

### 2026-08-16 — Trần thời gian cho màn hình chờ, màu navy, và giãn khối chữ
- **Người dùng**: hiểu là nó chờ tới khi map xong, *"nhưng có vài chỗ bị bug hoặc lỗi load lâu nên
  màn hình chờ cũng lâu theo"* → chỉ cho chờ tối đa **5–6 giây**. Cộng hai chỉnh về hình thức.
- **1. Trần 5 giây.** Nó chờ điều kiện *"bản đồ đã sẵn sàng"*, mà đó là **điều kiện có thể không bao
  giờ đạt** — đất server khảo sát chậm, một ô không bao giờ về, hoặc một lỗi chưa ai tìm ra. **Chờ một
  điều kiện không có cận trên** nghĩa là **một góc chậm của thế giới che mất tấm bản đồ đã vẽ xong chín
  phần mười và hoàn toàn dùng được.**
  - Đếm giờ **từ khung hình đầu tiên được vẽ**, không phải từ lúc đổi thế giới — người chơi có thể vào
    thế giới rất lâu trước khi sờ tới tablet.
- **2. Màu navy `#1B3F8F`.** Và lý do đổi **không phải chỉ vì màu**: màu accent **mang nghĩa** ở mọi
  chỗ khác trên thiết bị này — nó là màu của *thứ bạn có thể thao tác* — mà **dấu hiệu nhà sản xuất
  không phải một trong số đó**.
  - **Nâng khỏi navy thật (`#000080`) đủ để đọc được** trên nền gần đen. Ở đúng `#000080` nó là **một
    hình bạn tìm ra được, không phải một hình bạn nhìn thấy**.
- **3. Một đơn vị giãn cách duy nhất**, dùng giữa **mọi cặp** thay vì mỗi mối một số. Ba thứ trên màn
  hình đó là **một khối**, và một khối có các khe khác nhau thì đọc ra thành **ba thứ tình cờ đứng gần
  nhau**. Gấp đôi lên vì chữ đang dí sát vào logo.
- **Harness giờ vẽ bằng ĐÚNG hai màu đó**, đọc qua reflection. Chọn một màu rồi xem thử một màu khác
  là cách một bức ảnh trở về báo rằng lựa chọn ổn trong khi nó **chưa từng được thử**.
- **Trạng thái**: build sạch, boot sạch, `mapCheck` 9 kiểm.
- **Cần người dùng kiểm**: (1) chỗ load lâu giờ có tự bỏ qua sau 5 giây không; (2) navy có đọc được
  trên máy bạn không (nếu tối quá thì nâng `MARK_COLOUR`, một hằng số); (3) khoảng cách logo / chữ /
  thanh chờ đã thoáng chưa.

### 2026-08-16 — "Nham nhở và load chậm": thủ phạm là NHỊP XIN, không phải phần vẽ
- **Người dùng gửi ảnh**: bản đồ có mảng đen lớn nhỏ lởm chởm, đặc biệt lúc load; muốn **nhanh hơn**.
- **Đọc code trước khi sửa, và nó chỉ thẳng chỗ khác với chỗ tôi đang nhìn.** Phía server **đã bất
  đồng bộ và song song sẵn**: `ServerTerrainProvider` đọc 16 chunk mỗi ô trên thread đĩa, `ServerTileCache`
  nhớ lại. **Đất về nhanh đúng bằng tốc độ được hỏi.** Mà nó đang được hỏi ở nhịp:
  - `REQUEST_BATCH = 32` mỗi `REQUEST_INTERVAL_MS = 500` → **64 ô/giây**.
  - Tầm nhìn 2000m cần (2000/64)² ≈ **961 ô** → **15 giây**. Tầm 4000m → **hơn nửa phút**.
  - **Toàn bộ tối ưu phần vẽ của phiên này không chạm được vào con số đó.**
- **Đã sửa**: `REQUEST_BATCH` 32→**96**, `REQUEST_INTERVAL_MS` 500→**250** → **384 ô/giây, nhanh gấp 6**.
  Một ô ~2 KB trên dây, nên lúc đầy từ số không là **vài trăm KB/giây trong vài giây** — ít hơn phần
  game gửi chính các chunk, và **chỉ khi tablet đang mở nhìn vào đất chưa từng thấy**.
- **`SCAN_TILES` 48→64**: 48 ô = 3000 block, mà tầm nhìn rộng nhất còn vẽ đất là 4000. Đó là lý do bản
  đồ **dừng lại trước mép panel dù chờ bao lâu** — không phải chậm, mà là **sẽ không bao giờ tới**.
- **Và một lỗi ngân sách**: ô **rỗng vừa được cấp đất** đang bị tính vào ngân sách **dựng lại**
  (1/khung hình) thay vì ngân sách **dựng** (4/khung hình). Nhưng nó đang **vẽ ra từ chỗ không có gì**,
  đó là *dựng*, không phải *dựng lại*. Hậu quả đúng là thứ người dùng thấy: **rìa bản đồ hiện từng mảng
  một** trong khi đất của chúng đã nằm sẵn trong tay.
- **Logo ATLAS thay chữ FIRE DIRECTION ở góc.** Thiết bị làm gì thì đã viết khắp phần còn lại của màn
  hình; **góc là chỗ một vật nói nó LÀ gì.**
  - **Không nhét được bản đầy đủ**: thanh trạng thái cao 24 điểm ảnh giao diện, logo cao 40. Và **thu
    nhỏ là không được bàn** — mặt chữ là bitmap nướng cho đúng lưới này, cho chữ đi qua ma trận co giãn
    chính là lỗi dự án đã mất một tuần gỡ.
  - **Gấp lại thành 3 hàng bằng ô NỬA CHIỀU CAO** (`▀ ▄ █`): 3 hàng chữ mang được **6 hàng hình**, không
    đụng gì tới font.
  - **3 hàng ô đặc thì vừa nhưng KHÔNG đọc được**: chỉ có vạch trên, vạch giữa, vạch dưới thì **không gì
    phân biệt được S với E hay Z**.
  - Thanh cao **24→32**, và **kích thước lấy từ logo** chứ không phải cắt logo cho vừa thanh.
- **Harness vẽ cả hai logo vào một ảnh.** Không cái nào phán đoán được từ source, và bản gấp thì càng không.
- **Trạng thái**: build sạch, boot sạch, `mapCheck` 9 kiểm.
- **⚠️ Thứ harness KHÔNG kiểm được, phải nhờ mắt**: `▀` và `▄` trong font của game có nằm **đúng nửa
  trên / nửa dưới** ô chữ không. Nếu lệch, logo ở góc sẽ **đứt quãng theo hàng** — báo mình một tiếng
  là đổi lại.
- **Cần người dùng kiểm**: (1) bản đồ đầy **nhanh hơn hẳn** và bớt lởm chởm; (2) mép bản đồ giờ ra tới
  gần mép panel ở zoom rộng; (3) logo ở góc có liền mạch không; (4) `map trace` — `blits/frame` và
  `builds/frame` có tăng lên mức khó chịu không, vì đất giờ về nhanh gấp 6.

### 2026-08-16 — Nâng nhịp xin làm MỌI THỨ TỆ HƠN. Nguyên nhân thật: ngưỡng vá sai từ đầu
- **Người dùng**: *"load cực chậm và tệ hơn trước rất nhiều"*, và bỏ logo ATLAS ở header (to quá,
  trả lại `FIRE DIRECTION`).
- **Đã trả lại header** (`git revert`). Logo giữ ở màn hình chờ, nơi nó có chỗ để đọc.
- **Và lần nâng nhịp xin của tôi là SAI — nhưng nó làm lộ ra một lỗi lớn hơn, có từ trước.**
- **`MAX_PATCH_TILES = 24` đếm MỌI ô đã đổi TRÊN TOÀN THẾ GIỚI**, không phải số ô chạm vào sheet đang
  xét. Mà `REQUEST_BATCH` gốc đã là **32 > 24**.
  - ⇒ **Mỗi lượt đất về đều đẩy MỌI sheet vượt ngưỡng** ⇒ mọi sheet đi đường **dựng lại toàn bộ**
    ⇒ mà đường đó bị chặn ở **1 sheet/khung hình**.
  - ⇒ **Cả cơ chế vá — thứ cả phiên này xây — gần như CHƯA BAO GIỜ chạy**, và nó không chạy đúng lúc
    load, là lúc duy nhất nó có ý nghĩa.
  - Nâng batch lên 96 chỉ làm chuyện đó xảy ra **gấp ba lần**. Đó là lý do bản đồ **chậm đi** chứ
    không nhanh lên.
- **Đã sửa**: ngưỡng giờ là **phần trăm diện tích của CHÍNH sheet đó**, và cái đem ra so là **đúng hình
  chữ nhật hợp** mà lượt vá sẽ tô. *Mười sáu ô về ở xa là con số 0 đối với nó; hai ô về ngay dưới nó là
  hai.*
- **Nhịp xin trả về 32/500ms.** Trần ở đây **không phải tham vọng của ta** — nó là **một thread đĩa**
  trên server đọc 16 chunk mỗi ô (`IOWorker` của Minecraft là **đơn luồng**). Xin vượt trần chỉ **xếp
  hàng công việc**, không làm nó xong nhanh hơn, và còn tranh chỗ với chính việc nạp chunk của game.
- **`SCAN_TILES` giữ 64** — nó không đổi nhịp, chỉ mở rộng cửa sổ, và sửa lỗi thật là bản đồ **dừng
  trước mép panel dù chờ bao lâu**.
- **Thêm vào trace: `tiles/s arriving` và `outstanding`.** Đây là con số **duy nhất** phân biệt được
  *nghẽn ở phần vẽ* hay *nghẽn ở phần lấy đất*, và là con số **không ai có** lúc tôi đoán. Muốn nâng
  nhịp xin lần nữa thì phải nâng **dựa trên nó**.
- **Bài học, thuộc nhóm "cách làm việc"**: *một ngưỡng phải đo đúng thứ nó định bảo vệ.* Ngưỡng này
  định nói "vá chỗ này có đáng không" — một câu hỏi **về một sheet** — nhưng lại đo **một con số toàn
  cục**. Nó không sai ở giá trị; nó **sai ở đơn vị**, và loại sai đó **không lộ ra qua test**: mọi thứ
  vẫn chạy, chỉ là chạy đường chậm.
- **Trạng thái**: build sạch, boot sạch, `mapCheck` 9 kiểm.
- **Cần người dùng kiểm**: (1) load có nhanh hơn hẳn trước không — giờ đường vá mới thật sự chạy;
  (2) gửi lại dòng `map trace`, đặc biệt **`tiles/s` và `outstanding`**; (3) header đã về `FIRE DIRECTION`.

### 2026-08-16 — Trace trả lời dứt điểm: bản đồ KHÔNG chậm vẽ, nó ĐÓI ĐẤT
- **Tôi tự đọc `run/logs/latest.log`** (người dùng nhắc đúng — tôi ở ngay trong thư mục dự án).
- **Phần vẽ đã xong hẳn**: `0.09–0.5 ms/frame`, **11–43 blit** (trước 155), `builds/frame ≈ 0`,
  `per build 0.1–0.9 ms` (trước 3). Không còn gì để tối ưu ở đó.
- **Và dòng nói lên tất cả**, lặp đi lặp lại: **`0 tiles/s arriving, 64 outstanding`**.
  - Trung bình thật: **~4–10 ô/giây**. Client xin 64 ô rồi **ngồi đợi**.
  - ⇒ Nghẽn **100% ở phía cung cấp đất**. Cả hai lần tôi chỉnh nhịp xin đều **vô nghĩa** — xin nhanh
    hơn khi nguồn chỉ trả 4 ô/giây thì chỉ dài thêm hàng đợi.
- **Vì sao JourneyMap không có vấn đề đó — và không phải vì pipeline nhanh hơn.** Bytecode:
  `ChunkMD` là **một lớp bọc quanh `LevelChunk`, giữ bằng `WeakReference`**, và `BaseMapTask` lấy nó
  qua `DataCache.getChunkMD`. **Nó vẽ chunk mà CLIENT ĐÃ CÓ SẴN, và không hỏi ai cả.**
  - Server stream chunk về client như một phần của chơi bình thường ⇒ đất quanh người chơi **đã nằm
    trong bộ nhớ**. Đọc nó tốn **không đĩa, không mạng, không thời gian server**, và có **ngay khoảnh
    khắc người chơi nhìn thấy được**.
  - Đó chính xác là *"đi vào vùng chưa biết mà map họ vẫn vẽ nhanh"*: họ **không khảo sát gì cả**, họ
    đọc thứ game vừa đưa cho.
- **Đã áp**: `ClientTerrainSampler` — client tự dựng ô từ chunk của chính nó, **trước** khi hỏi kho
  đĩa và **rất lâu trước** khi hỏi server. Dùng **đúng hàm `sampleLive`** mà server dùng (đã tổng quát
  hoá từ `ServerLevel` sang `Level`), nên **một cột lấy ở client không phân biệt được với một cột lấy
  ở server** — nếu không, viền vùng nạp chunk sẽ hiện thành một vệt trên bản đồ.
- **Nó KHÔNG thay thế được khảo sát server, và không thể**: client chỉ giữ vài trăm block trong tầm
  nhìn, còn thiết bị này làm việc trên hàng kilômét đất không ai đứng lên. Thứ nó làm là khiến **phần
  bạn đang đứng hiện ra tức thì** và **rút hẳn phần đó khỏi hàng đợi của server** — mà đó là phần được
  nhìn nhiều nhất và ít ai chịu chờ nhất.
- **Toàn bộ hoặc không, cho mỗi ô**: một ô lấy được nửa là **một câu trả lời có lỗ**, mà cache coi lỗ
  là đất cần hỏi lại ⇒ vẫn tốn một request, lại còn vẽ ra một khoảng thủng ở giữa.
- **Trace thêm `(N of them local)`** để thấy tỉ lệ đất tự dựng.
- **Việc còn lại, và nó là trần thật**: server trả ~4–10 ô/giây vì `ServerTerrainProvider` gọi
  `chunkMap.read` — **`IOWorker` của Minecraft là ĐƠN LUỒNG** và còn phải tranh với chính việc nạp/lưu
  chunk của game. Muốn vượt trần đó phải **tự đọc region file bằng thread pool riêng**, đúng thứ
  `journeymap/client/io/nbt/RegionLoader` + `JMChunkLoader` làm. **Chưa làm** — nó là tự dựng lại tầng
  lưu trữ chunk của Minecraft, và luật của dự án là *đừng tái hiện điều kiện tiên quyết của người khác*.
- **Trạng thái**: build sạch, boot sạch, `mapCheck` 9 kiểm.
- **Cần người dùng kiểm**: (1) đất **quanh chỗ đứng** có hiện gần như tức thì không; (2) `map trace` —
  `tiles/s` và **`of them local`**; (3) có thấy **vệt nối** ở rìa tầm nạp chunk không (chỗ đất client
  dựng gặp đất server dựng) — nếu có thì hai đường lấy mẫu đã lệch nhau.

### 2026-08-16 — Nhanh hơn nữa: hàng đợi đọc riêng cho khảo sát
- **Trace sau lượt trước**: phần vẽ `0.2–0.6 ms/frame`, `builds ≈ 0`, và lúc đất về: **`32 tiles/s
  arriving (0 of them local)`**. Đất về nhanh hơn trước (32 so với 4–10) nhưng vẫn là **trần duy nhất
  còn lại**.
- **`0 of them local`** — bộ lấy mẫu client không dựng được ô nào. Nguyên nhân: nó bị hỏi về **mọi ô
  chưa có lời đáp trong cửa sổ 4 km**, mà client chỉ giữ vài trăm block. Nên nó tra **16 chunk × 4096
  ô, hai lần mỗi giây, để nhận "không" bốn nghìn lần**. Đã chặn bằng một phép tính khoảng cách rẻ tiền
  trước khi hỏi câu đắt tiền.
- **Trần thật, và cách vượt qua nó**: `chunkMap.read` xếp việc lên **worker của chính level** — **một
  luồng**, và **cùng luồng game dùng để lưu chunk khi người chơi di chuyển**. Khảo sát chờ game, game
  chờ khảo sát.
  - **Cách sửa không phải một trình đọc nhanh hơn, mà là MỘT HÀNG ĐỢI THỨ HAI.** Mở đúng thư mục
    `region` đó qua **chính lớp `ChunkStorage` của game** — nó **tự mang theo worker thread riêng**.
  - **Không tự phân tích region file, không tự dựng lại gì.** Đó đúng là sai lầm mà dự án này ghi đi
    ghi lại. Tất cả API dùng đều công khai: `getWorldPath`, `DimensionType.getStorageFolder`,
    `new ChunkStorage(path, fixer, false)`, `.read(pos)`.
  - **Chỉ ĐỌC, và điều đó giới hạn rủi ro.** Hai trình đọc trên cùng một thư mục về lý thuyết có thể
    bắt gặp file đang ghi dở — nhưng Minecraft ghi chunk vào **sector mới rồi mới cập nhật header**,
    nên người đọc thấy **bản cũ hoặc bản mới**. Tệ hơn nữa thì là một ô giải mã hỏng, vốn đã được bắt
    theo từng chunk và báo là đất chưa khảo sát. **Không dòng nào ở đây ghi được, nên không dòng nào
    làm hỏng được thế giới.**
  - **Mở không được thì rơi về worker của level.** Bản đồ đầy với tốc độ cũ vẫn tốt hơn nhiều so với
    bản đồ không đầy.
  - Đóng ở `ServerStoppingEvent` chứ không phải `Stopped`: một thế giới sắp được sao chép hay tải lên
    thì không nên còn file handle của ta giữ region của nó.
- **Trạng thái**: build sạch, boot sạch, `mapCheck` 9 kiểm.
- **Cần người dùng kiểm**: (1) `tiles/s arriving` có tăng rõ rệt không — **đây là con số quyết định**;
  (2) `of them local` giờ có khác 0 khi đứng ở vùng mới không; (3) game có bị khựng khi lưu chunk nữa
  không (đáng lẽ **bớt**, vì hai bên thôi tranh một luồng); (4) tìm dòng log `have their own queue at`
  để xác nhận hàng đợi thứ hai đã mở được.

### 2026-08-16 — Hàng đợi riêng có tác dụng, và nghẽn ĐỔI SANG PHÍA CLIENT
- **Đọc lại log**, và nó xác nhận cả hai điều:
  - `Tablet terrain reads for minecraft:overworld have their own queue at ...` → **hàng đợi thứ hai
    mở được**.
  - Trong 16 mẫu có việc đang chờ: **đỉnh 118 ô/giây, trung bình 50** (trước khi tách hàng đợi:
    **4–10**). Tức tách hàng đợi là **thứ đáng giá nhất** của cả vấn đề tốc độ.
  - **`outstanding` đứng đúng ở 64 trong 7/16 mẫu** — mà 64 = `REQUEST_BATCH 32 × 2 lượt đang bay.
    **Đó là trần của CLIENT, không phải của server.** Nghẽn đã đổi bên.
- **Đã nâng theo số**: `REQUEST_BATCH` 32→**64**, `REQUEST_INTERVAL_MS` 500→**250**.
  - Đây là **lần chỉnh thứ ba** của con số này và là **lần đầu tiên dựa trên đo đạc**. Lần 1 nâng khi
    server chỉ trả 4 ô/giây ⇒ chỉ kéo dài hàng đợi. Lần 2 chạm phải ngưỡng vá sai đơn vị. **Cả hai
    nguyên nhân đều đã hết và đều đã ghi lại.**
- **Thứ khiến lần này an toàn là một CỬA SỔ, không phải một nhịp**: `MAX_IN_FLIGHT = 192`.
  - Xin nhanh hơn là để **server không bao giờ rảnh**. Nhưng hàng đợi sâu hơn khả năng làm của nó thì
    **không phải thông lượng, mà là độ trễ** — và quá vài giây tồn đọng thì **bộ đếm retry bắt đầu xin
    lại những ô chưa hề mất**, tức **cùng một lần đọc đĩa làm hai lần**.
  - Mỗi lượt lấy **cái nhỏ hơn** giữa batch của nó và phần còn trống của cửa sổ.
- **Trạng thái**: build sạch, boot sạch, `mapCheck` 9 kiểm.
- **Cần người dùng kiểm**: (1) `tiles/s` có vượt hẳn 118 không; (2) `outstanding` có còn dính ở một
  con số cố định không — nếu **dính ở 192** thì server lại là trần và cách sửa tiếp là phía server;
  nếu nó **dao động dưới 192** thì hai bên đã cân, và trần tiếp theo là đĩa vật lý.

### 2026-08-16 — Ô đen là do CHÍNH hàng đợi riêng: nó không thấy `pendingWrites`
- **Người dùng**: load nhanh rồi, *"nhưng bị nhiều ô đen quá, và ô đen cũng xuất hiện nhiều hơn"*.
- **Đây là lỗi tôi vừa gieo ở lượt trước, và bytecode nói chính xác vì sao.** `IOWorker` giữ một map
  **`pendingWrites`** — những chunk game đã giao cho nó nhưng **chưa kịp ghi xuống đĩa** — và đường
  đọc của nó **tra map đó TRƯỚC** khi chạm file.
  - **Một trình đọc riêng không nhìn thấy map đó.** Nên một chunk vừa được sinh ra đọc ra **vắng mặt**,
    mà vắng mặt thì hiểu là **đất chưa khảo sát**, và đất chưa khảo sát vẽ ra **màu đen**.
  - Và đó chính là **phần lớn đất quanh một người chơi đang di chuyển** — đúng phần đang được hỏi tới.
  - **Vì sao nó tệ hơn khi load nhanh lên**: càng nhiều ô bay cùng lúc thì càng nhiều ô rơi trúng cửa
    sổ "đã sinh ra nhưng chưa ghi".
- **Đã sửa**: hàng đợi riêng vẫn là **đường nhanh**, còn worker của level là **nguồn phán quyết** —
  hỏi nó mỗi khi đường nhanh trả về rỗng.
  - Giá phải trả: **một lần đọc thứ hai** cho vùng đất **thật sự chưa bao giờ được sinh ra**, mà lần
    đó chỉ là tra header trong một file đang mở sẵn. Rẻ, so với việc **vẽ lỗ thủng lên đất mà game
    biết rõ mười mươi**.
- **Bài học**: *một đường tắt phải trả lời được ĐÚNG câu hỏi mà đường chính trả lời.* Tôi đã kiểm rằng
  `ChunkStorage` đọc được region file — đúng — nhưng **không kiểm xem `chunkMap.read` có làm gì HƠN
  thế không**. Nó có: nó trả lời từ bộ đệm ghi. Đây đúng là bài học số 1 của dự án ở dạng khác — *đừng
  tái hiện điều kiện tiên quyết của người khác; hãy quan sát trạng thái sau khi gọi*.
- **Trạng thái**: build sạch, boot sạch.
- **Trả lời câu hỏi "có cách vẽ bản đồ nào khác, hay client vẽ nhưng xin data từ map ảo của server"**:
  - **Kiến trúc hiện tại ĐÃ là như vậy** — server khảo sát, client vẽ. Không đổi được chỗ đó.
  - Thứ *"map ảo của server"* gợi ra và **chưa có**: **kho ô phía SERVER, lưu xuống đĩa**. Hiện
    `ServerTileCache` chỉ nhớ trong RAM 2 giây. Nếu server lưu ô đã khảo sát:
    - Mỗi ô chỉ đọc chunk **một lần trong đời thế giới**, thay vì mỗi lần có người hỏi.
    - **Dùng chung cho mọi người chơi**, và sống qua việc client xoá kho.
    - Tận dụng đúng `TerrainTile` + byte phiên bản đã có, và đúng khuôn `TerrainDisk` đã viết.
  - **Đó là bước tiếp theo đáng làm nhất**, và nó tấn công đúng thứ còn lại: chi phí đọc chunk.

### 2026-08-16 — Kho ô phía SERVER: một chunk chỉ đọc MỘT LẦN trong đời thế giới
- **Con số dẫn tới đây**: vẽ hết một tầm nhìn tốn **~0,04 giây**; lấy đủ đất cho tầm nhìn đó tốn
  **10–20 giây**. **Chênh ~400 lần.** Mỗi ô là 16 chunk đọc từ đĩa và giải nén, và trước đây việc đó
  làm lại **mỗi lần có người hỏi** — `ServerTileCache` chỉ nhớ **2 giây trong RAM** rồi vứt.
- **Đã làm**: `ServerTileStore` — ô đã khảo sát được **ghi xuống đĩa, nằm trong thư mục thế giới**, nên
  nó **đi theo khi thế giới được sao chép** và **mất khi thế giới bị xoá**. Nó là bộ nhớ đệm của thứ
  thế giới đã nói, nên mất nó chỉ tốn thời gian chứ không mất gì.
- **Tách `TileFiles` làm chủ sở hữu DUY NHẤT của bố cục file.** Giờ có **hai kho** (client giữ đất
  được báo, server giữ đất đã khảo sát). Viết hai lần thì chúng **sẽ trôi khỏi nhau**, và kiểu hỏng đó
  hiện ra dưới dạng *một file bên này ghi mà bên kia không đọc được*, phát hiện sau hàng tuần trên đất
  không ai quay lại.
- **Chỉ lưu ô ĐẦY ĐỦ.** Ô có lỗ nghĩa là chunk chưa được sinh ra lúc nhìn, mà đi bộ tới đó là nó sinh
  ra — ghi lại thì **đóng băng một khoảng trắng vào hồ sơ của thế giới**. Ô có lỗ khảo sát lại mỗi
  lần, và điều đó **rẻ**: đọc một chunk không tồn tại là **nhìn header của file**, không phải đọc file.
- **Ô đã lưu chỉ được dùng khi KHÔNG chunk nào của nó đang nạp.** Đất có người ở gần thì **khảo sát
  trực tiếp** — vừa là chỗ người ta xây dựng, vừa vốn dĩ là đường nhanh (chunk đã ở trong RAM).
  Đất không ai đứng thì **không đổi**, nên nhớ nó **không phải nói dối** — và client vốn **đã không bao
  giờ hỏi lại ô đầy đủ**, nên đây chỉ là việc server **đồng ý với một quyết định bản đồ đã chốt từ lâu**.
- **Harness `Store` mở rộng**: kiểm **25.921 tên file quanh gốc toạ độ, không hai ô nào trùng tên**.
  Đây là kiểu hỏng câm nhất còn lại — hai ô rơi vào một tên thì **đất thế giới này được phục vụ cho
  thế giới khác**, và sharding chia cho luỹ thừa 2 nên **chia cắt cụt thay vì `floorDiv`** sẽ đặt một
  ô và ảnh gương của nó vào cùng thư mục.
- **Trạng thái**: build sạch, boot sạch, `mapCheck` 10 kiểm.
- **Cần người dùng kiểm**:
  1. **Lần đầu** vào một vùng: vẫn chậm như cũ (phải khảo sát thật) — đây là đúng.
  2. **Lần thứ hai trở đi**, kể cả **sau khi khởi động lại game**: phải **nhanh hơn hẳn**.
  3. Xây một công trình rồi mở tablet **đứng ngay đó**: công trình **phải hiện** (chunk đang nạp ⇒ khảo
     sát trực tiếp). Đi thật xa rồi nhìn lại chỗ đó: **có thể là ảnh cũ** — đó là đánh đổi đã chốt.
  4. Thư mục `saves/<thế giới>/artillerytablet/` có sinh file `.tile` không.

### 2026-08-16 — Hai lỗi do chính hai cái kho: bản đồ ĐÓNG BĂNG, và một hàng đợi chắn trước mọi khảo sát
- **Người dùng**: vào lại vẫn hơi chậm và **nhiều ô đen đợi load hơn bình thường**; **xây công trình
  không hiện trên map**. Tự kiểm code, ra hai nguyên nhân riêng biệt, **cả hai đều do tôi**.

**Lỗi 1 — công trình không hiện: kho của CLIENT tự cho mình quyền quyết định.**
- `accept()` đánh dấu `COMPLETE` cho **mọi** ô đầy đủ, **kể cả ô vừa đọc từ kho riêng của client**.
- Mà `COMPLETE` nghĩa là **không bao giờ hỏi lại nữa**. ⇒ Kho trả về một ô **từ trước khi công trình
  được xây**, ô đó đầy đủ, nên client **khoá luôn** và bản đồ **đóng băng ở thứ nó được kể một lần**.
- **Và nó vĩnh viễn, qua cả các lần khởi động lại** — đó là lý do lỗi này chỉ lộ ra bây giờ, sau khi
  có kho đĩa client. Trước đó mỗi phiên đều hỏi lại server nên đất tự tươi lại.
- **Phân biệt đúng bản chất**: *ô từ server là **lời của thế giới** và là chung cuộc; ô từ kho của
  chính ta chỉ là **ký ức về điều thế giới nói lần trước**.*
- **Đã sửa**: ô đọc từ kho **chưa được xác nhận** cho tới khi server đồng ý. Lượt refresh mang theo
  **mã băm** ta đang giữ ⇒ không đổi thì server trả lời **vài byte** và `confirm()` chốt nó lại; có
  đổi thì đất mới về. **Một vòng hỏi rẻ mỗi ô mỗi phiên**, đổi lấy một bản đồ **biết thế giới đã thay đổi**.

**Lỗi 2 — kho của SERVER làm lần đầu tới một vùng CHẬM ĐI.**
- `ServerTileStore.read` đẩy **mọi** yêu cầu lên **một luồng duy nhất, ưu tiên thấp** — kể cả khi kho
  **rỗng**. ⇒ Một đường ống **200 khảo sát song song** biến thành **200 lượt xếp hàng một**.
- **Đúng nghịch lý**: thứ sinh ra để tăng tốc lại **chắn trước** đường nhanh.
- **Đã sửa**: kho giữ **chỉ mục trong bộ nhớ** những ô nó có, dựng **một lần** bằng cách liệt kê thư
  mục (chỉ đọc **tên file**, không đọc ô nào). ⇒ **Trượt chỉ mục là miễn phí**, trả lời ngay trên
  thread của người gọi. Trong lúc chỉ mục chưa dựng xong, mọi ô coi như không có ⇒ **đúng hành vi
  trước khi có kho**.
- **Harness kiểm tên file đọc ngược lại ra đúng ô** (81 trường hợp, cả toạ độ âm). Nếu phần *phân
  tích tên* lệch với phần *đặt tên* dù chỉ một dấu trừ, **chỉ mục ra rỗng, mọi lượt tra đều trượt, và
  kho im lặng không làm gì cả** — bản đồ chỉ đơn giản chậm lại, với **một lý do không thông báo lỗi
  nào nhắc tới**.
- **Bài học**: *một bộ nhớ đệm phải làm cho việc TRƯỢT rẻ hơn việc không có nó.* Nếu đường trượt đắt
  hơn đường gốc thì cache **không phải tối ưu, mà là một tầng chắn**.
- **Trạng thái**: build sạch, boot sạch, `mapCheck` 10 kiểm.
- **Cần người dùng kiểm**: (1) công trình vừa xây **có hiện** không (đứng gần, và cả sau khi đi xa rồi
  quay lại); (2) lần đầu tới vùng mới **không còn chậm hơn trước**; (3) lần thứ hai và sau khi khởi
  động lại **nhanh hơn hẳn**; (4) ô đen có ít đi không.

### 2026-08-16 — Khảo sát: có mod nào làm server-side mapping không?
Người dùng hỏi. Tra web (8/2026), ghi lại vì kết luận **đổi cách nhìn về việc ta đang làm**.

**Không có mod nào làm ĐÚNG thứ ta làm** — server khảo sát đất **chưa ai tới** rồi stream vào một bản
đồ **trong game**. Ba thứ gần nhất, và cả ba đều dừng trước vạch đó:

| Mod | Server làm gì | Vẽ được đất chưa ai tới? |
|---|---|---|
| **Map-Sync** (CivPlatform) | **Bị động** — chỉ nhận chunk từ client rồi phát lại cho bạn bè | ❌ *"it cannot map terrain nobody has explored"* |
| **JourneyMap** | Chưa có (tác giả xác nhận 2026-08-15: mới là hạ tầng chuẩn bị) | ❌ |
| **Xaero's** | Cài server chỉ để **định danh thế giới / waypoint** | ❌ |

**Nhưng server-side mapping CÓ tồn tại — ở dạng bản đồ WEB**: **Dynmap**, **BlueMap**, **squaremap**,
**Pl3xMap**. Chúng **đọc mọi chunk trên server và dựng tile thật**, tức là **đúng nửa đắt của bài toán
ta đang giải**. Cả Dynmap và BlueMap đều có bản Forge/NeoForge/Fabric. Khác biệt duy nhất: chúng xuất
ra **trình duyệt**, không phải HUD trong game.

**Và đây là điều đáng giá nhất của cả cuộc tra**: chúng **xác nhận chi phí của ta là cố hữu, không
phải do ta viết dở.**
> *"A full render reads every chunk of every world... Kick off a render while players are online and
> TPS drops and chunks load more slowly."* — và Dynmap được xếp là **nặng nhất** trong nhóm.

Tức là **cách làm chuẩn của ngành cho việc này KHÔNG phải làm theo yêu cầu**, mà là **một lượt dựng
sẵn chạy nền** (`fullrender`), có **số luồng cấu hình được theo số nhân CPU**, thường chạy lúc server
vắng. Ta đang cố làm cùng khối lượng công việc đó **ngay lúc người chơi mở tablet**.

**Suy ra hai việc, chưa làm:**
1. **Hâm nóng kho ô phía server ở chế độ nền** — đúng ý tưởng `fullrender` của Dynmap, nhưng nhỏ và
   liên tục: khi server rảnh, lặng lẽ khảo sát các ô quanh spawn và quanh người chơi rồi ghi vào
   `ServerTileStore`. Mở tablet ra thì **kho đã ấm sẵn**. Đây là thứ **đánh trúng gốc** nhất còn lại.
2. **Nhiều luồng đọc thay vì một**, đặt theo số nhân CPU — đúng cách Dynmap chỉnh. Rẻ, nhưng theo
   trace thì trần hiện tại đang nằm ở phía client chứ chưa phải ở đây, nên **đo trước đã**.

**Kết luận về kiến trúc**: ta **không có mod nào để dựa vào**, và cũng **không nên tìm** — thứ ta làm
là chỗ trống thật giữa "bản đồ web của server" và "minimap của client". Tác giả JourneyMap đã kê đúng
kiến trúc này khi được hỏi (xem 2026-08-15).

### 2026-08-16 — Khảo sát TRƯỚC khi ai hỏi, và công trình xây mới hiện ngay
Hai việc, một từ kết quả khảo sát mod, một từ báo lỗi của người dùng.

**1. Công trình mới không hiện — lỗ hổng cuối của quy tắc "ô đầy đủ không hỏi lại".**
- **Manh mối người dùng đưa rất chuẩn**: *"công trình đã build có hiện khi khởi động lại game, nhưng
  xây mới thì không hiện"*. **Hai nửa của cùng một quy tắc, nhìn từ hai phía.**
  - Khởi động lại ⇒ ô về từ kho client ⇒ **chưa được xác nhận** ⇒ hỏi lại một lần ⇒ tường hiện ra.
  - Trong phiên ⇒ ô đã `COMPLETE` ⇒ **không bao giờ hỏi lại** ⇒ đóng băng.
- Quy tắc đó **đúng cho đất không ai đứng** (nó là thứ chặn việc hỏi server mãi mãi), và **sai cho đất
  dưới chân bạn**.
- **Đã sửa, và không cần hỏi ai cả**: trong tầm nạp chunk, **client tự giữ chunk**, nên nó **tự nhìn
  lại** — không server, không đĩa, không request. Mỗi **2 giây**, vài ô mỗi lượt.
  - **Ô không đổi bị bỏ ngay tại chỗ lấy mẫu** (so `contentHash`), nên đứng yên chỉ tốn đúng phần lấy
    mẫu, không sinh version, không vá sheet.

**2. Hâm nóng nền — bài học từ Dynmap, và nó không phải về cách đọc chunk.**
- Các plugin bản đồ web đã giải **đúng nửa đắt** của bài toán này. Thứ đáng học **không phải họ đọc
  chunk thế nào, mà là KHI NÀO**: một lượt dựng theo lô, lúc server vắng, số luồng cấu hình được — và
  chính tài liệu của họ cảnh báo đừng chạy khi có người chơi online.
- **Tablet này đang làm cùng khối lượng công việc đó, cùng cách, đúng lúc người chơi mở màn hình và
  đứng đó chờ.** Không có mức tinh chỉnh nào thắng được một cuộc đua với cái đĩa. **Cách thắng là đã
  xong trước khi cuộc đua bắt đầu.**
- **`ServerTerrainWarmer`**: 4 ô mỗi 5 tick (~16 ô/giây), quanh **từng người chơi**, xoáy ốc **gần
  trước**, bỏ qua ô kho đã có (**miễn phí**, nhờ chỉ mục).
- **Luôn nhường game**: dừng khi `getAverageTickTime() > 35 ms` **và** khi `event.haveTime()` nói
  không còn thời gian. Hai câu hỏi khác nhau — một cái bắt server đang yếu nói chung, một cái bắt
  đúng tick đang yếu. *Một tác vụ nền mà phải để ý thấy là một tác vụ nền đã thất bại.*
- Con trỏ xoáy ốc **theo từng người chơi**, nên hai người ở hai nơi đều được hâm nóng chỗ của mình
  thay vì thay phiên.
- **Trạng thái**: build sạch, boot sạch (0 ERROR, 0 FATAL), `mapCheck` 10 kiểm.
- **Cần người dùng kiểm**:
  1. **Xây một khối ngay cạnh mình, tablet đang mở** — phải hiện trong **~2 giây**.
  2. Đứng yên vài phút rồi mở tablet — **đất quanh đó phải hiện tức thì** (đã hâm nóng sẵn).
  3. **TPS/độ mượt của game có tệ đi không** — nếu có, hạ `PER_PASS` hoặc nâng `EVERY_TICKS`.
  4. Thư mục `saves/<thế giới>/artillerytablet/` có **tự lớn dần** khi đứng yên không.

### 2026-08-16 — "Đáng lẽ nên setup trên VPS": đánh giá thẳng, và lỗ hổng THẬT
- **Người dùng nghi nền tảng dựng sai từ đầu** (dev trên máy client thay vì VPS). **Trực giác chỉ đúng
  chỗ, nhưng sai lý do.**
- **Nền tảng KHÔNG sai.** `runClient` với integrated server là **cách chuẩn** để phát triển mod Forge,
  và với một tính năng **nhìn bằng mắt** thì không có lựa chọn nào tốt hơn — VPS headless không chạy
  được client đồ hoạ.
- **Và VPS gần như chắc chắn KHÔNG nhanh hơn.** Nghẽn của ta là **đọc chunk từ đĩa**. Đĩa NVMe trên máy
  cá nhân thường **nhanh hơn** đĩa của VPS giá rẻ (vốn hay là network-backed, IOPS thấp). Chuyển sang
  VPS để mong nhanh hơn là **đặt cược sai chỗ**.
- **NHƯNG lỗ hổng thật thì có, và lớn.** Thử `./gradlew runServer`: **không có `run/eula.txt`** ⇒
  **mod này CHƯA TỪNG MỘT LẦN chạy trên dedicated server.** Mà nó được viết cho cấu hình đó.
- **Thứ hỏng khi lên server thật — và nó không phải hiệu năng, mà là KIẾN TRÚC:**
  - **Mọi giới hạn của bản đồ đều nằm ở CLIENT** (`REQUEST_BATCH`, `MAX_IN_FLIGHT`). Chúng là **giới
    hạn TRÊN MỖI CLIENT**. **10 người mở tablet = 10 lần số request, 10 lần số lần đọc chunk, 10 lần
    tải đĩa** — và **server không có tiếng nói gì**.
  - Đây là dạng mod **chạy hoàn hảo ở chơi đơn và đổ ngay tối đầu tiên đưa lên chỗ thật**.
  - `ServerTerrainWarmer` cũng chạy **theo từng người chơi** ⇒ nhân lên y hệt.
  - Đường `TerrainDisk` dùng **IP server** để đặt tên kho (nhánh multiplayer) — **chưa từng chạy**.
- **Đã sửa: `ServerSurveyBudget`** — **trần chuyển về đúng nơi có tài nguyên**.
  - **Tổng toàn server**: 256 khảo sát đồng thời. Cố ý **là hằng số**, không suy từ số người chơi:
    *server đông thì phục vụ mỗi người CHẬM HƠN, chứ không phải làm NẶNG HƠN.*
  - **Phần mỗi người**: 96. Hai con số này bảo vệ **hai thứ khác nhau** — tổng bảo vệ **server**, phần
    riêng bảo vệ **những người chơi khác** khỏi người vừa mở bản đồ vùng chưa ai tới.
  - Hết chỗ thì **không trả lời**, và **không cần giải thích gì**: client tự hỏi lại sau vài giây, đúng
    đường mà một gói tin rơi vẫn luôn đi.
  - **Hâm nóng nền chỉ chạy khi còn trống một nửa** — nó là việc không ai chờ, còn người đang nhìn bản
    đồ nạp thì có.
- **Phương án đề nghị, theo thứ tự:**
  1. **Test dedicated server ngay trên máy này trước** (`runServer` + `runClient` nối vào localhost).
     Nó chạy **mọi nhánh multiplayer** mà không cần VPS. Cần một dòng: `eula=true` trong `run/eula.txt`.
  2. **Rồi mới lên VPS** — để test **đúng môi trường đích**, không phải để nhanh hơn.
  3. Cái VPS thật sự mua được: kho ô và hâm nóng nền **dùng chung cho mọi người chơi** — hâm một lần,
     cả server hưởng. Đó là thứ chơi đơn **không bao giờ thể hiện được**.

### 2026-08-16 — Vắt tiếp: bỏ hướng seed, và hai chỗ lãng phí thuần tuý
- **Người dùng bác hướng seed** (đúng — xem đánh giá ở entry trước), và hỏi còn vắt được gì nữa.
- **Không đoán. Đọc lại đường giải mã chunk, và tìm ra chỗ nặng nhất chưa ai nhìn.**

**1. Giải mã 24 section trong khi bản đồ chỉ đọc 2.**
- `readSections` giải mã **mọi section của mọi chunk**: mỗi mục palette là **một `ResourceLocation` mới
  + một lượt tra registry**, cộng một `SimpleBitStorage` trên **4096 mục**.
- Chunk overworld có **24 section** (từ -64 tới 320). Bản đồ mặt đất đọc **2**. ⇒ **~90% công việc bị
  vứt đi, 16 lần mỗi ô, đúng trên cái thread mà cả bản đồ đang chờ.**
- **Đã sửa**: `Section` giữ tag và **tự giải mã lần đầu bị hỏi** — mà phần lớn section thì **không bao
  giờ bị hỏi**. Block và biome giải mã **tách nhau**, vì một cột cần cái này thì không cần cái kia
  cùng lúc.
- **Thêm `BY_NAME`**: cùng vài trăm cái tên block xuất hiện ở **mọi chunk của một thế giới**, mỗi lần
  gặp lại tốn một lượt tra. `ConcurrentHashMap` vì lấy mẫu chạy trên worker nào đọc chunk, và giờ có
  nhiều worker.

**2. Một hàng đọc → nhiều hàng đọc.**
- Một hàng riêng đã là khác biệt giữa **4 và 118 ô/giây** (bằng cách thoát khỏi hàng đợi lưu chunk của
  game). Nhiều hàng là **cùng một lý lẽ đó lặp lại**: một cái đĩa phục vụ được nhiều lượt đọc cùng lúc
  đang bị hỏi **từng lượt một**. Đây cũng **đúng cách các plugin bản đồ web làm** — số luồng đặt theo
  số nhân CPU.
- **Định tuyến theo REGION, không phải round-robin** ⇒ **một region file chỉ do đúng một hàng của ta
  mở**. Giữ cache file của mỗi hàng còn hữu ích, và **không tạo ra kiểu tranh chấp mới** — người đọc
  đồng thời của một file vẫn chỉ là *ta* và *game*, đúng tình huống đã cân nhắc từ trước.
- **Nửa số nhân, tối đa 4.** Server còn phải chạy game, còn đây là một cái bản đồ.

- **Trạng thái**: build sạch, boot sạch (0 FATAL; 1 ERROR là loot table SuperbWarfare trỏ Patchouli,
  có sẵn từ trước), `mapCheck` 10 kiểm.
- **Cần người dùng kiểm**: `tiles/s arriving` trong `map trace`. Hai thay đổi này **đều đánh vào thời
  gian mỗi ô**, nên nếu có tác dụng thì con số đó phải **lên rõ rệt** — và nếu **không lên**, thì trần
  đã chuyển sang đĩa vật lý và mình sẽ nói thẳng là hết chỗ vắt.

### 2026-08-16 — "Chấm đen lốm đốm theo vòng tròn": lại là ngân sách tôi vừa thêm
- **Người dùng**: mở tablet lần đầu, bản đồ load **theo vòng tròn** nhưng **lốm đốm chấm đen**; hết
  màn hình chờ vẫn còn. Nhanh, nhưng **nhìn khó chịu**.
- **Log của chính phiên đó nói ra nguyên nhân, và nó là lỗi tôi gieo lượt trước:**
  - `outstanding` đứng ở **152–191** trong khi **`0 tiles/s arriving`**, hàng chục giây liền.
  - Client giữ cửa sổ `MAX_IN_FLIGHT = **192**`; `ServerSurveyBudget.PER_PLAYER = **96**`.
  - ⇒ **Một nửa mỗi cửa sổ bị TỪ CHỐI IM LẶNG.** Mà một request bị từ chối **vẫn chiếm chỗ trong cửa
    sổ** cho tới khi `RETRY_AFTER_MS = 5s` nổ ⇒ client **ngừng xin gì mới**.
- **Và đây là chỗ hai triệu chứng gặp nhau**: những ô bị từ chối rơi **rải rác trong thứ tự client
  xin**, nên lỗ thủng cũng **rải rác** — chứ không nằm ở rìa. **Đúng hình dạng "vòng tròn gọn gàng
  rắc chấm đen"**.
- **Đã sửa: server GIỮ thứ nó chưa có chỗ làm, thay vì từ chối.** Hàng đợi mỗi người, tháo ra mỗi tick
  khi có chỗ trống.
  - Mỗi người được mời đúng phần của mình ⇒ **công bằng rơi ra từ cái trần đã có**, không cần thêm
    một thứ tự lượt riêng.
  - Hàng đợi **có trần và bỏ CÁI CŨ NHẤT**: người vừa kéo bản đồ qua nửa lục địa có một hàng đợi đầy
    đất **họ đã thôi nhìn**, còn request mới nhất luôn là cái gần màn hình họ nhất.
- **Bài học, và nó tổng quát**: *một hệ thống có hai tầng giới hạn thì hai con số phải BIẾT NHAU.*
  Cửa sổ của client và phần của server là hai cái van nối tiếp; đặt van sau nhỏ hơn van trước mà
  **không có đường hồi báo** thì phần chênh không biến thành "chậm hơn" — nó biến thành **đứng im rồi
  giật cục**. Cách chữa **không phải chỉnh con số**, mà là **đừng vứt yêu cầu đi**.
- **Trạng thái**: build sạch, boot sạch, `mapCheck` 10 kiểm.
- **Cần người dùng kiểm**: (1) chấm đen lốm đốm còn không — kỳ vọng **vòng tròn đặc, tiến ra đều**;
  (2) `map trace`: `outstanding` phải **trôi xuống** chứ không dính ở ~190 với 0 ô về.

### 2026-08-17 — Con số thứ bảy: batch 64 gửi đi 32, và nửa cửa sổ là ô ma
- **Người dùng yêu cầu**: chưa test server vội, đi sửa lỗi và **chỗ thừa trong kiến trúc**. Việc đầu
  tiên làm là *đọc lại đường lấy đất* thay vì tin bảng ràng buộc — và bảng đó đang **đã sai ở một ô**.
- **Lỗi nặng nhất, và nó nằm ngay trong lớp vừa mới sửa lượt trước:**
  - `TerrainClientCache.REQUEST_BATCH = **64**`, còn `RequestTerrainTilesMessage.MAX_TILES_PER_REQUEST
    = **32**` — và `encode` **cắt bớt trong im lặng**.
  - Client đánh dấu `ASKED` cho **cả 64 ô** trước khi gửi. Nên mỗi lượt quét sinh ra **32 ô ma**: được
    coi là "đang hỏi server", nhưng **chưa từng rời khỏi máy**, và **chiếm chỗ trong `MAX_IN_FLIGHT`
    cho tới khi `RETRY_AFTER_MS` nổ**.
  - Hệ quả tính ra được: cửa sổ 192 đầy sau **~6 lượt quét (1,5 giây)**, sau đó `room` ≈ 0 và client
    **gần như ngừng xin** cho tới khi timer 30 giây thả ô ma ra. Đây rất có thể là hình dạng thật của
    **"đỉnh 174, trung bình 62"** — không phải server lúc nhanh lúc chậm, mà **client tự bóp cổ mình
    theo chu kỳ**.
  - **Đây là lỗi thứ BẢY cùng một loại**: hai con số phải khớp, viết tay ở hai file không nhắc tới nhau.
- **Cách chữa: không chỉnh số, mà bỏ chỗ để đặt số.** Thêm `terrain/SurveyLimits.java` — **một nơi
  duy nhất** giữ nhóm số này, và **suy chúng ra từ nhau** thay vì viết cạnh nhau:
  - `MAX_IN_FLIGHT = PER_PLAYER + 2 × TILES_PER_REQUEST` (= 160) — cửa sổ client **chỉ được** rộng hơn
    phần của server đúng bằng lượng tồn đọng ta chấp nhận để server giữ hộ.
  - `RETRY_AFTER_MS = MAX_IN_FLIGHT ÷ tốc-độ-chậm-nhất-đo-được (2 ô/giây)` = **80 giây**. Số cũ 30
    giây **vi phạm chính ràng buộc mà bảng đầu tài liệu ghi**.
  - `MAX_QUEUED_PER_PLAYER = MAX_IN_FLIGHT` ⇒ hàng đợi server **không thể tràn** vì một client đúng
    giao thức. Tràn từ nay **chỉ có nghĩa là client không theo giao thức**, và bỏ cái cũ nhất là câu
    trả lời đúng cho việc đó.
  - `REQUEST_BATCH = TILES_PER_REQUEST` ⇒ **ô ma không còn tồn tại được**.
  - Cố ý **không nâng** `TILES_PER_REQUEST`: 32 chính là con số vẫn đang thực sự được gửi, nên hành vi
    không đổi ngoài việc hết ô ma. Nâng nó là *tối ưu khi chưa đo* — đúng thứ bài học số 0 cấm.
- **Thêm `mapcheck/Limits.java`** — biến bảng ràng buộc ở đầu tài liệu thành **kiểm chạy được**. Nó
  đọc hằng số thật qua reflection và khẳng định từng quan hệ, kèm câu giải thích hậu quả nếu phá.
  `./gradlew mapCheck` giờ có 11 kiểm.
- **Năm lỗi khác tìm được khi đọc:**
  1. **`ServerTerrainWarmer` chạy NGOÀI ngân sách.** Nó hỏi `roomForWarming()` rồi gọi thẳng
     `ServerTileCache.get` — nên con số `RUNNING` mà cả lớp tồn tại để giữ **không hề đếm việc duy
     nhất server tự khởi xướng**, và cái van chặn nó đọc chính con số nó không có mặt trong đó. Giờ
     đi qua `beginWarming()/finishWarming()`. **Xin chỗ chứ không xếp hàng** — không ai đợi nó cả.
  2. **Ngân sách rò chỗ khi task ném.** `submit` gọi `start.run()`, mà `finish` lại nằm *bên trong*
     task ⇒ task ném trước khi kịp gắn `whenComplete` thì **chỗ đó mất vĩnh viễn**. Đủ vài lần là bản
     đồ chết hẳn, hàng giờ sau nguyên nhân.
  3. **`ServerTileStore.rootFor` gọi `Files.createDirectories` mỗi lần đọc/ghi** — tức một syscall
     **trên server thread** cho mỗi ô, trong khi cả lý do lớp đó giữ chỉ mục trong RAM là để **trượt
     phải miễn phí**. Giờ giải một lần rồi nhớ.
  4. **Kho server và cache server SỐNG SÓT QUA LẦN ĐỔI THẾ GIỚI.** Single player nạp thế giới này rồi
     thế giới khác trong cùng tiến trình, và `minecraft:overworld` là **cùng một khoá** ở cả hai. Đã
     dọn ở `ServerStoppingEvent`, và chặn cả trường hợp một lượt ghi/quét thư mục về **sau** khi dọn.
  5. **`TerrainDisk` có thể sơn đất thế giới cũ lên thế giới mới.** `clear()` dọn hàng đã đọc xong,
     nhưng **lượt đọc đang bay thì không** — nó về sau đó và được nhận vào như đất của thế giới mới.
     Đã đóng dấu `epoch` lên mỗi request; câu trả lời lệch dấu bị bỏ.
- **Hai chỗ thừa đã dọn:**
  - `TerrainClientCache.hashAt` — **không ai gọi**; javadoc còn khẳng định "đường vẽ đọc mỗi khung
    hình 16 lần", vốn đã sai từ khi `changedSince` thay chỗ nó. Cùng một javadoc mồ côi của một hàm
    đã bị xoá đang treo trên đầu field `HASHES`.
  - **Kho client không có trần nào cả.** Mọi kho khác trong lớp này đều có: texture 256, survey 256,
    hàng đợi 512. Cái này chỉ dọn khi **đổi thế giới** ⇒ bay ngang thế giới là **~40 KB mỗi ô, không
    bao giờ trả lại**. Giờ có `MAX_REMEMBERED = 8192` (gấp đôi 4225 ô của một khung nhìn rộng nhất),
    và **chỉ buông đất ngoài khung nhìn** — vốn đã nằm trong kho đĩa client, đọc lại mất một hai khung.
    Cố ý **không** bump `version` khi buông: bảo một sheet rằng đất của nó đổi trong khi nó chỉ vừa
    bị buông là cách để một cơ chế tiết kiệm RAM đi vẽ ra lỗ.
- **Bước 1 của kế hoạch cũ cũng làm luôn** (không đổi hành vi): `ChunkNbtSampler.stateAt`/`biomeAt`
  quét tuyến tính danh sách section ⇒ đổi sang **mảng theo `sectionY`**, O(1). Một cột đại dương từ
  ~6.100 phép so sánh xuống 255.
- **Trạng thái**: `compileJava` sạch, `mapCheck` **11 kiểm đều qua**. **Chưa ai chạy trong game.**
- **Cần người dùng kiểm**: (1) `map trace` — `outstanding` phải **trôi đều quanh 160** chứ không dính
  trần rồi rơi về 0 theo chu kỳ, và `tiles/s` phải **bớt gợn** (đây là phép thử của lỗi ô ma);
  (2) dòng `tile source: … from store, … surveyed` — vẫn là câu hỏi Bước 0 chưa ai trả lời;
  (3) thoát ra menu rồi vào **một thế giới khác** — bản đồ phải trắng, **không được** có mảng đất của
  thế giới trước.

### 2026-08-17 — Crash khi mở tablet: sheet chưa có texture vẫn được đem đi vẽ
- **Bối cảnh**: bàn giao cho một agent khác làm lại "mũi 1" — nó **chuyển việc dựng sheet sang luồng
  nền** (`BAKE_POOL`, `submitBake`/`drainBakes`, `BakeWorkspace`). Người dùng chạy thử: **crash ngay
  khi mở tablet.**
- **Crash report nói thẳng chỗ**: `TerrainImage.drawPatches:717` → `g.blit(sheet.id, …)` →
  `NullPointerException: … because "pLocation" is null`. Tức **`sheet.id == null`**.
- **Nguyên nhân, và nó là một bài học cũ ở dạng mới:** `sheetFor` trả về sheet qua **tám** câu
  `return sheet.empty ? null : sheet;`. Câu đó hỏi *"ô này có trống không"*, trong khi câu vòng vẽ
  thật sự cần là *"ô này đã có ảnh trên card chưa"*. Hai câu đó **trùng nhau đúng chừng nào** một
  sheet đi từ chưa-có tới đã-vẽ **trong cùng một lời gọi**.
  - Dựng nền đẻ ra **trạng thái thứ ba**: *tồn tại, không trống, chưa có texture*. `Sheet` mới có
    `empty = false` (mặc định) và `id = null` ⇒ nhánh `if (sheet.baking)` trả về đúng nó ⇒ `blit`
    với id null ⇒ chết ở khung hình đầu tiên bản đồ được mở.
  - **Không phải lỗi của việc dựng nền.** Dựng nền chỉ là thứ **làm lộ** ra rằng một vị từ bị chép
    lại tám lần thì sớm muộn có một bản sai. (Bài học #16: phân biệt *gây ra* với *làm lộ*.)
- **Đã sửa**: `Sheet.drawable()` = `id != null && !empty`, và **mọi đường ra** của `sheetFor` (kể cả
  `finish`) đi qua `drawableOrNull(...)`. Giờ **không có cách nào** đưa cho vòng vẽ một sheet không
  blit được. Đây chính là luật #4 (một vùng hình học chỉ định nghĩa ở đúng một nơi) áp cho một vị từ.
- **Lỗi thứ hai tìm được khi soát cùng chỗ — `BlockPalette` đọc mảng NGOÀI khoá.** Bốn accessor làm
  `resolve(blockId)` (có `synchronized`) rồi `return colour[blockId]` (**không**). Mà cả bốn mảng
  đều **bị THAY, không phải sửa tại chỗ** — `grow()` và `forget()` gán mảng mới. Nên đổi thế giới
  hoặc một block được resolve ở luồng khác chen vào giữa hai dòng đó là **index out of bounds**. Vô
  hại khi chỉ có render thread; giờ luồng bake cũng gọi vào đây. Đã đưa phép đọc vào trong khoá.
- **Lỗi thứ ba, và nó là lỗi về QUY TRÌNH: `./gradlew mapCheck` đã CHẾT từ lần refactor đó.**
  - `gather`/`shade` đổi tên thành `gatherInto`/`shadeCell` và nhận thêm `BakeWorkspace`. Harness tìm
    chúng **bằng tên lúc chạy**, nên **không có gì fail lúc biên dịch** — `mapCheck` ném
    `NoSuchMethodException` ở `TileRender`, và **kéo theo bốn kiểm sau nó**: ảnh preview, `Patch`,
    `Store`, `Splash`. Từ lần đó tới nay **không kiểm nào trong bốn cái đó từng chạy**.
  - Đây đúng là cái giá mà chính tài liệu này đã ghi khi dựng harness: *"đổi tên một field làm chúng
    hỏng lúc chạy chứ không phải lúc biên dịch, và đó là cái giá xứng đáng để chúng không nói dối"*.
    Cái giá đó chỉ xứng đáng **nếu có người chạy và đọc kết quả**.
  - Đã sửa `TileRender` và `Patch` theo chữ ký mới. **11 kiểm chạy lại, tất cả qua**, và ảnh preview
    xác nhận màu vẫn đúng (nước xanh, cát, cỏ, đá, đốm dung nham cam — không hoán kênh R/B).

#### Ba khúc mắc của việc dựng nền — ĐÃ XỬ LÝ 2026-08-17
Ghi lại cả cách chữa lẫn lý do, vì hai cái đầu là **cùng một câu hỏi**: *thứ này thuộc về luồng nào?*

**1. `BlockPalette` và bảng tint biome bị đọc từ luồng bake. ✅ Đã chặn tận gốc.**
- Vẽ một cột cần hai thứ chỉ game mới nói được: block **nhìn từ trên** trông thế nào (`BlockPalette`
  → đọc baked model + texture atlas) và biome **tint** nó thành màu gì (`TerrainMips` → đọc
  `mc.level`). **Cả hai đều thuộc render thread**: reload resource pack thay model manager và
  **đóng mọi sprite**, đổi thế giới thay `level`. Luồng bake với tới đó là đọc thứ có thể vừa bị giải
  phóng. Không phải lỗi lý thuyết — nó là kiểu lỗi nổ **một lần trong nhiều giờ**, đúng lúc người
  dùng bấm F3+T hoặc đổi resource pack.
- **Cách chữa không phải thêm khoá, mà là hỏi trước.** `TerrainClientCache.accept()` (render thread)
  giờ gọi `BlockPalette.prewarm` + `TerrainMips.prewarmBiome` cho **mọi cột của mọi ô vừa về**. Một ô
  có 4096 cột nhưng chỉ vài block và vài biome khác nhau, và một id đã biết tốn đúng **một phép đọc
  mảng** — nên vòng lặp rẻ hơn vẻ ngoài của nó rất nhiều, và nó chạy lúc **ô về** (vài chục lần/giây)
  chứ không phải mỗi khung hình.
- **Và một chốt chặn, vì "chắc chắn đã warm hết" là một lời tuyên bố chứ không phải một sự thật:**
  nếu luồng bake vẫn gặp id chưa biết, nó **chỉ dùng nửa không cần client** (màu từ map palette,
  thuần dữ liệu registry) và **cố ý KHÔNG đánh dấu đã resolve**, để render thread làm lại cho đúng.
  Kèm một dòng log **một lần duy nhất**. Tức trường hợp xấu nhất là một block bị vẽ màu palette
  phẳng, không phải một cái crash.
  - **Kết quả đo (log 2026-08-17): KHÔNG có dòng đó.** Việc warm phủ hết mọi đường vào.

**2. `mipsAt` dựng cả chuỗi mip TRONG lúc giữ `TILE_LOCK`. ✅ Đã đưa ra ngoài khoá.**
- Dựng chuỗi mip đi qua toàn bộ ô và mọi màu trong đó. Làm việc đó dưới khoá nghĩa là **một luồng
  bake đang rút gọn một ô thì chặn render thread nhận ô kế tiếp** — đúng vào lúc đất về nhanh nhất,
  cái khoá duy nhất của lớp này sẽ thành thứ bản đồ phải đợi.
- Giờ: đọc dưới khoá → **rút gọn ngoài khoá** → cài lại dưới khoá, có double-check. An toàn vì **ô
  không bao giờ bị sửa sau khi accept** — `accept()` thay cả đối tượng chứ không ghi vào trong nó.
  Cài lại còn kiểm `TILES.get(key) != tile`: ô bị khảo sát lại hoặc bị buông trong lúc rút gọn thì
  **vứt công đi**, chứ không cài một bản rút gọn của phiên bản đã bị thay.

**3. Bake xong mới biết sheet đã bị evict — để nguyên, có chủ ý.**
- `drainBakes` đã kiểm `sheet == null` và bỏ qua, nên **không có lỗi**, chỉ có công bị phí. Sửa nó là
  tối ưu khi chưa đo, đúng thứ bài học số 0 cấm. **Phép thử nếu nghi**: nếu trace cho thấy nhiều bake
  bị vứt thì `MAX_BAKING` (8) và `MAX_LIVE_TEXTURES` (256) đang đánh nhau — nhìn cặp đó, đừng nhìn
  từng cái.

### 2026-08-17 — Dọn nốt: luồng nào sở hữu cái gì
- **Bài học mới (#22), và nó khái quát hơn cái crash vừa rồi:** *khi đưa việc sang luồng khác, thứ
  phải đi cùng nó không phải là dữ liệu — mà là câu trả lời cho mọi thứ nó sẽ hỏi.* Bake được đưa
  sang luồng nền cùng với dữ liệu ô (đúng, có khoá), nhưng **không** cùng với hai bảng tra mà nó vẫn
  phải hỏi game — và game chỉ trả lời trên render thread. Cách chữa đúng là **làm cho nó không còn
  gì để hỏi**: warm cả hai bảng lúc ô về, rồi luồng nền coi cả vùng đó là chỉ-đọc.
- **Hệ quả thứ hai của cùng bài học đó**: một khoá không biến việc thuộc-luồng-khác thành an toàn,
  nó chỉ biến việc đó thành **tuần tự**. `mipsAt` giữ khoá suốt lúc rút gọn là ví dụ — đúng về tính
  đúng đắn, sai về việc ai phải đợi ai.

### 2026-08-17 — Mờ vĩnh viễn một mảng bản đồ từ zoom 4000: cùng gốc với cái crash
- **Người dùng báo**: từ mức zoom **4000 trở lên**, **một phần** bản đồ bị mờ. Không phải cả tấm, và
  không phải mờ rồi nét — mờ và ở lại.
- **"Chỉ từ đúng một nấc zoom" là một DẤU VÂN TAY, không phải một triệu chứng** (bài học #10). 4000m
  là **nấc đầu tiên mức vẽ đổi** (level 0 → 1, xem bảng `zooms`), tức là nấc đầu tiên **cả một khung
  nhìn sheet mới bị đòi trong đúng một khung hình**. Đó là điều kiện, không phải nguyên nhân.
- **Nguyên nhân, và nó là CÙNG GỐC với cái crash hôm nay**: code hỏi *"ô này có trống không"*
  (`empty`) ở chỗ đáng lẽ phải hỏi *"ô này có câu trả lời nào chưa"*.
  - Một sheet bị **bỏ qua lượt bake** (pool đầy — `MAX_BAKING = 8` — hoặc hết budget) là:
    **không có pixel, và cũng chưa có kết luận "ở đây trống"**. Nó không thuộc cả hai loại mà code
    biết.
  - Khung hình sau, nó rơi vào lối tắt `if (logReaches && !spread)` — *"đất đổi ở chỗ khác, ô này
    vẫn đúng như nó vốn có"*. Câu đó **hoàn toàn đúng với một ô đã từng được nhìn**, và là **một sự
    thăng chức** với một ô chưa từng được vẽ: nó **đóng dấu version hiện tại lên một ô chưa hề có
    ảnh**.
  - Từ đó trở đi ô đó **được coi là mới nhất và không bao giờ được bake lại**. Lượt vẽ tinh không
    bao giờ vẽ nó ⇒ lớp phủ thô lộ ra ở đúng chỗ đó ⇒ **mờ, và ở lại tới hết phiên**.
- **Đã sửa — ba chỗ, một câu hỏi:** thêm `Sheet.answered()` = `id != null || empty` ("ô này có câu
  trả lời nào chưa"), tách khỏi `drawable()` = `id != null && !empty` ("ô này vẽ được chưa").
  1. Lối tắt "vẫn đúng như nó vốn có" giờ đòi thêm `sheet.answered()`.
  2. Nhánh "đúng version rồi" cũng đòi `sheet.answered()` — không thì ô kẹt vĩnh viễn ở đó.
  3. `allowed = sheet.empty ? mayBuild() : mayRebuild()` → **`sheet.drawable() ? mayRebuild() :
     mayBuild()`**. Một ô **chưa có pixel là một cái LỖ**, và lỗ dùng budget dựng (4/khung), không
     phải budget vẽ lại (**1/khung**). Chính javadoc của hai hằng số đó đã nói đúng điều này từ
     trước; chỉ có câu lệnh là hỏi nhầm.
- **Bài học #23**: *khi thêm một trạng thái trung gian, mọi câu hỏi nhị phân về trạng thái cũ đều
  thành nghi phạm.* Dựng nền thêm trạng thái **"đang chờ"** vào giữa "trống" và "đã vẽ". Một câu hỏi
  hai nhánh (`empty` hay không) không diễn tả được ba trạng thái — nên **mỗi** chỗ hỏi nó đều sai
  theo một kiểu riêng: chỗ vẽ thì **crash**, chỗ lối tắt thì **kẹt vĩnh viễn**, chỗ budget thì
  **chậm gấp bốn**. Cả ba đều là một dòng sửa, và không dòng nào tìm ra được nếu chỉ chữa triệu
  chứng của dòng kia.

### 2026-08-17 — Đất thế giới cũ hiện trên bản đồ thế giới mới: một sheet đang bake sống sót qua lần đổi thế giới
- **Người dùng test 5 điểm, bốn điểm đầu OK** (không crash, hết mảng mờ, đổi resource pack sống, màu
  đúng). **Điểm cuối hỏng**: vào thế giới mới, giữa đại dương có **một ô vuông sắc nét** là rừng của
  thế giới trước.
- **Kích thước ô vuông đó chính là manh mối.** Đo trên ảnh: ~2200m, tức **`groundPerSheet(3)` =
  2048** — một sheet **level 3**, đúng mức lớp phủ thô. Một sheet, không phải một vùng ⇒ **không
  phải kho đĩa trả sai**, mà là **đúng một kết quả bake** rơi nhầm chỗ.
- **Nguyên nhân**: bake **sống lâu hơn thế giới đã đặt nó**. Không có gì huỷ một bake đang chạy —
  luồng pool đang ở giữa một ô khi người chơi thoát ra menu — nên kết quả về **trong thế giới nào
  đang mở lúc đó**. `drainBakes` tra sheet **theo toạ độ**, mà toạ độ ở thế giới mới là chỗ khác
  hoàn toàn. `close()` có dọn hàng đợi và `sheets`, nhưng **không với tới thứ chưa xong**.
  - Level 3 là mức **đắt nhất** để bake, nên nó là mức **dễ còn đang chạy nhất** lúc đổi thế giới.
    Đó là lý do lỗi hiện ra ở đúng lớp phủ thô chứ không phải lớp tinh.
- **Đã sửa**: `BakeResult` mang thêm `generation` — đọc **trên render thread lúc submit** rồi truyền
  vào task (đọc trong task sẽ là thế giới lúc task giành được luồng, tức là sai). `drainBakes` bỏ
  mọi kết quả có `generation != builtGeneration`. **Đúng cái chốt mà `TerrainDisk` đã có, dựng thêm
  một tầng lên trên.**
- **Và một lỗ thứ hai cùng loại, chặn luôn**: kho đĩa client xếp thư mục theo **tên hiển thị** của
  thế giới. "New World" là tên game gợi ý **mọi lần** ⇒ hai thế giới cùng tên **dùng chung một kho**,
  và thế giới thứ hai mở bản đồ ra đã đầy đất của thế giới thứ nhất. Đổi sang **tên thư mục save**,
  vốn là câu trả lời của chính game cho cùng câu hỏi và **không thể trùng** (game tự thêm hậu tố).
  - Giá phải trả: đất đã lưu theo lối cũ thành mồ côi ⇒ khảo sát lại một lần. Rẻ nhất có thể so với
    thứ nó ngăn.
- **Bài học #24**: *mọi việc chạy nền đều phải mang theo dấu THẾ GIỚI nó được giao, không chỉ dấu dữ
  liệu.* Đây là lần thứ **ba** cùng một lỗi trong hai ngày — kho server (dọn ở `ServerStoppingEvent`),
  đọc đĩa client (`epoch`), và giờ là bake (`generation`). Ba tầng khác nhau, **một câu hỏi**: *câu
  trả lời này là về thế giới nào?* Chỗ nào có việc bất đồng bộ, chỗ đó phải trả lời được câu này.

### 2026-08-17 — Đọc log: Bước 0 đã có câu trả lời, và hai thứ đáng ngờ mới
- **Bước 0 — TRẢ LỜI XONG**: `tile source: 0 from store, 112 surveyed` — **`from store` bằng 0 ở MỌI
  dòng**, suốt cả phiên. Nhịp cao **hoàn toàn là khảo sát thật**, kho server không trả lời lấy một ô.
  - **Và đó đúng như thiết kế, không phải lỗi**: client hỏi mỗi ô **đúng một lần** (ô đầy đủ không
    bao giờ hỏi lại), nên trong **một** phiên kho không thể được hỏi lần thứ hai. Kho server chỉ ăn
    tiền ở **phiên sau** và ở **người chơi thứ hai**.
  - **Bài học đo đạc**: đừng đánh giá một cache bằng phiên chạy mà nó không thể được hỏi. Muốn đo kho
    server thì phải **thoát ra vào lại**, hoặc hai client.
- **Không có dòng `met block … on a background thread`** ⇒ việc warm palette phủ hết mọi đường vào.
- **Không có dòng `patched a square before it had been drawn`** ⇒ nghi vấn cũ đó coi như sạch.
- **Hai thứ MỚI đáng ngờ, chưa xử lý, đừng quên:**
  1. **`gather` mỗi lần dựng tăng dần theo thời gian**: 0,83 ms → 2,85 → 9,40 → **14,72 ms** trong
     vòng vài phút, mà `shade` gần như đứng yên (0,10 → 1,26). Tức chi phí nằm ở **đọc kho ô**, và nó
     **lớn dần theo số ô đang giữ**. Nghi phạm đầu tiên: khoá `TILE_LOCK` bị tranh, hoặc `mipsAt`
     dựng lại mip cho những ô vừa bị buông rồi đọc lại. **Đo trước, đừng đoán.**
  2. **`256 sheets live of 256` ở MỌI dòng** — trần texture lúc nào cũng đầy, tức `evictSurplus` chạy
     liên tục. Đúng thứ bài học #9 nói: bỏ chi phí lớn nhất thì chi phí kế tiếp thành lớn nhất.
- **Trạng thái**: `gradlew build` sạch, `mapCheck` 11 kiểm đều qua. **Bản sửa đổi-thế-giới chưa test.**
- **Cần người dùng kiểm**: thoát ra menu → vào **thế giới khác** → bản đồ phải trắng hoàn toàn.
  Làm **hai lần**, và lần thứ hai thoát ra **ngay khi bản đồ đang load dở** (đó là lúc có bake đang
  chạy — chính là điều kiện sinh ra lỗi).

### 2026-08-17 — Bước 2: hâm nóng thích ứng
- **Xuất phát từ ý tưởng của người dùng**: *"vẽ map ở client trước cho nhanh, data quan trọng chạy
  nền rồi trả về sau"*. Đã trao đổi lại và chốt được chỗ nó thực sự nằm:
  - **Nửa "client vẽ trước" ĐÃ LÀ kiến trúc**: chặng 1 (chunk client đang giữ) và chặng 2 (kho đĩa
    client) của đường đi 10 chặng, không đụng server. Và **server không hề vẽ gì** — nó chỉ đọc
    chunk từ đĩa. Vẽ đã ở client từ đầu và tốn 0,6 ms/khung. **Cái chậm không phải VẼ, mà là BIẾT.**
  - **Nửa "chạy nền rồi trả về" cũng đã có tên**: `ServerTerrainWarmer`. Nhưng nó chạy ở **1/10 công
    suất**, nên ý tưởng đúng mà cơ chế thì đang bị bỏ đói.
  - Phần duy nhất **không** áp dụng được là cho client tự có đất ngoài tầm nhìn — chỉ có cách sinh
    từ seed, vốn đã bị bác và bác đúng.
- **Bằng chứng nó đang bị bỏ đói, từ log của chính người dùng**: `0 running of 256, 0 queued` ở
  **mọi** dòng. Server rỗng gần như liên tục, trong khi người chơi đi tới chỗ mới vẫn phải đợi bản
  đồ. Một cái máy ngồi không, cạnh một người đang chờ.
- **Cách chữa KHÔNG phải một con số to hơn.** Một con số phẳng to hơn là **cùng một sai lầm quay
  ngược lại**: nó sẽ khảo sát mạnh nhất đúng lúc có người đang nhìn bản đồ load. Thứ còn thiếu là:
  nhịp đúng **phụ thuộc vào một câu hỏi server tự hỏi được** — *lúc này có ai đang đợi không?*
- **Tín hiệu đã có sẵn, không cần bịa ra**: client **chỉ xin đất khi tablet đang trên tay**, và chỉ
  xin khi nó **đang thiếu**. Nên chính các gói tin xin đất là tín hiệu. Một bản đồ mở sẵn và đã vẽ
  đủ thì không xin gì — và đúng là **trên màn hình đó cũng không ai đang đợi gì**.
  - `ServerSurveyBudget.noteRequest()` / `anyoneWatching()`, cửa sổ `QUIET_MS = 3s` — dài hơn hơn
    mười lượt quét của client (250 ms/lượt), nên thứ đang thực sự kéo đất **không thể trông có vẻ
    yên tĩnh**; ngắn đủ để hâm nóng nhường đường trong vòng một lượt của chính nó.
- **Nhịp, và nó SUY RA chứ không chọn tay** (theo đúng luật của `SurveyLimits`):
  | | |
  |---|---|
  | `PER_PASS_BUSY` | **4** — giữ nguyên. Người đang chờ trên màn hình là toàn bộ lý do lớp này tồn tại |
  | `PASSES_TO_FILL` | 2 giây — **con số duy nhất chọn tay**, và nó chỉ quyết định độ *mượt* khi ramp lên, không quyết định throughput |
  | `PER_PASS_QUIET` | `SPARE_FOR_WARMING / PASSES_TO_FILL` = **16/lượt = 64 ô/giây** |
  - **Vì sao suy từ budget**: `beginWarming()` từ chối khi quá `SPARE_FOR_WARMING`, nên nạp nhanh hơn
    mức đó **không phải nhịp cao hơn — nó là một vòng lặp quay không**. Thứ đang được định cỡ ở đây
    là **tốc độ NẠP**, vừa đủ giữ budget đầy, không hơn.
  - Kết quả: **16 → 64 ô/giây** khi rỗi (gấp 4), vẫn dưới năng lực đo được 62–174, và **vẫn 16/giây**
    khi có người nhìn.
- **Thêm luân phiên người chơi.** Budget dùng chung và một lượt **dừng ngay khi bị từ chối**, nên nếu
  không luân phiên thì người đứng đầu danh sách lấy hết phần hâm nóng và những người sau **không còn
  gì**. Vô hình ở một người chơi — đúng loại thứ phải **đúng do cấu trúc chứ không do test**.
- **Đo được, không phải tin được.** Dòng report giờ có thêm `| N warmed (quiet|someone watching)`.
  Hâm nóng là nửa **không ai nhìn thấy**: một khảo sát xong trước khi có người hỏi không để lại dấu
  vết nào trên màn hình — bản đồ chỉ đơn giản là *hiện ra*. Không có dòng này thì bằng chứng duy nhất
  của việc nó hoạt động là **sự vắng mặt của một lần phải chờ**, mà thứ đó không đọc được.
- **`mapcheck/Limits.java` thêm ba khẳng định**: nhịp rỗi không được **thấp hơn** nhịp bận (đó là
  toàn bộ ý nghĩa của lớp này, đảo ngược), một lượt không được định khởi động nhiều hơn cả budget, và
  nhịp rỗi phải **đủ để dùng hết phần budget đã dành cho nó**.
- **Trạng thái**: `gradlew build` sạch, `mapCheck` 11 kiểm đều qua, dòng limits in ra
  `warming 64/s quiet vs 16/s busy, budget 128`. **Chưa test trong game.**
- 🔴 **VÀ ĐÂY LÀ LÚC DEDICATED SERVER THỰC SỰ ĐÁNG TEST.** Thay đổi này đụng vào **cả ba** thứ đã ghi
  là điều kiện: `ServerSurveyBudget`, nhịp hâm nóng, và việc chia budget giữa nhiều người. Phần luân
  phiên người chơi **không thể quan sát được ở một người chơi**. Nhắc theo đúng giao kèo — quyết định
  là của người dùng.
- **Cần người dùng kiểm** (một người chơi):
  1. Đóng tablet, đứng yên ~30 giây → log phải có `N warmed (quiet)` với **N lớn hơn hẳn** trước đây,
     và `X running of 256` phải **khác 0**.
  2. Mở tablet ra và kéo bản đồ → dòng report phải đổi sang `(someone watching)` và **N tụt xuống**.
  3. Đi tới vùng đất mới **chưa từng tới** rồi mở tablet → đây là phép thử thật sự: bản đồ phải hiện
     **nhanh hơn rõ rệt** so với trước, vì đất lẽ ra đã được khảo sát sẵn lúc bạn đang đi tới đó.
  4. Không được có stutter. Nếu có, nghi phạm đầu tiên là `PASSES_TO_FILL` — nhưng **đo trước**.

### 2026-08-18 — Đóng phiên: nhận diện thế giới, dọn phần bắn, và một ngõ cụt đáng ghi lại

**Nhận diện thế giới bằng dimension thôi là sai — `checkWorld()` cũng dính.** Đổi thế giới A → B,
`minecraft:overworld` bằng chính nó ở cả hai, nên `checkWorld()` coi như không có gì đổi và **không
dọn gì cả**. Mọi chốt chặn dựng hôm trước (dấu thế giới trên bake, epoch kho đĩa) đều chờ đúng phép
so sánh này nên chưa từng nổ. Đã đổi sang **thế giới + dimension** làm danh tính, dùng chung câu trả
lời với kho đĩa. `ensureLineCovered` (đường cảnh báo vướng đạn trên HUD, dùng được khi tablet đóng)
cũng được thêm kiểm này.

**Rà lần đầu phần điều khiển bắn — tìm hai lỗi thật:**
- Công thức ngắm (điểm ngắm dưới tâm block + đảo cờ `depressed`) là tái hiện nội bộ SBW, bị **viết
  hai lần** ở `ReachabilityCheck` và `FlightProfile`. Không có gì giữ chúng khớp nhau khi SBW đổi.
  Gom vào `LaunchSolution` — một nơi duy nhất.
- `FireScheduler` (ripple, chờ nòng) không dọn khi server dừng — lệnh bắn thế giới cũ sống sót sang
  thế giới mới, ôm theo entity đã biến mất. Cùng họ với bài học #24, tìm thêm ở `ServerSurveyBudget`
  và `ServerTerrainWarmer` (hàng đợi/con trỏ ring không dọn khi server dừng).

**Đất thế giới cũ vẫn lọt sang — nguyên nhân thật nằm ở một dấu CHẤM.** `getWorldPath(ROOT)` trả về
`saves/<world>/.`, và tên file cuối của đường dẫn đó là `"."` — **giống hệt nhau ở mọi thế giới**.
Kết quả: mọi thế giới single player dùng chung một thư mục kho, bất kể tên. Đã chuẩn hoá đường dẫn
trước khi lấy tên, và **`safeName` giờ từ chối `.`/`..`** — không chỉ né riêng trường hợp này. Thêm
kiểm tự động vào `mapCheck`, và nó **fail ngay lần chạy đầu, hai lần liền** trước khi được sửa đúng.
- Cùng lúc, sửa luôn: `drainBakes` gỡ cờ "đang dựng" **trước** khi có thể từ chối kết quả — trước đó
  từ chối trước làm một ô kẹt "đang dựng" vĩnh viễn, không bao giờ được vẽ lại (nghi phạm cho mờ ở
  2000m mà người dùng báo — **chưa xác nhận hết**).
- Người dùng tự xoá `run/artillerytablet` (yêu cầu mình xoá hộ, đã kiểm chỉ có file `.tile` trước khi
  xoá). Log sau đó xác nhận kho lưu đúng thư mục `New_World`, và hâm nóng chạy đúng thiết kế
  (`320 warmed` mỗi 5s = 64 ô/giây khi rỗi).

**Ngõ cụt: thử BlueMap để so sánh, không chạy được — Java 21 vs Java 17.** Cài theo đúng khuôn
`runtimeOnly` mà JourneyMap đang dùng (toạ độ CurseForge xác minh qua trang thật, không đoán). Client
crash ngay lúc nạp mod: `UnsupportedClassVersionError`, bản BlueMap 5.12 compile cho Java 21, dự án
này khoá Java 17 (Forge 1.20.1 bắt buộc). Đã gỡ khỏi `build.gradle`, giữ toạ độ trong
`gradle.properties` kèm ghi chú — không phải lỗi cấu hình, là bản thân file không chạy được ở đây.

**Người dùng chốt ba việc để giảm usage, áp dụng từ phiên sau:**
1. Comment ngắn cho sửa nhỏ lặp khuôn (văn phong đầy đủ giữ cho quyết định kiến trúc thật)
2. Kiểm rẻ trước khi làm đắt (đừng chạy `runClient`/build đầy đủ để xác nhận thứ tra web là đủ)
3. Phiên mới cho chủ đề không liên quan, đừng nối dài một thread

**Trạng thái**: `gradlew build` sạch. `mapCheck` — cần chạy lại lần cuối trước khi đóng phiên (đã
qua ở mọi lần trong phiên, chưa chạy lại sau commit cuối). Đã bàn (không code): các hướng giảm chi
phí khảo sát (tách dữ liệu độ cao khỏi màu, lệnh khảo sát trước, ô nhỏ hơn) — **chưa đo, chưa làm**,
xem thảo luận cuối phiên nếu muốn tiếp tục.

**Cần người dùng kiểm ở phiên chơi tiếp theo** (chưa ai xác nhận từ khi các bản sửa trên vào):
1. Đổi thế giới → bản đồ trắng, không còn ô đất cũ (test lần 3, hai lần trước đều tìm ra lỗi mới)
2. Zoom 2000m và dưới — mờ còn không (nghi phạm đã sửa nhưng chưa xác nhận)
3. Bắn vài phát — `LaunchSolution` mới tách ra, đường bắn thật chưa test lại sau khi gom code

### 2026-08-18 (phiên sau) — Kiểm lại #12 bằng đường rẻ: hai nửa của bản sửa giờ có kiểm chạy được

**Mục tiêu**: test lại "đất thế giới cũ" (#12) — đã sửa ba lần, lần nào test cũng lộ lỗi mới.
Claude không chơi được, nên phần rẻ làm trước: đọc code cả sáu tầng, rồi biến phần **kiểm được ngoài
game** thành kiểm thật.

**Đọc code — sáu tầng đều có chỗ dọn, và chỗ dọn đó có chạy:**
kho server + cache ô server (`ServerTileStore.onServerStopping` gọi luôn `ServerTileCache.forgetAll`),
ngân sách khảo sát, con trỏ hâm nóng, hàng đọc chunk, lệnh bắn đang chờ — cả năm lớp đều bắt
`ServerStoppingEvent`, và **cả năm đều đăng ký trên FORGE bus** (kiểm riêng: một handler không đăng
ký thì đọc code thấy đúng mà chạy không làm gì). Phía client: `checkWorld` so **thư mục save + chiều**
chứ không chỉ chiều, và `TerrainImage.draw`/`drainBakes` gác bằng `generation`.

**Hai nửa chưa có kiểm nào chạm tới, giờ đã có:**
- `store: a read outlived its world and was refused on arrival` — chặn luồng IO duy nhất bằng latch
  để lượt đọc **treo lại thật**, đổi thế giới bên dưới, rồi thả. Không phải chờ may rủi.
- `store: 4 save paths each named after its own folder` — tách bước lấy tên thư mục save thành
  `TerrainDisk.saveFolderName(Path)` để harness gọi **đúng hàm sản phẩm**, không chép lại phép tính
  đường dẫn (chép lại thì kiểm chỉ chứng minh bản chép đúng).

**Thử phá guard để kiểm chính cái kiểm** (bài học 17): bỏ `epoch != asOf` → đỏ đúng câu "a tile read
for the previous world arrived in this one"; bỏ `.normalize()` → đỏ đúng câu tên save ra `null`.
Khôi phục cả hai → `map checks passed`.

**Còn lại cho người chơi** — không đường nào rẻ hơn: bản thân việc đổi thế giới trong game. Harness
không có `Minecraft.getInstance()`, nên nhánh chọn giữa single player và server, con dấu `generation`
của bake, và việc dọn phía server đều chỉ chạy thật trong game.

### 2026-08-18 (phiên sau) — Mảng mờ đóng băng ở 4000m đổ xuống: bản đồ dựng đất rồi tự vứt đi

**Người dùng báo**: một số khu mờ hoặc đang lấy nét thì đứng luôn, ở khoảng 4000m đổ xuống.

**Đọc code không ra.** Mọi nhánh của `sheetFor` mình lần đều tự phục hồi được ở khung sau. Có ba
giả thuyết khớp triệu chứng như nhau — ngân sách khung hình, pool bake, đuổi texture — và **không có
cách nào phân biệt bằng mắt**. Đúng tình huống bài học 0 cảnh báo.

**Nên đo thay vì đoán.** Trace cũ trả lời "một khung hình tốn bao nhiêu", không trả lời "vì sao mảng
kia còn mờ". Thêm bốn số vào chính dòng trace sẵn có: `blurred/frame`, số lần từ chối **tách riêng** vì
ngân sách và vì pool, `evicted/frame`, và `patched unbuilt`. Bốn số đó phân biệt được ba giả thuyết —
và số thứ tư (mờ đứng yên trong khi mọi số từ chối bằng 0) là câu trả lời tệ nhất: không ai còn cố
dựng ô đó nữa.

**Số đo đóng đinh nguyên nhân trong 40 giây đứng yên:**
```
7.0 blurred/frame | 0.0 builds/frame | 4.0 evicted/frame | 256/256 sheets | 0 outstanding
```
`outstanding = 0` loại mạng ra khỏi danh sách. Cấp phép **4** ô dựng mỗi khung, đuổi **đúng 4**, hoàn
thành **0** — một **vòng quay**: mỗi khung dựng 4 ô rồi vứt đúng 4 ô đó.

**Chỗ hỏng**: nhánh tạo sheet mới trong `sheetFor` **không đặt `lastDrawn`**. Bảy lối ra khác của hàm
đều đặt; đúng lối ra này thì không. Ô vừa tạo mang tuổi `0` — **già nhất có thể** — nên `evictSurplus`
chạy ở cuối *chính khung hình đó* xếp nó lên đầu hàng bị đuổi, trong khi bake còn đang chạy. Khung sau
lại thiếu, lại tạo, lại bị đuổi.
- **Vì sao chỉ 4000m đổ xuống**: vòng quay chỉ khởi động khi `sheets` chạm trần `MAX_LIVE_TEXTURES`.
  Chỉ tập ô của mức 0/1 mới đủ lớn để chạm trần. Không phải "lỗi của nấc zoom" mà là "nấc zoom nào
  chạm trần trước".
- **Ô đen trong ảnh người dùng gửi**: cùng lỗi ở lớp phủ thô — nó cũng bị đuổi, nên không còn gì phủ
  vào chỗ trống.

**Sửa**: đánh dấu ô vừa tạo là đang cần **trước khi** có thứ gì được phép dọn chỗ. Kèm chỗ thứ hai —
ô hâm nóng phải hạ bậc xuống dưới ô đang nhìn, không thì nó thừa hưởng đúng ưu tiên vừa cho và lấn
chỗ ô đang xem (cùng lỗi, đội mũ khác).

**Xác nhận bằng log lần chạy sau**: mờ tụt `51 → 9.7 → 2.0 → 0.0` trong ~8 giây; tại trần 256/256 giữ
`0.0 blurred, 0.0 evicted, refused 0`; một lần pan cho `8.2 blurred` rồi về `0.1` ngay báo cáo kế
tiếp. `evicted/frame` từ **4.0 cố định** xuống **0.0–0.4**.

**Còn treo, đã biết**: `patched unbuilt` vẫn nhích (0→11 trong ~50 giây) dù javadoc của nó ghi *"Should
stay at nought"*. Đường vá vẫn thỉnh thoảng rơi vào ô chưa có ảnh, và khi đó `repaint` nới ra dựng cả
sheet **ngay trên render thread**. Không còn gây đóng băng, nhưng là việc nặng nằm sai luồng — nghi
phạm tiếp theo nếu còn thấy khựng.
### 2026-08-18 (phiên sau) — Bậc thang răng cưa ngoài khơi: tint quần xã chưa được trộn

**Người dùng báo**: chỗ nước gần bờ (xanh đậm) giáp nước ngoài khơi (xanh nhạt) gãy răng cưa lởm chởm.

**Chẩn đoán từ chính bức ảnh**: vùng ngoài khơi **sáng hơn** vùng gần bờ. Nếu do độ sâu thì phải ngược
lại — `WATER_DARKEN` làm nước sâu **tối** đi. Vậy đó là **biên quần xã**, không phải độ sâu.

**Nguyên nhân**: màu nước của một quần xã là một **hàm bậc thang** — biển ấm và biển lạnh gặp nhau ở
một đường, không có gì ở giữa. Game giấu chuyện đó bằng cách **trung bình màu quần xã trên một vùng
lân cận** trước khi vẽ; mod lấy tint theo đúng một cột nên tái hiện nguyên cái bậc thang.

**Đã sửa**: `TerrainMips.blendedWaterTint` — trung bình bán kính 2 (đúng bán kính game dùng).
- **Chỉ trộn cột có nước.** Cột đất cũng mang một màu nước và nó vô nghĩa ở đó; đếm vào sẽ kéo màu
  biển về phía bờ. Mép đất/nước vì thế vẫn **sắc**, điều đáng giá hơn trên bản đồ dùng để đặt pháo.
- **Chặn ở mép ô, không với sang ô bên cạnh.** Ô bên có thể chưa được giữ, và một màu phụ thuộc vào
  việc nó tình cờ có mặt hay không sẽ làm cùng một vùng nước ra hai màu ở hai lần xem.
- **Trộn thẳng, không qua ánh sáng tuyến tính** — đây là tint quần xã chứ không phải độ sáng, và đó là
  phép tính game làm.

**Chỗ dễ sai nhất, đã chặn**: `groundColour` được gọi từ **hai** đường — `TerrainMips` cho mức thô và
`TerrainImage` cho mức 0. Trộn ở một đường thôi thì biển sẽ **đổi màu ngay nấc zoom đổi mức**. Cả hai
giờ gọi chung `blendedWaterTint`, và tint được **truyền vào** `groundColour` thay vì tra bên trong.

**Giới hạn của kiểm**: `mapCheck` không chạm được đường này — harness không có `Minecraft.getInstance()`
nên `known(biomeId)` luôn false và tint rơi về fallback. Chỉ mắt người trong game xác nhận được.

### 2026-08-18 (phiên sau) — Đóng phiên: ba việc xong, một việc hoá ra không phải việc

**Người dùng xác nhận trong game**: #12 (đất thế giới cũ) xong, #14 (mờ đóng băng) xong, nước hết
răng cưa xong.

**"Xé hình khi kéo" hoá ra là vsync bên máy người dùng, không phải mod.** Đáng ghi lại vì cách nó
được loại trừ: hình học đã kiểm là đúng (tâm số thực, một phép dịch chung), nghi phạm còn lại là
`MAG nearest` — vốn nằm trong bảng *"đừng bàn lại"*. Thay vì sửa, đưa ra một **dự đoán kiểm được**:
*nếu đúng là bộ lọc thì rung phải mạnh nhất ở 250m và biến mất từ 2000m trở lên, vì từ đó là thu nhỏ
và dùng `linear`*. Người dùng đi thử và tìm ra vsync. **Nếu đã sửa theo chẩn đoán thì đã phá một
quyết định kiến trúc để chữa một thứ không tồn tại** — cùng họ với bài học 17, nhưng ở phía ngược:
không chỉ phép thử sai buộc tội code, mà cả một triệu chứng thật cũng có thể không thuộc về code.

**Việc tiếp theo đã chốt, để dành cho phiên riêng**: đất cũ còn sót sau khi thoát và vào lại cùng một
thế giới. Không phải lỗi — là hai đánh đổi đã chốt gặp nhau (server chỉ khảo sát lại khi có chunk
đang nạp; client không bao giờ hỏi lại ô đầy đủ). Xem điểm 2 phần bàn giao.

### 2026-08-18 (phiên sau) — Thiết kế khung ngoài kiểu MFCS: phác thảo + hai quyết định đã chốt

**Người dùng muốn** một bộ khung ngoài cho UI, tham khảo MFCS của quân đội Mỹ nhưng **đổi vàng sa mạc
sang tông tối**. Theo bài học 15, phác thảo đúng tỉ lệ trước — `docs/mfcs-bezel-mockup.svg`, vẽ trong
**đơn vị điểm ảnh logic thật**, nên số chú thích chính là số đưa vào code.

**Hai quyết định người dùng đã chốt — không cần hỏi lại:**

**1. Thu bản đồ để lấy chỗ cho khung.** `SCREEN_FRACTION` **0.92 → ~0.86**. Giá: bản đồ mất ~15%
chiều, ~28% diện tích. Đổi lại khung đủ dày để có gờ và ốc, và **số ô sheet cần dựng giảm theo** —
nhẹ hơn cho trần `MAX_LIVE_TEXTURES = 256`, tức nó cũng đẩy lùi đúng cái trần đã gây ra lỗi #14.

**2. Mọi phím cứng phải bấm được.** Lý do người dùng chọn: trên một thiết bị bắn, một nút không làm
gì là một lời hứa sai (cùng tinh thần bài học 14).

**Bảng gán phím — và hai thứ CHƯA TỒN TẠI:**

| Phím | Việc | Có sẵn chưa |
|---|---|---|
| PWR | đóng tablet | ✅ `onClose` |
| BRT+ / BRT− | độ sáng bản đồ | 🔴 **CHƯA CÓ** — cần một hệ số sáng trong đường tô của `TerrainImage` |
| N/V | chế độ đêm (đơn sắc) | 🔴 **CHƯA CÓ** — cần bảng màu thứ hai |
| FN1–FN4 | FFE / ADJ / MOD / ARC | ✅ đúng 4 phím hành động của rail |
| CFF (đỏ) | mở thẳng FFE | ✅ |
| FN5–FN9 | BTY / TGT / AMO / STA / LOG | ✅ đúng 5 tab của rail |
| FN10 | về vị trí người chơi | ✅ `recentreOnPlayer` |
| FN11 | để trống, nhãn mờ | — |

**Trùng lặp với rail phần mềm là CỐ Ý**: trên MFCS thật, phím FN chính là lối tắt cứng tới chức năng
đang hiện trên màn hình. Không phải hai bộ điều khiển, là hai đường tới cùng một bộ.

**Đổi so với mẫu MFCS, có chủ ý:**
- Vỏ dùng dải xám xanh sẵn có của `TabletTheme`; không thêm màu mới nào ngoài xám vỏ và đỏ phím khẩn
- Băng `UNCLASSIFIED` xanh lá → dải **OPERATIONAL**, giữ đúng vai trò "trạng thái liếc một cái là thấy"
- Nút đỏ `911` → **CFF** (Call For Fire) — tương đương khẩn cấp nhưng đúng nghiệp vụ pháo binh
- Hai cột phím **26 px**, trùng đúng bề rộng `RAIL`, để viền ngoài và rail phần mềm xếp thẳng hàng

**CHƯA CODE.** Việc đầu tiên của phiên triển khai: một lớp giữ **toàn bộ** hình học khung (vỏ, hai cột
phím, viền trên/dưới, và chỗ màn hình nằm bên trong) — theo luật số 4, một vùng hình học chỉ được
định nghĩa ở đúng một nơi, và `TabletScreen` phải **hỏi** lớp đó chứ không tự tính lại.

### 2026-08-18 (phiên sau) — Kiểm SBW 0.8.9.1: có, nhưng là alpha, và cố ý không lấy

**Người dùng báo tác giả SBW vừa đăng 0.8.9.1.** Kiểm bằng đường rẻ nhất trước (đọc trang, không
build): CurseForge có `superbwarfare-0.8.9.1-snapshot-mc1.20.1-9bd3b3853-all.jar`, **file id 8644874**,
ngày 14/08/2026, **loại Alpha/snapshot**. Bản đang ghim là `8104849` (0.8.9-final, 17/05/2026, Release).

**Người dùng chốt: GIỮ 0.8.9-final.** Lý do đã ghi thẳng vào `gradle.properties` cạnh chính dòng ghim
— theo bài học 19, người định đổi số sẽ tới **chỗ số được dùng**, không tới nhật ký.
- Mod này phụ thuộc **duy nhất** SBW ⇒ mọi lỗi của họ thành lỗi của mình.
- File snapshot **có thể bị thay dưới cùng một id** bất cứ lúc nào.
- **Không có changelog để đọc** — trang releases trên GitHub trống ⇒ không biết nó đổi gì, mà đoán thì
  trái quy ước của dự án.

**Cách kiểm ĐÚNG nếu sau này nâng** (đã ghi trong `gradle.properties`): không đọc changelog mà trỏ
ghim sang `8644874`, để Gradle tải, rồi `javap` trên jar thật bốn chỗ addon chạm vào — `setTarget`,
`modifyGunData`, `canRequestReload_unsynced`, các accessor dữ liệu pháo — và so với những gì tài liệu
này ghi. **Cả bốn đều đã từng đổi.**

### 2026-08-18/19 — Vỏ máy: hai entry mô tả thiết kế đã bị XOÁ

Hai entry cũ ở chỗ này giải thích dài dòng vì sao vỏ máy có gờ nổi, hốc phím, khối góc và biển tên
như nó đã có. Người dùng yêu cầu xoá (2026-08-19), và lý do đáng giữ lại hơn chính nội dung đã xoá:

**Một đoạn văn giải thích cho một quyết định sai vẫn nghe hợp lý, nên nó sống lâu hơn quyết định
đó.** Mỗi phiên sau đọc những đoạn ấy rồi sửa vỏ **theo lời văn thay vì theo ảnh mẫu**, nên bốn vòng
liên tiếp đều "gần giống" mà không lần nào giống. Đỉnh điểm: đoạn văn lập luận rằng hốc phím chiếm
chỗ của mặt vỏ, và hốc bị **xoá** — trong khi ảnh mẫu **có** hốc, chỉ là hốc phải tối, còn code cũ tô
nó sáng hơn cả đáy gradient mặt vỏ.

**Còn lại đúng ba điều, và không điều nào mô tả hình dáng:**
1. **Ảnh mẫu là nguồn sự thật duy nhất cho vỏ máy.** Tài liệu không mô tả vỏ.
2. **`./gradlew caseView` vẽ vỏ bằng chính code sản phẩm, ngoài Minecraft** — xem mục bàn giao. Đây
   là thứ duy nhất đáng tin để phán, trừ cỡ chữ (harness không có font).
3. **Đèn LED nằm trên viền kính, không phải nắp phím sáng lên.** Giữ vì nó gỡ một mâu thuẫn có thật:
   phím vật lý không đổi màu được, mà trạng thái vẫn phải báo. Đèn là linh kiện riêng nên cả hai đều
   đúng. *(Đây là một ràng buộc về cơ chế, không phải một mô tả hình dáng.)*

Ba quyết định kỹ thuật của đợt đó **không liên quan tới hình dáng** nên giữ nguyên: độ sáng áp ở khâu
vẽ còn bộ lọc áp ở khâu quyết định màu (đơn sắc là **cộng ba kênh**, mà modulator chỉ **nhân** từng
kênh — nó tô màu được, không khử màu được, nên bộ lọc buộc phải nằm ở `TerrainMips.groundColour` và
đổi nó thì phải vứt mọi thứ đã tô); `stamp()` gộp `generation` và `paint` thành **một** số vì mọi lỗi
lớp này đều cùng hình dạng là một việc mang dấu này bị kiểm bằng dấu kia; và bấm Filter mà bản đồ
không đổi là do lối tắt *"đất không đổi thì ô này vẫn đúng"* — lối tắt ấy **không sai**, nó trả lời
một câu hỏi **về mặt đất**, mà đổi bảng màu không phải là đất đổi, nên phải có bộ đếm riêng.


### 2026-08-19 — Dựng lại vỏ từ ảnh mẫu, và một cái seam bị vi phạm

Người dùng yêu cầu xoá mọi mô tả thiết kế vỏ khỏi tài liệu (xem entry trên) rồi dựng lại từ ảnh. Ghi
lại **cơ chế**, không ghi hình dáng — hình dáng nằm ở ảnh mẫu.

**Ba cơ chế mới, mỗi cái sửa một loại sai:**

1. **Nắp phím tự vẽ cái hốc nó đứng trong.** Trước đây vỏ vẽ hốc, phím vẽ nắp — hai chủ cho một chỗ,
   nên hốc và nắp trôi khỏi nhau và có phiên hốc bị xoá mà nắp thì không biết. Giờ cả hai thuộc
   `UiButton`, cùng một hàm, không tách được.

2. **Chữ trên nắp phím phóng to theo nắp.** Vỏ co giãn theo cửa sổ, font Minecraft cao cố định 8px —
   nên cửa sổ càng lớn chữ càng chiếm phần nhỏ của nắp. Phóng bằng **ma trận**, và chỉ **số nguyên
   lần**: font là bitmap vẽ cho một lưới, phóng 2,4× thì nét rơi vào phần lẻ của điểm ảnh và ra mờ.

3. **Quyết định phóng bao nhiêu lần nằm DƯỚI seam `Paint`, không phải trên nó.** Lần đầu mình tính
   ngay trong `UiButton` — cần đo bề rộng chữ, mà đo thì cần font, mà font **không tồn tại ngoài
   game** ⇒ `caseView` ném NPE ngay. Bài học lặp lại của dự án ở dạng mới: **thứ đi qua seam phải là
   câu hỏi, không phải câu trả lời.** Người gọi nói *"to hết cỡ, cỡ một phần ba hộp này, chừa ngần
   này ở hai bên"*; phía game đo bằng font thật, phía harness ước lượng 6px/ký tự. Đúng cùng cách
   `label` thường vẫn làm — chỉ là mình quên áp cho biến thể mới.

**Đã kiểm:** `caseView`, `mapCheck` → `map checks passed`, `build` sạch. **Cỡ chữ thì harness không
kiểm được** (không có font) — chỉ trong game mới biết.

### 2026-08-19 — Đo ảnh mẫu bằng pixel thật, không nhìn bằng mắt nữa

Ba lần liền chỉnh vỏ theo mắt đều sai — mỗi lần sai một kiểu khác nhau, kể cả sau khi đã xoá hết mô
tả cũ khỏi tài liệu. Đó là bằng chứng đủ để đổi phương pháp: **đo ảnh mẫu bằng code**, không đoán.

**Cách làm:** viết một công cụ Java rời (`javax.imageio`, không phải thứ đóng gói với mod) đọc
`docs/ChatGPT Image 05_00_26 19 thg 8, 2026.png` (1672×941), quét từng điểm ảnh tìm chỗ màu đổi đột
ngột, và cắt-phóng-kẻ-lưới từng vùng (mỗi 10px một đường lưới, có ghi số) để nhìn trực tiếp đúng toạ
độ. Không có Pillow/ImageMagick trên máy nên tự viết bằng thứ chắc chắn có sẵn (JDK).

**Phát hiện lớn nhất, và là lý do ba vòng trước sai:** trên ảnh mẫu, **khe giữa hai phím một hàng
(~100px) RỘNG HƠN chính cái phím (~70px)**. Ba bản trước đều làm ngược — phím gần như kín cả dải,
khe chỉ còn một sợi. Mắt không giỏi so sánh "khe có rộng hơn phím không", mà đó đúng là thứ tấm ảnh
này xoay quanh.

**Số đo chốt (tỉ lệ theo thân vỏ ~1658×927):**
- `SIDE_F=0.108, ABOVE_F=0.147, BELOW_F=0.114` — gần như không đổi so với bản trước, đúng vô tình.
- `CORNER_W_F=0.060, CORNER_H_F=0.100` — gần vuông, đúng như comment cũ đã đoán.
- `ROW_KEY_W_F=0.038, ROW_STEP_F=0.090` — **phím chỉ chiếm ~42% một bước**, phần còn lại là khe.
- `COL_KEY_W_F=0.090, COL_KEY_H_F=0.085` — phím cạnh trái/phải **gần vuông** trên ảnh (không dẹt
  như số cũ 0.049/0.073 từng vẽ).
- `RADIUS_F=0.025` — bo góc thân vỏ lớn hơn số cũ (0.012) khá nhiều.
- Bo góc một nắp phím: `min(w,h)/10`, gờ dày `min(w,h)/28` — mỏng và gần vuông, không phải viên
  thuốc. Đo trực tiếp trên ảnh: góc phím chỉ hơi vát.

**Bỏ hẳn vòng hốc quanh phím.** Ảnh mẫu không có một vòng tối riêng lồi ra ngoài nắp phím — phím gần
như đứng thẳng trên mặt vỏ, chỉ có viền mỏng + bóng đổ một điểm ảnh. Vòng hốc từng được thêm rồi xoá
rồi thêm lại hai lần trước, luôn với lý do "để mặt vỏ lộ ra giữa các phím" — nhưng thứ cần sửa không
phải là hốc, mà là **khoảng cách giữa hai phím**. Bỏ hốc + nới khe mới là cặp sửa đúng.

**Chưa làm, biết rõ vì sao:** ảnh mẫu có hai phím "đầu dòng" (biểu tượng ngắm bên trái, biểu tượng
mặt trời bên phải) **rộng gần gấp đôi** các phím chữ ở giữa — một kiểu bố cục "hai đầu nhấn mạnh".
Mod hiện dùng bề rộng đồng nhất cho cả 10 phím một hàng vì bố cục chức năng của ta khác ảnh mẫu (ta
có 2 phím độ sáng riêng ở cuối hàng, ảnh mẫu chỉ có 1 phím mặt trời gộp cả hai). Có thể làm sau nếu
cần, đổi `rowKeyW()` nhận thêm `index` và trả về số lớn hơn ở vị trí 0 và cuối.

**Cũng biết rõ:** ảnh là ảnh AI sinh, không phải bản vẽ kỹ thuật — đo hai góc đối xứng ra hai số khác
nhau vài phần trăm (vd: `BELOW` đo được nhỏ hơn `ABOVE` rõ rệt, hai khối góc trên/dưới không đúng
cùng kích thước). Không cố ép các số này khớp nhau tuyệt đối — dùng số đo trực tiếp ở từng chỗ.

**Đã kiểm:** `caseView`, `mapCheck` → `map checks passed`, `build` sạch.

### 2026-08-19 — Sửa bố cục: hai phím đầu-cuối hàng phải rộng hơn, và một artifact "+"

Người dùng: "vẫn khá tệ" rồi "không đúng như bản mẫu, sai bố cục và sai chi tiết". Soát lại kỹ hai
điều đã đo nhưng **chưa đưa vào code** từ lượt đo pixel trước:

**1. Bốn phím đầu/cuối một hàng phải rộng gần gấp đôi phím giữa.** Đã đo tỉ lệ này từ trước (crosshair
~135px so với SA/WPN ~70px) nhưng chưa code. Giờ thêm `ROW_KEY_BOOKEND_W_F=0.073` (so với
`ROW_KEY_W_F=0.038`), áp cho ba phím: GRID (trên, đầu), BRIGHT-up (trên, cuối), NIGHT (dưới, đầu).
**POWER (dưới, cuối) cố tình giữ cỡ thường** — ảnh mẫu phân biệt nó bằng màu đỏ, không phải bằng cỡ.

`rowKey()`/`ledFor()` có thêm overload nhận cờ `wide`: lưới vị trí (bước đều) vẫn tính theo bề rộng
THƯỜNG, chỉ riêng phím được đánh dấu wide mới vẽ to hơn và **tự canh giữa trong đúng ô của nó** — nên
các phím lân cận không bị đẩy dạt.

🔴 **Bẫy đã dính:** sửa xong `TabletFrame`/`TabletScreen` rồi chạy `caseView`, ảnh KHÔNG đổi gì — vì
`CaseView` tự dựng phím bằng **reflection gọi đích danh chữ ký `rowKey(boolean,int)`** (bản 2 tham
số cũ), không hề biết bản 3 tham số mới tồn tại. Không crash, không báo lỗi — chỉ lặng lẽ vẽ sai.
Đây đúng bài học đã ghi nhiều lần trong tài liệu này (đổi chữ ký thì phải chạy VÀ ĐỌC `mapCheck`), áp
dụng luôn cho `caseView`. Đã sửa `CaseView.keys()` gọi đúng bản 3 tham số.

**2. Artifact hình dấu cộng gần phím GRID.** Không phải bug riêng — là đèn LED (nhỏ) và vạch chỉ báo
đứng quá gần nhau ở đúng phím đó. Tick giờ canh theo đúng bề rộng thực (rộng hay thường) của từng
phím thay vì luôn dùng bề rộng thường, nên với phím rộng nó không còn lệch tâm.

**Đã kiểm:** `caseView` (ảnh giờ có thấy rõ 3 phím rộng hơn hẳn phím giữa), `mapCheck` →
`map checks passed`, `build` sạch.

### 2026-08-19 — Bộ chuyển ảnh → mã Java (`./gradlew convert`)

Sau nhiều vòng đo tay vẫn không khớp ảnh mẫu, người dùng chốt hướng khác: **lấy hình trực tiếp từ
ảnh** thay vì đo lại rồi vẽ tay. Không phải texture — converter đọc ảnh, pixel hoá, rồi **sinh ra
mã Java gọi `p.fill()`**, vẫn đi qua đúng seam `Paint` đang có.

**Bốn bước, và THỨ TỰ chính là thiết kế** (mỗi bước vứt đi một loại thông tin khác nhau):
1. **Lượng tử màu ở độ phân giải gốc** (median cut, palette thích ứng) — quyết định thiết kế gồm
   những màu nào khi mọi điểm ảnh còn đủ để bỏ phiếu.
2. **Giảm về lưới logic bằng MÀU ÁP ĐẢO, không phải trung bình.** 🔴 Đây là dòng quan trọng nhất cả
   công cụ. Trung bình của hai màu-trong-palette ra màu **thứ ba không có trong palette** — mọi biên
   giữa hai mảng phẳng lại thành dốc mờ, tức là bước 1 vừa khử mờ xong thì bước 2 dựng lại. Lấy màu
   áp đảo buộc mỗi ô chọn hẳn một bên, biên vẫn là biên và nằm đúng mép ô để bước 4 cắt theo.
3. **Dọn nhiễu theo tương phản.** Không có cách "thông minh" nào phân biệt đốm nhiễu với lỗ vít —
   thứ đo được là **tương phản với vùng xung quanh**, nên vùng nhỏ chỉ bị hoà tan khi nó *cũng* gần
   màu xung quanh. Hai ngưỡng để trên slider vì đây là phán đoán về một tấm ảnh cụ thể.
4. **Gộp ô thành hình chữ nhật** (run ngang rồi nuốt xuống dưới), có kiểm **tái tạo đúng từng ô**.

**Ba lỗi thật chỉ lộ ra khi CHẠY và BIÊN DỊCH, không phải khi đọc code:**

🔴 **`code too large`.** 2.846 lệnh `fill()` trong một lambda **vượt giới hạn 64KB bytecode của một
method Java** — `javac` từ chối thẳng cả file. Đây là giới hạn của định dạng class, không phải của
việc vẽ; chia ra nhiều method (`part0`, `part1`… 500 hình mỗi method) là chạy được ngay. **Bài học:
sinh code mà không bao giờ build nó thì không phải là đã kiểm.**

🔴 **Vùng bản đồ nuốt 9/10 ngân sách hình.** Lần chạy đầu: 9.834 hình, phần lớn là ảnh vệ tinh vỡ vụn
— mà runtime thì map renderer vẽ đè lên toàn bộ vùng đó, không hình nào trong số ấy được nhìn thấy.
Thêm chức năng **làm phẳng vùng màn hình** (cho theo % nên đổi cỡ ảnh gốc vẫn đúng): **9.834 → 2.846
hình.** Để so sánh, vỏ vẽ tay hiện tại tốn ~9.156 hình — tức bản sinh tự động **rẻ hơn ba lần**.

🔴 **Biên chung phải đọc từ MỘT mảng.** Mã sinh ra dựng sẵn `cx[]`/`cy[]` rồi mọi hình đọc từ đó,
thay vì mỗi hình tự nhân-chia-làm tròn. Đây đúng luật số 4 của dự án (một vùng hình học chỉ định
nghĩa ở một nơi) áp cho code sinh: hai hình cạnh nhau tự tính riêng sẽ lệch nhau 1px và để lại khe.
Đã chứng minh bằng `generatedView`: dựng ở **1803×1014** (khác hẳn ảnh gốc 1536×1024) ra **0 điểm ảnh
hở**.

**Ba lệnh:**
- `./gradlew convert` — mở UI (4 slider, preview 4 bước: gốc → lưới logic → hình chữ nhật → mã Java,
  xuất `.java`/`.png`). Thêm `-Pimage=docs/sample.png` để nạp sẵn.
- `./gradlew convertCheck` — chạy headless, khẳng định hình chữ nhật tái tạo lưới **chính xác** (bắt
  cả ô chồng lẫn ô hở — hai lỗi mà preview không bao giờ cho thấy).
- `./gradlew generatedView` — vẽ class đã sinh **qua đúng `Paint` thật**, nền magenta để chỗ nào
  không được phủ thì lộ ra ngay.

**Chưa làm, và biết rõ vì sao:** chưa gắn `ConvertedCase` vào `TabletScreen`. Đèn LED và nhãn chữ
trên phím vẫn phải là code riêng vẽ đè — ảnh mẫu chỉ là **một khoảnh khắc tĩnh**, sinh mã từ nó sẽ
đóng băng đúng màu trong ảnh. Người dùng đã chốt: nút bấm **không** còn đổi màu theo trạng thái nữa,
chỉ đèn LED (một đường kẻ dọc) mang tín hiệu — nên phần tĩnh giờ bao được cả vỏ lẫn nút, chỉ chừa
LED. Còn treo: LED nằm trên viền kính hay cạnh nắp phím, và dài bằng chiều cao phím hay ngắn hơn.

### 2026-08-19 — Vỏ máy chuyển sang ảnh sinh: `ConvertedCase` được gắn vào thật

Người dùng chốt bản xuất ưng ý (`ConvertedCase.java`, lưới **360×203**, 28 màu, **5.190 hình**) và
yêu cầu triển khai. Đã gắn xong, build sạch.

**Đổi vai trò `TabletFrame`: giờ nó KHÔNG vẽ gì nữa.** Nó chỉ còn giữ **hình học** — cái gì nằm ở
đâu — để phần bấm được và phần được vẽ khớp nhau. `draw()` chỉ còn một dòng gọi `ConvertedCase.draw`.
🔴 **Hệ quả phải nhớ:** mọi con số trong `TabletFrame` giờ **đo từ chính ảnh sinh ra**. Nếu
`ConvertedCase` được sinh lại từ ảnh khác, **phải đo lại toàn bộ** — vùng bấm trôi khỏi phím được vẽ
là lỗi *không nhìn thấy được*, chỉ lộ khi bấm mà không có gì xảy ra.

**Đo bằng detector, không bằng mắt** (connected-component "không phải mặt vỏ" trong từng dải phím +
quét từ tâm ra cho ô kính). Kết quả trên ảnh 1803×1014: ô kính x 215..1586 / y 139..828; hàng trên 10
phím, phím đầu x=290, bước 126.3, 80×65, y=49; hàng dưới y=879 cao 70; cạnh 6 phím, x=85 rộng 90, y
đầu 229, bước 90. Bảng này chép vào đầu `TabletFrame` để lần sau còn đối chiếu.

**`SHELL_FRACTION` 0.94 → 1.0.** Ảnh **tự mang nền phía ngoài thân máy**, nên thụt vào lần nữa sẽ
hiện lề hai lần và thu nhỏ panel vô ích.

**Phím cứng không tự vẽ nắp nữa** — ảnh đã vẽ rồi. `UiButton` nhánh `hard` giờ chỉ còn **nhãn + đèn
LED**, đúng yêu cầu người dùng: nút bấm bỏ hẳn cơ chế đổi màu theo trạng thái, **chỉ đèn LED (một
đường kẻ dọc) mang tín hiệu**. LED đặt trong khe giữa nắp phím và ô kính, dựng đứng ở cả bốn cạnh.

**Hai lỗi thật gặp khi gắn, cả hai chỉ lộ khi nhìn ảnh dựng:**

🔴 **Nhãn trong ảnh bị bể thành vệt trắng vô nghĩa.** Ở lưới 360×203 chữ không sống sót. Nhãn thật
(BTY/TGT/F13…) phải do code vẽ đè — nhưng vẽ thẳng lên thì vệt trắng cũ lòi ra quanh chữ. Thêm
`clearLabelBand`: tô lại một dải bằng **màu mặt nắp** trước khi viết nhãn.

🔴 **Nút nguồn bị tô xám đè lên mặt đỏ.** Hệ quả trực tiếp của cái trên: `clearLabelBand` ban đầu
dùng **một** màu cho mọi phím, nên cái nắp đỏ bị phủ xám và chỉ còn viền đỏ lòi ra. Sửa: `capFace()`
trả màu theo loại phím (`#42473F` thường, `#D43835` cho power/danger), lấy mẫu từ chính ảnh.

**Đã kiểm:** `caseView` (vùng bấm khớp phím vẽ), `convertCheck`, `generatedView` → **0 điểm ảnh hở ở
1803×1014**, `mapCheck` → `map checks passed`, `build` sạch. **Chưa ai mở tablet trong game.**

**🕒 Còn treo, cần người dùng quyết:** phím cứng giờ **không có phản hồi hover nào cả**. Yêu cầu là bỏ
"sáng đèn khi kích hoạt", nhưng *hover* khác *đang bật* — hiện di chuột lên phím không thấy gì, chỉ
còn tooltip. Nếu thấy khó dùng thì cần một tín hiệu hover riêng (viền mảnh, hoặc chính đèn LED sáng
mờ khi trỏ vào).

### 2026-08-19 — Quay lại vẽ bằng code: ảnh sinh không phân biệt được nền với vỏ

Người dùng chạy thử `ConvertedCase` trong game thật, gửi ảnh chụp màn hình có khoanh đỏ 4 góc, và
chỉ ra đúng lỗi gốc: **ảnh không có biên** — không cách nào để công cụ (hay code) phân biệt "đây là
vỏ máy" với "đây là nền ảnh phía sau vỏ máy" khi cả hai chỉ là điểm ảnh. `ConvertedCase` vẽ luôn cả
phần nền mờ quanh thiết bị trong ảnh gốc, nên vỏ trông như tan vào một lớp sương không có cạnh.

**Quyết định: bỏ `ConvertedCase` làm nguồn vẽ chính, quay lại vẽ vỏ bằng code (`fill()`/`rounded()`/
`roundedShaded()`) như trước converter — nhưng lấy màu từ `docs/sample.png` bằng công cụ đo màu,
không đoán bằng mắt.** Class `ConvertedCase` vẫn còn trong repo (không xoá), chỉ không còn ai gọi.
Bài học giữ lại: **vẽ bằng code không có khái niệm "nền" vì nó chỉ vẽ đúng những gì được bảo vẽ** —
đây là ưu điểm cấu trúc mà một bức ảnh quét-thành-hình-chữ-nhật không có được.

**Sáu điều người dùng yêu cầu, đã làm:**

1. **Góc ô kính bo nhẹ** thay vì vuông nhọn — thêm `RADIUS_F=0.014`, áp cho cả khối vỏ lẫn khối
   khoét màn hình.
2. **Màu khung máy lấy từ `sample.png`** — đo bằng histogram màu trên một vùng mặt vỏ thật
   (`#333333`) và một vùng mặt phím thật (`#474747`), không phải màu tự chọn.
3. **Không còn vẽ nền** — hệ quả trực tiếp của việc quay lại vẽ bằng code (xem trên).
4. **Mọi phím cùng một hình dáng, khấc ngăn cách rõ.** Nắp phím giờ là MỘT khối duy nhất dùng chung
   cho mọi loại (thường/rộng/danger/power) — cùng bán kính, cùng độ dày viền, cùng đường viền lõm
   bên trong. Thêm `grooves()`: một viên thuốc xám sáng đứng giữa mỗi cặp phím liền kề (đo được từ
   `sample.png` — panel gốc có chi tiết này, phiên trước bỏ sót). Tính từ **hình chữ nhật thật** của
   hai phím lân cận (không phải bước lưới danh nghĩa), nên phím rộng (GRID/BRIGHT/NIGHT) vẫn có khấc
   nằm đúng khe của nó.
5. **Đèn LED chỉ còn một đường kẻ dọc duy nhất** — bỏ hẳn quầng sáng loang (halo) quanh đèn, bỏ luôn
   việc đổi hướng ngang/dọc theo cạnh (trước đây đèn cạnh trái/phải nằm ngang, giờ **luôn đứng**, một
   hình dạng ở mọi vị trí). `ABOVE_F = BELOW_F = 0.150` — hai viền trên/dưới bằng nhau tuyệt đối;
   người dùng đã nói rõ **tạm bỏ qua tỉ lệ bản đồ thật** để ưu tiên sửa khung trước.
6. **Nhãn phím canh giữa lại** — bỏ các offset `y+1`/`y+3` thủ công còn sót từ bản trước, `capLabel`
   giờ nhận đúng `(y, h)` của cả nắp, để `Ui.textCentredScaled` tự canh.

**Đã kiểm:** `caseView` — khấc giữa các phím thấy rõ, góc bo mềm, nút nguồn đỏ viền rõ, đèn LED một
nét thẳng (ảnh mẫu có một đèn xanh đang sáng, canh đúng giữa phím TGT). `mapCheck` →
`map checks passed`. `build` sạch.

**Còn treo:** vỏ giờ **không có phản hồi hover** (đã ghi ở entry trước, chưa giải quyết). Tỉ lệ trên/
dưới màn hình đang **cố tình sai lệch thật** so với ảnh mẫu (0.150/0.150 thay vì 0.137/0.182 đo
được) — người dùng đã đồng ý đánh đổi tạm thời, cần quay lại chỉnh nếu muốn khớp đúng ảnh mẫu sau
khi khung đã ổn.

### 2026-08-19 (tiếp) — Thử lại texture blit, rồi bỏ: ảnh chụp mang theo nhiễu không hợp Minecraft

Vòng này thử hướng thứ ba: cắt riêng phần vỏ (không map, không chữ trên phím) từ `sample.png` — dùng
connected-component để cô lập từng nắp phím thành "đảo" riêng, tô phẳng nội thất mỗi đảo bằng màu
trung bình của chính nó (xoá sạch chữ/icon không lem), cắt trong suốt đúng vùng màn hình bằng bo góc
đo từ ảnh. Kết quả dùng làm texture PNG, `Paint` có thêm `blitFrame()`, `TabletFrame.draw()` rút gọn
còn một dòng `p.blitFrame(...)`. `UiButton` bỏ phần vẽ nắp phím thủ công vì texture đã có sẵn. Đo lại
toàn bộ `ROW_*`/`COL_*` bằng pixel thật (bước phím `ROW_STEP_F` cũ 0.0702 sai ~10% so với đo được
0.0776, gây trôi dần đến gần 1 phím ở cuối hàng — đúng lỗi người dùng báo "lệch 1 nút").

**Test trong game xong, người dùng chê "chất lượng khá tệ".** Lý do đúng như bài học đã ghi ở entry
trước: ảnh chụp/render AI có cạnh chéo, đường tròn đều bị răng cưa hoá khi lấy đúng pixel — trong khi
code vẽ (`fill()` hình chữ nhật theo công thức) luôn cho cạnh hoàn hảo dù phóng to cỡ nào. Texture
mang theo đúng cái nhiễu đó, không sửa được bằng đổi bộ lọc GPU.

🔴 **Bài học nhắc lại, lần thứ hai:** vẽ bằng code không phải chỉ để "né vụ nền không có biên" (bài
học lần trước) — mà còn để **có cạnh sắc nét đúng ngôn ngữ render của Minecraft**, việc một texture
PNG không bao giờ làm được nếu nguồn là ảnh chụp/render thật. Ảnh chỉ nên là **số liệu để đo**, không
phải nội dung đem dán thẳng vào game.

**Phát hiện khi đo góc vỏ để vẽ lại bằng code:** khối bumper góc **không phải hình chữ nhật bo góc
đơn giản** như bản `corners()`/`block()` cũ từng vẽ — nó có bậc thang cắt vào, lỗ bolt chính lệch tâm,
kèm 1 bolt phụ nhỏ tách biệt. Quét pixel theo hàng/cột để suy ra hình dạng dễ đọc sai (nhầm viền khối
bumper với viền phím kế bên, tưởng lầm bậc thang là biên khối trong khi thực ra là góc bo của ô kính
chồng lên vùng crop) — mất vài vòng mới nhận ra.

**Phương pháp tạm thời đã thống nhất, dùng làm chuẩn cho toàn bộ khung từ đây:**

1. **Dò biên (Moore-neighbour contour tracing) cho từng chi tiết có viền tối rõ** (bolt, khấc, viền
   phím) — đáng tin hơn quét hàng/cột thủ công vì bám đúng 1 vùng khép kín thay vì lẫn giữa các vùng
   lân cận cùng tông màu.
2. **Áp hình học biết trước cho chi tiết có hình dạng chuẩn** — bolt là lỗ ốc nên chắc chắn tròn: fit
   hình tròn kiểu bình phương tối thiểu (Kåsa) lên contour đã dò, bỏ qua răng cưa pixel-hoá, dùng tâm
   + bán kính fit được để vẽ `disc` thật trong code (không phải bát giác pixel).
3. **Làm phẳng màu bề mặt ở cường độ vừa phải** (quantize bước ~10 + một lượt khử nhiễu theo số đông
   lân cận 8 ô) — không phẳng tuyệt đối về 1 màu (mất hết bóng đổ, khối bumper tan biến vào vỏ máy),
   cũng không giữ nguyên (nhiễu quá lớn: 1 góc 130×150px ra 4303 hình chữ nhật nếu không khử nhiễu, so
   với 1253 sau khi khử — biên độ mất chi tiết chấp nhận được, bumper vẫn đọc được là khối tối hơn vỏ).
4. **Phần khối lớn không có viền tách biệt màu** (bumper liền màu với vỏ máy xung quanh, ranh giới chỉ
   lộ ra nhờ đường bevel sáng chứ không phải viền kín) — dò biên tự động không dùng được, cần người
   dùng phác thảo ranh giới hoặc xác nhận thủ công thay vì thuật toán đoán.

Đang áp dụng bốn bước trên cho toàn bộ 4 góc (dùng đối xứng gương từ góc trên-trái để suy ra 3 góc
còn lại thay vì đo lại từng góc), rồi ghép vào `TabletFrame.corners()`/`bolt()` bằng code thật.

### 2026-08-20 — Vector hoá góc bằng phần mềm ngoài, rồi bỏ luôn cả hướng texture: quay về code phẳng

Tiếp tục hướng vector hoá góc máy (contour trace + RDP simplify) — áp dụng thành công cho 1 góc,
tích hợp vào `TabletFrame.CORNER_POLY` (19 đỉnh, scan-fill theo hàng thay vì bảng rect khổng lồ).
Người dùng sau đó tự vector hoá TOÀN BỘ khung bằng Illustrator Image Trace, gửi file
`tablet_vectored.svg` (298 path). Phát hiện quan trọng khi phân tích: **các path trong SVG chồng
lớp lên nhau** (không phải vùng phẳng độc lập) — lấy riêng 1 path ra không cho hình đúng, phải
composite đúng thứ tự mới ra hình sạch. Sau khi composite, hình khớp với bản tự dựng trước đó.

🔴 **Người dùng dừng hẳn hướng UI phức tạp giữa chừng:** "tôi thấy tôi làm texture này tệ quá...
quay lại thiết kế UI đơn giản bằng java". Trước đó người dùng đã tự vẽ tay 1 texture
(`tablet_frame.png`, Blockbench, 1536×1024) và mình đã tích hợp xong (`blitFrame()`,
`UiButton` bỏ vẽ cap/label vì texture có sẵn, đo lại `COL_INSET_F`/`COL_KEY_W_F`/LED theo texture
mới) — **toàn bộ phần này đã bị revert** theo yêu cầu dừng lại. Không giữ texture nào cả.

**Trạng thái hiện tại (chuẩn, không texture):**
- `TabletFrame.draw()`: hình học phẳng thuần — 1 `rounded()` cho vỏ, 1 `rounded()` cho well màn
  hình đen. Không góc bumper, không bolt, không texture, không `blitFrame()` (đã xoá khỏi `Paint`
  hoàn toàn — không còn dùng).
- **Toàn bộ 32 phím dùng chung 1 kích thước vuông** — `KEY_SIZE_F=0.06` (đo theo `downShell` để
  đảm bảo vuông thật dù shell không vuông), không còn khái niệm "bookend rộng hơn". Các hàm
  `keyW()/keyH()/rowKeyW()/rowKeyWide()/rowKeyH()/rowKeyBottomH()` đều trả về `keySize()`.
- `UiButton` nhánh `hard`: cap phẳng + viền + 2 lớp bóng đổ offset mờ dần (`0x30000000`/
  `0x40000000` tại +1/+2px) + label. Không mô phỏng vật liệu, không đổi màu theo trạng thái —
  chỉ LED (`lamp()`) đổi màu, đúng quyết định cũ.
- Màu vỏ đổi sang xám đen trung tính `#222224` (bớt ánh xanh so với `#2A2A2E` cũ).
- Viền khung giảm mỏng theo yêu cầu: `SIDE_F` 0.1195→0.07, `ABOVE_F`/`BELOW_F` 0.150→0.10 (và
  `ROW_TOP_Y_F`/`ROW_BOTTOM_Y_F` phải chỉnh theo để hàng phím không đè lên màn hình mới rộng hơn).
- Đã test co giãn ở tỷ lệ 4:3 (1600×1200) qua `caseView` — bố cục không vỡ, phím vẫn vuông.
- `CaseView.java` (harness) đã cập nhật để hiện **nhãn thật** (BTY/TGT/CFF/...) thay vì "ABC" —
  dùng để xem trước bố cục nhanh mà không cần build game.

**Đã tra cứu (không áp dụng ngay)**: 2 mẫu tham chiếu thật —
[RSD-G (Leonardo DRS)](https://www.leonardodrs.com/news/press-releases/leonardo-drs-launches-new-ai-enabled-rugged-smart-displays/)
15.6" FHD, và
[MRT104 II (Leonardo DRS)](https://www.leonardodrs.com/what-we-do/products-and-services/multi-function-rugged-tablet-mrt104/)
10.4" **XGA 4:3**. Người dùng gửi ảnh thật MRT104 II — bố cục hoàn toàn khác (chỉ 2 cột trái/phải,
~17 phím, có phím gộp BRT+/VOL+, không hàng trên/dưới) so với bố cục 32-phím-4-cạnh hiện tại
(dựa trên ảnh RSD hư cấu/AI-render ban đầu). Chưa quyết định đổi theo.

**Quyết định cuối phiên: giao phần UI cho AI/người khác chuyên thiết kế.** Người dùng nhận ra nên
tách việc "code logic" (agent này) khỏi việc "thiết kế hình ảnh" (nên giao chuyên gia khác). Đã
viết brief đầy đủ tại [`docs/ui-design-brief.md`](ui-design-brief.md) — liệt kê đúng 32 phím/chức
năng, 2 ảnh tham chiếu, style hiện tại, và **2 hình thức giao nộp chấp nhận được**: (A) texture
PNG kèm bounding-box chính xác từng phím, hoặc (B) style spec bằng số (màu/bán kính/shadow) để
code tự vẽ lại — tránh lặp lại lỗi "đo lại toạ độ từ ảnh" đã ngốn rất nhiều thời gian phiên này.

🕒 **Bắt đầu phiên sau từ đâu:**
1. Nếu người dùng mang về kết quả từ agent thiết kế khác (texture hoặc style spec) → tích hợp
   theo đúng 2 hình thức đã định nghĩa trong `ui-design-brief.md`. Nếu là texture không kèm
   bounding-box, **hỏi lại trước khi đo bằng tay** — đo pixel thủ công từ ảnh là nguồn tốn thời
   gian nhất phiên này (nhiều vòng đo sai/đo lại).
2. Nếu chưa có gì mới → trạng thái code hiện tại (`TabletFrame`/`UiButton` phẳng, đơn giản) đã
   build sạch, đã xem qua `caseView`, **nhưng chưa test trong game thật** (`runClient`) — nên làm
   trước khi thêm bất kỳ thay đổi hình ảnh nào khác.
3. Không tự ý quay lại hướng texture/vector hoá/procedural-phức-tạp nếu người dùng không yêu cầu
   rõ ràng — đã bị dừng giữa chừng 1 lần vì đi quá sâu vào chi tiết hình ảnh.
