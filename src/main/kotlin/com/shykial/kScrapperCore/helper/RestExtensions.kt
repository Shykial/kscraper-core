package com.shykial.kScrapperCore.helper

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

fun <T> T.toResponseEntity(status: HttpStatus = HttpStatus.OK): ResponseEntity<T> = ResponseEntity(this, status)
