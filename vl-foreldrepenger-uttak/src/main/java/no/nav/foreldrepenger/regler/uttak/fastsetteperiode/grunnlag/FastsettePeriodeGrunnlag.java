package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

/**
 * Interface for fastsette periode grunnlaget. Det er kun dette interfacet som skal brukes i selve regelen.
 */
@RuleDocumentationGrunnlag
public interface FastsettePeriodeGrunnlag {

    /**
     * Finn aktuell periode. Det er den perioden som er neste som skal behandles av regel.
     *
     * @return optional for aktuell periode. Perioden vil ikke være tilstede dersom det ikke er flere perioder som
     * skal behandles av regel.
     */
    Optional<UttakPeriode> getAktuellPeriode();

    /**
     * Hent perioden som er under avklaring.
     *
     * @return uttakPeriode som er under avklaring.
     */
    UttakPeriode hentPeriodeUnderBehandling();

    List<AktivitetIdentifikator> getAktiviteter();

    /**
     * Hent arbeidsprosenter for alle arbeidsforhold/aktiviteter
     *
     * @return arbeidsprosenter for alle arbeidsforhold/aktiviteter
     */
    Arbeidsprosenter getArbeidsprosenter();

    /**
     * Finn perioder der søker har gyldig grunn for tidlig oppstart eller utsettelse.
     *
     * @return Array av aktuelle perioder med gyldig grunn, sortert på fom dato. Returnerer tom array om det ikke finnes en aktuell periode eller om det ikke finnes overlappende
     * perioder med gyldig grunn.
     */
    List<GyldigGrunnPeriode> getAktuelleGyldigeGrunnPerioder();

    /**
     * Finn stønadskontotype for aktuell periode.
     *
     * @return stønadskontotype. Returmerer Stønadskontotype.UKJENT dersom det ikke er noen aktuell periode.
     */
    Stønadskontotype getStønadskontotype();

    /**
     * Finn søknadstype.
     *
     * @return søknadstype.
     */
    Søknadstype getSøknadstype();

    /**
     * Finn behandlingType.
     *
     * @return behandlingType.
     */
    Behandlingtype getBehandlingtype();

    /**
     * Finner dato for familiehendelsen som søknaden gjelder. Kan være dato for termin, fødsel eller omsorgsovertakelse.
     *
     * @return dato for familiehendelse.
     */
    LocalDate getFamiliehendelse();

    /**
     * Finn ut om søker er mor til barn som det er søkt stønad for.
     *
     * @return true dersom søker er mor, ellers false.
     */
    boolean isSøkerMor();

    /**
     * Finn ut om annen forelder er kjent med hvilke perioder det er søkt om.
     *
     * @return true dersom det er informert, ellers false.
     */
    boolean isSamtykke();

    void knekkPeriode(LocalDate knekkpunkt, Perioderesultattype perioderesultattype, Årsak årsak);

    void knekkPeriode(LocalDate knekkpunkt, Perioderesultattype perioderesultattype, Årsak årsak, Avkortingårsaktype avkortingårsaktype);

    /**
     * Finn alle uttaksperioder.
     *
     * @return array av uttaksperioder.
     */
    UttakPeriode[] getUttakPerioder();

    /**
     * Finn første dato for når gyldig uttak kan starte basert på søknadsfrist.
     *
     * @return første dato for når gyldig uttak kan starte.
     */
    LocalDate getFørsteLovligeUttaksdag();

    /**
     * Makgsgrense for lovlig uttak (p.t. 3 år etter fødsel/adopsjonsdato)
     *
     * @return siste lovlige dato (inkl)
     */
    LocalDate getMaksgrenseForLovligeUttaksdag(Konfigurasjon konfigurasjon);

    /**
     * Finn alle perioder med gyldig grunn for tidlig oppstart
     *
     * @return array av perioder med gyldig grunn for tidlig oppstart
     */
    GyldigGrunnPeriode[] getGyldigGrunnPerioder();

    /**
     * Finn alle perioder der søker ikke har omsorg for barnet/barna det søkes om
     *
     * @return array av perioder der søker ikke har omsorg for barnet/barna det søkes om
     */
    PeriodeUtenOmsorg[] getPerioderUtenOmsorg();

    /**
     * Finn alle perioder der søker er i fullt(100% eller mer) arbeid.
     *
     * @return array av perioder der søker er i fullt arbeid.
     */
    PeriodeMedFulltArbeid[] getPerioderMedFulltArbeid();

    /**
     * Finn alle perioder der søker er i arbeid(mellom 0 og 100%).
     *
     * @return array av perioder der søker er i arbeid.
     */
    PeriodeMedArbeid[] getPerioderMedArbeid();

    /**
     * Finn alle perioder der søker har bekreftet ferie.
     *
     * @return array av perioder der søker har ferie.
     */
    PeriodeMedFerie[] getPerioderMedFerie();

    /**
     * Finn alle perioder der søker har bekreftet sykdom eller skade.
     *
     * @return array av perioder der søker har bekreftet sykdom eller skade.
     */
    PeriodeMedSykdomEllerSkade[] getPerioderMedSykdomEllerSkade();

    /**
     * Finn alle perioder der søker har bekreftet innleggelse.
     *
     * @return array av perioder der søker har bekreftet innleggelse.
     */
    PeriodeMedInnleggelse[] getPerioderMedInnleggelse();

    /**
     * Finn alle perioder der søkers barn er innlagt på helseinstitusjon.
     *
     * @return array av perioder der søkers barn er innlagt på helseinstitusjon.
     */
    PeriodeMedBarnInnlagt[] getPerioderMedBarnInnlagt();

    /**
     * Finn alle perioder der søkers annen forelder er innlagt på helseinstitusjon.
     *
     * @return list av perioder der søkers annen forelder er innlagt på helseinstitusjon.
     */
    List<UttakPeriode> getPerioderMedAnnenForelderInnlagt();

    /**
     * Finn alle perioder der søkers annen forelder har bekreftet sykdom eller skade.
     *
     * @return list av perioder der søkers annen forelder har bekreftet sykdom eller skade.
     */
    List<UttakPeriode> getPerioderMedAnnenForelderSykdomEllerSkade();

    /**
     * Hopp til neste periode.
     */
    void nestePeriode();


    /**
     * Legg til perioder som fyller eventuelle hull i søkte perioder slik at periodene blir sammenhengende.
     *
     * @param oppholdPerioder
     */
    void medOppholdPerioder(List<OppholdPeriode> oppholdPerioder);


    /**
     * Knekk opp alle perioder ved angitte knekkpunkter.
     *
     * @param knekkpunkter
     */
    void knekkPerioder(Collection<LocalDate> knekkpunkter);


    /**
     * Har far/medmor rett til foreldrepenger.
     *
     * @return true dersom rett.
     */
    boolean isFarRett();

    /**
     * Har mor rett til foreldrepenger.
     *
     * @return true dersom rett.
     */

    boolean isMorRett();

    /**
     * Dato for mottatt endringssøknad
     * @return dato
     */
    LocalDate getEndringssøknadMottattdato();


    Set<Stønadskontotype> getGyldigeStønadskontotyper();

    /**
     * Om dette er en endringssøknad eller ikke
     */
    boolean erEndringssøknad();

    /**
     * Endringsdato for revurdering
     */
    LocalDate getRevurderingEndringsdato();

    /**
     * Om behandling er revurdering eller ikke
     */
    boolean erRevurdering();

    Trekkdagertilstand getTrekkdagertilstand();

    boolean harAleneomsorg();
}
