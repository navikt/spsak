package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.getSvarFrist;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettFlettefelt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.vedtak.util.StringUtils;

public class KlageOversendtKlageinstansDokument implements DokumentType {
    private BrevParametere brevParametere;
    private String fritekst;

    public KlageOversendtKlageinstansDokument(BrevParametere brevParametere, String fritekst) {
        super();
        this.brevParametere = brevParametere;
        this.fritekst = fritekst;
    }

    @Override
    public String getDokumentMalType() {
        return DokumentMalType.KLAGE_OVERSENDT_KLAGEINSTANS_DOK;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dto) {
        List<Flettefelt> flettefelter = new ArrayList<>();
        flettefelter.add(opprettFlettefelt(Flettefelt.YTELSE_TYPE, dto.getYtelsesTypeKode()));
        flettefelter.add(opprettFlettefelt(Flettefelt.MOTTATT_DATO, dto.getMottattKlageDato().toString()));

        // mapper til tomt felt (hack for tester.)
        flettefelter.add(opprettFlettefelt(Flettefelt.ANTALL_UKER, Optional.ofNullable(dto.getBehandlingsfristIUker()).map(Object::toString).orElse("")));

        Optional<String> faktiskFritekst = avklarFritekst(fritekst, dto.getFritekstKlageOversendt());
        faktiskFritekst.ifPresent(s -> flettefelter.add(opprettFlettefelt(Flettefelt.FRITEKST, s)));
        flettefelter.add(opprettFlettefelt(Flettefelt.FRIST_DATO, getSvarFrist(brevParametere).toString()));
        return flettefelter;
    }

    private Optional<String> avklarFritekst(String fritekst, Optional<String> lagretFritekst) {
        if (!StringUtils.nullOrEmpty(fritekst)) {
            return Optional.of(fritekst);
        }
        return lagretFritekst;
    }
}
