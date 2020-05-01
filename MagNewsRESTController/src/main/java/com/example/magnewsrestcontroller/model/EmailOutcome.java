package com.example.magnewsrestcontroller.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Setter
@Getter
public class EmailOutcome implements Serializable {

    private Integer id;
    private Outcome outcome;

    public EmailOutcome(Outcome outcome) {
        this.outcome = outcome;
    }
}
