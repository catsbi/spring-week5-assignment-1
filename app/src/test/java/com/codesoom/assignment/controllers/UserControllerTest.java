package com.codesoom.assignment.controllers;

import com.codesoom.assignment.UserNotFoundException;
import com.codesoom.assignment.application.UserService;
import com.codesoom.assignment.domain.User;
import com.codesoom.assignment.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dozermapper.core.Mapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//        회원 생성하기 - POST /user
//        회원 수정하기 - PATCH /user/{id} TODO   - dto validation
//        회원 삭제하기 - DELETE /user/{id}
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Mapper dozerMapper;

    @MockBean
    UserService userService;

    private User user;

    private UserDto validUpdateUserDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("양승인")
                .password("1234")
                .email("rhfpdk92@naver.com")
                .build();

        validUpdateUserDto = UserDto.builder()
                .name("양철수")
                .email("newId@naver.com")
                .password("12341234")
                .build();
    }

    @Nested
    @DisplayName("POST /user 요청은")
    class Describe_createUser {
        @Nested
        @DisplayName("requestbody에 회원의 정보가 있으면")
        class Context_exist_user {
            @BeforeEach
            void setUp() {
                given(userService.createUser(any(UserDto.class)))
                        .willReturn(dozerMapper.map(user, UserDto.class));
            }

            @Test
            @DisplayName("응답코드는 201이며 생성한 회원를 응답한다.")
            void it_return_createdUser() throws Exception {
                mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(user)))
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("name").exists());
            }
        }

        @Nested
        @DisplayName("requestbody에 user가 없으면")
        class Context_does_not_exist_user {
            @Test
            @DisplayName("응답코드는 400을 응답한다")
            void it_return_bad_request() throws Exception {
                mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
            }
        }
        @Nested
        @DisplayName("user에 파라미터가 없으면")
        class Context_user_does_not_have_parameter {
            User userWithoutName  = User.builder()
                    .id(1L)
                    .password("1234")
                    .email("rhfpdk92@naver.com")
                    .build();

            @Test
            @DisplayName("응답코드는 400며 에러메세지를 응답한다.")
            void it_return_createdUser() throws Exception {
                mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(userWithoutName)))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("PATCH /user/{id} 요청은")
    class Describe_updateUser {
        @Nested
        @DisplayName("id가 존재하고 requestbody에 회원의 정보가 있으면")
        class Context_exist_id_and_userdto {
            @BeforeEach
            void setUp() {
                given(userService.updateUser(eq(1L), any(UserDto.class)))
                        .willReturn(validUpdateUserDto);
            }

            @Test
            @DisplayName("응답코드는 200이며 수정된 회원를 응답한다.")
            void it_return_updatedUser() throws Exception {
                mockMvc.perform(patch("/user/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(validUpdateUserDto)))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("name").value("양철수"))
                        .andExpect(jsonPath("email").value("newId@naver.com"))
                        .andExpect(jsonPath("password").value("12341234"));
            }
        }

        @Nested
        @DisplayName("id가 존재하지 않으면")
        class Context_does_not_exist_user {

            @BeforeEach
            void setUp() {
                given(userService.updateUser(eq(100L), any(UserDto.class)))
                        .willThrow(new UserNotFoundException(100L));
            }

            @Test
            @DisplayName("응답코드는 404이며 에러메세지를 응답한다")
            void it_return_not_found() throws Exception {
                mockMvc.perform(patch("/user/{id}", 100L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(validUpdateUserDto)))
                        .andDo(print())
                        .andExpect(status().isNotFound());
            }
        }
    }
}


