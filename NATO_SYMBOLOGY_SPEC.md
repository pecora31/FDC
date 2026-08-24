# 🎖️ TÀI LIỆU QUY CHUẨN KÝ HIỆU QUÂN SỰ NATO MIL-STD-2525D / APP-6D
**Dự án**: ASTRA Frontline C2 & Artillery Tactical Tablet  
**Mã tài liệu**: `ASTRA-SPEC-MIL2525D-REV4`  
**Trạng thái**: **QUY CHUẨN GỐC CỐ ĐỊNH (MASTER BASELINE)**

---

## I. NGUYÊN TẮC THIẾT KẾ CỐT LÕI (CORE DESIGN PRINCIPLES)

1. **Tính độc lập chuẩn mực**: Toàn bộ icon chiến thuật hiển thị trên bản đồ số, danh sách ORBAT, và radar trinh sát bắt buộc phải tuân theo cấu trúc hình học chuẩn quân sự **NATO MIL-STD-2525D**.
2. **Quy tắc phối màu tương phản cao (High-Contrast Rules)**:
   * **Quân đồng minh (Friendly)**: Khung viền Xanh Biển (`#3B82F6`), nền xanh sẫm trong suốt (`0xDD0E294B`), chữ trắng (`#E2E8F0`).
   * **Quân đối phương (Hostile / OPFOR)**: Khung viền Đỏ Thắm (`#EF4444`), nền đỏ sẫm trong suốt (`0xDD3B0D0D`), chữ đỏ sáng (`#FEE2E8`).
   * **Lực lượng trung lập (Neutral / CIV)**: Khung viền Xanh Lục (`#10B981`), nền lục sẫm (`0xDD0A2E1C`).
   * **Chưa xác định (Unknown / Pending)**: Khung viền Vàng Hổ Phách (`#F59E0B`), nền vàng hổ phách sẫm (`0xDD3D2808`).
3. **Bố cục định danh & cấp bậc (Labeling & Echelon Positions)**:
   * **Cấp bậc đơn vị (Echelon modifier)**: Luôn đặt **chính giữa phía trên đỉnh khung**.
   * **Tên/Ký hiệu đơn vị (Designation)**: Luôn đặt **ngay phía dưới chân khung**.
   * **Đơn vị cấp trên (Higher formation)**: Đặt ở góc phải hoặc bên cạnh.

---

## II. BẢNG QUY CHUẨN HÌNH DẠNG KHUNG (AFFILIATION FRAMES)

| Nhận dạng lực lượng | Hình dạng khung hình học | Mã màu Hex viền | Mã màu nền Alpha | Mô tả chuẩn |
| :--- | :--- | :--- | :--- | :--- |
| **FRIENDLY (Đồng minh)** | **Hình chữ nhật bo góc (Rounded Rectangle)** | `#3B82F6` | `0xDD0E294B` | Biểu thị lực lượng phe ta và liên minh NATO |
| **HOSTILE (Đối phương)** | **Hình thoi sắc cạnh (Diamond)** | `#EF4444` | `0xDD3B0D0D` | Biểu thị mục tiêu địch, xe pháo, đài radar đối phương |
| **NEUTRAL (Trung lập)** | **Hình vuông chuẩn (Square)** | `#10B981` | `0xDD0A2E1C` | Dân thường, đoàn xe cứu trợ LHQ (UN), bệnh viện |
| **UNKNOWN (Chưa rõ)** | **Hình cỏ 4 lá uốn cong (Quatrefoil)** | `#F59E0B` | `0xDD3D2808` | Vết radar chưa nhận dạng được bạn hay thù |

---

## III. BẢNG TRA CỨU BIỂU TƯỢNG BINH CHỦNG (COMBAT ARMS & BRANCHES)

| STT | Binh chủng (Branch) | Ký hiệu hình học bên trong khung | Ví dụ khí tài / Đơn vị thực tế |
| :---: | :--- | :--- | :--- |
| **1** | **Bộ binh (Infantry)** | Dấu gạch chéo `X` (Tượng trưng cho dây đeo đạn chéo) | Lực lượng bộ binh cơ động, lính dù, lính thủy đánh bộ |
| **2** | **Tăng thiết giáp (Armor)** | Vòng xích bọc thép hình Oval nằm ngang | Xe tăng chủ lực: M1A2 Abrams, Leopard 2A7, T-90M |
| **3** | **Pháo binh mặt đất (Field Artillery)** | Điểm tròn đặc `●` (Viên đạn pháo Cannonball) | Lựu pháo xe kéo: M777 155mm, D-30 122mm |
| **4** | **Pháo tự hành (SPG)** | Vòng xích Oval bọc thép + Điểm tròn pháo binh `●` | PZh 2000, M109A6 Paladin, 2S19 Msta-S, CAESAR |
| **5** | **Pháo phản lực (MLRS / HIMARS)** | Điểm tròn pháo `●` + Mũi tên phóng tên lửa hướng lên | M142 HIMARS, M270 MLRS, BM-21 Grad, Tornado-S |
| **6** | **Súng cối (Mortar)** | Điểm tròn `●` kèm nòng cối thẳng đứng | Súng cối 120mm M120, 81mm L16, Cối tự hành Stryker |
| **7** | **Bộ binh cơ giới (Mech Infantry)** | Vòng xích Oval + Dấu gạch chéo `X` bộ binh | Xe chiến đấu bộ binh IFV: M2 Bradley, Puma, BMP-3 |
| **8** | **Phòng không (Air Defense)** | Vòm cung bảo vệ bầu trời `⌢` (Dome Arc) | MIM-104 Patriot, IRIS-T, NASAMS, Tor-M2, Pantsir-S1 |
| **9** | **Không quân trực thăng (Aviation)** | Cánh quạt quay hình nơ `⋈` (Rotary Bowtie) | Trực thăng vũ trang: AH-64 Apache, Ka-52, Tiger |
| **10** | **Trinh sát thiết giáp (Recon / Cav)** | Đường gạch chéo trinh sát đơn `/` (Cavalry Slash) | Xe trinh sát Fennek, BRDM-2, Jackal, AMX-10RC |
| **11** | **Chống tăng (Anti-Tank)** | Ký hiệu chữ V ngược `∧` (Inverted Arrowhead) | Tổ tên lửa chống tăng có điều khiển: Javelin, Kornet, Spike |
| **12** | **Đài quan sát tiền duyên (FO / JFST)** | Tam giác đài quan sát `△` kèm tâm mắt `●` | Đài điều khiển hỏa lực pháo binh, Forward Observer |
| **13** | **Sở chỉ huy (Headquarters / CP)** | Cán cờ chỉ huy nối dài từ góc trái khung xuống | Sở chỉ huy cấp Lữ đoàn (TOC), Sở chỉ huy Tiểu đoàn (CP) |
| **14** | **Công binh (Engineer / Sapper)** | Vòm cầu vượt / Cổng thành lũy kiên cố `⊓` | Xe bắc cầu cơ giới, xe phá mìn ABV, công binh dọn đường |
| **15** | **Thông tin liên lạc (Signal)** | Tia chớp sóng vô tuyến truyền tin `⚡` | Trạm vệ tinh chỉ huy C2, xe tiếp sóng chiến thuật |
| **16** | **Hậu cần tiếp vận (Logistics)** | Thanh ngang tiếp vận quân sự `⊞` | Đoàn xe tải đạn dược, bồn chứa nhiên liệu, đoàn tiếp tế |
| **17** | **Quân y cứu thương (Medical)** | Chữ thập đỏ Geneva y tế `+` | Bệnh viện dã chiến, xe cứu thương bọc thép M113 |

---

## IV. QUY CHUẨN CẤP BẬC ĐƠN VỊ TÁC CHIẾN (ECHELON SIZES)

| Ký hiệu trên đầu khung | Tên tiếng Anh | Tên tiếng Việt | Quy mô nhân sự / Khí tài tiêu chuẩn |
| :---: | :--- | :--- | :--- |
| **`Ø`** | **Team / Fireteam** | Tổ chiến đấu | 3 – 4 chiến sĩ |
| **`●`** | **Squad** | Tiểu đội | 8 – 12 chiến sĩ (1 xe IFV) |
| **`●●`** | **Section** | Phân đội / Khẩu đội hỏa lực | 2 – 3 tiểu đội / 2 khẩu pháo |
| **`●●●`** | **Platoon** | Trung đội | 3 – 4 tiểu đội / 4 xe tăng |
| **`\|`** | **Company / Battery / Troop** | Đại đội / Khẩu đội pháo | 3 – 4 trung đội (6 – 8 xe pháo) |
| **`\|\|`** | **Battalion / Squadron** | Tiểu đoàn / Phân đoàn | 3 – 5 đại đội (~300 - 800 quân) |
| **`\|\|\|`** | **Regiment / Group** | Trung đoàn | 2 – 3 tiểu đoàn |
| **`X`** | **Brigade** | Lữ đoàn (BCT) | 3 – 6 tiểu đoàn chiến đấu (~3,000 - 5,000 quân) |
| **`XX`** | **Division** | Sư đoàn | 3 – 4 lữ đoàn (~10,000 - 15,000 quân) |
| **`XXX`** | **Corps** | Quân đoàn | 2 – 4 sư đoàn tác chiến chiến dịch |

---

## V. TÍCH HỢP CODE JAVA (CODE IMPLEMENTATION)

* File nguồn thư viện đồ họa vector: [`NatoSymbolRenderer.java`](file:///c:/Users/Tran%20Bao%20Long/Desktop/UI%20TEST/ArtilleryTacticalTablet/src/main/java/net/nazarick/artillerytablet/client/screen/NatoSymbolRenderer.java)
* Cú pháp gọi vẽ chuẩn:
```java
NatoSymbolRenderer.drawSymbol(
    img, 
    centerX, centerY, size,
    NatoSymbolRenderer.Affiliation.FRIENDLY, 
    NatoSymbolRenderer.UnitType.SP_ARTILLERY, 
    NatoSymbolRenderer.Echelon.BATTERY, 
    "BTY-A", "PZH"
);
```

---
*Tài liệu này được xác lập làm chuẩn cứng vĩnh viễn cho dự án.*
