package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.søknadsfrist.SøknadsfristForeldrepengerTjeneste;
import no.nav.foreldrepenger.behandling.søknadsfrist.VurderSøknadsfristAksjonspunktDto;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderSøknadsfristDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurderSøknadsfristDto.class, adapter = AksjonspunktOppdaterer.class)
public class VurderSøknadsfristOppdaterer implements AksjonspunktOppdaterer<VurderSøknadsfristDto> {

    private SøknadsfristForeldrepengerTjeneste søknadsfristTjeneste;
    private HistorikkTjenesteAdapter historikkAdapter;
    private BehandlingRepositoryProvider repositoryProvider;


    public VurderSøknadsfristOppdaterer() {
        // for CDI proxy
    }


    @Inject
    public VurderSøknadsfristOppdaterer(SøknadsfristForeldrepengerTjeneste søknadsfristTjeneste,
                                        HistorikkTjenesteAdapter historikkAdapter, BehandlingRepositoryProvider repositoryProvider) {
        this.søknadsfristTjeneste = søknadsfristTjeneste;
        this.historikkAdapter = historikkAdapter;
        this.repositoryProvider = repositoryProvider;
    }

    @Override
    public OppdateringResultat oppdater(VurderSøknadsfristDto dto, Behandling behandling) {
        Søknad søknad = repositoryProvider.getSøknadRepository().hentSøknad(behandling);
        LocalDate søknadensMottatDato = søknad.getMottattDato();

        opprettHistorikkinnslag(dto, behandling, søknad);

        VurderSøknadsfristAksjonspunktDto adapter = new VurderSøknadsfristAksjonspunktDto(
            dto.harGyldigGrunn() ? dto.getAnsesMottattDato() : søknadensMottatDato,
            dto.getBegrunnelse());
        søknadsfristTjeneste.lagreVurderSøknadsfristResultat(behandling, adapter);
        return OppdateringResultat.utenOveropp();
    }

    private void opprettHistorikkinnslag(VurderSøknadsfristDto dto, Behandling behandling, Søknad søknad) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = repositoryProvider.getAksjonspunktRepository().finnAksjonspunktDefinisjon(dto.getKode());

        HistorikkInnslagTekstBuilder tekstBuilder = historikkAdapter.tekstBuilder()
            .medSkjermlenke(aksjonspunktDefinisjon, behandling)
            .medEndretFelt(HistorikkEndretFeltType.SOKNADSFRIST, null,
                dto.harGyldigGrunn() ? HistorikkEndretFeltVerdiType.HAR_GYLDIG_GRUNN : HistorikkEndretFeltVerdiType.HAR_IKKE_GYLDIG_GRUNN)
            .medBegrunnelse(dto.getBegrunnelse(),
                repositoryProvider.getAksjonspunktRepository().sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon,
                    dto.getBegrunnelse()));

        if (dto.harGyldigGrunn()) {
            Uttaksperiodegrense uttaksperiodegrense = repositoryProvider.getUttakRepository().hentUttaksperiodegrense(behandling.getId());
            LocalDate lagretMottattDato = uttaksperiodegrense.getMottattDato();
            LocalDate tidligereAnseesMottattDato = søknad.getMottattDato().equals(lagretMottattDato) ? null : lagretMottattDato;
            LocalDate dtoMottattDato = dto.getAnsesMottattDato();

            if (!dtoMottattDato.equals(tidligereAnseesMottattDato)) {
                tekstBuilder.medEndretFelt(HistorikkEndretFeltType.MOTTATT_DATO, tidligereAnseesMottattDato, dtoMottattDato);
            }
        }
    }
}
