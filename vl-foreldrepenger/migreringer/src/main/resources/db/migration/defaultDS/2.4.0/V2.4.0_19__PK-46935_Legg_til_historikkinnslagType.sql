-- Kodeverk HISTORIKKINNSLAG_TYPE
insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'FORSLAG_VEDTAK_UTEN_TOTRINN', 'Vedtak foreslått', to_date('2017-01-01', 'YYYY-MM-DD'), '{"mal": "TYPE2"}');
