package no.nav.foreldrepenger.domene.arbeidsforhold;

import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface InnhentingFeil extends DeklarerteFeil {

    InnhentingFeil FACTORY = FeilFactory.create(InnhentingFeil.class);

    @TekniskFeil(feilkode = "FP-349977", feilmelding = "Ignorerer Arena-sak uten %s, saksnummer: %s", logLevel = LogLevel.WARN)
    Feil ignorerArenaSak(String ignorert, Saksnummer saksnummer);

    @TekniskFeil(feilkode = "FP-112843", feilmelding = "Ignorerer Arena-sak uten %s, saksnummer: %s", logLevel = LogLevel.INFO)
    Feil ignorerArenaSakInfoLogg(String ignorert, Saksnummer saksnummer);

    @TekniskFeil(feilkode = "FP-597341", feilmelding = "Ignorerer Arena-sak med vedtakTom før vedtakFom, saksnummer: %s", logLevel = LogLevel.WARN)
    Feil ignorerArenaSakMedVedtakTomFørVedtakFom(Saksnummer saksnummer);
}

