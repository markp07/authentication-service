package nl.markpost.demo.weather.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Administrative {
    private String name;
    private String description;
    private String isoName;
    private int order;
    private int adminLevel;
    private String isoCode;
    private String wikidataId;
    private long geonameId;
}

