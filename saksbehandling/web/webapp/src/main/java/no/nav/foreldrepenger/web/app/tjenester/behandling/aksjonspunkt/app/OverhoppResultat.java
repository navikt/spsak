package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;

public class OverhoppResultat {
    Set<OppdateringResultat> oppdatereResultater = new LinkedHashSet<>();

    public static OverhoppResultat tomtResultat() {
        return new OverhoppResultat();
    }

    public void leggTil(OppdateringResultat delresultat) {
        oppdatereResultater.add(delresultat);
    }

    public Optional<BehandlingStegType> finnTilbakehoppSteg() {
        return oppdatereResultater.stream()
            .filter(delresultat -> delresultat.getOverhoppKontroll().equals(OverhoppKontroll.TILBAKEHOPP))
            .map(delresultat -> delresultat.getNesteSteg())
            .findFirst(); // TODO (essv): Sorter steg ut fra deres rekkefølge
    }

    public Optional<TransisjonIdentifikator> finnFremoverTransisjon() {
        return oppdatereResultater.stream()
            .filter(delresultat -> delresultat.getOverhoppKontroll().equals(OverhoppKontroll.FREMOVERHOPP))
            .map(OppdateringResultat::getTransisjon)
            .findFirst(); // TODO (essv): Sorter steg ut fra deres rekkefølge
    }

    public Optional<OppdateringResultat> finnHenleggelse() {
        return oppdatereResultater.stream()
            .filter(delresultat -> delresultat.getOverhoppKontroll().equals(OverhoppKontroll.HENLEGGELSE))
            .findFirst();
    }

    @Override
    public String toString() {
        return "OverhoppResultat{" +
            "oppdatereResultater=" + oppdatereResultater +
            '}';
    }
}
