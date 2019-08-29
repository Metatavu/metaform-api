create user if not exists sa identified by 'sa';
grant all on mftest.* to sa; 
drop database if exists mftest; 
create database mftest default charset utf8;