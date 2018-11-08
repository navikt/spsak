-- Tabell: MEDLEMSKAP
CREATE TABLE MEDLEMSKAP_VURDERING_LOPENDE (
  id                       NUMBER(19)                        NOT NULL,
  vurdert_periode_id       NUMBER(19)                        NOT NULL,
  oppholdsrett_vurdering   VARCHAR2(1 CHAR),
  lovlig_opphold_vurdering VARCHAR2(1 CHAR),
  bosatt_vurdering         VARCHAR2(1 CHAR),
  er_eos_borger            VARCHAR2(1 CHAR),
  vurderingsdato           DATE                              NOT NULL,
  manuell_vurd             VARCHAR2(100 CHAR),
  kl_manuell_vurd          VARCHAR2(100 CHAR) AS ('MEDLEMSKAP_MANUELL_VURD'),
  opprettet_av             VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid            TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                VARCHAR2(20 CHAR),
  endret_tid               TIMESTAMP(3),
  CONSTRAINT PK_MEDLEMSKAP_VL PRIMARY KEY (id),
  CONSTRAINT FK_VURDERT_MEDLEMSKAP_P_1 FOREIGN KEY (vurdert_periode_id) REFERENCES MEDLEMSKAP_VURDERING_PERIODE (id),
  CONSTRAINT FK_VURDERT_MEDLEMSKAP_P_2 FOREIGN KEY (kl_manuell_vurd, manuell_vurd) REFERENCES KODELISTE (kodeverk, kode)
);

COMMENT ON TABLE MEDLEMSKAP_VURDERING_LOPENDE  IS 'En koblingstabell som holder vurderte periode for løpende medlemskap';
COMMENT ON COLUMN MEDLEMSKAP_VURDERING_LOPENDE.id IS 'Timestamp som forteller nå transaksjonen inntraff. ';
COMMENT ON COLUMN MEDLEMSKAP_VURDERING_LOPENDE.vurdert_periode_id IS 'Peker til koblingstabell';
COMMENT ON COLUMN MEDLEMSKAP_VURDERING_LOPENDE.oppholdsrett_vurdering IS 'Om søker har oppholdsrett for vurderingsdato';
COMMENT ON COLUMN MEDLEMSKAP_VURDERING_LOPENDE.lovlig_opphold_vurdering IS 'Om søker har oppholdsrett for vurderingsdato';
COMMENT ON COLUMN MEDLEMSKAP_VURDERING_LOPENDE.bosatt_vurdering IS 'Om søker er bosatt for vurderingsdato';
COMMENT ON COLUMN MEDLEMSKAP_VURDERING_LOPENDE.er_eos_borger IS 'Om søker er EØS borger for vurderingsdato';
COMMENT ON COLUMN MEDLEMSKAP_VURDERING_LOPENDE.vurderingsdato IS 'Vurderingsdato';
COMMENT ON COLUMN MEDLEMSKAP_VURDERING_LOPENDE.manuell_vurd IS 'Manuell vurderingstype';
COMMENT ON COLUMN MEDLEMSKAP_VURDERING_LOPENDE.kl_manuell_vurd IS 'Peker til kodeliste';

CREATE SEQUENCE SEQ_MEDLEMSKAP_VL
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NOCACHE
  NOCYCLE;

CREATE UNIQUE INDEX UIDX_MEDLEMSKAP_VL_1 ON MEDLEMSKAP_VURDERING_LOPENDE(vurderingsdato, vurdert_periode_id);
CREATE INDEX IDX_MEDLEMSKAP_VL_1 ON MEDLEMSKAP_VURDERING_LOPENDE(vurdert_periode_id);
CREATE INDEX IDX_MEDLEMSKAP_VL_2 ON MEDLEMSKAP_VURDERING_LOPENDE(manuell_vurd);
