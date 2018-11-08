-- "Kan dere korrigere FP_VK_9 til FP_VK_33 ?  Dette er ikke et løpenummer men en refernse til FFA/LU behovsdokumentasjon"
-- Bruker FP_VK_8 som placeholder for FP_VK_33, for å unngå FK-integritetsproblem
UPDATE AKSJONSPUNKT_DEF SET VILKAR_TYPE = 'FP_VK_8' WHERE VILKAR_TYPE = 'FP_VK_9';
UPDATE AVSLAGSARSAK SET VILKAR_KODE = 'FP_VK_8' WHERE VILKAR_KODE = 'FP_VK_9';
-- Oppdaterer selve koden: FP_VK_9 -> FP_VK_33
ALTER TABLE VILKAR MODIFY (VILKAR_TYPE varchar2(8 char));
UPDATE VILKAR_TYPE SET KODE = 'FP_VK_33' WHERE kode = 'FP_VK_9';
-- Bytter placeholder med riktig kode: FP_VK_8 -> FP_VK_33
UPDATE AKSJONSPUNKT_DEF SET VILKAR_TYPE = 'FP_VK_33' WHERE VILKAR_TYPE = 'FP_VK_8';
UPDATE AVSLAGSARSAK SET VILKAR_KODE = 'FP_VK_33' WHERE VILKAR_KODE = 'FP_VK_8';

-- Andre dokoppdateringer
UPDATE VILKAR_TYPE SET NAVN = 'Fødselsvilkår Mor' WHERE KODE = 'FP_VK_1';
