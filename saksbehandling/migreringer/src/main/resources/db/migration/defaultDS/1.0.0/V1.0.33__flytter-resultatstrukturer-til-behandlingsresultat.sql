ALTER TABLE gr_medlemskap_vilkar_periode
  RENAME TO res_medlemskap_perioder;

ALTER TABLE res_medlemskap_perioder
  DROP COLUMN vilkar_resultat_id CASCADE;
ALTER TABLE res_medlemskap_perioder
  ADD COLUMN BEHANDLING_RESULTAT_ID bigint;
ALTER TABLE res_medlemskap_perioder
  ADD CONSTRAINT fk_res_medlemskap_perioder_1 FOREIGN KEY (BEHANDLING_RESULTAT_ID)
    REFERENCES behandling_resultat (ID);

ALTER TABLE res_opptjening
  DROP COLUMN vilkar_resultat_id CASCADE;
ALTER TABLE res_opptjening
  ADD COLUMN BEHANDLING_RESULTAT_ID bigint;
ALTER TABLE res_opptjening
  ADD CONSTRAINT fk_res_opptjening_1 FOREIGN KEY (BEHANDLING_RESULTAT_ID)
    REFERENCES behandling_resultat (ID);

