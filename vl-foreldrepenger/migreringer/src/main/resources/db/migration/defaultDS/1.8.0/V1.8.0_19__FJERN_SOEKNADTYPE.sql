alter table SOEKNAD drop column soeknad_type CASCADE CONSTRAINTS;
drop table SOEKNAD_TYPE cascade constraints purge;