insert into PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, beskrivelse)
values ('behandlingskontroll.åpneBehandlingForEndringer', 'Åpne behandling for endringer', 3, 'Åpner behandlingen for endringer ved å reaktivere inaktive aksjonspunkter før startpunktet og hopper til første startpunkt');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom, ekstra_data)
values (seq_kodeliste.nextval, 'HISTORIKKINNSLAG_TYPE', 'BEH_STARTET_PÅ_NYTT', 'Behandling startet på nytt', to_date('2017-01-01', 'YYYY-MM-DD'), '{"mal": "TYPE1"}');

insert into KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
values (seq_kodeliste.nextval, 'HISTORIKK_BEGRUNNELSE_TYPE', 'BEH_STARTET_PA_NYTT', 'Behandling startet på nytt', to_date('2017-01-01', 'YYYY-MM-DD'));
