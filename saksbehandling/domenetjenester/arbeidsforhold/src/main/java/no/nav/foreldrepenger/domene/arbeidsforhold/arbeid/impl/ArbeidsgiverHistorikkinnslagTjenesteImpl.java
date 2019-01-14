package no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;

@ApplicationScoped
public class ArbeidsgiverHistorikkinnslagTjenesteImpl implements ArbeidsgiverHistorikkinnslagTjeneste {
    private TpsTjeneste tpsTjeneste;

    public ArbeidsgiverHistorikkinnslagTjenesteImpl() {
        // CDI
    }

    @Inject
    public ArbeidsgiverHistorikkinnslagTjenesteImpl(TpsTjeneste tpsTjeneste) {
        this.tpsTjeneste = tpsTjeneste;
    }

    @Override
    public String lagArbeidsgiverHistorikkinnslagTekst(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef) {
        if (arbeidsgiver  != null && arbeidsforholdRef != null) {
            return lagTekstMedArbeidsgiverOgArbeidforholdRef(arbeidsgiver, arbeidsforholdRef);
        } else if (arbeidsgiver != null) {
            return lagTekstMedArbeidsgiver(arbeidsgiver);
        }
        throw new IllegalStateException("Klarte ikke lage historikkinnslagstekst for arbeidsgiver");
    }

    @Override
    public String lagArbeidsgiverHistorikkinnslagTekst(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver == null) {
            throw new IllegalStateException("Arbeidsgiver kan ikke være null");
        }
        return lagTekstMedArbeidsgiver(arbeidsgiver);
    }

    @Override
    public String lagHistorikkinnslagTekstForBeregningsgrunnlag(BeregningsgrunnlagPrStatusOgAndel bgAndel) {
        Optional<Arbeidsgiver> arbeidsgiver = bgAndel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver);
        Optional<ArbeidsforholdRef> arbeidsforholdRef = bgAndel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef);
        return arbeidsgiver.map(arbGiv -> arbeidsforholdRef.isPresent()
            ? lagArbeidsgiverHistorikkinnslagTekst(arbGiv, arbeidsforholdRef.get())
            : lagArbeidsgiverHistorikkinnslagTekst(arbGiv)).orElse(bgAndel.getArbeidsforholdType().getNavn());
    }

    private String lagTekstMedArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver.getErVirksomhet() && arbeidsgiver.getVirksomhet() != null) {
            return lagTekstForVirksomhetNavnOgOrgnummer(arbeidsgiver.getVirksomhet());
        } else if (arbeidsgiver.getAktørId() != null) {
            return lagTekstForPrivatpersonNavn(arbeidsgiver.getAktørId());
        }
        throw new IllegalStateException("Klarte ikke lage historikkinnslagstekst for arbeidsgiver");
    }

    private String lagTekstMedArbeidsgiverOgArbeidforholdRef(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef) {
        StringBuilder sb = new StringBuilder();
        sb.append(lagTekstMedArbeidsgiver(arbeidsgiver));
        sb.append(lagTekstMedArbeidsforholdref(arbeidsforholdRef));
        return sb.toString();
    }

    private String lagTekstMedArbeidsforholdref(ArbeidsforholdRef arbeidsforholdRef) {
        String referanse = arbeidsforholdRef.getReferanse();
        String sisteFireTegnIRef = referanse.substring(referanse.length()-4);
        StringBuilder sb = new StringBuilder();
        sb.append(" ...")
            .append(sisteFireTegnIRef);
        return sb.toString();

    }

    private String lagTekstForPrivatpersonNavn(AktørId aktørId) {
        Optional<Personinfo> personinfo = tpsTjeneste.hentBrukerForAktør(aktørId);
        if (!personinfo.isPresent()) {
            throw new IllegalStateException("Finner ikke arbeidsgiver i TPS");
        }
        return personinfo.get().getNavn();
    }

    private String lagTekstForVirksomhetNavnOgOrgnummer(Virksomhet virksomhet) {
        StringBuilder sb = new StringBuilder();
        sb.append(virksomhet.getNavn())
            .append(" (")
            .append(virksomhet.getOrgnr())
            .append(")");
        return sb.toString();
    }
}
