package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.vedtak.konfig.Tid;

public class BehandlingRelaterteYtelserMapper {
    public static final List<RelatertYtelseType> RELATERT_YTELSE_TYPER_FOR_SØKER = Collections.unmodifiableList(Arrays.asList(
        RelatertYtelseType.FORELDREPENGER,
        RelatertYtelseType.ENGANGSSTØNAD,
        RelatertYtelseType.SYKEPENGER,
        RelatertYtelseType.ENSLIG_FORSØRGER,
        RelatertYtelseType.DAGPENGER,
        RelatertYtelseType.ARBEIDSAVKLARINGSPENGER));

    public static final List<RelatertYtelseType> RELATERT_YTELSE_TYPER_FOR_ANNEN_FORELDER = Collections.unmodifiableList(Arrays.asList(
        RelatertYtelseType.FORELDREPENGER,
        RelatertYtelseType.ENGANGSSTØNAD));

    private BehandlingRelaterteYtelserMapper() {
    }

    public static List<TilgrensendeYtelserDto> mapFraBehandlingRelaterteYtelser(Collection<Ytelse> ytelser) {
        return ytelser.stream()
            .map(ytelse -> lagTilgrensendeYtelse(ytelse))
            .collect(Collectors.toList());
    }

    private static TilgrensendeYtelserDto lagTilgrensendeYtelse(Ytelse ytelse) {
        TilgrensendeYtelserDto tilgrensendeYtelserDto = new TilgrensendeYtelserDto();
        tilgrensendeYtelserDto.setRelatertYtelseType(ytelse.getRelatertYtelseType().getKode());
        tilgrensendeYtelserDto.setPeriodeFraDato(ytelse.getPeriode().getFomDato());
        tilgrensendeYtelserDto.setPeriodeTilDato(endreTomDatoHvisLøpende(ytelse.getPeriode().getTomDato()));
        tilgrensendeYtelserDto.setStatus(ytelse.getStatus().getKode());
        tilgrensendeYtelserDto.setSaksNummer(ytelse.getSaksnummer());
        return tilgrensendeYtelserDto;
    }

    public static TilgrensendeYtelserDto mapFraFagsak(Fagsak fagsak, LocalDate periodeDato) {
        TilgrensendeYtelserDto tilgrensendeYtelserDto = new TilgrensendeYtelserDto();
        if (FagsakYtelseType.FORELDREPENGER.equals(fagsak.getYtelseType())) {
            tilgrensendeYtelserDto.setRelatertYtelseType(RelatertYtelseType.FORELDREPENGER.getKode());
        }
        tilgrensendeYtelserDto.setStatus(fagsak.getStatus().getKode());
        tilgrensendeYtelserDto.setPeriodeFraDato(periodeDato);
        tilgrensendeYtelserDto.setPeriodeTilDato(endreTomDatoHvisLøpende(periodeDato));
        tilgrensendeYtelserDto.setSaksNummer(fagsak.getSaksnummer());
        return tilgrensendeYtelserDto;
    }

    private static LocalDate endreTomDatoHvisLøpende(LocalDate tomDato) {
        if (Tid.TIDENES_ENDE.equals(tomDato)) {
            return null;
        }
        return tomDato;
    }

    public static List<RelaterteYtelserDto> samleYtelserBasertPåYtelseType(List<TilgrensendeYtelserDto> tilgrensendeYtelser, List<RelatertYtelseType> ytelsesTyper) {
        List<RelaterteYtelserDto> relaterteYtelserDtos = new LinkedList<>();
        for (RelatertYtelseType relatertYtelseType : ytelsesTyper) {
            relaterteYtelserDtos.add(new RelaterteYtelserDto(relatertYtelseType.getKode(), sortTilgrensendeYtelser(tilgrensendeYtelser, relatertYtelseType.getKode())));
        }
        return relaterteYtelserDtos;
    }

    private static List<TilgrensendeYtelserDto> sortTilgrensendeYtelser(List<TilgrensendeYtelserDto> relatertYtelser, String relatertYtelseType) {
        return relatertYtelser.stream().filter(tilgrensendeYtelserDto -> (relatertYtelseType.equals(tilgrensendeYtelserDto.getRelatertYtelseType())))
            .sorted()
            .collect(Collectors.toList());
    }
}
