UPDATE PROSESS_TASK_TYPE SET kode = 'oppgavebehandling.opprettOppgaveBehandleSak' WHERE kode = 'oppgavebehandling.opprettOppgave';
UPDATE PROSESS_TASK_TYPE SET navn = 'Oppretter oppgave i GSAK for å behandle sak' WHERE kode = 'oppgavebehandling.opprettOppgaveBehandleSak';
UPDATE PROSESS_TASK_TYPE SET beskrivelse = 'Oppretter oppgave i GSAK for å behandle sak' WHERE kode = 'oppgavebehandling.opprettOppgaveBehandleSak';

INSERT INTO PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feil_sek_mellom_forsoek, feilhandtering_algoritme, beskrivelse)
        VALUES ('oppgavebehandling.opprettOppgaveRegistrerSøknad', 'Oppretter oppgave i GSAK for å registrere søknad',
        3, 60, 'DEFAULT', 'Oppretter oppgave i GSAK for å registrere ustrukturert søknad');
