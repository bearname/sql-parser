package com.sqlparser.model;

import java.util.ArrayList;
import java.util.List;

public class Query {
    private final List<String> columns = new ArrayList<>();
    private final List<String> fromSources = new ArrayList<>();
    private Join join;
    private final List<String> whereClauses = new ArrayList<>();
    private String groupBy;
    private String orderBy;
    private Limit limit;
//    private List<Source> fromSources;
//    private List<Join> joins;
//    private List<WhereClause> whereClauses;
//    private List<String> groupByColumns;
//    private List<Sort> sortColumns;
//    private Integer limit;
//    private Integer offset;

    public void addColumn(final String column) {
        this.columns.add(column);
    }

    public List<String> getColumns() {
        return columns;
    }

    public void addFromSource(final String fromSource) {
        this.fromSources.add(fromSource);
    }

    public List<String> getFromSources() {
        return fromSources;
    }

    public void addWhere(final String where) {
        this.whereClauses.add(where);
    }

    public List<String> getWhereClauses() {
        return whereClauses;
    }

    public void addGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void addOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void addLimit(Limit limit) {
        this.limit = limit;
    }

    public Limit getLimit() {
        return limit;
    }

    public void setJoin(final Join join) {
        this.join = join;
    }

    public Join getJoin() {
        return join;
    }
}