package com.smart.oauth2;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.AuthenticationProvider;
import com.smart.entities.Contact;
import com.smart.entities.User;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler{

	@Autowired
	private UserRepository userRepository;
	
	@Autowired 
	private ContactRepository contactRepository;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
		String emaiString = oAuth2User.getName();
		String nameString = oAuth2User.getFullName();
		String authProvider = oAuth2User.getAuthProvider();
//		String imageString = oAuth2User.getImage();
//		String aboutString = oAuth2User.getAbout();
//		System.out.println("Facebook User = "+emaiString);
		User user = this.userRepository.getUserByEmail(emaiString);
		if(user==null) {
			//Register newUser
			System.out.println("user is null");
			User newUser = new User();
			newUser.setName(nameString);
			newUser.setEmail(emaiString);
			newUser.setPassword(nameString+"+"+emaiString);			
			newUser.setRole("ROLE_USER");
			newUser.setImage("default.jpg");
			newUser.setAbout("");
			newUser.setEnabled(true);
			newUser.setCoins(100);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
			LocalDateTime now = LocalDateTime.now();
			newUser.setDate(dtf.format(now));
			newUser.setStatus(false);
			
			if (authProvider.equals("Facebook")) {
				newUser.setAuthProvider(AuthenticationProvider.FACEBOOK);
			}else if (authProvider.equals("Google")) {
				newUser.setAuthProvider(AuthenticationProvider.GOOGLE);
			}else {
				newUser.setAuthProvider(AuthenticationProvider.GITHUB);
			}
			
			this.userRepository.save(newUser);
			
			Contact contact = this.contactRepository.getContactByEmail(emaiString);
			if (contact!=null) {
				contact.setUnique_id(newUser.getId());
				this.contactRepository.save(contact);
			}
			
//			response.sendRedirect("signup?username+"nameString+"&email="+emaiString+"+&authprovider="+AuthenticationProvider.FACEBOOK);
		}else {
			if (authProvider.equals("Facebook")) {
				user.setAuthProvider(AuthenticationProvider.FACEBOOK);
			}else if (authProvider.equals("Google")) {
				user.setAuthProvider(AuthenticationProvider.GOOGLE);
			}else {
				user.setAuthProvider(AuthenticationProvider.GITHUB);
			}
			user.setStatus(true);
			this.userRepository.save(user);
		}
		
		//login with new User
		response.sendRedirect("/check/");
		super.onAuthenticationSuccess(request, response, authentication);
	}

}
