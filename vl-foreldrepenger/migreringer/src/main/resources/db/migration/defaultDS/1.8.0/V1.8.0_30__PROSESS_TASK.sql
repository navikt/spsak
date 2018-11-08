INSERT INTO PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feil_sek_mellom_forsoek, feilhandtering_algoritme, beskrivelse)
  VALUES ('behandlingskontroll.oppdatersakogbehandling', 'Melder om behandling til Sak Og Behandling.',  3, 60, 'DEFAULT', 'Task som melder om opprettet eller avsluttet behandling til Sak Og Behandling.');

DELETE FROM PROSESS_TASK_TYPE WHERE KODE = 'fordeling.oppdaterSakOgBehandling';
DELETE FROM PROSESS_TASK_TYPE WHERE KODE = 'fordeling.oppdaterSakOgBehandlingOgStopp';
