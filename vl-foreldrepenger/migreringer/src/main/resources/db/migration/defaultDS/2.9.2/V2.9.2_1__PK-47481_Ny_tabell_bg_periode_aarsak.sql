CREATE TABLE BG_PERIODE_AARSAK (
  id                 NUMBER(19, 0)                     NOT NULL,
  periode_aarsak VARCHAR2(100 char) NOT NULL,
  kl_periode_aarsak VARCHAR2(100 char) AS ('PERIODE_AARSAK'),
  bg_periode_id NUMBER(19, 0)                     NOT NULL,
  versjon            NUMBER(19, 0) DEFAULT 0           NOT NULL,
  opprettet_av       VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid      TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av          VARCHAR2(20 CHAR),
  endret_tid         TIMESTAMP(3),
  CONSTRAINT PK_BG_PERIODE_AARSAK PRIMARY KEY (id),
  CONSTRAINT FK_BG_PERIODE_AARSAK_1 FOREIGN KEY (bg_periode_id) REFERENCES BEREGNINGSGRUNNLAG_PERIODE(id)
);

CREATE INDEX IDX_BG_PERIODE_AARSAK_1 ON BG_PERIODE_AARSAK (bg_periode_id);

COMMENT ON TABLE BG_PERIODE_AARSAK IS 'Periodeårsaker for splitting av perioder i beregningsgrunnlag';
COMMENT ON COLUMN BG_PERIODE_AARSAK.ID IS 'Primary Key';
COMMENT ON COLUMN BG_PERIODE_AARSAK.periode_aarsak IS 'Årsak til splitting av periode';
COMMENT ON COLUMN BG_PERIODE_AARSAK.kl_periode_aarsak IS 'Kodeverkreferanse for periodeårsak';

CREATE SEQUENCE SEQ_BG_PERIODE_AARSAK MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

insert into BG_PERIODE_AARSAK (id, periode_aarsak, bg_periode_id)
select SEQ_BG_PERIODE_AARSAK.NEXTVAL, 'ARBEIDSFORHOLD_AVSLUTTET', id
from BEREGNINGSGRUNNLAG_PERIODE
where periode_aarsak = 'ARBEIDSFORHOLD_AVSLUTTET_OG_NATURALYTELSE_BORTFALT';

insert into BG_PERIODE_AARSAK (id, periode_aarsak, bg_periode_id)
select SEQ_BG_PERIODE_AARSAK.NEXTVAL, 'NATURALYTELSE_BORTFALT', id
from BEREGNINGSGRUNNLAG_PERIODE
where periode_aarsak = 'ARBEIDSFORHOLD_AVSLUTTET_OG_NATURALYTELSE_BORTFALT';


insert into BG_PERIODE_AARSAK (id, periode_aarsak, bg_periode_id)
select SEQ_BG_PERIODE_AARSAK.NEXTVAL, periode_aarsak, id
from BEREGNINGSGRUNNLAG_PERIODE
where periode_aarsak != 'ARBEIDSFORHOLD_AVSLUTTET_OG_NATURALYTELSE_BORTFALT';

ALTER TABLE BEREGNINGSGRUNNLAG_PERIODE
DROP COLUMN periode_aarsak;

ALTER TABLE BEREGNINGSGRUNNLAG_PERIODE
DROP COLUMN kl_periode_aarsak;

delete from KODELISTE
where kode = 'ARBEIDSFORHOLD_AVSLUTTET_OG_NATURALYTELSE_BORTFALT' and kodeverk = 'PERIODE_AARSAK';

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'NATURALYTELSE_TILKOMMER', 'Naturalytelse tilkommer', 'Naturalytelse tilkommer', 'PERIODE_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'ENDRING_I_REFUSJONSKRAV', 'Endring i refusjonskrav', 'Endring i refusjonskrav', 'PERIODE_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'REFUSJON_OPPHØRER', 'Refusjon opphører', 'Refusjon opphører', 'PERIODE_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'GRADERING', 'Gradering', 'Gradering', 'PERIODE_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

ALTER TABLE BG_PR_STATUS_OG_ANDEL
ADD naturalytelse_tilkommet_pr_aar  NUMBER(19,2);

COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.naturalytelse_tilkommet_pr_aar IS 'Sum av naturalytelser som tilkommer i denne eller tidligere periode';
