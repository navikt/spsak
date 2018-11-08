ALTER TABLE UTTAK_RESULTAT_PERIODE_AKT RENAME COLUMN OVERSTYRT_UTBETALINGSPROSENT TO UTBETALINGSPROSENT;
ALTER TABLE UTTAK_RESULTAT_PERIODE_AKT ADD GRADERING CHAR DEFAULT 'J' NOT NULL CHECK (GRADERING IN ('J', 'N'));

UPDATE UTTAK_RESULTAT_PERIODE_AKT SET GRADERING = 'N' where arbeidstidsprosent = 0 or arbeidstidsprosent = 100;

COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE_AKT.GRADERING IS 'Om aktiviteten er gradert';
