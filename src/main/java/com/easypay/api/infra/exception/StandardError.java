package com.easypay.api.infra.exception;

public record StandardError(
   String message,
   Integer status
) {}
