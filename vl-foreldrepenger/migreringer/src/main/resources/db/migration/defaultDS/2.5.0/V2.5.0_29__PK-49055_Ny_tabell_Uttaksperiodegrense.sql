CREATE TABLE UTTAKSPERIODEGRENSE (
  ID                        NUMBER(19, 0)                                       NOT NULL,
  BEHANDLING_RESULTAT_ID    NUMBER(19, 0)                                       NOT NULL,
  MOTTATTDATO               TIMESTAMP(3)                                        NOT NULL,
  FOERSTE_LOVLIGE_UTTAKSDAG TIMESTAMP(3)                                        NOT NULL,
  SPORING_INPUT             CLOB                                                        ,
  SPORING_REGEL             CLOB                                                        ,
  AKTIV                     CHAR(1)                                             NOT NULL,
  VERSJON                   NUMBER(19)            DEFAULT 0                     NOT NULL,
  OPPRETTET_AV              VARCHAR2(20 char)     DEFAULT 'VL'                  NOT NULL,
  OPPRETTET_TID             TIMESTAMP(3)          DEFAULT systimestamp          NOT NULL,
  ENDRET_AV                 VARCHAR2(20 char),
  ENDRET_TID                TIMESTAMP(3),
  CONSTRAINT PK_UTTAKSPERIODEGRENSE PRIMARY KEY (ID),
  CONSTRAINT FK_UTTAKSPERIODEGRENSE FOREIGN KEY (BEHANDLING_RESULTAT_ID) REFERENCES BEHANDLING_RESULTAT(ID)

);
CREATE SEQUENCE SEQ_UTTAKSPERIODEGRENSE MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;


COMMENT ON TABLE UTTAKSPERIODEGRENSE IS 'Holder nedre grense for når første uttak kan starte';
COMMENT ON COLUMN UTTAKSPERIODEGRENSE.AKTIV IS '''J'' dersom aktiv, ellers ''N''.';
COMMENT ON COLUMN UTTAKSPERIODEGRENSE.MOTTATTDATO IS 'Mottatt dato for søknad. Dersom søknadsfrist blir automatisk avklar, så vil dette feltet inneholde mottattdato fra søknad, ellers vil dette inneholde bekreftet mottattdato fra aksjonspunktet.';
COMMENT ON COLUMN UTTAKSPERIODEGRENSE.FOERSTE_LOVLIGE_UTTAKSDAG IS 'Dato for første lovlige uttak utregnet utifra mottattdato i denne tabellen.';
COMMENT ON COLUMN UTTAKSPERIODEGRENSE.SPORING_INPUT IS 'Sporing av input til regel.';
COMMENT ON COLUMN UTTAKSPERIODEGRENSE.SPORING_REGEL IS 'Sporing av regelkjøring.';

UPDATE AKSJONSPUNKT_DEF SET VILKAR_TYPE='-', NAVN='Manuell vurdering av søknadsfrist for foreldrepenger' WHERE KODE='5043';

