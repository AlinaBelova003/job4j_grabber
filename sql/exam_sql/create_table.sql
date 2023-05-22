create table company(
	id serial primary key NOT NULL,
    name character varying 
);

create table person(
	id serial primary key not null,
	name character varying,
	company_id int references company(id)
);