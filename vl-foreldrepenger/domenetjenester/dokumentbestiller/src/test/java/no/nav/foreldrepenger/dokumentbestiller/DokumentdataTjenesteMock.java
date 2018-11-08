package no.nav.foreldrepenger.dokumentbestiller;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper.DokumentTypeDtoMapper;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
class DokumentdataTjenesteMock extends DokumentDataTjenesteImpl {

    private static final String RETURNAVN = "returnavn";
    private static final String RETURADRESSE_1 = "returadresse1";
    private static final String RETUR_POSTNR = "1234";
    private static final String RETUR_POSTSTED = "OSLO";
    private static final String RETUR_KLAGENAVN = "NAVKlage";
    public static final String NORG_2_KONTAKT_TELEFON_NUMMER = "12344321";
    private static final String NORG_2_KLAGEINSTANS_TELEFON_NUMMER = "43211234";

    private DokumentTypeDtoMapper mapper;
    private Behandling behandling;
    private DokumentData dokumentData;

    DokumentdataTjenesteMock(Behandling behandling, DokumentRepository dokrep, TpsTjeneste tpsTjeneste, BehandlingRepositoryProvider repositoryProvider, DokumentTypeDtoMapper mapper) {
        super(NORG_2_KONTAKT_TELEFON_NUMMER, NORG_2_KLAGEINSTANS_TELEFON_NUMMER, dokrep, repositoryProvider, tpsTjeneste, new ReturadresseKonfigurasjon(RETURNAVN, RETURADRESSE_1, RETUR_POSTNR, RETUR_POSTSTED, RETUR_KLAGENAVN), null, mapper);
        this.behandling = behandling;
        this.mapper = mapper;
    }

    public void setBehandling(Behandling behandling) {
        this.behandling = behandling;
    }

    @Override
    public Long lagreDokumentData(Long behandlingId, DokumentType dokumentType) {
        dokumentData = opprettDokumentDataForBehandling(behandlingId, dokumentType);
        for (DokumentFelles df : dokumentData.getDokumentFelles()) {
            if (dokumentType.harPerioder()) {
                dokumentType.getFlettefelter(mapper.mapToDto(behandling, df.getSakspartNavn(), df.getSakspartPersonStatus(), dokumentType.harPerioder())).forEach(f ->
                    df.getDokumentTypeDataListe().add(fraFlettefelt(f, df)));
            } else {
                dokumentType.getFlettefelter(mapper.mapToDto(behandling, df.getSakspartNavn(), df.getSakspartPersonStatus())).forEach(f ->
                    df.getDokumentTypeDataListe().add(fraFlettefelt(f, df)));
            }
            Whitebox.setInternalState(df, "id", 1L);
        }
        return 0L;
    }

    @Override
    public DokumentData hentDokumentData(Long dokumentDataId) {
        return dokumentData;
    }
}
