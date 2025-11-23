package com.healthbridge.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
	private boolean success;
	private T data;
	private String error;

	//  Success response
	public static <T> ApiResult<T> ok(T data) {
		ApiResult<T> result = new ApiResult<>();
		result.setSuccess(true);
		result.setData(data);
		return result;
	}

	//  Failure with only message
	public static <T> ApiResult<T> fail(String message) {
		ApiResult<T> result = new ApiResult<>();
		result.setSuccess(false);
		result.setError(message);
		return result;
	}

	//  Failure with message and extra data (like validation errors)
	public static <T> ApiResult<T> fail(String message, T data) {
		ApiResult<T> result = new ApiResult<>();
		result.setSuccess(false);
		result.setError(message);
		result.setData(data);
		return result;
	}
}
