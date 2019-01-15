package no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.totrinn.app;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningsperiodeForSaksbehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VurderingsStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.totrinn.dto.TotrinnskontrollAktivitetDto;

@ApplicationScoped
public class TotrinnskontrollAktivitetDtoTjeneste {
    private OpptjeningsperioderTjeneste forSaksbehandlingTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;

    protected TotrinnskontrollAktivitetDtoTjeneste() {
        // for CDI proxy
    }

    @Inject
    public TotrinnskontrollAktivitetDtoTjeneste(OpptjeningsperioderTjeneste forSaksbehandlingTjeneste,
                                                VirksomhetTjeneste virksomhetTjeneste) {
        this.forSaksbehandlingTjeneste = forSaksbehandlingTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
    }

    List<TotrinnskontrollAktivitetDto> hentAktiviterEndretForOpptjening(Totrinnsvurdering aksjonspunkt,
                                                                                Behandling behandling,
                                                                                Optional<Long> inntektArbeidsYtelseGrunnlagId) {
        if (AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING.equals(aksjonspunkt.getAksjonspunktDefinisjon())) {
            List<OpptjeningsperiodeForSaksbehandling> aktivitetPerioder;
            if (inntektArbeidsYtelseGrunnlagId.isPresent()) {
                aktivitetPerioder = forSaksbehandlingTjeneste.hentRelevanteOpptjeningAktiveterForSaksbehandling(behandling, inntektArbeidsYtelseGrunnlagId.get());
            } else {
                aktivitetPerioder = forSaksbehandlingTjeneste.hentRelevanteOpptjeningAktiveterForSaksbehandling(behandling);
            }
            return aktivitetPerioder.stream()
                .filter(periode -> periode.erManueltBehandlet() || periode.getBegrunnelse() != null)
                .map(periode -> new TotrinnskontrollAktivitetDto(
                    periode.getOpptjeningAktivitetType().getNavn(),
                    periode.getErPeriodeEndret(),
                    hentVirksomhetNavnPåOrgnr(periode.getOrgnr()),
                    periode.getOrgnr(),
                    erPeriodeGodkjent(periode)))
                .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private boolean erPeriodeGodkjent(OpptjeningsperiodeForSaksbehandling periode) {
        return VurderingsStatus.GODKJENT.equals(periode.getVurderingsStatus()) || VurderingsStatus.FERDIG_VURDERT_GODKJENT.equals(periode.getVurderingsStatus());
    }

    private String hentVirksomhetNavnPåOrgnr(String orgnr) {
        if (orgnr == null) {
            return null;
        }
        return virksomhetTjeneste.finnOrganisasjon(orgnr).map(Virksomhet::getNavn)
            .orElseThrow(IllegalArgumentException::new);
    }
}
