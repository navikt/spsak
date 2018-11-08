package no.nav.foreldrepenger.domene.familiehendelse.omsorg.impl;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.OmsorgsovertakelseVilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.UidentifisertBarnEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.domene.personopplysning.AvklarForeldreansvarAksjonspunktData;
import no.nav.foreldrepenger.domene.personopplysning.AvklarOmsorgOgForeldreansvarAksjonspunktData;
import no.nav.foreldrepenger.domene.personopplysning.AvklartDataBarnAdapter;

class AvklarOmsorgOgForeldreansvarAksjonspunkt {

    private FamilieHendelseRepository familieGrunnlagRepository;
    private AksjonspunktRepository aksjonspunktRepository;
    private VilkårKodeverkRepository vilkårKodeverkRepository;

    AvklarOmsorgOgForeldreansvarAksjonspunkt(BehandlingRepositoryProvider repositoryProvider) {
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.vilkårKodeverkRepository = repositoryProvider.getVilkårKodeverkRepository();
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
    }

    void oppdater(Behandling behandling, AvklarForeldreansvarAksjonspunktData data) {
        // Omsorgsovertakelse
        avklareOmsorgovertakelse(behandling, data);
    }

    void oppdater(Behandling behandling, AvklarOmsorgOgForeldreansvarAksjonspunktData data) {
        // Omsorgsovertakelse
        avklareOmsorgovertakelse(behandling, data);

        // Aksjonspunkter
        behandling.getAksjonspunkter().stream()
            .filter(ap -> OmsorgsvilkårKonfigurasjon.getOmsorgsovertakelseAksjonspunkter().contains(ap.getAksjonspunktDefinisjon()))
            .filter(ap -> !Objects.equals(ap.getAksjonspunktDefinisjon(), data.getAksjonspunktDefinisjon())) // ikke avbryte seg selv
            .forEach(ap -> aksjonspunktRepository.setTilAvbrutt(ap));
    }

    private void avklareOmsorgovertakelse(Behandling behandling, AvklarForeldreansvarAksjonspunktData data) {
        final FamilieHendelseBuilder oppdatertOverstyrtHendelse = familieGrunnlagRepository.opprettBuilderFor(behandling);
        oppdatertOverstyrtHendelse
            .medAdopsjon(oppdatertOverstyrtHendelse.getAdopsjonBuilder()
                .medOmsorgovertalseVilkårType(OmsorgsovertakelseVilkårType.FORELDREANSVARSVILKÅRET_2_LEDD)
                .medOmsorgsovertakelseDato(data.getOmsorgsovertakelseDato())
                .medForeldreansvarDato(data.getForeldreansvarDato()))
            .tilbakestillBarn()
            .medAntallBarn(data.getAntallBarn());
        for (AvklartDataBarnAdapter avklartDataBarnAdapter : data.getBarn()) {
            oppdatertOverstyrtHendelse.leggTilBarn(new UidentifisertBarnEntitet(avklartDataBarnAdapter.getFødselsdato(), avklartDataBarnAdapter.getNummer()));
        }
        familieGrunnlagRepository.lagreOverstyrtHendelse(behandling, oppdatertOverstyrtHendelse);
    }

    private void avklareOmsorgovertakelse(Behandling behandling, AvklarOmsorgOgForeldreansvarAksjonspunktData data) {
        OmsorgsovertakelseVilkårType omsorgsovertakelseVilkårType = vilkårKodeverkRepository.finnOmsorgsovertakelseVilkårtype(data.getVilkarTypeKode());
        final FamilieHendelseBuilder oppdatertOverstyrtHendelse = familieGrunnlagRepository.opprettBuilderFor(behandling);
        oppdatertOverstyrtHendelse
            .medAdopsjon(oppdatertOverstyrtHendelse.getAdopsjonBuilder()
                .medOmsorgovertalseVilkårType(omsorgsovertakelseVilkårType)
                .medOmsorgsovertakelseDato(data.getOmsorgsovertakelseDato()))
            .tilbakestillBarn()
            .medAntallBarn(data.getAntallBarn());
        for (AvklartDataBarnAdapter avklartDataBarnAdapter : data.getBarn()) {
            oppdatertOverstyrtHendelse.leggTilBarn(new UidentifisertBarnEntitet(avklartDataBarnAdapter.getFødselsdato(), avklartDataBarnAdapter.getNummer()));
        }
        familieGrunnlagRepository.lagreOverstyrtHendelse(behandling, oppdatertOverstyrtHendelse);
    }

}
