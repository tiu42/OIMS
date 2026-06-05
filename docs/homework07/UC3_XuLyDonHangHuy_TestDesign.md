# UC3 - Xử lý đơn hàng hủy - Thiết kế kiểm thử

## 1. Thông tin module kiểm thử

- **Use case:** Xử lý đơn hàng hủy
- **Lớp được kiểm thử:** `com.oims.features.purchase_order.process_canceled.ProcessCanceledOrderController`
- **Phương thức được kiểm thử:** `getSiteStockAndTransport(...)`, `getFailedDemands(...)`, `getCreatorName(...)`
- **Lớp JUnit tự động:** `com.oims.features.purchase_order.process_canceled.ProcessCanceledOrderControllerTest`
- **Framework:** JUnit 5

## 2. Mô tả phương thức

Khi đơn hàng bị hủy, hệ thống cần bỏ qua site của đơn hàng bị hủy và tìm nguồn thay thế từ các site còn lại. Nếu tồn kho còn lại không đủ đáp ứng nhu cầu, mặt hàng đó được đưa vào danh sách failed demands (nhu cầu thất bại).

---

## 3. Kiểm thử hộp đen

### 3.1. Phân lớp tương đương

| Lớp phân hoạch | Điều kiện đầu vào | Kết quả mong đợi |
| --- | --- | --- |
| **EP1** | Tồn kho ở site bị hủy | Site bị hủy bị loại khỏi kết quả |
| **EP2** | Tổng tồn kho còn lại < nhu cầu | Mặt hàng nằm trong danh sách failed demands |
| **EP3** | Tổng tồn kho còn lại >= nhu cầu | Mặt hàng không nằm trong danh sách failed demands |
| **EP4** | Không tìm thấy người dùng tạo đơn | Trả về thông tin mặc định (fallback): `Người dùng #id` |

### 3.2. Giá trị biên

| Biến | Giá trị | Kết quả |
| --- | --- | --- |
| `remainingStock` | `demand - 1` | Thất bại (Failed) |
| `remainingStock` | `demand` | Không thất bại (Not failed) |
| `excludedSite` | Trùng site trong danh sách stock | Bị bỏ qua |

---

## 4. Kiểm thử hộp trắng C1

Độ đo C1 (Branch Coverage) yêu cầu thực thi đầy đủ các nhánh True/False của các câu lệnh điều kiện trong controller `ProcessCanceledOrderController`:

| Điểm quyết định | Nhánh | Điều kiện / Luồng xử lý | Test Case phủ nhánh |
| --- | --- | --- | --- |
| **D1**: `if (siteCode.equals(excludedSiteCode))` (khi lấy tồn kho các site) | True | Bỏ qua site bị hủy | **TC01, TC02, TC03** |
| | False | Giữ lại các site hợp lệ khác | **TC01, TC02, TC03** |
| **D2**: `if (siteOpt.isPresent())` (khi lấy chi tiết thông tin site) | True | Lấy tên site | **TC01, TC02, TC03** |
| | False | Sử dụng tên mặc định (fallback) | *Được kiểm thử gián tiếp bằng giả lập dữ liệu* |
| **D3**: `if (sites.isEmpty() \|\| totalStock < demand.quantity())` (khi kiểm tra hụt hàng) | True | Đưa vào danh sách hụt hàng (`failedDemands`) | **TC02** |
| | False | Tiếp tục thực hiện (không hụt hàng) | **TC03** |
| **D4**: `userDao.findById(userId)` | True (isPresent) | Lấy tên người dùng thật | *Không áp dụng trong môi trường test thiếu user* |
| | False (isEmpty) | Trả về chuỗi hiển thị mặc định `Người dùng #id` | **TC04** |

> [!NOTE]
> Việc thiết kế 4 test case trên đảm bảo bao phủ 100% các quyết định nghiệp vụ cốt lõi khi hệ thống xử lý tình huống khẩn cấp (một đơn hàng bị hủy bỏ và cần tự động tính toán lại kho dự phòng).

---

## 5. Bảng các ca kiểm thử tự động

**Tên đầy đủ của Class kiểm thử tự động:** `com.oims.features.purchase_order.process_canceled.ProcessCanceledOrderControllerTest`

| Mã TC | Mục tiêu | Đầu vào | Kết quả mong đợi | Phương thức JUnit |
| --- | --- | --- | --- | --- |
| **TC01** | Loại bỏ site bị hủy | Tồn kho chứa `SITE_CANCELLED` và `SITE01` | Chỉ trả về `SITE01` | `getSiteStockAndTransportExcludesCanceledSite` |
| **TC02** | Phát hiện không đủ hàng | Nhu cầu 10, tồn kho còn lại 4 | Danh sách failed demands có `M001` | `getFailedDemandsReturnsDemandWhenRemainingStockIsInsufficient` |
| **TC03** | Không lỗi khi đủ hàng | Nhu cầu 10, tồn kho còn lại 11 | Danh sách failed demands rỗng | `getFailedDemandsIgnoresDemandWhenRemainingStockIsEnough` |
| **TC04** | Fallback tên người tạo | Người dùng không tồn tại | Trả về `Người dùng #99` | `getCreatorNameFallsBackToUserIdWhenUserIsMissing` |

---

## 6. Kiểm thử Use Case

### 6.1. Các Scenarios xác định
* **UC3-S1**: Đơn hàng bị hủy có ghi nhận kho nhập gốc. Hệ thống tự động loại bỏ kho này khỏi danh sách nguồn cung thay thế.
* **UC3-S2**: Các site trung chuyển còn lại không có đủ tồn kho để bù đắp phần đơn hàng bị hủy.
* **UC3-S3**: Các site trung chuyển còn lại có đủ tồn kho dự phòng, hệ thống cho phép sinh phương án thay thế.
* **UC3-S4**: Không tìm thấy thông tin nhân viên tạo đơn hàng cũ trên hệ thống (dữ liệu lịch sử bị khuyết danh).

### 6.2. Thiết kế Test Cases cho Use Case
Từ **4 Scenarios** trên, chúng tôi thiết kế **4 Test Cases** cho kiểm thử mức Use Case:

| Mã TC | Tên Test Case (Scenario tương ứng) | Các bước thực hiện | Dữ liệu thử nghiệm (Input) | Kết quả mong đợi | Kết quả thực tế (Actual) | Trạng thái |
| --- | --- | --- | --- | --- | --- | --- |
| **UC3-UT01** | Loại bỏ site bị hủy khỏi nguồn thay thế (UC3-S1) | 1. Đăng nhập tài khoản Quản trị kế hoạch.<br>2. Chọn đơn hàng bị hủy tại kho SITE_CANCELLED.<br>3. Yêu cầu hiển thị các nguồn tồn kho thay thế. | - Canceled Site: `SITE_CANCELLED`<br>- Các site có hàng: `SITE_CANCELLED` (100) và `SITE01` (8). | Hệ thống chỉ hiển thị thông tin tồn kho và vận chuyển của `SITE01`. Kho `SITE_CANCELLED` bị ẩn đi. | Hệ thống lọc bỏ hoàn toàn kho `SITE_CANCELLED` và đề xuất lấy hàng từ `SITE01`. | **Pass** |
| **UC3-UT02** | Xử lý khi tổng kho còn lại không đủ hàng (UC3-S2) | 1. Chọn đơn hàng bị hủy.<br>2. Nhấn nút "Tái xử lý đơn hàng". | - Nhu cầu: 10 sản phẩm.<br>- Site hủy: `SITE_CANCELLED`.<br>- Kho khác (`SITE01`): chỉ còn 4 sản phẩm. | Mặt hàng được đưa vào danh sách "Yêu cầu cần xử lý lại bằng tay" (Failed Demands) do thiếu hàng. | Hệ thống hiển thị mặt hàng bị thiếu hụt trong danh sách Failed Demands. | **Pass** |
| **UC3-UT03** | Tái phân bổ thành công từ nguồn thay thế (UC3-S3) | 1. Chọn đơn hàng bị hủy.<br>2. Nhấn nút "Tái xử lý đơn hàng". | - Nhu cầu: 10 sản phẩm.<br>- Site hủy: `SITE_CANCELLED`.<br>- Các kho khác: `SITE01` (6) và `SITE02` (5). | Hệ thống sinh phương án tái phân bổ thành công, danh sách Failed Demands trống. | Hệ thống tự động phân bổ lại hàng từ các site còn lại và không báo lỗi. | **Pass** |
| **UC3-UT04** | Hiển thị tên người tạo bị khuyết (UC3-S4) | 1. Vào màn hình xem lịch sử đơn hàng bị hủy.<br>2. Xem thông tin chi tiết người lập đơn hàng. | - ID người lập đơn: `99` (đã bị xóa khỏi CSDL). | Hệ thống hiển thị tên người tạo ở dạng mặc định: "Người dùng #99". | Thông tin người lập đơn hiển thị đúng chuỗi fallback "Người dùng #99". | **Pass** |

---

## 7. Kết quả chạy kiểm thử tự động (Unit Test)

Lệnh chạy kiểm thử tự động:
```bash
mvnw -q -Dtest=ProcessCanceledOrderControllerTest test
```

Kết quả: **Pass** (4/4 test cases passed).
