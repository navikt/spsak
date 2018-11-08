package no.nav.foreldrepenger.dokumentbestiller;

import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface BrevFeil extends DeklarerteFeil {
    BrevFeil FACTORY = FeilFactory.create(BrevFeil.class);

    @TekniskFeil(feilkode = "FP-290951", feilmelding = "Brev med mal %s kan ikke sendes i denne behandlingen.", logLevel = LogLevel.ERROR)
    Feil brevmalIkkeTilgjengelig(String brevmalkode);

    @TekniskFeil(feilkode = "FP-290952", feilmelding = "Brev med malkode INNHEN krever at fritekst ikke er tom.", logLevel = LogLevel.WARN)
    Feil innhentDokumentasjonKreverFritekst();

    @TekniskFeil(feilkode = "FP-875839", feilmelding = "Ugyldig behandlingstype %s for bred med malkode INNHEN", logLevel = LogLevel.ERROR)
    Feil innhentDokumentasjonKreverGyldigBehandlingstype(String behandlingstype);

    @TekniskFeil(feilkode = "FP-729430", feilmelding = "Ugyldig innsynsresultattype %s", logLevel = LogLevel.ERROR)
    Feil innsynskravSvarHarUkjentResultatType(String type);

    @TekniskFeil(feilkode = "FP-316712", feilmelding = "Feil i ferdigstilling av dokument med journalpostId %s", logLevel = LogLevel.ERROR)
    Feil ferdigstillingAvDokumentFeil(JournalpostId journalpostId, Exception cause);

    @TekniskFeil(feilkode = "FP-795245", feilmelding = "Feil i knytting av vedlegg til dokument med id %s", logLevel = LogLevel.ERROR)
    Feil knyttingAvVedleggFeil(String dokumentId, Exception cause);

    @TekniskFeil(feilkode = "FP-875840", feilmelding = "Ugyldig behandlingstype %s for brev med malkode INNTID", logLevel = LogLevel.ERROR)
    Feil inntektsmeldingForTidligBrevKreverGyldigBehandlingstype(String behandlingstype);

    @TekniskFeil(feilkode = "FP-666915", feilmelding = "Ingen brevmal konfigurert for denne type behandlingen %d.", logLevel = LogLevel.ERROR)
    Feil ingenBrevmalKonfigurert(Long behandlingId);

    @TekniskFeil(feilkode = "FP-672326", feilmelding = "Ingen brev avslagsårsak kode konfigurert for denne avslagsårsak kode %s.", logLevel = LogLevel.ERROR)
    Feil ingenBrevAvslagsårsakKodeKonfigurert(String avslagsårsakKode);

    @TekniskFeil(feilkode = "FP-693339", feilmelding = "Mangler informasjon om lovhjemmel for avslagsårsak med kode %s.", logLevel = LogLevel.ERROR)
    Feil manglerInfoOmLovhjemmelForAvslagsårsak(String avslagsårsakKode);
}
