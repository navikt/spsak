insert into kodeverk (kode, navn, beskrivelse ) values ('AKSJONSPUNKT_KATEGORI', 'AksjonspunktKategori', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('AKSJONSPUNKT_STATUS', 'AksjonspunktStatus', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('AKSJONSPUNKT_TYPE', 'AksjonspunktType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('BEHANDLING_AARSAK', 'BehandlingÅrsakType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('BEHANDLING_RESULTAT_TYPE', 'BehandlingResultatType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('BEHANDLING_STATUS', 'BehandlingStatus', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('BEHANDLING_STEG_STATUS', 'BehandlingStegStatus', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('BRUKER_KJOENN', 'NavBrukerKjønn', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('BRUKER_ROLLE_TYPE', 'NavBrukerFamilierelasjon', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('DOKUMENT_TYPE', 'DokumentType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('DOKUMENT_MAL_RESTRIKSJON', 'DokumentMalRestriksjon', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('FAGSAK_ARSAK', 'FagsakÅrsakType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('FAGSAK_STATUS', 'FagsakStatus', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('FAGSAK_YTELSE', 'FagsakYtelseType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('FAR_SOEKER_TYPE', 'FarSøkerType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('FORELDRE_TYPE', 'ForeldreType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('HISTORIKK_AKTOER', 'HistorikkAktør', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('INNSENDINGSVALG', 'Innsendingsvalg', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('IVERKSETTING_STATUS', 'IverksettingStatus', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('KONFIG_VERDI_TYPE', 'KonfigVerdiType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('KONFIG_VERDI_GRUPPE', 'KonfigVerdiGruppe', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('LAGRET_VEDTAK_TYPE', 'LagretVedtakType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('LANDKODER', 'Landkoder', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('MEDLEMSKAP_TYPE', 'MedlemskapType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('MEDLEMSKAP_DEKNING', 'MedlemskapDekningType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('MEDLEMSKAP_KILDE', 'MedlemskapKildeType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('MEDLEMSKAP_MANUELL_VURD', 'MedlemskapManuellVurderingType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('OMSORGSOVERTAKELSE_VILKAR', 'OmsorgsovertakelseVilkårType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('OPPGAVE_AARSAK', 'OppgaveÅrsak', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('PERIODE_TYPE', 'PeriodeType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('PERSONSTATUS_TYPE', 'PersonstatusType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('POSTSTED', 'Poststed', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('REGION', 'Region', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('RELATERTE_YTELSER_STATUS', 'RelaterteYtelserStatus', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('RELATERT_YTELSE_TYPE', 'RelatertYtelseType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('RELATERT_YTELSE_RESULTAT', 'RelatertYtelseResultat', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('RELATERT_YTELSE_SAKSTYPE', 'RelatertYtelseSakstype', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('RELATERT_YTELSE_TILSTAND', 'RelatertYtelseTilstand', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('SATS_TYPE', 'SatsType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('SOEKNAD_ANNEN_PART', 'SøknadAnnenPartType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('SPRAAK_KODE', 'Språkkode', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('VEDTAK_RESULTAT_TYPE', 'VedtakResultatType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('VILKAR_RESULTAT_TYPE', 'VilkarResultatType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('VILKAR_UTFALL_TYPE', 'VilkarUtfallType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('VILKAR_UTFALL_MERKNAD', 'VilkarUtfallMerknad', '');

insert into kodeverk (kode, navn, beskrivelse ) values ('DISKRESJONSKODE', 'Diskresjonskode', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('OPPLYSNING_ADRESSE_TYPE', 'OpplysningAdresseType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('OPPLYSNINGSKILDE', 'OpplysningsKilde', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('RELASJONSROLLE_TYPE', 'RelasjonsRolleType', '');
insert into kodeverk (kode, navn, beskrivelse ) values ('SIVILSTAND_TYPE', 'SivilstandType', '');

--------------------------






insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'AKSJONSPUNKT_KATEGORI' from AKSJONSPUNKT_KATEGORI;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'AKSJONSPUNKT_STATUS' from AKSJONSPUNKT_STATUS;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'AKSJONSPUNKT_TYPE' from AKSJONSPUNKT_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'BEHANDLING_AARSAK' from BEHANDLING_AARSAK_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'BEHANDLING_RESULTAT_TYPE' from BEHANDLING_RESULTAT_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'BEHANDLING_STATUS' from BEHANDLING_STATUS;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'BEHANDLING_STEG_STATUS' from BEHANDLING_STEG_STATUS_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'BRUKER_KJOENN' from BRUKER_KJOENN;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'BRUKER_ROLLE_TYPE' from BRUKER_ROLLE_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'DOKUMENT_TYPE' from DOKUMENT_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'DOKUMENT_MAL_RESTRIKSJON' from DOKUMENT_MAL_RESTRIKSJON;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'FAGSAK_ARSAK' from FAGSAK_ARSAK_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'FAGSAK_STATUS' from FAGSAK_STATUS;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'FAGSAK_YTELSE' from FAGSAK_YTELSE_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'FAR_SOEKER_TYPE' from FAR_SOEKER_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'FORELDRE_TYPE' from FORELDRE_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_AKTOER' from HISTORIKK_AKTOER;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'INNSENDINGSVALG' from INNSENDINGSVALG;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'IVERKSETTING_STATUS' from IVERKSETTING_STATUS;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'KONFIG_VERDI_TYPE' from KONFIG_VERDI_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'KONFIG_VERDI_GRUPPE' from KONFIG_VERDI_GRUPPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'LAGRET_VEDTAK_TYPE' from LAGRET_VEDTAK_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'LANDKODER' from LANDKODER;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'MEDLEMSKAP_TYPE' from MEDLEMSKAP_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'MEDLEMSKAP_DEKNING' from MEDLEMSKAP_DEKNING_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'MEDLEMSKAP_KILDE' from MEDLEMSKAP_KILDE_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'MEDLEMSKAP_MANUELL_VURD' from MEDLEMSKAP_MANUELL_VURD_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'OMSORGSOVERTAKELSE_VILKAR' from OMSORGSOVERTAKELSE_VILKAR_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'OPPGAVE_AARSAK' from OPPGAVE_AARSAK;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'PERIODE_TYPE' from PERIODE_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'PERSONSTATUS_TYPE' from PERSONSTATUS_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'POSTSTED' from POSTSTED;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'REGION' from REGION;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'RELATERTE_YTELSER_STATUS' from RELATERTE_YTELSER_STATUS;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'RELATERT_YTELSE_TYPE' from RELATERT_YTELSE_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'RELATERT_YTELSE_RESULTAT' from RELATERT_YTELSE_RESULTAT;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'RELATERT_YTELSE_SAKSTYPE' from RELATERT_YTELSE_SAKSTYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'RELATERT_YTELSE_TILSTAND' from RELATERT_YTELSE_TILSTAND;

insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'SATS_TYPE' from SATS_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'SOEKNAD_ANNEN_PART' from SOEKNAD_ANNEN_PART_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'SPRAAK_KODE' from SPRAAK_KODE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'VEDTAK_RESULTAT_TYPE' from VEDTAK_RESULTAT_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'VILKAR_RESULTAT_TYPE' from VILKAR_RESULTAT_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'VILKAR_UTFALL_TYPE' from VILKAR_UTFALL_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'VILKAR_UTFALL_MERKNAD' from VILKAR_UTFALL_MERKNAD;

insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'DISKRESJONSKODE' from DISKRESJONSKODE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'OPPLYSNING_ADRESSE_TYPE' from OPPLYSNING_ADRESSE_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'OPPLYSNINGSKILDE' from OPPLYSNINGSKILDE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'RELASJONSROLLE_TYPE' from RELASJONSROLLE_TYPE;
insert into KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) select seq_kodeliste.nextval, kode, navn, beskrivelse, to_date('2000-01-01', 'YYYY-MM-DD'), 'SIVILSTAND_TYPE' from SIVILSTAND_TYPE;

--------------------------

begin 
    migrer_KODELISTE_fk('AKSJONSPUNKT_STATUS', 'AKSJONSPUNKT_STATUS');
end;
/

begin 
    migrer_KODELISTE_fk('LAGRET_VEDTAK_TYPE', 'LAGRET_VEDTAK_TYPE');
end;
/

begin 
    migrer_KODELISTE_fk('INNSENDINGSVALG', 'INNSENDINGSVALG');
end;
/

begin 
    migrer_KODELISTE_fk('RELATERTE_YTELSER_STATUS', 'RELATERTE_YTELSER_STATUS');
end;
/

begin 
    migrer_KODELISTE_fk('LANDKODER', 'LANDKODER');
end;
/

begin 
    migrer_KODELISTE_fk('VILKAR_UTFALL_MERKNAD', 'VILKAR_UTFALL_MERKNAD');
end;
/

begin 
    migrer_KODELISTE_fk('BEHANDLING_STATUS', 'BEHANDLING_STATUS');
end;
/

begin 
    migrer_KODELISTE_fk('MEDLEMSKAP_MANUELL_VURD_TYPE', 'MEDLEMSKAP_MANUELL_VURD');
end;
/

begin 
    migrer_KODELISTE_fk('REGION', 'REGION');
end;
/

begin 
    migrer_KODELISTE_fk('AKSJONSPUNKT_KATEGORI', 'AKSJONSPUNKT_KATEGORI');
end;
/

begin 
    migrer_KODELISTE_fk('MEDLEMSKAP_DEKNING_TYPE', 'MEDLEMSKAP_DEKNING');
end;
/

begin 
    migrer_KODELISTE_fk('MEDLEMSKAP_TYPE', 'MEDLEMSKAP_TYPE');
end;
/

begin 
    migrer_KODELISTE_fk('BEHANDLING_STEG_STATUS_TYPE', 'BEHANDLING_STEG_STATUS');
end;
/

begin 
    migrer_KODELISTE_fk('VILKAR_RESULTAT_TYPE', 'VILKAR_RESULTAT_TYPE');
end;
/

begin 
    migrer_KODELISTE_fk('IVERKSETTING_STATUS', 'IVERKSETTING_STATUS');
end;
/

begin 
    migrer_KODELISTE_fk('RELATERT_YTELSE_TYPE', 'RELATERT_YTELSE_TYPE');
end;
/


begin 
    migrer_KODELISTE_fk('POSTSTED', 'POSTSTED');
end;
/

begin 
    migrer_KODELISTE_fk('HISTORIKK_AKTOER', 'HISTORIKK_AKTOER');
end;
/

begin 
    migrer_KODELISTE_fk('DOKUMENT_MAL_RESTRIKSJON', 'DOKUMENT_MAL_RESTRIKSJON');
end;
/

begin 
    migrer_KODELISTE_fk('SOEKNAD_ANNEN_PART_TYPE', 'SOEKNAD_ANNEN_PART');
end;
/

begin 
    migrer_KODELISTE_fk('FAGSAK_STATUS', 'FAGSAK_STATUS');
end;
/

begin 
    migrer_KODELISTE_fk('BEHANDLING_RESULTAT_TYPE', 'BEHANDLING_RESULTAT_TYPE');
end;
/

begin 
    migrer_KODELISTE_fk('RELATERT_YTELSE_RESULTAT', 'RELATERT_YTELSE_RESULTAT');
end;
/

begin 
    migrer_KODELISTE_fk('FORELDRE_TYPE', 'FORELDRE_TYPE');
end;
/

begin 
    migrer_KODELISTE_fk('FAGSAK_YTELSE_TYPE', 'FAGSAK_YTELSE');
end;
/

begin 
    migrer_KODELISTE_fk('OMSORGSOVERTAKELSE_VILKAR_TYPE', 'OMSORGSOVERTAKELSE_VILKAR');
end;
/

begin 
    migrer_KODELISTE_fk('AKSJONSPUNKT_TYPE', 'AKSJONSPUNKT_TYPE');
end;
/

begin 
    migrer_KODELISTE_fk('KONFIG_VERDI_TYPE', 'KONFIG_VERDI_TYPE');
end;
/

begin 
    migrer_KODELISTE_fk('DOKUMENT_TYPE', 'DOKUMENT_TYPE');
end;
/

begin 
    migrer_KODELISTE_fk('OPPGAVE_AARSAK', 'OPPGAVE_AARSAK');
end;
/

begin 
    migrer_KODELISTE_fk('BEHANDLING_AARSAK_TYPE', 'BEHANDLING_AARSAK');
end;
/

begin 
    migrer_KODELISTE_fk('FAGSAK_ARSAK_TYPE', 'FAGSAK_ARSAK');
end;
/

begin 
    migrer_KODELISTE_fk('RELATERT_YTELSE_TILSTAND', 'RELATERT_YTELSE_TILSTAND');
end;
/

begin 
    migrer_KODELISTE_fk('MEDLEMSKAP_KILDE_TYPE', 'MEDLEMSKAP_KILDE');
end;
/

begin 
    migrer_KODELISTE_fk('FAR_SOEKER_TYPE', 'FAR_SOEKER_TYPE');
end;
/

begin 
    migrer_KODELISTE_fk('VEDTAK_RESULTAT_TYPE', 'VEDTAK_RESULTAT_TYPE');
end;
/

begin 
    migrer_KODELISTE_fk('RELATERT_YTELSE_SAKSTYPE', 'RELATERT_YTELSE_SAKSTYPE');
end;
/

begin 
    migrer_KODELISTE_fk('BRUKER_ROLLE_TYPE', 'BRUKER_ROLLE_TYPE');
end;
/

begin 
    migrer_KODELISTE_fk('SATS_TYPE', 'SATS_TYPE');
end;
/

begin 
    migrer_KODELISTE_fk('BRUKER_KJOENN', 'BRUKER_KJOENN');
end;
/

begin 
    migrer_KODELISTE_fk('SPRAAK_KODE', 'SPRAAK_KODE');
end;
/

begin 
    migrer_KODELISTE_fk('KONFIG_VERDI_GRUPPE', 'KONFIG_VERDI_GRUPPE');
end;
/

begin 
    migrer_KODELISTE_fk('VILKAR_UTFALL_TYPE', 'VILKAR_UTFALL_TYPE');
end;
/

begin 
    migrer_KODELISTE_fk('PERIODE_TYPE', 'PERIODE_TYPE');
end;
/

begin 
    migrer_KODELISTE_fk('PERSONSTATUS_TYPE', 'PERSONSTATUS_TYPE');
end;
/



begin
    migrer_KODELISTE_fk('DISKRESJONSKODE', 'DISKRESJONSKODE');
end;
/

begin
    migrer_KODELISTE_fk('OPPLYSNING_ADRESSE_TYPE', 'OPPLYSNING_ADRESSE_TYPE');
end;
/

begin
    migrer_KODELISTE_fk('OPPLYSNINGSKILDE', 'OPPLYSNINGSKILDE');
end;
/

begin
    migrer_KODELISTE_fk('RELASJONSROLLE_TYPE', 'RELASJONSROLLE_TYPE');
end;
/

begin
    migrer_KODELISTE_fk('SIVILSTAND_TYPE', 'SIVILSTAND_TYPE');
end;
/



--------------------------

drop table AKSJONSPUNKT_STATUS cascade constraints;
drop table LAGRET_VEDTAK_TYPE cascade constraints;
drop table INNSENDINGSVALG cascade constraints;
drop table RELATERTE_YTELSER_STATUS cascade constraints;
drop table LANDKODER cascade constraints;
drop table VILKAR_UTFALL_MERKNAD cascade constraints;
drop table BEHANDLING_STATUS cascade constraints;
drop table MEDLEMSKAP_MANUELL_VURD_TYPE cascade constraints;
drop table REGION cascade constraints;
drop table AKSJONSPUNKT_KATEGORI cascade constraints;
drop table MEDLEMSKAP_DEKNING_TYPE cascade constraints;

drop table MEDLEMSKAP_TYPE cascade constraints;
drop table BEHANDLING_STEG_STATUS_TYPE cascade constraints;
drop table VILKAR_RESULTAT_TYPE cascade constraints;
drop table IVERKSETTING_STATUS cascade constraints;
drop table RELATERT_YTELSE_TYPE cascade constraints;
drop table POSTSTED cascade constraints;
drop table HISTORIKK_AKTOER cascade constraints;
drop table DOKUMENT_MAL_RESTRIKSJON cascade constraints;
drop table SOEKNAD_ANNEN_PART_TYPE cascade constraints;
drop table FAGSAK_STATUS cascade constraints;
drop table BEHANDLING_RESULTAT_TYPE cascade constraints;
drop table RELATERT_YTELSE_RESULTAT cascade constraints;
drop table FORELDRE_TYPE cascade constraints;
drop table FAGSAK_YTELSE_TYPE cascade constraints;
drop table OMSORGSOVERTAKELSE_VILKAR_TYPE cascade constraints;
drop table AKSJONSPUNKT_TYPE cascade constraints;
drop table KONFIG_VERDI_TYPE cascade constraints;
drop table DOKUMENT_TYPE cascade constraints;
drop table OPPGAVE_AARSAK cascade constraints;
drop table BEHANDLING_AARSAK_TYPE cascade constraints;
drop table FAGSAK_ARSAK_TYPE cascade constraints;
drop table RELATERT_YTELSE_TILSTAND cascade constraints;
drop table MEDLEMSKAP_KILDE_TYPE cascade constraints;
drop table FAR_SOEKER_TYPE cascade constraints;
drop table VEDTAK_RESULTAT_TYPE cascade constraints;
drop table RELATERT_YTELSE_SAKSTYPE cascade constraints;
drop table BRUKER_ROLLE_TYPE cascade constraints;
drop table SATS_TYPE cascade constraints;
drop table BRUKER_KJOENN cascade constraints;
drop table SPRAAK_KODE cascade constraints;
drop table KONFIG_VERDI_GRUPPE cascade constraints;
drop table VILKAR_UTFALL_TYPE cascade constraints;
drop table PERIODE_TYPE cascade constraints;
drop table PERSONSTATUS_TYPE cascade constraints;

drop table DISKRESJONSKODE cascade constraints;
drop table OPPLYSNING_ADRESSE_TYPE cascade constraints;
drop table OPPLYSNINGSKILDE cascade constraints;
drop table RELASJONSROLLE_TYPE cascade constraints;
drop table SIVILSTAND_TYPE cascade constraints;

--------------------------

