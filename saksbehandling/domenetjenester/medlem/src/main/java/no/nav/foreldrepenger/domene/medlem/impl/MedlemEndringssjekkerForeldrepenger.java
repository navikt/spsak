package no.nav.foreldrepenger.domene.medlem.impl;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.behandlingslager.diff.YtelseKode;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class MedlemEndringssjekkerForeldrepenger extends MedlemEndringssjekker {

    @Override
    public RegisterdataDiffsjekker opprettNyDiffer() {
        return new RegisterdataDiffsjekker(YtelseKode.FP);
    }
}
