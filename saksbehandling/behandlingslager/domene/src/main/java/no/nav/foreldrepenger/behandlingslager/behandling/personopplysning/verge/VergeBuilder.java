package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.verge;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class VergeBuilder {
    private VergeEntitet kladd;

    public VergeBuilder() {
        kladd = new VergeEntitet();
    }

    public VergeBuilder medVedtaksdato(LocalDate vedtaksdato) {
        kladd.vedtaksdato = vedtaksdato;
        return this;
    }

    public VergeBuilder gyldigPeriode(LocalDate gyldigFom, LocalDate gyldigTom) {
        kladd.gyldigPeriode = DatoIntervallEntitet.fraOgMedTilOgMed(gyldigFom, gyldigTom);
        return this;
    }

    public VergeBuilder medVergeType(VergeType vergeType) {
        kladd.vergeType = vergeType;
        return this;
    }

    public VergeBuilder medMandatTekst(String mandatTekst) {
        kladd.mandatTekst = mandatTekst;
        return this;
    }

    public VergeBuilder medStønadMottaker(boolean stønadMottaker) {
        kladd.stønadMottaker = stønadMottaker;
        return this;
    }

    public VergeBuilder medBrevMottaker(BrevMottaker brevMottaker) {
        kladd.brevMottaker = brevMottaker;
        return this;
    }

    public VergeBuilder medBruker(NavBruker bruker) {
        kladd.bruker = bruker;
        return this;
    }

    public VergeEntitet build() {
        //verifiser oppbyggingen til objektet
        //TODO(sjekk om flere ting burde sjekkes, burde samsvare med hva som er NOT NULLABLE i databasen!)
        Objects.requireNonNull(kladd.brevMottaker, "brevMottaker"); //$NON-NLS-1$
        Objects.requireNonNull(kladd.vergeType, "vergeType"); //$NON-NLS-1$
        Objects.requireNonNull(kladd.stønadMottaker, "stønadMottaker"); //$NON-NLS-1$
        Objects.requireNonNull(kladd.bruker, "bruker"); //$NON-NLS-1$
        return kladd;
    }
}
