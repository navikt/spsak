insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
values (seq_kodeliste.nextval, 'UTTAK_SAMTIDIG_UTTAK', 'Samtidig uttak', 'Samtidig uttak', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');

delete from kodeliste where kode = 'UTTAK_GRADERING' and kodeverk = 'HISTORIKK_ENDRET_FELT_TYPE';
