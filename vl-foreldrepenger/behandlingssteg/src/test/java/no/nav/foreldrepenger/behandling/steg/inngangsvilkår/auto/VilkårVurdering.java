package no.nav.foreldrepenger.behandling.steg.inngangsvilkår.auto;

import static org.hamcrest.CoreMatchers.equalTo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.junit.rules.ErrorCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.inngangsvilkaar.adopsjon.AdopsjonsvilkårForeldrepenger;
import no.nav.foreldrepenger.inngangsvilkaar.fødsel.FødselsvilkårFar;
import no.nav.foreldrepenger.inngangsvilkaar.fødsel.FødselsvilkårMor;
import no.nav.foreldrepenger.inngangsvilkaar.medlemskap.Medlemskapsvilkår;
import no.nav.foreldrepenger.inngangsvilkaar.opptjening.Opptjeningsvilkår;
import no.nav.foreldrepenger.inngangsvilkaar.opptjeningsperiode.RegelFastsettOpptjeningsperiode;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.AdopsjonsvilkårGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.FødselsvilkårGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.MedlemskapsvilkårGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.OpptjeningsPeriode;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.Opptjeningsgrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.OpptjeningsvilkårResultat;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSummary;
import no.nav.vedtak.util.Tuple;

class VilkårVurdering {

    private static final Logger log = LoggerFactory.getLogger(VilkårVurdering.class);
    static final BiFunction<VilkårResultat, Object, Boolean> DO_NOTHING = (res, obj) -> {
        return true;
    };

    void vurderVilkår(String filenamePrefix, ErrorCollector collector, VilkårType vilkår, BiFunction<VilkårResultat, Object, Boolean> extraDataValidering) {
        Objects.requireNonNull(vilkår, "vilkår");
        final File vilkårMappe = new File("src/test/testscript/vilkår/" + vilkår.getKode() + "/");
        if (vilkårMappe.listFiles() != null) {
            List<File> fileList = Arrays.stream(vilkårMappe.listFiles()).filter(it -> it.getName().startsWith(filenamePrefix)).collect(Collectors.toList());
            vurderCaser(collector, vilkår, extraDataValidering, fileList.toArray(new File[0]));
        }
    }

    void vurderVilkår(ErrorCollector collector, VilkårType vilkår) {
        vurderVilkår(collector, vilkår, DO_NOTHING);
    }

    void vurderVilkår(ErrorCollector collector, VilkårType vilkår, BiFunction<VilkårResultat, Object, Boolean> extraDataValidering) {
        Objects.requireNonNull(vilkår, "vilkår");
        final File vilkårMappe = new File("src/test/testscript/vilkår/" + vilkår.getKode() + "/");
        final File[] files = vilkårMappe.listFiles();
        vurderCaser(collector, vilkår, extraDataValidering, files);
    }

    private void vurderCaser(ErrorCollector collector, VilkårType vilkår, BiFunction<VilkårResultat, Object, Boolean> extraDataValidering, File[] files) {
        if (files != null) {
            // Aktiverer funksjonell tid
            System.setProperty("funksjonelt.tidsoffset.aktivert", Boolean.TRUE.toString());
            final List<File> inputFiler = Arrays.stream(files).filter(it -> it.getName().endsWith(JsonUtil.INPUT_SUFFIX)).collect(Collectors.toList());
            log.info("Sjekker " + vilkår.getKode() + ", fant " + inputFiler.size() + " testcaser.");

            final Tuple<Tuple<Class<Object>, Object>, RuleService<Object>> vilkårRegel = getVilkårImplementasjon(vilkår);
            final ObjectMapper mapper = JsonUtil.getObjectMapper();
            for (File inputFile : inputFiler) {
                try {
                    final Tuple<Class<Object>, Object> vilkårObjectClasses = vilkårRegel.getElement1();

                    final Object input = mapper.readValue(inputFile, vilkårObjectClasses.getElement1());

                    final Optional<File> outputFile = finnOutputFil(files, inputFile);
                    if (outputFile.isPresent()) {
                        final VilkårResultat vilkårResultat = mapper.readValue(outputFile.get(), VilkårResultat.class);

                        settFunksjonellTidTilKjøretidspunkt(vilkårResultat);

                        final Evaluation evaluer;
                        final Object resultatObject = vilkårObjectClasses.getElement2();
                        if (resultatObject instanceof NoneObject) {
                            evaluer = vilkårRegel.getElement2().evaluer(input);
                        } else {
                            evaluer = vilkårRegel.getElement2().evaluer(input, resultatObject);
                        }

                        final EvaluationSummary evaluationSummary = new EvaluationSummary(evaluer);
                        log.info("Vurderer " + vilkårResultat);

                        collector.checkThat("Vurdering av " + inputFile.getName() + " ga ikke forventet resultat.",
                            getVilkårUtfallType(evaluationSummary), equalTo(vilkårResultat.getUtfall()));
                        
                        if(!extraDataValidering.apply(vilkårResultat, resultatObject)) {
                            log.info("Feil i output for inputFile=" + inputFile);
                            // Kommenter inn hvis sikker på at inputfilene ikke er korrekte lenger
                            // inputFile.delete();
                            // outputFile.ifPresent(o -> o.delete());
                        }
                    } else {
                        log.warn("Fant ikke output for evaluering av " + inputFile.getName());
                        collector.addError(new FileNotFoundException("Fant ikke output for evaluering av " + inputFile.getName()));
                    }
                } catch (IOException e) {
                    log.error("Noe uventet gikk galt under parsing av '" + inputFile.getName() + "' : " + e.getMessage(), e);
                    collector.addError(e);
                }
            }
        } else {
            log.warn("Fant ingen testcaser for " + vilkår.getKode());
        }
        System.setProperty("funksjonelt.tidsoffset.aktivert", Boolean.FALSE.toString());
    }

    private void settFunksjonellTidTilKjøretidspunkt(VilkårResultat vilkårResultat) {
        final Period periode = Period.between(LocalDate.now(), vilkårResultat.getKjøreTidspunkt());
        System.setProperty("funksjonelt.tidsoffset.offset", periode.toString());
    }

    private Optional<File> finnOutputFil(File[] files, File inputFile) {
        return Arrays.stream(files)
            .filter(it -> it.getName().endsWith(inputFile.getName().replace(JsonUtil.INPUT_SUFFIX, JsonUtil.OUTPUT_SUFFIX)))
            .findAny(); // Skal bare være en
    }

    private String getVilkårUtfallType(EvaluationSummary summary) {
        Collection<Evaluation> leafEvaluations = summary.leafEvaluations();
        for (Evaluation ev : leafEvaluations) {
            if (ev.getOutcome() != null) {
                Resultat res = ev.result();
                switch (res) {
                    case JA:
                        return VilkårUtfallType.OPPFYLT.getKode();
                    case NEI:
                        return VilkårUtfallType.IKKE_OPPFYLT.getKode();
                    case IKKE_VURDERT:
                        return VilkårUtfallType.IKKE_VURDERT.getKode();
                    default:
                        throw new IllegalArgumentException("Ukjent Resultat:" + res + " ved evaluering av:" + ev);
                }
            } else {
                return VilkårUtfallType.OPPFYLT.getKode();
            }
        }

        throw new IllegalArgumentException("leafEvaluations.isEmpty():" + leafEvaluations);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T, X> Tuple<Tuple<Class<T>, X>, RuleService<T>> getVilkårImplementasjon(VilkårType vilkår) {
        if (VilkårType.MEDLEMSKAPSVILKÅRET.equals(vilkår)) {
            return new Tuple(new Tuple<>(MedlemskapsvilkårGrunnlag.class, new NoneObject()), new Medlemskapsvilkår());
        }
        if (VilkårType.OPPTJENINGSVILKÅRET.equals(vilkår)) {
            return new Tuple(new Tuple<>(Opptjeningsgrunnlag.class, new OpptjeningsvilkårResultat()), new Opptjeningsvilkår());
        }
        if (VilkårType.FØDSELSVILKÅRET_MOR.equals(vilkår)) {
            return new Tuple(new Tuple<>(FødselsvilkårGrunnlag.class, new NoneObject()), new FødselsvilkårMor());
        }
        if (VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR.equals(vilkår)) {
            return new Tuple(new Tuple<>(FødselsvilkårGrunnlag.class, new NoneObject()), new FødselsvilkårFar());
        }
        if (VilkårType.ADOPSJONSVILKARET_FORELDREPENGER.equals(vilkår)) {
            return new Tuple(new Tuple<>(AdopsjonsvilkårGrunnlag.class, new NoneObject()), new AdopsjonsvilkårForeldrepenger());
        }
        if (VilkårType.OPPTJENINGSPERIODEVILKÅR.equals(vilkår)) {
            return new Tuple(new Tuple<>(OpptjeningsperiodeGrunnlag.class, new OpptjeningsPeriode()), new RegelFastsettOpptjeningsperiode());
        }
        throw new IllegalArgumentException("Støtter ikke vilkår: " + vilkår);
    }

    private static class NoneObject {
    }
}
