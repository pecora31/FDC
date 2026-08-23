# 📐 BẢNG QUY TẮC THIẾT KẾ GIAO DIỆN & KHUNG VỎ TACTICAL TABLET
> **Mục tiêu**: Chuẩn hóa toàn bộ thông số hình học, chất liệu, màu sắc và cơ chế kết xuất của Tactical Tablet. Không được tự ý thay đổi hoặc bổ sung các chi tiết ngoài bảng quy tắc này.

---

## 1. 📐 KÍCH THƯỚC & BỐ CỤC KHUNG VỎ (CHASSIS GEOMETRY)
* **Tổng thể Texture Master**: $980 \times 630\text{ px}$.
* **Khu vực Màn hình MFD**: $800 \times 450\text{ px}$ (Góc trên-trái: $X = 90, Y = 90$).
* **Vát góc ngoài & Ốc Lục Giác**: 4 góc vát bậc 3 tầng ($45 \times 45\text{ px}$), ốc lục giác M4 ở tâm góc.
* **Gờ U-Collar (Trên / Dưới)**: $120 \le X \le 860$ bao quanh dãy phím trên và dưới.
* **Khung C-Bracket (Trái / Phải)**: $120 \le Y \le 510$ bao quanh dãy phím 2 cạnh sườn.
* **Gờ Vạch Ngăn Cách Giữa Các Nút (Tactical Divider Ribs)**:
  * **Các vị trí CÓ vạch ngăn**: Tất cả các cặp phím chức năng nội bộ đều có **1 gờ trụ đơn nổi** nằm chính giữa (giữa `SA-WPN`, `WPN-DEF`, `DEF-SYS`, `SYS-DRV`, `DRV-STR`, `STR-COM`, `COM-BMS`; giữa `F13`..`F20`; giữa `F2`..`F6`; giữa `F7`..`F12`).
  * **4 vị trí TUYỆT ĐỐI KHÔNG CÓ vạch ngăn (theo ảnh mẫu chụp thực tế)**:
    1. Giữa phím `F20` và phím `POWER` (Hàng dưới, góc phải).
    2. Giữa phím `[◆]` (NIGHT) và phím `F13` (Hàng dưới, góc trái).
    3. Giữa phím `[+]` (GRID) và phím `SA` (Hàng trên, góc trái).
    4. Giữa phím `BMS` và phím `[*]` (BRIGHT) (Hàng trên, góc phải).
    *(Kèm theo khoảng giữa `CFF` và `F2` ở cạnh trái do nằm ở rãnh bậc phân vùng của khung U-Collar/C-Bracket).*

---

## 2. 🧱 CẤU TRÚC PHÍM BẤM (TACTICAL KEYCAP SPECIFICATION)
* **Số lượng phím**: Đúng 32 phím ($44 \times 44\text{ px}$, Bo góc tròn $R = 7\text{ px}$).
  * Hàng trên (10 phím): Tâm $Y = 41$, $X = 148 + i \times 76$ ($i = 0..9$).
  * Cạnh trái (6 phím): Tâm $X = 39$, $Y = 155 + i \times 64$ ($i = 0..5$).
  * Cạnh phải (6 phím): Tâm $X = 941$, $Y = 155 + i \times 64$ ($i = 0..5$).
  * Hàng dưới (10 phím): Tâm $Y = 589$, $X = 148 + i \times 76$ ($i = 0..9$).
* **Hình học 3D (Chuẩn theo ảnh mẫu thực tế)**:
  1. **Khung Viền Nổi Cao (Raised Outer Rim/Bezel)**:
     * Dày 4px bao quanh ngoài cùng.
     * Cạnh trên đón sáng phản quang nhẹ (`#545A68`), cạnh dưới đổ bóng xuống chân (`#282B33`).
  2. **Lòng Phím Chìm Sâu Chứa Ký Tự (Deep Recessed Dish Floor)**:
     * Lòng phím $36 \times 36\text{ px}$ (Bo góc $R = 4\text{ px}$), thụt sâu bên trong khung viền.
     * **Bóng đổ chiều sâu (Inner Depth Shadow)**: Mép trong phía trên & trái đổ bóng tối (`#1A1D24`) xuống lòng phím.
     * Mép trong dưới có vệt sáng hắt (`#4A505E`).
* **Bảng màu chất liệu (Color Palette)**:
  * **Phím PBT Xám Tro (Mặc định)**:
    * Thân lòng phím: `#363B45`
    * Gờ viền trên: `#545A68`
    * Gờ viền dưới: `#282B33`
    * Chữ in laser: `#F0F4FA` (Trắng ngà sắc nét)
    * Viền ngoài cùng: `#14161C` (1px)
  * **Phím Đỏ Tác Chiến (CFF / Nguồn)**:
    * Thân lòng phím: `#5E1212`
    * Gờ viền trên: `#B02828`
    * Gờ viền dưới: `#3C0808`
    * Chữ in laser: `#F0F4FA`

---

## 3. 💡 HỆ THỐNG ĐÈN LED (OPTICAL LIGHT-PIPE CAPSULE SPECIFICATION)
* **Số lượng**: Đúng 32 đèn LED, đặt ngay dưới chân / cạnh mỗi nút bấm.
* **⚠️ Quy tắc phương của đèn LED**: **Chiều dài của đèn LED luôn VUÔNG GÓC với cạnh kề của phím bấm**:
  * **Hàng trên & Hàng dưới** (kề cạnh ngang của phím): Đèn LED nằm **DỌC ($4 \times 8\text{ px}$)** tại $Y = 76$ (trên) và $Y = 546$ (dưới).
  * **Cạnh trái & Cạnh phải** (kề cạnh đứng của phím): Đèn LED nằm **NGANG ($8 \times 4\text{ px}$)** tại $X = 76$ (trái) và $X = 896$ (phải).
* **Trạng thái TẮT (Unlit - Thấu Kính Quang Học Chìm)**:
  * Khe khoét chìm màu đen: `#0C0E12`.
  * Thân que dẫn quang bằng polycarbonate khói: `#222630`.
  * **Vệt phản quang ánh sáng trắng bạc cong ôm sát mép dưới thấu kính**: `#7A889E` $\to$ `#D8E2F0`.
* **Trạng thái BẬT (Lit - Phát Quang Nội Ống Quang Học)**:
  * Thân que dẫn quang phát sáng bão hòa màu (`#00FF66` cho Green, `#FF2233` cho Red, `#FFBB00` cho Amber).
  * **Lõi sợi dẫn quang trắng (Axial Core Filament)**: Vạch sáng trắng (`#FFFFFF`) chạy dọc theo trục tâm ống dẫn.
  * **Quầng sáng Phosphor Aura**: Tỏa êm dịu 2–3px quanh thân ống hình con nhộng (`0x14` $\to$ `0x30` $\to$ `0x60`), không cường điệu chói gắt.

---

## 4. ⚙️ QUY TRÌNH KẾT XUẤT & XEM TRƯỚC (WORKFLOW)
1. Mã nguồn duy nhất chi phối giao diện: `TabletChassisPaint.java`.
2. Kiểm tra trực tiếp trên Antigravity IDE thông qua tab ảnh `ArtilleryTacticalTablet/build/mapcheck/case.png`.
3. Mọi công cụ kiểm tra nội bộ (`run_case_live_preview.bat`, `UI/`) phải luôn được loại trừ khỏi Git (`.gitignore`).
