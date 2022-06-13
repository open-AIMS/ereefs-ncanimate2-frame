<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor version="1.0.0"
  xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd"
  xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

  <NamedLayer>
    <Name>river_catchments</Name>
    <UserStyle>
      <Name>river_catchments</Name>
      <Title>River catchments</Title>
      <FeatureTypeStyle>

        <!-- Mulgrave -->
        <Rule>
          <Name>MulgraveCatchment</Name>
          <Title>Mulgrave catchment</Title>

          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>BNAME</ogc:PropertyName>
              <ogc:Literal>MULGRAVE-RUSSELL RIVERS</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>

          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill">#850149</CssParameter>
              <CssParameter name="fill-opacity">0.1</CssParameter>
            </Fill>
          </PolygonSymbolizer>
        </Rule>

        <!-- Herbert -->
        <Rule>
          <Name>HerbertCatchment</Name>
          <Title>Herbert catchment</Title>

          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>BNAME</ogc:PropertyName>
              <ogc:Literal>HERBERT RIVER</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>

          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill">#425C00</CssParameter>
              <CssParameter name="fill-opacity">0.1</CssParameter>
            </Fill>
          </PolygonSymbolizer>
        </Rule>

        <!-- Pioneer -->
        <Rule>
          <Name>PioneerCatchment</Name>
          <Title>Pioneer catchment</Title>

          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>BNAME</ogc:PropertyName>
              <ogc:Literal>PIONEER RIVER</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>

          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill">#0000B4</CssParameter>
              <CssParameter name="fill-opacity">0.1</CssParameter>
            </Fill>
          </PolygonSymbolizer>
        </Rule>

        <!-- Burnett -->
        <Rule>
          <Name>BurnettCatchment</Name>
          <Title>Burnett catchment</Title>

          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>BNAME</ogc:PropertyName>
              <ogc:Literal>BURNETT RIVER</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>

          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill">#325960</CssParameter>
              <CssParameter name="fill-opacity">0.1</CssParameter>
            </Fill>
          </PolygonSymbolizer>
        </Rule>

        <!-- Logan -->
        <Rule>
          <Name>LoganCatchment</Name>
          <Title>Logan catchment</Title>

          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>BNAME</ogc:PropertyName>
              <ogc:Literal>LOGAN-ALBERT RIVERS</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>

          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill">#69280B</CssParameter>
              <CssParameter name="fill-opacity">0.1</CssParameter>
            </Fill>
          </PolygonSymbolizer>
        </Rule>

      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>
