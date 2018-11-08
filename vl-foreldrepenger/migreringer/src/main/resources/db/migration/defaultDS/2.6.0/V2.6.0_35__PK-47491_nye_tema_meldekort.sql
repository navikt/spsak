INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'RELATERT_YTELSE_TEMA', 'AAP', 'AAP', 'Arbeidsavklaringspenger', '', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'RELATERT_YTELSE_TEMA', 'DAG', 'DAG', 'Dagpenger', 'Dagpenger av alle typer - ordinær, permittering, fiske mv', to_date('2000-01-01', 'YYYY-MM-DD'));
