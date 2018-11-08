INSERT INTO BEHANDLING_STEG_TYPE (kode, navn, behandling_status_def, beskrivelse)
VALUES ('KOARB', 'Kontroller arbeidsforhold', 'UTRED', 'Kontroll av arbeidsforhold');

INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'KOARB', 49, 'FP');

INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'KOARB', 49, 'FP');

INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN)
VALUES ('KOARB.UT', 'KOARB', 'UT', 'Kontroller Arbeidsforhold');

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, SKJERMLENKE_TYPE)
VALUES ('5080', 'Avklar arbeidsforhold', 'KOARB.UT',
        'Oppstår enten ved at Inntektsmelding mangler for arbeidsforhold i Aa-reg eller Arbeidsforhold i inntektsmelding kan ikke identifiseres i forhold til arbeidsforhold som er avklart tidligere',
        '-', 'J', '-');

CREATE TABLE IAY_INFORMASJON (
  id            NUMBER(19)                        NOT NULL,
  gr_id         NUMBER(19)                        NOT NULL,
  versjon       NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av  VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av     VARCHAR2(20 CHAR),
  endret_tid    TIMESTAMP(3),
  CONSTRAINT PK_IAY_INFORMASJON PRIMARY KEY (id)
);

COMMENT ON TABLE IAY_INFORMASJON
IS 'Mange til mange tabell for arbeidsforhold referanse og overstyrende betraktninger om arbeidsforhold';

CREATE SEQUENCE SEQ_IAY_INFORMASJON
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NOCACHE
  NOCYCLE;

ALTER TABLE GR_ARBEID_INNTEKT
  ADD (informasjon_id NUMBER(19));
ALTER TABLE GR_ARBEID_INNTEKT
  ADD CONSTRAINT FK_GR_ARBEID_INNTEKT_6 FOREIGN KEY (informasjon_id) REFERENCES IAY_INFORMASJON (id);
CREATE INDEX IDX_GR_ARBEID_INNTEKT_6
  ON GR_ARBEID_INNTEKT (informasjon_id);

ALTER TABLE IAY_ARBEIDSFORHOLD_REF
RENAME TO TMP_ARBEIDSFORHOLD_REF;
ALTER TABLE TMP_ARBEIDSFORHOLD_REF
RENAME CONSTRAINT PK_IAY_ARBEIDSFORHOLD_REF TO TMP_ARBEIDSFORHOLD_REF_1;
ALTER TABLE TMP_ARBEIDSFORHOLD_REF
RENAME CONSTRAINT FK_IAY_ARBEIDSFORHOLD_REF_1 TO TMP_ARBEIDSFORHOLD_REF_2;

CREATE TABLE IAY_ARBEIDSFORHOLD_REFER (
  id                         NUMBER(19)                        NOT NULL,
  informasjon_id             NUMBER(19)                        NOT NULL,
  intern_referanse           VARCHAR2(100 CHAR)                NOT NULL,
  ekstern_referanse          VARCHAR2(100 CHAR)                NOT NULL,
  arbeidsgiver_aktor_id      VARCHAR2(100 CHAR),
  arbeidsgiver_virksomhet_id NUMBER(19),
  versjon                    NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av               VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid              TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                  VARCHAR2(20 CHAR),
  endret_tid                 TIMESTAMP(3),
  CONSTRAINT PK_IAY_ARBEIDSFORHOLD_REFER PRIMARY KEY (id),
  CONSTRAINT FK_IAY_ARBEIDSFORHOLD_REFER_1 FOREIGN KEY (informasjon_id) REFERENCES IAY_INFORMASJON (id)
);

INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES
  ('ARBEIDSFORHOLD_HANDLING_TYPE',
   'Kodeverk over gyldige typer av handlinger saksbehandler kan utføre av overstyringer på arbeidsforhold', '',
   'VL');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, '-', 'Udefinert', 'Udefinert', to_date('2000-01-01', 'YYYY-MM-DD'),
        'ARBEIDSFORHOLD_HANDLING_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES
  (seq_kodeliste.nextval, 'BRUK', 'Bruk', 'Bruk', to_date('2000-01-01', 'YYYY-MM-DD'), 'ARBEIDSFORHOLD_HANDLING_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'BRUK_UTEN_INNTEKTSMELDING', 'Bruk, men ikke benytt inntektsmelding',
        'Bruk men ikke benytt inntektsmelding', to_date('2000-01-01', 'YYYY-MM-DD'), 'ARBEIDSFORHOLD_HANDLING_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'IKKE_BRUK', 'Ikke bruk', 'Ikke bruk', to_date('2000-01-01', 'YYYY-MM-DD'),
        'ARBEIDSFORHOLD_HANDLING_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'SLÅTT_SAMMEN_MED_ANNET', 'Arbeidsforholdet er slått sammen med et annet', 'Arbeidsforholdet er slått sammen med et annet', to_date('2000-01-01', 'YYYY-MM-DD'),
        'ARBEIDSFORHOLD_HANDLING_TYPE');

CREATE TABLE IAY_ARBEIDSFORHOLD (
  id                         NUMBER(19)                        NOT NULL,
  informasjon_id             NUMBER(19)                        NOT NULL,
  arbeidsforhold_id          VARCHAR2(100 CHAR)                NOT NULL,
  ny_arbeidsforhold_id          VARCHAR2(100 CHAR)                ,
  arbeidsgiver_aktor_id      VARCHAR2(100 CHAR),
  arbeidsgiver_virksomhet_id NUMBER(19),
  begrunnelse                VARCHAR2(2000 CHAR),
  handling_type              varchar2(100 char)                NOT NULL,
  kl_handling_type           VARCHAR2(100 char) AS ('ARBEIDSFORHOLD_HANDLING_TYPE'),
  versjon                    NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av               VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid              TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                  VARCHAR2(20 CHAR),
  endret_tid                 TIMESTAMP(3),
  CONSTRAINT PK_IAY_ARBEIDSFORHOLD PRIMARY KEY (id),
  CONSTRAINT FK_IAY_ARBEIDSFORHOLD_1 FOREIGN KEY (informasjon_id) REFERENCES IAY_INFORMASJON (id),
  CONSTRAINT FK_IAY_ARBEIDSFORHOLD_2 FOREIGN KEY (arbeidsgiver_virksomhet_id) REFERENCES VIRKSOMHET (id),
  CONSTRAINT FK_IAY_ARBEIDSFORHOLD_3 FOREIGN KEY (handling_type, kl_handling_type) references KODELISTE (kode, kodeverk)
);

COMMENT ON TABLE IAY_ARBEIDSFORHOLD
IS 'Overstyrende betraktninger om arbeidsforhold';
COMMENT ON COLUMN IAY_ARBEIDSFORHOLD.arbeidsgiver_aktor_id
IS 'Personlig foretak som arbeidsgiver';
COMMENT ON COLUMN IAY_ARBEIDSFORHOLD.arbeidsforhold_id
IS 'Intern nøkkel som representerer arbeidsforhodl-id fra AA-reg';
COMMENT ON COLUMN IAY_ARBEIDSFORHOLD.ny_arbeidsforhold_id
IS 'Den nye intern nøkkel som representerer arbeidsforhodl-id fra AA-reg etter merge av nøkler';
COMMENT ON COLUMN IAY_ARBEIDSFORHOLD.begrunnelse
IS 'Saksbehandlers begrunnelsen for tiltaket';


CREATE INDEX IDX_IAY_ARBEIDSFORHOLD_1
  ON IAY_ARBEIDSFORHOLD (informasjon_id);
CREATE INDEX IDX_IAY_ARBEIDSFORHOLD_2
  ON IAY_ARBEIDSFORHOLD (arbeidsgiver_virksomhet_id);
CREATE INDEX IDX_IAY_ARBEIDSFORHOLD_3
  ON IAY_ARBEIDSFORHOLD (handling_type);

CREATE SEQUENCE SEQ_IAY_ARBEIDSFORHOLD
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NOCACHE
  NOCYCLE;

ALTER TABLE IAY_ARBEIDSFORHOLD_REFER
  ADD CONSTRAINT FK_IAY_ARBEIDSFORHOLD_REFER_2 FOREIGN KEY (arbeidsgiver_virksomhet_id) REFERENCES VIRKSOMHET;

CREATE SEQUENCE SEQ_IAY_ARBEIDSFORHOLD_REFER
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NOCACHE
  NOCYCLE;

CREATE INDEX IDX_IAY_ARBEIDSFORHOLD_REFER_1
  ON IAY_ARBEIDSFORHOLD_REFER (intern_referanse);
CREATE INDEX IDX_IAY_ARBEIDSFORHOLD_REFER_2
  ON IAY_ARBEIDSFORHOLD_REFER (arbeidsgiver_virksomhet_id);
CREATE INDEX IDX_IAY_ARBEIDSFORHOLD_REFER_3
  ON IAY_ARBEIDSFORHOLD_REFER (informasjon_id);
CREATE INDEX IDX_IAY_ARBEIDSFORHOLD_REFER_4
  ON IAY_ARBEIDSFORHOLD_REFER (ekstern_referanse);

COMMENT ON TABLE IAY_ARBEIDSFORHOLD_REFER
IS 'Kobling mellom arbeidsforhold fra aa-reg og intern nøkkel for samme representasjon';
COMMENT ON COLUMN IAY_ARBEIDSFORHOLD_REFER.intern_referanse
IS 'Syntetisk nøkkel for å representere et arbeidsforhold';
COMMENT ON COLUMN IAY_ARBEIDSFORHOLD_REFER.ekstern_referanse
IS 'ArbeidsforholdId hentet fra AA-reg';
COMMENT ON COLUMN IAY_ARBEIDSFORHOLD_REFER.arbeidsgiver_aktor_id
IS 'Aktør til personlig foretak.';

-- Migrer TMP_ARBEIDSFORHOLD_REF til IAY_ARBEIDSFORHOLD_REF
INSERT INTO IAY_INFORMASJON (id, gr_id)
  SELECT
    SEQ_IAY_INFORMASJON.NEXTVAL AS id,
    gr_id
  FROM
    (
      SELECT DISTINCT gr.id as gr_id
      FROM GR_ARBEID_INNTEKT gr
    );
MERGE INTO GR_ARBEID_INNTEKT gr
USING (SELECT
         s.id AS informasjon_id,
         s.gr_id
       FROM IAY_INFORMASJON s) si
ON (si.gr_id = gr.id)
WHEN MATCHED THEN UPDATE SET gr.informasjon_id = si.informasjon_id;

ALTER TABLE IAY_INFORMASJON
  DROP COLUMN gr_id;

INSERT INTO IAY_ARBEIDSFORHOLD_REFER (id, informasjon_id, intern_referanse, ekstern_referanse, arbeidsgiver_aktor_id, arbeidsgiver_virksomhet_id)
  SELECT
    SEQ_IAY_ARBEIDSFORHOLD_REFER.NEXTVAL AS id,
    informasjon_id,
    intern_referanse,
    ekstern_referanse,
    null                                 as arbeidsgiver_aktor_id,
    arbeidsgiver_virksomhet_id
  FROM
    (
      SELECT DISTINCT
        gr.informasjon_id                                                        AS informasjon_id,
        im.ARBEIDSFORHOLD_ID                                                     AS intern_referanse,
        (SELECT ref.ekstern_referanse
         FROM TMP_ARBEIDSFORHOLD_REF ref
         WHERE ref.intern_referanse = im.ARBEIDSFORHOLD_ID AND ref.arbeidsgiver_virksomhet_id =
                                                               im.VIRKSOMHET_ID) AS ekstern_referanse,
        im.VIRKSOMHET_ID                                                         AS arbeidsgiver_virksomhet_id
      FROM IAY_INNTEKTSMELDING im
        INNER JOIN IAY_INNTEKTSMELDINGER ime ON im.INNTEKTSMELDINGER_ID = ime.id
        INNER JOIN GR_ARBEID_INNTEKT gr ON gr.INNTEKTSMELDINGER_ID = ime.id
      WHERE im.ARBEIDSFORHOLD_ID IS NOT NULL
    ) k where k.ekstern_referanse is not null;

INSERT INTO IAY_ARBEIDSFORHOLD_REFER (id, informasjon_id, intern_referanse, ekstern_referanse, arbeidsgiver_aktor_id, arbeidsgiver_virksomhet_id)
  SELECT
    SEQ_IAY_ARBEIDSFORHOLD_REFER.NEXTVAL AS id,
    informasjon_id,
    intern_referanse,
    ekstern_referanse,
    arbeidsgiver_aktor_id,
    arbeidsgiver_virksomhet_id
  FROM
    (
      SELECT DISTINCT
        gr.informasjon_id                                                                  AS informasjon_id,
        yr.ARBEIDSFORHOLD_ID                                                               AS intern_referanse,
        (SELECT ref.ekstern_referanse
         FROM TMP_ARBEIDSFORHOLD_REF ref
         WHERE ref.intern_referanse = yr.ARBEIDSFORHOLD_ID AND (ref.arbeidsgiver_virksomhet_id =
                                                                yr.ARBEIDSGIVER_VIRKSOMHET_ID OR
                                                                ref.arbeidsgiver_aktor_id =
                                                                yr.ARBEIDSGIVER_AKTOR_ID)) AS ekstern_referanse,
        yr.arbeidsgiver_aktor_id                                                           AS arbeidsgiver_aktor_id,
        yr.ARBEIDSGIVER_VIRKSOMHET_ID                                                      AS arbeidsgiver_virksomhet_id
      FROM IAY_YRKESAKTIVITET yr
        INNER JOIN IAY_AKTOER_ARBEID aa ON yr.AKTOER_ARBEID_ID = aa.id
        INNER JOIN IAY_INNTEKT_ARBEID_YTELSER ime ON aa.INNTEKT_ARBEID_YTELSER_ID = ime.id
        INNER JOIN GR_ARBEID_INNTEKT gr ON gr.INNTEKTSMELDINGER_ID = ime.id
      WHERE yr.ARBEIDSFORHOLD_ID IS NOT NULL AND NOT EXISTS(SELECT (1)
                                                            FROM IAY_ARBEIDSFORHOLD_REFER ref
                                                            WHERE ref.informasjon_id = gr.informasjon_id AND
                                                                  ref.intern_referanse = yr.ARBEIDSFORHOLD_ID AND
                                                                  (ref.arbeidsgiver_virksomhet_id =
                                                                   yr.ARBEIDSGIVER_VIRKSOMHET_ID OR
                                                                   ref.arbeidsgiver_aktor_id =
                                                                   yr.ARBEIDSGIVER_AKTOR_ID))
            AND EXISTS(SELECT (1)
                       FROM IAY_ARBEIDSFORHOLD_REFER ref
                       WHERE
                         ref.informasjon_id = gr.informasjon_id
                         AND
                         (ref.arbeidsgiver_virksomhet_id =
                          yr.ARBEIDSGIVER_VIRKSOMHET_ID OR
                          ref.arbeidsgiver_aktor_id =
                          yr.ARBEIDSGIVER_AKTOR_ID))
    );

DROP TABLE TMP_ARBEIDSFORHOLD_REF;
DROP SEQUENCE SEQ_ARBEIDSFORHOLD_REF;

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FAKTA_OM_ARBEIDSFORHOLD', 'Fakta om arbeidsforhold', 'Fakta om arbeidsforhold', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');

UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'FAKTA_OM_ARBEIDSFORHOLD' WHERE KODE = '5080';

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'ARBEIDSFORHOLD', 'Arbeidsforhold', 'Endring i Arbeidsforhold', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
