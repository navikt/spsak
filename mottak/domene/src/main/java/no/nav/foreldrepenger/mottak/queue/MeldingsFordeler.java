package no.nav.foreldrepenger.mottak.queue;


import no.nav.foreldrepenger.no.nav.spmottak.meldinger.dokumentnotifikasjonv1.Forsendelsesinformasjon;

public interface MeldingsFordeler {

    void execute(Forsendelsesinformasjon forsendelsesinfo);
}
