package com.margosha.kse.calories.user_graphql_subgraph.presentation.resolver;

import com.margosha.kse.calories.user_graphql_subgraph.business.dto.UserDto;
import com.margosha.kse.calories.user_graphql_subgraph.business.service.UserService;
import com.margosha.kse.calories.user_graphql_subgraph.presentation.model.Meta;
import com.margosha.kse.calories.user_graphql_subgraph.presentation.model.Pagination;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class UserResolver {
    private final UserService userService;

    public UserResolver(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    public UserDto user(@Argument UUID id){
        return userService.getUserById(id);
    }

    @QueryMapping
    public UserDto userByEmail(@Argument @Email String email){
        return userService.getUserByEmail(email);
    }

    @QueryMapping
    public Page<UserDto> users(@Argument @Valid Pagination pagination){
        if (pagination == null) pagination = new Pagination();
        return userService.getAllUsers(pagination.getLimit(), pagination.getOffset());
    }

    @SchemaMapping(typeName = "UserPage", field = "content")
    public List<UserDto> content(Page<UserDto> page) {
        return page.getContent();
    }

    @SchemaMapping(typeName = "UserPage", field = "meta")
    public Meta meta(Page<UserDto> page) {
        return new Meta(page);
    }

    @MutationMapping("createUser")
    public UserDto createUser(@Argument @Valid UserDto input){
        return userService.createUser(input);
    }

    @MutationMapping("updateUser")
    public UserDto updateUser(@Argument UUID id, @Argument @Valid UserDto input){
        return userService.updateUser(input, id);
    }

    @MutationMapping("deleteUser")
    public Boolean deleteUser(@Argument UUID id){
        return userService.deleteUser(id);
    }

    @BatchMapping(typeName = "User", field = "dailyTarget")
    public Map<UserDto, Integer> getDailyTargets(List<UserDto> users){
        Map<UUID, Integer> dailyTargets = userService.getDailyTargets(
                users.stream().map(UserDto::getId).toList()
        );
        return users.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        user -> dailyTargets.get(user.getId())));
    }

    @EntityMapping
    public UserDto user(Map<String, Object> representation) {
        String id = (String) representation.get("id");
        log.info("Resolving User entity with id: {}", id);
        return userService.getUserById(UUID.fromString(id));
    }
}