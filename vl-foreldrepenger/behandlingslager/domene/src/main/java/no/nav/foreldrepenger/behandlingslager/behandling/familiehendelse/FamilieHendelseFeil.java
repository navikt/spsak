package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface FamilieHendelseFeil extends DeklarerteFeil {

    FamilieHendelseFeil FACTORY = FeilFactory.create(FamilieHendelseFeil.class);

    @TekniskFeil(feilkode = "FP-947392", feilmelding = "Finner ikke FamilieHendelse grunnlag for behandling med id %s", logLevel = LogLevel.WARN)
    Feil fantIkkeForventetGrunnlagPåBehandling(long behandlingId);

    @TekniskFeil(feilkode = "FP-903132", feilmelding = "Kan ikke endre typen til '%s' når den er '%s'", logLevel = LogLevel.WARN)
    Feil kanIkkeEndreTypePåHendelseFraTil(FamilieHendelseType fraType, FamilieHendelseType tilType);

    @TekniskFeil(feilkode = "FP-124902", feilmelding = "Må basere seg på eksisterende versjon av familiehendelsen", logLevel = LogLevel.WARN)
    Feil måBasereSegPåEksisterendeVersjon();

    @TekniskFeil(feilkode = "FP-947231", feilmelding = "Kan ikke oppdatere søknadsversjonen etter at det har blitt satt.", logLevel = LogLevel.WARN)
    Feil kanIkkeOppdatereSøknadVersjon();

    @TekniskFeil(feilkode = "FP-949165", feilmelding = "Aggregat kan ikke være null ved opprettelse av builder", logLevel = LogLevel.WARN)
    Feil aggregatKanIkkeVæreNull();

    @TekniskFeil(feilkode = "FP-536282", feilmelding = "Ukjent versjonstype ved opprettelse av builder", logLevel = LogLevel.WARN)
    Feil ukjentVersjonstype();

}
