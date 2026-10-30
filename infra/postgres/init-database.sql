CREATE SCHEMA IF NOT EXISTS catalog AUTHORIZATION delivery_user;
GRANT ALL ON SCHEMA catalog TO delivery_user;

CREATE SCHEMA IF NOT EXISTS orders AUTHORIZATION delivery_user;
GRANT ALL ON SCHEMA orders TO delivery_user;

CREATE SCHEMA IF NOT EXISTS identity AUTHORIZATION delivery_user;
GRANT ALL ON SCHEMA identity TO delivery_user;