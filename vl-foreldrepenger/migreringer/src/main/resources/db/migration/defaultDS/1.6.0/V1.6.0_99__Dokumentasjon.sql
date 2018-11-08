

--ADOPSJON
COMMENT ON TABLE ADOPSJON IS 'Informasjon om en adopsjon.';
COMMENT ON COLUMN ADOPSJON.omsorgsovertakelse_dato IS 'Angir datoen for når man overtok omsorgen for barnet/barna.';
COMMENT ON COLUMN ADOPSJON.ektefelles_barn IS 'Angir om det er adopsjon av ektefelles barn.';
COMMENT ON COLUMN ADOPSJON.adopterer_alene IS 'Angir om man adopterer alene.';

--ADOPSJON_BARN
COMMENT ON TABLE ADOPSJON_BARN IS 'Informasjon om barnet som adopteres.';
COMMENT ON COLUMN ADOPSJON_BARN.foedselsdato IS 'Fødselsdatoen til barnet.';

--ADRESSE
COMMENT ON TABLE ADRESSE IS 'Inneholder adressen som et brev skal sendes til.';
COMMENT ON COLUMN ADRESSE.mottakernavn IS 'Navn på adressat.';
COMMENT ON COLUMN ADRESSE.adresselinje1 IS 'Adresse, kan f.eks være c/o, postboksnummer, eller gateadresse';
COMMENT ON COLUMN ADRESSE.adresselinje2 IS 'Adresse forts.';
COMMENT ON COLUMN ADRESSE.adresselinje3 IS 'Adresse forts.';
COMMENT ON COLUMN ADRESSE.post_nummer IS 'Postnummer';
COMMENT ON COLUMN ADRESSE.poststed IS 'Poststed';
COMMENT ON COLUMN ADRESSE.land IS 'Land';

--AKSJONSPUNKT
COMMENT ON TABLE AKSJONSPUNKT IS 'Aksjoner som en saksbehandler må utføre manuelt.';
COMMENT ON COLUMN AKSJONSPUNKT.behandling_steg_funnet IS 'Angir i hvilket behandlingssteg dette aksjonspunktet ble opprettet.';
COMMENT ON COLUMN AKSJONSPUNKT.periode_fom IS 'Angir starttidspunkt dersom aksjonspunktet gjelder en spesifikk periode. Brukes for aksjonspunkt som kan repteres flere ganger for en behandling.';
COMMENT ON COLUMN AKSJONSPUNKT.periode_tom IS 'Angir sluttidspunkt dersom aksjonspunktet gjelder en spesifikk periode.';
COMMENT ON COLUMN AKSJONSPUNKT.begrunnelse IS 'Begrunnelse for endringer gjort i forbindelse med aksjonspunktet.';

--AKSJONSPUNKT_DEF
COMMENT ON TABLE AKSJONSPUNKT_DEF IS 'Kodetabell som definerer de forskjellige typene aksjonspunkter.';

--AKSJONSPUNKT_KATEGORI
COMMENT ON TABLE AKSJONSPUNKT_KATEGORI IS 'Kodetabell som definerer kategorien aksjonspunktet faller under.';

--AKSJONSPUNKT_STATUS
COMMENT ON TABLE AKSJONSPUNKT_STATUS IS 'Kodetabell som definerer status på aksjonspunktet.';

--AKTOER_NORSK_IDENT_MAP
-- COMMENT ON TABLE AKTOER_NORSK_IDENT_MAP IS '';
-- COMMENT ON COLUMN AKTOER_NORSK_IDENT_MAP.aktoer_id IS '';
-- COMMENT ON COLUMN AKTOER_NORSK_IDENT_MAP.norsk_ident IS '';

--BARN
-- COMMENT ON TABLE BARN IS '';
-- COMMENT ON COLUMN BARN.aktoer_id IS '';
-- COMMENT ON COLUMN BARN.norsk_ident IS '';
-- COMMENT ON COLUMN BARN.foedseldato IS '';

--BEHANDLING
-- COMMENT ON TABLE BEHANDLING IS '';
-- COMMENT ON COLUMN BEHANDLING.opprettet_dato IS '';
-- COMMENT ON COLUMN BEHANDLING.avsluttet_dato IS '';

--BEHANDLING_GRUNNLAG
-- COMMENT ON TABLE BEHANDLING_GRUNNLAG IS '';

--BEHANDLING_RESULTAT
-- COMMENT ON TABLE BEHANDLING_RESULTAT IS '';

--BEHANDLING_STATUS
COMMENT ON TABLE BEHANDLING_STATUS IS 'Angir definerte statuser en behandling kan være i (faglig sett). Statusene er definert av Forretning og Fag';
COMMENT ON COLUMN BEHANDLING_STATUS.kode IS 'PK - angir unik kode som identifiserer en status';
COMMENT ON COLUMN BEHANDLING_STATUS.navn IS 'Et lesbart navn for status, ment for visning el.';
COMMENT ON COLUMN BEHANDLING_STATUS.beskrivelse IS 'Beskrivelse/forklaring av hva statusen innebærer for en behandling';

--BEHANDLING_STEG_TYPE
COMMENT ON TABLE BEHANDLING_STEG_TYPE IS 'Angir definerte behandlingsteg med hvilket status behandling skal stå i når steget kjøres';
COMMENT ON COLUMN BEHANDLING_STEG_TYPE.kode IS 'PK - angir unik kode som identifiserer behandlingssteget';
COMMENT ON COLUMN BEHANDLING_STEG_TYPE.navn IS 'Et lesbart navn for behandlingssteget, ment for visning el.';
COMMENT ON COLUMN BEHANDLING_STEG_TYPE.behandling_status_def IS 'Definert status behandling settes i når steget kjøres';
COMMENT ON COLUMN BEHANDLING_STEG_TYPE.beskrivelse IS 'Beskrivelse/forklaring av hva steget gjør';

--BEHANDLING_TYPE
-- COMMENT ON TABLE BEHANDLING_TYPE IS '';

--BEHANDLING_TYPE_STEG_SEKV
-- COMMENT ON TABLE BEHANDLING_TYPE_STEG_SEKV IS '';
-- COMMENT ON COLUMN BEHANDLING_TYPE_STEG_SEKV.sekvens_nr IS '';

--BEHANDLING_VEDTAK
-- COMMENT ON TABLE BEHANDLING_VEDTAK IS '';
-- COMMENT ON COLUMN BEHANDLING_VEDTAK.vedtaksdato IS '';
-- COMMENT ON COLUMN BEHANDLING_VEDTAK.ansvarlig_saksbehandler IS '';



--BEREGNING
-- COMMENT ON TABLE BEREGNING IS '';
-- COMMENT ON COLUMN BEREGNING.sats_verdi IS '';
-- COMMENT ON COLUMN BEREGNING.antall_barn IS '';
-- COMMENT ON COLUMN BEREGNING.beregnet_tilkjent_ytelse IS '';
-- COMMENT ON COLUMN BEREGNING.beregnet_dato IS '';

--BEREGNING_RESULTAT
-- COMMENT ON TABLE BEREGNING_RESULTAT IS '';


--BRUKER
-- COMMENT ON TABLE BRUKER IS '';
-- COMMENT ON COLUMN BRUKER.aktoer_id IS '';
-- COMMENT ON COLUMN BRUKER.foedseldato IS '';

--BRUKER_KJOENN
-- COMMENT ON TABLE BRUKER_KJOENN IS '';

--BRUKER_ROLLE_TYPE
-- COMMENT ON TABLE BRUKER_ROLLE_TYPE IS '';


--DOKUMENT_DATA
-- COMMENT ON TABLE DOKUMENT_DATA IS '';
-- COMMENT ON COLUMN DOKUMENT_DATA.journal_post_id IS '';
-- COMMENT ON COLUMN DOKUMENT_DATA.dokument_id IS '';
-- COMMENT ON COLUMN DOKUMENT_DATA.dokument_mal_navn IS '';
-- COMMENT ON COLUMN DOKUMENT_DATA.forhaandsvist_tid IS '';
-- COMMENT ON COLUMN DOKUMENT_DATA.sendt_tid IS '';

--DOKUMENT_FELLES
-- COMMENT ON TABLE DOKUMENT_FELLES IS '';
-- COMMENT ON COLUMN DOKUMENT_FELLES.saksnummer IS '';
-- COMMENT ON COLUMN DOKUMENT_FELLES.sign_saksbehandler_navn IS '';
-- COMMENT ON COLUMN DOKUMENT_FELLES.automatisk_behandlet IS '';
-- COMMENT ON COLUMN DOKUMENT_FELLES.sakspart_id IS '';
-- COMMENT ON COLUMN DOKUMENT_FELLES.sakspart_navn IS '';
-- COMMENT ON COLUMN DOKUMENT_FELLES.sign_beslutter_navn IS '';
-- COMMENT ON COLUMN DOKUMENT_FELLES.sign_beslutter_geo_enhet IS '';
-- COMMENT ON COLUMN DOKUMENT_FELLES.mottaker_id IS '';
-- COMMENT ON COLUMN DOKUMENT_FELLES.mottaker_navn IS '';
-- COMMENT ON COLUMN DOKUMENT_FELLES.navn_avsender_enhet IS '';
-- COMMENT ON COLUMN DOKUMENT_FELLES.nummer_avsender_enhet IS '';
-- COMMENT ON COLUMN DOKUMENT_FELLES.kontakt_telefon_nummer IS '';
-- COMMENT ON COLUMN DOKUMENT_FELLES.dokument_dato IS '';

--DOKUMENT_MAL_TYPE
COMMENT ON TABLE DOKUMENT_MAL_TYPE IS 'Angir definerte dokumentmaler som brukes ved brevproduksjon';
-- COMMENT ON COLUMN DOKUMENT_MAL_TYPE.DOKSYS_ID IS '';

--DOKUMENT_TYPE
COMMENT ON TABLE DOKUMENT_TYPE IS 'Typen til et mottatt dokument, eks: søknad, terminbekreftelse, adopsjonspapirer';

--DOKUMENT_TYPE_DATA
-- COMMENT ON TABLE DOKUMENT_TYPE_DATA IS '';

--FAGSAK
-- COMMENT ON TABLE FAGSAK IS '';

--FAGSAK_ARSAK_TYPE
-- COMMENT ON TABLE FAGSAK_ARSAK_TYPE IS '';

--FAGSAK_RELASJON
-- COMMENT ON TABLE FAGSAK_RELASJON IS '';

--FAGSAK_STATUS
-- COMMENT ON TABLE FAGSAK_STATUS IS '';

--FAGSAK_YTELSE_TYPE
-- COMMENT ON TABLE FAGSAK_YTELSE_TYPE IS '';

--FAR_SOEKER_TYPE
-- COMMENT ON TABLE FAR_SOEKER_TYPE IS '';

--FOEDSEL
-- COMMENT ON TABLE FOEDSEL IS '';
-- COMMENT ON COLUMN FOEDSEL.foedselsdato IS '';
-- COMMENT ON COLUMN FOEDSEL.antall_barn IS '';
-- COMMENT ON COLUMN FOEDSEL.antall_barn_tps_gjelder IS '';
-- COMMENT ON COLUMN FOEDSEL.kilde_saksbehandler IS '';
-- COMMENT ON COLUMN FOEDSEL.kilde_ref IS '';

--HISTORIKK_AKTOER
COMMENT ON TABLE HISTORIKK_AKTOER IS 'Angir definerte typer av aktører som kan opprette historikkinnslag';

--HISTORIKKINNSLAG
COMMENT ON TABLE HISTORIKKINNSLAG IS 'Historikk over hendelser i saken';
COMMENT ON COLUMN HISTORIKKINNSLAG.tekst IS 'Tekst som beskriver hendelsen (som skal vises i historikkfanen)';
COMMENT ON COLUMN HISTORIKKINNSLAG.bruker_id IS 'Referens til ekstern bruker ident som laget innslaget';

--HISTORIKKINNSLAG_DOK_LINK
COMMENT ON TABLE HISTORIKKINNSLAG_DOK_LINK IS 'Kobling fra historikkinnslag til aktuell dokumentasjon';
COMMENT ON COLUMN HISTORIKKINNSLAG_DOK_LINK.tag IS 'Tekst som vises for link til dokumentet';
COMMENT ON COLUMN HISTORIKKINNSLAG_DOK_LINK.journal_post_id IS 'Dokumentets Journal ID i JOARK';
COMMENT ON COLUMN HISTORIKKINNSLAG_DOK_LINK.dokument_id IS 'Dokumentets Dokument ID i JOARK';

--HISTORIKKINNSLAG_TYPE
COMMENT ON TABLE HISTORIKKINNSLAG_TYPE IS 'Angir definerte typer av historikkinnslag';

--INNGANGSVILKAR_RESULTAT
-- COMMENT ON TABLE INNGANGSVILKAR_RESULTAT IS '';

--INNSENDINGSVALG
COMMENT ON TABLE INNSENDINGSVALG IS 'Hvordan et vedlegg sendes inn, eks: LASTET_OPP, SEND_SENERE, osv';

--JOURNALDOKUMENT
-- COMMENT ON TABLE JOURNALDOKUMENT IS '';
COMMENT ON COLUMN JOURNALDOKUMENT.journalpost_id IS 'Dokumentets ID i JOARK';

--LAGRET_VEDTAK
-- COMMENT ON TABLE LAGRET_VEDTAK IS '';
-- COMMENT ON COLUMN LAGRET_VEDTAK.xml_clob IS '';

--LAGRET_VEDTAK_TYPE
COMMENT ON TABLE LAGRET_VEDTAK_TYPE IS 'Type av lagret vedtak, eks. FODSEL og ADOPSJON';

--LANDKODER
COMMENT ON TABLE LANDKODER IS 'Landkoder, eks: NO / NORGE';

--MOTTATT_STATUS
COMMENT ON TABLE MOTTATT_STATUS IS 'Status på mottatt dokument, eks: mottatt, etterspurt';

--MOTTATTE_DOKUMENT
COMMENT ON TABLE MOTTATTE_DOKUMENT IS 'Mottatt dokument som er lagret i Joark';
COMMENT ON COLUMN MOTTATTE_DOKUMENT.journal_post_id IS 'Joarks unike identifikator av journalposten (forsendelsenivå)';
COMMENT ON COLUMN MOTTATTE_DOKUMENT.dokument_id IS 'Joarks unike identifikator av DokumentInfo/Dokumentbeskrivelse (dokumentnivå)';
COMMENT ON COLUMN MOTTATTE_DOKUMENT.varientformat IS 'Angivelse av variant et dokument forekommer i, eks: produksjonsformat, arkivformat, sladdet dokument';

--OPPGAVE_BEHANDLING_KOBLING
-- COMMENT ON TABLE OPPGAVE_BEHANDLING_KOBLING IS '';
-- COMMENT ON COLUMN OPPGAVE_BEHANDLING_KOBLING.saks_id IS '';
-- COMMENT ON COLUMN OPPGAVE_BEHANDLING_KOBLING.ferdigstilt IS '';
-- COMMENT ON COLUMN OPPGAVE_BEHANDLING_KOBLING.ferdigstilt_av IS '';
-- COMMENT ON COLUMN OPPGAVE_BEHANDLING_KOBLING.ferdigstilt_tid IS '';

--OPPGAVE_AARSAK
-- COMMENT ON TABLE OPPGAVE_AARSAK IS '';

--POSTSTED
COMMENT ON TABLE POSTSTED IS 'Kodeverk over postnr og poststed';

--PROSESS_TASK
COMMENT ON TABLE PROSESS_TASK is 'Inneholder tasks som skal kjøres i bakgrunnen';
COMMENT ON COLUMN PROSESS_TASK.task_type is 'navn på task. Brukes til å matche riktig implementasjon';
COMMENT ON COLUMN PROSESS_TASK.prioritet is 'prioritet på task.  Høyere tall har høyere prioritet';
COMMENT ON COLUMN PROSESS_TASK.status is 'status på task: KLAR, NYTT_FORSOEK, FEILET, VENTER_SVAR, FERDIG';
COMMENT ON COLUMN PROSESS_TASK.neste_kjoering_etter is 'tasken skal ikke kjøeres før tidspunkt er passert';
COMMENT ON COLUMN PROSESS_TASK.feilede_forsoek is 'antall feilede forsøk';
COMMENT ON COLUMN PROSESS_TASK.siste_kjoering_ts is 'siste gang tasken ble forsøkt kjørt';
COMMENT ON COLUMN PROSESS_TASK.siste_kjoering_feil_kode is 'siste feilkode tasken fikk';
COMMENT ON COLUMN PROSESS_TASK.siste_kjoering_feil_tekst is 'siste feil tasken fikk';
COMMENT ON COLUMN PROSESS_TASK.siste_kjoering_server is 'navn på node som sist kjørte en task (server@pid)';
COMMENT ON COLUMN PROSESS_TASK.task_parametere is 'parametere angitt for en task';
COMMENT ON COLUMN PROSESS_TASK.task_payload is 'inputdata for en task';
COMMENT ON COLUMN PROSESS_TASK.task_sekvens is 'angir rekkefølge på task innenfor en gruppe ';
COMMENT ON COLUMN PROSESS_TASK.task_gruppe is 'angir en unik id som grupperer flere ';
COMMENT ON COLUMN PROSESS_TASK.versjon is 'angir versjon for optimistisk låsing';

--PROSESS_TASK_FEILHAND
-- COMMENT ON TABLE PROSESS_TASK_FEILHAND IS '';

--PROSESS_TASK_TYPE
-- COMMENT ON TABLE PROSESS_TASK_TYPE IS '';
-- COMMENT ON COLUMN PROSESS_TASK_TYPE.feil_maks_forsoek IS '';
-- COMMENT ON COLUMN PROSESS_TASK_TYPE.feil_sek_mellom_forsoek IS '';

--REGEL_MERKNAD
-- COMMENT ON TABLE REGEL_MERKNAD IS '';

--REGEL_MERKNAD_DEF
-- COMMENT ON TABLE REGEL_MERKNAD_DEF IS '';

--SAKSOPPLYSNING
-- COMMENT ON TABLE SAKSOPPLYSNING IS '';
-- COMMENT ON COLUMN SAKSOPPLYSNING.fagsak_id IS '';
-- COMMENT ON COLUMN SAKSOPPLYSNING.behandling_id IS '';
-- COMMENT ON COLUMN SAKSOPPLYSNING.ekstern_referanse IS '';

--SAKSOPPLYSNING_DOKUMENT
-- COMMENT ON TABLE SAKSOPPLYSNING_DOKUMENT IS '';
-- COMMENT ON COLUMN SAKSOPPLYSNING_DOKUMENT.dokument IS '';
COMMENT ON COLUMN SAKSOPPLYSNING_DOKUMENT.md5_hash_hex IS 'Dokumentets MD5-sum';

--SAKSOPPLYSNING_DOKUMENT_TYPE
COMMENT ON TABLE SAKSOPPLYSNING_DOKUMENT_TYPE IS 'Vilket format dokumentet har, eks. XML, JSON eller BINARY';

--SAKSOPPLYSNING_KILDE
-- COMMENT ON TABLE SAKSOPPLYSNING_KILDE IS '';

--SAKSOPPLYSNING_TYPE
-- COMMENT ON TABLE SAKSOPPLYSNING_TYPE IS '';

--SATS
COMMENT ON TABLE SATS IS 'Satser brukt ifm beregning av ytelser';

--SATS_TYPE
COMMENT ON TABLE SATS_TYPE IS 'Type av sats - eks. Engangsstoenad';

--SOEKNAD
-- COMMENT ON TABLE SOEKNAD IS '';
-- COMMENT ON COLUMN SOEKNAD.soeknadsdato IS '';
-- COMMENT ON COLUMN SOEKNAD.foedselsdato_fra_soeknad IS '';
-- COMMENT ON COLUMN SOEKNAD.utstedt_dato_terminbekreftelse IS '';
-- COMMENT ON COLUMN SOEKNAD.termindato_fra_soeknad IS '';
-- COMMENT ON COLUMN SOEKNAD.antall_barn_fra_soeknad IS '';
-- COMMENT ON COLUMN SOEKNAD.kilde_ref IS '';
-- COMMENT ON COLUMN SOEKNAD.tilleggsopplysninger IS '';
-- COMMENT ON COLUMN SOEKNAD.adop_omsorgover_dato IS '';
-- COMMENT ON COLUMN SOEKNAD.omsorg_omsorgover_dato IS '';
-- COMMENT ON COLUMN SOEKNAD.elektronisk_registrert IS '';
-- COMMENT ON COLUMN SOEKNAD.mottatt_dato IS '';

--SOEKNAD_ADOPSJON_BARN
-- COMMENT ON TABLE SOEKNAD_ADOPSJON_BARN IS '';
-- COMMENT ON COLUMN SOEKNAD_ADOPSJON_BARN.foedselsdato IS '';

--SOEKNAD_ANNEN_PART
COMMENT ON TABLE SOEKNAD_ANNEN_PART IS 'Opplysninger om den andre parten i søknaden';

--SOEKNAD_ANNEN_PART_TYPE
COMMENT ON TABLE SOEKNAD_ANNEN_PART_TYPE IS 'Type på den andre parten i søknaden, eks: mor, far, medmor, medfar';

--SOEKNAD_BARN
COMMENT ON TABLE SOEKNAD_BARN IS 'Fødselsdatoene på barna i en søknad';

--SOEKNAD_GJELDER_TYPE
-- COMMENT ON TABLE SOEKNAD_GJELDER_TYPE IS '';

--SOEKNAD_TYPE
-- COMMENT ON TABLE SOEKNAD_TYPE IS '';

--SPRAAK_KODE
-- COMMENT ON TABLE SPRAAK_KODE IS '';

--TERMINBEKREFTELSE
-- COMMENT ON TABLE TERMINBEKREFTELSE IS '';
-- COMMENT ON COLUMN TERMINBEKREFTELSE.termindato IS '';
-- COMMENT ON COLUMN TERMINBEKREFTELSE.utstedt_dato IS '';
-- COMMENT ON COLUMN TERMINBEKREFTELSE.antall_barn IS '';
-- COMMENT ON COLUMN TERMINBEKREFTELSE.kilde_ref IS '';

--TILKNYTNING_NORGE
COMMENT ON TABLE TILKNYTNING_NORGE IS 'Informasjon om søkers tilknytning til Norge';
COMMENT ON COLUMN TILKNYTNING_NORGE.opphold_norge_naa IS 'Nåværende opphold i Norge';

--UTLANDSOPPHOLD
COMMENT ON TABLE UTLANDSOPPHOLD IS 'Informasjon om utenlandsopphold';
COMMENT ON COLUMN UTLANDSOPPHOLD.periode_startdato IS 'Startdato for utenlandsopphold';
COMMENT ON COLUMN UTLANDSOPPHOLD.periode_sluttdato IS 'Sluttdato for utenlandsopphold';

--VEDLEGG
COMMENT ON TABLE VEDLEGG IS 'Vedlegg til søknad, eks: terminbekreftelse';
COMMENT ON COLUMN VEDLEGG.skjemanummer IS 'Skjemanummer på vedlegget';
COMMENT ON COLUMN VEDLEGG.tilleggsinfo IS 'Fritekst relatert til vedlegg';
COMMENT ON COLUMN VEDLEGG.er_paakrevd_i_soeknadsdialog IS 'Om vedlegget er påkrevd';

--VEDTAK_RESULTAT_TYPE
-- COMMENT ON TABLE VEDTAK_RESULTAT_TYPE IS '';

--VILKAR
-- COMMENT ON TABLE VILKAR IS '';

--VILKAR_RESULTAT_TYPE
-- COMMENT ON TABLE VILKAR_RESULTAT_TYPE IS '';

--VILKAR_TYPE
-- COMMENT ON TABLE VILKAR_TYPE IS '';

--VILKAR_UTFALL_MERKNAD
-- COMMENT ON TABLE VILKAR_UTFALL_MERKNAD IS '';

--VILKAR_UTFALL_TYPE
-- COMMENT ON TABLE VILKAR_UTFALL_TYPE IS '';

--VURDERINGSPUNKT_DEF
-- COMMENT ON TABLE VURDERINGSPUNKT_DEF IS '';
-- COMMENT ON COLUMN VURDERINGSPUNKT_DEF.vurderingspunkt_type IS '';


