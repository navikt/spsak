ALTER TABLE RES_BEREGNINGSRESULTAT_FP
  RENAME TO RES_BEREGNINGSRESULTAT;

DROP TABLE fh_familie_hendelse CASCADE;
DROP TABLE beregning CASCADE;
DROP TABLE beregning_resultat CASCADE;
DROP TABLE personopplysning CASCADE;
DROP TABLE opplysning_adresse CASCADE;
DROP TABLE familierelasjon CASCADE;
DROP TABLE fagsak_relasjon CASCADE;

ALTER TABLE beregningsresultat_fp
  RENAME TO BR_BEREGNINGSRESULTAT;
ALTER TABLE beregningsresultat_periode
  RENAME TO BR_beregningsresultat_periode;
ALTER TABLE beregningsresultat_andel
  RENAME TO BR_beregningsresultat_andel;

ALTER TABLE beregningsgrunnlag
  RENAME TO bg_beregningsgrunnlag;
ALTER TABLE beregningsgrunnlag_periode
  RENAME TO bg_beregningsgrunnlag_periode;
ALTER TABLE sammenligningsgrunnlag
  RENAME TO bg_sammenligningsgrunnlag;

ALTER TABLE verge
  RENAME TO ve_verge;

ALTER TABLE opptjening
  RENAME TO res_opptjening;
ALTER TABLE opptjening_aktivitet
  RENAME TO op_opptjening_aktivitet;

ALTER TABLE RES_BEREGNINGSRESULTAT
  DROP COLUMN behandling_id CASCADE;
ALTER TABLE RES_BEREGNINGSRESULTAT
  ADD COLUMN BEHANDLING_RESULTAT_ID bigint;
ALTER TABLE RES_BEREGNINGSRESULTAT
  ADD CONSTRAINT fk_res_beregningsresultat_fp_1 FOREIGN KEY (BEHANDLING_RESULTAT_ID)
    REFERENCES behandling_resultat (ID);
ALTER TABLE RES_BEREGNINGSRESULTAT
  ALTER COLUMN BEHANDLING_RESULTAT_ID SET NOT NULL;
CREATE INDEX IDX_RES_BEREGNINGSRESULTAT_F_6 ON RES_BEREGNINGSRESULTAT (BEHANDLING_RESULTAT_ID);
ALTER TABLE RES_BEREGNINGSRESULTAT
  RENAME COLUMN beregningsresultat_fp_id TO beregningsresultat_id;

