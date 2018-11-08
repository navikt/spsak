package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.grunnlagbyggere;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.domene.uttak.UttakOmsorgUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;

public final class OmsorgOgRettGrunnlagBygger {

    public void byggGrunnlag(FastsettePeriodeGrunnlagBuilder grunnlagBuilder,
                             Behandling behandling,
                             YtelseFordelingAggregat ytelseFordelingAggregat) {
        leggTilHvemSomHarRett(grunnlagBuilder, behandling, ytelseFordelingAggregat);
        leggTilPerioderMedAleneomsorg(grunnlagBuilder, ytelseFordelingAggregat);
    }

    private void leggTilPerioderMedAleneomsorg(FastsettePeriodeGrunnlagBuilder grunnlagBuilder, YtelseFordelingAggregat ytelseFordelingAggregat) {
        grunnlagBuilder.medAleneomsorg(UttakOmsorgUtil.harAleneomsorg(ytelseFordelingAggregat));
    }

    private void leggTilHvemSomHarRett(FastsettePeriodeGrunnlagBuilder grunnlagBuilder,
                                       Behandling behandling,
                                       YtelseFordelingAggregat ytelseFordelingAggregat) {
        boolean annenForelderRett = UttakOmsorgUtil.harAnnenForelderRett(ytelseFordelingAggregat.getOppgittRettighet());
        boolean søkerRett = UttakOmsorgUtil.harSøkerRett(behandling);
        if (RelasjonsRolleType.erMor(behandling.getRelasjonsRolleType())) {
            grunnlagBuilder.medFarRett(annenForelderRett);
            grunnlagBuilder.medMorRett(søkerRett);
        }
        if (RelasjonsRolleType.erFarEllerMedmor(behandling.getRelasjonsRolleType())) {
            grunnlagBuilder.medMorRett(annenForelderRett);
            grunnlagBuilder.medFarRett(søkerRett);
        }
    }
}
