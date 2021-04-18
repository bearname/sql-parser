package com.sqlparser.service;

import com.sqlparser.model.Join;
import com.sqlparser.model.JoinType;
import com.sqlparser.model.Query;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WhereTest {

    @Test
    public void whereBetween() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,    user.avatar AS usravatar,  user.id,  user.address    FROM users WHERE  users.id  BETWEEN 50 AND 100 ;";
            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sqlQueryInput);
            final Query query = sqlAnalyzer.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertEquals(columns.size(), 4);
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
            assertEquals(1, whereClauses.size());
            assertEquals("users.id BETWEEN 111 50 100", whereClauses.get(0));
            whereClauses.forEach(System.out::println);

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void whereNotBetween() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,    user.avatar AS usravatar,  user.id,  user.address    FROM users WHERE  users.id NOT  BETWEEN 50  AND 100 ;";
            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sqlQueryInput);
            final Query query = sqlAnalyzer.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertEquals(4, columns.size());
            assertEquals("user.email", columns.get(0));
            assertEquals("user.avatar AS usravatar", columns.get(1));
            assertEquals("user.id", columns.get(2));
            assertEquals("user.address", columns.get(3));

            System.out.println("=================");
            final List<String> fromSources = query.getFromSources();
            fromSources.forEach(System.out::println);
            assertEquals("users", fromSources.get(0) );

            System.out.println("=================");
            final List<String> whereClauses = query.getWhereClauses();
            assertEquals(1, whereClauses.size());
            assertEquals("users.id  NOT BETWEEN 115 50 100", whereClauses.get(0));
            whereClauses.forEach(System.out::println);
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }
    @Test
    public void compareLessThanOperand() {
        try {
            final String sql = "SELECT user.id FROM users WHERE user.created_at < 10 ;";
            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sql);
            final Query query = sqlAnalyzer.analyze();
            final List<String> whereClauses = query.getWhereClauses();
            whereClauses.forEach(System.out::println);

            assertEquals(1, whereClauses.size());
            assertEquals("user.created_at LESS_THAN 48 10", whereClauses.get(0));
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void compareLessThanOrEqualOperand() {
        try {
            final String sql = "SELECT user.id FROM users WHERE user.created_at <= 10 ;";
            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sql);
            final Query query = sqlAnalyzer.analyze();
            final List<String> whereClauses = query.getWhereClauses();
            whereClauses.forEach(System.out::println);

            assertEquals(1, whereClauses.size());
            assertEquals("user.created_at LESS_THAN_OR_EQUAL_TO 48 10", whereClauses.get(0));
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void compareGreaterThanOrEqualOperand() {
        try {
            final String sql = "SELECT user.id FROM users WHERE user.created_at >= 10 ;";
            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sql);
            final Query query = sqlAnalyzer.analyze();
            final List<String> whereClauses = query.getWhereClauses();
            whereClauses.forEach(System.out::println);

            assertEquals(1, whereClauses.size());
            assertEquals("user.created_at GREATER_THAN_OR_EQUAL_TO 48 10", whereClauses.get(0));
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void compareGreaterThanOperand() {
        try {
            final String sql = "SELECT user.id FROM users WHERE user.created_at > 10 ;";
            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sql);
            final Query query = sqlAnalyzer.analyze();
            final List<String> whereClauses = query.getWhereClauses();

            whereClauses.forEach(System.out::println);

            assertEquals(1, whereClauses.size());
            assertEquals("user.created_at GREATER_THAN 48 10", whereClauses.get(0));
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void compareEqualOperand() {
        try {
            final String sql = "SELECT user.id FROM users WHERE user.created_at = 10 ;";
            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sql);
            final Query query = sqlAnalyzer.analyze();
            final List<String> whereClauses = query.getWhereClauses();
            whereClauses.forEach(System.out::println);
            assertEquals(1, whereClauses.size());
            assertEquals("user.created_at EQUAL 48 10", whereClauses.get(0));
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void compareNotEqualOperand() {
        try {
            final String sql = "SELECT user.id FROM users WHERE user.created_at != 10 ;";
            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sql);
            final Query query = sqlAnalyzer.analyze();
            final List<String> whereClauses = query.getWhereClauses();

            whereClauses.forEach(System.out::println);

            assertEquals(1, whereClauses.size());
            assertEquals("user.created_at NOT_EQUAL 48 10", whereClauses.get(0));
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void compareNotEqualOperandSecond() {
        try {
            final String sql = "SELECT user.id FROM users WHERE user.created_at <> 10 ;";
            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sql);
            final Query query = sqlAnalyzer.analyze();
            final List<String> whereClauses = query.getWhereClauses();
            whereClauses.forEach(System.out::println);
            assertEquals(1, whereClauses.size());
            assertEquals("user.created_at NOT_EQUAL 48 10", whereClauses.get(0));

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void whereOrExpressionTest() {
        try {
            final String sqlQueryInput = "SELECT  client.id_client AS id_client, client.full_name AS full_name, " +
                    " client.phone     AS phone FROM room_in_reservation " +
                    " LEFT JOIN hotel ON hotel.id_hotel = room.id_hotel " +
                    " WHERE room_kind.name = 'Lux'   OR  hotel.name = 'Altay';";

            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sqlQueryInput);
            final Query query = sqlAnalyzer.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertEquals(columns.size(), 3);
            assertEquals(columns.get(0), "client.id_client AS id_client");
            assertEquals(columns.get(1), "client.full_name AS full_name");
            assertEquals(columns.get(2), "client.phone AS phone");

            System.out.println("=================");
            final List<Join> joins = query.getJoins();
            assertEquals(1, joins.size());
            joins.forEach(System.out::println);
            assertEquals(new Join(JoinType.LEFT, "hotel", "hotel.id_hotel", "room.id_hotel"),  joins.get(0));

            System.out.println("=================");
            final List<String> whereClauses = query.getWhereClauses();
            whereClauses.forEach(System.out::println);
            assertEquals(1, whereClauses.size());
            assertEquals("room_kind.name EQUAL 195 'Lux' OR 205 hotel.name EQUAL 220 'Altay'", whereClauses.get(0));
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void like() {
        try {
            final String sqlQueryInput = "SELECT  client.id_client AS id_client, client.full_name AS full_name, " +
                    " client.phone     AS phone FROM room_in_reservation " +
                    " LEFT JOIN hotel ON hotel.id_hotel = room.id_hotel " +
                    " WHERE room_kind.name LIKE 'Lux'   OR  hotel.name = 'Altay';";

            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sqlQueryInput);
            final Query query = sqlAnalyzer.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertEquals(columns.size(), 3);
            assertEquals(columns.get(0), "client.id_client AS id_client");
            assertEquals(columns.get(1), "client.full_name AS full_name");
            assertEquals(columns.get(2), "client.phone AS phone");

            System.out.println("=================");
            final List<Join> joins = query.getJoins();
            assertEquals(1, joins.size());
            joins.forEach(System.out::println);
            assertEquals(new Join(JoinType.LEFT, "hotel", "hotel.id_hotel", "room.id_hotel"),  joins.get(0));

            System.out.println("=================");
            final List<String> whereClauses = query.getWhereClauses();
            whereClauses.forEach(System.out::println);
            assertEquals(1, whereClauses.size());
            assertEquals("room_kind.name LIKE 195 'Lux' OR 208 hotel.name EQUAL 223 'Altay'", whereClauses.get(0));
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void notLike() {
        try {
            final String sqlQueryInput = "SELECT  client.id_client AS id_client, client.full_name AS full_name, " +
                    " client.phone     AS phone FROM room_in_reservation " +
                    " LEFT JOIN hotel ON hotel.id_hotel = room.id_hotel " +
                    " WHERE room_kind.name NOT LIKE 'Lux'   OR  hotel.name = 'Altay';";

            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sqlQueryInput);
            final Query query = sqlAnalyzer.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertEquals(columns.size(), 3);
            assertEquals(columns.get(0), "client.id_client AS id_client");
            assertEquals(columns.get(1), "client.full_name AS full_name");
            assertEquals(columns.get(2), "client.phone AS phone");

            System.out.println("=================");
            final List<Join> joins = query.getJoins();
            assertEquals(1, joins.size());
            joins.forEach(System.out::println);
            assertEquals(new Join(JoinType.LEFT, "hotel", "hotel.id_hotel", "room.id_hotel"),  joins.get(0));

            System.out.println("=================");
            final List<String> whereClauses = query.getWhereClauses();
            whereClauses.forEach(System.out::println);
            assertEquals(1, whereClauses.size());
            assertEquals("room_kind.name NOT LIKE 195 'Lux' OR 208 hotel.name EQUAL 223 'Altay'", whereClauses.get(0));
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void in() {
        try {
            final String sqlQueryInput = "SELECT  client.id_client AS id_client, client.full_name AS full_name, " +
                    " client.phone     AS phone FROM room_in_reservation " +
                    " LEFT JOIN hotel ON hotel.id_hotel = room.id_hotel " +
                    " WHERE room_kind.id IN (1)   OR  hotel.name = 'Altay';";

            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sqlQueryInput);
            final Query query = sqlAnalyzer.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertEquals(columns.size(), 3);
            assertEquals(columns.get(0), "client.id_client AS id_client");
            assertEquals(columns.get(1), "client.full_name AS full_name");
            assertEquals(columns.get(2), "client.phone AS phone");

            System.out.println("=================");
            final List<Join> joins = query.getJoins();
            assertEquals(1, joins.size());
            joins.forEach(System.out::println);
            assertEquals(new Join(JoinType.LEFT, "hotel", "hotel.id_hotel", "room.id_hotel"),  joins.get(0));

            System.out.println("=================");
            final List<String> whereClauses = query.getWhereClauses();
            whereClauses.forEach(System.out::println);
            assertEquals(1, whereClauses.size());
            assertEquals("room_kind.id IN 193 1 OR 202 hotel.name EQUAL 217 'Altay'", whereClauses.get(0));
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }
}
