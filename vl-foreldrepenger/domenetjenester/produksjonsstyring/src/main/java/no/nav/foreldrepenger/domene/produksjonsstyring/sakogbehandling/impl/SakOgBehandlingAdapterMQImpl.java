package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.impl;

import static no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.impl.SakOgBehandlingFeil.FACTORY;

import java.time.LocalDate;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.AvsluttetBehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.OpprettetBehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.SakOgBehandlingAdapter;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.Aktoer;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.Applikasjoner;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.Avslutningsstatuser;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.Behandlingstemaer;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.Behandlingstyper;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.PrimaerBehandling;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.PrimaerRelasjonstyper;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.Sakstemaer;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.sakogbehandling.SakOgBehandlingClient;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.util.FPDateUtil;

@Dependent
class SakOgBehandlingAdapterMQImpl implements SakOgBehandlingAdapter {

    private SakOgBehandlingClient sakOgBehandlingClient;

    private String applicationName;

    private static final String PRIMÆR_RELASJONSTYPE = "forrige"; //Er fra kodeverk: http://nav.no/kodeverk/Kode/Prim_c3_a6rRelasjonstyper/forrige?v=1
    private static final Fagsystem fpsak = Fagsystem.FPSAK;

    @Inject
    public SakOgBehandlingAdapterMQImpl(
        SakOgBehandlingClient sakOgBehandlingClient) {

        this.sakOgBehandlingClient = sakOgBehandlingClient;
        this.applicationName = fpsak.getOffisiellKode();
    }

    private String createUniqueBehandlingsId(String behandlingsId) {
        return String.format("%s_%s", applicationName, behandlingsId);

    }

    @Override
    public void behandlingOpprettet(OpprettetBehandlingStatus opprettetBehandlingStatus) {

        no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.BehandlingOpprettet behandlingOpprettet = new no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.BehandlingOpprettet();
        //BehandlingsId i S&B skal være unik i nav. Så vi prefikser med applikasjonsid.
        behandlingOpprettet.setBehandlingsID(createUniqueBehandlingsId(opprettetBehandlingStatus.getBehandlingsId()));

        Behandlingstemaer behandlingstema = new Behandlingstemaer();
        behandlingstema.setValue(opprettetBehandlingStatus.getBehandlingsTemaKode());
        behandlingOpprettet.setBehandlingstema(behandlingstema);

        String callId = MDCOperations.getCallId();
        behandlingOpprettet.setHendelsesId(callId);

        Applikasjoner applikasjoner = new Applikasjoner();
        applikasjoner.setValue(applicationName);
        behandlingOpprettet.setHendelsesprodusentREF(applikasjoner);

        behandlingOpprettet.setHendelsesTidspunkt(gregDate(opprettetBehandlingStatus.getHendelsesTidspunkt()));

        Behandlingstyper behandlingstype = new Behandlingstyper();
        behandlingstype.setValue(opprettetBehandlingStatus.getBehandlingsTypeKode());
        behandlingOpprettet.setBehandlingstype(behandlingstype);

        if (opprettetBehandlingStatus.getPrimaerBehandlingsRef() != null) {
            PrimaerBehandling primaerBehandling = new PrimaerBehandling();
            primaerBehandling.setBehandlingsREF(opprettetBehandlingStatus.getPrimaerBehandlingsRef()); //Legger til knytning mellom behandlinger.
            PrimaerRelasjonstyper primaerRelasjonstyper = new PrimaerRelasjonstyper();
            primaerRelasjonstyper.setValue(PRIMÆR_RELASJONSTYPE);
            behandlingOpprettet.setPrimaerBehandlingREF(primaerBehandling);
        }

        Sakstemaer sakstema = new Sakstemaer();
        sakstema.setValue(opprettetBehandlingStatus.getSakstemaKode());
        behandlingOpprettet.setSakstema(sakstema);

        Aktoer aktoer = new Aktoer();
        aktoer.setAktoerId(opprettetBehandlingStatus.getAktørId());
        behandlingOpprettet.getAktoerREF().add(aktoer);

        behandlingOpprettet.setAnsvarligEnhetREF(opprettetBehandlingStatus.getAnsvarligEnhetRef());

        sakOgBehandlingClient.sendBehandlingOpprettet(behandlingOpprettet);
    }

    @Override
    public void behandlingAvsluttet(AvsluttetBehandlingStatus avsluttetBehandlingStatus) {

        no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.BehandlingAvsluttet behandlingAvsluttet = new no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.BehandlingAvsluttet();

        behandlingAvsluttet.setBehandlingsID(createUniqueBehandlingsId(avsluttetBehandlingStatus.getBehandlingsId()));

        String callId = MDCOperations.getCallId();
        behandlingAvsluttet.setHendelsesId(callId);

        Applikasjoner applikasjoner = new Applikasjoner();
        applikasjoner.setValue(applicationName);
        behandlingAvsluttet.setHendelsesprodusentREF(applikasjoner);

        Avslutningsstatuser avslutningsstatus = new Avslutningsstatuser();

        // TODO (GS) hardkodingen fjernes i PKHUMLE-458
        avslutningsstatus.setValue("ok");
        behandlingAvsluttet.setAvslutningsstatus(avslutningsstatus);

        behandlingAvsluttet.setHendelsesTidspunkt(gregDate(LocalDate.now(FPDateUtil.getOffset())));

        Aktoer aktoer = new Aktoer();
        aktoer.setAktoerId(avsluttetBehandlingStatus.getAktørId());
        behandlingAvsluttet.getAktoerREF().add(aktoer);

        behandlingAvsluttet.setAnsvarligEnhetREF(avsluttetBehandlingStatus.getAnsvarligEnhetRef());

        Behandlingstyper behandlingstype = new Behandlingstyper();
        behandlingstype.setValue(avsluttetBehandlingStatus.getBehandlingsTypeKode());
        behandlingAvsluttet.setBehandlingstype(behandlingstype);

        Sakstemaer sakstema = new Sakstemaer();
        sakstema.setValue(avsluttetBehandlingStatus.getSakstemaKode());
        behandlingAvsluttet.setSakstema(sakstema);

        sakOgBehandlingClient.sendBehandlingAvsluttet(behandlingAvsluttet);
    }

    private XMLGregorianCalendar gregDate(LocalDate localDate) {
        try {
            return DateUtil.convertToXMLGregorianCalendar(localDate);
        } catch (DatatypeConfigurationException e) {
            throw FACTORY.xmlGregorianCalendarParsingFeil(e).toException();
        }
    }
}
