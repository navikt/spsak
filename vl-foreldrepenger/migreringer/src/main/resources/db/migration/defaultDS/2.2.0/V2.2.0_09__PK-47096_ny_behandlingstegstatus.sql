INSERT INTO KODELISTE (id, kode, navn, beskrivelse,kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'FREMOVERFØRT_MED_VEDTAK', 'Fremoverført til automatisk vedtak', 'Steget er fremoverført til fatte vedtak.', 'BEHANDLING_STEG_STATUS', to_date('2000-01-01', 'YYYY-MM-DD'));
