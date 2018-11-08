package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import java.util.List;

public interface PersonInformasjon {

    /**
     * Relasjoner mellom to aktører
     *
     * @return entitet
     */
    List<PersonRelasjon> getRelasjoner();

    /**
     * Alle relevante aktørers personopplysninger
     *
     * @return entitet
     */
    List<Personopplysning> getPersonopplysninger();

    /**
     * Alle relevante aktørers personstatuser med gyldighetstidspunkt (fom, tom)
     * <p>
     * Det er kun hentet inn historikk for søker, de andre aktørene ligger inne med perioden fødselsdato -> dødsdato/tidenes ende
     *
     * @return entitet
     */
    List<Personstatus> getPersonstatus();

    /**
     * Alle relevante aktørers statsborgerskap med gyldighetstidspunkt (fom, tom)
     * <p>
     * Det er kun hentet inn historikk for søker, de andre aktørene ligger inne med perioden fødselsdato -> dødsdato/tidenes ende
     *
     * @return entitet
     */
    List<Statsborgerskap> getStatsborgerskap();

    /**
     * Alle relevante aktørers adresser med gyldighetstidspunkt (fom, tom)
     * <p>
     * Det er kun hentet inn historikk for søker, de andre aktørene ligger inne med perioden fødselsdato -> dødsdato/tidenes ende
     *
     * @return entitet
     */
    List<PersonAdresse> getAdresser();

}
