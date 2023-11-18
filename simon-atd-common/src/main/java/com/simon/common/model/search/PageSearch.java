package com.simon.common.model.search;

import lombok.experimental.SuperBuilder;

/**
 * @Author yzy
 * @Date 2023/5/15
 */
@SuperBuilder(toBuilder = true)
public class PageSearch {
    private Integer pageNo       = 1;
    private Integer pageSize     = 5;
    private boolean isPagination = true;

    public PageSearch() {
    }

    public int getLimitOffset() {
        return (this.pageNo - 1) * this.pageSize;
    }

    public int getPageNo() {
        return this.pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isPagination() {
        return this.isPagination;
    }

    public void setPagination(boolean pagination) {
        this.isPagination = pagination;
    }

}
