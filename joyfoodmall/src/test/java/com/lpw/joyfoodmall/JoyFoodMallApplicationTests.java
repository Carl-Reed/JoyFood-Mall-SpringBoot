package com.lpw.joyfoodmall;

import com.lpw.joyfoodmall.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@EnableCaching
class JoyFoodMallApplicationTests {

	@Autowired
	private UserService userService;

	@Test
	void contextLoads() {
		// 运行这段代码生成 BCrypt 哈希
			PasswordEncoder encoder = new BCryptPasswordEncoder();
			System.out.println(encoder.encode("123456"));
			// 输出类似：$2a$10$Xg7qBZQp8Jv5k4fQwq2gCuOvJZ1yXyq2eYkq4wJv5eK
	}

	@Test
	void test() {
		for (int i=0; i<4; i++){
			String fileName = UUID.randomUUID().toString();
			System.out.println(fileName);
		}

	}

	@Test
	void pathTest(){
		String testHtml = "<p><img src=\"/api/files/images/productImages/ed71425300854a119c873c0a765c7e1c.jpg\" alt=\"\" data-href=\"\" style=\"\"/></p>";

		// 注意：[^>]+? 后面加个问号表示非贪婪匹配，防止一个标签匹配到结尾
		Pattern p = Pattern.compile("<img[^>]+?src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*?>");
		Matcher m = p.matcher(testHtml);

		while (m.find()) {
			String path = m.group(1);
			System.out.println("捕获: " + path);
			if (path.contains("/files/")) {
				System.out.println("结果: " + path.substring(path.indexOf("/files/")));
			}
		}
	}

}
