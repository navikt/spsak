-- Utvide AKSJONSPUNKT_DEF med søknadsfrist
ALTER TABLE AKSJONSPUNKT_DEF
  ADD FRIST_PERIODE VARCHAR2(20 CHAR);

-- Splitte opp eksisterende autopunkt (7002) i flere autopunkter
INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, AKSJONSPUNKT_KATEGORI, VILKAR_TYPE, VURDERINGSPUNKT, FRIST_PERIODE, ALLTID_TOTRINNSBEHANDLING, BESKRIVELSE, AKSJONSPUNKT_TYPE)
VALUES ('7001', 'Manuelt satt på vent', 'APK-004', NULL , 'KOFAK.UT', 'P4W', 'N', 'Manuelt satt på vent av saksbehandler', 'AUTO');

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, AKSJONSPUNKT_KATEGORI, VILKAR_TYPE, VURDERINGSPUNKT, FRIST_PERIODE, ALLTID_TOTRINNSBEHANDLING, BESKRIVELSE, AKSJONSPUNKT_TYPE)
VALUES ('7003', 'Mottak har satt på vent', 'APK-004', NULL , 'KOFAK.INN', 'P4W', 'N', 'Mottak har oppdaget ufulstending søknad og satt behandlingen på vent', 'AUTO');

UPDATE AKSJONSPUNKT_DEF
SET NAVN        = 'Vent på registrering av fødsel'
  , BESKRIVELSE = 'Vent på registrering av fødsel'
  , FRIST_PERIODE = 'P2W'
WHERE KODE = '7002';

UPDATE AKSJONSPUNKT_TYPE set BESKRIVELSE ='Et punkt som løses automatisk' WHERE KODE = 'Autopunkt';
