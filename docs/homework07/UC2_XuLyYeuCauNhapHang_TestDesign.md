# UC2 - Xử lý yêu cầu nhập hàng - Thiết kế kiểm thử

## 1. Thông tin module kiểm thử

- **Use case:** Xử lý yêu cầu nhập hàng
- **Lớp được kiểm thử:** `com.oims.features.sales_requests.process.PlanGenerationService`
- **Phương thức được kiểm thử:** `generatePlans(List<ItemDemand>, Map<String, ItemConfig>, Map<String, List<SiteStockTransportDTO>>)`
- **Lớp JUnit tự động:** `com.oims.features.sales_requests.process.PlanGenerationServiceTest`
- **Framework:** JUnit 5

## 2. Mô tả phương thức

Phương thức sinh các phương án đặt hàng dựa trên nhu cầu mặt hàng, tồn kho của các site, cấu hình ưu tiên site và phương thức giao hàng.

**Kết quả mong đợi:**

- **Nếu có đủ tồn kho:** Trả về danh sách `PlanDTO`.
- **Nếu bất kỳ mặt hàng nào không đủ nguồn cung:** Trả về danh sách rỗng.
- **Nếu site hỗ trợ cả tàu và máy bay:** Có các phương án giao hàng tương ứng.

---

## 3. Kiểm thử hộp đen

### 3.1. Phân lớp tương đương

| Lớp phân hoạch | Điều kiện đầu vào | Kết quả mong đợi |
| --- | --- | --- |
| **EP1** | Tổng tồn kho < nhu cầu | Không sinh phương án |
| **EP2** | Một site đủ hàng | Sinh phương án một site |
| **EP3** | Một site không đủ, nhiều site cộng lại đủ | Sinh phương án ghép nhiều site |
| **EP4** | Site hỗ trợ ship (tàu) và air (máy bay) | Sinh các lựa chọn phương thức giao hàng tương ứng |

### 3.2. Giá trị biên

| Biến | Giá trị | Kết quả |
| --- | --- | --- |
| `totalStock` | `demand - 1` | Không có phương án (Plan) |
| `totalStock` | `demand` | Có phương án (Plan) |
| `supportedDelivery` | Chỉ hỗ trợ SHIP (tàu) | Có đơn hàng SHIP |
| `supportedDelivery` | Hỗ trợ cả SHIP + AIR | Có đơn hàng SHIP và AIR |

---

## 4. Kiểm thử hộp trắng C1

Độ đo C1 (Branch Coverage) yêu cầu thực thi đầy đủ các nhánh True/False của các quyết định logic trong phương thức `generatePlans` và các hàm phụ trợ (`generateItemAllocationOptions`, `generateDeliveryCombinations`):

| Điểm quyết định | Nhánh | Điều kiện / Luồng xử lý | Test Case phủ nhánh |
| --- | --- | --- | --- |
| **D1**: `if (siteStockList == null)` (khi lấy kho lưu trữ cho mặt hàng) | True | Gán danh sách trống | *Không áp dụng trực tiếp (được giả lập đủ kho ở local)* |
| | False | Sử dụng danh sách kho tìm được | **TC01, TC02, TC03, TC04** |
| **D2**: `if (options.isEmpty())` (sau khi sinh phương án phân bổ) | True | Trả về danh sách plan trống | **TC01** |
| | False | Tiếp tục thực hiện tích Descartes và sinh phương án | **TC02, TC03, TC04** |
| **D3**: `if (remaining <= 0)` (khi duyệt kho của các site để gom đủ nhu cầu) | True | Ngắt việc gom kho (phân bổ tối giản) | **TC02, TC03, TC04** |
| | False | Tiếp tục trừ đi lượng tồn kho và duyệt tiếp | **TC01** *(duyệt hết vẫn thiếu)*, **TC03** *(duyệt tiếp site thứ 2)* |
| **D4**: `if (siteInfo.shipDays() > 0)` và `if (siteInfo.airDays() > 0)` | True/True | Thêm cả 2 phương thức vận chuyển (tàu và bay) | **TC04** |
| | True/False | Chỉ thêm phương thức tàu biển | **TC02, TC03** |

> [!NOTE]
> Việc thiết kế 4 test case trên đảm bảo bao phủ đầy đủ tất cả các nhánh điều kiện chính điều phối thuật toán phân bổ hàng từ các kho trung chuyển và lựa chọn hình thức giao vận.

---

## 5. Bảng các ca kiểm thử tự động

**Tên đầy đủ của Class kiểm thử tự động:** `com.oims.features.sales_requests.process.PlanGenerationServiceTest`

| Mã TC | Mục tiêu | Đầu vào | Kết quả mong đợi | Phương thức JUnit |
| --- | --- | --- | --- | --- |
| **TC01** | Không đủ tồn kho | Nhu cầu 10, tồn kho 4 | Danh sách phương án rỗng | `generatePlansReturnsEmptyListWhenAnyDemandCannotBeFulfilled` |
| **TC02** | Một site đủ hàng | Nhu cầu 10, tồn kho 15 | Có phương án, đặt từ SITE01, số lượng 10 | `generatePlansCreatesPlanFromSingleSiteStock` |
| **TC03** | Ghép nhiều site | Nhu cầu 10, SITE01 có 6, SITE02 có 5 | Tổng phân bổ = 10, số site = 2 | `generatePlansCombinesMultipleSitesWhenOneSiteIsNotEnough` |
| **TC04** | Nhiều phương thức giao vận | Site có shipDays và airDays > 0 | Có phương án cho cả tàu biển và máy bay | `generatePlansCreatesDeliveryAlternativesWhenSiteSupportsShipAndAir` |

---

## 6. Kiểm thử Use Case

### 6.1. Các Scenarios xác định
* **UC2-S1**: Yêu cầu nhập hàng có mặt hàng và hệ thống tìm được site có đủ tồn kho.
* **UC2-S2**: Tổng tồn kho của tất cả các site không đáp ứng đủ nhu cầu nhập hàng.
* **UC2-S3**: Một site đơn lẻ không đủ hàng, cần ghép tồn kho từ nhiều site để đáp ứng.
* **UC2-S4**: Site kho có cấu hình hỗ trợ nhiều cách thức vận chuyển (ship và air).

### 6.2. Thiết kế Test Cases cho Use Case
Từ **4 Scenarios** trên, chúng tôi thiết kế **4 Test Cases** cho kiểm thử mức Use Case:

| Mã TC | Tên Test Case (Scenario tương ứng) | Các bước thực hiện | Dữ liệu thử nghiệm (Input) | Kết quả mong đợi | Kết quả thực tế (Actual) | Trạng thái |
| --- | --- | --- | --- | --- | --- | --- |
| **UC2-UT01** | Sinh phương án từ một site đơn lẻ (UC2-S1) | 1. Đăng nhập tài khoản Quản trị kế hoạch.<br>2. Chọn yêu cầu nhập hàng cần xử lý.<br>3. Yêu cầu hệ thống sinh phương án phân bổ hàng. | - Nhu cầu: 10 sản phẩm.<br>- Kho SITE01: có sẵn 15 sản phẩm. | Hệ thống sinh ra phương án mua hàng từ SITE01 với số lượng 10. | Hệ thống đề xuất phương án lấy từ SITE01 với đúng số lượng 10 sản phẩm. | **Pass** |
| **UC2-UT02** | Xử lý khi thiếu hụt tồn kho toàn cục (UC2-S2) | 1. Đăng nhập tài khoản Quản trị kế hoạch.<br>2. Chọn yêu cầu nhập hàng.<br>3. Yêu cầu hệ thống đề xuất phương án. | - Nhu cầu: 10 sản phẩm.<br>- Kho SITE01: chỉ có 4 sản phẩm. | Hệ thống thông báo không thể sinh phương án do thiếu nguồn cung tồn kho. | Hệ thống trả về danh sách trống và đưa ra thông báo không đủ nguồn cung. | **Pass** |
| **UC2-UT03** | Ghép hàng từ nhiều site trung chuyển (UC2-S3) | 1. Đăng nhập tài khoản Quản trị kế hoạch.<br>2. Thực hiện chức năng xử lý tự động phân bổ hàng. | - Nhu cầu: 10 sản phẩm.<br>- SITE01: có 6 sản phẩm.<br>- SITE02: có 5 sản phẩm. | Hệ thống tự động ghép kho và đề xuất 1 phương án chứa 2 đơn hàng: SITE01 (6 sản phẩm) và SITE02 (4 sản phẩm). | Sinh ra phương án tối ưu kết hợp lấy hàng từ SITE01 và SITE02 với tổng số lượng 10. | **Pass** |
| **UC2-UT04** | Đề xuất đa lựa chọn phương thức giao vận (UC2-S4) | 1. Đăng nhập tài khoản Quản trị kế hoạch.<br>2. Thực hiện sinh phương án cho yêu cầu nhập hàng. | - Nhu cầu: 4 sản phẩm.<br>- SITE01: có 10 sản phẩm (hỗ trợ cả Tàu và Máy bay). | Hệ thống đưa ra 2 phương án đề xuất độc lập tương ứng với 2 hình thức SHIP và AIR. | Đề xuất thành công 2 phương án giao vận khác nhau để kế hoạch viên lựa chọn. | **Pass** |

---

## 7. Kết quả chạy kiểm thử tự động (Unit Test)

Lệnh chạy kiểm thử tự động:
```bash
mvnw -q -Dtest=PlanGenerationServiceTest test
```

Kết quả: **Pass** (4/4 test cases passed).
