-- RELATERTE_YTELSER_STATUS
comment on table RELATERTE_YTELSER_STATUS is 'Internt kodeverk som viser status for innhenting av relaterte ytelser fra Infotrygd.';
-- BEHANDLING_GRUNNLAG
comment on column BEHANDLING_GRUNNLAG.YTELSER_INFOTRYGD_STATUS is 'Status for innhenting av relaterte ytelser fra Infotrygd.';
-- BEHANDLING_REL_YTELSER
comment on column BEHANDLING_REL_YTELSER.ID is 'Primary Key';
comment on column BEHANDLING_REL_YTELSER.BEHANDLING_ID is 'FK: BEHANDLING';
comment on column BEHANDLING_REL_YTELSER.SOEKER is 'Angir om ytelsen gjelder bruker (Y) eller annen forelder (N)';
comment on column BEHANDLING_REL_YTELSER.RELATERT_YTELSE_TYPE is 'Ytelse type';
comment on column BEHANDLING_REL_YTELSER.RELATERT_YTELSE_TILSTAND is 'Ytelse tilstand';
comment on column BEHANDLING_REL_YTELSER.SAKSOPPLYSNING_KILDE is 'Angir kildesystem (fagsystemkode)';
comment on column BEHANDLING_REL_YTELSER.SAKSNUMMER is 'Saksnummer i kildesystemet';
comment on column BEHANDLING_REL_YTELSER.REGISTRERINGSDATO is 'Registreringsdato i kildesystemet';
comment on column BEHANDLING_REL_YTELSER.RELATERT_YTELSE_TEMA is 'Kildesystemets kode for tema';
comment on column BEHANDLING_REL_YTELSER.RELATERT_YTELSE_BEHANDL_TEMA is 'Kildesystemets kode for behandlingstema';
comment on column BEHANDLING_REL_YTELSER.RELATERT_YTELSE_SAKSTYPE is 'Kildesystemets kode for sakstype';
comment on column BEHANDLING_REL_YTELSER.RELATERT_YTELSE_STATUS is 'Kildesystemets kode for status';
comment on column BEHANDLING_REL_YTELSER.RELATERT_YTELSE_RESULTAT is 'Kildesystemets kode for resultat';
comment on column BEHANDLING_REL_YTELSER.VEDTAKDATO is 'Kildesystemets vedtaksdato';
comment on column BEHANDLING_REL_YTELSER.IVERKSETTELSESDATO is 'Kildesystemets iverksettelsesdato';
comment on column BEHANDLING_REL_YTELSER.OPPHOERFOMDATO is 'Kildesystemets opphørsdato (opphørt fra og med)';
-- BEHANDLING_RESULTAT
comment on column BEHANDLING_RESULTAT.BEHANDLING_RESULTAT_TYPE is 'Resultat av behandlingen';
-- BEHANDLING_RESULTAT_TYPE
comment on column BEHANDLING_RESULTAT_TYPE.KODE is 'Kodeverk Primary Key';
-- BEHANDLING_VEDTAK
comment on column BEHANDLING_VEDTAK.IVERKSETTING_STATUS is 'Status for iverksettingssteget';
-- DOKUMENT_MAL_RESTRIKSJON
comment on column DOKUMENT_MAL_RESTRIKSJON.KODE is 'Kodeverk Primary Key';
-- DOKUMENT_MAL_TYPE
comment on column DOKUMENT_MAL_TYPE.GENERISK is 'Kan dokumentmalen benyttes for informasjonsinnhenting i GUI (Y) eller ikke (N)';
comment on column DOKUMENT_MAL_TYPE.DOKUMENT_MAL_RESTRIKSJON is 'Restriksjonskode som begrenser bruken av dokumentmalen, eller INGEN';
-- IVERKSETTING_STATUS
comment on column IVERKSETTING_STATUS.KODE is 'Kodeverk Primary Key';
-- OKO_OPPDRAG_110
comment on column OKO_OPPDRAG_110.DATO_OPPDRAG_GJELDER_FOM is 'Dato oppdraget gjelder fra og med';
-- OKO_OPPDRAG_LINJE_150
comment on column OKO_OPPDRAG_LINJE_150.HENVISNING is 'Behandlingsid, brukes ved avstemming';
-- OPPDRAG_KONTROLL
comment on column OPPDRAG_KONTROLL.MELDING_KODE is 'Meldingkode fra oppdragskvittering';
comment on column OPPDRAG_KONTROLL.BEHANDLING_ID is 'FK: BEHANDLING';
comment on column OPPDRAG_KONTROLL.PROSESS_TASK_ID is 'FK: PROSESS_TASK';
comment on column OPPDRAG_KONTROLL.ALVORLIGHETSGRAD is 'Alvorlighetsgrad fra oppdragskvittering';
comment on column OPPDRAG_KONTROLL.BESKR_MELDING is 'Meldingsbeskrivelse fra oppdragskvittering';
-- RELATERTE_YTELSER_STATUS
comment on column RELATERTE_YTELSER_STATUS.KODE is 'Kodeverk Primary Key';
-- RELATERT_YTELSE_BEHANDLTEMA
comment on column RELATERT_YTELSE_BEHANDLTEMA.KODE is 'Kodeverk Primary Key';
-- RELATERT_YTELSE_RESULTAT
comment on column RELATERT_YTELSE_RESULTAT.KODE is 'Kodeverk Primary Key';
-- RELATERT_YTELSE_SAKSTYPE
comment on column RELATERT_YTELSE_SAKSTYPE.KODE is 'Kodeverk Primary Key';
-- RELATERT_YTELSE_STATUS
comment on column RELATERT_YTELSE_STATUS.KODE is 'Kodeverk Primary Key';
-- RELATERT_YTELSE_TEMA
comment on column RELATERT_YTELSE_TEMA.KODE is 'Kodeverk Primary Key';
-- RELATERT_YTELSE_TILSTAND
comment on column RELATERT_YTELSE_TILSTAND.KODE is 'Kodeverk Primary Key';
-- RELATERT_YTELSE_TYPE
comment on column RELATERT_YTELSE_TYPE.KODE is 'Kodeverk Primary Key';
