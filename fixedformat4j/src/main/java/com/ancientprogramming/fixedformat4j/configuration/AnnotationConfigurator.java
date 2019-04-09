package com.ancientprogramming.fixedformat4j.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Alter a single annotation's "key" value and save the current value so we can reset it later.
 * @author MoreaHa
 *
 */
public class AnnotationConfigurator {

  private Annotation annotation;
  private String key;
  private Object newValue;
  private Object savedValue;
  
  /**
   * Create an AnnotationConfigurator to set and save a single property of the annotation.
   * @param annotation
   * @param key
   * @param newValue
   */
  public AnnotationConfigurator(Annotation annotation, String key, Object newValue) {
    super();
    this.annotation = annotation;
    this.key = key;
    this.newValue = newValue;
  }
  
  public AnnotationConfigurator applyChanges() {
    this.savedValue = setAnnotationValue(this.newValue);
    return this;
  }
  
  public AnnotationConfigurator reset() {
    if (savedValue != null) {
      setAnnotationValue(savedValue);
    }
    return this;
  }

  /**
   * Changes the annotation value to newValue and returns
   * the previous value.
   */
  @SuppressWarnings("unchecked")
  private Object setAnnotationValue(Object newValue) {
      Object handler = Proxy.getInvocationHandler(annotation);
      java.lang.reflect.Field f;
      try {
          f = handler.getClass().getDeclaredField("memberValues");
      } catch (NoSuchFieldException | SecurityException e) {
          throw new IllegalStateException(e);
      }
      f.setAccessible(true);
      Map<String, Object> memberValues;
      try {
          memberValues = (Map<String, Object>) f.get(handler);
      } catch (IllegalArgumentException | IllegalAccessException e) {
          throw new IllegalStateException(e);
      }
      Object oldValue = memberValues.get(key);
      memberValues.put(key, newValue);
      return oldValue;
  }

  public String toString() {
    return "AnnotationConfigurator[annotation=" + annotation.toString() + "]";
  }
}
