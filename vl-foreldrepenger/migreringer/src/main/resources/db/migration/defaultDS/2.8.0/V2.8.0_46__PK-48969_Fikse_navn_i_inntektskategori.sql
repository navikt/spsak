UPDATE KODELISTE
SET navn = 'Selvstendig næringsdrivende'
WHERE kode = 'SELVSTENDIG_NÆRINGSDRIVENDE' and kodeverk = 'INNTEKTSKATEGORI';

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTSKATEGORI', 'SJØMANN', 'Sjømann', to_date('2000-01-01', 'YYYY-MM-DD'));
