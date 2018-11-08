-- Trenger noen initielle kodeliste verdier for enhetstester av sammensatt kodeverk funksjoner
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, gyldig_fom, gyldig_tom)
VALUES (seq_kodeliste.nextval, 'GEOPOLITISK', 'EU', 'EU', 'EU', to_date('2000-01-01', 'YYYY-MM-DD'), to_date('9999-12-31', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, gyldig_fom, gyldig_tom)
VALUES (seq_kodeliste.nextval, 'GEOPOLITISK', 'EØS', 'EØS', 'EØS', to_date('2000-01-01', 'YYYY-MM-DD'), to_date('9999-12-31', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, gyldig_fom, gyldig_tom)
VALUES (seq_kodeliste.nextval, 'GEOPOLITISK', 'NORDEN', 'NORDEN', 'NORDEN', to_date('2000-01-01', 'YYYY-MM-DD'), to_date('9999-12-31', 'YYYY-MM-DD'));

-- Trenger noen initielle kodeliste_relasjon verdier for enhetstester av sammensatt kodeverk funksjoner
INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2) VALUES (seq_kodeliste_relasjon.nextval, 'GEOPOLITISK','EU', 'LANDKODER', 'SWE');
INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2) VALUES (seq_kodeliste_relasjon.nextval, 'GEOPOLITISK','EØS', 'LANDKODER', 'NOR');
INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2) VALUES (seq_kodeliste_relasjon.nextval, 'GEOPOLITISK','NORDEN', 'LANDKODER', 'NOR');
