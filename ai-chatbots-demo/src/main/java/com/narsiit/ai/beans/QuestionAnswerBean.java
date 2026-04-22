package com.narsiit.ai.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class QuestionAnswerBean {
    private String question;
    private String answer;
}
