package no.nav.foreldrepenger.behandling.søknadsfrist.impl;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.regler.søknadsfrist.grunnlag.SøknadsfristGrunnlag;
import no.nav.foreldrepenger.uttaksvilkår.SøknadsfristRegelOrkestrering;
import no.nav.foreldrepenger.uttaksvilkår.SøknadsfristResultat;

class SøknadsfristForeldrepengerRegelAdapter {

    public SøknadsfristResultat vurderSøknadsfristForForeldrepenger(Søknad søknad, List<OppgittPeriode> oppgittePerioder, int antallMånederSøknadsfrist) {
        SøknadsfristGrunnlag søknadsfristGrunnlag = SøknadsfristForeldrepengerRegelOversetter.tilGrunnlag(søknad, oppgittePerioder, antallMånederSøknadsfrist);
        SøknadsfristRegelOrkestrering regelOrkestrering = new SøknadsfristRegelOrkestrering();
        SøknadsfristResultat søknadsfristResultat = regelOrkestrering.vurderSøknadsfrist(søknadsfristGrunnlag);
        return søknadsfristResultat;
    }
}
