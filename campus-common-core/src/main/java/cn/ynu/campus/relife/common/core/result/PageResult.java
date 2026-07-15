package cn.ynu.campus.relife.common.core.result;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应，见 docs/API接口规范.md §1.4
 */
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<T> list;
    private int page;
    private int size;
    private long total;
    private int pages;

    public PageResult() {
    }

    public PageResult(List<T> list, int page, int size, long total) {
        this.list = list == null ? Collections.emptyList() : list;
        this.page = page;
        this.size = size;
        this.total = total;
        this.pages = size <= 0 ? 0 : (int) Math.ceil((double) total / size);
    }

    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(Collections.emptyList(), page, size, 0);
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}
