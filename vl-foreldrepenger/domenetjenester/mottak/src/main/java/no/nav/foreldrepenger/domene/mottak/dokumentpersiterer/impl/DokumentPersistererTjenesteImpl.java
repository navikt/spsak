package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.DokumentPersistererTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.MottattDokumentFeil;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.MottattDokumentOversetter;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.NamespaceRef;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.xml.MottattDokumentXmlParser;

@SuppressWarnings("rawtypes")
@ApplicationScoped
public class DokumentPersistererTjenesteImpl implements DokumentPersistererTjeneste {

    @Inject
    public DokumentPersistererTjenesteImpl() {
    }

    @Override
    public MottattDokumentWrapper payloadTilWrapper(InngåendeSaksdokument dokument) {
        return MottattDokumentXmlParser.unmarshall(dokument.getPayloadType(), dokument.getPayload());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void persisterDokumentinnhold(MottattDokumentWrapper wrapper, InngåendeSaksdokument dokument, Behandling behandling, Optional<LocalDate> gjelderFra) {
        MottattDokumentOversetter dokumentOversetter = getDokumentOversetter(wrapper.getSkjemaType());
        dokumentOversetter.trekkUtDataOgPersister(wrapper, dokument, behandling, gjelderFra);
    }

    @Override
    public void persisterDokumentinnhold(InngåendeSaksdokument dokument, Behandling behandling) {
        MottattDokumentWrapper dokumentWrapper = payloadTilWrapper(dokument);
        persisterDokumentinnhold(dokumentWrapper, dokument, behandling, Optional.empty());
    }

    private MottattDokumentOversetter<?> getDokumentOversetter(String namespace) {
        NamespaceRef.NamespaceRefLiteral annotationLiteral = new NamespaceRef.NamespaceRefLiteral(namespace);

        Instance<MottattDokumentOversetter<?>> instance = CDI.current().select(new TypeLiteralMottattDokumentOversetter(), annotationLiteral);

        if (instance.isAmbiguous()) {
            throw MottattDokumentFeil.FACTORY.flereImplementasjonerAvSkjemaType(namespace).toException();
        } else if (instance.isUnsatisfied()) {
            throw MottattDokumentFeil.FACTORY.ukjentSkjemaType(namespace).toException();
        }
        MottattDokumentOversetter<?> minInstans = instance.get();
        if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
            throw new IllegalStateException("Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
        }
        return minInstans;
    }

    private static final class TypeLiteralMottattDokumentOversetter extends TypeLiteral<MottattDokumentOversetter<?>> {
    }

}
