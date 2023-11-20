package my.modulesource.aop_log.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
@Setter
public class UserDto {
    @NotNull
    public String name;
    public Integer age;
}
