# UC1 - Tao yeu cau nhap hang - Test Design

## 1. Thong tin module kiem thu

- Use case: Tao yeu cau nhap hang
- Lop duoc kiem thu: `com.oims.features.sales_requests.create.CreateRequestController`
- Phuong thuc duoc kiem thu: `saveSalesRequest(User creator, List<TempRequestItem> tempItems)`
- Lop JUnit tu dong: `com.oims.features.sales_requests.create.CreateRequestControllerTest`
- Framework: JUnit 5

## 2. Mo ta phuong thuc

Phuong thuc tao mot `SalesRequest` moi va cac `SalesRequestItem` tu danh sach mat hang tam. Phuong thuc validate nguoi tao, danh sach mat hang, ma hang, so luong, don vi va ngay nhan mong muon.

Ket qua mong doi:

- Hop le: tao request trang thai `PENDING` va insert cac item.
- Khong hop le: nem `IllegalArgumentException` hoac `SQLException`.

## 3. Kiem thu hop den

| Lop | Dieu kien dau vao | Ket qua mong doi |
| --- | --- | --- |
| EP1 | Creator hop le, danh sach item hop le | Tao request va item thanh cong |
| EP2 | Creator null | Bao loi |
| EP3 | Danh sach item null/rong | Bao loi |
| EP4 | So luong <= 0 | Bao loi |
| EP5 | Ngay nhan mong muon trong qua khu | Bao loi |

Gia tri bien:

| Bien | Gia tri | Ket qua |
| --- | --- | --- |
| quantity | 0 | Khong hop le |
| quantity | 1 | Hop le |
| desiredDate | Hom qua | Khong hop le |
| desiredDate | Hom nay/tuong lai | Hop le |

## 4. Kiem thu hop trang C1

Nhanh can phu:

1. `if (creator == null)` true/false.
2. `if (tempItems == null || tempItems.isEmpty())` true/false.
3. `if (item.quantity() <= 0)` true/false.
4. `if (item.desiredDate().isBefore(LocalDate.now()))` true/false.
5. Insert request thanh cong va lap insert tung item.

## 5. Bang test case tu dong

| Ma TC | Muc tieu | Dau vao | Ket qua mong doi | JUnit method |
| --- | --- | --- | --- | --- |
| TC01 | Tao request hop le | Creator hop le, 2 items hop le | 1 request `PENDING`, 2 items duoc insert | `saveSalesRequestWithValidItemsCreatesPendingRequestAndItems` |
| TC02 | Thieu creator | Creator null | Nem `IllegalArgumentException` | `saveSalesRequestRejectsMissingCreator` |
| TC03 | Danh sach rong | `List.of()` | Nem `IllegalArgumentException` | `saveSalesRequestRejectsEmptyItemList` |
| TC04 | So luong khong hop le | quantity = 0 | Nem `IllegalArgumentException` | `saveSalesRequestRejectsNonPositiveQuantity` |
| TC05 | Ngay qua khu | desiredDate = yesterday | Nem `IllegalArgumentException` | `saveSalesRequestRejectsPastDesiredDate` |

## 6. Kiem thu use case

| Scenario | Tien dieu kien | Buoc thuc hien | Ket qua mong doi |
| --- | --- | --- | --- |
| UC1-S1 | Nhan vien ban hang da dang nhap | Nhap danh sach hang hop le va luu | Tao yeu cau nhap hang thanh cong |
| UC1-S2 | Nhan vien ban hang da dang nhap | Khong them mat hang nao va luu | He thong bao danh sach khong duoc rong |
| UC1-S3 | Nhan vien ban hang da dang nhap | Nhap so luong 0 | He thong bao so luong phai lon hon 0 |
| UC1-S4 | Nhan vien ban hang da dang nhap | Nhap ngay nhan trong qua khu | He thong bao ngay nhan khong hop le |

## 7. Ket qua chay test

Lenh chay:

```bash
sh mvnw -q -Dtest=CreateRequestControllerTest test
```

Ket qua: Pass.
