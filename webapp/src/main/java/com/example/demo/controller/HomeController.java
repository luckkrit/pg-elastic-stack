package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Customers;
import com.example.demo.model.Orders;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.OrderDetailService;
import com.example.demo.elasticsearch.ProductSearchService;

@Controller
public class HomeController {

	private final CustomerRepository customerRepository;
	private final OrderRepository orderRepository;
	private final OrderDetailRepository orderDetailRepository;
	private final ProductRepository productRepository;
	private final OrderDetailService orderDetailService;
	private final ProductSearchService productSearchService;

	public HomeController(CustomerRepository customerRepository, OrderRepository orderRepository,
			OrderDetailRepository orderDetailRepository, ProductRepository productRepository,
			OrderDetailService orderDetailService, ProductSearchService productSearchService) {
		this.customerRepository = customerRepository;
		this.orderRepository = orderRepository;
		this.orderDetailRepository = orderDetailRepository;
		this.productRepository = productRepository;
		this.orderDetailService = orderDetailService;
		this.productSearchService = productSearchService;
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("customers", customerRepository.findAll());
		return "home";
	}

	@GetMapping("/customer/{customernumber}/order")
	public String customerOrder(@PathVariable("customernumber") Integer customernumber, Model model) {
		Customers customer = customerRepository.findById(customernumber)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		model.addAttribute("customer", customer);
		model.addAttribute("orders", orderRepository.findByCustomers_Customernumber(customernumber));
		return "order";
	}

	@GetMapping("/customer/{customernumber}/order/{ordernumber}/detail")
	public String customerOrderDetail(@PathVariable("customernumber") Integer customernumber,
			@PathVariable("ordernumber") Integer ordernumber, @RequestParam(required = false) String q,
			@RequestParam(defaultValue = "false") boolean es, Model model) {
		List<?> results = List.of();
		if (q != null && !q.isBlank()) {
			// results = es ? productSearchRepository.search(q.trim())
			// : productRepository.search(q.trim());
			results = productRepository.search(q.trim());
		}
		model.addAttribute("results", results);
		model.addAttribute("q", q);
		model.addAttribute("es", es);
		Customers customer = customerRepository.findById(customernumber)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Orders customerOrder = orderRepository.findById(ordernumber)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		model.addAttribute("customer", customer);
		model.addAttribute("order", customerOrder);
		model.addAttribute("orderDetails", orderDetailRepository.findById_Ordernumber(ordernumber));
		model.addAttribute("addProduct", "true");
		return "orderdetail";
	}

	@PostMapping("/customer/{customernumber}/order/{ordernumber}/lines")
	String updateLine(@PathVariable Integer customernumber, @PathVariable Integer ordernumber,
			@RequestParam String productcode,
			@RequestParam(defaultValue = "1") int delta,
			@RequestParam(required = false) String q,
			@RequestParam(defaultValue = "false") boolean es,
			RedirectAttributes ra) {
		orderDetailService.changeQuantity(ordernumber, productcode, delta);
		if (q != null && !q.isBlank())
			ra.addAttribute("q", q);
		if (es)
			ra.addAttribute("es", true);
		return "redirect:/customer/" + customernumber + "/order/" + ordernumber+"/detail";
	}
	@GetMapping("/product")
	public String showProducts(@RequestParam(required = false) String q,
			@RequestParam(defaultValue = "false") boolean es, Model model) {
		List<?> results = List.of();
		if (q != null && !q.isBlank()) {
			results = es ? productSearchService.search(q.trim())
			: productRepository.search(q.trim());
		}
		model.addAttribute("results", results);
		model.addAttribute("q", q);
		model.addAttribute("es", es);
		model.addAttribute("addProduct", null);
		return "product";
	}
}