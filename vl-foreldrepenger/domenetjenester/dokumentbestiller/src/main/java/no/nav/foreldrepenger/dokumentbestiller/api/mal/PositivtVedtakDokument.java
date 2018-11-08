package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettFlettefelt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerFeil;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.PersonstatusKodeType;

public class PositivtVedtakDokument implements DokumentType {
    private BrevParametere brevParametere;

    public PositivtVedtakDokument(BrevParametere brevParametere) {
        this.brevParametere = brevParametere;
    }

    @Override
    public String getDokumentMalType() {
        return DokumentMalType.POSITIVT_VEDTAK_DOK;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dto) {
        if (dto.getHarIkkeBehandlingsResultat()) {
            throw DokumentBestillerFeil.FACTORY.behandlingManglerResultat(dto.getBehandlingId()).toException();
        }

        List<Flettefelt> flettefelter = new ArrayList<>();

        flettefelter.add(opprettFlettefelt(Flettefelt.BEHANDLINGSTYPE, dto.getBehandlingsTypePositivtVedtak()));
        dto.getDokumentBehandlingsresultatDto().getBeløp().ifPresent(beløp -> flettefelter.add(opprettFlettefelt(Flettefelt.BELØP, beløp.toString())));

        flettefelter.add(opprettFlettefelt(Flettefelt.KLAGE_FRIST_UKER, brevParametere.getKlagefristUker().toString()));
        flettefelter.add(opprettFlettefelt(Flettefelt.PERSON_STATUS, dto.getPersonstatus()));
        flettefelter.add(opprettFlettefelt(Flettefelt.SOKERSNAVN, dto.getSøkersNavn()));

        return flettefelter;
    }

    @Override
    public String getPersonstatusVerdi(PersonstatusType personstatus) {
        return Objects.equals(personstatus, PersonstatusType.DØD) ? PersonstatusKodeType.DOD.value() : PersonstatusKodeType.ANNET.value();
    }
}
