package com.casestudy.employeessystem.cotrollers;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

import javax.validation.Valid;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.casestudy.employeessystem.models.Compensation;
import com.casestudy.employeessystem.models.Employee;
import com.casestudy.employeessystem.models.User;
import com.casestudy.employeessystem.repositories.IUserRepository;
import com.casestudy.employeessystem.service.CompensationService;
import com.casestudy.employeessystem.service.EmployeeServiceImpl;

@Controller
public class SystemsController {
	
	@Autowired
	private IUserRepository userRepo;
	@Autowired
	private CompensationService compService;
	@Autowired
	private EmployeeServiceImpl emplService;

	
	// ________index page______________
	@GetMapping("/")
	public String index() {
		return "index";
	}

	
	// ________register form______________
	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}
	// ________make the registration___________
	@PostMapping("/process_register")
	public String processRegistration(@Valid User user) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);

		userRepo.save(user);

		return "sucessful_registration";
	}

	
	// ________welcome page______________
	@GetMapping("/welcome")
	public String welcome() {
		return "welcome";
	}

	
	// ________add an employee form_____________
	@GetMapping("/employee/add")
	public String addEmplForm(Model model) {
		Employee empl = new Employee();
		model.addAttribute(empl);
		return "add_employee";
	}
	// ________save the employee________________
	@PostMapping("/employee")
	public String addNewEmployee(@ModelAttribute("employee") @Valid Employee empl, RedirectAttributes redirAttrs) { // send the employee object
		// get the birth date of the employee and convert it to java.util.date
		Date birthDate = empl.getBirthDate();
		java.util.Date birthDateUtilDate = new java.util.Date(birthDate.getTime());
		// get actual date
		LocalDateTime dateTime = LocalDateTime.now();
		// obtain minimum date
		LocalDateTime oldDate = dateTime.minusYears(21);
		java.util.Date c2 = oldDate.toDateTime().toDate();
		
		if(birthDateUtilDate.before(c2)) {
			if(emplService.exists(empl)!=true) { //successfully added
				emplService.saveEmployees(empl);
				return "redirect:/welcome?success";
			}else //error, employee already exists
				return "redirect:/welcome?duplicate";
		}else //wrong date
			return "redirect:/welcome?invalid";
	}

	
	// _______search form________________________
	@GetMapping("/search/form")
	public String searchForm() {
		return "search";
	}
	// _______search the employee___________
	@RequestMapping("/search")
	public String searchEmpl(Model model, String fname, String lname, String pos) {
		try {
			List<Employee> list = emplService.findEmployee(fname, lname, pos);
			model.addAttribute("list", list);

		} catch (Exception ex) {
			System.out.println(ex);
		}
		return "search";
	}

	/*
	 * // _______view all the employees________
	 * 
	 * @GetMapping("/list") public String listEmployees(Model model) { try {
	 * model.addAttribute("employees", service.listAllEmployees()); // employee's
	 * list } catch (Exception ex) { System.out.println(ex); }
	 * 
	 * return "employees"; }
	 */
	
	// _______edit employee______________
	@GetMapping("/employee/{uid}/edit")
	public String editEmployeeForm(@PathVariable int uid, Model model) { // receive the uid to get its info
		model.addAttribute("employee", emplService.getEmployeeById(uid));
		return "edit_employee";
	}
	// _______update info employee________
	@PostMapping("/employee/{uid}")
	public String updateEmployee(@PathVariable int uid, @ModelAttribute("employee") Employee employee, Model model) {
		Employee existingEmpl = emplService.getEmployeeById(uid);
		existingEmpl.setUid(employee.getUid());
		existingEmpl.setFirstName(employee.getFirstName()); // from the employee in the form, assign the data
		existingEmpl.setLastName(employee.getLastName());
		existingEmpl.setMiddleName(employee.getMiddleName());
		existingEmpl.setPosition(employee.getPosition());
		existingEmpl.setBirthDate(employee.getBirthDate());

		emplService.updateEmployee(existingEmpl);
		return "redirect:/welcome";
	}
	
	
	
	//________view employee's compensation history_______
	@GetMapping("/employee/{uid}/view_compensation")
	public String viewCompensation(@PathVariable int uid, Model model) {
		model.addAttribute("employee", emplService.getEmployeeById(uid));
		List<Compensation> compensations = compService.findCompensationsByEmployeeId(uid); //obtain month and amount of each compensation
		model.addAttribute("listComp", compensations);
		return "compensation_history";
	}
	//________new compensation form_________________
	@GetMapping("/employee/{uid}/new_compensation")
	public String compensationForm(@PathVariable int uid, Model model) {
		model.addAttribute("employee", emplService.getEmployeeById(uid));
		return "add_compensation";  
			
	}
	//________add a new compensation__________________
	@PostMapping("/employee/{uid}/add_compensation")
	public String addCompensation(@ModelAttribute("compensations") @Valid Compensation comp) {
		return "add_compensation";  
			
	}

	// DB error
	@ExceptionHandler({ SQLException.class, DataAccessException.class })
	public String databaseError() {
		return "error";
	}

}
