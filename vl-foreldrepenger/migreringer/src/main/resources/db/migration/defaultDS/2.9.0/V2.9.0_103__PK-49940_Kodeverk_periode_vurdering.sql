INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES('UTTAK_PERIODE_VURDERING_TYPE', 'Kodeverk over mulige vurderinger av uttaksperioder', 'Kodeverk over mulige vurderinger av uttaksperioder som saksbehandler kan gjøre i forbindelse med avklaring av fakta.', 'VL');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PERIODE_OK', 'Periode er OK', 'Saksbehandler vurderer at uttaksperioden er OK.', 'UTTAK_PERIODE_VURDERING_TYPE',
        to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PERIODE_OK_ENDRET', 'Periode er OK med endringer', 'Saksbehandler vurderer at uttaksperioden er OK etter å ha endret perioden.', 'UTTAK_PERIODE_VURDERING_TYPE',
        to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PERIODE_IKKE_VURDERT', 'Perioden er ikke vurdert', 'Perioden er ikke manuelt vurdert av saksbehandler.', 'UTTAK_PERIODE_VURDERING_TYPE',
        to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PERIODE_KAN_IKKE_AVKLARES', 'Perioden kan ikke avklares', 'Saksbehandler vurderer at uttaksperioden ikke kan avklares.', 'UTTAK_PERIODE_VURDERING_TYPE',
        to_date('2000-01-01', 'YYYY-MM-DD'));

ALTER TABLE YF_FORDELING_PERIODE ADD VURDERING_TYPE VARCHAR(100 char) DEFAULT 'PERIODE_IKKE_VURDERT';
ALTER TABLE YF_FORDELING_PERIODE ADD KL_VURDERING_TYPE VARCHAR2(100 CHAR) GENERATED ALWAYS AS ('UTTAK_PERIODE_VURDERING_TYPE') VIRTUAL;

ALTER TABLE YF_FORDELING_PERIODE ADD CONSTRAINT FK_YF_FORDELING_PERIODE_2 FOREIGN KEY (KL_VURDERING_TYPE, VURDERING_TYPE) REFERENCES KODELISTE (KODEVERK, KODE);
CREATE INDEX IDX_YF_FORDELING_PERIODE_6 ON YF_FORDELING_PERIODE (VURDERING_TYPE);

COMMENT ON COLUMN YF_FORDELING_PERIODE.VURDERING_TYPE IS 'Saksbehandlers vurdering av perioden ifbm avklaring av fakta.';
COMMENT ON COLUMN YF_FORDELING_PERIODE.KL_VURDERING_TYPE IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
