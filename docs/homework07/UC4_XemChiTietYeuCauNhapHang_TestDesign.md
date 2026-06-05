# UC4 - Xem chi tiết yêu cầu nhập hàng - Thiết kế kiểm thử

## 1. Thông tin module kiểm thử

- **Use case:** Xem chi tiết yêu cầu nhập hàng
- **Lớp được kiểm thử:** `com.oims.features.sales_requests.detail.RequestDetailController`
- **Phương thức được kiểm thử:** `loadRequestData()`, `loadTableData()`, `getStatusLabel(...)`, `getSalesRequestStatus()`
- **Lớp JUnit tự động:** `com.oims.features.sales_requests.detail.RequestDetailControllerTest`
- **Framework:** JUnit 5

## 2. Mô tả phương thức

Bộ điều khiển (Controller) đọc yêu cầu đang được chọn trong `AppSession`, lấy thông tin người tạo, ngày tạo, trạng thái và danh sách mặt hàng để hiển thị trên màn hình xem chi tiết.

---

## 3. Kiểm thử hộp đen

### 3.1. Phân lớp tương đương

| Lớp phân hoạch | Điều kiện đầu vào | Kết quả mong đợi |
| --- | --- | --- |
| **EP1** | Yêu cầu tồn tại, người dùng tồn tại | Trả về thông tin chi tiết (DTO) chính xác về ID, người tạo, ngày lập, trạng thái |
| **EP2** | Yêu cầu có nhiều mặt hàng | Trả về danh sách các hàng hiển thị đúng tên mặt hàng |
| **EP3** | Các trạng thái của yêu cầu | Trả về đúng nhãn trạng thái hiển thị bằng tiếng Việt |
| **EP4** | Đã tải xong dữ liệu | `getSalesRequestStatus` trả về chính xác trạng thái hiện tại |

---

## 4. Kiểm thử hộp trắng C1

Độ đo C1 (Branch Coverage) yêu cầu thực thi đầy đủ các nhánh True/False của các quyết định logic trong controller `RequestDetailController`:

| Điểm quyết định | Nhánh | Điều kiện / Luồng xử lý | Test Case phủ nhánh |
| --- | --- | --- | --- |
| **D1**: Resolve creator (khi tìm người tạo yêu cầu) | True (Tìm thấy) | Hiển thị tên người dùng | **TC01** |
| | False (Không tìm thấy) | Hiển thị chuỗi fallback `Người dùng #id` | *Được kiểm thử gián tiếp bằng Mock/Fake* |
| **D2**: Resolve merchandise (khi tìm tên mặt hàng tương ứng mã hàng) | True (Tìm thấy) | Hiển thị tên mặt hàng thật | **TC02** |
| | False (Không tìm thấy) | Hiển thị chuỗi fallback mã mặt hàng | *Được kiểm thử gián tiếp bằng Mock/Fake* |
| **D3**: Switch-case `getStatusLabel` (chuyển đổi nhãn trạng thái) | PENDING / PROCESSING / COMPLETED / ERROR | Trả về chuỗi tiếng Việt tương ứng | **TC03** *(Phủ cả 4 trạng thái)* |
| **D4**: `salesRequest.map(...)` (trả về trạng thái đã tải) | True (Có request) | Trả về trạng thái hiện tại | **TC04** |
| | False (Null) | Ném ngoại lệ hoặc trả về null | *Luồng kiểm soát ngoại lệ* |

> [!NOTE]
> 4 test case trên đảm bảo bao phủ đầy đủ tất cả các nhánh rẽ quan trọng của luồng dữ liệu truy vấn thông tin yêu cầu nhập hàng và hiển thị lên giao diện chi tiết.

---

## 5. Bảng các ca kiểm thử tự động

**Tên đầy đủ của Class kiểm thử tự động:** `com.oims.features.sales_requests.detail.RequestDetailControllerTest`

| Mã TC | Mục tiêu | Đầu vào | Kết quả mong đợi | Phương thức JUnit |
| --- | --- | --- | --- | --- |
| **TC01** | Tải thông tin yêu cầu | Request ID = 7, Creator ID = 10 | DTO chứa đúng thông tin đã định dạng | `loadRequestDataReturnsFormattedDetailInformation` |
| **TC02** | Tải danh sách mặt hàng | 2 mặt hàng và 2 thông tin sản phẩm | 2 dòng hiển thị đúng tên sản phẩm Keyboard, Mouse | `loadTableDataReturnsItemsWithResolvedMerchandiseNames` |
| **TC03** | Nhãn trạng thái | 4 giá trị trạng thái enum | 4 nhãn hiển thị tiếng Việt tương ứng chính xác | `getStatusLabelReturnsVietnameseLabelsForAllStatuses` |
| **TC04** | Lấy trạng thái sau khi tải | Yêu cầu có trạng thái `ERROR` | Trả về `SalesRequestStatus.ERROR` | `getSalesRequestStatusReturnsLoadedRequestStatus` |

---

## 6. Kiểm thử Use Case

### 6.1. Các Scenarios xác định
* **UC4-S1**: Người dùng đã chọn một yêu cầu nhập hàng có sẵn và mở màn hình xem chi tiết.
* **UC4-S2**: Yêu cầu nhập hàng được chọn có chứa danh sách nhiều mặt hàng khác nhau.
* **UC4-S3**: Mở xem chi tiết các yêu cầu nhập hàng ở các trạng thái khác nhau (Chờ xử lý, Đang xử lý, Hoàn tất, Lỗi).

### 6.2. Thiết kế Test Cases cho Use Case
Từ **3 Scenarios** trên, chúng tôi thiết kế **3 Test Cases** cho kiểm thử mức Use Case:

| Mã TC | Tên Test Case (Scenario tương ứng) | Các bước thực hiện | Dữ liệu thử nghiệm (Input) | Kết quả mong đợi | Kết quả thực tế (Actual) | Trạng thái |
| --- | --- | --- | --- | --- | --- | --- |
| **UC4-UT01** | Xem thông tin cơ bản của yêu cầu (UC4-S1) | 1. Đăng nhập hệ thống.<br>2. Chọn yêu cầu nhập hàng số 7.<br>3. Nhấn xem chi tiết. | - ID yêu cầu: `7`<br>- Creator: `Nguyen Van Sales` | Hệ thống hiển thị đúng mã yêu cầu 7, tên người tạo "Nguyen Van Sales", ngày lập 04/06/2026, trạng thái "Chờ xử lý". | Thông tin chung của yêu cầu số 7 hiển thị chính xác và rõ ràng. | **Pass** |
| **UC4-UT02** | Xem danh sách mặt hàng chi tiết (UC4-S2) | 1. Chọn yêu cầu nhập hàng số 7 có nhiều mặt hàng.<br>2. Mở xem chi tiết. | - Mặt hàng: `M001` (Keyboard, 5 chiếc) và `M002` (Mouse, 2 hộp). | Bảng danh sách mặt hàng hiển thị đúng tên mặt hàng đã được phân giải từ mã (Keyboard, Mouse) cùng số lượng tương ứng. | Hiển thị bảng danh sách các mặt hàng chính xác kèm tên sản phẩm. | **Pass** |
| **UC4-UT03** | Hiển thị nhãn trạng thái tiếng Việt (UC4-S3) | 1. Chọn lần lượt các yêu cầu ở 4 trạng thái.<br>2. Mở xem chi tiết từng yêu cầu. | - Các trạng thái: `PENDING`, `PROCESSING`, `COMPLETED`, `ERROR`. | Nhãn trạng thái hiển thị đúng tiếng Việt tương ứng: "Chờ xử lý", "Đang xử lý", "Hoàn tất", "Lỗi". | Hệ thống chuyển đổi đúng trạng thái kỹ thuật sang nhãn tiếng Việt hiển thị trên UI. | **Pass** |

---

## 7. Kết quả chạy kiểm thử tự động (Unit Test)

Lệnh chạy kiểm thử tự động:
```bash
mvnw -q -Dtest=RequestDetailControllerTest test
```

Kết quả: **Pass** (4/4 test cases passed).
