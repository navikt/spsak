package no.nav.foreldrepenger.web.app.tjenester.hendelser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.hendelser.feilhåndtering.HendelsemottakRepository;
import no.nav.foreldrepenger.behandlingslager.hendelser.feilhåndtering.HendelsemottakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.hendelser.sortering.HendelseSorteringRepository;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.hendelser.HendelseSorteringTjeneste;
import no.nav.foreldrepenger.domene.mottak.hendelser.KlargjørHendelseTask;
import no.nav.foreldrepenger.domene.mottak.hendelser.MottattHendelseTjeneste;
import no.nav.foreldrepenger.domene.mottak.hendelser.impl.HendelseSorteringTjenesteImpl;
import no.nav.foreldrepenger.domene.mottak.hendelser.impl.MottattHendelseTjenesteImpl;
import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.FødselHendelse;
import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.YtelseHendelse;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.abonnent.AktørIdDto;
import no.nav.foreldrepenger.kontrakter.abonnent.HendelseWrapperDto;
import no.nav.foreldrepenger.kontrakter.abonnent.infotrygd.InfotrygdHendelseDto;
import no.nav.foreldrepenger.kontrakter.abonnent.infotrygd.InfotrygdHendelseDtoBuilder;
import no.nav.foreldrepenger.kontrakter.abonnent.tps.FødselHendelseDto;
import no.nav.vedtak.felles.integrasjon.rest.JsonMapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

public class HendelserRestTjenesteTest {

    public static final String HENDELSE_ID = "1337";

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    HendelsemottakRepository hendelsemottakRepository = new HendelsemottakRepositoryImpl(repoRule.getEntityManager());
    ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), null);
    HendelseSorteringRepository sorteringRepository = mock(HendelseSorteringRepository.class);
    HendelseSorteringTjeneste sorteringTjeneste = new HendelseSorteringTjenesteImpl(sorteringRepository);
    MottattHendelseTjeneste tjeneste = new MottattHendelseTjenesteImpl(hendelsemottakRepository, prosessTaskRepository);
    HendelserRestTjeneste restTjeneste = new HendelserRestTjeneste(tjeneste, sorteringTjeneste);

    @Test
    public void skal_ta_imot_fødselshendelse_og_opprette_prosesstask() throws Exception {
        // act
        List<AktørId> aktørIdForeldre = Arrays.asList(new AktørId("900000001"), new AktørId("900000002"));
        LocalDate fødselsdato = LocalDate.now();
        HendelseWrapperDto hendelse = HendelseWrapperDto.lagDto(lagFødselHendelse(aktørIdForeldre, fødselsdato));
        restTjeneste.mottaHendelse(hendelse);
        repoRule.getEntityManager().flush();

        // assert
        assertThat(hendelsemottakRepository.hendelseErNy(HENDELSE_ID)).isFalse();
        List<ProsessTaskData> tasks = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(tasks).hasSize(1);
        ProsessTaskData task = tasks.get(0);
        assertThat(task.getTaskType()).isEqualTo(KlargjørHendelseTask.TASKNAME);
        assertThat(task.getPayloadAsString()).isEqualTo(JsonMapper.toJson(new FødselHendelse(aktørIdForeldre.stream().map(AktørId::getId).collect(Collectors.toList()), fødselsdato)));
        assertThat(task.getPropertyValue(KlargjørHendelseTask.PROPERTY_UID)).isEqualTo(HENDELSE_ID);
        assertThat(task.getPropertyValue(KlargjørHendelseTask.PROPERTY_HENDELSE_TYPE)).isEqualTo("FØDSEL");
    }

    @Test
    public void skal_ta_imot_infotrygdhendelse_og_opprette_prosesstask() throws Exception {
        LocalDate fom = LocalDate.now();
        String identDato = "2018-10-10";
        String aktørId = "900000001";
        String typeYtelse = "ab";
        String hendelseKode = InfotrygdHendelseDto.Hendelsetype.YTELSE_ENDRET.name();
        InfotrygdHendelseDto infotrygdDto = lagInfotrygdHendelse(InfotrygdHendelseDto.Hendelsetype.YTELSE_ENDRET,
            aktørId,
            typeYtelse,
            fom,
            identDato);
        HendelseWrapperDto hendelse = HendelseWrapperDto.lagDto(infotrygdDto);

        restTjeneste.mottaHendelse(hendelse);
        repoRule.getEntityManager().flush();

        assertThat(hendelsemottakRepository.hendelseErNy(HENDELSE_ID)).isFalse();
        List<ProsessTaskData> tasks = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(tasks).hasSize(1);
        ProsessTaskData task = tasks.get(0);
        assertThat(task.getTaskType()).isEqualTo(KlargjørHendelseTask.TASKNAME);
        assertThat(task.getPayloadAsString()).isEqualTo(JsonMapper.toJson(new YtelseHendelse(hendelseKode, typeYtelse, aktørId, fom, identDato)));
        assertThat(task.getPropertyValue(KlargjørHendelseTask.PROPERTY_UID)).isEqualTo(HENDELSE_ID);
        assertThat(task.getPropertyValue(KlargjørHendelseTask.PROPERTY_HENDELSE_TYPE)).isEqualTo(hendelseKode);
    }

    @Test
    public void skal_ikke_opprette_prosess_task_når_hendelse_med_samme_uid_tidligere_er_mottatt() throws Exception {
        hendelsemottakRepository.registrerMottattHendelse(HENDELSE_ID);

        // act
        List<AktørId> aktørIdForeldre = Arrays.asList(new AktørId("900000001"), new AktørId("900000002"));
        LocalDate fødselsdato = LocalDate.now();
        HendelseWrapperDto hendelse = HendelseWrapperDto.lagDto(lagFødselHendelse(aktørIdForeldre, fødselsdato));
        restTjeneste.mottaHendelse(hendelse);
        repoRule.getEntityManager().flush();

        // assert
        List<ProsessTaskData> tasks = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(tasks).isEmpty();
    }

    @Test
    public void skal_returnere_tom_liste_når_aktørId_ikke_er_registrert_eller_mangler_sak() {
        when(sorteringRepository.hentEksisterendeAktørIderMedSak(anyList())).thenReturn(Collections.emptyList());

        List<String> resultat = restTjeneste.grovSorter(Arrays.asList(new AktørIdDto("3512880731200")));
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_returnere_liste_med_4_aktørIder_som_har_sak() {
        List<AktørId> harSak = new ArrayList<>(Arrays.asList(
            new AktørId("3512880731200"),
            new AktørId("4512880731201"),
            new AktørId("5512880731202"),
            new AktørId("6512880731203")));

        when(sorteringRepository.hentEksisterendeAktørIderMedSak(anyList())).thenReturn(harSak);

        List<AktørIdDto> sorter = new ArrayList<>();
        sorter.add(new AktørIdDto("3512880731200"));
        sorter.add(new AktørIdDto("4512880731201"));
        sorter.add(new AktørIdDto("5512880731202"));
        sorter.add(new AktørIdDto("6512880731203"));
        sorter.add(new AktørIdDto("7512880731204"));
        sorter.add(new AktørIdDto("8512880731205"));
        sorter.add(new AktørIdDto("9512880731206"));

        List<String> resultat = restTjeneste.grovSorter(sorter);

        assertThat(resultat).hasSameSizeAs(harSak);
        assertThat(resultat).isEqualTo(harSak.stream().map(AktørId::getId).collect(Collectors.toList()));
    }

    private FødselHendelseDto lagFødselHendelse(List<AktørId> aktørIdForeldre, LocalDate fødselsdato) {
        FødselHendelseDto hendelse = new FødselHendelseDto();
        hendelse.setId(HENDELSE_ID);
        hendelse.setAktørIdForeldre(aktørIdForeldre.stream().map(AktørId::getId).collect(Collectors.toList()));
        hendelse.setFødselsdato(fødselsdato);
        return hendelse;
    }

    private InfotrygdHendelseDto lagInfotrygdHendelse(InfotrygdHendelseDto.Hendelsetype hendelsetype,
                                                      String aktørId,
                                                      String typeYtelse,
                                                      LocalDate fom,
                                                      String identDato) {
        InfotrygdHendelseDtoBuilder builder;
        switch (hendelsetype) {
            case YTELSE_OPPHØRT:
                builder = InfotrygdHendelseDtoBuilder.opphørt();
                break;
            case YTELSE_ENDRET:
                builder = InfotrygdHendelseDtoBuilder.endring();
                break;
            case YTELSE_ANNULERT:
                builder = InfotrygdHendelseDtoBuilder.annulert();
                break;
            case YTELSE_INNVILGET:
                builder = InfotrygdHendelseDtoBuilder.innvilget();
                break;
            default:
                throw new IllegalArgumentException("ugyldig hendelsetype");
        }
        return builder.medUnikId(HENDELSE_ID)
            .medAktørId(aktørId)
            .medFraOgMed(fom)
            .medIdentdato(identDato)
            .medTypeYtelse(typeYtelse)
            .build();
    }
}
