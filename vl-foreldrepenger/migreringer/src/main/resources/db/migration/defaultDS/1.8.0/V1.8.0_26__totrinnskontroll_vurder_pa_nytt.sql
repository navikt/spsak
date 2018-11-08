ALTER TABLE AKSJONSPUNKT ADD totrinnsbehandling_godkjent CHAR(1);

ALTER TABLE BEHANDLING ADD ansvarlig_beslutter VARCHAR2(100 CHAR);

DROP INDEX UIDX_BEHANDLING_STEG_TILSTA_1;

INSERT INTO BEHANDLING_STEG_STATUS_TYPE(kode, navn, beskrivelse) VALUES ('TILBAKEFØRT', 'Tilbakeført', 'Steget er avbrutt og tilbakeført til et tidligere steg.');
