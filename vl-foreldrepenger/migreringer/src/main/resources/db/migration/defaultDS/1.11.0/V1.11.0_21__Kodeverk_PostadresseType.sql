-- Nytt kodeverk for adressetype, med kodeverdier
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('POSTADRESSE_TYPE', 'N', 'N', 'Postadressetype', 'Gjeldende postadressetype mottas som del av personinfo fra TPS');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'BOSTEDSADRESSE', 'Bostedsadresse', 'Bostedsadresse', 'POSTADRESSE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'POSTADRESSE', 'Postadresse', 'Postadresse', 'POSTADRESSE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'POSTADRESSE_UTLAND', 'Postadresse i utlandet', 'Postadresse i utlandet', 'POSTADRESSE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'MIDLERTIDIG_POSTADRESSE_NORGE', 'Midlertidig postadresse i Norge', 'Midlertidig postadresse i Norge', 'POSTADRESSE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'MIDLERTIDIG_POSTADRESSE_UTLAND', 'Midlertidig postadresse i utlandet', 'Midlertidig postadresse i utlandet', 'POSTADRESSE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'UKJENT_ADRESSE', 'Ukjent adresse', 'Ukjent adresse', 'POSTADRESSE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
