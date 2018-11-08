INSERT INTO BEHANDLING_STEG_TYPE (kode, navn, behandling_status_def, beskrivelse)
VALUES ('VURDERKOMPLETT', 'Vurder kompletthet', 'UTRED', 'Vurder kompletthet');

INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'VURDERKOMPLETT', 35, 'FP');
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'VURDERKOMPLETT', 35, 'ES');

INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN)
VALUES ('VURDERKOMPLETT.INN', 'VURDERKOMPLETT', 'INN', 'Vurder kompletthet - Inngang');
INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN)
VALUES ('VURDERKOMPLETT.UT', 'VURDERKOMPLETT', 'UT', 'Vurder kompletthet - Utgang');

UPDATE AKSJONSPUNKT_DEF
SET VURDERINGSPUNKT = 'VURDERKOMPLETT.UT', NAVN = 'Venter på komplett søknad', BESKRIVELSE = 'Venter på komplett søknad'
WHERE KODE = '7003';

UPDATE KODELISTE
SET navn = 'Inntektsmelding', BESKRIVELSE = 'Arbeidsgivers opplysninger om arbeidstakers lønn, gradering, natural ytelser, ferie, utsettelser og krav om refusjon.'
WHERE KODEVERK = 'DOKUMENT_TYPE_ID' AND KODE = 'INNTEKTSMELDING';

ALTER TABLE ADOPSJON ADD ankomst_norge_dato DATE;


CREATE TABLE INNTEKTSMELDING_MANGLENDE (
  id                   NUMBER(19)                        NOT NULL,
  inntektsmeldinger_id NUMBER(19)                        NOT NULL,
  versjon              NUMBER(19) DEFAULT 0              NOT NULL,
  virksomhet_id        NUMBER(19)                        NOT NULL,
  opprettet_av         VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid        TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av            VARCHAR2(20 CHAR),
  endret_tid           TIMESTAMP(3),
  CONSTRAINT PK_INNTEKTSMELDING_MANGLENDE PRIMARY KEY (id),
  CONSTRAINT FK_INNTEKTSMELDING_MANGLENDE_1 FOREIGN KEY (inntektsmeldinger_id) REFERENCES INNTEKTSMELDINGER,
  CONSTRAINT FK_INNTEKTSMELDING_MANGLENDE_2 FOREIGN KEY (virksomhet_id) REFERENCES VIRKSOMHET
);

CREATE SEQUENCE SEQ_INNTEKTSMELDING_MANGLENDE
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NOCACHE
  NOCYCLE;

COMMENT ON TABLE INNTEKTSMELDING_MANGLENDE IS 'Inneholder inntektsmeldinger som søker har rapport (til saksbehandler) at ikke vil komme. Vi trenger kun vite orgnr for arbeidsgiveren.';
COMMENT ON COLUMN INNTEKTSMELDING_MANGLENDE.virksomhet_id IS 'Arbeidsgiver som gjelder inntektsmelding som ikke vil komme inn';

INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, SKJERMLENKE_TYPE)
VALUES ('5044', 'Vurder om vilkår for sykdom er oppfylt', 'KOFAK.UT', 'Vurder om vilkår for sykdom er oppfylt', 'FP_VK_11', 'J', '-');
