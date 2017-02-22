package com.domain.biz;

/**
 * Created by tangcheng on 2017/2/18.
 */

/**
 * http://www.jquery-bootgrid.com/
 * http://localhost/v1/getReviews?current=1&rowCount=10&searchPhrase=
 */
public class ReviewsReq {
    private Integer current;
    private Integer rowCount;
    private String searchPhrase;

    public Integer getCurrent() {
        return current;
    }

    public Integer getPageId() {
        return current - 1;//后台分布的页码是从0开始
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public String getSearchPhrase() {
        return searchPhrase;
    }

    public void setSearchPhrase(String searchPhrase) {
        this.searchPhrase = searchPhrase;
    }
}
