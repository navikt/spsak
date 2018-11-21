<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ns2="urn:no:nav:vedtak:felles:xml:vedtak:v1"
               >


    <xsl:template match="ns2:vedtak" >
        <html>
            <body>
                <!--Fagopplysninger-->
                <h3>Fagsak ID <xsl:value-of select="fagsakId" /> </h3>
                <strong>Fagsaktype: &#160;</strong><xsl:value-of select="fagsakType" /> &#xa; <br />
                <strong>Tema: &#160;</strong><xsl:value-of select="tema" /> &#xa; <br />
                <strong>Behandlingstema: &#160;</strong><xsl:value-of select="behandlingsTema" /> <br />
                <!--Skal saksbehanderID være med om det ikke ligger i xml ?-->
                <strong>Ansvarlig SaksbehandlerId: &#160;</strong><xsl:value-of select="ansvarligSaksbehandlerIdent" /> <br />
                <strong>Ansvarlig BeslutterId: &#160;</strong><xsl:value-of select="ansvarligBeslutterIdent" /> <br />
                <strong>Behandlendeenhet: &#160;</strong><xsl:value-of select="behandlendeEnhet" /> <br />


                <xsl:call-template name="datoKonverteringHvisFinnesStrong">
                    <xsl:with-param name="dato" select="soeknadsdato"/>
                    <xsl:with-param name="tekst" select="'Søknadsdato: '"/>
                </xsl:call-template>

                <xsl:call-template name="datoKonverteringHvisFinnesStrong">
                    <xsl:with-param name="dato" select="klagedato"/>
                    <xsl:with-param name="tekst" select="'Klagedato: '"/>
                </xsl:call-template>

                <xsl:call-template name="datoKonverteringHvisFinnesStrong">
                    <xsl:with-param name="dato" select="vedtaksdato"/>
                    <xsl:with-param name="tekst" select="'Vedtaksdato: '"/>
                </xsl:call-template>

                <!--PersonOpplysninger-->
                <xsl:apply-templates select="personOpplysninger"/>
                <!--Behandlingsresultat-->
                <xsl:apply-templates select="behandlingsresultat"/>

            </body>
        </html>
    </xsl:template>

    <!--Support templates-->
    <xsl:template name="booleanTilJaNei">
        <xsl:param name="boolean" />
        <xsl:param name="tekst" />
        <xsl:value-of select="$tekst"/>
        <xsl:choose>
            <xsl:when test="$boolean = 'true'"> Ja</xsl:when>
            <xsl:when test="$boolean = 'false'">Nei</xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
        <br />
    </xsl:template>

    <xsl:template name = "datoKonverteringHvisFinnes">
        <xsl:param name="dato" />
        <xsl:param name="tekst" />
        <xsl:if test="$dato != ''">
            <xsl:value-of select="$tekst"/>
            <xsl:call-template name="datoKonvertering">
                <xsl:with-param name="dato" select="$dato"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name = "datoKonverteringHvisFinnesStrong">
        <xsl:param name="dato" />
        <xsl:param name="tekst" />
        <xsl:if test="$dato != ''">
            <strong><xsl:value-of select="$tekst"/></strong>
            <xsl:call-template name="datoKonvertering">
                <xsl:with-param name="dato" select="$dato"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name = "datoKonvertering">
        <xsl:param name="dato" />
        <xsl:if test="$dato != ''">
            <xsl:value-of select="substring($dato,9,2)" />.
            <xsl:value-of select="substring($dato,6,2)" />.
            <xsl:value-of select="substring($dato,0,5)" /><br />
        </xsl:if>
    </xsl:template>

    <xsl:template name ="sokersKjonnLowerCase">
        <xsl:param name="soekersKjoenn"/>
        Søkers kjønn:
        <xsl:choose>
            <xsl:when test="$soekersKjoenn = 'KVINNE'">
                Kvinne <br />
            </xsl:when>
            <xsl:when test="$soekersKjoenn = 'MANN'">
                Mann <br />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$soekersKjoenn"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--Support templates slutt-->


    <xsl:template match="behandlingsresultat">
        <h4>Behandlingsresultat</h4>
        <hr />
        <strong>Resultat: &#160;</strong> <xsl:value-of select="behandlingsresultat" /><br />
        <strong>Behandlingstype: &#160;</strong> <xsl:value-of select="behandlingstype" /><br />
        <strong>BehandlingsId: &#160; </strong><xsl:value-of select="behandlingsId" /><br /><br />
        <xsl:for-each select="vurderteVilkaar">
            <!--Konstruksjon av alle vurderte vilkår:-->
            <xsl:for-each select="vilkaar">
                <strong>Vilkår: &#160; <xsl:value-of select="type" /></strong><br />
                <xsl:choose>
                    <!--Opplysningsplikt-->
                    <xsl:when test="type = 'Opplysningsplikt'">
                        <xsl:for-each select="vilkaarsgrunnlag">

                            <xsl:call-template name="booleanTilJaNei">
                                <xsl:with-param name="boolean" select="elektroniskSoeknad"/>
                                <xsl:with-param name="tekst" select="'Elektronisk Søknad: '"/>
                            </xsl:call-template>

                            <xsl:call-template name="booleanTilJaNei">
                                <xsl:with-param name="boolean" select="erBarnetFoedt"/>
                                <xsl:with-param name="tekst" select="'Er barnet født: '"/>
                            </xsl:call-template>

                            <xsl:call-template name="booleanTilJaNei">
                                <xsl:with-param name="boolean" select="erSoeknadenKomplett"/>
                                <xsl:with-param name="tekst" select="'Søknad komplett: '"/>
                            </xsl:call-template>

                        </xsl:for-each>
                    </xsl:when>
                    <!-- Opplysningsplikt slutt-->

                    <xsl:when test="type = 'Medlemskap'">
                        <xsl:for-each select="vilkaarsgrunnlag">
                            <xsl:if test="personstatus != ''">
                                Personstatus: &#160; <xsl:value-of select="personstatus" /><br />
                            </xsl:if>

                            <xsl:call-template name="booleanTilJaNei">
                                <xsl:with-param name="boolean" select="erBrukerMedlem"/>
                                <xsl:with-param name="tekst" select="'Bruker er medlem: '"/>
                            </xsl:call-template>

                            <xsl:call-template name="booleanTilJaNei">
                                <xsl:with-param name="boolean" select="erBrukerBosatt"/>
                                <xsl:with-param name="tekst" select="'Bruker er bosatt: '"/>
                            </xsl:call-template>

                            <xsl:call-template name="booleanTilJaNei">
                                <xsl:with-param name="boolean" select="harBrukerOppholdsrett"/>
                                <xsl:with-param name="tekst" select="'Bruker har oppholdsrett:  '"/>
                            </xsl:call-template>

                            <xsl:call-template name="booleanTilJaNei">
                                <xsl:with-param name="boolean" select="harBrukerLovligOppholdINorge"/>
                                <xsl:with-param name="tekst" select="'Bruker har lovlig opphold i Norge:  '"/>
                            </xsl:call-template>

                            <xsl:call-template name="booleanTilJaNei">
                                <xsl:with-param name="boolean" select="erBrukerNordiskstatsborger"/>
                                <xsl:with-param name="tekst" select="'Bruker er nordisk statsborger:  '"/>
                            </xsl:call-template>

                            <xsl:call-template name="booleanTilJaNei">
                                <xsl:with-param name="boolean" select="erBrukerBorgerAvEUEOS"/>
                                <xsl:with-param name="tekst" select="'Bruker er borger av EU/EØS: '"/>
                            </xsl:call-template>

                            <xsl:call-template name="booleanTilJaNei">
                                <xsl:with-param name="boolean" select="erBrukerPliktigEllerFrivilligMedlem"/>
                                <xsl:with-param name="tekst" select="'Bruker er pliktig eller frivilig medlem: '"/>
                            </xsl:call-template>

                        </xsl:for-each>
                    </xsl:when>
                    <!--Medlemskap slutt-->

                    <xsl:when test="type = 'Adopsjon'">
                        <xsl:for-each select="vilkaarsgrunnlag">

                            <xsl:call-template name="sokersKjonnLowerCase">
                                <xsl:with-param name="soekersKjoenn" select="soekersKjoenn"/>
                            </xsl:call-template>


                            <xsl:if test="adopsjon != ''">
                                <xsl:call-template name="booleanTilJaNei">
                                    <xsl:with-param name="boolean" select="adopsjon/erEktefellesBarn"/>
                                    <xsl:with-param name="tekst" select="'Ektefelles barn: '"/>
                                </xsl:call-template>

                                <xsl:call-template name="booleanTilJaNei">
                                    <xsl:with-param name="boolean" select="adopsjon/erMannAdoptererAlene"/>
                                    <xsl:with-param name="tekst" select="'Mann adopterer alene: '"/>
                                </xsl:call-template>

                                <xsl:call-template name="datoKonverteringHvisFinnes">
                                    <xsl:with-param name="dato" select="adopsjon/omsorgsovertakelsesdato"/>
                                    <xsl:with-param name="tekst" select="'Omsorgsovertakelsesdato: '"/>
                                </xsl:call-template>
                            </xsl:if>

                        </xsl:for-each>
                    </xsl:when>
                    <!-- Adopsjon slutt-->

                    <xsl:when test="type = 'Fødsel'">
                        <xsl:for-each select="vilkaarsgrunnlag">
                            <xsl:call-template name="sokersKjonnLowerCase">
                                <xsl:with-param name="soekersKjoenn" select="sokersKjoenn"/>
                            </xsl:call-template>

                            <xsl:call-template name="datoKonverteringHvisFinnes">
                                <xsl:with-param name="dato" select="foedselsdatoBarn"/>
                                <xsl:with-param name="tekst" select="'Fødselsdato barn: '"/>
                            </xsl:call-template>

                            Antall barn: &#160; <xsl:value-of select="antallBarn" /><br />
                            <xsl:if test="soekersRolle != ''">
                                Søkers rolle: &#160; <xsl:value-of select="soekersRolle" /><br />
                            </xsl:if>

                            <xsl:call-template name="datoKonverteringHvisFinnes">
                                <xsl:with-param name="dato" select="termindato"/>
                                <xsl:with-param name="tekst" select="'Termindato: '"/>
                            </xsl:call-template>

                            <xsl:call-template name="datoKonverteringHvisFinnes">
                                <xsl:with-param name="dato" select="soeknadsdato"/>
                                <xsl:with-param name="tekst" select="'Søknadsdato: '"/>
                            </xsl:call-template>

                        </xsl:for-each>
                    </xsl:when>
                    <!--Fødsel slutt-->

                    <xsl:when test="type = 'Søknadsfrist'">
                        <xsl:for-each select="vilkaarsgrunnlag">
                            <xsl:call-template name="booleanTilJaNei">
                                <xsl:with-param name="boolean" select="elektroniskSoeknad"/>
                                <xsl:with-param name="tekst" select="'Elektronisksøknad: '"/>
                            </xsl:call-template>

                            <xsl:call-template name="datoKonverteringHvisFinnes">
                                <xsl:with-param name="dato" select="skjaeringstidspunkt"/>
                                <xsl:with-param name="tekst" select="'Skjæringstidspunkt: '"/>
                            </xsl:call-template>

                            <xsl:call-template name="datoKonverteringHvisFinnes">
                                <xsl:with-param name="dato" select="soeknadMottattDato"/>
                                <xsl:with-param name="tekst" select="'Søknad mottatt: '"/>
                            </xsl:call-template>
                        </xsl:for-each>
                    </xsl:when>

                    <xsl:otherwise>
                    </xsl:otherwise>

                </xsl:choose>
                Utfall: &#160; <xsl:value-of select="utfall" /><br />
                <xsl:if test="utfallMerknad != ''">
                    Merknad for utfall: &#160; <xsl:value-of select="utfallMerknad" /><br />
                </xsl:if>
                Vurdert: <xsl:value-of select="@vurdert" /><br />
                <br />
            </xsl:for-each>
            <!--Konstruksjon av alle vurderte vilkår slutt -->
        </xsl:for-each>
        <xsl:for-each select="beregningsresultat">
            <h4>Beregningsresultat</h4>
            <hr />
            <strong>Beregninsgrunnlag </strong><br />
            Antall barn: &#160; <xsl:value-of select="beregningsgrunnlag/antallBarn" /><br />
            Sats (kr) : &#160; <xsl:value-of select="beregningsgrunnlag/sats" /><br />
            Tilkjent ytelse (kr): &#160; <xsl:value-of select="tilkjentYtelse/beloep" /><br /><br />
        </xsl:for-each>
        <xsl:for-each select="manuelleVurderinger">
            <strong>Manuelle vurderinger:    </strong><br /><br />
            <div style= "position: relative; left:40px;">
                <xsl:for-each select="manuellVurdering">
                    <strong>Vurdering: &#160; <xsl:value-of select="aksjonspunkt" /></strong><br />
                    <xsl:if test="gjelderVilkaar != ''">
                        Gjelder vilkår: &#160; <xsl:value-of select="gjelderVilkaar" /><br />
                    </xsl:if>
                    <xsl:if test="saksbehandlersBegrunnelse != ''">
                        Saksbehandlers begrunnelse: &#160; <xsl:value-of select="saksbehandlersBegrunnelse" /><br />
                    </xsl:if>
                    <br />
                </xsl:for-each>
            </div>
        </xsl:for-each>
    </xsl:template>


    <xsl:template match = "personOpplysninger">
        <h4>Personopplysninger</h4>
        <hr />
        <strong>Bruker</strong><br />
        <xsl:for-each select="bruker">
            Navn: &#160; <xsl:value-of select="navn" /><br />
            NorskIdent: &#160; <xsl:value-of select="norskIdent" /><br />
            Kjønn: &#160; <xsl:value-of select="kjoenn" /><br />
            Statsborgerskap: &#160; <xsl:value-of select="statsborgerskap" /><br />
            Personstatus: &#160; <xsl:value-of select="personstatus" /><br />
            Region: &#160; <xsl:value-of select="region" /><br />
            Sivilstand: &#160; <xsl:value-of select="sivilstand" /><br />
            <xsl:call-template name="datoKonverteringHvisFinnes">
                <xsl:with-param name="dato" select="doedsdato"/>
                <xsl:with-param name="tekst" select="'Dødsdato: '"/>
            </xsl:call-template>
            <xsl:call-template name="datoKonverteringHvisFinnes">
                <xsl:with-param name="dato" select="foedselssdato"/>
                <xsl:with-param name="tekst" select="'Fødselsdato: '"/>
            </xsl:call-template>
        </xsl:for-each>
        <br />

        <xsl:if test="verge != ''">
            <strong>Verge</strong><br />
            Verges navn: &#160; <xsl:value-of select="verge/navn" /><br />
            Vergetype: &#160; <xsl:value-of select="verge/vergetype" /><br />

            <xsl:call-template name="booleanTilJaNei">
                <xsl:with-param name="boolean" select="verge/tvungenForvaltning"/>
                <xsl:with-param name="tekst" select="'Tvungen forvaltning: '"/>
            </xsl:call-template>

            Mandattekst: &#160; <xsl:value-of select="verge/mandattekst" /><br />
            <xsl:call-template name="datoKonverteringHvisFinnes">
                <xsl:with-param name="dato" select="verge/vedtaksdato"/>
                <xsl:with-param name="tekst" select="'Vedtaksdato: '"/>
            </xsl:call-template>

            <xsl:if test="verge/gyldighetsperiode != ''">
                <xsl:call-template name="datoKonverteringHvisFinnes">
                    <xsl:with-param name="dato" select="verge/gyldighetsperiode/fom"/>
                    <xsl:with-param name="tekst" select="'Periode gyldig fra og med: '"/>
                </xsl:call-template>

                <xsl:call-template name="datoKonverteringHvisFinnes">
                    <xsl:with-param name="dato" select="verge/gyldighetsperiode/tom"/>
                    <xsl:with-param name="tekst" select="'Periode gyldig til og med: '"/>
                </xsl:call-template>
            </xsl:if>
            <br />
        </xsl:if>

        <!-- Adresser-->
        <xsl:for-each select="adresse">
            <strong><xsl:value-of select="addresseType" /></strong><br />
            Mottakers navn: &#160; <xsl:value-of select="mottakersNavn" /><br />
            <xsl:if test="addresselinje1 != ''">
                Adresselinje 1: &#160; <xsl:value-of select="addresselinje1" /><br />
            </xsl:if>
            <xsl:if test="addresselinje2 != ''">
                Adresselinje 2: &#160; <xsl:value-of select="addresselinje2" /><br />
            </xsl:if>
            <xsl:if test="addresselinje3 != ''">
                Adresselinje 3: &#160; <xsl:value-of select="addresselinje3" /><br />
            </xsl:if>

            <xsl:if test="postnummer != ''">
                Postnummer: &#160;<xsl:value-of select="postnummer" /><br />
            </xsl:if>
            Land: &#160; <xsl:value-of select="land" /><br /><br />
        </xsl:for-each>

        <!-- Fødsel-->
        <xsl:if test="foedsel != ''">
            <strong>Fødsel</strong><br />
            Antall barn: &#160; <xsl:value-of select="foedsel/antallBarn" /><br />
            <xsl:call-template name="datoKonverteringHvisFinnes">
                <xsl:with-param name="dato" select="foedselsdato"/>
                <xsl:with-param name="tekst" select="'Fødselsdato: '"/>
            </xsl:call-template>
            <br />
        </xsl:if>
        <!-- Adopsjon-->
        <xsl:if test="adopsjon != ''">
            <strong>Adopsjon</strong><br />
            <xsl:for-each select="adopsjon/adopsjonsbarn">
                <xsl:call-template name="datoKonverteringHvisFinnes">
                    <xsl:with-param name="dato" select="foedselsdato"/>
                    <xsl:with-param name="tekst" select="'Fødselsdato: '"/>
                </xsl:call-template>
            </xsl:for-each>

            <xsl:call-template name="booleanTilJaNei">
                <xsl:with-param name="boolean" select="adopsjon/erEktefellesBarn"/>
                <xsl:with-param name="tekst" select="'Ektefelles barn: '"/>
            </xsl:call-template>

            <xsl:call-template name="booleanTilJaNei">
                <xsl:with-param name="boolean" select="adopsjon/erMannAdoptererAlene"/>
                <xsl:with-param name="tekst" select="'Er mann som adopterer alene: '"/>
            </xsl:call-template>

            <xsl:call-template name="datoKonverteringHvisFinnes">
                <xsl:with-param name="dato" select="adopsjon/omsorgsovertakelsesdato"/>
                <xsl:with-param name="tekst" select="'Omsorgsovertakelsesdato: '"/>
            </xsl:call-template>
            <br />

        </xsl:if>

        <!-- Omsorgsovertakelse-->
        <xsl:if test="omsorgsovertakelse != ''">
            <strong>Omsorgsovertakelse</strong><br />
            <xsl:call-template name="datoKonverteringHvisFinnes">
                <xsl:with-param name="dato" select="omsorgsovertakelse/omsorgsovertakelsesdato"/>
                <xsl:with-param name="tekst" select="'Omsorgsovertakelsesdato: '"/>
            </xsl:call-template><br />
        </xsl:if>

        <!--- Terminbekreftelse -->
        <xsl:if test="terminbekreftelse != ''">
            <strong>Terminbekreftelse</strong><br />
            <xsl:call-template name="datoKonverteringHvisFinnes">
                <xsl:with-param name="dato" select="terminbekreftelse/termindato"/>
                <xsl:with-param name="tekst" select="'Termindato: '"/>
            </xsl:call-template>
            <xsl:call-template name="datoKonverteringHvisFinnes">
                <xsl:with-param name="dato" select="terminbekreftelse/utstedtDato"/>
                <xsl:with-param name="tekst" select="'UtstedtDato: '"/>
            </xsl:call-template>
            Atnall barn: &#160; <xsl:value-of select="terminbekreftelse/antallBarn" /><br /><br />
        </xsl:if>

        <!-- familierelasjonser -->
        <xsl:if test="familierelasjoner != ''">
            <strong>Familierelasjoner</strong>
            <xsl:for-each select="familierelasjoner/familierelasjon"><br/>
                Relasjontype : <xsl:value-of select="relasjon" /><br />
                Navn: &#160; <xsl:value-of select="tilPerson/navn" /><br />
                NorskIdent: &#160; <xsl:value-of select="tilPerson/norskIdent" /><br />
                Kjønn: &#160; <xsl:value-of select="tilPerson/kjoenn" /><br />
                Statsborgerskap: &#160; <xsl:value-of select="tilPerson/statsborgerskap" /><br />
                Personstatus: &#160; <xsl:value-of select="tilPerson/personstatus" /><br />
                Region: &#160; <xsl:value-of select="tilPerson/region" /><br />
                Sivilstand: &#160; <xsl:value-of select="tilPerson/sivilstand" /><br />

                <xsl:call-template name="datoKonverteringHvisFinnes">
                    <xsl:with-param name="dato" select="tilPerson/doedsdato"/>
                    <xsl:with-param name="tekst" select="'Dødsdato: '"/>
                </xsl:call-template>

                <xsl:call-template name="datoKonverteringHvisFinnes">
                    <xsl:with-param name="dato" select="tilPerson/foedselsdato"/>
                    <xsl:with-param name="tekst" select="'Fødselsdato: '"/>
                </xsl:call-template>
                <br/>
            </xsl:for-each>
            <br />
        </xsl:if>

        <!-- Medlemskapsperioder -->
        <xsl:if test="medlemskapsperioder != ''">
            <strong>Medlemskapsperioder</strong>
            <xsl:for-each select="medlemskapsperioder/medlemskapsperiode"><br/>
                <xsl:if test="periode != ''">
                    <xsl:call-template name="datoKonverteringHvisFinnes">
                        <xsl:with-param name="dato" select="periode/fom"/>
                        <xsl:with-param name="tekst" select="'Periode fra og med: '"/>
                    </xsl:call-template>

                    <xsl:call-template name="datoKonverteringHvisFinnes">
                        <xsl:with-param name="dato" select="periode/tom"/>
                        <xsl:with-param name="tekst" select="'Periode til og med: '"/>
                    </xsl:call-template>
                </xsl:if>
                Medlemskaptype : <xsl:value-of select="medlemskaptype" /><br />
                Dekningtype : <xsl:value-of select="dekningtype" /><br />

                <xsl:call-template name="datoKonverteringHvisFinnes">
                    <xsl:with-param name="dato" select="beslutningsdato"/>
                    <xsl:with-param name="tekst" select="'Beslutningsdato: '"/>
                </xsl:call-template>
                Lovvalgsland : <xsl:value-of select="lovvalgsland" /><br />
                Studieland : <xsl:value-of select="studieland" /><br />
                <xsl:call-template name="booleanTilJaNei">
                    <xsl:with-param name="boolean" select="erMedlem"/>
                    <xsl:with-param name="tekst" select="'Er medlem: '"/>
                </xsl:call-template>
                <br/>
            </xsl:for-each>
            <br />
        </xsl:if>

        <!-- Relaterte ytelser -->
        <xsl:if test="relaterteYtelser != ''">
            <strong>Medlemskapsperioder</strong>
            <xsl:for-each select="relaterteYtelser/relatertYtelse"><br/>
                Type : <xsl:value-of select="type" /><br />
                Tilstand : <xsl:value-of select="tilstand" /><br />
                Tema : <xsl:value-of select="tema" /><br />
                Behandlingstema : <xsl:value-of select="behandlingstema" /><br />

                <xsl:call-template name="datoKonverteringHvisFinnes">
                    <xsl:with-param name="dato" select="vedtaksdato"/>
                    <xsl:with-param name="tekst" select="'Vedtaksdato: '"/>
                </xsl:call-template>

                <xsl:call-template name="datoKonverteringHvisFinnes">
                    <xsl:with-param name="dato" select="iverksettelsesdato"/>
                    <xsl:with-param name="tekst" select="'Iverksettelsesdato: '"/>
                </xsl:call-template>

                <xsl:call-template name="datoKonverteringHvisFinnes">
                    <xsl:with-param name="dato" select="opphoerFom"/>
                    <xsl:with-param name="tekst" select="'Opphører fra og med: '"/>
                </xsl:call-template>
                Status : <xsl:value-of select="status" /><br />
                Resultat : <xsl:value-of select="resultat" /><br />
                <br/>
            </xsl:for-each>
            <br />
        </xsl:if>


        <!--Inntekter-->
        <xsl:if test="inntekter != ''">
            <strong>Inntekter</strong><br /><br />
            <xsl:for-each select="inntekter/inntekt">
                <strong>Inntekt:</strong><br />

                Mottaker aktørId: &#160; <xsl:value-of select="mottakerAktoerId" /><br />
                Arbeidsgiver: &#160; <xsl:value-of select="arbeidsgiver" /><br />

                <xsl:call-template name="booleanTilJaNei">
                    <xsl:with-param name="boolean" select="ytelse"/>
                    <xsl:with-param name="tekst" select="'Ytelse: '"/>
                </xsl:call-template>

                <xsl:if test="periode != ''">
                    <xsl:call-template name="datoKonverteringHvisFinnes">
                        <xsl:with-param name="dato" select="periode/fom"/>
                        <xsl:with-param name="tekst" select="'Periode fra og med: '"/>
                    </xsl:call-template>
                    <xsl:call-template name="datoKonverteringHvisFinnes">
                        <xsl:with-param name="dato" select="periode/tom"/>
                        <xsl:with-param name="tekst" select="'Periode til og med: '"/>
                    </xsl:call-template>
                </xsl:if>
                Beløp: &#160; <xsl:value-of select="beloep" /><br />
            </xsl:for-each>
        </xsl:if>

    </xsl:template>
</xsl:stylesheet>
