package com.sqlparser.service;

import com.sqlparser.model.Join;
import com.sqlparser.model.JoinType;
import com.sqlparser.model.Query;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JoinsTest {
    @Test
    public void leftJoin() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,   user.avatar AS usravatar,  user.id,  user.address    FROM users  LEFT JOIN messages ON messages.user_id  =  user.id ;";
            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sqlQueryInput);
            final Query query = sqlAnalyzer.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertEquals(columns.size(), 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            final List<Join> joins = query.getJoins();
            assertEquals(joins.size(), 1);
            final Join join = joins.get(0);
            System.out.println("=================" + join);
            assertEquals(new Join(JoinType.LEFT, "messages", "messages.user_id", "user.id"), join);

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void rightJoin() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,   user.avatar AS usravatar,  user.id,  user.address    FROM users  RIGHT JOIN messages ON messages.user_id = user.id ;";
            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sqlQueryInput);
            final Query query = sqlAnalyzer.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertEquals(columns.size(), 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            final List<Join> joins = query.getJoins();
            assertEquals(joins.size(), 1);
            final Join join = joins.get(0);
            System.out.println("=================" + join);
            assertEquals(new Join(JoinType.RIGHT, "messages", "messages.user_id", "user.id"), join);

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void fullOuterJoin() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,   user.avatar AS usravatar,  user.id,  user.address    FROM users  FULL OUTER JOIN messages ON messages.user_id = user.id ;";
            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sqlQueryInput);
            final Query query = sqlAnalyzer.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertEquals(columns.size(), 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            final List<Join> joins = query.getJoins();
            assertEquals(joins.size(), 1);
            final Join join = joins.get(0);
            System.out.println("=================" + join);
            assertEquals(new Join(JoinType.FULL_OUTER, "messages", "messages.user_id", "user.id"), join);

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void innerJoin() {
        try {
            final String sqlQueryInput = "SELECT    `user.email`   ,   user.avatar AS usravatar,  user.id,  user.address    FROM users  INNER JOIN messages ON messages.user_id = user.id ;";
            final SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sqlQueryInput);
            final Query query = sqlAnalyzer.analyze();
            final List<String> columns = query.getColumns();
            columns.forEach(System.out::println);
            assertEquals(columns.size(), 4);
            assertEquals(columns.get(0), "user.email");
            assertEquals(columns.get(1), "user.avatar AS usravatar");
            assertEquals(columns.get(2), "user.id");
            assertEquals(columns.get(3), "user.address");

            final List<Join> joins = query.getJoins();
            assertEquals(joins.size(), 1);
            final Join join = joins.get(0);
            System.out.println("=================" + join);
            assertEquals(new Join(JoinType.INNER, "messages", "messages.user_id", "user.id"), join);

        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void multipleJoins() {
        try {
            final String sqlQueryInput = "SELECT  client.id_client AS id_client, client.full_name AS full_name, " +
                    " client.phone     AS phone FROM room_in_reservation " +
                    " LEFT JOIN reservation ON reservation.id_reservation = room_in_reservation.id_reservation " +
                    " RIGHT JOIN client ON client.id_client = reservation.id_client  " +
                    " FULL OUTER JOIN room ON room.id_room = room_in_reservation.id_room_in_reservation  " +
                    " LEFT JOIN room_kind ON room_kind.id_room_kind = room.id_room_kind  " +
                    " INNER JOIN hotel ON hotel.id_hotel = room.id_hotel " +
                    " WHERE room_kind.name = 'Lux'   AND  hotel.name = 'Altay';";

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
            assertEquals(5, joins.size());
            joins.forEach(System.out::println);
            assertEquals(new Join(JoinType.LEFT, "reservation", "reservation.id_reservation", "room_in_reservation.id_reservation"), joins.get(0));
            assertEquals(new Join(JoinType.RIGHT, "client", "client.id_client", "reservation.id_client"), joins.get(1));
            assertEquals(new Join(JoinType.FULL_OUTER, "room", "room.id_room", "room_in_reservation.id_room_in_reservation"),  joins.get(2));
            assertEquals(new Join(JoinType.LEFT, "room_kind", "room_kind.id_room_kind", "room.id_room_kind"),  joins.get(3));
            assertEquals(new Join(JoinType.INNER, "hotel", "hotel.id_hotel", "room.id_hotel"),  joins.get(4));

            System.out.println("=================");
            final List<String> whereClauses = query.getWhereClauses();
            whereClauses.forEach(System.out::println);
            assertEquals(1, whereClauses.size());
            assertEquals("room_kind.name EQUAL 502 'Lux' AND 511 hotel.name EQUAL 528 'Altay'", whereClauses.get(0));
        } catch (Exception exception) {
            exception.printStackTrace();
            assertTrue(false);
        }
    }
}
