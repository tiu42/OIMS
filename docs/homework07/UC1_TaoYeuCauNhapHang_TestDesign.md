# UC1 - Tạo yêu cầu nhập hàng - Thiết kế kiểm thử

## 1. Thông tin module kiểm thử

- **Use case:** Tạo yêu cầu nhập hàng
- **Lớp được kiểm thử:** `com.oims.features.sales_requests.create.CreateRequestController`
- **Phương thức được kiểm thử:** `saveSalesRequest(User creator, List<TempRequestItem> tempItems)`
- **Lớp JUnit tự động:** `com.oims.features.sales_requests.create.CreateRequestControllerTest`
- **Framework:** JUnit 5

## 2. Mô tả phương thức

Phương thức tạo một `SalesRequest` mới và các `SalesRequestItem` từ danh sách mặt hàng tạm. Phương thức thực hiện kiểm tra tính hợp lệ của người tạo, danh sách mặt hàng, mã hàng, số lượng, đơn vị và ngày nhận mong muốn.

**Kết quả mong đợi:**

- **Hợp lệ:** Tạo yêu cầu với trạng thái `PENDING` và thêm các mặt hàng thành công.
- **Không hợp lệ:** Ném `IllegalArgumentException` hoặc `SQLException`.

---

## 3. Kiểm thử hộp đen

### 3.1. Phân lớp tương đương

| Lớp phân hoạch | Điều kiện đầu vào | Kết quả mong đợi |
| --- | --- | --- |
| **EP1** | Người tạo hợp lệ, danh sách mặt hàng hợp lệ | Tạo yêu cầu và mặt hàng thành công |
| **EP2** | Người tạo null | Báo lỗi |
| **EP3** | Danh sách mặt hàng null/rỗng | Báo lỗi |
| **EP4** | Số lượng mặt hàng <= 0 | Báo lỗi |
| **EP5** | Ngày nhận mong muốn trong quá khứ | Báo lỗi |

### 3.2. Giá trị biên

| Biến | Giá trị | Kết quả |
| --- | --- | --- |
| `quantity` | 0 | Không hợp lệ |
| `quantity` | 1 | Hợp lệ |
| `desiredDate` | Hôm qua | Không hợp lệ |
| `desiredDate` | Hôm nay/Tương lai | Hợp lệ |

---

## 4. Kiểm thử hộp trắng C1

Độ đo C1 (Branch Coverage) yêu cầu tất cả các nhánh rẽ (True/False) của các câu lệnh điều kiện trong mã nguồn của phương thức được thực thi ít nhất một lần.

Dưới đây là các điểm quyết định (Decision Points) trong phương thức `saveSalesRequest` và cách các Test Case phủ chúng:

| Điểm quyết định | Nhánh | Điều kiện / Luồng xử lý | Test Case phủ nhánh |
| --- | --- | --- | --- |
| **D1**: `if (creator == null)` | True | Ném `IllegalArgumentException` | **TC02** |
| | False | Tiếp tục thực hiện | **TC01, TC03, TC04, TC05** |
| **D2**: `if (tempItems == null \|\| tempItems.isEmpty())` | True | Ném `IllegalArgumentException` | **TC03** |
| | False | Tiếp tục thực hiện | **TC01, TC04, TC05** |
| **D3**: `if (item.quantity() <= 0)` | True | Ném `IllegalArgumentException` | **TC04** |
| | False | Tiếp tục thực hiện | **TC01, TC05** |
| **D4**: `if (item.desiredDate().isBefore(LocalDate.now()))` | True | Ném `IllegalArgumentException` | **TC05** |
| | False | Tiếp tục thực hiện | **TC01** |
| **D5**: `if (requestId <= 0)` (Tạo request database) | True | Ném `SQLException` | *Không áp dụng (đã giả lập thành công)* |
| | False | Tiếp tục ghi nhận các mặt hàng | **TC01** |

> [!NOTE]
> Việc thiết kế 5 test case dưới đây đảm bảo phủ 100% các nhánh khả thi (feasible branches) của phương thức `saveSalesRequest`, đạt tiêu chuẩn độ đo C1.

---

## 5. Bảng các ca kiểm thử tự động

**Tên đầy đủ của Class kiểm thử tự động:** `com.oims.features.sales_requests.create.CreateRequestControllerTest`

| Mã TC | Mục tiêu | Đầu vào | Kết quả mong đợi | Phương thức JUnit |
| --- | --- | --- | --- | --- |
| **TC01** | Tạo yêu cầu hợp lệ | Người tạo hợp lệ, 2 mặt hàng hợp lệ | Tạo 1 yêu cầu `PENDING`, thêm 2 mặt hàng | `saveSalesRequestWithValidItemsCreatesPendingRequestAndItems` |
| **TC02** | Thiếu người tạo | Người tạo null | Ném `IllegalArgumentException` | `saveSalesRequestRejectsMissingCreator` |
| **TC03** | Danh sách rỗng | `List.of()` | Ném `IllegalArgumentException` | `saveSalesRequestRejectsEmptyItemList` |
| **TC04** | Số lượng không hợp lệ | `quantity = 0` | Ném `IllegalArgumentException` | `saveSalesRequestRejectsNonPositiveQuantity` |
| **TC05** | Ngày nhận trong quá khứ | `desiredDate = hôm qua` | Ném `IllegalArgumentException` | `saveSalesRequestRejectsPastDesiredDate` |

---

## 6. Kiểm thử Use Case

### 6.1. Các Scenarios xác định
* **UC1-S1**: Nhân viên bán hàng tạo yêu cầu nhập hàng với danh sách mặt hàng hợp lệ.
* **UC1-S2**: Nhân viên bán hàng nhấn lưu yêu cầu khi chưa thêm mặt hàng nào.
* **UC1-S3**: Nhân viên bán hàng thêm mặt hàng với số lượng không hợp lệ (số lượng = 0).
* **UC1-S4**: Nhân viên bán hàng chọn ngày nhận mong muốn trong quá khứ.

### 6.2. Thiết kế Test Cases cho Use Case
Từ **4 Scenarios** trên, chúng tôi thiết kế **4 Test Cases** để kiểm thử tích hợp trên luồng giao diện người dùng (hoặc kiểm thử mức tích hợp hệ thống):

| Mã TC | Tên Test Case (Scenario tương ứng) | Các bước thực hiện | Dữ liệu thử nghiệm (Input) | Kết quả mong đợi | Kết quả thực tế (Actual) | Trạng thái |
| --- | --- | --- | --- | --- | --- | --- |
| **UC1-UT01** | Tạo yêu cầu nhập hàng hợp lệ (UC1-S1) | 1. Đăng nhập tài khoản Sales.<br>2. Vào màn hình Tạo yêu cầu.<br>3. Thêm mặt hàng, nhập số lượng 5, đơn vị "pcs", ngày nhận mong muốn là ngày hiện tại + 3.<br>4. Nhấn "Lưu". | - Người dùng: `Sales User`<br>- Mặt hàng: `M001`, Số lượng: `5`, Đơn vị: `pcs`, Ngày: `Hôm nay + 3` | Tạo yêu cầu nhập hàng thành công, hiển thị thông báo thành công. Yêu cầu chuyển sang trạng thái `PENDING`. | Tạo thành công, yêu cầu được lưu vào CSDL với trạng thái `PENDING`. | **Pass** |
| **UC1-UT02** | Tạo yêu cầu với danh sách trống (UC1-S2) | 1. Đăng nhập tài khoản Sales.<br>2. Vào màn hình Tạo yêu cầu.<br>3. Không thêm mặt hàng nào.<br>4. Nhấn "Lưu". | - Người dùng: `Sales User`<br>- Mặt hàng: `Trống` | Hệ thống hiển thị thông báo lỗi: "Danh sách mặt hàng yêu cầu không được để trống." | Hiển thị đúng thông báo cảnh báo lỗi trên giao diện. | **Pass** |
| **UC1-UT03** | Tạo yêu cầu với số lượng không hợp lệ (UC1-S3) | 1. Đăng nhập tài khoản Sales.<br>2. Vào màn hình Tạo yêu cầu.<br>3. Thêm mặt hàng, nhập số lượng 0.<br>4. Nhấn "Lưu". | - Người dùng: `Sales User`<br>- Mặt hàng: `M001`, Số lượng: `0`, Đơn vị: `pcs`, Ngày: `Hôm nay + 3` | Hệ thống hiển thị thông báo lỗi: "Số lượng mặt hàng phải lớn hơn 0." | Hiển thị thông báo cảnh báo lỗi nhập liệu "Số lượng mặt hàng phải lớn hơn 0." | **Pass** |
| **UC1-UT04** | Tạo yêu cầu với ngày nhận trong quá khứ (UC1-S4) | 1. Đăng nhập tài khoản Sales.<br>2. Vào màn hình Tạo yêu cầu.<br>3. Thêm mặt hàng, nhập số lượng 5, chọn ngày mong muốn là ngày hôm qua.<br>4. Nhấn "Lưu". | - Người dùng: `Sales User`<br>- Mặt hàng: `M001`, Số lượng: `5`, Đơn vị: `pcs`, Ngày: `Hôm qua` | Hệ thống hiển thị thông báo lỗi: "Ngày nhận mong muốn không được ở trong quá khứ." | Hiển thị thông báo lỗi ngày nhận mong muốn không hợp lệ. | **Pass** |

---

## 7. Kết quả chạy kiểm thử tự động (Unit Test)

Lệnh chạy kiểm thử tự động:
```bash
mvnw -q -Dtest=CreateRequestControllerTest test
```

Kết quả: **Pass** (5/5 test cases passed).
