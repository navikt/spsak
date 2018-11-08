INSERT INTO KODELISTE (id, kode, navn, beskrivelse,kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'FREMOVERFØRT', 'Fremoverført', 'Steget er fremoverført til et senere steg.', 'BEHANDLING_STEG_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
