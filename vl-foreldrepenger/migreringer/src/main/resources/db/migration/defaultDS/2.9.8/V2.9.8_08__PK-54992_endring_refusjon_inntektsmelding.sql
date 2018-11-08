CREATE TABLE IAY_REFUSJON (
  id                     NUMBER(19)                        NOT NULL,
  inntektsmelding_id     NUMBER(19)                        NOT NULL,
  refusjonsbeloep_mnd    NUMBER(10, 2)                     NOT NULL,
  fom                    DATE                              NOT NULL,
  versjon                NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av           VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid          TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av              VARCHAR2(20 CHAR),
  endret_tid             TIMESTAMP(3),
  CONSTRAINT PK_IAY_REFUSJON PRIMARY KEY (id),
  CONSTRAINT FK_IAY_REFUSJON_1 FOREIGN KEY (inntektsmelding_id) REFERENCES IAY_INNTEKTSMELDING
);
CREATE INDEX IDX_IAY_REFUSJON_1 ON IAY_REFUSJON(INNTEKTSMELDING_ID);

CREATE SEQUENCE SEQ_REFUSJON
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NOCACHE
  NOCYCLE;

COMMENT ON TABLE IAY_REFUSJON IS 'Endringer i refusjonsbeløp fra en oppgitt dato';
COMMENT ON COLUMN IAY_REFUSJON.ID IS 'Primær nøkkel';
COMMENT ON COLUMN IAY_REFUSJON.INNTEKTSMELDING_ID IS 'Fremmednøkkel til inntektsmelding';
COMMENT ON COLUMN IAY_REFUSJON.REFUSJONSBELOEP_MND IS 'Verdi i kroner per måned';
COMMENT ON COLUMN IAY_REFUSJON.FOM IS 'Dato refusjonsbeløpet gjelder fra';
