package no.nav.foreldrepenger.domene.kontrollerfakta.søknad;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.JA;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.NEI;
import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettListeForAksjonspunkt;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OppgittAnnenPart;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;

@ApplicationScoped
public class AksjonspunktUtlederForTidligereMottattEngangsstønad implements AksjonspunktUtleder {

    private static final List<AksjonspunktResultat> INGEN_AKSJONSPUNKTER = emptyList();
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private SøknadRepository søknadRepository;
    private PersonopplysningTjeneste personopplysningTjeneste;

    // For CDI.
    AksjonspunktUtlederForTidligereMottattEngangsstønad() {
    }

    @Inject
    public AksjonspunktUtlederForTidligereMottattEngangsstønad(BehandlingRepositoryProvider repositoryProvider, PersonopplysningTjeneste personopplysningTjeneste) {
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.personopplysningTjeneste = personopplysningTjeneste;
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        if (BehandlingType.REVURDERING.equals(behandling.getType())) {
            return INGEN_AKSJONSPUNKTER;
        }

        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, null).orElse(null);
        if (inntektArbeidYtelseGrunnlag == null) {
            return INGEN_AKSJONSPUNKTER;
        }

        AktørYtelse aktørYtelse = hentAktørYtelseEllerNull(behandling.getAktørId(), inntektArbeidYtelseGrunnlag);
        if (aktørYtelse != null && harMottattStønad(behandling, aktørYtelse) == JA) {
            return opprettListeForAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE);
        }

        if (harAnnenForelderMottattStønad(behandling, inntektArbeidYtelseGrunnlag) == JA) {
            return opprettListeForAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_OM_ANNEN_FORELDRE_HAR_MOTTATT_STØTTE);
        }

        return INGEN_AKSJONSPUNKTER;
    }

    private Utfall harMottattStønad(Behandling behandling, AktørYtelse aktørYtelse) {
        if (!(harYtelseEngangsstønad(aktørYtelse) || harYtelseForeldrepenger(aktørYtelse))) {
            return NEI;
        }
        return harMottattStønadSiste10Mnd(behandling, aktørYtelse);
    }

    private Utfall harAnnenForelderMottattStønad(Behandling behandling, InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag) {
        PersonopplysningerAggregat personopplysninger = personopplysningTjeneste.hentPersonopplysninger(behandling);
        OppgittAnnenPart annenPartFraSøknad = personopplysninger.getOppgittAnnenPart().orElse(null);

        if (annenPartFraSøknad != null && annenPartFraSøknad.getAktørId() != null) {
            AktørYtelse aktørYtelseAnnenForelder = hentAktørYtelseEllerNull(annenPartFraSøknad.getAktørId(), inntektArbeidYtelseGrunnlag);
            if (aktørYtelseAnnenForelder != null) {
                if (harYtelseEngangsstønad(aktørYtelseAnnenForelder)) {
                    return JA;
                }
                return harMottattStønadSiste10Mnd(behandling, aktørYtelseAnnenForelder);
            }
        }
        return NEI;
    }

    private Utfall harMottattStønadSiste10Mnd(Behandling behandling, AktørYtelse aktørYtelse) {
        Søknad søknad = søknadRepository.hentSøknad(behandling);
        if (aktørYtelse.getYtelser().isEmpty()) {
            return NEI;
        } else {
            return (finnYtelse(aktørYtelse) == JA
                || harMottattStønadFraVLDeSiste10Mnd(aktørYtelse.getYtelser(), søknad) == JA)
                ? JA
                : NEI;
        }
    }

    private AktørYtelse hentAktørYtelseEllerNull(AktørId aktørId, InntektArbeidYtelseGrunnlag eksisterendeAggregat) {
        return eksisterendeAggregat.getAktørYtelseFørStp(aktørId).orElse(null);
    }

    private Utfall harMottattStønadFraVLDeSiste10Mnd(Collection<Ytelse> relevanteYtelser, Søknad søknad) {
        LocalDate mottattDato = søknad.getMottattDato();
        LocalDate tiMndTilbake = mottattDato.minusMonths(10L);

        return relevanteYtelser.stream().anyMatch(v -> ytelseInnenfor10Mnd(v, tiMndTilbake)) ? JA : NEI;
    }

    private boolean ytelseInnenfor10Mnd(Ytelse ytelse, LocalDate tiMndTilbake) {
        return ytelse.getPeriode().getFomDato().isAfter(tiMndTilbake);
    }

    private boolean harYtelseEngangsstønad(AktørYtelse aktørYtelse) {
        List<Ytelse> engangsstønad = aktørYtelse.getYtelser()
            .stream()
            .filter(ytelse -> RelatertYtelseType.ENGANGSSTØNAD.equals(ytelse.getRelatertYtelseType()))
            .collect(toList());
        return engangsstønad != null && !engangsstønad.isEmpty();
    }

    private boolean harYtelseForeldrepenger(AktørYtelse aktørYtelse) {
        List<Ytelse> foreldrepenger = aktørYtelse.getYtelser()
            .stream()
            .filter(ytelse -> RelatertYtelseType.FORELDREPENGER.equals(ytelse.getRelatertYtelseType()))
            .collect(toList());
        return foreldrepenger != null && !foreldrepenger.isEmpty();
    }

    private Utfall finnYtelse(AktørYtelse aktørYtelse) {
        for (Ytelse ytelser : aktørYtelse.getYtelser()) {
            if (RelatertYtelseType.ENGANGSSTØNAD.equals(ytelser.getRelatertYtelseType())
                || RelatertYtelseType.FORELDREPENGER.equals(ytelser.getRelatertYtelseType())) {
                return JA;
            }
        }
        return NEI;
    }
}
