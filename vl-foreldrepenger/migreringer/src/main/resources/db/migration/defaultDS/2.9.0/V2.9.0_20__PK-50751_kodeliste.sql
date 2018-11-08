-- kodeliste IKKE_OPPFYLT_AARSAK
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4023', 'Arbeider mer enn 0 prosent', 'Arbeider mer enn 0 prosent', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '4025', 'Arbeider 100 prosent eller mer', 'Arbeider 100 prosent eller mer', 'IKKE_OPPFYLT_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
