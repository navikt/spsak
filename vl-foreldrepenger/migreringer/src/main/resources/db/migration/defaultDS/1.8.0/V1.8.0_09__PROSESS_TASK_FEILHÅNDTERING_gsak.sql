INSERT INTO PROSESS_TASK_FEILHAND (KODE,NAVN,BESKRIVELSE,OPPRETTET_AV) values ('TIL_GSAK_BACKOFF','Til Gsak ved funksjonell feil','Send sak til manuell journalføring hos gsak dersom det oppstår en funksjonell feil. Andre feil håndters som for DEFAULT.','VL');
INSERT INTO PROSESS_TASK_FEILHAND (KODE,NAVN,BESKRIVELSE,OPPRETTET_AV,INPUT_VARIABEL1,INPUT_VARIABEL2) values ('TIL_GSAK_ÅPNINGSTID','Til Gsak ved funksjonell feil','Send sak til manuell journalføring hos gsak dersom det oppstår en funksjonell feil. Andre feil håndters som for ÅPNINGSTID.','VL',7,18);

update PROSESS_TASK_TYPE set feilhandtering_algoritme = 'TIL_GSAK_BACKOFF' where kode = 'fordeling.tilJournalforing';
update PROSESS_TASK_TYPE set feilhandtering_algoritme = 'TIL_GSAK_BACKOFF' where kode = 'fordeling.hentFraJoark';
