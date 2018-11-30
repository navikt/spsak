package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;


public interface PersonopplysningFeil extends DeklarerteFeil  {

    PersonopplysningFeil FACTORY = FeilFactory.create(PersonopplysningFeil.class);

    @FunksjonellFeil(feilkode = "FP-154411", feilmelding = "Personopplysning må ha aktørId eller nummer", logLevel = LogLevel.ERROR, løsningsforslag = "Bruk .medAktørId(long) eller medNummer(long) sammen med builder.")
    Feil personopplysningManglerPåkrevdeFelter();

    @TekniskFeil(feilkode = "FP-454411", feilmelding = "Behandlingsgrunnlag skal kun settes av Behandlingsgrunnlag eller Behandlingsgrunnlag.Builder", logLevel = LogLevel.ERROR)
    Feil behandlingsgrunnlagIkkeSattAvRiktigKlasse();

    @TekniskFeil(feilkode = "FP-947232", feilmelding = "Kan ikke oppdatere registrerte personopplysninger etter at det har blitt satt.", logLevel = LogLevel.WARN)
    Feil kanIkkeOppdatereRegistrertVersjon();

    @TekniskFeil(feilkode = "FP-124903", feilmelding = "Må basere seg på eksisterende versjon av personopplysning", logLevel = LogLevel.WARN)
    Feil måBasereSegPåEksisterendeVersjon();
}

