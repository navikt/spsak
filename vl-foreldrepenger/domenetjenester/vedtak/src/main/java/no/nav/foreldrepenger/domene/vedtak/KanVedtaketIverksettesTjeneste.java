package no.nav.foreldrepenger.domene.vedtak;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface KanVedtaketIverksettesTjeneste {

    /**
     * Sjekker om vedtaket kan iverksettes basert på om det finnes en løpende ytelse fra infotrygd,
     * hva den nyeste registrerte hendelsen fra infotrygd er, samt hvorvidt FOM datoen til infotrygd
     * hendelsen er lik eller tidligere enn startdato for ytelsen i VL
     *
     * @param behandling en behandling som har beregningsgrunnlag, beregningsresultat, og
     *                  tilhørende beregningsresultat perioder
     * @return boolean - True hvis vedtaket kan iversettes, false hvis det ikke kan iverksettes.
     */
    boolean kanVedtaketIverksettes(Behandling behandling);

}
