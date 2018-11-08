package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.foreldrepenger.integrasjon.dokument.revurdering.AdvarselKodeKode;
import no.nav.vedtak.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.getSvarFrist;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettFlettefelt;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettIkkeObligatoriskeFlettefelt;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettObligatoriskeFlettefelt;

public class RevurderingDokument implements DokumentType {
    private String fritekst;
    private BrevParametere brevParametere;
    private String årsaksKode;

    public RevurderingDokument(BrevParametere brevParametere, String fritekst, String årsaksKode) {
        this.brevParametere = brevParametere;
        this.fritekst = fritekst;
        this.årsaksKode = årsaksKode;
        setÅrsakskodeHvisFritekst(fritekst, årsaksKode);
    }

    private void setÅrsakskodeHvisFritekst(String fritekst, String årsaksKode) {
        if (!StringUtils.nullOrEmpty(fritekst) && StringUtils.nullOrEmpty(årsaksKode)) {
            this.årsaksKode = AdvarselKodeKode.ANNET.value();
        }
    }

    @Override
    public String getDokumentMalType() {
        return DokumentMalType.REVURDERING_DOK;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dto) {
        List<Flettefelt> flettefelter = new ArrayList<>();

        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.FRIST_DATO, getSvarFrist(brevParametere).toString()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.YTELSE_TYPE, dto.getYtelsesTypeKode()));
        flettefelter.add(opprettObligatoriskeFlettefelt(Flettefelt.ANTALL_BARN, Integer.toString(dto.getAntallBarn())));

        Optional<LocalDate> terminDato = dto.getTermindatoFraOriginalBehandling();
        terminDato.ifPresent(localDate -> flettefelter.add(opprettFlettefelt(Flettefelt.TERMIN_DATO, localDate.toString())));

        opprettIkkeObligatoriskeFlettefelt(flettefelter, Flettefelt.FRITEKST, fritekst);
        opprettIkkeObligatoriskeFlettefelt(flettefelter, Flettefelt.ADVARSEL_KODE, årsaksKode);

        return flettefelter;
    }
}
