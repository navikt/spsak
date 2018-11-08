INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'NYTT_ARBEIDSFORHOLD', 'Arbeidsforholdet er ansett som nytt', 'Arbeidsforholdet er ansett som nytt', to_date('2000-01-01', 'YYYY-MM-DD'),
        'ARBEIDSFORHOLD_HANDLING_TYPE');
