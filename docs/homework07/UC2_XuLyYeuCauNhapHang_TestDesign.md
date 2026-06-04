# UC2 - Xu ly yeu cau nhap hang - Test Design

## 1. Thong tin module kiem thu

- Use case: Xu ly yeu cau nhap hang
- Lop duoc kiem thu: `com.oims.features.sales_requests.process.PlanGenerationService`
- Phuong thuc duoc kiem thu: `generatePlans(List<ItemDemand>, Map<String, ItemConfig>, Map<String, List<SiteStockTransportDTO>>)`
- Lop JUnit tu dong: `com.oims.features.sales_requests.process.PlanGenerationServiceTest`
- Framework: JUnit 5

## 2. Mo ta phuong thuc

Phuong thuc sinh cac phuong an dat hang dua tren nhu cau mat hang, ton kho cua cac site, cau hinh uu tien site va phuong thuc giao hang.

Ket qua mong doi:

- Neu co du ton kho: tra ve danh sach `PlanDTO`.
- Neu bat ky mat hang nao khong du nguon cung: tra ve danh sach rong.
- Neu site ho tro ca tau va may bay: co cac phuong an giao hang tuong ung.

## 3. Kiem thu hop den

| Lop | Dieu kien dau vao | Ket qua mong doi |
| --- | --- | --- |
| EP1 | Tong ton kho < nhu cau | Khong sinh phuong an |
| EP2 | Mot site du hang | Sinh phuong an mot site |
| EP3 | Mot site khong du, nhieu site cong lai du | Sinh phuong an ghep nhieu site |
| EP4 | Site ho tro ship va air | Sinh cac lua chon delivery |

Gia tri bien:

| Bien | Gia tri | Ket qua |
| --- | --- | --- |
| totalStock | demand - 1 | Khong co plan |
| totalStock | demand | Co plan |
| supportedDelivery | ship only | Co order ship |
| supportedDelivery | ship + air | Co order ship va air |

## 4. Kiem thu hop trang C1

Nhanh can phu:

1. `if (siteStockList == null)` true/false.
2. `if (options.isEmpty())` true/false.
3. Nhanh subset du hang va khong du hang.
4. Nhanh delivery supported ship/air.
5. Nhanh sap xep va danh lai `planId`.

## 5. Bang test case tu dong

| Ma TC | Muc tieu | Dau vao | Ket qua mong doi | JUnit method |
| --- | --- | --- | --- | --- |
| TC01 | Khong du ton kho | Demand 10, stock 4 | Danh sach plan rong | `generatePlansReturnsEmptyListWhenAnyDemandCannotBeFulfilled` |
| TC02 | Mot site du hang | Demand 10, stock 15 | Co plan, order SITE01, quantity 10 | `generatePlansCreatesPlanFromSingleSiteStock` |
| TC03 | Ghep nhieu site | Demand 10, SITE01 co 6, SITE02 co 5 | Tong allocate = 10, uniqueSites = 2 | `generatePlansCombinesMultipleSitesWhenOneSiteIsNotEnough` |
| TC04 | Nhieu delivery | Site co shipDays va airDays > 0 | Co plan ship va air | `generatePlansCreatesDeliveryAlternativesWhenSiteSupportsShipAndAir` |

## 6. Kiem thu use case

| Scenario | Tien dieu kien | Buoc thuc hien | Ket qua mong doi |
| --- | --- | --- | --- |
| UC2-S1 | Yeu cau co mat hang va site du ton kho | Xu ly yeu cau | He thong de xuat phuong an dat hang |
| UC2-S2 | Tong ton kho khong dap ung nhu cau | Xu ly yeu cau | He thong khong sinh phuong an hop le |
| UC2-S3 | Nhieu site moi du dap ung | Xu ly yeu cau | He thong sinh phuong an chia hang theo nhieu site |
| UC2-S4 | Site co nhieu cach van chuyen | Xu ly yeu cau | He thong sinh phuong an theo cac delivery hop le |

## 7. Ket qua chay test

Lenh chay:

```bash
sh mvnw -q -Dtest=PlanGenerationServiceTest test
```

Ket qua: Pass.
