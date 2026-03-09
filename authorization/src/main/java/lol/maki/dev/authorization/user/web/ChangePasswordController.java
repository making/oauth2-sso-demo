package lol.maki.dev.authorization.user.web;

import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
public class ChangePasswordController {

	private final JdbcUserDetailsManager userDetailsManager;

	private final PasswordEncoder passwordEncoder;

	private final ChangePasswordFormValidator changePasswordFormValidator;

	public ChangePasswordController(JdbcUserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
		this.userDetailsManager = userDetailsManager;
		this.passwordEncoder = passwordEncoder;
		this.changePasswordFormValidator = new ChangePasswordFormValidator(userDetailsManager, passwordEncoder);
	}

	@InitBinder("changePasswordForm")
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(this.changePasswordFormValidator);
	}

	@GetMapping("/change-password")
	public String changePasswordForm(Model model) {
		model.addAttribute("changePasswordForm", new ChangePasswordForm("", "", ""));
		return "user/change-password";
	}

	@PostMapping("/change-password")
	public String changePassword(@Validated ChangePasswordForm changePasswordForm, BindingResult bindingResult,
			Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("fieldErrors",
					bindingResult.getFieldErrors().stream().collect(Collectors.groupingBy(FieldError::getField)));
			return "user/change-password";
		}
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return "redirect:/login";
		}
		String username = authentication.getName();
		UserDetails userDetails = this.userDetailsManager.loadUserByUsername(username);
		String encodedNewPassword = this.passwordEncoder.encode(changePasswordForm.newPassword());
		this.userDetailsManager.updatePassword(userDetails, encodedNewPassword);
		return "redirect:/?passwordChanged";
	}

	public record ChangePasswordForm(String currentPassword, String newPassword, String confirmNewPassword) {
	}

	static class ChangePasswordFormValidator implements Validator {

		private final JdbcUserDetailsManager userDetailsManager;

		private final PasswordEncoder passwordEncoder;

		ChangePasswordFormValidator(JdbcUserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
			this.userDetailsManager = userDetailsManager;
			this.passwordEncoder = passwordEncoder;
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return ChangePasswordForm.class.isAssignableFrom(clazz);
		}

		@Override
		public void validate(Object target, Errors errors) {
			ChangePasswordForm form = (ChangePasswordForm) target;
			if (form.currentPassword() == null || form.currentPassword().isBlank()) {
				errors.rejectValue("currentPassword", "required", "Current password is required.");
			}
			else {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				if (authentication != null) {
					String username = authentication.getName();
					UserDetails userDetails = this.userDetailsManager.loadUserByUsername(username);
					if (!this.passwordEncoder.matches(form.currentPassword(), userDetails.getPassword())) {
						errors.rejectValue("currentPassword", "invalid", "Current password is incorrect.");
					}
				}
			}
			if (form.newPassword() == null || form.newPassword().isBlank()) {
				errors.rejectValue("newPassword", "required", "New password is required.");
			}
			if (form.confirmNewPassword() == null || form.confirmNewPassword().isBlank()) {
				errors.rejectValue("confirmNewPassword", "required", "Confirm new password is required.");
			}
			if (!errors.hasFieldErrors("newPassword") && !errors.hasFieldErrors("confirmNewPassword")
					&& !Objects.equals(form.newPassword(), form.confirmNewPassword())) {
				errors.rejectValue("confirmNewPassword", "mismatch", "New passwords do not match.");
			}
		}

	}

}
