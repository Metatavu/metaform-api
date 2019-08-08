create user if not exists sa identified by 'sa';
ALTER USER sa IDENTIFIED WITH mysql_native_password BY 'sa';
grant all on .* to sa; 
drop database if exists mftest; 
CREATE DATABASE `mftest` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */