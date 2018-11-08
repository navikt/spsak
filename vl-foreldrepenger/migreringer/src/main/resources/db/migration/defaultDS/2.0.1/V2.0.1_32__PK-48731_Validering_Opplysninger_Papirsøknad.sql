-- Nytt kodeverk for Andre ytelser
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('ANDRE_YTELSER', 'N', 'N',  'Andre ytelser', 'Kodeverk for andre ytelser');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'LONN_UTDANNING', 'Lønn under utdanning', 'Lønn under utdanning', 'ANDRE_YTELSER', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'ETTERLONN_ARBEIDSGIVER', 'Etterlønn arbeidsgiver', 'Etterlønn arbeidsgiver', 'ANDRE_YTELSER', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'MILITAER_SIVIL_TJENESTE', 'Militær eller siviltjeneste', 'Militær eller siviltjeneste', 'ANDRE_YTELSER', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'VENTELONN', 'Ventelønn', 'Ventelønn', 'ANDRE_YTELSER', to_date('2000-01-01', 'YYYY-MM-DD'));

--Nytt aksjonspunkt papirsøknad foreldrepenger
INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT) VALUES ('5040', 'Registrer papirsøknad foreldrepenger', 'REGSØK.UT', 'Registrer ustrukturert papirsøknad foreldrepenger', '-', 'N');

--Oppdater aksjonspunkt papirsøknad engangsstønad
UPDATE AKSJONSPUNKT_DEF SET NAVN = 'Registrer papirsøknad engangsstønad', BESKRIVELSE = 'Registrer ustrukturert papirsøknad engangsstønad' WHERE KODE = '5012';


alter table MEDLEMSKAP_OPPG_UTLAND rename to MEDLEMSKAP_OPPG_LAND;

-- Migrer data fra medlemskap_oppg_tilknytning til medlemskap_oppg_land
insert into MEDLEMSKAP_OPPG_LAND (id, OPPHOLD_HJEMLAND, periode_fom, periode_tom, TIDLIGERE_OPPHOLD, LAND)
select
  SEQ_UTLANDSOPPHOLD.nextval as neste_verdi,
  m.id as medl_id,
  s.mottatt_dato - 365 as per_fom,
  s.mottatt_dato as per_tom,
  'J',
  'NOR'
FROM MEDLEMSKAP_OPPG_TILKNYT m
inner join soeknad s on s.TILKNYTNING_HJEMLAND_ID = m.id
where m.OPPHOLD_SISTE_PERIODE = 'J';

-- Migrer data fra medlemskap_oppg_tilknytning til medlemskap_oppg_land
insert into MEDLEMSKAP_OPPG_LAND (id, OPPHOLD_HJEMLAND, periode_fom, periode_tom, TIDLIGERE_OPPHOLD, LAND)
select
  SEQ_UTLANDSOPPHOLD.nextval as neste_verdi,
  m.id as medl_id,
  s.mottatt_dato as per_fom,
  s.mottatt_dato + 365 as per_tom,
  'N',
  'NOR'
FROM MEDLEMSKAP_OPPG_TILKNYT m
inner join soeknad s on s.TILKNYTNING_HJEMLAND_ID = m.id
where m.OPPHOLD_NESTE_PERIODE = 'J';

-- Rename kolonne opphold_norge_naa til ophhold_naa
alter table medlemskap_oppg_tilknyt rename column opphold_norge_naa to opphold_naa;

-- Ny kolonne oppgitt_dato
alter table medlemskap_oppg_tilknyt add oppgitt_dato DATE;
merge into medlemskap_oppg_tilknyt
using soeknad
on (medlemskap_oppg_tilknyt.id = soeknad.TILKNYTNING_HJEMLAND_ID)
when matched then update set medlemskap_oppg_tilknyt.oppgitt_dato = soeknad.mottatt_dato;


-- Fjern kolonner fra MEDLEMSKAP_OPPG_TILKNYT
ALTER TABLE MEDLEMSKAP_OPPG_TILKNYT DROP COLUMN OPPHOLD_SISTE_PERIODE;
ALTER TABLE MEDLEMSKAP_OPPG_TILKNYT DROP COLUMN OPPHOLD_NESTE_PERIODE;

ALTER TABLE MEDLEMSKAP_OPPG_TILKNYT DROP CONSTRAINT FK_TILKNYTNING_HJEMLAND_80;
ALTER TABLE MEDLEMSKAP_OPPG_TILKNYT DROP COLUMN KL_PERIODE_TYPE;
ALTER TABLE MEDLEMSKAP_OPPG_TILKNYT DROP COLUMN PERIODE_TYPE;
