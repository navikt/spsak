<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>

    <!-- Variables used by multiple templates -->
    <xsl:variable name="upper" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZÆØÅ'"/>
    <xsl:variable name="lower" select="'abcdefghijklmnopqrstuvwxyzæøå'"/>

    <!-- Put all styled content in html tags -->
    <xsl:template match="/">
        <html>
            <body>
                <xsl:apply-templates/>
            </body>
        </html>
    </xsl:template>

    <xsl:strip-space elements="*"/>

    <!-- Match all direct children of parent element (vedtak) -->
    <xsl:template match="/*/*">
        <div>
            <xsl:call-template name="setFieldname">
                <xsl:with-param name="text" select="local-name(.)"/>
            </xsl:call-template>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <!-- Match descendants of first element's children of n-generations (here 3rd to 8th gen) -->
    <xsl:template match="/*/*/*|/*/*/*/*|/*/*/*/*/*|/*/*/*/*/*/*|/*/*/*/*/*/*/*|/*/*/*/*/*/*/*/*">
        <div style="padding-left:1em">
            <xsl:call-template name="setFieldname">
                <xsl:with-param name="text" select="local-name(.)"/>
            </xsl:call-template>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <!-- Perform operation on text content on nodes matched in previous two templates -->
    <xsl:template match="text()">
        <!-- replace true/false with ja/nei otherwise normalize-space -->
        <xsl:choose>
            <xsl:when test=".='true'">ja</xsl:when>
            <xsl:when test=".='false'">nei</xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="normalize-space(.)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Transform parameter fieldname (split, transform special chars, style) -->
    <xsl:template name="setFieldname">
        <xsl:param name="text"/>
        <xsl:variable name="normalizedFieldname">
            <xsl:call-template name="splitWordOnCamelCase">
                <xsl:with-param name="string" select="$text"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="transformed">
            <xsl:call-template name="replaceWithNorChars">
                <xsl:with-param name="string" select="$normalizedFieldname"/>
            </xsl:call-template>
        </xsl:variable>
        <strong>
            <xsl:call-template name="capitalizeString">
                <xsl:with-param name="string" select="$transformed"/>
            </xsl:call-template>
            <xsl:text>: </xsl:text>
        </strong>
    </xsl:template>

    <!-- Split string on CamelCase -->
    <xsl:template name="splitWordOnCamelCase">
        <xsl:param name="string" />
        <xsl:choose>
            <xsl:when test="string-length($string) &lt; 2">
                <xsl:value-of select="$string" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="splitWordHelper">
                    <xsl:with-param name="string" select="$string" />
                    <xsl:with-param name="token" select="substring($string, 1, 1)" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="splitWordHelper">
        <xsl:param name="string" select="''" />
        <xsl:param name="token" select="''" />
        <xsl:choose>
            <xsl:when test="string-length($string) = 0" />
            <xsl:when test="string-length($token) = 0" />
            <xsl:when test="string-length($string) = string-length($token)">
                <xsl:value-of select="$token" />
            </xsl:when>
            <xsl:when test="contains($upper,substring($string, string-length($token) + 1, 1)) and contains($lower, substring($string,string-length($token), 1))">
                <xsl:value-of select="concat($token, ' ')" />
                <xsl:call-template name="splitWordHelper">
                    <xsl:with-param name="string" select="substring-after($string, $token)" />
                    <xsl:with-param name="token" select="substring($string, string-length($token), 1)" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="splitWordHelper">
                    <xsl:with-param name="string" select="$string" />
                    <xsl:with-param name="token" select="substring($string, 1, string-length($token) + 1)" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Replace occurrence of aa, ae, oe with å, æ, ø -->
    <xsl:template name="replaceWithNorChars">
        <xsl:param name="string"/>
        <!-- replace and store result in variable to prevent duplicate printing -->
        <!-- replace aa with å -->
        <xsl:variable name="replAA">
            <xsl:call-template name="replaceAll">
                <xsl:with-param name="text" select="$string"/>
                <xsl:with-param name="find" select="'aa'"/>
                <xsl:with-param name="by" select="'å'"/>
            </xsl:call-template>
        </xsl:variable>
        <!-- replace ae with æ -->
        <xsl:variable name="replAE">
            <xsl:call-template name="replaceAll">
                <xsl:with-param name="text" select="$replAA"/>
                <xsl:with-param name="find" select="'ae'"/>
                <xsl:with-param name="by" select="'æ'"/>
            </xsl:call-template>
        </xsl:variable>
        <!-- replace oe with ø -->
        <xsl:variable name="replOE">
            <xsl:call-template name="replaceAll">
                <xsl:with-param name="text" select="$replAE"/>
                <xsl:with-param name="find" select="'oe'"/>
                <xsl:with-param name="by" select="'ø'"/>
            </xsl:call-template>
        </xsl:variable>
        <!-- prints final result -->
        <xsl:value-of select="$replOE"/>
    </xsl:template>

    <!-- Capitalize first letter in string -->
    <xsl:template name="capitalizeString">
        <xsl:param name="string"/>
        <xsl:value-of select="concat(translate(
            substring($string,1,1),$lower, $upper),
            substring($string,2),
            substring(' ',1 div not(position()=last())))"/>
    </xsl:template>

    <!-- Replace occurrence of $find in $text with $by -->
    <xsl:template name="replaceAll">
        <xsl:param name="text" />
        <xsl:param name="find" />
        <xsl:param name="by" />
        <xsl:choose>
            <xsl:when test="$text = '' or $find = ''or not($find)" >
                <!-- Prevent this routine from hanging -->
                <xsl:value-of select="$text" />
            </xsl:when>
            <xsl:when test="contains($text, $find)">
                <xsl:value-of select="substring-before($text,$find)" />
                <xsl:value-of select="$by" />
                <xsl:call-template name="replaceAll">
                    <xsl:with-param name="text" select="substring-after($text,$find)" />
                    <xsl:with-param name="find" select="$find" />
                    <xsl:with-param name="by" select="$by" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
