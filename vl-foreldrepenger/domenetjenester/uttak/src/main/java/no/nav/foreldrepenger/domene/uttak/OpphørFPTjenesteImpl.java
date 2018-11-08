package no.nav.foreldrepenger.domene.uttak;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.domene.medlem.api.EndringsresultatPersonopplysningerForMedlemskap;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;

@ApplicationScoped
public class OpphørFPTjenesteImpl implements OpphørFPTjeneste {

    private BehandlingRepository behandlingRepository;
    private UttakRepository uttakRepository;
    private MedlemskapRepository medlemskapRepository;
    private MedlemTjeneste medlemTjeneste;
    private PersonopplysningTjeneste personopplysningTjeneste;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    OpphørFPTjenesteImpl() {
        //CDI
    }

    @Inject
    public OpphørFPTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                PersonopplysningTjeneste personopplysningTjeneste,
                                MedlemTjeneste medlemTjeneste,
                                SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.medlemTjeneste = medlemTjeneste;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public Optional<LocalDate> getFørsteStønadsDato(Behandling behandling) {
        return
            finnOriginalInnvilgetBehandling(behandling)
                .map(this::hentUttakResultatFor)
                .map(this::finnFørsteFomDato)
                .orElse(Optional.empty());
    }

    private Optional<LocalDate> finnFørsteFomDato(UttakResultatEntitet uttakResultat) {
        return uttakResultat.getGjeldendePerioder().getPerioder()
            .stream()
            .filter(periode -> PeriodeResultatType.INNVILGET.equals(periode.getPeriodeResultatType()))
            .min(Comparator.comparing(UttakResultatPeriodeEntitet::getFom))
            .map(UttakResultatPeriodeEntitet::getFom);
    }

    private Optional<Behandling> finnOriginalInnvilgetBehandling(Behandling behandling) {
        return behandlingRepository
            .finnSisteAvsluttedeIkkeHenlagteBehandling(behandling.getFagsak().getId())
            .map(Behandling::getBehandlingsresultat)
            .map(Behandlingsresultat::getBehandlingVedtak)
            .filter(behandlingVedtak -> behandlingVedtak.getVedtakResultatType().equals(VedtakResultatType.INNVILGET))
            .map(BehandlingVedtak::getBehandlingsresultat)
            .map(Behandlingsresultat::getBehandling);
    }

    @Override
    public Optional<LocalDate> getOpphørsdato(Behandling behandling) {
        if (!behandling.getBehandlingsresultat().isBehandlingsresultatOpphørt()) {
            return Optional.empty();
        }

        LocalDate skjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(behandling);
        Optional<LocalDate> opphørsdatoFraUttak = utledOpphørsdatoFraUttak(hentUttakResultatFor(behandling), skjæringstidspunkt);
        Optional<LocalDate> endringIMedlemskapDato = utledOpphørsDatoFraEndringIMedlemskap(behandling).filter(date -> date.isAfter(skjæringstidspunkt));

        if (endringIMedlemskapDato.isPresent() && endringIMedlemskapDato.get().isBefore(opphørsdatoFraUttak.orElse(LocalDate.MAX))) {
            return endringIMedlemskapDato;
        } else if (opphørsdatoFraUttak.isPresent()) {
            return opphørsdatoFraUttak;
        } else {
            return Optional.ofNullable(skjæringstidspunkt);
        }
    }

    private UttakResultatEntitet hentUttakResultatFor(Behandling behandling) {
        return uttakRepository.hentUttakResultatHvisEksisterer(behandling).orElse(null);
    }

    private Optional<LocalDate> utledOpphørsdatoFraUttak(UttakResultatEntitet uttakResultat, LocalDate skjæringstidspunkt) {
        if (uttakResultat == null || uttakResultat.getGjeldendePerioder().getPerioder().isEmpty()) {
            return Optional.empty();
        }
        Set<PeriodeResultatÅrsak> opphørsAvslagÅrsaker = IkkeOppfyltÅrsak.opphørsAvslagÅrsaker();
        List<UttakResultatPeriodeEntitet> perioder = uttakResultat.getGjeldendePerioder().getPerioder()
            .stream()
            .sorted(Comparator.comparing(UttakResultatPeriodeEntitet::getFom).reversed())
            .collect(Collectors.toList());

        // Finn fom-dato i første periode av de siste sammenhengende periodene med opphørårsaker
        LocalDate fom = null;
        for (UttakResultatPeriodeEntitet periode : perioder) {
            if (opphørsAvslagÅrsaker.contains(periode.getPeriodeResultatÅrsak())) {
                fom = periode.getFom();
            } else if (fom != null && PeriodeResultatType.INNVILGET.equals(periode.getPeriodeResultatType())) {
                return Optional.of(fom);
            }
        }

        // => fom = null, eller tidligste periode er opphørt eller avslått
        return fom == null ? Optional.empty() : Optional.ofNullable(skjæringstidspunkt);
    }

    private Optional<LocalDate> utledOpphørsDatoFraEndringIMedlemskap(Behandling behandling) {
        Optional<LocalDate> endringIMedlemskapGjeldendeFra = datoOppgittFraManuelVurdering(behandling.getId());
        Optional<PersonopplysningerAggregat> aggregatOptional = personopplysningTjeneste.hentPersonopplysningerHvisEksisterer(behandling);

        if (aggregatOptional.isPresent()) {
            EndringsresultatPersonopplysningerForMedlemskap endringerIPersonopplysninger = medlemTjeneste.søkerHarEndringerIPersonopplysninger(behandling);
            List<EndringsresultatPersonopplysningerForMedlemskap.Endring> endredeAttributter = endringerIPersonopplysninger.getEndredeAttributter();
            if (!endredeAttributter.isEmpty()) {
                if (!endringIMedlemskapGjeldendeFra.isPresent()) {
                    endringIMedlemskapGjeldendeFra = endringerIPersonopplysninger.getGjeldendeFra();
                }
            } else {
                /**
                 * Ingen endringer i personopplysninger (siden siste vedtatte medlemskapsperiode), så vi setter
                 * gjeldende f.o.m fra nyeste endring i personstatus. Denne vises b.a. ifm. aksjonspunkt 5022
                 */
                Personstatus personstatus = aggregatOptional.get().getPersonstatusFor(behandling.getAktørId());
                if (endringIMedlemskapGjeldendeFra.isPresent() && personstatus != null) {
                    if (endringIMedlemskapGjeldendeFra.get().isBefore(personstatus.getPeriode().getFomDato())) {
                        endringIMedlemskapGjeldendeFra = Optional.of(personstatus.getPeriode().getFomDato());
                    }
                }
            }
        }
        return endringIMedlemskapGjeldendeFra;
    }

    private Optional<LocalDate> datoOppgittFraManuelVurdering(Long behandlingId) {
        Optional<MedlemskapAggregat> medlemskapOpt = medlemskapRepository.hentMedlemskap(behandlingId);
        if (medlemskapOpt.isPresent()) {
            MedlemskapAggregat aggregat = medlemskapOpt.get();
            Optional<VurdertMedlemskap> vurdertMedlemskapOpt = aggregat.getVurdertMedlemskap();
            if (vurdertMedlemskapOpt.isPresent()) {
                VurdertMedlemskap vurdertMedlemskap = vurdertMedlemskapOpt.get();
                if (MedlemskapManuellVurderingType.SAKSBEHANDLER_SETTER_OPPHØR_AV_MEDL_PGA_ENDRINGER_I_TPS.equals(vurdertMedlemskap.getMedlemsperiodeManuellVurdering())) {
                    return Optional.of(((VurdertMedlemskapEntitet)vurdertMedlemskap).getFom());
                }
            }
        }
        return Optional.empty();
    }
}
