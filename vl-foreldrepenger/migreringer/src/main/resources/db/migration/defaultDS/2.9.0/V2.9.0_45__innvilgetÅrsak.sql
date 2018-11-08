-- kodeverk INNVILGET_AARSAK
INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('INNVILGET_AARSAK', 'N', 'N', 'Årsak til oppfylt stønadsperiode', 'Årsak til oppfylt stønadsperiode');

-- kodeliste INNVILGET_AARSAK
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '2001', '§14-6: Uttak er oppfylt',
        '§14-6: Uttak er oppfylt', 'INNVILGET_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-6"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '2022', '§14-12: Overføring oppfylt, annen part er innlagt i helseinstitusjon',
        '§14-12: Overføring oppfylt, annen part er innlagt i helseinstitusjon', 'INNVILGET_AARSAK',
        to_date('2000-01-01', 'YYYY-MM-DD'), '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-12"}}}');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom, EKSTRA_DATA)
VALUES (seq_kodeliste.nextval, '2021', '§14-12: Overføring oppfylt, annen part er helt avhengig av hjelp til å ta seg av barnet',
        '§14-12: Overføring oppfylt, annen part er helt avhengig av hjelp til å ta seg av barnet', 'INNVILGET_AARSAK',
        to_date('2000-01-01', 'YYYY-MM-DD'), '{"fagsakYtelseType": {"FP": {"lovreferanse": "14-12"}}}');
