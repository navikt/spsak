INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'FLERBARNSDAGER', 'Ekstra dager som legges til fellesperioden ved flerbarnsfødsel.', 'STOENADSKONTOTYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'STOENADSKONTOTYPE', 'FLERBARNSDAGER', 'NB', 'Flerbarnsdager');

UPDATE STOENADSKONTO SET STOENADSKONTOTYPE='FLERBARNSDAGER' WHERE STOENADSKONTOTYPE='SAMTIDIGUTTAK';

DELETE FROM KODELISTE_NAVN_I18N WHERE KL_KODEVERK = 'STOENADSKONTOTYPE' AND KL_KODE = 'SAMTIDIGUTTAK';
DELETE FROM KODELISTE WHERE kode = 'SAMTIDIGUTTAK' and kodeverk = 'STOENADSKONTOTYPE';

ALTER TABLE UTTAK_RESULTAT_PERIODE ADD FLERBARNSDAGER CHAR;
UPDATE UTTAK_RESULTAT_PERIODE SET FLERBARNSDAGER = SAMTIDIG_UTTAK;
ALTER TABLE UTTAK_RESULTAT_PERIODE MODIFY FLERBARNSDAGER CHAR NOT NULL CHECK (FLERBARNSDAGER IN ('J', 'N'));

COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE.FLERBARNSDAGER IS 'Angir om flerbarnsdager skal benyttes i perioden';

ALTER TABLE YF_FORDELING_PERIODE ADD FLERBARNSDAGER CHAR;
UPDATE YF_FORDELING_PERIODE SET FLERBARNSDAGER = SAMTIDIG_UTTAK;
ALTER TABLE YF_FORDELING_PERIODE MODIFY FLERBARNSDAGER CHAR NOT NULL CHECK (FLERBARNSDAGER IN ('J', 'N'));

COMMENT ON COLUMN YF_FORDELING_PERIODE.FLERBARNSDAGER IS 'Angir om flerbarnsdager skal benyttes i perioden';
