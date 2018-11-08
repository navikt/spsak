INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) 
VALUES (seq_kodeliste.nextval, 'ÅPEN_BEHANDLING_IKKE_SENDT', 'Brev kan bare sendes en gang', 'Brev kan bare sendes en gang og fra en åpen behandling', to_date('2000-01-01', 'YYYY-MM-DD'), 'DOKUMENT_MAL_RESTRIKSJON');

UPDATE DOKUMENT_MAL_TYPE set DOKUMENT_MAL_RESTRIKSJON = 'ÅPEN_BEHANDLING_IKKE_SENDT' WHERE KODE = 'FORLME';
