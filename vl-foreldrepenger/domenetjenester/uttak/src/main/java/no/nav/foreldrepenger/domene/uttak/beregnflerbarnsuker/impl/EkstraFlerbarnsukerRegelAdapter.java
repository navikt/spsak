package no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker.impl;

import static java.util.Objects.requireNonNull;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.domene.uttak.UttakOmsorgUtil;
import no.nav.foreldrepenger.domene.uttak.beregnkontoer.impl.StønadskontoRegelOversetter;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.uttaksvilkår.StønadskontoRegelOrkestrering;
import no.nav.foreldrepenger.uttaksvilkår.StønadskontoResultat;

class EkstraFlerbarnsukerRegelAdapter {

    private StønadskontoRegelOrkestrering stønadskontoRegel = new StønadskontoRegelOrkestrering();
    private StønadskontoRegelOversetter stønadskontoRegelOversetter = new StønadskontoRegelOversetter();

    Integer beregnEkstraFlerbarnsuker(Behandling behandling, FamilieHendelseGrunnlag familieHendelseGrunnlag, YtelseFordelingAggregat ytelseFordelingAggregat, FagsakRelasjon fagsakRelasjon) {
        requireNonNull(familieHendelseGrunnlag);
        requireNonNull(fagsakRelasjon);

        boolean harSøkerRett = UttakOmsorgUtil.harSøkerRett(behandling);
        Integer antallBarn = familieHendelseGrunnlag.getGjeldendeVersjon().getAntallBarn();

        // hvis ett barn født = ikke noe ekstra dager.
        if (antallBarn == 1) {
            return 0;
        }

        BeregnKontoerGrunnlag beregnKontoerGrunnlag = stønadskontoRegelOversetter.tilRegelmodell(behandling.getRelasjonsRolleType(), familieHendelseGrunnlag,
            ytelseFordelingAggregat, fagsakRelasjon, harSøkerRett);
        StønadskontoResultat resultat = stønadskontoRegel.beregnKontoer(beregnKontoerGrunnlag);
        Integer antallFlerbarnsdager = resultat.getAntallFlerbarnsdager();
        return antallFlerbarnsdager != null ? antallFlerbarnsdager / 5 : 0;
    }
}
