/*
 * Copyright 2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ancientprogramming.fixedformat4j.format.impl;

import com.ancientprogramming.fixedformat4j.exception.FixedFormatException;
import com.ancientprogramming.fixedformat4j.format.AbstractFixedFormatter;
import com.ancientprogramming.fixedformat4j.format.FormatInstructions;
import org.apache.commons.lang.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Formatter for {@link java.time.LocalDate} data.
 * The formatting and parsing is performed by using an instance of the {@link DateTimeFormatter} class.
 *
 * @author Harry Moreau http://www.sator.ie
 * @since 1.5.0
 */
public class LocalDateFormatter extends AbstractFixedFormatter<LocalDate> {

  public LocalDate asObject(String string, FormatInstructions instructions) throws FixedFormatException {
    LocalDate result = null;

    if (!StringUtils.isEmpty(string)) {
        result = LocalDate.parse(string, getFormatter(instructions.getFixedFormatPatternData().getPattern()));
    }
    return result;
  }

  public String asString(LocalDate date, FormatInstructions instructions) {
    String result = null;
    if (date != null) {
      result = getFormatter(instructions.getFixedFormatPatternData().getPattern()).format(date);
    }
    return result;
  }

  DateTimeFormatter getFormatter(String pattern) {
    return DateTimeFormatter.ofPattern(pattern);
  }
}
