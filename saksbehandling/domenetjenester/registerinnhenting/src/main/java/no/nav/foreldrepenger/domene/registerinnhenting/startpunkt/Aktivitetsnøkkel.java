package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;

// Aktivitetsnøkkel er en gjengivelse av hvordan BeregningsresultatAndel identifiserer en unik godkjent aktivitet
// Er først og fremst knyttet til arbeidsforhold.
// Kompenserer for at det ikke finnes noen slik abstraksjon tilgjengelig i BeregningsresultatPerioder sin kodebase
class Aktivitetsnøkkel {
    private final Virksomhet virksomhet;
    private final ArbeidsforholdRef arbeidsforholdRef;
    private final AktivitetStatus aktivitetStatus;
    private final Inntektskategori inntektskategori;

    Aktivitetsnøkkel(BeregningsresultatAndel andel) {
        this.virksomhet = andel.getVirksomhet();
        this.arbeidsforholdRef = andel.getArbeidsforholdRef();
        this.aktivitetStatus = andel.getAktivitetStatus();
        this.inntektskategori = andel.getInntektskategori();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Aktivitetsnøkkel)){
            return false;
        }
        Aktivitetsnøkkel that = (Aktivitetsnøkkel) o;

        return Objects.equals(virksomhet, that.virksomhet)
            && Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef)
            && Objects.equals(aktivitetStatus, that.aktivitetStatus)
            && Objects.equals(inntektskategori, that.inntektskategori)
            ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(virksomhet, arbeidsforholdRef, aktivitetStatus, inntektskategori);
    }
}
