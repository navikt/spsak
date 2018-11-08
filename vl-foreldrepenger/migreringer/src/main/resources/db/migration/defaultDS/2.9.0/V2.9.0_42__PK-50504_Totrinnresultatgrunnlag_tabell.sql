CREATE TABLE TOTRINNRESULTATGRUNNLAG
(
  id                         NUMBER(19)                        NOT NULL,
  behandling_id              NUMBER(19)                        NOT NULL,
  inntekt_arbeid_grunnlag_id     NUMBER(19),
  ytelses_fordeling_grunnlag_id     NUMBER(19),
  uttak_resultat_id           NUMBER(19),
  beregningsgrunnlag_id      NUMBER(19),
  aktiv                      VARCHAR2(1 CHAR) NOT NULL,
  versjon                    NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av               VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid              TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                  VARCHAR2(20 CHAR),
  endret_tid                 TIMESTAMP(3),
  CONSTRAINT PK_TOTRINNRESULTATGRUNNLAG PRIMARY KEY (id),
	CONSTRAINT FK_TOTRINNRESULTATGRUNNLAG_1 FOREIGN KEY ( behandling_id ) REFERENCES behandling ( id ),
	CONSTRAINT FK_TOTRINNRESULTATGRUNNLAG_2 FOREIGN KEY ( inntekt_arbeid_grunnlag_id ) REFERENCES gr_arbeid_inntekt ( id ),
  CONSTRAINT FK_TOTRINNRESULTATGRUNNLAG_3 FOREIGN KEY ( ytelses_fordeling_grunnlag_id ) REFERENCES GR_YTELSES_FORDELING ( id ),
  CONSTRAINT FK_TOTRINNRESULTATGRUNNLAG_4 FOREIGN KEY ( uttak_resultat_id ) REFERENCES uttak_resultat ( id ),
  CONSTRAINT FK_TOTRINNRESULTATGRUNNLAG_5 FOREIGN KEY ( beregningsgrunnlag_id ) REFERENCES GR_BEREGNINGSGRUNNLAG ( id )
);

CREATE SEQUENCE SEQ_TOTRINNRESULTATGRUNNLAG MINVALUE 1 START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE INDEX IDX_TORINN_RES_GR_01 ON TOTRINNRESULTATGRUNNLAG (behandling_id);
CREATE INDEX IDX_TORINN_RES_GR_02 ON TOTRINNRESULTATGRUNNLAG (inntekt_arbeid_grunnlag_id);
CREATE INDEX IDX_TORINN_RES_GR_03 ON TOTRINNRESULTATGRUNNLAG (ytelses_fordeling_grunnlag_id);
CREATE INDEX IDX_TORINN_RES_GR_04 ON TOTRINNRESULTATGRUNNLAG (uttak_resultat_id);
CREATE INDEX IDX_TORINN_RES_GR_05 ON TOTRINNRESULTATGRUNNLAG (beregningsgrunnlag_id);

COMMENT ON TABLE TOTRINNRESULTATGRUNNLAG  IS 'Tabell som held grunnlagsId for data vist i panelet fra beslutter.';
COMMENT ON COLUMN TOTRINNRESULTATGRUNNLAG.ID IS 'PK';
COMMENT ON COLUMN TOTRINNRESULTATGRUNNLAG.behandling_id IS 'FK til behandling som hører til totrinn resultatet';
COMMENT ON COLUMN TOTRINNRESULTATGRUNNLAG.inntekt_arbeid_grunnlag_id IS 'FK til aktivt InntektArbeidGrunnlag ved totrinnsbehandlingen';
COMMENT ON COLUMN TOTRINNRESULTATGRUNNLAG.ytelses_fordeling_grunnlag_id IS 'FK til aktivt YtelsesFordelingGrunnlag ved totrinnsbehanddlingen';
COMMENT ON COLUMN TOTRINNRESULTATGRUNNLAG.uttak_resultat_id IS 'FK til aktivt UttakResultat ved totrinnsbehandlingen';
COMMENT ON COLUMN TOTRINNRESULTATGRUNNLAG.beregningsgrunnlag_id IS 'FK til aktivt Beregningsgrunnlag ved totrinnsbehandlingen';
