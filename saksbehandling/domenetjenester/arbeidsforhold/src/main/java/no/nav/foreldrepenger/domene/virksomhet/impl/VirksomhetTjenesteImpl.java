package no.nav.foreldrepenger.domene.virksomhet.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetAlleredeLagretException;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonConsumer;
import no.nav.vedtak.felles.integrasjon.organisasjon.hent.HentOrganisasjonRequest;


@ApplicationScoped
public class VirksomhetTjenesteImpl implements VirksomhetTjeneste {

    private static final String TJENESTE = "Organisasjon";

    private OrganisasjonConsumer organisasjonConsumer;
    private VirksomhetRepository virksomhetRepository;

    public VirksomhetTjenesteImpl() {
        // CDI
    }

    @Inject
    public VirksomhetTjenesteImpl(OrganisasjonConsumer organisasjonConsumer,
                                  VirksomhetRepository virksomhetRepository) {
        this.organisasjonConsumer = organisasjonConsumer;
        this.virksomhetRepository = virksomhetRepository;
    }

    @Override
    public Virksomhet hentOgLagreOrganisasjon(String orgNummer) {
        final Optional<Virksomhet> virksomhetOptional = virksomhetRepository.hentForEditering(orgNummer);
        if (!virksomhetOptional.isPresent() || virksomhetOptional.get().skalRehentes()) {
            HentOrganisasjonResponse response = hentOrganisasjon(orgNummer);

            final Virksomhet virksomhet = mapOrganisasjonResponseToOrganisasjon(response.getOrganisasjon(), virksomhetOptional);
            try {
                virksomhetRepository.lagre(virksomhet);
                return virksomhet;
            } catch (VirksomhetAlleredeLagretException exception) {
                return virksomhetOptional.orElseThrow(IllegalStateException::new);
            }
        }
        return virksomhetOptional.orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public Optional<Virksomhet> finnOrganisasjon(String orgNummer) {
        return OrganisasjonsNummerValidator.erGyldig(orgNummer) ? virksomhetRepository.hent(orgNummer) : Optional.empty();
    }

    private HentOrganisasjonResponse hentOrganisasjon(String orgNummer) {
        HentOrganisasjonRequest request = new HentOrganisasjonRequest(orgNummer);
        try {
            return organisasjonConsumer.hentOrganisasjon(request);
        } catch (HentOrganisasjonOrganisasjonIkkeFunnet e) {
            throw OrganisasjonTjenesteFeil.FACTORY.organisasjonIkkeFunnet(orgNummer, e).toException();
        } catch (HentOrganisasjonUgyldigInput e) {
            throw OrganisasjonTjenesteFeil.FACTORY.ugyldigInput(TJENESTE, orgNummer, e).toException();
        }
    }

    private Virksomhet mapOrganisasjonResponseToOrganisasjon(Organisasjon responsOrganisasjon, Optional<Virksomhet> virksomhetOptional) {
        final VirksomhetEntitet.Builder builder = getBuilder(virksomhetOptional)
            .medNavn(((UstrukturertNavn) responsOrganisasjon.getNavn()).getNavnelinje().stream().filter(it -> !it.isEmpty())
                .reduce("", (a, b) -> a + " " + b).trim())
            .medRegistrert(DateUtil.convertToLocalDate(responsOrganisasjon.getOrganisasjonDetaljer().getRegistreringsDato()));
        if(!virksomhetOptional.isPresent()) {
            builder.medOrgnr(responsOrganisasjon.getOrgnummer());
        }
        if (responsOrganisasjon instanceof no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Virksomhet) {
            no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Virksomhet virksomhet = (no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Virksomhet) responsOrganisasjon;

            if (virksomhet.getVirksomhetDetaljer().getOppstartsdato() != null) {
                builder.medOppstart(DateUtil.convertToLocalDate(virksomhet.getVirksomhetDetaljer().getOppstartsdato()));
            }
            if (virksomhet.getVirksomhetDetaljer().getNedleggelsesdato() != null) {
                builder.medAvsluttet(DateUtil.convertToLocalDate(virksomhet.getVirksomhetDetaljer().getNedleggelsesdato()));
            }
        }
        return builder.oppdatertOpplysningerNå().build();
    }

    private VirksomhetEntitet.Builder getBuilder(Optional<Virksomhet> virksomhetOptional) {
        return virksomhetOptional.map(VirksomhetEntitet.Builder::new).orElseGet(VirksomhetEntitet.Builder::new);
    }

}
