package no.nav.foreldrepenger.domene.personopplysning.impl;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingsgrunnlagKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

abstract class AbstractPersonopplysningTjenesteImpl {

    private PersonopplysningRepository personopplysningRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private BehandlingsgrunnlagKodeverkRepository behandlingsgrunnlagKodeverkRepository;

    AbstractPersonopplysningTjenesteImpl() {
        // CDI
    }

    AbstractPersonopplysningTjenesteImpl(BehandlingRepositoryProvider repositoryProvider, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        this.behandlingsgrunnlagKodeverkRepository = repositoryProvider.getBehandlingsgrunnlagKodeverkRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    public PersonopplysningerAggregat hentPersonopplysninger(Behandling behandling) {
        final LocalDate localDate = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
        return hentGjeldendePersoninformasjonPåTidspunkt(behandling, localDate);
    }

    public Optional<PersonopplysningerAggregat> hentPersonopplysningerHvisEksisterer(Behandling behandling) {
        final Optional<PersonopplysningGrunnlag> grunnlagOpt = getPersonopplysningRepository().hentPersonopplysningerHvisEksisterer(behandling);
        if (grunnlagOpt.isPresent()) {
            final LocalDate localDate = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
            return Optional.of(mapTilAggregat(behandling, localDate, grunnlagOpt.get()));
        }
        return Optional.empty();
    }

    public PersonopplysningerAggregat hentGjeldendePersoninformasjonPåTidspunkt(Behandling behandling, LocalDate tidspunkt) {
        final Optional<PersonopplysningGrunnlag> grunnlagOpt = getPersonopplysningRepository().hentPersonopplysningerHvisEksisterer(behandling);
        if (grunnlagOpt.isPresent()) {
            final Map<Landkoder, Region> landkoderRegionMap = getLandkoderOgRegion(grunnlagOpt.get());
            tidspunkt = tidspunkt == null ? LocalDate.now() : tidspunkt;
            return new PersonopplysningerAggregat(grunnlagOpt.get(), behandling.getAktørId(), DatoIntervallEntitet.fraOgMedTilOgMed(tidspunkt, tidspunkt.plusDays(1)), landkoderRegionMap);
        }
        throw new IllegalStateException("Utvikler feil: Har ikke innhentet opplysninger fra register enda.");
    }

    public Optional<PersonopplysningerAggregat> hentGjeldendePersoninformasjonPåTidspunktHvisEksisterer(Behandling behandling, LocalDate tidspunkt) {
        final Optional<PersonopplysningGrunnlag> grunnlagOpt = getPersonopplysningRepository().hentPersonopplysningerHvisEksisterer(behandling);
        if (grunnlagOpt.isPresent()) {
            final Map<Landkoder, Region> landkoderRegionMap = getLandkoderOgRegion(grunnlagOpt.get());
            tidspunkt = tidspunkt == null ? LocalDate.now() : tidspunkt;
            return Optional.of(new PersonopplysningerAggregat(grunnlagOpt.get(), behandling.getAktørId(), DatoIntervallEntitet.fraOgMedTilOgMed(tidspunkt, tidspunkt.plusDays(1)), landkoderRegionMap));
        }
        return Optional.empty();
    }

    public Optional<PersonopplysningerAggregat> hentGjeldendePersoninformasjonForPeriodeHvisEksisterer(Behandling behandling, DatoIntervallEntitet forPeriode) {
        final Optional<PersonopplysningGrunnlag> grunnlagOpt = getPersonopplysningRepository().hentPersonopplysningerHvisEksisterer(behandling);
        if (grunnlagOpt.isPresent()) {
            final Map<Landkoder, Region> landkoderRegionMap = getLandkoderOgRegion(grunnlagOpt.get());
            return Optional.of(new PersonopplysningerAggregat(grunnlagOpt.get(), behandling.getAktørId(), forPeriode, landkoderRegionMap));
        }
        return Optional.empty();
    }

    protected Map<Landkoder, Region> getLandkoderOgRegion(PersonopplysningGrunnlag grunnlag) {
        final List<Landkoder> landkoder = Optional.ofNullable(grunnlag.getRegisterVersjon())
            .map(PersonInformasjon::getStatsborgerskap)
            .orElse(Collections.emptyList())
            .stream()
            .map(Statsborgerskap::getStatsborgerskap)
            .collect(toList());
        return behandlingsgrunnlagKodeverkRepository.finnRegionForStatsborgerskap(landkoder);
    }

    protected PersonopplysningerAggregat mapTilAggregat(Behandling behandling, LocalDate tidspunkt, PersonopplysningGrunnlag grunnlag) {
        final Map<Landkoder, Region> landkoderRegionMap = getLandkoderOgRegion(grunnlag);
        tidspunkt = tidspunkt == null ? LocalDate.now() : tidspunkt;
        return new PersonopplysningerAggregat(grunnlag, behandling.getAktørId(), DatoIntervallEntitet.fraOgMedTilOgMed(tidspunkt, tidspunkt.plusDays(1)), landkoderRegionMap);
    }

    protected PersonopplysningRepository getPersonopplysningRepository() {
        return personopplysningRepository;
    }

}
