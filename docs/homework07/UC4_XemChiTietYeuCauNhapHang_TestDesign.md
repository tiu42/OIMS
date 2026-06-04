# UC4 - Xem chi tiet yeu cau nhap hang - Test Design

## 1. Thong tin module kiem thu

- Use case: Xem chi tiet yeu cau nhap hang
- Lop duoc kiem thu: `com.oims.features.sales_requests.detail.RequestDetailController`
- Phuong thuc duoc kiem thu: `loadRequestData()`, `loadTableData()`, `getStatusLabel(...)`, `getSalesRequestStatus()`
- Lop JUnit tu dong: `com.oims.features.sales_requests.detail.RequestDetailControllerTest`
- Framework: JUnit 5

## 2. Mo ta phuong thuc

Controller doc request dang duoc chon trong `AppSession`, lay thong tin nguoi tao, ngay tao, trang thai va danh sach mat hang de hien thi tren man hinh chi tiet.

## 3. Kiem thu hop den

| Lop | Dieu kien dau vao | Ket qua mong doi |
| --- | --- | --- |
| EP1 | Request ton tai, user ton tai | Tra DTO dung id, creator, date, status |
| EP2 | Request co nhieu item | Tra danh sach row dung ten mat hang |
| EP3 | Moi status cua request | Tra label tieng Viet dung |
| EP4 | Da load data | `getSalesRequestStatus` tra dung status |

## 4. Kiem thu hop trang C1

Nhanh can phu:

1. Resolve creator: tim thay user hoac fallback.
2. Resolve merchandise: tim thay merchandise hoac fallback.
3. Switch status label cho `PENDING`, `PROCESSING`, `COMPLETED`, `ERROR`.
4. `salesRequest.map(...)` sau khi load data.

## 5. Bang test case tu dong

| Ma TC | Muc tieu | Dau vao | Ket qua mong doi | JUnit method |
| --- | --- | --- | --- | --- |
| TC01 | Load thong tin request | Request id 7, creator id 10 | DTO dung thong tin da format | `loadRequestDataReturnsFormattedDetailInformation` |
| TC02 | Load table item | 2 item va 2 merchandise | 2 row co ten Keyboard, Mouse | `loadTableDataReturnsItemsWithResolvedMerchandiseNames` |
| TC03 | Label status | 4 gia tri enum | 4 label tieng Viet dung | `getStatusLabelReturnsVietnameseLabelsForAllStatuses` |
| TC04 | Lay status sau khi load | Request status ERROR | Tra `SalesRequestStatus.ERROR` | `getSalesRequestStatusReturnsLoadedRequestStatus` |

## 6. Kiem thu use case

| Scenario | Tien dieu kien | Buoc thuc hien | Ket qua mong doi |
| --- | --- | --- | --- |
| UC4-S1 | Da chon mot yeu cau | Mo man hinh chi tiet | Hien id, nguoi tao, ngay tao, trang thai |
| UC4-S2 | Yeu cau co nhieu mat hang | Mo chi tiet | Hien dung danh sach mat hang |
| UC4-S3 | Yeu cau o moi trang thai | Mo chi tiet | Hien label trang thai dung |

## 7. Ket qua chay test

Lenh chay:

```bash
sh mvnw -q -Dtest=RequestDetailControllerTest test
```

Ket qua: Pass.
