
insert into kodeverk (kode, navn, beskrivelse ) values ('BEHANDLING_TYPE', 'BehandlingType', '');
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk, offisiell_kode) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'BEHANDLING_TYPE', nav_offisiell_kode from BEHANDLING_TYPE;

--------------------------

begin 
    migrer_KODELISTE_fk('BEHANDLING_TYPE', 'BEHANDLING_TYPE');
end;
/


drop table BEHANDLING_TYPE cascade constraints;
