alter table BEREGNINGSGRUNNLAG add gjeldende_bg_kofakber_id NUMBER(19);

alter table BEREGNINGSGRUNNLAG add CONSTRAINT FK_BEREGNINGSGRUNNLAG FOREIGN KEY ( gjeldende_bg_kofakber_id ) REFERENCES BEREGNINGSGRUNNLAG ( id );
CREATE INDEX IDX_BEREGNINGSGRUNNLAG_1 ON BEREGNINGSGRUNNLAG (gjeldende_bg_kofakber_id);

COMMENT ON COLUMN BEREGNINGSGRUNNLAG.gjeldende_bg_kofakber_id IS 'FK til gjeldende Beregningsgrunnlag ved inngang til steg kontroller fakta om beregning';
