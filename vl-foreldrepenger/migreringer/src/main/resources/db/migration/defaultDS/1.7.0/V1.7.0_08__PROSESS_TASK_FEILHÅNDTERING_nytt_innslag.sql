-- Tabell: BEHANDLING_STEG_STATUS_TYPE
ALTER TABLE PROSESS_TASK_FEILHAND ADD (INPUT_VARIABEL1 NUMBER, INPUT_VARIABEL2 NUMBER);

INSERT INTO PROSESS_TASK_FEILHAND (KODE,NAVN,BESKRIVELSE,OPPRETTET_AV,INPUT_VARIABEL1,INPUT_VARIABEL2) values ('ÅPNINGSTID','Åpningstidsbasert feilhåndtering','Åpningstidsbasert feilhåndtering. INPUT_VARIABEL1 = åpningstid og INPUT_VARIABEL2 = stengetid','VL',7,18);

INSERT INTO PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feil_sek_mellom_forsoek, feilhandtering_algoritme, beskrivelse)
VALUES ('innhentsaksopplysninger.relaterteYtelser', 'Innhent informasjon fra Infotrygd',  3, 60, 'ÅPNINGSTID', 'Task som henter og lagrer data om relaterte ytelser fra Infotrygd');
