package com.disasterHelp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultForm {

    @JsonProperty("info")
    private String info;
    @JsonProperty("result")
    private String result;

}
