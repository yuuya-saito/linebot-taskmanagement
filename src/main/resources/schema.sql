CREATE TABLE task (
id VARCHAR(36) PRIMARY KEY NOT NULL,
user_id VARCHAR(255) NOT NULL,
title VARCHAR(255) NOT NULL,
specify_time DateTime NOT NULL,
created_at DateTime NOT NULL
);