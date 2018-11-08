package no.nav.foreldrepenger.domene.ytelse.beregning.impl;

import java.time.LocalDateTime;

import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.vedtak.util.FPDateUtil;

class TilkjentYtelseForEngangsstønad {



    private BeregningRepository beregningRepository;

    TilkjentYtelseForEngangsstønad(BeregningRepository beregningRepository) {
        this.beregningRepository = beregningRepository;
    }

    private Long finnOpprinneligBeløp(Beregning forrigeBeregning) {
        Long opprinneligBeløp;
        if (forrigeBeregning.isOverstyrt()) {
            opprinneligBeløp = forrigeBeregning.getOpprinneligBeregnetTilkjentYtelse();
        } else {
            opprinneligBeløp = forrigeBeregning.getBeregnetTilkjentYtelse();
        }
        return opprinneligBeløp;
    }

    void overstyrTilkjentYtelse(Long behandlingId, Beregning forrigeBeregning, Long tilkjentYtelse) {

        Beregning overstyrtBeregning = new Beregning(forrigeBeregning.getSatsVerdi(),
                forrigeBeregning.getAntallBarn(),
                tilkjentYtelse,
                LocalDateTime.now(FPDateUtil.getOffset()),
                true,
                finnOpprinneligBeløp(forrigeBeregning));

        beregningRepository.lagreBeregning(behandlingId, overstyrtBeregning);
    }
}
