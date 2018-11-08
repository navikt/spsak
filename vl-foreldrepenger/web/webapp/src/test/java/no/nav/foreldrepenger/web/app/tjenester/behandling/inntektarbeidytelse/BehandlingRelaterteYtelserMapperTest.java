package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse;

import static no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse.BehandlingRelaterteYtelserMapper.RELATERT_YTELSE_TYPER_FOR_ANNEN_FORELDER;
import static no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse.BehandlingRelaterteYtelserMapper.RELATERT_YTELSE_TYPER_FOR_SØKER;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class BehandlingRelaterteYtelserMapperTest {
    private static final LocalDate I_DAG = LocalDate.now();
    private static final Saksnummer SAKSNUMMER_42 = new Saksnummer("42");
    private NavBruker navBruker = new NavBrukerBuilder().medAktørId(new AktørId("99")).build();
    private Fagsak fagsakFødsel = Fagsak.opprettNy(FagsakYtelseType.ENGANGSTØNAD, navBruker, null, new Saksnummer("66"));

    @Test
    public void skal_returnerer_tilgrensende_ytelser_for_soker() {
        List<Ytelse> ytelser = Arrays.asList(
            opprettBuilderForBehandlingRelaterteYtelser(RelatertYtelseType.SYKEPENGER, RelatertYtelseTilstand.AVSLUTTET, I_DAG.minusDays(365), I_DAG.plusDays(360)),
            opprettBuilderForBehandlingRelaterteYtelser(RelatertYtelseType.FORELDREPENGER, RelatertYtelseTilstand.AVSLUTTET, I_DAG.minusDays(265), I_DAG.plusDays(200)),
            opprettBuilderForBehandlingRelaterteYtelser(RelatertYtelseType.FORELDREPENGER, RelatertYtelseTilstand.ÅPEN, I_DAG.minusDays(5), null));

        List<TilgrensendeYtelserDto> resultatListe = BehandlingRelaterteYtelserMapper.mapFraBehandlingRelaterteYtelser(ytelser);

        assertThat(resultatListe).hasSize(3);
        assertThat(resultatListe.get(0).getRelatertYtelseType()).isEqualTo(RelatertYtelseType.SYKEPENGER.getKode());
        assertThat(resultatListe.get(0).getPeriodeFraDato()).isEqualTo(I_DAG.minusDays(365));
        assertThat(resultatListe.get(1).getRelatertYtelseType()).isEqualTo(RelatertYtelseType.FORELDREPENGER.getKode());
        assertThat(resultatListe.get(1).getPeriodeTilDato()).isEqualTo(I_DAG.plusDays(200));
    }

    @Test
    public void skal_returnerer_tilgrensende_ytelser_for_annen_forelder() {
        List<Ytelse> ytelser = Arrays.asList(
            opprettBuilderForBehandlingRelaterteYtelser(RelatertYtelseType.SYKEPENGER, RelatertYtelseTilstand.AVSLUTTET, I_DAG.minusDays(365), I_DAG.plusDays(360)),
            opprettBuilderForBehandlingRelaterteYtelser(RelatertYtelseType.FORELDREPENGER, RelatertYtelseTilstand.AVSLUTTET, I_DAG.minusDays(265), I_DAG.plusDays(200)),
            opprettBuilderForBehandlingRelaterteYtelser(RelatertYtelseType.FORELDREPENGER, RelatertYtelseTilstand.LØPENDE, I_DAG.minusDays(5), null),
            opprettBuilderForBehandlingRelaterteYtelser(RelatertYtelseType.ENSLIG_FORSØRGER, RelatertYtelseTilstand.ÅPEN, I_DAG.minusDays(5), null));

        List<TilgrensendeYtelserDto> resultatListe = BehandlingRelaterteYtelserMapper.mapFraBehandlingRelaterteYtelser(ytelser);

        assertThat(resultatListe).hasSize(4);
        assertThat(resultatListe.get(0).getRelatertYtelseType()).isEqualTo(RelatertYtelseType.SYKEPENGER.getKode());
        assertThat(resultatListe.get(0).getStatus()).isEqualTo(RelatertYtelseTilstand.AVSLUTTET.getKode());
        assertThat(resultatListe.get(1).getRelatertYtelseType()).isEqualTo(RelatertYtelseType.FORELDREPENGER.getKode());
        assertThat(resultatListe.get(1).getStatus()).isEqualTo(RelatertYtelseTilstand.AVSLUTTET.getKode());
    }

    @Test
    public void skal_mapper_fagsak_til_tilgrensendeYtelserdto() {
        fagsakFødsel.setAvsluttet();

        TilgrensendeYtelserDto tilgrensendeYtelserDto = BehandlingRelaterteYtelserMapper.mapFraFagsak(fagsakFødsel, I_DAG.minusDays(5));

        assertThat(tilgrensendeYtelserDto.getRelatertYtelseType()).isEqualTo(RelatertYtelseType.ENGANGSSTØNAD.getKode());
        assertThat(tilgrensendeYtelserDto.getStatus()).isEqualTo(FagsakStatus.AVSLUTTET.getKode());
        assertThat(tilgrensendeYtelserDto.getPeriodeTilDato()).isEqualTo(I_DAG.minusDays(5));
        assertThat(tilgrensendeYtelserDto.getPeriodeFraDato()).isEqualTo(I_DAG.minusDays(5));
        assertThat(tilgrensendeYtelserDto.getSaksNummer()).isEqualTo("66");
    }

    @Test
    public void skal_returnerer_6_tom_tilgrensende_ytelser_for_soker() {
        @SuppressWarnings("unchecked")
        List<RelaterteYtelserDto> resultatListe = BehandlingRelaterteYtelserMapper.samleYtelserBasertPåYtelseType(Collections.EMPTY_LIST, RELATERT_YTELSE_TYPER_FOR_SØKER);

        assertThat(resultatListe.size()).isEqualTo(6);
        IntStream.range(0, RELATERT_YTELSE_TYPER_FOR_SØKER.size()).forEach(i -> {
            assertThat(resultatListe.get(i).getRelatertYtelseType()).isEqualTo(RELATERT_YTELSE_TYPER_FOR_SØKER.get(i).getKode());
            assertThat(resultatListe.get(i).getTilgrensendeYtelserListe()).isEmpty();
        });
    }

    @Test
    public void skal_returnerer_2_tom_tilgrensende_ytelser_for_soker() {
        @SuppressWarnings("unchecked")
        List<RelaterteYtelserDto> resultatListe = BehandlingRelaterteYtelserMapper.samleYtelserBasertPåYtelseType(Collections.EMPTY_LIST, RELATERT_YTELSE_TYPER_FOR_ANNEN_FORELDER);

        assertThat(resultatListe.size()).isEqualTo(2);
        IntStream.range(0, RELATERT_YTELSE_TYPER_FOR_ANNEN_FORELDER.size()).forEach(i -> {
            assertThat(resultatListe.get(i).getRelatertYtelseType()).isEqualTo(RELATERT_YTELSE_TYPER_FOR_SØKER.get(i).getKode());
            assertThat(resultatListe.get(i).getTilgrensendeYtelserListe()).isEmpty();
        });
    }

    @Test
    public void skal_returnerer_sortert_tilgrensende_ytelser_for_soker() {
        List<TilgrensendeYtelserDto> tilgrensendeYtelserDtos = Arrays.asList(
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.FORELDREPENGER, RelatertYtelseTilstand.AVSLUTTET, I_DAG.minusDays(365), I_DAG.plusDays(360)),
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.SYKEPENGER, RelatertYtelseTilstand.AVSLUTTET, I_DAG.minusDays(365), I_DAG.plusDays(360)),
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.SYKEPENGER, RelatertYtelseTilstand.LØPENDE, I_DAG.minusDays(265), I_DAG.plusDays(260)),
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.SYKEPENGER, RelatertYtelseTilstand.LØPENDE, null, null),
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.SYKEPENGER, RelatertYtelseTilstand.AVSLUTTET, I_DAG.minusDays(165), I_DAG.plusDays(160)),
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.SYKEPENGER, RelatertYtelseTilstand.LØPENDE, I_DAG.minusDays(65), I_DAG.plusDays(60)),
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.SYKEPENGER, RelatertYtelseTilstand.ÅPEN, I_DAG.minusDays(5), null),
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.ARBEIDSAVKLARINGSPENGER, RelatertYtelseTilstand.AVSLUTTET, I_DAG.minusDays(500), I_DAG.plusDays(400)));

        List<RelaterteYtelserDto> resultatListe = BehandlingRelaterteYtelserMapper.samleYtelserBasertPåYtelseType(tilgrensendeYtelserDtos, RELATERT_YTELSE_TYPER_FOR_SØKER);

        assertThat(resultatListe.size()).isEqualTo(6);
        assertThat(resultatListe.get(0).getRelatertYtelseType()).isEqualTo(RelatertYtelseType.FORELDREPENGER.getKode());
        assertThat(resultatListe.get(0).getTilgrensendeYtelserListe()).hasSize(1);
        assertThat(resultatListe.get(2).getRelatertYtelseType()).isEqualTo(RelatertYtelseType.SYKEPENGER.getKode());
        final List<TilgrensendeYtelserDto> sykepengerYtelserListe = resultatListe.get(2).getTilgrensendeYtelserListe();
        assertThat(sykepengerYtelserListe.size()).isEqualTo(6);
        assertThat(sykepengerYtelserListe.get(0).getPeriodeFraDato()).isNull();
        assertThat(sykepengerYtelserListe.get(1).getPeriodeFraDato()).isEqualTo(I_DAG.minusDays(5));
        assertThat(sykepengerYtelserListe.get(2).getPeriodeFraDato()).isEqualTo(I_DAG.minusDays(65));
        assertThat(sykepengerYtelserListe.get(3).getPeriodeFraDato()).isEqualTo(I_DAG.minusDays(165));
        assertThat(sykepengerYtelserListe.get(4).getPeriodeFraDato()).isEqualTo(I_DAG.minusDays(265));
        assertThat(sykepengerYtelserListe.get(5).getPeriodeFraDato()).isEqualTo(I_DAG.minusDays(365));
        assertThat(resultatListe.get(5).getRelatertYtelseType()).isEqualTo(RelatertYtelseType.ARBEIDSAVKLARINGSPENGER.getKode());
        assertThat(resultatListe.get(5).getTilgrensendeYtelserListe()).hasSize(1);
    }

    @Test
    public void skal_returnerer_sortert_tilgrensende_ytelser_for_annen_forelder() {
        List<TilgrensendeYtelserDto> tilgrensendeYtelserDtos = Arrays.asList(
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.SYKEPENGER, RelatertYtelseTilstand.AVSLUTTET, I_DAG.minusDays(365), I_DAG.plusDays(360)),
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.FORELDREPENGER, RelatertYtelseTilstand.AVSLUTTET, I_DAG.minusDays(365), I_DAG.plusDays(360)),
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.FORELDREPENGER, RelatertYtelseTilstand.AVSLUTTET, I_DAG.minusDays(165), I_DAG.plusDays(160)),
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.FORELDREPENGER, RelatertYtelseTilstand.ÅPEN, I_DAG.minusDays(5), null),
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.FORELDREPENGER, RelatertYtelseTilstand.ÅPEN, I_DAG.plusMonths(5), null),
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.FORELDREPENGER, RelatertYtelseTilstand.ÅPEN, null, null),
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.FORELDREPENGER, RelatertYtelseTilstand.ÅPEN, I_DAG, null),
            opprettTilgrensendeYtelserDto(SAKSNUMMER_42, RelatertYtelseType.ENGANGSSTØNAD, RelatertYtelseTilstand.AVSLUTTET, I_DAG.minusDays(500), I_DAG.plusDays(400)));

        List<RelaterteYtelserDto> resultatListe = BehandlingRelaterteYtelserMapper.samleYtelserBasertPåYtelseType(tilgrensendeYtelserDtos, RELATERT_YTELSE_TYPER_FOR_ANNEN_FORELDER);

        assertThat(resultatListe.size()).isEqualTo(2);
        assertThat(resultatListe.get(0).getRelatertYtelseType()).isEqualTo(RelatertYtelseType.FORELDREPENGER.getKode());
        final List<TilgrensendeYtelserDto> foreldrepengerYtelserListe = resultatListe.get(0).getTilgrensendeYtelserListe();
        assertThat(foreldrepengerYtelserListe.size()).isEqualTo(6);
        assertThat(foreldrepengerYtelserListe.get(0).getPeriodeFraDato()).isNull();
        assertThat(foreldrepengerYtelserListe.get(1).getPeriodeFraDato()).isEqualTo(I_DAG.plusMonths(5));
        assertThat(foreldrepengerYtelserListe.get(2).getPeriodeFraDato()).isEqualTo(I_DAG);
        assertThat(foreldrepengerYtelserListe.get(3).getPeriodeFraDato()).isEqualTo(I_DAG.minusDays(5));
        assertThat(foreldrepengerYtelserListe.get(4).getPeriodeFraDato()).isEqualTo(I_DAG.minusDays(165));
        assertThat(foreldrepengerYtelserListe.get(5).getPeriodeFraDato()).isEqualTo(I_DAG.minusDays(365));
        assertThat(resultatListe.get(1).getRelatertYtelseType()).isEqualTo(RelatertYtelseType.ENGANGSSTØNAD.getKode());
        assertThat(resultatListe.get(1).getTilgrensendeYtelserListe()).hasSize(1);
    }

    private Ytelse opprettBuilderForBehandlingRelaterteYtelser(RelatertYtelseType ytelseType,
                                                                                  RelatertYtelseTilstand ytelseTilstand,
                                                                                  LocalDate iverksettelsesDato,
                                                                                  LocalDate opphoerFomDato) {

        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder ytelserBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(new AktørId("1"));
        YtelseBuilder ytelse = ytelserBuilder.getYtelselseBuilderForType(Fagsystem.FPSAK, ytelseType, new Saksnummer("22"))
            .medYtelseType(ytelseType)
            .medBehandlingsTema(TemaUnderkategori.UDEFINERT)
            .medStatus(ytelseTilstand)
            .medPeriode(opphoerFomDato == null
                ? DatoIntervallEntitet.fraOgMed(iverksettelsesDato)
                : DatoIntervallEntitet.fraOgMedTilOgMed(iverksettelsesDato, opphoerFomDato))
            ;

        ytelserBuilder.leggTilYtelse(ytelse);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(ytelserBuilder);

        InntektArbeidYtelseAggregat build = inntektArbeidYtelseAggregatBuilder.build();
        return build.getAktørYtelse().iterator().next().getYtelser().iterator().next();
    }

    private TilgrensendeYtelserDto opprettTilgrensendeYtelserDto(Saksnummer saksnummer,
                                                                 RelatertYtelseType ytelseType,
                                                                 RelatertYtelseTilstand ytelseTilstand,
                                                                 LocalDate iverksettelsesDato,
                                                                 LocalDate opphoerFomDato) {
        TilgrensendeYtelserDto tilgrensendeYtelserDto = new TilgrensendeYtelserDto();
        tilgrensendeYtelserDto.setRelatertYtelseType(ytelseType.getKode());
        tilgrensendeYtelserDto.setStatus(ytelseTilstand.getKode());
        tilgrensendeYtelserDto.setSaksNummer(saksnummer);
        tilgrensendeYtelserDto.setPeriodeFraDato(iverksettelsesDato);
        tilgrensendeYtelserDto.setPeriodeTilDato(opphoerFomDato);
        return tilgrensendeYtelserDto;
    }
}
