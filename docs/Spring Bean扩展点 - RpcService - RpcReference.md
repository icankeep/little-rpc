## Spring Bean初始化的扩展点

Spring中存在很多的扩展点，也就是给了很多我们定制自己逻辑功能的插入点。

比如说下面就要介绍的`BeanPostProcessor`，该接口中定义了两个方法`postProcessBeforeInitialization`和`postProcessAfterInitialization`，
分别是在bean初始化前后需要执行的方法。如下：


```java
public interface BeanPostProcessor { 
    // bean初始化之前执行
	default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
	// bean初始化之后执行
	default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
```

我们可以通过实现该接口方法来自定义某些bean初始化前后需要实现的逻辑

## RpcReference注解

RpcReference是客户端使用rpc服务的注解，使用该注解，在具体执行时找到服务端发布的对应服务，执行相应逻辑。如下

```java
@Component
public class ExampleController {
    @RpcReference
    private ExampleService exampleService;

    public void testExampleService() {
        Example example = new Example().builder()
                .exampleName("passer")
                .id(1L)
                .build();
        String hello = exampleService.helloExample(example);
        System.out.println("client:" + hello);
    }
}
```

对于RpcReference注解的字段，我们需要给他代理到具体RpcService实现的服务上

所以在ExampleController这个类的Bean初始化设置exampleService字段时，我们需要将该字段设置为我们的代理对象

我们可以通过实现`BeanPostProcessor`的`postProcessAfterInitialization`方法来实现代理

```java
@Component
public class CustomSpringBeanPostProcessor implements BeanPostProcessor {

    private RpcRequestTransport client = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    
    private ServiceProvider provider = SingletonFactory.getInstance(ServiceProviderImpl.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        // 找到所有的声明字段，包括私有
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            // 筛选出有RpcReference注解的字段
            if (rpcReference != null) {
                // 根据注解上的group和version生成property
                RpcServiceProperty property = new RpcServiceProperty().builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version())
                        .build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(client, property);
                Object objectProxy = rpcClientProxy.getProxy(field.getType());
                field.setAccessible(true);
                try {
                    field.set(bean, objectProxy);
                } catch (IllegalAccessException e) {
                    log.error("set proxy error.", e);
                }
            }
        }
        return bean;
    }
}
```

这样我们就将exampleService设置成了我们的代理对象，当客户端进行调用时，实际调用的是我们的代理对象的invoke方法，invoke
方法中实际是给server端发送请求(附带RpcServiceProperty、方法名、方法参数列表、方法参数等)，等待server端根据这些参数选择对应的服务和方法并执行完毕再返回对应的执行结果，
对于Client的调用方，就可以完全屏蔽底层的这些请求和Server端，把Client当成具体的逻辑执行者就行

## RpcService注解
RpcService是服务端用于发布服务的注解，该注解可用在对应服务的实现类上

```java
@RpcService
public class ExampleServiceImpl implements ExampleService {
    @Override
    public String helloExample(Example example) {
        String hello = String.format("hello, example[%s]", example);
        System.out.println("server:" + hello);
        return hello;
    }
}
```

贴上改注解的实现类会自动将服务注册到zk，所以我们需要在Spring容器扫描中，加上RpcService注解的扫描逻辑

```java
// 新增注解扫描类
public class CustomSpringScan extends ClassPathBeanDefinitionScanner {

    public CustomSpringScan(BeanDefinitionRegistry registry, Class<? extends Annotation> anno) {
        super(registry);
        super.addIncludeFilter(new AnnotationTypeFilter(anno));
    }
}
```

通过RpcScan指定需要扫描的包，扫描该包下对应Component注解和RpcService注解
```java
public class CustomSpringScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private static final String CUSTOM_SPRING_BEAN_BASE_PACKAGE = "com.passer.littlerpc.core.spring";

    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackages";

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes
                .fromMap(annotationMetadata.getAnnotationAttributes(RpcScan.class.getName()));
        String[] rpcScanPackages = new String[0];
        if (annotationAttributes != null) {
            rpcScanPackages = annotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        if (rpcScanPackages.length == 0) {
            rpcScanPackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }
        CustomSpringScan springScan = new CustomSpringScan(registry, Component.class);
        CustomSpringScan rpcScan = new CustomSpringScan(registry, RpcService.class);
        if (resourceLoader != null) {
            springScan.setResourceLoader(resourceLoader);
            rpcScan.setResourceLoader(resourceLoader);
        }
        int springScanCount = springScan.scan(CUSTOM_SPRING_BEAN_BASE_PACKAGE);
        int rpcScanCount = rpcScan.scan(rpcScanPackages);
        log.info("spring component count: {}, rpc service count: {}", springScanCount, rpcScanCount);
    }
}
```

所以贴上RpcService的实现类就被放在了Spring的容器中，我们需要在该Bean初始化之前就将该Bean对应的服务注册到注册中心

所以我们实现`BeanPostProcessor`对应的`postProcessBeforeInitialization`方法在bean初始化之前就注册对应的服务

```java
@Component
public class CustomSpringBeanPostProcessor implements BeanPostProcessor {

    private RpcRequestTransport client = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    
    private ServiceProvider provider = SingletonFactory.getInstance(ServiceProviderImpl.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
        if (annotation != null) {
            RpcServiceProperty property = RpcServiceProperty.builder()
                    .group(annotation.group())
                    .version(annotation.version())
                    .build();
            provider.publishService(bean, property);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
```
