-- Migrerer kodeverk for HistorikkInnslagType
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
values ('HISTORIKKINNSLAG_TYPE', 'N', 'N', 'Historikkinnslag type', 'Hvilken type historikkinnslag');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'BEH_VENT', 'Behandling på vent', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE1');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'BEH_GJEN', 'Behandling gjenopptatt', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE1');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'BEH_STARTET', 'Behandling startet', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE1');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'VEDLEGG_MOTTATT', 'Vedlegg mottatt', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE1');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'BREV_SENT', 'Brev sendt', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE1');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'REGISTRER_PAPIRSØK', 'Registrer papirsøknad', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE1');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'REVURD_OPPR', 'Revurdering opprettet', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE1');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'VRS_REV_IKKE_SNDT', 'Varsel om revurdering ikke sendt', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE1');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'MANGELFULL_SØKNAD', 'Mangelfull søknad', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE1');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'NYE_REGOPPLYSNINGER', 'Nye registeropplysninger', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE1');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'BEH_AVBRUTT_VUR', 'Vurdering før vedtak', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE1');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'KLAGE_BEH_NFP', 'Klage behandling NFP', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE1');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'KLAGE_BEH_NK', 'Klage behandling NK', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE1');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'FORSLAG_VEDTAK', 'Forslag vedtak', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE2');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'VEDTAK_FATTET', 'Vedtak fattet', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE2');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'OVERSTYRT', 'Overstyrt', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE2');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'SAK_RETUR', 'Sak retur', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE3');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'AVBRUTT_BEH', 'Avbrutt behandling', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE4');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'FAKTA_ENDRET', 'Fakta endret', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE5');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'BYTT_ENHET', 'Bytt enhet', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE5');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'NY_INFO_FRA_TPS', 'Ny info fra TPS', to_date('2017-01-01', 'YYYY-MM-DD'), 'TYPE6');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
values (seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', '-', 'Ikke definert', to_date('2017-01-01', 'YYYY-MM-DD'));
