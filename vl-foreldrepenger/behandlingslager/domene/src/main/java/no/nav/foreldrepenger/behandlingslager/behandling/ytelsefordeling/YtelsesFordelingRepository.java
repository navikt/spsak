package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

public interface YtelsesFordelingRepository extends BehandlingslagerRepository {

    YtelseFordelingAggregat hentAggregat(Behandling behandling);

    Optional<YtelseFordelingGrunnlagEntitet> hentYtelseFordelingPåId(Long grunnlagId);

    YtelseFordelingAggregat hentYtelsesFordelingPåId(Long aggregatId);

    Optional<YtelseFordelingAggregat> hentAggregatHvisEksisterer(Behandling behandling);

    void lagre(Behandling behandling, OppgittRettighet oppgittRettighet);

    void lagre(Behandling behandling, OppgittFordeling oppgittPerioder);

    void lagreOverstyrtFordeling(Behandling behandling, OppgittFordeling oppgittPerioder, PerioderUttakDokumentasjonEntitet perioderUttakDokumentasjon);

    void lagreOverstyrtFordeling(Behandling behandling, OppgittFordeling oppgittPerioder);

    void lagre(Behandling behandling, OppgittDekningsgrad oppgittDekningsgrad);

    void lagre(Behandling behandling, PerioderUtenOmsorg perioderUtenOmsorg);

    void lagre(Behandling behandling, PerioderAleneOmsorg perioderAleneOmsorg);

    void lagre(Behandling behandling, AvklarteUttakDatoer avklarteUttakDatoer);

    /**
     * Kopierer grunnlag fra en tidligere behandling.  Endrer ikke aggregater, en skaper nye referanser til disse.
     */
    void kopierGrunnlagFraEksisterendeBehandling(Behandling gammelBehandling, Behandling nyBehandling);

    boolean erEndring(Behandling behandling, Behandling nyttAggregat);

    boolean erEndret(Long aggregat, Behandling nyttAggregat);

    Optional<Long> hentIdPåAktivYtelsesFordeling(Behandling behandling);

    void tilbakestillOverstyringOgDokumentasjonsperioder(Behandling behandling);

    void tilbakestillAvklarteDatoer(Behandling behandling);

    DiffResult diffResultat(Long grunnlagId1, Long grunnlagId2, FagsakYtelseType ytelseType, boolean kunSporedeEndringer);
}
