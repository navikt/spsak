package no.nav.foreldrepenger.domene.dokumentarkiv.journal.impl;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;

public class HentDokumentType {

    private HentDokumentType(){
        //Denne klassen skal ikke instansieres
    }

    static DokumentTypeId slåOppInngåendeDokumentType(KodeverkRepository kodeverkRepository, String offisiellDokumentType) {
        return kodeverkRepository.finnForKodeverkEiersKode(DokumentTypeId.class, offisiellDokumentType, DokumentTypeId.UDEFINERT);
    }
}
