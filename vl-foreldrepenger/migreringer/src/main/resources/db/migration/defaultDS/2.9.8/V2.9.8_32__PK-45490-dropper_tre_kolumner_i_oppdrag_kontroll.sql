--DISSE TRE COLUMNS BLE FLYTTET TIL OPPDRAG_KVITTERING OG SKAL DROPPES FRA OPPDRAG_KONTROLL
ALTER TABLE OPPDRAG_KONTROLL DROP COLUMN ALVORLIGHETSGRAD;
ALTER TABLE OPPDRAG_KONTROLL DROP COLUMN BESKR_MELDING;
ALTER TABLE OPPDRAG_KONTROLL DROP COLUMN MELDING_KODE;
