# 🎖️ NATO MIL-STD-2525D / APP-6D TACTICAL MILITARY SYMBOLOGY SPECIFICATION
**Project**: ASTRA Frontline C2 & Artillery Tactical Tablet System  
**Document Reference**: `ASTRA-SPEC-MIL2525D-MASTER-REV5`  
**Classification**: **PERMANENT MASTER HARD BASELINE**

---

## I. CORE DESIGN PRINCIPLES

1. **Strict Standards Compliance**: All tactical icons displayed across the digital map canvas, ORBAT registries, and radar reconnaissance views must strictly adhere to the geometric specifications of **NATO MIL-STD-2525D / APP-6D**.
2. **High-Contrast Tactical Color Rules**:
   * **Friendly Forces (Own & Allies)**: Tactical Blue frame border (`#3B82F6`), translucent deep navy fill (`0xDD0E294B`), crisp white text (`#E2E8F0`).
   * **Hostile Forces (Enemy / OPFOR)**: Crimson Red frame border (`#EF4444`), translucent deep maroon fill (`0xDD3B0D0D`), light crimson text (`#FEE2E8`).
   * **Neutral Forces (Civilian / UN / Non-Aligned)**: Emerald Green frame border (`#10B981`), translucent deep green fill (`0xDD0A2E1C`).
   * **Unknown / Pending (Unidentified Tracks)**: Tactical Amber frame border (`#F59E0B`), translucent deep amber fill (`0xDD3D2808`).
3. **Symbol Labeling & Modifier Positioning**:
   * **Echelon Size Modifier**: Always positioned **at the top center** directly above the frame.
   * **Unit Callsign / Designation**: Always positioned **at the bottom center** directly below the frame.
   * **Higher Formation / Parent Unit**: Positioned **at the right or left lateral flank** of the frame.

---

## II. STANDARD AFFILIATION FRAMES & GEOMETRY

| Affiliation | Geometric Frame Shape | Border Color Hex | Background Fill (ARGB) | Standard Military Definition |
| :--- | :--- | :--- | :--- | :--- |
| **FRIENDLY (Own & Allies)** | **Solid Rounded Rectangle Box** | `#3B82F6` | `0xDD0E294B` | Designates own forces and NATO allied combat units |
| **HOSTILE (Enemy - OPFOR)** | **Solid Diamond Frame** | `#EF4444` | `0xDD3B0D0D` | Designates confirmed enemy targets, combat vehicles, radars |
| **NEUTRAL (Non-Aligned)** | **Solid Square Frame** | `#10B981` | `0xDD0A2E1C` | Designates civilians, UN aid convoys, humanitarian assets |
| **UNKNOWN (Pending)** | **Quatrefoil / Cloverleaf Frame** | `#F59E0B` | `0xDD3D2808` | Designates unconfirmed radar tracks and pending tracks |

---

## III. MASTER BRANCH & COMBAT FUNCTION ICONS

### 1. Ground Combat Units
| No. | Military Branch | Geometric Internal Modifier | Operational Examples & Platforms |
| :---: | :--- | :--- | :--- |
| **1** | **Infantry (IN)** | Diagonal Cross (`X`) representing cartridge belts | Light Infantry, Airborne, Marine Battalions |
| **2** | **Motorized Infantry (MOT)** | Diagonal Cross (`X`) with two lower wheels (`○ ○`) | Wheeled Light Armor: Humvee, Oshkosh JLTV, Mastiff |
| **3** | **Mechanized Infantry (MECH)** | Armor Track Oval + Infantry Cross (`X`) | Infantry Fighting Vehicles: M2 Bradley, Puma, BMP-3, CV90 |
| **4** | **Armor / Tank (AR)** | Horizontal Capsule Armor Track Oval (`⬭`) | Main Battle Tanks: M1A2 Abrams, Leopard 2A7, T-90M |
| **5** | **Reconnaissance / Cavalry** | Single Forward Diagonal Slash (`/`) | Reconnaissance Patrols: Fennek, BRDM-2, Jackal |
| **6** | **Armored Recon (ARM CAV)** | Armor Track Oval + Forward Slash (`/`) | Cavalry Fighting Vehicles: M3A3 Bradley CFV, SpPz Luchs |
| **7** | **Anti-Tank (AT)** | Inverted Arrowhead (`∧`) | Anti-Tank Guided Missile Teams: Javelin, Kornet, Spike |
| **8** | **SP Anti-Tank (Tank Destroyer)**| Armor Track Oval + Inverted Arrowhead (`∧`) | Tank Destroyers: Stryker TOW, Khrizantema-S, Wiesel TOW |
| **9** | **Special Operations (SOF)** | Special Operations Modifier (`SF`) | Special Forces: SAS, US Green Berets, KSK, Delta |
| **10** | **Sniper / Marksman (SNP)** | Crosshair Reticle with Center Point | Long-Range Sniper Teams, Anti-Materiel Rifles |

---

### 2. Artillery & Fire Support Systems
| No. | Military Branch | Geometric Internal Modifier | Operational Examples & Platforms |
| :---: | :--- | :--- | :--- |
| **11** | **Field Artillery (FA)** | Solid Filled Circle (`●`) representing cannonball | Towed Howitzers: M777 155mm, D-30 122mm, FH-70 |
| **12** | **Self-Propelled Howitzer (SPG)**| Armor Track Oval + Solid Cannonball Dot (`●`) | PZh 2000, M109A6 Paladin, 2S19 Msta-S, CAESAR, Archer |
| **13** | **Rocket Artillery (MLRS)** | Cannonball Dot (`●`) + Upward Rocket Arrow | Precision Rockets: M142 HIMARS, M270 MLRS, BM-21 Grad |
| **14** | **Mortar Systems (MTR)** | Cannonball Dot (`●`) + Upward Mortar Tube | Infantry Mortars: M120 120mm, L16 81mm, 2B11 120mm |
| **15** | **SP Mortar Carrier** | Armor Track Oval + Mortar Tube | Armored Mortar Carriers: Stryker MCV, M1064A3, 2S23 |
| **16** | **Forward Observer (FO / JFST)** | Observation Triangle (`△`) + Target Eye (`●`) | Forward Fire Support Teams, Joint Fires Observers (JTAC) |
| **17** | **Counter-Battery Radar** | Artillery Dot (`●`) + Radar Emitting Wave | Counter-Battery Systems: AN/TPQ-53, COBRA Radar |

---

### 3. Air & Air Defense Systems
| No. | Military Branch | Geometric Internal Modifier | Operational Examples & Platforms |
| :---: | :--- | :--- | :--- |
| **18** | **Air Defense Gun (SPAAG)** | Sky Protection Dome Arc (`⌢`) | Self-Propelled AA Guns: Gepard, Tunguska, Shilka |
| **19** | **Air Defense Missile (SAM)** | Dome Arc (`⌢`) + Surface-to-Air Arrow | SAM Batteries: MIM-104 Patriot, IRIS-T SLM, Tor-M2 |
| **20** | **Attack Helicopter (Rotary)** | Rotary Wing Bowtie Symbol (`⋈`) | Attack & Recon Helicopters: AH-64 Apache, Ka-52, Tiger |
| **21** | **Combat Aircraft (Fixed-Wing)** | Fixed-Wing Jet Silhouette | Fighter Aircraft: F-35 Lightning II, F-16V, Su-35S |
| **22** | **Unmanned Aerial Vehicle (UAV)**| Delta-Wing Drone Silhouette | Recon & Strike Drones: MQ-9 Reaper, Bayraktar TB2 |

---

### 4. Combat Support & Combat Service Support (CSS)
| No. | Military Branch | Geometric Internal Modifier | Operational Examples & Platforms |
| :---: | :--- | :--- | :--- |
| **23** | **Headquarters (HQ / CP)** | Staff Flagpole extending down from lower-left frame | Brigade TOC, Battalion Command Post (CP) |
| **24** | **Combat Engineer (EN)** | Sapper Arch / Fortification Bridge (`⊓`) | Breaching Engineers, Armored Bridgelayers |
| **25** | **Armored Engineer (AVRE)** | Armor Track Oval + Sapper Arch (`⊓`) | Assault Breacher Vehicles: ABV, Wisent 2 |
| **26** | **Signal Communications (SIG)**| Radio Transmission Lightning Bolt (`⚡`) | C2 Satellite Terminals, Tactical Relay Vehicles |
| **27** | **Electronic Warfare (EW)** | Special Electronic Warfare Modifier (`EW`) | Jamming Platforms, SIGINT Intercept Stations |
| **28** | **CBRN Defense (NBC)** | CBRN Defense Modifier (`NBC`) | NBC Reconnaissance, Decontamination Units |
| **29** | **Logistics Supply (LOG)** | Crossbeam Horizontal Bar (`⊞`) | General Cargo Logistics, Forward Supply Depots |
| **30** | **Ammunition Point (ASP)** | Artillery Ammunition Shell Icon | Ammunition Supply Point, Reload Vehicles |
| **31** | **Petroleum Supply (POL)** | Special Fuel Modifier (`POL`) | Forward Arming & Refueling Point (FARP) |
| **32** | **Maintenance & Recovery** | Mechanic Wrench Symbol (`MTR`) | Armored Recovery Vehicles: Bergepanzer, M88A2 |
| **33** | **Medical Treatment (MED)** | Geneva Red Cross Symbol (`+`) | Field Hospitals, Armored Ambulances (M113 AMEV) |
| **34** | **Military Police (MP)** | Military Police Modifier (`MP`) | Route Control, Provost Marshal, Area Security |

---

## IV. ECHELON HIERARCHY SEQUENCE

| Echelon Marker | English Designation | Military Command Level | Standard Personnel / Asset Scale |
| :---: | :--- | :--- | :--- |
| **`Ø`** | **Team / Fireteam** | Combat Fireteam | 3 – 4 Soldiers |
| **`●`** | **Squad** | Squad / Combat Crew | 8 – 12 Soldiers (1 IFV) |
| **`●●`** | **Section** | Heavy Weapons Section | 2 Squads / 2 Artillery Pieces |
| **`●●●`** | **Platoon** | Platoon | 3 – 4 Squads / 4 Tanks |
| **`\|`** | **Company / Battery / Troop**| Company / Artillery Battery | 3 – 4 Platoons (6 – 8 Guns) |
| **`\|\|`** | **Battalion / Squadron** | Battalion / Cav Squadron | 3 – 5 Companies (~300 - 800 Personnel) |
| **`\|\|\|`** | **Regiment / Group** | Regiment / Tactical Group | 2 – 3 Battalions |
| **`X`** | **Brigade** | Brigade Combat Team (BCT) | 3 – 6 Battalions (~3,000 - 5,000 Personnel) |
| **`XX`** | **Division** | Combat Division | 3 – 4 Brigades (~10,000 - 15,000 Personnel) |
| **`XXX`** | **Corps** | Field Corps | 2 – 4 Divisions |
| **`XXXX`** | **Field Army** | Field Army Command | 2 – 4 Field Corps |
| **`XXXXX`** | **Army Group / Front** | Theater Army Group | Multiple Field Armies |

---

## V. TACTICAL OPERATIONAL CONTROL GRAPHICS & OVERLAYS

1. **Tactical Boundary Line**: Dashed line with echelon modifier `— || —` (Battalion Boundary) or `— X —` (Brigade Boundary).
2. **Operational Phase Line (PL)**: Solid green line `PL OAK`, `PL RED` demarcating operational phases.
3. **Gun-Target Line (GTL)**: Dashed tactical amber line connecting the artillery battery to the target with azimuth (`AZ: 052.4°`).
4. **Target Reference Point (TRP)**: Solid red crosshair reticle identifying pre-planned artillery target reference points (`TRP-001`).
5. **Dispersion Ellipse (CEP 90)**: Red dispersion ellipse showing the 90% circular error probable impact zone.
6. **Sensor & Radar Cone**: Translucent $35^\circ$ FOV sector beam illustrating forward observer and reconnaissance coverage.

---
*This document constitutes the official, authoritative master baseline for all future ASTRA C2 system designs.*
