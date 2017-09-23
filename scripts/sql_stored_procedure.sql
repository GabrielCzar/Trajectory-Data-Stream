SELECT pgr_createTopology('planet_osm_line', 0.0001, 'way', 'osm_id', 'source', 'target');


CREATE VIEW osm_line_src_tgt_cost AS SELECT osm_id AS id, source, target, st_length(way) AS cost FROM planet_osm_line

CREATE VIEW osm_line_src_tgt_cost_in_meters AS SELECT osm_id AS id, source, target, st_length(ST_Transform(way,26915)) AS cost FROM planet_osm_line


exemplo: SELECT * FROM osm_line_src_tgt_cost;

St_Length() returns the length in the units of its Spatial Reference System;

CREATE OR REPLACE FUNCTION shortest_path_in_osm_line(origin INTEGER, destiny INTEGER)
  RETURNS TABLE(seq INTEGER, path_seq INTEGER, node bigint, edge bigint, cost DOUBLE PRECISION, agg_cost DOUBLE PRECISION)
LANGUAGE plpgsql
AS $$
BEGIN
      RETURN QUERY SELECT * FROM pgr_dijkstra('SELECT osm_id as id, source, target, st_length(way) as cost FROM planet_osm_line',
                                              origin, destiny, TRUE);
  END
$$;

Para ter o custo em metros
CREATE OR REPLACE FUNCTION shortest_path_in_osm_line_in_meters(origin INTEGER, destiny INTEGER)
  RETURNS TABLE(seq INTEGER, path_seq INTEGER, node bigint, edge bigint, cost DOUBLE PRECISION, agg_cost DOUBLE PRECISION)
LANGUAGE plpgsql
AS $$
BEGIN
      RETURN QUERY SELECT * FROM pgr_dijkstra('SELECT osm_id as id, source, target, st_length(ST_Transform(way,26915)) as cost FROM planet_osm_line',
                                              origin, destiny, TRUE);
  END
$$;

exemplo: SELECT * from shortest_path_in_osm_line(7, 27);



planet_osm_point: which contains points of interest such as restaurants, hospitals, schools, supermarkets and addresses
planet_osm_lines: contains roads and streets
planet_osm_polygons: contains lakes, building footprints, administrative boundaries such as towns and cities


create table vertices_in_common (
  id INTEGER,
  latitude NUMERIC,
  longitude NUMERIC
);
INSERT INTO vertices_in_common (id, latitude, longitude)
SELECT v.id_vertice, v.latitude, v.longitude
FROM table_vertices v, nodes n
WHERE n.lat = v.latitude and v.longitude = n.lon;

INSERT INTO vertices_in_common_tvertices_nodes (id, latitude, longitude)
SELECT id, lat, lon FROM nodes n, planet_osm_point p WHERE n.id = p.osm_id;

para criar as latitude e longitude a partir das geometrias

UPDATE planet_osm_line_vertices_pgr
SET latitude = ST_Y(ST_TRANSFORM(t.the_geom, 4674)),
    longitude = ST_X(ST_TRANSFORM(t.the_geom, 4674))
FROM (
    SELECT id, the_geom
    FROM planet_osm_line_vertices_pgr) t
WHERE t.id = planet_osm_line_vertices_pgr.id;



-- criando nodes para as novas rotas

create table node_road as
  SELECT row_number() OVER (ORDER BY foo.p)::INTEGER as id,
    foo.p as the_geom
  FROM (
    SELECT DISTINCT planet_osm_line.source as p FROM planet_osm_line
    UNION
    SELECT DISTINCT planet_osm_line.target as p FROM planet_osm_line
  ) foo
  GROUP BY foo.p;


  CREATE TABLE network_node_lines AS
  SELECT a.*, b.id as start_id, c.id as end_id
  FROM planet_osm_line as a
        JOIN node_lines as b on a.source = b.the_geom
        JOIN node_lines as c on a.target = c.the_geom;




CREATE FUNCTION shortest_path_in_network_in_meters(origin INTEGER, destiny INTEGER)
  RETURNS TABLE(seq INTEGER, path_seq INTEGER, node bigint, edge bigint, cost DOUBLE PRECISION, agg_cost DOUBLE PRECISION)
LANGUAGE plpgsql
AS $$
BEGIN
      RETURN QUERY SELECT * FROM pgr_dijkstra('
   SELECT osm_id AS id,
          start_id::int4 AS source,
          end_id::int4 AS target,
          st_length(ST_Transform(way,26915)) as cost
   FROM network_node_lines', origin, destiny, TRUE); 
  END
$$;



CREATE FUNCTION shortest_path_in_network_in_meters_no_directed(origin INTEGER, destiny INTEGER)
  RETURNS TABLE(seq INTEGER, path_seq INTEGER, node bigint, edge bigint, cost DOUBLE PRECISION, agg_cost DOUBLE PRECISION)
LANGUAGE plpgsql
AS $$
BEGIN
      RETURN QUERY SELECT * FROM pgr_dijkstra('
   SELECT osm_id AS id,
          start_id::int4 AS source,
          end_id::int4 AS target,
          st_length(ST_Transform(way,26915)) as cost
   FROM network_node_lines', origin, destiny, FALSE);
  END
$$;


CREATE VIEW view_network_nodes AS
SELECT foo.id, st_centroid(st_collect(foo.pt)) AS geom
FROM (
  SELECT network_node_lines.source AS id,
         st_geometryn (st_multi(network_node_lines.way), 1) AS pt
  FROM network_node_lines
  UNION
  SELECT network_node_lines.target AS id,
         st_boundary(st_multi(network_node_lines.way)) AS pt
  FROM network_node_lines) foo
GROUP BY foo.id;

ALTER TABLE network_node_lines add COLUMN traveltime DOUBLE PRECISION;

UPDATE network SET traveltime = way / 158400 * 60; -- 30 miles per hour

UPDATE network_node_lines SET traveltime = st_length(way) / 158400 * 60


update data
  set geom = ST_SetSRID(ST_MakePoint(t.long, t.lat), 4326)
  from (select row as id, longitude as long, latitude as lat from data) as t
  WHERE t.id = data.row


-- n retona nada
SELECT *
  FROM data
    WHERE data.geom
          && st_astext(st_makeenvelope(39.68, 116.08, 40.18, 116.77));

-- 39604.41440835762 metros de distancia ate o center
SELECT st_length(
        ST_Transform(
                st_makeline(
                        ST_SetSRID(ST_MakePoint(116.08, 39.68), 4326), -- ponta
                        ST_SetSRID(ST_MakePoint(116.40, 39.90), 4326)), -- center
                26915)) as distancia
FROM points_limit_beijing

-- 8.916.471 pontos dentro ainda
SELECT
  count(*)
FROM
  data
WHERE
  ST_DWithin(
    Geography(data.geom),
    Geography(ST_GeographyFromText('SRID=4326;POINT(116.40 39.90)')),
      39604.41440835762
  );

-- taxi point no raio baseado no ponto baixo da essquerda do osm
create view taxi_points_radius_center_ext_point as
SELECT
  *
FROM
  data
WHERE
  ST_DWithin(
    Geography(data.geom),
    Geography(ST_GeographyFromText('SRID=4326;POINT(116.40 39.90)')),
      39604.41440835762
  );

-- inserir pontos dos taxi que estao inseridos no osm
insert into taxi_points_radius_center (longitude, latitude, geom) 
  select longitude, latitude, geom 
    from data 
    where geom && st_makeenvelope(116.08, 39.68, 116.77, 40.18, 4326);


