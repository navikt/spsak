package no.nav.foreldrepenger.domene.medlem.impl;

import static no.nav.foreldrepenger.domene.medlem.impl.Utfall.JA;
import static no.nav.foreldrepenger.domene.medlem.impl.Utfall.NEI;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntektspost;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapDekningType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.domene.medlem.api.MedlemskapPerioderTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.vedtak.felles.jpa.tid.IntervalUtils;

class AvklaringFaktaMedlemskap {

    private SøknadRepository søknadRepository;
    private MedlemskapRepository medlemskapRepository;
    private MedlemskapPerioderTjeneste medlemskapPerioderTjeneste;
    private PersonopplysningTjeneste personopplysningTjeneste;
    private final InntektArbeidYtelseRepository inntektArbeidYtelseRepository;


    AvklaringFaktaMedlemskap(GrunnlagRepositoryProvider repositoryProvider,
                             MedlemskapPerioderTjeneste medlemskapPerioderTjeneste,
                             PersonopplysningTjeneste personopplysningTjeneste) {
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.medlemskapPerioderTjeneste = medlemskapPerioderTjeneste;
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    }

    public Optional<MedlemResultat> utled(Behandling behandling, LocalDate vurderingsdato) { // NOSONAR - metoden kan ikke
        // gjøres kortere uten å brekke
        // 1:1 med BPM-diagram spesifisert
        // av fag
        Optional<MedlemskapAggregat> medlemskap = medlemskapRepository.hentMedlemskap(behandling);

        Set<RegistrertMedlemskapPerioder> medlemskapPerioder = medlemskap.isPresent()
            ? medlemskap.get().getRegistrertMedlemskapPerioder()
            : Collections.emptySet();

        PersonopplysningerAggregat personopplysninger = personopplysningTjeneste.hentGjeldendePersoninformasjonPåTidspunkt(behandling, vurderingsdato);

        if (harDekningsgrad(vurderingsdato, medlemskapPerioder) == JA) {
            if (erFrivilligMedlem(vurderingsdato, medlemskapPerioder) == JA) {
                return Optional.empty();
            } else {
                if (erUnntatt(vurderingsdato, medlemskapPerioder) == JA) {
                    if (harStatsborgerskapUSAellerPNG(personopplysninger) == JA) {
                        if (harStatusUtvandret(personopplysninger) == JA) {
                            return Optional.empty();
                        }
                        return Optional.of(MedlemResultat.AVKLAR_LOVLIG_OPPHOLD);
                    }
                    return Optional.empty();
                } else if (erIkkeMedlem(vurderingsdato, medlemskapPerioder) == JA) {
                    return Optional.empty();
                }
            }
        } else if (erUavklart(vurderingsdato, medlemskapPerioder) == JA) {
            return Optional.of(MedlemResultat.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE);
        } else {
            if (harStatusUtvandret(personopplysninger) == JA) {
                return Optional.empty();
            } else {

                switch (statsborgerskap(personopplysninger)) {
                    case EØS:
                        if (harInntektSiste3mnd(behandling, vurderingsdato) == JA) {
                            return Optional.empty();
                        }
                        return Optional.of(MedlemResultat.AVKLAR_OPPHOLDSRETT);
                    case TREDJE_LANDS_BORGER:
                        return Optional.of(MedlemResultat.AVKLAR_LOVLIG_OPPHOLD);
                    case NORDISK:
                        return Optional.empty();
                }
            }
        }
        throw new IllegalStateException("Udefinert utledning av aksjonspunkt for medlemskapsfakta"); //$NON-NLS-1$
    }

    Statsborgerskapsregioner statsborgerskap(PersonopplysningerAggregat søker) {
        Region region = søker.getStatsborgerskapFor(søker.getSøker().getAktørId()).stream().findFirst()
            .map(Statsborgerskap::getRegion).orElse(Region.UDEFINERT);
        if (Region.EOS.equals(region)) {
            return Statsborgerskapsregioner.EØS;
        }
        if (Region.NORDEN.equals(region)) {
            return Statsborgerskapsregioner.NORDISK;
        }
        return Statsborgerskapsregioner.TREDJE_LANDS_BORGER;
    }

    private Utfall harDekningsgrad(LocalDate vurderingsdato, Set<RegistrertMedlemskapPerioder> medlemskapPerioder) {
        List<MedlemskapDekningType> medlemskapDekningTypes = medlemskapPerioderTjeneste.finnGyldigeDekningstyper(medlemskapPerioder,
            vurderingsdato);
        return medlemskapPerioderTjeneste.erRegistrertSomAvklartMedlemskap(medlemskapDekningTypes) ? JA : NEI;
    }

    private Utfall erFrivilligMedlem(LocalDate vurderingsdato, Set<RegistrertMedlemskapPerioder> medlemskapPerioder) {
        List<MedlemskapDekningType> dekningTyper = medlemskapPerioderTjeneste.finnGyldigeDekningstyper(medlemskapPerioder, vurderingsdato);
        return medlemskapPerioderTjeneste.erRegistrertSomFrivilligMedlem(dekningTyper) ? JA : NEI;
    }

    private Utfall erUnntatt(LocalDate vurderingsdato, Set<RegistrertMedlemskapPerioder> medlemskapPerioder) {
        List<MedlemskapDekningType> dekningTyper = medlemskapPerioderTjeneste.finnGyldigeDekningstyper(medlemskapPerioder, vurderingsdato);
        return medlemskapPerioderTjeneste.erRegistrertSomUnntatt(dekningTyper) ? JA : NEI;
    }

    private Utfall erIkkeMedlem(LocalDate vurderingsdato, Set<RegistrertMedlemskapPerioder> medlemskapPerioder) {
        List<MedlemskapDekningType> dekningTyper = medlemskapPerioderTjeneste.finnGyldigeDekningstyper(medlemskapPerioder, vurderingsdato);
        return medlemskapPerioderTjeneste.erRegistrertSomIkkeMedlem(dekningTyper) ? JA : NEI;
    }

    private Utfall erUavklart(LocalDate vurderingsdato, Set<RegistrertMedlemskapPerioder> medlemskapPerioder) {
        List<MedlemskapDekningType> medlemskapDekningTyper = medlemskapPerioderTjeneste.finnGyldigeDekningstyper(medlemskapPerioder,
            vurderingsdato);
        return medlemskapPerioderTjeneste.erRegistrertSomUavklartMedlemskap(medlemskapDekningTyper) ? JA : NEI;
    }

    private Utfall harStatusUtvandret(PersonopplysningerAggregat bruker) {
        return medlemskapPerioderTjeneste.erStatusUtvandret(bruker) ? JA : NEI;
    }

    private Utfall harStatsborgerskapUSAellerPNG(PersonopplysningerAggregat bruker) {
        return medlemskapPerioderTjeneste.harStatsborgerskapUsaEllerPng(bruker) ? JA : NEI;
    }

    /**
     * Skal sjekke om bruker eller andre foreldre har inntekt eller ytelse fra NAV
     * innenfor de 3 siste månedene fra mottattdato
     */
    private Utfall harInntektSiste3mnd(Behandling behandling, LocalDate vurderingsdato) {
        LocalDate mottattDato = søknadRepository.hentSøknad(behandling).getMottattDato();
        LocalDate mottattDatoMinus3Mnd = mottattDato.minusMonths(3);
        IntervalUtils intervalUtils = new IntervalUtils(mottattDatoMinus3Mnd, mottattDato);
        List<Inntektspost> inntektsposter = new ArrayList<>();
        inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, vurderingsdato).ifPresent(e -> {
            e.getAktørInntektForFørStp()
                .forEach(it -> it.getInntektPensjonsgivende().forEach(ip -> inntektsposter.addAll(ip.getInntektspost())));
        });
        return inntektsposter.stream()
            .anyMatch(inntekt -> intervalUtils.overlapper(new IntervalUtils(inntekt.getFraOgMed(), inntekt.getTilOgMed()))) ? JA : NEI;
    }

    enum Statsborgerskapsregioner {
        NORDISK, EØS, TREDJE_LANDS_BORGER
    }
}
