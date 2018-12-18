package no.nav.foreldrepenger.mottak.task.dokumentforsendelse;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface BehandleDokumentforsendelseFeil extends DeklarerteFeil {
    BehandleDokumentforsendelseFeil FACTORY = FeilFactory.create(BehandleDokumentforsendelseFeil.class);

    @TekniskFeil(feilkode = "FP-581254", feilmelding = "Kunne ikke utlede neste steg", logLevel = ERROR)
    Feil kanIkkeUtledeNesteSteg();

    @TekniskFeil(feilkode = "FP-584104", feilmelding = "Kjenner ikke igjen søknad XML typen i forsendelsesdokument", logLevel = ERROR)
    Feil ukjentSøknadXMLType();

    @TekniskFeil(feilkode = "FP-758390", feilmelding = "Søkers ID samsvarer ikke med søkers ID i eksisterende sak", logLevel = WARN)
    Feil aktørIdMismatch();

    @TekniskFeil(feilkode = "FP-756353", feilmelding = "BehandlingTema i forsendelse samsvarer ikke med BehandlingTema i eksisterende sak {%s : %s}", logLevel = WARN)
    Feil behandlingTemaMismatch(String behandlingTemaforsendelse, String behandlingTemaSak);
}
