package no.nav.foreldrepenger.inngangsvilkaar.opptjening;

import java.time.LocalDate;
import java.util.Collection;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningAktivitetPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningInntektPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VurderingsStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.Aktivitet;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.Aktivitet.ReferanseType;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.AktivitetPeriode;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.Opptjeningsgrunnlag;
import no.nav.fpsak.tidsserie.LocalDateInterval;

class OpptjeningsgrunnlagAdapter {
    private LocalDate behandlingstidspunkt;
    private LocalDate startDato;
    private LocalDate sluttDato;

    OpptjeningsgrunnlagAdapter(LocalDate behandlingstidspunkt, LocalDate startDato, LocalDate sluttDato) {
        this.behandlingstidspunkt = behandlingstidspunkt;
        this.startDato = startDato;
        this.sluttDato = sluttDato;
    }

    Opptjeningsgrunnlag mapTilGrunnlag(Collection<OpptjeningAktivitetPeriode> opptjeningAktiveter,
                                       Collection<OpptjeningInntektPeriode> opptjeningInntekter) {
        Opptjeningsgrunnlag opptjeningsGrunnlag = new Opptjeningsgrunnlag(behandlingstidspunkt, startDato, sluttDato);

        // legger til alle rapporterte inntekter og aktiviteter hentet opp. håndterer duplikater/overlapp i
        // mellomregning.
        leggTilOpptjening(opptjeningAktiveter, opptjeningsGrunnlag);
        leggTilRapporterteInntekter(opptjeningInntekter, opptjeningsGrunnlag);

        return opptjeningsGrunnlag;
    }

    private void leggTilRapporterteInntekter(Collection<OpptjeningInntektPeriode> opptjeningInntekter,
                                             Opptjeningsgrunnlag opptjeningsGrunnlag) {
        for (OpptjeningInntektPeriode inn : opptjeningInntekter) {

            if (!InntektspostType.LØNN.equals(inn.getType())) {
                // TODO (OJR): tar kun med lønnsinntekter, ikke ytelse?
                continue;
            }

            LocalDateInterval dateInterval = new LocalDateInterval(inn.getFraOgMed(), inn.getTilOgMed());
            long beløpHeltall = inn.getBeløp() == null ? 0L : inn.getBeløp().longValue();

            Opptjeningsnøkkel opptjeningsnøkkel = inn.getOpptjeningsnøkkel();

            ReferanseType refType = getAktivtetReferanseType(opptjeningsnøkkel.getType());

            // TODO (OJR): alle inntekter er assignet til arbeid. hvordan passer det for frilansere? (ingen vurdering av
            // det her). Tar også kun med inntekter der vi kan finne Orgnr eller Aktørid (ignorerer null og tilfeller
            // med kun arbeidsforholdId)
            if (refType != null) {
                if (opptjeningsnøkkel.harType(Opptjeningsnøkkel.Type.ARBEIDSFORHOLD_ID)) {
                    Aktivitet aktivitet = new Aktivitet(Opptjeningsvilkår.ARBEID, getAktivitetReferanseFraNøkkel(opptjeningsnøkkel), refType);
                    opptjeningsGrunnlag.leggTilRapportertInntekt(dateInterval, aktivitet, beløpHeltall);
                } else {
                    Aktivitet aktivitet = new Aktivitet(Opptjeningsvilkår.ARBEID, opptjeningsnøkkel.getVerdi(), refType);
                    opptjeningsGrunnlag.leggTilRapportertInntekt(dateInterval, aktivitet, beløpHeltall);
                }
            }
        }
    }

    private String getAktivitetReferanseFraNøkkel(Opptjeningsnøkkel opptjeningsnøkkel) {
        String nøkkel = opptjeningsnøkkel.getForType(Opptjeningsnøkkel.Type.ORG_NUMMER);
        if(nøkkel == null) {
            nøkkel = opptjeningsnøkkel.getForType(Opptjeningsnøkkel.Type.AKTØR_ID);
        }
        return nøkkel;
    }

    private ReferanseType getAktivtetReferanseType(Opptjeningsnøkkel.Type type) {
        switch (type) {
            // skiller nå ikke på arbeidsforhold pr arbeidsgiver
            case ARBEIDSFORHOLD_ID:
            case ORG_NUMMER:
                return ReferanseType.ORGNR;
            case AKTØR_ID:
                return ReferanseType.AKTØRID;
            default:
                return null;
        }
    }

    private void leggTilOpptjening(Collection<OpptjeningAktivitetPeriode> opptjeningAktiveter, Opptjeningsgrunnlag opptjeningsGrunnlag) {
        for (OpptjeningAktivitetPeriode opp : opptjeningAktiveter) {
            LocalDateInterval dateInterval = new LocalDateInterval(opp.getPeriode().getFomDato(), opp.getPeriode().getTomDato());
            Opptjeningsnøkkel opptjeningsnøkkel = opp.getOpptjeningsnøkkel();
            if (opptjeningsnøkkel != null) {
                String identifikator = getIdentifikator(opp);
                Aktivitet opptjeningAktivitet = new Aktivitet(opp.getOpptjeningAktivitetType().getKode(), identifikator, getAktivtetReferanseType(opptjeningsnøkkel.getArbeidsgiverType()));
                AktivitetPeriode aktivitetPeriode = new AktivitetPeriode(dateInterval, opptjeningAktivitet, mapStatus(opp));
                opptjeningsGrunnlag.leggTil(aktivitetPeriode);
            } else {
                Aktivitet opptjeningAktivitet = new Aktivitet(opp.getOpptjeningAktivitetType().getKode(), null, null);
                AktivitetPeriode aktivitetPeriode = new AktivitetPeriode(dateInterval, opptjeningAktivitet, mapStatus(opp));
                opptjeningsGrunnlag.leggTil(aktivitetPeriode);
            }
        }
    }

    private String getIdentifikator(OpptjeningAktivitetPeriode opp) {
        String identifikator = opp.getOpptjeningsnøkkel().getForType(Opptjeningsnøkkel.Type.ORG_NUMMER);
        if (identifikator == null) {
            identifikator = opp.getOpptjeningsnøkkel().getForType(Opptjeningsnøkkel.Type.AKTØR_ID);
        }
        return identifikator;
    }

    private AktivitetPeriode.VurderingsStatus mapStatus(OpptjeningAktivitetPeriode periode) {
        if (VurderingsStatus.FERDIG_VURDERT_UNDERKJENT.equals(periode.getVurderingsStatus())) {
            return AktivitetPeriode.VurderingsStatus.VURDERT_UNDERKJENT;
        } else if (VurderingsStatus.FERDIG_VURDERT_GODKJENT.equals(periode.getVurderingsStatus())) {
            return AktivitetPeriode.VurderingsStatus.VURDERT_GODKJENT;
        }
        return AktivitetPeriode.VurderingsStatus.TIL_VURDERING;
    }
}
