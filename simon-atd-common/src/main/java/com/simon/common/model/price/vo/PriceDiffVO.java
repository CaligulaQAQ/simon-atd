package com.simon.common.model.price.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yzy
 * @Date 2023/11/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceDiffVO {
    private Long       timestamp;
    private BigDecimal predictPriceDiff;
    private BigDecimal actualPriceDiff;
    private BigDecimal confidence;
}
