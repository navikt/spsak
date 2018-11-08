insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
values ('registerinnhenting.fp.avvik.periode.før', 'Maks avvik før innhentperiode FP', 'INGEN', 'PERIOD', 'Maks avvik før STP for registerinnhenting før justering av perioden for FP');

insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
values (SEQ_KONFIG_VERDI.nextval, 'registerinnhenting.fp.avvik.periode.før', 'INGEN', 'P4M', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
values ('registerinnhenting.fp.avvik.periode.etter', 'Maks avvik etter innhentperiode FP', 'INGEN', 'PERIOD', 'Maks avvik etter STP for registerinnhenting før justering av perioden for FP');

insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
values (SEQ_KONFIG_VERDI.nextval, 'registerinnhenting.fp.avvik.periode.etter', 'INGEN', 'P1Y', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
values ('registerinnhenting.es.avvik.periode.før', 'Maks avvik før innhentperiode ES', 'INGEN', 'PERIOD', 'Maks avvik før STP for registerinnhenting før justering av perioden for FP');

insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
values (SEQ_KONFIG_VERDI.nextval, 'registerinnhenting.es.avvik.periode.før', 'INGEN', 'P9M', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
values ('registerinnhenting.es.avvik.periode.etter', 'Maks avvik etter innhentperiode ES', 'INGEN', 'PERIOD', 'Maks avvik før STP for registerinnhenting før justering av perioden for FP');

insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
values (SEQ_KONFIG_VERDI.nextval, 'registerinnhenting.es.avvik.periode.etter', 'INGEN', 'P6M', to_date('01.01.2016', 'dd.mm.yyyy'));

INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
VALUES ('registerinnhenting.fp.opplysningsperiode.før', 'Registerinnhetning periode før STP - FP', 'INGEN', 'PERIOD', 'Perioden med registerinnhenting før skjæringstidspunktet');

INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
VALUES (SEQ_KONFIG_VERDI.nextval, 'registerinnhenting.fp.opplysningsperiode.før', 'INGEN', 'P17M', to_date('01.01.2017', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
values ('registerinnhenting.fp.opplysningsperiode.etter', 'Registerinnhetning periode etter STP - FP', 'INGEN', 'PERIOD', 'Perioden med registerinnhenting etter skjæringstidspunktet');

insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
values (SEQ_KONFIG_VERDI.nextval, 'registerinnhenting.fp.opplysningsperiode.etter', 'INGEN', 'P4Y', to_date('01.01.2016', 'dd.mm.yyyy'));

INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
VALUES ('registerinnhenting.es.opplysningsperiode.før', 'Registerinnhetning periode før STP - ES', 'INGEN', 'PERIOD', 'Perioden med registerinnhenting før skjæringstidspunktet');

INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
VALUES (SEQ_KONFIG_VERDI.nextval, 'registerinnhenting.es.opplysningsperiode.før', 'INGEN', 'P12M', to_date('01.01.2017', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
values ('registerinnhenting.es.opplysningsperiode.etter', 'Registerinnhetning periode etter STP - ES', 'INGEN', 'PERIOD', 'Perioden med registerinnhenting etter skjæringstidspunktet');

insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
values (SEQ_KONFIG_VERDI.nextval, 'registerinnhenting.es.opplysningsperiode.etter', 'INGEN', 'P6M', to_date('01.01.2016', 'dd.mm.yyyy'));
