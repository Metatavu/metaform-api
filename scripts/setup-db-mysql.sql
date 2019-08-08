create user if not exists sa identified by 'sa';
ALTER USER sa IDENTIFIED WITH mysql_native_password BY 'sa';
grant all on .* to sa; 
drop database if exists ; 
create database default charset utf8;