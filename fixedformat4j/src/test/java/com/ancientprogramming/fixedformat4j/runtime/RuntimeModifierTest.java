package com.ancientprogramming.fixedformat4j.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ancientprogramming.fixedformat4j.annotation.Align;
import com.ancientprogramming.fixedformat4j.annotation.Field;
import com.ancientprogramming.fixedformat4j.annotation.FixedFormatPattern;
import com.ancientprogramming.fixedformat4j.annotation.Record;
import com.ancientprogramming.fixedformat4j.configuration.FixedFormatFieldConfigurer;
import com.ancientprogramming.fixedformat4j.format.FixedFormatManager;
import com.ancientprogramming.fixedformat4j.format.impl.FixedFormatManagerImpl;

import junit.framework.TestCase;

public class RuntimeModifierTest extends TestCase {

  private Log logger = LogFactory.getLog(RuntimeModifierTest.class);
  @Record
  public class BasicRecord {
    private String stringData;
    private Integer integerData;
    private LocalDate dateData;
    
    @Field(offset=1, length=20, align=Align.LEFT)
    public String getStringData() {
      return this.stringData;
    }
    
    public void setStringData(String stringData) {
      this.stringData = stringData;
    }
    
    @Field(offset=21, length=5, align=Align.RIGHT, paddingChar='0')
    public Integer getIntegerData() {
      return this.integerData;
    }
    
    public void setIntegerData(Integer integerData) {
      this.integerData = integerData;
    }
    
    @Field(offset=26, length=10)
    @FixedFormatPattern("dd/MM/yyyy")
    public LocalDate getDateData() {
      return this.dateData;
    }
    
    public void setDateData(LocalDate dateData) {
      this.dateData = dateData;
    }
  }
  
  private FixedFormatManager ffm = new FixedFormatManagerImpl();
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  public void testAnnotationOnClass() {
    Class<BasicRecord> basicRecordclazz = BasicRecord.class;
    Annotation[] annotations = basicRecordclazz.getAnnotations();
    assertTrue("Found no annotations in class", annotations.length > 0);
    Arrays.asList(annotations).forEach(a -> System.out.println(a.annotationType().getName()));
  }
  
  private static final String FRECORD = "123456789012345678900022717/06/2019";
  
  public void testChangeAnnotationsOnMethods() {
    Class<BasicRecord> basicRecordclazz = BasicRecord.class;
    try {
      Method m = basicRecordclazz.getMethod("getStringData",  new Class<?>[] {});
      Field fAnnotation = m.getAnnotation(Field.class);
      changeAnnotationValue(fAnnotation, "length", (Integer) 10); 
      assertTrue("Did not update the length", fAnnotation.length() == 10);
      BasicRecord record = ffm.load(BasicRecord.class, FRECORD);
      assertTrue("string data too long", record.getStringData().length() <= 10);
      changeAnnotationValue(fAnnotation, "length", (Integer) 20);
    } catch (NoSuchMethodException e) {
      fail("Couldn't find getStringData method");
    }
    
  }
  
  public void testChangeAnnotationLengthToZero() {
    try {
      Field fAnnotation = BasicRecord.class.getMethod("getStringData", new Class<?>[] {}).getAnnotation(Field.class);
      changeAnnotationValue(fAnnotation, "length", (Integer) 0);
      assertTrue("Did not set field length to 0", fAnnotation.length() == 0);
      BasicRecord record = ffm.load(BasicRecord.class, FRECORD);
      assertTrue("string data should not be there at all", record.getStringData() == null || record.getStringData().length() == 0);
      changeAnnotationValue(fAnnotation, "length", (Integer) 20);
    } catch (Exception e) {
      fail("Caught unexpected exception " + e.getMessage());
    }
  }
  
  public void testCanOverlapFields() {
    try {
      Field fAnnotation = BasicRecord.class.getMethod("getIntegerData", new Class<?>[] {}).getAnnotation(Field.class);
      Object savedValue = changeAnnotationValue(fAnnotation, "offset", (Integer) 1);
      assertTrue("Did not set field offset to 1", fAnnotation.offset() == 1);
      BasicRecord record = ffm.load(BasicRecord.class, FRECORD);
      assertTrue("Did not read overlapped integer field", record.getIntegerData() == 12345);
      assertTrue("Did not read overlapped string field", record.getStringData().equals("12345678901234567890"));
      changeAnnotationValue(fAnnotation, "offset", savedValue);
    } catch (Exception e) {
      fail("Caught unexpected exception " + e.getMessage());
    }
  }
  
  public void testFixedFormatFieldConfigurerAndReset() {
    try {
      FixedFormatFieldConfigurer ffc = 
        FixedFormatFieldConfigurer.forField("integerData")
          .inClass(BasicRecord.class)
          .offset(25)
          .length(10)
          .alignment(Align.LEFT)
          .paddingChar('#')
          .apply();
      Field fAnnotation = BasicRecord.class.getMethod("getIntegerData").getAnnotation(Field.class);
      assertTrue("Offset isn't 25", fAnnotation.offset() == 25);
      assertTrue("Length isn't 10", fAnnotation.length() == 10);
      assertTrue("Alignment isn't LEFT", fAnnotation.align().equals(Align.LEFT));
      assertTrue("Padding character isn't '#'", fAnnotation.paddingChar() == '#');
      ffc.reset();
      assertFalse("Offset is still 25", fAnnotation.offset() == 25);
      assertFalse("Length is still 10", fAnnotation.length() == 10);
      assertFalse("Alignment is still LEFT", fAnnotation.align().equals(Align.LEFT));
      assertFalse("Padding character is still '#'", fAnnotation.paddingChar() == '#');
    } catch (Exception e) {
      fail("Caught unexpected exception " + e.getMessage());
    }
  }
  
  public void testFixedFormatFieldConfiguratorWithMap() {
    try {
      Map<String,String> pMap = new HashMap<>();
      pMap.put("length", "10");
      pMap.put("offset", "25");
      pMap.put("align", "LEFT");
      pMap.put("paddingChar", "#");
      FixedFormatFieldConfigurer ffc = 
        FixedFormatFieldConfigurer.forFieldWith("integerData", pMap)
          .inClass(BasicRecord.class)
          .apply();
      Field fAnnotation = BasicRecord.class.getMethod("getIntegerData").getAnnotation(Field.class);
      assertTrue("Offset isn't 25", fAnnotation.offset() == 25);
      assertTrue("Length isn't 10", fAnnotation.length() == 10);
      assertTrue("Alignment isn't LEFT", fAnnotation.align().equals(Align.LEFT));
      assertTrue("Padding character isn't '#'", fAnnotation.paddingChar() == '#');
      ffc.reset();
    } catch (Exception e) {
      fail("Caught unexpected exception " + e.getMessage());
    }
  }
  
  public void testFixedFormatFieldConfiguratorWithMapBadLength() {
    try {
      Map<String,String> pMap = new HashMap<>();
      pMap.put("length", "quite long");
      FixedFormatFieldConfigurer.forFieldWith("integerData", pMap)
        .inClass(BasicRecord.class)
        .apply();
      fail("Should not have accepted a string for an integer in length");
    } catch (NumberFormatException e) {
      logger.info("Caught expected exception");
    } catch (Exception e) {
      fail("Caught unexpected exception " + e.getMessage());
    }
  }
  
  public void testFixedFormatFieldConfiguratorWithMapBadOffset() {
    try {
      Map<String,String> pMap = new HashMap<>();
      pMap.put("offset", "over there");
      FixedFormatFieldConfigurer.forFieldWith("integerData", pMap)
        .inClass(BasicRecord.class)
        .apply();
      fail("Should not have accepted a string for an integer in length");
    } catch (NumberFormatException e) {
      logger.info("Caught expected exception");
    } catch (Exception e) {
      fail("Caught unexpected exception " + e.getMessage());
    }
  }
  
  public void testFixedFormatFieldConfiguratorWithBadAlignment() {
    try {
      Map<String,String> pMap = new HashMap<>();
      pMap.put("align", "top");
      FixedFormatFieldConfigurer ffc = 
        FixedFormatFieldConfigurer.forFieldWith("integerData", pMap)
          .inClass(BasicRecord.class)
          .apply();
      Field fAnnotation = BasicRecord.class.getMethod("getIntegerData").getAnnotation(Field.class);
      assertTrue("Offset isn't 21", fAnnotation.offset() == 21);
      assertTrue("Length isn't 5", fAnnotation.length() == 5);
      assertTrue("Alignment isn't RIGHT", fAnnotation.align().equals(Align.RIGHT));
      ffc.reset();
    } catch (Exception e) {
      fail("Caught unexpected exception " + e.getMessage());
    }
  }

  public void testFixedFormatFieldConfiguratorWithBadPropertyInMap() {
    try {
      Map<String,String> pMap = new HashMap<>();
      pMap.put("paddington", "bear");
      FixedFormatFieldConfigurer ffc = 
        FixedFormatFieldConfigurer.forFieldWith("integerData", pMap)
          .inClass(BasicRecord.class)
          .apply();
      Field fAnnotation = BasicRecord.class.getMethod("getIntegerData").getAnnotation(Field.class);
      assertTrue("Offset isn't 21", fAnnotation.offset() == 21);
      assertTrue("Length isn't 5", fAnnotation.length() == 5);
      assertTrue("Alignment isn't RIGHT", fAnnotation.align().equals(Align.RIGHT));
      ffc.reset();
    } catch (Exception e) {
      fail("Caught unexpected exception " + e.getMessage());
    }
  }

  public void testFixedFormatFieldConfiguratorErrorNoClass() {
    try {
      FixedFormatFieldConfigurer.forField("stringData")
        .offset(10)
        .length(5)
        .apply();
    } catch (IllegalStateException e) {
      logger.info("Caught expected exception");
    } catch (Exception e) {
      fail("Caught unexpected exception " + e.getMessage());
    }
  }

  /**
   * Changes the annotation value for the given key of the given annotation to newValue and returns
   * the previous value.
   */
  @SuppressWarnings("unchecked")
  public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue){
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
      if (oldValue == null) {
        return null;
      }
      if (oldValue.getClass() != newValue.getClass()) {
          throw new IllegalArgumentException();
      }
      memberValues.put(key,newValue);
      return oldValue;
  }
  
  
}
