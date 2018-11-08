INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'ordinaertArbeidsforhold', 'ordinaertArbeidsforhold', 'Ordinært arbeidsforhold', NULL, to_date('2014-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'forenkletOppgjoersordning', 'forenkletOppgjoersordning', 'Forenklet oppgjørsordning ', NULL, to_date('2014-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'frilanserOppdragstakerHonorarPersonerMm', 'frilanserOppdragstakerHonorarPersonerMm', 'Frilansere/oppdragstakere, med mer', NULL, to_date('2014-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'maritimtArbeidsforhold', 'maritimtArbeidsforhold', 'Maritimt arbeidsforhold', NULL, to_date('2014-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'pensjonOgAndreTyperYtelserUtenAnsettelsesforhold', 'pensjonOgAndreTyperYtelserUtenAnsettelsesforhold', 'Pensjoner og andre typer ytelser', NULL, to_date('2014-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'SELVNAER', null, 'Selvstendig næringsdrivende', NULL, to_date('2014-01-01', 'YYYY-MM-DD'));
