package no.nav.foreldrepenger.økonomistøtte;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.OppdragKvittering;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.økonomistøtte.api.ØkonomiKvittering;
import no.nav.foreldrepenger.økonomistøtte.api.ØkonomioppdragApplikasjonTjeneste;
import no.nav.vedtak.felles.AktiverContextOgTransaksjon;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHendelseMottak;

@ApplicationScoped
@AktiverContextOgTransaksjon
public class ØkonomioppdragApplikasjonTjenesteImpl implements ØkonomioppdragApplikasjonTjeneste {

    private OppdragskontrollTjeneste oppdragskontrollTjeneste;
    private ProsessTaskHendelseMottak hendelsesmottak;
    private ØkonomioppdragRepository økonomioppdragRepository;

    private static final Logger log = LoggerFactory.getLogger(ØkonomioppdragApplikasjonTjenesteImpl.class);

    ØkonomioppdragApplikasjonTjenesteImpl() {
        // for CDI
    }

    @Inject
    public ØkonomioppdragApplikasjonTjenesteImpl(OppdragskontrollTjeneste oppdragskontrollTjeneste,
                                                 ProsessTaskHendelseMottak hendelsesmottak,
                                                 ØkonomioppdragRepository økonomioppdragRepository) {
        this.oppdragskontrollTjeneste = oppdragskontrollTjeneste;
        this.hendelsesmottak = hendelsesmottak;
        this.økonomioppdragRepository = økonomioppdragRepository;
    }

    @Override
    public void utførOppdrag(Long behandlingId, Long ventendeTaskId, boolean skalOppdragSendesTilØkonomi) {

        Long oppdragskontrollId = oppdragskontrollTjeneste.opprettOppdrag(behandlingId, ventendeTaskId);

        if (skalOppdragSendesTilØkonomi) {
            Oppdragskontroll oppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragskontrollId);
            //Opprette oppdragsmelding til Økonomiløsningen
            ØkonomioppdragMapper mapper = new ØkonomioppdragMapper(oppdrag);
            List<String> oppdragXMLListe = mapper.generateOppdragXML();
            //Legge oppdragXML i kø til Økonomiløsningen
            for (String oppdragXML : oppdragXMLListe) {
                log.warn("Skulle sendt oppdrag til økonomi \n" + oppdragXML);
            }
        }
    }

    @Override
    public void behandleKvittering(ØkonomiKvittering kvittering) {

        log.info("Behandler økonomikvittering med resultatkode: {} i behandling: {}",
            kvittering.getAlvorlighetsgrad(), kvittering.getBehandlingId()); //$NON-NLS-1$
        //Korrelere med lagret oppdrag
        Oppdragskontroll oppdrag = økonomioppdragRepository.finnVentendeOppdrag(kvittering.getBehandlingId());

        // oppdatere status i Økonomioppdrag-datalager
        List<Oppdrag110> okoOppdrag110Liste = oppdrag.getOppdrag110Liste();

        Oppdrag110 okoOppdrag110 = okoOppdrag110Liste.stream()
            .filter(oppdr110 -> oppdr110.getFagsystemId() == kvittering.getFagsystemId()).findFirst()
            .orElseThrow(() -> new IllegalStateException("Finnes ikke oppdrag for kvittering med fagsystemId: " + kvittering.getFagsystemId()));

        OppdragKvittering oppdragKvittering = new OppdragKvittering();
        oppdragKvittering.setAlvorlighetsgrad(kvittering.getAlvorlighetsgrad());
        oppdragKvittering.setMeldingKode(kvittering.getMeldingKode());
        oppdragKvittering.setBeskrMelding(kvittering.getBeskrMelding());
        oppdragKvittering.setOppdrag110(okoOppdrag110);
        okoOppdrag110.getOppdragKvitteringListe().add(oppdragKvittering);

        Boolean erAlleKvitteringerMottatt = sjekkAlleKvitteringMottatt(okoOppdrag110Liste);

        if (erAlleKvitteringerMottatt) {
            log.info("Alle økonomioppdrag-kvitteringer er mottatt for behandling: {}", kvittering.getBehandlingId());
            oppdrag.setVenterKvittering(false);
            //Dersom kvittering viser positivt resultat: La Behandlingskontroll/TaskManager fortsette behandlingen - trigger prosesstask Behandling.Avslutte hvis brev er bekreftet levert
            Boolean alleViserPositivtResultat = erAlleKvitteringerMedPositivtResultat(oppdrag);
            if (alleViserPositivtResultat) {
                log.info("Alle økonomioppdrag-kvitteringer viser positivt resultat for behandling: {}", kvittering.getBehandlingId());
                hendelsesmottak.mottaHendelse(oppdrag.getProsessTaskId(), ProsessTaskHendelse.ØKONOMI_OPPDRAG_KVITTERING);
            } else {
                log.warn("Ikke alle økonomioppdrag-kvitteringer viser positivt resultat for behandling: {}", kvittering.getBehandlingId());
            }
        }
        økonomioppdragRepository.lagre(oppdrag);
    }

    private boolean sjekkAlleKvitteringMottatt(List<Oppdrag110> oppdrag110Liste) {
        return oppdrag110Liste.stream().noneMatch(opp110 -> opp110.getOppdragKvitteringListe().isEmpty());
    }

    private boolean erAlleKvitteringerMedPositivtResultat(Oppdragskontroll oppdrag) {

        return oppdrag.getOppdrag110Liste().stream().flatMap(o -> o.getOppdragKvitteringListe().stream())
            .allMatch(o -> Integer.parseInt(o.getAlvorlighetsgrad()) < 5);
    }
}
