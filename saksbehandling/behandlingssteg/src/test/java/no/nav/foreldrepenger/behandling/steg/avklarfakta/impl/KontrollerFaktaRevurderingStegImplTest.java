package no.nav.foreldrepenger.behandling.steg.avklarfakta.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.steg.avklarfakta.api.KontrollerFaktaSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingerBuilder;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonAdresse;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Prosentsats;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KontrollerFaktaRevurderingStegImplTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private Behandling behandling;
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private PersonInformasjon.Builder personopplysningBuilder;

    @Inject
    @FagsakYtelseTypeRef("FP")
    @BehandlingTypeRef("BT-004")
    private KontrollerFaktaTjeneste kontrollerFaktaTjeneste;

    @Inject
    @FagsakYtelseTypeRef("FP")
    @BehandlingTypeRef("BT-004")
    private KontrollerFaktaSteg steg;

    // Trenger denne for å sette aktivt steg. Kunne med fordel heller ha vært mulig i scenariobuilder for behandling.
    @Inject
    private InternalManipulerBehandling internalManipulerBehandling;

    @Before
    public void oppsett() {
        LocalDate fødselsdato = LocalDate.now().minusYears(20);
        AktørId aktørId = new AktørId("1");

        ScenarioMorSøkerForeldrepenger førstegangScenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør()
            .medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD)
            .medBehandlingStegStart(BehandlingStegType.KONTROLLER_FAKTA);

        førstegangScenario.removeDodgyDefaultInntektArbeidYTelse();
        SykemeldingerBuilder builder = førstegangScenario.getSykemeldingerBuilder();
        SykemeldingBuilder sykemeldingBuilder = builder.sykemeldingBuilder("ASDF-ASDF-ASDF");
        sykemeldingBuilder.medPeriode(LocalDate.now(), LocalDate.now().plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(1234L)))
            .medGrad(new Prosentsats(100));
        builder.medSykemelding(sykemeldingBuilder);
        førstegangScenario.medSykemeldinger(builder);
        AktørId søkerAktørId = førstegangScenario.getDefaultBrukerAktørId();

        PersonInformasjon personInformasjon = førstegangScenario
            .opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .kvinne(søkerAktørId, SivilstandType.SAMBOER).statsborgerskap(Landkoder.USA)
            .build();

        førstegangScenario.medRegisterOpplysninger(personInformasjon);

        personopplysningBuilder = førstegangScenario.opprettBuilderForRegisteropplysninger();
        personopplysningBuilder.leggTilPersonopplysninger(
            Personopplysning.builder().aktørId(aktørId).sivilstand(SivilstandType.GIFT)
                .fødselsdato(fødselsdato).brukerKjønn(NavBrukerKjønn.KVINNE).navn("Marie Curie")
                .region(Region.UDEFINERT)
        ).leggTilAdresser(
            PersonAdresse.builder()
                .adresselinje1("dsffsd 13").aktørId(aktørId).land("USA")
                .adresseType(AdresseType.POSTADRESSE_UTLAND)
                .periode(fødselsdato, LocalDate.now())
        ).leggTilPersonstatus(
            Personstatus.builder().aktørId(aktørId).personstatus(PersonstatusType.UTVA)
                .periode(fødselsdato, LocalDate.now())
        ).leggTilStatsborgerskap(
            Statsborgerskap.builder().aktørId(aktørId)
                .periode(fødselsdato, LocalDate.now())
                .region(Region.UDEFINERT)
                .statsborgerskap(Landkoder.USA)
        );

        førstegangScenario.medRegisterOpplysninger(personopplysningBuilder.build());

        Behandling originalBehandling = førstegangScenario.lagre(repositoryProvider, resultatRepositoryProvider);
        // Legg til Uttaksperiodegrense -> dessverre ikke tilgjengelig i scenariobygger
        BehandlingLås lås = behandlingRepository.taSkriveLås(originalBehandling);
        behandlingRepository.lagre(originalBehandling, lås);
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(originalBehandling.getId());
        Uttaksperiodegrense uttaksperiodegrense = new Uttaksperiodegrense.Builder(behandlingsresultat)
            .medFørsteLovligeUttaksdag(LocalDate.now())
            .medMottattDato(LocalDate.now())
            .build();
        resultatRepositoryProvider.getUttakRepository().lagreUttaksperiodegrense(behandlingsresultat, uttaksperiodegrense);
        // Legg til Opptjeningsperidoe -> dessverre ikke tilgjengelig i scenariobygger
        resultatRepositoryProvider.getOpptjeningRepository().lagreOpptjeningsperiode(behandlingsresultat, LocalDate.now().minusYears(1), LocalDate.now());
        //Legg til fordelingsperiode

        ScenarioMorSøkerForeldrepenger revurderingScenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør()
            .medBehandlingType(BehandlingType.REVURDERING)
            .medRegisterOpplysninger(personopplysningBuilder.build())
            .medOriginalBehandling(originalBehandling, BehandlingÅrsakType.RE_ANNET);
        revurderingScenario.removeDodgyDefaultInntektArbeidYTelse();
        builder = revurderingScenario.getSykemeldingerBuilder();
        sykemeldingBuilder = builder.sykemeldingBuilder("ASDF-ASDF-ASDF");
        sykemeldingBuilder.medPeriode(LocalDate.now(), LocalDate.now().plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(1234L)))
            .medGrad(new Prosentsats(100));
        builder.medSykemelding(sykemeldingBuilder);
        revurderingScenario.medSykemeldinger(builder);

        behandling = revurderingScenario.lagre(repositoryProvider, resultatRepositoryProvider);
        //kopierer ytelsefordeling grunnlag

        // Nødvendig å sette aktivt steg for KOFAK revurdering
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.KONTROLLER_FAKTA);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
    }

    @Test
    public void skal_fjerne_aksjonspunkter_som_er_utledet_før_startpunktet() {
        Fagsak fagsak = behandling.getFagsak();
        // Arrange
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås);
        KodeverkTabellRepository kodeverkTabellRepository = repositoryProvider.getKodeverkRepository().getKodeverkTabellRepository();
        behandling.setStartpunkt(kodeverkTabellRepository.finnStartpunktType(StartpunktType.UTTAKSVILKÅR.getKode()));

        // Act
        List<AksjonspunktDefinisjon> aksjonspunkter = steg.utførSteg(kontekst).getAksjonspunktListe();

        // Assert
        assertThat(aksjonspunkter).doesNotContain(AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD);
    }

    @Test
    public void skal_ikke_fjerne_aksjonspunkter_som_er_utledet_etter_startpunktet() {
        Fagsak fagsak = behandling.getFagsak();
        // Arrange
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås);

        // Act
        List<AksjonspunktDefinisjon> aksjonspunkter = steg.utførSteg(kontekst).getAksjonspunktListe();

        // Assert
        assertThat(aksjonspunkter).contains(AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD);
        // Må verifisere at startpunkt er før aksjonpunktet for at assert ovenfor skal ha mening
        assertThat(behandling.getStartpunkt()).isEqualTo(StartpunktType.INNGANGSVILKÅR_MEDLEMSKAP);
    }

}    

