package no.nav.foreldrepenger.dokumentbestiller.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.InnhenteOpplysningerDokument.FLETTEFELT_SOKERS_NAVN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.DokumentMapperTjenesteProvider;
import no.nav.foreldrepenger.dokumentbestiller.DokumentMapperTjenesteProviderImpl;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametereImpl;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.InnhenteOpplysningerDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokumentBehandlingsresultatMapper;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokumentTypeDtoMapper;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.uttak.InfoOmResterendeDagerTjeneste;
import no.nav.foreldrepenger.domene.uttak.OpphørFPTjeneste;
import no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker.BeregnEkstraFlerbarnsukerTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.PersonstatusKode;
import no.nav.vedtak.exception.TekniskException;

public class InnhenteOpplysningerDokumentTest {

    @Test
    public void oppretterRiktigAntallFlettefelterPåEngangsstønad() {
        AbstractTestScenario<?> scenario = engangsstønadScenario();
        InnhenteOpplysningerDokument dokument = dokumentBuilder().build(scenario.getBehandling());

        assertThat(dokument.getFlettefelter(dtoForScenario(scenario)).size()).isEqualTo(7);
    }

    @Test
    public void oppretterMottattDatoFlettefeltPåEngangsstønad() {
        LocalDate mottattDato = LocalDate.now().minusWeeks(1);
        AbstractTestScenario<?> scenario = engangsstønadScenario(mottattDato);
        InnhenteOpplysningerDokument dokument = dokumentBuilder().build(scenario.getBehandling());

        Optional<Flettefelt> mottattDatoFlettefeltOpt = hentFlettefeltFraDokument(dokument, Flettefelt.SØKNAD_DATO, dtoForScenario(scenario));

        assertThat(mottattDatoFlettefeltOpt).hasValueSatisfying(mottattDatoFlettefelt ->
            assertThat(mottattDatoFlettefelt.getFeltverdi()).isEqualTo(mottattDato.toString())
        );
    }

    private DokumentTypeDto dtoForScenario(AbstractTestScenario<?> scenario) {
        DokumentTypeDtoMapper mapper = mapperForScenario(scenario);
        return mapper.mapToDto(scenario.getBehandling(), "navn", PersonstatusKode.DOD.value());
    }

    @Test
    public void oppretterFristDatoFlettefeltPåEngangsstønad() {
        int svarfristUker = 4;
        AbstractTestScenario<?> scenario = engangsstønadScenario();
        InnhenteOpplysningerDokument dokument = dokumentBuilder().medSvarfrist(Period.ofWeeks(svarfristUker)).build(scenario.getBehandling());

        Optional<Flettefelt> fristDatoFlettefeltOpt = hentFlettefeltFraDokument(dokument, Flettefelt.FRIST_DATO, dtoForScenario(scenario));

        assertThat(fristDatoFlettefeltOpt).hasValueSatisfying(fristDatoFlettefelt ->
            assertThat(fristDatoFlettefelt.getFeltverdi()).isEqualTo(LocalDate.now().plusWeeks(svarfristUker).toString())
        );
    }

    @Test
    public void oppretterFritekstFlettefeltPåEngangsstønad() {
        AbstractTestScenario<?> scenario = engangsstønadScenario();
        String fritekst = "dette er en fritekst";
        InnhenteOpplysningerDokument dokument = dokumentBuilder().medFritekst(fritekst).build(scenario.getBehandling());

        Optional<Flettefelt> fritekstFlettefeltOpt = hentFlettefeltFraDokument(dokument, Flettefelt.FRITEKST, dtoForScenario(scenario));

        assertThat(fritekstFlettefeltOpt).hasValueSatisfying(fritekstFlettefelt ->
            assertThat(fritekstFlettefelt.getFeltverdi()).isEqualTo(fritekst)
        );
    }

    @Test
    public void oppretterRiktigAntallFlettefelterPåForeldrepenger() {
        AbstractTestScenario<?> scenario = morSøkerForeldrepengerScenario();
        InnhenteOpplysningerDokument dokument = dokumentBuilder().build(scenario.getBehandling());

        assertThat(dokument.getFlettefelter(dtoForScenario(scenario)).size()).isEqualTo(7);
    }

    @Test
    public void oppretterYtelseTypeFlettefeltPåForeldrepenger() {
        AbstractTestScenario<?> scenario = morSøkerForeldrepengerScenario();
        InnhenteOpplysningerDokument dokument = dokumentBuilder().build(scenario.getBehandling());

        Optional<Flettefelt> ytelseTypeFlettefeltOpt = hentFlettefeltFraDokument(dokument, Flettefelt.YTELSE_TYPE, dtoForScenario(scenario));

        assertThat(ytelseTypeFlettefeltOpt).hasValueSatisfying(ytelseTypeFlettefelt ->
            assertThat(ytelseTypeFlettefelt.getFeltverdi()).isEqualTo(scenario.getFagsak().getYtelseType().getKode())
        );
    }

    @Test
    public void oppretterSøkersNavnFlettefeltPåForeldrepenger() {
        AbstractTestScenario<?> scenario = morSøkerForeldrepengerScenario();
        InnhenteOpplysningerDokument dokument = dokumentBuilder().build(scenario.getBehandling());

        DokumentTypeDtoMapper mapper = mapperForScenario(scenario);
        String navn = "Per Navnesen";
        DokumentTypeDto dto = mapper.mapToDto(scenario.getBehandling(), navn, PersonstatusKode.DOD.value());

        Optional<Flettefelt> søkersNavnFletteFeltOpt = hentFlettefeltFraDokument(dokument, FLETTEFELT_SOKERS_NAVN, dto);

        assertThat(søkersNavnFletteFeltOpt).hasValueSatisfying(søkersNavnFletteFelt ->
            assertThat(søkersNavnFletteFelt.getFeltverdi()).isEqualTo(navn)
        );
    }

    @Test
    public void oppretterPersonStatusFlettefeltPåForeldrepenger() {
        AbstractTestScenario<?> scenario = morSøkerForeldrepengerScenario();
        InnhenteOpplysningerDokument dokument = dokumentBuilder().build(scenario.getBehandling());

        DokumentTypeDtoMapper mapper = mapperForScenario(scenario);
        String personstatus = PersonstatusKode.ANNET.value();
        DokumentTypeDto dto = mapper.mapToDto(scenario.getBehandling(), "navn", personstatus);

        Optional<Flettefelt> personStatusFletteFeltOpt = hentFlettefeltFraDokument(dokument, Flettefelt.PERSON_STATUS, dto);

        assertThat(personStatusFletteFeltOpt).hasValueSatisfying(personStatusFletteFelt ->
            assertThat(personStatusFletteFelt.getFeltverdi()).isEqualTo(personstatus)
        );
    }

    @Test
    public void skalGodtaKlageSomGyldigBehandlingstype() {
        assertThatCode(() -> new InnhenteOpplysningerDokument(null, "", BehandlingType.KLAGE)).doesNotThrowAnyException();
    }

    @Test
    public void skalIkkeGodtaInnsynSomGyldigBehandlingstype() {
        assertThatThrownBy(() -> new InnhenteOpplysningerDokument(null, "", BehandlingType.INNSYN)).isInstanceOf(TekniskException.class);
    }

    @Test
    public void skalGodtaRevurderingSomGyldigBehandlingstype() {
        assertThatCode(() -> new InnhenteOpplysningerDokument(null, "", BehandlingType.REVURDERING)).doesNotThrowAnyException();
    }

    @Test
    public void skalGodtaFørstegangssøknadSomGyldigBehandlingstype() {
        assertThatCode(() -> new InnhenteOpplysningerDokument(null, "", BehandlingType.FØRSTEGANGSSØKNAD)).doesNotThrowAnyException();
    }

    private ScenarioMorSøkerForeldrepenger morSøkerForeldrepengerScenario() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.lagMocked();
        return scenario;
    }

    private ScenarioMorSøkerEngangsstønad engangsstønadScenario() {
        return engangsstønadScenario(LocalDate.now().minusWeeks(2));
    }

    private ScenarioMorSøkerEngangsstønad engangsstønadScenario(LocalDate mottattDato) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel().medFødselAdopsjonsdato(Collections.singletonList(mottattDato));
        scenario.medSøknad().medMottattDato(mottattDato);
        scenario.lagMocked();
        return scenario;
    }

    private Optional<Flettefelt> hentFlettefeltFraDokument(InnhenteOpplysningerDokument dokument,
                                                           String flettefelt,
                                                           DokumentTypeDto dto) {
        return dokument.getFlettefelter(dto).stream().filter(f -> flettefelt.equals(f.getFeltnavn())).findFirst();
    }

    private InnhenteOpplysningerDokumentBuilder dokumentBuilder() {
        return new InnhenteOpplysningerDokumentBuilder();
    }

    private DokumentTypeDtoMapper mapperForScenario(AbstractTestScenario<?> scenario) {
        BrevParametereImpl brevParametere = new BrevParametereImpl(6, 3, Period.ofWeeks(3), Period.ofWeeks(2));

        final BehandlingRepositoryProvider provider = scenario.mockBehandlingRepositoryProvider();
        DokumentMapperTjenesteProvider tjenesteProvider = new DokumentMapperTjenesteProviderImpl(
            new SkjæringstidspunktTjenesteImpl(provider,
                new BeregnMorsMaksdatoTjenesteImpl(provider, new RelatertBehandlingTjenesteImpl(provider)),
                new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
                Period.of(0, 3, 0),
                Period.of(0, 10, 0)),
            mock(BasisPersonopplysningTjeneste.class),
            mock(FamilieHendelseTjeneste.class),
            null,
            mock(HentGrunnlagsdataTjeneste.class),
            mock(BeregnEkstraFlerbarnsukerTjeneste.class),
            mock(OpphørFPTjeneste.class),
            mock(InfoOmResterendeDagerTjeneste.class));

        DokumentBehandlingsresultatMapper behandlingsresultatMapper = new DokumentBehandlingsresultatMapper(provider, tjenesteProvider);

        return new DokumentTypeDtoMapper(provider,
            tjenesteProvider, brevParametere, behandlingsresultatMapper);
    }


    private static class InnhenteOpplysningerDokumentBuilder {
        private String fritekst = "fritekst";
        private Period svarfristUker = Period.ofWeeks(3);

        private InnhenteOpplysningerDokument build(Behandling behandling) {
            BrevParametereImpl brevParametere = new BrevParametereImpl(6, 3, svarfristUker, Period.ofWeeks(2));
            return new InnhenteOpplysningerDokument(brevParametere, fritekst, behandling.getType());
        }

        private InnhenteOpplysningerDokumentBuilder medSvarfrist(Period svarfristUker) {
            this.svarfristUker = svarfristUker;
            return this;
        }

        private InnhenteOpplysningerDokumentBuilder medFritekst(String fritekst) {
            this.fritekst = fritekst;
            return this;
        }
    }
}
