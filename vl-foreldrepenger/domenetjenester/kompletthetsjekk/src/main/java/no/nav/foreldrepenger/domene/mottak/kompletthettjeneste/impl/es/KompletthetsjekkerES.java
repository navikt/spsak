package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.es;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadVedlegg;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetResultat;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.Kompletthetsjekker;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;

@ApplicationScoped
@BehandlingTypeRef
@FagsakYtelseTypeRef("ES")
public class KompletthetsjekkerES implements Kompletthetsjekker {
    private AksjonspunktRepository aksjonspunktRepository;
    private KodeverkRepository kodeverkRepository;
    private SøknadRepository søknadRepository;
    private FamilieHendelseRepository familieHendelseRepository;
    private PersonopplysningTjeneste personopplysningTjeneste;
    private DokumentArkivTjeneste dokumentArkivTjeneste;

    KompletthetsjekkerES() {
        // CDI
    }

    @Inject
    public KompletthetsjekkerES(BehandlingRepositoryProvider repositoryProvider,
                                DokumentArkivTjeneste dokumentArkivTjeneste,
                                KodeverkRepository kodeverkRepository,
                                PersonopplysningTjeneste personopplysningTjeneste) {
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.kodeverkRepository = kodeverkRepository;
        this.dokumentArkivTjeneste = dokumentArkivTjeneste;
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.personopplysningTjeneste = personopplysningTjeneste;
    }

    @Override
    public KompletthetResultat vurderSøknadMottatt(Behandling behandling) {
        // Denne vil alltid være oppfylt for engangsstønad
        return KompletthetResultat.oppfylt();
    }

    @Override
    public KompletthetResultat vurderSøknadMottattForTidlig(Behandling behandling) {
        throw new UnsupportedOperationException("Metode brukes ikke i ES"); //$NON-NLS-1$
    }

    @Override
    public KompletthetResultat vurderForsendelseKomplett(Behandling behandling) {
        if (utledAlleManglendeVedleggForForsendelse(behandling).isEmpty()) {
            return KompletthetResultat.oppfylt();
        } else {
            AksjonspunktDefinisjon definisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD.getKode());
            LocalDateTime ønsketFrist = LocalDateTime.now().plusDays(definisjon.getFristPeriod().getDays());
            return KompletthetResultat.ikkeOppfylt(ønsketFrist, Venteårsak.AVV_DOK);
        }
    }

    // Spør Joark om dokumentliste og sjekker det som finnes i vedleggslisten på søknaden mot det som ligger i Joark.
    // Vedleggslisten på søknaden regnes altså i denne omgang som fasit på hva som er påkrevd.
    @Override
    public List<ManglendeVedlegg> utledAlleManglendeVedleggForForsendelse(Behandling behandling) {

        final Optional<Søknad> søknad = søknadRepository.hentSøknadHvisEksisterer(behandling);

        // Manuelt registrerte søknader har foreløpig ikke vedleggsliste og kan derfor ikke kompletthetssjekkes:
        if (!søknad.isPresent() || (!søknad.get().getElektroniskRegistrert() || søknad.get().getSøknadVedlegg() == null || søknad.get().getSøknadVedlegg().isEmpty())) {
            return emptyList();
        }

        Set<DokumentTypeId> dokumentTypeIds = dokumentArkivTjeneste.hentDokumentTypeIdForSak(behandling.getFagsak().getSaksnummer(), LocalDate.MIN, Collections.emptyList());

        return søknad.get().getSøknadVedlegg()
            .stream()
            .filter(SøknadVedlegg::isErPåkrevdISøknadsdialog)
            .map(SøknadVedlegg::getSkjemanummer)
            .map(this::finnDokumentTypeId)
            .filter(doc -> !dokumentTypeIds.contains(doc))
            .map(ManglendeVedlegg::new)
            .collect(Collectors.toList());
    }

    @Override
    public List<ManglendeVedlegg> utledAlleManglendeVedleggSomIkkeKommer(Behandling behandling) {
        return emptyList();
    }

    @Override
    public boolean erForsendelsesgrunnlagKomplett(Behandling behandling) {
        Søknad søknad = søknadRepository.hentSøknad(behandling);
        if (søknad == null) {
            // Uten søknad må det antas at den heller ikke er komplett. Sjekker nedenfor forutsetter at søknad finnes.
            return false;
        }
        if (!søknad.getElektroniskRegistrert()) {
            // Søknad manuelt registrert av saksbehandlier - dermed er opplysningsplikt allerede vurdert av han/henne
            return true;
        }

        List<ManglendeVedlegg> manglendeVedlegg = utledAlleManglendeVedleggForForsendelse(behandling);
        if (manglendeVedlegg.isEmpty()) {
            return true;
        }
        if (familieHendelseRepository.hentAggregat(behandling).getSøknadVersjon().getGjelderFødsel()) {
            if (finnesBarnet(behandling)) {
                return true;
            }
        }
        return false;
    }

    private boolean finnesBarnet(Behandling behandling) {
        final Optional<LocalDate> fødselsDato = familieHendelseRepository.hentAggregat(behandling).getSøknadVersjon().getBarna()
            .stream().map(UidentifisertBarn::getFødselsdato).findFirst();

        if (fødselsDato.isPresent()) {
            PersonopplysningerAggregat personopplysninger = personopplysningTjeneste.hentPersonopplysninger(behandling);
            List<Personopplysning> alleBarn = personopplysninger.getBarna();
            return alleBarn.stream().anyMatch(bb -> bb.getFødselsdato().equals(fødselsDato.get()));
        }
        return false;
    }


    private DokumentTypeId finnDokumentTypeId(String dokumentTypeIdKode) {
        DokumentTypeId dokumentTypeId;
        try {
            dokumentTypeId = kodeverkRepository.finnForKodeverkEiersKode(DokumentTypeId.class, dokumentTypeIdKode);
        } catch (NoResultException e) { //NOSONAR
            // skal tåle dette
            dokumentTypeId = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.UDEFINERT);
        }
        return dokumentTypeId;
    }
}
