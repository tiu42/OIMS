# UC5 - Xac nhan don hang nhap kho - Test Design

## 1. Thong tin module kiem thu

- Use case: Xac nhan don hang nhap kho
- Lop duoc kiem thu: `com.oims.features.warehouse.process.ProcessPurchaseOrderController`
- Phuong thuc duoc kiem thu: `validateAndApproveReceipt(int orderId, List<ItemShortageResult> items)`
- Lop JUnit tu dong: `com.oims.features.warehouse.process.ProcessPurchaseOrderControllerTest`
- Framework: JUnit 5

## 2. Mo ta phuong thuc

Phuong thuc `validateAndApproveReceipt` thuc hien nghiep vu xac nhan mot don hang nhap kho. Dau vao gom ma don hang va danh sach ket qua kiem tra thuc nhan cua tung mat hang.

Phuong thuc co cac trach nhiem chinh:

- Kiem tra neu mat hang bi thieu thi so luong thieu phai lon hon 0.
- Kiem tra so luong thieu khong duoc vuot qua so luong da dat.
- Tim don hang theo `orderId`.
- Chi cho phep xac nhan don hang o trang thai `SENT` hoac `CONFIRMED`.
- Cap nhat trang thai don hang thanh `DELIVERED` neu hop le.

Ket qua mong doi:

- Neu du lieu hop le, don hang duoc cap nhat sang `DELIVERED`.
- Neu du lieu khong hop le, phuong thuc nem `IllegalArgumentException` hoac `SQLException`.

## 3. Kiem thu hop den

### 3.1. Phan lop tuong duong

| Lop | Dieu kien dau vao | Ket qua mong doi |
| --- | --- | --- |
| EP1 | Don hang ton tai, trang thai `SENT`, tat ca mat hang du | Cap nhat don sang `DELIVERED` |
| EP2 | Don hang ton tai, trang thai `CONFIRMED`, mat hang thieu hop le | Cap nhat don sang `DELIVERED` |
| EP3 | Mat hang co trang thai `Thiếu`, so luong thieu <= 0 | Bao loi |
| EP4 | Mat hang co trang thai `Thiếu`, so luong thieu > so luong dat | Bao loi |
| EP5 | Don hang khong ton tai | Bao loi |
| EP6 | Don hang ton tai nhung trang thai khong duoc phep | Bao loi |

### 3.2. Gia tri bien

| Bien | Gia tri kiem thu | Ly do |
| --- | --- | --- |
| `shortageQty` | 0 | Bien khong hop le vi phai lon hon 0 |
| `shortageQty` | 1 | Bien hop le nho nhat khi co thieu |
| `shortageQty` | `quantityOrdered` | Bien hop le lon nhat |
| `shortageQty` | `quantityOrdered + 1` | Bien khong hop le vi vuot so luong dat |

## 4. Kiem thu hop trang theo C1

### 4.1. Cac nhanh dieu kien

Trong phuong thuc co cac nhanh logic chinh:

1. `if ("Thiếu".equals(row.status()))`
   - True: mat hang duoc danh dau thieu.
   - False: mat hang duoc danh dau du.

2. `if (row.shortageQty() <= 0)`
   - True: loi so luong thieu khong hop le.
   - False: tiep tuc kiem tra.

3. `if (row.shortageQty() > row.quantityOrdered())`
   - True: loi so luong thieu vuot so luong dat.
   - False: tiep tuc xu ly.

4. `if (orderOpt.isPresent())`
   - True: tim thay don hang.
   - False: nem `SQLException`.

5. `if (order.getStatus() != SENT && order.getStatus() != CONFIRMED)`
   - True: trang thai don hang khong duoc phep.
   - False: cap nhat don hang thanh `DELIVERED`.

### 4.2. Bao phu C1

Bo test case da thiet ke bao phu ca hai nhanh True/False cua cac dieu kien quan trong:

- TC01 phu nhanh mat hang "Du", don hang `SENT`, cap nhat thanh cong.
- TC02 phu nhanh mat hang "Thieu" hop le, don hang `CONFIRMED`, cap nhat thanh cong.
- TC03 phu nhanh `shortageQty <= 0`.
- TC04 phu nhanh `shortageQty > quantityOrdered`.
- TC05 phu nhanh trang thai don hang khong duoc phep.
- TC06 phu nhanh khong tim thay don hang.

## 5. Bang test case tu dong

| Ma TC | Muc tieu | Dau vao | Ket qua mong doi | JUnit method |
| --- | --- | --- | --- | --- |
| TC01 | Xac nhan don moi gui va hang du | Order status `SENT`, items status `Đủ` | Order status thanh `DELIVERED` | `approveReceiptWithSentOrderAndFullItemsMarksOrderDelivered` |
| TC02 | Xac nhan don confirmed va co thieu hop le | Order status `CONFIRMED`, item `Thiếu`, shortage = 3, quantity = 10 | Order status thanh `DELIVERED` | `approveReceiptWithConfirmedOrderAndValidShortageMarksOrderDelivered` |
| TC03 | Tu choi so luong thieu bang 0 | Item `Thiếu`, shortage = 0 | Nem `IllegalArgumentException` | `approveReceiptRejectsShortageEqualToZero` |
| TC04 | Tu choi so luong thieu lon hon so luong dat | Item `Thiếu`, shortage = 11, quantity = 10 | Nem `IllegalArgumentException` | `approveReceiptRejectsShortageGreaterThanOrderedQuantity` |
| TC05 | Tu choi don hang da duyet | Order status `DELIVERED` | Nem `IllegalArgumentException` | `approveReceiptRejectsOrderInInvalidStatus` |
| TC06 | Tu choi don hang khong ton tai | `orderId = 999` | Nem `SQLException` | `approveReceiptRejectsMissingOrder` |

## 6. Kiem thu use case

| Scenario | Tien dieu kien | Cac buoc | Ket qua mong doi |
| --- | --- | --- | --- |
| UC5-S1 | Nhan vien kho da dang nhap, co don hang `SENT` | Chon don hang, mo man hinh xac nhan, danh dau tat ca mat hang du, bam xac nhan | Don hang chuyen sang trang thai da xac nhan nhap kho |
| UC5-S2 | Nhan vien kho da dang nhap, co don hang `CONFIRMED` | Chon don hang, danh dau mot mat hang thieu hop le, bam xac nhan | He thong chap nhan va cap nhat don hang sang da xac nhan |
| UC5-S3 | Nhan vien kho nhap so luong thieu bang 0 | Chon mat hang `Thiếu`, nhap 0, bam xac nhan | He thong hien thi loi so luong thieu phai lon hon 0 |
| UC5-S4 | Nhan vien kho nhap so luong thieu vuot so luong dat | Chon mat hang `Thiếu`, nhap so luong lon hon so luong dat, bam xac nhan | He thong hien thi loi so luong thieu khong duoc vuot qua so luong dat |
| UC5-S5 | Don hang da duoc xac nhan truoc do | Mo lai man hinh xac nhan cho don `DELIVERED` | He thong tu choi xu ly |

## 7. Ket qua chay test

Lenh chay:

```bash
sh mvnw -q -Dtest=ProcessPurchaseOrderControllerTest test
```

Ket qua:

- Tong so test case: 6
- Ket qua: Pass

Lenh chay toan bo test:

```bash
sh mvnw -q test
```

Ket qua:

- Tat ca test hien co trong project: Pass
