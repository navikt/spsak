package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Rule;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaPeriode;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.BekreftetUttakPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.KontrollerFaktaPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.ManuellAvklarFaktaUttakDto;

public class AvklarFaktaTestUtil {
    @Rule
    public static UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private static YtelsesFordelingRepository fordelingRepository = new YtelsesFordelingRepositoryImpl(repositoryRule.getEntityManager());

    private AvklarFaktaTestUtil() {
    }

    public static AvklarFaktaUttakDto opprettDtoAvklarFaktaUttakDto() {
        AvklarFaktaUttakDto dto = new AvklarFaktaUttakDto.AvklarFaktaUttakPerioderDto();
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto1 = getBekreftetUttakPeriodeDto(LocalDate.now().minusDays(20), LocalDate.now().minusDays(11), true);
        bekreftetUttakPeriodeDto1.setOrginalFom(LocalDate.now().minusDays(20));
        bekreftetUttakPeriodeDto1.setOrginalTom(LocalDate.now().minusDays(11));
        bekreftetUttakPeriodeDto1.setOriginalResultat(UttakPeriodeVurderingType.PERIODE_OK);
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto2 = getBekreftetUttakPeriodeDto(LocalDate.now().minusDays(10), LocalDate.now(), true);
        bekreftetUttakPeriodeDto2.setOrginalFom(LocalDate.now().minusDays(10));
        bekreftetUttakPeriodeDto2.setOrginalTom(LocalDate.now());
        bekreftetUttakPeriodeDto2.setOriginalResultat(UttakPeriodeVurderingType.PERIODE_OK);
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto3 = getBekreftetUttakPeriodeDto(LocalDate.now().plusDays(1), LocalDate.now().plusDays(10), false);
        dto.setBekreftedePerioder(Arrays.asList(bekreftetUttakPeriodeDto1, bekreftetUttakPeriodeDto2, bekreftetUttakPeriodeDto3));
        return dto;
    }

    public static ManuellAvklarFaktaUttakDto opprettDtoManuellAvklarFaktaUttakDto() {
        ManuellAvklarFaktaUttakDto dto = new ManuellAvklarFaktaUttakDto();
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto1 = getBekreftetUttakPeriodeDto(LocalDate.now().minusDays(20), LocalDate.now().minusDays(11), true);
        bekreftetUttakPeriodeDto1.setOrginalFom(LocalDate.now().minusDays(20));
        bekreftetUttakPeriodeDto1.setOrginalTom(LocalDate.now().minusDays(11));
        bekreftetUttakPeriodeDto1.setOriginalResultat(UttakPeriodeVurderingType.PERIODE_OK);
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto2 = getBekreftetUttakPeriodeDto(LocalDate.now().minusDays(10), LocalDate.now(), true);
        bekreftetUttakPeriodeDto2.setOrginalFom(LocalDate.now().minusDays(10));
        bekreftetUttakPeriodeDto2.setOrginalTom(LocalDate.now());
        bekreftetUttakPeriodeDto2.setOriginalResultat(UttakPeriodeVurderingType.PERIODE_OK);
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto3 = getBekreftetUttakPeriodeDto(LocalDate.now().plusDays(1), LocalDate.now().plusDays(10), false);
        dto.setBekreftedePerioder(Arrays.asList(bekreftetUttakPeriodeDto1, bekreftetUttakPeriodeDto2, bekreftetUttakPeriodeDto3));
        return dto;
    }

    public static Behandling opprettBehandling(ScenarioMorSøkerForeldrepenger scenario) {

        Behandling behandling = scenario.getBehandling();

        final OppgittPeriode periode_1 = OppgittPeriodeBuilder.ny()
            .medPeriode(LocalDate.now().minusDays(10), LocalDate.now())
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .build();
        final OppgittPeriode periode_2 = OppgittPeriodeBuilder.ny()
            .medPeriode(LocalDate.now().minusDays(20), LocalDate.now().minusDays(11))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .build();
        fordelingRepository.lagre(behandling, new OppgittFordelingEntitet(Arrays.asList(periode_1, periode_2), true));
        return behandling;
    }

    public static ScenarioMorSøkerForeldrepenger opprettScenarioMorSøkerForeldrepenger() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medSøknad();
        OppgittRettighetEntitet rettighet = new OppgittRettighetEntitet(false, false, true);
        scenario.medOppgittRettighet(rettighet);
        return scenario;
    }

    private static BekreftetUttakPeriodeDto getBekreftetUttakPeriodeDto(LocalDate fom, LocalDate tom, boolean bekreftet) {
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto = new BekreftetUttakPeriodeDto();
        OppgittPeriode bekreftetperiode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .build();
        if(bekreftet) {
            bekreftetUttakPeriodeDto.setBekreftetPeriode(new KontrollerFaktaPeriodeDto(KontrollerFaktaPeriode.automatiskBekreftet(bekreftetperiode)));
        } else {
            bekreftetUttakPeriodeDto.setBekreftetPeriode(new KontrollerFaktaPeriodeDto(KontrollerFaktaPeriode.ubekreftet(bekreftetperiode)));
        }
        return bekreftetUttakPeriodeDto;
    }
}
