package com.simon.common.model.price.dto;

import javax.validation.constraints.Pattern;

import com.simon.common.model.search.PageSearch;
import com.sun.istack.internal.NotNull;
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
    @NotNull
    @Pattern(regexp = "^2\\d{7}$")
    private String  startDate;
    @NotNull
    @Pattern(regexp = "^2\\d{7}$")
    private String  endDate;
    @NotNull
    private String  station;
    private Integer isFifteen;
}
