merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'BEHANDLING_TEMA' and k.offisiell_KODE = 'ab0327')
when not matched then
insert (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values (seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'ENGST', 'ab0327', 'Engangsstønad', null, to_date('2000-01-01', 'YYYY-MM-DD'));
--riktig fra-dato er 03.02.2018 i følge kodeverkklienten
