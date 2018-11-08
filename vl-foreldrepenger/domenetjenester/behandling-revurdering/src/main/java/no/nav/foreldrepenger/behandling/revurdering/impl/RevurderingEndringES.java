package no.nav.foreldrepenger.behandling.revurdering.impl;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.vedtak.feil.FeilFactory;

/** Sjekk om revurdering endrer utfall. */
@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class RevurderingEndringES implements RevurderingEndring {

    public RevurderingEndringES() {
    }

    @Override
    public boolean erRevurderingMedUendretUtfall(Behandling behandling) {
        if (!BehandlingType.REVURDERING.equals(behandling.getType())) {
            return false;
        }
        Optional<Behandling> originalBehandlingOptional = behandling.getOriginalBehandling();

        if (!originalBehandlingOptional.isPresent()) {
            throw FeilFactory.create(RevurderingFeil.class).revurderingManglerOriginalBehandling(behandling.getId()).toException();
        }

        Behandling originalBehandling = originalBehandlingOptional.get();
        BehandlingResultatType originalResultatType = originalBehandling.getBehandlingsresultat().getBehandlingResultatType();
        BehandlingResultatType nyResultatType = behandling.getBehandlingsresultat().getBehandlingResultatType();

        // Forskjellig utfall
        if (!nyResultatType.equals(originalResultatType)) {
            return false;
        }

        // Begge har utfall INNVILGET
        if (nyResultatType.equals(BehandlingResultatType.INNVILGET)) {
            Optional<Beregning> nyBeregning = behandling.getBehandlingsresultat().getBeregningResultat().getSisteBeregning();
            Optional<Beregning> originalBeregning = originalBehandling.getBehandlingsresultat().getBeregningResultat().getSisteBeregning();
            if (originalBeregning.isPresent() && nyBeregning.isPresent()) {
                return harSammeBeregnetYtelse(nyBeregning.get(), originalBeregning.get());
            } else {
                throw FeilFactory.create(RevurderingFeil.class)
                    .behandlingManglerBeregning(originalBeregning.isPresent() ? behandling.getId() : originalBehandling.getId())
                    .toException();
            }
        }
        // Begge har utfall AVSLÅTT
        return true;
    }

    private boolean harSammeBeregnetYtelse(Beregning nyBeregning, Beregning originalBeregning) {
        return Objects.equals(nyBeregning.getBeregnetTilkjentYtelse(), originalBeregning.getBeregnetTilkjentYtelse());
    }

}
