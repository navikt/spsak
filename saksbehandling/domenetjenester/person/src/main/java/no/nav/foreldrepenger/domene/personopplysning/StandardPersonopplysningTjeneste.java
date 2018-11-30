package no.nav.foreldrepenger.domene.personopplysning;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface StandardPersonopplysningTjeneste {

    /**
     * Gir personopplysningene på skjæringstidspunktet
     * @param behandling
     * @return personopplysninger
     */
    PersonopplysningerAggregat hentPersonopplysninger(Behandling behandling);

    Optional<PersonopplysningerAggregat> hentPersonopplysningerHvisEksisterer(Behandling behandling);

    /**
     * Filtrerer, og gir personopplysning-historikk som er gyldig for intervall [tidspunkt, tidspunkt+1).
     */
    PersonopplysningerAggregat hentGjeldendePersoninformasjonPåTidspunkt(Behandling behandling, LocalDate tidspunkt);

    /**
     * Filtrerer, og gir personopplysning-historikk som er gyldig for intervall [tidspunkt, tidspunkt+1).
     */
    Optional<PersonopplysningerAggregat> hentGjeldendePersoninformasjonPåTidspunktHvisEksisterer(Behandling behandling, LocalDate tidspunkt);

    /**
     * Filtrerer, og gir personopplysning-historikk som er gyldig for angitt intervall.
     */
    Optional<PersonopplysningerAggregat> hentGjeldendePersoninformasjonForPeriodeHvisEksisterer(Behandling behandling, DatoIntervallEntitet forPeriode);

}
