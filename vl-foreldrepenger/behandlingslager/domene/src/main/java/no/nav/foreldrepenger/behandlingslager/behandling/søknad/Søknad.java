package no.nav.foreldrepenger.behandlingslager.behandling.søknad;

import java.time.LocalDate;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgrad;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;

public interface Søknad {

    LocalDate getSøknadsdato();

    LocalDate getMottattDato();

    boolean getElektroniskRegistrert();

    String getBegrunnelseForSenInnsending();

    String getKildeReferanse();

    FarSøkerType getFarSøkerType();

    OppgittFordeling getFordeling();

    OppgittDekningsgrad getDekningsgrad();

    OppgittRettighet getRettighet();

    OppgittTilknytning getOppgittTilknytning();

    String getTilleggsopplysninger();

    Set<SøknadVedlegg> getSøknadVedlegg();

    OppgittOpptjening getOppgittOpptjening();

    boolean erEndringssøknad();

    RelasjonsRolleType getRelasjonsRolleType();
}
