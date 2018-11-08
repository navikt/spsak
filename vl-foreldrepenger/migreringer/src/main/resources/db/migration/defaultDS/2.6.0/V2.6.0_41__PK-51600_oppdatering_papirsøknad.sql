insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'DAGMAMMA', 'Dagmamma/familiebarnehage', 'Dagmamma/familiebarnehage', 'VIRKSOMHET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

DELETE FROM KODELISTE WHERE kode = 'FRILANS' and KODEVERK = 'VIRKSOMHET_TYPE';

/* Utvider modell for egen næring med informasjon om næring er nylig startet */
alter table EGEN_NAERING add nyoppstartet VARCHAR2(1 CHAR) DEFAULT 'N';

/* Stilling er fjernet fra søknad og slettes derfor fra modell */
drop table STILLING;
drop sequence SEQ_STILLING;
