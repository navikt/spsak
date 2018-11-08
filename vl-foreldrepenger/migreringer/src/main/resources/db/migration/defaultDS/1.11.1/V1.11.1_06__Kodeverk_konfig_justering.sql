-- Skrur av automatisk synkronisering, slik at de kan skrus på etter behov, under kontrollerte forhold
UPDATE KODEVERK SET KODEVERK_SYNK_NYE = 'N', KODEVERK_SYNK_EKSISTERENDE = 'N';

-- Justere konfigurasjon iht NAVs tilbydde versjoner
UPDATE KODEVERK SET KODEVERK_EIER_VER = 1 WHERE KODE = 'LANDKODER';

-- Hente inn overskrevne oppdateringer for BEHANDLING_TYPE
UPDATE KODELISTE SET NAVN = 'Førstegangsbehandling' WHERE KODE = 'BT-002' AND KODEVERK = 'BEHANDLING_TYPE';
UPDATE KODELISTE SET NAVN = 'Klage' WHERE KODE = 'BT-003' AND KODEVERK = 'BEHANDLING_TYPE';
UPDATE KODELISTE SET NAVN = 'Revurdering' WHERE KODE = 'BT-004' AND KODEVERK = 'BEHANDLING_TYPE';
UPDATE KODELISTE SET NAVN = 'Søknad' WHERE KODE = 'BT-005' AND KODEVERK = 'BEHANDLING_TYPE';
