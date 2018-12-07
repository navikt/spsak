package no.nav.foreldrepenger.behandling.steg.avklarfakta.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandling.steg.avklarfakta.api.KontrollerFaktaSteg;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.StartpunktRef;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.domene.kontrollerfakta.BehandlingÅrsakTjeneste;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaTjeneste;
import no.nav.foreldrepenger.domene.kontrollerfakta.StartpunktTjeneste;

@BehandlingStegRef(kode = "KOFAK")
@BehandlingTypeRef("BT-004")
@FagsakYtelseTypeRef("FP")
@StartpunktRef
@ApplicationScoped
public class KontrollerFaktaRevurderingStegForeldrepengerImpl implements KontrollerFaktaSteg {
    private static final Logger LOGGER = LoggerFactory.getLogger(KontrollerFaktaRevurderingStegForeldrepengerImpl.class);

    private static final StartpunktType DEFAULT_STARTPUNKT = StartpunktType.INNGANGSVILKÅR_OPPLYSNINGSPLIKT;

    private BehandlingRepository behandlingRepository;

    private KontrollerFaktaTjeneste tjeneste;

    private BehandlingRepositoryProvider repositoryProvider;

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    private KodeverkTabellRepository kodeverkTabellRepository;

    private UttakRepository uttakRepository;

    private OpptjeningRepository opptjeningRepository;

    private StartpunktTjeneste startpunktTjeneste;

    private BehandlingÅrsakTjeneste behandlingÅrsakTjeneste;

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;


    KontrollerFaktaRevurderingStegForeldrepengerImpl() {
        // for CDI proxy
    }

    @Inject
    KontrollerFaktaRevurderingStegForeldrepengerImpl(BehandlingRepositoryProvider repositoryProvider,
                                                     @FagsakYtelseTypeRef("FP") @BehandlingTypeRef("BT-004") @StartpunktRef KontrollerFaktaTjeneste tjeneste,
                                                     StartpunktTjeneste startpunktTjeneste, BehandlingÅrsakTjeneste behandlingÅrsakTjeneste,
                                                     BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.repositoryProvider = repositoryProvider;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.tjeneste = tjeneste;
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.kodeverkTabellRepository = repositoryProvider.getKodeverkRepository().getKodeverkTabellRepository();
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.opptjeningRepository = repositoryProvider.getOpptjeningRepository();
        this.startpunktTjeneste = startpunktTjeneste;
        this.behandlingÅrsakTjeneste = behandlingÅrsakTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        if (erStartpunktInitiert(behandling)) {
            // Startpunkt kan bare initieres én gang, og det gjøres i dette steget.
            // Suksessive eksekveringer av stegets aksjonspunktsutledere skjer utenfor steget
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }
        Set<BehandlingÅrsakType> behandlingÅrsaker = behandlingÅrsakTjeneste.utledBehandlingÅrsakerMotOriginalBehandling(behandling);
        leggTilBehandlingsårsaker(behandling, behandlingÅrsaker);

        StartpunktType startpunkt = utledStartpunkt(behandling);
        behandling.setStartpunkt(startpunkt);

        List<AksjonspunktResultat> aksjonspunktResultater = tjeneste
            .utledAksjonspunkterTilHøyreForStartpunkt(behandling.getId(), startpunkt);
        kopierResultaterAvhengigAvStartpunkt(behandling, kontekst);

        TransisjonIdentifikator transisjon = TransisjonIdentifikator.forId("revurdering-fremoverhopp-til-" + startpunkt.getBehandlingSteg().getKode());
        return BehandleStegResultat.fremoverførtMedAksjonspunktResultater(transisjon, aksjonspunktResultater);
    }

    private StartpunktType utledStartpunkt(Behandling revurdering) {
        StartpunktType startpunkt;
        StartpunktType startpunktForGrunnlagsendringer = startpunktTjeneste.utledStartpunktMotOriginalBehandling(revurdering);
        Behandlingsresultat behandlingsresultatOrginalBehandling = revurdering.getOriginalBehandling().get().getBehandlingsresultat();
        if (revurdering.erManueltOpprettet()) {
            startpunkt = DEFAULT_STARTPUNKT;
        } else if (behandlingsresultatOrginalBehandling != null && behandlingsresultatOrginalBehandling.isVilkårAvslått()) {
            startpunkt = DEFAULT_STARTPUNKT;
        } else {
            startpunkt = startpunktForGrunnlagsendringer.equals(StartpunktType.UDEFINERT) ? DEFAULT_STARTPUNKT : startpunktForGrunnlagsendringer;
        }

        // Startpunkt for revurdering kan kun hoppe fremover; default dersom startpunkt passert
        if (behandlingskontrollTjeneste.erStegPassert(revurdering, startpunkt.getBehandlingSteg())) {
            startpunkt = DEFAULT_STARTPUNKT;
        }
        LOGGER.info("Revurdering {} har fått fastsatt startpunkt {} ", revurdering.getId(), startpunkt.getKode());// NOSONAR //$NON-NLS-1$
        return kodeverkTabellRepository.finnStartpunktType(startpunkt.getKode());
    }

    private void leggTilBehandlingsårsaker(Behandling behandling, Set<BehandlingÅrsakType> behandlingÅrsaker) {
        BehandlingÅrsak.Builder builder = BehandlingÅrsak.builder(new ArrayList<>(behandlingÅrsaker));
        behandling.getOriginalBehandling().ifPresent(builder::medOriginalBehandling);
        builder.buildFor(behandling);
    }

    boolean harOverlappendePeriodeMedFørsteStønadsdag(UttakResultatEntitet eksisterendeUttakResultat, UttakResultatEntitet uttakResultatBerørtAv) {
        UttakResultatPeriodeEntitet eksisterendeUttakFørstePeriode = eksisterendeUttakResultat.getGjeldendePerioder().getPerioder().get(0);
        List<UttakResultatPeriodeEntitet> perioderBerørtAv = uttakResultatBerørtAv.getGjeldendePerioder().getPerioder();
        return perioderBerørtAv.stream().anyMatch(p -> p.getTidsperiode().overlapper(eksisterendeUttakFørstePeriode.getTidsperiode()));
    }

    private boolean erStartpunktInitiert(Behandling behandling) {
        return !StartpunktType.UDEFINERT.equals(behandling.getStartpunkt());
    }

    @Override
    public void vedHoppOverBakover(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, BehandlingStegType tilSteg, BehandlingStegType fraSteg) {
        RyddRegisterData rydder = new RyddRegisterData(modell, repositoryProvider, behandling, kontekst);
        rydder.ryddRegisterdataForeldrepenger();
    }

    private void kopierResultaterAvhengigAvStartpunkt(Behandling revurdering, BehandlingskontrollKontekst kontekst) {
        Behandling origBehandling = revurdering.getOriginalBehandling()
            .orElseThrow(() -> new IllegalStateException("Original behandling mangler på revurdering - skal ikke skje"));

        revurdering = kopierVilkår(origBehandling, revurdering, kontekst);
        revurdering = kopierUttaksperiodegrense(revurdering, origBehandling);

        if (StartpunktType.BEREGNING.equals(revurdering.getStartpunkt())) {
            opptjeningRepository.kopierGrunnlagFraEksisterendeBehandling(origBehandling, revurdering);
        }

        if (StartpunktType.UTTAKSVILKÅR.equals(revurdering.getStartpunkt())) {
            opptjeningRepository.kopierGrunnlagFraEksisterendeBehandling(origBehandling, revurdering);
            beregningsgrunnlagRepository.kopierGrunnlagFraEksisterendeBehandling(origBehandling, revurdering, BeregningsgrunnlagTilstand.OPPRETTET);
        }

    }

    private Behandling kopierUttaksperiodegrense(Behandling revurdering, Behandling origBehandling) {
        // Kopier Uttaksperiodegrense - må alltid ha en søknadsfrist angitt
        Optional<Uttaksperiodegrense> funnetUttaksperiodegrense = uttakRepository.hentUttaksperiodegrenseHvisEksisterer(origBehandling.getId());
        if (funnetUttaksperiodegrense.isPresent()) {
            Uttaksperiodegrense origGrense = funnetUttaksperiodegrense.get();
            Uttaksperiodegrense uttaksperiodegrense = new Uttaksperiodegrense.Builder(revurdering)
                .medFørsteLovligeUttaksdag(origGrense.getFørsteLovligeUttaksdag())
                .medMottattDato(origGrense.getMottattDato())
                .build();
            uttakRepository.lagreUttaksperiodegrense(revurdering, uttaksperiodegrense);
            return behandlingRepository.hentBehandling(revurdering.getId());
        }
        return revurdering;
    }

    private Behandling kopierVilkår(Behandling origBehandling, Behandling revurdering, BehandlingskontrollKontekst kontekst) {
        VilkårResultat vilkårResultat = Optional.ofNullable(revurdering.getBehandlingsresultat())
            .map(Behandlingsresultat::getVilkårResultat)
            .orElseThrow(() -> new IllegalStateException("VilkårResultat skal alltid være opprettet ved revurdering"));
        VilkårResultat.Builder vilkårBuilder = VilkårResultat.builderFraEksisterende(vilkårResultat);

        StartpunktType startpunkt = revurdering.getStartpunkt();
        Set<VilkårType> vilkårtyperFørStartpunkt = StartpunktType.finnVilkårHåndtertInnenStartpunkt(startpunkt);
        Objects.requireNonNull(vilkårtyperFørStartpunkt, "Startpunkt " + startpunkt.getKode() +
            " støttes ikke for kopiering av vilkår ved revurdering");

        Set<Vilkår> vilkårFørStartpunkt = origBehandling.getBehandlingsresultat().getVilkårResultat().getVilkårene().stream()
            .filter(vilkår -> vilkårtyperFørStartpunkt.contains(vilkår.getVilkårType()))
            .collect(Collectors.toSet());
        kopierVilkår(vilkårBuilder, vilkårFørStartpunkt);
        vilkårBuilder.buildFor(revurdering);

        behandlingRepository.lagre(revurdering.getBehandlingsresultat().getVilkårResultat(), kontekst.getSkriveLås());
        behandlingRepository.lagre(revurdering, kontekst.getSkriveLås());
        return behandlingRepository.hentBehandling(revurdering.getId());
    }

    private void kopierVilkår(VilkårResultat.Builder vilkårBuilder, Set<Vilkår> vilkårne) {
        vilkårne
            .forEach(vilkår -> vilkårBuilder.leggTilVilkårResultat(vilkår.getVilkårType(), vilkår.getGjeldendeVilkårUtfall(), vilkår.getVilkårUtfallMerknad(),
                vilkår.getMerknadParametere(), vilkår.getAvslagsårsak(), vilkår.erManueltVurdert(), vilkår.erOverstyrt(), vilkår.getRegelEvaluering(), vilkår.getRegelInput()));
    }
}
