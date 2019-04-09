package com.ancientprogramming.fixedformat4j.configuration;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ancientprogramming.fixedformat4j.annotation.Align;
import com.ancientprogramming.fixedformat4j.annotation.Field;
import com.ancientprogramming.fixedformat4j.annotation.FixedFormatPattern;

/**
 * A FixedFormatConfigurator defines the modifications to make to the fixed format annotations for a single property of a class.
 * The property name (fieldName) is required and the targetClass is also required.
 * 
 * Note that the use of this class is not thread safe because it manipulates annotations on a class
 * and not an instance of a class.  So you can only manipulate the class in series.
 * @author Harry Moreau
 *
 */
public class FixedFormatConfigurer {

  private Log logger = LogFactory.getLog(this.getClass());
  
  private String fieldName;
  private Class<?> targetClass;
  private Method getter;
  private Integer offset;
  private Integer length;
  private Align alignment;
  private Character paddingChar;
  private String pattern;
  
  /**
   * @param fieldName
   * @return a new {@link FixedFormatConfigurer} for the given fieldName.
   */
  public static FixedFormatConfigurer forField(String fieldName) {
    return new FixedFormatConfigurer(fieldName);
  }
  
  public static FixedFormatConfigurer forFieldWith(String fieldName, Map<String,String> properties) {
    return new FixedFormatConfigurer(fieldName).mapProperties(properties);
  }
  
  private FixedFormatConfigurer mapProperties(Map<String, String> properties) {
    properties.forEach((key, value) -> {
      switch(key.toLowerCase()) {
      case "offset":
        this.offset(Integer.parseInt(value));
        break;
      case "length":
        this.length(Integer.parseInt(value));
        break;
      case "alignment":
        String align = value.toLowerCase();
        if (align.equals("left")) {
          this.alignment(Align.LEFT);
        } else if (align.equals("right")) {
          this.alignment(Align.RIGHT);
        } else {
          logger.warn("Unknown alignment value for " + fieldName + ": " + alignment);
        }
        break;
      case "pattern":
        this.pattern(value);
        break;
      case "paddingchar":
        this.paddingChar(value.charAt(0));
        break;
      default:
        logger.warn("Unknown property " + key + " for field " + fieldName);
      }
    } );
    return this;
  }
  
  private static final String GET_PREFIX = "get";
  
  /**
   * Fluent way of setting the target class.
   * @param targetClass
   * @return the receiver object (this).
   */
  public FixedFormatConfigurer inClass(Class<?> targetClass) throws NoSuchMethodException {
    this.setTargetClass(targetClass);
    String methodName = GET_PREFIX + getFieldName().substring(0,1).toUpperCase() + getFieldName().substring(1);
    try {
      this.getter = targetClass.getMethod(methodName); // with no parameters
    } catch (NoSuchMethodException e) {
      logger.error("Unable to find method " + getTargetClass().getSimpleName() + "." + methodName + "()");
      throw e;
    }
    return this;
  }
  
  /**
   * Set offset fluently.
   * @param offset
   * @return the receiver object (this).
   */
  public FixedFormatConfigurer offset(Integer offset) {
    this.setOffset(offset);
    return this;
  }
  
  /**
   * Set the alignment fluently.
   * @param alignment
   * @return the receiver object (this).
   */
  public FixedFormatConfigurer alignment(Align alignment) {
    this.setAlignment(alignment);
    return this;
  }
  
  /**
   * Set the length fluently.
   * @param length
   * @return the receiver object (this).
   */
  public FixedFormatConfigurer length(Integer length) {
    this.setLength(length);
    return this;
  }
  
  /**
   * Set the padding character fluently. 
   * @param character
   * @return the receiver object (this).
   */
  public FixedFormatConfigurer paddingChar(Character character) {
    this.setPaddingChar(character);
    return this;
  }
  
  /**
   * Set the pattern fluently.
   * @param pattern
   * @return the receiver object (this).
   */
  public FixedFormatConfigurer pattern(String pattern) {
    this.setPattern(pattern);
    return this;
  }
  
  private FixedFormatConfigurer(String fieldName) {
    super();
    this.fieldName = fieldName;
  }
  
  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }
  
  public Integer getOffset() {
    return offset;
  }
  
  public void setOffset(Integer offset) {
    this.offset = offset;
  }
  
  public Integer getLength() {
    return length;
  }
  
  public void setLength(Integer length) {
    this.length = length;
  }
  
  public Align getAlignment() {
    return alignment;
  }
  
  public void setAlignment(Align alignment) {
    this.alignment = alignment;
  }
  
  public Character getPaddingChar() {
    return paddingChar;
  }
  
  public void setPaddingChar(Character paddingChar) {
    this.paddingChar = paddingChar;
  }
  
  public String getPattern() {
    return pattern;
  }
  
  public void setPattern(String pattern) {
    this.pattern = pattern;
  }
  
  
  public Class<?> getTargetClass() {
    return targetClass;
  }

  public void setTargetClass(Class<?> targetClass) {
    this.targetClass = targetClass;
  }

  private List<AnnotationConfigurator> annotationConfigurators = new ArrayList<AnnotationConfigurator>();
  
  /**
   * Apply the changes to the properties of the annotations, saving the original values so they can be set back again.
   * @return the receiver object (this).
   */
  public FixedFormatConfigurer apply() {
    if (getter == null) {
      throw new IllegalStateException("No class specified before apply() - try inClass()");
    }
    Annotation fieldAnnotation = getter.getAnnotation(Field.class);
    if (fieldAnnotation != null) {
      if (getOffset() != null) {
        annotationConfigurators.add(new AnnotationConfigurator(fieldAnnotation, "offset", getOffset()));
      }
      if (getLength() != null) {
        annotationConfigurators.add(new AnnotationConfigurator(fieldAnnotation, "length", getLength()));
      }
      if (getAlignment() != null) {
        annotationConfigurators.add(new AnnotationConfigurator(fieldAnnotation, "align", getAlignment()));
      }
      if (getPaddingChar() != null) {
        annotationConfigurators.add(new AnnotationConfigurator(fieldAnnotation, "paddingChar", getPaddingChar()));
      }
    } else {
      logger.warn("No @Field annotation found on method " + this.getter.toString());
    }
    if (getPattern() != null) {
      Annotation patternAnnotation = getter.getAnnotation(FixedFormatPattern.class);
      if (patternAnnotation == null) {
        logger.warn("No @FixedFormatPattern annotation found on method " + this.getter.toString());
      } else {
        annotationConfigurators.add(new AnnotationConfigurator(patternAnnotation, "value", getPattern()));
      }
    }
    annotationConfigurators.forEach(AnnotationConfigurator::applyChanges);
    return this;
  }
  
  public FixedFormatConfigurer reset() {
    annotationConfigurators.forEach(AnnotationConfigurator::reset);
    return this;
  }

  @Override
  public String toString() {
    StringWriter w = new StringWriter();
    w.append(this.getClass().getSimpleName());
    w.append("[");
    w.append("fieldName=" + this.getFieldName());
    if (this.getTargetClass() != null) {
      w.append(", targetClass=" + this.getTargetClass().getSimpleName());
    }
    // Append all the  non-null property values to the string representation...
    List<String> values = new ArrayList<>();
    if (this.getOffset() != null) {
      values.add("offset=" + this.getOffset().toString());
    }
    if (this.getLength() != null) {
      values.add("length=" + this.getLength().toString());
    }
    if (this.getAlignment() != null) {
      values.add("alignment=" + this.getAlignment().toString());
    }
    if (this.getPaddingChar() != null) {
      values.add("paddingChar=" + this.getPaddingChar().toString());
    }
    if (this.getPattern() != null) {
      values.add("pattern=" + this.getPattern());
    }
    values.forEach(v -> {
      w.append(", ");
      w.append(v);
    });
    w.append("]");
    return w.toString();
    
  }
}
