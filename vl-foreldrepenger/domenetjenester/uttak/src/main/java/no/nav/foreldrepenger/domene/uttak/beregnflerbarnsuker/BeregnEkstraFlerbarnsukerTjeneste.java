package no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface BeregnEkstraFlerbarnsukerTjeneste {

    /**
     * Beregner hvor mange uker ekstra får man ved flerbarnfødsel for en gitt behandling.
     *
     * @param behandling behandling som skal sjekkes.
     *
     * @return antall dager man får ekstra ved flerbarnfødsel. 0 hvis ikke noe ekstra dager.
     */
    Integer beregneEkstraFlerbarnsuker(Behandling behandling);

}
