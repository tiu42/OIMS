package com.oims.features.sales_requests.process;

import java.util.List;

public record ItemAllocationOption(
    List<AllocatedItemAllocation> allocations
) {}
