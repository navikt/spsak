
INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('TEMA', 'Tema', 'NAV Tema', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Tema', '2', 'Tema', 'N', 'N');

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'TEMA', 'FOR_SVA', 'FOR', 'Foreldre- og svangerskapspenger', 'Foreldre- og svangerskapspenger', to_date('2017-05-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'TEMA', '-', null, 'Ikke definert', 'Ikke definert', to_date('2017-05-01', 'YYYY-MM-DD'));
