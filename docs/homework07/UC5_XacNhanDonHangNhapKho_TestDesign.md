# UC5 - Xác nhận đơn hàng nhập kho - Thiết kế kiểm thử

## 1. Thông tin module kiểm thử

- **Use case:** Xác nhận đơn hàng nhập kho
- **Lớp được kiểm thử:** `com.oims.features.warehouse.process.ProcessPurchaseOrderController`
- **Phương thức được kiểm thử:** `validateAndApproveReceipt(int orderId, List<ItemShortageResult> items)`
- **Lớp JUnit tự động:** `com.oims.features.warehouse.process.ProcessPurchaseOrderControllerTest`
- **Framework:** JUnit 5

## 2. Mô tả phương thức

Phương thức `validateAndApproveReceipt` thực hiện nghiệp vụ xác nhận nhập kho cho một đơn đặt hàng (Purchase Order). Đầu vào gồm mã đơn hàng và danh sách kết quả kiểm tra thực tế hàng nhận của từng sản phẩm.

Phương thức chịu trách nhiệm:

- Kiểm tra nếu mặt hàng bị thiếu thì số lượng thiếu phải lớn hơn 0.
- Kiểm tra số lượng thiếu không được vượt quá số lượng đặt ban đầu.
- Tìm kiếm đơn hàng theo `orderId`.
- Chỉ cho phép xác nhận các đơn hàng có trạng thái `SENT` hoặc `CONFIRMED`.
- Cập nhật trạng thái đơn hàng thành `DELIVERED` (Đã nhập kho) nếu hợp lệ.

**Kết quả mong đợi:**

- **Hợp lệ:** Đơn hàng được cập nhật thành trạng thái `DELIVERED` trong cơ sở dữ liệu.
- **Không hợp lệ:** Ném ngoại lệ `IllegalArgumentException` hoặc `SQLException`.

---

## 3. Kiểm thử hộp đen

### 3.1. Phân lớp tương đương

| Lớp phân hoạch | Điều kiện đầu vào | Kết quả mong đợi |
| --- | --- | --- |
| **EP1** | Đơn hàng tồn tại, trạng thái là `SENT`, tất cả sản phẩm đều đủ hàng | Cập nhật trạng thái đơn hàng sang `DELIVERED` |
| **EP2** | Đơn hàng tồn tại, trạng thái là `CONFIRMED`, có mặt hàng thiếu hợp lệ | Cập nhật trạng thái đơn hàng sang `DELIVERED` |
| **EP3** | Mặt hàng đánh dấu trạng thái là `Thiếu`, số lượng thiếu <= 0 | Báo lỗi hệ thống |
| **EP4** | Mặt hàng đánh dấu trạng thái là `Thiếu`, số lượng thiếu > số lượng đã đặt | Báo lỗi hệ thống |
| **EP5** | Đơn hàng không tồn tại trong hệ thống | Báo lỗi hệ thống |
| **EP6** | Đơn hàng tồn tại nhưng có trạng thái không được phép xử lý | Báo lỗi hệ thống |

### 3.2. Giá trị biên

| Biến | Giá trị kiểm thử | Lý do kiểm thử |
| --- | --- | --- |
| `shortageQty` | 0 | Biên không hợp lệ (số lượng thiếu tối thiểu phải là 1) |
| `shortageQty` | 1 | Biên hợp lệ nhỏ nhất khi phát hiện thiếu hàng |
| `shortageQty` | `quantityOrdered` | Biên hợp lệ lớn nhất (thiếu toàn bộ đơn hàng) |
| `shortageQty` | `quantityOrdered + 1` | Biên không hợp lệ (số lượng thiếu vượt lượng đã đặt) |

---

## 4. Kiểm thử hộp trắng theo C1

### 4.1. Các nhánh điều kiện trong code

Trong phương thức `validateAndApproveReceipt`, các điểm quyết định và cách chúng được phủ bởi bộ test cases như sau:

| Điểm quyết định | Nhánh | Điều kiện / Luồng xử lý | Test Case phủ nhánh |
| --- | --- | --- | --- |
| **D1**: `if ("Thiếu".equals(row.status()))` | True | Mặt hàng bị thiếu, cần kiểm định số lượng thiếu | **TC02, TC03, TC04** |
| | False | Mặt hàng đủ, không cần kiểm định | **TC01, TC05, TC06** |
| **D2**: `if (row.shortageQty() <= 0)` (khi trạng thái là Thiếu) | True | Ném `IllegalArgumentException` (số lượng thiếu sai) | **TC03** |
| | False | Tiếp tục kiểm tra nghiệp vụ | **TC02, TC04** |
| **D3**: `if (row.shortageQty() > row.quantityOrdered())` | True | Ném `IllegalArgumentException` (thiếu vượt quá đặt) | **TC04** |
| | False | Hợp lệ, tiếp tục xử lý | **TC02** |
| **D4**: `if (orderOpt.isPresent())` (sau khi tìm đơn hàng) | True | Tiếp tục xử lý kiểm tra trạng thái đơn hàng | **TC01, TC02, TC03, TC04, TC05** |
| | False | Ném `SQLException` (đơn hàng không tồn tại) | **TC06** |
| **D5**: `if (order.getStatus() != SENT && order.getStatus() != CONFIRMED)` | True | Ném `IllegalArgumentException` (sai trạng thái xử lý) | **TC05** |
| | False | Cập nhật trạng thái đơn hàng thành `DELIVERED` | **TC01, TC02** |

> [!NOTE]
> Việc thiết kế 6 test case trên đảm bảo bao phủ 100% tất cả các nhánh điều kiện True/False khả thi của phương thức nghiệp vụ xác nhận đơn nhập kho.

---

## 5. Bảng các ca kiểm thử tự động

**Tên đầy đủ của Class kiểm thử tự động:** `com.oims.features.warehouse.process.ProcessPurchaseOrderControllerTest`

| Mã TC | Mục tiêu | Đầu vào | Kết quả mong đợi | Phương thức JUnit |
| --- | --- | --- | --- | --- |
| **TC01** | Xác nhận đơn hàng mới gửi và nhận đủ hàng | Trạng thái đơn `SENT`, trạng thái các mặt hàng `Đủ` | Trạng thái đơn chuyển thành `DELIVERED` | `approveReceiptWithSentOrderAndFullItemsMarksOrderDelivered` |
| **TC02** | Xác nhận đơn đã xác nhận và có thiếu hợp lệ | Trạng thái đơn `CONFIRMED`, mặt hàng `Thiếu`, thiếu = 3, đặt = 10 | Trạng thái đơn chuyển thành `DELIVERED` | `approveReceiptWithConfirmedOrderAndValidShortageMarksOrderDelivered` |
| **TC03** | Từ chối khi số lượng thiếu bằng 0 | Mặt hàng `Thiếu`, thiếu = 0 | Ném `IllegalArgumentException` | `approveReceiptRejectsShortageEqualToZero` |
| **TC04** | Từ chối khi số lượng thiếu vượt quá số lượng đặt | Mặt hàng `Thiếu`, thiếu = 11, đặt = 10 | Ném `IllegalArgumentException` | `approveReceiptRejectsShortageGreaterThanOrderedQuantity` |
| **TC05** | Từ chối đơn hàng đã được nhập kho | Trạng thái đơn `DELIVERED` | Ném `IllegalArgumentException` | `approveReceiptRejectsOrderInInvalidStatus` |
| **TC06** | Từ chối đơn hàng không tồn tại | `orderId = 999` | Ném ngoại lệ `SQLException` | `approveReceiptRejectsMissingOrder` |

---

## 6. Kiểm thử Use Case

### 6.1. Các Scenarios xác định
* **UC5-S1**: Nhân viên kho xác nhận nhập kho cho đơn hàng mới gửi (`SENT`) với toàn bộ mặt hàng đều đủ.
* **UC5-S2**: Nhân viên kho xác nhận nhập kho cho đơn hàng đã xác nhận (`CONFIRMED`) có ghi nhận mặt hàng bị thiếu hợp lệ.
* **UC5-S3**: Nhân viên kho nhập số lượng thiếu bằng 0 khi đánh dấu mặt hàng bị thiếu.
* **UC5-S4**: Nhân viên kho nhập số lượng thiếu lớn hơn số lượng đã đặt hàng.
* **UC5-S5**: Nhân viên cố gắng truy cập màn hình xác nhận cho đơn hàng đã duyệt nhập kho từ trước (`DELIVERED`).

### 6.2. Thiết kế Test Cases cho Use Case
Từ **5 Scenarios** trên, chúng tôi thiết kế **5 Test Cases** cho kiểm thử mức Use Case:

| Mã TC | Tên Test Case (Scenario tương ứng) | Các bước thực hiện | Dữ liệu thử nghiệm (Input) | Kết quả mong đợi | Kết quả thực tế (Actual) | Trạng thái |
| --- | --- | --- | --- | --- | --- | --- |
| **UC5-UT01** | Xác nhận đơn hàng đầy đủ hàng (UC5-S1) | 1. Đăng nhập tài khoản Nhân viên kho.<br>2. Chọn đơn hàng trạng thái `SENT`.<br>3. Đánh dấu tất cả mặt hàng là `Đủ`.<br>4. Nhấn "Xác nhận nhập kho". | - ID đơn hàng: `1` (Status: `SENT`) | Đơn hàng được cập nhật trạng thái thành `DELIVERED` (Đã nhập kho). | Hệ thống cập nhật thành công trạng thái đơn hàng và ghi nhận đủ. | **Pass** |
| **UC5-UT02** | Xác nhận đơn hàng thiếu hụt hợp lệ (UC5-S2) | 1. Đăng nhập tài khoản Nhân viên kho.<br>2. Chọn đơn hàng trạng thái `CONFIRMED`.<br>3. Chọn một mặt hàng, đánh dấu `Thiếu`, nhập số lượng thiếu là 3.<br>4. Nhấn "Xác nhận nhập kho". | - ID đơn hàng: `2` (Status: `CONFIRMED`) | Đơn hàng cập nhật thành công thành `DELIVERED`. Hệ thống lưu lại lịch sử thiếu hụt. | Đơn hàng được chuyển trạng thái thành công, thông tin thiếu hụt được ghi nhận vào CSDL. | **Pass** |
| **UC5-UT03** | Nhập số lượng thiếu bằng 0 (UC5-S3) | 1. Chọn đơn hàng `SENT`.<br>2. Chọn mặt hàng, đánh dấu `Thiếu`, nhập số lượng thiếu là 0.<br>3. Nhấn "Xác nhận". | - Trạng thái hàng: `Thiếu`<br>- Shortage Qty: `0` | Hệ thống hiển thị cảnh báo lỗi: "Số lượng thiếu phải lớn hơn 0 cho mặt hàng...". | Hệ thống chặn thao tác và hiển thị đúng thông báo lỗi trên UI. | **Pass** |
| **UC5-UT04** | Nhập số lượng thiếu vượt quá số lượng đặt (UC5-S4) | 1. Chọn đơn hàng `SENT`.<br>2. Chọn mặt hàng (đặt 10 chiếc), đánh dấu `Thiếu`, nhập số lượng thiếu là 11.<br>3. Nhấn "Xác nhận". | - Ordered Qty: `10`<br>- Shortage Qty: `11` | Hệ thống báo lỗi: "Số lượng thiếu không được vượt quá số lượng đặt". | Hiển thị cảnh báo lỗi nhập liệu không cho phép xác nhận. | **Pass** |
| **UC5-UT05** | Xác nhận đơn hàng đã nhập kho (UC5-S5) | 1. Chọn đơn hàng đã có trạng thái `DELIVERED`.<br>2. Cố gắng nhấn nút "Xác nhận nhập kho". | - ID đơn hàng: `5` (Status: `DELIVERED`) | Hệ thống hiển thị lỗi cảnh báo: "Chỉ có thể duyệt đơn hàng nhập kho ở trạng thái chưa xác nhận." | Hệ thống ngăn chặn truy cập và từ chối xử lý thao tác. | **Pass** |

---

## 7. Kết quả chạy kiểm thử tự động (Unit Test)

Lệnh chạy kiểm thử tự động:
```bash
mvnw -q -Dtest=ProcessPurchaseOrderControllerTest test
```

Kết quả: **Pass** (6/6 test cases passed).
