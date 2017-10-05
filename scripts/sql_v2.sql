INSERT INTO taxi_graph_hopper_teste(taxi_id, date_time, longitude, latitude, geom)
SELECT taxi_id, date_time, longitude, latitude, geom from taxi_data WHERE taxi_id = 1368 AND geom && st_makeenvelope(116.08, 39.68, 116.77, 40.18, 4326);


SELECT taxi_id, osm_id,  st_distance(st_transform(taxi.geom, 26986), st_transform(line.way, 26986)) AS distance
FROM taxi_graph_hopper_teste taxi, planet_osm_line line
WHERE line.way && st_expand(taxi.geom, 100000000)
ORDER BY distance ASC LIMIT 1;
-- Falta fazer para cada ponto
-- Para saber a distancia
-- st_distance(st_transform(taxi.geom, 26986), st_transform(line.way, 26986)) AS distance

-- talvez algo assim
SELECT st_astext(st_closestpoint(st_transform(way, 4326), st_transform(geom, 4326))) as novo_point,
    st_astext(geom) as atual
FROM taxi_graph_hopper_teste taxi, planet_osm_line line
WHERE osm_id = 169620647 and row = 627;

-- talvez para cada ROW(id) do linha dos taxi
SELECT row, osm_id, st_geomfromtext(st_astext(st_closestpoint(st_transform(way, 4326), st_transform(geom, 4326)))) as novo_point,
    st_geomfromtext(st_astext(geom)) as atual,
    st_distance(st_transform(taxi.geom, 4326), st_transform(line.way, 4326)) as distancia
FROM taxi_graph_hopper_teste taxi, planet_osm_line line
WHERE row = 627
ORDER BY distancia LIMIT 1;

--axo q é o mesmo
SELECT row, osm_id, st_geomfromtext(st_astext(st_closestpoint(st_transform(way, 4326), st_transform(geom, 4326)))) as novo_point,
    st_geomfromtext(st_astext(geom)) as atual,
    st_distance(st_transform(taxi.geom, 4326), st_transform(line.way, 4326)) as distancia
FROM taxi_graph_hopper_teste taxi, planet_osm_line line
WHERE row = 627 and st_transform(way, 4326) && st_expand(st_transform(geom, 4326), 10)
ORDER BY distancia LIMIT 1;

-- atualizacao 
UPDATE taxi_graph_hopper_teste
set new_geom = t.novo_point
from (SELECT row as id,
    st_geomfromtext(st_astext(st_closestpoint(st_transform(way, 4326), st_transform(geom, 4326)))) as novo_point,
    st_distance(st_transform(taxi.geom, 4326), st_transform(line.way, 4326)) as distancia
FROM taxi_graph_hopper_teste taxi, planet_osm_line line
WHERE st_transform(way, 4326) && st_expand(st_transform(geom, 4326), 10) and id = taxi_graph_hopper_teste.row
ORDER BY distancia LIMIT 1) t
WHERE t.id = row;

-- 
-- ok work
UPDATE taxi_graph_hopper_teste
SET new_lat = ST_Y(geometry(t.new_geom)),
    new_lon = ST_X(geometry(t.new_geom))
FROM ( 
   SELECT row, new_geom
    FROM taxi_graph_hopper_teste) t
WHERE t.row = taxi_graph_hopper_teste.row;

UPDATE taxi_graph_hopper_teste SET new_lat = ST_Y(geometry(t.new_geom)), new_lon = ST_X(geometry(t.new_geom)) FROM ( SELECT row, new_geom FROM taxi_graph_hopper_teste) t WHERE t.row = taxi_graph_hopper_teste.row;
--

SELECT row as id,st_geomfromtext(st_astext(st_closestpoint(st_transform(way, 4326), st_transform(geom, 4326)))) as novo_point,st_distance(st_transform(taxi.geom, 4326), st_transform(line.way, 4326)) as distancia FROM taxi_graph_hopper_teste taxi, planet_osm_line line WHERE st_transform(way, 4326) && st_expand(st_transform(geom, 4326), 10) and row = 629 ORDER BY distancia LIMIT 1

-- ok work
SELECT st_geomfromtext(st_astext(st_closestpoint(st_transform(way, 4326), st_transform(geom, 4326)))) as novo_point, st_distance(st_transform(taxi.geom, 4326), st_transform(line.way, 4326)) as distancia, st_y(st_geomfromtext(st_astext(st_closestpoint(st_transform(way, 4326), st_transform(geom, 4326))))) as latitude, st_x(st_geomfromtext(st_astext(st_closestpoint(st_transform(way, 4326), st_transform(geom, 4326))))) as longitude FROM taxi_graph_hopper_teste taxi, planet_osm_line line WHERE st_transform(way, 4326) && st_expand(st_transform(geom, 4326), 10) and row = 629 ORDER BY distancia LIMIT 1;