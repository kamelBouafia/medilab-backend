package com.medilab.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportDto {
    private List<ImportItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportItem {
        private Long globalTestId;
        private BigDecimal price;
    }
}
