-- oppdater kodeliste med manglande innslag
INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
VALUES(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'KLAGEBEH_STARTET', 'Klage mottatt', to_date('2017-01-01', 'YYYY-MM-DD'), '{"mal": "TYPE1"}');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom, ekstra_data)
VALUES(seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'UENDRET_UTFALL', 'Uendret utfall', 'Revurdering ga samme resultat som opprinnelig behandling' ,to_date('2017-01-01', 'YYYY-MM-DD'), '{"mal": "TYPE2"}');

-- fjern refs til kodeverk
ALTER TABLE HISTORIKKINNSLAG DROP CONSTRAINT FK_HISTORIKKINNSLAG_TYPE;

-- konverter fra kodeverk til kodeliste for historikkinnslag_type
ALTER TABLE HISTORIKKINNSLAG RENAME COLUMN historikkinnslag_type_id TO historikkinnslag_type;
ALTER TABLE HISTORIKKINNSLAG MODIFY (historikkinnslag_type VARCHAR2(100 CHAR));
ALTER TABLE HISTORIKKINNSLAG ADD kl_historikkinnslag_type VARCHAR2(100 CHAR) AS ('HISTORIKKINNSLAG_TYPE') NOT NULL;

ALTER TABLE HISTORIKKINNSLAG ADD CONSTRAINT FK_HISTORIKKINNSLAG_5  FOREIGN KEY (kl_historikkinnslag_type, historikkinnslag_type) REFERENCES KODELISTE (kodeverk, kode);

CREATE INDEX IDX_HISTORIKKINNSLAG_03 ON HISTORIKKINNSLAG(historikkinnslag_type);

-- comments
COMMENT ON COLUMN HISTORIKKINNSLAG.historikkinnslag_type IS 'Kodeverk Primary Key';
COMMENT ON COLUMN HISTORIKKINNSLAG.kl_historikkinnslag_type IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';

-- fjern, nå, ubrukt tabell
DROP TABLE HISTORIKKINNSLAG_TYPE;

