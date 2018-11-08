package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;

/**
 * Dette er et Repository for håndtering av alle persistente endringer i en søkers perioder for medlemskapvilkår
 * <p>
 * Merk: "standard" regler adoptert for "grunnlag" (ikke helt standard, ettersom vi her knytter
 * MedlemskapVilkårPeriodeGrunnlag til Vilkårsresultat i stedet for Behandling) - ett Grunnlag eies av ett
 * Vilkårsresultat. Et Aggregat (MedlemskapVilkårPeriodeGrunnlag-graf) har en selvstenig livssyklus og vil kopieres
 * ved hver endring.
 * Ved multiple endringer i et grunnlag for et MedlemskapVilkårPeriodeGrunnlag vil alltid kun et innslag i grunnlag
 * være aktiv for angitt Vilkårsresultat.
 */
public interface MedlemskapVilkårPeriodeRepository extends BehandlingslagerRepository {

    Optional<MedlemskapsvilkårPeriodeGrunnlag> hentAggregatHvisEksisterer(Behandling behandling);

    void kopierGrunnlagFraEksisterendeBehandling(Behandling eksisterendeBehandling, Behandling nyBehandling);

    MedlemskapsvilkårPeriodeGrunnlag.Builder hentBuilderFor(Behandling behandling);

    void lagreMedlemskapsvilkår(Behandling behandling, MedlemskapsvilkårPeriodeGrunnlag.Builder builder);

    VilkårUtfallType utledeVilkårStatus(Behandling behandling);
}
