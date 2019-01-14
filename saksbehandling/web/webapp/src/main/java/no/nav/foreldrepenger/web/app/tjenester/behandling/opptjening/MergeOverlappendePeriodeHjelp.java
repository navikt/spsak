package no.nav.foreldrepenger.web.app.tjenester.behandling.opptjening;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetKlassifisering;
import no.nav.spsak.tidsserie.LocalDateInterval;
import no.nav.spsak.tidsserie.LocalDateSegment;
import no.nav.spsak.tidsserie.LocalDateTimeline;

public class MergeOverlappendePeriodeHjelp {
    private MergeOverlappendePeriodeHjelp() {
    }

    public static List<FastsattOpptjeningAktivitetDto> mergeOverlappenePerioder(List<OpptjeningAktivitet> opptjeningAktivitet) {
        LocalDateTimeline<OpptjeningAktivitetKlassifisering> godkjent = behandleBekrefetGodkjent(opptjeningAktivitet
            .stream()
            .filter(oa -> oa.getKlassifisering().equals(OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT))
            .collect(Collectors.toList()));
        LocalDateTimeline<OpptjeningAktivitetKlassifisering> godkjentOgMellomliggende = behandleMellomliggende_perioder(godkjent, opptjeningAktivitet
            .stream()
            .filter(oa -> oa.getKlassifisering().equals(OpptjeningAktivitetKlassifisering.MELLOMLIGGENDE_PERIODE))
            .collect(Collectors.toList()));

        LocalDateTimeline<OpptjeningAktivitetKlassifisering> resultat = behandleBekreftetAvvist(godkjentOgMellomliggende, opptjeningAktivitet
            .stream()
            .filter(oa -> oa.getKlassifisering().equals(OpptjeningAktivitetKlassifisering.BEKREFTET_AVVIST))
            .collect(Collectors.toList()));

        return lagDtoer(resultat);
    }

    private static List<FastsattOpptjeningAktivitetDto> lagDtoer(LocalDateTimeline<OpptjeningAktivitetKlassifisering> resultatInn) {
        if (resultatInn == null) {
            return Collections.emptyList();
        }
        List<FastsattOpptjeningAktivitetDto> resultat = new ArrayList<>();
        NavigableSet<LocalDateInterval> datoIntervaller = resultatInn.getDatoIntervaller();
        for (LocalDateInterval intervall : datoIntervaller) {
            LocalDateSegment<OpptjeningAktivitetKlassifisering> segment = resultatInn.getSegment(intervall);
            OpptjeningAktivitetKlassifisering klassifisering = segment.getValue();
            resultat.add(new FastsattOpptjeningAktivitetDto(intervall.getFomDato(), intervall.getTomDato(),
                klassifisering));
        }
        resultat.sort(Comparator.comparing(FastsattOpptjeningAktivitetDto::getFom));
        return resultat;
    }

    private static LocalDateTimeline<OpptjeningAktivitetKlassifisering> behandleBekrefetGodkjent(List<OpptjeningAktivitet> opptjeningAktivitet) {
        LocalDateTimeline<OpptjeningAktivitetKlassifisering> tidsserie = null;
        for (OpptjeningAktivitet aktivitet : opptjeningAktivitet) {
            if (tidsserie == null) {
                tidsserie = new LocalDateTimeline<>(aktivitet.getFom(), aktivitet.getTom(), OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
            } else {
                LocalDateTimeline<OpptjeningAktivitetKlassifisering> timeline = new LocalDateTimeline<>(aktivitet.getFom(), aktivitet.getTom(), OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
                tidsserie = tidsserie.combine(timeline, MergeOverlappendePeriodeHjelp::mergeGodkjente, LocalDateTimeline.JoinStyle.CROSS_JOIN);
            }
        }
        return tidsserie != null ? tidsserie.compress() : null;
    }

    private static LocalDateTimeline<OpptjeningAktivitetKlassifisering> behandleMellomliggende_perioder(
        LocalDateTimeline<OpptjeningAktivitetKlassifisering> tidsserie, List<OpptjeningAktivitet> opptjeningAktivitet) {
        for (OpptjeningAktivitet aktivitet : opptjeningAktivitet) {
            if (tidsserie == null) {
                tidsserie = new LocalDateTimeline<>(aktivitet.getFom(), aktivitet.getTom(), OpptjeningAktivitetKlassifisering.MELLOMLIGGENDE_PERIODE);
            } else {
                LocalDateTimeline<OpptjeningAktivitetKlassifisering> timeline = new LocalDateTimeline<>(aktivitet.getFom(), aktivitet.getTom(), OpptjeningAktivitetKlassifisering.MELLOMLIGGENDE_PERIODE);
                tidsserie = tidsserie.combine(timeline, MergeOverlappendePeriodeHjelp::mergeMellomliggende, LocalDateTimeline.JoinStyle.CROSS_JOIN);
            }
        }
        return tidsserie != null ? tidsserie.compress() : null;
    }

    private static LocalDateTimeline<OpptjeningAktivitetKlassifisering> behandleBekreftetAvvist(LocalDateTimeline<OpptjeningAktivitetKlassifisering> tidsserie, List<OpptjeningAktivitet> opptjeningAktivitet) {
        for (OpptjeningAktivitet aktivitet : opptjeningAktivitet) {
            if (tidsserie == null) {
                tidsserie = new LocalDateTimeline<>(aktivitet.getFom(), aktivitet.getTom(), OpptjeningAktivitetKlassifisering.BEKREFTET_AVVIST);
            } else {
                LocalDateTimeline<OpptjeningAktivitetKlassifisering> timeline = new LocalDateTimeline<>(aktivitet.getFom(), aktivitet.getTom(), OpptjeningAktivitetKlassifisering.BEKREFTET_AVVIST);
                tidsserie = tidsserie.combine(timeline, MergeOverlappendePeriodeHjelp::mergeBekreftAvvist, LocalDateTimeline.JoinStyle.CROSS_JOIN);
            }
        }
        return tidsserie != null ? tidsserie.compress() : null;
    }

    private static LocalDateSegment<OpptjeningAktivitetKlassifisering> mergeMellomliggende(LocalDateInterval di,
                                                                                           LocalDateSegment<OpptjeningAktivitetKlassifisering> lhs,
                                                                                           LocalDateSegment<OpptjeningAktivitetKlassifisering> rhs) {

        // legger inn perioden for mellomliggende
        if (lhs == null || lhs.getValue().equals(OpptjeningAktivitetKlassifisering.MELLOMLIGGENDE_PERIODE)) {
            return new LocalDateSegment<>(di, OpptjeningAktivitetKlassifisering.MELLOMLIGGENDE_PERIODE);
            // legger inn periode for bekreftet godkjent
        } else if (rhs == null) {
            return new LocalDateSegment<>(di, lhs.getValue());
        } else {
            return new LocalDateSegment<>(di, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        }
    }

    private static LocalDateSegment<OpptjeningAktivitetKlassifisering> mergeBekreftAvvist(LocalDateInterval di,
                                                                                          LocalDateSegment<OpptjeningAktivitetKlassifisering> lhs,
                                                                                          LocalDateSegment<OpptjeningAktivitetKlassifisering> rhs) {

        // legger inn perioden for bekreftet avvist
        if (lhs == null || lhs.getValue().equals(OpptjeningAktivitetKlassifisering.BEKREFTET_AVVIST)) {
            return new LocalDateSegment<>(di, OpptjeningAktivitetKlassifisering.BEKREFTET_AVVIST);
        } else if (rhs == null) {
            return new LocalDateSegment<>(di, lhs.getValue());
        } else {
            return new LocalDateSegment<>(di, lhs.getValue());
        }
    }

    private static LocalDateSegment<OpptjeningAktivitetKlassifisering> mergeGodkjente(LocalDateInterval di,
                                                                                      LocalDateSegment<OpptjeningAktivitetKlassifisering> lhs,
                                                                                      LocalDateSegment<OpptjeningAktivitetKlassifisering> rhs) {
        if (lhs != null) {
            return new LocalDateSegment<>(di, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        } else {
            OpptjeningAktivitetKlassifisering value = rhs.getValue();
            return new LocalDateSegment<>(di, value);
        }
    }
}
