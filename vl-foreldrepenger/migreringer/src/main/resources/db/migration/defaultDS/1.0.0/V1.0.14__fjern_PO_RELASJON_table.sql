drop table PO_RELASJON cascade constraints purge;

alter table so_soeknad drop constraint FK_SOEKNAD_1;

alter table so_soeknad drop column BRUKER_ROLLE;
alter table so_soeknad drop column KL_BRUKER_ROLLE;
