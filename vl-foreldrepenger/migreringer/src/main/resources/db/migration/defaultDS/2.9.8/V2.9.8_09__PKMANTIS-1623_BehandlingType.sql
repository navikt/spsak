MERGE INTO KODELISTE kodeliste
USING (SELECT
         a.KODE,
         a.BEHANDLINGSTID_FRIST_UKER,
         a.BEHANDLINGSTID_VARSELBREV
       FROM BEHANDLING_TYPE a) bt
ON (bt.KODE = kodeliste.KODE and kodeliste.KODEVERK = 'BEHANDLING_TYPE')
WHEN MATCHED THEN
UPDATE SET kodeliste.ekstra_data = '{ "behandlingstidFristUker" : ' || bt.BEHANDLINGSTID_FRIST_UKER || ', "behandlingstidVarselbrev" : "' || bt.BEHANDLINGSTID_VARSELBREV || '" }';

DROP TABLE BEHANDLING_TYPE;

INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'BEHANDLING_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
