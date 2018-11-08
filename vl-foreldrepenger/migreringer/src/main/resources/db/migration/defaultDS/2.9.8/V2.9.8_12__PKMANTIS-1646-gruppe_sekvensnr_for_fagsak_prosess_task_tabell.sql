-- GRUPPE_SEKVENSNR må være samme for alle tasks som inngår i en gruppe for en fagsak.  
ALTER TABLE FAGSAK_PROSESS_TASK ADD GRUPPE_SEKVENSNR NUMBER(19, 0);

create index IDX_FAGSAK_PROSESS_TASK_4 ON fagsak_prosess_task(gruppe_sekvensnr);

COMMENT ON COLUMN FAGSAK_PROSESS_TASK.GRUPPE_SEKVENSNR is 'For en gitt fagsak angir hvilken rekkefølge task skal kjøres.  Kun tasks med laveste gruppe_sekvensnr vil kjøres. Når disse er FERDIG vil de ryddes bort og neste med lavest sekvensnr kan kjøres (gitt at den er KLAR)';