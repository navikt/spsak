create table MOTTATT_HENDELSE (
  HENDELSE_UID               varchar2(100 char) not null,
  OPPRETTET_AV               varchar2(20 char) default 'VL' not null,
  OPPRETTET_TID              timestamp(3) default systimestamp not null,
  ENDRET_AV                  varchar2(20 char),
  ENDRET_TID                 timestamp(3),
  constraint PK_HENDELSE     primary key (HENDELSE_UID)
);

comment on table MOTTATT_HENDELSE is 'Holder unik identifikator for alle mottatte hendelser. Brukes for å unngå at en hendelse medfører flere revurderinger';
comment on column MOTTATT_HENDELSE.HENDELSE_UID is 'Unik identifikator for hendelse mottatt';

insert into prosess_task_type (kode, navn, feil_maks_forsoek, feil_sek_mellom_forsoek, feilhandtering_algoritme, beskrivelse)
  values ('hendelser.klargjoering', 'Klargjøring av mottatt forretningshendelse.', 3, 60, 'DEFAULT', 'Første steg av håndtering av mottatt forretningshendelse. Identifiserer saker som er kandidat for revurdering.');
