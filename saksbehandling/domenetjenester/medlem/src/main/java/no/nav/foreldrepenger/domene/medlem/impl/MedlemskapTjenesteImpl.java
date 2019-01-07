package no.nav.foreldrepenger.domene.medlem.impl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapVilkårPeriodeGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapsvilkårPerioder;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.foreldrepenger.domene.medlem.api.AvklarFortsattMedlemskapAksjonspunktDto;
import no.nav.foreldrepenger.domene.medlem.api.BekreftBosattVurderingAksjonspunktDto;
import no.nav.foreldrepenger.domene.medlem.api.BekreftErMedlemVurderingAksjonspunktDto;
import no.nav.foreldrepenger.domene.medlem.api.BekreftOppholdVurderingAksjonspunktDto;
import no.nav.foreldrepenger.domene.medlem.api.EndringsresultatPersonopplysningerForMedlemskap;
import no.nav.foreldrepenger.domene.medlem.api.EndringsresultatPersonopplysningerForMedlemskap.EndretAttributt;
import no.nav.foreldrepenger.domene.medlem.api.FinnMedlemRequest;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.Medlemskapsperiode;
import no.nav.foreldrepenger.domene.medlem.api.UtledVurderingsdatoerForMedlemskapTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.VurderMedlemskap;
import no.nav.foreldrepenger.domene.medlem.api.VurderMedlemskapTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.VurderingsÅrsak;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class MedlemskapTjenesteImpl implements MedlemTjeneste {

    private static Map<MedlemResultat, AksjonspunktDefinisjon> mapMedlemResulatTilAkDef = new HashMap<>();

    static {
        mapMedlemResulatTilAkDef.put(MedlemResultat.AVKLAR_OM_ER_BOSATT, AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT);
        mapMedlemResulatTilAkDef.put(MedlemResultat.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE, AksjonspunktDefinisjon.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE);
        mapMedlemResulatTilAkDef.put(MedlemResultat.AVKLAR_LOVLIG_OPPHOLD, AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD);
        mapMedlemResulatTilAkDef.put(MedlemResultat.AVKLAR_OPPHOLDSRETT, AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT);
    }

    private MedlemEndringssjekkerProvider endringssjekkerProvider;
    private MedlemskapRepository medlemskapRepository;
    private KodeverkTabellRepository kodeverkTabellRepository;
    private HentMedlemskapFraRegister hentMedlemskapFraRegister;
    private GrunnlagRepositoryProvider repositoryProvider;
    private MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private PersonopplysningTjeneste personopplysningTjeneste;
    private UtledVurderingsdatoerForMedlemskapTjeneste utledVurderingsdatoerTjeneste;
    private VurderMedlemskapTjeneste vurderMedlemskapTjeneste;

    MedlemskapTjenesteImpl() {
        // CDI
    }

    @Inject
    public MedlemskapTjenesteImpl(MedlemEndringssjekkerProvider endringssjekkerProvider, GrunnlagRepositoryProvider repositoryProvider,
                                  HentMedlemskapFraRegister hentMedlemskapFraRegister, MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository,
                                  SkjæringstidspunktTjeneste skjæringstidspunktTjeneste, PersonopplysningTjeneste personopplysningTjeneste,
                                  UtledVurderingsdatoerForMedlemskapTjeneste utledVurderingsdatoerForMedlemskapTjeneste, VurderMedlemskapTjeneste vurderMedlemskapTjeneste) {
        this.endringssjekkerProvider = endringssjekkerProvider;
        this.hentMedlemskapFraRegister = hentMedlemskapFraRegister;
        this.repositoryProvider = repositoryProvider;
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.medlemskapVilkårPeriodeRepository = medlemskapVilkårPeriodeRepository;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.utledVurderingsdatoerTjeneste = utledVurderingsdatoerForMedlemskapTjeneste;
        this.vurderMedlemskapTjeneste = vurderMedlemskapTjeneste;
        KodeverkRepository kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.kodeverkTabellRepository = kodeverkRepository.getKodeverkTabellRepository();
    }

    @Override
    public List<Medlemskapsperiode> finnMedlemskapPerioder(FinnMedlemRequest finnMedlemRequest) {
        return hentMedlemskapFraRegister.finnMedlemskapPerioder(finnMedlemRequest);
    }

    @Override
    public void bekreftErMedlem(Behandling behandling, String manuellVurderingKode) {
        MedlemskapManuellVurderingType medlemskapManuellVurderingType = kodeverkTabellRepository
            .finnMedlemskapManuellVurderingType(manuellVurderingKode);

        Optional<VurdertMedlemskap> vurdertMedlemskap = medlemskapRepository.hentVurdertMedlemskap(behandling);

        VurdertMedlemskap nytt = new VurdertMedlemskapBuilder(vurdertMedlemskap)
            .medMedlemsperiodeManuellVurdering(medlemskapManuellVurderingType)
            .build();

        medlemskapRepository.lagreMedlemskapVurdering(behandling, nytt);
    }

    @Override
    public void aksjonspunktBekreftMeldlemVurdering(Behandling behandling, BekreftErMedlemVurderingAksjonspunktDto adapter) {
        new BekreftErMedlemVurderingAksjonspunkt(repositoryProvider).oppdater(behandling, adapter);
    }

    @Override
    public void aksjonspunktBekreftOppholdVurdering(Behandling behandling, BekreftOppholdVurderingAksjonspunktDto adapter) {
        new BekreftOppholdsrettVurderingAksjonspunkt(repositoryProvider).oppdater(behandling, adapter);
    }

    @Override
    public void aksjonspunktBekreftBosattVurdering(Behandling behandling, BekreftBosattVurderingAksjonspunktDto adapter) {
        new BekreftBosattVurderingAksjonspunkt(repositoryProvider).oppdater(behandling, adapter);
    }

    @Override
    public void aksjonspunktAvklarFortsattMedlemskap(Behandling behandling, AvklarFortsattMedlemskapAksjonspunktDto adapter) {
        new AvklarFortsattMedlemskapAksjonspunkt(repositoryProvider).oppdater(behandling, adapter);
    }

    @Override
    public Optional<MedlemskapAggregat> hentMedlemskap(Behandling behandling) {
        return medlemskapRepository.hentMedlemskap(behandling);
    }

    @Override
    public boolean oppdaterMedlemskapHvisEndret(Behandling behandling, Optional<MedlemskapAggregat> funnetMedlemskap,
                                                List<RegistrertMedlemskapPerioder> eksisterendePerioder,
                                                List<RegistrertMedlemskapPerioder> nyePerioder) {

        boolean endringsresultat = this.erMedlemskapPerioderEndret(behandling, funnetMedlemskap, eksisterendePerioder, nyePerioder);

        if (endringsresultat) {
            medlemskapRepository.lagreMedlemskapRegisterOpplysninger(behandling, nyePerioder);
        }
        return endringsresultat;

    }

    @Override
    public boolean erMedlemskapPerioderEndret(Behandling behandling, Optional<MedlemskapAggregat> funnetMedlemskap,
                                              List<RegistrertMedlemskapPerioder> eksisterendePerioder,
                                              List<RegistrertMedlemskapPerioder> nyePerioder) {

        MedlemEndringssjekker endringssjekker = endringssjekkerProvider.getEndringssjekker(behandling);
        return endringssjekker.erEndret(funnetMedlemskap, eksisterendePerioder, nyePerioder);
    }

    @Override
    public EndringsresultatSnapshot finnAktivGrunnlagId(Behandling behandling) {
        Optional<Long> funnetId = medlemskapRepository.hentIdPåAktivMedlemskap(behandling);
        return funnetId
            .map(id -> EndringsresultatSnapshot.medSnapshot(MedlemskapAggregat.class, id))
            .orElse(EndringsresultatSnapshot.utenSnapshot(MedlemskapAggregat.class));
    }

    @Override
    // TODO Diamant (Denne gjelder kun revurdering og foreldrepenger, bør eksponeres som egen tjeneste for FP + BT004)
    public EndringsresultatPersonopplysningerForMedlemskap søkerHarEndringerIPersonopplysninger(Behandling revurderingBehandling) {

        EndringsresultatPersonopplysningerForMedlemskap.Builder builder = EndringsresultatPersonopplysningerForMedlemskap.builder();
        if (revurderingBehandling.erRevurdering()) {
            Optional<PersonopplysningerAggregat> historikkAggregat =
                personopplysningTjeneste.hentGjeldendePersoninformasjonForPeriodeHvisEksisterer(revurderingBehandling, DatoIntervallEntitet.fraOgMedTilOgMed(finnStartdato(revurderingBehandling), LocalDate.now()));

            historikkAggregat.ifPresent(historikk -> {
                sjekkEndringer(historikk.getStatsborgerskapFor(revurderingBehandling.getAktørId()).stream()
                    .map(e -> new ElementMedGyldighetsintervallWrapper<>(e.getStatsborgerskap(), e.getPeriode())), builder, EndretAttributt.StatsborgerskapRegion);

                sjekkEndringer(historikk.getPersonstatuserFor(revurderingBehandling.getAktørId()).stream()
                    .map(e -> new ElementMedGyldighetsintervallWrapper<>(e.getPersonstatus(), e.getPeriode())), builder, EndretAttributt.Personstatus);

                sjekkEndringer(historikk.getAdresserFor(revurderingBehandling.getAktørId()).stream()
                    .map(e -> new ElementMedGyldighetsintervallWrapper<>(e.getAdresseType(), e.getPeriode())), builder, EndretAttributt.Adresse);
            });
        }
        return builder.build();
    }

    @Override
    public Map<LocalDate, VurderMedlemskap> utledVurderingspunkterMedAksjonspunkt(Behandling behandling) {
        final Map<LocalDate, Set<VurderingsÅrsak>> vurderingsdatoer = utledVurderingsdatoerTjeneste.finnVurderingsdatoerMedÅrsak(behandling.getId());
        final HashMap<LocalDate, VurderMedlemskap> map = new HashMap<>();
        for (LocalDate vurderingsdato : vurderingsdatoer.keySet()) {
            final Set<MedlemResultat> vurderinger = vurderMedlemskapTjeneste.vurderMedlemskap(behandling.getId(), vurderingsdato);
            map.put(vurderingsdato, mapTilVurderMeldemspa(vurderinger, vurderingsdatoer.get(vurderingsdato)));
        }
        return map;
    }

    private VurderMedlemskap mapTilVurderMeldemspa(Set<MedlemResultat> vurderinger, Set<VurderingsÅrsak> vurderingsÅrsaks) {
        final Set<AksjonspunktDefinisjon> aksjonspunkter = vurderinger.stream()
            .map(vu -> Optional.ofNullable(mapMedlemResulatTilAkDef.get(vu)).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        return new VurderMedlemskap(aksjonspunkter, vurderingsÅrsaks);
    }

    @Override
    public DiffResult diffResultat(EndringsresultatDiff idDiff, FagsakYtelseType ytelseType, boolean kunSporedeEndringer) {
        Objects.requireNonNull(idDiff.getGrunnlagId1(), "kan ikke diffe når id1 ikke er oppgitt");
        Objects.requireNonNull(idDiff.getGrunnlagId2(), "kan ikke diffe når id2 ikke er oppgitt");

        return medlemskapRepository.diffResultat(idDiff.getGrunnlagId1(), idDiff.getGrunnlagId2(), ytelseType, kunSporedeEndringer);
    }

    private <T extends Kodeliste> void sjekkEndringer(Stream<ElementMedGyldighetsintervallWrapper<T>> elementer, EndringsresultatPersonopplysningerForMedlemskap.Builder builder, EndretAttributt endretAttributt) {
        List<ElementMedGyldighetsintervallWrapper<T>> endringer = elementer
            .sorted(Comparator.comparing(ElementMedGyldighetsintervallWrapper::sortPeriode))
            .distinct().collect(Collectors.toList());

        leggTilEndringer(endringer, builder, endretAttributt);
    }

    private <T extends Kodeliste> void leggTilEndringer(List<ElementMedGyldighetsintervallWrapper<T>> endringer, EndringsresultatPersonopplysningerForMedlemskap.Builder builder, EndretAttributt endretAttributt) {
        if (endringer != null && endringer.size() > 1) {
            for (int i = 0; i < endringer.size() - 1; i++) {
                String endretFra = endringer.get(i).element.getNavn();
                String endretTil = endringer.get(i + 1).element.getNavn();
                DatoIntervallEntitet periode = endringer.get(i + 1).gylidghetsintervall;
                builder.leggTilEndring(endretAttributt, periode, endretFra, endretTil);
            }
        }
    }

    private LocalDate finnStartdato(Behandling revurderingBehandling) {

        Optional<MedlemskapVilkårPeriodeGrunnlag> medlemskapsvilkårPeriodeGrunnlag = medlemskapVilkårPeriodeRepository.hentAggregatHvisEksisterer(revurderingBehandling.getOriginalBehandling().get().getBehandlingsresultat());

        LocalDate startDato = skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(revurderingBehandling);
        if (medlemskapsvilkårPeriodeGrunnlag.isPresent()) {
            LocalDate date = medlemskapsvilkårPeriodeGrunnlag.get()
                .getMedlemskapsvilkårPeriode()
                .getPerioder()
                .stream().map(MedlemskapsvilkårPerioder::getFom)
                .max(LocalDate::compareTo)
                .get();

            if (startDato.isBefore(date)) {
                startDato = date;
            }
        }

        return startDato.isAfter(LocalDate.now()) ? LocalDate.now() : startDato;
    }

    private static final class ElementMedGyldighetsintervallWrapper<T> {
        private final T element;
        private final DatoIntervallEntitet gylidghetsintervall;

        private ElementMedGyldighetsintervallWrapper(T element, DatoIntervallEntitet gylidghetsintervall) {
            Objects.nonNull(element);
            Objects.nonNull(gylidghetsintervall);
            this.element = element;
            this.gylidghetsintervall = gylidghetsintervall;
        }

        private static Long sortPeriode(ElementMedGyldighetsintervallWrapper<?> e) {
            return e.gylidghetsintervall.getFomDato().toEpochDay();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            if (obj instanceof ElementMedGyldighetsintervallWrapper<?>) {
                ElementMedGyldighetsintervallWrapper<?> other = (ElementMedGyldighetsintervallWrapper<?>) obj;
                return element.equals(other.element);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(element, gylidghetsintervall);
        }
    }
}
