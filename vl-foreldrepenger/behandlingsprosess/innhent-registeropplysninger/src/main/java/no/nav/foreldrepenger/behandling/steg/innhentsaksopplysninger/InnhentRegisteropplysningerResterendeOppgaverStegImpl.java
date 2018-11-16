package no.nav.foreldrepenger.behandling.steg.innhentsaksopplysninger;


import static java.util.Arrays.asList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.vedtak.util.FPDateUtil;


@BehandlingStegRef(kode = "INREG_AVSL")
@BehandlingTypeRef
@FagsakYtelseTypeRef
@ApplicationScoped
public class InnhentRegisteropplysningerResterendeOppgaverStegImpl implements BehandlingSteg {

    private BehandlingRepository behandlingRepository;
    private FagsakTjeneste fagsakTjeneste;
    private PersonopplysningTjeneste personopplysningTjeneste;

    InnhentRegisteropplysningerResterendeOppgaverStegImpl() {
        // for CDI proxy
    }

    @Inject
    public InnhentRegisteropplysningerResterendeOppgaverStegImpl(BehandlingRepositoryProvider repositoryProvider,
                                                                 FagsakTjeneste fagsakTjeneste,
                                                                 PersonopplysningTjeneste personopplysningTjeneste) {

        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.fagsakTjeneste = fagsakTjeneste;
        this.personopplysningTjeneste = personopplysningTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        long behandlingId = kontekst.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        return BehandleStegResultat.utførtMedAksjonspunkter(sjekkPersonstatus(behandling));

    }

    // TODO(OJR) flytte denne til egen utleder?
    private List<AksjonspunktDefinisjon> sjekkPersonstatus(Behandling behandling) {
        List<PersonstatusType> liste = asList(PersonstatusType.BOSA, PersonstatusType.DØD, PersonstatusType.UTVA);

        PersonopplysningerAggregat personopplysninger = personopplysningTjeneste.hentPersonopplysninger(behandling);

        List<AksjonspunktDefinisjon> aksjonspunktDefinisjoner = new ArrayList<>();
        for (Personstatus personstatus : personopplysninger.getPersonstatuserFor(behandling.getAktørId())) {
            if (!liste.contains(personstatus.getPersonstatus())) {
                aksjonspunktDefinisjoner.add(AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS);
                break; // Trenger ikke loope mer når vi får aksjonspunkt
            }
        }

        if (erSøkerUnder18ar(behandling)) {
            aksjonspunktDefinisjoner.add(AksjonspunktDefinisjon.AVKLAR_VERGE);
        }
        return aksjonspunktDefinisjoner;
    }

    private boolean erSøkerUnder18ar(Behandling behandling) {
        PersonopplysningerAggregat personopplysninger = personopplysningTjeneste.hentPersonopplysninger(behandling);
        Personopplysning søker = personopplysninger.getSøker();
        return søker.getFødselsdato().isAfter(LocalDate.now(FPDateUtil.getOffset()).minusYears(18));
    }

}

