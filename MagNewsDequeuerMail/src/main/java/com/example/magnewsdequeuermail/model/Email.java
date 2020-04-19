package com.example.magnewsdequeuermail.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Setter
@Getter
public class Email {
    private String sender;
    private String receiver;
    private String object;
    private String body;
}
