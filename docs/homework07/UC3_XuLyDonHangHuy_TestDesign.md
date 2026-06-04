# UC3 - Xu ly don hang huy - Test Design

## 1. Thong tin module kiem thu

- Use case: Xu ly don hang huy
- Lop duoc kiem thu: `com.oims.features.purchase_order.process_canceled.ProcessCanceledOrderController`
- Phuong thuc duoc kiem thu: `getSiteStockAndTransport(...)`, `getFailedDemands(...)`, `getCreatorName(...)`
- Lop JUnit tu dong: `com.oims.features.purchase_order.process_canceled.ProcessCanceledOrderControllerTest`
- Framework: JUnit 5

## 2. Mo ta phuong thuc

Khi don hang bi huy, he thong can bo qua site cua don hang bi huy va tim nguon thay the tu cac site con lai. Neu ton kho con lai khong du dap ung nhu cau, mat hang do duoc dua vao danh sach failed demands.

## 3. Kiem thu hop den

| Lop | Dieu kien dau vao | Ket qua mong doi |
| --- | --- | --- |
| EP1 | Ton kho o site bi huy | Site bi huy bi loai khoi ket qua |
| EP2 | Tong ton kho con lai < nhu cau | Mat hang nam trong failed demands |
| EP3 | Tong ton kho con lai >= nhu cau | Mat hang khong nam trong failed demands |
| EP4 | Khong tim thay user tao don | Tra ve fallback `Người dùng #id` |

Gia tri bien:

| Bien | Gia tri | Ket qua |
| --- | --- | --- |
| remainingStock | demand - 1 | Failed |
| remainingStock | demand | Not failed |
| excludedSite | Trung site trong stock | Bi bo qua |

## 4. Kiem thu hop trang C1

Nhanh can phu:

1. `if (siteCode.equals(excludedSiteCode))` true/false.
2. `if (siteOpt.isPresent())` true/false.
3. `if (!transInfos.isEmpty())` true/false.
4. `if (sites.isEmpty() || totalStock < demand.quantity())` true/false.
5. `userDao.findById(...).orElse(...)` co user/khong co user.

## 5. Bang test case tu dong

| Ma TC | Muc tieu | Dau vao | Ket qua mong doi | JUnit method |
| --- | --- | --- | --- | --- |
| TC01 | Loai site bi huy | Stock co SITE_CANCELLED va SITE01 | Chi tra SITE01 | `getSiteStockAndTransportExcludesCanceledSite` |
| TC02 | Phat hien khong du hang | Demand 10, stock con lai 4 | Failed demands co M001 | `getFailedDemandsReturnsDemandWhenRemainingStockIsInsufficient` |
| TC03 | Khong failed khi du hang | Demand 10, stock con lai 11 | Failed demands rong | `getFailedDemandsIgnoresDemandWhenRemainingStockIsEnough` |
| TC04 | Fallback ten creator | User khong ton tai | Tra `Người dùng #99` | `getCreatorNameFallsBackToUserIdWhenUserIsMissing` |

## 6. Kiem thu use case

| Scenario | Tien dieu kien | Buoc thuc hien | Ket qua mong doi |
| --- | --- | --- | --- |
| UC3-S1 | Don hang bi huy co site goc | Xu ly don huy | He thong loai site goc khoi nguon thay the |
| UC3-S2 | Site con lai khong du ton kho | Xu ly don huy | He thong bao mat hang can xu ly lai |
| UC3-S3 | Site con lai du ton kho | Xu ly don huy | He thong co the sinh phuong an thay the |
| UC3-S4 | Nguoi tao don khong tim thay | Xem thong tin don huy | He thong hien thi fallback user id |

## 7. Ket qua chay test

Lenh chay:

```bash
sh mvnw -q -Dtest=ProcessCanceledOrderControllerTest test
```

Ket qua: Pass.
