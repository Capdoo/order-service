package org.rnontol.dto;

import lombok.Data;

@Data
public class ClientDto {
    public Long id;
    public String firstName;
    public String lastName;
    public String document;
    public int age;
    public String type;

}
