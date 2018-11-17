package no.nav.foreldrepenger.vedtak.xml;

import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Tema;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.vedtak.v2.VedtakConstants;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.BehandlingsresultatXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.oppdrag.OppdragXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.personopplysninger.PersonopplysningXmlTjeneste;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;
import no.nav.vedtak.felles.xml.felles.v2.KodeverksOpplysning;
import no.nav.vedtak.felles.xml.vedtak.v2.FagsakType;
import no.nav.vedtak.felles.xml.vedtak.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.vedtak.v2.Vedtak;

public abstract class VedtakXmlTjeneste {

    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;
    private ObjectFactory factory;
    private PersonopplysningXmlTjeneste personopplysningXmlTjeneste;
    private BehandlingsresultatXmlTjeneste behandlingsresultatXmlTjeneste;
    private SøknadRepository søknadRepository;
    private KodeverkRepository kodeverkRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;

    private Optional<OppdragXmlTjeneste> oppdragXmlTjeneste = Optional.empty();

    VedtakXmlTjeneste() {
        // for CDI proxy
    }

    public VedtakXmlTjeneste(BehandlingRepositoryProvider repositoryProvider, PersonopplysningXmlTjeneste personopplysningXmlTjeneste,
                             BehandlingsresultatXmlTjeneste behandlingsresultatXmlTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.personopplysningXmlTjeneste = personopplysningXmlTjeneste;
        this.behandlingsresultatXmlTjeneste = behandlingsresultatXmlTjeneste;
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        this.factory = new ObjectFactory();
    }

    public void setOppdragXmlTjeneste(OppdragXmlTjeneste oppdragXmlTjeneste) {
        this.oppdragXmlTjeneste = Optional.of(oppdragXmlTjeneste);
    }

    public void setPersonopplysningXmlTjeneste(PersonopplysningXmlTjeneste personopplysningXmlTjeneste) {
        this.personopplysningXmlTjeneste = personopplysningXmlTjeneste;
    }

    public String opprettVedtakXml(Long behandlingId) {
        Vedtak vedtak = fraBehandling(behandlingId);
        try {
            return JaxbHelper.marshalJaxb(VedtakConstants.JAXB_CLASS,
                vedtak,
                VedtakConstants.ADDITIONAL_CLASSES);
        } catch (JAXBException | SAXException e) {
            throw FeilFactory.create(VedtakXmlFeil.class).serialiseringsfeil(behandlingId, e).toException();
        }
    }

    public Vedtak fraBehandling(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Fagsak fagsak = fagsakRepository.finnEksaktFagsak(behandling.getFagsakId());
        Vedtak vedtak = factory.createVedtak();

        setFagsakId(vedtak, fagsak);
        setTema(vedtak);
        setFagsakType(vedtak, fagsak);
        setVedtaksdato(behandling, vedtak);
        setAnsvarligBeslutterIdent(vedtak, behandling);
        setAnsvarligSaksbehandlerIdent(vedtak, behandling);
        setBehandlendeEnhet(vedtak, behandling);
        setBehandlingsTema(vedtak, behandling);
        setKlagedato(vedtak, behandling);
        setSøknadsdato(vedtak, behandling);
        setVedtaksResultat(vedtak, behandling);
        personopplysningXmlTjeneste.setPersonopplysninger(vedtak, behandling);
        behandlingsresultatXmlTjeneste.setBehandlingresultat(vedtak, behandling);

        // Sett inn spesifikke elementer fra hver subklasse
        leggTilOptionalElementerPåVedtak(vedtak, behandling);
        return vedtak;
    }

    public void leggTilOptionalElementerPåVedtak(Vedtak vedtak, Behandling behandling) {
        oppdragXmlTjeneste.ifPresent(oppdragTjeneste -> oppdragTjeneste.setOppdrag(vedtak, behandling));
    }

    private void setTema(Vedtak vedtak) {
        Kodeliste temaKode = kodeverkRepository.finn(Tema.class, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        vedtak.setTema(VedtakXmlUtil.lagKodeverksOpplysning(temaKode));
    }

    private void setSøknadsdato(Vedtak vedtak, Behandling behandling) {
        Optional<Søknad> søknadOptional = søknadRepository.hentSøknadHvisEksisterer(behandling);
        if (søknadOptional.isPresent()) {
            Søknad søknad = søknadOptional.get();
            vedtak.setSoeknadsdato(VedtakXmlUtil.tilCalendar(søknad.getSøknadsdato()));
        }
    }

    private void setKlagedato(Vedtak vedtak, Behandling behandling) {
        if (BehandlingType.KLAGE.equals(behandling.getType())) {
            vedtak.setKlagedato(VedtakXmlUtil.tilCalendar(behandling.getOpprettetDato().toLocalDate()));
        }
    }

    private void setBehandlingsTema(Vedtak vedtak, Behandling behandling) {
        Kodeliste behandlingTema = kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.fraFagsak(behandling.getFagsak()));
        vedtak.setBehandlingsTema(VedtakXmlUtil.lagKodeverksOpplysning(behandlingTema));
    }

    private void setBehandlendeEnhet(Vedtak vedtak, Behandling behandling) {
        vedtak.setBehandlendeEnhet(behandling.getBehandlendeEnhet());
    }

    private void setAnsvarligSaksbehandlerIdent(Vedtak vedtak, Behandling behandling) {
        vedtak.setAnsvarligSaksbehandlerIdent(behandling.getAnsvarligSaksbehandler());
    }

    private void setAnsvarligBeslutterIdent(Vedtak vedtak, Behandling behandling) {
        if (behandling.getAnsvarligBeslutter() != null) {
            vedtak.setAnsvarligBeslutterIdent(behandling.getAnsvarligBeslutter());
        }
    }

    void setFagsakType(Vedtak vedtak, Fagsak fagsak) {
        KodeverksOpplysning kodeverksOpplysning = new KodeverksOpplysning();
        if ((FagsakYtelseType.FORELDREPENGER.equals(fagsak.getYtelseType()))) {
            kodeverksOpplysning.setValue(FagsakType.FORELDREPENGER.value());
        }
        vedtak.setFagsakType(kodeverksOpplysning);
    }

    private void setFagsakId(Vedtak vedtak, Fagsak fagsak) {
        vedtak.setFagsakId(fagsak.getId().toString());
    }

    private void setVedtaksdato(Behandling behandling, Vedtak vedtakKontrakt) {
        Optional<BehandlingVedtak> vedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId());
        vedtak.ifPresent(v -> vedtakKontrakt.setVedtaksdato(VedtakXmlUtil.tilCalendar(v.getVedtaksdato())));
    }

    private void setVedtaksResultat(Vedtak vedtak, Behandling behandling) {
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        BehandlingVedtak behandlingVedtak = behandlingsresultat.getBehandlingVedtak();
        if (Objects.nonNull(behandlingVedtak)) {
            vedtak.setVedtaksresultat(VedtakXmlUtil.lagKodeverksOpplysning(behandlingVedtak.getVedtakResultatType()));
        }
    }
}
