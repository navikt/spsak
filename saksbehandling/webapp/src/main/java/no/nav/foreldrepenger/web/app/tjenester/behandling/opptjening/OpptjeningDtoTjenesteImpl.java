package no.nav.foreldrepenger.web.app.tjenester.behandling.opptjening;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningsperiodeForSaksbehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VurderingsStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.foreldrepenger.domene.typer.Stillingsprosent;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;

@ApplicationScoped
public class OpptjeningDtoTjenesteImpl implements OpptjeningDtoTjeneste {

    private OpptjeningsperioderTjeneste forSaksbehandlingTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;

    OpptjeningDtoTjenesteImpl() {
        // Hibernate
    }

    @Inject
    public OpptjeningDtoTjenesteImpl(OpptjeningsperioderTjeneste forSaksbehandlingTjeneste,
                                     VirksomhetTjeneste virksomhetTjeneste) {
        this.forSaksbehandlingTjeneste = forSaksbehandlingTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
    }

    @Override
    public Optional<OpptjeningDto> mapFra(Behandling behandling) {
        Optional<Opptjening> fastsattOpptjening = forSaksbehandlingTjeneste.hentOpptjeningHvisFinnes(behandling);

        OpptjeningDto resultat = new OpptjeningDto();
        if (fastsattOpptjening.isPresent() && fastsattOpptjening.get().getAktiv()) {
            List<OpptjeningAktivitet> opptjeningAktivitet = fastsattOpptjening.get().getOpptjeningAktivitet();
            resultat.setFastsattOpptjening(new FastsattOpptjeningDto(fastsattOpptjening.get().getFom(),
                fastsattOpptjening.get().getTom(), mapFastsattOpptjening(fastsattOpptjening.get()),
                MergeOverlappendePeriodeHjelp.mergeOverlappenePerioder(opptjeningAktivitet)));
        }

        if (fastsattOpptjening.isPresent()) {
            List<OpptjeningsperiodeForSaksbehandling> aktiviteter = forSaksbehandlingTjeneste.hentRelevanteOpptjeningAktiveterForSaksbehandling(behandling);
            if (!aktiviteter.isEmpty()) {
                List<OpptjeningAktivitetDto> oadList = new ArrayList<>();
                for (OpptjeningsperiodeForSaksbehandling oap : aktiviteter) {
                    oadList.add(lagDtoFraOAPeriode(oap));
                }
                resultat.setOpptjeningAktivitetList(oadList);
            }
        } else {
            resultat.setOpptjeningAktivitetList(Collections.emptyList());
        }

        if (resultat.getFastsattOpptjening() == null && resultat.getOpptjeningAktivitetList().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(resultat);
    }

    private OpptjeningPeriodeDto mapFastsattOpptjening(Opptjening fastsattOpptjening) {
        return fastsattOpptjening.getOpptjentPeriode() != null ? new OpptjeningPeriodeDto(fastsattOpptjening.getOpptjentPeriode().getMonths(),
            fastsattOpptjening.getOpptjentPeriode().getDays()) : new OpptjeningPeriodeDto();
    }

    private OpptjeningAktivitetDto lagDtoFraOAPeriode(OpptjeningsperiodeForSaksbehandling oap) {
        OpptjeningAktivitetDto oad = new OpptjeningAktivitetDto(oap.getOpptjeningAktivitetType(),
            oap.getPeriode().getFomDato(), oap.getPeriode().getTomDato());
        if (oap.getOrgnr() != null && OrganisasjonsNummerValidator.erGyldig(oap.getOrgnr())) {
            Virksomhet virksomhet = virksomhetTjeneste.finnOrganisasjon(oap.getOrgnr())
                .orElseThrow(IllegalArgumentException::new); // Utvikler feil hvis exception
            oad.setArbeidsgiver(virksomhet.getNavn());
            oad.setOppdragsgiverOrg(oap.getOrgnr());
            oad.setNaringRegistreringsdato(virksomhet.getRegistrert());
            oad.setStillingsandel(Optional.ofNullable(oap.getStillingsprosent()).map(Stillingsprosent::getVerdi).orElse(BigDecimal.ZERO));
        } else if (oap.getOrgnr() != null && !OrganisasjonsNummerValidator.erGyldig(oap.getOrgnr())) {
            // Personlig foretak, hente fra tps?
            oad.setOppdragsgiverOrg(oap.getOrgnr());
            oad.setStillingsandel(Optional.ofNullable(oap.getStillingsprosent()).map(Stillingsprosent::getVerdi).orElse(BigDecimal.ZERO));
        } else {
            oad.setArbeidsgiver(oap.getArbeidsGiverNavn());
        }
        if (oap.getVurderingsStatus().equals(VurderingsStatus.GODKJENT)) {
            oad.setErGodkjent(true);
        } else if (oap.getVurderingsStatus().equals(VurderingsStatus.UNDERKJENT)) {
            oad.setErGodkjent(false);
        }
        oad.setErManueltOpprettet(oap.getErManueltRegistrert());
        oad.setBegrunnelse(oap.getBegrunnelse());
        oad.setErEndret(oap.erManueltBehandlet());
        oad.setErPeriodeEndret(oap.getErPeriodeEndret());
        oad.setArbeidsforholdRef(Optional.ofNullable(oap.getOpptjeningsnøkkel())
            .flatMap(Opptjeningsnøkkel::getArbeidsforholdRef)
            .map(ArbeidsforholdRef::getReferanse)
            .orElse(null));
        return oad;
    }
}
