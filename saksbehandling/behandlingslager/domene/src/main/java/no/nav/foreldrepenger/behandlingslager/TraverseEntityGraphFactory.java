package no.nav.foreldrepenger.behandlingslager;

import java.util.Arrays;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.TraverseEntityGraph;
import no.nav.foreldrepenger.behandlingslager.diff.YtelseKode;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabell;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;

public final class TraverseEntityGraphFactory {
    private TraverseEntityGraphFactory() {
    }

    public static TraverseEntityGraph build(boolean medChangedTrackedOnly) {
        return build(medChangedTrackedOnly, null);
    }

    public static TraverseEntityGraph build(boolean medChangedTrackedOnly, YtelseKode ytelseKode) {
        /* default oppsett for behandlingslager. */
        TraverseEntityGraph traverseEntityGraph = new TraverseEntityGraph();
        traverseEntityGraph.setIgnoreNulls(true);
        if (ytelseKode != null) {
            traverseEntityGraph.setYtelseKoder(Arrays.asList(ytelseKode));
        }
        traverseEntityGraph.setOnlyCheckTrackedFields(medChangedTrackedOnly);
        traverseEntityGraph.addLeafClasses(KodeverkTabell.class);
        traverseEntityGraph.addLeafClasses(Kodeliste.class);
        traverseEntityGraph.addLeafClasses(DatoIntervallEntitet.class, ÅpenDatoIntervallEntitet.class);
        traverseEntityGraph.addRootClasses(Behandling.class, SøknadEntitet.class);
        return traverseEntityGraph;
    }

    public static TraverseEntityGraph build() {
        return build(false);
    }
}
