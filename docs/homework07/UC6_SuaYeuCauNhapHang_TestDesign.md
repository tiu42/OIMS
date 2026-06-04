# UC6 - Sua yeu cau nhap hang - Test Design

## 1. Thong tin module kiem thu

- Use case: Sua yeu cau nhap hang
- Lop duoc kiem thu: `com.oims.features.sales_requests.edit.EditRequestController`
- Phuong thuc duoc kiem thu: `updateSalesRequest(int requestId, User modifier, List<TempRequestItem> tempItems)`
- Lop JUnit tu dong: `com.oims.features.sales_requests.edit.EditRequestControllerTest`
- Framework: JUnit 5

## 2. Mo ta phuong thuc

Phuong thuc cap nhat danh sach mat hang cua yeu cau nhap hang. Chi yeu cau o trang thai `PENDING` moi duoc sua. Khi hop le, he thong xoa item cu va insert item moi.

## 3. Kiem thu hop den

| Lop | Dieu kien dau vao | Ket qua mong doi |
| --- | --- | --- |
| EP1 | Request `PENDING`, modifier va item hop le | Xoa item cu, them item moi |
| EP2 | Modifier null | Bao loi |
| EP3 | Request khong ton tai | Bao loi |
| EP4 | Request khong phai `PENDING` | Bao loi |
| EP5 | Ngay nhan trong qua khu | Bao loi |

Gia tri bien:

| Bien | Gia tri | Ket qua |
| --- | --- | --- |
| status | `PENDING` | Duoc sua |
| status | `COMPLETED` | Khong duoc sua |
| desiredDate | Hom qua | Khong hop le |
| desiredDate | Tuong lai | Hop le |

## 4. Kiem thu hop trang C1

Nhanh can phu:

1. `if (modifier == null)` true/false.
2. `if (tempItems == null || tempItems.isEmpty())` true/false.
3. Validation tung item.
4. `if (requestOpt.isEmpty())` true/false.
5. `if (salesRequest.getStatus() != PENDING)` true/false.
6. Nhanh xoa item cu va insert item moi.

## 5. Bang test case tu dong

| Ma TC | Muc tieu | Dau vao | Ket qua mong doi | JUnit method |
| --- | --- | --- | --- | --- |
| TC01 | Sua request hop le | Request `PENDING`, 2 item moi | Xoa item cu, them 2 item moi | `updateSalesRequestWithPendingRequestReplacesExistingItems` |
| TC02 | Thieu modifier | Modifier null | Nem `IllegalArgumentException` | `updateSalesRequestRejectsMissingModifier` |
| TC03 | Request khong ton tai | `requestId = 404` | Nem `IllegalArgumentException` | `updateSalesRequestRejectsMissingRequest` |
| TC04 | Request sai trang thai | Status `COMPLETED` | Nem `IllegalArgumentException` | `updateSalesRequestRejectsNonPendingRequest` |
| TC05 | Ngay qua khu | desiredDate = yesterday | Nem `IllegalArgumentException` | `updateSalesRequestRejectsPastDesiredDate` |

## 6. Kiem thu use case

| Scenario | Tien dieu kien | Buoc thuc hien | Ket qua mong doi |
| --- | --- | --- | --- |
| UC6-S1 | Yeu cau dang cho xu ly | Sua danh sach mat hang va luu | He thong cap nhat thanh cong |
| UC6-S2 | Yeu cau da xu ly xong | Co gang sua | He thong tu choi |
| UC6-S3 | Khong tim thay yeu cau | Mo/sua request khong ton tai | He thong bao loi |
| UC6-S4 | Nhap ngay qua khu | Sua item va luu | He thong bao ngay khong hop le |

## 7. Ket qua chay test

Lenh chay:

```bash
sh mvnw -q -Dtest=EditRequestControllerTest test
```

Ket qua: Pass.
