package com.simon.common.model.search;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * @Author yzy
 * @Date 2023/5/15
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResult<T> {
    private List<T> data;

    /**
     * 每页大小
     */
    private long pageSize;

    /**
     * 当前页数
     */
    private long pageNo;

    /**
     * 总记录数
     */
    private long total;

    public PageResult(long pageSize, long total) {
        this.pageSize = pageSize;
        this.total = total;
    }

    public long getPageCount() {
        if (total <= 0) {
            return 0;
        }
        return (total / pageSize) + ((total % pageSize == 0) ? 0 : 1);
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = Math.max(pageSize, 1);
    }

    public long getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = Math.max(pageNo, 1);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
