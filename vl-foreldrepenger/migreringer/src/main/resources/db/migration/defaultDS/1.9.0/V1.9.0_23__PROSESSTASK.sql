INSERT INTO PROSESS_TASK_TYPE (kode, navn) values ('behandlingsprosess.etterkontroll', 'Automatisk etterkontroll');

INSERT INTO PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, beskrivelse)
values ('behandlingskontroll.startBehandlingTomPapirsøknad', 'Start behandling av tom papirsøknad', 3, 'Start behandling av papirsøknad med mangelfull informasjon');
