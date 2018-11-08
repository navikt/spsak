create table YF_AVKLART_DATO (
  id                NUMBER(19)                        NOT NULL,
  forste_uttaksdato DATE,
  endringsdato      DATE,
  opprettet_av      VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid     TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av         VARCHAR2(20 CHAR),
  endret_tid        TIMESTAMP(3),
  versjon           NUMBER(19) DEFAULT 0              NOT NULL,
  CONSTRAINT PK_YF_AVKLART_DATO PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_YF_AVKLART_DATO
  MINVALUE 1000000
  START WITH 1000000
  INCREMENT BY 50
  NOCACHE
NOCYCLE;

COMMENT ON TABLE YF_AVKLART_DATO
IS 'Avklarte datoer til uttaksvurdering';
COMMENT ON COLUMN YF_AVKLART_DATO.forste_uttaksdato
IS 'Avklart første uttaksdato, settes ved avvik mellom søknad og inntektsmeldinger';
COMMENT ON COLUMN YF_AVKLART_DATO.endringsdato
IS 'Endringsdatoen';

ALTER TABLE GR_YTELSES_FORDELING
  ADD (
  yf_AVKLART_DATO_id NUMBER(19)
  );


ALTER TABLE GR_YTELSES_FORDELING
  ADD CONSTRAINT FK_GR_YTELSES_FORDELING_9 FOREIGN KEY (yf_AVKLART_DATO_id) REFERENCES YF_AVKLART_DATO (ID);

CREATE INDEX IDX_GR_YTELSES_FORDELING_11 ON GR_YTELSES_FORDELING (yf_AVKLART_DATO_id);
