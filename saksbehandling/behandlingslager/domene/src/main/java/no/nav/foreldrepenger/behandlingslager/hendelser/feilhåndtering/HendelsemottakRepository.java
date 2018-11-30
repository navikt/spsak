package no.nav.foreldrepenger.behandlingslager.hendelser.feilhåndtering;

public interface HendelsemottakRepository {
    boolean hendelseErNy(String hendelseUid);

    void registrerMottattHendelse(String hendelseUid);
}
