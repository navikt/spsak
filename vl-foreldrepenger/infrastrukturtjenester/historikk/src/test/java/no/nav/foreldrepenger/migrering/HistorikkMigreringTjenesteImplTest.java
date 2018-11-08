package no.nav.foreldrepenger.migrering;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagTotrinnsvurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.migrering.api.HistorikkMigreringConstants;
import no.nav.foreldrepenger.migrering.api.HistorikkMigreringRepository;
import no.nav.foreldrepenger.migrering.api.HistorikkMigreringTjeneste;
import no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverterFactory;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class HistorikkMigreringTjenesteImplTest {
    private static final int ANTALL_BEHANDLINGER = 1;
    private static final int ANTALL_HISTORIKKINNSLAGDELER_PER_BEHANDLING = 7;

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();
    private EntityManager entityManager = repositoryRule.getEntityManager();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private AksjonspunktRepository aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private HistorikkMigreringRepository historikkMigreringRepository = new HistorikkMigreringRepositoryImpl(entityManager);
    private HistorikkMigreringKonverterFactory historikkinnslagMigreringKonverterFactory = new HistorikkMigreringKonverterFactory(aksjonspunktRepository, behandlingRepository);
    private HistorikkMigreringTjeneste historikkMigreringTjeneste = new HistorikkMigreringTjenesteImpl(historikkinnslagMigreringKonverterFactory, historikkMigreringRepository);
    private HistorikkMigreringTestdataBuilder testDataProvider = new HistorikkMigreringTestdataBuilder(historikkMigreringRepository, aksjonspunktRepository);

    @Test
    public void skal_lage_del_hvis_tekst_er_null() {
        // Arrange
        String tekst = null;
        lagreHistorikkinnslag(HistorikkinnslagType.NYE_REGOPPLYSNINGER, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        assertThat(deler.get(0).getHendelse()).hasValueSatisfying(hendelse ->
            assertThat(hendelse.getNavn()).isEqualTo(HistorikkinnslagType.NYE_REGOPPLYSNINGER.getKode()));
    }

    @Test
    public void skal_konvertere_dokumentasjon_foreligger() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"Kun utbetalt for ett barn\",\"endredeFelter\":[{\"navn\":\"SjekkFodselDokForm.DokumentasjonForeligger\",\"fraVerdi\":null,\"tilVerdi\":true},{\"navn\":\"SjekkFodselDokForm.BrukAntallIYtelsesvedtaket\",\"fraVerdi\":null,\"tilVerdi\":true}],\"skjermlinke\":{\"faktaNavn\":\"foedsel\",\"punktNavn\":\"foedsel\",\"linkTekst\":\"Fødsel\"},\"opplysninger\":[{\"verdi\":1,\"navn\":\"Historikk.Template.5.AntallBarn\"}]}\n";
        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertThat(del.getBegrunnelse()).hasValueSatisfying(begrunnelse ->
            assertThat(begrunnelse).isEqualTo("Kun utbetalt for ett barn"));
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke ->
            assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_FOEDSEL.getKode()));

        assertEndretFelt(del, HistorikkEndretFeltType.DOKUMENTASJON_FORELIGGER, null, "true");
        assertEndretFelt(del, HistorikkEndretFeltType.BRUK_ANTALL_I_VEDTAKET, null, "true");
        assertOpplysning(del, HistorikkOpplysningType.ANTALL_BARN, "1");
    }

    @Test
    public void skal_konvertere_type_6() {
        // Arrange
        String tekst = "{\"endredeFelter\":[],\"opplysninger\":[{\"verdi\":\"20.04.2018\",\"navn\":\"Registrering.Fodselsdato\"},{\"verdi\":1,\"navn\":\"Registrering.AntallBarn\"}],\"hendelse\":\"Ny info fra TPS\"}";

        lagreHistorikkinnslag(HistorikkinnslagType.NY_INFO_FRA_TPS, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);

        assertThat(del.getEndredeFelt()).isEmpty();
        assertThat(del.getHendelse()).hasValueSatisfying(hendelse ->
            assertThat(hendelse.getNavn()).isEqualTo(HistorikkinnslagType.NY_INFO_FRA_TPS.getKode()));
        assertThat(del.getOpplysninger()).hasSize(2);

        assertOpplysning(del, HistorikkOpplysningType.FODSELSDATO, "20.04.2018");
        assertOpplysning(del, HistorikkOpplysningType.TPS_ANTALL_BARN, "1");
    }

    private void assertOpplysning(HistorikkinnslagDel del, HistorikkOpplysningType opplysningType, String verdi) {
        Optional<HistorikkinnslagFelt> opplysningOpt = del.getOpplysning(opplysningType);
        assertThat(opplysningOpt).as(HistorikkMigreringConstants.OPPLYSNING).hasValueSatisfying(opplysning -> {
            assertThat(opplysning.getNavn()).as(HistorikkMigreringConstants.NAVN).isEqualTo(opplysningType.getKode());
            assertThat(opplysning.getTilVerdi()).as(HistorikkMigreringConstants.TIL_VERDI).isEqualTo(verdi);
        });
    }

    @Test
    @Ignore("Ikke lenger relevant for produksjon, fundamentet-data er allerede migrert over")
    public void skal_konverter_sak_retur_medlemskap() {
        // Arrange
        String tekst = "{\"totrinnsvurdering\":[{\"skjermlinke\":{\"faktaNavn\":\"medlemskap\",\"punktNavn\":\"medlemskap\",\"linkTekst\":\"Medlemskap\"},\"aksjonspunkter\":[{\"begrunnelse\":\"oppholdstillatelse er ennå ikke behandlet\",\"godkjent\":false,\"kode\":\"5022\"},{\"begrunnelse\":\"kan ikke se at bruker har gyldig oppholdstillatelse. Det er dato for fornyelse av pass som er til og med 130219\",\"godkjent\":false,\"kode\":\"5019\"}]}],\"endredeFelter\":[],\"opplysninger\":[],\"hendelse\":\"Vedtak returnert\"}";

        lagreHistorikkinnslag(HistorikkinnslagType.SAK_RETUR, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);

        assertThat(del.getHendelse()).hasValueSatisfying(hendelse ->
            assertThat(hendelse.getNavn()).isEqualTo(HistorikkinnslagType.SAK_RETUR.getKode()));
        List<HistorikkinnslagTotrinnsvurdering> totrinnsvurderinger = del.getTotrinnsvurderinger(aksjonspunktRepository);
        assertThat(totrinnsvurderinger).hasSize(2);
        assertTotrinnsvurdering(totrinnsvurderinger.get(0), "5022", false, "oppholdstillatelse er ennå ikke behandlet");
        assertTotrinnsvurdering(totrinnsvurderinger.get(1), "5019", false, "kan ikke se at bruker har gyldig oppholdstillatelse. Det er dato for fornyelse av pass som er til og med 130219");
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke ->
            assertThat(skjermlenke).as(HistorikkMigreringConstants.SKJERMLENKE).isEqualTo(SkjermlenkeType.FAKTA_OM_MEDLEMSKAP.getKode()));
    }

    @Test
    public void skal_konvertere_sak_retur_5024() {
        // Arrange
        String tekst = "{\"totrinnsvurdering\":[{\"aksjonspunkter\":[{\"begrunnelse\":\"Bruker ikke medlem. Sverige er kompetent land \",\"godkjent\":false,\"kode\":\"5024\"}]},{\"skjermlinke\":{\"faktaNavn\":\"foedsel\",\"punktNavn\":\"foedsel\",\"linkTekst\":\"Fødsel\"},\"aksjonspunkter\":[{\"begrunnelse\":\"\",\"godkjent\":true,\"kode\":\"5031\"}]}],\"endredeFelter\":[],\"opplysninger\":[],\"hendelse\":\"Vedtak returnert\"}";

        lagreHistorikkinnslag(HistorikkinnslagType.SAK_RETUR, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(2);

        HistorikkinnslagDel del = deler.get(0);
        assertThat(del.getHendelse()).hasValueSatisfying(hendelse ->
            assertThat(hendelse.getNavn()).isEqualTo(HistorikkinnslagType.SAK_RETUR.getKode()));

        List<HistorikkinnslagTotrinnsvurdering> totrinnsvurderinger = del.getTotrinnsvurderinger(aksjonspunktRepository);
        assertThat(totrinnsvurderinger).hasSize(1);
        assertTotrinnsvurdering(totrinnsvurderinger.get(0), "5031", true, "");
        assertThat(del.getSkjermlenke()).isEmpty();

        del = deler.get(1);
        totrinnsvurderinger = del.getTotrinnsvurderinger(aksjonspunktRepository);
        assertThat(totrinnsvurderinger).hasSize(1);
        assertTotrinnsvurdering(totrinnsvurderinger.get(0), "5024", false, "Bruker ikke medlem. Sverige er kompetent land ");
        assertThat(del.getSkjermlenke()).isEmpty();
    }

    @Test
    public void skal_konvertere_sak_retur_med_skjermlenke_klage_beh_nk() {
        // Arrange
        String tekst = "{\"totrinnsvurdering\":[{\"aksjonspunkter\":[{\"begrunnelse\":\"hhj\",\"godkjent\":false,\"kode\":\"5036\"}]}],\"endredeFelter\":[],\"opplysninger\":[],\"hendelse\":\"Vedtak returnert\"}";

        lagreHistorikkinnslag(HistorikkinnslagType.SAK_RETUR, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);

        assertThat(del.getHendelse()).hasValueSatisfying(hendelse ->
            assertThat(hendelse.getNavn()).isEqualTo(HistorikkinnslagType.SAK_RETUR.getKode()));
        List<HistorikkinnslagTotrinnsvurdering> totrinnsvurderinger = del.getTotrinnsvurderinger(aksjonspunktRepository);
        assertThat(totrinnsvurderinger).hasOnlyOneElementSatisfying(vurdering5036 -> {
            assertThat(vurdering5036.getAksjonspunktDefinisjon().getKode()).isEqualTo("5036");
            assertThat(vurdering5036.erGodkjent()).isFalse();
            assertThat(vurdering5036.getBegrunnelse()).isEqualTo("hhj");
        });
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke ->
            assertThat(skjermlenke).as(HistorikkMigreringConstants.SKJERMLENKE).isEqualTo(SkjermlenkeType.KLAGE_BEH_NK.getKode()));
    }

    @Test
    public void skal_lage_to_deler_for_sak_retur() {
        // Arrange
        String tekst = "{\"totrinnsvurdering\":[{\"skjermlinke\":{\"faktaNavn\":\"medlemskap\",\"punktNavn\":\"medlemskap\",\"linkTekst\":\"Medlemskap\"},\"aksjonspunkter\":[{\"begrunnelse\":\"tye5t\",\"godkjent\":false,\"kode\":\"5020\"}]},{\"skjermlinke\":{\"faktaNavn\":\"adopsjon\",\"punktNavn\":\"adopsjon\",\"linkTekst\":\"Adopsjon\"},\"aksjonspunkter\":[{\"begrunnelse\":\"\",\"godkjent\":true,\"kode\":\"5006\"},{\"begrunnelse\":\"\",\"godkjent\":true,\"kode\":\"5005\"}]}],\"endredeFelter\":[],\"opplysninger\":[],\"hendelse\":\"Vedtak returnert\"}";
        Behandling behandling = lagreHistorikkinnslag(HistorikkinnslagType.SAK_RETUR, tekst);
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT);
        entityManager.persist(behandling);
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_OM_SØKER_ER_MANN_SOM_ADOPTERER_ALENE);
        entityManager.persist(behandling);
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN);
        entityManager.persist(behandling);
        entityManager.flush();

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        deler.sort(Comparator.comparing(hd -> hd.getSkjermlenke().orElse("")));
        assertThat(deler).hasSize(2);

        HistorikkinnslagDel adopsjonDel = deler.get(0);
        assertThat(adopsjonDel.getSkjermlenke()).hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_ADOPSJON.getKode()));
        List<HistorikkinnslagTotrinnsvurdering> totrinnsvurderinger = adopsjonDel.getTotrinnsvurderinger(aksjonspunktRepository);
        assertThat(totrinnsvurderinger).hasSize(2);
        assertTotrinnsvurdering(totrinnsvurderinger.get(0), "5006", true, "");
        assertTotrinnsvurdering(totrinnsvurderinger.get(1), "5005", true, "");

        HistorikkinnslagDel medlemskapDel = deler.get(1);
        assertThat(medlemskapDel.getHendelse()).hasValueSatisfying(hendelse ->
            assertThat(hendelse.getNavn()).isEqualTo(HistorikkinnslagType.SAK_RETUR.getKode()));
        assertThat(medlemskapDel.getSkjermlenke()).hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_MEDLEMSKAP.getKode()));
        assertThat(medlemskapDel.getTotrinnsvurderinger(aksjonspunktRepository)).hasOnlyOneElementSatisfying(vurdering5020 ->
            assertTotrinnsvurdering(vurdering5020, "5020", false, "tye5t"));
    }

    private void assertTotrinnsvurdering(HistorikkinnslagTotrinnsvurdering vurdering, String expectedKode, boolean expectedErGodkjent, String expectedBegrunnelse) {
        assertThat(vurdering.getAksjonspunktDefinisjon().getKode()).isEqualTo(expectedKode);
        assertThat(vurdering.erGodkjent()).isEqualTo(expectedErGodkjent);
        assertThat(vurdering.getBegrunnelse()).isEqualTo(expectedBegrunnelse);
    }

    @Test
    public void skal_konverter_foreldreansvar() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"ukghkghk\",\"endredeFelter\":[{\"navn\":\"ErForeldreansvar4LeddVilkaarOppfyltForm.Foreldreansvar\",\"fraVerdi\":null,\"tilVerdi\":\"ikke oppfylt\"}],\"skjermlinke\":{\"faktaNavn\":\"foreldreansvar\",\"punktNavn\":\"foreldreansvar\",\"linkTekst\":\"Foreldreansvar\"},\"opplysninger\":[]}";

        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertEndretFelt(del, HistorikkEndretFeltType.FORELDREANSVARSVILKARET, null, HistorikkEndretFeltVerdiType.IKKE_OPPFYLT.getKode());
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.PUNKT_FOR_FORELDREANSVAR.getKode()));
    }

    @Test
    public void skal_konverter_fakta_om_omsorg_og_foreldreansvar() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"vdegvervg\",\"endredeFelter\":[{\"navn\":\"DokumentasjonFaktaForm.Omsorgsovertakelsesdato\",\"fraVerdi\":null,\"tilVerdi\":\"18.01.2018\"}],\"skjermlinke\":{\"faktaNavn\":\"omsorg\",\"punktNavn\":\"omsorg\",\"linkTekst\":\"Omsorg\"},\"opplysninger\":[]}";

        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertThat(del.getBegrunnelse()).as(HistorikkMigreringConstants.BEGRUNNELSE).hasValueSatisfying(begrunnelse -> assertThat(begrunnelse).isEqualTo("vdegvervg"));
        assertEndretFelt(del, HistorikkEndretFeltType.OMSORGSOVERTAKELSESDATO, null, "18.01.2018");
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_OMSORG_OG_FORELDREANSVAR.getKode()));
    }

    @Test
    public void skal_henlegge_med_aarsak() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"mnjolkpo\",\"endredeFelter\":[],\"opplysninger\":[],\"hendelse\":\"Behandlingen er henlagt\",\"aarsak\":\"Henlagt, søknaden er trukket\"}";
        lagreHistorikkinnslag(HistorikkinnslagType.BEH_VENT, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertThat(del.getBegrunnelse()).as(HistorikkMigreringConstants.BEGRUNNELSE).hasValueSatisfying(begrunnelse -> assertThat(begrunnelse).isEqualTo("mnjolkpo"));
        assertThat(del.getHendelse()).as(HistorikkMigreringConstants.HENDELSE).hasValueSatisfying(hendelse -> assertThat(hendelse.getNavn()).isEqualTo(HistorikkinnslagType.AVBRUTT_BEH.getKode()));
        assertThat(del.getAarsak()).as(HistorikkMigreringConstants.AARSAK).hasValueSatisfying(aarsak -> assertThat(aarsak).isEqualTo(BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET.getKode()));
    }

    @Test
    public void skal_konvertere_frilansinntekt() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"asd\",\"endredeFelter\":[{\"navn\":\"Beregningsgrunnlag.AarsinntektPanel.Frilansinntekt\",\"fraVerdi\":null,\"tilVerdi\":445716.0}],\"skjermlinke\":{\"faktaNavn\":\"beregning\",\"punktNavn\":\"beregning\",\"linkTekst\":\"Beregning\"},\"opplysninger\":[],\"resultat\":\"Grunnlag for beregnet årsinntekt\"}\n";
        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertEndretFelt(del, HistorikkEndretFeltType.FRILANS_INNTEKT, null, "445716");
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.BEREGNING_FORELDREPENGER.getKode()));
        assertThat(del.getResultat()).as(HistorikkMigreringConstants.RESULTAT).hasValueSatisfying(resultat ->
            assertThat(resultat).isEqualTo(HistorikkResultatType.BEREGNET_AARSINNTEKT.getKode()));
    }

    @Test
    public void test_ikke_lovlig_opphold() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"erftgjukilø\",\"endredeFelter\":[{\"navn\":\"MedlemskapInfoPanel.IkkeEOSBorgerMedLovligOpphold\",\"fraVerdi\":null,\"tilVerdi\":false}],\"skjermlinke\":{\"faktaNavn\":\"medlemskap\",\"punktNavn\":\"medlemskap\",\"linkTekst\":\"Medlemskap\"},\"opplysninger\":[]}";
        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertEndretFelt(del, HistorikkEndretFeltType.OPPHOLDSRETT_IKKE_EOS, null, HistorikkEndretFeltVerdiType.IKKE_LOVLIG_OPPHOLD.getKode());
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_MEDLEMSKAP.getKode()));
    }

    @Test
    public void test_oppholdsrett() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"erftgjukilø\",\"endredeFelter\":[{\"navn\":\"MedlemskapInfoPanel.EOSBorgerMedOppholdsrett\",\"fraVerdi\":null,\"tilVerdi\":true}],\"skjermlinke\":{\"faktaNavn\":\"medlemskap\",\"punktNavn\":\"medlemskap\",\"linkTekst\":\"Medlemskap\"},\"opplysninger\":[]}";
        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertEndretFelt(del, HistorikkEndretFeltType.OPPHOLDSRETT_EOS, null, HistorikkEndretFeltVerdiType.OPPHOLDSRETT.getKode());
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_MEDLEMSKAP.getKode()));
    }

    @Test
    public void test_lovlig_opphold() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"erftgjukilø\",\"endredeFelter\":[{\"navn\":\"MedlemskapInfoPanel.IkkeEOSBorgerMedLovligOpphold\",\"fraVerdi\":null,\"tilVerdi\":true}],\"skjermlinke\":{\"faktaNavn\":\"medlemskap\",\"punktNavn\":\"medlemskap\",\"linkTekst\":\"Medlemskap\"},\"opplysninger\":[]}";
        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertEndretFelt(del, HistorikkEndretFeltType.OPPHOLDSRETT_IKKE_EOS, null, HistorikkEndretFeltVerdiType.LOVLIG_OPPHOLD.getKode());
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_MEDLEMSKAP.getKode()));
    }

    @Test
    public void test_ikke_oppholdsrett() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"erftgjukilø\",\"endredeFelter\":[{\"navn\":\"MedlemskapInfoPanel.EOSBorgerMedOppholdsrett\",\"fraVerdi\":true,\"tilVerdi\":false},{\"navn\":\"MedlemskapInfoPanel.IkkeEOSBorgerMedLovligOpphold\",\"fraVerdi\":false,\"tilVerdi\":null}],\"skjermlinke\":{\"faktaNavn\":\"medlemskap\",\"punktNavn\":\"medlemskap\",\"linkTekst\":\"Medlemskap\"},\"opplysninger\":[]}";
        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertEndretFelt(del, HistorikkEndretFeltType.OPPHOLDSRETT_EOS, HistorikkEndretFeltVerdiType.OPPHOLDSRETT.getKode(), HistorikkEndretFeltVerdiType.IKKE_OPPHOLDSRETT.getKode());
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_MEDLEMSKAP.getKode()));
    }

    @Test
    public void skal_konvertere_ikke_aleneomsorg() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"sdfsdf sdf sdf\",\"endredeFelter\":[{\"navn\":\"OmsorgFaktaForm.Aleneomsorg\",\"fraVerdi\":null,\"tilVerdi\":\"Søker har ikke aleneomsorg for barnet\"}],\"skjermlinke\":{\"faktaNavn\":\"Fakta om omsorg\",\"punktNavn\":\"Fakta for omsorg\",\"linkTekst\":\"Fakta for omsorg\"},\"opplysninger\":[]}\n";
        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertThat(del.getBegrunnelse()).as(HistorikkMigreringConstants.BEGRUNNELSE).hasValueSatisfying(begrunnelse -> assertThat(begrunnelse).isEqualTo("sdfsdf sdf sdf"));
        assertEndretFelt(del, HistorikkEndretFeltType.ALENEOMSORG, null, HistorikkEndretFeltVerdiType.IKKE_ALENEOMSORG.getKode());
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_FOR_OMSORG.getKode()));
    }

    @Test
    public void skal_konvertere_soknadsfristvilkaret() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"testsest\",\"endredeFelter\":[{\"navn\":\"ErSoknadsfristVilkaretOppfyltForm.ApplicationInformation\",\"fraVerdi\":null,\"tilVerdi\":\"ikke oppfylt\"}],\"skjermlinke\":{\"faktaNavn\":\"soeknadsfrist\",\"punktNavn\":\"soeknadsfrist\",\"linkTekst\":\"Søknadsfrist\"},\"opplysninger\":[]}";
        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertThat(del.getBegrunnelse()).as(HistorikkMigreringConstants.BEGRUNNELSE).hasValueSatisfying(begrunnelse -> assertThat(begrunnelse).isEqualTo("testsest"));
        assertEndretFelt(del, HistorikkEndretFeltType.SOKNADSFRISTVILKARET, null, HistorikkEndretFeltVerdiType.IKKE_OPPFYLT.getKode());
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke -> assertThat(skjermlenke).isEqualTo(SkjermlenkeType.SOEKNADSFRIST.getKode()));
    }

    @Test
    public void skal_ikke_lage_aarsak_hvis_udefinert() {
        // Arrange
        String tekst = "{\"endredeFelter\":[],\"opplysninger\":[],\"hendelse\":\"Behandlingen er satt på vent med frist 27.03.2018\",\"aarsak\":\"Ikke definert\"}";
        lagreHistorikkinnslag(HistorikkinnslagType.BEH_VENT, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertThat(del.getAarsak()).isNotPresent();
    }

    @Test
    public void skal_bestille_brev_med_begrunnelse() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"Behandling avbrutt\",\"endredeFelter\":[],\"opplysninger\":[],\"hendelse\":\"Brevet er bestilt\"}";
        lagreHistorikkinnslag(HistorikkinnslagType.BREV_BESTILT, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertThat(del.getBegrunnelse()).hasValueSatisfying(begrunnelse -> assertThat(begrunnelse).isEqualTo(DokumentMalType.HENLEGG_BEHANDLING_DOK));
        assertThat(del.getHendelse()).hasValueSatisfying(hendelse ->
            assertThat(hendelse.getNavn()).isEqualTo(HistorikkinnslagType.BREV_BESTILT.getKode()));
    }

    @Test
    public void skal_sette_navn_verdi_for_inntekt_fra() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"asd\",\"endredeFelter\":[{\"navn\":\"Inntekt fra 973861778\",\"fraVerdi\":null,\"tilVerdi\":540000.0}],\"skjermlinke\":{\"faktaNavn\":\"beregning\",\"punktNavn\":\"beregning\",\"linkTekst\":\"Beregning\"},\"opplysninger\":[],\"resultat\":\"Grunnlag for beregnet årsinntekt\"}";
        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertThat(del.getBegrunnelse()).hasValueSatisfying(begrunnelse -> assertThat(begrunnelse).isEqualTo("asd"));
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke ->
            assertThat(skjermlenke).isEqualTo(SkjermlenkeType.BEREGNING_FORELDREPENGER.getKode()));
        assertThat(del.getResultat()).as(HistorikkMigreringConstants.RESULTAT).hasValueSatisfying(resultat ->
            assertThat(resultat).isEqualTo(HistorikkResultatType.BEREGNET_AARSINNTEKT.getKode()));

        assertEndretFelt(del, HistorikkEndretFeltType.INNTEKT_FRA_ARBEIDSFORHOLD, null, "540000");
    }

    @Test
    public void skal_konvertere_hvis_begrunnelse_er_null() {
        // Arrange
        String tekst = "{\"begrunnelse\":null,\"endredeFelter\":[{\"navn\":\"SjekkFodselDokForm.DokumentasjonForeligger\",\"fraVerdi\":false,\"tilVerdi\":true}],\"skjermlinke\":{\"faktaNavn\":\"foedsel\",\"punktNavn\":\"foedsel\",\"linkTekst\":\"Fødsel\"},\"opplysninger\":[{\"verdi\":1,\"navn\":\"Historikk.Template.5.AntallBarn\"}]}";
        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertThat(del.getBegrunnelse()).isNotPresent();
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke ->
            assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_FOEDSEL.getKode()));

        Optional<HistorikkinnslagFelt> endretFeltOpt = del.getEndretFelt(HistorikkEndretFeltType.DOKUMENTASJON_FORELIGGER);
        assertThat(endretFeltOpt).hasValueSatisfying(endretFelt -> {
            assertThat(endretFelt.getNavn()).as(HistorikkMigreringConstants.NAVN).isEqualTo(HistorikkEndretFeltType.DOKUMENTASJON_FORELIGGER.getKode());
            assertThat(endretFelt.getFraVerdi()).as(HistorikkMigreringConstants.FRA_VERDI).isEqualTo("false");
            assertThat(endretFelt.getTilVerdi()).as(HistorikkMigreringConstants.TIL_VERDI).isEqualTo("true");
        });
    }

    @Test
    public void skal_konvertere_AdopsjonVilkarForm_Adopsjon() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"fdffdd\",\"endredeFelter\":[{\"navn\":\"AdopsjonVilkarForm.Adopsjon\",\"fraVerdi\":null,\"tilVerdi\":\"oppfylt\"}],\"skjermlinke\":{\"faktaNavn\":\"adopsjon\",\"punktNavn\":\"adopsjon\",\"linkTekst\":\"Adopsjon\"},\"opplysninger\":[]}";
        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertThat(del.getBegrunnelse()).hasValueSatisfying(begrunnelse ->
            assertThat(begrunnelse).isEqualTo("fdffdd"));
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke ->
            assertThat(skjermlenke).isEqualTo(SkjermlenkeType.PUNKT_FOR_ADOPSJON.getKode()));

        Optional<HistorikkinnslagFelt> endretFeltOpt = del.getEndretFelt(HistorikkEndretFeltType.ADOPSJONSVILKARET);
        assertThat(endretFeltOpt).hasValueSatisfying(endretFelt -> {
            assertThat(endretFelt.getNavn()).as(HistorikkMigreringConstants.NAVN).isEqualTo(HistorikkEndretFeltType.ADOPSJONSVILKARET.getKode());
            assertThat(endretFelt.getFraVerdi()).as(HistorikkMigreringConstants.FRA_VERDI).isNull();
            assertThat(endretFelt.getTilVerdi()).as(HistorikkMigreringConstants.TIL_VERDI).isEqualTo(HistorikkEndretFeltVerdiType.OPPFYLT.getKode());
        });
    }

    @Test
    public void skal_konvertere_SjekkFodselDokForm_BrukAntallISoknad() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"test\",\"endredeFelter\":[{\"navn\":\"SjekkFodselDokForm.DokumentasjonForeligger\",\"fraVerdi\":null,\"tilVerdi\":true},{\"navn\":\"SjekkFodselDokForm.BrukAntallISoknad\",\"fraVerdi\":null,\"tilVerdi\":true}],\"skjermlinke\":{\"faktaNavn\":\"foedsel\",\"punktNavn\":\"foedsel\",\"linkTekst\":\"Fødsel\"},\"opplysninger\":[{\"verdi\":1,\"navn\":\"Historikk.Template.5.AntallBarn\"}]}";
        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertThat(del.getBegrunnelse()).hasValueSatisfying(begrunnelse ->
            assertThat(begrunnelse).isEqualTo("test"));
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke ->
            assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_FOEDSEL.getKode()));

        Optional<HistorikkinnslagFelt> endretFeltOpt1 = del.getEndretFelt(HistorikkEndretFeltType.DOKUMENTASJON_FORELIGGER);
        assertThat(endretFeltOpt1).hasValueSatisfying(endretFelt -> {
            assertThat(endretFelt.getNavn()).as(HistorikkMigreringConstants.NAVN).isEqualTo(HistorikkEndretFeltType.DOKUMENTASJON_FORELIGGER.getKode());
            assertThat(endretFelt.getFraVerdi()).as(HistorikkMigreringConstants.FRA_VERDI).isNull();
            assertThat(endretFelt.getTilVerdi()).as(HistorikkMigreringConstants.TIL_VERDI).isEqualTo("true");
        });

        Optional<HistorikkinnslagFelt> endretFeltOpt2 = del.getEndretFelt(HistorikkEndretFeltType.BRUK_ANTALL_I_SOKNAD);
        assertThat(endretFeltOpt2).hasValueSatisfying(endretFelt -> {
            assertThat(endretFelt.getNavn()).as(HistorikkMigreringConstants.NAVN).isEqualTo(HistorikkEndretFeltType.BRUK_ANTALL_I_SOKNAD.getKode());
            assertThat(endretFelt.getFraVerdi()).as(HistorikkMigreringConstants.FRA_VERDI).isNull();
            assertThat(endretFelt.getTilVerdi()).as(HistorikkMigreringConstants.TIL_VERDI).isEqualTo("true");
        });
    }

    @Test
    public void skal_konvertere_nar_hendelse_mangelfull_papirsoknad() {
        // Arrange
        String tekst = "{\"endredeFelter\":[],\"opplysninger\":[],\"hendelse\":\"Mangelfull papirsøknad\"}";
        lagreHistorikkinnslag(HistorikkinnslagType.MANGELFULL_SØKNAD, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertThat(del.getHendelse()).hasValueSatisfying(hendelse ->
            assertThat(hendelse.getNavn()).isEqualTo(HistorikkinnslagType.MANGELFULL_SØKNAD.getKode()));
    }

    @Test
    public void skal_konvertere_endretfelt_brukantallitps() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"Medlemskap er ok. Barn sjekket mot folkeregisteret.\",\"endredeFelter\":[{\"navn\":\"SjekkFodselDokForm.DokumentasjonForeligger\",\"fraVerdi\":null,\"tilVerdi\":true},{\"navn\":\"SjekkFodselDokForm.BrukAntallITPS\",\"fraVerdi\":null,\"tilVerdi\":true}],\"skjermlinke\":{\"faktaNavn\":\"foedsel\",\"punktNavn\":\"foedsel\",\"linkTekst\":\"Fødsel\"},\"opplysninger\":[{\"verdi\":1,\"navn\":\"Historikk.Template.5.AntallBarn\"}]}";
        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertEndretFelt(del, HistorikkEndretFeltType.BRUK_ANTALL_I_TPS, null, "true");
        assertEndretFelt(del, HistorikkEndretFeltType.DOKUMENTASJON_FORELIGGER, null, "true");
    }

    @Test
    public void skal_konvertere_booleanTilVerdi_til_endretFeltVerdiType() {
        // Arrange
        String tekst = "{\"begrunnelse\":\"fghgfhgfh\",\"endredeFelter\":[{\"navn\":\"MedlemskapInfoPanel.ErSokerBosattINorge\",\"fraVerdi\":null,\"tilVerdi\":true}," + "{\"navn\":\"MedlemskapInfoPanel.EOSBorgerMedOppholdsrett\",\"fraVerdi\":null,\"tilVerdi\":true}," + "{\"navn\":\"MedlemskapInfoPanel.IkkeEOSBorgerMedLovligOpphold\",\"fraVerdi\":null,\"tilVerdi\":true}]," + "\"skjermlinke\":{\"faktaNavn\":\"medlemskap\",\"punktNavn\":\"medlemskap\",\"linkTekst\":\"Medlemskap\"},\"opplysninger\":[]}";
        lagreHistorikkinnslag(HistorikkinnslagType.FAKTA_ENDRET, tekst);

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(1);
        HistorikkinnslagDel del = deler.get(0);
        assertThat(del.getBegrunnelse()).hasValueSatisfying(begrunnelse ->
            assertThat(begrunnelse).isEqualTo("fghgfhgfh"));
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke ->
            assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_MEDLEMSKAP.getKode()));
        Optional<HistorikkinnslagFelt> endretFeltOpt1 = del.getEndretFelt(HistorikkEndretFeltType.ER_SOKER_BOSATT_I_NORGE);
        assertThat(endretFeltOpt1).hasValueSatisfying(endretFelt -> {
            assertThat(endretFelt.getNavn()).as(HistorikkMigreringConstants.NAVN).isEqualTo(HistorikkEndretFeltType.ER_SOKER_BOSATT_I_NORGE.getKode());
            assertThat(endretFelt.getFraVerdi()).as(HistorikkMigreringConstants.FRA_VERDI).isNull();
            assertThat(endretFelt.getTilVerdi()).as(HistorikkMigreringConstants.TIL_VERDI).isEqualTo(HistorikkEndretFeltVerdiType.BOSATT_I_NORGE.getKode());
        });
        Optional<HistorikkinnslagFelt> endretFeltOpt2 = del.getEndretFelt(HistorikkEndretFeltType.OPPHOLDSRETT_EOS);
        assertThat(endretFeltOpt2).hasValueSatisfying(endretFelt -> {
            assertThat(endretFelt.getNavn()).as(HistorikkMigreringConstants.NAVN).isEqualTo(HistorikkEndretFeltType.OPPHOLDSRETT_EOS.getKode());
            assertThat(endretFelt.getFraVerdi()).as(HistorikkMigreringConstants.FRA_VERDI).isNull();
            assertThat(endretFelt.getTilVerdi()).as(HistorikkMigreringConstants.TIL_VERDI).isEqualTo(HistorikkEndretFeltVerdiType.OPPHOLDSRETT.getKode());
        });
        Optional<HistorikkinnslagFelt> endretFeltOpt3 = del.getEndretFelt(HistorikkEndretFeltType.OPPHOLDSRETT_IKKE_EOS);
        assertThat(endretFeltOpt3).hasValueSatisfying(endretFelt -> {
            assertThat(endretFelt.getNavn()).as(HistorikkMigreringConstants.NAVN).isEqualTo(HistorikkEndretFeltType.OPPHOLDSRETT_IKKE_EOS.getKode());
            assertThat(endretFelt.getFraVerdi()).as(HistorikkMigreringConstants.FRA_VERDI).isNull();
            assertThat(endretFelt.getTilVerdi()).as(HistorikkMigreringConstants.TIL_VERDI).isEqualTo(HistorikkEndretFeltVerdiType.LOVLIG_OPPHOLD.getKode());
        });
    }


    @SuppressWarnings("deprecation")
    private Behandling lagreHistorikkinnslag(HistorikkinnslagType historikkinnslagType, String tekst) {
        Behandling behandling = ScenarioMorSøkerEngangsstønad.forFødsel().lagre(repositoryProvider);
        Historikkinnslag historikkinnslag1 = new Historikkinnslag();
        historikkinnslag1.setBehandling(behandling);
        historikkinnslag1.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);
        historikkinnslag1.setType(historikkinnslagType);
        historikkinnslag1.setTekst(tekst);
        historikkMigreringRepository.lagre(historikkinnslag1);
        entityManager.flush();
        return behandling;
    }

    @Test
    public void test_alle_dokumentmaltyper() {
        // Arrange
        IntStream.range(0, ANTALL_BEHANDLINGER).forEach(i -> {
            if (i % 100 == 0) {
                historikkMigreringRepository.flush();
            }
            opprettBehandlingMedHistorikkinnslag();
        });
        historikkMigreringRepository.flush();
        entityManager.clear();

        // Act
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();

        // Assert
        List<HistorikkinnslagDel> deler = hentHistorikkinnslagDeler();
        assertThat(deler).hasSize(ANTALL_BEHANDLINGER * ANTALL_HISTORIKKINNSLAGDELER_PER_BEHANDLING);
        deler.forEach(this::assertHistorikkinnslagDel);
    }


    private void assertHistorikkinnslagDel(HistorikkinnslagDel del) {
        HistorikkinnslagType type = del.getHistorikkinnslag().getType();
        switch (type.getKode()) {
            case "BEH_STARTET":
                assertBehStartet(del);
                break;
            case "VEDTAK_FATTET":
                assertVedtakFattet(del);
                break;
            case "SAK_RETUR":
                assertSakRetur(del);
                break;
            case "BEH_VENT":
                assertBehVent(del);
                break;
            case "FAKTA_ENDRET":
                assertFaktaEndret(del);
                break;
            case "OVERSTYRT":
                assertOverstyrt(del);
                break;
            default:
                throw new UnsupportedOperationException("Mangler støtte for historikkinnslagType " + type.getKode());
        }
    }

    private void assertBehStartet(HistorikkinnslagDel del) {
        assertThat(del.getHendelse()).as(HistorikkMigreringConstants.HENDELSE).hasValueSatisfying(hendelse ->
            assertThat(hendelse.getNavn()).isEqualTo(HistorikkinnslagType.BEH_STARTET.getKode()));
        assertThat(del.getEndredeFelt()).isEmpty();
        assertThat(del.getOpplysninger()).isEmpty();
    }

    private void assertVedtakFattet(HistorikkinnslagDel del) {
        assertThat(del.getHendelse()).hasValueSatisfying(hendelse ->
            assertThat(hendelse.getNavn()).isEqualTo(HistorikkinnslagType.VEDTAK_FATTET.getKode()));
        assertThat(del.getResultat()).as(HistorikkMigreringConstants.RESULTAT).hasValueSatisfying(resultat ->
            assertThat(resultat).isEqualTo(VedtakResultatType.INNVILGET.getKode()));
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke ->
            assertThat(skjermlenke).isEqualTo(SkjermlenkeType.VEDTAK.getKode()));
        assertThat(del.getEndredeFelt()).isEmpty();
        assertThat(del.getOpplysninger()).isEmpty();
    }

    private void assertSakRetur(HistorikkinnslagDel del) {
        assertThat(del.getEndredeFelt()).isEmpty();
        assertThat(del.getOpplysninger()).isEmpty();

        List<HistorikkinnslagTotrinnsvurdering> totrinnsvurderinger = del.getTotrinnsvurderinger(aksjonspunktRepository);
        assertThat(totrinnsvurderinger).hasSize(1);
        HistorikkinnslagTotrinnsvurdering vurdering = totrinnsvurderinger.get(0);

        List<String> aksjonspunktKoder = new ArrayList<>();
        aksjonspunktKoder.add("6004");
        aksjonspunktKoder.add("5005");
        assertThat(aksjonspunktKoder).contains(vurdering.getAksjonspunktDefinisjon().getKode());

        if (vurdering.getAksjonspunktDefinisjon().getKode().equals("6004")) {
            assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke ->
                assertThat(skjermlenke).as(HistorikkMigreringConstants.SKJERMLENKE).isEqualTo(SkjermlenkeType.PUNKT_FOR_ADOPSJON.getKode()));
            assertThat(vurdering.erGodkjent()).isFalse();
            assertThat(vurdering.getBegrunnelse()).isEqualTo("ddas");
        } else if (vurdering.getAksjonspunktDefinisjon().getKode().equals("5005")) {
            assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke ->
                assertThat(skjermlenke).as(HistorikkMigreringConstants.SKJERMLENKE).isEqualTo(SkjermlenkeType.FAKTA_OM_ADOPSJON.getKode()));
            assertThat(vurdering.erGodkjent()).isFalse();
            assertThat(vurdering.getBegrunnelse()).isEqualTo("asddas");
        }
    }

    private void assertBehVent(HistorikkinnslagDel del) {
        assertThat(del.getHendelse()).hasValueSatisfying(hendelse -> {
            assertThat(hendelse.getNavn()).as(HistorikkMigreringConstants.HENDELSE).isEqualTo(HistorikkinnslagType.BEH_VENT.getKode());
            assertThat(hendelse.getTilVerdi()).as(HistorikkMigreringConstants.TIL_VERDI).isEqualTo("18.12.2017");
        });
        assertThat(del.getEndredeFelt()).isEmpty();
        assertThat(del.getOpplysninger()).isEmpty();
    }

    private void assertFaktaEndret(HistorikkinnslagDel del) {
        assertThat(del.getBegrunnelse()).hasValueSatisfying(begrunnelse ->
            assertThat(begrunnelse).isEqualTo("sdf sdf sdf sdf sdf "));
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke ->
            assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_FOEDSEL.getKode()));

        assertEndretFelt(del, HistorikkEndretFeltType.DOKUMENTASJON_FORELIGGER, "false", "true");
        assertEndretFelt(del, HistorikkEndretFeltType.ANTALL_BARN, "0", "1");

        Optional<HistorikkinnslagFelt> antallBarnOpplysningOpt = del.getOpplysning(HistorikkOpplysningType.ANTALL_BARN);
        assertThat(antallBarnOpplysningOpt).hasValueSatisfying(antallBarn -> {
            assertThat(antallBarn.getNavn()).isEqualTo(HistorikkOpplysningType.ANTALL_BARN.getKode());
            assertThat(antallBarn.getFraVerdi()).isNull();
            assertThat(antallBarn.getTilVerdi()).isEqualTo("1");
        });
    }

    private void assertOverstyrt(HistorikkinnslagDel del) {
        assertThat(del.getBegrunnelse()).hasValueSatisfying(begrunnelse ->
            assertThat(begrunnelse).isEqualTo("ojioki"));
        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke ->
            assertThat(skjermlenke).isEqualTo(SkjermlenkeType.PUNKT_FOR_ADOPSJON.getKode()));
        assertEndretFelt(del, HistorikkEndretFeltType.OVERSTYRT_VURDERING, HistorikkEndretFeltVerdiType.VILKAR_IKKE_OPPFYLT.getKode(), HistorikkEndretFeltVerdiType.VILKAR_OPPFYLT.getKode());
    }

    private void assertEndretFelt(HistorikkinnslagDel del, HistorikkEndretFeltType endretFeltType, String
        expectedFraVerdi, String expectedTilVerdi) {
        Optional<HistorikkinnslagFelt> endretFeltOpt = del.getEndretFelt(endretFeltType);
        assertThat(endretFeltOpt).as(HistorikkMigreringConstants.ENDRET_FELT).hasValueSatisfying(dokumentasjonForeligger -> {
            assertThat(dokumentasjonForeligger.getNavn()).as(HistorikkMigreringConstants.NAVN).isEqualTo(endretFeltType.getKode());
            assertThat(dokumentasjonForeligger.getFraVerdi()).as(HistorikkMigreringConstants.FRA_VERDI).isEqualTo(expectedFraVerdi);
            assertThat(dokumentasjonForeligger.getTilVerdi()).as(HistorikkMigreringConstants.TIL_VERDI).isEqualTo(expectedTilVerdi);
        });
    }

    @SuppressWarnings("unchecked")
    private List<HistorikkinnslagDel> hentHistorikkinnslagDeler() {
        Query query = entityManager.createQuery("SELECT del FROM HistorikkinnslagDel del");
        return query.getResultList();
    }

    private void opprettBehandlingMedHistorikkinnslag() {
        Behandling behandling = ScenarioMorSøkerEngangsstønad.forFødsel().lagre(repositoryProvider);
        testDataProvider.opprettHistorikkinnslag(behandling);
    }
}
