# 🎖️ TÀI LIỆU QUY CHUẨN KÝ HIỆU QUÂN SỰ NATO MIL-STD-2525D / APP-6D (MASTER BASELINE)
**Dự án**: ASTRA Frontline C2 & Artillery Tactical Tablet  
**Mã tài liệu**: `ASTRA-SPEC-MIL2525D-MASTER-REV5`  
**Trạng thái**: **QUY CHUẨN GỐC CỐ ĐỊNH (PERMANENT HARD BASELINE)**

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

## III. BẢNG QUY CHUẨN TOÀN DIỆN CÁC BINH CHỦNG QUÂN SỰ (COMBAT ARMS & BRANCHES)

### 1. Lực lượng Tác chiến Mặt đất (Ground Combat Units)
| STT | Binh chủng (Branch) | Ký hiệu hình học bên trong khung | Ví dụ khí tài / Đơn vị thực tế |
| :---: | :--- | :--- | :--- |
| **1** | **Bộ binh (Infantry)** | Dấu gạch chéo `X` (Dây đeo đạn chéo) | Lực lượng bộ binh cơ động, lính dù, lính thủy đánh bộ |
| **2** | **Bộ binh cơ động xe (Motorized)** | Dấu gạch chéo `X` + 2 bánh xe phía dưới | Bộ binh cơ động trên xe bọc thép bánh lốp Humvee, Oshkosh JLTV |
| **3** | **Bộ binh cơ giới (Mech Infantry)** | Vòng xích Oval bọc thép + Dấu gạch chéo `X` | Xe chiến đấu bộ binh IFV: M2 Bradley, Puma, BMP-3, CV90 |
| **4** | **Tăng thiết giáp (Armor)** | Vòng xích bọc thép hình Oval nằm ngang | Xe tăng chủ lực: M1A2 Abrams, Leopard 2A7, T-90M, Challenger 2 |
| **5** | **Trinh sát cơ động (Reconnaissance)** | Đường gạch chéo trinh sát đơn `/` (Cavalry Slash) | Xe trinh sát Fennek, BRDM-2, Jackal, AMX-10RC |
| **6** | **Trinh sát thiết giáp (Armored Recon)** | Vòng xích Oval + Gạch chéo trinh sát `/` | Xe trinh sát chiến đấu bọc thép: M3 Bradley CFV, SpPz 2 Luchs |
| **7** | **Chống tăng (Anti-Tank)** | Ký hiệu chữ V ngược `∧` (Inverted Arrowhead) | Tổ tên lửa chống tăng vác vai / giá phóng: Javelin, Kornet, Spike |
| **8** | **Diệt tăng tự hành (Armored Anti-Tank)** | Vòng xích Oval + Ký hiệu chữ V ngược `∧` | Xe diệt tăng tự hành: Stryker TOW, Khrizantema-S, Wiesel TOW |
| **9** | **Đặc nhiệm (Special Forces)** | Ký hiệu chữ `SF` chuyên dụng | Lực lượng tác chiến đặc biệt: SAS, US Green Berets, KSK |
| **10** | **Bắn tỉa (Sniper / Marksman)** | Tâm ngắm chữ thập Reticle kèm điểm tâm | Tổ bắn tỉa thiện xạ cự ly xa, súng bắn tỉa Barrett .50 |

---

### 2. Pháo binh & Chi viện Hỏa lực (Artillery & Fire Support)
| STT | Binh chủng (Branch) | Ký hiệu hình học bên trong khung | Ví dụ khí tài / Đơn vị thực tế |
| :---: | :--- | :--- | :--- |
| **11** | **Pháo binh mặt đất (Field Artillery)** | Điểm tròn đặc `●` (Viên đạn pháo Cannonball) | Lựu pháo xe kéo: M777 155mm, D-30 122mm, FH-70 |
| **12** | **Pháo tự hành (SPG Howitzer)** | Vòng xích Oval + Điểm tròn pháo binh `●` | PZh 2000, M109A6 Paladin, 2S19 Msta-S, CAESAR, Archer |
| **13** | **Pháo phản lực (MLRS / HIMARS)** | Điểm tròn pháo `●` + Mũi tên phóng hướng lên | M142 HIMARS, M270 MLRS, BM-21 Grad, Tornado-S, BM-30 |
| **14** | **Súng cối (Mortar)** | Điểm tròn `●` kèm nòng cối thẳng đứng | Súng cối 120mm M120, 81mm L16, Cối bộ binh cơ động |
| **15** | **Súng cối tự hành (SP Mortar)** | Vòng xích Oval + Nòng cối thẳng đứng | Xe cối tự hành bọc thép Stryker MCV, M1064A3, 2S23 Nona-SVK |
| **16** | **Đài quan sát tiền duyên (FO / JFST)** | Tam giác đài quan sát `△` kèm tâm mắt `●` | Đài quan sát hỏa lực tiền phương, Sĩ quan liên lạc hỏa lực JTAC |
| **17** | **Radar trinh sát pháo (Counter-Battery)**| Điểm tròn pháo `●` + Vòng sóng radar | Radar phản pháo phát hiện trận địa: AN/TPQ-53, COBRA |

---

### 3. Phòng không & Không quân (Air & Air Defense)
| STT | Binh chủng (Branch) | Ký hiệu hình học bên trong khung | Ví dụ khí tài / Đơn vị thực tế |
| :---: | :--- | :--- | :--- |
| **18** | **Pháo phòng không (Air Defense Gun)** | Vòm cung bảo vệ bầu trời `⌢` (Dome Arc) | Pháo phòng không tự hành: Gepard, Tunguska, ZSU-23-4 |
| **19** | **Tên lửa phòng không (SAM Missile)** | Vòm cung `⌢` + Mũi tên tên lửa thẳng đứng | Tổ hợp SAM: MIM-104 Patriot, IRIS-T SLM, Tor-M2, Pantsir-S1 |
| **20** | **Không quân trực thăng (Rotary Aviation)**| Cánh quạt quay hình nơ `⋈` (Rotary Bowtie) | Trực thăng vũ trang: AH-64 Apache, Ka-52 Alligator, Tiger |
| **21** | **Không quân tiêm kích (Fixed-Wing Jet)**| Cánh máy bay phản lực hình chữ thập | Tiêm kích phản lực: F-35 Lightning II, F-16V, Su-35S |
| **22** | **Máy bay không người lái (UAV / Drone)**| Thân máy bay không người lái dạng cánh tam giác | UCAV / Recon: MQ-9 Reaper, Bayraktar TB2, ScanEagle |

---

### 4. Đảm bảo Tác chiến & Hậu cần Tiếp vận (Combat Support & CSS)
| STT | Binh chủng (Branch) | Ký hiệu hình học bên trong khung | Ví dụ khí tài / Đơn vị thực tế |
| :---: | :--- | :--- | :--- |
| **23** | **Sở chỉ huy (Headquarters / CP)** | Cán cờ chỉ huy nối dài từ góc trái khung xuống | Sở chỉ huy cấp Lữ đoàn (TOC), Sở chỉ huy Tiểu đoàn (CP) |
| **24** | **Công binh (Combat Engineer)** | Vòm cầu vượt / Cổng thành lũy kiên cố `⊓` | Công binh rà phá vật cản, mở đường cơ động |
| **25** | **Công binh bọc thép (Armored Engineer)**| Vòng xích Oval + Vòm cầu vượt `⊓` | Xe phá mìn bọc thép ABV, xe công trình Wisent 2 |
| **26** | **Thông tin liên lạc (Signal)** | Tia chớp sóng vô tuyến truyền tin `⚡` | Trạm vệ tinh chỉ huy C2, xe tiếp sóng chiến thuật |
| **27** | **Tác chiến điện tử (Electronic Warfare)**| Ký hiệu `EW` chuyên dụng | Xe gây nhiễu radar, đài trinh sát điện từ |
| **28** | **Phòng hóa (CBRN / NBC)** | Ký hiệu `NBC` chuyên dụng | Đơn vị trinh sát phóng xạ, tẩy độc môi trường |
| **29** | **Hậu cần tiếp vận (Logistics)** | Thanh ngang tiếp vận quân sự `⊞` | Đoàn xe tải vận tải hàng hóa, tiếp tế tổng hợp |
| **30** | **Kho tiếp tế đạn dược (Ammunition ASP)**| Biểu tượng đầu đạn pháo | Điểm tập kết đạn dược dã chiến (Ammunition Supply Point) |
| **31** | **Tiếp tế xăng dầu (POL Fuel)** | Ký hiệu `POL` chuyên dụng | Đoàn xe bồn nhiên liệu, kho dã chiến xăng dầu |
| **32** | **Sửa chữa cứu kéo (Maintenance / Recovery)**| Mỏ-lết cứu kéo cơ giới | Xe cứu kéo bọc thép Bergepanzer, trạm sửa chữa lưu động |
| **33** | **Quân y cứu thương (Medical)** | Chữ thập đỏ Geneva y tế `+` | Bệnh viện dã chiến, xe cứu thương bọc thép M113 AMEV |
| **34** | **Kiểm soát quân sự (Military Police)** | Ký hiệu `MP` chuyên dụng | Đơn vị tuần tra, giữ gìn trật tự và bảo vệ sở chỉ huy |

---

## IV. QUY CHUẨN CẤP BẬC ĐƠN VỊ TÁC CHIẾN (ECHELON HIERARCHY)

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
| **`XXXX`** | **Field Army** | Tập đoàn quân | 2 – 4 quân đoàn |
| **`XXXXX`** | **Army Group / Front** | Cụm tập đoàn quân / Mặt trận | Nhiều tập đoàn quân phối hợp chiến dịch |

---

## V. ĐƯỜNG PHÂN GIỚI & KÝ HIỆU ĐIỀU PHỐI TÁC CHIẾN (TACTICAL CONTROL GRAPHICS)

1. **Phân giới tác chiến (Boundary Line)**: Đường nét đứt xanh/đỏ đi kèm ký hiệu cấp bậc `— || —` (Phân giới tiểu đoàn) hoặc `— X —` (Phân giới lữ đoàn).
2. **Tuyến kiểm soát quân sự (Phase Line - PL)**: Đường kẻ liền màu xanh lục `PL OAK`, `PL RED` phân chia giai đoạn tiến công.
3. **Đường hướng bắn pháo (Gun-Target Line - GTL)**: Đường nét đứt màu vàng hổ phách nối từ trận địa pháo tới mục tiêu kèm thông số phương vị (`AZ: 052.4°`).
4. **Điểm chuẩn mục tiêu (Target Reference Point - TRP)**: Vòng tròn chữ thập màu đỏ xác định điểm chuẩn pháo binh (`TRP-001`).
5. **Elip tản mát đạn pháo (CEP 90 Dispersion Ellipse)**: Đường viền elip đỏ bao quanh điểm nổ dự tính đại diện sai số xác suất vòng $90\%$.
6. **Nón quan sát / Radar (Sensor Cone / LOS FOV)**: Vùng quạt quét hình nón góc mở $35^\circ$ mờ sáng thể hiện tầm quan sát của đài trinh sát/UAV.

---
*Tài liệu này là quy chuẩn kỹ thuật gốc có hiệu lực bắt buộc cho toàn bộ mã nguồn dự án ASTRA.*
