insert into KODELISTE (id, kodeverk, kode, BESKRIVELSE, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'BEH_OPPDATERT_NYE_OPPL', 'Behandling oppdatert med nye opplysninger', to_date('2017-01-01', 'YYYY-MM-DD'), '{"mal": "TYPE1"}');
insert into KODELISTE (id, kodeverk, kode, BESKRIVELSE, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'SPOLT_TILBAKE', 'Behadnling er flyttet', to_date('2017-01-01', 'YYYY-MM-DD'), '{"mal": "TYPE1"}');
