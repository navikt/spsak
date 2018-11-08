-- Manglet kommentar for kolonne
COMMENT ON COLUMN KODEVERK.sammensatt IS 'Skiller mellom sammensatt kodeverk og enkel kodeliste';
COMMENT ON COLUMN KODEVERK.kodeverk_synk_eksisterende IS 'Om eksisterende koder fra kodeverkeier skal endres ved oppdatering.';
COMMENT ON COLUMN KODEVERK.kodeverk_synk_nye IS 'Om nye koder fra kodeverkeier skal legges til ved oppdatering.';

-- Gjenopptar synkronisering av kodeverk for geografi
UPDATE KODEVERK SET kodeverk_synk_nye = 'J', kodeverk_synk_eksisterende = 'J'
WHERE KODE IN ('LANDKODE_ISO2', 'GEOPOLITISK', 'LANDGRUPPER', 'BYDELER', 'FYLKER', 'KOMMUNER', 'GEOGRAFI', 'GEOPOLITISKE_REGIONER');

UPDATE KODEVERK SET kodeverk_eier_ver = 3 WHERE kode = 'KOMMUNER';

-- Trenger noen landkode_iso2 verdier for enhetstester av landkodemapping funksjoner
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'LANDKODE_ISO2', 'NO', 'NO', 'NORGE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'LANDKODE_ISO2', 'SE', 'SE', 'SVERIGE', to_date('2000-01-01', 'YYYY-MM-DD'));

-- Manglet unik constraint på kodeliste relasjon
ALTER TABLE KODELISTE_RELASJON ADD CONSTRAINT UIDX_KODELISTE_RELASJON_1 UNIQUE (kodeverk1, kode1, kodeverk2, kode2);
