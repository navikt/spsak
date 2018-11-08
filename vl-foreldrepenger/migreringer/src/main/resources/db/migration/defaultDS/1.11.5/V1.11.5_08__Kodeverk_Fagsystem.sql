-- NAV har ikke noe offisielt kodeverk for Fagsystem.
-- Dette kodeverket er laget utfra http://stash.devillo.no/projects/FELL/repos/fellessystemer/browse/environment/src/main/resources/xml/fagsystem.xml?at=refs%2Fheads%2Frelease%2F2018-HL0
-- etter råd fra Vegard Skjefstad (GSak).

INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('FAGSYSTEM', 'Fagsystemer', 'NAV Fagsystemer', 'GSak', null, null, 'Fagsystemer', 'N', 'N');

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'FAGSYSTEM', 'ARENA', 'AO01', 'Arena', 'Arena', to_date('2010-02-13', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'FAGSYSTEM', 'GRISEN', 'AO11', 'Grisen', 'Grisen', to_date('2011-01-27', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'FAGSYSTEM', 'GOSYS', 'FS22', 'Gosys', 'Gosys', to_date('2009-04-25', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'FAGSYSTEM', 'INFOTRYGD', 'IT01', 'Infotrygd', 'Infotrygd', to_date('2010-02-13', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'FAGSYSTEM', 'HJE_HEL_ORT', 'OEBS', 'Hjelpemidler, Helsetjenester og Ort. Hjelpemidler', 'Hjelpemidler, Helsetjenester og Ort. Hjelpemidler', to_date('2010-02-13', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'FAGSYSTEM', 'PESYS', 'PP01', 'Pesys', 'Pesys', to_date('2011-12-10', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'FAGSYSTEM', 'VENTELONN', 'V2', 'Ventelønn', 'Ventelønn', to_date('2010-02-13', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'FAGSYSTEM', 'UNNTAK', 'UFM', 'Unntak', 'Unntak', to_date('2010-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'FAGSYSTEM', 'FPSAK', 'FS36', 'Vedtaksløsning Foreldrepenger', 'Vedtaksløsning Foreldrepenger', to_date('2017-06-28', 'YYYY-MM-DD'));

-- TODO (rune) PKHUMLE-425 : legg til evt. flere som mangler ift. no.nav.vedtak.kodeverk.Fagsystem

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'FAGSYSTEM', '-', null, 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'));
