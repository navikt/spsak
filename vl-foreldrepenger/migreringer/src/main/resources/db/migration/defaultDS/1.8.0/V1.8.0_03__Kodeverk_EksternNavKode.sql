-- PK-43460
-- legger til nav offisiell kode som kan benyttes til å mappe en intern kode til offisielt Nav kodeverk direkte for oppslag 
-- gjelder bare der dette er kjent design time.
--
-- Unngår dermed unødvendig hardkoding av Nav kodeverk i koden på ulike plasser

alter table AKSJONSPUNKT_DEF add (nav_offisiell_kode varchar2(250 char));
alter table AKSJONSPUNKT_KATEGORI add (nav_offisiell_kode varchar2(250 char));
alter table AKSJONSPUNKT_STATUS add (nav_offisiell_kode varchar2(250 char));
alter table BEHANDLING_STATUS add (nav_offisiell_kode varchar2(250 char));
alter table BEHANDLING_STEG_STATUS_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table BEHANDLING_STEG_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table BEHANDLING_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table BRUKER_KJOENN add (nav_offisiell_kode varchar2(250 char));
alter table BRUKER_ROLLE_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table DOKUMENT_MAL_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table DOKUMENT_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table FAGSAK_ARSAK_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table FAGSAK_STATUS add (nav_offisiell_kode varchar2(250 char));
alter table FAGSAK_YTELSE_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table FAR_SOEKER_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table INNSENDINGSVALG add (nav_offisiell_kode varchar2(250 char));
alter table LAGRET_VEDTAK_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table MOTTATT_STATUS add (nav_offisiell_kode varchar2(250 char));
alter table OPPGAVE_AARSAK add (nav_offisiell_kode varchar2(250 char));
alter table SAKSOPPLYSNING_DOKUMENT_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table SAKSOPPLYSNING_KILDE add (nav_offisiell_kode varchar2(250 char));
alter table SAKSOPPLYSNING_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table SATS_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table SOEKNAD_ANNEN_PART_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table VEDTAK_RESULTAT_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table VILKAR_RESULTAT_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table VILKAR_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table VILKAR_UTFALL_MERKNAD add (nav_offisiell_kode varchar2(250 char));
alter table VILKAR_UTFALL_TYPE add (nav_offisiell_kode varchar2(250 char));
alter table VURDERINGSPUNKT_DEF add (nav_offisiell_kode varchar2(250 char));


