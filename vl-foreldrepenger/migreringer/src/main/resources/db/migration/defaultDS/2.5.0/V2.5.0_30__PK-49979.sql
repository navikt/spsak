insert into KODEVERK (KODE, KODEVERK_SYNK_NYE, KODEVERK_SYNK_EKSISTERENDE, NAVN, BESKRIVELSE) values ('DOKUMENT_GRUPPE', 'N', 'N', 'Dokumentgruppe', 'Internt kodeverk som grupperer dokument i dokumentmottak');

insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, 'SØKNAD', 'Søknad', 'Dokumentkoder av type Søknad', 'DOKUMENT_GRUPPE');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, 'ENDRINGSSØKNAD', 'Endringssøknad', 'Dokumentkoder av type Endringssøknad', 'DOKUMENT_GRUPPE');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, 'KLAGE', 'Klage', 'Dokumentkoder av type Klage', 'DOKUMENT_GRUPPE');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, 'VEDLEGG', 'Vedlegg', 'Dokumentkoder av type Vedlegg', 'DOKUMENT_GRUPPE');
insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'DOKUMENT_GRUPPE');

insert into KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK) values (seq_kodeliste.nextval, 'RE_ENDRING_FRA_BRUKER', 'RE_ENDRING_FRA_BRUKER', 'Endring fra bruker"', 'BEHANDLING_AARSAK');
