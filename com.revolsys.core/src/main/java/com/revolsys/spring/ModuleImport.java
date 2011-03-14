package com.revolsys.spring;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;

import com.revolsys.beans.ResourceEditorRegistrar;
import com.revolsys.collection.AttributeMap;
import com.revolsys.spring.config.AttributesBeanConfigurer;

public class ModuleImport implements BeanDefinitionRegistryPostProcessor,
  BeanNameAware {

  private GenericApplicationContext applicationContext;

  private Map<String, String> exportBeanAliases = Collections.emptyMap();

  private List<String> exportBeanNames = Collections.emptyList();

  private List<String> importBeanNames = Collections.emptyList();

  private boolean exportAllBeans = false;

  private Map<String, Object> parameters = new HashMap<String, Object>();

  private Resource resource;

  private boolean enabled = true;

  private ResourceEditorRegistrar resourceEditorRegistrar = new ResourceEditorRegistrar();

  private String beanName;

  public void setBeanName(String beanName) {
    this.beanName = beanName;
  }

  protected void afterPostProcessBeanDefinitionRegistry(
    final BeanDefinitionRegistry registry) {
  }

  protected void beforePostProcessBeanDefinitionRegistry(
    final BeanDefinitionRegistry registry) throws BeansException {
  }

  protected GenericApplicationContext getApplicationContext(
    BeanDefinitionRegistry parentRegistry) {
    if (applicationContext == null) {
      applicationContext = new GenericApplicationContext();
      final DefaultListableBeanFactory beanFactory = applicationContext.getDefaultListableBeanFactory();

      final BeanFactory parentBeanFactory = (BeanFactory)parentRegistry;
      for (String beanName : parentRegistry.getBeanDefinitionNames()) {
        BeanDefinition beanDefinition = parentRegistry.getBeanDefinition(beanName);
        String beanClassName = beanDefinition.getBeanClassName();
        if (beanClassName.equals(AttributeMap.class.getName())) {
          registerTargetBeanDefinition(applicationContext, parentBeanFactory,
            beanName, beanName);
        } else if (beanClassName.equals(MapFactoryBean.class.getName())) {
          final PropertyValue targetMapClass = beanDefinition.getPropertyValues()
            .getPropertyValue("targetMapClass");
          if (targetMapClass != null) {
            final Object mapClass = targetMapClass.getValue();
            if (AttributeMap.class.getName().equals(mapClass)) {
              registerTargetBeanDefinition(applicationContext,
                parentBeanFactory, beanName, beanName);
            }
          }
        }
      }
      beanFactory.addPropertyEditorRegistrar(resourceEditorRegistrar);
      final AttributesBeanConfigurer attributesConfig = new AttributesBeanConfigurer(
        parameters);
      applicationContext.addBeanFactoryPostProcessor(attributesConfig);
      for (final String beanName : importBeanNames) {
        registerTargetBeanDefinition(applicationContext, parentBeanFactory,
          beanName, beanName);
      }
      final XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(
        applicationContext);
      beanReader.loadBeanDefinitions(resource);
      applicationContext.refresh();
    }
    return applicationContext;
  }

  public List<String> getImportBeanNames() {
    return importBeanNames;
  }

  public void setImportBeanNames(List<String> importBeanNames) {
    this.importBeanNames = importBeanNames;
  }

  public Map<String, String> getExportBeanAliases() {
    return exportBeanAliases;
  }

  public List<String> getExportBeanNames() {
    return exportBeanNames;
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public Resource getResource() {
    return resource;
  }

  public ResourceEditorRegistrar getResourceEditorRegistrar() {
    return resourceEditorRegistrar;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isExportAllBeans() {
    return exportAllBeans;
  }

  public void postProcessBeanDefinitionRegistry(
    final BeanDefinitionRegistry registry) throws BeansException {
    beforePostProcessBeanDefinitionRegistry(registry);
    if (enabled) {
      final GenericApplicationContext beanFactory = getApplicationContext(registry);
      if (exportAllBeans) {
        for (final String beanName : beanFactory.getBeanDefinitionNames()) {
          registerTargetBeanDefinition(registry, beanFactory, beanName,
            beanName);
          for (final String alias : beanFactory.getAliases(beanName)) {
            registerTargetBeanDefinition(registry, beanFactory, beanName, alias);
          }
        }
      } else {
        for (final String beanName : exportBeanNames) {
          registerTargetBeanDefinition(registry, beanFactory, beanName,
            beanName);
        }
      }

      for (final Entry<String, String> exportBeanAlias : exportBeanAliases.entrySet()) {
        final String beanName = exportBeanAlias.getKey();
        final String alias = exportBeanAlias.getValue();
        registerTargetBeanDefinition(registry, beanFactory, beanName, alias);
      }
      afterPostProcessBeanDefinitionRegistry(registry);
    }
  }

  public void postProcessBeanFactory(
    final ConfigurableListableBeanFactory beanFactory) throws BeansException {
  }

  protected void registerTargetBeanDefinition(
    final BeanDefinitionRegistry registry, final BeanFactory beanFactory,
    final String beanName, final String alias) {
    final BeanDefinition beanDefinition = createTargetBeanDefinition(
      (BeanDefinitionRegistry)beanFactory, beanName);
    if (beanDefinition != null) {
      registry.registerBeanDefinition(alias, beanDefinition);
    }
  }

  protected GenericBeanDefinition createTargetBeanDefinition(
    final BeanDefinitionRegistry beanFactory, final String beanName) {
    final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
    if (beanDefinition == null) {
      return null;
    } else {
      final boolean singleton = beanDefinition.isSingleton();
      final GenericBeanDefinition proxyBeanDefinition = new GenericBeanDefinition();
      proxyBeanDefinition.setBeanClass(TargetBeanFactoryBean.class);
      final MutablePropertyValues values = new MutablePropertyValues();
      final String beanClassName = beanDefinition.getBeanClassName();
      final PropertyValue beanDefinitionProperty = new PropertyValue("targetBeanDefinition", beanDefinition);
      beanDefinitionProperty.setConvertedValue(beanDefinition);
      values.addPropertyValue(beanDefinitionProperty);
      values.addPropertyValue("targetBeanName", beanName);
      values.addPropertyValue("targetBeanFactory", beanFactory);
      values.addPropertyValue("singleton", singleton);
      proxyBeanDefinition.setPropertyValues(values);
      return proxyBeanDefinition;
    }
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public void setExportAllBeans(final boolean exportAllBeans) {
    this.exportAllBeans = exportAllBeans;
  }

  public void setExportBeanAliases(final Map<String, String> exportBeanAliases) {
    this.exportBeanAliases = exportBeanAliases;
  }

  public void setExportBeanNames(final List<String> exportBeanNames) {
    this.exportBeanNames = exportBeanNames;
  }

  public void setParameters(final Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  public void setResource(final Resource resource) {
    this.resource = resource;
  }

  public void setResourceEditorRegistrar(
    final ResourceEditorRegistrar resourceEditorRegistrar) {
    this.resourceEditorRegistrar = resourceEditorRegistrar;
  }

}
