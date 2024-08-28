create table resource_configuration(
id BIGSERIAL PRIMARY KEY NOT NULL,
name VARCHAR(255) NOT NULL,
description VARCHAR(4000),
version BIGINT NOT NULL,
creation_date TIMESTAMP,
modified_date TIMESTAMP,
config JSONB NOT NULL,
contents BYTEA,
CONSTRAINT unique_name UNIQUE (name));