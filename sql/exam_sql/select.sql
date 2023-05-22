select * from person;
select * from company;

select * from person as p
join company as c 
on p.company_id = c.id
where p.company_id != 5;

select c.name, count(p.id) 
from company as c 
join person as p on
p.company_id = c.id
group BY c.name;
