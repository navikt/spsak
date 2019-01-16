package no.nav.foreldrepenger.domene.arbeidsforhold.arbeid;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;

@ApplicationScoped
public class ArbeidsgiverHistorikkinnslagTjeneste {
    private TpsTjeneste tpsTjeneste;

    public ArbeidsgiverHistorikkinnslagTjeneste() {
        // CDI
    }

    @Inject
    public ArbeidsgiverHistorikkinnslagTjeneste(TpsTjeneste tpsTjeneste) {
        this.tpsTjeneste = tpsTjeneste;
    }

    /**
     * Metode som lager string som representerer arbeidsgiver i historikkinnslagene
     * hvis arbeidsforholdref ikke er tilgjengelig.
     *
     * @param arbeidsgiver      Arbeidsgiveren det skal lages historikkinnslag om
     * @param arbeidsforholdRef Arbeidsforholdreferansen til det aktuelle arbeidsforholdet
     * @return Returnerer en string på formatet: Statoil (938471284) ...ef2k for virksomheter, Ole Jensen (999888777666) ...9fj4 for privatpersoner
     */
    public String lagArbeidsgiverHistorikkinnslagTekst(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef) {
        if (arbeidsgiver != null && arbeidsforholdRef != null) {
            return lagTekstMedArbeidsgiverOgArbeidforholdRef(arbeidsgiver, arbeidsforholdRef);
        } else if (arbeidsgiver != null) {
            return lagTekstMedArbeidsgiver(arbeidsgiver);
        }
        throw new IllegalStateException("Klarte ikke lage historikkinnslagstekst for arbeidsgiver");
    }

    /**
     * Metode som lager string som representerer arbeidsgiver i historikkinnslagene
     * hvis arbeidsforholdref er tilgjengelig. Viser kun de siste 4 tegn i arbeidsforholdreferansen.
     *
     * @param arbeidsgiver Arbeidsgiveren det skal lages historikkinnslag om
     * @return Returnerer en string på formatet: Statoil (938471284) for virksomheter, Ole Jensen (999888777666) for privatpersoner
     */
    public String lagArbeidsgiverHistorikkinnslagTekst(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver == null) {
            throw new IllegalStateException("Arbeidsgiver kan ikke være null");
        }
        return lagTekstMedArbeidsgiver(arbeidsgiver);
    }

    /**
     * Metode som tar inn en beregningsgrunnlagandel og deretter kaller en passende metode for å lage en
     * tekstlig representasjon av arbeidsgiver og arbeidsforholdet.
     *
     * @param bgAndel Beregningsgrunnlagsandelen det skal lages historikkinnslag for
     * @return Returnerer en string på formatet: Statoil (938471284) for virksomheter, Ole Jensen (999888777666) for privatpersoner
     */
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
        String sisteFireTegnIRef = referanse.substring(referanse.length() - 4);
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
