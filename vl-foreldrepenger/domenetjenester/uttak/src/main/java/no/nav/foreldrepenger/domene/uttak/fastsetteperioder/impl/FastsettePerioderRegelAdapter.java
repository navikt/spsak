package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.FastsettePerioderRegelGrunnlagBygger;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.FastsettePerioderRegelResultatKonverterer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.uttaksvilkår.FastsettePeriodeResultat;
import no.nav.foreldrepenger.uttaksvilkår.FastsettePerioderRegelOrkestrering;
import no.nav.vedtak.felles.integrasjon.unleash.FeatureToggle;

@ApplicationScoped
public class FastsettePerioderRegelAdapter {

    private static final FastsettePerioderRegelOrkestrering regel = new FastsettePerioderRegelOrkestrering();
    private FastsettePerioderRegelGrunnlagBygger regelGrunnlagBygger;
    private FastsettePerioderRegelResultatKonverterer regelResultatKonverterer;
    private Unleash unleash;

    FastsettePerioderRegelAdapter() {
        // For CDI
    }

    @Inject
    public FastsettePerioderRegelAdapter(FastsettePerioderRegelGrunnlagBygger regelGrunnlagBygger,
                                         FastsettePerioderRegelResultatKonverterer regelResultatKonverterer,
                                         @FeatureToggle("fpsak") Unleash unleash) {
        this.regelGrunnlagBygger = regelGrunnlagBygger;
        this.regelResultatKonverterer = regelResultatKonverterer;
        this.unleash = unleash;
    }

    public UttakResultatPerioderEntitet fastsettePerioder(Behandling behandling) {
        FastsettePeriodeGrunnlag grunnlag = regelGrunnlagBygger.byggGrunnlag(behandling);
        List<FastsettePeriodeResultat> resultat = regel.fastsettePerioder(grunnlag, new FastsettePeriodeFeatureToggles(unleash, behandling));
        return regelResultatKonverterer.konverter(behandling, grunnlag.getAktiviteter(), resultat);
    }
}









