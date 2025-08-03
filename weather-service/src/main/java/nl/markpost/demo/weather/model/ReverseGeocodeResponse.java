package nl.markpost.demo.weather.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReverseGeocodeResponse {

    private String city;

    private String locality;

    private String countryName;

    private double latitude;

    private String lookupSource;

    private double longitude;

    private String localityLanguageRequested;

    private String continent;

    private String continentCode;

    private String countryCode;

    private String principalSubdivision;

    private String principalSubdivisionCode;

    private String postcode;

    private String plusCode;

    private LocalityInfo localityInfo;
}
