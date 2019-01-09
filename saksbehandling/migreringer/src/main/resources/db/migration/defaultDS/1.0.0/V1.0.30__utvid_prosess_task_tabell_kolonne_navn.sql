alter table prosess_task alter column siste_kjoering_server varchar(1000);
alter table prosess_task alter column OPPRETTET_AV varchar(1000);

alter table PROSESS_TASK_FEILHAND alter column OPPRETTET_AV varchar(1000);
alter table PROSESS_TASK_FEILHAND alter column ENDRET_AV varchar(1000);
alter table PROSESS_TASK_FEILHAND alter column NAVN varchar(1000);
alter table PROSESS_TASK_FEILHAND alter column KODE varchar(50);

alter table PROSESS_TASK_TYPE alter column NAVN varchar(1000);
alter table PROSESS_TASK_TYPE alter column OPPRETTET_AV varchar(1000);
alter table PROSESS_TASK_TYPE alter column ENDRET_AV varchar(1000);