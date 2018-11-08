package no.nav.foreldrepenger.økonomistøtte.fp;

import java.time.LocalDate;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFPKobling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl.EndringsdatoRevurderingUtleder;
import no.nav.foreldrepenger.økonomistøtte.Oppdragsmottaker;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.OppdragsmottakerStatus;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.ØkonomioppdragRepository;

@ApplicationScoped
public class OppdragskontrollEndringFP extends OppdragskontrollForeldrepenger {

    private static final Logger log = LoggerFactory.getLogger(OppdragskontrollEndringFP.class);

    private BeregningsresultatFPRepository beregningsresultatFPRepository;
    private OppdragskontrollFørstegangFP oppdragskontrollFørstegangFP;
    private OppdragskontrollOpphørFP oppdragskontrollOpphørFP;
    private EndringsdatoRevurderingUtleder endringsdatoUtleder;

    OppdragskontrollEndringFP() {
        // For CDI
    }

    @Inject
    public OppdragskontrollEndringFP(BehandlingRepositoryProvider repositoryProvider, TpsTjeneste tpsTjeneste,
                                     ØkonomioppdragRepository økonomioppdragRepository, OppdragskontrollOpphørFP oppdragskontrollOpphørFP,
                                     OppdragskontrollFørstegangFP oppdragskontrollFørstegangFP, EndringsdatoRevurderingUtleder endringsdatoUtleder) {
        super(repositoryProvider, tpsTjeneste, økonomioppdragRepository);
        this.beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
        this.oppdragskontrollFørstegangFP = oppdragskontrollFørstegangFP;
        this.oppdragskontrollOpphørFP = oppdragskontrollOpphørFP;
        this.endringsdatoUtleder = endringsdatoUtleder;
    }

    @Override
    public void opprettØkonomiOppdrag(Behandling behandling, Optional<Oppdragskontroll> forrigeOppdragOpt, Oppdragskontroll oppdragskontroll) {
        BehandlingInfoFP behandlingInfo = oppsettBehandlingInfo(behandling);
        Oppdragskontroll forrigeOppdrag = forrigeOppdragOpt.orElseThrow(() -> new IllegalStateException("Fant ikke forrigeOppdrag"));
        Behandling revurderingBehandling = behandlingInfo.getBehandling();
        Behandling originalBehandling = behandlingInfo.getBehandling().getOriginalBehandling()
            .orElseThrow(() -> new IllegalStateException("Det finnes ikke en original behandling som revurdering behandling baserer seg på" +
                behandlingInfo.getBehandling().getId()));
        BeregningsresultatFPKobling beregningsresultatFPKobling = beregningsresultatFPRepository.hentBeregningsresultatFPKobling(originalBehandling)
            .orElseThrow(() -> new IllegalStateException("Mangler beregningsresultatFPKobling for behandling " + behandlingInfo.getBehandling().getId()));
        BeregningsresultatFP bgresultatFPOriginal = beregningsresultatFPKobling.getBeregningsresultatFP();

        LocalDate endringsdato = endringsdatoUtleder.utledEndringsdato(revurderingBehandling);
        log.info("Endringstidspunkt som skal brukes i økonomi oppdrag er = {}.", endringsdato);

        lagOppdragForMottakereSomSkalOpphøre(behandlingInfo, forrigeOppdrag, bgresultatFPOriginal, endringsdato, oppdragskontroll);

        List<BeregningsresultatAndel> andelerOriginal = hentAndeler(bgresultatFPOriginal);
        Map<Oppdragsmottaker, List<BeregningsresultatAndel>> andelPrMottakerMap = finnMottakereMedDeresAndelForEndringsoppdrag(behandlingInfo, andelerOriginal);

        if (andelPrMottakerMap.isEmpty()) {
            throw new IllegalStateException("Finnes ingen oppdragsmottakere i behandling " + behandlingInfo.getBehandling().getId());
        }
        opprettEndringsoppdrag(behandlingInfo, forrigeOppdrag, endringsdato, andelPrMottakerMap, oppdragskontroll);
    }

    private boolean erEndringstidspunktEtterSisteDatoIForrigeOppdrag(LocalDate endringsdato, Oppdragskontroll forrigeOppdrag, Oppdragsmottaker mottaker) {
        if (mottaker.erBruker()) {
            List<Oppdragslinje150> tidligereOpp150Liste = Oppdragslinje150Verktøy.hentTidligereOppdragslinje150(forrigeOppdrag, true, false);
            LocalDate sisteDato = tidligereOpp150Liste.stream().map(Oppdragslinje150::getDatoVedtakTom).max(Comparator.comparing(Function.identity()))
                .orElseThrow(() -> new IllegalStateException("Forrige oppdragsmelding mangler vedtak tom dato"));
            return endringsdato.isAfter(sisteDato);
        } else {
            List<Oppdragslinje150> tidligereOpp150ListeForAlleArbgvr = Oppdragslinje150Verktøy.hentTidligereOppdragslinje150(forrigeOppdrag, false, false);
            LocalDate sisteDato = tidligereOpp150ListeForAlleArbgvr.stream().filter(opp150 -> opp150.getRefusjonsinfo156() != null)
                .filter(opp150 -> opp150.getRefusjonsinfo156().getRefunderesId().equals(endreTilElleveSiffer(mottaker.getId())))
                .map(Oppdragslinje150::getDatoVedtakTom)
                .max(Comparator.comparing(Function.identity()))
                .orElseThrow(() -> new IllegalStateException("Forrige oppdragsmelding mangler vedtak tom dato"));
            return endringsdato.isAfter(sisteDato);
        }
    }

    private void opprettEndringsoppdrag(BehandlingInfoFP behandlingInfo, Oppdragskontroll forrigeOppdrag,
                                        LocalDate endringsdato, Map<Oppdragsmottaker, List<BeregningsresultatAndel>> andelPrMottakerMap,
                                        Oppdragskontroll oppdragskontroll) {
        long løpVerdiForFørstegangsoppdrag = finnInitialLøpenummerVerdi(forrigeOppdrag);
        for (Map.Entry<Oppdragsmottaker, List<BeregningsresultatAndel>> entry : andelPrMottakerMap.entrySet()) {
            Oppdragsmottaker mottaker = entry.getKey();
            if (mottaker.getStatus() != null && mottaker.getStatus().equals(OppdragsmottakerStatus.NY)) {
                opprettOppdragForNyeMottakere(behandlingInfo, oppdragskontroll, entry, mottaker, løpVerdiForFørstegangsoppdrag);
                løpVerdiForFørstegangsoppdrag++;
            }
            if (mottaker.getStatus() != null && mottaker.getStatus().equals(OppdragsmottakerStatus.UENDR)) {
                if (mottaker.erBruker()) {
                    opprettEndringsoppdragForBruker(behandlingInfo, forrigeOppdrag, endringsdato, oppdragskontroll, entry, mottaker);
                } else {
                    opprettEndringsoppdragForHverMottaker(behandlingInfo, forrigeOppdrag, endringsdato, oppdragskontroll, entry, mottaker);
                }
            }
        }
    }

    private void opprettEndringsoppdragForBruker(BehandlingInfoFP behandlingInfo, Oppdragskontroll forrigeOppdrag,
                                                 LocalDate endringsdato, Oppdragskontroll oppdragskontroll,
                                                 Map.Entry<Oppdragsmottaker, List<BeregningsresultatAndel>> entry, Oppdragsmottaker mottaker) {
        boolean erDetFlereKlassekodeIForrigeOppdrag = finnesFlereKlassekodeIForrigeOppdrag(forrigeOppdrag, mottaker.erBruker());
        boolean erDetFlereKlassekodeINyOppdrag = getKlassekodeListe(entry.getValue()).size() > 1;
        if (!erDetFlereKlassekodeINyOppdrag && !erDetFlereKlassekodeIForrigeOppdrag) {
            opprettEndringsoppdragForHverMottaker(behandlingInfo, forrigeOppdrag, endringsdato, oppdragskontroll, entry, mottaker);
        } else {
            opprettEndringsoppdragForBrukerMedFlereKlassekode(behandlingInfo, forrigeOppdrag, endringsdato, oppdragskontroll, entry, mottaker);
        }
    }

    private void opprettEndringsoppdragForHverMottaker(BehandlingInfoFP behandlingInfo, Oppdragskontroll forrigeOppdrag,
                                                       LocalDate endringsdato, Oppdragskontroll oppdragskontroll, Map.Entry<Oppdragsmottaker,
        List<BeregningsresultatAndel>> entry, Oppdragsmottaker mottaker) {
        if (mottaker.erBruker()) {
            Optional<Oppdragslinje150> sisteOppdr150BrukerOpt = Oppdragslinje150Verktøy.finnSisteLinjeIKjedeForBruker(forrigeOppdrag);
            Oppdrag110 nyOppdrag110;
            if (sisteOppdr150BrukerOpt.isPresent()) {
                Oppdragslinje150 sisteOppdr150Bruker = sisteOppdr150BrukerOpt.get();
                boolean endringstidspunktEtterSisteDatoIForrigeOppdrag = erEndringstidspunktEtterSisteDatoIForrigeOppdrag(endringsdato, forrigeOppdrag, mottaker);
                Optional<Oppdrag110> nyOppdrag110Opt = opprettOpphørIEndringsoppdragBruker(behandlingInfo, forrigeOppdrag,
                    oppdragskontroll, endringsdato, endringstidspunktEtterSisteDatoIForrigeOppdrag, true);
                nyOppdrag110 = nyOppdrag110Opt.orElseGet(() -> opprettOppdrag110MedRelaterteOppdragsmeldinger(behandlingInfo,
                    oppdragskontroll, sisteOppdr150Bruker));
                List<BeregningsresultatAndel> andelListe = entry.getValue();
                List<Oppdragslinje150> oppdragslinje150List = opprettOppdragslinje150FP(behandlingInfo, nyOppdrag110,
                    andelListe, mottaker, sisteOppdr150Bruker);
                opprettAttestant180(oppdragslinje150List, behandlingInfo.getAnsvarligSaksbehandler());
            }
        } else {
            List<Oppdragslinje150> sisteOppdr150ArbeidsgivereListe = Oppdragslinje150Verktøy.finnSisteLinjeIKjedeForArbeidsgivere(forrigeOppdrag);
            for (Oppdragslinje150 sisteOppdr150 : sisteOppdr150ArbeidsgivereListe) {
                String orgNrFraRevurd = entry.getKey().getId();
                String orgNrFraOriginal = sisteOppdr150.getRefusjonsinfo156().getRefunderesId();
                Oppdrag110 nyOppdrag110;
                if (orgNrFraOriginal.equals(endreTilElleveSiffer(orgNrFraRevurd))) {
                    Optional<Oppdrag110> nyOppdrag110Opt = opprettOpphørIEndringsoppdragArbeidsgiver(behandlingInfo, oppdragskontroll, endringsdato, sisteOppdr150, mottaker, true);
                    nyOppdrag110 = nyOppdrag110Opt.orElseGet(() -> opprettOppdrag110MedRelaterteOppdragsmeldinger(behandlingInfo, oppdragskontroll, sisteOppdr150));
                    List<BeregningsresultatAndel> andelListe = entry.getValue();
                    List<Oppdragslinje150> oppdragslinje150List = opprettOppdragslinje150FP(behandlingInfo, nyOppdrag110, andelListe, mottaker, sisteOppdr150);
                    opprettAttestant180(oppdragslinje150List, behandlingInfo.getAnsvarligSaksbehandler());
                }
            }
        }
    }

    private Oppdrag110 opprettOppdrag110MedRelaterteOppdragsmeldinger(BehandlingInfoFP behandlingInfo, Oppdragskontroll oppdragskontroll, Oppdragslinje150 sisteOppdr150Bruker) {
        Avstemming115 avstemming115 = opprettAvstemming115();
        Oppdrag110 forrigeOppdrag110 = sisteOppdr150Bruker.getOppdrag110();
        long fagsystemId = forrigeOppdrag110.getFagsystemId();
        boolean erBrukerMottaker = forrigeOppdrag110.getKodeFagomrade().equals(ØkonomiKodeFagområde.FP.name());
        Oppdrag110 nyOppdrag110 = opprettOppdrag110FP(behandlingInfo, oppdragskontroll, avstemming115, erBrukerMottaker, false, fagsystemId);
        opprettOppdragsenhet120(nyOppdrag110);

        return nyOppdrag110;
    }

    private void opprettEndringsoppdragForBrukerMedFlereKlassekode(BehandlingInfoFP behandlingInfo, Oppdragskontroll forrigeOppdrag,
                                                   LocalDate endringsdato, Oppdragskontroll oppdragskontroll,
                                                   Map.Entry<Oppdragsmottaker, List<BeregningsresultatAndel>> entry, Oppdragsmottaker mottaker) {
        boolean erDetFlereKlassekodeIForrigeOppdrag = finnesFlereKlassekodeIForrigeOppdrag(forrigeOppdrag, mottaker.erBruker());
        boolean erDetFlereKlassekodeINyOppdrag = getKlassekodeListe(entry.getValue()).size() > 1;
        List<BeregningsresultatAndel> andelListe = entry.getValue();
        if (!erDetFlereKlassekodeIForrigeOppdrag && erDetFlereKlassekodeINyOppdrag) {
            opprettOppdragForBrukerMedFlereKlassekodeIRevurdering(behandlingInfo, forrigeOppdrag, endringsdato, oppdragskontroll, mottaker, andelListe);
        } else if (erDetFlereKlassekodeIForrigeOppdrag && !erDetFlereKlassekodeINyOppdrag) {
            opprettOppdragForBrukerMedFlereKlassekodeIForrigeBehandling(behandlingInfo, forrigeOppdrag, endringsdato,
                oppdragskontroll, mottaker, andelListe);
        } else {
            opprettOppdragForBrukerMedFlereKlassekode(behandlingInfo, forrigeOppdrag, endringsdato, oppdragskontroll, mottaker, andelListe);
        }
    }

    private void opprettOppdragForBrukerMedFlereKlassekode(BehandlingInfoFP behandlingInfo, Oppdragskontroll forrigeOppdrag, LocalDate endringsdato, Oppdragskontroll oppdragskontroll, Oppdragsmottaker mottaker, List<BeregningsresultatAndel> andelListe) {
        Optional<Oppdrag110> nyOppdrag110Opt = oppdragskontrollOpphørFP.opprettOpphørsoppdragForBrukerMedFlereKlassekode(behandlingInfo, oppdragskontroll, forrigeOppdrag, endringsdato, true);
        List<Oppdragslinje150> tidligereOpp150Liste = Oppdragslinje150Verktøy.hentTidligereOppdragslinje150(forrigeOppdrag, true, true);
        List<Oppdragslinje150> opphørtOppdragslinje150Liste = new ArrayList<>();
        Oppdragslinje150 tidligereOppdr150Bruker = tidligereOpp150Liste.get(0);
        Oppdrag110 nyOppdrag110 = nyOppdrag110Opt.orElseGet(() -> opprettOppdrag110MedRelaterteOppdragsmeldinger(behandlingInfo, oppdragskontroll, tidligereOppdr150Bruker));
        for (Oppdragslinje150 nyOpp150 : nyOppdrag110.getOppdragslinje150Liste()) {
            tidligereOpp150Liste.stream().filter(forrigeOpp150 -> forrigeOpp150.getDelytelseId().equals(nyOpp150.getDelytelseId())).findFirst().ifPresent(opphørtOppdragslinje150Liste::add);
        }
        List<List<BeregningsresultatAndel>> andelerGruppertMedKlassekode = gruppereAndelerMedKlassekode(andelListe);
        List<Oppdragslinje150> oppdragslinje150List = opprettOppdr150ForBrukerMedFlereKlassekode(behandlingInfo, nyOppdrag110, andelerGruppertMedKlassekode, mottaker, opphørtOppdragslinje150Liste);
        opprettAttestant180(oppdragslinje150List, behandlingInfo.getAnsvarligSaksbehandler());
    }

    private void opprettOppdragForBrukerMedFlereKlassekodeIForrigeBehandling(BehandlingInfoFP behandlingInfo,
                                                                             Oppdragskontroll forrigeOppdrag,
                                                                             LocalDate endringsdato,
                                                                             Oppdragskontroll oppdragskontroll,
                                                                             Oppdragsmottaker mottaker,
                                                                             List<BeregningsresultatAndel> andelListe) {
        Optional<Oppdrag110> nyOppdrag110Opt = oppdragskontrollOpphørFP.opprettOpphørsoppdragForBrukerMedFlereKlassekode(behandlingInfo,
            oppdragskontroll, forrigeOppdrag, endringsdato, true);
        Inntektskategori inntektskategori = andelListe.stream().map(BeregningsresultatAndel::getInntektskategori).findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Mangler inntektskategori"));
        String kodeKlassifik = InntektskategoriKlassekodeMapper.inntektskategoriTilKlassekode(inntektskategori);
        List<Oppdragslinje150> tidligereOppdr150Liste = Oppdragslinje150Verktøy.hentTidligereOppdragslinje150(forrigeOppdrag, true, false);
        Oppdragslinje150 oppdr150MedMaxDelytelseId = tidligereOppdr150Liste.stream().max(Comparator.comparing(Oppdragslinje150::getDelytelseId))
            .orElseThrow(() -> new IllegalStateException("Utvikler feil: Mangler forrige oppdrag"));
        Oppdragslinje150 sisteOppdr150Bruker = tidligereOppdr150Liste.stream().filter(oppdr150 -> oppdr150.getKodeKlassifik().equals(kodeKlassifik))
            .max(Comparator.comparing(Oppdragslinje150::getDatoVedtakFom)).orElse(oppdr150MedMaxDelytelseId);
        Oppdrag110 nyOppdrag110 = nyOppdrag110Opt.orElseGet(() -> opprettOppdrag110MedRelaterteOppdragsmeldinger(behandlingInfo,
            oppdragskontroll, sisteOppdr150Bruker));
        List<Oppdragslinje150> oppdragslinje150List = opprettOppdragslinje150FP(behandlingInfo, nyOppdrag110, andelListe, mottaker, sisteOppdr150Bruker);
        opprettAttestant180(oppdragslinje150List, behandlingInfo.getAnsvarligSaksbehandler());
    }

    private void opprettOppdragForBrukerMedFlereKlassekodeIRevurdering(BehandlingInfoFP behandlingInfo,
                                                                       Oppdragskontroll forrigeOppdrag,
                                                                       LocalDate endringsdato,
                                                                       Oppdragskontroll oppdragskontroll,
                                                                       Oppdragsmottaker mottaker, List<BeregningsresultatAndel> andelListe) {
        Optional<Oppdragslinje150> sisteOppdr150BrukerOpt = Oppdragslinje150Verktøy.finnSisteLinjeIKjedeForBruker(forrigeOppdrag);
        if (sisteOppdr150BrukerOpt.isPresent()) {
            Oppdragslinje150 sisteOppdr150Bruker = sisteOppdr150BrukerOpt.get();
            boolean endringstidspunktEtterSisteDatoIForrigeOppdrag = erEndringstidspunktEtterSisteDatoIForrigeOppdrag(endringsdato, forrigeOppdrag, mottaker);
            Optional<Oppdrag110> nyOppdrag110Opt = opprettOpphørIEndringsoppdragBruker(behandlingInfo, forrigeOppdrag,
                oppdragskontroll, endringsdato, endringstidspunktEtterSisteDatoIForrigeOppdrag, true);
            Oppdrag110 nyOppdrag110 = nyOppdrag110Opt.orElseGet(() -> opprettOppdrag110MedRelaterteOppdragsmeldinger(behandlingInfo, oppdragskontroll, sisteOppdr150Bruker));
            List<List<BeregningsresultatAndel>> andelerGruppertMedKlassekode = gruppereAndelerMedKlassekode(andelListe);
            List<Oppdragslinje150> oppdragslinje150List = opprettOppdr150ForBrukerMedFlereKlassekode(behandlingInfo, nyOppdrag110,
                andelerGruppertMedKlassekode, mottaker, Collections.singletonList(sisteOppdr150Bruker));
            opprettAttestant180(oppdragslinje150List, behandlingInfo.getAnsvarligSaksbehandler());
        }
    }

    private void lagOpphørsoppdragForBrukerMedEnKlassekode(BehandlingInfoFP behandlingInfo, Oppdragskontroll forrigeOppdrag,
                                                           Oppdragskontroll oppdragskontroll, LocalDate endringsdato, Oppdragsmottaker mottaker) {
        boolean endringstidspunktEtterSisteDatoIForrigeOppdrag = erEndringstidspunktEtterSisteDatoIForrigeOppdrag(endringsdato, forrigeOppdrag, mottaker);
        opprettOpphørIEndringsoppdragBruker(behandlingInfo, forrigeOppdrag, oppdragskontroll, endringsdato, endringstidspunktEtterSisteDatoIForrigeOppdrag, false);
    }

    private Optional<Oppdrag110> opprettOpphørIEndringsoppdragBruker(BehandlingInfoFP behandlingInfo,
                                                                     Oppdragskontroll forrigeOppdrag, Oppdragskontroll oppdragskontroll,
                                                                     LocalDate endringsdato, boolean endringstidspunktEtterSisteDatoIForrigeOppdrag, boolean opphFørEndringsoppdrFeriepg) {
        if (oppdragskontrollOpphørFP.erEndringsdatoEtterSisteDatoAvAlleTidligereOppdrag(endringsdato, forrigeOppdrag)) {
            return Optional.empty();
        }
        List<Oppdragslinje150> opp150OpphList = new ArrayList<>();
        Optional<Oppdragslinje150> sisteOppdr150BrukerOpt = Oppdragslinje150Verktøy.finnSisteLinjeIKjedeForBruker(forrigeOppdrag);
        Oppdragslinje150 sisteOppdr150Bruker = sisteOppdr150BrukerOpt.orElseThrow(() -> new IllegalStateException("Utviklerfeil: Mangler tidligere oppdragsmelding"));
        Oppdrag110 nyOppdrag110 = opprettOppdrag110MedRelaterteOppdragsmeldinger(behandlingInfo, oppdragskontroll, sisteOppdr150Bruker);
        if (!endringstidspunktEtterSisteDatoIForrigeOppdrag) {
            List<Oppdragslinje150> tidligereOppdr150Liste = Oppdragslinje150Verktøy.hentTidligereOppdragslinje150(forrigeOppdrag, true, false);
            LocalDate datoStatusFom = oppdragskontrollOpphørFP.finnOpphørFomDato(tidligereOppdr150Liste, endringsdato);
            Oppdragslinje150 oppdragslinje150ForOpphør = oppdragskontrollOpphørFP.opprettOppdragslinje150ForStatusOPPH(behandlingInfo, sisteOppdr150Bruker, nyOppdrag110, datoStatusFom);
            opp150OpphList.add(oppdragslinje150ForOpphør);
        }
        Oppdrag110 forrigeOppdrag110 = sisteOppdr150Bruker.getOppdrag110();
        oppdragskontrollOpphørFP.opprettOppdr150LinjeForFeriepengerOPPH(behandlingInfo, opphFørEndringsoppdrFeriepg,
            opp150OpphList, nyOppdrag110, forrigeOppdrag110);
        opprettAttestant180(opp150OpphList, behandlingInfo.getAnsvarligSaksbehandler());
        return Optional.of(nyOppdrag110);
    }

    private void opprettOppdragForNyeMottakere(BehandlingInfoFP behandlingInfo, Oppdragskontroll oppdragskontroll,
                                               Map.Entry<Oppdragsmottaker, List<BeregningsresultatAndel>> entry,
                                               Oppdragsmottaker mottaker, long løpVerdiForFørstegangsoppdr) {
        boolean brukerErMottaker = mottaker.erBruker();
        long fagsystemId = settFagsystemId(behandlingInfo.getFagsak(), løpVerdiForFørstegangsoppdr, true);
        Oppdrag110 oppdrag110 = opprettNyOppdrag110(behandlingInfo, oppdragskontroll, fagsystemId, brukerErMottaker);
        List<BeregningsresultatAndel> andelListe = entry.getValue();
        List<Oppdragslinje150> oppdragslinje150List = opprettOppdragslinje150FP(behandlingInfo, oppdrag110, andelListe, mottaker);
        opprettAttestant180(oppdragslinje150List, behandlingInfo.getAnsvarligSaksbehandler());
    }

    private long finnInitialLøpenummerVerdi(Oppdragskontroll forrigeOppdrag) {
        return forrigeOppdrag.getOppdrag110Liste().stream()
            .map(Oppdrag110::getFagsystemId)
            .max(Comparator.comparing(Function.identity()))
            .orElseThrow(() -> new IllegalStateException("Utvikler feil: Forrige oppdrag mangler fagsystemId"));
    }

    private void lagOppdragForMottakereSomSkalOpphøre(BehandlingInfoFP behandlingInfo, Oppdragskontroll forrigeOppdrag, BeregningsresultatFP bgresultatFPOriginal, LocalDate endringsdato, Oppdragskontroll oppdragskontroll) {
        List<Oppdragsmottaker> oppdragsmottakerList = finnEndringerForMottakere(behandlingInfo, bgresultatFPOriginal);
        List<Oppdragsmottaker> ikkeMottakerLengerList = oppdragsmottakerList.stream()
            .filter(mottaker -> mottaker.getStatus() != null)
            .filter(mottaker -> mottaker.getStatus().equals(OppdragsmottakerStatus.OPPH)).collect(Collectors.toList());
        for (Oppdragsmottaker mottaker : ikkeMottakerLengerList) {
            if (mottaker.erBruker()) {
                lagOpphørsoppdragForBruker(behandlingInfo, forrigeOppdrag, endringsdato, oppdragskontroll, mottaker);
            } else {
                lagOpphørsoppdragForArbeidsgiver(behandlingInfo, forrigeOppdrag, endringsdato, oppdragskontroll, mottaker);
            }
        }
    }

    private void lagOpphørsoppdragForArbeidsgiver(BehandlingInfoFP behandlingInfo, Oppdragskontroll forrigeOppdrag, LocalDate endringsdato, Oppdragskontroll oppdragskontroll, Oppdragsmottaker mottaker) {
        List<Oppdragslinje150> sisteOppdr150ArbeidsgivereListe = Oppdragslinje150Verktøy.finnSisteLinjeIKjedeForArbeidsgivere(forrigeOppdrag);
        Optional<Oppdragslinje150> oppdr150 = sisteOppdr150ArbeidsgivereListe.stream()
            .filter(opp150 -> opp150.getRefusjonsinfo156().getRefunderesId().equals(endreTilElleveSiffer(mottaker.getId())))
            .findFirst();
        if (oppdr150.isPresent()) {
            Oppdragslinje150 oppdragslinje150 = oppdr150.get();
            lagOpphørsoppdragForArbeidsgiver(behandlingInfo, oppdragskontroll, endringsdato, mottaker, oppdragslinje150);
        }
    }

    private void lagOpphørsoppdragForBruker(BehandlingInfoFP behandlingInfo, Oppdragskontroll forrigeOppdrag, LocalDate endringsdato, Oppdragskontroll oppdragskontroll, Oppdragsmottaker mottaker) {
        boolean erDetFlereInntektskategoriBruker = finnesFlereKlassekodeIForrigeOppdrag(forrigeOppdrag, true);
        if (erDetFlereInntektskategoriBruker) {
            oppdragskontrollOpphørFP.opprettOpphørsoppdragForBrukerMedFlereKlassekode(behandlingInfo, oppdragskontroll, forrigeOppdrag, endringsdato, false);
        } else {
            lagOpphørsoppdragForBrukerMedEnKlassekode(behandlingInfo, forrigeOppdrag, oppdragskontroll, endringsdato, mottaker);
        }
    }

    private void lagOpphørsoppdragForArbeidsgiver(BehandlingInfoFP behandlingInfo, Oppdragskontroll oppdragskontroll, LocalDate endringsdato, Oppdragsmottaker mottaker, Oppdragslinje150 oppdragslinje150) {
        opprettOpphørIEndringsoppdragArbeidsgiver(behandlingInfo, oppdragskontroll, endringsdato, oppdragslinje150, mottaker, false);
    }

    private Optional<Oppdrag110> opprettOpphørIEndringsoppdragArbeidsgiver(BehandlingInfoFP behandlingInfo, Oppdragskontroll oppdragskontroll, LocalDate endringsdato, Oppdragslinje150 sisteOppdr150, Oppdragsmottaker mottaker, boolean opphFørEndringsoppdrFeriepg) {
        Oppdrag110 forrigeOppdrag110 = sisteOppdr150.getOppdrag110();
        Oppdragskontroll forrigeOppdrag = forrigeOppdrag110.getOppdragskontroll();
        boolean endringsdatoEtterSisteDatoAvAlleTidligereOppdrag = erEndringsdatoEtterSisteDatoAvAlleTidligereOppdrag(endringsdato, forrigeOppdrag, sisteOppdr150);
        if (endringsdatoEtterSisteDatoAvAlleTidligereOppdrag) {
            return Optional.empty();
        }
        List<Oppdragslinje150> opp150OpphList = new ArrayList<>();
        Oppdrag110 nyOppdrag110 = opprettOppdrag110MedRelaterteOppdragsmeldinger(behandlingInfo, oppdragskontroll, sisteOppdr150);
        boolean endringstidspunktEtterSisteDatoIForrigeOppdrag = erEndringstidspunktEtterSisteDatoIForrigeOppdrag(endringsdato, forrigeOppdrag, mottaker);
        if (!endringstidspunktEtterSisteDatoIForrigeOppdrag) {
            List<Oppdragslinje150> tidligereOpp150ListeForAlleArbgvr = Oppdragslinje150Verktøy.hentTidligereOppdragslinje150(forrigeOppdrag, false, false);
            List<Oppdragslinje150> tidligereOpp150ListeForDenneArbgvren = tidligereOpp150ListeForAlleArbgvr.stream().filter(opp150 -> opp150.getRefusjonsinfo156() != null)
                .filter(opp150 -> opp150.getRefusjonsinfo156().getRefunderesId().equals(endreTilElleveSiffer(mottaker.getId()))).collect(Collectors.toList());
            LocalDate datoStatusFom = oppdragskontrollOpphørFP.finnOpphørFomDato(tidligereOpp150ListeForDenneArbgvren, endringsdato);
            Oppdragslinje150 oppdragslinje150ForOpphør = oppdragskontrollOpphørFP.opprettOppdragslinje150ForStatusOPPH(behandlingInfo, sisteOppdr150, nyOppdrag110, datoStatusFom);
            opp150OpphList.add(oppdragslinje150ForOpphør);
        }
        if (!opphFørEndringsoppdrFeriepg || oppdragskontrollOpphørFP.skalFeriepengerOpphøres(behandlingInfo, forrigeOppdrag110, Optional.of(mottaker))) {
            List<Oppdragslinje150> nyOpp150FeriepengerListe = oppdragskontrollOpphørFP.opprettOppdr150LinjeForFeriepengerOPPH(behandlingInfo, nyOppdrag110, forrigeOppdrag110, false, opphFørEndringsoppdrFeriepg);
            opp150OpphList.addAll(nyOpp150FeriepengerListe);
        }
        oppdragskontrollOpphørFP.kobleAndreMeldingselementerTilOpp150Opphør(behandlingInfo, sisteOppdr150, opp150OpphList);
        return Optional.of(nyOppdrag110);
    }

    private boolean erEndringsdatoEtterSisteDatoAvAlleTidligereOppdrag(LocalDate endringsdato, Oppdragskontroll forrigeOppdrag, Oppdragslinje150 sisteOppdr150) {
        if (endringsdato != null) {
            List<Oppdragslinje150> tidligereOppdr150Liste = Oppdragslinje150Verktøy.hentTidligereOppdragslinje150(forrigeOppdrag, false, true);
            String refunderesId = sisteOppdr150.getRefusjonsinfo156().getRefunderesId();
            Optional<LocalDate> sisteDatoOpt = tidligereOppdr150Liste.stream()
                .filter(opp150 -> opp150.getRefusjonsinfo156().getRefunderesId().equals(refunderesId))
                .map(Oppdragslinje150::getDatoVedtakTom)
                .max(Comparator.comparing(Function.identity()));
            return sisteDatoOpt.map(endringsdato::isAfter).orElse(true);
        }
        return false;
    }

    private List<Oppdragsmottaker> finnEndringerForMottakere(BehandlingInfoFP behandlingInfo, BeregningsresultatFP bgResultatFPOriginal) {
        List<BeregningsresultatAndel> andelerOriginal = hentAndeler(bgResultatFPOriginal);
        List<BeregningsresultatAndel> andelerRevurdering = hentAndeler(behandlingInfo
            .getBeregningsresultatFP()
            .orElseThrow(() -> new IllegalStateException("BeregningsresultatFP kan ikke være null")));
        return finnStatusForMottakere(behandlingInfo, andelerOriginal, andelerRevurdering);
    }

    private List<Oppdragsmottaker> finnStatusForMottakere(BehandlingInfoFP behandlingInfo, List<BeregningsresultatAndel> andelerOriginal, List<BeregningsresultatAndel> andelerRevurdering) {
        List<Oppdragsmottaker> oppdragsmottakerList = new ArrayList<>();
        Optional<OppdragsmottakerStatus> statusBrukerOpt = finnStatusForMottakerBruker(andelerOriginal, andelerRevurdering);
        statusBrukerOpt.ifPresent(statusBruker -> {
            Oppdragsmottaker mottakerBruker = new Oppdragsmottaker(behandlingInfo.getPersonIdent().getIdent(), true);
            mottakerBruker.setStatus(statusBruker);
            oppdragsmottakerList.add(mottakerBruker);
        });
        List<Oppdragsmottaker> arbeidsgiverList = finnStatusForMottakerArbeidsgiver(andelerOriginal, andelerRevurdering);
        oppdragsmottakerList.addAll(arbeidsgiverList);

        return oppdragsmottakerList;
    }

    private Optional<OppdragsmottakerStatus> finnStatusForMottakerBruker(List<BeregningsresultatAndel> andelerOriginal, List<BeregningsresultatAndel> andelerRevurdering) {
        boolean eksistererBrukerMottakerIOriginal = andelerOriginal.stream().anyMatch(BeregningsresultatAndel::erBrukerMottaker);
        boolean eksistererBrukerMottakerIRevurdering = andelerRevurdering.stream().anyMatch(BeregningsresultatAndel::erBrukerMottaker);

        if (eksistererBrukerMottakerIOriginal && !eksistererBrukerMottakerIRevurdering) {
            return Optional.of(OppdragsmottakerStatus.OPPH);
        } else if (!eksistererBrukerMottakerIOriginal && eksistererBrukerMottakerIRevurdering) {
            return Optional.of(OppdragsmottakerStatus.NY);
        } else {
            return !eksistererBrukerMottakerIOriginal ? Optional.empty() : Optional.of(OppdragsmottakerStatus.UENDR);
        }
    }

    private List<Oppdragsmottaker> finnStatusForMottakerArbeidsgiver(List<BeregningsresultatAndel> andelerOriginal, List<BeregningsresultatAndel> andelerRevurdering) {

        List<Oppdragsmottaker> oppdragsmottakerList = new ArrayList<>();
        List<String> fjernetOrgnrIRevurd = new ArrayList<>();
        List<String> orgnrFraOriginalList = andelerOriginal.stream()
            .filter(andel -> !andel.erBrukerMottaker())
            .map(BeregningsresultatAndel::getArbeidsforholdOrgnr)
            .distinct()
            .collect(Collectors.toList());

        finnArbeidsgiverSomIkkeErMottakerLenger(andelerRevurdering, fjernetOrgnrIRevurd, orgnrFraOriginalList);
        gruppereArbeidsgivere(andelerOriginal, andelerRevurdering, oppdragsmottakerList);

        fjernetOrgnrIRevurd.forEach(orgnr -> {
            Oppdragsmottaker oppdragsmottaker = new Oppdragsmottaker(orgnr, false);
            oppdragsmottaker.setStatus(OppdragsmottakerStatus.OPPH);
            oppdragsmottakerList.add(oppdragsmottaker);
        });
        return oppdragsmottakerList;
    }

    private void finnArbeidsgiverSomIkkeErMottakerLenger(List<BeregningsresultatAndel> andelerRevurdering, List<String> fjernetOrgnrIRevurd, List<String> orgnrFraOriginalList) {
        for (String orgnrOriginal : orgnrFraOriginalList) {
            boolean finnesIkkeIRevur = andelerRevurdering.stream().filter(andel -> !andel.erBrukerMottaker())
                .noneMatch(bra -> bra.getArbeidsforholdOrgnr().equals(orgnrOriginal));
            if (finnesIkkeIRevur) {
                fjernetOrgnrIRevurd.add(orgnrOriginal);
            }
        }
    }

    private void gruppereArbeidsgivere(List<BeregningsresultatAndel> andelerOriginal, List<BeregningsresultatAndel> andelerRevurdering, List<Oppdragsmottaker> oppdragsmottakerList) {
        List<String> orgnrFraRevurdList = andelerRevurdering.stream().filter(andel -> !andel.erBrukerMottaker())
            .map(BeregningsresultatAndel::getArbeidsforholdOrgnr).distinct().collect(Collectors.toList());
        for (String orgnrRevur : orgnrFraRevurdList) {
            Oppdragsmottaker oppdragsmottaker = new Oppdragsmottaker(orgnrRevur, false);
            boolean finnesIOriginal = andelerOriginal.stream().filter(andel -> !andel.erBrukerMottaker()).anyMatch(bra -> bra.getArbeidsforholdOrgnr().equals(orgnrRevur));
            if (finnesIOriginal) {
                oppdragsmottaker.setStatus(OppdragsmottakerStatus.UENDR);
                oppdragsmottakerList.add(oppdragsmottaker);
            } else {
                oppdragsmottaker.setStatus(OppdragsmottakerStatus.NY);
                oppdragsmottakerList.add(oppdragsmottaker);
            }
        }
    }

    private Map<Oppdragsmottaker, List<BeregningsresultatAndel>> finnMottakereMedDeresAndelForEndringsoppdrag(BehandlingInfoFP behandlingInfo,
                                                                                                              List<BeregningsresultatAndel> andelerOriginal) {
        Map<Oppdragsmottaker, List<BeregningsresultatAndel>> andelPrMottakerMap = new LinkedHashMap<>();

        List<BeregningsresultatAndel> brukersAndelerListe = new ArrayList<>();
        List<BeregningsresultatAndel> arbeidsgiversAndelerListe = new ArrayList<>();
        List<BeregningsresultatAndel> alleAndelerListe = hentAndeler(behandlingInfo
            .getBeregningsresultatFP()
            .orElseThrow(() -> new IllegalStateException("BeregningsresultatFP kan ikke være null"))
        );

        skilleAndelerMellomArbeidsgiverOgBruker(brukersAndelerListe, arbeidsgiversAndelerListe, alleAndelerListe);
        List<BeregningsresultatPeriode> brPeriodeListe = behandlingInfo.getBeregningsresultatFP()
            .map(BeregningsresultatFP::getBeregningsresultatPerioder).orElse(Collections.emptyList());
        oppdragskontrollFørstegangFP.slåAndelerMedSammePeriodeOgKlassekodeSammen(brPeriodeListe, brukersAndelerListe, true);

        if (!brukersAndelerListe.isEmpty()) {
            Oppdragsmottaker mottakerBruker = new Oppdragsmottaker(behandlingInfo.getPersonIdent().getIdent(), true);
            Optional<OppdragsmottakerStatus> brukerStatusOpt = finnStatusForMottakerBruker(andelerOriginal, alleAndelerListe);
            OppdragsmottakerStatus brukerStatus = brukerStatusOpt.orElseThrow(() -> new IllegalStateException("Utvikler feil: Bruker mangler mottaker status"));
            mottakerBruker.setStatus(brukerStatus);
            andelPrMottakerMap.put(mottakerBruker, brukersAndelerListe);
        }
        if (!arbeidsgiversAndelerListe.isEmpty()) {
            Map<String, List<BeregningsresultatAndel>> groupedByOrgnr = arbeidsgiversAndelerListe.stream()
                .collect(Collectors.groupingBy(
                    BeregningsresultatAndel::getArbeidsforholdOrgnr,
                    LinkedHashMap::new,
                    Collectors.mapping(Function.identity(), Collectors.toList())));

            List<Oppdragsmottaker> mottakerArbeidsgiverList = finnStatusForMottakerArbeidsgiver(andelerOriginal, arbeidsgiversAndelerListe);
            for (Map.Entry<String, List<BeregningsresultatAndel>> entry : groupedByOrgnr.entrySet()) {
                Optional<Oppdragsmottaker> mottakerArbeidsgiverOpt = mottakerArbeidsgiverList.stream()
                    .filter(mottaker -> mottaker.getId().equals(entry.getKey()))
                    .findFirst();
                mottakerArbeidsgiverOpt.ifPresent(mottakerArbeidsgiver -> {
                        List<BeregningsresultatAndel> finalArbgvrAndelerListe = entry.getValue();
                        oppdragskontrollFørstegangFP.slåAndelerMedSammePeriodeOgKlassekodeSammen(brPeriodeListe, finalArbgvrAndelerListe, false);
                        andelPrMottakerMap.put(mottakerArbeidsgiver, finalArbgvrAndelerListe);
                    }
                );
            }
        }
        return andelPrMottakerMap;
    }
}
