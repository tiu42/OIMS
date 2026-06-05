# UC6 - Sửa yêu cầu nhập hàng - Thiết kế kiểm thử

## 1. Thông tin module kiểm thử

- **Use case:** Sửa yêu cầu nhập hàng
- **Lớp được kiểm thử:** `com.oims.features.sales_requests.edit.EditRequestController`
- **Phương thức được kiểm thử:** `updateSalesRequest(int requestId, User modifier, List<TempRequestItem> tempItems)`
- **Lớp JUnit tự động:** `com.oims.features.sales_requests.edit.EditRequestControllerTest`
- **Framework:** JUnit 5

## 2. Mô tả phương thức

Phương thức cập nhật danh sách mặt hàng của yêu cầu nhập hàng. Chỉ yêu cầu ở trạng thái `PENDING` (Chờ xử lý) mới được phép chỉnh sửa. Khi thông tin hợp lệ, hệ thống sẽ thực hiện xóa danh sách mặt hàng cũ và thêm các mặt hàng mới vào yêu cầu.

---

## 3. Kiểm thử hộp đen

### 3.1. Phân lớp tương đương

| Lớp phân hoạch | Điều kiện đầu vào | Kết quả mong đợi |
| --- | --- | --- |
| **EP1** | Yêu cầu trạng thái `PENDING`, người sửa và mặt hàng hợp lệ | Xóa các mặt hàng cũ, thêm các mặt hàng mới thành công |
| **EP2** | Người sửa null | Báo lỗi hệ thống |
| **EP3** | Yêu cầu không tồn tại trên hệ thống | Báo lỗi hệ thống |
| **EP4** | Yêu cầu không có trạng thái `PENDING` | Báo lỗi hệ thống |
| **EP5** | Ngày nhận mong muốn của mặt hàng ở trong quá khứ | Báo lỗi hệ thống |

### 3.2. Giá trị biên

| Biến | Giá trị | Kết quả |
| --- | --- | --- |
| `status` | `PENDING` | Được phép chỉnh sửa |
| `status` | `COMPLETED` | Bị từ chối chỉnh sửa |
| `desiredDate` | Hôm qua | Không hợp lệ |
| `desiredDate` | Tương lai | Hợp lệ |

---

## 4. Kiểm thử hộp trắng C1

Độ đo C1 (Branch Coverage) yêu cầu thực thi đầy đủ các nhánh True/False của các câu lệnh điều kiện trong phương thức `updateSalesRequest`:

| Điểm quyết định | Nhánh | Điều kiện / Luồng xử lý | Test Case phủ nhánh |
| --- | --- | --- | --- |
| **D1**: `if (modifier == null)` (khi kiểm tra người sửa) | True | Ném `IllegalArgumentException` (lỗi đăng nhập) | **TC02** |
| | False | Tiếp tục thực hiện | **TC01, TC03, TC04, TC05** |
| **D2**: `if (tempItems == null \|\| tempItems.isEmpty())` | True | Ném `IllegalArgumentException` | *Bao phủ gián tiếp qua validate đầu vào* |
| | False | Tiếp tục thực hiện | **TC01, TC03, TC04, TC05** |
| **D3**: `if (requestOpt.isEmpty())` (sau khi tìm yêu cầu gốc) | True | Ném `IllegalArgumentException` (yêu cầu không tồn tại) | **TC03** |
| | False | Tiếp tục thực hiện kiểm tra trạng thái | **TC01, TC04, TC05** |
| **D4**: `if (salesRequest.getStatus() != PENDING)` | True | Ném `IllegalArgumentException` (yêu cầu đã được xử lý) | **TC04** |
| | False | Cho phép sửa, tiếp tục kiểm tra tính hợp lệ mặt hàng | **TC01, TC05** |
| **D5**: `if (item.desiredDate().isBefore(LocalDate.now()))` | True | Ném `IllegalArgumentException` (ngày nhận trong quá khứ) | **TC05** |
| | False | Hợp lệ, tiến hành cập nhật (xóa cũ và ghi nhận mới) | **TC01** |

> [!NOTE]
> 5 test case trên đảm bảo bao phủ 100% tất cả các nhánh điều kiện True/False khả thi của phương thức cập nhật yêu cầu nhập hàng.

---

## 5. Bảng các ca kiểm thử tự động

**Tên đầy đủ của Class kiểm thử tự động:** `com.oims.features.sales_requests.edit.EditRequestControllerTest`

| Mã TC | Mục tiêu | Đầu vào | Kết quả mong đợi | Phương thức JUnit |
| --- | --- | --- | --- | --- |
| **TC01** | Sửa yêu cầu hợp lệ | Yêu cầu `PENDING`, 2 mặt hàng mới | Xóa mặt hàng cũ, thêm 2 mặt hàng mới | `updateSalesRequestWithPendingRequestReplacesExistingItems` |
| **TC02** | Thiếu thông tin người sửa | Người sửa null | Ném ngoại lệ `IllegalArgumentException` | `updateSalesRequestRejectsMissingModifier` |
| **TC03** | Yêu cầu không tồn tại | `requestId = 404` | Ném ngoại lệ `IllegalArgumentException` | `updateSalesRequestRejectsMissingRequest` |
| **TC04** | Sai trạng thái yêu cầu | Trạng thái yêu cầu là `COMPLETED` | Ném ngoại lệ `IllegalArgumentException` | `updateSalesRequestRejectsNonPendingRequest` |
| **TC05** | Ngày nhận trong quá khứ | `desiredDate = hôm qua` | Ném ngoại lệ `IllegalArgumentException` | `updateSalesRequestRejectsPastDesiredDate` |

---

## 6. Kiểm thử Use Case

### 6.1. Các Scenarios xác định
* **UC6-S1**: Người dùng chỉnh sửa một yêu cầu đang ở trạng thái `PENDING` bằng các mặt hàng hợp lệ mới.
* **UC6-S2**: Người dùng cố chỉnh sửa yêu cầu đã xử lý hoàn tất (`COMPLETED`).
* **UC6-S3**: Người dùng cố chỉnh sửa một yêu cầu không tồn tại trong hệ thống.
* **UC6-S4**: Người dùng sửa mặt hàng mới có ngày nhận mong muốn trong quá khứ.

### 6.2. Thiết kế Test Cases cho Use Case
Từ **4 Scenarios** trên, chúng tôi thiết kế **4 Test Cases** cho kiểm thử mức Use Case:

| Mã TC | Tên Test Case (Scenario tương ứng) | Các bước thực hiện | Dữ liệu thử nghiệm (Input) | Kết quả mong đợi | Kết quả thực tế (Actual) | Trạng thái |
| --- | --- | --- | --- | --- | --- | --- |
| **UC6-UT01** | Sửa yêu cầu thành công (UC6-S1) | 1. Đăng nhập tài khoản Sales.<br>2. Chọn một yêu cầu đang ở trạng thái `PENDING`.<br>3. Thay đổi danh sách mặt hàng cũ bằng 2 mặt hàng mới.<br>4. Nhấn "Lưu". | - ID yêu cầu: `1` (Status: `PENDING`) | Hệ thống cập nhật thành công, xóa các mặt hàng cũ của yêu cầu và thêm 2 mặt hàng mới vào CSDL. | Yêu cầu số 1 được sửa đổi thành công, mặt hàng cũ bị thay thế đúng như mong đợi. | **Pass** |
| **UC6-UT02** | Từ chối sửa yêu cầu đã xử lý (UC6-S2) | 1. Chọn yêu cầu ở trạng thái `COMPLETED`.<br>2. Cố gắng nhấn nút chỉnh sửa hoặc lưu. | - ID yêu cầu: `2` (Status: `COMPLETED`) | Hệ thống hiển thị thông báo lỗi: "Chỉ có thể chỉnh sửa yêu cầu ở trạng thái Chờ xử lý." | Hệ thống ngăn chặn chỉnh sửa và hiển thị đúng thông báo lỗi trạng thái. | **Pass** |
| **UC6-UT03** | Sửa yêu cầu không tồn tại (UC6-S3) | 1. Cố truy cập và sửa yêu cầu có mã sai. | - ID yêu cầu: `404` (Không tồn tại) | Hệ thống hiển thị thông báo lỗi: "Yêu cầu nhập hàng không tồn tại." | Trả về lỗi yêu cầu không tìm thấy trên hệ thống. | **Pass** |
| **UC6-UT04** | Nhập ngày mong muốn trong quá khứ (UC6-S4) | 1. Chọn yêu cầu `PENDING`.<br>2. Sửa thông tin ngày nhận mong muốn của mặt hàng thành ngày hôm qua.<br>3. Nhấn "Lưu". | - Ngày nhận: `Hôm qua` | Hệ thống hiển thị thông báo lỗi: "Ngày nhận mong muốn không được ở trong quá khứ." | Thao tác sửa bị chặn và hiển thị đúng thông báo lỗi ngày nhận. | **Pass** |

---

## 7. Kết quả chạy kiểm thử tự động (Unit Test)

Lệnh chạy kiểm thử tự động:
```bash
mvnw -q -Dtest=EditRequestControllerTest test
```

Kết quả: **Pass** (5/5 test cases passed).
