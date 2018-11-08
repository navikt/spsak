INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, AKSJONSPUNKT_TYPE, SKJERMLENKE_TYPE)
VALUES ('7008', 'Satt på vent pga for tidlig søknad', 'INSØK.UT', 'Satt på vent pga for tidlig søknad', '-', 'N', 'AUTO', '-');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES
  (seq_kodeliste.nextval, 'VENT_AARSAK', 'FOR_TIDLIG_SOKNAD', 'Venter pga for tidlig søknad', 'Venter pga for tidlig søknad', to_date('2018-03-15', 'YYYY-MM-DD'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
values ('ventefrist.uker.ved.tidlig.fp.soeknad', 'Ventefrist i uker ved for tidlig søknad', 'INGEN', 'INTEGER', 'Behandling settes på vent med definert ventefrist dersom søknad for fp mottas for tidlig');

insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
values (SEQ_KONFIG_VERDI.nextval, 'ventefrist.uker.ved.tidlig.fp.soeknad', 'INGEN', '4', to_date('15.03.2018', 'dd.mm.yyyy'));
