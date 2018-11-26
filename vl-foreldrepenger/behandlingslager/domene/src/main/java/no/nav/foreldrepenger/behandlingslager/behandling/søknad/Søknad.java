package no.nav.foreldrepenger.behandlingslager.behandling.søknad;

import java.time.LocalDate;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytning;

public interface Søknad {

    LocalDate getSøknadsdato();

    LocalDate getMottattDato();

    OppgittTilknytning getOppgittTilknytning();

    String getTilleggsopplysninger();

    Set<SøknadVedlegg> getSøknadVedlegg();

    OppgittOpptjening getOppgittOpptjening();

}
