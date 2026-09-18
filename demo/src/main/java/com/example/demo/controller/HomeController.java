package com.example.demo.controller;
import org.springframework.ui.Model;
import com.example.demo.repository.CustomerRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

@Controller
public class HomeController {

	private final CustomerRepository customerRepository;

	public HomeController(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@GetMapping("/")
    public String home(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        return "home";
    }
}
