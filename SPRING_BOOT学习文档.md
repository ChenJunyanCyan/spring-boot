# Spring Boot 学习文档

> 基于 Spring Boot 4.1.0-SNAPSHOT 源码

---

## 目录

1. [Spring Boot 简介](#1-spring-boot-简介)
2. [环境准备](#2-环境准备)
3. [第一个 Spring Boot 应用](#3-第一个-spring-boot-应用)
4. [核心概念](#4-核心概念)
5. [自动配置原理](#5-自动配置原理)
6. [常用功能模块](#6-常用功能模块)
7. [Web 开发](#7-web-开发)
8. [数据访问](#8-数据访问)
9. [测试](#9-测试)
10. [部署与运维](#10-部署与运维)
11. [进阶主题](#11-进阶主题)
12. [学习资源](#12-学习资源)

---

## 1. Spring Boot 简介

### 1.1 什么是 Spring Boot

Spring Boot 是一个用于创建 Spring 驱动的、生产级应用程序的框架，旨在让您以最小的麻烦快速启动项目。

### 1.2 主要目标

- 为所有 Spring 开发提供极快的、广泛可用的入门体验
- 提供观点化的配置，但当需求与默认值不同时，能够快速退出
- 提供一系列大型项目通用的非功能性特性（如嵌入式服务器、安全性、指标、健康检查、外部化配置）
- 绝对没有代码生成，也不需要 XML 配置

### 1.3 核心特性

- **起步依赖（Starters）**：简化的依赖描述符
- **自动配置**：自动配置 Spring 应用程序
- **Actuator**：生产就绪功能
- **内嵌服务器**：Tomcat、Jetty、Undertow
- **CLI 工具**：Spring Boot 命令行工具

---

## 2. 环境准备

### 2.1 系统要求

- **JDK**: 25 或更高版本
- **构建工具**: Gradle 9.5+（需要 JVM 17+）或 Maven 3.9+
- **内存**: 建议至少 4GB RAM
- **磁盘空间**: 至少 1GB 可用空间

### 2.2 安装 JDK

```bash
# Windows (使用 SDKMAN 或手动安装)
# 下载 JDK 25: https://adoptium.net/

# 验证安装
java -version
```

### 2.3 设置 JAVA_HOME

```powershell
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
$env:Path += ";$env:JAVA_HOME\bin"
```

### 2.4 构建工具

**使用 Gradle Wrapper（推荐）**：
```bash
# 编译项目
./gradlew build

# 发布到本地 Maven 仓库
./gradlew publishToMavenLocal

# 运行测试
./gradlew test
```

---

## 3. 第一个 Spring Boot 应用

### 3.1 创建项目

使用 Spring Initializr（https://start.spring.io）或手动创建。

### 3.2 项目结构

```
my-spring-boot-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/
│   │   │       └── MyApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/example/
│               └── MyApplicationTests.java
├── pom.xml (或 build.gradle)
└── README.md
```

### 3.3 主应用程序类

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 3.4 简单的 REST 控制器

```java
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

@RestController
@SpringBootApplication
public class Example {

    @RequestMapping("/")
    String home() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        SpringApplication.run(Example.class, args);
    }
}
```

### 3.5 运行应用

```bash
# 使用 Gradle
./gradlew bootRun

# 使用 Maven
mvn spring-boot:run

# 打包后运行
./gradlew build
java -jar build/libs/myapp-0.0.1-SNAPSHOT.jar
```

---

## 4. 核心概念

### 4.1 @SpringBootApplication

这是一个组合注解，包含：
- `@SpringBootConfiguration`: 标记为配置类
- `@EnableAutoConfiguration`: 启用自动配置
- `@ComponentScan`: 启用组件扫描

```java
@SpringBootApplication
// 等同于
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan
public class MyApplication { }
```

### 4.2 Spring Application Context

Spring Boot 自动创建和配置 ApplicationContext：

```java
ConfigurableApplicationContext context = SpringApplication.run(MyApplication.class, args);

// 获取 Bean
MyService service = context.getBean(MyService.class);
```

### 4.3 外部化配置

Spring Boot 支持多种配置方式（优先级从高到低）：

1. 命令行参数
2. JNDI 属性
3. Java 系统属性
4. 操作系统环境变量
5. `application-{profile}.properties/yml`
6. `application.properties/yml`
7. `@PropertySource` 注解
8. 默认属性

### 4.4 配置文件示例

**application.properties**:
```properties
# 服务器配置
server.port=8080
server.servlet.context-path=/api

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=secret

# 日志配置
logging.level.root=INFO
logging.level.com.example=DEBUG
```

**application.yml**:
```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: secret

logging:
  level:
    root: INFO
    com.example: DEBUG
```

### 4.5 Profile 配置

```java
@Profile("dev")
@Configuration
public class DevConfig {
    // 开发环境配置
}

@Profile("prod")
@Configuration
public class ProdConfig {
    // 生产环境配置
}
```

激活 Profile：
```bash
java -jar app.jar --spring.profiles.active=dev
```

---

## 5. 自动配置原理

### 5.1 什么是自动配置

Spring Boot 自动配置尝试根据您添加的 jar 依赖项自动配置 Spring 应用程序。

### 5.2 工作原理

1. 检查 classpath 中的依赖
2. 检查已配置的 Bean
3. 检查属性设置
4. 自动配置合适的 Bean

### 5.3 常见自动配置

```java
// 数据源自动配置
// 当 classpath 中有 H2、Tomcat JDBC 等时自动配置

// MVC 自动配置
// 当 classpath 中有 spring-webmvc 时自动配置

// JPA 自动配置
// 当 classpath 中有 Hibernate 和 JDBC 时自动配置
```

### 5.4 禁用自动配置

```java
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class MyApplication { }
```

### 5.5 自定义自动配置

创建 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 文件：

```
com.example.MyCustomAutoConfiguration
```

---

## 6. 常用功能模块

### 6.1 Spring Boot Actuator

生产就绪功能，提供监控和管理端点。

**依赖**：
```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

**常用端点**：
- `/actuator/health` - 应用健康状态
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 应用指标
- `/actuator/env` - 环境变量
- `/actuator/beans` - 所有 Bean

**配置**：
```properties
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

### 6.2 Spring Boot DevTools

开发时自动重启和实时重载。

**依赖**：
```gradle
developmentOnly 'org.springframework.boot:spring-boot-devtools'
```

### 6.3 日志配置

Spring Boot 默认使用 Logback。

```properties
# 日志级别
logging.level.root=INFO
logging.level.com.example=DEBUG

# 日志文件
logging.file.name=application.log
logging.file.path=/var/log

# 日志格式
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

---

## 7. Web 开发

### 7.1 RESTful Web 服务

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        user.setId(id);
        return ResponseEntity.ok(userService.save(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

### 7.2 异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal server error",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### 7.3 请求验证

```java
public class UserDTO {
    
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email should be valid")
    private String email;

    @Min(value = 18, message = "Age must be at least 18")
    private Integer age;

    // getters and setters
}
```

```java
@PostMapping
public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) {
    // 如果验证失败，会自动返回 400 错误
    User user = userService.save(User.fromDTO(userDTO));
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
}
```

### 7.4 拦截器和过滤器

**拦截器**：
```java
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        log.info("Request URL: {}", request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, 
                               Exception ex) {
        log.info("Response Status: {}", response.getStatus());
    }
}
```

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/public/**");
    }
}
```

---

## 8. 数据访问

### 8.1 Spring Data JPA

**依赖**：
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'com.mysql:mysql-connector-j'
```

**实体类**：
```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    // getters and setters
}
```

**Repository**：
```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    List<User> findByNameContaining(String name);
    
    @Query("SELECT u FROM User u WHERE u.email LIKE %:domain")
    List<User> findByEmailDomain(@Param("domain") String domain);
}
```

**Service**：
```java
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
```

### 8.2 数据库迁移

**使用 Flyway**：
```gradle
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-mysql'
```

迁移文件位置：`src/main/resources/db/migration/`

```sql
-- V1__create_users_table.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V2__add_age_column.sql
ALTER TABLE users ADD COLUMN age INT;
```

### 8.3 Redis 集成

**依赖**：
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

**配置**：
```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
```

**使用**：
```java
@Service
public class CacheService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void set(String key, String value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MINUTES);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
```

---

## 9. 测试

### 9.1 单元测试

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testFindById() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test User", result.get().getName());
    }
}
```

### 9.2 集成测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testCreateUser() throws Exception {
        String userJson = """
            {
                "name": "John Doe",
                "email": "john@example.com",
                "age": 25
            }
            """;

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }
}
```

### 9.3 Testcontainers

```java
@SpringBootTest
@Testcontainers
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindUser() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");

        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getName());
    }
}
```

---

## 10. 部署与运维

### 10.1 打包应用

```bash
# 使用 Gradle
./gradlew bootJar

# 使用 Maven
mvn package
```

### 10.2 运行 JAR

```bash
java -jar myapp-0.0.1-SNAPSHOT.jar

# 指定配置
java -jar myapp.jar --spring.profiles.active=prod

# 指定 JVM 参数
java -Xmx512m -Xms256m -jar myapp.jar
```

### 10.3 Docker 部署

**Dockerfile**：
```dockerfile
FROM eclipse-temurin:25-jre
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

**构建和运行**：
```bash
docker build -t myapp .
docker run -p 8080:8080 myapp
```

### 10.4 健康检查

```properties
management.endpoint.health.show-details=always
management.health.defaults.enabled=true
```

自定义健康指示器：
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // 执行健康检查逻辑
        if (isServiceHealthy()) {
            return Health.up().build();
        }
        return Health.down()
                .withDetail("error", "Service is unhealthy")
                .build();
    }

    private boolean isServiceHealthy() {
        // 检查逻辑
        return true;
    }
}
```

### 10.5 监控和指标

```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'io.micrometer:micrometer-registry-prometheus'
```

```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.prometheus.enabled=true
```

---

## 11. 进阶主题

### 11.1 安全性

**依赖**：
```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
```

**基础配置**：
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 11.2 异步处理

```java
@SpringBootApplication
@EnableAsync
public class MyApplication { }
```

```java
@Service
public class AsyncService {

    @Async
    public CompletableFuture<String> asyncMethod() {
        // 异步执行
        return CompletableFuture.completedFuture("Result");
    }
}
```

### 11.3 定时任务

```java
@SpringBootApplication
@EnableScheduling
public class MyApplication { }
```

```java
@Component
public class ScheduledTasks {

    @Scheduled(fixedRate = 5000) // 每 5 秒执行
    public void fixedRateTask() {
        log.info("Fixed rate task executed");
    }

    @Scheduled(cron = "0 0 12 * * ?") // 每天中午 12 点执行
    public void cronTask() {
        log.info("Cron task executed");
    }
}
```

### 11.4 缓存

```java
@SpringBootApplication
@EnableCaching
public class MyApplication { }
```

```java
@Service
public class UserService {

    @Cacheable(value = "users", key = "#id")
    public User findById(Long id) {
        // 从数据库查询
        return userRepository.findById(id).orElse(null);
    }

    @CachePut(value = "users", key = "#user.id")
    public User update(User user) {
        return userRepository.save(user);
    }

    @CacheEvict(value = "users", key = "#id")
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
```

### 11.5 消息队列

**RabbitMQ**：
```gradle
implementation 'org.springframework.boot:spring-boot-starter-amqp'
```

```java
@Configuration
public class RabbitConfig {

    @Bean
    public Queue myQueue() {
        return new Queue("my-queue", true);
    }
}
```

```java
@Service
public class MessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend("my-queue", message);
    }
}
```

```java
@Service
public class MessageConsumer {

    @RabbitListener(queues = "my-queue")
    public void receiveMessage(String message) {
        log.info("Received message: {}", message);
    }
}
```

---

## 12. 学习资源

### 12.1 官方文档

- [Spring Boot 官方文档](https://docs.spring.io/spring-boot)
- [Spring Boot 教程](https://docs.spring.io/spring-boot/tutorial/first-application/index.html)
- [Spring Boot API 文档](https://docs.spring.io/spring-boot/api)

### 12.2 学习指南

- [Building an Application with Spring Boot](https://spring.io/guides/gs/spring-boot/)
- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
- [Securing a Web Application](https://spring.io/guides/gs/securing-web/)

### 12.3 源码学习

- [Spring Boot GitHub 仓库](https://github.com/spring-projects/spring-boot)
- 核心模块：
  - `core/spring-boot` - 核心模块
  - `core/spring-boot-autoconfigure` - 自动配置
  - `module/` - 各种功能模块
  - `starter/` - 起步依赖

### 12.4 构建源码

```bash
# 克隆仓库
git clone https://github.com/spring-projects/spring-boot.git

# 进入目录
cd spring-boot

# 构建并发布到本地 Maven 仓库
./gradlew publishToMavenLocal

# 运行完整构建（包含测试）
./gradlew build
```

### 12.5 社区和支持

- [Stack Overflow - spring-boot 标签](https://stackoverflow.com/questions/tagged/spring-boot)
- [Spring Boot Issues](https://github.com/spring-projects/spring-boot/issues)
- [Spring Boot Release Notes](https://github.com/spring-projects/spring-boot/wiki)

### 12.6 推荐学习路径

1. **入门阶段**（1-2 周）
   - 理解 Spring Boot 基本概念
   - 创建第一个 Spring Boot 应用
   - 学习 RESTful API 开发
   - 掌握配置文件使用

2. **进阶阶段**（2-4 周）
   - 深入理解自动配置原理
   - 学习 Spring Data JPA
   - 掌握异常处理和验证
   - 学习单元测试和集成测试

3. **高级阶段**（1-2 月）
   - 学习 Spring Security
   - 掌握 Actuator 监控
   - 学习微服务架构
   - 理解性能优化

4. **源码研究**（持续）
   - 阅读核心模块源码
   - 理解设计模式应用
   - 参与开源贡献

---

## 附录

### A. 常用注解

| 注解 | 说明 |
|------|------|
| `@SpringBootApplication` | 主应用类注解 |
| `@RestController` | REST 控制器 |
| `@Service` | 服务层组件 |
| `@Repository` | 数据访问层组件 |
| `@Component` | 通用组件 |
| `@Configuration` | 配置类 |
| `@Bean` | 定义 Bean |
| `@Autowired` | 自动注入 |
| `@Value` | 注入属性值 |
| `@ConfigurationProperties` | 绑定配置属性 |
| `@EnableAutoConfiguration` | 启用自动配置 |
| `@ComponentScan` | 组件扫描 |
| `@ConditionalOnClass` | 条件注解 - 类存在时 |
| `@ConditionalOnMissingBean` | 条件注解 - Bean 不存在时 |

### B. 常用 Starter

| Starter | 说明 |
|---------|------|
| `spring-boot-starter` | 核心 starter |
| `spring-boot-starter-web` | Web 应用 |
| `spring-boot-starter-data-jpa` | JPA 数据访问 |
| `spring-boot-starter-data-redis` | Redis 支持 |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-test` | 测试支持 |
| `spring-boot-starter-actuator` | 监控端点 |
| `spring-boot-starter-amqp` | RabbitMQ 支持 |
| `spring-boot-starter-mail` | 邮件发送 |
| `spring-boot-starter-validation` | 数据验证 |

### C. 常见问题

**Q: 如何修改默认端口？**
```properties
server.port=9090
```

**Q: 如何禁用某个自动配置？**
```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
```

**Q: 如何读取自定义配置？**
```java
@Value("${my.custom.property}")
private String customProperty;
```

**Q: 如何配置多数据源？**
```java
@Configuration
public class DataSourceConfig {
    
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    @ConfigurationProperties("spring.datasource.secondary")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }
}
```

---

## 结语

Spring Boot 是一个强大且易用的框架，通过约定优于配置的理念，大大简化了 Spring 应用的开发。本学习文档涵盖了从入门到进阶的主要内容，但 Spring Boot 的世界远不止于此。

建议您：
1. 多实践，多写代码
2. 阅读官方文档
3. 研究源码实现
4. 参与社区讨论
5. 关注最新版本更新

祝您学习愉快！

---

**文档版本**: 1.0  
**基于版本**: Spring Boot 4.1.0-SNAPSHOT  
**最后更新**: 2026-05-12
