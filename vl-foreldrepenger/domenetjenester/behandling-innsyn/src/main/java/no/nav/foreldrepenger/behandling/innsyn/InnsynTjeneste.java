package no.nav.foreldrepenger.behandling.innsyn;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynResultat;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public interface InnsynTjeneste {

    Behandling opprettManueltInnsyn(Saksnummer saksnummer);

    void lagreVurderInnsynResultat(Behandling behandling, InnsynResultat<?> innsynResultat);
}
