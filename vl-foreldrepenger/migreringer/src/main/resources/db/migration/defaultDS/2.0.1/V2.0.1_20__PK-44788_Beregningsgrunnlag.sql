ALTER TABLE BEREGNINGSGRUNNLAG
ADD BEHANDLING_ID NUMBER(19,0) NOT NULL;

ALTER TABLE BEREGNINGSGRUNNLAG
        ADD CONSTRAINT CHK_UNIQUE_BEHANDLING_ID UNIQUE (BEHANDLING_ID);
