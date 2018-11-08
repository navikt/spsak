create table STARTPUNKT_TYPE (
  KODE varchar2(50 char) not null,
  NAVN varchar2(70 char) not null,
  BEHANDLING_STEG varchar2(100 char) not null,
  RANGERING number(19) not null,
  BESKRIVELSE varchar2(4000 char),
  OPPRETTET_AV varchar2(20 char) default 'VL' not null,
  OPPRETTET_TID timestamp(3) default systimestamp not null,
  ENDRET_AV varchar2(20 char),
  ENDRET_TID timestamp(3),
  constraint PK_STARTPUNKT_TYPE primary key (KODE),
  constraint FK_STARTPUNKT_TYPE_1 foreign key (BEHANDLING_STEG) references BEHANDLING_STEG_TYPE
);

insert into STARTPUNKT_TYPE (KODE, NAVN, BEHANDLING_STEG, RANGERING, BESKRIVELSE) values ('INNGANGSVILKÅR_OPPL', 'Inngangsvilkår opplysningsplikt', 'VURDEROP', 1, 'Startpunkt inngangsvilkår for søkers opplysningsplikt');
insert into STARTPUNKT_TYPE (KODE, NAVN, BEHANDLING_STEG, RANGERING, BESKRIVELSE) values ('INNGANGSVILKÅR_MEDL', 'Inngangsvilkår medlemskapsvilkår', 'VURDERMV', 2, 'Startpunkt inngangsvilkår for medlemskap');
insert into STARTPUNKT_TYPE (KODE, NAVN, BEHANDLING_STEG, RANGERING, BESKRIVELSE) values ('BEREGNING', 'Beregning', 'FORS_BERGRUNN', 3, 'Startpunkt beregning');
insert into STARTPUNKT_TYPE (KODE, NAVN, BEHANDLING_STEG, RANGERING, BESKRIVELSE) values ('UTTAKSVILKÅR', 'Uttaksvilkår', 'VURDER_UTTAK', 4, 'Startpunkt uttaksvilkår');
insert into STARTPUNKT_TYPE (KODE, NAVN, BEHANDLING_STEG, RANGERING, BESKRIVELSE) values ('-', 'Ikke definert', 'VURDEROP', 99, 'Ikke definert - defaulter til å kjøre hele behandlingen på nytt fra og med inngangsvilkår');

alter table FORRETNINGSHENDELSE_DEF drop constraint FK_FORRETNINGSHENDELSE_DEF_3;
alter table FORRETNINGSHENDELSE_DEF drop column KL_STARTPUNKT_TYPE;
update FORRETNINGSHENDELSE_DEF set STARTPUNKT_TYPE = 'INNGANGSVILKÅR_OPPL' where STARTPUNKT_TYPE = 'INNGANGSVILKÅR';
alter table FORRETNINGSHENDELSE_DEF add constraint FK_FORRETNINGSHENDELSE_DEF_3 foreign key (STARTPUNKT_TYPE) references STARTPUNKT_TYPE;
alter table FORRETNINGSHENDELSE_DEF modify STARTPUNKT_TYPE varchar2(50 char);

alter table BEHANDLING drop constraint FK_BEHANDLING_7;
alter table BEHANDLING drop column KL_STARTPUNKT_TYPE;
alter table BEHANDLING add constraint FK_BEHANDLING_7 foreign key (STARTPUNKT_TYPE) references STARTPUNKT_TYPE;
alter table BEHANDLING modify STARTPUNKT_TYPE varchar2(50 char);

delete from KODELISTE where KODEVERK = 'STARTPUNKT_TYPE';
delete from KODEVERK where KODE = 'STARTPUNKT_TYPE';

comment on table STARTPUNKT_TYPE is 'Kodetabell som definerer de forskjellige startpunktene';
comment on column STARTPUNKT_TYPE.KODE is 'Primary Key: Koden som definerer startpunktet';
comment on column STARTPUNKT_TYPE.NAVN is 'Navn på startpunktet';
comment on column STARTPUNKT_TYPE.BEHANDLING_STEG is 'FK: BEHANDLING_STEG_TYPE. Konkret behandlingssteg der behandlingen skal starte gitt startpunktet.';
comment on column STARTPUNKT_TYPE.RANGERING is 'Rangering av startpunktene';
