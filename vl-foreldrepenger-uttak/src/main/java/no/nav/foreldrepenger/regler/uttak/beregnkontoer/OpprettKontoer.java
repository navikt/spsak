package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerPropertyType;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(OpprettKontoer.ID)
class OpprettKontoer extends LeafSpecification<BeregnKontoerGrunnlag> {

    private Konfigurasjon konfigurasjon;
    private Kontokonfigurasjon[] kontokonfigurasjoner;
    public static final String ID = "Opprett kontoer";

    OpprettKontoer(Konfigurasjon konfigurasjon, Kontokonfigurasjon... kontokonfigurasjontoner) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
        this.kontokonfigurasjoner = kontokonfigurasjontoner;
    }

    @Override
    public Evaluation evaluate(BeregnKontoerGrunnlag grunnlag) {
        Map<Stønadskontotype, Integer> kontoerMap = new EnumMap<>(Stønadskontotype.class);
        int antallExtraBarnDager = 0;

        // Finn antall ekstra dager først.
        for (Kontokonfigurasjon kontokonfigurasjon : kontokonfigurasjoner) {
            if (kontokonfigurasjon.getStønadskontotype() == Stønadskontotype.FLERBARNSDAGER) {
                antallExtraBarnDager = konfigurasjon.getParameter(kontokonfigurasjon.getParametertype(), grunnlag.getFamiliehendelsesdato());
                break;
            }
        }

        // Opprette alle kontoer utenom samtidig uttak
        for (Kontokonfigurasjon kontokonfigurasjon : kontokonfigurasjoner) {
            if (kontokonfigurasjon.getStønadskontotype() != Stønadskontotype.FLERBARNSDAGER) {
                int antallDager = konfigurasjon.getParameter(kontokonfigurasjon.getParametertype(), grunnlag.getFamiliehendelsesdato());
                if (antallExtraBarnDager > 0) {
                    // Legg ekstra dager til foreldrepenger eller fellesperiode.
                    if ((kontokonfigurasjon.getStønadskontotype().equals(Stønadskontotype.FORELDREPENGER))) {
                        antallDager += antallExtraBarnDager;
                    } else if (kontokonfigurasjon.getStønadskontotype().equals(Stønadskontotype.FELLESPERIODE)) {
                        antallDager += antallExtraBarnDager;
                        kontoerMap.put(Stønadskontotype.FLERBARNSDAGER, antallExtraBarnDager);
                    }
                }
                kontoerMap.put(kontokonfigurasjon.getStønadskontotype(), antallDager);
            }
        }
        return beregnetMedResultat(kontoerMap, antallExtraBarnDager);
    }

    private Evaluation beregnetMedResultat(Map<Stønadskontotype, Integer> kontoer, Integer antallExtraBarnDager) {
        SingleEvaluation eval = ja();
        Map<String, Object> properties = new HashMap<>();
        properties.put(BeregnKontoerPropertyType.KONTOER, kontoer);
        properties.put(BeregnKontoerPropertyType.ANTALL_FLERBARN_DAGER, antallExtraBarnDager);

        eval.setEvaluationProperties(properties);
        return eval;
    }
}
