package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.REGISTRER_PAPIRSØKNAD_ENGANGSSTØNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.REGISTRER_PAPIRSØKNAD_FORELDREPENGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.REGISTRER_PAPIR_ENDRINGSØKNAD_FORELDREPENGER;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.registrerer.DokumentRegistrererTjeneste;
import no.nav.foreldrepenger.domene.registrerer.ManuellRegistreringAksjonspunktDto;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.søknad.v1.SøknadConstants;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringEndringsøknadDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringEngangsstonadDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringForeldrepengerDto;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;
import no.nav.vedtak.felles.xml.soeknad.v1.ObjectFactory;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

@ApplicationScoped
@DtoTilServiceAdapter(dto = ManuellRegistreringDto.class, adapter = AksjonspunktOppdaterer.class)
public class ManuellRegistreringOppdaterer implements AksjonspunktOppdaterer<ManuellRegistreringDto> {

    private FagsakRepository fagsakRepository;
    private HistorikkTjenesteAdapter historikkApplikasjonTjeneste;
    private DokumentRegistrererTjeneste dokumentRegistrererTjeneste;
    private TpsTjeneste tpsTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;

    ManuellRegistreringOppdaterer() {
        // CDI
    }

    @Inject
    public ManuellRegistreringOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                         HistorikkTjenesteAdapter historikkApplikasjonTjeneste,
                                         DokumentRegistrererTjeneste dokumentRegistrererTjeneste, TpsTjeneste tpsTjeneste,
                                         VirksomhetTjeneste virksomhetTjeneste) {
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.historikkApplikasjonTjeneste = historikkApplikasjonTjeneste;
        this.dokumentRegistrererTjeneste = dokumentRegistrererTjeneste;
        this.tpsTjeneste = tpsTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
    }

    @Override
    public OppdateringResultat oppdater(ManuellRegistreringDto registreringDto, Behandling behandling, VilkårResultat.Builder vilkårBuilder) {
        if (!registreringDto.getUfullstendigSoeknad()) {
            ManuellRegistreringValidator.validerOpplysninger(registreringDto);

            Fagsak fagsak = fagsakRepository.finnEksaktFagsak(behandling.getFagsakId());
            NavBruker navBruker = fagsak.getNavBruker();
            String søknadXml = opprettSøknadsskjema(registreringDto, navBruker);
            String dokumentTypeIdKode = finnDokumentType(registreringDto, behandling.getType());

            final ManuellRegistreringAksjonspunktDto adapter = new ManuellRegistreringAksjonspunktDto(!registreringDto.getUfullstendigSoeknad(), søknadXml,
                dokumentTypeIdKode, registreringDto.getMottattDato(), registreringDto.isRegistrerVerge());
            dokumentRegistrererTjeneste.aksjonspunktManuellRegistrering(behandling, adapter);

            lagHistorikkInnslag(behandling.getId(), HistorikkinnslagType.REGISTRER_PAPIRSØK, registreringDto.getKommentarEndring());
            return OppdateringResultat.utenOveropp();

        } else {

            final ManuellRegistreringAksjonspunktDto adapter = new ManuellRegistreringAksjonspunktDto(!registreringDto.getUfullstendigSoeknad());
            dokumentRegistrererTjeneste.aksjonspunktManuellRegistrering(behandling, adapter);

            vilkårBuilder.leggTilVilkårResultatManueltIkkeVurdert(VilkårType.SØKERSOPPLYSNINGSPLIKT);

            lagHistorikkInnslag(behandling.getId(), HistorikkinnslagType.MANGELFULL_SØKNAD, null);
            return OppdateringResultat.medFremoverHopp(FellesTransisjoner.FREMHOPP_TIL_KONTROLLERER_SØKERS_OPPLYSNINGSPLIKT);
        }
    }

    private String finnDokumentType(ManuellRegistreringDto registreringDto, BehandlingType behandlingType) {
        String søknadsType = registreringDto.getSoknadstype().getKode();

        if (FagsakYtelseType.ENGANGSTØNAD.getKode().equals(søknadsType) &&
            FamilieHendelseType.FØDSEL.getKode().equals(registreringDto.getTema().getKode())) {
            return DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.getKode();
        }
        if (erEngangsstønadFødsel(registreringDto, søknadsType)) {
            return DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.getKode();
        }
        if (erEngangsstønadAdopsjon(registreringDto, søknadsType)) {
            return DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON.getKode();
        }
        if (erForeldrepengerFødsel(registreringDto, søknadsType)) {
            return DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.getKode();
        }
        if (erForeldrepengerAdopsjon(registreringDto, søknadsType)) {
            return DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON.getKode();
        }
        if (erForeldrepengerEndringssøknad(behandlingType, søknadsType)) {
            return DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD.getKode();
        }
        return DokumentTypeId.UDEFINERT.getKode();
    }

    private boolean erForeldrepengerAdopsjon(ManuellRegistreringDto registreringDto, String søknadsType) {
        return FagsakYtelseType.FORELDREPENGER.getKode().equals(søknadsType) &&
            FamilieHendelseType.ADOPSJON.getKode().equals(registreringDto.getTema().getKode()) ||
            FamilieHendelseType.OMSORG.getKode().equals(registreringDto.getTema().getKode());
    }

    private boolean erForeldrepengerFødsel(ManuellRegistreringDto registreringDto, String søknadsType) {
        return FagsakYtelseType.FORELDREPENGER.getKode().equals(søknadsType) &&
            FamilieHendelseType.FØDSEL.getKode().equals(registreringDto.getTema().getKode());
    }

    private boolean erForeldrepengerEndringssøknad(BehandlingType behandlingType, String søknadsType) {
        return FagsakYtelseType.ENDRING_FORELDREPENGER.getKode().equals(søknadsType) && behandlingType.equals(BehandlingType.REVURDERING);
    }

    private boolean erEngangsstønadAdopsjon(ManuellRegistreringDto registreringDto, String søknadsType) {
        return FagsakYtelseType.ENGANGSTØNAD.getKode().equals(søknadsType) &&
            (FamilieHendelseType.ADOPSJON.getKode().equals(registreringDto.getTema().getKode()) ||
                FamilieHendelseType.OMSORG.getKode().equals(registreringDto.getTema().getKode()));
    }

    private boolean erEngangsstønadFødsel(ManuellRegistreringDto registreringDto, String søknadsType) {
        return FagsakYtelseType.ENGANGSTØNAD.getKode().equals(søknadsType) &&
            FamilieHendelseType.FØDSEL.getKode().equals(registreringDto.getTema().getKode());
    }

    private String opprettSøknadsskjema(ManuellRegistreringDto registreringDto, NavBruker navBruker) {
        Soeknad søknad = null;
        if (registreringDto.getKode().equals(REGISTRER_PAPIRSØKNAD_ENGANGSSTØNAD.getKode())) {
            søknad = SøknadMapperES.mapTilEngangsstønad((ManuellRegistreringEngangsstonadDto) registreringDto, navBruker, tpsTjeneste);
        } else if (registreringDto.getKode().equals(REGISTRER_PAPIR_ENDRINGSØKNAD_FORELDREPENGER.getKode())) {
            søknad = SøknadMapperFP.mapTilForeldrepengerEndringssøknad((ManuellRegistreringEndringsøknadDto) registreringDto, navBruker);
        } else if (registreringDto.getKode().equals(REGISTRER_PAPIRSØKNAD_FORELDREPENGER.getKode())) {
            søknad = SøknadMapperFP.mapTilForeldrepenger((ManuellRegistreringForeldrepengerDto) registreringDto, navBruker, tpsTjeneste, virksomhetTjeneste);
        }
        String søknadXml;
        try {
            søknadXml = JaxbHelper.marshalAndValidateJaxb(SøknadConstants.JAXB_CLASS,
                new ObjectFactory().createSoeknad(søknad),
                SøknadConstants.XSD_LOCATION,
                SøknadConstants.ADDITIONAL_XSD_LOCATION,
                SøknadConstants.ADDITIONAL_CLASSES);
        } catch (JAXBException | SAXException e) {
            throw ManuellRegistreringFeil.FACTORY.marshallingFeil(e).toException();
        }
        return søknadXml;
    }

    private void lagHistorikkInnslag(Long behandlingId, HistorikkinnslagType innslagType, String kommentarEndring) {
        Historikkinnslag innslag = new Historikkinnslag();
        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder();

        innslag.setAktør(HistorikkAktør.SAKSBEHANDLER);
        innslag.setBehandlingId(behandlingId);
        innslag.setType(innslagType);
        builder.medHendelse(innslagType);
        if (kommentarEndring != null) {
            builder.medBegrunnelse(kommentarEndring);
        }
        builder.build(innslag);
        historikkApplikasjonTjeneste.lagInnslag(innslag);
    }
}
