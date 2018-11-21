package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingerBuilder;

public interface SykefraværRepository extends BehandlingslagerRepository {

    SykemeldingerBuilder oppretBuilderForSykemeldinger(Long behandlingId);

    SykefraværBuilder oppretBuilderForSykefravær(Long behandlingId);

    void lagre(Behandling behandling, SykemeldingerBuilder builder);

    void lagre(Behandling behandling, SykefraværBuilder builder);

    /**
     * Henter ut Sykefraværs grunnlaget hvis dette eksisterer.
     *
     * @param behandlingId behandlingen
     * @return grunnlaget for behandlingen, optional.empty hvis ikke
     */
    Optional<SykefraværGrunnlag> hentHvisEksistererFor(Long behandlingId);

    /**
     * Henter ut Sykefraværs grunnlaget hvis dette eksisterer.
     * NB! Metoden forutsetter at det eksisterer og kaster exception hvis det ikke gjør det.
     *
     * @param behandlingId behandlingen
     * @return grunnlaget for behandlinge
     * @throws IllegalStateException hvis det ikke finnes når det er forventet at det skal finnes
     */
    SykefraværGrunnlag hentFor(Long behandlingId);

}
