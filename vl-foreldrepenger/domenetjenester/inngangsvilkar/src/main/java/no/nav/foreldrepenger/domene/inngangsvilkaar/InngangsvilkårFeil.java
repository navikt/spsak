package no.nav.foreldrepenger.domene.inngangsvilkaar;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface InngangsvilkårFeil extends DeklarerteFeil {

    InngangsvilkårFeil FACTORY = FeilFactory.create(InngangsvilkårFeil.class);

    @TekniskFeil(feilkode = "FP-905511", feilmelding = "Fant ingen kompletthetssjekk for behandling av type %s", logLevel = WARN)
    Feil ukjentType(String fagsakYtelseType);

    @TekniskFeil(feilkode = "FP-905512", feilmelding = "Mer enn en implementasjon funnet av kompletthetssjekk for behandling av type %s", logLevel = WARN)
    Feil flereImplementasjonerAvKompletthetsSjekk(String fagsakYtelseType);
}
