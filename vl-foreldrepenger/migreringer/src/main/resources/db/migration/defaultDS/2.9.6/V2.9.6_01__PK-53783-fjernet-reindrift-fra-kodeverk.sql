-- Testdata som inneholder reindrift må oppdateres før vi kan fjerne koden.
UPDATE IAY_EGEN_NAERING e set e.VIRKSOMHET_TYPE='ANNEN' where e.VIRKSOMHET_TYPE='REINDRIFT';
DELETE from KODELISTE where KODEVERK='VIRKSOMHET_TYPE' AND KODE='REINDRIFT';
