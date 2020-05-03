package com.example.magnewsrestcontroller.model;

import java.io.Serializable;

public class EmailOutcome implements Serializable {

    private Integer id;
    private Outcome outcome;

    public EmailOutcome() {}

    public EmailOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public EmailOutcome(Integer id, Outcome outcome) {
        this.id = id;
        this.outcome = outcome;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    @Override
    public String toString() {
        return "EmailOutcome{" +
                "id=" + id +
                ", outcome=" + outcome +
                '}';
    }
}
