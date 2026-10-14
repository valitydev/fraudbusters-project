package dev.vality.fraudbusters.notificator.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message {

    private String from;
    private String[] to;
    private String subject;
    private String content;
    private Attachment attachment;

    private String partyId;
    private long claimId;

}
