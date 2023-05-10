ALTER TABLE post DROP CONSTRAINT unique_created_id;
ALTER TABLE post DROP CONSTRAINT post_created_key;

CREATE UNIQUE INDEX CONCURRENTLY post_link_id ON post (link);

ALTER TABLE post 
ADD CONSTRAINT unique_link_id 
UNIQUE USING INDEX post_link_id;


