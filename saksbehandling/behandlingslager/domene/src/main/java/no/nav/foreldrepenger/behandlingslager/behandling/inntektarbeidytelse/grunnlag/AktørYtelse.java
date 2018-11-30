package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag;

import java.util.Collection;

import no.nav.foreldrepenger.domene.typer.AktørId;

public interface AktørYtelse {

    /**
     * Aktøren tilstøtende ytelser gjelder for
     * @return aktørId
     */
    AktørId getAktørId();

    /**
     * Tilstøtende ytelser
     * @return liste av {@link Ytelse}
     */
    Collection<Ytelse> getYtelser();
}
