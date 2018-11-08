INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');

ALTER TABLE AKSJONSPUNKT_DEF ADD SKJERMLENKE_TYPE      VARCHAR2(100);
ALTER TABLE AKSJONSPUNKT_DEF ADD KL_SKJERMLENKE_TYPE   VARCHAR2(100)  AS ('SKJERMLENKE_TYPE');
COMMENT ON COLUMN AKSJONSPUNKT_DEF.SKJERMLENKE_TYPE IS 'FK: SKJERMLENKE_TYPE';
COMMENT ON COLUMN AKSJONSPUNKT_DEF.KL_SKJERMLENKE_TYPE IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';

UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'FAKTA_OM_ADOPSJON'
WHERE KODE IN ('5006', '5005', '5004');
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'SOEKNADSFRIST'
WHERE KODE IN ('5007', '6006');
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'FAKTA_OM_MEDLEMSKAP'
WHERE VILKAR_TYPE = 'FP_VK_2';
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'OPPLYSNINGSPLIKT'
WHERE VILKAR_TYPE = 'FP_VK_34';
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'FAKTA_FOR_OPPTJENING'
WHERE VILKAR_TYPE = 'FP_VK_23';
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'FORELDREANSVAR'
WHERE VILKAR_TYPE = 'FP_VK_8';
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'FORELDREANSVAR'
WHERE VILKAR_TYPE = 'FP_VK_33';
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'BEREGNING_FORELDREPENGER'
WHERE VILKAR_TYPE = 'FP_VK_41';
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'PUNKT_FOR_OMSORG'
WHERE KODE in ('5011');
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'PUNKT_FOR_FORELDREANSVAR'
WHERE KODE in ('5013');

/* Overstyring */
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'PUNKT_FOR_ADOPSJON'
WHERE KODE = '6004';
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'SOEKNADSFRIST'
WHERE KODE = '6006';
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'PUNKT_FOR_FOEDSEL'
WHERE KODE = '6003';
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'PUNKT_FOR_MEDLEMSKAP'
WHERE KODE = '6005';
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'BEREGNING_FORELDREPENGER'
WHERE KODE = '6007';

UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'FAKTA_OM_FOEDSEL'
WHERE KODE in ('5001', '5027');
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'KLAGE_BEH_NFP'
WHERE KODE in ('5035');
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'KLAGE_BEH_NK'
WHERE KODE in ('5036');
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'VEDTAK'
WHERE KODE in ('5028', '5016', '5041');

/* Søknadsfrist foreldrepenger */
UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = 'SOEKNADSFRIST'
WHERE KODE = '5043';


UPDATE AKSJONSPUNKT_DEF SET SKJERMLENKE_TYPE = '-'
WHERE SKJERMLENKE_TYPE IS NULL;

ALTER TABLE AKSJONSPUNKT_DEF MODIFY (SKJERMLENKE_TYPE NOT NULL);
