insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
values ('registerinnhenting.grenseverdi.avstand', 'Grenseverdi for registerinnhenting', 'INGEN', 'PERIOD', 'Øvre grense for når vi endre intervall for registerinnheting');

insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
values (SEQ_KONFIG_VERDI.nextval, 'registerinnhenting.grenseverdi.avstand', 'INGEN', 'P4M', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
values ('opplysningsperiode.lengde.etter', 'Perioden fremover i tid', 'INGEN', 'PERIOD', 'Lengden på perioden med registerdata som innhentes etter skjæringstidspunkt');

insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
values (SEQ_KONFIG_VERDI.nextval, 'opplysningsperiode.lengde.etter', 'INGEN', 'P4Y', to_date('01.01.2016', 'dd.mm.yyyy'));
