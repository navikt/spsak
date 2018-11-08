package no.nav.foreldrepenger.domene.medlem.impl;

import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.JA;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.NEI;
import static no.nav.foreldrepenger.domene.medlem.impl.MedlemResultat.VENT_PÅ_FØDSEL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntektspost;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingsgrunnlagKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;


class AvklarOmSøkerOppholderSegINorge {

    private FamilieHendelseRepository familieGrunnlagRepository;
    private SøknadRepository søknadRepository;
    private PersonopplysningTjeneste personopplysningTjeneste;
    private BehandlingsgrunnlagKodeverkRepository behandlingsgrunnlagKodeverkRepository;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;

    AvklarOmSøkerOppholderSegINorge(BehandlingRepositoryProvider repositoryProvider,
                                    PersonopplysningTjeneste personopplysningTjeneste) {
        this.behandlingsgrunnlagKodeverkRepository = repositoryProvider.getBehandlingsgrunnlagKodeverkRepository();
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.personopplysningTjeneste = personopplysningTjeneste;
    }

    public Optional<MedlemResultat> utled(Behandling behandling, LocalDate vurderingstidspunkt) {
        final List<String> landkoder = getLandkode(behandling, vurderingstidspunkt);
        Region region = Region.UDEFINERT;
        if (!landkoder.isEmpty()) {
            region = behandlingsgrunnlagKodeverkRepository.finnHøyestRangertRegion(landkoder);
        }
        if ((harFødselsdato(behandling) == JA) || (harDatoForOmsorgsovertakelse(behandling) == JA)) {
            return Optional.empty();
        }
        if ((harNordiskStatsborgerskap(region) == JA) || (harAnnetStatsborgerskap(region) == JA)) {
            return Optional.empty();
        }
        if ((erGiftMedNordiskBorger(behandling) == JA) || (erGiftMedBorgerMedANNETStatsborgerskap(behandling) == JA)) {
            return Optional.empty();
        }
        if (harSøkerHattInntektINorgeDeSiste3Mnd(behandling, vurderingstidspunkt) == JA) {
            return Optional.empty();
        }
        if (harTermindatoPassertMed14Dager(behandling) == NEI) {
            return Optional.of(VENT_PÅ_FØDSEL);
        }
        return Optional.of(MedlemResultat.AVKLAR_OPPHOLDSRETT);
    }

    private Utfall harFødselsdato(Behandling behandling) {
        final FamilieHendelseGrunnlag grunnlag = familieGrunnlagRepository.hentAggregat(behandling);
        if (!grunnlag.getGjeldendeBekreftetVersjon().map(FamilieHendelse::getBarna).map(List::isEmpty).orElse(true)) {
            return JA;
        }
        final FamilieHendelse søknad = grunnlag.getSøknadVersjon();
        if (!FamilieHendelseType.FØDSEL.equals(søknad.getType())) {
            return NEI;
        }
        return  søknad.getBarna().isEmpty() ? NEI : JA;
    }

    private Utfall harDatoForOmsorgsovertakelse(Behandling behandling) {
        final FamilieHendelseGrunnlag grunnlag = familieGrunnlagRepository.hentAggregat(behandling);
        final FamilieHendelse søknad = grunnlag.getSøknadVersjon();
        return søknad.getAdopsjon().map(Adopsjon::getOmsorgsovertakelseDato).isPresent() ? JA : NEI;
    }

    private Utfall harNordiskStatsborgerskap(Region region) {
        return Region.NORDEN.equals(region) ? JA : NEI;
    }

    private Utfall harAnnetStatsborgerskap(Region region) {
        return (region == null || Region.TREDJELANDS_BORGER.equals(region)) || Region.UDEFINERT.equals(region) ? JA : NEI;
    }

    private Utfall erGiftMedNordiskBorger(Behandling behandling) {
        return erGiftMed(behandling, Region.NORDEN);
    }

    private Utfall erGiftMedBorgerMedANNETStatsborgerskap(Behandling behandling) {
        Utfall utfall = erGiftMed(behandling, Region.TREDJELANDS_BORGER);
        if (utfall == NEI) {
            utfall = erGiftMed(behandling, Region.UDEFINERT);
        }
        return utfall;
    }

    private Utfall erGiftMed(Behandling behandling, Region region) {
        Optional<Personopplysning> ektefelle = personopplysningTjeneste.hentPersonopplysninger(behandling).getEktefelle();
        if (ektefelle.isPresent()) {
            if (ektefelle.get().getRegion().equals(region)) {
                return JA;
            }
        }
        return NEI;
    }

    private Utfall harSøkerHattInntektINorgeDeSiste3Mnd(Behandling behandling, LocalDate vurderingstidspunkt) {
        final Søknad søknad = søknadRepository.hentSøknad(behandling);
        LocalDate mottattDato = søknad.getMottattDato();
        LocalDate treMndTilbake = mottattDato.minusMonths(3L);
        AktørId søkerAktørId = behandling.getAktørId();

        // OBS: ulike regler for vilkår og autopunkt. For EØS-par skal man vente hvis søker ikke har inntekt siste 3mnd.
        List<Inntektspost> inntektsposter = new ArrayList<>();
        inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, vurderingstidspunkt).ifPresent(aggregat ->
            aggregat.getAktørInntektForFørStp(søkerAktørId).ifPresent(ai -> ai.getInntektPensjonsgivende().forEach(i -> inntektsposter.addAll(i.getInntektspost()))));

        for (Inntektspost inntekt : inntektsposter) {
            if (inntekt.getTilOgMed().isAfter(treMndTilbake)) {
                return JA;
            }
        }
        return NEI;
    }

    private Utfall harTermindatoPassertMed14Dager(Behandling behandling) {
        LocalDate dagensDato = LocalDate.now();
        final Optional<LocalDate> termindato = familieGrunnlagRepository.hentAggregat(behandling).getGjeldendeTerminbekreftelse()
            .map(Terminbekreftelse::getTermindato);
        return termindato.filter(localDate -> localDate.plusDays(14L).isBefore(dagensDato)).map(localDate -> JA).orElse(NEI);
    }

    private List<String> getLandkode(Behandling behandling, LocalDate vurderingstidspunkt) {
        PersonopplysningerAggregat personopplysninger = personopplysningTjeneste.hentGjeldendePersoninformasjonPåTidspunkt(behandling, vurderingstidspunkt);

        return personopplysninger.getStatsborgerskapFor(behandling.getAktørId())
            .stream()
            .map(Statsborgerskap::getStatsborgerskap)
            .map(Landkoder::getKode)
            .collect(Collectors.toList());
    }
}
