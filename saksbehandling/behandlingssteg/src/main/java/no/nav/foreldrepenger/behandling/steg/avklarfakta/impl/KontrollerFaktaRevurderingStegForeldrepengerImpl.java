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
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
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

    private static final StartpunktType DEFAULT_STARTPUNKT = StartpunktType.INNGANGSVILKÅR_MEDLEMSKAP;

    private BehandlingRepository behandlingRepository;

    private KontrollerFaktaTjeneste tjeneste;

    private GrunnlagRepositoryProvider repositoryProvider;

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
    KontrollerFaktaRevurderingStegForeldrepengerImpl(GrunnlagRepositoryProvider repositoryProvider,
                                                     ResultatRepositoryProvider resultatRepositoryProvider,
                                                     @FagsakYtelseTypeRef("FP") @BehandlingTypeRef("BT-004") @StartpunktRef KontrollerFaktaTjeneste tjeneste,
                                                     StartpunktTjeneste startpunktTjeneste, BehandlingÅrsakTjeneste behandlingÅrsakTjeneste,
                                                     BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.repositoryProvider = repositoryProvider;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.tjeneste = tjeneste;
        this.beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        this.kodeverkTabellRepository = repositoryProvider.getKodeverkRepository().getKodeverkTabellRepository();
        this.uttakRepository = resultatRepositoryProvider.getUttakRepository();
        this.opptjeningRepository = resultatRepositoryProvider.getOpptjeningRepository();
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
        Optional<Behandlingsresultat> behandlingsresultatOrginalBehandling = behandlingRepository.hentResultatHvisEksisterer(revurdering.getOriginalBehandling().get().getId());
        if (revurdering.erManueltOpprettet()) {
            startpunkt = DEFAULT_STARTPUNKT;
        } else if (behandlingsresultatOrginalBehandling.isPresent() && behandlingsresultatOrginalBehandling.get().isVilkårAvslått()) {
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

    private StartpunktType tidligsteStartpunkt(StartpunktType berørtStartpunkt, StartpunktType startpunktUtledetFraRegisterEndringer) {
        if (berørtStartpunkt.getRangering() < startpunktUtledetFraRegisterEndringer.getRangering()) {
            return berørtStartpunkt;
        }
        return startpunktUtledetFraRegisterEndringer;
    }

    private boolean berørtAvFlytterPåFørsteStønadsdag(Behandling behandling, Behandling behandlingBerørtAv) {
        UttakResultatEntitet eksisterendeUttakResultat = uttakRepository.hentUttakResultat(behandling.getOriginalBehandling().get());
        Optional<UttakResultatEntitet> uttakResultatBerørtAv = uttakRepository.hentUttakResultatHvisEksisterer(behandlingBerørtAv);
        if (uttakResultatBerørtAv.isPresent()) {
            return harOverlappendePeriodeMedFørsteStønadsdag(eksisterendeUttakResultat, uttakResultatBerørtAv.get());
        }
        return false;
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

        Behandlingsresultat originaltResultat = behandlingRepository.hentResultat(origBehandling.getId());
        Behandlingsresultat revurderingResultat = behandlingRepository.hentResultat(revurdering.getId());

        if (StartpunktType.BEREGNING.equals(revurdering.getStartpunkt())) {
            opptjeningRepository.kopierGrunnlagFraEksisterendeBehandling(revurdering, originaltResultat, revurderingResultat);
        }

        if (StartpunktType.UTTAKSVILKÅR.equals(revurdering.getStartpunkt())) {
            opptjeningRepository.kopierGrunnlagFraEksisterendeBehandling(revurdering, originaltResultat, revurderingResultat);
            beregningsgrunnlagRepository.kopierGrunnlagFraEksisterendeBehandling(origBehandling, revurdering, BeregningsgrunnlagTilstand.OPPRETTET);
        }

    }

    private Behandling kopierUttaksperiodegrense(Behandling revurdering, Behandling origBehandling) {
        // Kopier Uttaksperiodegrense - må alltid ha en søknadsfrist angitt
        Optional<Uttaksperiodegrense> funnetUttaksperiodegrense = uttakRepository.hentUttaksperiodegrenseHvisEksisterer(origBehandling.getId());
        if (funnetUttaksperiodegrense.isPresent()) {
            Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(revurdering.getId());
            Uttaksperiodegrense origGrense = funnetUttaksperiodegrense.get();
            Uttaksperiodegrense uttaksperiodegrense = new Uttaksperiodegrense.Builder(behandlingsresultat)
                .medFørsteLovligeUttaksdag(origGrense.getFørsteLovligeUttaksdag())
                .medMottattDato(origGrense.getMottattDato())
                .build();
            uttakRepository.lagreUttaksperiodegrense(behandlingsresultat, uttaksperiodegrense);
            return behandlingRepository.hentBehandling(revurdering.getId());
        }
        return revurdering;
    }

    private Behandling kopierVilkår(Behandling origBehandling, Behandling revurdering, BehandlingskontrollKontekst kontekst) {
        Optional<Behandlingsresultat> resultatOpt = behandlingRepository.hentResultatHvisEksisterer(revurdering.getId());
        VilkårResultat vilkårResultat = resultatOpt
            .map(Behandlingsresultat::getVilkårResultat)
            .orElseThrow(() -> new IllegalStateException("VilkårResultat skal alltid være opprettet ved revurdering"));
        VilkårResultat.Builder vilkårBuilder = VilkårResultat.builderFraEksisterende(vilkårResultat);

        StartpunktType startpunkt = revurdering.getStartpunkt();
        Set<VilkårType> vilkårtyperFørStartpunkt = StartpunktType.finnVilkårHåndtertInnenStartpunkt(startpunkt);
        Objects.requireNonNull(vilkårtyperFørStartpunkt, "Startpunkt " + startpunkt.getKode() +
            " støttes ikke for kopiering av vilkår ved revurdering");

        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(origBehandling.getId());
        Behandlingsresultat revurderingsResultat = resultatOpt.orElse(Behandlingsresultat.opprettFor(revurdering));
        Set<Vilkår> vilkårFørStartpunkt = behandlingsresultat.getVilkårResultat().getVilkårene().stream()
            .filter(vilkår -> vilkårtyperFørStartpunkt.contains(vilkår.getVilkårType()))
            .collect(Collectors.toSet());
        kopierVilkår(vilkårBuilder, vilkårFørStartpunkt);
        vilkårBuilder.buildFor(revurderingsResultat);

        behandlingRepository.lagre(revurderingsResultat.getVilkårResultat(), kontekst.getSkriveLås());
        behandlingRepository.lagre(revurderingsResultat, kontekst.getSkriveLås());
        return revurdering;
    }

    private void kopierVilkår(VilkårResultat.Builder vilkårBuilder, Set<Vilkår> vilkårne) {
        vilkårne
            .forEach(vilkår -> vilkårBuilder.leggTilVilkårResultat(vilkår.getVilkårType(), vilkår.getGjeldendeVilkårUtfall(), vilkår.getVilkårUtfallMerknad(),
                vilkår.getMerknadParametere(), vilkår.getAvslagsårsak(), vilkår.erManueltVurdert(), vilkår.erOverstyrt(), vilkår.getRegelEvaluering(), vilkår.getRegelInput()));
    }
}
