package com.oims.features.sales_requests.shared;

import com.oims.features.sales_requests.dto.RequestItemDTO;

import java.time.LocalDate;
import java.util.List;

public final class RequestItemValidator {

    private RequestItemValidator() {
    }

    public static void validateAll(List<RequestItemDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Danh sách mặt hàng yêu cầu không được để trống.");
        }

        for (RequestItemDTO item : items) {
            validateItem(item);
        }
    }

    public static void validateItem(RequestItemDTO item) {
        if (item == null) {
            throw new IllegalArgumentException("Dòng mặt hàng không hợp lệ.");
        }
        if (item.merchandiseCode() == null || item.merchandiseCode().isBlank()) {
            throw new IllegalArgumentException("Mã mặt hàng không hợp lệ.");
        }
        if (item.quantity() <= 0) {
            throw new IllegalArgumentException("Số lượng mặt hàng phải lớn hơn 0.");
        }
        if (item.unit() == null || item.unit().isBlank()) {
            throw new IllegalArgumentException("Đơn vị mặt hàng không được để trống.");
        }
        if (item.desiredDate() == null) {
            throw new IllegalArgumentException("Ngày nhận mong muốn không được để trống.");
        }
        if (item.desiredDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày nhận mong muốn không được ở trong quá khứ.");
        }
    }
}
