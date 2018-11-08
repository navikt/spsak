INSERT INTO PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feil_sek_mellom_forsoek, feilhandtering_algoritme, beskrivelse)
VALUES ('oppgavebehandling.opprettOppgaveSakSendtTilbake',
        'Oppretter oppgave i GSAK for sak sendt tilbake', 3, 60, 'DEFAULT',
        'Oppretter oppgave i GSAK etter at beslutter sender saken tilbake til saksbehandler');
