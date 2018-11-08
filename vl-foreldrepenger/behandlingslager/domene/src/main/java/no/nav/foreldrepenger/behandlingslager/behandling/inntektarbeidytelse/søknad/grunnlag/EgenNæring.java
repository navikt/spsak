package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface EgenNæring {

    LocalDate getFraOgMed();

    LocalDate getTilOgMed();

    DatoIntervallEntitet getPeriode();

    VirksomhetType getVirksomhetType();

    Virksomhet getVirksomhet();

    String getRegnskapsførerNavn();

    String getRegnskapsførerTlf();

    LocalDate getEndringDato();

    BigDecimal getBruttoInntekt();

    String getBegrunnelse();

    boolean getNyoppstartet();

    boolean getNyIArbeidslivet();

    boolean getVarigEndring();

    boolean getNærRelasjon();

    UtenlandskVirksomhet getUtenlandskVirksomhet();

}
