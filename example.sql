SELECT * FROM `product` ORDER BY calorific ASC;
SELECT * FROM `product` ORDER BY calorific DESC;
SELECT * FROM `product` ORDER BY calorific DESC
SELECT * FROM `product` ORDER BY name, calorific ASC;
SELECT name, calorific FROM `product` ORDER BY name ASC;
SELECT * FROM `dish` LEFT JOIN type_dish ON dish.id_type_dish = type_dish.id_type_dish;
SELECT * FROM `dish` LEFT JOIN type_dish ON dish.id_type_dish = type_dish.id_type_dish
SELECT * FROM `dish` LEFT JOIN type_dish ON dish.id_type_dish = type_dish.id_type_dish;
SELECT  client.id_client AS id_client, client.full_name AS full_name, client.phone     AS phone FROM room_in_reservation LEFT JOIN reservation ON reservation.id_reservation = room_in_reservation.id_reservation LEFT JOIN client ON client.id_client = reservation.id_client  LEFT JOIN room ON room.id_room = room_in_reservation.id_room_in_reservation   LEFT JOIN room_kind ON room_kind.id_room_kind = room.id_room_kind   LEFT JOIN hotel ON hotel.id_hotel = room.id_hotel  WHERE room_kind.name = 'люкс' AND hotel.name = 'Алтай';
SELECT 	country.country_name_eng,	SUM(CASE WHEN call.id IS NOT NULL THEN 1 ELSE 0 END) AS calls,	AVG(ISNULL(DATEDIFF(SECOND, call.start_time, call.end_time),0)) AS avg_difference FROM country LEFT JOIN city ON city.country_id = country.id LEFT JOIN customer ON city.id = customer.city_id LEFT JOIN call ON call.customer_id = customer.id GROUP BY country.id,	country.country_name_eng HAVING AVG(ISNULL(DATEDIFF(SECOND, call.start_time, call.end_time),0)) > (SELECT AVG(DATEDIFF(SECOND, call.start_time, call.end_time)) FROM call) ORDER BY calls DESC, country.id ASC;