package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.ReferanseType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetKlassifisering;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;

@ApplicationScoped
public class BeregningOpptjeningTestUtil {

    private static final String ORG_NR = "1337";
    private OpptjeningRepository opptjeningRepository;
    private BeregningArbeidsgiverTestUtil virksomhetTestUtil;

    BeregningOpptjeningTestUtil() {
        // for CDI
    }

    @Inject
    public BeregningOpptjeningTestUtil(ResultatRepositoryProvider repositoryProvider, BeregningArbeidsgiverTestUtil beregningArbeidsgiverTestUtil) {
        opptjeningRepository = repositoryProvider.getOpptjeningRepository();
        virksomhetTestUtil = beregningArbeidsgiverTestUtil;
    }

    public void leggTilOpptjening(Behandling behandling, LocalDate skjæringstidspunktOpptjening) {
        leggTilOpptjening(behandling, skjæringstidspunktOpptjening, ORG_NR);
    }

    public void leggTilOpptjening(Behandling behandling, LocalDate skjæringstidspunktOpptjening, String orgNr) {
        leggTilOpptjening(behandling, skjæringstidspunktOpptjening, Collections.singletonMap(orgNr, Periode.månederFør(skjæringstidspunktOpptjening, 7)));
    }

    public void leggTilOpptjening(Behandling behandling, LocalDate skjæringstidspunktOpptjening, Map<String, Periode> perioder) {
        HashMap<String, String> referanseMap = new HashMap<>();
        perioder.keySet().forEach(key -> referanseMap.put(key, key));
        leggTilOpptjening(skjæringstidspunktOpptjening, perioder, Collections.emptyMap(), Collections.emptyMap(), referanseMap, behandling.getBehandlingsresultat());
    }

    public void leggTilOpptjening(LocalDate skjæringstidspunktOpptjening, Map<String, Periode> perioder, Map<String,
        OpptjeningAktivitetType> aktivitetType, Map<String, ReferanseType> referanseType, Map<String, String> referanseMap, Behandlingsresultat behandlingsresultat) {
        List<OpptjeningAktivitet> opptjeningAktivitetList = new ArrayList<>();
        for (Map.Entry<String, String> entry : referanseMap.entrySet()) {
            virksomhetTestUtil.forArbeidsgiverVirksomhet(entry.getKey());
            opptjeningAktivitetList.add(getOpptjeningAktivitet(entry.getValue(), perioder.get(entry.getKey()).getFom(),
                perioder.get(entry.getKey()).getTomOrNull(), aktivitetType.get(entry.getKey()), referanseType.get(entry.getKey())));
        }
        opptjeningRepository.lagreOpptjeningsperiode(behandlingsresultat, skjæringstidspunktOpptjening.minusYears(1), skjæringstidspunktOpptjening);
        opptjeningRepository.lagreOpptjeningResultat(behandlingsresultat, Period.ofDays(100), opptjeningAktivitetList);
    }

    public void leggOpptjeningAktivitetTilOpptjening(Behandling behandling,
                                                     LocalDate skjæringstidspunktOpptjening,
                                                     String orgnr,
                                                     Periode periode,
                                                     OpptjeningAktivitetType opptjeningAktivitet) {
        leggTilOpptjening(skjæringstidspunktOpptjening,
            Collections.singletonMap(orgnr, periode),
            Collections.singletonMap(orgnr, opptjeningAktivitet),
            Collections.emptyMap(),
            Collections.singletonMap(orgnr, orgnr), behandling.getBehandlingsresultat());
    }

    private OpptjeningAktivitet getOpptjeningAktivitet(String ref, LocalDate fom, LocalDate tom, OpptjeningAktivitetType opptjeningAktivitetType, ReferanseType referanseType) {
        return new OpptjeningAktivitet(fom,
            tom,
            opptjeningAktivitetType == null ? OpptjeningAktivitetType.ARBEID : opptjeningAktivitetType,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ref,
            referanseType == null ? ReferanseType.ORG_NR : referanseType);
    }

}
