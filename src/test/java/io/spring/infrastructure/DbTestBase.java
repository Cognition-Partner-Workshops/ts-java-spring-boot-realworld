package io.spring.infrastructure;

import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@MybatisTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class DbTestBase {}
