-- Legger til fagsystem:
insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'FAGSYSTEM', 'ENHETSREGISTERET', 'ER01', 'Enhetsregisteret', 'Enhetsregisteret', to_date('2017-12-08', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'FAGSYSTEM', 'AAREGISTERET', 'AR01', 'AAregisteret', 'AAregisteret', to_date('2017-12-12', 'YYYY-MM-DD'));

-- Legger til saksopplysningtype
INSERT INTO SAKSOPPLYSNING_TYPE (kode, navn) VALUES ('ORG', 'Organisasjon');
INSERT INTO SAKSOPPLYSNING_TYPE (kode, navn) VALUES ('ARB', 'Arbeidsforhold');

-- Legger til konfigverdi for

INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
VALUES ('opplysningsperiode.lengde', 'Lengden på opplysningsperioden for opptjening.', 'INGEN', 'PERIOD', 'Lengden på opplysningsperioden for opptjening.');

INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
VALUES (SEQ_KONFIG_VERDI.nextval, 'opplysningsperiode.lengde', 'INGEN', 'P17M', to_date('01.01.2017', 'dd.mm.yyyy'));
