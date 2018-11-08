package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag;

import java.util.Collection;

import no.nav.foreldrepenger.domene.typer.AktørId;

public interface AktørArbeid {

    /**
     * Aktøren som avtalene gjelder for
     * @return aktørId
     */
    AktørId getAktørId();

    /**
     * Collection av aktiviteter filtrert iht ArbeidsforholdInformasjon, uten frilans enkeltoppdrag  {@link no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType}
     * @return Liste av {@link Yrkesaktivitet}
     */
    Collection<Yrkesaktivitet> getYrkesaktiviteter();

    /**
     * Collection av frilansaktiviteter / enkeltoppdrag {@link no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType}
     * @return Liste av {@link Yrkesaktivitet}
     */
    Collection<Yrkesaktivitet> getFrilansOppdrag();
}
