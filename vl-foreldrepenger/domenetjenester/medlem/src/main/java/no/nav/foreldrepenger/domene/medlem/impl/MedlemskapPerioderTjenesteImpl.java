package no.nav.foreldrepenger.domene.medlem.impl;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapDekningType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.domene.medlem.api.MedlemskapPerioderTjeneste;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.konfig.Tid;

/**
 * Gjenbrukbar logikk for behandling av data fra MEDL.
 */
@ApplicationScoped
public class MedlemskapPerioderTjenesteImpl implements MedlemskapPerioderTjeneste {

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private int antallMånederFørSkjæringsdato;
    private int antallMånederEtterSkjæringsdato;

    @Inject
    public MedlemskapPerioderTjenesteImpl(@KonfigVerdi(value = "medlem.måneder.før.skjæringsdato") int antallMånederFørSkjæringsdato,
                                          @KonfigVerdi(value = "medlem.måneder.etter.skjæringsdato") int antallMånederEtterSkjæringsdato,
                                          SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.antallMånederFørSkjæringsdato = antallMånederFørSkjæringsdato;
        this.antallMånederEtterSkjæringsdato = antallMånederEtterSkjæringsdato;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    public MedlemskapPerioderTjenesteImpl() {
        // CDI
    }

    // FP VK 2.13 Maskinell avklaring
    @Override
    public boolean brukerMaskineltAvklartSomIkkeMedlem(Behandling behandling, PersonopplysningerAggregat personopplysningerAggregat,
                                                       Set<RegistrertMedlemskapPerioder> medlemskapPerioder) {
        LocalDate skjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
        List<MedlemskapDekningType> dekningTyper = finnGyldigeDekningstyper(medlemskapPerioder, skjæringstidspunkt);

        // Premiss alternativ 1: Bruker er registert med dekningstype som er klassifisert som Ikke medlem
        boolean erPeriodeRegistrertSomIkkeMedlem = erRegistrertSomIkkeMedlem(dekningTyper);

        // Premiss alternativ 2: Bruker er registert med dekningstype "Unntatt", og ikke er bosatt med statsb. USA/PNG
        boolean erPeriodeRegistrertSomUnntatt = erRegistrertSomUnntatt(dekningTyper);
        boolean harStatsborgerskapUsaEllerPng = harStatsborgerskapUsaEllerPng(personopplysningerAggregat);

        boolean erIkkeUsaEllerPngOgUntatt = !harStatsborgerskapUsaEllerPng
            && erPeriodeRegistrertSomUnntatt;

        // Sammenstill premisser
        return erPeriodeRegistrertSomIkkeMedlem || erIkkeUsaEllerPngOgUntatt;
    }

    @Override
    public boolean erNySkjæringsdatoUtenforInnhentetMedlemskapsintervall(Behandling behandling, LocalDate nyDato) {
        Objects.requireNonNull(nyDato, "nyDato må være satt"); //$NON-NLS-1$

        LocalDate skjæringsdato = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);

        return skjæringsdato != null && (nyDato.isBefore(skjæringsdato.minusMonths(antallMånederFørSkjæringsdato))
            || nyDato.isAfter(skjæringsdato.plusMonths(antallMånederEtterSkjæringsdato)));

    }

    @Override
    public boolean erRegistrertSomUnntatt(List<MedlemskapDekningType> dekningTyper) {
        return dekningTyper.stream()
            .anyMatch(MedlemskapDekningType.DEKNINGSTYPE_ER_MEDLEM_UNNTATT::contains);
    }

    @Override
    public boolean erRegistrertSomIkkeMedlem(List<MedlemskapDekningType> dekningTyper) {
        return dekningTyper.stream()
            .anyMatch(MedlemskapDekningType.DEKNINGSTYPE_ER_IKKE_MEDLEM::contains);
    }

    // FP VK 2.2 Maskinell avklaring
    @Override
    public boolean brukerMaskineltAvklartSomFrivilligEllerPliktigMedlem(Behandling behandling,
                                                                        Set<RegistrertMedlemskapPerioder> medlemskapPerioder) {
        LocalDate skjæringsdato = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
        List<MedlemskapDekningType> dekningTyper = finnGyldigeDekningstyper(medlemskapPerioder, skjæringsdato);

        return erRegistrertSomFrivilligMedlem(dekningTyper);
    }

    @Override
    public boolean erRegistrertSomFrivilligMedlem(List<MedlemskapDekningType> dekningTyper) {
        return dekningTyper.stream()
            .anyMatch(MedlemskapDekningType.DEKNINGSTYPE_ER_FRIVILLIG_MEDLEM::contains);
    }

    @Override
    public boolean erRegistrertSomAvklartMedlemskap(List<MedlemskapDekningType> dekningTyper) {
        return dekningTyper.stream()
            .anyMatch(MedlemskapDekningType.DEKNINGSTYPER::contains);
    }

    @Override
    public boolean erRegistrertSomUavklartMedlemskap(List<MedlemskapDekningType> dekningTyper) {
        return dekningTyper.stream()
            .anyMatch(MedlemskapDekningType.DEKNINGSTYPE_ER_UAVKLART::contains);
    }

    @Override
    public List<MedlemskapDekningType> finnGyldigeDekningstyper(Collection<RegistrertMedlemskapPerioder> medlemskapPerioder,
                                                                LocalDate skjæringsdato) {
        return medlemskapPerioder.stream()
            .filter(periode -> erDatoInnenforLukketPeriode(periode.getFom(), periode.getTom(), skjæringsdato)
                && periode.getDekningType() != null
                && !periode.getMedlemskapType().equals(MedlemskapType.UNDER_AVKLARING))
            .map(RegistrertMedlemskapPerioder::getDekningType)
            .collect(toList());
    }

    /**
     * Sært, men USA og Papua Ny-Guinea særbehandles.
     *
     * @param personopplysningerAggregat
     */
    @Override
    public boolean harStatsborgerskapUsaEllerPng(PersonopplysningerAggregat personopplysningerAggregat) {
        if (personopplysningerAggregat == null) {
            return false;
        }
        List<Statsborgerskap> statsborgerskap = personopplysningerAggregat.getStatsborgerskapFor(personopplysningerAggregat.getSøker().getAktørId());
        for (Statsborgerskap statsborgerskap1 : statsborgerskap) {
            if (Landkoder.USA.equals(statsborgerskap1.getStatsborgerskap()) || Landkoder.PNG.equals(statsborgerskap1.getStatsborgerskap())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean erStatusUtvandret(PersonopplysningerAggregat bruker) {
        return bruker != null && bruker.getSøker() != null
            && bruker.getPersonstatusFor(bruker.getSøker().getAktørId()) != null
            && bruker.getPersonstatusFor(bruker.getSøker().getAktørId()).getPersonstatus() != null
            && bruker.getPersonstatusFor(bruker.getSøker().getAktørId()).getPersonstatus().equals(PersonstatusType.UTVA);
    }

    /**
     * Bestemme om søknadens termindato, fødselsdato eller dato for omsorgsovertakelse er
     * i periode som er under avklaring eller ikke har start eller sluttdato
     */
    @Override
    public boolean harPeriodeUnderAvklaring(Set<RegistrertMedlemskapPerioder> medlemskapPerioder, LocalDate skjæringsdato) {
        boolean periodeUnderAvklaring = medlemskapPerioder.stream()
            .anyMatch(periode -> erDatoInnenforLukketPeriode(periode.getFom(), periode.getTom(), skjæringsdato)
                && periode.getMedlemskapType().equals(MedlemskapType.UNDER_AVKLARING));
        boolean åpenPeriode = medlemskapPerioder.stream()
            .anyMatch(periode -> erDatoInnenforÅpenPeriode(periode.getFom(), periode.getTom(), skjæringsdato));
        return (periodeUnderAvklaring || åpenPeriode);
    }

    private boolean erDatoInnenforLukketPeriode(LocalDate periodeFom, LocalDate periodeTom, LocalDate dato) {
        return (dato != null && !periodeFom.equals(Tid.TIDENES_BEGYNNELSE) && !periodeTom.equals(Tid.TIDENES_ENDE)
            && (dato.isAfter(periodeFom) || dato.isEqual(periodeFom))
            && (dato.isBefore(periodeTom) || dato.isEqual(periodeTom)));
    }

    private boolean erDatoInnenforÅpenPeriode(LocalDate periodeFom, LocalDate periodeTom, LocalDate dato) {
        return (dato != null
            && ((periodeTom.equals(Tid.TIDENES_ENDE) && (dato.isAfter(periodeFom) || dato.isEqual(periodeFom)))
            || (periodeFom.equals(Tid.TIDENES_BEGYNNELSE) && (dato.isBefore(periodeTom) || dato.isEqual(periodeTom)))));
    }
}
