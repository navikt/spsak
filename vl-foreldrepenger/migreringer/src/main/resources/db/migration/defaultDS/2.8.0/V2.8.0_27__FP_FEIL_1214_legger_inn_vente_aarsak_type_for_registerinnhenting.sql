INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VENT_AARSAK', 'VENT_REGISTERINNHENTING', 'Venter på registerinformasjon', 'Venter på registerinnformasjon fra eksterne systemer', to_date('2000-01-01', 'YYYY-MM-DD'));

UPDATE AKSJONSPUNKT_DEF set AKSJONSPUNKT_TYPE = 'AUTO' where kode = '7013';
