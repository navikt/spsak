INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN)
values ('VURDER_UTTAK.INN', 'VURDER_UTTAK', 'INN', 'Vurder uttaksvilkår - Inngang');

INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN)
VALUES ('VURDER_UTTAK.UT', 'VURDER_UTTAK', 'UT', 'Vurder uttaksvilkår - Utgang');

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT) VALUES ('5060', 'Manuell kontroll av om bruker har aleneomsorg', 'VURDER_UTTAK.INN', 'Opprettes hvis bruker oppgir aleneomsorg, men har samme adresse som ektefelle eller oppgitt annen forelder', '-', 'N');

ALTER TABLE FAMILIERELASJON ADD har_samme_bosted VARCHAR2(1 CHAR);
COMMENT ON COLUMN FAMILIERELASJON.har_samme_bosted IS 'Indikerer om personene i relasjonen bor på samme adresse';
