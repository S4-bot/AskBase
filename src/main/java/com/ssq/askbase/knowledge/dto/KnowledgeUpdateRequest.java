package com.ssq.askbase.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeUpdateRequest {

    @Size(min = 1, max = 100, message = "名称长度必须在1到100个字符之间")
    private String name;

    @Size(min = 0, max = 500, message = "描述长度必须在0到500个字符之间")
    private String description;
}
