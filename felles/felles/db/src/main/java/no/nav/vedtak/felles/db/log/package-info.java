/**
 * Utility klasser for forbedret logging av databasekall (enn hva eks. Hibernate tilbyr).
 * Inliner parametere slik at de enklere kan trekkes ut av log og testes mot GUI.
 * 
 * Zero overhead når ikke skrudd på.
 * 
 * Kun til bruk i enhetstesting, testmiljøer.
 * 
 * <h3>Hvordan enable i koden:</h3>
 * Bruk enten {@link no.nav.vedtak.felles.db.log.FastDataSourceSpy} for å gjøre klar en Datasource slik at den logger
 * ved behov, e
 * eller mer spesifikt {@link no.nav.vedtak.felles.db.log.FastConnectionLogSpy#spy(java.sql.Connection)} for å logge kun
 * en spesiell connection.
 * Ved bruk via hibernate bør første velges, da opprettelse av connections ikke er like transparent. Ved JDBC kan siste
 * benyttes.
 * 
 * <h3>Hvordan konfigurere logger på:</h3>
 * For å skru på, sett log appender "jdbc.sql" til INFO i logback konfigurasjon. (default OFF).d
 */
package no.nav.vedtak.felles.db.log;