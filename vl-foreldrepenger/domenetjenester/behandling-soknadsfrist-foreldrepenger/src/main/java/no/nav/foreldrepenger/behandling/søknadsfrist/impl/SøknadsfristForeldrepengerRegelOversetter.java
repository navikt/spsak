package no.nav.foreldrepenger.behandling.søknadsfrist.impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.regler.søknadsfrist.grunnlag.SøknadsfristGrunnlag;

class SøknadsfristForeldrepengerRegelOversetter {

    private SøknadsfristForeldrepengerRegelOversetter() {
        // For å unngå instanser
    }

    public static SøknadsfristGrunnlag tilGrunnlag(Søknad søknad, List<OppgittPeriode> oppgittePerioder, int antallMånederSøknadsfrist) {
        List<OppgittPeriode> uttaksperioder = oppgittePerioder.stream()
            .sorted(Comparator.comparing(OppgittPeriode::getFom))
            .collect(Collectors.toList());

        SøknadsfristGrunnlag grunnlag = SøknadsfristGrunnlag.builder()
            .medSøknadMottattDato(søknad.getMottattDato())
            .medErSøknadOmUttak(!uttaksperioder.isEmpty())
            .medFørsteUttaksdato(uttaksperioder.isEmpty() ? null : uttaksperioder.get(0).getFom())
            .medAntallMånederSøknadsfrist(antallMånederSøknadsfrist)
            .build();

        return grunnlag;
    }
}
