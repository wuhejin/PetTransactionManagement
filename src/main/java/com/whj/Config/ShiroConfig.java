package com.whj.Config;


import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.whj.Realm.ManagementRealm;
import com.whj.Util.SerializeUtils;
import lombok.Data;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import java.util.*;


@Data
@Configuration
@ConfigurationProperties(prefix = "shiro")
public class ShiroConfig {

    //加密方式
    private String hashAlgorithmName;
    //加密次数
    private int hashIterations;
    //未登陆跳转页面
    private String loginUrl;
    /*//登陆成功跳转页面
    private String successUrl;*/
    //未授权跳转页面
    private String unauthorizedUrl;
    //无需认证即可访问页面
    private String[] anonUrls;
    //退出页面
    private String logoutUrl;
    //认证后访问页面
    private String[] authcUrls;
    //redis host
    private String host;
    //redis 端口号
    private int port;
    //redis 密码
    private String password;


    //密码加密
    @Bean
    public RetryLimitHashedCredentialsMatcher getHashedCredentialsMatcher() {
        RetryLimitHashedCredentialsMatcher hashedCredentialsMatcher = new RetryLimitHashedCredentialsMatcher(CacheManager());
        //注入加密方式
        hashedCredentialsMatcher.setHashAlgorithmName(hashAlgorithmName);
        //注入加密次数
        hashedCredentialsMatcher.setHashIterations(hashIterations);

        return hashedCredentialsMatcher;
    }

    //注入自定义Realm
    @Bean(name = "managementRealm")
    public ManagementRealm getManagementRealm() {
        ManagementRealm managementRealm = new ManagementRealm();
//        密码对比器
        managementRealm.setCredentialsMatcher(getHashedCredentialsMatcher());

        managementRealm.setCachingEnabled(true);
        //启用身份验证缓存，即缓存AuthenticationInfo信息，默认false
        managementRealm.setAuthenticationCachingEnabled(true);
        //缓存AuthenticationInfo信息的缓存名称 在ehcache-shiro.xml中有对应缓存的配置
        managementRealm.setAuthenticationCacheName("authenticationCache");
        //启用授权缓存，即缓存AuthorizationInfo信息，默认false
        managementRealm.setAuthorizationCachingEnabled(true);
        //缓存AuthorizationInfo信息的缓存名称  在ehcache-shiro.xml中有对应缓存的配置
        managementRealm.setAuthorizationCacheName("authorizationCache");
        return managementRealm;
    }


    //声明SecurityManager
    @Bean
    public SecurityManager securityManager(ManagementRealm managementRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(managementRealm);
        //配置记住我管理器
//        securityManager.setRememberMeManager(rememberMeManager());


        //配置 缓存管理器
        securityManager.setCacheManager(CacheManager());

        //配置自定义session管理，使用redis
        securityManager.setSessionManager(sessionManager());

        return securityManager;
    }

    //Shiro的Web过滤器
    @Bean
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        //Shiro的核心安全接口
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        //注入未登录的跳转页面
        shiroFilterFactoryBean.setLoginUrl(loginUrl);
        /*//注入登陆成功跳转页面
        shiroFilterFactoryBean.setSuccessUrl(successUrl);*/
        // 注入未授权的跳转页面
        shiroFilterFactoryBean.setUnauthorizedUrl(unauthorizedUrl);
        Map<String, String> map = new LinkedHashMap<>();
        // 注入无需授权即可跳转页面
        if (anonUrls != null && anonUrls.length > 0) {
            for (String url : anonUrls) {
                map.put(url, "anon");
            }
        }
        //注入退出返回页面
        map.put(logoutUrl, "logout");
        /*
         * 前者(authc)是认证过，后者(user)是登录过，如果开启了rememberMe功能的话，后者(user)也是可以通过的，而前者(authc)通过不了。
         * 故我们用authc来校验一些关键操作，比如购买，我们可以采用user校验即可。而支付的时候，我们需要认证的用户，那就需要authc了。
         */
        //注入需要认证才能跳转页面
        if (authcUrls != null && authcUrls.length > 0) {
            for (String url : authcUrls) {
                map.put(url, "authc");
            }
        }
        shiroFilterFactoryBean.setFilterChainDefinitionMap(map);
        return shiroFilterFactoryBean;
    }

    /**
     * 开启shiro aop注解支持.--------------------------------------------------------------
     * 使用代理方式;所以需要开启代码支持;
     * 如 @RequiresPermissions("userInfo:add")
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    //必须（thymeleaf页面使用shiro标签控制按钮是否显示）--------------------------------

    /**
     * 未引入thymeleaf包，Caused by: java.lang.ClassNotFoundException: org.thymeleaf.dialect.AbstractProcessorDialect
     *
     * @return
     */
    @Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }

    //跳转403无权限页面，404未找到页面-------------------------------------------

    /**
     * 解决： 无权限页面不跳转 shiroFilterFactoryBean.setUnauthorizedUrl("/unauthorized") 无效
     * shiro的源代码ShiroFilterFactoryBean.Java定义的filter必须满足filter instanceof AuthorizationFilter，
     * 只有perms，roles，ssl，rest，port才是属于AuthorizationFilter，而anon，authcBasic，auchc，user是AuthenticationFilter，
     * 所以unauthorizedUrl设置后页面不跳转 Shiro注解模式下，登录失败与没有权限都是通过抛出异常。
     * 并且默认并没有去处理或者捕获这些异常。在SpringMVC下需要配置捕获相应异常来通知用户信息
     *
     * @return
     */
    @Bean(name = "simpleMappingExceptionResolver")
    public SimpleMappingExceptionResolver
    createSimpleMappingExceptionResolver() {
        SimpleMappingExceptionResolver simpleMappingExceptionResolver = new SimpleMappingExceptionResolver();
        Properties mappings = new Properties();
        //捕捉无权限异常，跳转403页面
        mappings.setProperty("UnauthorizedException", "/403");
        simpleMappingExceptionResolver.setExceptionMappings(mappings);  // None by default
        return simpleMappingExceptionResolver;
    }

    /**
     * 解决spring-boot2.0 Whitelabel Error Page
     * 页面无法找到跳转resources/static/404.html
     *
     * @return
     */
    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404.html");
            factory.addErrorPages(error404Page);
        };
    }
    //shiro-redis缓存--------------------------------------------------
    /**
     * shiro缓存管理器;
     * 需要添加到securityManager中
     * @return
     */
    @Bean
    public RedisCacheManager CacheManager(){
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager());
        //redis中针对不同用户缓存
        redisCacheManager.setPrincipalIdFieldName("tel");
        //用户权限信息缓存时间
        redisCacheManager.setExpire(200000);
        return redisCacheManager;
    }

    @Bean
    public RedisManager redisManager(){
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(host);
        redisManager.setPort(port);
        redisManager.setPassword(password);
        return redisManager;
    }

    /**
     * shiro redis缓存使用的模板
     * 实例化 RedisTemplate 对象
     * @return
     */
    @Bean("redisTemplate")
    public RedisTemplate shiroRedisTemplate(RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new SerializeUtils());
        redisTemplate.setValueSerializer(new SerializeUtils());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

//    --------------------------------------------------

    /**
     * 让某个实例的某个方法的返回值注入为Bean的实例
     * Spring静态注入
     * @return
     */
    @Bean
    public MethodInvokingFactoryBean getMethodInvokingFactoryBean(@Qualifier("managementRealm") ManagementRealm managementRealm){
        MethodInvokingFactoryBean factoryBean = new MethodInvokingFactoryBean();
        factoryBean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
        factoryBean.setArguments(new Object[]{securityManager(managementRealm)});
        return factoryBean;
    }


    //shiro-session-------------------------------------
    /**
     * 配置session监听
     * @return
     */
    @Bean("sessionListener")
    public ShiroSessionListener sessionListener(){
        ShiroSessionListener sessionListener = new ShiroSessionListener();
        return sessionListener;
    }
    /**
     * 配置会话ID生成器
     * @return
     */
    @Bean
    public SessionIdGenerator sessionIdGenerator() {
        return new JavaUuidSessionIdGenerator();
    }
    /**
     * SessionDAO的作用是为Session提供CRUD并进行持久化的一个shiro组件
     * MemorySessionDAO 直接在内存中进行会话维护
     * EnterpriseCacheSessionDAO  提供了缓存功能的会话维护，默认情况下使用MapCache实现，内部使用ConcurrentHashMap保存缓存的会话。
     * @return
     */
    @Bean
    public SessionDAO sessionDAO() {
        EnterpriseCacheSessionDAO enterpriseCacheSessionDAO = new EnterpriseCacheSessionDAO();
        //使用CacheManager
        enterpriseCacheSessionDAO.setCacheManager(CacheManager());
        //设置session缓存的名字 默认为 shiro-activeSessionCache
        enterpriseCacheSessionDAO.setActiveSessionsCacheName("shiro-activeSessionCache");
        //sessionId生成器
        enterpriseCacheSessionDAO.setSessionIdGenerator(sessionIdGenerator());
        return enterpriseCacheSessionDAO;
    }
    /**
     * 配置保存sessionId的cookie
     * 注意：这里的cookie 不是上面的记住我 cookie 记住我需要一个cookie session管理 也需要自己的cookie
     * @return
     */
    @Bean("sessionIdCookie")
    public SimpleCookie sessionIdCookie(){
        //这个参数是cookie的名称
        SimpleCookie simpleCookie = new SimpleCookie("sid");
        //setcookie的httponly属性如果设为true的话，会增加对xss防护的安全系数。它有以下特点：

        //setcookie()的第七个参数
        //设为true后，只能通过http访问，javascript无法访问
        //防止xss读取cookie
        simpleCookie.setHttpOnly(true);
        simpleCookie.setPath("/");
        //maxAge=-1表示浏览器关闭时失效此Cookie
        simpleCookie.setMaxAge(-1);
        return simpleCookie;
    }
    /**
     * 配置会话管理器，设定会话超时及保存
     * @return
     */
    @Bean("sessionManager")
    public SessionManager sessionManager() {

        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        Collection<SessionListener> listeners = new ArrayList<SessionListener>();
        //配置监听
        listeners.add(sessionListener());
        sessionManager.setSessionListeners(listeners);
        sessionManager.setSessionIdCookie(sessionIdCookie());
        sessionManager.setSessionDAO(sessionDAO());
        sessionManager.setCacheManager(CacheManager());

        //全局会话超时时间（单位毫秒），默认30分钟
        sessionManager.setGlobalSessionTimeout(1800000);
        //是否开启删除无效的session对象  默认为true
        sessionManager.setDeleteInvalidSessions(true);
        //是否开启定时调度器进行检测过期session 默认为true
        sessionManager.setSessionValidationSchedulerEnabled(true);
        //设置session失效的扫描时间, 清理用户直接关闭浏览器造成的孤立会话 默认为 1个小时
        //设置该属性 就不需要设置 ExecutorServiceSessionValidationScheduler 底层也是默认自动调用ExecutorServiceSessionValidationScheduler
        //暂时设置为 15分钟
        sessionManager.setSessionValidationInterval(900000);

        //取消url 后面的 JSESSIONID
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        return sessionManager;

    }
}
