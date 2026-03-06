package lol.maki.dev.authorization;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignupController {

	private final JdbcUserDetailsManager userDetailsManager;

	private final PasswordEncoder passwordEncoder;

	private final SignupFormValidator signupFormValidator;

	public SignupController(JdbcUserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
		this.userDetailsManager = userDetailsManager;
		this.passwordEncoder = passwordEncoder;
		this.signupFormValidator = new SignupFormValidator(userDetailsManager);
	}

	@InitBinder("signupForm")
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(this.signupFormValidator);
	}

	@GetMapping("/signup")
	public String signupForm(Model model) {
		model.addAttribute("signupForm", new SignupForm("", "", ""));
		return "signup";
	}

	@PostMapping("/signup")
	public String signup(@Validated SignupForm signupForm, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("fieldErrors",
					bindingResult.getFieldErrors().stream().collect(Collectors.groupingBy(FieldError::getField)));
			return "signup";
		}
		this.userDetailsManager.createUser(User.withUsername(signupForm.username())
			.passwordEncoder(this.passwordEncoder::encode)
			.password(signupForm.password())
			.roles("USER")
			.build());
		return "redirect:/login?signup";
	}

	public record SignupForm(String username, String password, String confirmPassword) {
	}

	static class SignupFormValidator implements Validator {

		private static final Pattern EMAIL_PATTERN = Pattern
			.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

		private final JdbcUserDetailsManager userDetailsManager;

		SignupFormValidator(JdbcUserDetailsManager userDetailsManager) {
			this.userDetailsManager = userDetailsManager;
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return SignupForm.class.isAssignableFrom(clazz);
		}

		@Override
		public void validate(Object target, Errors errors) {
			SignupForm form = (SignupForm) target;
			if (form.username() == null || form.username().isBlank()) {
				errors.rejectValue("username", "required", "Username is required.");
			}
			else if (!EMAIL_PATTERN.matcher(form.username()).matches()) {
				errors.rejectValue("username", "invalid", "Please enter a valid email address.");
			}
			else if (this.userDetailsManager.userExists(form.username())) {
				errors.rejectValue("username", "duplicate", "Username already exists.");
			}
			if (form.password() == null || form.password().isBlank()) {
				errors.rejectValue("password", "required", "Password is required.");
			}
			else if (!errors.hasFieldErrors("password") && !errors.hasFieldErrors("confirmPassword")
					&& !form.password().equals(form.confirmPassword())) {
				errors.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
			}
			if (form.confirmPassword() == null || form.confirmPassword().isBlank()) {
				errors.rejectValue("confirmPassword", "required", "Confirm password is required.");
			}

		}

	}

}
