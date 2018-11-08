insert into KODEVERK (KODE, KODEVERK_SYNK_NYE, KODEVERK_SYNK_EKSISTERENDE, NAVN, BESKRIVELSE) values ('FORRETNINGSHENDELSE_TYPE', 'N', 'N', 'ForretningshendelseType', 'Internt kodeverk som definerer forretningshendelser.');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, 'FØDSEL', 'Fødsel', 'Forretningshendelse fødsel', 'FORRETNINGSHENDELSE_TYPE');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, 'INGEN_HENDELSE', 'Ingen hendelse', 'Ingen forretningshendelse funnet', 'FORRETNINGSHENDELSE_TYPE');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'FORRETNINGSHENDELSE_TYPE');

insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, 'RE-HENDELSE-FØDSEL', 'Hendelse fødsel', 'Revurdering pga. Forretningshendelse fødsel', 'BEHANDLING_AARSAK');

insert into KODEVERK (KODE, KODEVERK_SYNK_NYE, KODEVERK_SYNK_EKSISTERENDE, NAVN, BESKRIVELSE) values ('STARTPUNKT_TYPE', 'N', 'N', 'StartpunktType', 'Internt kodeverk som definerer startpunkt for behandling ved revurdering.');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, 'INNGANGSVILKÅR', 'Inngangsvilkår', 'Startpunkt inngangsvilkår', 'STARTPUNKT_TYPE');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, 'BEREGNING', 'Beregning', 'Startpunkt beregning', 'STARTPUNKT_TYPE');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, 'UTTAKSVILKÅR', 'Uttaksvilkår', 'Startpunkt uttaksvilkår', 'STARTPUNKT_TYPE');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'STARTPUNKT_TYPE');

create table FORRETNINGSHENDELSE_DEF (
  KODE varchar2(50 char) not null,
  NAVN varchar2(70 char) not null,
  FORRETNINGSHENDELSE_TYPE varchar2(100 char) not null,
  KL_FORRETNINGSHENDELSE_TYPE varchar2(100 char) default 'FORRETNINGSHENDELSE_TYPE' not null,
  REVURDERINGSÅRSAK_TYPE varchar2(100 char) not null,
  KL_REVURDERINGSÅRSAK_TYPE varchar2(100 char) default 'BEHANDLING_AARSAK' not null,
  STARTPUNKT_TYPE varchar2(100 char) not null,
  KL_STARTPUNKT_TYPE varchar2(100 char) default 'STARTPUNKT_TYPE' not null,
  BESKRIVELSE varchar2(4000 char),
  OPPRETTET_AV varchar2(20 char) default 'VL' not null,
  OPPRETTET_TID timestamp(3) default systimestamp not null,
  ENDRET_AV varchar2(20 char),
  ENDRET_TID timestamp(3),
  constraint PK_FORRETNINGSHENDELSE_DEF primary key (KODE),
  constraint FK_FORRETNINGSHENDELSE_DEF_1 foreign key (FORRETNINGSHENDELSE_TYPE, KL_FORRETNINGSHENDELSE_TYPE) references KODELISTE (KODE, KODEVERK),
  constraint FK_FORRETNINGSHENDELSE_DEF_2 foreign key (REVURDERINGSÅRSAK_TYPE, KL_REVURDERINGSÅRSAK_TYPE) references KODELISTE (KODE, KODEVERK),
  constraint FK_FORRETNINGSHENDELSE_DEF_3 foreign key (STARTPUNKT_TYPE, KL_STARTPUNKT_TYPE) references KODELISTE (KODE, KODEVERK)
);

insert into FORRETNINGSHENDELSE_DEF (KODE, NAVN, FORRETNINGSHENDELSE_TYPE, REVURDERINGSÅRSAK_TYPE, STARTPUNKT_TYPE, BESKRIVELSE) values ('FØDSEL', 'Fødsel', 'FØDSEL', 'RE-HENDELSE-FØDSEL', 'INNGANGSVILKÅR', 'Fødsel har inntruffet og er registrert i TPS.');

alter table BEHANDLING add STARTPUNKT_TYPE varchar2(100 char) default '-' not null;
alter table BEHANDLING add KL_STARTPUNKT_TYPE varchar2(100 char) default 'STARTPUNKT_TYPE' not null;
alter table BEHANDLING add constraint FK_BEHANDLING_7 foreign key (STARTPUNKT_TYPE, KL_STARTPUNKT_TYPE) references KODELISTE (KODE, KODEVERK);

comment on table FORRETNINGSHENDELSE_DEF is 'Kodetabell som definerer de forskjellige typene forretningshendelse';
comment on column FORRETNINGSHENDELSE_DEF.KODE is 'Primary Key: Koden som definerer forretningshendelsen';
comment on column FORRETNINGSHENDELSE_DEF.NAVN is 'Navn på forretningshendelsen';
comment on column FORRETNINGSHENDELSE_DEF.FORRETNINGSHENDELSE_TYPE is 'FK: FORRETNINGSHENDELSE_TYPE';
comment on column FORRETNINGSHENDELSE_DEF.KL_FORRETNINGSHENDELSE_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column FORRETNINGSHENDELSE_DEF.REVURDERINGSÅRSAK_TYPE is 'FK: BEHANDLING_AARSAK';
comment on column FORRETNINGSHENDELSE_DEF.KL_REVURDERINGSÅRSAK_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column FORRETNINGSHENDELSE_DEF.STARTPUNKT_TYPE is 'FK: STARTPUNKT_TYPE. Startpunktet slik det er gitt av forretningshendelsen.';
comment on column FORRETNINGSHENDELSE_DEF.KL_STARTPUNKT_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column FORRETNINGSHENDELSE_DEF.BESKRIVELSE is 'Utdypende beskrivelse av koden';
comment on column BEHANDLING.STARTPUNKT_TYPE is 'FK: STARTPUNKT_TYPE. Startpunktet slik det er gitt av forretningshendelsen.';
comment on column BEHANDLING.KL_STARTPUNKT_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
