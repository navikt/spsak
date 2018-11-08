-- Konfigurasjonsverdier for brev i klagebehandling
insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('saksbehandling.frist.uker.klagebehandling', 'Saksbehandlingsfrist klagebehandling', 'INGEN', 'INTEGER', 'Frist for når saksbehandlingen i en klagebehandling skal være ferdig etter at klagen er mottatt');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'saksbehandling.frist.uker.klagebehandling', 'INGEN', '12', to_date('01.01.2016', 'dd.mm.yyyy'));

-- Nye kodeverk for klagebehandling, med kodeverdier
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('KLAGE_VURDERT_AV', 'N', 'N', 'Klage vurdert av', 'Hvilken type enhet har vurdert klagen');
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('KLAGEVURDERING', 'N', 'N', 'Klagevurdering', 'Resultat av klagevurderingen');
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('KLAGE_AVVIST_AARSAK', 'N', 'N', 'Klage avvist årsak', 'Årsak til at klagen er avvist');
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('KLAGE_MEDHOLD_AARSAK', 'N', 'N', 'Klage medhold årsak', 'Årsak til medhold til klagen');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'NFP', 'NAV Familie- og Pensjonsytelser', 'NAV Familie- og Pensjonsytelser', 'KLAGE_VURDERT_AV', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'NK', 'NAV Klageenhet', 'NAV Klageenhet', 'KLAGE_VURDERT_AV', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'OPPHEVE_YTELSESVEDTAK', 'Ytelsesvedtaket oppheves', 'Ytelsesvedtaket oppheves', 'KLAGEVURDERING', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'STADFESTE_YTELSESVEDTAK', 'Ytelsesvedtaket stadfestes', 'Ytelsesvedtaket stadfestes', 'KLAGEVURDERING', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'MEDHOLD_I_KLAGE', 'Medhold', 'Medhold', 'KLAGEVURDERING', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'AVVIS_KLAGE', 'Klagen avvises', 'Klagen avvises', 'KLAGEVURDERING', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'KLAGET_FOR_SENT', 'Bruker har klaget for sent', 'Bruker har klaget etter at klagefristen er utløpt', 'KLAGE_AVVIST_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'KLAGE_UGYLDIG', 'Klage er ugyldig', 'Klage er ugyldig', 'KLAGE_AVVIST_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'KLAGE_AVVIST_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'I000027', 'Klage', 'Klage', 'DOKUMENT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'NYE_OPPLYSNINGER', 'Nye opplysninger som oppfyller vilkår', 'Nye opplysninger som oppfyller vilkår', 'KLAGE_MEDHOLD_AARSAK', to_date('2017-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'ULIK_REGELVERKSTOLKNING', 'Ulik regelverkstolkning', 'Ulik regelverkstolkning', 'KLAGE_MEDHOLD_AARSAK', to_date('2017-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'ULIK_VURDERING', 'Ulik skjønnsmessig vurdering', 'Ulik skjønnsmessig vurdering', 'KLAGE_MEDHOLD_AARSAK', to_date('2017-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'PROSESSUELL_FEIL', 'Prosessuell feil', 'Prosessuell feil', 'KLAGE_MEDHOLD_AARSAK', to_date('2017-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'KLAGE_MEDHOLD_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

-- Nye BehandlingStegType for KLAGE
INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse)
VALUES ('KLAGENFP', 'Vurder Klage (NFP)', 'UTRED', 'Vurder Klage for NAV Familie og pensjon');

INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse)
VALUES ('KLAGENK', 'Vurder Klage (NK)', 'UTRED', 'Vurder Klage for NAV Klageinstans');

-- Nye BehandlingResultatType for KLAGE
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'KLAGE_AVVIST', 'Klage er avvist', 'Klage er avvist', 'BEHANDLING_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'KLAGE_MEDHOLD', 'Medhold', 'Bruker har fått medhold i klage', 'BEHANDLING_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'KLAGE_YTELSESVEDTAK_OPPHEVET', 'Ytelsesvedtak opphevet', 'Ytelsesvedtak er opphevet etter klagebehandling', 'BEHANDLING_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'KLAGE_YTELSESVEDTAK_STADFESTET', 'Ytelsesvedtak stadfestet', 'Ytelsesvedtak er stadfestet etter klagebehandling', 'BEHANDLING_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'HENLAGT_KLAGE_TRUKKET', 'Henlagt, klagen er trukket', 'Klagen er henlagt, trukket av bruker', 'BEHANDLING_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

-- Nye BehandlingTypeStegSekvens for KLAGE
Insert into BEHANDLING_TYPE_STEG_SEKV (ID,BEHANDLING_TYPE,BEHANDLING_STEG_TYPE,SEKVENS_NR) values (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval,'BT-003','KLAGENFP','1');
Insert into BEHANDLING_TYPE_STEG_SEKV (ID,BEHANDLING_TYPE,BEHANDLING_STEG_TYPE,SEKVENS_NR) values (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval,'BT-003','KLAGENK','2');
Insert into BEHANDLING_TYPE_STEG_SEKV (ID,BEHANDLING_TYPE,BEHANDLING_STEG_TYPE,SEKVENS_NR) values (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval,'BT-003','FORVEDSTEG','3');
Insert into BEHANDLING_TYPE_STEG_SEKV (ID,BEHANDLING_TYPE,BEHANDLING_STEG_TYPE,SEKVENS_NR) values (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval,'BT-003','FVEDSTEG','4');
Insert into BEHANDLING_TYPE_STEG_SEKV (ID,BEHANDLING_TYPE,BEHANDLING_STEG_TYPE,SEKVENS_NR) values (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval,'BT-003','IVEDSTEG','5');

-- Nye VurderingspunktDefinisjon for KLAGE
INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
VALUES ('KLAGENFP.UT', 'Klage (NFP) - Utgang', 'KLAGENFP', 'UT');
INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
VALUES ('KLAGENK.UT', 'Klage (NK) - Utgang', 'KLAGENK', 'UT');

-- Nye AksjonspunktDefinisjon for KLAGE
INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT)
VALUES ('5035', 'Manuell vurdering av klage (NFP)', 'KLAGENFP.UT', 'Vurder klage (NFP)', '-', 'J');

INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT)
VALUES ('5036', 'Manuell vurdering av klage (NK)', 'KLAGENK.UT', 'Vurder klage (NK)', '-', 'J');

-- Ny VedtakResultatType for KLAGE
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'VEDTAK_I_KLAGEBEHANDLING', 'vedtak i klagebehandling', 'vedtak i klagebehandling', 'VEDTAK_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

/*HISTORIKKINNSLAG*/
INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('KLAGE_BEH_NFP', 'Klage behandlet av NAV Familie og Pensjon');
INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('KLAGE_BEH_NK', 'Klage behandlet av NAV Klageinstans');

-- Ny tabell KlageVurderingResultat for KLAGE
CREATE TABLE KLAGE_VURDERING_RESULTAT (
    id                      NUMBER(19) NOT NULL,
    behandling_id           NUMBER(19) NOT NULL,
    klage_vurdert_av        VARCHAR2(100) NOT NULL,
    kl_klage_vurdert_av     VARCHAR2(100) AS ('KLAGE_VURDERT_AV'),
    klagevurdering          VARCHAR2(100) NOT NULL,
    kl_klagevurdering       VARCHAR2(100) AS ('KLAGEVURDERING'),
    begrunnelse             VARCHAR2(2000) NOT NULL,
    klage_avvist_aarsak     VARCHAR2(100),
    kl_klage_avvist_aarsak  VARCHAR2(100) AS ('KLAGE_AVVIST_AARSAK'),
    klage_medhold_aarsak    VARCHAR2(100),
    kl_klage_medhold_aarsak VARCHAR2(100) AS ('KLAGE_MEDHOLD_AARSAK'),
    opprettet_av            VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid           TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av               VARCHAR2(20 CHAR),
    endret_tid              TIMESTAMP(3),
    CONSTRAINT PK_KLAGE_VURDERING_RESULTAT PRIMARY KEY (id),
    CONSTRAINT FK_KLAGE_VURDERING_RESULTAT_1 FOREIGN KEY (behandling_id) REFERENCES BEHANDLING(id),
    CONSTRAINT FK_KLAGE_VURDERING_RESULTAT_2 FOREIGN KEY (kl_klagevurdering, klagevurdering) REFERENCES KODELISTE(kodeverk, kode),
    CONSTRAINT FK_KLAGE_VURDERING_RESULTAT_3 FOREIGN KEY (kl_klage_vurdert_av, klage_vurdert_av) REFERENCES KODELISTE(kodeverk, kode),
    CONSTRAINT FK_KLAGE_VURDERING_RESULTAT_4 FOREIGN KEY (kl_klage_avvist_aarsak, klage_avvist_aarsak) REFERENCES KODELISTE(kodeverk, kode),
    CONSTRAINT FK_KLAGE_VURDERING_RESULTAT_5 FOREIGN KEY (kl_klage_medhold_aarsak, klage_medhold_aarsak) REFERENCES KODELISTE(kodeverk, kode)
);

CREATE SEQUENCE SEQ_KLAGE_VURDERING_RESULTAT MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

-- Ny BehandlingÅrsak for KLAGE
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'ETTER_KLAGE', 'Ny behandling eller revurdering etter klage', 'Ny behandling eller revurdering etter klage', 'BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
