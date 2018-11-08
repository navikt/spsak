package no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.sigrun;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.integrasjon.sigrun.BeregnetSkatt;
import no.nav.vedtak.felles.integrasjon.sigrun.SigrunConsumer;
import no.nav.vedtak.felles.integrasjon.sigrun.SigrunResponse;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;


@ApplicationScoped
public class SigrunTjenesteImpl implements SigrunTjeneste {

    private static final Map<String, InntektspostType> TEKNISK_NAVN_TIL_KODE_MAP = new HashMap<>();
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private SigrunConsumer sigrunConsumer;

    SigrunTjenesteImpl() {
        //CDI
    }

    @Inject
    public SigrunTjenesteImpl(SigrunConsumer sigrunConsumer, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        this.sigrunConsumer = sigrunConsumer;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        fyllMap(TEKNISK_NAVN_TIL_KODE_MAP);
    }

    @Override
    public void hentOgLagrePGI(Behandling behandling, AktørId aktørId) {
        SigrunResponse beregnetskatt = sigrunConsumer.beregnetskatt(aktørId.longValue());

        Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> map = mapFraSigrunTilIntern(beregnetskatt.getBeregnetSkatt());

        InntektArbeidYtelseAggregatBuilder aggregatBuilder = inntektArbeidYtelseTjeneste.opprettBuilderForRegister(behandling);
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = aggregatBuilder.getAktørInntektBuilder(aktørId);

        AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektsKilde.SIGRUN, null);

        for (Map.Entry<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> entry : map.entrySet()) {
            for (Map.Entry<InntektspostType, BigDecimal> type : entry.getValue().entrySet()) {
                InntektEntitet.InntektspostBuilder inntektspostBuilder = inntektBuilder.getInntektspostBuilder();
                inntektspostBuilder
                    .medInntektspostType(type.getKey())
                    .medBeløp(type.getValue())
                    .medPeriode(entry.getKey().getFomDato(), entry.getKey().getTomDato());
                inntektBuilder.leggTilInntektspost(inntektspostBuilder);
            }
        }
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        aggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);

        inntektArbeidYtelseTjeneste.lagre(behandling, aggregatBuilder);
    }

    private Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> mapFraSigrunTilIntern(Map<Year, List<BeregnetSkatt>> beregnetSkatt) {
        Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> årTilInntektMap = new HashMap<>();

        for (Map.Entry<Year, List<BeregnetSkatt>> entry : beregnetSkatt.entrySet()) {
            DatoIntervallEntitet intervallEntitet = lagDatoIntervall(entry);
            Map<InntektspostType, BigDecimal> typeTilVerdiMap = new HashMap<>();
            for (BeregnetSkatt beregnetSkatteobjekt : entry.getValue()) {
                InntektspostType type = TEKNISK_NAVN_TIL_KODE_MAP.get(beregnetSkatteobjekt.getTekniskNavn());
                BigDecimal beløp = typeTilVerdiMap.get(type);
                if (beløp == null) {
                    typeTilVerdiMap.put(type, new BigDecimal(beregnetSkatteobjekt.getVerdi()));
                } else {
                    typeTilVerdiMap.replace(type, beløp.add(new BigDecimal(beregnetSkatteobjekt.getVerdi())));
                }
            }
            årTilInntektMap.put(intervallEntitet, typeTilVerdiMap);
        }

        return årTilInntektMap;
    }

    private DatoIntervallEntitet lagDatoIntervall(Map.Entry<Year, List<BeregnetSkatt>> entry) {
        Year år = entry.getKey();
        LocalDateTime førsteDagIÅret = LocalDateTime.now().withYear(år.getValue()).withDayOfYear(1);
        LocalDateTime sisteDagIÅret = LocalDateTime.now().withYear(år.getValue()).withDayOfYear(år.length());
        return DatoIntervallEntitet.fraOgMedTilOgMed(førsteDagIÅret.toLocalDate(), sisteDagIÅret.toLocalDate());
    }

    //TODO(OJR) vurder å flytte til kodeliste
    private void fyllMap(Map<String, InntektspostType> map) {
        map.put("personinntektLoenn", InntektspostType.LØNN);
        map.put("personinntektBarePensjonsdel", InntektspostType.LØNN);
        map.put("personinntektNaering", InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE);
        map.put("personinntektFiskeFangstFamiliebarnehage", InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE);
        map.put("svalbardLoennLoennstrekkordningen", InntektspostType.LØNN);
        map.put("svalbardPersoninntektNaering", InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE);
    }
}
