package com.shykial.kScrapperCore.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("test")
class TestController {

    @GetMapping
    suspend fun getReactiveHelloWorld(@RequestParam name: String) =
        "Hello reactive world! $name"
}
