package no.nav.foreldrepenger.batch.feil;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

import java.util.Set;

public interface BatchFeil extends DeklarerteFeil {

    BatchFeil FACTORY = FeilFactory.create(BatchFeil.class);

    @TekniskFeil(feilkode = "FP-189013", feilmelding = "Ugyldig job argumenter %s", logLevel = LogLevel.WARN, exceptionClass = InvalidArgumentsVLBatchException.class)
    Feil ugyldigeJobParametere(BatchArguments arguments);

    @TekniskFeil(feilkode = "FP-959814", feilmelding = "Ukjente job argumenter %s", logLevel = LogLevel.WARN, exceptionClass = UnknownArgumentsReceivedVLBatchException.class)
    Feil ukjenteJobParametere(Set<String> arguments);

    @TekniskFeil(feilkode = "FP-630260", feilmelding = "Ugyldig job-navn %s", logLevel = LogLevel.WARN)
    Feil ugyldiJobbNavnOppgitt(String navn);

}
