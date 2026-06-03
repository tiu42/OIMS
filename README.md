# OIMS — Order and Inventory Management System

Hệ thống Quản lý Đơn hàng và Hàng tồn kho (OIMS - Order and Inventory Management System) của **GLOCERIMEX** là một ứng dụng máy để bàn được xây dựng bằng công nghệ **JavaFX (Java 21)** và cơ sở dữ liệu **MySQL**, giúp tối ưu hóa luồng cung ứng, xử lý các yêu cầu nhập hàng và lập kế hoạch mua hàng quốc tế một cách tự động, chính xác và hiệu quả.

---

## I. Luồng hoạt động và Thiết kế kiến trúc (SOLID Compliant)

Hệ thống được thiết kế theo mô hình **MVC (Model-View-Controller)** kết hợp với mô hình **Service-DAO (Data Access Object)** truyền thống của Java. Nhằm nâng cao khả năng mở rộng, bảo trì và viết kiểm thử đơn vị (Unit Test), mã nguồn của hệ thống đã được refactor toàn diện để tuân thủ 5 nguyên lý thiết kế **SOLID**:

```
+-----------------------------------------------------------+
|                        VIEW (JavaFX)                      |
| (Hiển thị giao diện UI, các bảng dữ liệu, nút tương tác)  |
+-----------------------------+-----------------------------+
                              | (Gọi controller nghiệp vụ)
                              v
+-----------------------------------------------------------+
|                     CONTROLLER LAYER                      |
|  (Nhận yêu cầu từ View, điều phối xử lý nghiệp vụ)       |
+-----------------------------+-----------------------------+
                              | (Lấy DAO & Service qua Factory)
                              v
+-----------------------------------------------------------+
|          FACTORY LAYER (DaoFactory, ServiceFactory)       |
|    (Giải quyết sự phụ thuộc, hỗ trợ Mocking khi Test)     |
+-----------------------------+-----------------------------+
                              |
       +----------------------+----------------------+
       | (Sử dụng Service)                           | (Sử dụng DAO)
       v                                             v
+-----------------------------+             +-----------------------------+
|        SERVICE LAYER        |             |          DAO LAYER          |
|  (Logic thuật toán phức tạp |             | (Truy vấn CSDL, tương tác   |
|   như Plan Generation)      |             |  MySQL thông qua JDBC)      |
+-----------------------------+             +-----------------------------+
```

Dưới đây là mô tả chi tiết về luồng hoạt động và thiết kế của 6 tính năng cốt lõi:

### 1. Tạo yêu cầu nhập hàng (Create Sales/Purchase Request)
*   **Người thực hiện:** Nhân viên Kinh doanh (Sales Staff).
*   **Luồng hoạt động:** Nhân viên Sales chọn các mặt hàng cần nhập từ danh sách, điền số lượng mong muốn, đơn vị tính và ngày mong muốn nhận hàng. Hệ thống lưu yêu cầu nhập hàng dưới trạng thái `PENDING` (Chờ xử lý).
*   **Thiết kế SOLID (DIP):** View UI `CreateRequestView` thu thập dữ liệu và chuyển đổi sang dạng DTO để gọi `CreateRequestController`. Controller này làm việc trực tiếp với các interface abstractions `IMerchandiseDao`, `ISalesRequestDao`, và `ISalesRequestItemDao` được phân phối bởi `DaoFactory`. Điều này giúp bộ điều khiển hoàn toàn độc lập với các cài đặt cơ sở dữ liệu cụ thể và dễ dàng mock khi test.

### 2. Xử lý yêu cầu nhập hàng (Process Request)
*   **Người thực hiện:** Nhân viên Mua hàng nước ngoài (Overseas Order Staff).
*   **Luồng hoạt động:** Nhân viên Overseas Order xem danh sách yêu cầu chờ xử lý. Hệ thống tự động phân tích tồn kho hiện tại ở các site đối tác và thời gian vận chuyển (đường biển, đường hàng không) để tính toán, sinh ra các phương án mua hàng (Plans) đề xuất tối ưu nhất. Người dùng có thể tùy chỉnh các cấu hình ưu tiên (site ưu tiên, phương tiện ưu tiên) và chọn duyệt một phương án tối ưu để tạo các đơn hàng nháp (Purchase Orders) gửi đối tác.
*   **Thiết kế SOLID (OCP & DIP):**
    *   **DIP:** Logic thuật toán và lưu trữ được tách biệt thành `IPlanGenerationService` và `IPlanPersistenceService`, được cung cấp thông qua `ServiceFactory`.
    *   **OCP (Strategy Pattern):** Các phương án sinh ra cần được xếp hạng. Thay vì hardcode thuật toán sắp xếp độ ưu tiên, hệ thống định nghĩa interface `PlanSortingStrategy` và triển khai mặc định qua `DefaultPlanSortingStrategy` (ưu tiên site mong muốn -> phương tiện mong muốn -> gom cụm ít site nhất -> tổng tồn kho lớn nhất). Thiết kế này giúp hệ thống dễ dàng cấu hình hoặc thêm mới các thuật toán sắp xếp phương án khác (ví dụ: `CheapestPlanSortingStrategy` theo giá rẻ nhất, `FastestPlanSortingStrategy` theo thời gian nhanh nhất) mà không cần chỉnh sửa mã nguồn của `PlanGenerationService`.

### 3. Xử lý đơn hàng hủy (Process Canceled Purchase Order)
*   **Người thực hiện:** Nhân viên Mua hàng nước ngoài (Overseas Order Staff).
*   **Luồng hoạt động:** Trong trường hợp đơn hàng gửi đối tác bị đối tác từ chối hoặc hủy bỏ, nhân viên sẽ kích hoạt chức năng xử lý đơn hàng hủy. Hệ thống sẽ tải lại các mặt hàng của đơn hàng đó, phân tích lượng tồn kho ở các site đối tác thay thế khác và sinh ra phương án phân bổ bù đắp mới.
*   **Thiết kế SOLID (Reusability):** `ProcessCanceledOrderController` được thiết kế để tái sử dụng trực tiếp các dịch vụ lõi `IPlanGenerationService` và `IPlanPersistenceService` thông qua `ServiceFactory`, đảm bảo tính nhất quán của thuật toán sinh phương án trong toàn hệ thống.

### 4. Xem chi tiết yêu cầu nhập hàng (View Request Details)
*   **Người thực hiện:** Tất cả người dùng (Sales, Overseas, Warehouse, Admin).
*   **Luồng hoạt động:** Người dùng chọn một yêu cầu từ danh sách để xem thông tin chi tiết: người tạo, ngày tạo, trạng thái xử lý hiện tại và danh sách bảng các mặt hàng yêu cầu (mã mặt hàng, tên mặt hàng, số lượng, đơn vị và ngày nhận mong muốn).
*   **Thiết kế SOLID (ISP):** Sử dụng `RequestDetailController` để chỉ truy xuất thông tin cụ thể cần hiển thị. Nhờ tách biệt các interface nhỏ như `ISalesRequestDao`, `ISalesRequestItemDao`, controller không bị ép buộc phải phụ thuộc vào các phương thức ghi/xóa hoặc thao tác không liên quan của các thực thể khác.

### 5. Xác nhận đơn hàng nhập kho (Confirm Purchase Order / Warehouse Import)
*   **Người thực hiện:** Nhân viên thủ kho (Warehouse Staff).
*   **Luồng hoạt động:** Khi hàng thực tế được chuyển về kho, thủ kho đối chiếu số lượng thực nhận với đơn hàng gốc (Purchase Order). Với mỗi dòng sản phẩm, thủ kho chọn trạng thái nhận hàng ("Đủ" hoặc "Thiếu"). Nếu chọn "Thiếu", thủ kho bắt buộc phải nhập số lượng thiếu. Hệ thống kiểm tra tính hợp lệ của số lượng thiếu (phải lớn hơn 0 và không vượt quá số lượng đặt hàng), sau đó cập nhật đơn hàng sang trạng thái `DELIVERED` (Đã nhập kho) và in ra báo cáo kiểm hàng.
*   **Thiết kế SOLID (SRP & DIP):**
    *   **SRP:** Nguyên bản lớp `ProcessPurchaseOrderView` ôm đồm cả giao diện JavaFX lẫn logic kiểm tra điều kiện dữ liệu và cập nhật CSDL. Hiện tại đã được refactor tách biệt hoàn toàn: giao diện UI nằm ở `ProcessPurchaseOrderView`, trong khi toàn bộ logic kiểm tra hợp lệ, tính toán số lượng thực nhận và cập nhật DB được chuyển sang [ProcessPurchaseOrderController.java](src/main/java/com/oims/features/warehouse/process/ProcessPurchaseOrderController.java).
    *   View giao tiếp với Controller bằng record DTO [ItemShortageResult](src/main/java/com/oims/features/warehouse/process/ProcessPurchaseOrderController.java#L87-L93) thuần túy. Controller không hề sử dụng bất kỳ thư viện UI JavaFX nào (như `SimpleStringProperty` hay `Button`), đảm bảo tính độc lập và khả năng tái sử dụng.

### 6. Sửa yêu cầu nhập hàng (Edit Request)
*   **Người thực hiện:** Nhân viên Kinh doanh (Sales Staff).
*   **Luồng hoạt động:** Đối với các yêu cầu nhập hàng đang ở trạng thái `PENDING` (Chờ xử lý), nhân viên Sales có quyền chỉnh sửa thông tin các dòng hàng, thay đổi số lượng, đơn vị tính hoặc ngày mong muốn nhận hàng trước khi phòng mua hàng tiến hành xử lý đơn hàng.
*   **Thiết kế SOLID (DIP):** Logic chỉnh sửa yêu cầu được thực hiện an toàn trong `EditRequestController` thông qua cơ chế Transaction của JDBC, truy cập CSDL qua các interface DAO từ `DaoFactory` nhằm đảm bảo dữ liệu được cập nhật đồng bộ và toàn vẹn.

---

## II. Hướng dẫn cài đặt và Khởi chạy ứng dụng

Để khởi chạy dự án này trên môi trường local của bạn sau khi clone từ GitHub, hãy làm theo các bước hướng dẫn chi tiết dưới đây:

### 1. Yêu cầu môi trường chuẩn bị
*   **Java Development Kit (JDK):** Phiên bản **21** trở lên.
*   **Apache Maven:** Phiên bản **3.8** trở lên.
*   **MySQL Server:** Phiên bản **8.0** trở lên.

### 2. Thiết lập Cơ sở dữ liệu (Database Setup)
1.  Khởi động máy chủ MySQL cục bộ của bạn.
2.  Mở một công cụ quản lý cơ sở dữ liệu (như MySQL Workbench, DBeaver, Navicat hoặc dòng lệnh terminal) và kết nối với server MySQL.
3.  Tìm đến thư mục mã nguồn và chạy lần lượt 2 file SQL theo thứ tự sau:
    *   **Bước 1 - Khởi tạo cấu trúc bảng:** Chạy file [db_init.sql](src/main/java/com/oims/core/database/db_init.sql) để tạo cơ sở dữ liệu `glocerimex` và các bảng liên quan.
    *   **Bước 2 - Nạp dữ liệu mẫu:** Chạy file [seed.sql](src/main/java/com/oims/core/database/seed.sql) để tạo dữ liệu mẫu phong phú (người dùng, mặt hàng, các site đối tác, kho bãi, các yêu cầu và đơn hàng mẫu có sẵn).

### 3. Cấu hình kết nối CSDL trong Dự án
1.  Mở mã nguồn dự án bằng IDE của bạn hoặc một trình soạn thảo văn bản.
2.  Mở file cấu hình kết nối CSDL tại đường dẫn: [DBConnection.java](src/main/java/com/oims/core/database/DBConnection.java).
3.  Thay đổi thông tin cấu hình cổng (port), tên cơ sở dữ liệu, **tên đăng nhập (USER)** và **mật khẩu đăng nhập MySQL (PASSWORD)** của bạn:
    ```java
    // Mặc định cấu hình kết nối của dự án
    private static final String URL = "jdbc:mysql://localhost:3306/glocerimex";
    private static final String USER = "root";
    private static final String PASSWORD = "your_mysql_password"; // Thay đổi mật khẩu này cho khớp với MySQL cục bộ
    ```

### 4. Biên dịch và Khởi chạy ứng dụng
Có 2 cách thông dụng để khởi chạy dự án:

#### Cách 1: Sử dụng Maven từ dòng lệnh (Terminal/Command Prompt)
Di chuyển terminal vào thư mục gốc của dự án (nơi chứa file `pom.xml`) và chạy lệnh sau:
```bash
# Biên dịch dự án
mvn clean compile

# Chạy ứng dụng thông qua plugin javafx-maven-plugin
mvn javafx:run
```

#### Cách 2: Sử dụng các IDE phổ biến (IntelliJ IDEA / Eclipse)
1.  Chọn **Open** dự án và chọn thư mục chứa dự án OIMS (IDE sẽ tự động nhận diện dự án Maven).
2.  Chờ IDE tải xong các thư viện dependencies được khai báo trong `pom.xml`.
3.  Tìm đến class khởi chạy chính tại đường dẫn: [OIMS.java](/src/main/java/com/oims/OIMS.java).
4.  Nhấp chuột phải vào file và chọn **Run 'OIMS.main()'**.

---

## III. Tài khoản Đăng nhập kiểm thử

Hệ thống phân quyền truy cập chức năng rất chặt chẽ dựa trên chức vụ (Role) của người đăng nhập. Bạn có thể sử dụng các tài khoản mẫu sau đây (được nạp từ file `seed.sql`) để đăng nhập và kiểm thử các tính năng tương ứng:

| Tên tài khoản (Username) | Mật khẩu (Password) | Chức vụ (Role) | Chức năng kiểm thử chính |
| :--- | :--- | :--- | :--- |
| **sales01** | `12345678` | **Sales** | Tạo yêu cầu nhập hàng (F1), Sửa yêu cầu (F6), Xem danh sách yêu cầu |
| **overseas01** | `12345678` | **Overseas Order** | Xử lý yêu cầu nhập hàng (F2), Xử lý đơn hàng hủy (F3), Xem kế hoạch |
| **warehouse01** | `12345678` | **Warehouse** | Xác nhận đơn hàng nhập kho (F5) |
| **admin01** | `12345678` | **Admin** | Quản trị viên hệ thống đầy đủ quyền |
