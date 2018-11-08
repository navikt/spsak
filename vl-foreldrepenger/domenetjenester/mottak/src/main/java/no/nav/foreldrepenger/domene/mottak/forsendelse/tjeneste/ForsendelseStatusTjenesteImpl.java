package no.nav.foreldrepenger.domene.mottak.forsendelse.tjeneste;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.domene.mottak.forsendelse.ForsendelseIdDto;
import no.nav.foreldrepenger.domene.mottak.forsendelse.ForsendelseStatus;
import no.nav.foreldrepenger.domene.mottak.forsendelse.ForsendelseStatusDataDTO;

@ApplicationScoped
public class ForsendelseStatusTjenesteImpl implements ForsendelseStatusTjeneste {

    private MottatteDokumentRepository mottatteDokumentRepository;

    private BehandlingRepository behandlingRepository;

    public ForsendelseStatusTjenesteImpl() {
        // FOR CDI
    }

    @Inject
    public ForsendelseStatusTjenesteImpl(MottatteDokumentRepository mottatteDokumentRepository, BehandlingRepository behandlingRepository) {
        this.mottatteDokumentRepository = mottatteDokumentRepository;
        this.behandlingRepository = behandlingRepository;
    }

    @Override
    public ForsendelseStatusDataDTO getStatusInformasjon(ForsendelseIdDto forsendelseIdDto) {
        UUID forsendelseId = forsendelseIdDto.getForsendelseId();
        List<MottattDokument> mottattDokumentList = mottatteDokumentRepository.hentMottatteDokumentMedForsendelseId(forsendelseId);

        if (mottattDokumentList == null || mottattDokumentList.isEmpty()) {
            throw ForsendelseStatusFeil.FACTORY.finnesIkkeMottatDokument(forsendelseId).toException();
        } else if (mottattDokumentList.size() != 1) {
            throw ForsendelseStatusFeil.FACTORY.flereMotattDokument(forsendelseId).toException();
        }
        MottattDokument mottattDokument = mottattDokumentList.get(0);
        Behandling behandling = behandlingRepository.hentBehandling(mottattDokument.getBehandlingId());
        ForsendelseStatusDataDTO forsendelseStatusDataDTO = getForsendelseStatusDataDTO(behandling, forsendelseId);
        return forsendelseStatusDataDTO;
    }

    private ForsendelseStatusDataDTO getForsendelseStatusDataDTO(Behandling behandling, UUID forsendelseId) {
        ForsendelseStatusDataDTO forsendelseStatusDataDTO;
        BehandlingStatus behandlingStatus = behandling.getStatus();
        if (behandlingStatus.equals(BehandlingStatus.AVSLUTTET) || behandlingStatus.equals(BehandlingStatus.IVERKSETTER_VEDTAK)) {
            BehandlingResultatType resultat = behandling.getBehandlingsresultat().getBehandlingResultatType();
            if(resultat.equals(BehandlingResultatType.INNVILGET)) {
                forsendelseStatusDataDTO = new ForsendelseStatusDataDTO(ForsendelseStatus.INNVLIGET);
            } else if(resultat.equals(BehandlingResultatType.AVSLÅTT)) {
                forsendelseStatusDataDTO = new ForsendelseStatusDataDTO(ForsendelseStatus.AVSLÅTT);
            } else {
                throw ForsendelseStatusFeil.FACTORY.ugyldigBehandlingResultat(forsendelseId).toException();
            }

        } else {
            List<Aksjonspunkt> aksjonspunkt = behandling.getÅpneAksjonspunkter();
            if (aksjonspunkt.isEmpty()) {
                forsendelseStatusDataDTO = new ForsendelseStatusDataDTO(ForsendelseStatus.PÅGÅR);
            } else {
                forsendelseStatusDataDTO = new ForsendelseStatusDataDTO(ForsendelseStatus.PÅ_VENT);
            }
        }
        return forsendelseStatusDataDTO;
    }

}
