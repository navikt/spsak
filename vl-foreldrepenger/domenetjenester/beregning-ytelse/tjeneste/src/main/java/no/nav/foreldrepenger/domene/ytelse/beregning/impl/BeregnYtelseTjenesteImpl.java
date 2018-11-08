package no.nav.foreldrepenger.domene.ytelse.beregning.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.domene.ytelse.beregning.BeregnYtelseTjeneste;

@ApplicationScoped
public class BeregnYtelseTjenesteImpl implements BeregnYtelseTjeneste {

    private BeregningRepository beregningRepository;

    BeregnYtelseTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public BeregnYtelseTjenesteImpl(BeregningRepository beregningRepository) {
        this.beregningRepository = beregningRepository;
    }

    /** Overstyr tilkjent engangsytelse (for Engangsstønad). */
    @Override
    public void overstyrTilkjentYtelseForEngangsstønad(Behandling behandling, Long tilkjentYtelse) {
        Optional<Beregning> beregningOptional = beregningRepository.getSisteBeregning(behandling.getId());
        if (beregningOptional.isPresent()) {
            Beregning forrigeBeregning = beregningOptional.get();

            new TilkjentYtelseForEngangsstønad(beregningRepository)
                    .overstyrTilkjentYtelse(behandling.getId(), forrigeBeregning, tilkjentYtelse);

        }
    }

}
