insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('brev.returadresse.enhet.navn', 'NAV enhetsnavn i returadresse', 'INGEN', 'STRING', 'NAV enhetsnavn i returadresse');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'brev.returadresse.enhet.navn', 'INGEN', 'NAV Familie- og pensjonsytelser', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('brev.returadresse.adresselinje1', 'Adresselinje1 i returadresse', 'INGEN', 'STRING', 'Adresselinje1 i returadresse');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'brev.returadresse.adresselinje1', 'INGEN', 'Postboks 6600 Etterstad', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('brev.returadresse.postnummer', 'Postnummer i returadresse', 'INGEN', 'STRING', 'Postnummer i returadresse');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'brev.returadresse.postnummer', 'INGEN', '0607', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('brev.returadresse.poststed', 'Poststed i returadresse', 'INGEN', 'STRING', 'Poststed i returadresse');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'brev.returadresse.poststed', 'INGEN', 'OSLO', to_date('01.01.2016', 'dd.mm.yyyy'));
