ALTER TABLE IAY_INNTEKTSPOST DISABLE CONSTRAINT FK_INNTEKTSPOST_3;
UPDATE IAY_INNTEKTSPOST set INNTEKTSPOST_TYPE = 'LØNN' where INNTEKTSPOST_TYPE = 'LONN';
UPDATE KODELISTE set KODE = 'LØNN' where KODEVERK = 'INNTEKTSPOST_TYPE' and KODE = 'LONN';
ALTER TABLE IAY_INNTEKTSPOST ENABLE CONSTRAINT FK_INNTEKTSPOST_3;

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom, EKSTRA_DATA, OFFISIELL_KODE)
VALUES (seq_kodeliste.nextval, 'INNTEKTSPOST_TYPE', 'SELVSTENDIG_NÆRINGSDRIVENDE', 'Selvstendig næringsdrivende', to_date('2000-01-01', 'YYYY-MM-DD'), '{typer: [svalbardpersoninntektNaering,  personinntektNaering]', '-');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom, EKSTRA_DATA, OFFISIELL_KODE)
VALUES (seq_kodeliste.nextval, 'INNTEKTSPOST_TYPE', 'NÆRING_FISKE_FANGST_FAMBARNEHAGE', 'Jordbruk/Skogbruk/Fiske/FamilieBarnehage', to_date('2000-01-01', 'YYYY-MM-DD'), '{typer: [personinntektFiskeFangstFamilebarnehage]', 'personinntektFiskeFangstFamilebarnehage');
