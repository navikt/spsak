--Nytt aksjonspunkt papir endringssøknad foreldrepenger
INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, SKJERMLENKE_TYPE, TILBAKEHOPP_VED_GJENOPPTAKELSE, AKSJONSPUNKT_TYPE)
VALUES ('7013', 'Venter på søknad', 'REGSØK.UT', 'Venter på mottak av manglende søknad', '-', 'N', '-', 'J', 'AUTO');

UPDATE AKSJONSPUNKT_DEF set TILBAKEHOPP_VED_GJENOPPTAKELSE = 'J' where KODE IN ('7008');


-- Ny verdi i BehandlingResultatType
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'HENLAGT_SØKNAD_MANGLER', 'Henlagt søknad mangler', 'Behandling er henlagt, søknad ikke mottatt', 'BEHANDLING_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

-- Lagt til RelasjonsRolleType på SøknadEntitet
alter table SOEKNAD add BRUKER_ROLLE varchar2(100 char) default '-' not null;
alter table SOEKNAD add KL_BRUKER_ROLLE varchar2(100 char) as ('RELASJONSROLLE_TYPE');
alter table SOEKNAD add constraint FK_SOEKNAD_1 foreign key (KL_BRUKER_ROLLE, BRUKER_ROLLE) references KODELISTE (KODEVERK, KODE);
create index IDX_SOEKNAD_11 on SOEKNAD(BRUKER_ROLLE);

COMMENT ON COLUMN SOEKNAD.bruker_rolle IS 'FK: RELASJONSROLLE_TYPE';
COMMENT ON COLUMN SOEKNAD.kl_bruker_rolle IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
