package com.sqlparser.service;

import com.sqlparser.model.Join;
import com.sqlparser.model.JoinType;
import com.sqlparser.model.Limit;
import com.sqlparser.model.Query;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyzerServiceTest {
    @Test
    public void empty() {
        try {
            new AnalyzerService("").analyze();
            assertTrue(false);
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(true);
        }
    }

    @Test
    public void invalidFirstCommand() {
        try {
            new AnalyzerService("SLECT ;").analyze();
            assertTrue(false);
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(true);
        }
    }

    @Test
    public void allColumnsAlias() {
        try {
            final AnalyzerService analyzerService = new AnalyzerService("SELECT * FROM table;");
            final Query analyze = analyzerService.analyze();
            analyze.getColumns().forEach(System.out::println);
            assertTrue(true);
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void oneAggregateColumnsWithoutAlias() {
        try {
            final AnalyzerService analyzerService = new AnalyzerService("SELECT email FROM table;");
            final Query analyze = analyzerService.analyze();
            analyze.getColumns().forEach(System.out::println);
            assertTrue(true);
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void oneAggregateColumnNameAndFamilyName() {
        try {
            final AnalyzerService analyzerService = new AnalyzerService("SELECT user.email FROM table;");
            final Query analyze = analyzerService.analyze();
            analyze.getColumns().forEach(System.out::println);
            assertTrue(true);
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void oneAggregateColumnNameAndFamilyNameWithAlias() {
        try {
            final AnalyzerService analyzerService = new AnalyzerService("SELECT 'user.email' AS useremail FROM table;");
            final Query analyze = analyzerService.analyze();
            analyze.getColumns().forEach(System.out::println);
            assertTrue(true);
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void oneAggregateColumnsWithAlias() {
        try {
            final AnalyzerService analyzerService = new AnalyzerService("SELECT email AS usremail FROM table;");
            final Query analyze = analyzerService.analyze();
            analyze.getColumns().forEach(System.out::println);
            assertTrue(true);
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void quotedTableRefBeforeNotClose() {
        try {
            final AnalyzerService analyzerService = new AnalyzerService("SELECT `email ;");
            final Query analyze = analyzerService.analyze();
            analyze.getColumns().forEach(System.out::println);
            assertTrue(false);
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(true);
        }
    }

    @Test
    public void quotedTableRefAfterNotClose() {
        try {
            final AnalyzerService analyzerService = new AnalyzerService("SELECT email` ;");
            final Query analyze = analyzerService.analyze();
            analyze.getColumns().forEach(System.out::println);
            assertTrue(false);
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(true);
        }
    }

    @Test
    public void quotedTableRef() {
        try {
            final AnalyzerService analyzerService = new AnalyzerService("SELECT `email` ;");
            final Query analyze = analyzerService.analyze();
            analyze.getColumns().forEach(System.out::println);
            assertTrue(true);
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void twoColumn() {
        try {
            final AnalyzerService analyzerService = new AnalyzerService("SELECT     user.email   ,    user.avatar FROM users;");
            final Query query = analyzerService.analyze();

            final List<String> columns = query.getColumns();
            assertTrue(columns.size() == 2);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar");
            columns.forEach(System.out::println);
            final List<String> fromSources = query.getFromSources();
            System.out.println("=================");
            fromSources.forEach(System.out::println);
            assertTrue(fromSources.size() == 1);
            assertEquals(fromSources.get(0), "users");
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void twoColumnWithAlias() {
        try {
            final AnalyzerService analyzerService = new AnalyzerService("SELECT    `user.email`   ,    user.avatar AS usravatar FROM users;");
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            assertTrue(columns.size() == 2);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            columns.forEach(System.out::println);
            final List<String> fromSources = query.getFromSources();
            System.out.println("=================");
            fromSources.forEach(System.out::println);
            assertTrue(fromSources.size() == 1);
            assertEquals(fromSources.get(0), "users");
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void threeAggregatedColumns() {
        try {
            final AnalyzerService analyzerService = new AnalyzerService("SELECT    `user.email`   , user.avatar AS usravatar,  user.id    FROM users, flights;");
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 3);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            final List<String> fromSources = query.getFromSources();
            System.out.println("=================");
            fromSources.forEach(System.out::println);
            assertTrue(fromSources.size() == 2);
            assertEquals(fromSources.get(0), "users");
            assertEquals(fromSources.get(1), "flights");
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }

    }

    @Test
    public void multipleColumn() {
        try {
            final AnalyzerService analyzerService = new AnalyzerService("SELECT    `user.email`   ,    user.avatar AS usravatar,  user.id,  user.address    FROM users;");
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            System.out.println("=================");
            final List<String> fromSources = query.getFromSources();
            fromSources.forEach(System.out::println);
            assertTrue(fromSources.size() == 1);
            assertEquals(fromSources.get(0), "users");

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void multipleTable() {
        try {
            final AnalyzerService analyzerService = new AnalyzerService("SELECT    `user.email`   ,    user.avatar AS usravatar,  user.id,  user.address    FROM users, messages;");
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            System.out.println("=================");
            final List<String> fromSources = query.getFromSources();
            fromSources.forEach(System.out::println);
            assertTrue(fromSources.size() == 2);
            assertEquals(fromSources.get(0), "users");
            assertEquals(fromSources.get(1), "messages");

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void whereBetween() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,    user.avatar AS usravatar,  user.id,  user.address    FROM users WHERE  users.id  BETWEEN 50 AND 100 ;";
            final AnalyzerService analyzerService = new AnalyzerService(sqlQueryInput);
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            System.out.println("=================");
            final List<String> fromSources = query.getFromSources();
            fromSources.forEach(System.out::println);
            assertEquals(fromSources.get(0), "users");

            System.out.println("=================");
            final List<String> whereClauses = query.getWhereClauses();
            assertTrue(whereClauses.size() == 1);
            assertEquals(whereClauses.get(0), "users.id");
            whereClauses.forEach(System.out::println);

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void whereNotBetween() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,    user.avatar AS usravatar,  user.id,  user.address    FROM users WHERE  users.id NOT  BETWEEN 50 AND 100 ;";
            final AnalyzerService analyzerService = new AnalyzerService(sqlQueryInput);
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            System.out.println("=================");
            final List<String> fromSources = query.getFromSources();
            fromSources.forEach(System.out::println);
            assertEquals(fromSources.get(0), "users");

            System.out.println("=================");
            final List<String> whereClauses = query.getWhereClauses();
            assertTrue(whereClauses.size() == 1);
            assertEquals(whereClauses.get(0), "users.id");
            whereClauses.forEach(System.out::println);

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void groupByField() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,    user.avatar AS usravatar,  user.id,  user.address    FROM users GROUP BY  user.address ;";
            final AnalyzerService analyzerService = new AnalyzerService(sqlQueryInput);
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            System.out.println("=================");
            final List<String> fromSources = query.getFromSources();
            fromSources.forEach(System.out::println);
            assertEquals(fromSources.get(0), "users");

            final String groupBy = query.getGroupBy();
            System.out.println("=================" + groupBy);
            assertEquals(groupBy, "ORDER BY 94 user.address");


        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void orderByField() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,    user.avatar AS usravatar,  user.id,  user.address    FROM users ORDER BY  user.address   ;";
            final AnalyzerService analyzerService = new AnalyzerService(sqlQueryInput);
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            System.out.println("=================");
            final List<String> fromSources = query.getFromSources();
            fromSources.forEach(System.out::println);
            assertEquals(fromSources.get(0), "users");

            final String groupBy = query.getOrderBy();
            System.out.println("=================" + groupBy);
            assertEquals(groupBy, "ORDER BY 94 user.address");


        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void orderByFieldASC() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,    user.avatar AS usravatar,  user.id,  user.address    FROM users ORDER BY  user.address  ASC ;";
            final AnalyzerService analyzerService = new AnalyzerService(sqlQueryInput);
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            System.out.println("=================");
            final List<String> fromSources = query.getFromSources();
            fromSources.forEach(System.out::println);
            assertEquals(fromSources.get(0), "users");

            final String orderBy = query.getOrderBy();
            System.out.println("=================" + orderBy);
            assertEquals("ORDER BY 94 user.address ASC", orderBy );


        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void orderMultipleFieldsByASC() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,    user.avatar AS usravatar,  user.id,  user.address    FROM users ORDER BY  user.address,   user.id  ASC ;";
            final AnalyzerService analyzerService = new AnalyzerService(sqlQueryInput);
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            System.out.println("=================");
            final List<String> fromSources = query.getFromSources();
            fromSources.forEach(System.out::println);
            assertEquals(fromSources.get(0), "users");

            final String orderBy = query.getOrderBy();
            System.out.println("=================" + orderBy);
            assertEquals("ORDER BY 94 user.address, user.id ASC", orderBy );


        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void orderByFieldDesc() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,    user.avatar AS usravatar,  user.id,  user.address    FROM users ORDER BY  user.address  DESC ;";
            final AnalyzerService analyzerService = new AnalyzerService(sqlQueryInput);
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            System.out.println("=================");
            final List<String> fromSources = query.getFromSources();
            fromSources.forEach(System.out::println);
            assertEquals(fromSources.get(0), "users");

            final String orderBy = query.getOrderBy();
            System.out.println("=================" + orderBy);
            assertEquals(orderBy, "ORDER BY 94 user.address DESC");


        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void limitWithoutOffset() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,    user.avatar AS usravatar,  user.id,  user.address    FROM users ORDER BY  user.address  DESC   LIMIT 20 ;";
            final AnalyzerService analyzerService = new AnalyzerService(sqlQueryInput);
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            System.out.println("=================");
            final List<String> fromSources = query.getFromSources();
            fromSources.forEach(System.out::println);
            assertEquals(fromSources.get(0), "users");

            final String orderBy = query.getOrderBy();
            System.out.println("=================" + orderBy);
            assertEquals(orderBy, "ORDER BY 94 user.address DESC");

            final Limit limit = query.getLimit();
            System.out.println("=================" + limit);
            assertEquals(new Limit(20, 125), limit);

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void limitWithOffset() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,    user.avatar AS usravatar,  user.id,  user.address    FROM users ORDER BY  user.address  DESC   LIMIT 20 OFFSET 10 ;";
            final AnalyzerService analyzerService = new AnalyzerService(sqlQueryInput);
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            System.out.println("=================");
            final List<String> fromSources = query.getFromSources();
            fromSources.forEach(System.out::println);
            assertEquals(fromSources.get(0), "users");

            final String orderBy = query.getOrderBy();
            System.out.println("=================" + orderBy);
            assertEquals(orderBy, "ORDER BY 94 user.address DESC");

            final Limit limit = query.getLimit();
            System.out.println("=================" + limit);
            assertEquals(new Limit(20,  10, 125), limit);

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void leftJoin() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,   user.avatar AS usravatar,  user.id,  user.address    FROM users  LEFT JOIN messages ON messages.user_id  =  user.id ;";
            final AnalyzerService analyzerService = new AnalyzerService(sqlQueryInput);
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            final Join join = query.getJoin();
            System.out.println("=================" + join);
            assertEquals(new Join(JoinType.LEFT,  "messages", "messages.user_id", "user.id"), join);

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void rightJoin() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,   user.avatar AS usravatar,  user.id,  user.address    FROM users  RIGHT JOIN messages ON messages.user_id = user.id ;";
            final AnalyzerService analyzerService = new AnalyzerService(sqlQueryInput);
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            final Join join = query.getJoin();
            System.out.println("=================" + join);
            assertEquals(new Join(JoinType.RIGHT,  "messages", "messages.user_id", "user.id"), join);

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void fullOuterJoin() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,   user.avatar AS usravatar,  user.id,  user.address    FROM users  FULL OUTER JOIN messages ON messages.user_id = user.id ;";
            final AnalyzerService analyzerService = new AnalyzerService(sqlQueryInput);
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            final Join join = query.getJoin();
            System.out.println("=================" + join);
            assertEquals(new Join(JoinType.FULL_OUTER,  "messages", "messages.user_id", "user.id"), join);

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }
    @Test
    public void innerJoin() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,   user.avatar AS usravatar,  user.id,  user.address    FROM users  INNER JOIN messages ON messages.user_id = user.id ;";
            final AnalyzerService analyzerService = new AnalyzerService(sqlQueryInput);
            final Query query = analyzerService.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertTrue(columns.size() == 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            final Join join = query.getJoin();
            System.out.println("=================" + join);
            assertEquals(new Join(JoinType.INNER,  "messages", "messages.user_id", "user.id"), join);

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }
}