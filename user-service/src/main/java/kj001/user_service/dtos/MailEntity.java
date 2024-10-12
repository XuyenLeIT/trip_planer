package kj001.user_service.dtos;

import lombok.Data;

@Data
public class MailEntity {
    private String email;
    private String subject;
    private String content;
}
