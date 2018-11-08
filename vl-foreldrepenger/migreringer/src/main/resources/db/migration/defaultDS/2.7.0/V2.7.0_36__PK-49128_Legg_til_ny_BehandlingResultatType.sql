-- Ny verdi i BehandlingResultatType
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'MERGET_OG_HENLAGT', 'Merget og henlagt', 'Søknaden er henlagt, ble merget sammen med nyere søknad', 'BEHANDLING_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
