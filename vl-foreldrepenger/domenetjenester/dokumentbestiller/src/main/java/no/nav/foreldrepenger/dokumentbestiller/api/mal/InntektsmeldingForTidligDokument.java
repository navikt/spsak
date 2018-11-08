package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettFlettefelt;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettStrukturertFlettefeltListe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.BrevFeil;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerFeil;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeMedPerioderDto;
import no.nav.vedtak.feil.FeilFactory;

public class InntektsmeldingForTidligDokument implements DokumentType {
    private static final Set<BehandlingType> gyldigeKoder = new HashSet<>(Arrays.asList(
        BehandlingType.REVURDERING,
        BehandlingType.FØRSTEGANGSSØKNAD
    ));

    private BehandlingType behandlingType;
    private BrevParametere brevParametere;

    public InntektsmeldingForTidligDokument(BrevParametere brevParametere, BehandlingType behandlingType) {
        this.brevParametere = brevParametere;
        this.behandlingType = behandlingType;
        if (!gyldigeKoder.contains(behandlingType)) {
            throw BrevFeil.FACTORY.inntektsmeldingForTidligBrevKreverGyldigBehandlingstype(behandlingType.getKode()).toException();
        }
    }

    @Override
    public String getDokumentMalType() {
        return DokumentMalType.INNTEKTSMELDING_FOR_TIDLIG_DOK;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dto) {
        if (!DokumentTypeMedPerioderDto.class.isInstance(dto)) {
            throw new IllegalArgumentException("DokumentTypeDto må være av " + DokumentTypeMedPerioderDto.class.getTypeName());
        }
        DokumentTypeMedPerioderDto perioderDto = (DokumentTypeMedPerioderDto) dto;

        List<Flettefelt> flettefelter = new ArrayList<>();
        flettefelter.add(nullSafeOpprettFlettefelt(Flettefelt.BEHANDLINGSTYPE, behandlingType.getKode()));
        flettefelter.add(nullSafeOpprettFlettefelt(Flettefelt.ARBEIDSGIVER_NAVN, perioderDto.getArbeidsgiversNavn()));
        flettefelter.add(opprettFlettefelt(Flettefelt.MOTTATT_DATO, (perioderDto.getMottattInntektsmelding() != null ? perioderDto.getMottattInntektsmelding().toString() : null)));
        flettefelter.addAll(opprettStrukturertFlettefeltListe(Flettefelt.PERIODE_LISTE, perioderDto.getFeriePerioder()));
        flettefelter.add(opprettFlettefelt(Flettefelt.SOK_ANTALL_UKER_FOR, (brevParametere.getSøkAntallUker() != null ? brevParametere.getSøkAntallUker().toString() : null)));
        return flettefelter;
    }

    private Flettefelt nullSafeOpprettFlettefelt(String feltnavn, Object feltverdi) {
        if (null == feltverdi) {
            throw FeilFactory.create(DokumentBestillerFeil.class).feltManglerVerdi(feltnavn).toException();
        }
        return opprettFlettefelt(feltnavn, feltverdi.toString());
    }

    @Override
    public boolean harPerioder() {
        return true;
    }
}


