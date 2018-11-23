alter table fagsak drop constraint FK_FAGSAK_81;
alter table fagsak drop column bruker_rolle;
alter table fagsak drop column KL_RELASJONSROLLE_TYPE;

delete from aksjonspunkt_utelukkende;

delete from aksjonspunkt_def where vilkar_type in ('FP_VK_8', 'FP_VK_33', 'FP_VK_11', 'FP_VK_16', 'FP_VK_1', 'FP_VK_4', 'FP_VK_5');
DELETE FROM KODELISTE_NAVN_I18N WHERE KL_KODEVERK = 'VILKAR_TYPE' and KL_kode in ('FP_VK_8', 'FP_VK_33', 'FP_VK_11', 'FP_VK_16', 'FP_VK_1', 'FP_VK_4', 'FP_VK_5');
delete from kodeliste_relasjon where kodeverk1 = 'VILKAR_TYPE' and kode1 in ('FP_VK_8', 'FP_VK_33', 'FP_VK_11', 'FP_VK_16', 'FP_VK_1', 'FP_VK_4', 'FP_VK_5');
delete from kodeliste_relasjon where kodeverk2 = 'VILKAR_TYPE' and kode2 in ('FP_VK_8', 'FP_VK_33', 'FP_VK_11', 'FP_VK_16', 'FP_VK_1', 'FP_VK_4', 'FP_VK_5');
DELETE from kodeliste where kodeverk='VILKAR_TYPE' and kode in ('FP_VK_8', 'FP_VK_33', 'FP_VK_11', 'FP_VK_16', 'FP_VK_1',  'FP_VK_4', 'FP_VK_5');

delete from kodeliste_navn_i18n where kl_kodeverk = 'FAKTA_OM_BEREGNING_TILFELLE' and kl_kode = 'FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE';
delete from kodeliste where kodeverk='FAKTA_OM_BEREGNING_TILFELLE' and kode = 'FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE';

delete from kodeliste_navn_i18n where kl_kodeverk='SKJERMLENKE_TYPE' and kl_kode in ('BEREGNING_ENGANGSSTOENAD', 'PUNKT_FOR_FORELDREANSVAR', 'FAKTA_FOR_OMSORG', 'FAKTA_OM_ADOPSJON', 'FAKTA_OM_FOEDSEL', 'PUNKT_FOR_ADOPSJON', 'PUNKT_FOR_FOEDSEL', 'PUNKT_FOR_OMSORG', 'FAKTA_OM_OMSORG_OG_FORELDREANSVAR');
delete from kodeliste where kodeverk='SKJERMLENKE_TYPE' and kode in ('BEREGNING_ENGANGSSTOENAD', 'PUNKT_FOR_FORELDREANSVAR', 'FAKTA_FOR_OMSORG', 'FAKTA_OM_ADOPSJON', 'FAKTA_OM_FOEDSEL', 'PUNKT_FOR_ADOPSJON', 'PUNKT_FOR_FOEDSEL', 'PUNKT_FOR_OMSORG', 'FAKTA_OM_OMSORG_OG_FORELDREANSVAR');


delete from behandling_type_steg_sekv where fagsak_ytelse_type='ES';

delete from behandling_type_steg_sekv where behandling_steg_type='INSØK';