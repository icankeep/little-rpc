package com.passer.littlerpc.domain;

import lombok.*;

/**
 * @author passer
 * @time 2021/3/28 12:53 下午
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Example {
    private Long id;
    private String exampleName;
}
