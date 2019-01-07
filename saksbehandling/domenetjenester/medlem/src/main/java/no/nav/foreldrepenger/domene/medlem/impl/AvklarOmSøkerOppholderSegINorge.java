package no.nav.foreldrepenger.domene.medlem.impl;

import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.JA;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.NEI;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntektspost;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingsgrunnlagKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;


class AvklarOmSøkerOppholderSegINorge {

    private SøknadRepository søknadRepository;
    private PersonopplysningTjeneste personopplysningTjeneste;
    private BehandlingsgrunnlagKodeverkRepository behandlingsgrunnlagKodeverkRepository;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;

    AvklarOmSøkerOppholderSegINorge(GrunnlagRepositoryProvider repositoryProvider,
                                    PersonopplysningTjeneste personopplysningTjeneste) {
        this.behandlingsgrunnlagKodeverkRepository = repositoryProvider.getBehandlingsgrunnlagKodeverkRepository();
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
        if ((harNordiskStatsborgerskap(region) == JA) || (harAnnetStatsborgerskap(region) == JA)) {
            return Optional.empty();
        }
        if (harSøkerHattInntektINorgeDeSiste3Mnd(behandling, vurderingstidspunkt) == JA) {
            return Optional.empty();
        }
        return Optional.of(MedlemResultat.AVKLAR_OPPHOLDSRETT);
    }

    private Utfall harNordiskStatsborgerskap(Region region) {
        return Region.NORDEN.equals(region) ? JA : NEI;
    }

    private Utfall harAnnetStatsborgerskap(Region region) {
        return (region == null || Region.TREDJELANDS_BORGER.equals(region)) || Region.UDEFINERT.equals(region) ? JA : NEI;
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

    private List<String> getLandkode(Behandling behandling, LocalDate vurderingstidspunkt) {
        PersonopplysningerAggregat personopplysninger = personopplysningTjeneste.hentGjeldendePersoninformasjonPåTidspunkt(behandling, vurderingstidspunkt);

        return personopplysninger.getStatsborgerskapFor(behandling.getAktørId())
            .stream()
            .map(Statsborgerskap::getStatsborgerskap)
            .map(Landkoder::getKode)
            .collect(Collectors.toList());
    }
}
