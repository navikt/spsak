package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.Årsak;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.BekreftetUttakPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.SlettetUttakPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakDokumentasjonDto;

@ApplicationScoped
public class FaktaUttakToTrinnsTjenesteImpl implements FaktaUttakToTrinnsTjeneste {

    private AksjonspunktRepository aksjonspunktRepository;

    FaktaUttakToTrinnsTjenesteImpl() {
        //For CDI proxy
    }

    @Inject
    public FaktaUttakToTrinnsTjenesteImpl(BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.aksjonspunktRepository = behandlingRepositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public void oppdaterTotrinnskontrollVedEndringerFaktaUttak(AvklarFaktaUttakDto dto, Behandling behandling) {
        if(erDetEndringer(dto.getSlettedePerioder(), dto.getBekreftedePerioder())) {
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode()));
        }
    }

    private boolean erDetEndringer(List<SlettetUttakPeriodeDto> slettedePerioder,  List<BekreftetUttakPeriodeDto> bekreftedePerioder) {
        return !slettedePerioder.isEmpty() || harEndringerPåPerioder(bekreftedePerioder);
    }

    private boolean harEndringerPåPerioder(List<BekreftetUttakPeriodeDto> bekreftedePerioder) {
        List<BekreftetUttakPeriodeDto> alleEndredePerioder = bekreftedePerioder
            .stream().filter(p -> isNotBlank(p.getBekreftetPeriode().getBegrunnelse()))
            .collect(Collectors.toList());

        List<Årsak> årsakerInnleggelse = Arrays.asList(UtsettelseÅrsak.INSTITUSJON_SØKER, UtsettelseÅrsak.INSTITUSJON_BARN);
        List<BekreftetUttakPeriodeDto> innleggelsePerioder = alleEndredePerioder.stream()
            .filter(p -> p.getBekreftetPeriode().getUtsettelseÅrsak() != null && årsakerInnleggelse.contains(p.getBekreftetPeriode().getUtsettelseÅrsak()))
            .collect(Collectors.toList());

        if (alleEndredePerioder.size() != innleggelsePerioder.size()) {
            return true;
        }

        for (BekreftetUttakPeriodeDto periode : innleggelsePerioder) {
            List<UttakDokumentasjonDto> dokumentertePerioder = periode.getBekreftetPeriode().getDokumentertePerioder();
            if (dokumentertePerioder == null || dokumentertePerioder.isEmpty() || erNyPeriode(periode)) {
                return true;
            }

            if (erHelePeriodenDokumentert(periode, dokumentertePerioder)) {
                return false;
            }
        }
        return true;
    }

    private boolean erHelePeriodenDokumentert(BekreftetUttakPeriodeDto periode, List<UttakDokumentasjonDto> dokumentertePerioder) {
        List<UttakDokumentasjonDto> sortertDokPerioder = dokumentertePerioder.stream()
            .sorted(Comparator.comparing(UttakDokumentasjonDto::getFom))
            .collect(Collectors.toList());

        LocalDate startDato = periode.getBekreftetPeriode().getFom();
        for (UttakDokumentasjonDto dokumentasjon : sortertDokPerioder) {
            if (!dokumentasjon.getFom().isAfter(startDato)) {
                startDato = dokumentasjon.getTom();
            }
            if (!startDato.isBefore(periode.getBekreftetPeriode().getTom())) {
                return true;
            }
        }
        return false;
    }

    private boolean erNyPeriode(BekreftetUttakPeriodeDto bkftUttakPeriodeDto) {
        return bkftUttakPeriodeDto.getOrginalFom() == null && bkftUttakPeriodeDto.getOrginalTom() == null;
    }
}
