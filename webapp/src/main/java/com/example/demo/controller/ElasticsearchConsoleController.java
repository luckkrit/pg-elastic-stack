package com.example.demo.controller;

import java.io.IOException;
import java.util.List;

import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.elasticsearch.ElasticsearchConsoleService;

import co.elastic.clients.transport.rest5_client.low_level.ResponseException;

@Controller
public class ElasticsearchConsoleController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);
    private final ElasticsearchConsoleService elasticsearchConsoleService;

    public ElasticsearchConsoleController(ElasticsearchConsoleService elasticsearchConsoleService) {
        this.elasticsearchConsoleService = elasticsearchConsoleService;
    }

    @GetMapping("/console")
    public String console() {
        return "elasticsearch-console";
    }

    @PostMapping("/console")
    public String execute(
            @RequestParam String command,
            Model model) {

        model.addAttribute("command", command);

        try {

            String json = elasticsearchConsoleService.search(command);

            model.addAttribute("status", "Success");
            model.addAttribute("result", json);

        } catch (IllegalArgumentException ex) {

            model.addAttribute("status", "Invalid Command");
            model.addAttribute("result", ex.getMessage());

        } catch (ResponseException ex) {

            model.addAttribute(
                    "status",
                    ex.getResponse().getStatusCode());

            try {

                String errorJson = EntityUtils.toString(
                        ex.getResponse().getEntity());

                model.addAttribute("result", errorJson);

            } catch (IOException e) {

                model.addAttribute(
                        "result",
                        "Unable to read Elasticsearch error response.");
            } catch (ParseException e) {
                model.addAttribute(
                        "result",
                        "Unable to parse JSON error response.");
            }

        } catch (IOException ex) {

            model.addAttribute("status", "Connection Error");

            model.addAttribute(
                    "result",
                    "Cannot communicate with Elasticsearch.");
        } catch (ParseException e) {
            model.addAttribute(
                    "result",
                    "Unable to parse JSON response.");
            e.printStackTrace();
        }

        return "elasticsearch-console";
    }
}
