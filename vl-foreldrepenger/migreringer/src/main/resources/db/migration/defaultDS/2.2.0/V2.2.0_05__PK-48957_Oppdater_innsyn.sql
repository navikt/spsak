-- Definer stegsekvens for behandlingstype Innsyn for foreldrepenger
insert into behandling_type_steg_sekv (id, behandling_type, behandling_steg_type, fagsak_ytelse_type, sekvens_nr)
values (seq_behandling_type_steg_sekv.nextval, 'BT-006', 'INPER', 'FP', 1);
insert into behandling_type_steg_sekv (id, behandling_type, behandling_steg_type, fagsak_ytelse_type, sekvens_nr)
values (seq_behandling_type_steg_sekv.nextval, 'BT-006', 'VURDINNSYN', 'FP', 2);
insert into behandling_type_steg_sekv (id, behandling_type, behandling_steg_type, fagsak_ytelse_type, sekvens_nr)
values (seq_behandling_type_steg_sekv.nextval, 'BT-006', 'FORVEDSTEG', 'FP', 3);
insert into behandling_type_steg_sekv (id, behandling_type, behandling_steg_type, fagsak_ytelse_type, sekvens_nr)
values (seq_behandling_type_steg_sekv.nextval, 'BT-006', 'FVEDSTEG', 'FP', 4);
insert into behandling_type_steg_sekv (id, behandling_type, behandling_steg_type, fagsak_ytelse_type, sekvens_nr)
values (seq_behandling_type_steg_sekv.nextval, 'BT-006', 'IVEDSTEG', 'FP', 5);


-- Tabell INNSYN_DOKUMENT
create table INNSYN_DOKUMENT (
  ID                         number(19, 0) not null,
  INNSYN_ID                  number(19, 0) not null,
  JOURNALPOST_ID             varchar2(100 char) not null,
  DOKUMENT_ID                varchar2(4000 char) not null,
  FIKK_INNSYN                char(1 byte) default 'N' not null,
  VERSJON                    number(19, 0) default 0 not null,
  OPPRETTET_AV               varchar2(20 char) default 'VL' not null,
  OPPRETTET_TID              timestamp(3) default systimestamp not null,
  ENDRET_AV                  varchar2(20 char),
  ENDRET_TID                 timestamp(3),
  constraint PK_INNSYN_DOKUMENT primary key (ID)
);

comment on table INNSYN_DOKUMENT is 'Dokumenter som kan legges ved som vedlegg i innsynsbehandling';
comment on column INNSYN_DOKUMENT.ID is 'Primary Key';
comment on column INNSYN_DOKUMENT.INNSYN_ID is 'FK: INNSYN';
comment on column INNSYN_DOKUMENT.DOKUMENT_ID is 'Id på dokumentet i JOARK';
comment on column INNSYN_DOKUMENT.JOURNALPOST_ID is 'Id på journalpost i JOARK';
comment on column INNSYN_DOKUMENT.FIKK_INNSYN is 'Verdi som sier om dokumentet skal være med som vedlegg eller ikke';

CREATE SEQUENCE SEQ_INNSYN_DOKUMENT MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;


alter table INNSYN add INNSYN_DOKUMENT_ID number(19, 0);
alter table INNSYN add constraint FK_INNSYN_DOKUMENT foreign key (INNSYN_DOKUMENT_ID) references INNSYN_DOKUMENT;

-- Aksjonspunkt
insert into aksjonspunkt_def (kode, navn, vurderingspunkt, beskrivelse, VILKAR_TYPE, totrinn_behandling_default, tilbakehopp_ved_gjenopptakelse)
values (7007, 'Venter på scanning', 'VURDINNSYN.UT', 'Venter på scanning av dokumenter', '-', 'N', 'J');
insert into aksjonspunkt_def (kode, navn, vurderingspunkt, beskrivelse, VILKAR_TYPE, totrinn_behandling_default)
values (5041, 'Foreslå vedtak - innsyn', 'FORVEDSTEG.UT', 'Foreslå vedtak om innsyn', '-', 'N');
