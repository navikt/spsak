--merge into i tilfelle kodeverksynkronisering har vært kjørt
merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'BEHANDLING_TYPE' and k.KODE = 'BT-006')
when not matched then
insert (ID, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, KODEVERK, GYLDIG_FOM) values (seq_kodeliste.nextval, 'BT-006', 'ae0042', 'Dokumentinnsyn', 'Dokumentinnsyn', 'BEHANDLING_TYPE', TO_DATE('2006-07-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

insert into BEHANDLING_TYPE (KODE,BEHANDLINGSTID_FRIST_UKER,BEHANDLINGSTID_VARSELBREV)
values ('BT-006','1','N');

-- Nytt steg 'Innhent personopplysninger'
insert into behandling_steg_type (kode, navn, behandling_status_def, beskrivelse )
values ('INPER', 'Innhent personopplysninger', 'UTRED', 'Innhenter personopplysninger på nytt fra TPS for at navn, adresse, fødselsnummer m.m. skal være ansatt');

insert into vurderingspunkt_def (kode, behandling_steg, vurderingspunkt_type, navn)
values ('INPER.INN', 'INPER', 'INN', 'Innhent personopplysninger - Inngang');

insert into vurderingspunkt_def (kode, behandling_steg, vurderingspunkt_type, navn)
values ('INPER.UT', 'INPER', 'UT', 'Innhent personopplysninger - Utgang');

-- Nytt steg 'Vurder innsynskrav'
insert into behandling_steg_type (kode, navn, behandling_status_def, beskrivelse )
values ('VURDINNSYN', 'Vurder innsynskrav', 'UTRED', 'Vurder om bruker har rett til innsyn i saken');

insert into vurderingspunkt_def (kode, behandling_steg, vurderingspunkt_type, navn)
values ('VURDINNSYN.INN', 'VURDINNSYN', 'INN', 'Vurder innsyn - Inngang');

insert into vurderingspunkt_def (kode, behandling_steg, vurderingspunkt_type, navn)
values ('VURDINNSYN.UT', 'VURDINNSYN', 'UT', 'Vurder innsyn - Utgang');

-- Definer stegsekvens for behandlingstype Innsyn
insert into behandling_type_steg_sekv (id, behandling_type, behandling_steg_type, fagsak_ytelse_type, sekvens_nr)
values (seq_behandling_type_steg_sekv.nextval, 'BT-006', 'INPER', 'ES', 1);
insert into behandling_type_steg_sekv (id, behandling_type, behandling_steg_type, fagsak_ytelse_type, sekvens_nr)
values (seq_behandling_type_steg_sekv.nextval, 'BT-006', 'VURDINNSYN', 'ES', 2);
insert into behandling_type_steg_sekv (id, behandling_type, behandling_steg_type, fagsak_ytelse_type, sekvens_nr)
values (seq_behandling_type_steg_sekv.nextval, 'BT-006', 'FORVEDSTEG', 'ES', 3);
insert into behandling_type_steg_sekv (id, behandling_type, behandling_steg_type, fagsak_ytelse_type, sekvens_nr)
values (seq_behandling_type_steg_sekv.nextval, 'BT-006', 'FVEDSTEG', 'ES', 4);
insert into behandling_type_steg_sekv (id, behandling_type, behandling_steg_type, fagsak_ytelse_type, sekvens_nr)
values (seq_behandling_type_steg_sekv.nextval, 'BT-006', 'IVEDSTEG', 'ES', 5);

-- Kodeverk
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK)
values (seq_kodeliste.nextval, 'INNSYN', 'Dokumentinnsyn', 'Dokumentinnsyn', 'LAGRET_VEDTAK_TYPE');

insert into VENT_AARSAK (KODE,NAVN)
values ('SCANN','Venter på scanning');

insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK)
values (seq_kodeliste.nextval, '5037', 'Vurder innsyn', 'Vurder innsyn', 'AKSJONSPUNKT_TYPE');

-- Aksjonspunkt
insert into aksjonspunkt_def (kode, navn, vurderingspunkt, beskrivelse, VILKAR_TYPE, totrinn_behandling_default)
values (5037, 'Vurder innsyn', 'VURDINNSYN.UT', 'Vurder om bruker har rett til innsyn i saken', '-', 'N');

-- Tabell INNSYN
create table INNSYN (
  ID                         number(19, 0) not null,
  BEHANDLING_ID              number(19, 0) not null,
  MOTTATT_DATO               date not null,
  INNSYN_RESULTAT_TYPE       varchar2(100 char) not null,
  KL_INNSYN_RESULTAT_TYPE    varchar2(100 char) as ('INNSYN_RESULTAT_TYPE'),
  BEGRUNNELSE                varchar2(4000 char) not null,
  VERSJON                    number(19, 0) default 0 not null,
  OPPRETTET_AV               varchar2(20 char) default 'VL' not null,
  OPPRETTET_TID              timestamp(3) default systimestamp not null,
  ENDRET_AV                  varchar2(20 char),
  ENDRET_TID                 timestamp(3),
  constraint PK_INNSYN primary key (ID)
);

comment on table INNSYN is 'Resultat av innsynsbehandling';
comment on column INNSYN.ID is 'Primary Key';
comment on column INNSYN.BEHANDLING_ID is 'FK: BEHANDLING';
comment on column INNSYN.MOTTATT_DATO is 'Dato for mottak av innsynskrav';
comment on column INNSYN.INNSYN_RESULTAT_TYPE is 'FK: INNSYN_RESULTAT_TYPE';
comment on column INNSYN.BEGRUNNELSE is 'Saksbehandlers begrunnelse for svar på innsynskrav';

CREATE SEQUENCE SEQ_INNSYN MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
create unique index UIDX_INNSYN_01 on INNSYN (BEHANDLING_ID);

alter table INNSYN add constraint FK_INNSYN_BEH foreign key (BEHANDLING_ID) references BEHANDLING;
alter table INNSYN add constraint FK_INNSYN_RESULTAT foreign key (KL_INNSYN_RESULTAT_TYPE, INNSYN_RESULTAT_TYPE) references KODELISTE(KODEVERK, KODE);


-- Kodeverk INNSYN_RESULTAT_TYPE
insert into KODEVERK (KODE, NAVN, BESKRIVELSE ) values ('INNSYN_RESULTAT_TYPE', 'Type for innsyn resultat', '');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK)
values (seq_kodeliste.nextval, 'INNV', 'Innvilget innsyn', 'Innvilget innsyn', 'INNSYN_RESULTAT_TYPE');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK)
values (seq_kodeliste.nextval, 'DELV', 'Delvis innvilget innsyn', 'Delvis innvilget innsyn', 'INNSYN_RESULTAT_TYPE');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK)
values (seq_kodeliste.nextval, 'AVVIST', 'Avslått innsyn', 'Avslått innsyn', 'INNSYN_RESULTAT_TYPE');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK)
values (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'INNSYN_RESULTAT_TYPE');



-- Kodeverk HISTORIKKINNSLAG_TYPE
insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'INNSYN_OPPR', 'Behandling om innsyn opprettet', to_date('2017-01-01', 'YYYY-MM-DD'), '{"mal": "TYPE1"}');
