package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;

public interface OverstyrUttakResultatValidator {

    void valider(Fagsak fagsak, UttakResultatPerioder opprinnelig, UttakResultatPerioder perioder);
}
