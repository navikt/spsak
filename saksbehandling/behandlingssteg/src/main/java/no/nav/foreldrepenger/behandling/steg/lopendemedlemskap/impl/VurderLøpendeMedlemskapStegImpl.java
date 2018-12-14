package no.nav.foreldrepenger.behandling.steg.lopendemedlemskap.impl;

import java.time.LocalDate;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.behandling.steg.lopendemedlemskap.api.VurderLøpendeMedlemskapSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.VilkårData;
import no.nav.foreldrepenger.domene.inngangsvilkaar.medlemskap.VurderLøpendeMedlemskap;
import no.nav.vedtak.util.Tuple;

@BehandlingStegRef(kode = "VULOMED")
@BehandlingTypeRef("BT-004")
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class VurderLøpendeMedlemskapStegImpl implements VurderLøpendeMedlemskapSteg {

    static final String FPSAK_LØPENDE_MEDLEMSKAP = "fpsak.lopende-medlemskap";
    private VurderLøpendeMedlemskap vurderLøpendeMedlemskap;
    private MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository;
    private Unleash unleash;
    private BehandlingRepository behandlingRepository;

    @Inject
    public VurderLøpendeMedlemskapStegImpl(Unleash unleash, VurderLøpendeMedlemskap vurderLøpendeMedlemskap,
                                           BehandlingRepositoryProvider provider) {
        this.unleash = unleash;
        this.vurderLøpendeMedlemskap = vurderLøpendeMedlemskap;
        this.medlemskapVilkårPeriodeRepository = provider.getMedlemskapVilkårPeriodeRepository();
        this.behandlingRepository = provider.getBehandlingRepository();
    }

    VurderLøpendeMedlemskapStegImpl() {
        //CDI
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        if (unleash.isEnabled(FPSAK_LØPENDE_MEDLEMSKAP)) {
            Long behandlingId = kontekst.getBehandlingId();
            Map<LocalDate, VilkårData> localDateVilkårDataMap = vurderLøpendeMedlemskap.vurderLøpendeMedlemskap(behandlingId);
            Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

            MedlemskapVilkårPeriodeGrunnlagEntitet.Builder builder = medlemskapVilkårPeriodeRepository.hentBuilderFor(behandling);
            MedlemskapsvilkårPeriodeEntitet.Builder perioderBuilder = builder.getPeriodeBuilder();

            localDateVilkårDataMap.forEach((vurderingsdato, value) -> {
                MedlemskapsvilkårPerioderEntitet.Builder periodeBuilder = perioderBuilder.getBuilderForVurderingsdato(vurderingsdato);
                periodeBuilder.medVurderingsdato(vurderingsdato);
                periodeBuilder.medVilkårUtfall(value.getUtfallType());
                perioderBuilder.leggTil(periodeBuilder);
            });
            builder.medMedlemskapsvilkårPeriode(perioderBuilder);
            medlemskapVilkårPeriodeRepository.lagreMedlemskapsvilkår(behandling, builder);

            Tuple<VilkårUtfallType, VilkårUtfallMerknad> utfall = medlemskapVilkårPeriodeRepository.utledeVilkårStatus(behandling);
            VilkårResultat.Builder vilkårBuilder = VilkårResultat.builderFraEksisterende(behandling.getBehandlingsresultat().getVilkårResultat());
            vilkårBuilder.leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, utfall.getElement1());

            BehandlingLås lås = kontekst.getSkriveLås();
            behandlingRepository.lagre(vilkårBuilder.buildFor(behandling), lås);
        }
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }
}
