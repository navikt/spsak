alter table behandling_grunnlag add opplysninger_oppdatert_tid timestamp(3);
alter table medlemskap_perioder add medl_id number(19);

INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE)
VALUES ('5024', 'Avklar oppdatering av registerdata', 'KOFAK.INN', 'Saksbehandler må avklare hvilke verdier som er gjeldene, det er mismatch mellom register- og lokaldata', 'J', '-');

INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
VALUES ('oppdatere.registerdata.tidspunkt', 'Periode for hvor ofte registerdata skal oppdateres', 'INGEN', 'DURATION', 'Periode for hvor ofte registerdata skal oppdateres');

INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
VALUES (SEQ_KONFIG_VERDI.nextval, 'oppdatere.registerdata.tidspunkt', 'INGEN', 'PT10H', to_date('01.01.2017', 'dd.mm.yyyy'));

INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('NYE_REGOPPLYSNINGER', 'Nye registeropplysninger');
