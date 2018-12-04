
DELETE FROM KODELISTE_NAVN_I18N WHERE KL_KODEVERK = 'BEHANDLING_TEMA' and kl_kode!='-';
DELETE FROM KODELISTE WHERE KODEVERK = 'BEHANDLING_TEMA'  and kode!='-';

Insert into KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,BESKRIVELSE,GYLDIG_FOM,GYLDIG_TOM,OPPRETTET_AV,OPPRETTET_TID,ENDRET_AV,ENDRET_TID,EKSTRA_DATA) values 
('1003200','BEHANDLING_TEMA','ab0061','ab0061',null,to_date('01.07.2006','DD.MM.YYYY'),to_date('31.12.9999','DD.MM.YYYY'),'VL',to_timestamp('09.11.2018 08.37.36,391000000','DD.MM.YYYY HH24.MI.SS'),null,null,null);

Insert into KODELISTE_NAVN_I18N (ID,KL_KODEVERK,KL_KODE,SPRAK,NAVN,OPPRETTET_AV,OPPRETTET_TID,ENDRET_AV,ENDRET_TID) values 
('1003200','BEHANDLING_TEMA','ab0061','NB','Sykepenger','VL',to_timestamp('09.11.2018 08.38.08,026000000','DD.MM.YYYY HH24.MI.SS'),null,null);
