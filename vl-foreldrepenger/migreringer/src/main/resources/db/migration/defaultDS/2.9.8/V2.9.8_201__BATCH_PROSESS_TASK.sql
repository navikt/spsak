
INSERT INTO PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feil_sek_mellom_forsoek, feilhandtering_algoritme, beskrivelse)
     VALUES ('batch.scheduler', 'Oppretter batchrunners og ny instans',  1, 30, 'DEFAULT', 'Task som oppretter tasks for planlagte batches og ny scheduler');
INSERT INTO PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feil_sek_mellom_forsoek, feilhandtering_algoritme, beskrivelse)
     VALUES ('batch.runner', 'Kjører angitt batch',  1, 30, 'DEFAULT', 'Kjører batch som angitt i parametere');
