package no.nav.foreldrepenger.web.app.tjenester.behandling.ytelsefordeling;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoer;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeAleneOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderAleneOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderUtenOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.domene.ytelsefordeling.YtelseFordelingTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.omsorg.PeriodeKonverter;

@ApplicationScoped
public class YtelseFordelingDtoTjenesteImpl implements YtelseFordelingDtoTjeneste {

    private YtelseFordelingTjeneste ytelseFordelingTjeneste;

    YtelseFordelingDtoTjenesteImpl() {
        //CDI
    }

    @Inject
    public YtelseFordelingDtoTjenesteImpl(YtelseFordelingTjeneste ytelseFordelingTjeneste) {
        this.ytelseFordelingTjeneste = ytelseFordelingTjeneste;
    }

    @Override
    public Optional<YtelseFordelingDto> mapFra(Behandling behandling) {
        Optional<YtelseFordelingAggregat> ytelseFordelingAggregat = ytelseFordelingTjeneste.hentAggregatHvisEksisterer(behandling);
        if(ytelseFordelingAggregat.isPresent()) {
            Optional<PerioderUtenOmsorg> perioderUtenOmsorg = ytelseFordelingAggregat.get().getPerioderUtenOmsorg();
            Optional<PerioderAleneOmsorg> perioderAleneOmsorg = ytelseFordelingAggregat.get().getPerioderAleneOmsorg();
            Optional<AvklarteUttakDatoer> avklarteUttakDatoerOpt = ytelseFordelingAggregat.get().getAvklarteDatoer();

            YtelseFordelingDto.Builder dtoBuilder = new YtelseFordelingDto.Builder();
            if(perioderAleneOmsorg.isPresent()) {
                List<PeriodeAleneOmsorg> periodeAleneOmsorgs = perioderAleneOmsorg.get().getPerioder();
                dtoBuilder.medAleneOmsorgPerioder(PeriodeKonverter.mapAleneOmsorgsperioder(periodeAleneOmsorgs));
            }
            if(perioderUtenOmsorg.isPresent()) {
                List<PeriodeUtenOmsorg> periodeUtenOmsorgs = perioderUtenOmsorg.get().getPerioder();
                dtoBuilder.medIkkeOmsorgPerioder(PeriodeKonverter.mapUtenOmsorgperioder(periodeUtenOmsorgs));
            }

            if (avklarteUttakDatoerOpt.isPresent()) {
                dtoBuilder.medEndringsDato(avklarteUttakDatoerOpt.get().getEndringsdato());
                dtoBuilder.medFørsteUttaksDato(avklarteUttakDatoerOpt.get().getFørsteUttaksDato());
            }
            return Optional.of(dtoBuilder.build());

        }
        return Optional.empty();
    }

}
