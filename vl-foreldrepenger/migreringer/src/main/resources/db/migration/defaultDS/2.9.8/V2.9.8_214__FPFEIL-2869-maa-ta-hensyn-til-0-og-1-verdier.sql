-- Fiks til V2.9.8_125__FPFEIL-2411_fikser_mismatch_mellom_entitet_og_db.sql
-- Må ta hensyn til 0/1 verdier, kan ikke bare kopiere over kolonnen og deretter rename slik det ble gjort.
-- Oppdaterer her alle kolonner, uavhengig av om de har hatt 0/1-verdier eller ikke, for å slippe å gå
-- gjennom hvilke som hadde slike verdier. Legger også på constraint for sjekke av lovlige verdier.

UPDATE AKSJONSPUNKT_DEF SET TILBAKEHOPP_VED_GJENOPPTAKELSE = 'N' WHERE TILBAKEHOPP_VED_GJENOPPTAKELSE = '0';
UPDATE AKSJONSPUNKT_DEF SET TILBAKEHOPP_VED_GJENOPPTAKELSE = 'J' WHERE TILBAKEHOPP_VED_GJENOPPTAKELSE IN ('1', 'Y');
ALTER TABLE AKSJONSPUNKT_DEF add constraint CHK_TILBAKHP_VED_GJENOPPT CHECK (TILBAKEHOPP_VED_GJENOPPTAKELSE  IN ('J', 'N'));

UPDATE BEHANDLING SET totrinnsbehandling = 'N' WHERE totrinnsbehandling = '0';
UPDATE BEHANDLING SET totrinnsbehandling = 'J' WHERE totrinnsbehandling IN ('1', 'Y') ;
ALTER TABLE BEHANDLING add constraint CHK_totrinnsbehandling1 CHECK (totrinnsbehandling IN ('J', 'N'));

UPDATE BEHANDLING_VEDTAK SET BESLUTNING = 'N' WHERE BESLUTNING = '0';
UPDATE BEHANDLING_VEDTAK SET BESLUTNING = 'J' WHERE BESLUTNING IN ('1', 'Y');
ALTER TABLE BEHANDLING_VEDTAK add constraint CHK_BESLUTNING CHECK (BESLUTNING IN ('J', 'N'));

UPDATE BEREGNING SET overstyrt = 'N' WHERE overstyrt = '0';
UPDATE BEREGNING SET overstyrt = 'J' WHERE overstyrt IN ('1', 'Y');
ALTER TABLE BEREGNING add constraint CHK_overstyrt CHECK (overstyrt IN ('J', 'N'));

UPDATE BEREGNING_RESULTAT SET overstyrt = 'N' WHERE overstyrt = '0';
UPDATE BEREGNING_RESULTAT SET overstyrt = 'J' WHERE overstyrt IN ('1', 'Y');
ALTER TABLE BEREGNING_RESULTAT add constraint CHK_overstyrt1 CHECK (overstyrt IN ('J', 'N'));

UPDATE BEREGNINGSRESULTAT_ANDEL SET bruker_er_mottaker = 'N' WHERE bruker_er_mottaker = '0';
UPDATE BEREGNINGSRESULTAT_ANDEL SET bruker_er_mottaker = 'J' WHERE bruker_er_mottaker IN ('1', 'Y');
ALTER TABLE BEREGNINGSRESULTAT_ANDEL add constraint CHK_bruker_mottaker CHECK (bruker_er_mottaker IN ('J', 'N'));

UPDATE BG_PR_STATUS_OG_ANDEL SET lønnsendring_i_perioden = 'N' WHERE lønnsendring_i_perioden = '0';
UPDATE BG_PR_STATUS_OG_ANDEL SET lønnsendring_i_perioden = 'J' WHERE lønnsendring_i_perioden IN ('1', 'Y');
ALTER TABLE BG_PR_STATUS_OG_ANDEL add constraint CHK_lonsendringiperioden CHECK (lønnsendring_i_perioden IN ('J', 'N'));

UPDATE FH_FAMILIE_HENDELSE SET mor_for_syk_ved_fodsel = 'N' WHERE mor_for_syk_ved_fodsel = '0';
UPDATE FH_FAMILIE_HENDELSE SET mor_for_syk_ved_fodsel = 'J' WHERE mor_for_syk_ved_fodsel IN ('1', 'Y');
ALTER TABLE FH_FAMILIE_HENDELSE add constraint CHK_mor_syk_fodsel CHECK (mor_for_syk_ved_fodsel IN ('J', 'N'));

UPDATE GR_MEDLEMSKAP SET aktiv = 'N' WHERE aktiv = '0';
UPDATE GR_MEDLEMSKAP SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE GR_MEDLEMSKAP add constraint CHK_aktiv CHECK (aktiv IN ('J', 'N'));

UPDATE GR_YTELSES_FORDELING SET aktiv = 'N' WHERE aktiv = '0';
UPDATE GR_YTELSES_FORDELING SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE GR_YTELSES_FORDELING add constraint CHK_aktiv1 CHECK (aktiv IN ('J', 'N'));

UPDATE GR_ARBEID_INNTEKT SET aktiv = 'N' WHERE aktiv = '0';
UPDATE GR_ARBEID_INNTEKT SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE GR_ARBEID_INNTEKT add constraint CHK_aktiv2 CHECK (aktiv IN ('J', 'N'));

UPDATE OPPTJENING SET aktiv = 'N' WHERE aktiv = '0';
UPDATE OPPTJENING SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE OPPTJENING add constraint CHK_aktiv3 CHECK (aktiv IN ('J', 'N'));

UPDATE UTTAK_RESULTAT SET aktiv = 'N' WHERE aktiv = '0';
UPDATE UTTAK_RESULTAT SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE UTTAK_RESULTAT add constraint CHK_aktiv4 CHECK (aktiv IN ('J', 'N'));

UPDATE TOTRINNSVURDERING SET aktiv = 'N' WHERE aktiv = '0';
UPDATE TOTRINNSVURDERING SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE TOTRINNSVURDERING add constraint CHK_aktiv5 CHECK (aktiv IN ('J', 'N'));

UPDATE UTTAK_RESULTAT SET aktiv = 'N' WHERE aktiv = '0';
UPDATE UTTAK_RESULTAT SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE UTTAK_RESULTAT add constraint CHK_aktiv6 CHECK (aktiv IN ('J', 'N'));

UPDATE GR_VERGE SET aktiv = 'N' WHERE aktiv = '0';
UPDATE GR_VERGE SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE GR_VERGE add constraint CHK_aktiv7 CHECK (aktiv IN ('J', 'N'));

UPDATE GR_MEDLEMSKAP SET aktiv = 'N' WHERE aktiv = '0';
UPDATE GR_MEDLEMSKAP SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE GR_MEDLEMSKAP add constraint CHK_aktiv8 CHECK (aktiv IN ('J', 'N'));

UPDATE RES_BEREGNINGSRESULTAT_FP SET aktiv = 'N' WHERE aktiv = '0';
UPDATE RES_BEREGNINGSRESULTAT_FP SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE RES_BEREGNINGSRESULTAT_FP add constraint CHK_aktiv9 CHECK (aktiv IN ('J', 'N'));

UPDATE GR_MEDLEMSKAP_VILKAR_PERIODE SET aktiv = 'N' WHERE aktiv = '0';
UPDATE GR_MEDLEMSKAP_VILKAR_PERIODE SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE GR_MEDLEMSKAP_VILKAR_PERIODE add constraint CHK_aktiv10 CHECK (aktiv IN ('J', 'N'));

UPDATE GR_BEREGNINGSGRUNNLAG SET aktiv = 'N' WHERE aktiv = '0';
UPDATE GR_BEREGNINGSGRUNNLAG SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE GR_BEREGNINGSGRUNNLAG add constraint CHK_aktiv11 CHECK (aktiv IN ('J', 'N'));

UPDATE FAGSAK_RELASJON SET aktiv = 'N' WHERE aktiv = '0';
UPDATE FAGSAK_RELASJON SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE FAGSAK_RELASJON add constraint CHK_aktiv12 CHECK (aktiv IN ('J', 'N'));

UPDATE TOTRINNRESULTATGRUNNLAG SET aktiv = 'N' WHERE aktiv = '0';
UPDATE TOTRINNRESULTATGRUNNLAG SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE TOTRINNRESULTATGRUNNLAG add constraint CHK_aktiv13 CHECK (aktiv IN ('J', 'N'));

UPDATE GR_PERSONOPPLYSNING SET aktiv = 'N' WHERE aktiv = '0';
UPDATE GR_PERSONOPPLYSNING SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE GR_PERSONOPPLYSNING add constraint CHK_aktiv14 CHECK (aktiv IN ('J', 'N'));

UPDATE INNSYN_DOKUMENT SET fikk_innsyn = 'N' WHERE fikk_innsyn = '0';
UPDATE INNSYN_DOKUMENT SET fikk_innsyn = 'J' WHERE fikk_innsyn IN ('1', 'Y');
ALTER TABLE INNSYN_DOKUMENT add constraint CHK_fikk_innsyn CHECK (fikk_innsyn IN ('J', 'N'));

UPDATE KODEVERK SET kodeverk_synk_eksisterende = 'N' WHERE kodeverk_synk_eksisterende = '0';
UPDATE KODEVERK SET kodeverk_synk_eksisterende = 'J' WHERE kodeverk_synk_eksisterende IN ('1', 'Y');
ALTER TABLE KODEVERK add constraint CHK_kvrk_synk_eksist CHECK (kodeverk_synk_eksisterende IN ('J', 'N'));

UPDATE KODEVERK SET kodeverk_synk_nye = 'N' WHERE kodeverk_synk_nye = '0';
UPDATE KODEVERK SET kodeverk_synk_nye = 'J' WHERE kodeverk_synk_nye IN ('1', 'Y');
ALTER TABLE KODEVERK add constraint CHK_kodeverk_synk_nye CHECK (kodeverk_synk_nye IN ('J', 'N'));

UPDATE KODEVERK SET sammensatt = 'N' WHERE sammensatt = '0';
UPDATE KODEVERK SET sammensatt = 'J' WHERE sammensatt IN ('1', 'Y');
ALTER TABLE KODEVERK add constraint CHK_sammensatt CHECK (sammensatt IN ('J', 'N'));

UPDATE SOEKNAD_VEDLEGG SET VEDLEGG_PAKREVD = 'N' WHERE VEDLEGG_PAKREVD = '0';
UPDATE SOEKNAD_VEDLEGG SET VEDLEGG_PAKREVD = 'J' WHERE VEDLEGG_PAKREVD IN ('1', 'Y');
ALTER TABLE SOEKNAD_VEDLEGG add constraint CHK_VEDLEGG_PAKREVD CHECK (VEDLEGG_PAKREVD IN ('J', 'N'));

UPDATE UTTAK_RESULTAT_DOK_REGEL SET til_manuell_behandling = 'N' WHERE til_manuell_behandling = '0';
UPDATE UTTAK_RESULTAT_DOK_REGEL SET til_manuell_behandling = 'J' WHERE til_manuell_behandling IN ('1', 'Y');
ALTER TABLE UTTAK_RESULTAT_DOK_REGEL add constraint CHK_til_manu_behndling CHECK (til_manuell_behandling IN ('J', 'N'));

UPDATE UTTAK_RESULTAT_PERIODE SET flerbarnsdager = 'N' WHERE flerbarnsdager = '0';
UPDATE UTTAK_RESULTAT_PERIODE SET flerbarnsdager = 'J' WHERE flerbarnsdager IN ('1', 'Y');
ALTER TABLE UTTAK_RESULTAT_PERIODE add constraint CHK_flerbarnsdager CHECK (flerbarnsdager IN ('J', 'N'));

UPDATE UTTAK_RESULTAT_PERIODE SET gradering_innvilget = 'N' WHERE gradering_innvilget = '0';
UPDATE UTTAK_RESULTAT_PERIODE SET gradering_innvilget = 'J' WHERE gradering_innvilget IN ('1', 'Y');
ALTER TABLE UTTAK_RESULTAT_PERIODE add constraint CHK_gradering_innvilget CHECK (gradering_innvilget IN ('J', 'N'));

UPDATE UTTAK_RESULTAT_PERIODE SET manuelt_behandlet = 'N' WHERE manuelt_behandlet = '0';
UPDATE UTTAK_RESULTAT_PERIODE SET manuelt_behandlet = 'J' WHERE manuelt_behandlet IN ('1', 'Y');
ALTER TABLE UTTAK_RESULTAT_PERIODE add constraint CHK_manuelt_behandlet CHECK (manuelt_behandlet IN ('J', 'N'));

UPDATE UTTAK_RESULTAT_PERIODE SET samtidig_uttak = 'N' WHERE samtidig_uttak = '0';
UPDATE UTTAK_RESULTAT_PERIODE SET samtidig_uttak = 'J' WHERE samtidig_uttak IN ('1', 'Y');
ALTER TABLE UTTAK_RESULTAT_PERIODE add constraint CHK_samtidig_uttak CHECK (samtidig_uttak IN ('J', 'N'));

UPDATE UTTAK_RESULTAT_PERIODE_AKT SET gradering = 'N' WHERE gradering = '0';
UPDATE UTTAK_RESULTAT_PERIODE_AKT SET gradering = 'J' WHERE gradering IN ('1', 'Y');
ALTER TABLE UTTAK_RESULTAT_PERIODE_AKT add constraint CHK_gradering CHECK (gradering IN ('J', 'N'));

UPDATE UTTAK_RESULTAT_PERIODE_SOKNAD SET samtidig_uttak = 'N' WHERE samtidig_uttak = '0';
UPDATE UTTAK_RESULTAT_PERIODE_SOKNAD SET samtidig_uttak = 'J' WHERE samtidig_uttak IN ('1', 'Y');
ALTER TABLE UTTAK_RESULTAT_PERIODE_SOKNAD add constraint CHK_samtidig_uttak1 CHECK (samtidig_uttak IN ('J', 'N'));

UPDATE UTTAKSPERIODEGRENSE SET aktiv = 'N' WHERE aktiv = '0';
UPDATE UTTAKSPERIODEGRENSE SET aktiv = 'J' WHERE aktiv IN ('1', 'Y');
ALTER TABLE UTTAKSPERIODEGRENSE add constraint CHK_aktiv15 CHECK (aktiv IN ('J', 'N'));

UPDATE VILKAR_RESULTAT SET overstyrt = 'N' WHERE overstyrt = '0';
UPDATE VILKAR_RESULTAT SET overstyrt = 'J' WHERE overstyrt IN ('1', 'Y');
ALTER TABLE VILKAR_RESULTAT add constraint CHK_overstyrt2 CHECK (overstyrt IN ('J', 'N'));

UPDATE YF_FORDELING SET annenForelderErInformert = 'N' WHERE annenForelderErInformert = '0';
UPDATE YF_FORDELING SET annenForelderErInformert = 'J' WHERE annenForelderErInformert IN ('1', 'Y');
ALTER TABLE YF_FORDELING add constraint CHK_annenForelderErInformert CHECK (annenForelderErInformert IN ('J', 'N'));

UPDATE YF_FORDELING_PERIODE SET arbeidstaker = 'N' WHERE arbeidstaker = '0';
UPDATE YF_FORDELING_PERIODE SET arbeidstaker = 'J' WHERE arbeidstaker IN ('1', 'Y');
ALTER TABLE YF_FORDELING_PERIODE add constraint CHK_arbeidstaker CHECK (arbeidstaker IN ('J', 'N'));

UPDATE YF_FORDELING_PERIODE SET samtidig_uttak = 'N' WHERE samtidig_uttak = '0';
UPDATE YF_FORDELING_PERIODE SET samtidig_uttak = 'J' WHERE samtidig_uttak IN ('1', 'Y');
ALTER TABLE YF_FORDELING_PERIODE add constraint CHK_samtidig_uttak2 CHECK (samtidig_uttak IN ('J', 'N'));

UPDATE YF_FORDELING_PERIODE SET flerbarnsdager = 'N' WHERE flerbarnsdager = '0';
UPDATE YF_FORDELING_PERIODE SET flerbarnsdager = 'J' WHERE flerbarnsdager IN ('1', 'Y');
ALTER TABLE YF_FORDELING_PERIODE add constraint CHK_flerbarnsdager2 CHECK (flerbarnsdager IN ('J', 'N'));
