package com.spring.exercise.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.ErrorResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
						 AuthenticationException authException) throws IOException {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setMessage(AppMessages.NOT_AUTHORIZED_ERROR);
		Map<Object, List<Object> > errorMap = Map.of("errors", List.of(errorResponse));
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().write(new ObjectMapper().writeValueAsString(errorMap));
	}
}