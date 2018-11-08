-- VilkarType for Opptjeningsperiodevilkår

INSERT INTO VURDERINGSPUNKT_DEF(KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN, BESKRIVELSE)
values ('VURDER_OPPTJ.INN', 'VURDER_OPPTJ', 'INN', 'Vurder Opptjeningsvilkår - Inngang', 'Vurderingspunkt ved inngang til Behandling Steg - Vurder Opptjeningsvilkår');

INSERT INTO VURDERINGSPUNKT_DEF(KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN, BESKRIVELSE)
VALUES ('VURDER_OPPTJ.UT', 'VURDER_OPPTJ', 'UT', 'Vurder Opptjeningsvilkår - Utgang', 'Vurderingspunkt ved utgang fra Behandling Steg - Vurder Opptjeningsvilkår');

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, AKSJONSPUNKT_TYPE, TOTRINN_BEHANDLING_DEFAULT, FRIST_PERIODE)
VALUES (7006, 'Venter på opptjeningsopplysninger', 'VURDER_OPPTJ.UT', 'Satt på vent i påvente av at opplysninger relevant for opptjening (inntekt) skal komme inn for manglende perioder', 'FP_VK_23', 'AUTO', 'N', NULL);

INSERT INTO KODEVERK (KODE, KODEVERK_EIER, NAVN, BESKRIVELSE) VALUES ('OPPTJENING_AKTIVITET_TYPE', 'VL', 'Opptjening aktivitet type', 'Typer av aktivitet (eks. Arbeid, Næring, Likestilt med Yrkesaktivitet, andre Ytelser) som er tatt med i vurdering av Opptjening, etter vilkår FP_VK_23');

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'ARBEID', 'Arbeid', 'Registrert arbeidsforhold i AA-registeret', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'FRILANS', 'Frilans', 'Frilanser', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'NÆRING', 'Næring', 'Registrert i Enhetsregisteret som selvstendig næringsdrivende', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

-- Ytelser
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'DAGPENGER', 'Dagpenger', 'Mottar ytelse for Dagpenger', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'AAP', 'Arbeidsavklaringspenger', 'Mottar ytelse for Arbeidsavklaringspenger', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'FORELDREPENGER', 'Foreldrepenger', 'Mottar ytelse for Foreldrepenger', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'SYKEPENGER', 'Sykepenger', 'Mottar ytelse for Sykepenger', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'SVANGERSKAPSPENGER', 'Svangerskapspenger', 'Mottar ytelse for Svangerskapspenger', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'OPPLÆRINGSPENGER', 'Opplæringspenger', 'Mottar ytelse for Opplæringspenger', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'OMSORGSPENGER', 'Omsorgspenger', 'Mottar ytelse for Omsorgspenger', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'PLEIEPENGER', 'Pleiepenger', 'Mottar ytelse for Pleiepenger', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

-- Pensjonsgivende inntekt som likestilles med yrkesaktivietet

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'VARTPENGER', 'Vartpenger', 'Vartpenger. Pensjonsgivende inntekt som likestilles med yrkesaktivitet', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'VENTELØNN', 'Vartpenger', 'Ventelønn. Pensjonsgivende inntekt som likestilles med yrkesaktivitet', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'VIDERE_ETTERUTDANNING', 'Videre- og etterutdanning', 'Lønn fra arbeidsgiver ifbm. videre- og etterutdanning. Pensjonsgivende inntekt som likestilles med yrkesaktivitet', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_TYPE', 'MILITÆR_ELLER_SIVILTJENESTE', 'Militær- eller siviltjeneste', 'vtjening av militær- eller siviltjeneste eller obligatorisk sivilforsvarstjeneste. Pensjonsgivende inntekt som likestilles med yrkesaktivitet', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

-- Opptjening aktivitet periode klassifisering

INSERT INTO KODEVERK (KODE, KODEVERK_EIER, NAVN, BESKRIVELSE) VALUES ('OPPTJENING_AKTIVITET_KLASSIFISERING', 'VL', 'Opptjening aktivitet status', 'Klassifisering av aktivitet (godkjent, antatt, ikke godkjent, etc.)');

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_KLASSIFISERING', 'BEKREFET_GODKJENT', 'Bekreftet godkjent', 'Aktivitet er bekreftet godkjent i forbindelse med vurdering av Opptjeningsvilkåret.', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_KLASSIFISERING', 'ANTATT_GODKJENT', 'Antatt godkjent', 'Aktivitet er antatt godkjent (medfører at behandling settes på vent)', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'OPPTJENING_AKTIVITET_KLASSIFISERING', 'BEKREFTET_AVVIST', 'Bekreftet avvist', 'Aktivitet er bekreftet avvist i forbindelse med vurdering av Opptjeningsvilkåret.', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));


-- Tabell OPPTJENING_AKTIVITET
CREATE TABLE OPPTJENING_AKTIVITET (
  id                 NUMBER(19, 0)                     NOT NULL,
  fom                DATE                              NOT NULL,
  tom                DATE                              NOT NULL,
  opptjeningsperiode_id NUMBER(19, 0)                  NOT NULL,
  aktivitet_type     varchar2(100 char)                NOT NULL,
  aktivitet_referanse varchar2(1000 char)              NOT NULL,
  klassifisering     varchar2(100 char)                NOT NULL,
  versjon            NUMBER(19, 0) DEFAULT 0           NOT NULL,
  opprettet_av       VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid      TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av          VARCHAR2(20 CHAR),
  endret_tid         TIMESTAMP(3),
  KL_AKTIVITET_TYPE VARCHAR2(100 char) AS ('OPPTJENING_AKTIVITET_TYPE'),
  KL_KLASSIFISERING VARCHAR2(100 char) AS ('OPPTJENING_AKTIVITET_KLASSIFISERING'),
  CONSTRAINT PK_OPPTJENING_AKTIVITET PRIMARY KEY (id),
  CONSTRAINT FK_OPPTJENING_AKTIVITET FOREIGN KEY (opptjeningsperiode_id) REFERENCES OPPTJENINGSPERIODE(ID)
);

CREATE INDEX IDX_OPPTJENING_AKTIVITET_01
  ON OPPTJENING_AKTIVITET (opptjeningsperiode_id);
  
CREATE INDEX IDX_OPPTJENING_AKTIVITET_02
  ON OPPTJENING_AKTIVITET (aktivitet_type, kl_aktivitet_type);

CREATE INDEX IDX_OPPTJENING_AKTIVITET_03
  ON OPPTJENING_AKTIVITET (klassifisering, kl_klassifisering);
  
alter table OPPTJENING_AKTIVITET add constraint FK_OPPTJENING_AKTIVITET_TYPE foreign key (aktivitet_type, kl_aktivitet_type) references KODELISTE(kode, kodeverk);

alter table OPPTJENING_AKTIVITET add constraint FK_OPPTJENING_AKTIVITET_KLASS foreign key (klassifisering, kl_klassifisering) references KODELISTE(kode, kodeverk);

-- er dessverre ingen enkel måte å forhindre overlappende fom/tom intervaller i Oracle uten å gå veien om triggere eller materialized views.  
-- Enforcer derfor det i Applikasjonslaget og legger kun på et basic unique constraint her.
CREATE UNIQUE INDEX UIDX_OPPTJENING_AKTIVITET_01
  ON OPPTJENING_AKTIVITET (opptjeningsperiode_id, aktivitet_type, aktivitet_referanse, fom);

CREATE INDEX UIDX_OPPTJENING_AKTIVITET_04
  ON OPPTJENING_AKTIVITET (fom, tom, aktivitet_type, opptjeningsperiode_id);
  
  
CREATE SEQUENCE SEQ_OPPTJENING_AKTIVITET MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE OPPTJENING_AKTIVITET IS 'Opptjening aktiviteter som er vurdert innenfor angitt opptjeningperiode og klassifisert ifht. anvendelse';
COMMENT ON COLUMN OPPTJENING_AKTIVITET.ID IS 'Primary Key';
COMMENT ON COLUMN OPPTJENING_AKTIVITET.aktivitet_type IS 'Type aktivitet som har inngått i vurdering av opptjening';
COMMENT ON COLUMN OPPTJENING_AKTIVITET.aktivitet_referanse IS 'En referanse (spesifikk ifht. aktivitet_type) som skilles ulike innslag av samme aktivitet for samme periode (eks. Virksomhetsnummer for aktivitet av type ARBEID)';
COMMENT ON COLUMN OPPTJENING_AKTIVITET.klassifisering IS 'Klassifisering av anvendelse av aktivitet for angitt periode (godkjent, avvist, antatt godkjent)';
COMMENT ON COLUMN OPPTJENING_AKTIVITET.FOM IS 'Aktivitet gyldig fra-og-med dato';
COMMENT ON COLUMN OPPTJENING_AKTIVITET.FOM IS 'Aktivitet gyldig til-og-med dato';



-- legg til opptjeningsperiode på Opptjeningsperiode tabell og rename til OPPTJENING
alter table opptjeningsperiode add (opptjent_periode varchar2(50 char));
alter table opptjeningsperiode rename to opptjening;
alter table opptjening rename constraint FK_OPPTJENINGSPERIODE to FK_OPPTJENING_1;
alter table opptjening rename constraint PK_OPPTJENINGSPERIODE to PK_OPPTJENING;
ALTER INDEX IDX_OPPTJENINGSPERIODE_01 RENAME TO IDX_OPPTJENING_01;
ALTER INDEX PK_OPPTJENINGSPERIODE RENAME TO PK_OPPTJENING;
DROP SEQUENCE SEQ_OPPTJENINGSPERIODE;
CREATE SEQUENCE SEQ_OPPTJENING MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
alter table OPPTJENING add aktiv VARCHAR2(1 CHAR) DEFAULT 'N' NOT NULL;
COMMENT ON COLUMN OPPTJENING.AKTIV IS 'Angir hvilket Opptjening-innslag er aktivt for angitt vilkårresultat(vilkar_resultat_id).  Hvis flere, kan kun ett innslag være aktivt til enhver tid.';

CREATE UNIQUE INDEX UIDX_OPPTJENING_02
  ON OPPTJENING (
    (CASE WHEN AKTIV = 'J'
      THEN VILKAR_RESULTAT_ID
     ELSE NULL END),
    (CASE WHEN AKTIV = 'J'
      THEN AKTIV
     ELSE NULL END)
  );


-- øk størrelse for koder
ALTER TABLE VURDERINGSPUNKT_DEF MODIFY(KODE VARCHAR2(100 CHAR));
alter table aksjonspunkt_def modify(vurderingspunkt varchar2(100 char));
alter table aksjonspunkt modify(behandling_steg_funnet varchar2(100 char));
ALTER TABLE BEHANDLING_STEG_TYPE MODIFY(KODE VARCHAR2(100 CHAR));
ALTER TABLE VURDERINGSPUNKT_DEF MODIFY(BEHANDLING_STEG VARCHAR2(100 CHAR));
ALTER TABLE BEHANDLING_STEG_TILSTAND MODIFY(BEHANDLING_STEG VARCHAR2(100 CHAR));
ALTER TABLE BEHANDLING_TYPE_STEG_SEKV MODIFY(BEHANDLING_STEG_TYPE VARCHAR2(100 CHAR));

-- legg til nye steg for Vurder Opptjeningsperiode og Vurder Opptjening fakta
INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse)
VALUES ('VURDER_OPPTJ_FAKTA', 'Vurder Opptjening Fakta', 'UTRED', 'Vurder fakta for opptjening');

INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse)
VALUES ('VURDER_OPPTJ_PERIODE', 'Vurder Opptjening Periode', 'UTRED', 'Vurder opptjeningsperiode');

update behandling_type_steg_sekv set sekvens_nr = 82 where behandling_steg_type='VURDER_OPPTJ';

INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'VURDER_OPPTJ_PERIODE', 80, 'FP');
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'VURDER_OPPTJ_FAKTA', 81, 'FP');

INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'VURDER_OPPTJ_PERIODE', 80, 'FP');
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr, fagsak_ytelse_type)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-004', 'VURDER_OPPTJ_FAKTA', 81, 'FP');


INSERT INTO VURDERINGSPUNKT_DEF(KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN, BESKRIVELSE)
values ('VURDER_OPPTJ_FAKTA.INN', 'VURDER_OPPTJ_FAKTA', 'INN', 'Vurder Opptjening Fakta - Inngang', 'Vurderingspunkt ved inngang til Behandling Steg - Vurder Opptjening Fakta');

INSERT INTO VURDERINGSPUNKT_DEF(KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN, BESKRIVELSE)
VALUES ('VURDER_OPPTJ_FAKTA.UT', 'VURDER_OPPTJ_FAKTA', 'UT', 'Vurder Opptjening Fakta - Utgang', 'Vurderingspunkt ved utgang fra Behandling Steg - Vurder Opptjening Fakta');

INSERT INTO VURDERINGSPUNKT_DEF(KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN, BESKRIVELSE)
values ('VURDER_OPPTJ_PERIODE.INN', 'VURDER_OPPTJ_PERIODE', 'INN', 'Vurder Opptjeningsperiode - Inngang', 'Vurderingspunkt ved inngang til Behandling Steg - Vurder Opptjeningsperiode');

INSERT INTO VURDERINGSPUNKT_DEF(KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN, BESKRIVELSE)
VALUES ('VURDER_OPPTJ_PERIODE.UT', 'VURDER_OPPTJ_PERIODE', 'UT', 'Vurder Opptjeningsperiode - Utgang', 'Vurderingspunkt ved utgang fra Behandling Steg - Vurder Opptjeningsperiode');

  




