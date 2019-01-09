alter table prosess_task modify(siste_kjoering_server varchar(1000));
alter table prosess_task modify(OPPRETTET_AV varchar(1000));

alter table PROSESS_TASK_FEILHAND modify(OPPRETTET_AV varchar(1000));
alter table PROSESS_TASK_FEILHAND modify(ENDRET_AV varchar(1000));
alter table PROSESS_TASK_FEILHAND modify(NAVN varchar(1000));
alter table PROSESS_TASK_FEILHAND modify(KODE varchar(50));

alter table PROSESS_TASK_TYPE modify(NAVN varchar(1000));
alter table PROSESS_TASK_TYPE modify(OPPRETTET_AV varchar(1000));
alter table PROSESS_TASK_TYPE modify(ENDRET_AV varchar(1000));