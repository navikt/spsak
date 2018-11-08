-- AKTOER_NORSK_IDENT_MAP
comment on table AKTOER_NORSK_IDENT_MAP is 'Mellomlagring av mapping mellom aktørId og fødselnummer. Skal brukes som fallback når oppslag mot aktør tjenesten feiler.';
comment on column AKTOER_NORSK_IDENT_MAP.AKTOER_ID is 'Aktør ID';
comment on column AKTOER_NORSK_IDENT_MAP.NORSK_IDENT is 'Fødselsnummer eller D-nummer.';
-- BEHANDLING
comment on column BEHANDLING.OPPRETTET_DATO is 'Dato når behandlingen ble opprettet.';
comment on column BEHANDLING.AVSLUTTET_DATO is 'Dato når behandlingen ble avsluttet.';
-- BEHANDLING_REL_YTELSER
comment on table BEHANDLING_REL_YTELSER is 'Relaterte ytelser for en behandling.';
-- BEHANDLING_RESULTAT
comment on table BEHANDLING_RESULTAT is 'Aggregert behandlingsresultat som settes basert på vilkårene.';
-- BEHANDLING_RESULTAT_TYPE
comment on table BEHANDLING_RESULTAT_TYPE is 'Internt kodeverk for behandlingsresultat utfall.';
-- BEHANDLING_TYPE
comment on table BEHANDLING_TYPE is 'Internt kodeverk for behandlingstyper.';
-- BEHANDLING_VEDTAK
comment on table BEHANDLING_VEDTAK is 'Vedtak koblet til en behandling via et behandlingsresultat.';
comment on column BEHANDLING_VEDTAK.VEDTAKSDATO is 'Vedtaksdato.';
comment on column BEHANDLING_VEDTAK.ANSVARLIG_SAKSBEHANDLER is 'Ansvarlig saksbehandler som godkjente vedtaket.';
-- BEREGNING
comment on table BEREGNING is 'Beregnet engangsstønad.';
comment on column BEREGNING.SATS_VERDI is 'Engangsstønad sats som ble brukt til beregning.';
comment on column BEREGNING.ANTALL_BARN is 'Antall barn det blir beregnet stønad for.';
comment on column BEREGNING.BEREGNET_TILKJENT_YTELSE is 'Beregnet tilkjent ytelse.';
comment on column BEREGNING.BEREGNET_DATO is 'Når beregning ble utført.';
-- BEHANDLING_RESULTAT
comment on table BEHANDLING_RESULTAT is 'Beregningsresultat. Knytter sammen beregning og behandling.';
-- BRUKER
comment on table BRUKER is 'Bruker som saken gjelder.';
comment on column BRUKER.AKTOER_ID is 'AktørId for bruker.';
comment on column BRUKER.FOEDSELDATO is 'Brukers fødselsdato.';
-- BRUKER_KJOENN
comment on table BRUKER_KJOENN is 'Internt kodeverk for kjønn.';
-- BRUKER_ROLLE_TYPE
comment on table BRUKER_ROLLE_TYPE is 'Internt kodeverk for brukers rolle(mor/far).';
-- DOKUMENT_DATA
comment on table DOKUMENT_DATA is 'Dokumentdata innholder informasjon om et brev som skal bestilles, eller som er bestilt.';
comment on column DOKUMENT_DATA.JOURNAL_POST_ID is 'Referanse til journalposten i Joark.';
comment on column DOKUMENT_DATA.DOKUMENT_ID is 'Referanse til dokumentet i Joark.';
comment on column DOKUMENT_DATA.DOKUMENT_MAL_NAVN is 'Navn på mal som brevet er lages fra.';
comment on column DOKUMENT_DATA.FORHAANDSVIST_TID is 'Tidspunkt for forhåndsvisning.';
comment on column DOKUMENT_DATA.SENDT_TID is 'Tidspunkt for oversendelse til brevløsningen.';
-- FAGSAK
comment on table FAGSAK is 'Fagsak for engangsstønad og foreldrepenger. Alle behandling er koblet mot en fagsak.';
comment on column FAGSAK.SAKSNUMMER is 'Saksnummer.';
-- FAGSAK_ARSAK_TYPE
comment on table FAGSAK_ARSAK_TYPE is 'Internt kodeverk for årsak til at fagsak ble opprettet.';
-- FAGSAK_RELASJON
comment on table FAGSAK_RELASJON is 'Fagsakrelasjon knyttet fagsaker sammen. F.eks. mor og fars sak.';
-- FAGSAK_STATUS
comment on table FAGSAK_STATUS is 'Internt kodeverk for statuser på fagsaker.';
-- FAGSAK_YTELSE_TYPE
comment on table FAGSAK_YTELSE_TYPE is 'Internt kodeverk for ytelsestype.';
-- FAR_SOEKER_TYPE
comment on table FAR_SOEKER_TYPE is 'Internt kodeverk for grunner til at far søker.';
-- FOEDSEL
comment on table FOEDSEL is 'Fødsel inneholder bekreftet informasjon angående en fødsel.';
comment on column FOEDSEL.FOEDSELSDATO is 'Fødselsdato for barn.';
comment on column FOEDSEL.ANTALL_BARN is 'Antall barn som ble født.';
comment on column FOEDSEL.ANTALL_BARN_TPS_GJELDER is '"J" dersom informasjon fra TPS gjelder, ellers "N".';
comment on column FOEDSEL.KILDE_SAKSBEHANDLER is '"J" dersom saksbehandler er kilden, ellers "N".';
comment on column FOEDSEL.KILDE_REF is 'Eventuell referanse til kilde.';
-- INNGANGSVILKAR_RESULTAT
comment on table INNGANGSVILKAR_RESULTAT is 'En samling av inngangsvilkår resultat.';
-- IVERKSETTING_STATUS
comment on table IVERKSETTING_STATUS is 'Internt kodeverk for status på iverksetting.';
-- LAGRET_VEDTAK
comment on table LAGRET_VEDTAK is 'Vedtakslager.';
comment on column LAGRET_VEDTAK.FAGSAK_ID is 'FK: kobling til fagsak.';
comment on column LAGRET_VEDTAK.BEHANDLING_ID is 'FK: kobling til behandling.';
comment on column LAGRET_VEDTAK.XML_CLOB is 'Vedtak XML.';
comment on column LAGRET_VEDTAK.LAGRET_VEDTAK_TYPE is 'Fødsel eller adopsjon.';
-- OPPGAVE_AARSAK
comment on table OPPGAVE_AARSAK is 'Internt kodeverk for årsaker til at oppgave i GSAK ble opprettet.';
-- OPPGAVE_BEHANDLING_KOBLING
comment on table OPPGAVE_BEHANDLING_KOBLING is 'Kobling mellom opprettede oppgaver i GSAK og behandlinger.';
comment on column OPPGAVE_BEHANDLING_KOBLING.OPPGAVE_ID is 'Oppgave id i GSAK.';
comment on column OPPGAVE_BEHANDLING_KOBLING.SAKS_ID is 'Saks id i GSAK.';
comment on column OPPGAVE_BEHANDLING_KOBLING.FERDIGSTILT is 'Er oppgaven ferdigstilt.';
comment on column OPPGAVE_BEHANDLING_KOBLING.FERDIGSTILT_AV is 'Ident til den som har ferdigstilt oppgaven.';
comment on column OPPGAVE_BEHANDLING_KOBLING.FERDIGSTILT_TID is 'Tidspunkt for når oppgave ble ferdigstilt.';
-- RELATERT_YTELSE_STATUS
comment on table RELATERT_YTELSE_STATUS is 'Kodeverk for status på relaterte ytelser.';
-- RELATERT_YTELSE_BEHANDLTEMA
comment on table RELATERT_YTELSE_BEHANDLTEMA is 'Kodeverk for behandlingstema på relaterte ytelser';
-- RELATERT_YTELSE_RESULTAT
comment on table RELATERT_YTELSE_RESULTAT is 'Kodeverk for resultat på relaterte ytelser.';
-- RELATERT_YTELSE_SAKSTYPE
comment on table RELATERT_YTELSE_SAKSTYPE is 'Kodeverk for sakstype på relaterte ytelser.';
-- RELATERT_YTELSE_TEMA
comment on table RELATERT_YTELSE_TEMA is 'Kodeverk for tema på relaterte ytelser.';
-- RELATERT_YTELSE_TILSTAND
comment on table RELATERT_YTELSE_TILSTAND is 'Kodeverk for tilstand på relaterte ytelser.';
-- RELATERT_YTELSE_TYPE
comment on table RELATERT_YTELSE_TYPE is 'Kodeverk for type på relaterte ytelser.';
-- SAKSOPPLYSNING
comment on table SAKSOPPLYSNING is 'Saksopplysningslager.';
comment on column SAKSOPPLYSNING.FAGSAK_ID is 'FK: Kobling til fagsak.';
comment on column SAKSOPPLYSNING.BEHANDLING_ID is 'FK: Kobling til behandling.';
comment on column SAKSOPPLYSNING.EKSTERN_REFERANSE is 'Ekstern referanse.';
-- SAKSOPPLYSNING_DOKUMENT
comment on table SAKSOPPLYSNING_DOKUMENT is 'Saksopplysnings dokumentet med selve innholdet.';
comment on column SAKSOPPLYSNING_DOKUMENT.DOKUMENT is 'Saksopplysning rå-data. Typisk: XML-respons.';
-- SAKSOPPLYSNING_KILDE
comment on table SAKSOPPLYSNING_KILDE is 'Internt kodeverk for saksopplysning kilder. F.eks: TPS, JOARK osv.';
-- SAKSOPPLYSNING_METADATA
comment on table SAKSOPPLYSNING_METADATA is 'Saksopplysning metadata.';
-- SAKSOPPLYSNING_TYPE
comment on table SAKSOPPLYSNING_TYPE is 'Internt kodeverk for type saksopplysninger.';
-- SATS
comment on column SATS.VERDI is 'Sats verdi.';
-- SPRAAK_KODE
comment on table SPRAAK_KODE is 'Internt kodeverk for språk.';
-- VEDTAK_RESULTAT_TYPE
comment on table VEDTAK_RESULTAT_TYPE is 'Internt kodeverk for vedtak resultat. (INNVILGET/AVSLAG)';
-- VILKAR
comment on table VILKAR is 'Vilkår som inneholder utfallet for en gitt vilkårstype.';
-- VILKAR_RESULTAT_TYPE
comment on table VILKAR_RESULTAT_TYPE is 'Internt kodeverk for vilkårsresultattype.';
-- VILKAR_TYPE
comment on table VILKAR_TYPE is 'Internt koderverk for vilkårstype.';
-- VILKAR_UTFALL_MERKNAD
comment on table VILKAR_UTFALL_MERKNAD is 'Merknader fra regler ifm et utfall på vilkår.';
-- VILKAR_UTFALL_TYPE
comment on table VILKAR_UTFALL_TYPE is 'Internt kodeverk for vilkår utfall type.';
-- VURDERINGSPUNKT_DEF
comment on table VURDERINGSPUNKT_DEF is 'Internt kodeverk for definisjoner av vurderingspunkt.';
comment on column VURDERINGSPUNKT_DEF.VURDERINGSPUNKT_TYPE is 'Angir om det er et inngående eller utgående vurderingspunkt. Verdier: "INN" eller "UT".';
