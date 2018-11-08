-- Trenger noen initielle kodeliste verdier for enhetstester av sammensatt kodeverk funksjoner
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'LANDGRUPPER', 'INNLAND', 'INNLAND', 'Innland', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'LANDGRUPPER', 'UTLAND', 'UTLAND', 'Utland', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'FYLKER', '07', '07', 'Vestfold', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'KOMMUNER', '0722', '0722', 'Nøtterøy', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'KOMMUNER', '0723', '0723', 'Tjøme', to_date('2000-01-01', 'YYYY-MM-DD'));

-- Trenger noen initielle kodeliste_relasjon verdier for enhetstester av sammensatt kodeverk funksjoner
INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2) VALUES (seq_kodeliste_relasjon.nextval, 'LANDGRUPPER','INNLAND', 'LANDKODER', 'NOR');
INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2) VALUES (seq_kodeliste_relasjon.nextval, 'LANDGRUPPER','UTLAND', 'LANDKODER', 'SWE');
INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2) VALUES (seq_kodeliste_relasjon.nextval, 'LANDKODER','NOR', 'FYLKER', '07');
INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2) VALUES (seq_kodeliste_relasjon.nextval, 'FYLKER','07', 'KOMMUNER', '0722');
INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2) VALUES (seq_kodeliste_relasjon.nextval, 'FYLKER','07', 'KOMMUNER', '0723');
