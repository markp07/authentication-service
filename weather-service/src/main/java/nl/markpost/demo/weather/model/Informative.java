package nl.markpost.demo.weather.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Informative {
    private String name;
    private String description;
    private int order;
    private String isoCode;
    private String wikidataId;
    private long geonameId;
}

