package no.nav.foreldrepenger.domene.uttak.fastsetteperioder;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.uttaksvilkår.FastsettePeriodeResultat;

public interface FastsettePerioderRegelResultatKonverterer {
    UttakResultatPerioderEntitet konverter(Behandling behandling,
                                           List<AktivitetIdentifikator> aktiviteter,
                                           List<FastsettePeriodeResultat> resultat);
}
