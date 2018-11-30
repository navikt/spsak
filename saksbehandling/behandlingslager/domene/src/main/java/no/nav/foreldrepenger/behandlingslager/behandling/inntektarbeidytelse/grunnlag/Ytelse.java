package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag;

import java.util.Collection;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.FagsystemUnderkategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface Ytelse {

    RelatertYtelseType getRelatertYtelseType();

    TemaUnderkategori getBehandlingsTema();

    RelatertYtelseTilstand getStatus();

    DatoIntervallEntitet getPeriode();

    Saksnummer getSaksnummer();

    Fagsystem getKilde();

    FagsystemUnderkategori getFagsystemUnderkategori();

    Optional<YtelseGrunnlag> getYtelseGrunnlag();

    Collection<YtelseAnvist> getYtelseAnvist();
}
