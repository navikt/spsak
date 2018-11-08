-- Feil kode1 for avslagsårsak 1051 - skal være FP_VK_16_3
update KODELISTE_RELASJON
set KODE1 = 'FP_VK_16_3'
where KODEVERK2 = 'AVSLAGSARSAK' and KODE2 = '1051';

-- Legge inn ny vilkårutfallmerknad
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, '1051', '1051', 'Stebarnsadopsjon ikke flere dager igjen', 'VILKAR_UTFALL_MERKNAD', to_date('2000-01-01', 'yyyy-mm-dd'));

