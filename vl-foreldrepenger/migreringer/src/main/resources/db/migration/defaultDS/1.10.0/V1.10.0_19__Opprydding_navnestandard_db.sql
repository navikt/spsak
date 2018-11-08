ALTER table AKSJONSPUNKT_DEF drop (NAV_OFFISIELL_KODE);
ALTER table BEHANDLING_STEG_TYPE drop (NAV_OFFISIELL_KODE);
ALTER table DOKUMENT_MAL_TYPE drop (NAV_OFFISIELL_KODE);
ALTER table SAKSOPPLYSNING_DOKUMENT_TYPE drop (NAV_OFFISIELL_KODE);
ALTER table SAKSOPPLYSNING_KILDE drop (NAV_OFFISIELL_KODE);
ALTER table SAKSOPPLYSNING_TYPE drop (NAV_OFFISIELL_KODE);
ALTER table VILKAR_TYPE drop (NAV_OFFISIELL_KODE);
ALTER table VURDERINGSPUNKT_DEF drop (NAV_OFFISIELL_KODE);

-- FOEDSEL_DATO
ALTER table ADOPSJON_BARN RENAME COLUMN FOEDSELSDATO TO FOEDSEL_DATO;
ALTER table BARN RENAME COLUMN FOEDSELDATO TO FOEDSEL_DATO;
ALTER table FOEDSEL RENAME COLUMN FOEDSELSDATO TO FOEDSEL_DATO;
ALTER table SOEKNAD_ADOPSJON_BARN RENAME COLUMN FOEDSELSDATO TO FOEDSEL_DATO;
ALTER table SOEKNAD_BARN RENAME COLUMN FOEDSELSDATO TO FOEDSEL_DATO;
ALTER table BRUKER RENAME COLUMN FOEDSELDATO TO FOEDSEL_DATO;
ALTER table BRUKER RENAME COLUMN SPRAAK_KODE TO SPRAK_KODE;

--
ALTER TABLE LAND_REGION RENAME COLUMN LANDKODE TO LAND;

--
ALTER TABLE AKSJONSPUNKT RENAME COLUMN TOTRINNSBEHANDLING TO TOTRINN_BEHANDLING;
ALTER TABLE AKSJONSPUNKT RENAME COLUMN TOTRINNSBEHANDLING_GODKJENT TO TOTRINN_BEHANDLING_GODKJENT;
--
ALTER TABLE AKSJONSPUNKT_DEF RENAME COLUMN ALLTID_TOTRINNSBEHANDLING TO TOTRINN_BEHANDLING_DEFAULT;
--
ALTER table SOEKNAD_ANNEN_PART RENAME COLUMN AARSAK TO ARSAK;
--
ALTER table SOEKNAD_VEDLEGG RENAME COLUMN ER_PAAKREVD_I_SOEKNADSDIALOG TO VEDLEGG_PAKREVD;
--






