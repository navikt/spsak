INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE, OPPRETTET_AV, OPPRETTET_TID, ENDRET_AV, ENDRET_TID)
VALUES ('INREG_AVSL', 'Innhent registeropplysninger - resterende oppgaver', 'UTRED',
        'Innhenting av registeropplysninger som vil benyttes til avklaring av fakta og vurdering av saken', 'VL',
        sysdate, NULL, NULL);

UPDATE BEHANDLING_STEG_TYPE
SET NAVN = 'Innhent registeropplysninger - innledende oppgaver'
WHERE KODE = 'INREG';

INSERT INTO behandling_type_steg_sekv (id, behandling_type, behandling_steg_type, fagsak_ytelse_type, sekvens_nr)
VALUES (seq_behandling_type_steg_sekv.nextval, 'BT-002', 'INREG_AVSL', 'FP', 42);
INSERT INTO behandling_type_steg_sekv (id, behandling_type, behandling_steg_type, fagsak_ytelse_type, sekvens_nr)
VALUES (seq_behandling_type_steg_sekv.nextval, 'BT-004', 'INREG_AVSL', 'FP', 42);
INSERT INTO behandling_type_steg_sekv (id, behandling_type, behandling_steg_type, fagsak_ytelse_type, sekvens_nr)
VALUES (seq_behandling_type_steg_sekv.nextval, 'BT-002', 'INREG_AVSL', 'ES', 42);
INSERT INTO behandling_type_steg_sekv (id, behandling_type, behandling_steg_type, fagsak_ytelse_type, sekvens_nr)
VALUES (seq_behandling_type_steg_sekv.nextval, 'BT-004', 'INREG_AVSL', 'ES', 42);

INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN)
VALUES ('INREG_AVSL.INN', 'INREG_AVSL', 'INN', 'Innhent registeropplysninger - restopg - Inngang');

INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN)
VALUES ('INREG_AVSL.UT', 'INREG_AVSL', 'UT', 'Innhent registeropplysninger - restopg - Utgang');

UPDATE VURDERINGSPUNKT_DEF
SET NAVN = 'Innhent registeropplysninger - innlopg - Inngang'
WHERE kode = 'INREG.INN';

UPDATE VURDERINGSPUNKT_DEF
SET NAVN = 'Innhent registeropplysninger - innlopg - Utgang'
WHERE kode = 'INREG.UT';

update AKSJONSPUNKT_DEF
set VURDERINGSPUNKT = 'INREG_AVSL.UT'
WHERE kode = 5024;

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, AKSJONSPUNKT_TYPE, TILBAKEHOPP_VED_GJENOPPTAKELSE, FRIST_PERIODE)
VALUES ('7010', 'Venter på prosesstask for innhenting av registeropplysninger', 'INREG_AVSL.UT',
        'Venter på prosesstask innhentsaksopplysninger.relaterteYtelser laget av innhent registeropplysninger - innledende oppgaver.',
        '-', 'N', 'AUTO', 'N', 'P1Y');


ALTER TABLE AKSJONSPUNKT_DEF ADD LAG_UTEN_HISTORIKK VARCHAR2(1) DEFAULT 'N';

UPDATE AKSJONSPUNKT_DEF SET LAG_UTEN_HISTORIKK = 'N';

ALTER TABLE AKSJONSPUNKT_DEF MODIFY(LAG_UTEN_HISTORIKK NOT NULL check(LAG_UTEN_HISTORIKK IN ('J','N')));

UPDATE AKSJONSPUNKT_DEF SET LAG_UTEN_HISTORIKK = 'J' WHERE KODE = 7010;
