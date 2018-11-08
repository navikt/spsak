INSERT INTO KODEVERK (kode, navn, beskrivelse ) VALUES ('BEHANDLING_MOTIV_TYPE', 'BehandlingsmotivType', 'Internt kodeverk for behandlingsmotiv');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse,kodeverk, gyldig_fom) VALUES (seq_kodeliste.nextval, 'FØDSEL', 'Fødsel', 'Fødsel', 'BEHANDLING_MOTIV_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse,kodeverk, gyldig_fom) VALUES (seq_kodeliste.nextval, 'ADOPSJON', 'Adopsjon', 'Adopsjon', 'BEHANDLING_MOTIV_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse,kodeverk, gyldig_fom) VALUES (seq_kodeliste.nextval, 'OMSORG', 'Omsorg', 'Omsorg', 'BEHANDLING_MOTIV_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse,kodeverk, gyldig_fom) VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'BEHANDLING_MOTIV_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
