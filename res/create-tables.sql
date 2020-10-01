CREATE TABLE IF NOT EXISTS Move
(
   UUID VARCHAR (36),
   Date BIGINT,
   Version TINYINT,
   X FLOAT,
   Y FLOAT,
   Z FLOAT,
   Yaw FLOAT,
   Pitch FLOAT,
   HasPos BOOLEAN,
   HasLook BOOLEAN,
   Gamemode TINYINT (1),
   Health FLOAT,
   WalkSpeed FLOAT,
   FlySpeed FLOAT,
   Sneaking BOOLEAN,
   Sprinting BOOLEAN,
   Blocking BOOLEAN,
   ItemInHand INT
)