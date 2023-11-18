package com.simon.common.model.price.excel;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yzy
 * @Date 2023/11/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceDO {
    private String     dateTime;
    private BigDecimal price;
}
