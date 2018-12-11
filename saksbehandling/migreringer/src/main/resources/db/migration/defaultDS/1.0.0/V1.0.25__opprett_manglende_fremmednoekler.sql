ALTER TABLE beregningsresultat_andel ADD CONSTRAINT FK_beregningsresultat_andel_90 FOREIGN KEY (aktivitet_status, kl_aktivitet_status) REFERENCES KODELISTE (KODE, KODEVERK);

ALTER TABLE iay_oppgitt_arbeidsforhold ADD CONSTRAINT FK_iay_oppgitt_arbeidsforhold_90 FOREIGN KEY (land, kl_landkoder) REFERENCES KODELISTE (KODE, KODEVERK);

ALTER TABLE medlemskap_vilkar_perioder ADD CONSTRAINT FK_medlemskap_vilkar_perioder_90 FOREIGN KEY (VILKAR_UTFALL, KL_VILKAR_UTFALL_TYPE) REFERENCES KODELISTE (KODE, KODEVERK);

ALTER TABLE verge ADD CONSTRAINT FK_verge_90 FOREIGN KEY (brev_mottaker, kl_brev_mottaker) REFERENCES KODELISTE (KODE, KODEVERK);
