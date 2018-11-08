package no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "Venteårsak")
@DiscriminatorValue(Venteårsak.DISCRIMINATOR)
public class Venteårsak extends Kodeliste {
    public static final String DISCRIMINATOR = "VENT_AARSAK";

    public static final Venteårsak AVV_DOK = new Venteårsak("AVV_DOK"); //$NON-NLS-1$
    public static final Venteårsak AVV_FODSEL = new Venteårsak("AVV_FODSEL"); //$NON-NLS-1$
    public static final Venteårsak AVV_RESPONS_REVURDERING = new Venteårsak("AVV_RESPONS_REVURDERING"); //$NON-NLS-1$
    public static final Venteårsak UTV_FRIST = new Venteårsak("UTV_FRIST"); //$NON-NLS-1$
    public static final Venteårsak SCANN = new Venteårsak("SCANN"); //$NON-NLS-1$
    public static final Venteårsak FOR_TIDLIG_SOKNAD = new Venteårsak("FOR_TIDLIG_SOKNAD");
    public static final Venteårsak OPPDATERING_ÅPEN_BEHANDLING = new Venteårsak("OPPD_ÅPEN_BEH");
    public static final Venteårsak VENT_OPDT_INNTEKTSMELDING = new Venteårsak("VENT_OPDT_INNTEKTSMELDING"); //$NON-NLS-1$
    public static final Venteårsak VENT_REGISTERINNHENTING = new Venteårsak("VENT_REGISTERINNHENTING"); //$NON-NLS-1$
    public static final Venteårsak VENT_TIDLIGERE_BEHANDLING = new Venteårsak("VENT_TIDLIGERE_BEHANDLING"); //$NON-NLS-1$
    public static final Venteårsak VENT_INFOTRYGD = new Venteårsak("VENT_INFOTRYGD"); //$NON-NLS-1$
    public static final Venteårsak VENT_ÅPEN_BEHANDLING = new Venteårsak("VENT_ÅPEN_BEHANDLING"); //$NON-NLS-1$
    public static final Venteårsak VENT_OPPTJENING_OPPLYSNINGER = new Venteårsak("VENT_OPPTJENING_OPPLYSNINGER");
    public static final Venteårsak VENT_INNTEKT_RAPPORTERINGSFRIST = new Venteårsak("VENT_INNTEKT_RAPPORTERINGSFRIST");

    public static final Venteårsak UDEFINERT = new Venteårsak("-"); //$NON-NLS-1$

    public Venteårsak() {
    }

    public Venteårsak(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static Venteårsak getByKode(String kode){
        if(AVV_DOK.getKode().equals(kode)){
            return AVV_DOK;
        } else if(AVV_FODSEL.getKode().equals(kode)){
            return AVV_FODSEL;
        } else if(UTV_FRIST.getKode().equals(kode)){
            return UTV_FRIST;
        } else if(SCANN.getKode().equals(kode)){
            return SCANN;
        } else if (FOR_TIDLIG_SOKNAD.getKode().equals(kode)) {
            return FOR_TIDLIG_SOKNAD;
        } else if (VENT_OPDT_INNTEKTSMELDING.getKode().equals(kode)) {
            return VENT_OPDT_INNTEKTSMELDING;
        } else if (AVV_RESPONS_REVURDERING.getKode().equals(kode)) {
            return AVV_RESPONS_REVURDERING;
        } else if (VENT_REGISTERINNHENTING.getKode().equals(kode)) {
            return VENT_REGISTERINNHENTING;
        } else if (VENT_OPPTJENING_OPPLYSNINGER.getKode().equals(kode)){
            return VENT_OPPTJENING_OPPLYSNINGER;
        }
        return null;
    }
}
