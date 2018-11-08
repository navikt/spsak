package no.nav.foreldrepenger.økonomistøtte.fp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.Kopimaskin;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.økonomistøtte.Oppdragsmottaker;
import no.nav.foreldrepenger.økonomistøtte.ØkonomioppdragRepository;

@ApplicationScoped
public class OppdragskontrollFørstegangFP extends OppdragskontrollForeldrepenger {

    OppdragskontrollFørstegangFP() {
    }

    @Inject
    public OppdragskontrollFørstegangFP(BehandlingRepositoryProvider repositoryProvider, TpsTjeneste tpsTjeneste,
                                        ØkonomioppdragRepository økonomioppdragRepository) {
        super(repositoryProvider, tpsTjeneste, økonomioppdragRepository);
    }

    @Override
    public void opprettØkonomiOppdrag(Behandling behandling, Optional<Oppdragskontroll> forrigeOppdragOpt, Oppdragskontroll oppdragskontroll) {
        BehandlingInfoFP behandlingInfo = oppsettBehandlingInfo(behandling);
        Map<Oppdragsmottaker, List<BeregningsresultatAndel>> andelPrMottakerMap = finnMottakereMedDeresAndel(behandlingInfo);
        if (andelPrMottakerMap.isEmpty()) {
            throw new IllegalStateException("Finner ingen oppdragsmottakere i behandling " + behandlingInfo.getBehandling().getId());
        }
        long initialLøpenummer = INITIAL_LØPENUMMER;
        for (Map.Entry<Oppdragsmottaker, List<BeregningsresultatAndel>> entry : andelPrMottakerMap.entrySet()) {
            Oppdragsmottaker mottaker = entry.getKey();
            boolean brukerErMottaker = mottaker.erBruker();
            long fagsystemId = settFagsystemId(behandlingInfo.getFagsak(), initialLøpenummer, false);
            Oppdrag110 oppdrag110 = opprettNyOppdrag110(behandlingInfo, oppdragskontroll, fagsystemId, brukerErMottaker);
            List<BeregningsresultatAndel> andelListe = entry.getValue();
            List<Oppdragslinje150> oppdragslinje150List = opprettOppdragslinje150FP(behandlingInfo, oppdrag110, andelListe, mottaker);
            opprettAttestant180(oppdragslinje150List, behandlingInfo.getAnsvarligSaksbehandler());
            initialLøpenummer++;
        }
    }

    private Map<Oppdragsmottaker, List<BeregningsresultatAndel>> finnMottakereMedDeresAndel(BehandlingInfoFP behandlingInfo) {
        Map<Oppdragsmottaker, List<BeregningsresultatAndel>> andelPrMottakerMap = new LinkedHashMap<>();

        List<BeregningsresultatAndel> brukersAndelerListe = new ArrayList<>();
        List<BeregningsresultatAndel> arbeidsgiversAndelerListe = new ArrayList<>();
        List<BeregningsresultatAndel> alleAndelersListe = hentAndeler(behandlingInfo
            .getBeregningsresultatFP()
            .orElseThrow(() -> new IllegalStateException("BeregningsresultatFP kan ikke være null"))
        );
        skilleAndelerMellomArbeidsgiverOgBruker(brukersAndelerListe, arbeidsgiversAndelerListe, alleAndelersListe);
        List<BeregningsresultatPeriode> brPeriodeListe = behandlingInfo.getBeregningsresultatFP()
            .map(BeregningsresultatFP::getBeregningsresultatPerioder).orElse(Collections.emptyList());
        slåAndelerMedSammePeriodeOgKlassekodeSammen(brPeriodeListe, brukersAndelerListe, true);

        if (!brukersAndelerListe.isEmpty()) {
            Oppdragsmottaker oppdragsmottaker = new Oppdragsmottaker(behandlingInfo.getPersonIdent().getIdent(), true);
            andelPrMottakerMap.put(oppdragsmottaker, brukersAndelerListe);
        }
        if (!arbeidsgiversAndelerListe.isEmpty()) {
            grupperArbeidsgiversAndelerMedId(andelPrMottakerMap, arbeidsgiversAndelerListe, brPeriodeListe);
        }

        return andelPrMottakerMap;
    }

    void slåAndelerMedSammePeriodeOgKlassekodeSammen(List<BeregningsresultatPeriode> brPeriodeListe, List<BeregningsresultatAndel> andelersListe, boolean erBrukerMottaker) {
        if (andelersListe.isEmpty()) {
            return;
        }
        List<BeregningsresultatAndel> andelerFiltrertListe = new ArrayList<>();

        for (BeregningsresultatPeriode brPeriode : brPeriodeListe) {
            List<BeregningsresultatAndel> brAndelListe = brPeriode.getBeregningsresultatAndelList().stream().filter(andel -> andel.erBrukerMottaker() == erBrukerMottaker)
                .filter(andel -> andel.getDagsats() > 0).collect(Collectors.toList());
            if (erBrukerMottaker) {
                finnAndelerMedSammeKlassekode(brPeriode, brAndelListe, andelerFiltrertListe);
            } else {
                String arbeidsforholdOrgnr = andelersListe.get(0).getArbeidsforholdOrgnr();
                List<BeregningsresultatAndel> brAndelListeArbeidsgiver = brAndelListe.stream()
                    .filter(andel -> andel.getArbeidsforholdOrgnr().equals(arbeidsforholdOrgnr)).collect(Collectors.toList());
                finnAndelerMedSammeKlassekode(brPeriode, brAndelListeArbeidsgiver, andelerFiltrertListe);
            }
        }
        for (BeregningsresultatAndel andel : andelerFiltrertListe) {
            String klassekode = InntektskategoriKlassekodeMapper.inntektskategoriTilKlassekode(andel.getInntektskategori());
            boolean erDetFjernet = andelersListe.removeIf(a -> a.getBeregningsresultatPeriode().equals(andel.getBeregningsresultatPeriode())
                && klassekode.equals(InntektskategoriKlassekodeMapper.inntektskategoriTilKlassekode(a.getInntektskategori())));
            if (erDetFjernet) {
                andelersListe.add(andel);
            }
        }
        andelersListe.sort(Comparator.comparing(a -> a.getBeregningsresultatPeriode().getBeregningsresultatPeriodeFom()));
    }

    private void finnAndelerMedSammeKlassekode(BeregningsresultatPeriode brPeriode, List<BeregningsresultatAndel> brAndelListe, List<BeregningsresultatAndel> andelerFiltrertListe) {
        boolean erBrukerMottaker = brAndelListe.stream().allMatch(BeregningsresultatAndel::erBrukerMottaker);
        if (erBrukerMottaker) {
            List<String> klassekodeListe = getKlassekodeListe(brAndelListe);
            for (String klassekode : klassekodeListe) {
                List<BeregningsresultatAndel> andelerMedSammeKlassekode = brAndelListe.stream()
                    .filter(andel -> klassekode.equals(InntektskategoriKlassekodeMapper.inntektskategoriTilKlassekode(andel.getInntektskategori())))
                    .collect(Collectors.toList());
                if (andelerMedSammeKlassekode.size() > 1) {
                    BeregningsresultatAndel bgresultatAndel = lagNyBeregningsresultatAndel(brPeriode, andelerMedSammeKlassekode);
                    andelerFiltrertListe.add(bgresultatAndel);
                }
            }
        } else {
            if (brAndelListe.size() > 1) {
                BeregningsresultatAndel bgresultatAndel = lagNyBeregningsresultatAndel(brPeriode, brAndelListe);
                andelerFiltrertListe.add(bgresultatAndel);
            }
        }
    }

    private BeregningsresultatAndel lagNyBeregningsresultatAndel(BeregningsresultatPeriode brPeriode, List<BeregningsresultatAndel> andelerMedSammeKlassekode) {

        BeregningsresultatAndel andel = andelerMedSammeKlassekode.get(0);
        int dagsatsSum = andelerMedSammeKlassekode.stream().mapToInt(BeregningsresultatAndel::getDagsats).sum();

        BeregningsresultatAndel nyAndel = Kopimaskin.deepCopy(andel, brPeriode);

        BeregningsresultatAndel.Builder brAndelbuilder = BeregningsresultatAndel.builder(nyAndel);
        brAndelbuilder.medInntektskategori(andel.getInntektskategori())
            .medBrukerErMottaker(andel.erBrukerMottaker())
            .medDagsats(dagsatsSum)
            .medStillingsprosent(andel.getStillingsprosent())
            .medUtbetalingsgrad(andel.getUtbetalingsgrad());
        if (!andel.erBrukerMottaker()) {
            VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder(andel.getVirksomhet()).build();
            brAndelbuilder.medVirksomhet(virksomhet);
        }
        return nyAndel;
    }

    private void grupperArbeidsgiversAndelerMedId(Map<Oppdragsmottaker, List<BeregningsresultatAndel>> andelPrMottakerMap, List<BeregningsresultatAndel> arbeidsgiversAndelListe,
                                                  List<BeregningsresultatPeriode> brPeriodeListe) {
        Map<String, List<BeregningsresultatAndel>> groupedByOrgnr = arbeidsgiversAndelListe.stream()
            .collect(Collectors.groupingBy(
                BeregningsresultatAndel::getArbeidsforholdOrgnr,
                LinkedHashMap::new,
                Collectors.mapping(Function.identity(), Collectors.toList())));

        for (Map.Entry<String, List<BeregningsresultatAndel>> entry : groupedByOrgnr.entrySet()) {
            List<BeregningsresultatAndel> finalArbgvrAndelerListe = entry.getValue();
            slåAndelerMedSammePeriodeOgKlassekodeSammen(brPeriodeListe, finalArbgvrAndelerListe, false);
            andelPrMottakerMap.put(new Oppdragsmottaker(entry.getKey(), false), finalArbgvrAndelerListe);
        }
    }
}
