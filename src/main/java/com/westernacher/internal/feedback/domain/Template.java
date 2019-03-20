package com.westernacher.internal.feedback.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "template")
public class Template {

    @Id
    private String id;
    private String unit;
    private List<String> questions;

}
