CREATE TABLE IAY_ARBEIDSFORHOLD_REF (
  id                         NUMBER(19)                        NOT NULL,
  intern_referanse           VARCHAR2(100 CHAR)                     NOT NULL,
  ekstern_referanse          VARCHAR2(100 CHAR)                     NOT NULL,
  arbeidsgiver_aktor_id      VARCHAR2(100 CHAR),
  arbeidsgiver_virksomhet_id NUMBER(19),
  versjon                    NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av               VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid              TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                  VARCHAR2(20 CHAR),
  endret_tid                 TIMESTAMP(3),
  CONSTRAINT PK_IAY_ARBEIDSFORHOLD_REF PRIMARY KEY (id)
);

ALTER TABLE IAY_ARBEIDSFORHOLD_REF
  ADD CONSTRAINT FK_IAY_ARBEIDSFORHOLD_REF_1 FOREIGN KEY (arbeidsgiver_virksomhet_id) REFERENCES VIRKSOMHET;

CREATE SEQUENCE SEQ_ARBEIDSFORHOLD_REF
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NOCACHE
  NOCYCLE;

CREATE INDEX IDX_IAY_ARBEIDSFORHOLD_REF_1
  ON IAY_ARBEIDSFORHOLD_REF (intern_referanse);
CREATE INDEX IDX_IAY_ARBEIDSFORHOLD_REF_2
  ON IAY_ARBEIDSFORHOLD_REF (arbeidsgiver_virksomhet_id);

COMMENT ON TABLE IAY_ARBEIDSFORHOLD_REF IS 'Kobling mellom arbeidsforhold fra aa-reg og intern nøkkel for samme representasjon';
COMMENT ON COLUMN IAY_ARBEIDSFORHOLD_REF.intern_referanse IS 'Syntetisk nøkkel for å representere et arbeidsforhold';
COMMENT ON COLUMN IAY_ARBEIDSFORHOLD_REF.ekstern_referanse IS 'ArbeidsforholdId hentet fra AA-reg';
COMMENT ON COLUMN IAY_ARBEIDSFORHOLD_REF.arbeidsgiver_aktor_id IS 'Aktør til personlig foretak.';
