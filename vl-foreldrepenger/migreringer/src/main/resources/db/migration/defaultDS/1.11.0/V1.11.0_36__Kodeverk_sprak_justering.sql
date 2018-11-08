-- Forhindre automatisk lasting av språk; kan gi uforutsigbare konsekvenser for VL
update KODEVERK set KODEVERK_SYNK_EKSISTERENDE = 'N' where KODE = 'SPRAAK_KODE';
-- Sette inn manglende koder manuelt
insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'NO', 'Norsk bokmål (foreldet forkortelse)', 'SPRAAK_KODE', to_date('2000-01-01', 'yyyy-mm-dd'));


