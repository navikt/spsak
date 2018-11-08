package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivDokument;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivJournalPost;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.dokumentarkiv.Kommunikasjonsretning;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

@Alternative
@Priority(1)
@Dependent
public class DokumentArkivTjenesteConsumerMock implements DokumentArkivTjeneste {

    @Override
    public byte[] hentDokumnet(JournalpostId journalpostId, String dokumentId) {
        byte [] bytes = {1, 2, 7};
        return bytes;
    }

    @Override
    public List<ArkivJournalPost> hentAlleDokumenterForVisning(Saksnummer saksnummer) {
        return hentAlleJournalposterForSak(saksnummer);
    }

    @Override
    public List<ArkivJournalPost> hentAlleJournalposterForSak(Saksnummer saksnummer) {
        ArkivJournalPost journalPost = ArkivJournalPost.Builder.ny()
            .medSaksnummer(saksnummer)
            .medJournalpostId(new JournalpostId("12345678"))
            .medKommunikasjonsretning(Kommunikasjonsretning.INN)
            .medTidspunkt(LocalDate.now().minusDays(1))
            .medHoveddokument(ArkivDokument.Builder.ny().medDokumentId("34534573").medTittel("Terminbekreftelse")
                .medDokumentTypeId(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL)
                .medDokumentKategori(DokumentKategori.UDEFINERT).build())
            .build();
        return Collections.singletonList(journalPost);
    }

    @Override
    public Optional<ArkivJournalPost> hentJournalpostForSak(Saksnummer saksnummer, JournalpostId journalpostId) {
        return Optional.of(hentAlleJournalposterForSak(saksnummer).get(0));
    }

    @Override
    public Set<DokumentTypeId> hentDokumentTypeIdForSak(Saksnummer saksnummer, LocalDate mottattEtterDato, List<DokumentTypeId> eksisterende) {
        return Collections.singleton(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL);
    }

    @Override
    public DokumentTypeId utledDokumentTypeFraTittel(Saksnummer saksnummer, JournalpostId journalpostId) {
        return DokumentTypeId.UDEFINERT;
    }

}
