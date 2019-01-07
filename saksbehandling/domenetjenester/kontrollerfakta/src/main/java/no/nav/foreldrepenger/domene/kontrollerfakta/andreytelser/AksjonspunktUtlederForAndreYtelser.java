package no.nav.foreldrepenger.domene.kontrollerfakta.andreytelser;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettListeForAksjonspunkt;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

/**
 * Aksjonspunkt for avklaring v tilleggopplysninger som oppgis i søknad.
 */
@ApplicationScoped
public class AksjonspunktUtlederForAndreYtelser implements AksjonspunktUtleder {

    private static final List<AksjonspunktResultat> INGEN_AKSJONSPUNKTER = emptyList();
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    AksjonspunktUtlederForAndreYtelser() {
    }

    @Inject
    public AksjonspunktUtlederForAndreYtelser(GrunnlagRepositoryProvider repositoryProvider, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        LocalDate skjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
        Optional<InntektArbeidYtelseGrunnlag> grunnlagOpt = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, skjæringstidspunkt);

        if (grunnlagOpt.isPresent()) {
            InntektArbeidYtelseGrunnlag grunnlag = grunnlagOpt.get();
            Optional<AktørYtelse> ytelseFørStp = grunnlag.getAktørYtelseFørStp(behandling.getAktørId());

            if (ytelseFørStp.isPresent()) {
                AktørYtelse ytelser = ytelseFørStp.get();
                DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(2), skjæringstidspunkt);
                boolean harYtelseSiste2Måneder = ytelser.getYtelser()
                    .stream()
                    .filter(this::erIkkeSykepenger)
                    .anyMatch(ytelse -> ytelse.getPeriode().overlapper(periode));

                if (harYtelseSiste2Måneder) {
                    return opprettListeForAksjonspunkt(AksjonspunktDefinisjon.VURDER_ANDRE_YTELSER);
                }
            }
        }
        return INGEN_AKSJONSPUNKTER;
    }

    private boolean erIkkeSykepenger(Ytelse ytelse) {
        return !RelatertYtelseType.SYKEPENGER.equals(ytelse.getRelatertYtelseType());
    }

}
