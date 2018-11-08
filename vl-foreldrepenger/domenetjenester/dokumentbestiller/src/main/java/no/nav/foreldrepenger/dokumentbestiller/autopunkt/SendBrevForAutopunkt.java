package no.nav.foreldrepenger.dokumentbestiller.autopunkt;


import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;

public interface SendBrevForAutopunkt {

    void sendBrevForSøknadIkkeMottatt(Behandling behandling);

    void sendBrevForTidligSøknad(Behandling behandling, Aksjonspunkt ap);

    void sendBrevForVenterPåFødsel(Behandling behandling, Aksjonspunkt ap);
}
