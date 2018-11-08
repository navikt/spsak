package no.nav.foreldrepenger.domene.medlem.impl;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.behandlingslager.diff.YtelseKode;

@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class MedlemEndringssjekkerEngangsstønad extends MedlemEndringssjekker {

    @Override
    public RegisterdataDiffsjekker opprettNyDiffer() {
        return new RegisterdataDiffsjekker(YtelseKode.ES);
    }
}
