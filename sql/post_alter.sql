ALTER TABLE post DROP CONSTRAINT created;

CREATE UNIQUE INDEX CONCURRENTLY post_created_id ON post (created);

ALTER TABLE post 
ADD CONSTRAINT unique_created_id 
UNIQUE USING INDEX post_created_id;
