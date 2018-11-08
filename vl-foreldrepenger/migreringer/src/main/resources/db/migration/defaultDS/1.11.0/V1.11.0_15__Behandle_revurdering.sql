INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, BESKRIVELSE, VILKAR_TYPE)
VALUES ('5027', 'Sjekk manglende fødsel', 'VURDERBV.INN', 'N', 'For å sjekke manglende fødsel hvis det finner at det har gått 25 dager siden oppgitt termindato eller at det er revurderingsbehandling', 'FP_VK_1');

INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, BESKRIVELSE, VILKAR_TYPE)
VALUES ('5028', 'Foreslå vedtak manuelt', 'FORVEDSTEG.UT', 'N', 'Skal alltid få et aksjonspunkt for foreslå vedtak manuelt for revurderingsbehandling', '-');

ALTER TABLE BEHANDLING_ARSAK ADD original_behandling_id NUMBER(19);
ALTER TABLE BEHANDLING_ARSAK ADD CONSTRAINT FK_BEHANDLING_ARSAK_2 FOREIGN KEY (original_behandling_id) REFERENCES BEHANDLING;

COMMENT ON COLUMN BEHANDLING_ARSAK.original_behandling_id IS 'FK: BEHANDLING';

ALTER TABLE BEHANDLING_VEDTAK ADD beslutning char(1) DEFAULT 'N' NOT NULL ;

DROP INDEX IDX_BEHANDLING_ARSAK_1;
CREATE UNIQUE INDEX IDX_BEHANDLING_ARSAK_1 ON BEHANDLING_ARSAK (behandling_id);

UPDATE AKSJONSPUNKT SET AKSJONSPUNKT_DEF = '5027' WHERE AKSJONSPUNKT_DEF = '5002' OR AKSJONSPUNKT_DEF = '5003';

DELETE FROM AKSJONSPUNKT_DEF WHERE kode='5002';
DELETE FROM AKSJONSPUNKT_DEF WHERE kode='5003';

ALTER TABLE FOEDSEL ADD dokumentasjon_foreligger char(1);
COMMENT ON COLUMN FOEDSEL.dokumentasjon_foreligger IS 'Dokumentasjon avklares fødsel foreligger';

UPDATE AKSJONSPUNKT_DEF SET VURDERINGSPUNKT = 'FORVEDSTEG.UT' WHERE KODE = '5015';

insert into kodeliste (id, kode, navn, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'VUR_KONS_YTE_FOR', 'Vurder konsekvens for ytelse foreldrepenger', 'OPPGAVE_AARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));

INSERT INTO PROSESS_TASK_TYPE(KODE, NAVN, FEIL_MAKS_FORSOEK, FEIL_SEK_MELLOM_FORSOEK, BESKRIVELSE)
VALUES ('oppgavebehandling.opprettOppgaveVurderKonsekvens', 'Opprett oppgave vurder konsekvens for ytelse', 3, 60, 'Oppretter oppgave i GSAK for å vurdere konsekvens for ytelse');
