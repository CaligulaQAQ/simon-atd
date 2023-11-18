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
public class PriceVO {
    private Long       timestamp;
    private BigDecimal predictRealPrice;
    private BigDecimal predictAheadPrice;
    private BigDecimal actualRealPrice;
    private BigDecimal actualAheadPrice;
}
