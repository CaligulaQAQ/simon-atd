package com.simon.common.model.price.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.simon.common.model.search.PageSearch;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @Author yzy
 * @Date 2023/11/15
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class BaseDTO extends PageSearch {

    @NotNull(message = "startDate不能为空")
    @Pattern(regexp = "^2\\d{7}$", message = "startDate格式有误")
    private String startDate;
    @NotNull(message = "endDate不能为空")
    @Pattern(regexp = "^2\\d{7}$", message = "endDate格式有误")
    private String endDate;
    @NotNull(message = "station不能为空")
    private String station;
    private Integer isFifteen;
}
