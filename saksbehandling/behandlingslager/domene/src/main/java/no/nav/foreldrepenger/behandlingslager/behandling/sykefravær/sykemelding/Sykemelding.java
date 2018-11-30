package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.domene.typer.Prosentsats;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface Sykemelding {

    DatoIntervallEntitet getPeriode();

    Arbeidsgiver getArbeidsgiver();

    Prosentsats getGrad();

    /**
     * Ekstern referanse til sykemeldings-registeret som unikt identifiserer sykemeldingen
     * @return
     */
    String getEksternReferanse();
}
