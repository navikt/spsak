ALTER TABLE BEHANDLING_RESULTAT ADD (OVERSKRIFT VARCHAR2(200 CHAR), FRITEKSTBREV CLOB);
COMMENT ON COLUMN BEHANDLING_RESULTAT.OVERSKRIFT IS 'Overskrift felt brukt som hovedoverskrift i frikestbrev';
COMMENT ON COLUMN BEHANDLING_RESULTAT.FRITEKSTBREV IS 'Fritekstbrev felt brukt som hovedoverskrift i frikestbrev';

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VEDTAKSBREV', 'FRITEKST', 'Fritekstbrev', to_date('2000-01-01', 'YYYY-MM-DD'));
