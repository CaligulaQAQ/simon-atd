package com.simon.common.model.price.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yzy
 * @Date 2023/11/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceDetailVO {
    private Long       timestamp;
    private BigDecimal predictPriceDiff;
    private BigDecimal actualPriceDiff;
    private BigDecimal confidence;
    private BigDecimal predictRealPrice;
    private BigDecimal predictAheadPrice;
    private BigDecimal actualRealPrice;
    private BigDecimal actualAheadPrice;
}
