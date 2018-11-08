-- Tabell MEDLEMSKAP_VILKAR_PERIODER
CREATE TABLE MEDLEMSKAP_VILKAR_PERIODER (
  id                             NUMBER(19) NOT NULL,
  vilkar_resultat_id             NUMBER(19) NOT NULL,
  versjon                        NUMBER(19) NOT NULL,
  aktiv                          VARCHAR2(1 CHAR) NOT NULL,
  fom                            DATE NOT NULL,
  tom                            DATE,
  vilkar_utfall                  VARCHAR2(100 CHAR) NOT NULL,
  kl_vilkar_utfall_type          VARCHAR2(100 CHAR) DEFAULT 'VILKAR_UTFALL_TYPE' NOT NULL,
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_MEDLEMSKAP_VILKAR_PERIODER PRIMARY KEY (id),
  CONSTRAINT FK_MEDLEMSKAP_VILKAR_PERIODER FOREIGN KEY (vilkar_resultat_id) REFERENCES VILKAR_RESULTAT
);

CREATE INDEX MEDLEMSKAP_VILKAR_PERIODER_1 ON MEDLEMSKAP_VILKAR_PERIODER(vilkar_resultat_id);

CREATE SEQUENCE SEQ_MEDLEMSKAP_VILKAR_PERIODER MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE MEDLEMSKAP_VILKAR_PERIODER IS 'Periodisering av vilkårsvurderingen for medlemskap';
COMMENT ON COLUMN MEDLEMSKAP_VILKAR_PERIODER.fom IS 'Fra og med dato';
COMMENT ON COLUMN MEDLEMSKAP_VILKAR_PERIODER.tom IS 'Til og med dato';
COMMENT ON COLUMN MEDLEMSKAP_VILKAR_PERIODER.aktiv IS 'Til og med dato';
COMMENT ON COLUMN MEDLEMSKAP_VILKAR_PERIODER.vilkar_utfall IS 'Utfallet av medlemskapsvilkåret i den angitte perioden';
COMMENT ON COLUMN MEDLEMSKAP_VILKAR_PERIODER.kl_vilkar_utfall_type IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';


ALTER TABLE MEDLEMSKAP_VURDERING ADD fom DATE;
