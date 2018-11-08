delete from KODELISTE where kode = 'UFORETRYGD';
delete from KODELISTE where kode = 'UTDOGARB';

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'SAMMTIDIGUTTAK', 'Samtidig uttak flerbarnsfødsel', 'Samtidig uttak flerbarnsfødsel', 'MORS_AKTIVITET', to_date('2000-01-01', 'YYYY-MM-DD'));
