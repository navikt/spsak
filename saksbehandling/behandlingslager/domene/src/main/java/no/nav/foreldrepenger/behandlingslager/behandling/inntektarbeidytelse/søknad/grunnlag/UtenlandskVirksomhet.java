package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag;

import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;

public interface UtenlandskVirksomhet {

    Landkoder getLandkode();

    String getUtenlandskVirksomhetNavn();
}
