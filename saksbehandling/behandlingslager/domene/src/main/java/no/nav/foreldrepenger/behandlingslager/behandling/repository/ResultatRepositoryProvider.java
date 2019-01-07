package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.fravær.FraværResultatRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;

/**
 * Repository provider for resultatstrukturer
 */
public interface ResultatRepositoryProvider {

    KodeverkRepository getKodeverkRepository();

    FraværResultatRepository getFraværResultatRepository();

    BehandlingVedtakRepository getVedtakRepository();

    OpptjeningRepository getOpptjeningRepository();

    BeregningsgrunnlagRepository getBeregningsgrunnlagRepository();

    BeregningsresultatRepository getBeregningsresultatRepository();

    MedlemskapVilkårPeriodeRepository getMedlemskapVilkårPeriodeRepository();

    BehandlingRepository getBehandlingRepository();

    // TEMP
    UttakRepository getUttakRepository();

}
