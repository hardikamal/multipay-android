package com.multipay.android.dtos;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;

public class LoginResponseDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty(value="Valid")
        private Boolean valid;
        @JsonProperty(value="Message")
        private String message;
        @JsonProperty(value="UserId")
        private Integer userId;
        @JsonProperty(value="Username")
        private String userName;

        public Boolean getValid() {
                return this.valid;
        }

        public void setValid(Boolean valid) {
                this.valid = valid;
        }

        public String getMessage() {
                return this.message;
        }

        public void setMessage(String message) {
                this.message = message;
        }

        public Integer getUserId() {
                return this.userId;
        }

        public void setUserId(Integer userId) {
                this.userId = userId;
        }

        public String getUserName() {
                return this.userName;
        }

        public void setUserName(String userName) {
                this.userName = userName;
        }
}