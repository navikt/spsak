package no.nav.foreldrepenger.datavarehus.tjeneste;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.datavarehus.BehandlingDvh;
import no.nav.vedtak.util.FPDateUtil;

public class BehandlingDvhMapper {

    private static final ArrayList<BehandlingResultatType> AVBRUTT_BEHANDLINGSRESULTAT = new ArrayList<>();

    static {
        AVBRUTT_BEHANDLINGSRESULTAT.add(BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET);
        AVBRUTT_BEHANDLINGSRESULTAT.add(BehandlingResultatType.HENLAGT_FEILOPPRETTET);
        AVBRUTT_BEHANDLINGSRESULTAT.add(BehandlingResultatType.HENLAGT_BRUKER_DØD);
        AVBRUTT_BEHANDLINGSRESULTAT.add(BehandlingResultatType.MERGET_OG_HENLAGT);
        AVBRUTT_BEHANDLINGSRESULTAT.add(BehandlingResultatType.HENLAGT_SØKNAD_MANGLER);
    }

    private static final String UTENLANDSTILSNITT = "NASJONAL";

    public BehandlingDvh map(Behandling behandling, Optional<BehandlingVedtak> vedtak) {
        return BehandlingDvh.builder()
            .ansvarligBeslutter(behandling.getAnsvarligBeslutter())
            .ansvarligSaksbehandler(behandling.getAnsvarligSaksbehandler())
            .behandlendeEnhet(behandling.getBehandlendeEnhet())
            .behandlingId(behandling.getId())
            .behandlingResultatType(finnBehandlingResultatType(behandling))
            .behandlingStatus(behandling.getStatus().getKode())
            .behandlingType(behandling.getType().getKode())
            .endretAv(CommonDvhMapper.finnEndretAvEllerOpprettetAv(behandling))
            .fagsakId(behandling.getFagsakId())
            .funksjonellTid(FPDateUtil.nå())
            .opprettetDato(behandling.getOpprettetDato().toLocalDate())
            .utlandstilsnitt(UTENLANDSTILSNITT)
            .toTrinnsBehandling(behandling.isToTrinnsBehandling())
            .vedtakId(vedtak.map(BehandlingVedtak::getId).orElse(null))
            .relatertBehandling(behandling.getOriginalBehandling().map(o -> o.getId()).orElse(null))
            .ferdig(mapFerdig(behandling))
            .vedtatt(mapVedtatt(behandling))
            .avbrutt(mapAvbrutt(behandling))
            .build();
    }

    private boolean mapAvbrutt(Behandling behandling) {
        if (FagsakStatus.AVSLUTTET.equals(behandling.getFagsak().getStatus())) {
            Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
            if (Objects.nonNull(behandlingsresultat)) {
                return AVBRUTT_BEHANDLINGSRESULTAT.stream().anyMatch(type -> type.equals(behandlingsresultat.getBehandlingResultatType()));
            }
        }
        return false;
    }

    private boolean mapVedtatt(Behandling behandling) {
        if (Objects.nonNull(behandling.getBehandlingsresultat())) {
            BehandlingResultatType behandlingResultatType = behandling.getBehandlingsresultat().getBehandlingResultatType();

            if (FagsakStatus.AVSLUTTET.equals(behandling.getFagsak().getStatus())) {
                return BehandlingResultatType.AVSLÅTT.equals(behandlingResultatType);
            } else if (FagsakStatus.LØPENDE.equals(behandling.getFagsak().getStatus())) {
                return BehandlingResultatType.INNVILGET.equals(behandlingResultatType);
            }
        }
        return false;
    }

    private boolean mapFerdig(Behandling behandling) {
        return FagsakStatus.AVSLUTTET.equals(behandling.getFagsak().getStatus());
    }

    private String finnBehandlingResultatType(Behandling b) {
        Optional<Behandling> ob = Optional.ofNullable(b);
        return ob.map(Behandling::getBehandlingsresultat).map(Behandlingsresultat::getBehandlingResultatType).map(BehandlingResultatType::getKode)
            .orElse(null);
    }
}
