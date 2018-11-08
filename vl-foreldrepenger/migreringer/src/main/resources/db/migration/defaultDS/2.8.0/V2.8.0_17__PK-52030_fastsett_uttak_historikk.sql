insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'FASTSATT_UTTAK', 'Manuelt fastsetting av uttak', to_date('2017-01-01', 'YYYY-MM-DD'), '{"mal": "TYPE10"}');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'FASTSATT_UTTAK_SPLITT', 'Manuelt fastsetting av uttak - splitting av periode', to_date('2017-01-01', 'YYYY-MM-DD'), '{"mal": "TYPE9"}');
