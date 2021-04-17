package com.sqlparser.model;

import java.util.Objects;

public class Join {
    private final JoinType joinType;
    private final String joinTable;
    private final String joinLeftTableKey;
    private final String joinRightTableKey;

    public Join(JoinType joinType, String joinTable, String joinTableKey, String thisTableJoinKey) {
        this.joinType = joinType;
        this.joinTable = joinTable;
        this.joinLeftTableKey = joinTableKey;
        this.joinRightTableKey = thisTableJoinKey;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public String getJoinTable() {
        return joinTable;
    }

    public String getJoinLeftTableKey() {
        return joinLeftTableKey;
    }

    public String getJoinRightTableKey() {
        return joinRightTableKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Join join = (Join) o;
        return joinType == join.joinType &&
                Objects.equals(joinTable, join.joinTable) &&
                Objects.equals(joinLeftTableKey, join.joinLeftTableKey) &&
                Objects.equals(joinRightTableKey, join.joinRightTableKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(joinType, joinTable, joinLeftTableKey, joinRightTableKey);
    }

    @Override
    public String toString() {
        return "Join{" +
                "joinType=" + joinType +
                ", joinTable='" + joinTable + '\'' +
                ", joinTableKey='" + joinLeftTableKey + '\'' +
                ", thisTableJoinKey='" + joinRightTableKey + '\'' +
                '}';
    }
}
