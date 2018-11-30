package no.nav.foreldrepenger.datavarehus;

public interface DatavarehusRepository {

    long lagre(FagsakDvh fagsakDvh);

    long lagre(BehandlingDvh behandlingDvh);

    long lagre(BehandlingStegDvh behandlingStegDvh);

    long lagre(AksjonspunktDvh aksjonspunktDvh);

    long lagre(KontrollDvh kontrollDvh);

    long lagre(BehandlingVedtakDvh behandlingVedtakDvh);

    void lagre(VedtakUtbetalingDvh vedtakXml);
}
