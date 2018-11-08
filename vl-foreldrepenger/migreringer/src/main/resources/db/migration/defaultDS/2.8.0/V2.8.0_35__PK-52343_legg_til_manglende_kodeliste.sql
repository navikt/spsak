-- kodeliste IKKE_OPPFYLT_AARSAK
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4006', 'Påkrevd stønadsperiode mangler', 'Påkrevd stønadsperiode mangler', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4015', 'Mor søker foreldrepenger før fødsel for tidlig', 'Mor søker foreldrepenger før fødsel for tidlig', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4017', 'Far/medmor søker foreldrepenger før termin/fødsel', 'Far/medmor søker foreldrepenger før termin/fødsel', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

-- kodeliste GRADERING_AVSLAG_AARSAK
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4523', 'Avslag gradering - arbeid 100% eller mer', 'Avslag gradering - arbeid 100% eller mer', 'GRADERING_AVSLAG_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4504', 'Avslag gradering - gradering før uke 7', 'Avslag gradering - gradering før uke 7', 'GRADERING_AVSLAG_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
